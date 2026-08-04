package com.cuac_xd.zenprofiles.storage;

import com.cuac_xd.zenprofiles.model.Profile;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Storage contract interface defining asynchronous persistence operations for player profiles.
 *
 * @author cuac_xd
 */
public interface ProfileStorage {

    /**
     * Initializes the storage engine.
     *
     * @return CompletableFuture completing upon successful initialization.
     */
    CompletableFuture<Void> init();

    /**
     * Asynchronously loads all profiles for a player UUID.
     *
     * @param playerUuid The player account UUID.
     * @return CompletableFuture containing the list of loaded profiles.
     */
    CompletableFuture<List<Profile>> loadProfiles(UUID playerUuid);

    /**
     * Asynchronously saves a profile.
     *
     * @param profile The Profile object to save.
     * @return CompletableFuture completing when saved.
     */
    CompletableFuture<Void> saveProfile(Profile profile);

    /**
     * Asynchronously deletes a profile.
     *
     * @param playerUuid The player account UUID.
     * @param profileId The profile UUID to delete.
     * @return CompletableFuture completing when deleted.
     */
    CompletableFuture<Void> deleteProfile(UUID playerUuid, UUID profileId);

    /**
     * Closes the storage engine.
     *
     * @return CompletableFuture completing upon clean shutdown.
     */
    CompletableFuture<Void> close();
}
