package com.cuac_xd.zenprofiles.hook;

import com.cuac_xd.zenprofiles.ZenProfiles;
import com.cuac_xd.zenprofiles.model.Profile;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.context.ContextCalculator;
import net.luckperms.api.context.ContextConsumer;
import net.luckperms.api.context.ContextSet;
import net.luckperms.api.context.ImmutableContextSet;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * LuckPerms ContextCalculator integration hook.
 * Registers a custom context key 'zenprofiles:profile=<profile_uuid>' to enable
 * per-profile permission node assignments while preserving global player groups.
 *
 * @author cuac_xd
 */
public class LuckPermsHook implements ContextCalculator<Player> {

    private final ZenProfiles plugin;
    private LuckPerms luckPerms;
    private boolean enabled = false;

    public LuckPermsHook(ZenProfiles plugin) {
        this.plugin = plugin;
    }

    /**
     * Bootstraps the LuckPerms context calculator registration.
     */
    public void setup() {
        if (Bukkit.getPluginManager().isPluginEnabled("LuckPerms")) {
            try {
                this.luckPerms = LuckPermsProvider.get();
                this.luckPerms.getContextManager().registerCalculator(this);
                this.enabled = true;
                plugin.getLogger().info("LuckPerms per-profile context calculator registered successfully!");
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to hook into LuckPerms: " + e.getMessage());
            }
        }
    }

    @Override
    public void calculate(@NonNull Player target, @NonNull ContextConsumer consumer) {
        Profile activeProfile = plugin.getProfileManager().getActiveProfile(target.getUniqueId());
        if (activeProfile != null) {
            consumer.accept("zenprofiles:profile", activeProfile.getProfileId().toString());
            consumer.accept("zenprofiles:profile_name", activeProfile.getName().toLowerCase());
        }
    }

    @Override
    public @NonNull ContextSet estimatePotentialContexts() {
        ImmutableContextSet.Builder builder = ImmutableContextSet.builder();
        builder.add("zenprofiles:profile", "none");
        return builder.build();
    }

    /**
     * Recalculates and updates the LuckPerms context for a player upon profile switch.
     *
     * @param player The player instance.
     */
    public void updateContext(Player player) {
        if (enabled && luckPerms != null) {
            luckPerms.getContextManager().signalContextUpdate(player);
        }
    }
}
