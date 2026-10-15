package com.mlc.mlcgames.party.core;

import com.mlc.mlcgames.party.arcade.miner.MinerColumn;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MinerColumnTest {
    @Test void sourceGeometryHasNinetySixLayersFourAirBlocksAndRoof() {
        MinerColumn column = new MinerColumn(-96, 49, 613, 96);
        assertEquals(146, column.startY());
        assertEquals(150, column.bounds().maxY());
        assertEquals(918, column.bounds().volume());
        assertTrue(column.diggable(-96, 50, 613));
        assertTrue(column.diggable(-96, 145, 613));
        assertFalse(column.diggable(-96, 49, 613));
        assertFalse(column.diggable(-96, 146, 613));
    }
    @Test void onlyCentralShaftCanBeDugAndBarrierWallsRemainProtected() {
        MinerColumn column = new MinerColumn(-10, -60, -20, 96);
        assertFalse(column.diggable(-11, 0, -20));
        assertFalse(column.diggable(-10, 0, -19));
        assertTrue(column.bounds().contains(-11, -60, -21));
    }
    @Test void overlappingWallsAreRejectedButSeparatedOrStackedShaftsArePossible() {
        MinerColumn column = new MinerColumn(0, 0, 0, 96);
        assertTrue(column.overlaps(new MinerColumn(2, 0, 2, 96)));
        assertFalse(column.overlaps(new MinerColumn(3, 0, 0, 96)));
        assertFalse(column.overlaps(new MinerColumn(0, 102, 0, 96)));
    }
    @Test void configurableDepthIsBounded() {
        assertThrows(IllegalArgumentException.class, () -> new MinerColumn(0, 0, 0, 0));
        assertThrows(IllegalArgumentException.class, () -> new MinerColumn(0, 0, 0, 257));
        assertEquals(101, new MinerColumn(0, -160, 0, 256).bounds().maxY());
    }
}
