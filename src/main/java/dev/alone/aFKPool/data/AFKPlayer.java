package dev.alone.aFKPool.data;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Represents a player's AFK data and statistics
 */
public class AFKPlayer {

    private final UUID uuid;
    private String name;
    private long timeEntered;
    private long totalAFKTime;
    private long lastRewardTime;
    private boolean inRegion;
    private boolean rewardsDisabled;
    private String rewardTier;
    private int totalRewards;
    private String lastRewardName;
    private final Map<String, Integer> rewardsByType;

    /**
     * Create a new AFKPlayer
     * @param uuid The player's UUID
     */
    public AFKPlayer(UUID uuid) {
        this.uuid = uuid;
        this.timeEntered = 0;
        this.totalAFKTime = 0;
        this.lastRewardTime = 0;
        this.inRegion = false;
        this.rewardsDisabled = false;
        this.rewardTier = "default";
        this.totalRewards = 0;
        this.lastRewardName = "None";
        this.rewardsByType = new HashMap<>();
    }

    /**
     * Get the player's UUID
     * @return UUID
     */
    public UUID getUuid() {
        return uuid;
    }

    /**
     * Get the player's name
     * @return Name
     */
    public String getName() {
        return name;
    }

    /**
     * Set the player's name
     * @param name Name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get the time the player entered the region
     * @return Time entered (epoch millis)
     */
    public long getTimeEntered() {
        return timeEntered;
    }

    /**
     * Set the time the player entered the region
     * @param timeEntered Time entered (epoch millis)
     */
    public void setTimeEntered(long timeEntered) {
        this.timeEntered = timeEntered;
    }

    /**
     * Get the total time spent in the AFK region
     * @return Total AFK time in milliseconds
     */
    public long getTotalAFKTime() {
        return totalAFKTime;
    }

    /**
     * Set the total AFK time
     * @param totalAFKTime Total AFK time in milliseconds
     */
    public void setTotalAFKTime(long totalAFKTime) {
        this.totalAFKTime = totalAFKTime;
    }

    /**
     * Get the last reward time
     * @return Last reward time (epoch millis)
     */
    public long getLastRewardTime() {
        return lastRewardTime;
    }

    /**
     * Set the last reward time
     * @param lastRewardTime Last reward time (epoch millis)
     */
    public void setLastRewardTime(long lastRewardTime) {
        this.lastRewardTime = lastRewardTime;
    }

    /**
     * Check if the player is currently in the AFK region
     * @return true if in region
     */
    public boolean isInRegion() {
        return inRegion;
    }

    /**
     * Set whether the player is in the AFK region
     * @param inRegion true if in region
     */
    public void setInRegion(boolean inRegion) {
        this.inRegion = inRegion;
    }

    /**
     * Check if rewards are disabled for this player
     * @return true if rewards are disabled
     */
    public boolean isRewardsDisabled() {
        return rewardsDisabled;
    }

    /**
     * Set whether rewards are disabled for this player
     * @param rewardsDisabled true to disable rewards
     */
    public void setRewardsDisabled(boolean rewardsDisabled) {
        this.rewardsDisabled = rewardsDisabled;
    }

    /**
     * Get the player's current reward tier
     * @return Reward tier
     */
    public String getRewardTier() {
        return rewardTier;
    }

    /**
     * Set the player's reward tier
     * @param rewardTier Reward tier
     */
    public void setRewardTier(String rewardTier) {
        this.rewardTier = rewardTier;
    }

    /**
     * Get the total number of rewards received
     * @return Total rewards
     */
    public int getTotalRewards() {
        return totalRewards;
    }

    /**
     * Set the total number of rewards received
     * @param totalRewards Total rewards
     */
    public void setTotalRewards(int totalRewards) {
        this.totalRewards = totalRewards;
    }

    /**
     * Increment the total rewards counter
     */
    public void incrementTotalRewards() {
        this.totalRewards++;
    }

    /**
     * Get the name of the last reward received
     * @return Last reward name
     */
    public String getLastRewardName() {
        return lastRewardName;
    }

    /**
     * Set the last reward name
     * @param lastRewardName Last reward name
     */
    public void setLastRewardName(String lastRewardName) {
        this.lastRewardName = lastRewardName;
    }

    /**
     * Get rewards received by type
     * @return Map of reward type to count
     */
    public Map<String, Integer> getRewardsByType() {
        return rewardsByType;
    }

    /**
     * Increment a reward type counter
     * @param type The reward type
     */
    public void incrementRewardType(String type) {
        rewardsByType.put(type, rewardsByType.getOrDefault(type, 0) + 1);
    }

    /**
     * Get the current time spent in the region (for this session)
     * @return Time in region in milliseconds
     */
    public long getTimeInRegion() {
        if (!inRegion || timeEntered == 0) {
            return 0;
        }
        return System.currentTimeMillis() - timeEntered;
    }

    /**
     * Get the total session time (current time in region + accumulated time)
     * @return Session time in milliseconds
     */
    public long getSessionTime() {
        return totalAFKTime + getTimeInRegion();
    }

    /**
     * Check if the player is eligible for a reward
     * @param interval Reward interval in milliseconds
     * @return true if eligible
     */
    public boolean isEligibleForReward(long interval) {
        if (!inRegion || rewardsDisabled) {
            return false;
        }

        long currentTime = System.currentTimeMillis();
        long timeSinceLastReward = currentTime - lastRewardTime;

        return timeSinceLastReward >= interval;
    }

    /**
     * Grant a reward to this player
     */
    public void grantReward() {
        this.lastRewardTime = System.currentTimeMillis();
        incrementTotalRewards();
    }

    /**
     * Reset the player's AFK progress
     */
    public void reset() {
        this.timeEntered = 0;
        this.totalAFKTime = 0;
        this.lastRewardTime = 0;
        this.inRegion = false;
    }

    /**
     * Handle player entering the AFK region
     */
    public void enterRegion() {
        if (!inRegion) {
            this.inRegion = true;
            this.timeEntered = System.currentTimeMillis();

            // Set last reward time to current time to prevent immediate rewards
            // Player must wait the full interval before receiving first reward
            this.lastRewardTime = System.currentTimeMillis();
        }
    }

    /**
     * Handle player exiting the AFK region
     */
    public void exitRegion() {
        if (inRegion) {
            // Add current session time to total
            this.totalAFKTime += getTimeInRegion();
            this.inRegion = false;
            this.timeEntered = 0;
        }
    }
}
