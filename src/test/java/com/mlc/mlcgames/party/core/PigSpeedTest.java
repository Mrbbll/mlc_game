package com.mlc.mlcgames.party.core;

import com.mlc.mlcgames.party.racing.car_race.PigSpeed;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PigSpeedTest {
    private PigSpeed.Step step(int speed, boolean back, boolean forward, boolean stop, boolean slow) {
        return PigSpeed.step(speed, back, forward, stop, slow, 6, 5, 3, 100, 10, 5);
    }
    @Test void accelerationIsAppliedBeforeFriction() {
        assertEquals(new PigSpeed.Step(6, 3), step(0, false, true, false, false));
    }
    @Test void backwardAndForwardUseSourceOrder() {
        assertEquals(new PigSpeed.Step(6, 3), step(3, true, true, false, false));
    }
    @Test void jumpStopsEvenIfForwardIsHeld() {
        assertEquals(new PigSpeed.Step(0, 0), step(90, false, true, true, true));
    }
    @Test void capIsSoftAndAllowsTheSourceOvershoot() {
        assertEquals(new PigSpeed.Step(105, 102), step(99, false, true, false, false));
        assertEquals(new PigSpeed.Step(102, 99), step(102, false, true, false, false));
    }
    @Test void glowstonePenaltyPrecedesFrictionAndHasAThreshold() {
        assertEquals(new PigSpeed.Step(7, 4), step(12, false, false, false, true));
        assertEquals(new PigSpeed.Step(9, 6), step(9, false, false, false, true));
    }
    @Test void brakingAndFrictionNeverProduceNegativeSpeed() {
        assertEquals(new PigSpeed.Step(0, 0), step(2, true, false, false, false));
        assertEquals(new PigSpeed.Step(1, 0), step(1, false, false, false, false));
    }
}
