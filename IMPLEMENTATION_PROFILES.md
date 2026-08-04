# ZenProfiles Integration Guide for the ZEN Plugins Suite

This documentation details the integration standards, code patterns, and best practices for all plugins in the **ZEN** suite (`ZenBank`, `ZenSacks`, `ZenFairy`, `ZenRankups`, `ZenQuests`, `ZenMilestones`, `ZenRewards`, `ZenCosmetics`, `ZenBoosters`, `ZenDungeons`, `ZenForges`, `ZenDuels`, `ZenPvPCore`) to natively support **ZenProfiles** per-profile data isolation.

---

## 1. Dependency Configuration

In each plugin within the ZEN suite, declare **`ZenProfiles`** as a soft dependency in `plugin.yml`:

```yaml
softdepend: [ZenProfiles]
```

In your `pom.xml` (or local dependency manifest):

```xml
<dependency>
    <groupId>com.cuac_xd.zenprofiles</groupId>
    <artifactId>ZenProfiles</artifactId>
    <version>1.0.0</version>
    <scope>provided</scope>
</dependency>
```

---

## 2. Profile UUID Retrieval Pattern

Since all ZEN suite APIs accept a `UUID` as their primary storage key (MySQL/SQLite or in-memory RAM cache), simply retrieve the active profile's `UUID` using **`ZenProfilesAPI.getActiveProfileUUID(...)`**:

### Recommended Helper Class (Include in each ZEN plugin)

```java
import com.cuac_xd.zenprofiles.api.ZenProfilesAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class ProfileUtil {

    /**
     * Resolves the target UUID for data storage:
     * Returns the active profile UUID if ZenProfiles is enabled;
     * otherwise falls back to the player's account UUID.
     */
    public static UUID getTargetUUID(Player player) {
        if (player == null) return null;
        if (Bukkit.getPluginManager().isPluginEnabled("ZenProfiles")) {
            return ZenProfilesAPI.getActiveProfileUUID(player);
        }
        return player.getUniqueId();
    }

    /**
     * Overload for online player UUID resolution.
     */
    public static UUID getTargetUUID(UUID playerUuid) {
        if (playerUuid == null) return null;
        if (Bukkit.getPluginManager().isPluginEnabled("ZenProfiles")) {
            return ZenProfilesAPI.getActiveProfileUUID(playerUuid);
        }
        return playerUuid;
    }
}
```

---

## 3. Handling Profile Switch Events (`ProfileSwitchEvent`)

When a player switches profiles via `/profile` command or GUI, **ZenProfiles** fires `ProfileSwitchEvent`. Each ZEN suite plugin should listen to this event to **save and unload the cache for the previous profile** and **load the target profile's data**.

### Example Event Listener

```java
package com.cuac_xd.zenplugin.listeners;

import com.cuac_xd.zenprofiles.api.event.ProfileSwitchEvent;
import com.cuac_xd.zenprofiles.model.Profile;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.UUID;

public class ProfileSwitchListener implements Listener {

    private final MyZenPlugin plugin;

    public ProfileSwitchListener(MyZenPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onProfileSwitch(ProfileSwitchEvent event) {
        Player player = event.getPlayer();
        Profile oldProfile = event.getPreviousProfile();
        Profile newProfile = event.getNewProfile();

        // 1. Flush and unload in-memory cache for previous profile
        if (oldProfile != null) {
            UUID oldProfileId = oldProfile.getProfileId();
            plugin.getDataManager().saveAndUnloadCache(oldProfileId);
        }

        // 2. Asynchronously load database contents for target profile
        if (newProfile != null) {
            UUID newProfileId = newProfile.getProfileId();
            plugin.getDataManager().loadDataAsync(player, newProfileId);
        }
    }
}
```

---

## 4. ZenProfiles Event Registry

All custom events are located in `com.cuac_xd.zenprofiles.api.event`:

| Event | Cancellable | Description |
| :--- | :---: | :--- |
| **`ProfilePreSwitchEvent`** | **Yes** | Fired *before* a profile switch occurs. Allows cancelling switches during combat, dungeons, or active minigames. |
| **`ProfileSwitchEvent`** | No | Fired *after* a profile switch is completed and attributes are applied. |
| **`ProfileCreateEvent`** | No | Fired when a new profile is created. |
| **`ProfileDeleteEvent`** | No | Fired when a profile is permanently deleted. |

---

## 5. Code Examples by ZEN Suite Plugin

### ZenBank (Per-Profile Bank Balance)
```java
// Deposit or query bank balance
UUID profileId = ProfileUtil.getTargetUUID(player);
double balance = zenBankAPI.getBalance(profileId);
```

### ZenSacks (Per-Profile Item Sacks)
```java
// Insert items into sack
UUID profileId = ProfileUtil.getTargetUUID(player);
zenSacksAPI.addSackAmount(profileId, "mining", "cobblestone", 64);
```

### ZenFairy (Per-Profile Fairy Souls)
```java
// Mark discovered fairy soul
UUID profileId = ProfileUtil.getTargetUUID(player);
zenFairyAPI.addFoundSoul(profileId, "fairy", block.getLocation());
```

### ZenRankups (Per-Profile Ranks & Stats)
```java
// Query rank or execute rankup
UUID profileId = ProfileUtil.getTargetUUID(player);
String rankId = zenRankupsAPI.getPlayerRank(profileId);
```

---

## 6. Summary

1. Always use `ZenProfilesAPI.getActiveProfileUUID(player)` as the primary UUID key in database queries or cache maps.
2. Listen to `ProfileSwitchEvent` across each plugin to handle in-memory cache transitions.
3. Plugins will operate seamlessly both standalone and integrated with **ZenProfiles** without internal storage modifications.
