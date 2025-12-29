package dev.alone.aFKZone.manager;

import dev.alone.aFKZone.AFKZone;
import dev.alone.aFKZone.data.AFKPlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages AFK player data and tracking
 */
public class AFKManager {

    private final AFKZone plugin;
    private final Map<UUID, AFKPlayer> afkPlayers;

    /**
     * Create a new AFKManager
     * @param plugin The plugin instance
     */
    public AFKManager(AFKZone plugin) {
        this.plugin = plugin;
        this.afkPlayers = new ConcurrentHashMap<>();
    }

    /**
     * Get or create an AFKPlayer
     * @param uuid The player's UUID
     * @return The AFKPlayer instance
     */
    public AFKPlayer getAFKPlayer(UUID uuid) {
        return afkPlayers.get(uuid);
    }

    /**
     * Get or create an AFKPlayer
     * @param player The player
     * @return The AFKPlayer instance
     */
    public AFKPlayer getOrCreateAFKPlayer(Player player) {
        return afkPlayers.computeIfAbsent(player.getUniqueId(), uuid -> {
            AFKPlayer afkPlayer = new AFKPlayer(uuid);
            afkPlayer.setName(player.getName());
            return afkPlayer;
        });
    }

    /**
     * Add or update an AFKPlayer
     * @param afkPlayer The AFKPlayer to add
     */
    public void addPlayer(AFKPlayer afkPlayer) {
        afkPlayers.put(afkPlayer.getUuid(), afkPlayer);
    }

    /**
     * Remove a player from tracking
     * @param uuid The player's UUID
     */
    public void removePlayer(UUID uuid) {
        afkPlayers.remove(uuid);
    }

    /**
     * Get all tracked AFK players
     * @return Collection of all AFKPlayers
     */
    public Collection<AFKPlayer> getAllPlayers() {
        return afkPlayers.values();
    }

    /**
     * Get all players currently in the AFK region
     * @return List of AFKPlayers in region
     */
    public List<AFKPlayer> getPlayersInRegion() {
        List<AFKPlayer> inRegion = new ArrayList<>();
        for (AFKPlayer afkPlayer : afkPlayers.values()) {
            if (afkPlayer.isInRegion()) {
                inRegion.add(afkPlayer);
            }
        }
        return inRegion;
    }

    /**
     * Handle player entering the AFK region
     * @param player The player
     */
    public void handleEnterRegion(Player player) {
        AFKPlayer afkPlayer = getOrCreateAFKPlayer(player);

        if (!afkPlayer.isInRegion()) {
            afkPlayer.enterRegion();

            // Send title if enabled
            if (plugin.getConfigManager().isTitleEnabled()) {
                String titleText = plugin.getConfigManager().getMessages()
                    .getString("titles.enter-region.title", "<green><bold>AFK POOL</bold></green>");
                String subtitleText = plugin.getConfigManager().getMessages()
                    .getString("titles.enter-region.subtitle", "<gray>Stay here to earn rewards!</gray>");

                Component titleComponent = dev.alone.aFKZone.util.MessageUtil.toComponent(titleText);
                Component subtitleComponent = dev.alone.aFKZone.util.MessageUtil.toComponent(subtitleText);

                Title title = Title.title(
                    titleComponent,
                    subtitleComponent,
                    Title.Times.times(
                        Duration.ofMillis(plugin.getConfigManager().getTitleFadeIn() * 50L),
                        Duration.ofMillis(plugin.getConfigManager().getTitleStay() * 50L),
                        Duration.ofMillis(plugin.getConfigManager().getTitleFadeOut() * 50L)
                    )
                );

                player.showTitle(title);
            }

            // Send chat message
            String message = plugin.getConfigManager().getMessages()
                .getString("messages.enter-region", "%prefix% <green>You entered the AFK Pool! Earn rewards every <yellow>%reward_interval%</yellow>.</green>");
            String prefix = plugin.getConfigManager().getPrefix();
            message = message.replace("%prefix%", prefix);
            message = message.replace("%reward_interval%",
                dev.alone.aFKZone.util.MessageUtil.formatTime(plugin.getConfigManager().getRewardInterval()));

            Component messageComponent = dev.alone.aFKZone.util.MessageUtil.toComponentWithPlaceholders(player, message);
            player.sendMessage(messageComponent);

            if (plugin.getConfigManager().isDebug()) {
                plugin.getLogger().info(player.getName() + " entered AFK region");
            }
        }
    }

    /**
     * Handle player exiting the AFK region
     * @param player The player
     */
    public void handleExitRegion(Player player) {
        AFKPlayer afkPlayer = getAFKPlayer(player.getUniqueId());

        if (afkPlayer != null && afkPlayer.isInRegion()) {
            afkPlayer.exitRegion();

            // Send chat message
            String message = plugin.getConfigManager().getMessages()
                .getString("messages.exit-region", "%prefix% <red>You left the AFK Pool. Progress reset.</red>");
            String prefix = plugin.getConfigManager().getPrefix();
            message = message.replace("%prefix%", prefix);

            Component messageComponent = dev.alone.aFKZone.util.MessageUtil.toComponentWithPlaceholders(player, message);
            player.sendMessage(messageComponent);

            if (plugin.getConfigManager().isDebug()) {
                plugin.getLogger().info(player.getName() + " exited AFK region");
            }
        }
    }

    /**
     * Update a player's region status
     * @param player The player
     * @param inRegion Whether they're in the region
     */
    public void updateRegionStatus(Player player, boolean inRegion) {
        AFKPlayer afkPlayer = getOrCreateAFKPlayer(player);

        if (inRegion && !afkPlayer.isInRegion()) {
            handleEnterRegion(player);
        } else if (!inRegion && afkPlayer.isInRegion()) {
            handleExitRegion(player);
        }
    }

    /**
     * Clear all AFK player data
     */
    public void clearAll() {
        afkPlayers.clear();
    }

    /**
     * Get the number of tracked players
     * @return Player count
     */
    public int getPlayerCount() {
        return afkPlayers.size();
    }

    /**
     * Get the number of players in the AFK region
     * @return Count of players in region
     */
    public int getPlayersInRegionCount() {
        return (int) afkPlayers.values().stream()
            .filter(AFKPlayer::isInRegion)
            .count();
    }
}
