package com.mlc.mlcgames.dungeongame.managers;

import com.mlc.mlcgames.dungeongame.Dungeongame;
import com.mlc.mlcgames.dungeongame.rooms.RoomType;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;

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
    private final DungeonMobDropService drops;
    private final DungeonPartyService party;

    DungeonEncounterService(DungeonSession session, DungeonWaveService waves,
                            DungeonLootChestService loot, DungeonProgressionService progression,
                            DungeonMobDropService drops, DungeonPartyService party) {
        this.session = session;
        this.waves = waves;
        this.loot = loot;
        this.progression = progression;
        this.drops = drops;
        this.party = party;
    }

    /** 检查玩家所在房间；一次移动事件最多激活一场遭遇。 */
    void handlePlayerPosition(Player player) {
        for (DungeonSession.Encounter encounter : session.encounters) {
            if (encounter.state == DungeonSession.EncounterState.WAITING
                    && encounter.bounds.contains(player.getLocation())) {
                activate(encounter, player);
                return;
            }
        }
    }

    /** 只接管本服务登记过的怪物；普通世界实体的掉落与经验完全不受影响。 */
    void handleMonsterDeath(EntityDeathEvent event) {
        UUID uuid = event.getEntity().getUniqueId();
        DungeonSession.Encounter encounter = session.monsterOwners.remove(uuid);
        if (encounter == null) return;
        drops.replaceDrops(event);
        encounter.monsters.remove(uuid);
        if (encounter.monsters.isEmpty()) waves.onCurrentWaveCleared(encounter, this::clear);
    }

    /** 兼容旧 API 的无事件入口；只能推进归属状态，无法改写 Bukkit 掉落列表。 */
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

    private void activate(DungeonSession.Encounter encounter, Player trigger) {
        // 必须先切换状态：PlayerTeleportEvent 继承移动事件，集结传送可能同步回到本方法；
        // 提前标记 ACTIVE 可以保证同一个 WAITING 房间只激活一次。
        encounter.state = DungeonSession.EncounterState.ACTIVE;
        // 在关门和刷怪前集结队友，这样慢一步的成员不会被屏障留在房间外。
        party.rallyForEncounter(trigger, encounter.bounds);
        Worldmanager.setRoomDoors(encounter.doors,
                DungeonConfiguration.material("encounters.closed-door-block", Material.BARRIER));

        // Boss 房是一次性决战，不参与难度波次；普通/特殊房才使用 2/3/4 波规则。
        int totalWaves = encounter.roomType == RoomType.Boss
                ? 1
                : Dungeongame.difficulty.waveCount();
        waves.start(encounter, totalWaves, this::clear);
    }

    private void clear(DungeonSession.Encounter encounter) {
        if (encounter.state == DungeonSession.EncounterState.CLEARED) return;
        encounter.state = DungeonSession.EncounterState.CLEARED;
        Worldmanager.setRoomDoors(encounter.doors, Material.AIR);
        progression.activate(encounter.progressionPortal);
        loot.spawn(encounter);
    }
}
