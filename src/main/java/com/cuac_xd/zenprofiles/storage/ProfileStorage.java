package com.cuac_xd.zenprofiles.storage;

import com.cuac_xd.zenprofiles.model.Profile;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface ProfileStorage {

    CompletableFuture<Void> init();

    CompletableFuture<List<Profile>> loadProfiles(UUID playerUuid);

    CompletableFuture<Void> saveProfile(Profile profile);

    CompletableFuture<Void> deleteProfile(UUID profileId);

    CompletableFuture<Void> close();
}
