package com.cuac_xd.zenprofiles.storage;

import com.cuac_xd.zenprofiles.ZenProfiles;
import com.cuac_xd.zenprofiles.model.Profile;
import com.cuac_xd.zenprofiles.model.ProfileData;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * YAML file storage engine implementation.
 * Manages asynchronous reading and writing of profile data files stored under
 * plugins/ZenProfiles/profiles/<player_uuid>/<profile_id>.yml.
 *
 * @author cuac_xd
 */
public class YamlStorage implements ProfileStorage {

    private final ZenProfiles plugin;
    private final File profilesDir;

    public YamlStorage(ZenProfiles plugin) {
        this.plugin = plugin;
        this.profilesDir = new File(plugin.getDataFolder(), "profiles");
    }

    @Override
    public CompletableFuture<Void> init() {
        return CompletableFuture.runAsync(() -> {
            if (!profilesDir.exists()) {
                profilesDir.mkdirs();
            }
        });
    }

    @Override
    public CompletableFuture<List<Profile>> loadProfiles(UUID playerUuid) {
        return CompletableFuture.supplyAsync(() -> {
            List<Profile> profiles = new ArrayList<>();
            File userFolder = new File(profilesDir, playerUuid.toString());

            if (!userFolder.exists() || !userFolder.isDirectory()) {
                return profiles;
            }

            File[] files = userFolder.listFiles((dir, name) -> name.endsWith(".yml"));
            if (files == null) return profiles;

            for (File file : files) {
                try {
                    YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
                    String idStr = config.getString("profileId");
                    String name = config.getString("name");
                    long createdAt = config.getLong("createdAt", System.currentTimeMillis());
                    long lastPlayed = config.getLong("lastPlayed", System.currentTimeMillis());

                    if (idStr == null || name == null) continue;
                    UUID profileId = UUID.fromString(idStr);

                    ProfileData data = deserializeData(config.getConfigurationSection("data"));
                    Profile profile = new Profile(profileId, playerUuid, name, createdAt, lastPlayed, data);
                    profiles.add(profile);

                } catch (Exception e) {
                    plugin.getLogger().warning("Failed to load profile file: " + file.getName() + " - " + e.getMessage());
                }
            }
            return profiles;
        });
    }

    @Override
    public CompletableFuture<Void> saveProfile(Profile profile) {
        return CompletableFuture.runAsync(() -> {
            File userFolder = new File(profilesDir, profile.getPlayerUuid().toString());
            if (!userFolder.exists()) userFolder.mkdirs();

            File file = new File(userFolder, profile.getProfileId().toString() + ".yml");
            YamlConfiguration config = new YamlConfiguration();

            config.set("profileId", profile.getProfileId().toString());
            config.set("playerUuid", profile.getPlayerUuid().toString());
            config.set("name", profile.getName());
            config.set("createdAt", profile.getCreatedAt());
            config.set("lastPlayed", profile.getLastPlayed());

            serializeData(config.createSection("data"), profile.getData());

            try {
                config.save(file);
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to save profile " + profile.getName() + ": " + e.getMessage());
            }
        });
    }

    @Override
    public CompletableFuture<Void> deleteProfile(UUID playerUuid, UUID profileId) {
        return CompletableFuture.runAsync(() -> {
            File userFolder = new File(profilesDir, playerUuid.toString());
            if (!userFolder.exists()) return;

            File file = new File(userFolder, profileId.toString() + ".yml");
            if (file.exists()) {
                file.delete();
            }
        });
    }

    @Override
    public CompletableFuture<Void> close() {
        return CompletableFuture.completedFuture(null);
    }

