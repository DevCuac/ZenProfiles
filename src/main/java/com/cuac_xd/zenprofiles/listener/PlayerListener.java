package com.cuac_xd.zenprofiles.listener;

import com.cuac_xd.zenprofiles.ZenProfiles;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {

    private final ZenProfiles plugin;

    public PlayerListener(ZenProfiles plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        plugin.getProfileManager().loadPlayerProfiles(player);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        plugin.getProfileManager().saveAndUnloadPlayer(player);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player) {
            plugin.getProfileManager().tagCombat(player.getUniqueId());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player attacker) {
            plugin.getProfileManager().tagCombat(attacker.getUniqueId());
        }
        if (event.getEntity() instanceof Player victim) {
            plugin.getProfileManager().tagCombat(victim.getUniqueId());
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onAsyncChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        if (plugin.getProfileManager().getPendingChatInput().remove(player.getUniqueId())) {
            event.setCancelled(true);
            String text = PlainTextComponentSerializer.plainText().serialize(event.message()).trim();

            if (text.equalsIgnoreCase("cancelar") || text.equalsIgnoreCase("cancel")) {
                player.sendMessage(plugin.getMessageManager().getComponent("profile.creation-cancelled"));
                return;
            }

            Bukkit.getScheduler().runTask(plugin, () -> plugin.getProfileManager().createProfile(player, text));
        }
    }
}
