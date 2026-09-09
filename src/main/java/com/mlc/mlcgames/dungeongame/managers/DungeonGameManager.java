package com.mlc.mlcgames.dungeongame.managers;

import com.mlc.mlcgames.dungeongame.Dungeongame;
import com.mlc.mlcgames.dungeongame.mobs.CraftEngineMobEquipment;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;

/**
 * 地牢模块的对外门面。
 *
 * <p>命令、菜单和 Bukkit 监听器只依赖这个类；具体工作分别交给生成、遭遇、怪物、
 * 推进和战利品服务。这样外部 API 保持稳定，同时每个内部类只有一个清晰职责。</p>
 */
public final class DungeonGameManager {
    public static final int DUNGEON_LEVELS = DungeonSession.DUNGEON_LEVELS;
    public static final int FLOORS_PER_LEVEL = DungeonSession.FLOORS_PER_LEVEL;

    private static DungeonGameManager instance;

    private final JavaPlugin plugin;
    private final DungeonSession session;
    private final DungeonGenerationService generation;
    private final DungeonProgressionService progression;
    private final DungeonEncounterService encounters;
    private final DungeonMobRegistry mobRegistry;

    private DungeonGameManager(JavaPlugin plugin) {
        this.plugin = plugin;
        Random random = new Random();
        session = new DungeonSession();

        progression = new DungeonProgressionService(session, new DungeonProgressionService.Listener() {
            @Override
            public void onStageEntered(Player player) {
                // 传送完成后的下一 tick 再检测，确保 Bukkit 已更新玩家所在区块与坐标。
                Bukkit.getScheduler().runTask(plugin, () -> handlePlayerPosition(player));
            }

            @Override
            public void onVictory(Player player) {
                finishWithVictory(player);
            }
        });

        CraftEngineMobEquipment equipment = new CraftEngineMobEquipment(plugin);
        mobRegistry = new DungeonMobRegistry(plugin);
        DungeonMobService mobs = new DungeonMobService(plugin, random, session, equipment);
        DungeonWaveService waves = new DungeonWaveService(plugin, session, mobs);
        DungeonLootChestService loot = new DungeonLootChestService(plugin, random);
        encounters = new DungeonEncounterService(session, waves, loot, progression);
        generation = new DungeonGenerationService(plugin, session, random, mobRegistry);
    }

    /** 初始化唯一实例；由插件启动流程调用一次。 */
    public static DungeonGameManager initialize(JavaPlugin plugin) {
        if (instance == null) instance = new DungeonGameManager(plugin);
        return instance;
    }

    /** 获取已初始化的门面，供命令和菜单使用。 */
    public static DungeonGameManager get() {
        if (instance == null) {
            throw new IllegalStateException("DungeonGameManager is not initialized");
        }
        return instance;
    }

    /** 插件关闭钩子：只在实例存在时清理，避免初始化失败后的二次异常。 */
    public static void shutdownIfInitialized() {
        if (instance != null && instance.isRunningOrGenerating()) instance.endGame();
    }

    public boolean isRunningOrGenerating() {
        return Dungeongame.isstart || generation.isGenerating();
    }

    public String status() {
        if (generation.isGenerating()) {
            return "正在生成 " + session.stages.size() + "/" + DungeonSession.TOTAL_STAGES + " 个地牢";
        }
        if (!Dungeongame.isstart) return "未开始";
        return "运行中，共 " + session.stages.size() + " 个地牢、"
                + session.generatedRoomCount + " 个房间";
    }

    /** 事务式重载环境怪物文件；已经生成的房间继续使用开局时保存的配置快照。 */
    public int reloadMobDefinitions() {
        return mobRegistry.reload();
    }

