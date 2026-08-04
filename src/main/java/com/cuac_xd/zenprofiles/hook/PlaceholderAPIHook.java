package com.cuac_xd.zenprofiles.hook;

import com.cuac_xd.zenprofiles.ZenProfiles;
import com.cuac_xd.zenprofiles.model.Profile;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlaceholderAPIHook extends PlaceholderExpansion {

    private final ZenProfiles plugin;

    public PlaceholderAPIHook(ZenProfiles plugin) {
        this.plugin = plugin;
    }

    public void registerExpansion() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            this.register();
            plugin.getLogger().info("PlaceholderAPI expansion registered successfully!");
        }
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
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) return "";

        Profile active = plugin.getProfileManager().getActiveProfile(player.getUniqueId());

        switch (params.toLowerCase()) {
            case "profile_name":
            case "name":
                return active != null ? active.getName() : "None";
            case "profile_id":
            case "id":
                return active != null ? active.getProfileId().toString() : "None";
            case "total_profiles":
            case "total":
                return String.valueOf(plugin.getProfileManager().getPlayerProfiles(player.getUniqueId()).size());
            case "max_profiles":
            case "max":
                return String.valueOf(plugin.getProfileManager().getMaxProfiles(player));
            case "coins":
            case "balance":
                return active != null ? String.format("%.2f", active.getData().getBalance()) : "0.00";
            case "formatted_balance":
            case "formatted_coins":
                return active != null ? String.format("$%,.2f", active.getData().getBalance()) : "$0.00";
            default:
                return null;
        }
    }
}
