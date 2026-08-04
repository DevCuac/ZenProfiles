package com.cuac_xd.zenprofiles.command;

import com.cuac_xd.zenprofiles.ZenProfiles;
import com.cuac_xd.zenprofiles.gui.ConfirmDeleteGUI;
import com.cuac_xd.zenprofiles.gui.ProfileGUI;
import com.cuac_xd.zenprofiles.model.Profile;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Primary command executor and tab completer for /profile commands.
 * Handles /profile, /profile select, /profile create, /profile delete,
 * /profile list, /profile setspawn, and /profile reload.
 *
 * @author cuac_xd
 */
public class ProfileCommand implements CommandExecutor, TabCompleter {

    private final ZenProfiles plugin;

    public ProfileCommand(ZenProfiles plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be executed by in-game players.");
            return true;
        }

        if (!player.hasPermission("zenprofiles.command.profile")) {
            player.sendMessage(plugin.getMessageManager().getMessage("no-permission"));
            return true;
        }

        if (args.length == 0) {
            new ProfileGUI(plugin).open(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();
        switch (subCommand) {
            case "list" -> handleList(player);
            case "create" -> handleCreate(player, args);
            case "select" -> handleSelect(player, args);
            case "delete" -> handleDelete(player, args);
            case "setspawn" -> handleSetSpawn(player);
            case "reload" -> handleReload(player);
            default -> new ProfileGUI(plugin).open(player);
        }

        return true;
    }

    private void handleList(Player player) {
        List<Profile> profiles = plugin.getProfileManager().getPlayerProfiles(player.getUniqueId());
        Profile active = plugin.getProfileManager().getActiveProfile(player.getUniqueId());

        player.sendMessage(plugin.getMessageManager().parse("<gradient:green:dark_green><bold>--- Your Profiles ---</bold></gradient>"));
        for (Profile p : profiles) {
            boolean isActive = active != null && active.getProfileId().equals(p.getProfileId());
            String status = isActive ? "<green>[ACTIVE]</green>" : "<gray>[INACTIVE]</gray>";
            player.sendMessage(plugin.getMessageManager().parse("<dark_gray>- </dark_gray><yellow>" + p.getName() + "</yellow> " + status));
        }
    }

    private void handleCreate(Player player, String[] args) {
        int max = plugin.getProfileManager().getMaxProfiles(player);
        List<Profile> existing = plugin.getProfileManager().getPlayerProfiles(player.getUniqueId());

        if (existing.size() >= max) {
            player.sendMessage(plugin.getMessageManager().getMessage("max-profiles-reached"));
            return;
        }

        String name = args.length > 1 ? args[1] : plugin.getProfileManager().generateNextFruitName(player.getUniqueId());
        plugin.getProfileManager().createProfile(player.getUniqueId(), name).thenAccept(newProfile -> {
            if (newProfile != null) {
                org.bukkit.Bukkit.getScheduler().runTask(plugin, () -> {
                    player.sendMessage(plugin.getMessageManager().getMessage("profile-created", "%profile_name%", newProfile.getName()));
                    plugin.getProfileManager().initiateSwitch(player, newProfile);
                });
            }
        });
    }

    private void handleSelect(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(plugin.getMessageManager().parse("<red>Usage: /profile select <name/id></red>"));
            return;
        }

        String targetName = args[1];
        List<Profile> profiles = plugin.getProfileManager().getPlayerProfiles(player.getUniqueId());
        Profile target = profiles.stream()
                .filter(p -> p.getName().equalsIgnoreCase(targetName) || p.getProfileId().toString().equalsIgnoreCase(targetName))
                .findFirst()
                .orElse(null);

        if (target == null) {
            player.sendMessage(plugin.getMessageManager().parse("<red>Profile not found: " + targetName + "</red>"));
            return;
        }

        plugin.getProfileManager().initiateSwitch(player, target);
    }

    private void handleDelete(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(plugin.getMessageManager().parse("<red>Usage: /profile delete <name/id></red>"));
            return;
        }

        String targetName = args[1];
        List<Profile> profiles = plugin.getProfileManager().getPlayerProfiles(player.getUniqueId());
        Profile target = profiles.stream()
                .filter(p -> p.getName().equalsIgnoreCase(targetName) || p.getProfileId().toString().equalsIgnoreCase(targetName))
                .findFirst()
                .orElse(null);

        if (target == null) {
            player.sendMessage(plugin.getMessageManager().parse("<red>Profile not found: " + targetName + "</red>"));
            return;
        }

        Profile active = plugin.getProfileManager().getActiveProfile(player.getUniqueId());
        if (active != null && active.getProfileId().equals(target.getProfileId())) {
            player.sendMessage(plugin.getMessageManager().getMessage("cannot-delete-active"));
            return;
        }

        new ConfirmDeleteGUI(plugin).open(player, target);
    }

    private void handleSetSpawn(Player player) {
        if (!player.hasPermission("zenprofiles.admin")) {
            player.sendMessage(plugin.getMessageManager().getMessage("no-permission"));
            return;
        }

        Location loc = player.getLocation();
        FileConfiguration config = plugin.getConfig();

        config.set("spawn.use-custom-spawn", true);
        config.set("spawn.custom-spawn.world", loc.getWorld().getName());
        config.set("spawn.custom-spawn.x", loc.getX());
        config.set("spawn.custom-spawn.y", loc.getY());
        config.set("spawn.custom-spawn.z", loc.getZ());
        config.set("spawn.custom-spawn.yaw", loc.getYaw());
        config.set("spawn.custom-spawn.pitch", loc.getPitch());

        plugin.saveConfig();
        player.sendMessage(plugin.getMessageManager().parse("<green>Profile spawn location updated to your current position!</green>"));
    }

    private void handleReload(Player player) {
        if (!player.hasPermission("zenprofiles.admin")) {
            player.sendMessage(plugin.getMessageManager().getMessage("no-permission"));
            return;
        }

        plugin.reloadConfig();
        plugin.getMessageManager().loadMessages();
        plugin.getMenuManager().loadMenuConfigs();
        plugin.getProfileManager().loadFruitNames();

        player.sendMessage(plugin.getMessageManager().getMessage("reload-success"));
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return filterTabCompletions(List.of("list", "create", "select", "delete", "setspawn", "reload"), args[0]);
        }

        if (args.length == 2 && sender instanceof Player player) {
            String sub = args[0].toLowerCase();
            if ("select".equals(sub) || "delete".equals(sub)) {
                List<String> names = plugin.getProfileManager().getPlayerProfiles(player.getUniqueId()).stream()
                        .map(Profile::getName)
                        .toList();
                return filterTabCompletions(names, args[1]);
            }
        }

        return List.of();
    }

    private List<String> filterTabCompletions(List<String> choices, String input) {
        List<String> completions = new ArrayList<>();
        String lowerInput = input.toLowerCase();
        for (String choice : choices) {
            if (choice.toLowerCase().startsWith(lowerInput)) {
                completions.add(choice);
            }
        }
        return completions;
    }
}
