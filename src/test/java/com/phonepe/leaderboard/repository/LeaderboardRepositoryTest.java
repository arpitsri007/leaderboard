package com.phonepe.leaderboard.repository;

import com.phonepe.leaderboard.model.Leaderboard;
import com.phonepe.leaderboard.util.TimeProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Set;

public class LeaderboardRepositoryTest {
    private LeaderboardRepository repository;
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
        repository = new LeaderboardRepository(mockTimeProvider);
    }

    @Test
    void testCreateLeaderboard() {
        Leaderboard leaderboard = repository.createLeaderboard(GAME_ID, START_TIME, END_TIME);
        
        assertNotNull(leaderboard);
        assertEquals(GAME_ID, leaderboard.getGameId());
        assertEquals(START_TIME, leaderboard.getStartTime());
        assertEquals(END_TIME, leaderboard.getEndTime());
        
        // Verify leaderboard is stored
        Leaderboard retrieved = repository.getLeaderboard(leaderboard.getId());
        assertNotNull(retrieved);
        assertEquals(leaderboard.getId(), retrieved.getId());
    }

    @Test
    void testGetLeaderboardIdsForGame() {
        // Create multiple leaderboards for the same game
        Leaderboard leaderboard1 = repository.createLeaderboard(GAME_ID, START_TIME, END_TIME);
        Leaderboard leaderboard2 = repository.createLeaderboard(GAME_ID, START_TIME + 1000, END_TIME + 1000);
        
        Set<String> leaderboardIds = repository.getLeaderboardIdsForGame(GAME_ID);
        
        assertEquals(2, leaderboardIds.size());
        assertTrue(leaderboardIds.contains(leaderboard1.getId()));
        assertTrue(leaderboardIds.contains(leaderboard2.getId()));
    }

    @Test
    void testGetActiveLeaderboardsForGame() {
        // Create active and inactive leaderboards
        Leaderboard activeLeaderboard = repository.createLeaderboard(GAME_ID, START_TIME, END_TIME);
        Leaderboard inactiveLeaderboard = repository.createLeaderboard(GAME_ID, START_TIME - 1000, START_TIME - 1);
        
        // Set time to be between START_TIME and END_TIME
        mockTimeProvider.setCurrentTime(START_TIME + 100);
        
        List<Leaderboard> activeLeaderboards = repository.getActiveLeaderboardsForGame(GAME_ID);
        
        assertEquals(1, activeLeaderboards.size());
        assertEquals(activeLeaderboard.getId(), activeLeaderboards.get(0).getId());
    }

    @Test
    void testGetAllLeaderboards() {
        // Create leaderboards for different games
        Leaderboard leaderboard1 = repository.createLeaderboard(GAME_ID, START_TIME, END_TIME);
        Leaderboard leaderboard2 = repository.createLeaderboard("another-game", START_TIME, END_TIME);
        
        List<Leaderboard> allLeaderboards = repository.getAllLeaderboards();
        
        assertEquals(2, allLeaderboards.size());
        assertTrue(allLeaderboards.stream().anyMatch(lb -> lb.getId().equals(leaderboard1.getId())));
        assertTrue(allLeaderboards.stream().anyMatch(lb -> lb.getId().equals(leaderboard2.getId())));
    }

    @Test
    void testGetNonExistentLeaderboard() {
        Leaderboard leaderboard = repository.getLeaderboard("non-existent-id");
        assertNull(leaderboard);
    }
} 