package dev.alone.aFKZone;

import dev.alone.aFKZone.command.AFKZoneCommand;
import dev.alone.aFKZone.config.ConfigManager;
import dev.alone.aFKZone.gui.LeaderboardGUI;
import dev.alone.aFKZone.listener.PlayerJoinQuitListener;
import dev.alone.aFKZone.listener.PlayerMoveListener;
import dev.alone.aFKZone.manager.AFKManager;
import dev.alone.aFKZone.manager.DataManager;
import dev.alone.aFKZone.manager.LeaderboardManager;
import dev.alone.aFKZone.manager.RegionManager;
import dev.alone.aFKZone.manager.RewardManager;
import dev.alone.aFKZone.placeholder.AFKPlaceholder;
import dev.alone.aFKZone.task.ActionBarTask;
import dev.alone.aFKZone.task.RewardDistributionTask;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public final class AFKZone extends JavaPlugin {

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
        getLogger().info("Enabling AFKZone v" + getDescription().getVersion());

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
        PluginCommand command = getCommand("afkzone");
        if (command != null) {
            AFKZoneCommand afkZoneCommand = new AFKZoneCommand(this);
            command.setExecutor(afkZoneCommand);
            command.setTabCompleter(afkZoneCommand);
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
        getLogger().info("AFKZone has been enabled successfully! (" + (endTime - startTime) + "ms)");
        getLogger().info("Region: " + configManager.getRegionName());
        getLogger().info("Reward Interval: " + (configManager.getRewardInterval() / 1000) + " seconds");
        getLogger().info("Loaded " + configManager.getRewardPools().size() + " reward pools");
    }

    @Override
    public void onDisable() {
        getLogger().info("Disabling AFKZone...");

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

        getLogger().info("AFKZone has been disabled successfully!");
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
