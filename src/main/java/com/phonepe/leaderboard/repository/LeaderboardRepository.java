package com.phonepe.leaderboard.repository;

import com.phonepe.leaderboard.model.Leaderboard;
import com.phonepe.leaderboard.model.LeaderboardFactory;
import com.phonepe.leaderboard.util.TimeProvider;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class LeaderboardRepository {
    private final Map<String, Leaderboard> leaderboards; // leaderboardId -> Leaderboard
    private final Map<String, Set<String>> gameLeaderboards; // gameId -> Set of leaderboardIds
    private final LeaderboardFactory leaderboardFactory;

    public LeaderboardRepository(TimeProvider timeProvider) {
        this.leaderboards = new ConcurrentHashMap<>();
        this.gameLeaderboards = new ConcurrentHashMap<>();
        this.leaderboardFactory = new LeaderboardFactory(timeProvider);
    }

    public Leaderboard createLeaderboard(String gameId, long startTime, long endTime) {
        Leaderboard leaderboard = leaderboardFactory.createLeaderboard(gameId, startTime, endTime);
        
        leaderboards.put(leaderboard.getId(), leaderboard);
        gameLeaderboards.computeIfAbsent(gameId, k -> new HashSet<>()).add(leaderboard.getId());
        
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