package com.cuac_xd.zenprofiles.hook;

import com.cuac_xd.zenprofiles.ZenProfiles;
import com.cuac_xd.zenprofiles.model.Profile;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * PlaceholderAPI expansion integration hook.
 * Exposes %zenprofiles_profile_name%, %zenprofiles_profile_id%,
 * %zenprofiles_total_profiles%, %zenprofiles_max_profiles%, %zenprofiles_coins%,
 * and %zenprofiles_formatted_balance% placeholders.
 *
 * @author cuac_xd
 */
public class PlaceholderAPIHook extends PlaceholderExpansion {

    private final ZenProfiles plugin;

    public PlaceholderAPIHook(ZenProfiles plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "zenprofiles";
    }

    @Override
    public @NotNull String getAuthor() {
        return "cuac_xd";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer offlinePlayer, @NotNull String params) {
        if (offlinePlayer == null) return "";

        Player player = offlinePlayer.isOnline() ? offlinePlayer.getPlayer() : null;

        Profile active = plugin.getProfileManager().getActiveProfile(offlinePlayer.getUniqueId());

        switch (params.toLowerCase()) {
            case "profile_name":
                return active != null ? active.getName() : "None";

            case "profile_id":
                return active != null ? active.getProfileId().toString() : "None";

            case "total_profiles":
                List<Profile> profiles = plugin.getProfileManager().getPlayerProfiles(offlinePlayer.getUniqueId());
                return String.valueOf(profiles.size());

            case "max_profiles":
                return player != null ? String.valueOf(plugin.getProfileManager().getMaxProfiles(player)) : "3";

            case "coins":
                return active != null ? String.valueOf(active.getData().getBalance()) : "0.0";

            case "formatted_balance":
            case "formatted_coins":
                return active != null ? plugin.getMessageManager().formatCoins(active.getData().getBalance()) : "0.00";
        }

        return null;
    }
}
