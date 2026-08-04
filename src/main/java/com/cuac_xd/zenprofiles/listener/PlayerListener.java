package com.cuac_xd.zenprofiles.listener;

import com.cuac_xd.zenprofiles.ZenProfiles;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Core Bukkit player event listener.
 * Manages player profile loading on join, profile saving on quit, combat tracking,
 * and combat state verification.
 *
 * @author cuac_xd
 */
public class PlayerListener implements Listener {

    private final ZenProfiles plugin;

    public PlayerListener(ZenProfiles plugin) {
        this.plugin = plugin;
    }

    /**
     * Handles player join event. Loads player profile data asynchronously.
     *
     * @param event The player join event.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        plugin.getProfileManager().loadPlayerProfiles(player);
    }

    /**
     * Handles player quit event. Saves active profile data and unloads player state asynchronously.
     *
     * @param event The player quit event.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        plugin.getProfileManager().saveAndUnloadPlayer(player);
    }

    /**
     * Tracks PvP/PvE combat interactions for profile switch restrictions.
     *
     * @param event The entity damage event.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player) {
            plugin.getProfileManager().tagCombat(player);
        }
    }

    /**
     * Tracks combat interactions when a player attacks another entity.
     *
     * @param event The entity damage by entity event.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player player) {
            plugin.getProfileManager().tagCombat(player);
        }
    }
}
