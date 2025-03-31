package com.phonepe.leaderboard.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;
import java.util.TreeMap;

public class LeaderboardTest {
    private Leaderboard leaderboard;
    private static final String LEADERBOARD_ID = "test-leaderboard";
    private static final String GAME_ID = "test-game";
    private static final long START_TIME = 1000L;
    private static final long END_TIME = 2000L;

    @BeforeEach
    void setUp() {
        leaderboard = new Leaderboard(LEADERBOARD_ID, GAME_ID, START_TIME, END_TIME);
    }

    @Test
    void testLeaderboardCreation() {
        assertEquals(LEADERBOARD_ID, leaderboard.getId());
        assertEquals(GAME_ID, leaderboard.getGameId());
        assertEquals(START_TIME, leaderboard.getStartTime());
        assertEquals(END_TIME, leaderboard.getEndTime());
        assertTrue(leaderboard.getUserScores().isEmpty());
        assertTrue(leaderboard.getScoreToUser().isEmpty());
    }

    @Test
    void testUpdateScore() {
        // Test first score submission
        leaderboard.updateScore("user1", 1000);
        assertEquals(1000, leaderboard.getUserScores().get("user1"));
        assertEquals("user1", leaderboard.getScoreToUser().get(1000));

        // Test higher score update
        leaderboard.updateScore("user1", 2000);
        assertEquals(2000, leaderboard.getUserScores().get("user1"));
        assertEquals("user1", leaderboard.getScoreToUser().get(2000));
        assertNull(leaderboard.getScoreToUser().get(1000));

        // Test lower score update (should not update)
        leaderboard.updateScore("user1", 1500);
        assertEquals(2000, leaderboard.getUserScores().get("user1"));
        assertEquals("user1", leaderboard.getScoreToUser().get(2000));
    }

    @Test
    void testMultipleUsers() {
        leaderboard.updateScore("user1", 1000);
        leaderboard.updateScore("user2", 2000);
        leaderboard.updateScore("user3", 1500);

        Map<String, Integer> userScores = leaderboard.getUserScores();
        assertEquals(3, userScores.size());
        assertEquals(1000, userScores.get("user1"));
        assertEquals(2000, userScores.get("user2"));
        assertEquals(1500, userScores.get("user3"));

        TreeMap<Integer, String> scoreToUser = leaderboard.getScoreToUser();
        assertEquals(3, scoreToUser.size());
        assertEquals("user2", scoreToUser.get(2000));
        assertEquals("user3", scoreToUser.get(1500));
        assertEquals("user1", scoreToUser.get(1000));
    }

    @Test
    void testIsActive() {
        // Mock current time to be within the leaderboard period
        long currentTime = 1500L;
        assertTrue(leaderboard.isActive());

        // Mock current time to be before start time
        currentTime = 500L;
        assertFalse(leaderboard.isActive());

        // Mock current time to be after end time
        currentTime = 2500L;
        assertFalse(leaderboard.isActive());
    }
} 