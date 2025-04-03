package com.phonepe.leaderboard.service;

import com.phonepe.leaderboard.config.LeaderboardConfig;
import com.phonepe.leaderboard.exception.GameNotSupportedException;
import com.phonepe.leaderboard.exception.InvalidScoreException;
import com.phonepe.leaderboard.util.TimeProvider;
import com.phonepe.leaderboard.validation.RangeScoreValidationStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;

public class LeaderboardServiceTest {
    private LeaderboardService service;
    private static final String GAME_ID = "test-game";
    private static final String USER_ID = "test-user";
    private static final int START_TIME = 1000;
    private static final int END_TIME = 2000;

    private static class MockTimeProvider implements TimeProvider {
        private long currentTime = START_TIME + 500;

        @Override
        public long getCurrentTimeInSeconds() {
            return currentTime;
        }

//        public void setCurrentTime(long time) {
//            this.currentTime = time;
//        }
    }

    @BeforeEach
    void setUp() {
        service = new LeaderboardService(
            new MockTimeProvider(),
            new RangeScoreValidationStrategy(LeaderboardConfig.MIN_SCORE, LeaderboardConfig.MAX_SCORE)
        );
        service.addSupportedGame(GAME_ID);
    }

    @Test
    void testAddSupportedGame() {
        service.addSupportedGame("new-game");
        List<String> supportedGames = service.getSupportedGames();
        assertTrue(supportedGames.contains("new-game"));
    }

    @Test
    void testCreateLeaderboard() {
        String leaderboardId = service.createLeaderboard(GAME_ID, START_TIME, END_TIME);
        assertNotNull(leaderboardId);
        
        Map<String, Integer> leaderboard = service.getLeaderboard(leaderboardId);
        assertNotNull(leaderboard);
        assertTrue(leaderboard.isEmpty());
    }

    @Test
    void testCreateLeaderboardForUnsupportedGame() {
        assertThrows(GameNotSupportedException.class, () -> 
            service.createLeaderboard("unsupported-game", START_TIME, END_TIME)
        );
    }

    @Test
    void testCreateLeaderboardWithInvalidTimeRange() {
        assertThrows(IllegalArgumentException.class, () -> 
            service.createLeaderboard(GAME_ID, END_TIME, START_TIME)
        );
    }

    @Test
    void testSubmitScore() {
        String leaderboardId = service.createLeaderboard(GAME_ID, START_TIME, END_TIME);
        service.submitScore(GAME_ID, USER_ID, 1000);
        
        Map<String, Integer> leaderboard = service.getLeaderboard(leaderboardId);
        assertEquals(1000, leaderboard.get(USER_ID));
    }

    @Test
    void testSubmitScoreForUnsupportedGame() {
        assertThrows(GameNotSupportedException.class, () -> 
            service.submitScore("unsupported-game", USER_ID, 1000)
        );
    }

    @Test
    void testSubmitInvalidScore() {
        assertThrows(InvalidScoreException.class, () -> 
            service.submitScore(GAME_ID, USER_ID, LeaderboardConfig.MIN_SCORE - 1)
        );
        assertThrows(InvalidScoreException.class, () -> 
            service.submitScore(GAME_ID, USER_ID, LeaderboardConfig.MAX_SCORE + 1)
        );
    }

    @Test
    void testUpdateScore() {
        String leaderboardId = service.createLeaderboard(GAME_ID, START_TIME, END_TIME);
        
        // Submit initial score
        service.submitScore(GAME_ID, USER_ID, 1000);
        Map<String, Integer> leaderboard = service.getLeaderboard(leaderboardId);
        assertEquals(1000, leaderboard.get(USER_ID));
        
        // Submit higher score
        service.submitScore(GAME_ID, USER_ID, 2000);
        leaderboard = service.getLeaderboard(leaderboardId);
        assertEquals(2000, leaderboard.get(USER_ID));
        
        // Submit lower score (should not update)
        service.submitScore(GAME_ID, USER_ID, 1500);
        leaderboard = service.getLeaderboard(leaderboardId);
        assertEquals(2000, leaderboard.get(USER_ID));
    }

    @Test
    void testListPlayersNext() {
        String leaderboardId = service.createLeaderboard(GAME_ID, START_TIME, END_TIME);
        
        // Add multiple users with different scores
        service.submitScore(GAME_ID, "user1", 1000);
        service.submitScore(GAME_ID, "user2", 2000);
        service.submitScore(GAME_ID, "user3", 1500);
        
        List<Map.Entry<String, Integer>> nextPlayers = service.listPlayersNext(GAME_ID, leaderboardId, "user2", 1);
        assertEquals(1, nextPlayers.size());
        assertEquals("user3", nextPlayers.get(0).getKey());
        assertEquals(1500, nextPlayers.get(0).getValue());
    }

    @Test
    void testListPlayersPrev() {
        String leaderboardId = service.createLeaderboard(GAME_ID, START_TIME, END_TIME);
        
        // Add multiple users with different scores
        service.submitScore(GAME_ID, "user1", 1000);
        service.submitScore(GAME_ID, "user2", 2000);
        service.submitScore(GAME_ID, "user3", 1500);
        
        List<Map.Entry<String, Integer>> prevPlayers = service.listPlayersPrev(GAME_ID, leaderboardId, "user3", 1);
        assertEquals(1, prevPlayers.size());
        assertEquals("user2", prevPlayers.get(0).getKey());
        assertEquals(2000, prevPlayers.get(0).getValue());
    }

    @Test
    void testListPlayersForNonExistentUser() {
        String leaderboardId = service.createLeaderboard(GAME_ID, START_TIME, END_TIME);
        
        List<Map.Entry<String, Integer>> nextPlayers = service.listPlayersNext(GAME_ID, leaderboardId, "non-existent", 1);
        assertTrue(nextPlayers.isEmpty());
        
        List<Map.Entry<String, Integer>> prevPlayers = service.listPlayersPrev(GAME_ID, leaderboardId, "non-existent", 1);
        assertTrue(prevPlayers.isEmpty());
    }

    @Test
    void testGetNonExistentLeaderboard() {
        assertThrows(IllegalArgumentException.class, () -> 
            service.getLeaderboard("non-existent-id")
        );
    }
} 