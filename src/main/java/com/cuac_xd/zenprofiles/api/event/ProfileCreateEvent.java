package com.cuac_xd.zenprofiles.api.event;

import com.cuac_xd.zenprofiles.model.Profile;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Event fired when a player creates a new profile.
 *
 * @author cuac_xd
 */
public class ProfileCreateEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final UUID playerUuid;
    private final Profile createdProfile;

    public ProfileCreateEvent(UUID playerUuid, Profile createdProfile) {
        this.playerUuid = playerUuid;
        this.createdProfile = createdProfile;
    }

    public UUID getPlayerUuid() { return playerUuid; }
    public Profile getCreatedProfile() { return createdProfile; }

    @Override
    public @NotNull HandlerList getHandlers() { return HANDLERS; }
    public static HandlerList getHandlerList() { return HANDLERS; }
}
