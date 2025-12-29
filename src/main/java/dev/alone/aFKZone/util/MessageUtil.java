package dev.alone.aFKZone.util;

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
    private static final LegacyComponentSerializer LEGACY_AMPERSAND_SERIALIZER = LegacyComponentSerializer.legacyAmpersand();

    /**
     * Parse a message string to a Component (supports both MiniMessage and legacy &codes)
     * Handles mixed formats by converting legacy codes first
     * @param message The message string
     * @return Adventure Component
     */
    public static Component toComponent(String message) {
        if (message == null || message.isEmpty()) {
            return Component.empty();
        }

        // Convert legacy codes to MiniMessage format for consistent handling
        String processed = convertLegacyToMiniMessage(message);

        // Parse as MiniMessage
        try {
            return MINI_MESSAGE.deserialize(processed);
        } catch (Exception e) {
            // Fallback to legacy if MiniMessage fails
            return LEGACY_AMPERSAND_SERIALIZER.deserialize(message);
        }
    }

    /**
     * Convert legacy color codes (&) to MiniMessage format
     * @param message The message with legacy codes
     * @return Message with MiniMessage format
     */
    private static String convertLegacyToMiniMessage(String message) {
        if (message == null || !message.contains("&")) {
            return message;
        }

        // Map of legacy codes to MiniMessage equivalents
        message = message.replace("&0", "<black>");
        message = message.replace("&1", "<dark_blue>");
        message = message.replace("&2", "<dark_green>");
        message = message.replace("&3", "<dark_aqua>");
        message = message.replace("&4", "<dark_red>");
        message = message.replace("&5", "<dark_purple>");
        message = message.replace("&6", "<gold>");
        message = message.replace("&7", "<gray>");
        message = message.replace("&8", "<dark_gray>");
        message = message.replace("&9", "<blue>");
        message = message.replace("&a", "<green>");
        message = message.replace("&b", "<aqua>");
        message = message.replace("&c", "<red>");
        message = message.replace("&d", "<light_purple>");
        message = message.replace("&e", "<yellow>");
        message = message.replace("&f", "<white>");

        // Formatting codes
        message = message.replace("&k", "<obfuscated>");
        message = message.replace("&l", "<bold>");
        message = message.replace("&m", "<strikethrough>");
        message = message.replace("&n", "<underlined>");
        message = message.replace("&o", "<italic>");
        message = message.replace("&r", "<reset>");

        return message;
    }

    /**
     * Colorize a message string (supports both MiniMessage and legacy &codes)
     * @param message The message string
     * @return Colored string with § codes
     */
    public static String colorize(String message) {
        if (message == null || message.isEmpty()) {
            return "";
        }

        // Check if message contains legacy color codes (&)
        if (message.contains("&")) {
            // Convert & to § directly
            return message.replace('&', '§');
        }

        // Otherwise parse as MiniMessage and convert to legacy
        try {
            Component component = MINI_MESSAGE.deserialize(message);
            return LEGACY_SERIALIZER.serialize(component);
        } catch (Exception e) {
            // Fallback: just return as-is
            return message;
        }
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
     * @param message The message string (supports both MiniMessage and legacy codes)
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

        // Use toComponent which handles both formats
        return toComponent(processed);
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
