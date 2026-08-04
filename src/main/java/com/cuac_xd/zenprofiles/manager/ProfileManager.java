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
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Core Profile Manager.
 * Orchestrates profile loading, creation, deletion, state synchronization,
 * combat tracking, fruit-name generation, and asynchronous profile switching.
 *
 * @author cuac_xd
 */
public class ProfileManager {

    private final ZenProfiles plugin;

    // Cache of loaded profiles per player UUID: UUID -> List<Profile>
    private final Map<UUID, List<Profile>> loadedProfiles = new ConcurrentHashMap<>();

    // Map of active profiles per player UUID: UUID -> Profile
    private final Map<UUID, Profile> activeProfiles = new ConcurrentHashMap<>();

    // Map tracking combat timestamps for combat-check validation
    private final Map<UUID, Long> combatMap = new ConcurrentHashMap<>();

    // Pre-configured fruit name list for 1-click automatic profile creation
    private final List<String> fruitNames = new ArrayList<>();

    public ProfileManager(ZenProfiles plugin) {
        this.plugin = plugin;
        loadFruitNames();
    }

    /**
     * Loads the fruit name list from config.yml.
     */
    public void loadFruitNames() {
        fruitNames.clear();
        List<String> configFruits = plugin.getConfig().getStringList("skyblock-fruit-names");
        if (configFruits != null && !configFruits.isEmpty()) {
            fruitNames.addAll(configFruits);
        } else {
            fruitNames.addAll(List.of("Cucumber", "Kiwi", "Peach", "Mango", "Papaya", "Pineapple", "Coconut", "Watermelon", "Grape", "Lemon"));
        }
    }

    /**
     * Generates the next available fruit name for a player.
     *
     * @param playerUuid The UUID of the player account.
     * @return An unassigned fruit name string.
     */
    public String generateNextFruitName(UUID playerUuid) {
        List<Profile> existing = getPlayerProfiles(playerUuid);
        List<String> usedNames = existing.stream()
                .map(p -> p.getName().toLowerCase())
                .toList();

        for (String fruit : fruitNames) {
            if (!usedNames.contains(fruit.toLowerCase())) {
                return fruit;
            }
        }
        return "Fruit_" + (existing.size() + 1);
    }

