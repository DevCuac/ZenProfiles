package com.cuac_xd.zenprofiles.gui;

import com.cuac_xd.zenprofiles.ZenProfiles;

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
import java.util.List;

/**
 * Auxiliary profile creation menu GUI.
 * Allows 1-click automatic profile creation with pre-configured fruit names.
 *
 * @author cuac_xd
 */
public class CreateProfileGUI {

    private final ZenProfiles plugin;

    public CreateProfileGUI(ZenProfiles plugin) {
        this.plugin = plugin;
    }

    /**
     * Opens the creation menu for a player.
     *
     * @param player The target player.
     */
    public void open(Player player) {
        FileConfiguration config = plugin.getMenuManager().getCreateProfileConfig();
        String titleText = config.getString("title", "<gradient:green:dark_green><bold>Create New Profile</bold></gradient>");
        int size = config.getInt("size", 27);

        ZenHolder holder = new ZenHolder("create_profile");
        Inventory gui = Bukkit.createInventory(holder, size, plugin.getMessageManager().parse(titleText));
        holder.setInventory(gui);

        // Fill decorative border elements
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

        // Create Button
        int createSlot = config.getInt("items.create.slot", 13);
        gui.setItem(createSlot, buildButtonItem(config, "items.create", "LIME_TERRACOTTA"));

        player.openInventory(gui);
    }

    private ItemStack buildButtonItem(FileConfiguration config, String path, String defaultMat) {
        Material mat = Material.matchMaterial(config.getString(path + ".material", defaultMat));
        if (mat == null) mat = Material.matchMaterial(defaultMat);
        if (mat == null) mat = Material.STONE;

        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        String name = config.getString(path + ".name", "<green>Create Profile");
        meta.displayName(plugin.getMessageManager().parse(name));

        List<String> rawLore = config.getStringList(path + ".lore");
        List<net.kyori.adventure.text.Component> formattedLore = new ArrayList<>();

        for (String line : rawLore) {
            formattedLore.add(plugin.getMessageManager().parse(line));
        }

        meta.lore(formattedLore);
        item.setItemMeta(meta);
        return item;
    }

    /**
     * Listener class managing creation GUI interactions with anti-dupe security.
     */
    public static class GUIListener implements Listener {

        private final ZenProfiles plugin;

        public GUIListener(ZenProfiles plugin) {
            this.plugin = plugin;
        }

        @EventHandler(priority = EventPriority.HIGH)
        public void onInventoryClick(InventoryClickEvent event) {
            if (!(event.getInventory().getHolder() instanceof ZenHolder holder)) return;
            if (!"create_profile".equals(holder.getMenuId())) return;

            event.setCancelled(true);

            if (!(event.getWhoClicked() instanceof Player player)) return;
            if (event.getClickedInventory() != event.getInventory()) return;

            FileConfiguration config = plugin.getMenuManager().getCreateProfileConfig();
            int createSlot = config.getInt("items.create.slot", 13);

            if (event.getSlot() == createSlot) {
                player.closeInventory();

                String autoFruitName = plugin.getProfileManager().generateNextFruitName(player.getUniqueId());
                plugin.getProfileManager().createProfile(player.getUniqueId(), autoFruitName).thenAccept(newProfile -> {
                    if (newProfile != null) {
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            player.sendMessage(plugin.getMessageManager().getMessage("profile-created", "%profile_name%", newProfile.getName()));
                            plugin.getProfileManager().initiateSwitch(player, newProfile);
                        });
                    }
                });
            }
        }

        @EventHandler(priority = EventPriority.HIGH)
        public void onInventoryDrag(InventoryDragEvent event) {
            if (event.getInventory().getHolder() instanceof ZenHolder holder && "create_profile".equals(holder.getMenuId())) {
                event.setCancelled(true);
            }
        }
    }
}
