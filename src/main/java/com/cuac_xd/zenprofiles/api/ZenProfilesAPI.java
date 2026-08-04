package com.cuac_xd.zenprofiles.api;

import com.cuac_xd.zenprofiles.ZenProfiles;
import com.cuac_xd.zenprofiles.model.Profile;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class ZenProfilesAPI {

    private static ZenProfiles instance;

    public static void init(ZenProfiles plugin) {
        instance = plugin;
    }

    /**
     * Gets the currently active profile of an online player.
     */
    public static Profile getActiveProfile(Player player) {
        if (instance == null || player == null) return null;
        return instance.getProfileManager().getActiveProfile(player.getUniqueId());
    }

    /**
     * Gets the active profile UUID of a player, or the player's own account UUID if no profile is active.
     */
    public static UUID getActiveProfileUUID(Player player) {
        if (player == null) return null;
        Profile active = getActiveProfile(player);
        return active != null ? active.getProfileId() : player.getUniqueId();
    }

    /**
     * Gets the active profile UUID of an online player by player account UUID.
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
     * Checks if a player has an active profile loaded.
     */
    public static boolean hasActiveProfile(Player player) {
        return getActiveProfile(player) != null;
    }

    /**
     * Gets all profiles owned by a player.
     */
    public static List<Profile> getProfiles(UUID playerUuid) {
        if (instance == null || playerUuid == null) return List.of();
        return instance.getProfileManager().getPlayerProfiles(playerUuid);
    }

    /**
     * Initiates a profile switch for a player to a target profile.
     */
    public static void switchProfile(Player player, Profile targetProfile) {
        if (instance != null && player != null && targetProfile != null) {
            instance.getProfileManager().initiateSwitch(player, targetProfile);
        }
    }
}
