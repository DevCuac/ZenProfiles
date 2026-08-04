package com.cuac_xd.zenprofiles.manager;

import com.cuac_xd.zenprofiles.ZenProfiles;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;

/**
 * Message and Component formatting manager.
 * Parses Kyori Adventure MiniMessage tags and legacy Bukkit ampersand color codes.
 * Enforces removal of default italic decorations on item display components.
 *
 * @author cuac_xd
 */
public class MessageManager {

    private final ZenProfiles plugin;
    private final MiniMessage miniMessage;
    private final LegacyComponentSerializer legacySerializer;
    private FileConfiguration messagesConfig;
    private final DecimalFormat coinsFormat = new DecimalFormat("#,##0.00");

    public MessageManager(ZenProfiles plugin) {
        this.plugin = plugin;
        this.miniMessage = MiniMessage.miniMessage();
        this.legacySerializer = LegacyComponentSerializer.legacyAmpersand();
        loadMessages();
    }

    /**
     * Loads messages.yml configuration.
     */
    public void loadMessages() {
        File file = new File(plugin.getDataFolder(), "messages.yml");
        if (!file.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        messagesConfig = YamlConfiguration.loadConfiguration(file);

        // Merge defaults if updated
        InputStream defaultStream = plugin.getResource("messages.yml");
        if (defaultStream != null) {
            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultStream, StandardCharsets.UTF_8));
            messagesConfig.setDefaults(defaultConfig);
        }
    }

    /**
     * Parses MiniMessage and legacy color text into a Kyori Component with italic decoration disabled by default.
     *
     * @param text The input text string.
     * @return Formatted Component.
     */
    public Component parse(String text) {
        if (text == null || text.isEmpty()) return Component.empty();

        Component parsed;
        if (text.contains("&")) {
            parsed = legacySerializer.deserialize(text);
        } else {
            parsed = miniMessage.deserialize(text);
        }
        return parsed.decoration(TextDecoration.ITALIC, false);
    }

    /**
     * Retrieves a formatted Component message by key with placeholder replacements.
     *
     * @param key Message key in messages.yml.
     * @param replacements Key-value placeholder pairs.
     * @return Formatted Component message.
     */
    public Component getMessage(String key, String... replacements) {
        String raw = messagesConfig.getString("messages." + key, "<red>Missing message: " + key + "</red>");
        String prefix = messagesConfig.getString("prefix", "<gradient:green:dark_green><bold>[ZenProfiles]</bold></gradient> ");

        raw = raw.replace("%prefix%", prefix);
        for (int i = 0; i < replacements.length; i += 2) {
            if (i + 1 < replacements.length) {
                raw = raw.replace(replacements[i], replacements[i + 1]);
            }
        }
        return parse(raw);
    }

    /**
     * Formats a double balance into a readable currency string.
     *
     * @param amount Balance amount.
     * @return Formatted balance string.
     */
    public String formatCoins(double amount) {
        return coinsFormat.format(amount);
    }
}
