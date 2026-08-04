# ZenProfiles

[![Platform](https://img.shields.io/badge/Platform-Paper%20%2F%20Spigot%201.21%2B-blue.svg)](https://papermc.io)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![License](https://img.shields.io/badge/License-MIT%20with%20Attribution-green.svg)](LICENSE)

**ZenProfiles** is a high-performance, RPG profile management system built for **Paper / Spigot 1.21+ (Java 21)**, inspired by the profile architecture of **Hypixel Skyblock**.

It enables players to manage multiple distinct profiles under a single Minecraft account. Each profile operates with complete state isolation — including inventories, equipment, Ender Chest, potion effects, stats, Vault economy balance (`Purse Coins`), spawn locations, and per-profile LuckPerms permissions — while preserving global user ranks and account-wide permissions.

---

## Core Features

- **Hypixel Skyblock GUI Architecture**:
  - Main `/profile` interface displaying active profile (**Emerald Block**), inactive profiles (**Grass/Dirt Blocks**), available empty profile slots (**Dirt Block**), and locked slots (**Bedrock**).
  - **1-Click Creation**: Clicking an available slot automatically assigns a fruit name (*Cucumber, Kiwi, Peach, Mango, Papaya, etc.*), creates the profile, and performs an instant profile switch.
  - **Deletion Confirmation**: Secure `Shift + Click` confirmation menu to prevent accidental profile deletion.

- **Complete State Isolation**:
  - **Attributes & Health**: Current health, maximum health (`GENERIC_MAX_HEALTH`), food level, saturation, and exhaustion.
  - **Experience**: XP level and exact progress percentage.
  - **Inventories**: Main Inventory (36 slots), Armor (4 slots), Offhand, and Ender Chest.
  - **Effects & Gamemode**: Active potion effects and gamemode state.
  - **Spawn Teleportation**: New profiles spawn automatically at the configured server spawn location (`/profile setspawn`).

- **Per-Profile Economy (Vault Purse Coins)**:
  - Native implementation of Vault `Economy` service provider.
  - Each profile maintains its own balance. Chest shops, auctions, and economy transactions operate directly on the active profile's purse.

- **LuckPerms Integration (Per-Profile Permissions)**:
  - Implements a custom `ContextCalculator<Player>` using the context key `zenprofiles:profile=<profile_uuid>`.
  - Enables administrators to assign per-profile permission nodes without altering global user ranks or group inheritances.

- **ZEN Plugins Suite Integration**:
  - Built-in API compatibility with `ZenBank`, `ZenSacks`, `ZenFairy`, `ZenRankups`, `ZenQuests`, `ZenMilestones`, `ZenRewards`, `ZenCosmetics`, `ZenBoosters`, `ZenDungeons`, `ZenForges`, `ZenDuels`, and `ZenPvPCore` via `ZenProfilesAPI.getActiveProfileUUID(player)`.

---

## Commands & Permissions

Primary command: `/profile` (Aliases: `/profiles`, `/zprofile`, `/zprofiles`)

| Command | Description | Permission |
| :--- | :--- | :--- |
| `/profile` | Opens the Hypixel Skyblock profile management GUI. | `zenprofiles.command.profile` |
| `/profile list` | Lists all profiles owned by the player in chat. | `zenprofiles.command.profile` |
| `/profile select <name/id>` | Switches directly to the specified profile. | `zenprofiles.command.profile` |
| `/profile create <name>` | Creates a new profile with a custom name. | `zenprofiles.command.profile` |
| `/profile delete <name/id>` | Opens the deletion confirmation menu. | `zenprofiles.command.profile` |
| `/profile setspawn` | Sets the new profile spawn point at your current location. | `zenprofiles.admin` |
| `/profile reload` | Reloads all configuration files, menus, and messages. | `zenprofiles.admin` |

### Profile Limit Permissions

The default profile limit is configured via `default-max-profiles: 3` in `config.yml`. Higher limits can be granted via permission nodes:
- **`zenprofiles.max.<number>`**: Grants a specific profile slot count (e.g., `zenprofiles.max.5`).
- **`zenprofiles.max.unlimited`**: Grants unlimited profile slots.

---

## Configuration & Customization

- **`messages.yml`**: Full support for **MiniMessage** formatting (`<gradient:aqua:blue>`, `<green>`, `<bold>`) alongside legacy color codes (`&a`, `&c`).
- **`menus/profile_selector.yml`**: Fully customizable main GUI layout.
- **`menus/confirm_delete.yml`**: Customizable deletion confirmation GUI layout.

---

## Developer API & Integration

### Adding Dependency via JitPack (Maven)

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.github.cuac-xd</groupId>
        <artifactId>ZenProfiles</artifactId>
        <version>1.0.0</version>
        <scope>provided</scope>
    </dependency>
</dependencies>
```

### API Usage Example

```java
import com.cuac_xd.zenprofiles.api.ZenProfilesAPI;
import com.cuac_xd.zenprofiles.api.event.ProfileSwitchEvent;

// Get the active profile UUID for data lookups
UUID activeProfileId = ZenProfilesAPI.getActiveProfileUUID(player);

// Listen to profile switch events
@EventHandler
public void onProfileSwitch(ProfileSwitchEvent event) {
    Player player = event.getPlayer();
    UUID oldProfileId = event.getPreviousProfile().getProfileId();
    UUID newProfileId = event.getNewProfile().getProfileId();
    // Flush & load per-profile cache...
}
```

For complete integration patterns across the ZEN suite, see [IMPLEMENTATION_PROFILES.md](IMPLEMENTATION_PROFILES.md).

---

## License

This project is licensed under the **MIT License with Attribution Requirement**. You are free to use, modify, and distribute this software provided that original attribution to **cuac_xd** is preserved. Claiming original authorship or ownership is strictly prohibited. See [LICENSE](LICENSE) for details.
