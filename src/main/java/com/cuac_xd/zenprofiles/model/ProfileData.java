package com.cuac_xd.zenprofiles.model;

import org.bukkit.Location;
import org.bukkit.potion.PotionEffect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Payload data model containing isolated per-profile state.
 * Includes health, food, XP, gamemode, serialized inventory contents,
 * armor, offhand, ender chest, active potion effects, Vault balance, and PDC data.
 *
 * @author cuac_xd
 */
public class ProfileData {

    private double health = 20.0;
    private double maxHealth = 20.0;
    private int foodLevel = 20;
    private float saturation = 5.0f;
    private float exhaustion = 0.0f;
    private int xpLevel = 0;
    private float xpProgress = 0.0f;
    private String gamemode = "SURVIVAL";

    private double balance = 0.0;

    private List<Map<String, Object>> inventoryContents = new ArrayList<>();
    private List<Map<String, Object>> armorContents = new ArrayList<>();
    private Map<String, Object> offhandContent = new HashMap<>();
    private List<Map<String, Object>> enderChestContents = new ArrayList<>();

    private List<PotionEffect> potionEffects = new ArrayList<>();
    private Location location;

    private Map<String, String> customPdcData = new HashMap<>();

    public ProfileData() {}

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
    public int getXpLevel() { return xpLevel; }
    public void setXpLevel(int xpLevel) { this.xpLevel = xpLevel; }
    public float getXpProgress() { return xpProgress; }
    public void setXpProgress(float xpProgress) { this.xpProgress = xpProgress; }
    public String getGamemode() { return gamemode; }
    public void setGamemode(String gamemode) { this.gamemode = gamemode; }

    public double getBalance() { return balance; }
    public void setBalance(double balance) { this.balance = balance; }

    public List<Map<String, Object>> getInventoryContents() { return inventoryContents; }
    public void setInventoryContents(List<Map<String, Object>> inventoryContents) { this.inventoryContents = inventoryContents; }

    public List<Map<String, Object>> getArmorContents() { return armorContents; }
    public void setArmorContents(List<Map<String, Object>> armorContents) { this.armorContents = armorContents; }

    public Map<String, Object> getOffhandContent() { return offhandContent; }
    public void setOffhandContent(Map<String, Object> offhandContent) { this.offhandContent = offhandContent; }

    public List<Map<String, Object>> getEnderChestContents() { return enderChestContents; }
    public void setEnderChestContents(List<Map<String, Object>> enderChestContents) { this.enderChestContents = enderChestContents; }

    public List<PotionEffect> getPotionEffects() { return potionEffects; }
    public void setPotionEffects(List<PotionEffect> potionEffects) { this.potionEffects = potionEffects; }

    public Location getLocation() { return location; }
    public void setLocation(Location location) { this.location = location; }

    public Map<String, String> getCustomPdcData() { return customPdcData; }
    public void setCustomPdcData(Map<String, String> customPdcData) { this.customPdcData = customPdcData; }
}
