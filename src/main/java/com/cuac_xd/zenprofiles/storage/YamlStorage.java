package com.cuac_xd.zenprofiles.storage;

import com.cuac_xd.zenprofiles.ZenProfiles;
import com.cuac_xd.zenprofiles.model.Profile;
import com.cuac_xd.zenprofiles.model.ProfileData;
import org.bukkit.GameMode;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class YamlStorage implements ProfileStorage {

    private final ZenProfiles plugin;
    private final File baseFolder;

    public YamlStorage(ZenProfiles plugin) {
        this.plugin = plugin;
        this.baseFolder = new File(plugin.getDataFolder(), "profiles");
    }

    @Override
    public CompletableFuture<Void> init() {
        return CompletableFuture.runAsync(() -> {
            if (!baseFolder.exists()) {
                baseFolder.mkdirs();
            }
        });
    }

    @Override
    public CompletableFuture<List<Profile>> loadProfiles(UUID playerUuid) {
        return CompletableFuture.supplyAsync(() -> {
            List<Profile> profiles = new ArrayList<>();
            File playerFolder = new File(baseFolder, playerUuid.toString());
            if (!playerFolder.exists() || !playerFolder.isDirectory()) {
                return profiles;
            }

            File[] files = playerFolder.listFiles((dir, name) -> name.endsWith(".yml"));
            if (files == null) return profiles;

            for (File file : files) {
                try {
                    YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
                    UUID profileId = UUID.fromString(config.getString("profileId"));
                    String name = config.getString("name");
                    long createdAt = config.getLong("createdAt");
                    long lastPlayed = config.getLong("lastPlayed");

                    ProfileData data = new ProfileData();
                    data.setHealth(config.getDouble("data.health", 20.0));
                    data.setMaxHealth(config.getDouble("data.maxHealth", 20.0));
                    data.setFoodLevel(config.getInt("data.foodLevel", 20));
                    data.setSaturation((float) config.getDouble("data.saturation", 5.0));
                    data.setExhaustion((float) config.getDouble("data.exhaustion", 0.0));
                    data.setLevel(config.getInt("data.level", 0));
                    data.setExp((float) config.getDouble("data.exp", 0.0));
                    data.setGameMode(config.getString("data.gameMode", GameMode.SURVIVAL.name()));
                    data.setBalance(config.getDouble("data.balance", 0.0));

                    data.setWorldName(config.getString("data.location.world"));
                    data.setX(config.getDouble("data.location.x"));
                    data.setY(config.getDouble("data.location.y"));
                    data.setZ(config.getDouble("data.location.z"));
                    data.setYaw((float) config.getDouble("data.location.yaw"));
                    data.setPitch((float) config.getDouble("data.location.pitch"));

                    // Custom PDC / Eco Data
                    if (config.isConfigurationSection("data.pdc")) {
                        Map<String, String> pdc = new HashMap<>();
                        for (String key : config.getConfigurationSection("data.pdc").getKeys(false)) {
                            pdc.put(key, config.getString("data.pdc." + key));
                        }
                        data.setCustomPdcData(pdc);
                    }

                    // Potion effects
                    List<?> potionList = config.getList("data.potionEffects");
                    if (potionList != null) {
                        List<PotionEffect> effects = new ArrayList<>();
                        for (Object obj : potionList) {
                            if (obj instanceof PotionEffect effect) {
                                effects.add(effect);
                            }
                        }
                        data.setPotionEffects(effects);
                    }

                    // Inventories
                    List<?> invList = config.getList("data.inventory");
                    if (invList != null) {
                        data.setInventoryContents(invList.toArray(new ItemStack[0]));
                    }

                    List<?> armorList = config.getList("data.armor");
                    if (armorList != null) {
                        data.setArmorContents(armorList.toArray(new ItemStack[0]));
                    }

                    data.setOffHandItem(config.getItemStack("data.offhand"));

                    List<?> enderList = config.getList("data.enderchest");
                    if (enderList != null) {
                        data.setEnderChestContents(enderList.toArray(new ItemStack[0]));
                    }

                    // Custom per-profile permissions
                    if (config.isConfigurationSection("data.permissions")) {
                        Map<String, Boolean> perms = new HashMap<>();
                        for (String key : config.getConfigurationSection("data.permissions").getKeys(false)) {
                            perms.put(key, config.getBoolean("data.permissions." + key));
                        }
                        data.setPerProfilePermissions(perms);
                    }

                    Profile profile = new Profile(profileId, playerUuid, name, createdAt, lastPlayed, data);
                    profiles.add(profile);

                } catch (Exception e) {
                    plugin.getLogger().warning("Failed to load profile file: " + file.getName() + " - " + e.getMessage());
                }
            }

            // Sort profiles by last played descending
            profiles.sort((a, b) -> Long.compare(b.getLastPlayed(), a.getLastPlayed()));
            return profiles;
        });
    }

    @Override
    public CompletableFuture<Void> saveProfile(Profile profile) {
        return CompletableFuture.runAsync(() -> {
            File playerFolder = new File(baseFolder, profile.getPlayerUuid().toString());
            if (!playerFolder.exists()) {
                playerFolder.mkdirs();
            }

            File file = new File(playerFolder, profile.getProfileId().toString() + ".yml");
            YamlConfiguration config = new YamlConfiguration();

            config.set("profileId", profile.getProfileId().toString());
            config.set("playerUuid", profile.getPlayerUuid().toString());
            config.set("name", profile.getName());
            config.set("createdAt", profile.getCreatedAt());
            config.set("lastPlayed", profile.getLastPlayed());

            ProfileData data = profile.getData();
            config.set("data.health", data.getHealth());
            config.set("data.maxHealth", data.getMaxHealth());
            config.set("data.foodLevel", data.getFoodLevel());
            config.set("data.saturation", data.getSaturation());
            config.set("data.exhaustion", data.getExhaustion());
            config.set("data.level", data.getLevel());
            config.set("data.exp", data.getExp());
            config.set("data.gameMode", data.getGameMode());
            config.set("data.balance", data.getBalance());

            config.set("data.location.world", data.getWorldName());
            config.set("data.location.x", data.getX());
            config.set("data.location.y", data.getY());
            config.set("data.location.z", data.getZ());
            config.set("data.location.yaw", data.getYaw());
            config.set("data.location.pitch", data.getPitch());

            for (Map.Entry<String, String> entry : data.getCustomPdcData().entrySet()) {
                config.set("data.pdc." + entry.getKey(), entry.getValue());
            }

            config.set("data.potionEffects", data.getPotionEffects());
            config.set("data.inventory", data.getInventoryContents());
            config.set("data.armor", data.getArmorContents());
            config.set("data.offhand", data.getOffHandItem());
            config.set("data.enderchest", data.getEnderChestContents());

            for (Map.Entry<String, Boolean> entry : data.getPerProfilePermissions().entrySet()) {
                config.set("data.permissions." + entry.getKey(), entry.getValue());
            }

            try {
                config.save(file);
            } catch (IOException e) {
                plugin.getLogger().severe("Could not save profile " + profile.getProfileId() + ": " + e.getMessage());
            }
        });
    }

    @Override
    public CompletableFuture<Void> deleteProfile(UUID profileId) {
        return CompletableFuture.runAsync(() -> {
            File[] playerFolders = baseFolder.listFiles(File::isDirectory);
            if (playerFolders != null) {
                for (File pFolder : playerFolders) {
                    File target = new File(pFolder, profileId.toString() + ".yml");
                    if (target.exists()) {
                        target.delete();
                        break;
                    }
                }
            }
        });
    }

    @Override
    public CompletableFuture<Void> close() {
        return CompletableFuture.completedFuture(null);
    }
}
