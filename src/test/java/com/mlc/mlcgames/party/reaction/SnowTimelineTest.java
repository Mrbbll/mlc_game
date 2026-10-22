package com.mlc.mlcgames.party.reaction;
import com.mlc.mlcgames.party.survival.disastersnow.SnowTimeline;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class SnowTimelineTest {
    private final SnowTimeline source=new SnowTimeline(15,6,10,30,30,5,20,5);
    @Test void nineAccelerationsReachTenExactlyAtSixtyNineSeconds(){assertEquals(1,source.speed(20));assertEquals(2,source.speed(21));assertEquals(9,source.speed(68));assertEquals(10,source.speed(69));assertEquals(10,source.speed(10000));}
    @Test void pvpAndShrinkWarningsPreserveTheirThirtySecondWindows(){assertEquals(99,source.pvpAt());assertEquals(129,source.firstShrinkAt());assertEquals(224,source.stopSnowAt());assertEquals(229,source.finishAt());}
    @Test void twentyShrinkEventsDoNotContinueBeyondTheEmptyFloor(){int events=0;for(int second=0;second<1000;second++)if(source.shrinkAt(second))events++;assertEquals(20,events);assertFalse(source.shrinkAt(128));assertTrue(source.shrinkAt(129));assertTrue(source.shrinkAt(224));assertFalse(source.shrinkAt(229));}
    @Test void invalidIntervalsAreRejected(){assertThrows(IllegalArgumentException.class,()->new SnowTimeline(15,0,10,30,30,5,20,5));}
}
