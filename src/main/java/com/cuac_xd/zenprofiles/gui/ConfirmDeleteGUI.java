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
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ConfirmDeleteGUI implements InventoryHolder {

    private final ZenProfiles plugin;
    private final Player player;
    private final Profile profile;
    private Inventory inventory;

    private int confirmSlot = 11;
    private int cancelSlot = 15;

    public ConfirmDeleteGUI(ZenProfiles plugin, Player player, Profile profile) {
        this.plugin = plugin;
        this.player = player;
        this.profile = profile;
    }

    public void open() {
        YamlConfiguration config = plugin.getMenuManager().getMenuConfig("confirm_delete");
        if (config == null) return;

        String rawTitle = config.getString("title", "<dark_red>¿Eliminar {profile_name}?</dark_red>")
                .replace("{profile_name}", profile.getName());
        Component title = MessageManager.parse(rawTitle);
        int size = config.getInt("size", 27);

        inventory = Bukkit.createInventory(this, size, title);

        // Background
        if (config.isConfigurationSection("fill-item")) {
            Material fillMat = Material.matchMaterial(config.getString("fill-item.material", "RED_STAINED_GLASS_PANE"));
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

        // Confirm Button
        if (config.isConfigurationSection("confirm-button")) {
            confirmSlot = config.getInt("confirm-button.slot", 11);
            Material mat = Material.matchMaterial(config.getString("confirm-button.material", "LIME_TERRACOTTA"));
            if (mat != null) {
                ItemStack btn = new ItemStack(mat);
                ItemMeta meta = btn.getItemMeta();
                if (meta != null) {
                    meta.displayName(MessageManager.parse(config.getString("confirm-button.name", "<green><bold>¡CONFIRMAR ELIMINACIÓN!</bold></green>")));
                    List<Component> lore = new ArrayList<>();
                    for (String line : config.getStringList("confirm-button.lore")) {
                        lore.add(MessageManager.parse(line.replace("{profile_name}", profile.getName())));
                    }
                    meta.lore(lore);
                    btn.setItemMeta(meta);
                }
                inventory.setItem(confirmSlot, btn);
            }
        }

        // Cancel Button
        if (config.isConfigurationSection("cancel-button")) {
            cancelSlot = config.getInt("cancel-button.slot", 15);
            Material mat = Material.matchMaterial(config.getString("cancel-button.material", "RED_TERRACOTTA"));
            if (mat != null) {
                ItemStack btn = new ItemStack(mat);
                ItemMeta meta = btn.getItemMeta();
                if (meta != null) {
                    meta.displayName(MessageManager.parse(config.getString("cancel-button.name", "<red><bold>CANCELAR</bold></red>")));
                    List<Component> lore = new ArrayList<>();
                    for (String line : config.getStringList("cancel-button.lore")) {
                        lore.add(MessageManager.parse(line.replace("{profile_name}", profile.getName())));
                    }
                    meta.lore(lore);
                    btn.setItemMeta(meta);
                }
                inventory.setItem(cancelSlot, btn);
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
            if (!(event.getInventory().getHolder() instanceof ConfirmDeleteGUI gui)) return;
            event.setCancelled(true);

            Player player = (Player) event.getWhoClicked();
            int slot = event.getRawSlot();

            if (slot == gui.confirmSlot) {
                player.closeInventory();
                plugin.getProfileManager().deleteProfile(player, gui.profile);
            } else if (slot == gui.cancelSlot) {
                player.closeInventory();
                new ProfileGUI(plugin, player).open();
            }
        }
    }
}
