package com.cuac_xd.zenprofiles.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

/**
 * Abstract InventoryHolder marker used to uniquely identify all ZenProfiles GUI menus.
 * Implements strict inventory ownership tagging to prevent item duplication exploits
 * and unauthorized inventory interactions.
 *
 * @author cuac_xd
 */
public class ZenHolder implements InventoryHolder {

    private final String menuId;
    private Inventory inventory;

    /**
     * Constructs a new ZenHolder instance with a specific menu identifier.
     *
     * @param menuId The unique identifier for the GUI menu type.
     */
    public ZenHolder(String menuId) {
        this.menuId = menuId;
    }

    /**
     * Gets the unique menu identifier associated with this inventory holder.
     *
     * @return The menu identifier string.
     */
    public String getMenuId() {
        return menuId;
    }

    /**
     * Binds the Bukkit Inventory instance to this holder.
     *
     * @param inventory The created Bukkit Inventory.
     */
    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}
