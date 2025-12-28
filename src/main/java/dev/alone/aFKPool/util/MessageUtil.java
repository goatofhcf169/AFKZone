package dev.alone.aFKPool.util;

import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;

/**
 * Utility class for message formatting using MiniMessage
 */
public class MessageUtil {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.legacySection();

    /**
     * Parse a MiniMessage string to a Component
     * @param message The MiniMessage string
     * @return Adventure Component
     */
    public static Component toComponent(String message) {
        if (message == null || message.isEmpty()) {
            return Component.empty();
        }
        return MINI_MESSAGE.deserialize(message);
    }

    /**
     * Parse a MiniMessage string and convert to legacy string (for backwards compatibility)
     * @param message The MiniMessage string
     * @return Legacy colored string
     */
    public static String colorize(String message) {
        if (message == null || message.isEmpty()) {
            return "";
        }

        // Parse MiniMessage and convert to legacy
        Component component = MINI_MESSAGE.deserialize(message);
        return LEGACY_SERIALIZER.serialize(component);
    }

    /**
     * Replace placeholders in a message (PlaceholderAPI support)
     * @param player The player for placeholder context
     * @param message The message to process
     * @return Processed message
     */
    public static String replacePlaceholders(Player player, String message) {
        if (message == null || message.isEmpty()) {
            return "";
        }

        String processed = message;

        // PlaceholderAPI support
        if (player != null && isPlaceholderAPIAvailable()) {
            processed = PlaceholderAPI.setPlaceholders(player, processed);
        }

        return colorize(processed);
    }

    /**
     * Replace placeholders and return a Component (PlaceholderAPI support)
     * @param player The player for placeholder context
     * @param message The MiniMessage string
     * @return Adventure Component
     */
    public static Component toComponentWithPlaceholders(Player player, String message) {
        if (message == null || message.isEmpty()) {
            return Component.empty();
        }

        String processed = message;

        // PlaceholderAPI support
        if (player != null && isPlaceholderAPIAvailable()) {
            processed = PlaceholderAPI.setPlaceholders(player, processed);
        }

        return MINI_MESSAGE.deserialize(processed);
    }

    /**
     * Format time in milliseconds to a readable string
     * @param millis Time in milliseconds
     * @return Formatted time string (e.g., "5m 30s")
     */
    public static String formatTime(long millis) {
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;

        seconds %= 60;
        minutes %= 60;

        if (hours > 0) {
            return String.format("%dh %dm %ds", hours, minutes, seconds);
        } else if (minutes > 0) {
            return String.format("%dm %ds", minutes, seconds);
        } else {
            return String.format("%ds", seconds);
        }
    }

    /**
     * Replace time placeholders in a message
     * @param message The message with time placeholders
     * @param millis Time in milliseconds
     * @return Message with replaced time
     */
    public static String replaceTime(String message, long millis) {
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;

        seconds %= 60;
        minutes %= 60;

        return message
            .replace("%hours%", String.valueOf(hours))
            .replace("%minutes%", String.valueOf(minutes))
            .replace("%seconds%", String.valueOf(seconds));
    }

    /**
     * Check if PlaceholderAPI is available
     * @return true if PlaceholderAPI is available
     */
    private static boolean isPlaceholderAPIAvailable() {
        try {
            Class.forName("me.clip.placeholderapi.PlaceholderAPI");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
