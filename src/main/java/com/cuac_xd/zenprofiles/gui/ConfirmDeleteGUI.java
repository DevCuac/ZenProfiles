package com.cuac_xd.zenprofiles.gui;

import com.cuac_xd.zenprofiles.ZenProfiles;
import com.cuac_xd.zenprofiles.model.Profile;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Confirmation GUI displayed when a player attempts to delete a profile.
 * Features strict confirmation requirements to prevent accidental data loss.
 *
 * @author cuac_xd
 */
public class ConfirmDeleteGUI {

    private final ZenProfiles plugin;
    private static final Map<UUID, Profile> targetProfiles = new HashMap<>();

    public ConfirmDeleteGUI(ZenProfiles plugin) {
        this.plugin = plugin;
    }

    /**
     * Opens the deletion confirmation GUI for a player targeting a specific profile.
     *
     * @param player The player deleting the profile.
     * @param target The target profile to be deleted.
     */
    public void open(Player player, Profile target) {
        targetProfiles.put(player.getUniqueId(), target);

        FileConfiguration config = plugin.getMenuManager().getConfirmDeleteConfig();
        String titleText = config.getString("title", "<red><bold>Confirm Deletion</bold></red>");
        int size = config.getInt("size", 27);

        ZenHolder holder = new ZenHolder("confirm_delete");
        Inventory gui = Bukkit.createInventory(holder, size, plugin.getMessageManager().parse(titleText));
        holder.setInventory(gui);

        // Fill decorative elements
        if (config.getBoolean("filler.enabled", true)) {
            Material fillerMat = Material.matchMaterial(config.getString("filler.material", "GRAY_STAINED_GLASS_PANE"));
            if (fillerMat == null) fillerMat = Material.GRAY_STAINED_GLASS_PANE;

            ItemStack fillerItem = new ItemStack(fillerMat);
            ItemMeta meta = fillerItem.getItemMeta();
            if (meta != null) {
                meta.displayName(plugin.getMessageManager().parse(" "));
                fillerItem.setItemMeta(meta);
            }

            for (int i = 0; i < size; i++) {
                gui.setItem(i, fillerItem);
            }
        }

        // Confirm Button (Red Stained Glass / Terracotta)
        int confirmSlot = config.getInt("items.confirm.slot", 11);
        gui.setItem(confirmSlot, buildButtonItem(config, "items.confirm", target, "RED_TERRACOTTA"));

        // Cancel Button (Green Stained Glass / Terracotta)
        int cancelSlot = config.getInt("items.cancel.slot", 15);
        gui.setItem(cancelSlot, buildButtonItem(config, "items.cancel", target, "GREEN_TERRACOTTA"));

        player.openInventory(gui);
    }

    private ItemStack buildButtonItem(FileConfiguration config, String path, Profile profile, String defaultMat) {
        Material mat = Material.matchMaterial(config.getString(path + ".material", defaultMat));
        if (mat == null) mat = Material.matchMaterial(defaultMat);
        if (mat == null) mat = Material.STONE;

        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        String name = config.getString(path + ".name", "");
        name = name.replace("%profile_name%", profile.getName());
        meta.displayName(plugin.getMessageManager().parse(name));

        List<String> rawLore = config.getStringList(path + ".lore");
        List<net.kyori.adventure.text.Component> formattedLore = new ArrayList<>();

        for (String line : rawLore) {
            line = line.replace("%profile_name%", profile.getName());
            formattedLore.add(plugin.getMessageManager().parse(line));
        }

        meta.lore(formattedLore);
        item.setItemMeta(meta);
        return item;
    }

    /**
     * Listener class managing confirmation GUI interactions with anti-dupe security.
     */
    public static class GUIListener implements Listener {

        private final ZenProfiles plugin;

        public GUIListener(ZenProfiles plugin) {
            this.plugin = plugin;
        }

        @EventHandler(priority = EventPriority.HIGH)
        public void onInventoryClick(InventoryClickEvent event) {
            if (!(event.getInventory().getHolder() instanceof ZenHolder holder)) return;
            if (!"confirm_delete".equals(holder.getMenuId())) return;

            event.setCancelled(true);

            if (!(event.getWhoClicked() instanceof Player player)) return;
            if (event.getClickedInventory() != event.getInventory()) return;

            Profile target = targetProfiles.get(player.getUniqueId());
            if (target == null) {
                player.closeInventory();
                return;
            }

            FileConfiguration config = plugin.getMenuManager().getConfirmDeleteConfig();
            int confirmSlot = config.getInt("items.confirm.slot", 11);
            int cancelSlot = config.getInt("items.cancel.slot", 15);

            if (event.getSlot() == confirmSlot) {
                // Confirmed deletion
                targetProfiles.remove(player.getUniqueId());
                player.closeInventory();

                plugin.getProfileManager().deleteProfile(player.getUniqueId(), target.getProfileId()).thenAccept(success -> {
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        if (success) {
                            player.sendMessage(plugin.getMessageManager().getMessage("profile-deleted", "%profile_name%", target.getName()));
                        }
                        new ProfileGUI(plugin).open(player);
                    });
                });

            } else if (event.getSlot() == cancelSlot) {
                // Cancelled deletion
                targetProfiles.remove(player.getUniqueId());
                player.closeInventory();
                new ProfileGUI(plugin).open(player);
            }
        }

        @EventHandler(priority = EventPriority.HIGH)
        public void onInventoryDrag(InventoryDragEvent event) {
            if (event.getInventory().getHolder() instanceof ZenHolder holder && "confirm_delete".equals(holder.getMenuId())) {
                event.setCancelled(true);
            }
        }
    }
}
