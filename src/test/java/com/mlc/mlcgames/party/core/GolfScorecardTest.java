package com.mlc.mlcgames.party.core;

import com.mlc.mlcgames.party.sports.golf.GolfScorecard;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GolfScorecardTest {
    @Test void sixSourceClubCostsSubtractFromHoleScore() {
        GolfScorecard card = new GolfScorecard();
        for (int cost = 5; cost <= 30; cost += 5) card.shot(cost);
        assertEquals(105, card.energy());
        assertEquals(4895, card.hole(5000));
    }
    @Test void sourceEnergyCarriesBetweenCoursesAndSuccessfulScoresAccumulate() {
        GolfScorecard card = new GolfScorecard();
        card.shot(5);
        assertEquals(4995, card.hole(5000));
        card.shot(10);
        assertEquals(4985, card.hole(5000));
        assertEquals(9980, card.total());
    }
    @Test void retryOptionResetsEnergyButKeepsPreviousSuccessfulPoints() {
        GolfScorecard card = new GolfScorecard();
        card.shot(30); card.hole(5000); card.retry(true);
        assertEquals(0, card.energy());
        assertEquals(4970, card.total());
    }
    @Test void retryCanPreserveEnergyAndExpensiveHoleCanScoreNegative() {
        GolfScorecard card = new GolfScorecard();
        for (int i = 0; i < 200; i++) card.shot(30);
        card.retry(false);
        assertEquals(-1000, card.hole(5000));
    }
}
