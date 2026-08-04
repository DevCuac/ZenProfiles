# 🍉 ZenProfiles

**ZenProfiles** es un plugin avanzado de gestión de perfiles de personaje para **Spigot / Paper 1.21+ (Java 21)**, diseñado para servidores RPG y de Supervivencia e inspirado en el sistema de perfiles de **Hypixel Skyblock**.

Permite a los jugadores gestionar múltiples perfiles independientes bajo una misma cuenta de Minecraft, aislando estadísticas, inventario, equipamiento, Ender Chest, efectos de poción, ubicación, economía por perfil (`Purse Coins` de Vault) y permisos específicos de LuckPerms, mientras se conservan los rangos y grupos globales a nivel de usuario.

---

## ✨ Características Principales

- **🎮 Interfaz Interactiva Réplica Hypixel Skyblock**:
  - Menú `/profile` con visualización clara de perfiles activos (**Bloques de Esmeralda**), perfiles inactivos (**Bloques de Pasto/Tierra**), slots vacíos disponibles (**Bloques de Tierra**) y slots bloqueados (**Bedrock**).
  - **Creación en 1-Clic**: Al hacer clic en un slot de perfil vacío, el plugin asigna automáticamente un nombre de fruta estilo Hypixel (*Cucumber, Kiwi, Peach, Mango, Papaya, etc.*), crea el perfil y cambia al personaje de forma instantánea.
  - **Confirmación de Eliminación**: Menú de seguridad para borrar perfiles con `Shift + Clic`.

- **🔄 Aislamiento Total de Estado por Perfil**:
  - **Atributos y Salud**: Vida actual, vida máxima (atributo `GENERIC_MAX_HEALTH`), nivel de comida, saturación y agotamiento.
  - **Experiencia**: Nivel de XP y progreso de experiencia.
  - **Inventario Completo**: Inventario principal (36 slots), Armadura (4 slots), Mano Secundaria (Offhand) y Ender Chest.
  - **Efectos y Gamemode**: Efectos de poción activos y modo de juego (Survival, Creative, etc.).
  - **Ubicación e Inicio en Spawn**: Los nuevos perfiles aparecen automáticamente en el Spawn configurado (`/profile setspawn`).

- **💰 Economía Vault por Perfil (`Purse Coins`)**:
  - Integración nativa como proveedor de `net.milkbowl.vault.economy.Economy`.
  - Cada perfil mantiene su propio dinero. Tiendas, subastas, comandos de dinero y depósitos operan sobre el dinero del **perfil activo**.

- **🔑 Integración con LuckPerms (Permisos por Perfil)**:
  - Registro de un `ContextCalculator<Player>` con la clave `zenprofiles:profile=<profile_uuid>`.
  - Permite asignar permisos específicos a un perfil concreto manteniendo los rangos/grupos del jugador a nivel de cuenta.

- **🌐 Integración Nativa con la Suite de Plugins ZEN**:
  - Diseñado para vincularse de forma transparente con `ZenBank`, `ZenSacks`, `ZenFairy`, `ZenRankups`, `ZenQuests`, `ZenMilestones`, `ZenRewards`, `ZenCosmetics`, `ZenBoosters`, `ZenDungeons`, `ZenForges`, `ZenDuels` y `ZenPvPCore` utilizando `ZenProfilesAPI.getActiveProfileUUID(player)`.

---

## 📜 Comandos y Permisos

### Comandos principales: `/profile` (Aliases: `/profiles`, `/zprofile`, `/zprofiles`)

| Comando | Descripción | Permiso |
| :--- | :--- | :--- |
| `/profile` | Abre la interfaz de gestión de perfiles estilo Hypixel Skyblock. | `zenprofiles.command.profile` |
| `/profile list` | Muestra la lista de perfiles del jugador en el chat. | `zenprofiles.command.profile` |
| `/profile select <nombre|id>` | Cambia directamente al perfil especificado. | `zenprofiles.command.profile` |
| `/profile create <nombre>` | Crea un nuevo perfil con un nombre personalizado. | `zenprofiles.command.profile` |
| `/profile delete <nombre|id>` | Abre el menú de confirmación para eliminar un perfil. | `zenprofiles.command.profile` |
| `/profile setspawn` | Establece el punto de aparición de nuevos perfiles en tu ubicación actual. | `zenprofiles.admin` |
| `/profile reload` | Recarga la configuración, menús y mensajes. | `zenprofiles.admin` |

### Permisos de Límite de Perfiles

Por defecto, el número máximo de perfiles por jugador está definido por `default-max-profiles: 3` en `config.yml`. Puedes otorgar límites personalizados mediante nodos de permiso:
- **`zenprofiles.max.<numero>`**: Otorga un número específico de slots de perfil (ej. `zenprofiles.max.5`).
- **`zenprofiles.max.unlimited`**: Otorga slots de perfiles ilimitados.

---

## 🎨 Personalización (MiniMessage & Menús)

- **`messages.yml`**: Todos los mensajes del plugin admiten formato nativo de **MiniMessage** (`<gradient:aqua:blue>`, `<green>`, `<bold>`, etc.) y códigos de color tradicionales (`&a`, `&c`).
- **`menus/profile_selector.yml`**: Configuración totalmente editable de la GUI principal.
- **`menus/confirm_delete.yml`**: Configuración del menú de confirmación de borrado.

---

## 💻 Desarrolladores & API (JitPack)

### Añadir la API a tu Proyecto (Maven)

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

### Uso de la API en Código

```java
import com.cuac_xd.zenprofiles.api.ZenProfilesAPI;
import com.cuac_xd.zenprofiles.api.event.ProfileSwitchEvent;

// Obtener la UUID del perfil activo del jugador
UUID profileId = ZenProfilesAPI.getActiveProfileUUID(player);

// Escuchar el evento de cambio de perfil
@EventHandler
public void onProfileSwitch(ProfileSwitchEvent event) {
    Player player = event.getPlayer();
    UUID oldProfileId = event.getPreviousProfile().getProfileId();
    UUID newProfileId = event.getNewProfile().getProfileId();
    // Guardar y cargar datos por perfil...
}
```

Para más detalles sobre la integración con la suite ZEN, consulta el documento [IMPLEMENTATION_PROFILES.md](IMPLEMENTATION_PROFILES.md).

---

## 📄 Licencia

Este proyecto está bajo la licencia **MIT with Attribution Requirement**. Eres libre de usar, modificar y distribuir este software siempre que mantengas la atribución de autoría a **cuac_xd**. No está permitido atribuirse la autoría original del código. Consulta el archivo [LICENSE](LICENSE) para más detalles.
