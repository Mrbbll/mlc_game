package com.mlc.mlcgames.dungeongame.managers;

import com.mlc.mlcgames.dungeongame.rooms.Room;
import org.bukkit.Location;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

/**
 * 把布局格坐标转换为世界绝对坐标，并统一计算门洞、房间边界与出生点。
 * 所有运行期数据都通过不可变 bounds 快照交给会话，避免依赖下一层会覆盖的布局表。
 */
final class DungeonRoomGeometry {
    private DungeonRoomGeometry() { }

    static Roommanager.BridgeAxis getBridgeAxis(RoomSpawner.Connection connection) {
        if (connection.corridor().isEmpty()) {
            throw new IllegalArgumentException("A connection must contain at least one bridge cell");
        }
        return connection.axis();
    }

    static void validatePassageConfig() {
        if (Worldmanager.passageWidth < 1 || Worldmanager.passageWidth > Worldmanager.layoutCellSize) {
            throw new IllegalArgumentException("passage.width must be between 1 and 16");
        }
        if (Worldmanager.passageHeight < 1) {
            throw new IllegalArgumentException("passage.height must be at least 1");
        }
        if (Worldmanager.passageBottomOffset < 0) {
            throw new IllegalArgumentException("passage.bottom-offset cannot be negative");
        }
    }

    /** 打开一条直桥两端所接触的房间墙面。 */
    static void openConnection(RoomSpawner.Connection connection) {
        validatePassageConfig();
        if (connection.directionX() == 0 && connection.directionZ() == 0) {
            throw new IllegalArgumentException("Connection direction is missing");
        }

        RoomSpawner.Locpoint firstBridge = connection.corridor().getFirst();
        RoomSpawner.Locpoint lastBridge = connection.corridor().getLast();
        RoomSpawner.Locpoint sourcePort = new RoomSpawner.Locpoint(
                firstBridge.x - connection.directionX(), firstBridge.y - connection.directionZ());
        RoomSpawner.Locpoint targetPort = new RoomSpawner.Locpoint(
                lastBridge.x + connection.directionX(), lastBridge.y + connection.directionZ());
        fillDoor(connection.from(), sourcePort,
                connection.directionX(), connection.directionZ(), Material.AIR);
        fillDoor(connection.to(), targetPort,
                -connection.directionX(), -connection.directionZ(), Material.AIR);
    }

    static List<Worldmanager.DoorBounds> getRoomDoors(Room room) {
        List<Worldmanager.DoorBounds> doors = new ArrayList<>();
        for (RoomSpawner.Connection connection : RoomSpawner.connections) {
            if (connection.from() == room) {
                RoomSpawner.Locpoint first = connection.corridor().getFirst();
                doors.add(getDoorBounds(room,
                        new RoomSpawner.Locpoint(first.x - connection.directionX(),
                                first.y - connection.directionZ()),
                        connection.directionX(), connection.directionZ()));
            } else if (connection.to() == room) {
                RoomSpawner.Locpoint last = connection.corridor().getLast();
                doors.add(getDoorBounds(room,
                        new RoomSpawner.Locpoint(last.x + connection.directionX(),
                                last.y + connection.directionZ()),
                        -connection.directionX(), -connection.directionZ()));
            }
        }
        return List.copyOf(doors);
    }

    static void setRoomDoors(List<Worldmanager.DoorBounds> doors, Material material) {
        for (Worldmanager.DoorBounds door : doors) {
            fillCuboid(door.minX(), door.minY(), door.minZ(),
                    door.maxX(), door.maxY(), door.maxZ(), material);
        }
    }

    static Location getRoomSpawn(Room room) {
        requireWorld();
        RoomSpawner.Locpoint point = RoomSpawner.roompointMap.get(room);
        if (point == null) {
            throw new IllegalArgumentException("Room is not part of the current dungeon layout");
        }
        double x = roomOriginX(point) + room.getSizeX() / 2.0;
        double z = roomOriginZ(point) + room.getSizeZ() / 2.0;
        return new Location(Worldmanager.dungeonWorld, x, Worldmanager.layoutOriginY + 1, z, 0.0F, 0.0F);
    }

