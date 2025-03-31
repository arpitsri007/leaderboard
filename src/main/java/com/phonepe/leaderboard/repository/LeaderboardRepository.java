package com.phonepe.leaderboard.repository;

import com.phonepe.leaderboard.model.Leaderboard;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class LeaderboardRepository {
    private final Map<String, Leaderboard> leaderboards; // leaderboardId -> Leaderboard
    private final Map<String, Set<String>> gameLeaderboards; // gameId -> Set of leaderboardIds

    public LeaderboardRepository() {
        this.leaderboards = new ConcurrentHashMap<>();
        this.gameLeaderboards = new ConcurrentHashMap<>();
    }

    public Leaderboard createLeaderboard(String gameId, long startTime, long endTime) {
        String leaderboardId = UUID.randomUUID().toString();
        Leaderboard leaderboard = new Leaderboard(leaderboardId, gameId, startTime, endTime);
        
        leaderboards.put(leaderboardId, leaderboard);
        gameLeaderboards.computeIfAbsent(gameId, k -> new HashSet<>()).add(leaderboardId);
        
        return leaderboard;
    }

    public Leaderboard getLeaderboard(String leaderboardId) {
        return leaderboards.get(leaderboardId);
    }

    public Set<String> getLeaderboardIdsForGame(String gameId) {
        return gameLeaderboards.getOrDefault(gameId, new HashSet<>());
    }

    public List<Leaderboard> getActiveLeaderboardsForGame(String gameId) {
        Set<String> leaderboardIds = gameLeaderboards.getOrDefault(gameId, new HashSet<>());
        List<Leaderboard> activeLeaderboards = new ArrayList<>();
        
        for (String leaderboardId : leaderboardIds) {
            Leaderboard leaderboard = leaderboards.get(leaderboardId);
            if (leaderboard != null && leaderboard.isActive()) {
                activeLeaderboards.add(leaderboard);
            }
        }
        
        return activeLeaderboards;
    }

    public List<Leaderboard> getAllLeaderboards() {
        return new ArrayList<>(leaderboards.values());
    }
} 