# Guía de Implementación de ZenProfiles para la Suite de Plugins ZEN

Esta documentación detalla los estándares de integración y patrones de código para que todos los plugins de la suite **ZEN** (`ZenBank`, `ZenSacks`, `ZenFairy`, `ZenRankups`, `ZenQuests`, `ZenMilestones`, `ZenRewards`, `ZenCosmetics`, `ZenBoosters`, `ZenDungeons`, `ZenForges`, `ZenDuels`, `ZenPvPCore`) soporten automáticamente el sistema de perfiles independientes de **ZenProfiles**.

---

## 1. Configuración de Dependencias

En cada uno de tus plugins de la suite ZEN, añade **`ZenProfiles`** como dependencia suave (`softdepend`) en el archivo `plugin.yml`:

```yaml
softdepend: [ZenProfiles]
```

En tu `pom.xml` (o dependencia local del proyecto):

```xml
<dependency>
    <groupId>com.cuac_xd.zenprofiles</groupId>
    <artifactId>ZenProfiles</artifactId>
    <version>1.0.0</version>
    <scope>provided</scope>
</dependency>
```

---

## 2. Patrón de Obtención de UUID de Perfil

Dado que todas las APIs de la suite ZEN aceptan una `UUID` como clave primaria de almacenamiento (base de datos SQLite/MySQL o caché en RAM), basta con obtener la `UUID` del perfil activo usando **`ZenProfilesAPI.getActiveProfileUUID(...)`**:

### Método Helper Recomendado (Para incluir en cada plugin ZEN)

```java
import com.cuac_xd.zenprofiles.api.ZenProfilesAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class ProfileUtil {

    /**
     * Obtiene la UUID adecuada para almacenar datos:
     * Si ZenProfiles está activo, devuelve la UUID del perfil activo del jugador.
     * De lo contrario, devuelve la UUID de la cuenta de Minecraft.
     */
    public static UUID getTargetUUID(Player player) {
        if (player == null) return null;
        if (Bukkit.getPluginManager().isPluginEnabled("ZenProfiles")) {
            return ZenProfilesAPI.getActiveProfileUUID(player);
        }
        return player.getUniqueId();
    }

    /**
     * Versión para UUID de jugador online.
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

## 3. Escuchar Eventos de Perfil (`ProfileSwitchEvent`)

Cuando un jugador cambia de perfil mediante el comando o la GUI de **ZenProfiles**, se dispara el evento `ProfileSwitchEvent`. Cada plugin de la suite ZEN debe escuchar este evento para **guardar y descargar la caché del perfil anterior** y **cargar la data del nuevo perfil**.

### Ejemplo de Listener en tus Plugins ZEN

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

    private final MiZenPlugin plugin;

    public ProfileSwitchListener(MiZenPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onProfileSwitch(ProfileSwitchEvent event) {
        Player player = event.getPlayer();
        Profile oldProfile = event.getPreviousProfile();
        Profile newProfile = event.getNewProfile();

        // 1. Guardar y vaciar datos en caché del perfil anterior
        if (oldProfile != null) {
            UUID oldProfileId = oldProfile.getProfileId();
            plugin.getDataManager().saveAndUnloadCache(oldProfileId);
        }

        // 2. Cargar datos desde la base de datos para el nuevo perfil
        if (newProfile != null) {
            UUID newProfileId = newProfile.getProfileId();
            plugin.getDataManager().loadDataAsync(player, newProfileId);
        }
    }
}
```

---

## 4. Registro de Eventos Disponibles en ZenProfiles

Todos los eventos se encuentran en el paquete `com.cuac_xd.zenprofiles.api.event`:

| Evento | Cancellable | Descripción |
| :--- | :---: | :--- |
| **`ProfilePreSwitchEvent`** | **Sí** | Se dispara *antes* de que el jugador cambie de perfil. Permite cancelar el cambio si el jugador está en minijuegos, combates o mazmorras. |
| **`ProfileSwitchEvent`** | No | Se dispara *después* de que se ha completado el cambio de perfil y aplicado las estadísticas. |
| **`ProfileCreateEvent`** | No | Se dispara cuando un jugador crea un nuevo perfil (ej. al hacer clic en un slot de perfil disponible). |
| **`ProfileDeleteEvent`** | No | Se dispara cuando un perfil ha sido eliminado permanentemente por el jugador. |

---

## 5. Ejemplos Prácticos por Plugin de la Suite ZEN

### 🏦 Ejemplo A: ZenBank (Saldo bancario por perfil)
```java
// Al depositar o consultar el saldo bancario de un jugador
UUID profileId = ProfileUtil.getTargetUUID(player);
double saldo = zenBankAPI.getBalance(profileId);
```

### 🎒 Ejemplo B: ZenSacks (Sacos de ítems por perfil)
```java
// Al agregar ítems al saco
UUID profileId = ProfileUtil.getTargetUUID(player);
zenSacksAPI.addSackAmount(profileId, "mining", "cobblestone", 64);
```

### 🧚 Ejemplo C: ZenFairy (Almas encontradas por perfil)
```java
// Al descubrir un alma de hada
UUID profileId = ProfileUtil.getTargetUUID(player);
zenFairyAPI.addFoundSoul(profileId, "fairy", block.getLocation());
```

### 🏆 Ejemplo D: ZenRankups (Rangos y estadísticas por perfil)
```java
// Al consultar el rango o subir de nivel
UUID profileId = ProfileUtil.getTargetUUID(player);
String rankId = zenRankupsAPI.getPlayerRank(profileId);
```

---

## 6. Resumen de Integración

1. Usa `ZenProfilesAPI.getActiveProfileUUID(player)` como la clave UUID primaria en tus llamadas a base de datos o arreglos de caché.
2. Escucha `ProfileSwitchEvent` en cada plugin para sincronizar el intercambio de perfiles en memoria.
3. Tus plugins funcionarán tanto de forma independiente como acoplados al 100% con **ZenProfiles** sin modificar la lógica interna de almacenamiento.
