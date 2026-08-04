package com.cuac_xd.zenprofiles.api.event;

import com.cuac_xd.zenprofiles.model.Profile;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class ProfileSwitchEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();
    private final Player player;
    private final Profile previousProfile;
    private final Profile newProfile;

    public ProfileSwitchEvent(Player player, Profile previousProfile, Profile newProfile) {
        this.player = player;
        this.previousProfile = previousProfile;
        this.newProfile = newProfile;
    }

    public Player getPlayer() { return player; }
    public Profile getPreviousProfile() { return previousProfile; }
    public Profile getNewProfile() { return newProfile; }

    @NotNull
    @Override
    public HandlerList getHandlers() { return HANDLERS; }

    public static HandlerList getHandlerList() { return HANDLERS; }
}
