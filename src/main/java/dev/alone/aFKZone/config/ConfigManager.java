package dev.alone.aFKZone.config;

import dev.alone.aFKZone.AFKZone;
import dev.alone.aFKZone.data.Reward;
import dev.alone.aFKZone.data.RewardPool;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * Manages all configuration files for the plugin
 */
public class ConfigManager {

    private final AFKZone plugin;
    private FileConfiguration config;
    private FileConfiguration rewards;
    private FileConfiguration messages;
    private FileConfiguration leaderboardGui;

    private final Map<String, RewardPool> rewardPools;

    /**
     * Create a new ConfigManager
     * @param plugin The plugin instance
     */
    public ConfigManager(AFKZone plugin) {
        this.plugin = plugin;
        this.rewardPools = new HashMap<>();
    }

    /**
     * Load all configuration files
     */
    public void loadConfigs() {
        // Create plugin folder if it doesn't exist
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        // Load or create config.yml
        File configFile = new File(plugin.getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            plugin.saveResource("config.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(configFile);

        // Load or create rewards.yml
        File rewardsFile = new File(plugin.getDataFolder(), "rewards.yml");
        if (!rewardsFile.exists()) {
            plugin.saveResource("rewards.yml", false);
        }
        rewards = YamlConfiguration.loadConfiguration(rewardsFile);

        // Load or create messages.yml
        File messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        messages = YamlConfiguration.loadConfiguration(messagesFile);

        // Create gui folder if it doesn't exist
        File guiFolder = new File(plugin.getDataFolder(), "gui");
        if (!guiFolder.exists()) {
            guiFolder.mkdirs();
        }

        // Load or create gui/leaderboard.yml
        File leaderboardGuiFile = new File(guiFolder, "leaderboard.yml");
        if (!leaderboardGuiFile.exists()) {
            plugin.saveResource("gui/leaderboard.yml", false);
        }
        leaderboardGui = YamlConfiguration.loadConfiguration(leaderboardGuiFile);

        // Load reward pools
        loadRewardPools();

        plugin.getLogger().info("Configuration files loaded successfully!");
    }

    /**
     * Reload all configuration files
     * @return true if successful
     */
    public boolean reloadConfigs() {
        try {
            File configFile = new File(plugin.getDataFolder(), "config.yml");
            config = YamlConfiguration.loadConfiguration(configFile);

            File rewardsFile = new File(plugin.getDataFolder(), "rewards.yml");
            rewards = YamlConfiguration.loadConfiguration(rewardsFile);

            File messagesFile = new File(plugin.getDataFolder(), "messages.yml");
            messages = YamlConfiguration.loadConfiguration(messagesFile);

            File leaderboardGuiFile = new File(plugin.getDataFolder(), "gui/leaderboard.yml");
            leaderboardGui = YamlConfiguration.loadConfiguration(leaderboardGuiFile);

            // Clear and reload reward pools
            rewardPools.clear();
            loadRewardPools();

            plugin.getLogger().info("Configuration files reloaded successfully!");
            return true;
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error reloading configuration files", e);
            return false;
        }
    }

    /**
     * Load reward pools from rewards.yml
     */
    private void loadRewardPools() {
        for (String key : rewards.getKeys(false)) {
            if (!rewards.isConfigurationSection(key)) continue;

            ConfigurationSection poolSection = rewards.getConfigurationSection(key);
            if (poolSection == null) continue;

            boolean enabled = poolSection.getBoolean("enabled", true);
            String permission = poolSection.getString("permission", "");

            RewardPool pool = new RewardPool(key, permission, enabled);

            // Load rewards for this pool
            ConfigurationSection rewardsSection = poolSection.getConfigurationSection("rewards");
            if (rewardsSection != null) {
                for (String rewardKey : rewardsSection.getKeys(false)) {
                    ConfigurationSection rewardSection = rewardsSection.getConfigurationSection(rewardKey);
                    if (rewardSection == null) continue;

                    Reward reward = loadReward(rewardSection);
                    if (reward != null) {
                        pool.addReward(reward);
                    }
                }
            }

            if (pool.hasRewards()) {
                rewardPools.put(key, pool);
                plugin.getLogger().info("Loaded reward pool '" + key + "' with " + pool.getRewards().size() + " rewards");
            }
        }
    }

    /**
     * Load a single reward from a configuration section
     * @param section The configuration section
     * @return The loaded reward, or null if invalid
     */
    private Reward loadReward(ConfigurationSection section) {
        try {
            String typeString = section.getString("type", "ITEM").toUpperCase();
            Reward.RewardType type = Reward.RewardType.valueOf(typeString);
            double chance = section.getDouble("chance", 1.0);

            Reward reward = new Reward(type, chance);

            // Set display name
            if (section.contains("display-name")) {
                reward.setDisplayName(section.getString("display-name"));
            }

            switch (type) {
                case ITEM:
                    loadItemReward(reward, section);
                    break;
                case COMMAND:
                    reward.setCommands(section.getStringList("commands"));
                    break;
                case EXPERIENCE:
                    reward.setExpAmount(section.getInt("amount", 0));
                    break;
            }

            return reward;
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Error loading reward from section", e);
            return null;
        }
    }

    /**
     * Load item-specific reward data
     * @param reward The reward to populate
     * @param section The configuration section
     */
    private void loadItemReward(Reward reward, ConfigurationSection section) {
        // Material
        String materialString = section.getString("material", "STONE");
        try {
            Material material = Material.valueOf(materialString.toUpperCase());
            reward.setMaterial(material);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid material: " + materialString);
            reward.setMaterial(Material.STONE);
        }

        // Amount
        reward.setAmount(section.getInt("amount", 1));

        // Name
        if (section.contains("name")) {
            reward.setItemName(section.getString("name"));
        }

        // Lore
        if (section.contains("lore")) {
            reward.setLore(section.getStringList("lore"));
        }

        // Glow
        reward.setGlow(section.getBoolean("glow", false));

        // Enchantments
        if (section.contains("enchantments")) {
            List<Reward.EnchantmentData> enchantments = new ArrayList<>();
            for (String enchantString : section.getStringList("enchantments")) {
                String[] parts = enchantString.split(":");
                if (parts.length == 2) {
                    try {
                        Enchantment enchantment = Enchantment.getByName(parts[0].toUpperCase());
                        int level = Integer.parseInt(parts[1]);
                        if (enchantment != null) {
                            enchantments.add(new Reward.EnchantmentData(enchantment, level));
                        }
                    } catch (Exception e) {
                        plugin.getLogger().warning("Invalid enchantment: " + enchantString);
                    }
                }
            }
            reward.setEnchantments(enchantments);
        }

        // Flags
        if (section.contains("flags")) {
            List<ItemFlag> flags = new ArrayList<>();
            for (String flagString : section.getStringList("flags")) {
                try {
                    ItemFlag flag = ItemFlag.valueOf(flagString.toUpperCase());
                    flags.add(flag);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid item flag: " + flagString);
                }
            }
            reward.setFlags(flags);
        }

        // Custom model data
        if (section.contains("custom-model-data")) {
            reward.setCustomModelData(section.getInt("custom-model-data"));
        }
    }

    // Getters for configuration values

    public FileConfiguration getConfig() {
        return config;
    }

    public FileConfiguration getRewards() {
        return rewards;
    }

    public FileConfiguration getMessages() {
        return messages;
    }

    public FileConfiguration getLeaderboardGui() {
        return leaderboardGui;
    }

    public Map<String, RewardPool> getRewardPools() {
        return rewardPools;
    }

    public String getRegionName() {
        return config.getString("settings.region-name", "afk");
    }

    public int getRegionCheckInterval() {
        return config.getInt("settings.region-check-interval", 5);
    }

    public boolean isPersistData() {
        return config.getBoolean("settings.persist-data", true);
    }

    public int getSaveInterval() {
        return config.getInt("settings.save-interval", 6000);
    }

    public long getRewardInterval() {
        return config.getInt("rewards.interval", 300) * 1000L; // Convert to milliseconds
    }

    public boolean isDropIfFull() {
        return config.getBoolean("rewards.drop-if-full", true);
    }

    public List<String> getTierPriority() {
        return config.getStringList("rewards.tier-priority");
    }

    public boolean isSoundEnabled() {
        return config.getBoolean("rewards.sound.enabled", true);
    }

    public String getSoundType() {
        return config.getString("rewards.sound.type", "ENTITY_PLAYER_LEVELUP");
    }

    public float getSoundVolume() {
        return (float) config.getDouble("rewards.sound.volume", 1.0);
    }

    public float getSoundPitch() {
        return (float) config.getDouble("rewards.sound.pitch", 1.0);
    }

    public boolean isTitleEnabled() {
        return config.getBoolean("visuals.title.enabled", true);
    }

    public int getTitleFadeIn() {
        return config.getInt("visuals.title.fade-in", 10);
    }

    public int getTitleStay() {
        return config.getInt("visuals.title.stay", 60);
    }

    public int getTitleFadeOut() {
        return config.getInt("visuals.title.fade-out", 20);
    }

    public boolean isActionBarEnabled() {
        return config.getBoolean("visuals.actionbar.enabled", true);
    }

    public int getActionBarUpdateInterval() {
        return config.getInt("visuals.actionbar.update-interval", 20);
    }

    public boolean isParticlesEnabled() {
        return config.getBoolean("visuals.particles.enabled", true);
    }

    public String getParticleType() {
        return config.getString("visuals.particles.type", "VILLAGER_HAPPY");
    }

    public int getParticleCount() {
        return config.getInt("visuals.particles.count", 20);
    }

    public double getParticleSpread() {
        return config.getDouble("visuals.particles.spread", 1.0);
    }

    public boolean isDebug() {
        return config.getBoolean("settings.debug", false);
    }

    // Message getters
    public String getMessage(String path) {
        return messages.getString("messages." + path, "Message not found: " + path);
    }

    public String getPrefix() {
        return messages.getString("prefix", "<gradient:#91EFF6:#FFEAC2><bold>AFKPOOL</bold></gradient> <dark_gray><bold>»</bold></dark_gray>");
    }
}
