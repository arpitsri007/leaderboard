package com.phonepe.leaderboard.model;

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

    public Leaderboard(String id, String gameId, long startTime, long endTime) {
        this.id = id;
        this.gameId = gameId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.userScores = new ConcurrentHashMap<>();
        this.scoreToUser = new TreeMap<>((a, b) -> b.compareTo(a)); // Reverse order for highest scores first
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
        long currentTime = System.currentTimeMillis() / 1000;
        return currentTime >= startTime && currentTime <= endTime;
    }

    public void updateScore(String userId, int score) {
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