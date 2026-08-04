package com.cuac_xd.zenprofiles.gui;

import com.cuac_xd.zenprofiles.ZenProfiles;
import com.cuac_xd.zenprofiles.manager.MessageManager;
import com.cuac_xd.zenprofiles.model.Profile;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.SimpleDateFormat;
import java.util.*;

public class ProfileGUI implements Listener, InventoryHolder {

    private final ZenProfiles plugin;
    private final Player player;
    private Inventory inventory;

    private final Map<Integer, Profile> slotProfileMap = new HashMap<>();
    private final Set<Integer> emptySlotSet = new HashSet<>();
    private int closeButtonSlot = 31;

    public ProfileGUI(ZenProfiles plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
    }

    public void open() {
        YamlConfiguration config = plugin.getMenuManager().getMenuConfig("profile_selector");
        if (config == null) return;

        Component title = MessageManager.parse(config.getString("title", "<dark_gray>Profile Management</dark_gray>"));
        int size = config.getInt("size", 36);

        inventory = Bukkit.createInventory(this, size, title);

        // Fill background
        if (config.isConfigurationSection("fill-item")) {
            Material fillMat = Material.matchMaterial(config.getString("fill-item.material", "GRAY_STAINED_GLASS_PANE"));
            if (fillMat != null) {
                ItemStack filler = new ItemStack(fillMat);
                ItemMeta meta = filler.getItemMeta();
                if (meta != null) {
                    meta.displayName(MessageManager.parse(config.getString("fill-item.name", " ")));
                    filler.setItemMeta(meta);
                }
                for (int i = 0; i < size; i++) {
                    inventory.setItem(i, filler);
                }
            }
        }

        List<Integer> slots = config.getIntegerList("profile-slots");
        if (slots.isEmpty()) slots = Arrays.asList(11, 12, 13, 14, 15);

        List<Profile> profiles = plugin.getProfileManager().getPlayerProfiles(player.getUniqueId());
        Profile activeProfile = plugin.getProfileManager().getActiveProfile(player.getUniqueId());
        int maxProfiles = plugin.getProfileManager().getMaxProfiles(player);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");

        for (int i = 0; i < slots.size(); i++) {
            int slot = slots.get(i);

            if (i < profiles.size()) {
                // Profile exists
                Profile p = profiles.get(i);
                boolean isActive = activeProfile != null && activeProfile.getProfileId().equals(p.getProfileId());
                String sectionKey = isActive ? "items.active-profile" : "items.inactive-profile";

                Material mat = Material.matchMaterial(config.getString(sectionKey + ".material", isActive ? "EMERALD_BLOCK" : "GRASS_BLOCK"));
                if (mat == null) mat = isActive ? Material.EMERALD_BLOCK : Material.GRASS_BLOCK;

                ItemStack item = new ItemStack(mat);
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    String rawName = config.getString(sectionKey + ".name", "<yellow>Profile: <green>{profile_name}</green></yellow>");
                    meta.displayName(MessageManager.parse(rawName.replace("{profile_name}", p.getName())));

                    List<String> rawLore = config.getStringList(sectionKey + ".lore");
                    List<Component> lore = new ArrayList<>();
                    for (String line : rawLore) {
                        String replaced = line.replace("{profile_id}", p.getProfileId().toString())
                                .replace("{profile_name}", p.getName())
                                .replace("{creation_date}", sdf.format(new Date(p.getCreatedAt())))
                                .replace("{last_played}", sdf.format(new Date(p.getLastPlayed())))
                                .replace("{balance}", String.format("%,.2f", p.getData().getBalance()));
                        lore.add(MessageManager.parse(replaced));
                    }
                    meta.lore(lore);
                    item.setItemMeta(meta);
                }

                inventory.setItem(slot, item);
                slotProfileMap.put(slot, p);

            } else if (i < maxProfiles) {
                // Empty profile slot (Available for instant 1-click creation!)
                Material mat = Material.matchMaterial(config.getString("items.empty-slot.material", "DIRT"));
                if (mat == null) mat = Material.DIRT;

                ItemStack item = new ItemStack(mat);
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    meta.displayName(MessageManager.parse(config.getString("items.empty-slot.name", "<yellow>Empty Profile Slot</yellow>")));
                    List<String> rawLore = config.getStringList("items.empty-slot.lore");
                    List<Component> lore = new ArrayList<>();
                    for (String line : rawLore) {
                        lore.add(MessageManager.parse(line));
                    }
                    meta.lore(lore);
                    item.setItemMeta(meta);
                }

                inventory.setItem(slot, item);
                emptySlotSet.add(slot);

            } else {
                // Locked profile slot
                Material mat = Material.matchMaterial(config.getString("items.locked-slot.material", "BEDROCK"));
                if (mat == null) mat = Material.BEDROCK;

                ItemStack item = new ItemStack(mat);
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    meta.displayName(MessageManager.parse(config.getString("items.locked-slot.name", "<red>Locked profile slot</red>")));
                    List<String> rawLore = config.getStringList("items.locked-slot.lore");
                    List<Component> lore = new ArrayList<>();
                    for (String line : rawLore) {
                        lore.add(MessageManager.parse(line.replace("{slot_number}", String.valueOf(i + 1))));
                    }
                    meta.lore(lore);
                    item.setItemMeta(meta);
                }

                inventory.setItem(slot, item);
            }
        }

        // Close Button
        if (config.isConfigurationSection("close-button")) {
            closeButtonSlot = config.getInt("close-button.slot", 31);
            Material mat = Material.matchMaterial(config.getString("close-button.material", "BARRIER"));
            if (mat != null) {
                ItemStack btn = new ItemStack(mat);
                ItemMeta meta = btn.getItemMeta();
                if (meta != null) {
                    meta.displayName(MessageManager.parse(config.getString("close-button.name", "<red><bold>Close</bold></red>")));
                    List<Component> lore = new ArrayList<>();
                    for (String line : config.getStringList("close-button.lore")) {
                        lore.add(MessageManager.parse(line));
                    }
                    meta.lore(lore);
                    btn.setItemMeta(meta);
                }
                inventory.setItem(closeButtonSlot, btn);
            }
        }

        player.openInventory(inventory);
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public static class GUIListener implements Listener {

        private final ZenProfiles plugin;

        public GUIListener(ZenProfiles plugin) {
            this.plugin = plugin;
        }

        @EventHandler
        public void onInventoryClick(InventoryClickEvent event) {
            if (!(event.getInventory().getHolder() instanceof ProfileGUI gui)) return;
            event.setCancelled(true);

            Player player = (Player) event.getWhoClicked();
            int slot = event.getRawSlot();

            if (slot == gui.closeButtonSlot) {
                player.closeInventory();
                return;
            }

            // Clicked an available Empty Profile Slot -> Instant 1-click automatic creation!
            if (gui.emptySlotSet.contains(slot)) {
                player.closeInventory();
                String autoFruitName = plugin.getProfileManager().generateNextFruitName(player.getUniqueId());
                plugin.getProfileManager().createProfile(player, autoFruitName);
                return;
            }

            // Clicked an existing Profile
            Profile clickedProfile = gui.slotProfileMap.get(slot);
            if (clickedProfile != null) {
                if (event.getClick() == ClickType.SHIFT_LEFT || event.getClick() == ClickType.SHIFT_RIGHT) {
                    // Confirm Delete GUI
                    player.closeInventory();
                    new ConfirmDeleteGUI(plugin, player, clickedProfile).open();
                } else {
                    // Select Profile
                    player.closeInventory();
                    plugin.getProfileManager().initiateSwitch(player, clickedProfile);
                }
            }
        }
    }
}
