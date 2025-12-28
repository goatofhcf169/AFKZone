package dev.alone.aFKPool.data;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a reward that can be given to players
 */
public class Reward {

    private final RewardType type;
    private final double chance;
    private String displayName;

    // Item reward fields
    private Material material;
    private int amount;
    private String itemName;
    private List<String> lore;
    private boolean glow;
    private List<EnchantmentData> enchantments;
    private List<ItemFlag> flags;
    private Integer customModelData;

    // Command reward fields
    private List<String> commands;

    // Experience reward fields
    private int expAmount;

    /**
     * Create a new Reward
     * @param type The reward type
     * @param chance The chance weight
     */
    public Reward(RewardType type, double chance) {
        this.type = type;
        this.chance = chance;
        this.lore = new ArrayList<>();
        this.enchantments = new ArrayList<>();
        this.flags = new ArrayList<>();
        this.commands = new ArrayList<>();
    }

    /**
     * Get the reward type
     * @return Reward type
     */
    public RewardType getType() {
        return type;
    }

    /**
     * Get the chance weight
     * @return Chance
     */
    public double getChance() {
        return chance;
    }

    /**
     * Get the display name
     * @return Display name
     */
    public String getDisplayName() {
        return displayName != null ? displayName : (itemName != null ? itemName : "Unknown Reward");
    }

    /**
     * Set the display name
     * @param displayName Display name
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    // Item reward getters/setters

    public Material getMaterial() {
        return material;
    }

    public void setMaterial(Material material) {
        this.material = material;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public List<String> getLore() {
        return lore;
    }

    public void setLore(List<String> lore) {
        this.lore = lore;
    }

    public boolean isGlow() {
        return glow;
    }

    public void setGlow(boolean glow) {
        this.glow = glow;
    }

    public List<EnchantmentData> getEnchantments() {
        return enchantments;
    }

    public void setEnchantments(List<EnchantmentData> enchantments) {
        this.enchantments = enchantments;
    }

    public List<ItemFlag> getFlags() {
        return flags;
    }

    public void setFlags(List<ItemFlag> flags) {
        this.flags = flags;
    }

    public Integer getCustomModelData() {
        return customModelData;
    }

    public void setCustomModelData(Integer customModelData) {
        this.customModelData = customModelData;
    }

    // Command reward getters/setters

    public List<String> getCommands() {
        return commands;
    }

    public void setCommands(List<String> commands) {
        this.commands = commands;
    }

    // Experience reward getters/setters

    public int getExpAmount() {
        return expAmount;
    }

    public void setExpAmount(int expAmount) {
        this.expAmount = expAmount;
    }

    /**
     * Reward type enum
     */
    public enum RewardType {
        ITEM,
        COMMAND,
        EXPERIENCE
    }

    /**
     * Enchantment data holder
     */
    public static class EnchantmentData {
        private final Enchantment enchantment;
        private final int level;

        public EnchantmentData(Enchantment enchantment, int level) {
            this.enchantment = enchantment;
            this.level = level;
        }

        public Enchantment getEnchantment() {
            return enchantment;
        }

        public int getLevel() {
            return level;
        }
    }
}
