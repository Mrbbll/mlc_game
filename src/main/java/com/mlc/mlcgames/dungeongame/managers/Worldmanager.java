package com.mlc.mlcgames.dungeongame.managers;

import com.mlc.mlcgames.dungeongame.rooms.Room;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

import java.util.List;

/**
 * 世界相关操作的兼容门面。
 *
 * <p>旧调用方继续使用本类的静态 API；具体实现已分别移入世界生命周期、模板粘贴和
 * 房间几何组件。公开字段暂时保留，以兼容命令及旧初始化代码。</p>
 */
public class Worldmanager {
    public static World dungeonWorld;
    public static int layoutOriginX = 0;
    public static int layoutOriginY = 80;
    public static int layoutOriginZ = 0;
    /** 一个逻辑布局格严格对应一个 Minecraft 区块。 */
    public static final int layoutCellSize = 16;
    public static int passageWidth = 8;
    public static int passageHeight = 8;
    public static int passageBottomOffset = 1;

    /** 保留旧版可实例化行为；新代码应只使用静态门面。 */
    public Worldmanager() { }

    public static void createDungeonWorld() {
        DungeonWorldLifecycle.create();
    }

    public static void deleteDungeonWorld() {
        DungeonWorldLifecycle.delete();
    }

    public static void putRoomInWorld(Room room, RoomSpawner.Locpoint point) {
        DungeonTemplatePlacement.putRoom(room, point);
    }

    public static Roommanager.BridgeAxis getBridgeAxis(RoomSpawner.Connection connection) {
        return DungeonRoomGeometry.getBridgeAxis(connection);
    }

    public static void putBridgeInWorld(Room bridge, RoomSpawner.Connection connection) {
        DungeonTemplatePlacement.putBridge(bridge, connection);
    }

    public static void validateRoomTemplate(Room room) {
        DungeonTemplatePlacement.validateRoom(room);
    }

    public static void validateBridgeTemplate(Room bridge, RoomSpawner.Connection connection) {
        DungeonTemplatePlacement.validateBridge(bridge, connection);
    }

    public static void openConnection(RoomSpawner.Connection connection) {
        DungeonRoomGeometry.openConnection(connection);
    }

    public static void validatePassageConfig() {
        DungeonRoomGeometry.validatePassageConfig();
    }

    public static void setRoomDoors(Room room, Material material) {
        setRoomDoors(getRoomDoors(room), material);
    }

    /**
     * 保存门洞的绝对坐标；即使 RoomSpawner 随后生成下一层并覆盖布局表，仍可安全封门。
     */
    public static List<DoorBounds> getRoomDoors(Room room) {
        return DungeonRoomGeometry.getRoomDoors(room);
    }

    public static void setRoomDoors(List<DoorBounds> doors, Material material) {
        DungeonRoomGeometry.setRoomDoors(doors, material);
    }

    public static Location getRoomSpawn(Room room) {
        return DungeonRoomGeometry.getRoomSpawn(room);
    }

    public static RoomBounds getRoomBounds(Room room) {
        return DungeonRoomGeometry.getRoomBounds(room);
    }

    public record RoomBounds(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        public boolean contains(Location location) {
            return location.getWorld() == dungeonWorld
                    && location.getX() >= minX && location.getX() < maxX
                    && location.getZ() >= minZ && location.getZ() < maxZ
                    && location.getY() >= minY && location.getY() <= maxY;
        }
    }

    public record DoorBounds(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) { }
}
