package dev.alone.aFKZone.manager;

import dev.alone.aFKZone.AFKZone;
import dev.alone.aFKZone.data.AFKPlayer;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Manages leaderboard data and rankings
 */
public class LeaderboardManager {

    private final AFKZone plugin;

    /**
     * Create a new LeaderboardManager
     * @param plugin The plugin instance
     */
    public LeaderboardManager(AFKZone plugin) {
        this.plugin = plugin;
    }

    /**
     * Get top players by total rewards received
     * @param limit Maximum number of players to return
     * @return List of top AFKPlayers sorted by total rewards
     */
    public List<AFKPlayer> getTopByRewards(int limit) {
        return plugin.getAFKManager().getAllPlayers().stream()
            .sorted(Comparator.comparingInt(AFKPlayer::getTotalRewards).reversed())
            .limit(limit)
            .collect(Collectors.toList());
    }

    /**
     * Get top players by total AFK time
     * @param limit Maximum number of players to return
     * @return List of top AFKPlayers sorted by total AFK time
     */
    public List<AFKPlayer> getTopByTime(int limit) {
        return plugin.getAFKManager().getAllPlayers().stream()
            .sorted(Comparator.comparingLong(AFKPlayer::getTotalAFKTime).reversed())
            .limit(limit)
            .collect(Collectors.toList());
    }

    /**
     * Get a player's rank by total rewards
     * @param afkPlayer The AFKPlayer to check
     * @return The player's rank (1-indexed), or -1 if not found
     */
    public int getRankByRewards(AFKPlayer afkPlayer) {
        List<AFKPlayer> sorted = plugin.getAFKManager().getAllPlayers().stream()
            .sorted(Comparator.comparingInt(AFKPlayer::getTotalRewards).reversed())
            .collect(Collectors.toList());

        for (int i = 0; i < sorted.size(); i++) {
            if (sorted.get(i).getUuid().equals(afkPlayer.getUuid())) {
                return i + 1; // 1-indexed
            }
        }

        return -1;
    }

    /**
     * Get a player's rank by total AFK time
     * @param afkPlayer The AFKPlayer to check
     * @return The player's rank (1-indexed), or -1 if not found
     */
    public int getRankByTime(AFKPlayer afkPlayer) {
        List<AFKPlayer> sorted = plugin.getAFKManager().getAllPlayers().stream()
            .sorted(Comparator.comparingLong(AFKPlayer::getTotalAFKTime).reversed())
            .collect(Collectors.toList());

        for (int i = 0; i < sorted.size(); i++) {
            if (sorted.get(i).getUuid().equals(afkPlayer.getUuid())) {
                return i + 1; // 1-indexed
            }
        }

        return -1;
    }

    /**
     * Leaderboard sort type
     */
    public enum SortType {
        REWARDS,
        TIME
    }
}
