package com.mlc.mlcgames.dungeongame.managers;

import com.mlc.mlcgames.dungeongame.Dungeongame;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * 管理地牢玩家的死亡、旁观、跨层复活和团灭结束。
 *
 * <p>怪物死亡仍由遭遇服务处理；这里仅接管已经进入当前局的玩家。
 * 死亡玩家会自动完成 Bukkit 重生并进入旁观模式，避免停留在死亡界面而错过下一层复活。</p>
 */
final class DungeonPlayerLifeService {
    private static final long PARTY_WIPE_DELAY_TICKS = 5L * 20L;

    private final JavaPlugin plugin;
    private final DungeonPartyService party;
    private final Runnable onPartyWiped;
    private final Set<UUID> awaitingRevival = new HashSet<>();
    private final Map<UUID, Location> deathLocations = new HashMap<>();
    private BukkitTask partyWipeTask;

    DungeonPlayerLifeService(JavaPlugin plugin, DungeonPartyService party, Runnable onPartyWiped) {
        this.plugin = plugin;
        this.party = party;
        this.onPartyWiped = onPartyWiped;
    }

    /** 仅处理当前地牢世界中、本局已登记参与者的死亡。 */
    void handleDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        if (!Dungeongame.isstart
                || player.getWorld() != Worldmanager.dungeonWorld
                || !Dungeongame.participants.contains(player)
                || !party.isMember(player)
                || !awaitingRevival.add(player.getUniqueId())) {
            return;
        }

        deathLocations.put(player.getUniqueId(), player.getLocation().clone());
        Player livingTeammate = party.findLivingTeammate(player, awaitingRevival);
        requestSpectatorRespawn(player);

        if (livingTeammate != null) {
            player.sendMessage("§e你已阵亡，将以旁观模式等待队友进入下一层后复活。");
            livingTeammate.sendMessage("§e" + player.getName() + " 已阵亡，进入下一层可将其复活。");
            return;
        }

        player.sendMessage("§c队伍已无可战斗成员，地牢将在 5 秒后结束。");
        schedulePartyWipe();
    }

    /** 把地牢死亡玩家放回死亡位置，并在下一 tick 设置自由旁观状态。 */
    void handleRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        if (!awaitingRevival.contains(player.getUniqueId())) return;

        Location location = deathLocations.get(player.getUniqueId());
        if (location != null) event.setRespawnLocation(location);
        Bukkit.getScheduler().runTask(plugin, () -> applySpectatorMode(player));
    }

    boolean isAwaitingRevival(Player player) {
        return awaitingRevival.contains(player.getUniqueId());
    }

    /**
     * 任一存活队员进入下一层时，复活全部在线等待者并传送到该层起点。
     * 返回成功复活的玩家，Manager 会在下一 tick 为他们执行房间位置检测。
     */
    List<Player> reviveAtNextStage(Location stageStart) {
        List<Player> revived = new ArrayList<>();
        for (UUID uuid : new ArrayList<>(awaitingRevival)) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null || !player.isOnline()) continue;

            if (player.isDead()) player.spigot().respawn();
            if (player.getGameMode() == GameMode.SPECTATOR) player.setSpectatorTarget(null);
            player.setGameMode(GameMode.SURVIVAL);
            if (!player.teleport(stageStart.clone())) {
                // 传送被其他插件取消时继续保持等待状态，避免玩家以生存模式留在旧层。
                player.setGameMode(GameMode.SPECTATOR);
                continue;
            }

            awaitingRevival.remove(uuid);
            deathLocations.remove(uuid);
            Dungeongame.participants.add(player);
            player.sendMessage("§a队友已进入下一层，你已复活并回到队伍中。");
            revived.add(player);
        }
        if (!revived.isEmpty()) cancelPartyWipeTask();
        return List.copyOf(revived);
    }

    /** 结束或生成失败时取消延时，并恢复仍处于旁观等待状态的在线玩家。 */
    void cleanup() {
        cancelPartyWipeTask();
        for (UUID uuid : awaitingRevival) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null || !player.isOnline()) continue;
            if (player.isDead()) player.spigot().respawn();
            if (player.getGameMode() == GameMode.SPECTATOR) player.setSpectatorTarget(null);
            player.setGameMode(GameMode.SURVIVAL);
        }
        awaitingRevival.clear();
        deathLocations.clear();
    }

    private void requestSpectatorRespawn(Player player) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (!awaitingRevival.contains(player.getUniqueId()) || !player.isOnline()) return;
            if (player.isDead()) {
                // Spigot respawn 会触发 PlayerRespawnEvent，由该事件完成位置和模式设置。
                player.spigot().respawn();
            } else {
                applySpectatorMode(player);
            }
        });
    }

    private void applySpectatorMode(Player player) {
        if (!awaitingRevival.contains(player.getUniqueId()) || !player.isOnline()) return;
        player.setGameMode(GameMode.SPECTATOR);
    }

    private void schedulePartyWipe() {
        if (partyWipeTask != null) return;
        partyWipeTask = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            partyWipeTask = null;
            if (!Dungeongame.isstart) return;
            Bukkit.broadcastMessage("§c地牢队伍已全灭，游戏结束。");
            onPartyWiped.run();
        }, PARTY_WIPE_DELAY_TICKS);
    }

    private void cancelPartyWipeTask() {
        if (partyWipeTask == null) return;
        partyWipeTask.cancel();
        partyWipeTask = null;
    }
}
