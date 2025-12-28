package dev.alone.aFKPool.manager;

import dev.alone.aFKPool.AFKPool;
import dev.alone.aFKPool.data.AFKPlayer;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Manages data persistence for AFK players
 */
public class DataManager {

    private final AFKPool plugin;
    private final File dataFolder;

    /**
     * Create a new DataManager
     * @param plugin The plugin instance
     */
    public DataManager(AFKPool plugin) {
        this.plugin = plugin;
        this.dataFolder = new File(plugin.getDataFolder(), "data/players");

        // Create data folder if it doesn't exist
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
    }

    /**
     * Save player data to a file
     * @param afkPlayer The AFKPlayer to save
     */
    public void savePlayerData(AFKPlayer afkPlayer) {
        if (!plugin.getConfigManager().isPersistData()) {
            return;
        }

        try {
            File playerFile = new File(dataFolder, afkPlayer.getUuid().toString() + ".yml");
            YamlConfiguration config = new YamlConfiguration();

            config.set("uuid", afkPlayer.getUuid().toString());
            config.set("name", afkPlayer.getName());
            config.set("total-afk-time", afkPlayer.getTotalAFKTime());
            config.set("total-rewards", afkPlayer.getTotalRewards());
            config.set("last-reward-time", afkPlayer.getLastRewardTime());
            config.set("last-reward-name", afkPlayer.getLastRewardName());
            config.set("rewards-disabled", afkPlayer.isRewardsDisabled());
            config.set("reward-tier", afkPlayer.getRewardTier());

            // Save rewards by type
            config.set("rewards-by-type", afkPlayer.getRewardsByType());

            config.save(playerFile);

            if (plugin.getConfigManager().isDebug()) {
                plugin.getLogger().info("Saved data for " + afkPlayer.getName());
            }

        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save player data for " + afkPlayer.getName(), e);
        }
    }

    /**
     * Load player data from a file
     * @param uuid The player's UUID
     * @return The loaded AFKPlayer, or null if not found
     */
    public AFKPlayer loadPlayerData(UUID uuid) {
        if (!plugin.getConfigManager().isPersistData()) {
            return null;
        }

        try {
            File playerFile = new File(dataFolder, uuid.toString() + ".yml");

            if (!playerFile.exists()) {
                return null;
            }

            YamlConfiguration config = YamlConfiguration.loadConfiguration(playerFile);

            AFKPlayer afkPlayer = new AFKPlayer(uuid);
            afkPlayer.setName(config.getString("name", "Unknown"));
            afkPlayer.setTotalAFKTime(config.getLong("total-afk-time", 0));
            afkPlayer.setTotalRewards(config.getInt("total-rewards", 0));
            afkPlayer.setLastRewardTime(config.getLong("last-reward-time", 0));
            afkPlayer.setLastRewardName(config.getString("last-reward-name", "None"));
            afkPlayer.setRewardsDisabled(config.getBoolean("rewards-disabled", false));
            afkPlayer.setRewardTier(config.getString("reward-tier", "default"));

            // Load rewards by type
            if (config.contains("rewards-by-type")) {
                for (String type : config.getConfigurationSection("rewards-by-type").getKeys(false)) {
                    int count = config.getInt("rewards-by-type." + type, 0);
                    for (int i = 0; i < count; i++) {
                        afkPlayer.incrementRewardType(type);
                    }
                }
            }

            if (plugin.getConfigManager().isDebug()) {
                plugin.getLogger().info("Loaded data for " + afkPlayer.getName());
            }

            return afkPlayer;

        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to load player data for UUID " + uuid, e);
            return null;
        }
    }

    /**
     * Save all player data
     */
    public void saveAllPlayerData() {
        if (!plugin.getConfigManager().isPersistData()) {
            return;
        }

        int count = 0;
        for (AFKPlayer afkPlayer : plugin.getAFKManager().getAllPlayers()) {
            savePlayerData(afkPlayer);
            count++;
        }

        plugin.getLogger().info("Saved data for " + count + " players");
    }

    /**
     * Delete player data file
     * @param uuid The player's UUID
     * @return true if successful
     */
    public boolean deletePlayerData(UUID uuid) {
        File playerFile = new File(dataFolder, uuid.toString() + ".yml");
        if (playerFile.exists()) {
            return playerFile.delete();
        }
        return false;
    }

    /**
     * Get the number of saved player files
     * @return Number of player files
     */
    public int getSavedPlayerCount() {
        File[] files = dataFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        return files != null ? files.length : 0;
    }
}
