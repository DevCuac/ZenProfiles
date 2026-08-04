package com.cuac_xd.zenprofiles.command;

import com.cuac_xd.zenprofiles.ZenProfiles;
import com.cuac_xd.zenprofiles.gui.ConfirmDeleteGUI;
import com.cuac_xd.zenprofiles.gui.ProfileGUI;
import com.cuac_xd.zenprofiles.manager.MessageManager;
import com.cuac_xd.zenprofiles.model.Profile;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class ProfileCommand implements CommandExecutor, TabCompleter {

    private final ZenProfiles plugin;

    public ProfileCommand(ZenProfiles plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(plugin.getMessageManager().getComponent("player-only"));
                return true;
            }
            if (!player.hasPermission("zenprofiles.command.profile")) {
                player.sendMessage(plugin.getMessageManager().getComponent("no-permission"));
                return true;
            }
            new ProfileGUI(plugin, player).open();
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "setspawn":
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(plugin.getMessageManager().getComponent("player-only"));
                    return true;
                }
                if (!player.hasPermission("zenprofiles.admin")) {
                    player.sendMessage(plugin.getMessageManager().getComponent("no-permission"));
                    return true;
                }
                plugin.getProfileManager().setSpawnLocation(player.getLocation());
                player.sendMessage(plugin.getMessageManager().getComponent("setspawn-success"));
                return true;

            case "reload":
                if (!sender.hasPermission("zenprofiles.admin")) {
                    sender.sendMessage(plugin.getMessageManager().getComponent("no-permission"));
                    return true;
                }
                plugin.reloadConfig();
                plugin.getMessageManager().reload();
                plugin.getMenuManager().reload();
                sender.sendMessage(plugin.getMessageManager().getComponent("reload-success"));
                return true;

            case "create":
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(plugin.getMessageManager().getComponent("player-only"));
                    return true;
                }
                if (!player.hasPermission("zenprofiles.command.profile")) {
                    player.sendMessage(plugin.getMessageManager().getComponent("no-permission"));
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage(plugin.getMessageManager().getComponentWithoutPrefix("prefix")
                            .append(MessageManager.parse("<red>Usa: /profile create <nombre></red>")));
                    return true;
                }
                plugin.getProfileManager().createProfile(player, args[1]);
                return true;

            case "select":
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(plugin.getMessageManager().getComponent("player-only"));
                    return true;
                }
                if (!player.hasPermission("zenprofiles.command.profile")) {
                    player.sendMessage(plugin.getMessageManager().getComponent("no-permission"));
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage(plugin.getMessageManager().getComponentWithoutPrefix("prefix")
                            .append(MessageManager.parse("<red>Usa: /profile select <nombre|id></red>")));
                    return true;
                }
                Profile targetSelect = findProfile(player.getUniqueId(), args[1]);
                if (targetSelect == null) {
                    player.sendMessage(MessageManager.parse(plugin.getMessageManager().getRawMessage("profile.not-found")
                            .replace("{profile_name}", args[1])));
                    return true;
                }
                plugin.getProfileManager().initiateSwitch(player, targetSelect);
                return true;

            case "delete":
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(plugin.getMessageManager().getComponent("player-only"));
                    return true;
                }
                if (!player.hasPermission("zenprofiles.command.profile")) {
                    player.sendMessage(plugin.getMessageManager().getComponent("no-permission"));
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage(plugin.getMessageManager().getComponentWithoutPrefix("prefix")
                            .append(MessageManager.parse("<red>Usa: /profile delete <nombre|id></red>")));
                    return true;
                }
                Profile targetDelete = findProfile(player.getUniqueId(), args[1]);
                if (targetDelete == null) {
                    player.sendMessage(MessageManager.parse(plugin.getMessageManager().getRawMessage("profile.not-found")
                            .replace("{profile_name}", args[1])));
                    return true;
                }
                new ConfirmDeleteGUI(plugin, player, targetDelete).open();
                return true;

            case "list":
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(plugin.getMessageManager().getComponent("player-only"));
                    return true;
                }
                if (!player.hasPermission("zenprofiles.command.profile")) {
                    player.sendMessage(plugin.getMessageManager().getComponent("no-permission"));
                    return true;
                }
                List<Profile> profiles = plugin.getProfileManager().getPlayerProfiles(player.getUniqueId());
                Profile active = plugin.getProfileManager().getActiveProfile(player.getUniqueId());

                player.sendMessage(MessageManager.parse("<dark_gray>========= <aqua>Tus Perfiles (<yellow>" + profiles.size() + "</yellow>/<yellow>" + plugin.getProfileManager().getMaxProfiles(player) + "</yellow>)</aqua> =========</dark_gray>"));
                for (Profile p : profiles) {
                    boolean isActive = active != null && active.getProfileId().equals(p.getProfileId());
                    String status = isActive ? " <green>[ACTIVO]</green>" : "";
                    player.sendMessage(MessageManager.parse("<gray>- <yellow>" + p.getName() + "</yellow> <dark_gray>(ID: " + p.getProfileId() + ")</dark_gray>" + status + "</gray>"));
                }
                return true;

            default:
                sender.sendMessage(plugin.getMessageManager().getComponentWithoutPrefix("prefix")
                        .append(MessageManager.parse("<yellow>Usa: /profile [create|select|delete|list|reload]</yellow>")));
                return true;
        }
    }

    private Profile findProfile(UUID playerUuid, String query) {
        List<Profile> profiles = plugin.getProfileManager().getPlayerProfiles(playerUuid);
        for (Profile p : profiles) {
            if (p.getName().equalsIgnoreCase(query) || p.getProfileId().toString().equalsIgnoreCase(query)) {
                return p;
            }
        }
        return null;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> subs = new ArrayList<>(Arrays.asList("create", "select", "delete", "list"));
            if (sender.hasPermission("zenprofiles.admin")) {
                subs.add("reload");
                subs.add("setspawn");
            }
            return subs.stream().filter(s -> s.startsWith(args[0].toLowerCase())).collect(Collectors.toList());
        }

        if (args.length == 2 && sender instanceof Player player) {
            if (args[0].equalsIgnoreCase("select") || args[0].equalsIgnoreCase("delete")) {
                return plugin.getProfileManager().getPlayerProfiles(player.getUniqueId())
                        .stream().map(Profile::getName)
                        .filter(n -> n.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }

        return Collections.emptyList();
    }
}
