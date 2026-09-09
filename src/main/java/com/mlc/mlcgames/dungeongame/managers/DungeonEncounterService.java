package com.mlc.mlcgames.dungeongame.managers;

import com.mlc.mlcgames.dungeongame.Dungeongame;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.UUID;

/**
 * 管理房间遭遇的状态机：进入房间、封门、登记怪物、清房、开门和生成奖励。
 * 怪物的具体构造与传送门实现分别委托给独立服务。
 */
final class DungeonEncounterService {
    private final DungeonSession session;
    private final DungeonWaveService waves;
    private final DungeonLootChestService loot;
    private final DungeonProgressionService progression;

    DungeonEncounterService(DungeonSession session, DungeonWaveService waves,
                            DungeonLootChestService loot, DungeonProgressionService progression) {
        this.session = session;
        this.waves = waves;
        this.loot = loot;
        this.progression = progression;
    }

    /** 检查玩家所在房间；一次移动事件最多激活一场遭遇。 */
    void handlePlayerPosition(Player player) {
        for (DungeonSession.Encounter encounter : session.encounters) {
            if (encounter.state == DungeonSession.EncounterState.WAITING
                    && encounter.bounds.contains(player.getLocation())) {
                activate(encounter);
                return;
            }
        }
    }

    /** 只处理本服务登记过的怪物，普通世界中的实体死亡不会影响地牢状态。 */
    void handleMonsterDeath(UUID uuid) {
        DungeonSession.Encounter encounter = session.monsterOwners.remove(uuid);
        if (encounter == null) return;
        encounter.monsters.remove(uuid);
        if (encounter.monsters.isEmpty()) waves.onCurrentWaveCleared(encounter, this::clear);
    }

    /**
     * 结束游戏时移除仍存活的受控怪物，并重新打开所有进行中的房门。
     * 先清归属表再 remove，确保由 remove 引发的其他事件也不会重复结算。
     */
    void cleanup() {
        waves.cancelAll();
        var monsterIds = new ArrayList<>(session.monsterOwners.keySet());
        session.monsterOwners.clear();
        for (UUID uuid : monsterIds) {
            Entity entity = Bukkit.getEntity(uuid);
            if (entity != null) entity.remove();
        }
        for (DungeonSession.Encounter encounter : session.encounters) {
            encounter.monsters.clear();
            if (encounter.state == DungeonSession.EncounterState.ACTIVE) {
                Worldmanager.setRoomDoors(encounter.doors, Material.AIR);
            }
        }
    }

    private void activate(DungeonSession.Encounter encounter) {
        encounter.state = DungeonSession.EncounterState.ACTIVE;
        Worldmanager.setRoomDoors(encounter.doors,
                DungeonConfiguration.material("encounters.closed-door-block", Material.BARRIER));

        waves.start(encounter, Dungeongame.difficulty.waveCount(), this::clear);
    }

    private void clear(DungeonSession.Encounter encounter) {
        if (encounter.state == DungeonSession.EncounterState.CLEARED) return;
        encounter.state = DungeonSession.EncounterState.CLEARED;
        Worldmanager.setRoomDoors(encounter.doors, Material.AIR);
        progression.activate(encounter.progressionPortal);
        loot.spawn(encounter);
    }
}
