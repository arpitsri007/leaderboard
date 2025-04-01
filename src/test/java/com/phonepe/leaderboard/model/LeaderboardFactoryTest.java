package com.phonepe.leaderboard.model;

import com.phonepe.leaderboard.util.TimeProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class LeaderboardFactoryTest {
    private LeaderboardFactory factory;
    private static final String GAME_ID = "test-game";
    private static final long START_TIME = 1000L;
    private static final long END_TIME = 2000L;

    private static class MockTimeProvider implements TimeProvider {
        @Override
        public long getCurrentTimeInSeconds() {
            return START_TIME + 500;
        }
    }

    @BeforeEach
    void setUp() {
        factory = new LeaderboardFactory(new MockTimeProvider());
    }

    @Test
    void testCreateLeaderboard() {
        Leaderboard leaderboard = factory.createLeaderboard(GAME_ID, START_TIME, END_TIME);
        
        assertNotNull(leaderboard);
        assertNotNull(leaderboard.getId());
        assertEquals(GAME_ID, leaderboard.getGameId());
        assertEquals(START_TIME, leaderboard.getStartTime());
        assertEquals(END_TIME, leaderboard.getEndTime());
    }

    @Test
    void testCreateMultipleLeaderboards() {
        Leaderboard leaderboard1 = factory.createLeaderboard(GAME_ID, START_TIME, END_TIME);
        Leaderboard leaderboard2 = factory.createLeaderboard(GAME_ID, START_TIME + 1000, END_TIME + 1000);
        
        assertNotNull(leaderboard1.getId());
        assertNotNull(leaderboard2.getId());
        assertNotEquals(leaderboard1.getId(), leaderboard2.getId());
    }
} 