package com.mlc.mlcgames.party.sports.golf;

/** The source clears energy at game start and optionally on retry, not on hole completion. */
public final class GolfScorecard {
    private int energy;
    private int total;
    public void shot(int cost) {
        if (cost < 1) throw new IllegalArgumentException("cost");
        energy = Math.addExact(energy, cost);
    }
    public int hole(int base) {
        int earned = Math.subtractExact(base, energy);
        total = Math.addExact(total, earned);
        return earned;
    }
    public void retry(boolean reset) { if (reset) energy = 0; }
    public int energy() { return energy; }
    public int total() { return total; }
}
