package com.mlc.mlcgames.dungeongame.managers;

import com.mlc.mlcgames.Mlcgames;
import com.mlc.mlcgames.dungeongame.Dungeongame;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * 控制一间房的顺序波次。当前波的受控怪物全部死亡后才会调度下一波；
 * 房门在所有波次结束前始终保持关闭。
 */
final class DungeonWaveService {
    private final JavaPlugin plugin;
    private final DungeonSession session;
    private final DungeonMobService mobs;
    private final Map<DungeonSession.Encounter, BukkitTask> pendingTasks = new HashMap<>();

    DungeonWaveService(JavaPlugin plugin, DungeonSession session, DungeonMobService mobs) {
        this.plugin = plugin;
        this.session = session;
        this.mobs = mobs;
    }

    void start(DungeonSession.Encounter encounter, int totalWaves,
               Consumer<DungeonSession.Encounter> onAllWavesCleared) {
        encounter.currentWave = 0;
        encounter.totalWaves = Math.max(1, totalWaves);
        spawnNext(encounter, onAllWavesCleared);
    }

    /** 由最后一只怪物的死亡事件调用；其余死亡事件不会推进波次。 */
    void onCurrentWaveCleared(DungeonSession.Encounter encounter,
                              Consumer<DungeonSession.Encounter> onAllWavesCleared) {
        if (encounter.state != DungeonSession.EncounterState.ACTIVE
                || !encounter.monsters.isEmpty()
                || pendingTasks.containsKey(encounter)) {
            return;
        }
        if (encounter.currentWave >= encounter.totalWaves) {
            onAllWavesCleared.accept(encounter);
            return;
        }

        long delay = Math.max(1L, Mlcgames.dungeonConfiguration.getLong(
                "encounters.next-wave-delay-ticks", 40L));
        BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            pendingTasks.remove(encounter);
            if (encounter.state == DungeonSession.EncounterState.ACTIVE) {
                spawnNext(encounter, onAllWavesCleared);
            }
        }, delay);
        pendingTasks.put(encounter, task);
    }

    /** 结束游戏时必须取消等待中的下一波，否则旧会话可能在新世界中继续刷怪。 */
    void cancelAll() {
        for (BukkitTask task : pendingTasks.values()) task.cancel();
        pendingTasks.clear();
    }

    private void spawnNext(DungeonSession.Encounter encounter,
                           Consumer<DungeonSession.Encounter> onAllWavesCleared) {
        encounter.currentWave++;
        announce(encounter);
        for (DungeonSession.SpawnMarker marker : encounter.markers) {
            LivingEntity living = mobs.spawn(encounter, marker);
            if (living == null) continue;
            encounter.monsters.add(living.getUniqueId());
            session.monsterOwners.put(living.getUniqueId(), encounter);
        }
        // 配置实体全部生成失败时也要继续推进，不能把玩家永久锁在房内。
        if (encounter.monsters.isEmpty()) {
            onCurrentWaveCleared(encounter, onAllWavesCleared);
        }
    }

    private static void announce(DungeonSession.Encounter encounter) {
        String message = "§e第 " + encounter.currentWave + "/" + encounter.totalWaves + " 波";
        for (Player player : Dungeongame.participants) {
            if (player.isOnline() && encounter.bounds.contains(player.getLocation())) {
                player.sendActionBar(message);
            }
        }
    }
}
