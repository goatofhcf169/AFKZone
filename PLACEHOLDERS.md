# AFKPool PlaceholderAPI Placeholders

This document lists all available PlaceholderAPI placeholders for the AFKPool plugin.

## Requirements
- PlaceholderAPI must be installed on your server
- AFKPool will automatically register its placeholders when PlaceholderAPI is detected
- AFKPool uses **MiniMessage** format for all colors and formatting

## Available Placeholders

### Time-Based Placeholders

| Placeholder | Description | Example Output |
|------------|-------------|----------------|
| `%afkpool_time_in_region%` | Current time spent in AFK region (this session) | `5m 30s` |
| `%afkpool_time_remaining%` | Time until next reward | `2m 15s` |
| `%afkpool_session_time%` | Current session time (accumulated + current) | `15m 42s` |
| `%afkpool_total_afk_time%` | Total AFK time across all sessions | `3h 25m 12s` |

### Statistics Placeholders

| Placeholder | Description | Example Output |
|------------|-------------|----------------|
| `%afkpool_total_rewards%` | Total number of rewards received | `42` |
| `%afkpool_last_reward%` | Name of the last reward received | `3x Diamond` |

### Status Placeholders

| Placeholder | Description | Example Output |
|------------|-------------|----------------|
| `%afkpool_in_region%` | Whether player is currently in AFK region | `Yes` / `No` |
| `%afkpool_reward_tier%` | Player's current reward tier (always uppercase) | `DEFAULT` / `VIP` / `MVP` |
| `%afkpool_rewards_disabled%` | Whether player has disabled rewards | `Yes` / `No` |

## Usage Examples

### In Chat Format (MiniMessage)
```
<gray>[<yellow>%afkpool_reward_tier%</yellow>]</gray> %player_name%: Hello!
```
Output: `[VIP] PlayerName: Hello!`

### In Scoreboard (MiniMessage)
```
<gray>AFK Time: <yellow>%afkpool_time_in_region%</yellow></gray>
<gray>Next Reward: <green>%afkpool_time_remaining%</green></gray>
<gray>Total Rewards: <gold>%afkpool_total_rewards%</gold></gray>
```

### In Tab List (MiniMessage)
```
<aqua>%afkpool_reward_tier%</aqua> <dark_gray>|</dark_gray> %player_name%
```
Output: `VIP | PlayerName`

### In Action Bar (MiniMessage - via other plugins)
```
<gray>You are in the AFK Pool! <dark_gray>(<yellow>%afkpool_time_remaining%</yellow> <gray>until reward</gray><dark_gray>)</dark_gray></gray>
```

### In Holograms (MiniMessage - DeluxeMenus, etc.)
```
<gradient:#91EFF6:#FFEAC2><bold>AFK Pool Statistics</bold></gradient>
<gray>Time: <yellow>%afkpool_time_in_region%</yellow></gray>
<gray>Rewards: <green>%afkpool_total_rewards%</green></gray>
<gray>Tier: <aqua>%afkpool_reward_tier%</aqua></gray>
```

## Time Format

All time-based placeholders use the following format:
- **Hours**: `Xh Ym Zs` (e.g., `2h 30m 45s`)
- **Minutes**: `Ym Zs` (e.g., `30m 45s`)
- **Seconds**: `Zs` (e.g., `45s`)

## Tier Values

The `%afkpool_reward_tier%` placeholder will always return **UPPERCASE** values:
- `DEFAULT` - No special permissions
- `VIP` - Has `afkpool.vip` permission
- `MVP` - Has `afkpool.mvp` permission
- Or any custom tier name you configure

## Notes

- All placeholders return empty string (`""`) if:
  - Player data is not loaded
  - Player has never been in the AFK pool
  - PlaceholderAPI is not installed

- Placeholders update in real-time based on player activity

- Time placeholders automatically format to the most appropriate unit

## Support

For issues or questions about placeholders:
1. Check that PlaceholderAPI is installed and running
2. Use `/papi parse me %afkpool_<placeholder>%` to test placeholders
3. Check plugin logs for errors
4. Report issues on the plugin's GitHub page

---

**Plugin Version**: 1.0.0
**Last Updated**: December 28, 2025
