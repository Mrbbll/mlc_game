package com.mlc.mlcgames.dungeongame.managers;

import com.mlc.mlcgames.dungeongame.rooms.Room;
import com.mlc.mlcgames.dungeongame.rooms.RoomType;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * 一局地牢的纯运行时数据。
 *
 * <p>这个类不负责生成世界、创建怪物或传送玩家，只保存这些流程共同需要的数据。
 * 把数据模型从各个服务中集中出来，可以避免服务相互持有并修改对方的私有集合。</p>
 */
final class DungeonSession {
    static final int DUNGEON_LEVELS = 3;
    static final int FLOORS_PER_LEVEL = 5;
    static final int TOTAL_STAGES = DUNGEON_LEVELS * FLOORS_PER_LEVEL;
    UUID id = UUID.randomUUID();

    /** 原理图刷怪标记所代表的战斗等级。 */
    enum MarkerKind { NORMAL, ELITE, BOSS }

    /**
     * WAITING：玩家尚未进入；ACTIVE：门已关闭且怪物已生成；CLEARED：奖励已结算。
     * 状态只允许按这个顺序向前推进，防止移动事件重复激活同一房间。
     */
    enum EncounterState { WAITING, ACTIVE, CLEARED }

    record SpawnMarker(Location location, MarkerKind kind) { }

    /** 一间带刷怪标记的房间在本局中的绝对坐标快照。 */
    static final class Encounter {
        final UUID id = UUID.randomUUID();
        final String roomName;
        final RoomType roomType;
        final int dungeonLevel;
        final int floor;
        final int environmentSet;
        final DungeonMobPack mobPack;
        final Worldmanager.RoomBounds bounds;
        final List<Worldmanager.DoorBounds> doors;
        final List<SpawnMarker> markers;
        final Set<UUID> monsters = new HashSet<>();
        EncounterState state = EncounterState.WAITING;
        int currentWave;
        int totalWaves;
        ProgressionPortal progressionPortal;

        Encounter(Room room, int dungeonLevel, int floor, int environmentSet, DungeonMobPack mobPack,
                  Worldmanager.RoomBounds bounds,
                  List<Worldmanager.DoorBounds> doors, List<SpawnMarker> markers) {
            this.roomName = room.getName();
            this.roomType = room.getType();
            this.dungeonLevel = dungeonLevel;
            this.floor = floor;
            this.environmentSet = environmentSet;
            this.mobPack = mobPack;
            this.bounds = bounds;
            this.doors = doors;
            this.markers = markers;
        }
    }

    /** 一层已经生成完成的布局；其中坐标不会受下一层静态布局表覆盖。 */
    static final class Stage {
        final int dungeonLevel;
        final int floor;
        final int environmentSet;
        final Location start;
        final Location portalMarker;
        final Encounter terminalEncounter;
        ProgressionPortal portal;

        Stage(int dungeonLevel, int floor, int environmentSet, Location start,
              Location portalMarker, Encounter terminalEncounter) {
            this.dungeonLevel = dungeonLevel;
            this.floor = floor;
            this.environmentSet = environmentSet;
            this.start = start;
            this.portalMarker = portalMarker;
            this.terminalEncounter = terminalEncounter;
        }
    }

    /** 当前层到下一层的传送关系；最后一层的 target 为 null，并代表胜利出口。 */
    static final class ProgressionPortal {
        final Stage source;
        final Stage target;
        final boolean victory;
        Location location;
        boolean active;

        ProgressionPortal(Stage source, Stage target) {
            this.source = source;
            this.target = target;
            this.victory = target == null;
        }
    }

    final List<Stage> stages = new ArrayList<>();
    final List<Encounter> encounters = new ArrayList<>();
    final List<ProgressionPortal> portals = new ArrayList<>();
    final Map<UUID, Encounter> monsterOwners = new HashMap<>();
    int generatedRoomCount;

    /** 清除一局的全部快照，使同一个 Manager 实例可以安全开始下一局。 */
    void clear() {
        stages.clear();
        encounters.clear();
        portals.clear();
        monsterOwners.clear();
        generatedRoomCount = 0;
        id = UUID.randomUUID();
    }
}
