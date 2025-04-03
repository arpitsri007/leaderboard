package com.phonepe.leaderboard.model;

import com.phonepe.leaderboard.util.TimeProvider;
import java.util.Map;
import java.util.TreeMap;
import java.util.Set;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

public class Leaderboard {
    private final String id;
    private final String gameId;
    private final long startTime;
    private final long endTime;
    private final Map<String, Integer> userScores; // userId -> score
    private final TreeMap<Integer, Set<String>> scoreToUser; // score -> Set of userIds (for efficient ranking)
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

    public void updateScore(String userId, int score) {
        // logging

        synchronized (this) {
            Integer currentScore = userScores.get(userId);
            if (currentScore == null || score > currentScore) {
                if (currentScore != null) {
                    // Remove user from old score's set
                    Set<String> usersAtScore = scoreToUser.get(currentScore);
                    if (usersAtScore != null) {
                        usersAtScore.remove(userId);
                        if (usersAtScore.isEmpty()) {
                            scoreToUser.remove(currentScore);
                        }
                    }
                }

                userScores.put(userId, score);
                scoreToUser.computeIfAbsent(score, k -> new HashSet<>()).add(userId);
            }
        }

        // score 100 - user1
        // thread1 - score 150 - user1
        // thread2- score 100 - user1


        // notify
    }
    

    public Map<String, Integer> getUserScores() {
        return userScores;
    }

    public TreeMap<Integer, Set<String>> getScoreToUser() {
        return scoreToUser;
    }
} 