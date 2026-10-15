package com.mlc.mlcgames.party.sports.poolwar;

/** Pure tick model, including source's capacity threshold and uncapped existing reserve. */
public final class PoolStamina {
    private int remaining;
    private int capacity;
    public PoolStamina(int initial) {
        if (initial < 1) throw new IllegalArgumentException("initial");
        remaining = capacity = initial;
    }
    public void tick(boolean water, boolean resting, boolean second, int minimum, int loss) {
        if (water) remaining--;
        else if (resting && remaining < capacity) remaining++;
        if (water && second && capacity >= minimum + loss) capacity -= loss;
    }
    public int remaining() { return remaining; }
    public int capacity() { return capacity; }
}
