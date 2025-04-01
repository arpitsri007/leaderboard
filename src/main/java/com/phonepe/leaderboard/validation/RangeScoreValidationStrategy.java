package com.phonepe.leaderboard.validation;

public class RangeScoreValidationStrategy implements ScoreValidationStrategy {
    private final int minScore;
    private final int maxScore;

    public RangeScoreValidationStrategy(int minScore, int maxScore) {
        this.minScore = minScore;
        this.maxScore = maxScore;
    }

    @Override
    public boolean isValid(int score) {
        return score >= minScore && score <= maxScore;
    }

    @Override
    public String getErrorMessage() {
        return String.format("Score must be between %d and %d", minScore, maxScore);
    }
} 