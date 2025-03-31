# PhonePe Leaderboard Service

A scalable and efficient leaderboard management service for managing game scores and rankings. This service supports multiple games, concurrent score submissions, and various leaderboard configurations.

## Features

- **Multiple Game Support**: Host leaderboards for multiple games simultaneously
- **Flexible Leaderboard Configuration**: Create daily, weekly, or custom duration leaderboards
- **Score Management**:
  - Automatic score submission to all active leaderboards
  - Only highest score retention per user
  - Score validation (0 to 1 billion)
- **Ranking Features**:
  - Full leaderboard view
  - Top X players query
  - Players around a specific user (X players above and below)
- **Historical Access**: Access completed leaderboards for reward distribution
- **Thread-Safe Operations**: Handles concurrent score submissions efficiently

## Technical Stack

- Java 11
- Maven
- JUnit 5 for testing
- In-memory storage with thread-safe collections

## Project Structure

```
src/
├── main/java/com/phonepe/leaderboard/
│   ├── model/
│   │   └── Leaderboard.java
│   ├── repository/
│   │   └── LeaderboardRepository.java
│   └── service/
│       └── LeaderboardService.java
└── test/java/com/phonepe/leaderboard/
    ├── model/
    │   └── LeaderboardTest.java
    ├── repository/
    │   └── LeaderboardRepositoryTest.java
    └── service/
        └── LeaderboardServiceTest.java
```

## Setup

1. Ensure you have Java 11 and Maven installed
2. Clone the repository
3. Navigate to the project directory
4. Build the project:
   ```bash
   mvn clean install
   ```

## Usage Examples

### Basic Usage

```java
// Create a leaderboard service
LeaderboardService service = new LeaderboardService();

// Add supported games
service.addSupportedGame("game1");
service.addSupportedGame("game2");

// Create a daily leaderboard
int currentTime = (int)(System.currentTimeMillis() / 1000);
String leaderboardId = service.createLeaderboard("game1", currentTime, currentTime + 86400);

// Submit scores
service.submitScore("game1", "user1", 1000);
service.submitScore("game1", "user2", 2000);
service.submitScore("game1", "user3", 1500);

// Get the full leaderboard
Map<String, Integer> leaderboard = service.getLeaderboard(leaderboardId);

// Get players around a specific user
List<Map.Entry<String, Integer>> nextPlayers = service.listPlayersNext("game1", leaderboardId, "user2", 2);
List<Map.Entry<String, Integer>> prevPlayers = service.listPlayersPrev("game1", leaderboardId, "user2", 2);
```

## API Reference

### LeaderboardService

- `void addSupportedGame(String gameId)`: Add a supported game
- `List<String> getSupportedGames()`: Get list of supported games
- `String createLeaderboard(String gameId, int startEpochSeconds, int endEpochSeconds)`: Create a new leaderboard
- `Map<String, Integer> getLeaderboard(String leaderboardId)`: Get leaderboard data
- `void submitScore(String gameId, String userId, int score)`: Submit a score
- `List<Map.Entry<String, Integer>> listPlayersNext(String gameId, String leaderboardId, String userId, int nPlayers)`: Get next N players
- `List<Map.Entry<String, Integer>> listPlayersPrev(String gameId, String leaderboardId, String userId, int nPlayers)`: Get previous N players

## Testing

Run the test suite:
```bash
mvn test
```

The test suite includes:
- Unit tests for Leaderboard model
- Repository layer tests
- Service layer tests
- Edge case and validation tests

## Design Considerations

1. **Data Structures**:
   - `ConcurrentHashMap` for thread-safe user score storage
   - `TreeMap` for efficient score-based ranking
   - Separate maps for user->score and score->user mappings

2. **Concurrency**:
   - Thread-safe operations for score submissions
   - Atomic score updates
   - Safe leaderboard access

3. **Performance**:
   - O(log n) for score updates
   - O(log n) for rank queries
   - Efficient memory usage

4. **Scalability**:
   - Support for multiple games
   - Independent leaderboard configurations
   - Extensible design

## Assumptions

1. Scores are integers between 0 and 1 billion
2. Each user can have only one score per leaderboard
3. Only the highest score is retained for each user
4. Leaderboard time ranges are in epoch seconds
5. In-memory storage is sufficient for the use case

## Future Enhancements

1. Persistent storage integration
2. Caching layer for frequently accessed leaderboards
3. Real-time updates using WebSocket
4. Analytics and reporting features
5. Rate limiting and throttling
6. Distributed leaderboard support 