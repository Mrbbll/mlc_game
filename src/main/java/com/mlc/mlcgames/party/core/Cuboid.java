package com.mlc.mlcgames.party.core;

/** Inclusive block bounds. Independent of Bukkit so geometry is testable. */
public record Cuboid(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
    public Cuboid {
        if (minX > maxX || minY > maxY || minZ > maxZ) throw new IllegalArgumentException("区域角点顺序非法");
    }

    public static Cuboid between(int x1, int y1, int z1, int x2, int y2, int z2) {
        return new Cuboid(Math.min(x1, x2), Math.min(y1, y2), Math.min(z1, z2),
                Math.max(x1, x2), Math.max(y1, y2), Math.max(z1, z2));
    }

    public long volume() {
        return Math.multiplyExact(Math.multiplyExact((long) maxX - minX + 1, (long) maxY - minY + 1), (long) maxZ - minZ + 1);
    }

    public boolean contains(int x, int y, int z) {
        return x >= minX && x <= maxX && y >= minY && y <= maxY && z >= minZ && z <= maxZ;
    }

    public boolean contains(Cuboid other) {
        return contains(other.minX, other.minY, other.minZ) && contains(other.maxX, other.maxY, other.maxZ);
    }
}
