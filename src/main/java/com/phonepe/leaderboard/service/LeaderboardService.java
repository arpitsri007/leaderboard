package com.phonepe.leaderboard.service;

import com.phonepe.leaderboard.exception.GameNotSupportedException;
import com.phonepe.leaderboard.exception.InvalidScoreException;
import com.phonepe.leaderboard.model.Leaderboard;
import com.phonepe.leaderboard.repository.LeaderboardRepository;
import com.phonepe.leaderboard.util.TimeProvider;
import com.phonepe.leaderboard.validation.ScoreValidationStrategy;
import java.util.*;
import java.util.stream.Collectors;

public class LeaderboardService {
    private final LeaderboardRepository repository;
    private final Set<String> supportedGames;
    private final ScoreValidationStrategy scoreValidationStrategy;

    public LeaderboardService(TimeProvider timeProvider, ScoreValidationStrategy scoreValidationStrategy) {
        this.repository = new LeaderboardRepository(timeProvider);
        this.supportedGames = new HashSet<>();
        this.scoreValidationStrategy = scoreValidationStrategy;
    }

    public void addSupportedGame(String gameId) {
        supportedGames.add(gameId);
    }

    public List<String> getSupportedGames() {
        return new ArrayList<>(supportedGames);
    }

    public String createLeaderboard(String gameId, int startEpochSeconds, int endEpochSeconds) {
        if (!supportedGames.contains(gameId)) {
            throw new GameNotSupportedException("Game not supported: " + gameId);
        }
        
        if (startEpochSeconds >= endEpochSeconds) {
            throw new IllegalArgumentException("Start time must be before end time");
        }

        Leaderboard leaderboard = repository.createLeaderboard(gameId, startEpochSeconds, endEpochSeconds);
        return leaderboard.getId();
    }

    public Map<String, Integer> getLeaderboard(String leaderboardId) {
        Leaderboard leaderboard = repository.getLeaderboard(leaderboardId);
        if (leaderboard == null) {
            throw new IllegalArgumentException("Leaderboard not found: " + leaderboardId);
        }
        return leaderboard.getUserScores();
    }

    public void submitScore(String gameId, String userId, int score) {
        if (!supportedGames.contains(gameId)) {
            throw new GameNotSupportedException("Game not supported: " + gameId);
        }

        if (!scoreValidationStrategy.isValid(score)) {
            throw new InvalidScoreException(scoreValidationStrategy.getErrorMessage());
        }

        List<Leaderboard> activeLeaderboards = repository.getActiveLeaderboardsForGame(gameId);
        for (Leaderboard leaderboard : activeLeaderboards) {
            leaderboard.updateScore(userId, score);
        }
    }

    public List<Map.Entry<String, Integer>> listPlayersNext(String gameId, String leaderboardId, String userId, int nPlayers) {
        Leaderboard leaderboard = repository.getLeaderboard(leaderboardId);
        if (leaderboard == null) {
            throw new IllegalArgumentException("Leaderboard not found: " + leaderboardId);
        }

        TreeMap<Integer, String> scoreToUser = leaderboard.getScoreToUser();
        Integer userScore = leaderboard.getUserScores().get(userId);
        if (userScore == null) {
            return new ArrayList<>();
        }

        return scoreToUser.entrySet().stream()
                .filter(entry -> entry.getKey() < userScore)
                .limit(nPlayers)
                .map(entry -> new AbstractMap.SimpleEntry<>(entry.getValue(), entry.getKey()))
                .collect(Collectors.toList());
    }

    public List<Map.Entry<String, Integer>> listPlayersPrev(String gameId, String leaderboardId, String userId, int nPlayers) {
        Leaderboard leaderboard = repository.getLeaderboard(leaderboardId);
        if (leaderboard == null) {
            throw new IllegalArgumentException("Leaderboard not found: " + leaderboardId);
        }

        TreeMap<Integer, String> scoreToUser = leaderboard.getScoreToUser();
        Integer userScore = leaderboard.getUserScores().get(userId);
        if (userScore == null) {
            return new ArrayList<>();
        }

        return scoreToUser.entrySet().stream()
                .filter(entry -> entry.getKey() > userScore)
                .limit(nPlayers)
                .map(entry -> new AbstractMap.SimpleEntry<>(entry.getValue(), entry.getKey()))
                .collect(Collectors.toList());
    }
} 