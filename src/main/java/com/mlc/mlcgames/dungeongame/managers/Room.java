package com.mlc.mlcgames.dungeongame.managers;

import com.mlc.mlcgames.dungeongame.Dungeongame;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.structure.Mirror;
import org.bukkit.block.structure.StructureRotation;
import org.bukkit.structure.Structure;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

/**
 * 单个房间。
 * 创建房间 = 用 StructureManager 把模板世界里框选的一块区域截成 Structure(createStructure + fill)；
 * 加载房间 = 用 Structure#place 把房间贴到目标世界任意坐标，可带随机旋转。
 */
public class Room {
    private final String name;
    private final World sourceWorld;
    private final int minX;
    private final int minY;
    private final int minZ;
    private final int maxX;
    private final int maxY;
    private final int maxZ;
    private final int sizeX;
    private final int sizeY;
    private final int sizeZ;
    private Structure structure;

    public Room(String name, World sourceWorld, Location corner1, Location corner2) {
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
    }

    /** 创建房间：把源世界里这块区域(连同实体)截成 Structure，之后可反复粘贴。 */
    public @NotNull Structure create() {
        if (structure == null) {
            structure = Bukkit.getStructureManager().createStructure();
            structure.fill(new Location(sourceWorld, minX, minY, minZ),
                    new Location(sourceWorld, maxX, maxY, maxZ),
                    Dungeongame.capture_entities);
        }
        return structure;
    }

    /** 加载房间：把房间贴到目标世界，房间 min 角落在 (x, y, z)，rotation 为旋转方向。 */
    public void paste(@NotNull World target, int x, int y, int z, @NotNull StructureRotation rotation) {
        create().place(new Location(target, x, y, z),
                Dungeongame.capture_entities, rotation, Mirror.NONE, 100, 0f, new Random());
    }

    /** 宽=深时旋转 90 度不会破坏网格对齐，否则只能不旋转。 */
    public boolean isSquare() {
        return sizeX == sizeZ;
    }

    public String getName() {
        return name;
    }

    public World getSourceWorld() {
        return sourceWorld;
    }

    public int getSizeX() {
        return sizeX;
    }

    public int getSizeY() {
        return sizeY;
    }

    public int getSizeZ() {
        return sizeZ;
    }
}
