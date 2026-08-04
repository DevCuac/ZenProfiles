package com.cuac_xd.zenprofiles.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * Domain model representing a single player profile instance.
 * Encapsulates profile metadata (UUID, account UUID, fruit name, creation/last played timestamps)
 * and holds the associated ProfileData payload.
 *
 * @author cuac_xd
 */
public class Profile {

    private final UUID profileId;
    private final UUID playerUuid;
    private String name;
    private final long createdAt;
    private long lastPlayed;
    private ProfileData data;

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    public Profile(UUID profileId, UUID playerUuid, String name) {
        this.profileId = profileId;
        this.playerUuid = playerUuid;
        this.name = name;
        this.createdAt = System.currentTimeMillis();
        this.lastPlayed = System.currentTimeMillis();
        this.data = new ProfileData();
    }

    public Profile(UUID profileId, UUID playerUuid, String name, long createdAt, long lastPlayed, ProfileData data) {
        this.profileId = profileId;
        this.playerUuid = playerUuid;
        this.name = name;
        this.createdAt = createdAt;
        this.lastPlayed = lastPlayed;
        this.data = data != null ? data : new ProfileData();
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

    public String getCreatedAtFormatted() {
        return DATE_FORMAT.format(new Date(createdAt));
    }

    public String getLastPlayedFormatted() {
        return DATE_FORMAT.format(new Date(lastPlayed));
    }
}
