package dev.alone.aFKZone.listener;

import dev.alone.aFKZone.AFKZone;
import dev.alone.aFKZone.util.FoliaScheduler;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * Listens for player movement to detect region entry/exit
 */
public class PlayerMoveListener implements Listener {

    private final AFKZone plugin;

    /**
     * Create a new PlayerMoveListener
     * @param plugin The plugin instance
     */
    public PlayerMoveListener(AFKZone plugin) {
        this.plugin = plugin;
    }

    /**
     * Handle player movement
     * Uses AsyncScheduler for region checks and EntityScheduler for player updates
     * @param event The PlayerMoveEvent
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        // Only check if player moved to a different block
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() &&
            event.getFrom().getBlockY() == event.getTo().getBlockY() &&
            event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }

        Player player = event.getPlayer();

        // Check region status asynchronously if enabled
        if (plugin.getConfigManager().getConfig().getBoolean("performance.async-region-checks", true)) {
            FoliaScheduler.runAsync(plugin, () -> {
                boolean inRegion = plugin.getRegionManager().isInAFKRegion(player);

                // Update status on the player's region thread
                FoliaScheduler.runEntity(plugin, player, () -> {
                    plugin.getAFKManager().updateRegionStatus(player, inRegion);
                });
            });
        } else {
            // Sync check on player's region thread
            boolean inRegion = plugin.getRegionManager().isInAFKRegion(player);
            plugin.getAFKManager().updateRegionStatus(player, inRegion);
        }
    }
}
