package dev.alone.aFKZone.gui;

import dev.alone.aFKZone.AFKZone;
import dev.alone.aFKZone.data.AFKPlayer;
import dev.alone.aFKZone.manager.LeaderboardManager;
import dev.alone.aFKZone.util.ItemBuilder;
import dev.alone.aFKZone.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * GUI for displaying the AFK Pool leaderboard
 */
public class LeaderboardGUI implements Listener, InventoryHolder {

    private final AFKZone plugin;
    private final LeaderboardManager.SortType sortType;
    private Inventory inventory;

    /**
     * Create a new LeaderboardGUI
     * @param plugin The plugin instance
     * @param sortType The sort type for the leaderboard
     */
    public LeaderboardGUI(AFKZone plugin, LeaderboardManager.SortType sortType) {
        this.plugin = plugin;
        this.sortType = sortType;
    }

    /**
     * Open the leaderboard GUI for a player
     * @param player The player to show the GUI to
     */
    public void open(Player player) {
        // Get title from config
        String titleKey = sortType == LeaderboardManager.SortType.REWARDS ?
            "gui.title-rewards" : "gui.title-time";
        String title = MessageUtil.colorize(
            plugin.getConfigManager().getLeaderboardGui().getString(titleKey, "<aqua><bold>AFK Pool</bold></aqua>")
        );

        // Get rows from config
        int rows = plugin.getConfigManager().getLeaderboardGui().getInt("gui.rows", 6);
        int size = rows * 9;

        this.inventory = Bukkit.createInventory(this, size, title);

        // Get max players from config
        int maxPlayers = plugin.getConfigManager().getLeaderboardGui().getInt("gui.max-players", 45);

        // Get top players
        List<AFKPlayer> topPlayers = sortType == LeaderboardManager.SortType.REWARDS ?
            plugin.getLeaderboardManager().getTopByRewards(maxPlayers) :
            plugin.getLeaderboardManager().getTopByTime(maxPlayers);

        // Fill leaderboard slots
        for (int i = 0; i < topPlayers.size() && i < maxPlayers; i++) {
            AFKPlayer afkPlayer = topPlayers.get(i);
            this.inventory.setItem(i, createPlayerHead(afkPlayer, i + 1));
        }

        // Add control buttons from config
        this.inventory.setItem(
            plugin.getConfigManager().getLeaderboardGui().getInt("buttons.sort.slot", 49),
            createSortButton()
        );
        this.inventory.setItem(
            plugin.getConfigManager().getLeaderboardGui().getInt("buttons.your-stats.slot", 48),
            createYourStatsButton(player)
        );
        this.inventory.setItem(
            plugin.getConfigManager().getLeaderboardGui().getInt("buttons.close.slot", 50),
            createCloseButton()
        );

        // Add filler items if enabled
        if (plugin.getConfigManager().getLeaderboardGui().getBoolean("filler.enabled", true)) {
            addFillerItems(this.inventory);
        }

        player.openInventory(this.inventory);
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        return this.inventory;
    }

    /**
     * Create a player head item for the leaderboard
     * @param afkPlayer The AFKPlayer data
     * @param rank The player's rank
     * @return ItemStack with player head
     */
    private ItemStack createPlayerHead(AFKPlayer afkPlayer, int rank) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();

