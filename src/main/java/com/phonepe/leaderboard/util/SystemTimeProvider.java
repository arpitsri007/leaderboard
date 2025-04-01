package com.phonepe.leaderboard.util;

public class SystemTimeProvider implements TimeProvider {
    @Override
    public long getCurrentTimeInSeconds() {
        return System.currentTimeMillis() / 1000;
    }
} 