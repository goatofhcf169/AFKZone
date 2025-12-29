package dev.alone.aFKZone.task;

import dev.alone.aFKZone.AFKZone;
import dev.alone.aFKZone.util.FoliaScheduler;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;

/**
 * Task that checks and grants rewards to eligible players
 * Uses Folia's GlobalRegionScheduler for region-aware execution
 */
public class RewardDistributionTask {

    private final AFKZone plugin;
    private ScheduledTask task;

    /**
     * Create a new RewardDistributionTask
     * @param plugin The plugin instance
     */
    public RewardDistributionTask(AFKZone plugin) {
        this.plugin = plugin;
    }

    /**
     * Start the task
     */
    public void start() {
        // Run every second (20 ticks) to check for rewards
        task = FoliaScheduler.runGlobalTimer(plugin, () -> {
            try {
                plugin.getRewardManager().checkAndGrantRewards();
            } catch (Exception e) {
                plugin.getLogger().severe("Error in RewardDistributionTask: " + e.getMessage());
                if (plugin.getConfigManager().isDebug()) {
                    e.printStackTrace();
                }
            }
        }, 20L, 20L);
        plugin.getLogger().info("RewardDistributionTask started");
    }

    /**
     * Cancel the task
     */
    public void cancel() {
        if (task != null) {
            task.cancel();
        }
    }
}
