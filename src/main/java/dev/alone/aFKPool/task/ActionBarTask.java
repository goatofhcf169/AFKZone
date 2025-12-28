package dev.alone.aFKPool.task;

import dev.alone.aFKPool.AFKPool;
import dev.alone.aFKPool.data.AFKPlayer;
import dev.alone.aFKPool.util.MessageUtil;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Task that updates action bars for all online players
 */
public class ActionBarTask extends BukkitRunnable {

    private final AFKPool plugin;

    /**
     * Create a new ActionBarTask
     * @param plugin The plugin instance
     */
    public ActionBarTask(AFKPool plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        if (!plugin.getConfigManager().isActionBarEnabled()) {
            return;
        }

        try {
            for (Player player : Bukkit.getOnlinePlayers()) {
                AFKPlayer afkPlayer = plugin.getAFKManager().getAFKPlayer(player.getUniqueId());

                if (afkPlayer == null) {
                    continue;
                }

                // Only show action bar when player is in the AFK region
                if (!afkPlayer.isInRegion()) {
                    continue;
                }

                String message = getActionBarMessage(player, afkPlayer);
                if (message != null && !message.isEmpty()) {
                    Component component = MessageUtil.toComponent(message);
                    player.sendActionBar(component);
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Error in ActionBarTask: " + e.getMessage());
            if (plugin.getConfigManager().isDebug()) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Get the action bar message for a player
     * @param player The player
     * @param afkPlayer The AFKPlayer data
     * @return The message to display
     */
    private String getActionBarMessage(Player player, AFKPlayer afkPlayer) {
        String message;

        if (afkPlayer.isInRegion()) {
            long timeRemaining = calculateTimeRemaining(afkPlayer);

            if (timeRemaining <= 10000) { // Last 10 seconds
                message = plugin.getConfigManager().getMessages()
                    .getString("actionbar.reward-soon", "<green><bold>REWARD IN <yellow><bold>%afkpool_time_remaining%</bold></yellow></bold></green>");
            } else {
                message = plugin.getConfigManager().getMessages()
                    .getString("actionbar.in-region", "<gray>Next reward in: <yellow>%afkpool_time_remaining%</yellow> <dark_gray>|</dark_gray> <gray>Tier: <aqua>%afkpool_reward_tier%</aqua></gray>");
            }

            message = replacePlaceholders(message, player, afkPlayer, timeRemaining);
        } else {
            message = plugin.getConfigManager().getMessages()
                .getString("actionbar.out-region", "<red>Enter the <yellow>AFK Pool</yellow> <red>to earn rewards!</red>");
            message = MessageUtil.replacePlaceholders(player, message);
        }

        return message;
    }

    /**
     * Calculate time remaining until next reward
     * @param afkPlayer The AFKPlayer data
     * @return Time remaining in milliseconds
     */
    private long calculateTimeRemaining(AFKPlayer afkPlayer) {
        long rewardInterval = plugin.getConfigManager().getRewardInterval();
        long currentTime = System.currentTimeMillis();
        long timeSinceLastReward = currentTime - afkPlayer.getLastRewardTime();
        long timeRemaining = rewardInterval - timeSinceLastReward;

        return Math.max(0, timeRemaining);
    }

    /**
     * Replace plugin-specific placeholders
     * @param message The message
     * @param player The player
     * @param afkPlayer The AFKPlayer data
     * @param timeRemaining Time remaining in milliseconds
     * @return Processed message
     */
    private String replacePlaceholders(String message, Player player, AFKPlayer afkPlayer, long timeRemaining) {
        message = message.replace("%afkpool_time_remaining%", MessageUtil.formatTime(timeRemaining));
        message = message.replace("%afkpool_time_in_region%", MessageUtil.formatTime(afkPlayer.getTimeInRegion()));
        message = message.replace("%afkpool_total_rewards%", String.valueOf(afkPlayer.getTotalRewards()));
        message = message.replace("%afkpool_last_reward%", afkPlayer.getLastRewardName());
        message = message.replace("%afkpool_in_region%", afkPlayer.isInRegion() ? "Yes" : "No");
        message = message.replace("%afkpool_reward_tier%", afkPlayer.getRewardTier().toUpperCase());
        message = message.replace("%afkpool_session_time%", MessageUtil.formatTime(afkPlayer.getSessionTime()));

        // PlaceholderAPI support (without colorizing to legacy)
        if (player != null) {
            try {
                Class.forName("me.clip.placeholderapi.PlaceholderAPI");
                message = PlaceholderAPI.setPlaceholders(player, message);
            } catch (ClassNotFoundException ignored) {
                // PlaceholderAPI not available
            }
        }

        return message;
    }

    /**
     * Start the task
     */
    public void start() {
        int interval = plugin.getConfigManager().getActionBarUpdateInterval();
        this.runTaskTimer(plugin, interval, interval);
        plugin.getLogger().info("ActionBarTask started (interval: " + interval + " ticks)");
    }
}
