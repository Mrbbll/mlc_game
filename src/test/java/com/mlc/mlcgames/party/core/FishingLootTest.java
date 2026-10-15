package com.mlc.mlcgames.party.core;

import com.mlc.mlcgames.party.arcade.fishing.FishingLoot;
import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FishingLootTest {
    private static FishingLoot.Entry entry(String name, int weight, boolean jungle) {
        return new FishingLoot.Entry(new FishingLoot.Catch(name, 3), weight, jungle);
    }
    private FishingLoot loot(double chance) {
        return new FishingLoot(new FishingLoot.Pool(85, List.of(entry("COD", 60, false), entry("SALMON", 25, false), entry("TROPICAL_FISH", 2, false), entry("PUFFERFISH", 13, false))),
                new FishingLoot.Pool(10, List.of(entry("LILY_PAD", 17, false), entry("BAMBOO", 10, true))),
                new FishingLoot.Pool(5, List.of(entry("NAME_TAG", 1, false))), chance);
    }
    private static Random rolls(double condition, int groupTicket, int itemTicket, int groupBound, int itemBound) {
        return new Random(0) {
            int call;
            @Override public double nextDouble() { return condition; }
            @Override public int nextInt(int bound) {
                assertEquals(call == 0 ? groupBound : itemBound, bound);
                return call++ == 0 ? groupTicket : itemTicket;
            }
        };
    }
    @Test void treasureConditionChangesEligibleWeightRatherThanGrantingTreasureDirectly() {
        assertEquals("NAME_TAG", loot(.8).draw(rolls(.79, 10, 0, 100, 1), false).material());
        assertEquals("COD", loot(.8).draw(rolls(.80, 10, 0, 95, 100), false).material());
    }
    @Test void passingTreasureConditionStillAllowsOrdinaryFish() {
        assertEquals("COD", loot(.8).draw(rolls(0, 15, 0, 100, 100), false).material());
    }
    @Test void bambooOnlyParticipatesInJungleItemPool() {
        assertEquals("BAMBOO", loot(.8).draw(rolls(0, 0, 26, 100, 27), true).material());
        assertEquals("LILY_PAD", loot(.8).draw(rolls(0, 0, 16, 100, 17), false).material());
    }
    @Test void allFourFishWeightsHaveExactBoundaries() {
        int[] tickets = {0,59,60,84,85,86,87,99};
        String[] expected = {"COD","COD","SALMON","SALMON","TROPICAL_FISH","TROPICAL_FISH","PUFFERFISH","PUFFERFISH"};
        for (int i = 0; i < tickets.length; i++) assertEquals(expected[i], loot(.8).draw(rolls(0, 99, tickets[i], 100, 100), false).material());
    }
    @Test void zeroAndOneChanceAreSupported() {
        assertEquals("COD", loot(0).draw(rolls(0, 10, 0, 95, 100), false).material());
        assertEquals("NAME_TAG", loot(1).draw(rolls(.999, 10, 0, 100, 1), false).material());
    }
    @Test void invalidWeightsAndImpossibleNonJunglePoolAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> entry("COD", 0, false));
        assertThrows(IllegalArgumentException.class, () -> new FishingLoot.Pool(1, List.of(entry("BAMBOO", 10, true))));
        assertThrows(IllegalArgumentException.class, () -> loot(Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> loot(1.01));
    }
}
