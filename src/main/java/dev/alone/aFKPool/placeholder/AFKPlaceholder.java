package dev.alone.aFKPool.placeholder;

import dev.alone.aFKPool.AFKPool;
import dev.alone.aFKPool.data.AFKPlayer;
import dev.alone.aFKPool.util.MessageUtil;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * PlaceholderAPI expansion for AFKPool
 */
public class AFKPlaceholder extends PlaceholderExpansion {

    private final AFKPool plugin;

    /**
     * Create a new AFKPlaceholder expansion
     * @param plugin The plugin instance
     */
    public AFKPlaceholder(AFKPool plugin) {
        this.plugin = plugin;
    }

    @Override
    @NotNull
    public String getIdentifier() {
        return "afkpool";
    }

    @Override
    @NotNull
    public String getAuthor() {
        return "Alone";
    }

    @Override
    @NotNull
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String identifier) {
        if (player == null) {
            return "";
        }

        AFKPlayer afkPlayer = plugin.getAFKManager().getAFKPlayer(player.getUniqueId());
        if (afkPlayer == null) {
            return "";
        }

        switch (identifier.toLowerCase()) {
            case "time_in_region":
                return MessageUtil.formatTime(afkPlayer.getTimeInRegion());

            case "time_remaining":
                long currentTime = System.currentTimeMillis();
                long timeSinceLastReward = currentTime - afkPlayer.getLastRewardTime();
                long rewardInterval = plugin.getConfigManager().getRewardInterval();
                long timeRemaining = Math.max(0, rewardInterval - timeSinceLastReward);
                return MessageUtil.formatTime(timeRemaining);

            case "total_rewards":
                return String.valueOf(afkPlayer.getTotalRewards());

            case "last_reward":
                return afkPlayer.getLastRewardName();

            case "in_region":
                return afkPlayer.isInRegion() ? "Yes" : "No";

            case "reward_tier":
                return afkPlayer.getRewardTier().toUpperCase();

            case "session_time":
                return MessageUtil.formatTime(afkPlayer.getSessionTime());

            case "total_afk_time":
                return MessageUtil.formatTime(afkPlayer.getTotalAFKTime());

            case "rewards_disabled":
                return afkPlayer.isRewardsDisabled() ? "Yes" : "No";

            default:
                return null;
        }
    }
}
