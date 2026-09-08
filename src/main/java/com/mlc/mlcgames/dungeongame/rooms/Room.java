package com.mlc.mlcgames.dungeongame.rooms;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.Bukkit;
import org.bukkit.structure.Structure;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.math.BlockVector3;

import java.io.File;
import java.io.IOException;

/**
 * 单个房间。
 * 创建房间 = 用 StructureManager 把模板世界里框选的一块区域截成 Structure(createStructure + fill)；
 * 加载房间 = 用 Structure#place 把房间贴到目标世界任意坐标，可带随机旋转。
 */
public class Room {
    private final RoomType type;
    private final String name;
    private final World sourceWorld;
    private final int minX;
    private final int minY;
    private final int minZ;
    private final int maxX;
    private final int maxY;
    private final int maxZ;
    private int sizeX;
    private int sizeY;
    private int sizeZ;
    private Structure structure;
    private final File schematicFile;
    private Clipboard clipboard;
    /** Lowest non-air block measured relative to the schematic origin. */
    private int lowestSolidRelativeY;
    private int minimumRelativeX;
    private int minimumRelativeZ;
    private final int id;

    public Room(RoomType type, String name, World sourceWorld, Location corner1, Location corner2, int id) {
        this.type = type;
        this.name = name;
        this.sourceWorld = sourceWorld;
        this.minX = Math.min(corner1.getBlockX(), corner2.getBlockX());
        this.minY = Math.min(corner1.getBlockY(), corner2.getBlockY());
        this.minZ = Math.min(corner1.getBlockZ(), corner2.getBlockZ());
        this.maxX = Math.max(corner1.getBlockX(), corner2.getBlockX());
        this.maxY = Math.max(corner1.getBlockY(), corner2.getBlockY());
        this.maxZ = Math.max(corner1.getBlockZ(), corner2.getBlockZ());
        this.sizeX = maxX - minX + 1;
        this.sizeY = maxY - minY + 1;
        this.sizeZ = maxZ - minZ + 1;
        this.id = id;
        this.schematicFile = null;
        this.lowestSolidRelativeY = 0;
        this.minimumRelativeX = 0;
        this.minimumRelativeZ = 0;
    }

    /** A room template stored as a FAWE/WorldEdit .schem file. */
    public Room(RoomType type, String name, File schematicFile, int id) {
        this.type = type;
        this.name = name;
        this.sourceWorld = null;
        this.minX = this.minY = this.minZ = 0;
        this.maxX = this.maxY = this.maxZ = 0;
        this.id = id;
        this.schematicFile = schematicFile;
        loadClipboard(); // Validate the file and obtain its actual dimensions at startup.
    }

    /** Creates an independent placement of this room template. */
    public Room createInstance(int instanceId) {
        if (schematicFile != null) {
            return new Room(type, name, schematicFile, instanceId);
        }
        return new Room(type, name, sourceWorld,
                new Location(sourceWorld, minX, minY, minZ),
                new Location(sourceWorld, maxX, maxY, maxZ), instanceId);
    }

    /** Lazily snapshots the selected cuboid as a Bukkit structure. */
    public Structure getStructure() {
        if (schematicFile != null) {
            throw new IllegalStateException("Schematic rooms must be pasted through FAWE");
        }
        if (structure == null) {
            structure = Bukkit.getStructureManager().createStructure();
            structure.fill(new Location(sourceWorld, minX, minY, minZ),
                    new Location(sourceWorld, maxX, maxY, maxZ), true);
        }
        return structure;
    }

    public RoomType getType() { return type; }
    public String getName() { return name; }
    public World getSourceWorld() { return sourceWorld; }
    public int getMinX() { return minX; }
    public int getMinY() { return minY; }
    public int getMinZ() { return minZ; }
    public int getSizeX() { return sizeX; }
    public int getSizeY() { return sizeY; }
    public int getSizeZ() { return sizeZ; }
    public int getId() { return id; }
    public boolean isSchematic() { return schematicFile != null; }
    public Clipboard getClipboard() { return loadClipboard(); }
    public int getLowestSolidRelativeY() { return lowestSolidRelativeY; }
    public int getMinimumRelativeX() { return minimumRelativeX; }
    public int getMinimumRelativeZ() { return minimumRelativeZ; }

    private Clipboard loadClipboard() {
        if (clipboard != null) return clipboard;
        ClipboardFormat format = ClipboardFormats.findByFile(schematicFile);
        if (format == null) {
            throw new IllegalArgumentException("Unsupported schematic format: " + schematicFile.getName());
        }
        try {
            clipboard = format.load(schematicFile);
            sizeX = clipboard.getDimensions().x();
            sizeY = clipboard.getDimensions().y();
            sizeZ = clipboard.getDimensions().z();
            BlockVector3 minimum = clipboard.getRegion().getMinimumPoint();
            minimumRelativeX = minimum.x() - clipboard.getOrigin().x();
            minimumRelativeZ = minimum.z() - clipboard.getOrigin().z();
            int lowestSolidY = Integer.MAX_VALUE;
            for (BlockVector3 position : clipboard) {
                if (!clipboard.getBlock(position).isAir()) {
                    lowestSolidY = Math.min(lowestSolidY, position.y());
                }
            }
            if (lowestSolidY == Integer.MAX_VALUE) {
                throw new IllegalArgumentException("Schematic contains no non-air blocks: " + schematicFile.getName());
            }
            lowestSolidRelativeY = lowestSolidY - clipboard.getOrigin().y();
            return clipboard;
        } catch (IOException exception) {
            throw new IllegalArgumentException("Unable to read schematic " + schematicFile.getName(), exception);
        }
    }
}