    /**
     * Asynchronously loads all profiles owned by a player upon join.
     *
     * @param player The player instance.
     * @return CompletableFuture completing with the loaded profile list.
     */
    public CompletableFuture<List<Profile>> loadPlayerProfiles(Player player) {
        UUID uuid = player.getUniqueId();
        return plugin.getStorage().loadProfiles(uuid).thenApply(profiles -> {
            loadedProfiles.put(uuid, profiles);

            if (!profiles.isEmpty()) {
                // Determine most recently played profile
                Profile mostRecent = profiles.stream()
                        .max((p1, p2) -> Long.compare(p1.getLastPlayed(), p2.getLastPlayed()))
                        .orElse(profiles.get(0));

                activeProfiles.put(uuid, mostRecent);

                Bukkit.getScheduler().runTask(plugin, () -> {
                    applyProfileToPlayer(player, mostRecent);
                });
            } else {
                // Auto-create initial profile for first-time players
                String initialName = generateNextFruitName(uuid);
                createProfile(uuid, initialName).thenAccept(newProfile -> {
                    if (newProfile != null) {
                        activeProfiles.put(uuid, newProfile);
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            applyProfileToPlayer(player, newProfile);
                        });
                    }
                });
            }
            return profiles;
        });
    }

    /**
     * Gets all loaded profiles for a player.
     *
     * @param playerUuid The player account UUID.
     * @return List of profiles.
     */
    public List<Profile> getPlayerProfiles(UUID playerUuid) {
        return loadedProfiles.getOrDefault(playerUuid, new ArrayList<>());
    }

    /**
     * Gets the currently active profile for a player.
     *
     * @param playerUuid The player account UUID.
     * @return The active Profile instance, or null if none loaded.
     */
    public Profile getActiveProfile(UUID playerUuid) {
        return activeProfiles.get(playerUuid);
    }

    /**
     * Gets the maximum profile slots allowed for a player based on permissions and config.
     *
     * @param player The player instance.
     * @return The maximum profile count integer.
     */
    public int getMaxProfiles(Player player) {
        if (player.hasPermission("zenprofiles.max.unlimited") || player.isOp()) {
            return 27;
        }

        int max = plugin.getConfig().getInt("default-max-profiles", 3);
        for (int i = 27; i >= 1; i--) {
            if (player.hasPermission("zenprofiles.max." + i)) {
                return i;
            }
        }
        return max;
    }

    /**
     * Asynchronously creates a new profile for a player.
     *
     * @param playerUuid The player account UUID.
     * @param name The profile name.
     * @return CompletableFuture completing with the created Profile object.
     */
    public CompletableFuture<Profile> createProfile(UUID playerUuid, String name) {
        Profile profile = new Profile(UUID.randomUUID(), playerUuid, name);
        List<Profile> list = loadedProfiles.computeIfAbsent(playerUuid, k -> new ArrayList<>());

        Player player = Bukkit.getPlayer(playerUuid);
        if (player != null) {
            Location spawnLoc = getSpawnLocation();
            if (spawnLoc != null) {
                profile.getData().setLocation(spawnLoc);
            }
        }

        list.add(profile);
        return plugin.getStorage().saveProfile(profile).thenApply(v -> {
            Bukkit.getPluginManager().callEvent(new ProfileCreateEvent(playerUuid, profile));
            return profile;
        });
    }

    /**
     * Asynchronously deletes a profile.
     *
     * @param playerUuid The player account UUID.
     * @param profileId The profile UUID to delete.
     * @return CompletableFuture completing with true if successful.
     */
    public CompletableFuture<Boolean> deleteProfile(UUID playerUuid, UUID profileId) {
        List<Profile> list = loadedProfiles.get(playerUuid);
        if (list == null) return CompletableFuture.completedFuture(false);

        Profile toRemove = list.stream().filter(p -> p.getProfileId().equals(profileId)).findFirst().orElse(null);
        if (toRemove == null) return CompletableFuture.completedFuture(false);

        list.remove(toRemove);
        return plugin.getStorage().deleteProfile(playerUuid, profileId).thenApply(v -> {
            Bukkit.getPluginManager().callEvent(new ProfileDeleteEvent(playerUuid, toRemove));
            return true;
        });
    }

    /**
     * Tags a player in combat to prevent immediate profile switching.
     *
     * @param player The player instance.
     */
    public void tagCombat(Player player) {
        combatMap.put(player.getUniqueId(), System.currentTimeMillis());
    }

    /**
     * Checks if a player is currently tagged in combat.
     *
     * @param player The player instance.
     * @return true if in combat, false otherwise.
     */
    public boolean isInCombat(Player player) {
        if (!plugin.getConfig().getBoolean("combat-check.enabled", true)) return false;
        Long lastCombat = combatMap.get(player.getUniqueId());
        if (lastCombat == null) return false;

        long durationMs = plugin.getConfig().getLong("combat-check.duration-seconds", 10) * 1000L;
        return (System.currentTimeMillis() - lastCombat) < durationMs;
    }

    /**
     * Initiates an asynchronous profile switch for a player.
     *
     * @param player The player switching profiles.
     * @param targetProfile The profile to switch to.
     */
    public void initiateSwitch(Player player, Profile targetProfile) {
        if (isInCombat(player)) {
            player.sendMessage(plugin.getMessageManager().getMessage("in-combat"));
            return;
        }

        Profile currentActive = getActiveProfile(player.getUniqueId());
        if (currentActive != null && currentActive.getProfileId().equals(targetProfile.getProfileId())) {
            player.sendMessage(plugin.getMessageManager().getMessage("already-active"));
            return;
        }

        // Fire cancellable pre-switch event
        ProfilePreSwitchEvent preEvent = new ProfilePreSwitchEvent(player, currentActive, targetProfile);
        Bukkit.getPluginManager().callEvent(preEvent);
        if (preEvent.isCancelled()) return;

        int delaySeconds = plugin.getConfig().getInt("switch-delay-seconds", 0);
        if (delaySeconds <= 0 || player.hasPermission("zenprofiles.admin")) {
            executeSwitch(player, currentActive, targetProfile);
        } else {
            player.sendMessage(plugin.getMessageManager().getMessage("switch-delayed", "%seconds%", String.valueOf(delaySeconds)));
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (player.isOnline() && !isInCombat(player)) {
                    executeSwitch(player, currentActive, targetProfile);
                }
            }, delaySeconds * 20L);
        }
    }

    private void executeSwitch(Player player, Profile currentActive, Profile targetProfile) {
        if (currentActive != null) {
            saveCurrentPlayerToProfile(player, currentActive);
            plugin.getStorage().saveProfile(currentActive);
        }

        activeProfiles.put(player.getUniqueId(), targetProfile);

        // Apply profile data to player on Bukkit main thread
        applyProfileToPlayer(player, targetProfile);

        // Fire profile switch completion event
        Bukkit.getPluginManager().callEvent(new ProfileSwitchEvent(player, currentActive, targetProfile));

        // Update LuckPerms context
        plugin.getLuckPermsHook().updateContext(player);

        player.sendMessage(plugin.getMessageManager().getMessage("profile-switched", "%profile_name%", targetProfile.getName()));

        // Save new profile state
        plugin.getStorage().saveProfile(targetProfile);
    }

    /**
     * Saves current player attributes, inventory, potion effects, and location into the active Profile object.
     *
     * @param player The player instance.
     * @param profile The target Profile object.
     */
    public void saveCurrentPlayerToProfile(Player player, Profile profile) {
        ProfileData data = profile.getData();
        data.setHealth(player.getHealth());

        AttributeInstance maxHealthAttr = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (maxHealthAttr != null) {
            data.setMaxHealth(maxHealthAttr.getBaseValue());
        }

        data.setFoodLevel(player.getFoodLevel());
        data.setSaturation(player.getSaturation());
        data.setExhaustion(player.getExhaustion());
        data.setXpLevel(player.getLevel());
        data.setXpProgress(player.getExp());
        data.setGamemode(player.getGameMode().name());

        data.setInventoryContents(serializeItemArray(player.getInventory().getContents()));
        data.setArmorContents(serializeItemArray(player.getInventory().getArmorContents()));
        data.setOffhandContent(serializeItemStack(player.getInventory().getItemInOffHand()));
        data.setEnderChestContents(serializeItemArray(player.getEnderChest().getContents()));

        data.setPotionEffects(new ArrayList<>(player.getActivePotionEffects()));
        data.setLocation(player.getLocation());

        profile.setLastPlayed(System.currentTimeMillis());
    }

    /**
     * Applies stored profile attributes, inventory, potion effects, and location to a player.
     *
     * @param player The player instance.
     * @param profile The source Profile object.
     */
    public void applyProfileToPlayer(Player player, Profile profile) {
        ProfileData data = profile.getData();

        // Clear active potion effects
        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }

        // Apply health attributes
        AttributeInstance maxHealthAttr = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (maxHealthAttr != null) {
            maxHealthAttr.setBaseValue(data.getMaxHealth());
        }
        player.setHealth(Math.min(data.getHealth(), data.getMaxHealth()));

        player.setFoodLevel(data.getFoodLevel());
        player.setSaturation(data.getSaturation());
        player.setExhaustion(data.getExhaustion());
        player.setLevel(data.getXpLevel());
        player.setExp(data.getXpProgress());

        try {
            player.setGameMode(GameMode.valueOf(data.getGamemode()));
        } catch (Exception e) {
            player.setGameMode(GameMode.SURVIVAL);
        }

        // Restore inventories
        player.getInventory().setContents(deserializeItemArray(data.getInventoryContents()));
        player.getInventory().setArmorContents(deserializeItemArray(data.getArmorContents()));
        player.getInventory().setItemInOffHand(deserializeItemStack(data.getOffhandContent()));
        player.getEnderChest().setContents(deserializeItemArray(data.getEnderChestContents()));

        // Restore potion effects
        for (PotionEffect effect : data.getPotionEffects()) {
            player.addPotionEffect(effect);
        }

        // Teleportation handling
        Location targetLoc = data.getLocation();
        Location spawnLoc = getSpawnLocation();

        if (plugin.getConfig().getBoolean("spawn.teleport-on-every-switch", false) && spawnLoc != null) {
            player.teleport(spawnLoc);
        } else if (targetLoc != null && targetLoc.getWorld() != null) {
            player.teleport(targetLoc);
        } else if (spawnLoc != null) {
            player.teleport(spawnLoc);
        }
    }

    /**
     * Gets the configured spawn location.
     *
     * @return Spawn Location object, or world spawn if set.
     */
    public Location getSpawnLocation() {
        FileConfiguration config = plugin.getConfig();
        if (config.getBoolean("spawn.use-custom-spawn", false)) {
            String worldName = config.getString("spawn.custom-spawn.world", "world");
            World world = Bukkit.getWorld(worldName);
            if (world != null) {
                double x = config.getDouble("spawn.custom-spawn.x", 0);
                double y = config.getDouble("spawn.custom-spawn.y", 64);
                double z = config.getDouble("spawn.custom-spawn.z", 0);
                float yaw = (float) config.getDouble("spawn.custom-spawn.yaw", 0);
                float pitch = (float) config.getDouble("spawn.custom-spawn.pitch", 0);
                return new Location(world, x, y, z, yaw, pitch);
            }
        }
        World mainWorld = Bukkit.getWorlds().get(0);
        return mainWorld != null ? mainWorld.getSpawnLocation() : null;
    }

    /**
     * Saves active profile and unloads player state on quit.
     *
     * @param player The player instance.
     * @return CompletableFuture completing when save operations finish.
     */
    public CompletableFuture<Void> saveAndUnloadPlayer(Player player) {
        UUID uuid = player.getUniqueId();
        Profile active = activeProfiles.remove(uuid);
        List<Profile> profiles = loadedProfiles.remove(uuid);
        combatMap.remove(uuid);

        if (active != null) {
            saveCurrentPlayerToProfile(player, active);
            return plugin.getStorage().saveProfile(active);
        } else if (profiles != null && !profiles.isEmpty()) {
            List<CompletableFuture<Void>> futures = new ArrayList<>();
            for (Profile p : profiles) {
                futures.add(plugin.getStorage().saveProfile(p));
            }
            return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        }
        return CompletableFuture.completedFuture(null);
    }

    // Helper methods for ItemStack serialization
    private List<Map<String, Object>> serializeItemArray(ItemStack[] items) {
        List<Map<String, Object>> list = new ArrayList<>();
        if (items == null) return list;
        for (ItemStack item : items) {
            if (item != null && !item.getType().isAir()) {
                list.add(item.serialize());
            } else {
                list.add(new HashMap<>());
            }
        }
        return list;
    }

    private ItemStack[] deserializeItemArray(List<Map<String, Object>> list) {
        if (list == null || list.isEmpty()) return new ItemStack[0];
        ItemStack[] items = new ItemStack[list.size()];
        for (int i = 0; i < list.size(); i++) {
            Map<String, Object> map = list.get(i);
            if (map != null && !map.isEmpty()) {
                items[i] = ItemStack.deserialize(map);
            } else {
                items[i] = null;
            }
        }
        return items;
    }

    private Map<String, Object> serializeItemStack(ItemStack item) {
        return (item != null && !item.getType().isAir()) ? item.serialize() : new HashMap<>();
    }

    private ItemStack deserializeItemStack(Map<String, Object> map) {
        return (map != null && !map.isEmpty()) ? ItemStack.deserialize(map) : null;
    }
}
