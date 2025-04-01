# PhonePe Leaderboard Service

A scalable leaderboard service for managing game scores and rankings.

## Quick Start

1. Build the project:
```bash
mvn clean package
```

2. Run the demo:
```bash
java -jar target/leaderboard-1.0-SNAPSHOT.jar
```

## Features

- Multiple game support
- Daily/weekly leaderboards
- Score management (highest score retention)
- Score validation (0 to 1 billion)
- Ranking features:
  - Full leaderboard view
  - Players around a specific user
- Thread-safe operations

## Example Usage

```java
// Initialize service
LeaderboardService service = new LeaderboardService(
    new SystemTimeProvider(),
    new RangeScoreValidationStrategy(LeaderboardConfig.MIN_SCORE, LeaderboardConfig.MAX_SCORE)
);

// Add games
service.addSupportedGame("PUBG_MOBILE");
service.addSupportedGame("CRICKET");

// Create leaderboard
int currentTime = (int)(System.currentTimeMillis() / 1000);
String leaderboardId = service.createLeaderboard("PUBG_MOBILE", currentTime, currentTime + 86400);

// Submit scores
service.submitScore("PUBG_MOBILE", "user1", 1000);
service.submitScore("PUBG_MOBILE", "user2", 2000);

// Get leaderboard
Map<String, Integer> leaderboard = service.getLeaderboard(leaderboardId);

// Get players around a user
List<Map.Entry<String, Integer>> nextPlayers = service.listPlayersNext("PUBG_MOBILE", leaderboardId, "user2", 2);
List<Map.Entry<String, Integer>> prevPlayers = service.listPlayersPrev("PUBG_MOBILE", leaderboardId, "user2", 2);
```

## Requirements

- Java 11 or higher
- Maven (for building)

## API

### LeaderboardService

- `addSupportedGame(String gameId)`: Add a supported game
- `createLeaderboard(String gameId, int startTime, int endTime)`: Create a new leaderboard
- `submitScore(String gameId, String userId, int score)`: Submit a score
- `getLeaderboard(String leaderboardId)`: Get leaderboard data
- `listPlayersNext(String gameId, String leaderboardId, String userId, int nPlayers)`: Get next N players
- `listPlayersPrev(String gameId, String leaderboardId, String userId, int nPlayers)`: Get previous N players

## Error Handling

- `GameNotSupportedException`: For unsupported games
- `InvalidScoreException`: For scores outside valid range
- `IllegalArgumentException`: For other invalid inputs
