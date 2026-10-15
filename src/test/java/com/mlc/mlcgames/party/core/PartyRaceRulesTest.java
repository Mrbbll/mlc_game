package com.mlc.mlcgames.party.core;

import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class PartyRaceRulesTest {
    @Test void touchingFinishOnConsecutiveTicksCannotAwardTwice() {
        FinishOrder<String> order = new FinishOrder<>();
        assertEquals(1, order.finish("first"));
        assertEquals(0, order.finish("first"));
        assertEquals(2, order.finish("second"));
        assertEquals(0, order.finish("first"));
        assertEquals(2, order.count());
    }
    @Test void oldResultSnapshotsCannotBeMutatedOrRenumbered() {
        FinishOrder<String> order = new FinishOrder<>();
        order.finish("first");
        Map<String, Integer> saved = order.places();
        order.finish("second");
        assertEquals(Map.of("first", 1), saved);
        assertThrows(UnsupportedOperationException.class, () -> saved.put("fake", 0));
        assertEquals(2, order.places().get("second"));
    }
    @Test void eggTiesUseDenseRanksRatherThanSkippingSecondPlace() {
        Map<String, Integer> eggs = new LinkedHashMap<>();
        eggs.put("A", 8); eggs.put("B", 8); eggs.put("C", 5); eggs.put("D", 0);
        var ranks = Rankings.descending(eggs);
        assertEquals(List.of("A", "B", "C", "D"), ranks.stream().map(Rankings.Row::id).toList());
        assertEquals(List.of(1, 1, 2, 3), ranks.stream().map(Rankings.Row::rank).toList());
        assertThrows(UnsupportedOperationException.class, () -> ranks.clear());
    }
    @Test void zeroEggsAreStillAnExplicitTieAndEmptyRaceHasNoWinner() {
        assertTrue(Rankings.descending(Map.<String, Integer>of()).isEmpty());
        assertTrue(Rankings.descending(Map.of("A", 0, "B", 0)).stream().allMatch(row -> row.rank() == 1));
        assertEquals(-100, Rankings.descending(Map.of("A", -100, "B", -200)).getFirst().score());
    }
    @Test void freezeStartsAtDeadlineAndCannotBeExtendedByEarlyTransitions() {
        FreezeClock clock = new FreezeClock(120);
        assertFalse(clock.frozen());
        assertFalse(clock.due(119));
        assertThrows(IllegalArgumentException.class, () -> clock.transition(119, 60));
        assertTrue(clock.due(120));
        clock.transition(120, 60);
        assertTrue(clock.frozen());
        assertEquals(1, clock.remaining(179));
        assertTrue(clock.due(180));
        clock.transition(180, 160);
        assertFalse(clock.frozen());
        assertEquals(160, clock.remaining(180));
    }
    @Test void invalidFreezeDurationsCannotCreateAnEndlessToggle() {
        assertThrows(IllegalArgumentException.class, () -> new FreezeClock(0));
        FreezeClock clock = new FreezeClock(1);
        assertThrows(IllegalArgumentException.class, () -> clock.transition(1, 0));
        assertFalse(clock.frozen());
        assertEquals(0, clock.remaining(50));
    }
}
