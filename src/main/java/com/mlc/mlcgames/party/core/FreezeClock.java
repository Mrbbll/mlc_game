package com.mlc.mlcgames.party.core;

/** Freeze phases have absolute deadlines: jitter never extends a phase implicitly. */
public final class FreezeClock {
    private int deadline;
    private boolean frozen;
    public FreezeClock(int firstMovingTicks) {
        if (firstMovingTicks <= 0) throw new IllegalArgumentException("行动时间必须大于 0");
        deadline = firstMovingTicks;
    }
    public boolean frozen() { return frozen; }
    public int remaining(int now) { return Math.max(0, deadline - now); }
    public boolean due(int now) { return now >= deadline; }
    public void transition(int now, int nextDuration) {
        if (!due(now) || nextDuration <= 0) throw new IllegalArgumentException("非法冻结状态切换");
        frozen = !frozen;
        deadline = Math.addExact(now, nextDuration);
    }
}
