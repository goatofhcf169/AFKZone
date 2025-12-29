package dev.alone.aFKZone.listener;

import dev.alone.aFKZone.AFKZone;
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
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                boolean inRegion = plugin.getRegionManager().isInAFKRegion(player);

                // Update status on main thread
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    plugin.getAFKManager().updateRegionStatus(player, inRegion);
                });
            });
        } else {
            // Sync check
            boolean inRegion = plugin.getRegionManager().isInAFKRegion(player);
            plugin.getAFKManager().updateRegionStatus(player, inRegion);
        }
    }
}
