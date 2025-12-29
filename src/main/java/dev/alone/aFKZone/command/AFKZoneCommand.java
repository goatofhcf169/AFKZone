package dev.alone.aFKZone.command;

import dev.alone.aFKZone.AFKZone;
import dev.alone.aFKZone.data.AFKPlayer;
import dev.alone.aFKZone.gui.LeaderboardGUI;
import dev.alone.aFKZone.manager.LeaderboardManager;
import dev.alone.aFKZone.util.MessageUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Main command handler for AFKZone
 */
public class AFKZoneCommand implements CommandExecutor, TabCompleter {

    private final AFKZone plugin;

    /**
     * Create a new AFKZoneCommand
     * @param plugin The plugin instance
     */
    public AFKZoneCommand(AFKZone plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "stats":
                return handleStats(sender, args);

            case "reload":
                return handleReload(sender);

            case "toggle":
                return handleToggle(sender);

            case "info":
                return handleInfo(sender);

            case "reset":
                return handleReset(sender, args);

            case "leaderboard":
            case "lb":
            case "top":
                return handleLeaderboard(sender, args);

            case "help":
            default:
                sendHelp(sender);
                return true;
        }
    }

    /**
     * Handle /afkzone stats [player]
     */
    private boolean handleStats(CommandSender sender, String[] args) {
        Player target;

        if (args.length > 1) {
            // Check permission to view others' stats
            if (!sender.hasPermission("afkzone.stats.others")) {
                sendMessage(sender, plugin.getConfigManager().getMessage("no-permission"));
                return true;
            }

            target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sendMessage(sender, plugin.getConfigManager().getMessage("invalid-player"));
                return true;
            }
        } else {
            // View own stats
            if (!(sender instanceof Player)) {
                sendMessage(sender, plugin.getConfigManager().getMessage("player-only"));
                return true;
            }

            if (!sender.hasPermission("afkzone.stats")) {
                sendMessage(sender, plugin.getConfigManager().getMessage("no-permission"));
                return true;
            }

            target = (Player) sender;
        }

        AFKPlayer afkPlayer = plugin.getAFKManager().getAFKPlayer(target.getUniqueId());
        if (afkPlayer == null) {
            sender.sendMessage(MessageUtil.toComponent("<red>No AFK data found for that player.</red>"));
            return true;
        }

        // Send stats
        String header = plugin.getConfigManager().getMessages().getString("messages.stats-header");
        String time = plugin.getConfigManager().getMessages().getString("messages.stats-time");
        String rewards = plugin.getConfigManager().getMessages().getString("messages.stats-rewards");
        String next = plugin.getConfigManager().getMessages().getString("messages.stats-next");
        String footer = plugin.getConfigManager().getMessages().getString("messages.stats-footer");

        // Calculate time remaining
        long currentTime = System.currentTimeMillis();
        long timeSinceLastReward = currentTime - afkPlayer.getLastRewardTime();
        long rewardInterval = plugin.getConfigManager().getRewardInterval();
        long timeRemaining = Math.max(0, rewardInterval - timeSinceLastReward);

        sender.sendMessage(MessageUtil.toComponent(header));
        sender.sendMessage(MessageUtil.toComponent(time
            .replace("%afkpool_time_in_region%", MessageUtil.formatTime(afkPlayer.getSessionTime()))));
        sender.sendMessage(MessageUtil.toComponent(rewards
            .replace("%afkpool_total_rewards%", String.valueOf(afkPlayer.getTotalRewards()))));
        sender.sendMessage(MessageUtil.toComponent(next
            .replace("%afkpool_time_remaining%", afkPlayer.isInRegion() ?
                MessageUtil.formatTime(timeRemaining) : "Not in region")));
        sender.sendMessage(MessageUtil.toComponent(footer));

        return true;
    }

    /**
     * Handle /afkzone reload
     */
    private boolean handleReload(CommandSender sender) {
        if (!sender.hasPermission("afkzone.reload")) {
            sendMessage(sender, plugin.getConfigManager().getMessage("no-permission"));
            return true;
        }

        boolean success = plugin.getConfigManager().reloadConfigs();

        if (success) {
            sendMessage(sender, plugin.getConfigManager().getMessage("reload-success"));
        } else {
            sendMessage(sender, plugin.getConfigManager().getMessage("reload-failed"));
        }

        return true;
    }

    /**
     * Handle /afkzone toggle
     */
    private boolean handleToggle(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sendMessage(sender, plugin.getConfigManager().getMessage("player-only"));
            return true;
        }

        if (!sender.hasPermission("afkzone.toggle")) {
            sendMessage(sender, plugin.getConfigManager().getMessage("no-permission"));
            return true;
        }

        Player player = (Player) sender;
        AFKPlayer afkPlayer = plugin.getAFKManager().getOrCreateAFKPlayer(player);

        afkPlayer.setRewardsDisabled(!afkPlayer.isRewardsDisabled());

        String message = afkPlayer.isRewardsDisabled() ?
            "<red>AFK rewards have been disabled.</red>" :
            "<green>AFK rewards have been enabled.</green>";

        Component messageComponent = MessageUtil.toComponent(plugin.getConfigManager().getPrefix() + " " + message);
        player.sendMessage(messageComponent);

        return true;
    }

    /**
     * Handle /afkzone info
     */
    private boolean handleInfo(CommandSender sender) {
        if (!sender.hasPermission("afkzone.info")) {
            sendMessage(sender, plugin.getConfigManager().getMessage("no-permission"));
            return true;
        }

        sender.sendMessage(MessageUtil.toComponent("<dark_gray><strikethrough>----------</strikethrough></dark_gray> <aqua><bold>AFK Zone Info</bold></aqua> <dark_gray><strikethrough>----------</strikethrough></dark_gray>"));
        sender.sendMessage(MessageUtil.toComponent("<gray>Version: <yellow>" + plugin.getDescription().getVersion() + "</yellow></gray>"));
        sender.sendMessage(MessageUtil.toComponent("<gray>Author: <yellow>" + plugin.getDescription().getAuthors().get(0) + "</yellow></gray>"));
        sender.sendMessage(MessageUtil.toComponent("<gray>Tracked Players: <yellow>" + plugin.getAFKManager().getPlayerCount() + "</yellow></gray>"));
        sender.sendMessage(MessageUtil.toComponent("<gray>Players in AFK Region: <yellow>" + plugin.getAFKManager().getPlayersInRegionCount() + "</yellow></gray>"));
        sender.sendMessage(MessageUtil.toComponent("<gray>Reward Interval: <yellow>" + MessageUtil.formatTime(plugin.getConfigManager().getRewardInterval()) + "</yellow></gray>"));
        sender.sendMessage(MessageUtil.toComponent("<gray>Loaded Reward Pools: <yellow>" + plugin.getConfigManager().getRewardPools().size() + "</yellow></gray>"));
        sender.sendMessage(MessageUtil.toComponent("<dark_gray><strikethrough>----------------------------------</strikethrough></dark_gray>"));

        return true;
    }

    /**
     * Handle /afkzone reset <player>
     */
    private boolean handleReset(CommandSender sender, String[] args) {
        if (!sender.hasPermission("afkzone.reset")) {
            sendMessage(sender, plugin.getConfigManager().getMessage("no-permission"));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(MessageUtil.toComponent("<red>Usage: /afkzone reset <player></red>"));
            return true;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sendMessage(sender, plugin.getConfigManager().getMessage("invalid-player"));
            return true;
        }

        AFKPlayer afkPlayer = plugin.getAFKManager().getAFKPlayer(target.getUniqueId());
        if (afkPlayer != null) {
            afkPlayer.reset();
            afkPlayer.setTotalRewards(0);
            afkPlayer.setTotalAFKTime(0);

            sender.sendMessage(MessageUtil.toComponent(plugin.getConfigManager().getPrefix() +
                " <green>Reset AFK data for " + target.getName() + "</green>"));
        } else {
            sender.sendMessage(MessageUtil.toComponent("<red>No AFK data found for that player.</red>"));
        }

        return true;
    }

    /**
     * Handle /afkzone leaderboard [rewards|time]
     */
    private boolean handleLeaderboard(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sendMessage(sender, plugin.getConfigManager().getMessage("player-only"));
            return true;
        }

        if (!sender.hasPermission("afkzone.leaderboard")) {
            sendMessage(sender, plugin.getConfigManager().getMessage("no-permission"));
            return true;
        }

        Player player = (Player) sender;

        // Determine sort type from arguments
        LeaderboardManager.SortType sortType = LeaderboardManager.SortType.REWARDS;
        if (args.length > 1) {
            if (args[1].equalsIgnoreCase("time")) {
                sortType = LeaderboardManager.SortType.TIME;
            }
        }

        // Open the leaderboard GUI
        new LeaderboardGUI(plugin, sortType).open(player);

        return true;
    }

    /**
     * Send help menu (operator-aware)
     * Non-OP: Shows stats, leaderboard, toggle
     * OP: Shows all commands
     */
    private void sendHelp(CommandSender sender) {
        String header = plugin.getConfigManager().getMessages().getString("help.header");
        String footer = plugin.getConfigManager().getMessages().getString("help.footer");

        sender.sendMessage(MessageUtil.toComponent(header));

        boolean isOp = sender.isOp();

        // Always show these commands to everyone
        sender.sendMessage(MessageUtil.toComponent("<yellow>/afkzone stats</yellow> <gray>- View your AFK statistics</gray>"));
        sender.sendMessage(MessageUtil.toComponent("<yellow>/afkzone leaderboard</yellow> <gray>- View the top players</gray>"));
        sender.sendMessage(MessageUtil.toComponent("<yellow>/afkzone toggle</yellow> <gray>- Toggle AFK rewards on/off</gray>"));

        // Show admin commands only to operators
        if (isOp) {
            sender.sendMessage(MessageUtil.toComponent("<yellow>/afkzone reload</yellow> <gray>- Reload configuration</gray> <red>(Admin)</red>"));
            sender.sendMessage(MessageUtil.toComponent("<yellow>/afkzone reset <player></yellow> <gray>- Reset player data</gray> <red>(Admin)</red>"));
            sender.sendMessage(MessageUtil.toComponent("<yellow>/afkzone info</yellow> <gray>- View plugin information</gray>"));
        }

        sender.sendMessage(MessageUtil.toComponent(footer));
    }

    /**
     * Send a message with prefix
     */
    private void sendMessage(CommandSender sender, String message) {
        String prefix = plugin.getConfigManager().getPrefix();
        message = message.replace("%prefix%", prefix);
        Component messageComponent = MessageUtil.toComponent(message);
        sender.sendMessage(messageComponent);
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            List<String> subCommands = Arrays.asList("stats", "reload", "toggle", "info", "reset", "leaderboard", "help");
            return subCommands.stream()
                .filter(cmd -> cmd.toLowerCase().startsWith(args[0].toLowerCase()))
                .collect(Collectors.toList());
        }

        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("stats") || args[0].equalsIgnoreCase("reset")) {
                return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
            } else if (args[0].equalsIgnoreCase("leaderboard") || args[0].equalsIgnoreCase("lb") || args[0].equalsIgnoreCase("top")) {
                List<String> sortOptions = Arrays.asList("rewards", "time");
                return sortOptions.stream()
                    .filter(option -> option.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
            }
        }

        return completions;
    }
}
