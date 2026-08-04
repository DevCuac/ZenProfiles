package com.cuac_xd.zenprofiles.hook;

import com.cuac_xd.zenprofiles.ZenProfiles;
import com.cuac_xd.zenprofiles.model.Profile;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.context.ContextCalculator;
import net.luckperms.api.context.ContextConsumer;
import net.luckperms.api.context.ContextSet;
import net.luckperms.api.context.MutableContextSet;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.logging.Level;

public class LuckPermsHook implements ContextCalculator<Player> {

    private final ZenProfiles plugin;
    private LuckPerms luckPerms;
    private boolean enabled = false;
    private String contextKey = "profile";

    public LuckPermsHook(ZenProfiles plugin) {
        this.plugin = plugin;
    }

    public void setup() {
        if (!plugin.getConfig().getBoolean("luckperms.enabled", true)) {
            return;
        }

        if (Bukkit.getPluginManager().getPlugin("LuckPerms") == null) {
            plugin.getLogger().info("LuckPerms not found. Per-profile LuckPerms context integration is disabled.");
            return;
        }

        try {
            this.luckPerms = LuckPermsProvider.get();
            this.contextKey = plugin.getConfig().getString("luckperms.context-key", "profile");
            this.luckPerms.getContextManager().registerCalculator(this);
            this.enabled = true;
            plugin.getLogger().info("LuckPerms integration enabled! Registered context: zenprofiles:" + contextKey);
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Failed to hook into LuckPerms: " + e.getMessage(), e);
        }
    }

    @Override
    public void calculate(Player target, ContextConsumer consumer) {
        Profile activeProfile = plugin.getProfileManager().getActiveProfile(target.getUniqueId());
        if (activeProfile != null) {
            consumer.accept("zenprofiles:" + contextKey, activeProfile.getProfileId().toString());
            consumer.accept("zenprofiles:name", activeProfile.getName().toLowerCase());
        }
    }

    @Override
    public ContextSet estimatePotentialContexts() {
        MutableContextSet set = MutableContextSet.create();
        set.add("zenprofiles:" + contextKey, "*");
        set.add("zenprofiles:name", "*");
        return set.immutableCopy();
    }

    public boolean isEnabled() {
        return enabled;
    }
}
