package com.mlc.mlcgames.party.racing.car_race;

/** Preserve source command order and its soft threshold (a forward step may overshoot the cap). */
public final class PigSpeed {
    public record Step(int applied, int next) {}
    public static Step step(int speed, boolean backward, boolean forward, boolean stop, boolean slow,
                            int acceleration, int brake, int friction, int cap, int slowThreshold, int slowPenalty) {
        if (speed < 0 || acceleration < 1 || brake < 1 || friction < 0 || cap < 1 || slowThreshold < 1 || slowPenalty < 1) throw new IllegalArgumentException("speed configuration");
        if (backward) speed = Math.max(0, speed - brake);
        if (forward && speed < cap) speed = Math.addExact(speed, acceleration);
        if (stop) speed = 0;
        if (slow && speed >= slowThreshold) speed = Math.max(0, speed - slowPenalty);
        return new Step(speed, Math.max(0, speed - friction));
    }
}
