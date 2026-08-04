package com.cuac_xd.zenprofiles;

import com.cuac_xd.zenprofiles.api.ZenProfilesAPI;
import com.cuac_xd.zenprofiles.command.ProfileCommand;
import com.cuac_xd.zenprofiles.gui.ConfirmDeleteGUI;
import com.cuac_xd.zenprofiles.gui.CreateProfileGUI;
import com.cuac_xd.zenprofiles.gui.ProfileGUI;
import com.cuac_xd.zenprofiles.hook.LuckPermsHook;
import com.cuac_xd.zenprofiles.hook.PlaceholderAPIHook;
import com.cuac_xd.zenprofiles.hook.VaultHook;
import com.cuac_xd.zenprofiles.hook.ZenSuiteHook;
import com.cuac_xd.zenprofiles.listener.PlayerListener;
import com.cuac_xd.zenprofiles.manager.MenuManager;
import com.cuac_xd.zenprofiles.manager.MessageManager;
import com.cuac_xd.zenprofiles.manager.ProfileManager;
import com.cuac_xd.zenprofiles.storage.ProfileStorage;
import com.cuac_xd.zenprofiles.storage.YamlStorage;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main plugin class for ZenProfiles.
 * Handles lifecycle initialization, storage engine bootstrap, event listener registration,
 * command registration, and third-party integration hooks.
 *
 * @author cuac_xd
 */
public class ZenProfiles extends JavaPlugin {

    private static ZenProfiles instance;

    private ProfileStorage storage;
    private MessageManager messageManager;
    private MenuManager menuManager;
    private ProfileManager profileManager;

    private LuckPermsHook luckPermsHook;
    private PlaceholderAPIHook placeholderAPIHook;
    private VaultHook vaultHook;
    private ZenSuiteHook zenSuiteHook;

    @Override
    public void onEnable() {
        instance = this;

        // Load configuration files
        saveDefaultConfig();

        // Initialize core utility managers
        messageManager = new MessageManager(this);
        menuManager = new MenuManager(this);

        // Initialize storage layer
        storage = new YamlStorage(this);
        storage.init().join();

        // Initialize core profile manager
        profileManager = new ProfileManager(this);

        // Initialize developer API singleton
        ZenProfilesAPI.init(this);

        // Setup third-party integration hooks
        luckPermsHook = new LuckPermsHook(this);
        luckPermsHook.setup();

        placeholderAPIHook = new PlaceholderAPIHook(this);
        placeholderAPIHook.registerExpansion();

        vaultHook = new VaultHook(this);
        vaultHook.setup();

        zenSuiteHook = new ZenSuiteHook(this);
        zenSuiteHook.setup();

        // Register event listeners
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new ProfileGUI.GUIListener(this), this);
        getServer().getPluginManager().registerEvents(new ConfirmDeleteGUI.GUIListener(this), this);
        getServer().getPluginManager().registerEvents(new CreateProfileGUI.GUIListener(this), this);

        // Register main commands
        ProfileCommand profileCmd = new ProfileCommand(this);
        if (getCommand("profiles") != null) {
            getCommand("profiles").setExecutor(profileCmd);
            getCommand("profiles").setTabCompleter(profileCmd);
        }

        // Handle online players in case of plugin reload
        for (Player online : Bukkit.getOnlinePlayers()) {
            profileManager.loadPlayerProfiles(online);
        }

        getLogger().info("ZenProfiles v" + getDescription().getVersion() + " initialized successfully!");
    }

    @Override
    public void onDisable() {
        // Save and unload all active player profiles
        if (profileManager != null) {
            for (Player online : Bukkit.getOnlinePlayers()) {
                profileManager.saveAndUnloadPlayer(online).join();
            }
        }

        // Close storage engine
        if (storage != null) {
            storage.close().join();
        }

        getLogger().info("ZenProfiles disabled successfully!");
    }

    public static ZenProfiles getInstance() { return instance; }
    public ProfileStorage getStorage() { return storage; }
    public MessageManager getMessageManager() { return messageManager; }
    public MenuManager getMenuManager() { return menuManager; }
    public ProfileManager getProfileManager() { return profileManager; }
    public LuckPermsHook getLuckPermsHook() { return luckPermsHook; }
    public VaultHook getVaultHook() { return vaultHook; }
    public ZenSuiteHook getZenSuiteHook() { return zenSuiteHook; }
}
