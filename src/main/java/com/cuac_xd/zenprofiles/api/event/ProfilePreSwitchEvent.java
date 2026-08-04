package com.cuac_xd.zenprofiles.api.event;

import com.cuac_xd.zenprofiles.model.Profile;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Cancellable event fired before a player switches profiles.
 * Allows third-party plugins to cancel profile switching (e.g. during active minigames or dungeons).
 *
 * @author cuac_xd
 */
public class ProfilePreSwitchEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final Profile previousProfile;
    private final Profile targetProfile;
    private boolean cancelled = false;

    public ProfilePreSwitchEvent(Player player, Profile previousProfile, Profile targetProfile) {
        this.player = player;
        this.previousProfile = previousProfile;
        this.targetProfile = targetProfile;
    }

    public Player getPlayer() { return player; }
    public Profile getPreviousProfile() { return previousProfile; }
    public Profile getTargetProfile() { return targetProfile; }

    @Override
    public boolean isCancelled() { return cancelled; }

    @Override
    public void setCancelled(boolean cancel) { this.cancelled = cancel; }

    @Override
    public @NotNull HandlerList getHandlers() { return HANDLERS; }
    public static HandlerList getHandlerList() { return HANDLERS; }
}
