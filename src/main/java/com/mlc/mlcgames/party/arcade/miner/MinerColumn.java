package com.mlc.mlcgames.party.arcade.miner;

import com.mlc.mlcgames.party.core.Cuboid;

/** Bedrock floor, random layers, four blocks of headroom and a barrier roof. */
public record MinerColumn(int x, int floorY, int z, int layers) {
    public MinerColumn { if (layers < 1 || layers > 256) throw new IllegalArgumentException("矿柱随机层数必须为 1..256"); }
    public Cuboid bounds() { return new Cuboid(x - 1, floorY, z - 1, x + 1, floorY + layers + 5, z + 1); }
    public int startY() { return floorY + layers + 1; }
    public boolean diggable(int bx, int by, int bz) { return bx == x && bz == z && by > floorY && by <= floorY + layers; }
    public boolean overlaps(MinerColumn other) {
        return Math.abs((long) x - other.x) <= 2 && Math.abs((long) z - other.z) <= 2
                && bounds().minY() <= other.bounds().maxY() && bounds().maxY() >= other.bounds().minY();
    }
}
