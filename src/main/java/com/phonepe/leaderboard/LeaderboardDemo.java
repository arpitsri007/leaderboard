package com.phonepe.leaderboard;

import com.phonepe.leaderboard.config.LeaderboardConfig;
import com.phonepe.leaderboard.service.LeaderboardService;
import com.phonepe.leaderboard.util.SystemTimeProvider;
import com.phonepe.leaderboard.validation.RangeScoreValidationStrategy;
import java.util.List;
import java.util.Map;

public class LeaderboardDemo {
    public static void main(String[] args) {
        LeaderboardService service = new LeaderboardService(
            new SystemTimeProvider(),
            new RangeScoreValidationStrategy(LeaderboardConfig.MIN_SCORE, LeaderboardConfig.MAX_SCORE)
        );
        
        service.addSupportedGame("PUBG_MOBILE");
        service.addSupportedGame("CRICKET");
        
        int currentTime = (int)(System.currentTimeMillis() / 1000);
        String leaderboardId = service.createLeaderboard("PUBG_MOBILE", currentTime, currentTime + 86400); // 24 hours
        
   
        service.submitScore("PUBG_MOBILE", "user1", 1000);
        service.submitScore("PUBG_MOBILE", "user2", 2000);
        service.submitScore("PUBG_MOBILE", "user3", 1500);
        service.submitScore("PUBG_MOBILE", "user1", 2500);
        service.submitScore("PUBG_MOBILE", "user2", 1800); // This will not update user1's score
        

        Map<String, Integer> leaderboard = service.getLeaderboard(leaderboardId);
        System.out.println("Full Leaderboard:");
        leaderboard.forEach((userId, score) -> System.out.println(userId + ": " + score));
        
        // Get players around user2
        System.out.println("\nPlayers around user2:");
        List<Map.Entry<String, Integer>> nextPlayers = service.listPlayersNext("PUBG_MOBILE", leaderboardId, "user2", 2);
        List<Map.Entry<String, Integer>> prevPlayers = service.listPlayersPrev("PUBG_MOBILE", leaderboardId, "user2", 2);
        
        System.out.println("Previous players:");
        prevPlayers.forEach(entry -> System.out.println(entry.getKey() + ": " + entry.getValue()));
        System.out.println("Next players:");
        nextPlayers.forEach(entry -> System.out.println(entry.getKey() + ": " + entry.getValue()));
    }
} 