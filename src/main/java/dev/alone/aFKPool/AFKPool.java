package dev.alone.aFKPool;

import dev.alone.aFKPool.command.AFKPoolCommand;
import dev.alone.aFKPool.config.ConfigManager;
import dev.alone.aFKPool.gui.LeaderboardGUI;
import dev.alone.aFKPool.listener.PlayerJoinQuitListener;
import dev.alone.aFKPool.listener.PlayerMoveListener;
import dev.alone.aFKPool.manager.AFKManager;
import dev.alone.aFKPool.manager.DataManager;
import dev.alone.aFKPool.manager.LeaderboardManager;
import dev.alone.aFKPool.manager.RegionManager;
import dev.alone.aFKPool.manager.RewardManager;
import dev.alone.aFKPool.placeholder.AFKPlaceholder;
import dev.alone.aFKPool.task.ActionBarTask;
import dev.alone.aFKPool.task.RewardDistributionTask;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public final class AFKPool extends JavaPlugin {

    // Managers
    private ConfigManager configManager;
    private RegionManager regionManager;
    private AFKManager afkManager;
    private RewardManager rewardManager;
    private DataManager dataManager;
    private LeaderboardManager leaderboardManager;

    // Tasks
    private RewardDistributionTask rewardTask;
    private ActionBarTask actionBarTask;
    private BukkitTask autoSaveTask;

    @Override
    public void onEnable() {
        long startTime = System.currentTimeMillis();
        getLogger().info("Enabling AFKPool v" + getDescription().getVersion());

        // Initialize configuration manager
        getLogger().info("Loading configuration...");
        configManager = new ConfigManager(this);
        configManager.loadConfigs();

        // Initialize WorldGuard
        getLogger().info("Initializing WorldGuard integration...");
        regionManager = new RegionManager(this);
        if (!regionManager.initialize()) {
            getLogger().severe("Failed to initialize WorldGuard! Disabling plugin...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Initialize managers
        getLogger().info("Initializing managers...");
        afkManager = new AFKManager(this);
        rewardManager = new RewardManager(this);
        dataManager = new DataManager(this);
        leaderboardManager = new LeaderboardManager(this);

        // Register event listeners
        getLogger().info("Registering event listeners...");
        getServer().getPluginManager().registerEvents(new PlayerMoveListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinQuitListener(this), this);
        getServer().getPluginManager().registerEvents(new LeaderboardGUI(this, LeaderboardManager.SortType.REWARDS), this);

        // Register commands
        getLogger().info("Registering commands...");
        PluginCommand command = getCommand("afkpool");
        if (command != null) {
            AFKPoolCommand afkPoolCommand = new AFKPoolCommand(this);
            command.setExecutor(afkPoolCommand);
            command.setTabCompleter(afkPoolCommand);
        }

        // Start scheduled tasks
        getLogger().info("Starting scheduled tasks...");
        rewardTask = new RewardDistributionTask(this);
        rewardTask.start();

        actionBarTask = new ActionBarTask(this);
        actionBarTask.start();

        // Start auto-save task if persistence is enabled
        if (configManager.isPersistData()) {
            int saveInterval = configManager.getSaveInterval();
            autoSaveTask = getServer().getScheduler().runTaskTimerAsynchronously(this, () -> {
                dataManager.saveAllPlayerData();
            }, saveInterval, saveInterval);
            getLogger().info("Auto-save task started (interval: " + saveInterval + " ticks)");
        }

        // Register PlaceholderAPI expansion if available
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            getLogger().info("Registering PlaceholderAPI expansion...");
            new AFKPlaceholder(this).register();
            getLogger().info("PlaceholderAPI hook registered!");
        }

        long endTime = System.currentTimeMillis();
        getLogger().info("AFKPool has been enabled successfully! (" + (endTime - startTime) + "ms)");
        getLogger().info("Region: " + configManager.getRegionName());
        getLogger().info("Reward Interval: " + (configManager.getRewardInterval() / 1000) + " seconds");
        getLogger().info("Loaded " + configManager.getRewardPools().size() + " reward pools");
    }

    @Override
    public void onDisable() {
        getLogger().info("Disabling AFKPool...");

        // Cancel all tasks
        if (rewardTask != null) {
            rewardTask.cancel();
        }
        if (actionBarTask != null) {
            actionBarTask.cancel();
        }
        if (autoSaveTask != null) {
            autoSaveTask.cancel();
        }

        // Save all player data
        if (dataManager != null && configManager != null && configManager.isPersistData()) {
            getLogger().info("Saving all player data...");
            dataManager.saveAllPlayerData();
        }

        // Clear caches
        if (regionManager != null) {
            regionManager.clearCache();
        }

        getLogger().info("AFKPool has been disabled successfully!");
    }

    // Getters for managers

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public RegionManager getRegionManager() {
        return regionManager;
    }

    public AFKManager getAFKManager() {
        return afkManager;
    }

    public RewardManager getRewardManager() {
        return rewardManager;
    }

    public DataManager getDataManager() {
        return dataManager;
    }

    public LeaderboardManager getLeaderboardManager() {
        return leaderboardManager;
    }
}
