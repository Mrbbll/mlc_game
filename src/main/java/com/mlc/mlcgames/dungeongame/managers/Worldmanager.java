package com.mlc.mlcgames.dungeongame.managers;

import com.mlc.mlcgames.dungeongame.rooms.Room;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.structure.Mirror;
import org.bukkit.block.structure.StructureRotation;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.Random;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;

public class Worldmanager {
    public static World dungeonWorld;
    public static int layoutOriginX = 0;
    public static int layoutOriginY = 80;
    public static int layoutOriginZ = 0;
    /** One logical grid cell is exactly one Minecraft chunk. */
    public static final int layoutCellSize = 16;
    public static int passageWidth = 8;
    public static int passageHeight = 8;
    public static int passageBottomOffset = 1;

    public static void createDungeonWorld() {
        deleteDungeonWorld();
        WorldCreator wc = WorldCreator.name("Dungeongame");
        wc.generateStructures(false);
        wc.bonusChest(false);
        wc.hardcore(false);
        wc.biomeProvider(new BiomeProvider() {
            @Override
            public @NotNull Biome getBiome(@NotNull WorldInfo worldInfo, int x, int y, int z) {
                return Biome.THE_VOID;
            }

            @Override
            public @NotNull List<Biome> getBiomes(@NotNull WorldInfo worldInfo) {
                return List.of(Biome.THE_VOID);
            }
        });
        wc.environment(World.Environment.NORMAL);
        wc.generator(new ChunkGenerator() {
            @Override
            public boolean shouldGenerateNoise() {
                return super.shouldGenerateNoise();
            }

            @Override
            public boolean shouldGenerateSurface() {
                return super.shouldGenerateSurface();
            }

            @Override
            public boolean shouldGenerateCaves() {
                return super.shouldGenerateCaves();
            }

            @Override
            public boolean shouldGenerateMobs() {
                return super.shouldGenerateMobs();
            }

            @Override
            public boolean shouldGenerateDecorations() {
                return super.shouldGenerateDecorations();
            }

            @Override
            public boolean shouldGenerateStructures() {
                return super.shouldGenerateStructures();
            }
        });

        dungeonWorld = wc.createWorld();
    }

