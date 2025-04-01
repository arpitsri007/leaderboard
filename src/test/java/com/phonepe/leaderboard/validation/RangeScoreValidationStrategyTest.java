package com.phonepe.leaderboard.validation;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class RangeScoreValidationStrategyTest {
    private static final int MIN_SCORE = 0;
    private static final int MAX_SCORE = 1000;
    private final RangeScoreValidationStrategy strategy = new RangeScoreValidationStrategy(MIN_SCORE, MAX_SCORE);

    @Test
    void testValidScores() {
        assertTrue(strategy.isValid(MIN_SCORE));
        assertTrue(strategy.isValid(MAX_SCORE));
        assertTrue(strategy.isValid(MIN_SCORE + 1));
        assertTrue(strategy.isValid(MAX_SCORE - 1));
        assertTrue(strategy.isValid((MIN_SCORE + MAX_SCORE) / 2));
    }

    @Test
    void testInvalidScores() {
        assertFalse(strategy.isValid(MIN_SCORE - 1));
        assertFalse(strategy.isValid(MAX_SCORE + 1));
        assertFalse(strategy.isValid(Integer.MIN_VALUE));
        assertFalse(strategy.isValid(Integer.MAX_VALUE));
    }

    @Test
    void testErrorMessage() {
        String errorMessage = strategy.getErrorMessage();
        assertTrue(errorMessage.contains(String.valueOf(MIN_SCORE)));
        assertTrue(errorMessage.contains(String.valueOf(MAX_SCORE)));
    }
} 