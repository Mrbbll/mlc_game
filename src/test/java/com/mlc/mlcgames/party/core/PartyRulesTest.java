package com.mlc.mlcgames.party.core;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class PartyRulesTest {
    @Test void shutdownDrainsPartlyRestoredArenaWithoutOverflowOrSkippingTail() {
        assertEquals(100000, BatchWindow.end(100000, 87000, Integer.MAX_VALUE));
        assertEquals(88000, BatchWindow.end(100000, 87000, 1000));
        assertEquals(100000, BatchWindow.end(100000, 100000, Integer.MAX_VALUE));
        assertThrows(IllegalArgumentException.class, () -> BatchWindow.end(10, 11, 1));
    }
    @Test void reversedCornersIncludeBothEndpointsAndNegativeCoordinates() {
        Cuboid area = Cuboid.between(3, 8, -5, -2, -1, -9);
        assertEquals(300, area.volume());
        assertTrue(area.contains(-2, -1, -9));
        assertTrue(area.contains(3, 8, -5));
        assertFalse(area.contains(4, 8, -5));
        assertFalse(area.contains(3, 9, -5));
        assertFalse(area.contains(3, 8, -4));
    }
    @Test void volumeOverflowCannotBypassArenaLimits() {
        Cuboid huge = Cuboid.between(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
        assertThrows(ArithmeticException.class, huge::volume);
        assertEquals(1, Cuboid.between(2, 3, 4, 2, 3, 4).volume());
    }
    @Test void specialRegionsMustFitInAllThreeDimensions() {
        Cuboid arena = Cuboid.between(-10, 0, -10, 10, 20, 10);
        assertTrue(arena.contains(Cuboid.between(-10, 0, -10, 10, 20, 10)));
        assertFalse(arena.contains(Cuboid.between(-10, -1, -10, 10, 20, 10)));
        assertThrows(IllegalArgumentException.class, () -> new Cuboid(1, 0, 0, 0, 1, 1));
    }
    @Test void sourceHotPotatoCountThresholdsLeaveAtLeastOneRunnerIncluding64Players() {
        ThresholdTable table = new ThresholdTable(List.of(new ThresholdTable.Entry(2,1), new ThresholdTable.Entry(5,2), new ThresholdTable.Entry(8,3), new ThresholdTable.Entry(12,4), new ThresholdTable.Entry(16,6), new ThresholdTable.Entry(20,8), new ThresholdTable.Entry(24,9), new ThresholdTable.Entry(28,10), new ThresholdTable.Entry(32,11), new ThresholdTable.Entry(36,12), new ThresholdTable.Entry(40,13), new ThresholdTable.Entry(44,14), new ThresholdTable.Entry(48,15), new ThresholdTable.Entry(52,16), new ThresholdTable.Entry(56,17), new ThresholdTable.Entry(60,18)));
        assertEquals(1, table.value(4));
        assertEquals(2, table.value(5));
        assertEquals(4, table.value(15));
        assertEquals(6, table.value(16));
        assertEquals(18, table.value(64));
        for (int n = 2; n <= 64; n++) assertTrue(table.value(n) < n);
    }
    @Test void unorderedThresholdsAreSortedButAmbiguousOrMissingBaseRulesFail() {
        ThresholdTable table = new ThresholdTable(List.of(new ThresholdTable.Entry(16,60), new ThresholdTable.Entry(2,30), new ThresholdTable.Entry(8,50)));
        assertEquals(30, table.value(7));
        assertEquals(50, table.value(8));
        assertEquals(60, table.value(64));
        assertThrows(IllegalArgumentException.class, () -> table.value(1));
        assertThrows(IllegalArgumentException.class, () -> new ThresholdTable(List.of()));
        assertThrows(IllegalArgumentException.class, () -> new ThresholdTable(List.of(new ThresholdTable.Entry(3,30))));
        assertThrows(IllegalArgumentException.class, () -> new ThresholdTable(List.of(new ThresholdTable.Entry(2,30), new ThresholdTable.Entry(2,60))));
    }
}
