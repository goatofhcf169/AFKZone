package dev.alone.aFKPool.manager;

import dev.alone.aFKPool.AFKPool;
import dev.alone.aFKPool.data.AFKPlayer;
import dev.alone.aFKPool.data.Reward;
import dev.alone.aFKPool.data.RewardPool;
import dev.alone.aFKPool.util.ItemBuilder;
import dev.alone.aFKPool.util.MessageUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.time.Duration;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Manages reward selection and distribution
 */
public class RewardManager {

    private final AFKPool plugin;

    /**
     * Create a new RewardManager
     * @param plugin The plugin instance
     */
    public RewardManager(AFKPool plugin) {
        this.plugin = plugin;
    }

    /**
     * Select a reward for a player using weighted random selection
     * @param player The player
     * @return Selected reward, or null if none available
     */
    public Reward selectReward(Player player) {
        List<RewardPool> eligiblePools = getEligiblePools(player);

        if (eligiblePools.isEmpty()) {
            plugin.getLogger().warning("No eligible reward pools for player " + player.getName());
            return null;
        }

        // Get the highest priority pool
        RewardPool selectedPool = eligiblePools.get(0);

        if (!selectedPool.hasRewards()) {
            return null;
        }

        // Weighted random selection
        double totalWeight = selectedPool.getTotalWeight();
        double random = ThreadLocalRandom.current().nextDouble(0, totalWeight);

        double currentWeight = 0;
        for (Reward reward : selectedPool.getRewards()) {
            currentWeight += reward.getChance();
            if (random <= currentWeight) {
                return reward;
            }
        }

        // Fallback to first reward
        return selectedPool.getRewards().get(0);
    }

    /**
     * Get eligible reward pools for a player (sorted by priority)
     * @param player The player
     * @return List of eligible pools
     */
    private List<RewardPool> getEligiblePools(Player player) {
        List<RewardPool> eligible = new ArrayList<>();
        List<String> tierPriority = plugin.getConfigManager().getTierPriority();

        for (String tierPerm : tierPriority) {
            // Check if player has permission for this tier
            boolean hasPermission;

            if (tierPerm.equals("default")) {
                hasPermission = true;
            } else {
                // Check if player has explicit permission (not from OP)
                hasPermission = player.hasPermission(tierPerm) &&
                    (player.isPermissionSet(tierPerm) || !player.isOp());
            }

            if (hasPermission) {
                // Get the pool name from permission (e.g., "afkpool.vip" -> "vip")
                String poolName = tierPerm.equals("default") ? "default" :
                    (tierPerm.contains(".") ? tierPerm.substring(tierPerm.lastIndexOf('.') + 1) : tierPerm);

                RewardPool pool = plugin.getConfigManager().getRewardPools().get(poolName);
                if (pool != null && pool.isEnabled()) {
                    eligible.add(pool);
                    break; // Only use highest priority pool
                }
            }
        }

        return eligible;
    }

