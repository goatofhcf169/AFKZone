package dev.alone.aFKZone.manager;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import dev.alone.aFKZone.AFKZone;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Manages WorldGuard region detection and caching
 */
public class RegionManager {

    private final AFKZone plugin;
    private final Cache<UUID, Boolean> regionCache;
    private RegionContainer container;
    private RegionQuery query;

    /**
     * Create a new RegionManager
     * @param plugin The plugin instance
     */
    public RegionManager(AFKZone plugin) {
        this.plugin = plugin;

        // Initialize cache with expiry based on config
        int cacheDuration = plugin.getConfigManager().getConfig()
            .getInt("performance.region-cache-duration", 20);

        this.regionCache = CacheBuilder.newBuilder()
            .expireAfterWrite(cacheDuration * 50L, TimeUnit.MILLISECONDS) // Convert ticks to ms
            .maximumSize(1000)
            .build();
    }

    /**
     * Initialize WorldGuard hook
     * @return true if successful
     */
    public boolean initialize() {
        try {
            container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            query = container.createQuery();
            plugin.getLogger().info("WorldGuard hook initialized successfully!");
            return true;
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to initialize WorldGuard hook: " + e.getMessage());
            return false;
        }
    }

    /**
     * Check if a player is in the AFK region
     * @param player The player to check
     * @return true if in AFK region
     */
    public boolean isInAFKRegion(Player player) {
        return checkWorldGuardRegion(player);
    }

    /**
     * Check WorldGuard region directly
     * @param player The player to check
     * @return true if in AFK region
     */
    private boolean checkWorldGuardRegion(Player player) {
        try {
            Location loc = player.getLocation();

            com.sk89q.worldguard.protection.managers.RegionManager regionManager =
                container.get(BukkitAdapter.adapt(loc.getWorld()));

            if (regionManager == null) {
                return false;
            }

            String regionName = plugin.getConfigManager().getRegionName();
            ApplicableRegionSet set = regionManager.getApplicableRegions(
                BukkitAdapter.asBlockVector(loc)
            );

            return set.getRegions().stream()
                .anyMatch(region -> region.getId().equalsIgnoreCase(regionName));

        } catch (Exception e) {
            if (plugin.getConfigManager().isDebug()) {
                plugin.getLogger().warning("Error checking WorldGuard region: " + e.getMessage());
            }
            return false;
        }
    }

    /**
     * Invalidate cache for a specific player
     * @param uuid The player's UUID
     */
    public void invalidateCache(UUID uuid) {
        regionCache.invalidate(uuid);
    }

    /**
     * Clear all cached data
     */
    public void clearCache() {
        regionCache.invalidateAll();
    }

    /**
     * Get cache statistics (for debugging)
     * @return Cache stats string
     */
    public String getCacheStats() {
        return "Region Cache - Size: " + regionCache.size() +
               ", Hit Rate: " + String.format("%.2f%%", regionCache.stats().hitRate() * 100);
    }
}
