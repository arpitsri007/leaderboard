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

        TreeMap<Integer, Set<String>> scoreToUser = leaderboard.getScoreToUser();
        Integer userScore = leaderboard.getUserScores().get(userId);
        if (userScore == null) {
            return new ArrayList<>();
        }

        // 100 - [user1, user2, user3]
        // userscore - 100

        List<Map.Entry<String, Integer>> result = new ArrayList<>();
        for (Map.Entry<Integer, Set<String>> entry : scoreToUser.entrySet()) {
            if (entry.getKey() < userScore) {
                for (String user : entry.getValue()) {
                    if (result.size() >= nPlayers) break;
                    result.add(new AbstractMap.SimpleEntry<>(user, entry.getKey()));
                }
            }
            if (result.size() >= nPlayers) break;
        }
        return result;
    }

    public List<Map.Entry<String, Integer>> listPlayersPrev(String gameId, String leaderboardId, String userId, int nPlayers) {
        Leaderboard leaderboard = repository.getLeaderboard(leaderboardId);
        if (leaderboard == null) {
            throw new IllegalArgumentException("Leaderboard not found: " + leaderboardId);
        }

        TreeMap<Integer, Set<String>> scoreToUser = leaderboard.getScoreToUser();
        Integer userScore = leaderboard.getUserScores().get(userId);
        if (userScore == null) {
            return new ArrayList<>(); // User not found on leaderboard
        }

        List<Map.Entry<String, Integer>> allHigherPlayers = new ArrayList<>();
        // scoreToUser iterates from highest score to lowest
        for (Map.Entry<Integer, Set<String>> entry : scoreToUser.entrySet()) {
            if (entry.getKey() > userScore) {
                for (String user : entry.getValue()) {
                    // Add all users with scores higher than the target user
                    allHigherPlayers.add(new AbstractMap.SimpleEntry<>(user, entry.getKey()));
                }
            } else {
                // Since the map is sorted descending, we can stop once we reach scores <= userScore
                break;
            }
        }

        // Reverse the list to sort from just-above-userScore upwards
        Collections.reverse(allHigherPlayers);

        // Return the first nPlayers (the page immediately preceding the user)
        int endIndex = Math.min(nPlayers, allHigherPlayers.size());
        return allHigherPlayers.subList(0, endIndex);
    }
} 