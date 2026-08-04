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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class CreateProfileGUI implements InventoryHolder {

    private final ZenProfiles plugin;
    private final Player player;
    private Inventory inventory;

    private final Map<Integer, String> slotFruitMap = new HashMap<>();
    private int customNameSlot = 31;
    private int backSlot = 35;

    public CreateProfileGUI(ZenProfiles plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
    }

    public void open() {
        YamlConfiguration config = plugin.getMenuManager().getMenuConfig("create_profile");
        if (config == null) return;

        Component title = MessageManager.parse(config.getString("title", "<dark_gray>Crear Nuevo Perfil</dark_gray>"));
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

        // Available Hypixel Skyblock Fruit Options
        List<String> fruitNames = plugin.getConfig().getStringList("skyblock-fruit-names");
        if (fruitNames.isEmpty()) {
            fruitNames = Arrays.asList("Cucumber", "Fruit", "Kiwi", "Peach", "Mango", "Blueberry", "Zucchini", "Papaya");
        }

        List<Profile> existingProfiles = plugin.getProfileManager().getPlayerProfiles(player.getUniqueId());
        List<String> availableFruits = new ArrayList<>();
        for (String fruit : fruitNames) {
            boolean taken = false;
            for (Profile p : existingProfiles) {
                if (p.getName().equalsIgnoreCase(fruit)) {
                    taken = true;
                    break;
                }
            }
            if (!taken) {
                availableFruits.add(fruit);
            }
        }

        List<Integer> slots = config.getIntegerList("preset-slots");
        if (slots.isEmpty()) slots = Arrays.asList(10, 11, 12, 13, 14, 15, 16);

        for (int i = 0; i < Math.min(slots.size(), availableFruits.size()); i++) {
            int slot = slots.get(i);
            String fruitName = availableFruits.get(i);

            ItemStack apple = new ItemStack(Material.APPLE);
            ItemMeta meta = apple.getItemMeta();
            if (meta != null) {
                meta.displayName(MessageManager.parse("<gradient:yellow:gold>Perfil " + fruitName + "</gradient>"));
                List<Component> lore = new ArrayList<>();
                lore.add(MessageManager.parse("<gray>Haz clic para crear instantáneamente</gray>"));
                lore.add(MessageManager.parse("<gray>el perfil <yellow>" + fruitName + "</yellow>.</gray>"));
                meta.lore(lore);
                apple.setItemMeta(meta);
            }
            inventory.setItem(slot, apple);
            slotFruitMap.put(slot, fruitName);
        }

        // Custom Name Button
        if (config.isConfigurationSection("custom-name-button")) {
            customNameSlot = config.getInt("custom-name-button.slot", 31);
            Material mat = Material.matchMaterial(config.getString("custom-name-button.material", "PAPER"));
            if (mat != null) {
                ItemStack btn = new ItemStack(mat);
                ItemMeta meta = btn.getItemMeta();
                if (meta != null) {
                    meta.displayName(MessageManager.parse(config.getString("custom-name-button.name", "<yellow><bold>Nombre Personalizado</bold></yellow>")));
                    List<Component> lore = new ArrayList<>();
                    for (String line : config.getStringList("custom-name-button.lore")) {
                        lore.add(MessageManager.parse(line));
                    }
                    meta.lore(lore);
                    btn.setItemMeta(meta);
                }
                inventory.setItem(customNameSlot, btn);
            }
        }

        // Back Button
        if (config.isConfigurationSection("back-button")) {
            backSlot = config.getInt("back-button.slot", 35);
            Material mat = Material.matchMaterial(config.getString("back-button.material", "ARROW"));
            if (mat != null) {
                ItemStack btn = new ItemStack(mat);
                ItemMeta meta = btn.getItemMeta();
                if (meta != null) {
                    meta.displayName(MessageManager.parse(config.getString("back-button.name", "<red><bold>Volver</bold></red>")));
                    List<Component> lore = new ArrayList<>();
                    for (String line : config.getStringList("back-button.lore")) {
                        lore.add(MessageManager.parse(line));
                    }
                    meta.lore(lore);
                    btn.setItemMeta(meta);
                }
                inventory.setItem(backSlot, btn);
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
            if (!(event.getInventory().getHolder() instanceof CreateProfileGUI gui)) return;
            event.setCancelled(true);

            Player player = (Player) event.getWhoClicked();
            int slot = event.getRawSlot();

            if (slot == gui.backSlot) {
                player.closeInventory();
                new ProfileGUI(plugin, player).open();
                return;
            }

            if (slot == gui.customNameSlot) {
                player.closeInventory();
                plugin.getProfileManager().getPendingChatInput().add(player.getUniqueId());
                player.sendMessage(plugin.getMessageManager().getComponent("profile.type-name-prompt"));
                return;
            }

            String fruitName = gui.slotFruitMap.get(slot);
            if (fruitName != null) {
                player.closeInventory();
                plugin.getProfileManager().createProfile(player, fruitName);
            }
        }
    }
}