        if (meta != null) {
            // Set player head
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(afkPlayer.getUuid());
            meta.setOwningPlayer(offlinePlayer);

            // Get rank color from config
            String rankColor = getRankColor(rank);

            // Set display name from config
            String nameFormat = plugin.getConfigManager().getLeaderboardGui()
                .getString("player-head.name", "%rank_color%#%rank% <white>%player%</white>");
            nameFormat = nameFormat
                .replace("%rank_color%", rankColor)
                .replace("%rank%", String.valueOf(rank))
                .replace("%player%", afkPlayer.getName());
            meta.setDisplayName(MessageUtil.colorize(nameFormat));

            // Set lore from config
            List<String> loreTemplate = plugin.getConfigManager().getLeaderboardGui()
                .getStringList("player-head.lore");

            String statusMessage = afkPlayer.isInRegion() ?
                plugin.getConfigManager().getLeaderboardGui().getString("player-head.status.in-pool") :
                plugin.getConfigManager().getLeaderboardGui().getString("player-head.status.not-in-pool");

            List<String> lore = new ArrayList<>();
            for (String line : loreTemplate) {
                line = line
                    .replace("%rewards%", String.valueOf(afkPlayer.getTotalRewards()))
                    .replace("%time%", MessageUtil.formatTime(afkPlayer.getTotalAFKTime()))
                    .replace("%status%", statusMessage);
                lore.add(MessageUtil.colorize(line));
            }

            meta.setLore(lore);
            skull.setItemMeta(meta);
        }