    /**
     * Grant a reward to a player
     * @param player The player
     * @param reward The reward to grant
     */
    public void grantReward(Player player, Reward reward) {
        if (reward == null) {
            plugin.getLogger().warning("Attempted to grant null reward to " + player.getName());
            return;
        }

        AFKPlayer afkPlayer = plugin.getAFKManager().getAFKPlayer(player.getUniqueId());
        if (afkPlayer == null) return;

        switch (reward.getType()) {
            case ITEM:
                grantItemReward(player, reward);
                break;
            case COMMAND:
                grantCommandReward(player, reward);
                break;
            case EXPERIENCE:
                grantExperienceReward(player, reward);
                break;
        }

        // Update player statistics
        afkPlayer.grantReward();
        afkPlayer.setLastRewardName(reward.getDisplayName());
        afkPlayer.incrementRewardType(reward.getType().name());

        // Show title
        if (plugin.getConfigManager().isTitleEnabled()) {
            String titleText = plugin.getConfigManager().getMessages()
                .getString("titles.reward-received.title", "<gold><bold>REWARD RECEIVED!</bold></gold>");
            String subtitleText = plugin.getConfigManager().getMessages()
                .getString("titles.reward-received.subtitle", "<yellow>%reward_name%</yellow>");

            subtitleText = subtitleText.replace("%reward_name%", reward.getDisplayName());

            Component titleComponent = MessageUtil.toComponent(titleText);
            Component subtitleComponent = MessageUtil.toComponent(subtitleText);

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
        String message = plugin.getConfigManager().getMessage("reward-received");
        String prefix = plugin.getConfigManager().getPrefix();
        message = message.replace("%prefix%", prefix)
            .replace("%reward_name%", reward.getDisplayName());
        Component messageComponent = MessageUtil.toComponentWithPlaceholders(player, message);
        player.sendMessage(messageComponent);

        // Play sound
        if (plugin.getConfigManager().isSoundEnabled()) {
            try {
                Sound sound = Sound.valueOf(plugin.getConfigManager().getSoundType());
                player.playSound(
                    player.getLocation(),
                    sound,
                    plugin.getConfigManager().getSoundVolume(),
                    plugin.getConfigManager().getSoundPitch()
                );
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid sound type: " + plugin.getConfigManager().getSoundType());
            }
        }

        // Spawn particles
        if (plugin.getConfigManager().isParticlesEnabled()) {
            try {
                Particle particle = Particle.valueOf(plugin.getConfigManager().getParticleType());
                player.getWorld().spawnParticle(
                    particle,
                    player.getLocation().add(0, 1, 0),
                    plugin.getConfigManager().getParticleCount(),
                    plugin.getConfigManager().getParticleSpread(),
                    plugin.getConfigManager().getParticleSpread(),
                    plugin.getConfigManager().getParticleSpread()
                );
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid particle type: " + plugin.getConfigManager().getParticleType());
            }
        }

        if (plugin.getConfigManager().isDebug()) {
            plugin.getLogger().info("Granted reward '" + reward.getDisplayName() + "' to " + player.getName());
        }
    }

    /**
     * Grant an item reward to a player
     * @param player The player
     * @param reward The reward
     */
    private void grantItemReward(Player player, Reward reward) {
        ItemBuilder builder = new ItemBuilder(reward.getMaterial());
        builder.amount(reward.getAmount());

        if (reward.getItemName() != null) {
            builder.name(reward.getItemName());
        }

        if (reward.getLore() != null && !reward.getLore().isEmpty()) {
            builder.lore(reward.getLore());
        }

        if (reward.isGlow()) {
            builder.glow();
        }

        if (reward.getEnchantments() != null) {
            for (Reward.EnchantmentData enchData : reward.getEnchantments()) {
                builder.enchant(enchData.getEnchantment(), enchData.getLevel());
            }
        }

        if (reward.getFlags() != null && !reward.getFlags().isEmpty()) {
            builder.flags(reward.getFlags().toArray(new ItemFlag[0]));
        }

        if (reward.getCustomModelData() != null) {
            builder.customModelData(reward.getCustomModelData());
        }

        ItemStack item = builder.build();

        // Try to add to inventory
        HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(item);

        // Drop items if inventory is full and drop-if-full is enabled
        if (!leftover.isEmpty() && plugin.getConfigManager().isDropIfFull()) {
            for (ItemStack drop : leftover.values()) {
                player.getWorld().dropItem(player.getLocation(), drop);
            }

            String message = plugin.getConfigManager().getMessage("inventory-full");
            String prefix = plugin.getConfigManager().getPrefix();
            message = message.replace("%prefix%", prefix);
            Component messageComponent = MessageUtil.toComponentWithPlaceholders(player, message);
            player.sendMessage(messageComponent);
        }
    }

    /**
     * Grant a command reward to a player
     * @param player The player
     * @param reward The reward
     */
    private void grantCommandReward(Player player, Reward reward) {
        for (String command : reward.getCommands()) {
            String processedCommand = command.replace("%player%", player.getName())
                .replace("%uuid%", player.getUniqueId().toString());

            Bukkit.getScheduler().runTask(plugin, () -> {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), processedCommand);
            });
        }
    }

    /**
     * Grant an experience reward to a player
     * @param player The player
     * @param reward The reward
     */
    private void grantExperienceReward(Player player, Reward reward) {
        player.giveExp(reward.getExpAmount());
    }

    /**
     * Check all players in AFK region for reward eligibility
     */
    public void checkAndGrantRewards() {
        long rewardInterval = plugin.getConfigManager().getRewardInterval();

        for (AFKPlayer afkPlayer : plugin.getAFKManager().getPlayersInRegion()) {
            Player player = Bukkit.getPlayer(afkPlayer.getUuid());

            if (player == null || !player.isOnline()) {
                continue;
            }

            if (afkPlayer.isEligibleForReward(rewardInterval)) {
                Reward reward = selectReward(player);
                if (reward != null) {
                    grantReward(player, reward);
                }
            }
        }
    }
}
