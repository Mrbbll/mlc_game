package com.mlc.mlcgames.dungeongame.rooms;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.structure.Structure;

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
    private final int sizeX;
    private final int sizeY;
    private final int sizeZ;
    private Structure structure;
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
    }

}
