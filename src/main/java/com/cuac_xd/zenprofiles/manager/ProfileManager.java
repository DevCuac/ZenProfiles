package com.cuac_xd.zenprofiles.manager;

import com.cuac_xd.zenprofiles.ZenProfiles;
import com.cuac_xd.zenprofiles.api.event.ProfileCreateEvent;
import com.cuac_xd.zenprofiles.api.event.ProfileDeleteEvent;
import com.cuac_xd.zenprofiles.api.event.ProfilePreSwitchEvent;
import com.cuac_xd.zenprofiles.api.event.ProfileSwitchEvent;
import com.cuac_xd.zenprofiles.model.Profile;
import com.cuac_xd.zenprofiles.model.ProfileData;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public class ProfileManager {

    private final ZenProfiles plugin;

    // Cache of profiles per player: playerUuid -> List<Profile>
    private final Map<UUID, List<Profile>> loadedProfiles = new ConcurrentHashMap<>();
    // Map of active profile: playerUuid -> Profile
    private final Map<UUID, Profile> activeProfiles = new ConcurrentHashMap<>();
    // Per-profile permission attachments: playerUuid -> PermissionAttachment
    private final Map<UUID, PermissionAttachment> perProfileAttachments = new ConcurrentHashMap<>();

    // Combat tracking: playerUuid -> timestamp millis
    private final Map<UUID, Long> combatTimestamps = new ConcurrentHashMap<>();

    // Profile switch countdowns: playerUuid -> BukkitTask
    private final Map<UUID, BukkitTask> switchTasks = new ConcurrentHashMap<>();

    // Chat prompt input for profile creation: playerUuid
    private final Set<UUID> pendingChatInput = ConcurrentHashMap.newKeySet();

    public ProfileManager(ZenProfiles plugin) {
        this.plugin = plugin;
    }

    public CompletableFuture<Void> loadPlayerProfiles(Player player) {
        return plugin.getStorage().loadProfiles(player.getUniqueId()).thenAccept(profiles -> {
            loadedProfiles.put(player.getUniqueId(), profiles);

            if (profiles.isEmpty()) {
                // Create a default first profile for new player
                Profile defaultProfile = createProfileInstance(player, "Default");
                profiles.add(defaultProfile);
                plugin.getStorage().saveProfile(defaultProfile);
                Bukkit.getPluginManager().callEvent(new ProfileCreateEvent(player, defaultProfile));
            }

            // Default to most recently played profile
            Profile toActivate = profiles.get(0);
            applyProfile(player, toActivate);
        });
    }

    public CompletableFuture<Void> saveAndUnloadPlayer(Player player) {
        UUID pUuid = player.getUniqueId();
        cancelSwitchTask(pUuid);
        pendingChatInput.remove(pUuid);

        Profile active = activeProfiles.get(pUuid);
        if (active != null) {
            captureProfileData(player, active);
            active.setLastPlayed(System.currentTimeMillis());

            // Clean permission attachment
            PermissionAttachment attachment = perProfileAttachments.remove(pUuid);
            if (attachment != null) {
                player.removeAttachment(attachment);
            }

            activeProfiles.remove(pUuid);
            return plugin.getStorage().saveProfile(active).thenRun(() -> loadedProfiles.remove(pUuid));
        }

        loadedProfiles.remove(pUuid);
        return CompletableFuture.completedFuture(null);
    }

    public List<Profile> getPlayerProfiles(UUID playerUuid) {
        return loadedProfiles.getOrDefault(playerUuid, Collections.emptyList());
    }

    public Profile getActiveProfile(UUID playerUuid) {
        return activeProfiles.get(playerUuid);
    }

    public int getMaxProfiles(Player player) {
        if (player.hasPermission("zenprofiles.max.unlimited") || player.isOp()) {
            return 99;
        }

        int max = plugin.getConfig().getInt("default-max-profiles", 3);
        for (int i = 100; i >= 1; i--) {
            if (player.hasPermission("zenprofiles.max." + i)) {
                return i;
            }
        }
        return max;
    }

    public void tagCombat(UUID playerUuid) {
        combatTimestamps.put(playerUuid, System.currentTimeMillis());
    }

    public boolean isInCombat(UUID playerUuid) {
        if (!plugin.getConfig().getBoolean("profile-switching.combat-check.enabled", true)) {
            return false;
        }
        Long lastCombat = combatTimestamps.get(playerUuid);
        if (lastCombat == null) return false;
        long durationMs = plugin.getConfig().getLong("profile-switching.combat-check.duration-seconds", 10) * 1000L;
        return (System.currentTimeMillis() - lastCombat) < durationMs;
    }

    public boolean validateProfileName(String name) {
        if (name == null) return false;
        String regex = plugin.getConfig().getString("profile-naming.regex", "^[a-zA-Z0-9_]{3,16}$");
        if (!Pattern.matches(regex, name)) return false;

        List<String> blacklist = plugin.getConfig().getStringList("profile-naming.blacklisted-names");
        return !blacklist.contains(name.toLowerCase());
    }

    public String generateNextFruitName(UUID playerUuid) {
        List<String> fruitNames = plugin.getConfig().getStringList("skyblock-fruit-names");
        if (fruitNames == null || fruitNames.isEmpty()) {
            fruitNames = Arrays.asList("Cucumber", "Fruit", "Kiwi", "Peach", "Mango", "Blueberry", "Zucchini", "Papaya", "Pineapple", "Coconut", "Lemon", "Lime");
        }

        List<Profile> existing = getPlayerProfiles(playerUuid);
        for (String fruit : fruitNames) {
            boolean used = false;
            for (Profile p : existing) {
                if (p.getName().equalsIgnoreCase(fruit)) {
                    used = true;
                    break;
                }
            }
            if (!used) return fruit;
        }

        return "Profile_" + (existing.size() + 1);
    }

    public Location getSpawnLocation() {
        if (plugin.getConfig().getBoolean("spawn.use-world-spawn", true)) {
            World defaultWorld = Bukkit.getWorlds().isEmpty() ? null : Bukkit.getWorlds().get(0);
            if (defaultWorld != null) {
                return defaultWorld.getSpawnLocation();
            }
        }

        String wName = plugin.getConfig().getString("spawn.custom-spawn.world", "world");
        World world = Bukkit.getWorld(wName);
        if (world == null && !Bukkit.getWorlds().isEmpty()) {
            world = Bukkit.getWorlds().get(0);
        }

        if (world != null) {
            double x = plugin.getConfig().getDouble("spawn.custom-spawn.x", 0.5);
            double y = plugin.getConfig().getDouble("spawn.custom-spawn.y", 64.0);
            double z = plugin.getConfig().getDouble("spawn.custom-spawn.z", 0.5);
            float yaw = (float) plugin.getConfig().getDouble("spawn.custom-spawn.yaw", 0.0);
            float pitch = (float) plugin.getConfig().getDouble("spawn.custom-spawn.pitch", 0.0);
            return new Location(world, x, y, z, yaw, pitch);
        }

        return new Location(Bukkit.getWorlds().get(0), 0.5, 64, 0.5);
    }

    public void setSpawnLocation(Location loc) {
        if (loc == null || loc.getWorld() == null) return;
        plugin.getConfig().set("spawn.use-world-spawn", false);
        plugin.getConfig().set("spawn.custom-spawn.world", loc.getWorld().getName());
        plugin.getConfig().set("spawn.custom-spawn.x", loc.getX());
        plugin.getConfig().set("spawn.custom-spawn.y", loc.getY());
        plugin.getConfig().set("spawn.custom-spawn.z", loc.getZ());
        plugin.getConfig().set("spawn.custom-spawn.yaw", (double) loc.getYaw());
        plugin.getConfig().set("spawn.custom-spawn.pitch", (double) loc.getPitch());
        plugin.saveConfig();
    }

    public Profile createProfileInstance(Player player, String name) {
        Profile profile = new Profile(player.getUniqueId(), name);
        ProfileData data = profile.getData();

        Location spawn = getSpawnLocation();
        if (spawn != null && spawn.getWorld() != null) {
            data.setWorldName(spawn.getWorld().getName());
            data.setX(spawn.getX());
            data.setY(spawn.getY());
            data.setZ(spawn.getZ());
            data.setYaw(spawn.getYaw());
            data.setPitch(spawn.getPitch());
        }

        return profile;
    }

    public Profile createProfile(Player player, String name) {
        List<Profile> profiles = loadedProfiles.getOrDefault(player.getUniqueId(), new ArrayList<>());
        if (profiles.size() >= getMaxProfiles(player)) {
            player.sendMessage(MessageManager.parse(plugin.getMessageManager().getRawMessage("profile.limit-reached")
                    .replace("{max}", String.valueOf(getMaxProfiles(player)))));
            return null;
        }

        if (!validateProfileName(name)) {
            player.sendMessage(plugin.getMessageManager().getComponent("profile.invalid-name"));
            return null;
        }

        for (Profile p : profiles) {
            if (p.getName().equalsIgnoreCase(name)) {
                player.sendMessage(MessageManager.parse(plugin.getMessageManager().getRawMessage("profile.name-taken")
                        .replace("{profile_name}", name)));
                return null;
            }
        }

        Profile newProfile = createProfileInstance(player, name);
        profiles.add(newProfile);
        loadedProfiles.put(player.getUniqueId(), profiles);
        plugin.getStorage().saveProfile(newProfile);

        Bukkit.getPluginManager().callEvent(new ProfileCreateEvent(player, newProfile));
        player.sendMessage(MessageManager.parse(plugin.getMessageManager().getRawMessage("profile.created")
                .replace("{profile_name}", name)));

        // Automatically switch to the newly created profile!
        initiateSwitch(player, newProfile);
        return newProfile;
    }

    public void deleteProfile(Player player, Profile profile) {
        Profile active = getActiveProfile(player.getUniqueId());
        if (active != null && active.getProfileId().equals(profile.getProfileId())) {
            player.sendMessage(plugin.getMessageManager().getComponent("profile.cannot-delete-active"));
            return;
        }

        List<Profile> profiles = loadedProfiles.get(player.getUniqueId());
        if (profiles != null) {
            profiles.remove(profile);
        }

        plugin.getStorage().deleteProfile(profile.getProfileId());
        Bukkit.getPluginManager().callEvent(new ProfileDeleteEvent(player, profile));
        player.sendMessage(MessageManager.parse(plugin.getMessageManager().getRawMessage("profile.deleted")
                .replace("{profile_name}", profile.getName())));
    }

    public void initiateSwitch(Player player, Profile targetProfile) {
        Profile current = getActiveProfile(player.getUniqueId());
        if (current != null && current.getProfileId().equals(targetProfile.getProfileId())) {
            player.sendMessage(MessageManager.parse(plugin.getMessageManager().getRawMessage("profile.already-active")
                    .replace("{profile_name}", targetProfile.getName())));
            return;
        }

        if (isInCombat(player.getUniqueId())) {
            player.sendMessage(plugin.getMessageManager().getComponent("switch.in-combat"));
            return;
        }

        ProfilePreSwitchEvent preEvent = new ProfilePreSwitchEvent(player, current, targetProfile);
        Bukkit.getPluginManager().callEvent(preEvent);
        if (preEvent.isCancelled()) return;

        int delay = plugin.getConfig().getInt("profile-switching.delay-seconds", 3);
        if (delay <= 0 || player.hasPermission("zenprofiles.bypass.delay")) {
            executeSwitch(player, current, targetProfile);
            return;
        }

        cancelSwitchTask(player.getUniqueId());

        player.sendMessage(MessageManager.parse(plugin.getMessageManager().getRawMessage("switch.countdown")
                .replace("{profile_name}", targetProfile.getName())
                .replace("{seconds}", String.valueOf(delay))));

        Location startLoc = player.getLocation().clone();

        BukkitTask task = new BukkitRunnable() {
            int remaining = delay;

            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancel();
                    switchTasks.remove(player.getUniqueId());
                    return;
                }

                if (isInCombat(player.getUniqueId())) {
                    player.sendMessage(plugin.getMessageManager().getComponent("switch.cancelled-combat"));
                    cancel();
                    switchTasks.remove(player.getUniqueId());
                    return;
                }

                if (plugin.getConfig().getBoolean("profile-switching.cancel-on-move", true)) {
                    if (startLoc.getWorld() != player.getWorld() || startLoc.distanceSquared(player.getLocation()) > 0.1) {
                        player.sendMessage(plugin.getMessageManager().getComponent("switch.cancelled-move"));
                        cancel();
                        switchTasks.remove(player.getUniqueId());
                        return;
                    }
                }

                remaining--;
                if (remaining <= 0) {
                    cancel();
                    switchTasks.remove(player.getUniqueId());
                    executeSwitch(player, current, targetProfile);
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);

        switchTasks.put(player.getUniqueId(), task);
    }

    public void cancelSwitchTask(UUID playerUuid) {
        BukkitTask task = switchTasks.remove(playerUuid);
        if (task != null) {
            task.cancel();
        }
    }

    private void executeSwitch(Player player, Profile previousProfile, Profile targetProfile) {
        if (previousProfile != null) {
            captureProfileData(player, previousProfile);
            previousProfile.setLastPlayed(System.currentTimeMillis());
            plugin.getStorage().saveProfile(previousProfile);
        }

        applyProfile(player, targetProfile);

        Bukkit.getPluginManager().callEvent(new ProfileSwitchEvent(player, previousProfile, targetProfile));
        player.sendMessage(MessageManager.parse(plugin.getMessageManager().getRawMessage("profile.selected")
                .replace("{profile_name}", targetProfile.getName())));
    }

    public void captureProfileData(Player player, Profile profile) {
        ProfileData data = profile.getData();

        data.setHealth(player.getHealth());
        AttributeInstance maxHealthAttr = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (maxHealthAttr != null) {
            data.setMaxHealth(maxHealthAttr.getBaseValue());
        }

        data.setFoodLevel(player.getFoodLevel());
        data.setSaturation(player.getSaturation());
        data.setExhaustion(player.getExhaustion());
        data.setLevel(player.getLevel());
        data.setExp(player.getExp());
        data.setGameMode(player.getGameMode().name());

        Location loc = player.getLocation();
        if (loc.getWorld() != null) {
            data.setWorldName(loc.getWorld().getName());
            data.setX(loc.getX());
            data.setY(loc.getY());
            data.setZ(loc.getZ());
            data.setYaw(loc.getYaw());
            data.setPitch(loc.getPitch());
        }

        data.setPotionEffects(new ArrayList<>(player.getActivePotionEffects()));
        data.setInventoryContents(player.getInventory().getStorageContents());
        data.setArmorContents(player.getInventory().getArmorContents());
        data.setOffHandItem(player.getInventory().getItemInOffHand());
        data.setEnderChestContents(player.getEnderChest().getStorageContents());
    }

    public void applyProfile(Player player, Profile profile) {
        activeProfiles.put(player.getUniqueId(), profile);
        ProfileData data = profile.getData();

        // Max Health Attribute
        AttributeInstance maxHealthAttr = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (maxHealthAttr != null) {
            maxHealthAttr.setBaseValue(data.getMaxHealth());
        }

        double healthToSet = Math.min(data.getHealth(), data.getMaxHealth());
        player.setHealth(Math.max(1.0, healthToSet));

        player.setFoodLevel(data.getFoodLevel());
        player.setSaturation(data.getSaturation());
        player.setExhaustion(data.getExhaustion());
        player.setLevel(data.getLevel());
        player.setExp(data.getExp());

        try {
            player.setGameMode(GameMode.valueOf(data.getGameMode()));
        } catch (Exception e) {
            player.setGameMode(GameMode.SURVIVAL);
        }

        // Potion effects
        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }
        for (PotionEffect effect : data.getPotionEffects()) {
            player.addPotionEffect(effect);
        }

        // Inventories
        player.getInventory().clear();
        if (data.getInventoryContents() != null) {
            player.getInventory().setStorageContents(data.getInventoryContents());
        }
        if (data.getArmorContents() != null) {
            player.getInventory().setArmorContents(data.getArmorContents());
        }
        if (data.getOffHandItem() != null) {
            player.getInventory().setItemInOffHand(data.getOffHandItem());
        }
        player.getEnderChest().clear();
        if (data.getEnderChestContents() != null) {
            player.getEnderChest().setStorageContents(data.getEnderChestContents());
        }

        // Location & Spawn handling
        boolean alwaysSpawn = plugin.getConfig().getBoolean("spawn.teleport-on-every-switch", false);
        if (alwaysSpawn || data.getWorldName() == null) {
            Location spawnLoc = getSpawnLocation();
            if (spawnLoc != null) {
                player.teleport(spawnLoc);
            }
        } else {
            World w = Bukkit.getWorld(data.getWorldName());
            if (w != null) {
                player.teleport(new Location(w, data.getX(), data.getY(), data.getZ(), data.getYaw(), data.getPitch()));
            } else {
                Location spawnLoc = getSpawnLocation();
                if (spawnLoc != null) player.teleport(spawnLoc);
            }
        }

        // Apply custom per-profile permissions
        PermissionAttachment oldAttachment = perProfileAttachments.remove(player.getUniqueId());
        if (oldAttachment != null) {
            player.removeAttachment(oldAttachment);
        }
        if (!data.getPerProfilePermissions().isEmpty()) {
            PermissionAttachment newAttachment = player.addAttachment(plugin);
            data.getPerProfilePermissions().forEach(newAttachment::setPermission);
            perProfileAttachments.put(player.getUniqueId(), newAttachment);
        }
    }

    public Set<UUID> getPendingChatInput() { return pendingChatInput; }
}