    private void serializeData(ConfigurationSection section, ProfileData data) {
        section.set("health", data.getHealth());
        section.set("maxHealth", data.getMaxHealth());
        section.set("foodLevel", data.getFoodLevel());
        section.set("saturation", data.getSaturation());
        section.set("exhaustion", data.getExhaustion());
        section.set("xpLevel", data.getXpLevel());
        section.set("xpProgress", data.getXpProgress());
        section.set("gamemode", data.getGamemode());
        section.set("balance", data.getBalance());

        section.set("inventory", data.getInventoryContents());
        section.set("armor", data.getArmorContents());
        section.set("offhand", data.getOffhandContent());
        section.set("enderChest", data.getEnderChestContents());

        // Serialize potion effects
        List<Map<String, Object>> potionList = new ArrayList<>();
        for (PotionEffect effect : data.getPotionEffects()) {
            Map<String, Object> map = new HashMap<>();
            map.put("type", effect.getType().getName());
            map.put("duration", effect.getDuration());
            map.put("amplifier", effect.getAmplifier());
            map.put("ambient", effect.isAmbient());
            map.put("particles", effect.hasParticles());

            potionList.add(map);
        }
        section.set("potionEffects", potionList);

        // Serialize location
        if (data.getLocation() != null && data.getLocation().getWorld() != null) {
            ConfigurationSection locSection = section.createSection("location");
            locSection.set("world", data.getLocation().getWorld().getName());
            locSection.set("x", data.getLocation().getX());
            locSection.set("y", data.getLocation().getY());
            locSection.set("z", data.getLocation().getZ());
            locSection.set("yaw", data.getLocation().getYaw());
            locSection.set("pitch", data.getLocation().getPitch());
        }

        // Serialize custom PDC data
        section.set("pdc", data.getCustomPdcData());
    }

    @SuppressWarnings("unchecked")
    private ProfileData deserializeData(ConfigurationSection section) {
        ProfileData data = new ProfileData();
        if (section == null) return data;

        data.setHealth(section.getDouble("health", 20.0));
        data.setMaxHealth(section.getDouble("maxHealth", 20.0));
        data.setFoodLevel(section.getInt("foodLevel", 20));
        data.setSaturation((float) section.getDouble("saturation", 5.0));
        data.setExhaustion((float) section.getDouble("exhaustion", 0.0));
        data.setXpLevel(section.getInt("xpLevel", 0));
        data.setXpProgress((float) section.getDouble("xpProgress", 0.0));
        data.setGamemode(section.getString("gamemode", "SURVIVAL"));
        data.setBalance(section.getDouble("balance", 0.0));

        data.setInventoryContents((List<Map<String, Object>>) section.get("inventory"));
        data.setArmorContents((List<Map<String, Object>>) section.get("armor"));
        data.setOffhandContent((Map<String, Object>) section.get("offhand"));
        data.setEnderChestContents((List<Map<String, Object>>) section.get("enderChest"));

        // Deserialize potion effects
        List<PotionEffect> potions = new ArrayList<>();
        List<Map<?, ?>> rawPotions = section.getMapList("potionEffects");
        for (Map<?, ?> map : rawPotions) {
            try {
                String typeName = (String) map.get("type");
                int duration = (int) map.get("duration");
                int amplifier = (int) map.get("amplifier");
                boolean ambient = map.containsKey("ambient") && (boolean) map.get("ambient");
                boolean particles = !map.containsKey("particles") || (boolean) map.get("particles");

                PotionEffectType type = PotionEffectType.getByName(typeName);
                if (type != null) {
                    potions.add(new PotionEffect(type, duration, amplifier, ambient, particles));
                }
            } catch (Exception ignored) {}
        }
        data.setPotionEffects(potions);

        // Deserialize location
        ConfigurationSection locSection = section.getConfigurationSection("location");
        if (locSection != null) {
            String worldName = locSection.getString("world");
            if (worldName != null) {
                World world = Bukkit.getWorld(worldName);
                double x = locSection.getDouble("x");
                double y = locSection.getDouble("y");
                double z = locSection.getDouble("z");
                float yaw = (float) locSection.getDouble("yaw");
                float pitch = (float) locSection.getDouble("pitch");

                if (world != null) {
                    data.setLocation(new Location(world, x, y, z, yaw, pitch));
                }
            }
        }

        // Deserialize custom PDC data
        ConfigurationSection pdcSec = section.getConfigurationSection("pdc");
        if (pdcSec != null) {
            Map<String, String> pdcMap = new HashMap<>();
            for (String key : pdcSec.getKeys(false)) {
                pdcMap.put(key, pdcSec.getString(key));
            }
            data.setCustomPdcData(pdcMap);
        }

        return data;
    }
}
