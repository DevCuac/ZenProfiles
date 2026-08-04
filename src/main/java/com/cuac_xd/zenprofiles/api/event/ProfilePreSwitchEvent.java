package com.cuac_xd.zenprofiles.api.event;

import com.cuac_xd.zenprofiles.model.Profile;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class ProfilePreSwitchEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();
    private final Player player;
    private final Profile currentProfile;
    private final Profile targetProfile;
    private boolean cancelled = false;

    public ProfilePreSwitchEvent(Player player, Profile currentProfile, Profile targetProfile) {
        this.player = player;
        this.currentProfile = currentProfile;
        this.targetProfile = targetProfile;
    }

    public Player getPlayer() { return player; }
    public Profile getCurrentProfile() { return currentProfile; }
    public Profile getTargetProfile() { return targetProfile; }

    @Override
    public boolean isCancelled() { return cancelled; }

    @Override
    public void setCancelled(boolean cancel) { this.cancelled = cancel; }

    @NotNull
    @Override
    public HandlerList getHandlers() { return HANDLERS; }

    public static HandlerList getHandlerList() { return HANDLERS; }
}
