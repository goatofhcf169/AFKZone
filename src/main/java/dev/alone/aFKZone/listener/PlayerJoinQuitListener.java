package dev.alone.aFKZone.listener;

import dev.alone.aFKZone.AFKZone;
import dev.alone.aFKZone.data.AFKPlayer;
import dev.alone.aFKZone.util.FoliaScheduler;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Listens for player join/quit events to manage AFK data
 */
public class PlayerJoinQuitListener implements Listener {

    private final AFKZone plugin;

    /**
     * Create a new PlayerJoinQuitListener
     * @param plugin The plugin instance
     */
    public PlayerJoinQuitListener(AFKZone plugin) {
        this.plugin = plugin;
    }

    /**
     * Handle player join
     * Uses AsyncScheduler for data loading and EntityScheduler for player updates
     * @param event The PlayerJoinEvent
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Load or create player data
        if (plugin.getDataManager() != null) {
            FoliaScheduler.runAsync(plugin, () -> {
                AFKPlayer loadedPlayer = plugin.getDataManager().loadPlayerData(player.getUniqueId());

                FoliaScheduler.runEntity(plugin, player, () -> {
                    AFKPlayer afkPlayer;
                    if (loadedPlayer == null) {
                        afkPlayer = new AFKPlayer(player.getUniqueId());
                    } else {
                        afkPlayer = loadedPlayer;
                    }

                    afkPlayer.setName(player.getName());

                    plugin.getAFKManager().addPlayer(afkPlayer);

                    if (plugin.getConfigManager().isDebug()) {
                        plugin.getLogger().info("Loaded AFK data for " + player.getName());
                    }
                });
            });
        } else {
            // If data manager not initialized, create new player
            AFKPlayer afkPlayer = new AFKPlayer(player.getUniqueId());
            afkPlayer.setName(player.getName());
            plugin.getAFKManager().addPlayer(afkPlayer);
        }
    }

    /**
     * Handle player quit
     * Uses AsyncScheduler for data saving
     * @param event The PlayerQuitEvent
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        AFKPlayer afkPlayer = plugin.getAFKManager().getAFKPlayer(player.getUniqueId());

        if (afkPlayer != null) {
            // Exit region if they're in it
            if (afkPlayer.isInRegion()) {
                afkPlayer.exitRegion();
            }

            // Save data asynchronously
            if (plugin.getDataManager() != null && plugin.getConfigManager().isPersistData()) {
                final AFKPlayer finalAfkPlayer = afkPlayer;
                FoliaScheduler.runAsync(plugin, () -> {
                    plugin.getDataManager().savePlayerData(finalAfkPlayer);

                    if (plugin.getConfigManager().isDebug()) {
                        plugin.getLogger().info("Saved AFK data for " + player.getName());
                    }
                });
            }

            // Remove from memory if persistence is disabled
            if (!plugin.getConfigManager().isPersistData()) {
                plugin.getAFKManager().removePlayer(player.getUniqueId());
            }
        }
    }
}
