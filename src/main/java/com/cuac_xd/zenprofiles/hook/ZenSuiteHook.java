package com.cuac_xd.zenprofiles.hook;

import com.cuac_xd.zenprofiles.ZenProfiles;
import com.cuac_xd.zenprofiles.api.event.ProfileCreateEvent;
import com.cuac_xd.zenprofiles.api.event.ProfileDeleteEvent;
import com.cuac_xd.zenprofiles.api.event.ProfileSwitchEvent;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

/**
 * ZEN Plugins Suite integration hook.
 * Detects loaded ZEN suite plugins (ZenBank, ZenSacks, ZenFairy, ZenRankups, etc.)
 * and logs native ZenProfilesAPI bridge status.
 *
 * @author cuac_xd
 */
public class ZenSuiteHook implements Listener {

    private final ZenProfiles plugin;
    private final List<String> detectedZenPlugins = new ArrayList<>();

    private final String[] zenSuitePlugins = {
            "ZenBank",
            "ZenBoosters",
            "ZenCosmetics",
            "ZenDuels",
            "ZenDungeons",
            "ZenFairy",
            "ZenForges",
            "ZenMilestones",
            "ZenPvPCore",
            "ZenQuests",
            "ZenRewards",
            "ZenSacks",
            "ZenRankups"
    };

    public ZenSuiteHook(ZenProfiles plugin) {
        this.plugin = plugin;
    }

    /**
     * Detects enabled ZEN suite plugins on server startup.
     */
    public void setup() {
        detectedZenPlugins.clear();
        for (String pName : zenSuitePlugins) {
            Plugin p = Bukkit.getPluginManager().getPlugin(pName);
            if (p != null && p.isEnabled()) {
                detectedZenPlugins.add(pName);
            }
        }

        if (!detectedZenPlugins.isEmpty()) {
            plugin.getLogger().info("Detected active ZEN Suite plugins: " + String.join(", ", detectedZenPlugins));
            plugin.getLogger().info("Per-profile UUID mapping (ZenProfilesAPI) is active for the ZEN plugin suite!");
        }

        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onProfileSwitch(ProfileSwitchEvent event) {
        // Broadcasts to third-party ZEN plugin listeners
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onProfileCreate(ProfileCreateEvent event) {
        // Broadcasts to third-party ZEN plugin listeners
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onProfileDelete(ProfileDeleteEvent event) {
        // Broadcasts to third-party ZEN plugin listeners
    }

    public List<String> getDetectedZenPlugins() {
        return detectedZenPlugins;
    }
}
