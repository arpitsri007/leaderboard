package com.phonepe.leaderboard.validation;

public interface ScoreValidationStrategy {
    boolean isValid(int score);
    String getErrorMessage();
} 