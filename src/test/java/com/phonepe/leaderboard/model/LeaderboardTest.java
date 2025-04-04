package com.phonepe.leaderboard.model;

import com.phonepe.leaderboard.util.TimeProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;
import java.util.TreeMap;
import java.util.Set;

public class LeaderboardTest {
    private Leaderboard leaderboard;
    private static final String LEADERBOARD_ID = "test-leaderboard";
    private static final String GAME_ID = "test-game";
    private static final long START_TIME = 1000L;
    private static final long END_TIME = 2000L;
    private MockTimeProvider mockTimeProvider;

    private static class MockTimeProvider implements TimeProvider {
        private long currentTime = START_TIME + 500;

        @Override
        public long getCurrentTimeInSeconds() {
            return currentTime;
        }

        public void setCurrentTime(long time) {
            this.currentTime = time;
        }
    }

    @BeforeEach
    void setUp() {
        mockTimeProvider = new MockTimeProvider();
        leaderboard = new Leaderboard.Builder()
                .id(LEADERBOARD_ID)
                .gameId(GAME_ID)
                .startTime(START_TIME)
                .endTime(END_TIME)
                .timeProvider(mockTimeProvider)
                .build();
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
    void testBuilderWithMissingRequiredFields() {
        assertThrows(IllegalStateException.class, () -> 
            new Leaderboard.Builder()
                .id(LEADERBOARD_ID)
                .build()
        );
    }

    @Test
    void testUpdateScore() {
        // Test initial score update
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

        TreeMap<Integer, Set<String>> scoreToUser = leaderboard.getScoreToUser();
        assertEquals(3, scoreToUser.size());
        assertEquals(1, scoreToUser.get(2000).size());
        assertEquals(1, scoreToUser.get(1500).size());
        assertEquals(1, scoreToUser.get(1000).size());
        assertTrue(scoreToUser.get(2000).contains("user2"));
        assertTrue(scoreToUser.get(1500).contains("user3"));
        assertTrue(scoreToUser.get(1000).contains("user1"));
    }

    @Test
    void testIsActive() {
        // Mock current time to be within the leaderboard period
        mockTimeProvider.setCurrentTime(1500L);
        assertTrue(leaderboard.isActive());

        // Mock current time to be before start time
        mockTimeProvider.setCurrentTime(500L);
        assertFalse(leaderboard.isActive());

        // Mock current time to be after end time
        mockTimeProvider.setCurrentTime(2500L);
        assertFalse(leaderboard.isActive());

        // Mock current time to be exactly at start time
        mockTimeProvider.setCurrentTime(START_TIME);
        assertTrue(leaderboard.isActive());

        // Mock current time to be exactly at end time
        mockTimeProvider.setCurrentTime(END_TIME);
        assertTrue(leaderboard.isActive());
    }

    @Test
    void testMultipleUsersSameScore() {
        // Test multiple users with same score
        leaderboard.updateScore("user1", 1000);
        leaderboard.updateScore("user2", 1000);
        leaderboard.updateScore("user3", 1000);

        Map<String, Integer> userScores = leaderboard.getUserScores();
        assertEquals(3, userScores.size());
        assertEquals(1000, userScores.get("user1"));
        assertEquals(1000, userScores.get("user2"));
        assertEquals(1000, userScores.get("user3"));

        TreeMap<Integer, Set<String>> scoreToUser = leaderboard.getScoreToUser();
        assertEquals(1, scoreToUser.size());
        Set<String> usersAtScore = scoreToUser.get(1000);
        assertNotNull(usersAtScore);
        assertEquals(3, usersAtScore.size());
        assertTrue(usersAtScore.contains("user1"));
        assertTrue(usersAtScore.contains("user2"));
        assertTrue(usersAtScore.contains("user3"));
    }

    @Test
    void testUpdateScoreWithMultipleUsers() {
        // Set up initial scores
        leaderboard.updateScore("user1", 1000);
        leaderboard.updateScore("user2", 1000);
        leaderboard.updateScore("user3", 2000);

        // Verify initial state
        TreeMap<Integer, Set<String>> scoreToUser = leaderboard.getScoreToUser();
        assertEquals(2, scoreToUser.size());
        assertEquals(2, scoreToUser.get(1000).size());
        assertEquals(1, scoreToUser.get(2000).size());

        // Update user1's score
        leaderboard.updateScore("user1", 2000);
        
        // Verify user1 moved to higher score
        assertEquals(2, scoreToUser.size());
        assertEquals(1, scoreToUser.get(1000).size());
        assertEquals(2, scoreToUser.get(2000).size());
        assertTrue(scoreToUser.get(2000).contains("user1"));
        assertTrue(scoreToUser.get(2000).contains("user3"));
        assertTrue(scoreToUser.get(1000).contains("user2"));
    }

    @Test
    void testConcurrentScoreUpdates() throws InterruptedException {
        int numThreads = 10;
        int numUpdatesPerThread = 100;
        Thread[] threads = new Thread[numThreads];

        for (int i = 0; i < numThreads; i++) {
            final int threadId = i;
            threads[i] = new Thread(() -> {
                for (int j = 0; j < numUpdatesPerThread; j++) {
                    leaderboard.updateScore("user" + threadId, j);
                }
            });
            threads[i].start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        // Verify each user has their highest score
        for (int i = 0; i < numThreads; i++) {
            assertEquals(numUpdatesPerThread - 1, leaderboard.getUserScores().get("user" + i));
        }
    }

    @Test
    void testConcurrentScoreUpdatesWithSameScore() throws InterruptedException {
        int numThreads = 10;
        int numUpdatesPerThread = 100;
        Thread[] threads = new Thread[numThreads];

        for (int i = 0; i < numThreads; i++) {
            final int threadId = i;
            threads[i] = new Thread(() -> {
                for (int j = 0; j < numUpdatesPerThread; j++) {
                    leaderboard.updateScore("user" + threadId, j);
                }
            });
            threads[i].start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        // Verify each user has their highest score
        for (int i = 0; i < numThreads; i++) {
            assertEquals(numUpdatesPerThread - 1, leaderboard.getUserScores().get("user" + i));
        }

        // Verify all users with highest score are in the set
        TreeMap<Integer, Set<String>> scoreToUser = leaderboard.getScoreToUser();
        Set<String> usersAtHighestScore = scoreToUser.get(numUpdatesPerThread - 1);
        assertEquals(numThreads, usersAtHighestScore.size());
        for (int i = 0; i < numThreads; i++) {
            assertTrue(usersAtHighestScore.contains("user" + i));
        }
    }
} 