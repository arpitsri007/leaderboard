package com.phonepe.leaderboard.model;

import com.phonepe.leaderboard.util.TimeProvider;
import java.util.UUID;

public class LeaderboardFactory {
    private final TimeProvider timeProvider;

    public LeaderboardFactory(TimeProvider timeProvider) {
        this.timeProvider = timeProvider;
    }

    public Leaderboard createLeaderboard(String gameId, long startTime, long endTime) {
        return new Leaderboard.Builder()
                .id(UUID.randomUUID().toString())
                .gameId(gameId)
                .startTime(startTime)
                .endTime(endTime)
                .timeProvider(timeProvider)
                .build();
    }
} 