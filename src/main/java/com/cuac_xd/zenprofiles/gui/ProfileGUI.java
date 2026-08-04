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
import java.util.List;
import java.util.UUID;

/**
 * Hypixel Skyblock style profile management GUI.
 * Renders emerald blocks for active profile, grass/dirt blocks for inactive profiles,
 * dirt blocks for empty available slots (1-click creation), and bedrock for locked slots.
 *
 * @author cuac_xd
 */
public class ProfileGUI {

    private final ZenProfiles plugin;

    public ProfileGUI(ZenProfiles plugin) {
        this.plugin = plugin;
    }

    /**
     * Opens the profile selector GUI for the target player.
     *
     * @param player The player opening the GUI.
     */
    public void open(Player player) {
        FileConfiguration config = plugin.getMenuManager().getProfileSelectorConfig();
        String titleText = config.getString("title", "<gradient:green:dark_green><bold>Profile Management</bold></gradient>");
        int size = config.getInt("size", 54);

        ZenHolder holder = new ZenHolder("profile_selector");
        Inventory gui = Bukkit.createInventory(holder, size, plugin.getMessageManager().parse(titleText));
        holder.setInventory(gui);

        List<Profile> playerProfiles = plugin.getProfileManager().getPlayerProfiles(player.getUniqueId());
        Profile activeProfile = plugin.getProfileManager().getActiveProfile(player.getUniqueId());
        int maxProfiles = plugin.getProfileManager().getMaxProfiles(player);

        List<Integer> profileSlots = config.getIntegerList("profile-slots");
        if (profileSlots.isEmpty()) {
            profileSlots = List.of(10, 11, 12, 13, 14);
        }

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
                if (!profileSlots.contains(i)) {
                    gui.setItem(i, fillerItem);
                }
            }
        }

        // Populate profile slots
        for (int index = 0; index < profileSlots.size(); index++) {
            int slot = profileSlots.get(index);
            int slotIndex = index + 1;

            if (slotIndex <= playerProfiles.size()) {
                // Existing Profile Slot
                Profile profile = playerProfiles.get(index);
                boolean isActive = activeProfile != null && activeProfile.getProfileId().equals(profile.getProfileId());

                String nodePath = isActive ? "items.active-profile" : "items.inactive-profile";
                ItemStack item = buildProfileItem(config, nodePath, profile, isActive, slotIndex);
                gui.setItem(slot, item);

            } else if (slotIndex <= maxProfiles) {
                // Available Empty Profile Slot (1-Click Creation)
                ItemStack item = buildEmptySlotItem(config, "items.empty-slot", slotIndex);
                gui.setItem(slot, item);

            } else {
                // Locked Profile Slot (Requires Higher Permission)
                ItemStack item = buildLockedSlotItem(config, "items.locked-slot", slotIndex);
                gui.setItem(slot, item);
            }
        }

        player.openInventory(gui);
    }

    private ItemStack buildProfileItem(FileConfiguration config, String path, Profile profile, boolean isActive, int slotIndex) {
        String defaultMatName = isActive ? "EMERALD_BLOCK" : "GRASS_BLOCK";
        Material mat = Material.matchMaterial(config.getString(path + ".material", defaultMatName));
        if (mat == null) mat = Material.GRASS_BLOCK;

        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        String name = config.getString(path + ".name", "<green>Profile: " + profile.getName());
        name = name.replace("%profile_name%", profile.getName())
                   .replace("%slot%", String.valueOf(slotIndex));
        meta.displayName(plugin.getMessageManager().parse(name));

        List<String> rawLore = config.getStringList(path + ".lore");
        List<net.kyori.adventure.text.Component> formattedLore = new ArrayList<>();

        for (String line : rawLore) {
            line = line.replace("%profile_name%", profile.getName())
                       .replace("%profile_id%", profile.getProfileId().toString())
                       .replace("%created_at%", profile.getCreatedAtFormatted())
                       .replace("%last_played%", profile.getLastPlayedFormatted())
                       .replace("%coins%", plugin.getMessageManager().formatCoins(profile.getData().getBalance()))
                       .replace("%slot%", String.valueOf(slotIndex));
            formattedLore.add(plugin.getMessageManager().parse(line));
        }

        meta.lore(formattedLore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack buildEmptySlotItem(FileConfiguration config, String path, int slotIndex) {
        Material mat = Material.matchMaterial(config.getString(path + ".material", "DIRT"));
        if (mat == null) mat = Material.DIRT;

        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        String name = config.getString(path + ".name", "<gray>Empty Slot");
        name = name.replace("%slot%", String.valueOf(slotIndex));
        meta.displayName(plugin.getMessageManager().parse(name));

        List<String> rawLore = config.getStringList(path + ".lore");
        List<net.kyori.adventure.text.Component> formattedLore = new ArrayList<>();

        for (String line : rawLore) {
            line = line.replace("%slot%", String.valueOf(slotIndex));
            formattedLore.add(plugin.getMessageManager().parse(line));
        }

        meta.lore(formattedLore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack buildLockedSlotItem(FileConfiguration config, String path, int slotIndex) {
        Material mat = Material.matchMaterial(config.getString(path + ".material", "BEDROCK"));
        if (mat == null) mat = Material.BEDROCK;

        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        String name = config.getString(path + ".name", "<red>Locked Slot");
        name = name.replace("%slot%", String.valueOf(slotIndex));
        meta.displayName(plugin.getMessageManager().parse(name));

        List<String> rawLore = config.getStringList(path + ".lore");
        List<net.kyori.adventure.text.Component> formattedLore = new ArrayList<>();

        for (String line : rawLore) {
            line = line.replace("%slot%", String.valueOf(slotIndex));
            formattedLore.add(plugin.getMessageManager().parse(line));
        }

        meta.lore(formattedLore);
        item.setItemMeta(meta);
        return item;
    }

    /**
     * Listener class managing profile GUI interactions with anti-dupe security checks.
     */
    public static class GUIListener implements Listener {

        private final ZenProfiles plugin;

        public GUIListener(ZenProfiles plugin) {
            this.plugin = plugin;
        }

        @EventHandler(priority = EventPriority.HIGH)
        public void onInventoryClick(InventoryClickEvent event) {
            if (!(event.getInventory().getHolder() instanceof ZenHolder holder)) return;
            if (!"profile_selector".equals(holder.getMenuId())) return;

            // Enforce strict cancellation to prevent item duplication or stealing
            event.setCancelled(true);

            if (!(event.getWhoClicked() instanceof Player player)) return;
            if (event.getClickedInventory() != event.getInventory()) return;

            int clickedSlot = event.getSlot();
            FileConfiguration config = plugin.getMenuManager().getProfileSelectorConfig();
            List<Integer> profileSlots = config.getIntegerList("profile-slots");
            if (profileSlots.isEmpty()) profileSlots = List.of(10, 11, 12, 13, 14);

            if (!profileSlots.contains(clickedSlot)) return;

            int slotIndex = profileSlots.indexOf(clickedSlot);
            List<Profile> playerProfiles = plugin.getProfileManager().getPlayerProfiles(player.getUniqueId());
            Profile activeProfile = plugin.getProfileManager().getActiveProfile(player.getUniqueId());
            int maxProfiles = plugin.getProfileManager().getMaxProfiles(player);

            if (slotIndex < playerProfiles.size()) {
                // Clicked an existing profile slot
                Profile targetProfile = playerProfiles.get(slotIndex);

                if (event.isShiftClick()) {
                    // Shift + Right Click to initiate deletion process
                    if (activeProfile != null && activeProfile.getProfileId().equals(targetProfile.getProfileId())) {
                        player.sendMessage(plugin.getMessageManager().getMessage("cannot-delete-active"));
                        player.closeInventory();
                        return;
                    }
                    player.closeInventory();
                    new ConfirmDeleteGUI(plugin).open(player, targetProfile);
                    return;
                }

                // Regular click to select profile
                if (activeProfile != null && activeProfile.getProfileId().equals(targetProfile.getProfileId())) {
                    player.sendMessage(plugin.getMessageManager().getMessage("already-active"));
                    return;
                }

                player.closeInventory();
                plugin.getProfileManager().initiateSwitch(player, targetProfile);

            } else if (slotIndex < maxProfiles) {
                // Clicked available empty profile slot -> Hypixel Skyblock 1-Click Auto Creation
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

            } else {
                // Clicked locked slot
                player.sendMessage(plugin.getMessageManager().getMessage("max-profiles-reached"));
            }
        }

        @EventHandler(priority = EventPriority.HIGH)
        public void onInventoryDrag(InventoryDragEvent event) {
            if (event.getInventory().getHolder() instanceof ZenHolder holder && "profile_selector".equals(holder.getMenuId())) {
                event.setCancelled(true);
            }
        }
    }
}
