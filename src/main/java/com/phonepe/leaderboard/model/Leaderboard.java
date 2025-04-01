package com.phonepe.leaderboard.model;

import com.phonepe.leaderboard.util.TimeProvider;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

public class Leaderboard {
    private final String id;
    private final String gameId;
    private final long startTime;
    private final long endTime;
    private final Map<String, Integer> userScores; // userId -> score
    private final TreeMap<Integer, String> scoreToUser; // score -> userId (for efficient ranking)
    private final TimeProvider timeProvider;

    private Leaderboard(Builder builder) {
        this.id = builder.id;
        this.gameId = builder.gameId;
        this.startTime = builder.startTime;
        this.endTime = builder.endTime;
        this.timeProvider = builder.timeProvider;
        this.userScores = new ConcurrentHashMap<>();
        this.scoreToUser = new TreeMap<>((a, b) -> b.compareTo(a)); // Reverse order for highest scores first
    }

    public static class Builder {
        private String id;
        private String gameId;
        private long startTime;
        private long endTime;
        private TimeProvider timeProvider;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder gameId(String gameId) {
            this.gameId = gameId;
            return this;
        }

        public Builder startTime(long startTime) {
            this.startTime = startTime;
            return this;
        }

        public Builder endTime(long endTime) {
            this.endTime = endTime;
            return this;
        }

        public Builder timeProvider(TimeProvider timeProvider) {
            this.timeProvider = timeProvider;
            return this;
        }

        public Leaderboard build() {
            if (id == null || gameId == null || timeProvider == null) {
                throw new IllegalStateException("Required fields not set");
            }
            return new Leaderboard(this);
        }
    }

    public String getId() {
        return id;
    }

    public String getGameId() {
        return gameId;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public boolean isActive() {
        long currentTime = timeProvider.getCurrentTimeInSeconds();
        return currentTime >= startTime && currentTime <= endTime;
    }

    public synchronized void updateScore(String userId, int score) {
        Integer currentScore = userScores.get(userId);
        if (currentScore == null || score > currentScore) {
            if (currentScore != null) {
                scoreToUser.remove(currentScore);
            }
            userScores.put(userId, score);
            scoreToUser.put(score, userId);
        }
    }

    public Map<String, Integer> getUserScores() {
        return userScores;
    }

    public TreeMap<Integer, String> getScoreToUser() {
        return scoreToUser;
    }
} 