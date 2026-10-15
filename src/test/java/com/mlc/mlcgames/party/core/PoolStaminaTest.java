package com.mlc.mlcgames.party.core;

import com.mlc.mlcgames.party.sports.poolwar.PoolStamina;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PoolStaminaTest {
    @Test void waterCostsOnePerTickButCapacityFallsOnlyEverySecond() {
        PoolStamina value = new PoolStamina(80);
        for (int tick = 1; tick <= 20; tick++) value.tick(true, false, tick % 20 == 0, 30, 5);
        assertEquals(60, value.remaining());
        assertEquals(75, value.capacity());
    }
    @Test void restingRecoversOnlyToTheReducedCapacity() {
        PoolStamina value = new PoolStamina(80);
        for (int tick = 1; tick <= 40; tick++) value.tick(true, false, tick % 20 == 0, 30, 5);
        for (int tick = 0; tick < 100; tick++) value.tick(false, true, false, 30, 5);
        assertEquals(70, value.remaining());
        assertEquals(70, value.capacity());
    }
    @Test void capacityStopsAtThirtyAndReserveMayRunOut() {
        PoolStamina value = new PoolStamina(80);
        for (int tick = 1; tick <= 200; tick++) value.tick(true, false, tick % 20 == 0, 30, 5);
        assertEquals(30, value.capacity());
        assertTrue(value.remaining() <= 0);
    }
    @Test void NeitherWaterNorRestDoesNotRecover() {
        PoolStamina value = new PoolStamina(80);
        value.tick(true, false, false, 30, 5);
        value.tick(false, false, true, 30, 5);
        assertEquals(79, value.remaining());
    }
    @Test void losingCapacityDoesNotImmediatelyDiscardExistingReserve() {
        PoolStamina value = new PoolStamina(80);
        value.tick(true, false, true, 30, 5);
        assertEquals(79, value.remaining());
        assertEquals(75, value.capacity());
        value.tick(false, true, false, 30, 5);
        assertEquals(79, value.remaining());
    }
}
