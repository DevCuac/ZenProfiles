package com.cuac_xd.zenprofiles.model;

import org.bukkit.GameMode;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents player state isolated per profile.
 */
public class ProfileData {

    private double health = 20.0;
    private double maxHealth = 20.0;
    private int foodLevel = 20;
    private float saturation = 5.0f;
    private float exhaustion = 0.0f;
    private int level = 0;
    private float exp = 0.0f;
    private String gameMode = GameMode.SURVIVAL.name();
    private String worldName;
    private double x, y, z;
    private float yaw, pitch;

    // Per-profile Vault Economy balance (Purse Coins)
    private double balance = 0.0;

    private List<PotionEffect> potionEffects = new ArrayList<>();

    private ItemStack[] inventoryContents = new ItemStack[36];
    private ItemStack[] armorContents = new ItemStack[4];
    private ItemStack offHandItem;
    private ItemStack[] enderChestContents = new ItemStack[27];

    private Map<String, Boolean> perProfilePermissions = new HashMap<>();

    // Custom Persistent Data Container (PDC) & Eco Data per profile
    private Map<String, String> customPdcData = new HashMap<>();

    public ProfileData() {}

    // Getters and Setters
    public double getHealth() { return health; }
    public void setHealth(double health) { this.health = health; }

    public double getMaxHealth() { return maxHealth; }
    public void setMaxHealth(double maxHealth) { this.maxHealth = maxHealth; }

    public int getFoodLevel() { return foodLevel; }
    public void setFoodLevel(int foodLevel) { this.foodLevel = foodLevel; }

    public float getSaturation() { return saturation; }
    public void setSaturation(float saturation) { this.saturation = saturation; }

    public float getExhaustion() { return exhaustion; }
    public void setExhaustion(float exhaustion) { this.exhaustion = exhaustion; }

    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }

    public float getExp() { return exp; }
    public void setExp(float exp) { this.exp = exp; }

    public String getGameMode() { return gameMode; }
    public void setGameMode(String gameMode) { this.gameMode = gameMode; }

    public String getWorldName() { return worldName; }
    public void setWorldName(String worldName) { this.worldName = worldName; }

    public double getX() { return x; }
    public void setX(double x) { this.x = x; }

    public double getY() { return y; }
    public void setY(double y) { this.y = y; }

    public double getZ() { return z; }
    public void setZ(double z) { this.z = z; }

    public float getYaw() { return yaw; }
    public void setYaw(float yaw) { this.yaw = yaw; }

    public float getPitch() { return pitch; }
    public void setPitch(float pitch) { this.pitch = pitch; }

    public double getBalance() { return balance; }
    public void setBalance(double balance) { this.balance = balance; }

    public List<PotionEffect> getPotionEffects() { return potionEffects; }
    public void setPotionEffects(List<PotionEffect> potionEffects) { this.potionEffects = potionEffects; }

    public ItemStack[] getInventoryContents() { return inventoryContents; }
    public void setInventoryContents(ItemStack[] inventoryContents) { this.inventoryContents = inventoryContents; }

    public ItemStack[] getArmorContents() { return armorContents; }
    public void setArmorContents(ItemStack[] armorContents) { this.armorContents = armorContents; }

    public ItemStack getOffHandItem() { return offHandItem; }
    public void setOffHandItem(ItemStack offHandItem) { this.offHandItem = offHandItem; }

    public ItemStack[] getEnderChestContents() { return enderChestContents; }
    public void setEnderChestContents(ItemStack[] enderChestContents) { this.enderChestContents = enderChestContents; }

    public Map<String, Boolean> getPerProfilePermissions() { return perProfilePermissions; }
    public void setPerProfilePermissions(Map<String, Boolean> perProfilePermissions) { this.perProfilePermissions = perProfilePermissions; }

    public Map<String, String> getCustomPdcData() { return customPdcData; }
    public void setCustomPdcData(Map<String, String> customPdcData) { this.customPdcData = customPdcData; }
}