        return skull;
    }

    /**
     * Get rank color based on position (from config)
     * @param rank The rank number
     * @return Color code
     */
    private String getRankColor(int rank) {
        String key;
        switch (rank) {
            case 1:
                key = "rank-colors.first";
                break;
            case 2:
                key = "rank-colors.second";
                break;
            case 3:
                key = "rank-colors.third";
                break;
            default:
                key = "rank-colors.default";
        }
        return plugin.getConfigManager().getLeaderboardGui().getString(key, "<yellow>");
    }

    /**
     * Create a sort button (cycles between REWARDS and TIME)
     * @return ItemStack button
     */
    private ItemStack createSortButton() {
        String materialKey = sortType == LeaderboardManager.SortType.REWARDS ?
            "buttons.sort.material-rewards" : "buttons.sort.material-time";
        String nameKey = sortType == LeaderboardManager.SortType.REWARDS ?
            "buttons.sort.name-rewards" : "buttons.sort.name-time";
        String loreKey = sortType == LeaderboardManager.SortType.REWARDS ?
            "buttons.sort.lore-rewards" : "buttons.sort.lore-time";

        String materialName = plugin.getConfigManager().getLeaderboardGui()
            .getString(materialKey, "DIAMOND");
        Material material = Material.valueOf(materialName);

        String name = plugin.getConfigManager().getLeaderboardGui()
            .getString(nameKey, "<aqua><bold>Sort</bold></aqua>");

        List<String> loreTemplate = plugin.getConfigManager().getLeaderboardGui()
            .getStringList(loreKey);

        ItemBuilder builder = new ItemBuilder(material)
            .name(name)
            .lore(loreTemplate);

        if (plugin.getConfigManager().getLeaderboardGui().getBoolean("buttons.sort.glow", true)) {
            builder.glow();
        }

        return builder.build();
    }

    /**
     * Create the "Your Stats" button
     * @param player The player
     * @return ItemStack button
     */
    private ItemStack createYourStatsButton(Player player) {
        AFKPlayer afkPlayer = plugin.getAFKManager().getAFKPlayer(player.getUniqueId());

        String materialName = plugin.getConfigManager().getLeaderboardGui()
            .getString("buttons.your-stats.material", "PLAYER_HEAD");
        Material material = Material.valueOf(materialName);

        String name = plugin.getConfigManager().getLeaderboardGui()
            .getString("buttons.your-stats.name", "<green><bold>Your Statistics</bold></green>");

        List<String> lore = new ArrayList<>();
        if (afkPlayer != null) {
            int rankByRewards = plugin.getLeaderboardManager().getRankByRewards(afkPlayer);
            int rankByTime = plugin.getLeaderboardManager().getRankByTime(afkPlayer);

            List<String> loreTemplate = plugin.getConfigManager().getLeaderboardGui()
                .getStringList("buttons.your-stats.lore");

            for (String line : loreTemplate) {
                line = line
                    .replace("%rank_rewards%", rankByRewards > 0 ? String.valueOf(rankByRewards) : "N/A")
                    .replace("%rank_time%", rankByTime > 0 ? String.valueOf(rankByTime) : "N/A")
                    .replace("%your_rewards%", String.valueOf(afkPlayer.getTotalRewards()))
                    .replace("%your_time%", MessageUtil.formatTime(afkPlayer.getTotalAFKTime()));
                lore.add(line);
            }
        } else {
            List<String> noDataLore = plugin.getConfigManager().getLeaderboardGui()
                .getStringList("buttons.your-stats.lore-no-data");
            lore.addAll(noDataLore);
        }

        return new ItemBuilder(material)
            .name(name)
            .lore(lore)
            .build();
    }

    /**
     * Create the close button
     * @return ItemStack button
     */
    private ItemStack createCloseButton() {
        String materialName = plugin.getConfigManager().getLeaderboardGui()
            .getString("buttons.close.material", "BARRIER");
        Material material = Material.valueOf(materialName);

        String name = plugin.getConfigManager().getLeaderboardGui()
            .getString("buttons.close.name", "<red><bold>Close</bold></red>");

        List<String> loreTemplate = plugin.getConfigManager().getLeaderboardGui()
            .getStringList("buttons.close.lore");

        return new ItemBuilder(material)
            .name(name)
            .lore(loreTemplate)
            .build();
    }

    /**
     * Add filler items to empty slots
     * @param inv The inventory
     */
    private void addFillerItems(Inventory inv) {
        List<Integer> slots = plugin.getConfigManager().getLeaderboardGui()
            .getIntegerList("filler.slots");

        String materialName = plugin.getConfigManager().getLeaderboardGui()
            .getString("filler.material", "GRAY_STAINED_GLASS_PANE");
        Material material = Material.valueOf(materialName);

        String name = plugin.getConfigManager().getLeaderboardGui()
            .getString("filler.name", " ");

        ItemStack filler = new ItemBuilder(material)
            .name(name)
            .build();

        for (int slot : slots) {
            if (slot < inv.getSize()) {
                inv.setItem(slot, filler);
            }
        }
    }

    /**
     * Handle inventory clicks
     * @param event The InventoryClickEvent
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        // Check if it's our leaderboard GUI by checking the InventoryHolder
        if (!(event.getInventory().getHolder() instanceof LeaderboardGUI)) {
            return;
        }

        // Cancel ALL click types immediately to prevent ANY item interaction
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();

        // Only handle clicks in the top inventory (the GUI)
        if (event.getClickedInventory() == null || !(event.getClickedInventory().getHolder() instanceof LeaderboardGUI)) {
            return;
        }

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) {
            return;
        }

        int slot = event.getSlot();

        // Get the LeaderboardGUI instance
        LeaderboardGUI gui = (LeaderboardGUI) event.getInventory().getHolder();

        // Get button slots from config
        int sortSlot = plugin.getConfigManager().getLeaderboardGui().getInt("buttons.sort.slot", 49);
        int closeSlot = plugin.getConfigManager().getLeaderboardGui().getInt("buttons.close.slot", 50);

        // Handle sort button click (cycles between REWARDS and TIME)
        if (slot == sortSlot) {
            LeaderboardManager.SortType newSortType = gui.sortType == LeaderboardManager.SortType.REWARDS ?
                LeaderboardManager.SortType.TIME : LeaderboardManager.SortType.REWARDS;
            new LeaderboardGUI(plugin, newSortType).open(player);
        } else if (slot == closeSlot) {
            // Close button
            player.closeInventory();
        }
        // Other slots are player heads or "Your Stats" (no action)
    }

    /**
     * Handle inventory drag to prevent item dragging
     * @param event The InventoryDragEvent
     */
    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        // Check if it's our leaderboard GUI by checking the InventoryHolder
        if (event.getInventory().getHolder() instanceof LeaderboardGUI) {
            // Cancel ALL drag events to prevent item dragging
            event.setCancelled(true);
        }
    }
}
