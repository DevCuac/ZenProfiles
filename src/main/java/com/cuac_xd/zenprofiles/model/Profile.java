package com.cuac_xd.zenprofiles.model;

import java.util.UUID;

/**
 * Represents a player profile.
 */
public class Profile {

    private final UUID profileId;
    private final UUID playerUuid;
    private String name;
    private final long createdAt;
    private long lastPlayed;
    private ProfileData data;

    public Profile(UUID profileId, UUID playerUuid, String name, long createdAt, long lastPlayed, ProfileData data) {
        this.profileId = profileId;
        this.playerUuid = playerUuid;
        this.name = name;
        this.createdAt = createdAt;
        this.lastPlayed = lastPlayed;
        this.data = data != null ? data : new ProfileData();
    }

    public Profile(UUID playerUuid, String name) {
        this(UUID.randomUUID(), playerUuid, name, System.currentTimeMillis(), System.currentTimeMillis(), new ProfileData());
    }

    public UUID getProfileId() { return profileId; }
    public UUID getPlayerUuid() { return playerUuid; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public long getCreatedAt() { return createdAt; }
    public long getLastPlayed() { return lastPlayed; }
    public void setLastPlayed(long lastPlayed) { this.lastPlayed = lastPlayed; }
    public ProfileData getData() { return data; }
    public void setData(ProfileData data) { this.data = data; }
}
