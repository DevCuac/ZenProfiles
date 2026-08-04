package com.cuac_xd.zenprofiles.manager;

import com.cuac_xd.zenprofiles.ZenProfiles;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class MenuManager {

    private final ZenProfiles plugin;
    private final Map<String, YamlConfiguration> menuConfigs = new HashMap<>();

    public MenuManager(ZenProfiles plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        menuConfigs.clear();
        File folder = new File(plugin.getDataFolder(), "menus");
        if (!folder.exists()) {
            folder.mkdirs();
        }

        saveDefaultMenu("profile_selector.yml");
        saveDefaultMenu("confirm_delete.yml");

        File[] files = folder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files != null) {
            for (File file : files) {
                String menuName = file.getName().replace(".yml", "");
                menuConfigs.put(menuName, YamlConfiguration.loadConfiguration(file));
            }
        }
    }

    private void saveDefaultMenu(String fileName) {
        File target = new File(new File(plugin.getDataFolder(), "menus"), fileName);
        if (!target.exists()) {
            plugin.saveResource("menus/" + fileName, false);
        }
    }

    public YamlConfiguration getMenuConfig(String name) {
        return menuConfigs.get(name);
    }
}
