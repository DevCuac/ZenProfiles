package com.cuac_xd.zenprofiles.manager;

import com.cuac_xd.zenprofiles.ZenProfiles;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

/**
 * Menu configuration loader manager.
 * Loads and manages YAML configurations for profile_selector.yml, confirm_delete.yml,
 * and create_profile.yml GUI layouts.
 *
 * @author cuac_xd
 */
public class MenuManager {

    private final ZenProfiles plugin;

    private FileConfiguration profileSelectorConfig;
    private FileConfiguration confirmDeleteConfig;
    private FileConfiguration createProfileConfig;

    public MenuManager(ZenProfiles plugin) {
        this.plugin = plugin;
        loadMenuConfigs();
    }

    /**
     * Loads all GUI menu configuration files from the menus/ subdirectory.
     */
    public void loadMenuConfigs() {
        File menusDir = new File(plugin.getDataFolder(), "menus");
        if (!menusDir.exists()) {
            menusDir.mkdirs();
        }

        profileSelectorConfig = loadOrSaveResource(menusDir, "profile_selector.yml");
        confirmDeleteConfig = loadOrSaveResource(menusDir, "confirm_delete.yml");
        createProfileConfig = loadOrSaveResource(menusDir, "create_profile.yml");
    }

    private FileConfiguration loadOrSaveResource(File dir, String filename) {
        File file = new File(dir, filename);
        if (!file.exists()) {
            plugin.saveResource("menus/" + filename, false);
        }
        return YamlConfiguration.loadConfiguration(file);
    }

    public FileConfiguration getProfileSelectorConfig() { return profileSelectorConfig; }
    public FileConfiguration getConfirmDeleteConfig() { return confirmDeleteConfig; }
    public FileConfiguration getCreateProfileConfig() { return createProfileConfig; }
}