    public static void deleteDungeonWorld() {
        World existing = Bukkit.getWorld("Dungeongame");
        File worldFolder = existing != null ? existing.getWorldFolder()
                : new File(Bukkit.getWorldContainer(), "Dungeongame");
        if (!worldFolder.exists()) return;
        verifyDungeonWorldFolder(worldFolder, existing);
        if (existing != null && !Bukkit.unloadWorld(existing, false)) {
            throw new IllegalStateException("Unable to unload the current dungeon world");
        }
        try {
            Files.walkFileTree(worldFolder.toPath(), new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path directory, IOException exception) throws IOException {
                    if (exception != null) throw exception;
                    Files.delete(directory);
                    return FileVisitResult.CONTINUE;
                }
            });
            dungeonWorld = null;
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to delete the previous dungeon world", exception);
        }
    }

    public static void putRoomInWorld(Room room, RoomSpawner.Locpoint locpoint){
        if (dungeonWorld == null) {
            throw new IllegalStateException("Dungeon world has not been created");
        }
        validateRoomTemplate(room);
        int x = roomOriginX(locpoint);
        int z = roomOriginZ(locpoint);
        if (room.isSchematic()) {
            pasteSchematic(room, x, layoutOriginY, z);
            return;
        }
        room.getStructure().place(new org.bukkit.Location(dungeonWorld, x, layoutOriginY, z),
                true, StructureRotation.NONE, Mirror.NONE, -1, 1.0F, new Random());
    }

    public static Roommanager.BridgeAxis getBridgeAxis(RoomSpawner.Connection connection) {
        if (connection.corridor().isEmpty()) throw new IllegalArgumentException("A connection must contain at least one bridge cell");
        return connection.axis();
    }

    /** Pastes one bridge from the lower X/Z room edge towards the higher edge. */
    public static void putBridgeInWorld(Room bridge, RoomSpawner.Connection connection) {
        validateBridgeTemplate(bridge, connection);
        getBridgeAxis(connection);
        for (RoomSpawner.Locpoint cell : connection.corridor()) {
            pasteSchematic(bridge, roomOriginX(cell), layoutOriginY, roomOriginZ(cell));
        }
    }

    public static void validateRoomTemplate(Room room) {
        RoomSpawner.Footprint footprint = RoomSpawner.roomSizeMap.get(room);
        if (footprint == null) throw new IllegalArgumentException("Room is not part of the current layout");
        int expectedX = footprint.width() * layoutCellSize;
        int expectedZ = footprint.depth() * layoutCellSize;
        if (room.getSizeX() != expectedX || room.getSizeZ() != expectedZ) {
            throw new IllegalArgumentException("Room '" + room.getName() + "' must be " + expectedX + " x " + expectedZ
                    + " blocks in X/Z for its " + footprint.width() + "x" + footprint.depth() + " chunk footprint");
        }
    }

    public static void validateBridgeTemplate(Room bridge, RoomSpawner.Connection connection) {
        if (!bridge.isSchematic()) {
            throw new IllegalArgumentException("Bridge templates must be FAWE .schem files");
        }
        getBridgeAxis(connection);
        if (bridge.getSizeX() != layoutCellSize || bridge.getSizeZ() != layoutCellSize) {
            throw new IllegalArgumentException("Bridge '" + bridge.getName() + "' must occupy exactly one chunk (16 x 16 blocks)");
        }
    }

    /** Opens both room walls touched by a straight bridge connection. */
    public static void openConnection(RoomSpawner.Connection connection) {
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

        fillDoor(connection.from(), sourcePort, connection.directionX(), connection.directionZ(), Material.AIR);
        fillDoor(connection.to(), targetPort, -connection.directionX(), -connection.directionZ(), Material.AIR);
    }

    public static void validatePassageConfig() {
        if (passageWidth < 1 || passageWidth > layoutCellSize) {
            throw new IllegalArgumentException("passage.width must be between 1 and 16");
        }
        if (passageHeight < 1) throw new IllegalArgumentException("passage.height must be at least 1");
        if (passageBottomOffset < 0) throw new IllegalArgumentException("passage.bottom-offset cannot be negative");
    }

    public static void setRoomDoors(Room room, Material material) {
        setRoomDoors(getRoomDoors(room), material);
    }

    /** Captures absolute door cuboids so a room remains usable after another layout replaces the static plan. */
    public static List<DoorBounds> getRoomDoors(Room room) {
        List<DoorBounds> doors = new java.util.ArrayList<>();
        for (RoomSpawner.Connection connection : RoomSpawner.connections) {
            if (connection.from() == room) {
                RoomSpawner.Locpoint first = connection.corridor().getFirst();
                doors.add(getDoorBounds(room,
                        new RoomSpawner.Locpoint(first.x - connection.directionX(), first.y - connection.directionZ()),
                        connection.directionX(), connection.directionZ()));
            } else if (connection.to() == room) {
                RoomSpawner.Locpoint last = connection.corridor().getLast();
                doors.add(getDoorBounds(room,
                        new RoomSpawner.Locpoint(last.x + connection.directionX(), last.y + connection.directionZ()),
                        -connection.directionX(), -connection.directionZ()));
            }
        }
        return List.copyOf(doors);
    }

    public static void setRoomDoors(List<DoorBounds> doors, Material material) {
        for (DoorBounds door : doors) {
            fillCuboid(door.minX(), door.minY(), door.minZ(), door.maxX(), door.maxY(), door.maxZ(), material);
        }
    }

    private static void fillDoor(Room room, RoomSpawner.Locpoint portCell, int outwardX, int outwardZ, Material material) {
        DoorBounds door = getDoorBounds(room, portCell, outwardX, outwardZ);
        fillCuboid(door.minX(), door.minY(), door.minZ(), door.maxX(), door.maxY(), door.maxZ(), material);
    }

    private static DoorBounds getDoorBounds(Room room, RoomSpawner.Locpoint portCell, int outwardX, int outwardZ) {
        RoomSpawner.Locpoint roomPoint = RoomSpawner.roompointMap.get(room);
        RoomSpawner.Footprint footprint = RoomSpawner.roomSizeMap.get(room);
        if (roomPoint == null || footprint == null) throw new IllegalArgumentException("Room is not part of the current layout");

        int horizontalOffset = (layoutCellSize - passageWidth) / 2;
        int minY = layoutOriginY + passageBottomOffset;
        int maxY = minY + passageHeight - 1;
        if (outwardX != 0) {
            int wallX = outwardX > 0
                    ? roomOriginX(roomPoint) + footprint.width() * layoutCellSize - 1
                    : roomOriginX(roomPoint);
            int minZ = layoutOriginZ + portCell.y * layoutCellSize + horizontalOffset;
            return new DoorBounds(wallX, minY, minZ, wallX, maxY, minZ + passageWidth - 1);
        } else {
            int wallZ = outwardZ > 0
                    ? roomOriginZ(roomPoint) + footprint.depth() * layoutCellSize - 1
                    : roomOriginZ(roomPoint);
            int minX = layoutOriginX + portCell.x * layoutCellSize + horizontalOffset;
            return new DoorBounds(minX, minY, wallZ, minX + passageWidth - 1, maxY, wallZ);
        }
    }

    private static void fillCuboid(int minX, int minY, int minZ, int maxX, int maxY, int maxZ, Material material) {
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    dungeonWorld.getBlockAt(x, y, z).setType(material, false);
                }
            }
        }
    }

    /** Returns a practical initial teleport point near the centre of a room. */
    public static org.bukkit.Location getRoomSpawn(Room room) {
        if (dungeonWorld == null) {
            throw new IllegalStateException("Dungeon world has not been created");
        }
        RoomSpawner.Locpoint point = RoomSpawner.roompointMap.get(room);
        if (point == null) {
            throw new IllegalArgumentException("Room is not part of the current dungeon layout");
        }
        double x = roomOriginX(point) + room.getSizeX() / 2.0;
        double z = roomOriginZ(point) + room.getSizeZ() / 2.0;
        return new org.bukkit.Location(dungeonWorld, x, layoutOriginY + 1, z, 0.0F, 0.0F);
    }

    public record RoomBounds(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        public boolean contains(org.bukkit.Location location) {
            return location.getWorld() == dungeonWorld && location.getX() >= minX && location.getX() < maxX
                    && location.getZ() >= minZ && location.getZ() < maxZ
                    && location.getY() >= minY && location.getY() <= maxY;
        }
    }

    public record DoorBounds(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) { }

    public static RoomBounds getRoomBounds(Room room) {
        RoomSpawner.Locpoint point = RoomSpawner.roompointMap.get(room);
        RoomSpawner.Footprint footprint = RoomSpawner.roomSizeMap.get(room);
        if (point == null || footprint == null) throw new IllegalArgumentException("Room is not part of the current layout");
        int minX = roomOriginX(point);
        int minZ = roomOriginZ(point);
        return new RoomBounds(minX, layoutOriginY, minZ,
                minX + footprint.width() * layoutCellSize,
                layoutOriginY + Math.max(room.getSizeY(), passageHeight + passageBottomOffset + 2),
                minZ + footprint.depth() * layoutCellSize);
    }

    private static int roomOriginX(RoomSpawner.Locpoint point) {
        return layoutOriginX + point.x * layoutCellSize;
    }

    private static int roomOriginZ(RoomSpawner.Locpoint point) {
        return layoutOriginZ + point.y * layoutCellSize;
    }

    private static void pasteSchematic(Room room, int x, int y, int z) {
        try (EditSession editSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(dungeonWorld))) {
            Operations.complete(new ClipboardHolder(room.getClipboard()).createPaste(editSession)
                    .to(BlockVector3.at(x - room.getMinimumRelativeX(), y - room.getLowestSolidRelativeY(),
                            z - room.getMinimumRelativeZ())).ignoreAirBlocks(false).build());
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to paste schematic room '" + room.getName() + "'", exception);
        }
    }

    private static void verifyDungeonWorldFolder(File folder, World loadedWorld) {
        Path root = Bukkit.getWorldContainer().toPath().toAbsolutePath().normalize();
        Path target = folder.toPath().toAbsolutePath().normalize();
        boolean namedDungeon = target.getFileName() != null
                && "Dungeongame".equalsIgnoreCase(target.getFileName().toString());
        boolean exactLoadedWorldFolder = loadedWorld != null
                && target.equals(loadedWorld.getWorldFolder().toPath().toAbsolutePath().normalize());
        boolean insideWorldContainer = target.startsWith(root) && !target.equals(root);
        if (!namedDungeon || (!exactLoadedWorldFolder && !insideWorldContainer)) {
            throw new IllegalArgumentException("Refusing to delete a world folder outside the named dungeon target: " + target);
        }
    }
}
