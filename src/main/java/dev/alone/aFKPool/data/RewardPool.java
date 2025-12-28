package dev.alone.aFKPool.data;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a pool of rewards for a specific permission tier
 */
public class RewardPool {

    private final String name;
    private final String permission;
    private final boolean enabled;
    private final List<Reward> rewards;
    private double totalWeight;

    /**
     * Create a new RewardPool
     * @param name The pool name
     * @param permission The required permission
     * @param enabled Whether the pool is enabled
     */
    public RewardPool(String name, String permission, boolean enabled) {
        this.name = name;
        this.permission = permission;
        this.enabled = enabled;
        this.rewards = new ArrayList<>();
        this.totalWeight = 0;
    }

    /**
     * Get the pool name
     * @return Name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the required permission
     * @return Permission
     */
    public String getPermission() {
        return permission;
    }

    /**
     * Check if the pool is enabled
     * @return true if enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Get all rewards in the pool
     * @return List of rewards
     */
    public List<Reward> getRewards() {
        return rewards;
    }

    /**
     * Add a reward to the pool
     * @param reward The reward to add
     */
    public void addReward(Reward reward) {
        rewards.add(reward);
        totalWeight += reward.getChance();
    }

    /**
     * Get the total weight of all rewards
     * @return Total weight
     */
    public double getTotalWeight() {
        return totalWeight;
    }

    /**
     * Check if the pool has any rewards
     * @return true if the pool has rewards
     */
    public boolean hasRewards() {
        return !rewards.isEmpty();
    }
}
