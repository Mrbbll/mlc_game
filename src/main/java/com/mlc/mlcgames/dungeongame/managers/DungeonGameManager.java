package com.mlc.mlcgames.dungeongame.managers;

import com.mlc.mlcgames.dungeongame.Dungeongame;
import com.mlc.mlcgames.dungeongame.listener.DungeonRoomListener;
import com.mlc.mlcgames.dungeongame.mobs.CraftEngineMobEquipment;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
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
    private final DungeonPartyService party;
    private final DungeonPlayerLifeService lives;
    private final Sidebarmanager sidebar;
    private DungeonRoomListener runtimeListener;

    private DungeonGameManager(JavaPlugin plugin) {
        this.plugin = plugin;
        Random random = new Random();
        session = new DungeonSession();
        party = new DungeonPartyService();
        lives = new DungeonPlayerLifeService(plugin, party, this::endGame);
        sidebar = new Sidebarmanager(plugin);

        progression = new DungeonProgressionService(session, new DungeonProgressionService.Listener() {
            @Override
            public void onStageEntered(Player player) {
                // “进入下一层”是统一复活边界。复活传送本身不触发新房间；玩家必须在
                // 落地后实际跨方块走入尚未激活的房间，才会启动遭遇并集结队友。
                lives.reviveAtNextStage(player.getLocation());
            }

            @Override
            public void onVictory(Player player) {
                finishWithVictory(player);
            }
        });

        CraftEngineMobEquipment equipment = new CraftEngineMobEquipment(plugin);
        mobRegistry = new DungeonMobRegistry(plugin);
        DungeonItemResolver itemResolver = new DungeonItemResolver(plugin);
        DungeonMobService mobs = new DungeonMobService(plugin, random, session, equipment, itemResolver);
        DungeonMobDropService drops = new DungeonMobDropService(itemResolver);
        DungeonWaveService waves = new DungeonWaveService(plugin, session, mobs);
        DungeonLootChestService loot = new DungeonLootChestService(plugin, random);
        encounters = new DungeonEncounterService(session, waves, loot, progression, drops, party);
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
        sidebar.reloadConfiguration();
        return mobRegistry.reload();
    }

    /** 菜单只向准备队和正式地牢队开放。 */
    public boolean canOpenMenu(Player player) {
        return party.canAccessLobby(player);
    }

    /** 创建干净世界并开始分批生成全部十五层。 */
    public void startGame(Player owner) {
        if (isRunningOrGenerating()) {
            owner.sendMessage("§c地牢游戏已经开始或正在生成。");
            return;
        }
        if (!party.canAccessLobby(owner)) {
            owner.sendMessage("§c只有 dungeongame_prepareteam 或 dungeongame_team 的玩家才能开始地牢。");
            return;
        }

        try {
            int promoted = party.promotePreparedPlayers();
            enableRuntimeListener();
            clearRuntimeState();
            Dungeongame.lastGameWon = false;
            Dungeongame.lastWinner = null;
            Dungeongame.currentLevel = 1;
            Dungeongame.currentFloor = 1;
            Worldmanager.createDungeonWorld();
            owner.sendMessage("§e已将 " + promoted + " 名准备队玩家加入地牢队伍；"
                    + "开始生成 3 关 × 每关 5 层地牢，请稍候……");
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
        // 先停止接收地牢事件，避免撤离传送或清理实体时重新推进房间状态。
        disableRuntimeListener();
        generation.cancel();
        sidebar.stopAndRestore();
        lives.cleanup();
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

    /** 将一名在线地牢队员补进当前局并送到第一关第一层。 */
    public void enter(Player player) {
        if (!Dungeongame.isstart || session.stages.isEmpty()) {
            player.sendMessage(generation.isGenerating()
                    ? "§e地牢仍在生成，请稍候。" : "§c地牢游戏尚未开始。");
            return;
        }

        if (!party.isMember(player)) {
            player.sendMessage("§c你不在 dungeongame_team 队伍中，无法进入当前地牢。");
            return;
        }

        Dungeongame.currentLevel = 1;
        Dungeongame.currentFloor = 1;
        if (!party.enterMember(player, session.stages.getFirst().start)) {
            player.sendMessage("§c进入地牢失败，请稍后重试。");
            return;
        }
        sidebar.show(player);
        player.sendMessage("§a进入地牢第 1 关，第 1 层。");
    }

    /**
     * 玩家主动跨方块移动时的统一入口：传送门优先于房间激活，避免一次事件执行两条流程。
     * 此方法不得由 PlayerTeleportEvent 或传送完成回调调用，否则任意传送都会被误判成进房。
     */
    public void handlePlayerPosition(Player player) {
        if (!Dungeongame.isstart) return;
        if (player.getWorld() != Worldmanager.dungeonWorld) {
            sidebar.hide(player);
            return;
        }
        // 非队员即使因管理员传送等原因进入地牢世界，也不能推进关卡或触发刷怪。
        if (!party.isMember(player)) {
            sidebar.hide(player);
            return;
        }
        sidebar.show(player);
        // 阵亡旁观者只能等待跨层复活，不能通过飞行替队伍触发传送点或新遭遇。
        if (lives.isAwaitingRevival(player)) return;
        Dungeongame.participants.add(player);
        if (progression.handlePlayerPosition(player)) return;
        encounters.handlePlayerPosition(player);
    }

    /**
     * 传送完成后只同步参与者与私人侧边栏，不执行层级推进和房间遭遇检测。
     * 延迟一 tick 是为了读取 Bukkit 已经提交的最终世界；队伍集结传送也安全复用此入口。
     */
    public void handlePlayerTeleport(Player player) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (!Dungeongame.isstart) return;
            if (player.getWorld() != Worldmanager.dungeonWorld || !party.isMember(player)) {
                sidebar.hide(player);
                return;
            }
            Dungeongame.participants.add(player);
            sidebar.show(player);
        });
    }

    /** 实体死亡监听器入口；归属判断与清房结算由遭遇服务完成。 */
    public void handleMonsterDeath(EntityDeathEvent event) {
        encounters.handleMonsterDeath(event);
    }

    /** 玩家死亡监听入口；旁观、复活和团灭延时均由生命服务管理。 */
    public void handlePlayerDeath(PlayerDeathEvent event) {
        lives.handleDeath(event);
    }

    /** 自动重生时修正位置，并在事件结束后的下一 tick 应用旁观模式。 */
    public void handlePlayerRespawn(PlayerRespawnEvent event) {
        lives.handleRespawn(event);
    }

    /** @deprecated Bukkit 监听器应传入完整死亡事件，才能应用地牢专属掉落规则。 */
    @Deprecated(forRemoval = false)
    public void handleMonsterDeath(UUID uuid) {
        encounters.handleMonsterDeath(uuid);
    }

    private void finishGeneration(Player owner) {
        progression.initializePortals();
        Dungeongame.isstart = true;
        sidebar.start();
        var entered = party.enterOnlineTeam(session.stages.getFirst().start);
        for (Player player : entered) {
            sidebar.show(player);
        }

        if (owner.isOnline()) {
            owner.sendMessage("§a已在同一世界生成全部 15 个地牢，共 "
                    + session.generatedRoomCount + " 个房间；已传送 " + entered.size() + " 名在线队员。");
        }
        if (entered.isEmpty()) {
            Bukkit.broadcastMessage("§a全部 15 个地牢已生成，在线队员可使用 /dungeongame enter 加入。");
        }
    }

    private void abortGeneration(Player owner, RuntimeException exception) {
        disableRuntimeListener();
        generation.cancel();
        Dungeongame.isstart = false;
        sidebar.stopAndRestore();
        lives.cleanup();
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

    /** 除菜单外的地牢事件只在一局开始后存在，结束或失败时立即注销。 */
    private void enableRuntimeListener() {
        if (runtimeListener != null) return;
        runtimeListener = new DungeonRoomListener(this);
        Bukkit.getPluginManager().registerEvents(runtimeListener, plugin);
    }

    private void disableRuntimeListener() {
        if (runtimeListener == null) return;
        HandlerList.unregisterAll(runtimeListener);
        runtimeListener = null;
    }
}
