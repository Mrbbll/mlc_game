package com.mlc.mlcgames.dungeongame.managers;

import com.mlc.mlcgames.dungeongame.rooms.Room;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import org.bukkit.Location;
import org.bukkit.block.structure.Mirror;
import org.bukkit.block.structure.StructureRotation;

import java.util.Random;

/** 负责校验并粘贴房间/桥模板，不处理房门几何或世界生命周期。 */
final class DungeonTemplatePlacement {
    private DungeonTemplatePlacement() { }

    static void putRoom(Room room, RoomSpawner.Locpoint point) {
        requireWorld();
        validateRoom(room);
        int x = DungeonRoomGeometry.roomOriginX(point);
        int z = DungeonRoomGeometry.roomOriginZ(point);
        if (room.isSchematic()) {
            pasteSchematic(room, x, Worldmanager.layoutOriginY, z);
            return;
        }
        room.getStructure().place(new Location(Worldmanager.dungeonWorld, x, Worldmanager.layoutOriginY, z),
                true, StructureRotation.NONE, Mirror.NONE, -1, 1.0F, new Random());
    }

    static void putBridge(Room bridge, RoomSpawner.Connection connection) {
        requireWorld();
        validateBridge(bridge, connection);
        for (RoomSpawner.Locpoint cell : connection.corridor()) {
            pasteSchematic(bridge, DungeonRoomGeometry.roomOriginX(cell),
                    Worldmanager.layoutOriginY, DungeonRoomGeometry.roomOriginZ(cell));
        }
    }

    static void validateRoom(Room room) {
        RoomSpawner.Footprint footprint = RoomSpawner.roomSizeMap.get(room);
        if (footprint == null) throw new IllegalArgumentException("Room is not part of the current layout");
        int expectedX = footprint.width() * Worldmanager.layoutCellSize;
        int expectedZ = footprint.depth() * Worldmanager.layoutCellSize;
        if (room.getSizeX() != expectedX || room.getSizeZ() != expectedZ) {
            throw new IllegalArgumentException("Room '" + room.getName() + "' must be "
                    + expectedX + " x " + expectedZ + " blocks in X/Z for its "
                    + footprint.width() + "x" + footprint.depth() + " chunk footprint");
        }
    }

    static void validateBridge(Room bridge, RoomSpawner.Connection connection) {
        if (!bridge.isSchematic()) {
            throw new IllegalArgumentException("Bridge templates must be FAWE .schem files");
        }
        DungeonRoomGeometry.getBridgeAxis(connection);
        if (bridge.getSizeX() != Worldmanager.layoutCellSize
                || bridge.getSizeZ() != Worldmanager.layoutCellSize) {
            throw new IllegalArgumentException("Bridge '" + bridge.getName()
                    + "' must occupy exactly one chunk (16 x 16 blocks)");
        }
    }

    /** 根据模板最低实体方块做 Y 对齐，X/Z 则以模板最小相对坐标对齐布局格。 */
    private static void pasteSchematic(Room room, int x, int y, int z) {
        try (EditSession editSession = WorldEdit.getInstance()
                .newEditSession(BukkitAdapter.adapt(Worldmanager.dungeonWorld))) {
            Operations.complete(new ClipboardHolder(room.getClipboard()).createPaste(editSession)
                    .to(BlockVector3.at(x - room.getMinimumRelativeX(), y - room.getLowestSolidRelativeY(),
                            z - room.getMinimumRelativeZ()))
                    .ignoreAirBlocks(false)
                    .build());
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to paste schematic room '" + room.getName() + "'", exception);
        }
    }

    private static void requireWorld() {
        if (Worldmanager.dungeonWorld == null) {
            throw new IllegalStateException("Dungeon world has not been created");
        }
    }
}