    static Worldmanager.RoomBounds getRoomBounds(Room room) {
        RoomSpawner.Locpoint point = RoomSpawner.roompointMap.get(room);
        RoomSpawner.Footprint footprint = RoomSpawner.roomSizeMap.get(room);
        if (point == null || footprint == null) {
            throw new IllegalArgumentException("Room is not part of the current layout");
        }
        int minX = roomOriginX(point);
        int minZ = roomOriginZ(point);
        return new Worldmanager.RoomBounds(minX, Worldmanager.layoutOriginY, minZ,
                minX + footprint.width() * Worldmanager.layoutCellSize,
                Worldmanager.layoutOriginY + Math.max(room.getSizeY(),
                        Worldmanager.passageHeight + Worldmanager.passageBottomOffset + 2),
                minZ + footprint.depth() * Worldmanager.layoutCellSize);
    }

    static int roomOriginX(RoomSpawner.Locpoint point) {
        return Worldmanager.layoutOriginX + point.x * Worldmanager.layoutCellSize;
    }

    static int roomOriginZ(RoomSpawner.Locpoint point) {
        return Worldmanager.layoutOriginZ + point.y * Worldmanager.layoutCellSize;
    }

    private static void fillDoor(Room room, RoomSpawner.Locpoint portCell,
                                 int outwardX, int outwardZ, Material material) {
        Worldmanager.DoorBounds door = getDoorBounds(room, portCell, outwardX, outwardZ);
        fillCuboid(door.minX(), door.minY(), door.minZ(),
                door.maxX(), door.maxY(), door.maxZ(), material);
    }

    private static Worldmanager.DoorBounds getDoorBounds(Room room, RoomSpawner.Locpoint portCell,
                                                          int outwardX, int outwardZ) {
        RoomSpawner.Locpoint roomPoint = RoomSpawner.roompointMap.get(room);
        RoomSpawner.Footprint footprint = RoomSpawner.roomSizeMap.get(room);
        if (roomPoint == null || footprint == null) {
            throw new IllegalArgumentException("Room is not part of the current layout");
        }

        int horizontalOffset = (Worldmanager.layoutCellSize - Worldmanager.passageWidth) / 2;
        int minY = Worldmanager.layoutOriginY + Worldmanager.passageBottomOffset;
        int maxY = minY + Worldmanager.passageHeight - 1;
        if (outwardX != 0) {
            int wallX = outwardX > 0
                    ? roomOriginX(roomPoint) + footprint.width() * Worldmanager.layoutCellSize - 1
                    : roomOriginX(roomPoint);
            int minZ = Worldmanager.layoutOriginZ
                    + portCell.y * Worldmanager.layoutCellSize + horizontalOffset;
            return new Worldmanager.DoorBounds(
                    wallX, minY, minZ, wallX, maxY, minZ + Worldmanager.passageWidth - 1);
        }

        int wallZ = outwardZ > 0
                ? roomOriginZ(roomPoint) + footprint.depth() * Worldmanager.layoutCellSize - 1
                : roomOriginZ(roomPoint);
        int minX = Worldmanager.layoutOriginX
                + portCell.x * Worldmanager.layoutCellSize + horizontalOffset;
        return new Worldmanager.DoorBounds(
                minX, minY, wallZ, minX + Worldmanager.passageWidth - 1, maxY, wallZ);
    }

    private static void fillCuboid(int minX, int minY, int minZ,
                                   int maxX, int maxY, int maxZ, Material material) {
        requireWorld();
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    Worldmanager.dungeonWorld.getBlockAt(x, y, z).setType(material, false);
                }
            }
        }
    }

    private static void requireWorld() {
        if (Worldmanager.dungeonWorld == null) {
            throw new IllegalStateException("Dungeon world has not been created");
        }
    }
}
