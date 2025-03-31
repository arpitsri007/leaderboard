package com.phonepe.leaderboard;

import com.phonepe.leaderboard.service.LeaderboardService;
import java.util.List;
import java.util.Map;

public class LeaderboardDemo {
    public static void main(String[] args) {
        LeaderboardService service = new LeaderboardService();
        
        // Add supported games
        service.addSupportedGame("game1");
        service.addSupportedGame("game2");
        
        // Create a daily leaderboard for game1
        int currentTime = (int)(System.currentTimeMillis() / 1000);
        String leaderboardId = service.createLeaderboard("game1", currentTime, currentTime + 86400); // 24 hours
        
        // Submit some scores
        service.submitScore("game1", "user1", 1000);
        service.submitScore("game1", "user2", 2000);
        service.submitScore("game1", "user3", 1500);
        service.submitScore("game1", "user1", 2500);
        service.submitScore("game1", "user2", 1800); // This will not update user1's score
        
        // Get the full leaderboard
        Map<String, Integer> leaderboard = service.getLeaderboard(leaderboardId);
        System.out.println("Full Leaderboard:");
        leaderboard.forEach((userId, score) -> System.out.println(userId + ": " + score));
        
        // Get players around user2
        System.out.println("\nPlayers around user2:");
        List<Map.Entry<String, Integer>> nextPlayers = service.listPlayersNext("game1", leaderboardId, "user2", 2);
        List<Map.Entry<String, Integer>> prevPlayers = service.listPlayersPrev("game1", leaderboardId, "user2", 2);
        
        System.out.println("Previous players:");
        prevPlayers.forEach(entry -> System.out.println(entry.getKey() + ": " + entry.getValue()));
        System.out.println("Next players:");
        nextPlayers.forEach(entry -> System.out.println(entry.getKey() + ": " + entry.getValue()));
    }
} 