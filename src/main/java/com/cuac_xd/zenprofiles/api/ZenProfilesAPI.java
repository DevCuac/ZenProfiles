package com.cuac_xd.zenprofiles.api;

import com.cuac_xd.zenprofiles.ZenProfiles;
import com.cuac_xd.zenprofiles.model.Profile;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

/**
 * Public developer API entry point for ZenProfiles.
 * Exposes methods to query active profiles, retrieve profile UUIDs for database operations,
 * and initiate profile switches programmatically.
 *
 * @author cuac_xd
 */
public class ZenProfilesAPI {

    private static ZenProfiles instance;

    /**
     * Internal API initializer.
     *
     * @param plugin Main plugin instance.
     */
    public static void init(ZenProfiles plugin) {
        instance = plugin;
    }

    /**
     * Gets the currently active Profile of an online player.
     *
     * @param player The target player.
     * @return Active Profile object, or null if none loaded.
     */
    public static Profile getActiveProfile(Player player) {
        if (instance == null || player == null) return null;
        return instance.getProfileManager().getActiveProfile(player.getUniqueId());
    }

    /**
     * Gets the active profile UUID of a player, or falls back to the player's account UUID.
     *
     * @param player The target player.
     * @return Active profile UUID or account UUID.
     */
    public static UUID getActiveProfileUUID(Player player) {
        if (player == null) return null;
        Profile active = getActiveProfile(player);
        return active != null ? active.getProfileId() : player.getUniqueId();
    }

    /**
     * Gets the active profile UUID of an online player by player account UUID.
     *
     * @param playerUuid The player account UUID.
     * @return Active profile UUID or account UUID.
     */
    public static UUID getActiveProfileUUID(UUID playerUuid) {
        if (playerUuid == null) return null;
        Player online = Bukkit.getPlayer(playerUuid);
        if (online != null) {
            return getActiveProfileUUID(online);
        }
        return playerUuid;
    }

    /**
     * Checks if an online player has an active profile loaded.
     *
     * @param player The target player.
     * @return true if an active profile is present.
     */
    public static boolean hasActiveProfile(Player player) {
        return getActiveProfile(player) != null;
    }

    /**
     * Gets all profiles owned by a player UUID.
     *
     * @param playerUuid The player account UUID.
     * @return List of profiles.
     */
    public static List<Profile> getProfiles(UUID playerUuid) {
        if (instance == null || playerUuid == null) return List.of();
        return instance.getProfileManager().getPlayerProfiles(playerUuid);
    }

    /**
     * Initiates an asynchronous profile switch for a player.
     *
     * @param player The target player.
     * @param targetProfile The target profile to switch to.
     */
    public static void switchProfile(Player player, Profile targetProfile) {
        if (instance != null && player != null && targetProfile != null) {
            instance.getProfileManager().initiateSwitch(player, targetProfile);
        }
    }
}
