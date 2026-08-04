package com.cuac_xd.zenprofiles.manager;

import com.cuac_xd.zenprofiles.ZenProfiles;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MessageManager {

    private final ZenProfiles plugin;
    private YamlConfiguration messagesConfig;
    private File messagesFile;

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.legacyAmpersand();

    public MessageManager(ZenProfiles plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
    }

    public String getRawMessage(String path) {
        return messagesConfig.getString(path, "<red>Missing message: " + path + "</red>");
    }

    public Component getComponent(String path) {
        String prefix = messagesConfig.getString("prefix", "<dark_gray>[<cyan>ZenProfiles</cyan>]</dark_gray> ");
        String msg = messagesConfig.getString(path, "<red>Missing message: " + path + "</red>");
        return parse(prefix + msg);
    }

    public Component getComponentWithoutPrefix(String path) {
        String msg = messagesConfig.getString(path, "<red>Missing message: " + path + "</red>");
        return parse(msg);
    }

    public String getMessage(String path) {
        return LEGACY_SERIALIZER.serialize(getComponent(path));
    }

    public String getMessageWithoutPrefix(String path) {
        return LEGACY_SERIALIZER.serialize(getComponentWithoutPrefix(path));
    }

    public List<String> getStringList(String path) {
        return messagesConfig.getStringList(path);
    }

    public List<Component> getComponentList(String path) {
        List<String> list = messagesConfig.getStringList(path);
        List<Component> colorized = new ArrayList<>();
        for (String line : list) {
            colorized.add(parse(line));
        }
        return colorized;
    }

    /**
     * Parses a string supporting both MiniMessage (<red>, <gradient:...>, etc.) and Legacy ampersand (&a, &b) formatting.
     * Automatically disables Minecraft's default item italic formatting.
     */
    public static Component parse(String text) {
        if (text == null || text.isEmpty()) return Component.empty();

        Component comp;
        if (text.contains("&")) {
            Component legacyComp = LEGACY_SERIALIZER.deserialize(text);
            String convertedToMini = MINI_MESSAGE.serialize(legacyComp);
            comp = MINI_MESSAGE.deserialize(convertedToMini);
        } else {
            comp = MINI_MESSAGE.deserialize(text);
        }

        return comp.decoration(TextDecoration.ITALIC, false);
    }

    public static String colorize(String text) {
        return LEGACY_SERIALIZER.serialize(parse(text));
    }
}