    /** 创建干净世界并开始分批生成全部十五层。 */
    public void startGame(Player owner) {
        if (isRunningOrGenerating()) {
            owner.sendMessage("§c地牢游戏已经开始或正在生成。");
            return;
        }

        try {
            clearRuntimeState();
            Dungeongame.lastGameWon = false;
            Dungeongame.lastWinner = null;
            Dungeongame.currentLevel = 1;
            Dungeongame.currentFloor = 1;
            Worldmanager.createDungeonWorld();
            owner.sendMessage("§e开始生成 3 关 × 每关 5 层地牢，请稍候……");
            generation.start(owner, new DungeonGenerationService.Listener() {
                @Override
                public void onCompleted(Player completedOwner) {
                    finishGeneration(completedOwner);
                }

                @Override
                public void onFailed(Player failedOwner, RuntimeException exception) {
                    abortGeneration(failedOwner, exception);
                }
            });
        } catch (RuntimeException exception) {
            abortGeneration(owner, exception);
        }
    }

    /**
     * 结束生成或运行中的游戏。此方法是幂等的，可由菜单、命令、胜利流程或异常清理调用。
     */
    public void endGame() {
        generation.cancel();
        encounters.cleanup();

        // 必须先把玩家送离地牢世界，之后该世界才可能在下一局安全卸载和删除。
        World fallback = Bukkit.getWorlds().stream()
                .filter(world -> world != Worldmanager.dungeonWorld)
                .findFirst()
                .orElse(null);
        if (fallback != null) {
            for (Player player : new ArrayList<>(Dungeongame.participants)) {
                if (player.isOnline()) player.teleport(fallback.getSpawnLocation());
            }
        }

        clearRuntimeState();
        Dungeongame.isstart = false;
    }

    /** 将玩家加入当前局并送到第一关第一层。 */
    public void enter(Player player) {
        if (!Dungeongame.isstart || session.stages.isEmpty()) {
            player.sendMessage(generation.isGenerating()
                    ? "§e地牢仍在生成，请稍候。" : "§c地牢游戏尚未开始。");
            return;
        }

        Dungeongame.participants.add(player);
        Dungeongame.currentLevel = 1;
        Dungeongame.currentFloor = 1;
        player.teleport(session.stages.getFirst().start);
        player.sendMessage("§a进入地牢第 1 关，第 1 层。");
        Bukkit.getScheduler().runTask(plugin, () -> handlePlayerPosition(player));
    }

    /** 玩家跨方块移动时的统一入口：传送门优先于房间激活，避免一次事件执行两条流程。 */
    public void handlePlayerPosition(Player player) {
        if (!Dungeongame.isstart || player.getWorld() != Worldmanager.dungeonWorld) return;
        if (progression.handlePlayerPosition(player)) return;
        encounters.handlePlayerPosition(player);
    }

    /** 实体死亡监听器入口；归属判断与清房结算由遭遇服务完成。 */
    public void handleMonsterDeath(UUID uuid) {
        encounters.handleMonsterDeath(uuid);
    }

    private void finishGeneration(Player owner) {
        progression.initializePortals();
        Dungeongame.isstart = true;
        if (owner.isOnline()) {
            enter(owner);
            owner.sendMessage("§a已在同一世界生成全部 15 个地牢，共 "
                    + session.generatedRoomCount + " 个房间。");
        } else {
            Bukkit.broadcastMessage("§a全部 15 个地牢已生成，可以使用 /dungeongame enter 加入。");
        }
    }

    private void abortGeneration(Player owner, RuntimeException exception) {
        generation.cancel();
        Dungeongame.isstart = false;
        encounters.cleanup();
        clearRuntimeState();
        plugin.getLogger().severe("Dungeon generation failed: " + exception.getMessage());
        if (owner != null && owner.isOnline()) {
            owner.sendMessage("§c地牢生成失败：" + exception.getMessage());
        }
    }

    private void finishWithVictory(Player player) {
        Dungeongame.lastGameWon = true;
        Dungeongame.lastWinner = player.getUniqueId();
        Bukkit.broadcastMessage("§6" + player.getName() + " 完成了全部 3×5 个地牢，游戏胜利！");
        endGame();
    }

    private void clearRuntimeState() {
        session.clear();
        Dungeongame.participants.clear();
    }
}
