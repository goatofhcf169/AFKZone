package dev.alone.aFKZone.util;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Builder class for creating ItemStacks with custom properties
 */
public class ItemBuilder {

    private final ItemStack itemStack;
    private final ItemMeta itemMeta;

    /**
     * Create a new ItemBuilder
     * @param material The material of the item
     */
    public ItemBuilder(Material material) {
        this.itemStack = new ItemStack(material);
        this.itemMeta = itemStack.getItemMeta();
    }

    /**
     * Create a new ItemBuilder from an existing ItemStack
     * @param itemStack The ItemStack to build from
     */
    public ItemBuilder(ItemStack itemStack) {
        this.itemStack = itemStack.clone();
        this.itemMeta = itemStack.getItemMeta();
    }

    /**
     * Set the amount of items
     * @param amount The amount
     * @return This builder
     */
    public ItemBuilder amount(int amount) {
        itemStack.setAmount(amount);
        return this;
    }

    /**
     * Set the display name of the item
     * @param name The display name (supports color codes)
     * @return This builder
     */
    public ItemBuilder name(String name) {
        if (itemMeta != null && name != null) {
            itemMeta.setDisplayName(MessageUtil.colorize(name));
        }
        return this;
    }

    /**
     * Set the lore of the item
     * @param lore The lore lines (supports color codes)
     * @return This builder
     */
    public ItemBuilder lore(List<String> lore) {
        if (itemMeta != null && lore != null) {
            List<String> colorizedLore = new ArrayList<>();
            for (String line : lore) {
                colorizedLore.add(MessageUtil.colorize(line));
            }
            itemMeta.setLore(colorizedLore);
        }
        return this;
    }

    /**
     * Set the lore of the item
     * @param lore The lore lines (supports color codes)
     * @return This builder
     */
    public ItemBuilder lore(String... lore) {
        return lore(Arrays.asList(lore));
    }

    /**
     * Add an enchantment to the item
     * @param enchantment The enchantment
     * @param level The level
     * @return This builder
     */
    public ItemBuilder enchant(Enchantment enchantment, int level) {
        if (itemMeta != null) {
            itemMeta.addEnchant(enchantment, level, true);
        }
        return this;
    }

    /**
     * Add item flags to hide certain attributes
     * @param flags The flags to add
     * @return This builder
     */
    public ItemBuilder flags(ItemFlag... flags) {
        if (itemMeta != null) {
            itemMeta.addItemFlags(flags);
        }
        return this;
    }

    /**
     * Set custom model data
     * @param data The custom model data
     * @return This builder
     */
    public ItemBuilder customModelData(int data) {
        if (itemMeta != null) {
            itemMeta.setCustomModelData(data);
        }
        return this;
    }

    /**
     * Make the item glow (adds a fake enchantment effect)
     * @return This builder
     */
    public ItemBuilder glow() {
        if (itemMeta != null) {
            itemMeta.addEnchant(Enchantment.UNBREAKING, 1, true);
            itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        return this;
    }

    /**
     * Make the item unbreakable
     * @return This builder
     */
    public ItemBuilder unbreakable() {
        if (itemMeta != null) {
            itemMeta.setUnbreakable(true);
            itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        }
        return this;
    }

    /**
     * Build the ItemStack
     * @return The built ItemStack
     */
    public ItemStack build() {
        if (itemMeta != null) {
            itemStack.setItemMeta(itemMeta);
        }
        return itemStack;
    }
}
