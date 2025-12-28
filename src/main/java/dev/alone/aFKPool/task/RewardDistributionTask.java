package dev.alone.aFKPool.task;

import dev.alone.aFKPool.AFKPool;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Task that checks and grants rewards to eligible players
 */
public class RewardDistributionTask extends BukkitRunnable {

    private final AFKPool plugin;

    /**
     * Create a new RewardDistributionTask
     * @param plugin The plugin instance
     */
    public RewardDistributionTask(AFKPool plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        try {
            plugin.getRewardManager().checkAndGrantRewards();
        } catch (Exception e) {
            plugin.getLogger().severe("Error in RewardDistributionTask: " + e.getMessage());
            if (plugin.getConfigManager().isDebug()) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Start the task
     */
    public void start() {
        // Run every second (20 ticks) to check for rewards
        this.runTaskTimer(plugin, 20L, 20L);
        plugin.getLogger().info("RewardDistributionTask started");
    }
}
