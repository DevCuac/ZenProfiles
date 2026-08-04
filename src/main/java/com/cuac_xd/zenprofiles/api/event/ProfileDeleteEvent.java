package com.cuac_xd.zenprofiles.api.event;

import com.cuac_xd.zenprofiles.model.Profile;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Event fired when a player profile is permanently deleted.
 *
 * @author cuac_xd
 */
public class ProfileDeleteEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final UUID playerUuid;
    private final Profile deletedProfile;

    public ProfileDeleteEvent(UUID playerUuid, Profile deletedProfile) {
        this.playerUuid = playerUuid;
        this.deletedProfile = deletedProfile;
    }

    public UUID getPlayerUuid() { return playerUuid; }
    public Profile getDeletedProfile() { return deletedProfile; }

    @Override
    public @NotNull HandlerList getHandlers() { return HANDLERS; }
    public static HandlerList getHandlerList() { return HANDLERS; }
}
