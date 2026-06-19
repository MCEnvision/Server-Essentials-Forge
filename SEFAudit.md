# Phase 0 Audit — Server Essentials Forge: Forge 1.20.1 → NeoForge 1.21.1

**Status:** Audit only. No code written. Awaiting review before Phase 1.
**Method:** 13 parallel source-readers over the real `SourceCodeOld/Server-Essentials-Forge` tree (file:line cited), an adversarial web-verify pass over uncertain/high-risk mappings (11 corrections applied), and a completeness critic. ~143 findings: 7 CRITICAL, 26 HIGH, 34 MEDIUM, 76 LOW.

---

## 0. Scope & ground truth

- **Source:** package `com.enviouse.sef`, mod id `sef`, **111 Java files / ~12.5k LOC**, Forge 47.3.12 / MC 1.20.1, Java 17, ForgeGradle 6 + mixingradle.
- **Target:** NeoForge 21.1.x / MC 1.21.1, Java 21, ModDevGradle 2.0.141, Mojang runtime mappings (the skeleton already pins these).
- **Heaviest subsystem:** the **vanish module** — 28 mixins + 3 access transformers + a custom `PlayerVanishEvent` + direct vanilla packet manipulation.
- **The `OLD1201Version/` path in the brief does not exist** — the real source is `SourceCodeOld/Server-Essentials-Forge/`. The skeleton (`com.enviouse.sefported`) is still the **stock MDK example** (blocks/items/creative tabs/client-setup) and must be gutted.

### Confirmed CLEAN (no work needed) — verified by grep across the whole tree
| Axis | Result |
|---|---|
| `new ResourceLocation(...)` (removed in 1.21) | **Zero occurrences** — code already uses `ResourceLocation.tryParse` (`banned/BannedEntry.java:92,108`) |
| Custom networking (SimpleChannel/payloads) | **Truly none.** Click-to-reply is pure `ClickEvent`/`HoverEvent`; vanish uses vanilla `connection.send(...)` only |
| Capabilities / `LazyOptional` | None |
| ItemStack NBT / DataComponent disk serialization (1.20.5 datafixer risk) | None — banned items persist RL strings via Gson, invsee operates on live containers |
| Client-only classes in common code | None — dedicated-server safe; invsee uses vanilla `MenuType.GENERIC_9x6` server-side |
| `Component.Serializer` JSON (needs RegistryAccess in 1.20.5+) | Not used — formatting is all `Style`/`ChatFormatting`/`TextColor`, stable in 1.21.1 |
| Threads/executors | Only `server.execute(Runnable)` (unchanged) |

---

## 1. DECISIONS NEEDED BEFORE ANY CODE MOVES

These gate everything and are product calls, not mechanical:

1. **Mod id / package: keep `sef` + `com.enviouse.sef`, or adopt skeleton's `sefported` + `com.enviouse.sefported`?**
   **Recommendation: KEEP `sef` and `com.enviouse.sef`.** It preserves the config namespace (`config/sef/common.toml`), the ~76 `sef.*` permission nodes, the `/sef` command tree, world data dirs (`<world>/serverconfig/sef/*.json`), and avoids touching 111 files' package declarations. Rename the skeleton's `mod_id`/group/package/`<modid>.mixins.json` to `sef` instead (a handful of edits). This is the lower-risk, behavior-preserving choice you asked for. (The mod would still be *named* SEFPORTED in display metadata if you like, but the **id** should stay `sef`.)

2. **Confirm target stays exactly MC 1.21.1** (not 1.21.5+). `ClickEvent`/`HoverEvent` are plain constructors in 1.21.1 but became **records in 1.21.5** — drifting the target later would re-break `chat`/formatting code. Audit assumes **1.21.1**.

3. **`displayTest` value (server-side-only marker).** See §2; recommended **`IGNORE_SERVER_VERSION`**.

---

## 2. Build / project / entrypoint / metadata

| Surface | file:line | NeoForge 1.21.1 replacement | Risk |
|---|---|---|---|
| ForgeGradle 6 + mixingradle, `minecraft{}`, `reobfJar`, `fg.deobf` | `build.gradle:1-19,32-46,153,170,218` | Adopt skeleton's `net.neoforged.moddev` 2.0.141 + `neoForge{}`. No reobf, no `fg.deobf` (Mojang-mapped deps). | MED |
| Mixin via spongepowered gradle plugin + `mixin{}` + refmap `sef.refmap.json` + mixinextras compileOnly | `build.gradle:10,19,156,244-253`, `sef.mixins.json:6` | **No mixingradle, no refmap.** Declare mixins only in `neoforge.mods.toml [[mixins]]`. Drop `refmap`+`mixinextras` keys from the json. ModDev supplies the AP; MixinExtras bundled in NeoForge. `compatibilityLevel: JAVA_17→JAVA_21`. | **CRIT** |
| `@Mod` no-arg ctor + `FMLJavaModLoadingContext.get().getModEventBus()` + `ModLoadingContext.get()` | `ServerEssentialsForge.java:37-85,76,82` | `public ServerEssentialsForge(IEventBus modEventBus, ModContainer modContainer)` (FML injection). Use `modEventBus` for mod-bus listeners; `modContainer.registerConfig(...)`. | HIGH |
| `utils/loader.java`: `IExtensionPoint.DisplayTest` + `NetworkConstants.IGNORESERVERONLY` + `registerExtensionPoint` + `ModLoadingContext.registerConfig` + `MinecraftForge.EVENT_BUS.register` | `utils/loader.java:1-32`, `ServerEssentialsForge.java:71-72` | **Delete the DisplayTest/NetworkConstants path** (both removed; `IExtensionPoint` is now an empty marker). Server-only behavior becomes **`displayTest = "IGNORE_SERVER_VERSION"`** in `neoforge.mods.toml [[mods]]`. `loader.register(o)` → `NeoForge.EVENT_BUS.register(o)`. `MLConfig` → `modContainer.registerConfig(ModConfig.Type.COMMON, ConfigHandler.spec, "sef/common.toml")`. | HIGH |
| Entry-point event imports: `MinecraftForge.EVENT_BUS`, `eventbus.api.SubscribeEvent`, `TickEvent.ServerTickEvent` w/ `phase==END`, `ServerStarted/StoppingEvent`, `RegisterCommandsEvent`, `ModList`, `FMLPaths`, `FMLLoadCompleteEvent` | `ServerEssentialsForge.java:26-33,93-205` | `NeoForge.EVENT_BUS`; `net.neoforged.bus.api.SubscribeEvent`; **`net.neoforged.neoforge.event.tick.ServerTickEvent.Post`** (drop the `phase==END` check); `net.neoforged.neoforge.event.server.*`; `net.neoforged.neoforge.event.RegisterCommandsEvent`; `net.neoforged.fml.ModList`; `net.neoforged.fml.loading.FMLPaths`; `net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent`. | HIGH |
| Mod deps via `fg.deobf` + `flatDir lib/` (ftb 2001.2.2, luckperms api 5.4, curios-forge 5.14.1+1.20.1) | `build.gradle:130-145,167-170` | Plain `compileOnly` (no remap). **LuckPerms `net.luckperms:api:5.4` unchanged.** **FTB `dev.ftb.mods:ftb-essentials-neoforge:2101.1.x`** (+ `ftb-library-neoforge`, maven `maven.ftb.dev/releases`). **Curios `top.theillusivec4.curios:curios-neoforge:9.5.1+1.21.1:api`** (maven `maven.theillusivec4.top`). Old 1.20.1 jars unusable. | HIGH |
| `parchment_minecraft_version=1.21.11`, `parchment_mappings_version=2025.12.20` | `gradle.properties:21-22` | **Neither exists.** Use `parchment_minecraft_version=1.21.1` + `parchment_mappings_version=2024.11.17` (final published for 1.21.1). | MED |
| Stale Forge `mods.toml` in resources (`modLoader=javafml`, `loaderVersion=[47,)`) | `src/main/resources/META-INF/mods.toml:10,18,22` | Do **not** carry over; the new tree uses `neoforge.mods.toml`. Don't ship both. | LOW |
| `pack.mcmeta` `pack_format: 10` | `pack.mcmeta:4` | `34` for 1.21.1 (the source value was stale even for 1.20.1). | LOW |

---

## 3. Registration & event bus

| Surface | file:line | Replacement | Risk |
|---|---|---|---|
| `@Mod.EventBusSubscriber` (12 classes) | `config/PermissionsHandler.java:20`, `config/ConfigurationEventHandler.java:10`, `disablebuilding/...:18`, `banned/...:31`, `events/PlayerEventHandler.java:30`, `events/ExternalModLoadingEvent.java:15`, `vanish/VanishEventListener.java:32`, `events/ChatEventHandler.java:34`, `events/CommandRegistrationHandler.java:37`, `invlock/...:20`, `freeze/...:24` | `net.neoforged.fml.common.EventBusSubscriber` (top-level). `Bus.FORGE→Bus.GAME`. **Caveat:** annotation auto-registers **static** handlers; several of these classes use non-static instance handlers and are *also* manually registered via `loader.register` — see chat/lifecycle notes. | MED |
| `MinecraftForge.EVENT_BUS` (register/addListener) | `utils/loader.java:15`, `ServerEssentialsForge.java:79-80` | `net.neoforged.neoforge.common.NeoForge.EVENT_BUS` | LOW |
| `ForgeRegistries.ITEMS/BLOCKS` `.getKey()/.getKeys()` (9 sites) | `banned/BannedItemsManager.java:247,259,267,467,483,519,520`, `banned/BannedItemsEventHandler.java:57`, `banned/BannedItemsCommands.java:303` | `BuiltInRegistries.ITEM/BLOCK` (`net.minecraft.core.registries`). `.getKeys()`→`.keySet()`. Note `getKey()` returns `minecraft:air`/`minecraft:` key, never null. | MED |
| No `DeferredRegister`/`RegistryObject`/`@ObjectHolder` | — | None present (confirmed). | — |

---

## 4. Events — chat (the core hook)

| Surface | file:line | Replacement | Risk |
|---|---|---|---|
| `net.minecraftforge.event.ServerChatEvent`, `onServerChat(ServerChatEvent)` | `events/ChatEventHandler.java:30,72` | `net.neoforged.neoforge.event.ServerChatEvent`. Confirm message accessor is `getMessage()` (Component) — used at `:80`. Single load-bearing chat hook. | HIGH |
| `ChatEventHandler` is `@EventBusSubscriber` but `onServerChat` is a **non-static instance** method holding `IReloadable` state | `events/ChatEventHandler.java:34,72` | Must be registered as an **instance** on `NeoForge.EVENT_BUS` (annotation only wires statics). Confirm the registration site survives the port or handler silently stops firing. | HIGH |
| Chat managers (reply/threading/admin/op-bulletin), `server.execute`, `ClickEvent`/`HoverEvent` build, sounds | `chat/ChatMessageManager.java`, `chat/ChatReplyHandler.java`, `chat/AdminChatHandler.java`, `chat/OpBulletinHandler.java` | Mostly vanilla, stable. `OpBulletinHandler.java:33` `getServerDirectory().toPath()` → see §13 (now returns `Path`). | MED |

---

## 5. Events — player / lifecycle / tick

| Surface | file:line | Replacement | Risk |
|---|---|---|---|
| `TickEvent.ServerTickEvent`/`PlayerTickEvent` + `phase==END` (3 sites) | `ServerEssentialsForge.java:171-172`, `events/PlayerEventHandler.java:119-120`, `vanish/VanishEventListener.java:72-73` | `net.neoforged.neoforge.event.tick.ServerTickEvent.Post` / `PlayerTickEvent.Post` (no `.phase`). | HIGH |
| `PlayerEvent.TabListNameFormat` (tab rank display), `PlayerEvent.NameFormat` (nickname display) | `events/PlayerEventHandler.java:16,17,55,61`, `vanish/VanishEventListener.java:18` | **Both still exist** in NeoForge 1.21.1 under `net.neoforged.neoforge.event.entity.player.PlayerEvent.*` (CONFIRMED). Setter takes a `Component`. | MED |
| `PlayerEvent.PlayerLoggedIn/OutEvent`, `SaveToFile`/`LoadFromFile` | `events/PlayerEventHandler.java:14,15,74,89` | `net.neoforged.neoforge.event.entity.player.PlayerEvent.*` (same leaf names). | LOW |
| `ServerStarted/Stopping/StoppedEvent` | `ServerEssentialsForge.java:117,191`, `events/...`, `vanish/...` | `net.neoforged.neoforge.event.server.*` | LOW |

---

## 6. Events — gameplay (game bus)

| Surface | file:line | Replacement | Risk |
|---|---|---|---|
| **`EntityItemPickupEvent`** | `banned/BannedItemsEventHandler.java:16,129-141`, `invlock/InvLockEventHandler.java:6,39-46` | **`net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent.Pre`** (game bus). `getEntity()→getPlayer()`, `getItem()→getItemEntity()`. **Not cancelable** — deny via `event.setCanPickup(net.neoforged.neoforge.common.util.TriState.FALSE)` (not `setCanceled`). | HIGH |
| `BlockEvent.BreakEvent` / `EntityPlaceEvent` | `banned/...`, `disablebuilding/...`, `freeze/...` | `net.neoforged.neoforge.event.level.BlockEvent.*` (same leaf). Confirm `getPlayer()` return type for `instanceof` patterns. | MED |
| `PlayerInteractEvent.{RightClickBlock,EntityInteract}`, `AttackEntityEvent`, `PlayerContainerEvent`, `LivingEntityUseItemEvent`, `ProjectileImpactEvent`, `LivingChangeTargetEvent`, `VanillaGameEvent`, `CommandEvent` | freeze/invlock/banned/disablebuilding handlers | `net.neoforged.neoforge.event.*` (package swap). Confirm `setCanceled` is exposed directly (via `ICancellableEvent`). | MED |

---

## 7. Commands (Brigadier)

| Surface | file:line | Replacement | Risk |
|---|---|---|---|
| `RegisterCommandsEvent` (+ low-priority override of vanilla `/msg`,`/tell`,`/w` and FTB `/invsee`) | `events/CommandRegistrationHandler.java:33,61,153`, `ServerEssentialsForge.java:93` | `net.neoforged.neoforge.event.RegisterCommandsEvent` (`getDispatcher()`/`getBuildContext()`/`getCommandSelection()`). Confirm priority-ordered listeners still honored. | MED |
| Permission gating inside `.requires()`/`executes()` via `PermissionAPI`/`PermissionNode` | all `*Command.java` + `PermissionsHandler` | See §8. | HIGH |
| Brigadier argument types (`EntityArgument`, `StringArgumentType`, etc.) | commands/* | Vanilla, stable. No `ItemArgument`/`ResourceArgument` needing `CommandBuildContext` found. | LOW |

---

## 8. Permissions

| Surface | file:line | Replacement | Risk |
|---|---|---|---|
| `new PermissionNode<>(MODID, id, PermissionTypes.BOOLEAN, resolver)` (~76 nodes) | `config/PermissionsHandler.java:198-199`, `ServerEssentialsForge.java:101,108` | `net.neoforged.neoforge.server.permission.nodes.PermissionNode<T>(String modId, String nodeName, PermissionType<T>, PermissionResolver<T>, PermissionDynamicContextKey<?>...)` — 4-arg call valid. `PermissionTypes.BOOLEAN` in `...permission.nodes`. | HIGH |
| `PermissionGatherEvent.Nodes` + `addNodes(...)` | `config/PermissionsHandler.java:181`, `ServerEssentialsForge.java:98-111` | `net.neoforged.neoforge.server.permission.events.PermissionGatherEvent.Nodes` (note `.events` package, **not** `.nodes`). **Open Q:** which bus it fires on (likely GAME). | HIGH |
| `PermissionAPI.getOfflinePermission(UUID, PermissionNode<Boolean>)` (basis of ~all perm checks) | `config/PermissionsHandler.java:207` | `net.neoforged.neoforge.server.permission.PermissionAPI.getOfflinePermission(...)` — retained, same signature. Keep the `catch(IllegalStateException)` for pre-init queries. | HIGH |
| `PermissionNode.setInformation(Component, Component)` | `config/PermissionsHandler.java` | Confirm still present (Open Q). | MED |

---

## 9. Config & persistence

| Surface | file:line | Replacement | Risk |
|---|---|---|---|
| `ForgeConfigSpec`/`.Builder`/`.ConfigValue`/`.BooleanValue` (every config field, ~200) | `config/ConfigHandler.java:6,9,11,29-206`, `vanish/VanishConfig.java:5-7` | `net.neoforged.neoforge.common.ModConfigSpec.*` — same Builder/ConfigValue API. Pure rename. | MED |
| `ModLoadingContext.get().registerConfig(type, spec, "sef/common.toml")` + custom filename | `utils/loader.java:13,26-29`, `ServerEssentialsForge.java:76` | `modContainer.registerConfig(type, spec, "sef/common.toml")`. Custom-filename + subdir overload retained. | HIGH |
| **`ConfigHandler.reloadFromDisk()` hand-rolls reload via `spec.setConfig(cfg)`** | `config/ConfigHandler.java:13-26` | **`ModConfigSpec` has NO `setConfig()` in NeoForge.** `acceptConfig(ILoadedConfig)` takes a *sealed* type you can't build. **`/sef reload`'s manual re-read cannot be ported as-is** — instead drive reload off `ModConfigEvent.Reloading` or just read `ConfigValue` getters (they reflect FML's loaded config). **Behavior change to call out.** | HIGH |
| `ModConfigEvent.Loading/Reloading` | `config/ConfigurationEventHandler.java:8,25,29` | `net.neoforged.fml.event.config.ModConfigEvent.*` (MOD bus). | LOW |
| `FMLPaths.CONFIGDIR`, nightconfig `CommentedFileConfig`/`WritingMode` | `config/ConfigHandler.java:3-4`, `ServerEssentialsForge.java:141` | `net.neoforged.fml.loading.FMLPaths` (same `.get()`); nightconfig still bundled. | LOW |
| Gson JSON persistence (mute/warn/alts/motd/filter/banned/announcements/bulletin) | respective `*Manager.java`/`DataStore` | Plain `java.nio`/Gson — **unaffected**. Confirm Gson on runtime classpath (likely transitive; add explicit dep if not). | LOW |

---

## 10. Integration — LuckPerms (optional/guarded)

| Surface | file:line | Replacement | Risk |
|---|---|---|---|
| `net.luckperms.api.*` (`LuckPermsProvider.get()`, `User`, `CachedMetaData.getPrefixes()/getSuffixes()`) | `utils/moddeps/LuckPermsProvider.java:15-25,51-52`, `events/PlayerEventHandler.java:106-109`, `tab/TabAnimationManager.java:14,22` | **API artifact `5.4` is identical for 1.21.1** (runtime jar `LuckPerms-NeoForge-5.4.139/.140`). No source change beyond confirming the package; guard via `ModList.isLoaded("luckperms")`. | LOW |
| Optional-dep guard | `events/ExternalModLoadingEvent.java:34`, `PlayerEventHandler.java:107` | `ModList.isLoaded("luckperms")` gates all LP class touches — load-safe when absent. | LOW |

---

## 11. Integration — FTB / Curios / Discord

| Surface | file:line | Replacement | Risk |
|---|---|---|---|
| **`FTBEPlayerData.getOrCreate(GameProfile)`** | `utils/moddeps/FTBNicknameProvider.java:11-14` | **The `GameProfile` overload was REMOVED in 2101.1.x** (verified against FTB-Essentials branch `1.21.1`). Only `getOrCreate(Player)` and `getOrCreate(MinecraftServer, UUID)` survive. Rewrite: `FTBEPlayerData.getOrCreate(server, profile.getId()).map(FTBEPlayerData::getNick)` (or re-type provider to `ServerPlayer`). Won't compile otherwise. | HIGH |
| `FTBMuteChecker` (FTB mute API) | `utils/moddeps/FTBMuteChecker.java` | Re-validate against 2101.1.x; guard `ModList.isLoaded("ftbessentials")`. | MED |
| Curios `CuriosApi.getCuriosInventory(player)` + `ICurioStacksHandler.getStacks()` | `utils/moddeps/CuriosInventoryHelper.java:7,52-57,93-101,130-138` | `getCuriosInventory(...)` **still returns `Optional`** (keep `.ifPresent`). Move `IItemHandlerModifiable` import to `net.neoforged.neoforge.items.*`. `ICuriosItemHandler` now in `...api.type.capability`. `getStacks()→IDynamicStackHandler extends NeoForge IItemHandlerModifiable` (assignment compiles). | MED |
| Discord bridges (mc2discord/sdlink/playtime) — reflective | `vanish/compat/Mc2DiscordCompat.java`, `SDLinkCompat.java`, `SDLinkHideTracker.java`, `PlaytimeCompat.java` | String-based reflection → resilient & load-safe (graceful no-op). Vanish→Discord feature *silently* breaks if those mods' 1.21.1 internals renamed — verify against target builds, not load-blocking. | LOW |

---

## 12. Vanish — mixins, access transformers, custom event (biggest risk)

> NeoForge runs **Mojang mappings at runtime**, so every mixin `@At`/`@Shadow`/`method=` target and every AT entry uses **Mojang names directly (no refmap, no SRG)**. The heavy work is re-validating each target against the **1.21.1** Mojang signatures (many changed 1.20.1→1.21.1). Best done in Phase 1 *after* the dev environment is up, so targets can be read from decompiled sources rather than guessed.

| Surface | file:line | Replacement / re-validation | Risk |
|---|---|---|---|
| AT #1 `f_140150_` (ChunkMap.entityMap) | `accesstransformer.cfg:3` | `public net.minecraft.server.level.ChunkMap entityMap` — **confirm field name in 1.21.1 mappings**. | HIGH |
| AT #2 `ChunkMap$TrackedEntity` (class) | `accesstransformer.cfg:4` | Name-agnostic; carries over unchanged. | LOW |
| AT #3 `f_244436_` (ClientboundPlayerInfoUpdatePacket.entries, `public-f`) | `accesstransformer.cfg:5` | `public-f net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket entries`. **Most fragile AT** — packet reworked 1.20.2+; `ServerGamePacketListenerImplMixin.java:65` *reassigns* the field. If now truly immutable, rewrite to build a new packet. | **CRIT** |
| `world/LivingEntityMixin` — `hasEffect/getEffect/updateInvisibilityStatus`, `getActiveEffectsMap().containsKey(MobEffects.INVISIBILITY)` | `vanish/mixin/world/LivingEntityMixin.java:37,44,51,54,60` | **MobEffect→`Holder<MobEffect>` (1.20.5):** target descriptors `(Lnet/minecraft/world/effect/MobEffect;)` → `(Lnet/minecraft/core/Holder;)`, params become `Holder<MobEffect>`, `MobEffects.INVISIBILITY` is now a Holder. Highest-confidence breaker. | **CRIT** |
| `ServerGamePacketListenerImplMixin` — `send(Packet)`/`send(Packet,PacketSendListener)`, `entries` field write, `Entry.profileId()`, `ClientboundSystemChatPacket.content()`, handle* | `vanish/mixin/ServerGamePacketListenerImplMixin.java:35,54-65,122-124,200-228` | Re-validate both `send` overloads; `entries` write depends on AT #3; `ServerLifecycleHooks` import → `net.neoforged.neoforge.server.ServerLifecycleHooks`. | **CRIT** |
| `PlayerListMixin` — `placeNewPlayer` | `vanish/mixin/PlayerListMixin.java:27-39` | **`placeNewPlayer` gained a `CommonListenerCookie` 3rd param in 1.20.2** — `@Inject` descriptor + handler params must add it. Confirmed signature change. | **CRIT** |
| `gui/MinecraftServerMixin` — `ServerStatus.forgeData()` reconstruction | `vanish/mixin/gui/MinecraftServerMixin.java:69` | **No `forgeData()` in NeoForge.** `ServerStatus` is a 6-arg record `(Component, Optional<Players>, Optional<Version>, Optional<Favicon>, boolean enforcesSecureChat, boolean isModded)` — use `isModded()`, keep the 6th boolean. | HIGH |
| `ServerPlayerMixin` — chat-signing path (`sendChatMessage(OutgoingChatMessage,boolean,ChatType.Bound)`, `ChatType.bind`, `PlayerChatMessage.link().sender()`) | `vanish/mixin/ServerPlayerMixin.java:41-68` | Re-validate against 1.21.1 chat-signing internals. | HIGH |
| `VanishingHandler` core packets — `createPlayerInitializing`, `ClientboundPlayerInfoRemove/RemoveEntities/SetActionBarText`, `chunkMap.entityMap` (AT), `refreshTabListName`, `HoverEvent` ctor | `vanish/VanishingHandler.java:43,63-86,116` | `MinecraftForge.EVENT_BUS→NeoForge.EVENT_BUS`. Re-validate vanilla packet factories/fields. | HIGH |
| Mixin groups `chat/` (6), `sound/` (1), `interaction/` (8) — selector/death-message/collision/pickup/projectile filtering | `vanish/mixin/chat/*`, `sound/EntityMixin.java`, `interaction/*` | Re-validate each Mojang target descriptor against 1.21.1 (EntitySelector/EntityArgument, CombatTracker `getFallMessage`/`CombatEntry` reworked 1.20.5, Block `fallOn/entityInside/stepOn`, `AbstractMinecart.tick`, etc.). | HIGH |
| `PlayerVanishEvent extends PlayerEvent` (custom event) | `vanish/api/PlayerVanishEvent.java` | Confirm NeoForge `PlayerEvent` base ctor + accessor (`getEntity`). | MED |

---

## 13. Cross-cutting (critic)

| Surface | file:line | Replacement | Risk |
|---|---|---|---|
| **`MinecraftServer.getServerDirectory()` File→Path** (7 sites) | `ServerEssentialsForge.java:137,139`, `filter/FilterManager.java:16`, `alts/AltTracker.java:50`, `announcements/AnnouncementManager.java:70`, `chat/OpBulletinHandler.java:33`, `warn/WarnManager.java:76` | Returns **`Path`** in 1.21.1 — drop `.toPath()`, retype the `File` local. Clean compile break. | HIGH |
| `SoundEvents.*` `.get()` vs raw `SoundEvent` inconsistency | `warn/WarnCommand.java:142`, `chat/ChatReplyHandler.java:90`, `chat/AdminChatHandler.java:146,172,193`, `freeze/FreezeManager.java:104`, `commands/MsgCommands.java:227` | Normalize Holder-vs-raw for 1.21.1; `playNotifySound(SoundEvent,...)` still exists. `NOTE_BLOCK_BELL.get()` implies Holder there. | MED |

---

## 14. Corrections the verification pass made to initial mappings (truth log)

1. **Server-only marker → `IGNORE_SERVER_VERSION`**, not `IGNORE_ALL_VERSIONS` (the latter is for mods with *no* server component). One reader claimed `displayTest` doesn't exist in 1.21.1; the authoritative NeoForge ModDevGradle template confirms it does.
2. **`ItemEntityPickupEvent.Pre` deny = `setCanPickup(TriState.FALSE)`**, not the invented `setCanReceiveStack`; `Pre` is not cancelable.
3. **`/sef reload`: `ModConfigSpec.setConfig()` does not exist** — manual re-read pattern must change (see §9).
4. **Parchment** `1.21.1` + `2024.11.17` (skeleton's `1.21.11`/`2025.12.20` are not real).
5. **FTB `dev.ftb.mods:ftb-essentials-neoforge:2101.1.x`** + FTB Library; **Curios `curios-neoforge:9.5.1+1.21.1:api`**; `fg.deobf` removed.
6. **`FTBEPlayerData.getOrCreate(GameProfile)` removed** in 2101.1.x.
7. **`ServerStatus` has `isModded()`**, not `forgeData()` (6-arg record).
8. **Permission classes split:** `PermissionGatherEvent.Nodes` in `...permission.events`; `PermissionNode`/`PermissionTypes` in `...permission.nodes`.
9. **Curios `getCuriosInventory` still returns `Optional`** (keep `.ifPresent`); `IItemHandlerModifiable` import moves to neoforge package.
10. **AT format confirmed** (auto-detected at `META-INF/accesstransformer.cfg`; `public-f` removes final) — but the **SRG→Mojang field names themselves remain unverified** (mappings sites unreachable; see Open Questions).
11. **TickEvent split** + `ServerStartedEvent`/etc. packages confirmed.

---

## 15. Ordered porting plan (dependency order)

**P0 — Decisions (you):** mod id/package (§1.1), target = 1.21.1 (§1.2), `displayTest` value.

**P1 — Skeleton stands up EMPTY & builds:**
1. Gut the MDK example (blocks/items/tabs/client-setup) from `Sefported.java`/`Config.java`.
2. Fix `gradle.properties` (parchment), set `mod_id`/group; `neoforge.mods.toml` (`displayTest="IGNORE_SERVER_VERSION"`, `[[mixins]]`, optional `[[accessTransformers]]`); add LuckPerms/FTB/Curios `compileOnly` deps + mavens.
3. `./gradlew build` (empty) → green before porting logic. This also brings decompiled 1.21.1 Mojang sources online for mixin/AT verification.

**P2 — Foundation:** `loader.java` (drop DisplayTest, `NeoForge.EVENT_BUS`, ctor-injected `ModContainer`) → `ServerEssentialsForge` entrypoint (ctor signature, lifecycle, tick→`.Post`) → **config** (`ForgeConfigSpec→ModConfigSpec`, `registerConfig` move, **reload rework**) → `PlayerData`/`IReloadable`. Build.

**P3 — Chat formatting core:** `TextFormatter`/`MarkdownFormatter`/`BitwiseStyling`/`SEFUtilities` (mostly stable). Build.

**P4 — Permissions:** `PermissionsHandler` (node ctor, `PermissionAPI`, `PermissionGatherEvent.Nodes` bus). Build — unblocks all commands.

**P5 — Events:** chat (`ServerChatEvent` + instance registration) → player/lifecycle (`TabListNameFormat`/`NameFormat`/tick) → gameplay (`ItemEntityPickupEvent.Pre`, `BlockEvent`, interacts). Build each.

**P6 — Commands:** `CommandRegistrationHandler` + all `*Command.java`. Build.

**P7 — Cross-cutting + persistence:** `getServerDirectory()` Path, `ForgeRegistries→BuiltInRegistries`, `SoundEvents`, all Gson managers. Build.

**P8 — Integrations:** LuckPerms (smallest) → FTB (`FTBEPlayerData` rewrite) → Curios → Discord compat. Build, then **confirm standalone load with all optional mods absent.**

**P9 — InvSee menu:** `InvSeeContainer`/`Command`/`Layout` (`openMenu`, `SlotItemHandler`, Curios slots). Build.

**P10 — Vanish (last, hardest):** AT (Mojang names, verified against decompiled sources) → `VanishingHandler` → mixins by subpackage (`LivingEntityMixin`, `PlayerListMixin`, `ServerGamePacketListenerImplMixin` first) → `PlayerVanishEvent`. Build after each subpackage.

**P11 — Full build + dedicated-server smoke test** (load with and without LuckPerms/FTB/Curios).

---

## 16. Open questions / unverified mappings (need confirmation in Phase 1)

**Must verify against actual decompiled 1.21.1 Mojang sources (do once dev env is up in P1):**
- AT field names: is `ChunkMap.entityMap` (was `f_140150_`) and `ClientboundPlayerInfoUpdatePacket.entries` (was `f_244436_`) — and is `entries` still a reassignable `List<Entry>` or now immutable? (gates vanish; CRITICAL)
- `PlayerList.placeNewPlayer` — confirm `CommonListenerCookie` 3rd param in 1.21.1.
- `ServerGamePacketListenerImpl.send(Packet)` vs `send(Packet, PacketSendListener)` — both still present?
- `LivingEntity.hasEffect/getEffect/updateInvisibilityStatus` + `getActiveEffectsMap()` key now `Holder<MobEffect>`.
- `ServerPlayer.sendChatMessage(OutgoingChatMessage, boolean, ChatType.Bound)` + `ChatType.bind` overloads.
- `CombatTracker.getFallMessage`/`CombatEntry` shape (reworked 1.20.5).
- `Entity.broadcastToPlayer(ServerPlayer)`, `AbstractMinecart.tick` still present.

**NeoForge API shapes to confirm:**
- `PermissionGatherEvent.Nodes` fires on GAME or MOD bus? (affects registration site)
- `PermissionNode` exact ctor arity + `setInformation(Component, Component)` present?
- `ServerChatEvent.getMessage()` accessor name in 1.21.1.
- `FMLLoadCompleteEvent` still fires on MOD bus (used to defer handler registration)?
- `displayTest` value: confirm `IGNORE_SERVER_VERSION` at first build (one-liner, trivially testable).

**Integration versions to pin:**
- Exact FTB Essentials 2101.1.x + matching FTB Library version, and confirm `dev.ftb.mods.ftbessentials.util.FTBEPlayerData` is on the compileOnly classpath.
- Curios `curios-neoforge` exact version (9.x) matching the target modpack.
- mc2discord / sdlink / playtime 1.21.1 internal class names (reflective compat — non-blocking).
- Gson explicitly on classpath or transitive?

**Behavior changes to confirm acceptable:**
- `/sef reload` reload mechanism change (§9) — confirm reading `ConfigValue` getters / `ModConfigEvent.Reloading` is acceptable vs the old manual re-read.


---

# Appendix — Complete machine-extracted inventory


_Auto-generated from the Phase 0 audit workflow result so nothing is dropped. 13 dimensions; 143 findings total._


## Appendix A — Every finding, by dimension


### Build system, mod metadata, entrypoint & server-side-only marker (`build-skeleton`)


**Summary:** The build moves from ForgeGradle 6 + mixingradle (SRG-mapped, Java 17, reobf) to the NeoForge ModDevGradle plugin (Mojang runtime mappings, Java 21, no reobf). The skeleton is the stock NeoForge MDK and is full of example block/item/creative-tab/client-setup code that must be gutted before SEF's 110 source files are dropped in. The entrypoint changes are mechanical but load-bearing: @Mod ctor gains (IEventBus, ModContainer), FMLJavaModLoadingContext/ModLoadingContext are gone, the custom utils/loader.java (DisplayTest + NetworkConstants.IGNORESERVERONLY + registerConfig + EVENT_BUS.register) must be rebuilt against NeoForge APIs, and the server-side-only marker becomes a single displayTest="IGNORE_ALL_VERSIONS" line in neoforge.mods.toml. Two property/metadata decisions (mod_id "sef" vs "sefported", package com.enviouse.sef vs com.enviouse.sefported) and a parchment_minecraft_version=1.21.11 vs 1.21.1 mismatch must be resolved up front because everything else depends on them. Overall risk MEDIUM, but the AT SRG->Mojang name translation and mixin refmap removal are CRITICAL gating items.


**Findings (12):**


- **[MEDIUM] ForgeGradle 6 plugin (net.minecraftforge.gradle 6.0.+) + buildscript classpath, apply plugin 'net.minecraftforge.gradle', minecraft{} block, mappings channel:'official' 1.20.1, reobfJar/finalizedBy, fg.deobf()**
  - _file:line:_ build.gradle:1-19, build.gradle:32-46, build.gradle:153, build.gradle:170, build.gradle:218
  - _→ NeoForge 1.21.1:_ Use the NeoForge ModDevGradle plugin already in the skeleton: plugins{ id 'net.neoforged.moddev' version '2.0.141' } with neoForge{ version = neo_version } (SEFPORTED/build.gradle:5, :21-22). No reobf step (Mojang mappings at runtime), no fg.deobf wrapper for mod deps. Adopt skeleton wholesale; do not port the old buildscript block.
  - _notes:_ The whole old build.gradle is replaced, not edited. Old uses Java 17 toolchain (build.gradle:29); new is Java 21 (SEFPORTED/build.gradle:19). reobf/SRG pipeline disappears entirely. The skeleton build.gradle is essentially correct already; main work is adding deps + (optionally) AT line.


- **[CRITICAL] Mixin configured via org.spongepowered.mixin gradle plugin + mixin{} block (add sourceSets.main 'sef.refmap.json'; config 'sef.mixins.json') + annotationProcessor 'org.spongepowered:mixin:0.8.5:processor' + MixinConfigs jar manifest attr; refmap 'sef.refmap.json' in sef.mixins.json**
  - _file:line:_ build.gradle:10, build.gradle:19, build.gradle:156, build.gradle:244-253, sef.mixins.json:6
  - _→ NeoForge 1.21.1:_ Under ModDev there is NO mixingradle and NO refmap. Declare mixins only in neoforge.mods.toml [[mixins]] config="<modid>.mixins.json" (already in skeleton template at neoforge.mods.toml:38-39). Remove the "refmap" and "mixinextras" keys from the mixins json (skeleton sefported.mixins.json has neither). NeoForge applies mixins via its own mixin runtime; ModDev provides the AP automatically. MixinExtras (@WrapOperation) is bundled in NeoForge at runtime; no compileOnly needed. Crucially: because runtime is Mojang-mapped, every mixin @At/@Shadow target and the AT below must use Mojang names, not SRG.
  - _notes:_ sef.mixins.json lists 28 mixins (PlayerListMixin, ServerGamePacketListenerImplMixin, etc.) with compatibilityLevel JAVA_17 -> JAVA_21 and package com.enviouse.sef.vanish.mixin -> new package. The refmap mechanism (sef.refmap.json) is obsolete; any @At target string written for SRG must be re-pointed to Mojang names. The actual mixin BODY correctness is a different dimension; here the build wiring + refmap removal + JAVA_21 bump is the deliverable.


- **[CRITICAL] Access Transformer with SRG field names: 'f_140150_' (ChunkMap entityMap) and 'f_244436_' (ClientboundPlayerInfoUpdatePacket entries), declared via minecraft{ accessTransformer = file('.../accesstransformer.cfg') }**  _(uncertain — needs verification)_
  - _file:line:_ accesstransformer.cfg:3, accesstransformer.cfg:5, build.gradle:70
  - _→ NeoForge 1.21.1:_ NeoForge runs on Mojang mappings, so AT entries must use Mojang names: 'public net.minecraft.server.level.ChunkMap entityMap' (was f_140150_) and 'public-f net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket entries' (was f_244436_). The class-level lines (ChunkMap$TrackedEntity, line 4) are name-agnostic and carry over unchanged. ModDev auto-detects META-INF/accesstransformer.cfg, so no build.gradle line is strictly required; optionally uncomment neoForge.accessTransformers.add(...) (SEFPORTED/build.gradle:31) or the [[accessTransformers]] block in neoforge.mods.toml:43-44.
  - _notes:_ The SRG->Mojang field-name translation is the load-bearing bit. The inline comments confirm intent (entityMap, entries) but I am not 100% certain the exact Mojang field name on ClientboundPlayerInfoUpdatePacket is literally 'entries' in 1.21.1 (the comment says so; verify against 1.21.1 mappings). Mistakes here = silent vanish breakage or load failure.


- **[HIGH] @Mod no-arg constructor: public ServerEssentialsForge() { ... } with FMLJavaModLoadingContext.get().getModEventBus() and ModLoadingContext.get()**
  - _file:line:_ src/main/java/com/enviouse/sef/ServerEssentialsForge.java:37-85, ServerEssentialsForge.java:76, ServerEssentialsForge.java:82
  - _→ NeoForge 1.21.1:_ Constructor becomes public ServerEssentialsForge(IEventBus modEventBus, ModContainer modContainer) (FML injects these), per skeleton Sefported.java:64. Replace FMLJavaModLoadingContext.get().getModEventBus() with the injected modEventBus (line 82 -> modEventBus.addListener(this::loadComplete)). Replace ModLoadingContext.get().registerConfig(...) (line 76) with modContainer.registerConfig(ModConfig.Type.SERVER, VanishConfig.SERVER_SPEC, "sef-vanish-server.toml"). ModConfig import becomes net.neoforged.fml.config.ModConfig.
  - _notes:_ Mechanical but touches the core init path. Note FMLLoadCompleteEvent (line 31/86) still exists under net.neoforged.fml.event.lifecycle in NeoForge 1.21.1 - verify it fires on MOD bus as before. The mod stores both ConfigHandler.spec and VanishConfig.SERVER_SPEC (both ForgeConfigSpec) which feed registerConfig; those spec types are ported in the config dimension, but this ctor must call the NEW modContainer.registerConfig signature.


- **[HIGH] utils/loader.java: ModLoadingContext.get(); IExtensionPoint.DisplayTest + NetworkConstants.IGNORESERVERONLY (server-only marker); registerExtensionPoint; mlc.registerConfig(Type, IConfigSpec, path); MinecraftForge.EVENT_BUS.register**
  - _file:line:_ src/main/java/com/enviouse/sef/utils/loader.java:1-32, ServerEssentialsForge.java:71-72
  - _→ NeoForge 1.21.1:_ DELETE the DisplayTest/NetworkConstants path entirely (loader.MlContext / loader.java:17-24): IExtensionPoint.DisplayTest and NetworkConstants are GONE in NeoForge. The server-side-only behavior is expressed declaratively by adding displayTest="IGNORE_ALL_VERSIONS" inside the [[mods]] block of neoforge.mods.toml (the skeleton template has no such line yet; must be added near neoforge.mods.toml:16-22). loader.register(obj) -> NeoForge.EVENT_BUS.register(obj) (net.neoforged.neoforge.common.NeoForge). loader.MLConfig -> modContainer.registerConfig(ModConfig.Type.COMMON, ConfigHandler.spec, "sef/common.toml"). The loader util can be kept as a thin wrapper but must take a ModContainer (no global ModLoadingContext.get() for config registration).
  - _notes:_ This file is the heart of the server-side-only marker for this dimension. loader.java imports net.minecraftforge.fml.config.IConfigSpec (line 6) whose signature CHANGED in NeoForge - but here it is only used as the static param type of MLConfig; replacing MLConfig with a direct ModContainer.registerConfig(ModConfigSpec) call avoids touching IConfigSpec at all. mlc is a static field initialized at class-load (line 13) - that pattern must go since registerConfig moved onto ModContainer (per-instance), not a global context.


- **[MEDIUM] Server-side-only marker via NetworkConstants.IGNORESERVERONLY + DisplayTest (Java code) — and mods.toml dependency on modId='forge'**
  - _file:line:_ src/main/java/com/enviouse/sef/utils/loader.java:19-23, mods.toml:43-53
  - _→ NeoForge 1.21.1:_ Add to the [[mods]] table in neoforge.mods.toml: displayTest="IGNORE_ALL_VERSIONS" (no Java code). Change the loaderVersion/modLoader and the forge dependency: mods.toml modId="forge" dependency (mods.toml:45) -> neoforge (skeleton template already uses neoforge at neoforge.mods.toml:51); modLoader stays "javafml"; loaderVersion -> ${loader_version_range} ([4,) from gradle.properties:20). minecraft versionRange "1.20.1" -> ${minecraft_version_range} = [1.21.1,1.22).
  - _notes:_ Straightforward once you know IGNORE_ALL_VERSIONS is the replacement. The luckperms optional dependency block (mods.toml:62-67, mandatory=false, ordering=BEFORE) should be re-expressed as type="optional" in NeoForge's new dependency schema (type field replaces mandatory bool). FTB/Curios are NOT declared as toml deps in the old file (only luckperms is) - keep them compileOnly only.


- **[HIGH] @EventBusSubscriber / @SubscribeEvent / TickEvent / event imports under net.minecraftforge.* in entrypoint (onServerStarted, onServerTick phase==END, onServerStopping, RegisterCommandsEvent, PermissionGatherEvent.Nodes, PermissionNode ctor, ModList, FMLPaths)**  _(uncertain — needs verification)_
  - _file:line:_ src/main/java/com/enviouse/sef/ServerEssentialsForge.java:26-33, ServerEssentialsForge.java:93-113, ServerEssentialsForge.java:116-205
  - _→ NeoForge 1.21.1:_ Import swaps in the entrypoint: MinecraftForge.EVENT_BUS->NeoForge.EVENT_BUS (lines 26,79-80); net.minecraftforge.eventbus.api.SubscribeEvent->net.neoforged.bus.api.SubscribeEvent (line 27); TickEvent.ServerTickEvent + phase==END (lines 171-172) -> net.neoforged.neoforge.event.tick.ServerTickEvent.Post (drop the phase check); ServerStartedEvent/ServerStoppingEvent -> net.neoforged.neoforge.event.server.* (lines 117,191); RegisterCommandsEvent -> net.neoforged.neoforge.event.RegisterCommandsEvent (line 93); ModList -> net.neoforged.fml.ModList (line 163); FMLPaths -> net.neoforged.fml.loading.FMLPaths (line 141). PermissionGatherEvent.Nodes + PermissionNode + PermissionTypes -> net.neoforged.neoforge.server.permission.* (lines 98-111).
  - _notes:_ These leaf events (tick split into .Pre/.Post, permission API package move, PermissionNode ctor) overlap with the events/permissions dimensions; flagged here only because they appear directly in the entrypoint file. PermissionNode constructor shape and which bus PermissionGatherEvent.Nodes fires on need verification in NeoForge 1.21.1 - I am not confident the ctor signature (modid, path, PermissionType, defaultResolver) is byte-for-byte identical.


- **[MEDIUM] mod_id, package, group, archivesName divergence: OLD mod_id=sef, group/package com.enviouse.sef, archivesName 'sef'; NEW mod_id=sefported, package com.enviouse.sefported, mod_group_id=com.enviouse**
  - _file:line:_ gradle.properties:45, gradle.properties:55, build.gradle:22-26, SEFPORTED/gradle.properties:26, SEFPORTED/gradle.properties:36, SEFPORTED/src/main/java/com/enviouse/sefported/Sefported.java:1, SEFPORTED/src/main/java/com/enviouse/sefported/Sefported.java:39
  - _→ NeoForge 1.21.1:_ RECOMMEND keeping the original mod_id="sef" and package com.enviouse.sef. Rationale: (1) mod_id is baked into config paths ('sef/common.toml', 'sef-vanish-server.toml'), permission node namespaces (MODID used in PermissionNode at ServerEssentialsForge.java:101/108), lang key itemGroup, and the chat brand string CHAT_ID_STR (ServerEssentialsForge.java:39-41); changing it silently orphans existing server config/permission setups. (2) All 110 source files declare package com.enviouse.sef and every file cross-references it (grep: 110/110). Adopting 'sefported' forces a 110-file package rename plus mixin json 'package' key plus AT (AT is name-only, unaffected). Concretely: set gradle.properties mod_id=sef, mod_group_id=com.enviouse.sef, mod_name=ServerEssentialsForge, mod_version=1.1; keep com.enviouse.sef packages; rename the skeleton's Sefported.java->ServerEssentialsForge.java (or just paste the ported entrypoint). The skeleton's 'sefported'/com.enviouse.sefported scaffolding should be discarded, not renamed.
  - _notes:_ Either choice works mechanically; cost asymmetry favors keeping 'sef'. If they insist on 'sefported', it is a global find/replace across 110 files + mixin json package + the 3 config/permission string literals + lang file - higher churn, no functional benefit. The skeleton mixins json is com.enviouse.sefported.mixin (sefported.mixins.json:4) vs old com.enviouse.sef.vanish.mixin (sef.mixins.json:4); pick to match the package decision.


- **[LOW] Example/scaffold code in skeleton that must be gutted: DeferredRegister.Blocks/Items/CreativeModeTabs, EXAMPLE_BLOCK/ITEM/TAB, BuildCreativeModeTabContentsEvent, addCreative, ClientModEvents (Dist.CLIENT, Minecraft.getInstance()), FMLClientSetupEvent; example Config.java (LOG_DIRT_BLOCK/MAGIC_NUMBER/ITEM_STRINGS)**
  - _file:line:_ SEFPORTED/src/main/java/com/enviouse/sefported/Sefported.java:42-60, :69-73, :81, :99-119, SEFPORTED/src/main/java/com/enviouse/sefported/Config.java:18-50
  - _→ NeoForge 1.21.1:_ Delete all block/item/creative-tab registers and the @EventBusSubscriber(value=Dist.CLIENT) ClientModEvents inner class (server-side-only mod must NOT reference net.minecraft.client.Minecraft or FMLClientSetupEvent - Sefported.java:4,17,24,112-118). Delete the example Config.java entirely and bring over SEF's ConfigHandler/VanishConfig (ForgeConfigSpec->ModConfigSpec ported in config dimension). Keep only: @Mod class, ctor(IEventBus,ModContainer), config registration, NeoForge.EVENT_BUS.register, and SEF's own handler wiring. Also delete src/main/resources/assets/sefported/lang/en_us.json's example entries / itemGroup keys.
  - _notes:_ Pure deletion; low risk but must be thorough or the server crashes on Minecraft.getInstance() (client-only class) at load. The skeleton's empty sefported.mixins.json (sefported.mixins.json:5-7) needs SEF's 28 mixin entries copied in with the chosen package.


- **[HIGH] compileOnly mod dependencies via fg.deobf + flatDir lib/ jars: ftb-essentials-forge:2001.2.2, net.luckperms:api:5.4, top.theillusivec4.curios:curios-forge:5.14.1+1.20.1, plus lib/ jars (curios-forge, ftb-essentials-forge, ftb-library-forge, luckperms-api, LuckPerms-Forge)**  _(uncertain — needs verification)_
  - _file:line:_ build.gradle:137-144, build.gradle:167-170, build.gradle:130-145
  - _→ NeoForge 1.21.1:_ Re-express under ModDev. luckperms: compileOnly 'net.luckperms:api:5.4' (artifact unchanged for 1.21.1, no remap needed - it's a plain API jar, no fg.deobf). FTB Essentials 1.21.1 NeoForge = version line 2101.1.x (e.g. 2101.1.9), needs FTB Library too; declare via Maven (FTB maven) as compileOnly, or local lib/ jars swapped to the 1.21.1 NeoForge builds. Curios: 1.21.1 NeoForge build under top.theillusivec4.curios:curios-neoforge:<ver> via maven.theillusivec4.top; compileOnly, NO fg.deobf (ModDev auto-remaps Mojang-namespace mod jars / or they're already Mojang-mapped). flatDir is replaced by either the existing repositories or, for local jars, neoForge mod-jar handling; the old 1.20.1 Forge jars in lib/ are all unusable and must be replaced with 1.21.1 NeoForge equivalents.
  - _notes:_ All five lib/ jars are 1.20.1 FORGE builds (confirmed: curios-forge-5.14.1+1.20.1.jar, ftb-essentials-forge-2001.2.2.jar, ftb-library-forge-2001.2.3.jar, LuckPerms-Forge-5.4.102.jar) - none load on NeoForge 1.21.1. Need NeoForge 1.21.1 replacements. FTBEPlayerData accessors and Curios slot-handler API for 1.21.1 (2101.1.x / curios-neoforge) must be verified in the integrations dimension - exact artifact coords and whether fg.deobf-equivalent remapping is needed under ModDev is uncertain.


- **[MEDIUM] parchment_minecraft_version=1.21.11 (should be 1.21.1) feeding neoForge.parchment{ minecraftVersion } ; settings.gradle pluginManagement (Forge maven) and foojay 0.7.0**  _(uncertain — needs verification)_
  - _file:line:_ SEFPORTED/gradle.properties:21-22, SEFPORTED/build.gradle:25-28, settings.gradle:1-9
  - _→ NeoForge 1.21.1:_ Fix parchment_minecraft_version=1.21.11 -> 1.21.1 in SEFPORTED/gradle.properties:21 to match minecraft_version=1.21.1 (gradle.properties:10). '1.21.11' is not a real MC version and will fail Parchment resolution. parchment_mappings_version=2025.12.20 should be a date that actually published for 1.21.1 - verify on ParchmentMC maven. settings.gradle is already NeoForge-correct (maven.neoforged.net, foojay 0.8.0); the OLD settings.gradle (Forge maven, foojay 0.7.0) is discarded.
  - _notes:_ Parchment is optional; if it fails to resolve it blocks the whole build/sync. Either correct the version or remove the parchment{} block. I cannot confirm 2025.12.20 is a published Parchment release for 1.21.1 from here - verify. The date is also suspiciously future-dated relative to typical 1.21.1 Parchment exports; flag for verification.


- **[LOW] pack.mcmeta pack_format=10 (1.20.1)**  _(uncertain — needs verification)_
  - _file:line:_ src/main/resources/pack.mcmeta:1-6
  - _→ NeoForge 1.21.1:_ For 1.21.1, resource/data pack_format must be bumped (1.21.1 uses pack_format 34 for resources / 48 for data, or use the NeoForge MDK's neoforge:pack.mcmeta convention). The skeleton may rely on neoforge.mods.toml for pack metadata; verify whether a pack.mcmeta is still needed and use the correct 1.21.1 format. Server-only mod has minimal assets so this is low impact but will warn if wrong.
  - _notes:_ Server-side-only mod ships almost no assets (just a lang file), so the pack format is cosmetic, but a wrong format produces load warnings. Confirm the exact 1.21.1 pack_format number against the target.


**Ordering notes:**

- DECIDE mod_id + package (sef vs sefported) FIRST — it gates the mixin json 'package' key, neoforge.mods.toml ${mod_id}, config/permission string literals, lang keys, and whether 110 files get a package rename. Everything downstream assumes this.
- Fix gradle.properties (parchment 1.21.11->1.21.1, set mod_id/mod_group_id/mod_name/mod_version/mod_authors/mod_description to SEF values) and gut the skeleton example code BEFORE attempting any compile, or the server crashes on client-only Minecraft.getInstance().
- Config dimension must port ForgeConfigSpec->ModConfigSpec for ConfigHandler.spec and VanishConfig.SERVER_SPEC BEFORE the entrypoint's modContainer.registerConfig(...) calls will compile (this dimension only fixes the registration call site / signature).
- AT SRG->Mojang field-name translation must be settled before mixins are validated, since both depend on the same Mojang-name runtime; verify the two field names (entityMap, entries) against 1.21.1 mappings as a single up-front task.
- Re-source the five lib/ dependency jars to NeoForge 1.21.1 builds (luckperms api/runtime, FTB Essentials+Library, Curios) before the integrations/permissions/invsee code will resolve; build wiring (compileOnly, no fg.deobf, repo coords) is this dimension, API verification is the integrations dimension.
- Mixin build wiring (remove refmap, remove mixingradle, JAVA_17->JAVA_21, declare via neoforge.mods.toml [[mixins]]) can proceed in parallel with the entrypoint port but must finish before the vanish mixin bodies (other dimension) are tested.


**Open questions (this dimension):**

- Is the exact Mojang field name on ClientboundPlayerInfoUpdatePacket literally 'entries' in 1.21.1 (AT line 5, old SRG f_244436_)? And is ChunkMap's field 'entityMap' (old f_140150_)? Must confirm against 1.21.1 official mappings before writing the AT.
- Does net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent still exist and fire on the MOD bus in NeoForge 1.21.1 (used at ServerEssentialsForge.java:82-90 to defer handler registration)?
- Is parchment_mappings_version=2025.12.20 an actually-published Parchment export for MC 1.21.1, or does it need correcting alongside the 1.21.11->1.21.1 fix?
- What is the correct pack_format for a 1.21.1 NeoForge mod's pack.mcmeta (or is pack.mcmeta superseded)?
- Exact NeoForge 1.21.1 artifact coordinates for FTB Essentials (2101.1.x) + FTB Library and Curios (curios-neoforge) and whether ModDev needs any remap directive for them as compileOnly mod jars.
- Should the project keep mod_id 'sef'/package com.enviouse.sef (lower churn, preserves config & permission namespaces) or adopt the skeleton's 'sefported' (110-file rename)? Needs a product decision before any code is moved.
- PermissionNode constructor signature and the bus PermissionGatherEvent.Nodes fires on in NeoForge 1.21.1 (appears directly in entrypoint at ServerEssentialsForge.java:98-111).


**Verification verdicts:**

- **CONFIRMED** — Mixin via org.spongepowered.mixin gradle plugin + mixin{} block (refmap 'sef.refmap.json') + annotationProcessor mixin:0.8.5:processor + MixinConfigs jar manifest attr; refmap declared in mixins json
    - claim: Under ModDevGradle there is NO mixingradle plugin and NO refmap; declare mixins only in neoforge.mods.toml [[mixins]] config; remove refmap/mixinextras keys from the mixins json; ModDev provides the AP automatically; MixinExtras is bundled in NeoForge at runtime (no compileOnly); @At/@Shadow targets and AT must use Mojang names, not SRG.
    - verified → same (with one nuance): Correct direction. NeoForge 1.21.1 runs on official/Mojang mappings, so there is no SRG reobfuscation step and the legacy refmap is not used the way ForgeGradle used it. Mixin configs are declared via the [[mixins]] config entry in neoforge.mods.toml (already present in the skeleton template at lines 38-39: config = "${mod_id}.mixins.json"). The skeleton sefported.mixins.json correctly has neither a 'refmap' nor 'mixinextras' key. MixinExtras (incl. @WrapOperation) is bundled in NeoForge (>=20.2.84), so no compileOnly is needed for the bundled version. All @At/@Shadow targets and the AT must use Mojang names. Note: ModDevGradle does not advertise a dedicated mixin{} DSL block; mixin AP wiring is handled by the NeoForge runtime/userdev deps it pulls in. The MixinConfigs manifest attribute is replaced entirely by the toml [[mixins]] entry.
    - evidence: https://docs.neoforged.net/docs/1.21.1/gettingstarted/modfiles/ ([[mixins]] header, mandatory 'config' property, example config="examplemod.mixins.json"); https://deepwiki.com/neoforged/ModDevGradle/2.3-creating-your-first-mod (mixins declared in neoforge.mods.toml, no refmap/AP block in build.gradle); NeoForge 20.3 release notes confirm MixinExtras bundled since 20.2.84 (https://neoforged.net/news/20.3release/ and MixinExtras README). Skeleton: /mnt/hermes/projects/SEFPORTED/src/main/templates/META-INF/neoforge.mods.toml lines 38-39; /mnt/hermes/projects/SEFPORTED/src/main/resources/sefported.mixins.json
- **CORRECTED** — AT with SRG field names f_140150_ (ChunkMap.entityMap) and f_244436_ (ClientboundPlayerInfoUpdatePacket.entries) via minecraft{ accessTransformer = file(...) }
    - claim: AT must use Mojang names: 'public net.minecraft.server.level.ChunkMap entityMap' and 'public-f net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket entries'; class-level lines are name-agnostic; ModDev auto-detects META-INF/accesstransformer.cfg so no build.gradle line strictly required; optionally neoForge.accessTransformers.add(...) or [[accessTransformers]] in toml.
    - verified → Format and auto-detection CONFIRMED: NeoForge ATs use official/Mojang names, 'public <fqcn> <field>' makes a field public, and 'public-f'/'-f' removes ACC_FINAL; NeoForge auto-detects META-INF/accesstransformer.cfg. The 'public-f ... entries' (removing final) is a valid form. CAVEAT (uncertain, not verified): the specific Mojang field names entityMap on ChunkMap and entries on ClientboundPlayerInfoUpdatePacket, and the SRG->Mojang mapping f_140150_->entityMap / f_244436_->entries, were NOT verified against mappings.dev/linkie (not reachable here). The class-level ChunkMap$TrackedEntity line being name-agnostic is correct. Also note the skeleton build.gradle line 31 is commented as 'accessTransformers.add(...)' inside the neoForge{} block (not 'neoForge.accessTransformers.add'); the toml block is at lines 41-44.
    - evidence: https://docs.neoforged.net/docs/1.21.1/advanced/accesstransformers/ (format 'public net.minecraft.server.MinecraftServer random'; 'protected-f ...' removes final; 'By default, NeoForge will search for META-INF/accesstransformer.cfg'; official names); https://docs.neoforged.net/docs/1.21.1/gettingstarted/modfiles/ ([[accessTransformers]] mandatory 'file' property). Field-name SRG->Mojang mappings unverified (mappings.dev/linkie not reached). Skeleton: /mnt/hermes/projects/SEFPORTED/build.gradle line 31; /mnt/hermes/projects/SEFPORTED/src/main/templates/META-INF/neoforge.mods.toml lines 41-44
- **CONFIRMED** — @Mod no-arg constructor with FMLJavaModLoadingContext.get().getModEventBus() and ModLoadingContext.get()
    - claim: Constructor becomes public Ctor(IEventBus modEventBus, ModContainer modContainer) (FML injects); use injected modEventBus.addListener(...); replace ModLoadingContext.get().registerConfig(...) with modContainer.registerConfig(ModConfig.Type.SERVER, spec, "...toml"); ModConfig import becomes net.neoforged.fml.config.ModConfig.
    - verified → same. NeoForge 1.21.1 injects IEventBus and ModContainer into the @Mod constructor (FML recognizes IEventBus/ModContainer params). Config is registered via modContainer.registerConfig(ModConfig.Type.<...>, spec[, fileName]). ModConfig is net.neoforged.fml.config.ModConfig. The Type used should match the original mod's intent (the SEF skeleton uses Type.COMMON for the example; SERVER is valid where the original was SERVER).
    - evidence: Skeleton entrypoint /mnt/hermes/projects/SEFPORTED/src/main/java/com/enviouse/sefported/Sefported.java lines 20-23 (imports: net.neoforged.bus.api.IEventBus, net.neoforged.fml.ModContainer, net.neoforged.fml.config.ModConfig), line 64 (public Sefported(IEventBus modEventBus, ModContainer modContainer)), line 66 (modEventBus.addListener), line 84 (modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC)); https://docs.neoforged.net/docs/1.21.1/gettingstarted/ and /misc/config/
- **CORRECTED** — loader util: ModLoadingContext.get(); IExtensionPoint.DisplayTest + NetworkConstants.IGNORESERVERONLY (server-only marker); registerExtensionPoint; mlc.registerConfig; MinecraftForge.EVENT_BUS.register
    - claim: DELETE the DisplayTest/NetworkConstants path entirely (gone in NeoForge); express server-side-only declaratively via displayTest="IGNORE_ALL_VERSIONS" inside [[mods]] of neoforge.mods.toml; loader.register -> NeoForge.EVENT_BUS.register; loader.MLConfig -> modContainer.registerConfig(...); util must take a ModContainer.
    - verified → PARTLY CORRECT, but the displayTest claim is wrong for NeoForge 1.21.1. CORRECT: IExtensionPoint.DisplayTest and NetworkConstants.IGNORESERVERONLY were removed in NeoForge's 1.20.4+ networking rewrite; MinecraftForge.EVENT_BUS -> NeoForge.EVENT_BUS (net.neoforged.neoforge.common.NeoForge); config registration moves to modContainer.registerConfig (no global ModLoadingContext.get()). WRONG: there is NO displayTest field in the NeoForge 1.21.1 [[mods]] block - the 1.21.1 docs do not document displayTest/IGNORE_ALL_VERSIONS at all. In NeoForge 1.21.1, a mod is 'server-side only' simply by not requiring itself on the client: it should not register required network payloads/channels, and may optionally use a dedicated @Mod(value="sef", dist=Dist.DEDICATED_SERVER) class for server-only loading. There is no toml key to add for version-mismatch tolerance; vanilla/mismatched clients can connect as long as the mod registers no required-sync content.
    - evidence: https://docs.neoforged.net/docs/1.21.1/gettingstarted/modfiles/ (no displayTest property listed in [[mods]]); https://docs.neoforged.net/docs/1.21.1/concepts/sides/ (@Mod(value=..., dist=Dist.DEDICATED_SERVER) for server-only loading); https://neoforged.net/news/2025serversidesummer/ ('A mod is considered a serverside mod if it can run on a server and retains most functionalities when a vanilla client connects'); NeoForge networking rewrite (1.20.4) removed NetworkConstants/IExtensionPoint.DisplayTest (forums.minecraftforge.net thread 141188 confirms NetworkConstants gone). NeoForge.EVENT_BUS: net.neoforged.neoforge.common.NeoForge, used at /mnt/hermes/projects/SEFPORTED/.../Sefported.java line 78
- **CORRECTED** — Forge event imports in entrypoint: MinecraftForge.EVENT_BUS, net.minecraftforge.eventbus.api.SubscribeEvent, TickEvent.ServerTickEvent phase==END, ServerStartedEvent/ServerStoppingEvent, RegisterCommandsEvent, ModList, FMLPaths, PermissionGatherEvent.Nodes/PermissionNode/PermissionTypes
    - claim: MinecraftForge.EVENT_BUS->NeoForge.EVENT_BUS; net.minecraftforge.eventbus.api.SubscribeEvent->net.neoforged.bus.api.SubscribeEvent; TickEvent.ServerTickEvent+phase==END->net.neoforged.neoforge.event.tick.ServerTickEvent.Post (drop phase check); ServerStartedEvent/ServerStoppingEvent->net.neoforged.neoforge.event.server.*; RegisterCommandsEvent->net.neoforged.neoforge.event.RegisterCommandsEvent; ModList->net.neoforged.fml.ModList; FMLPaths->net.neoforged.fml.loading.FMLPaths; PermissionGatherEvent.Nodes/PermissionNode/PermissionTypes->net.neoforged.neoforge.server.permission.*
    - verified → Mostly CONFIRMED with a package-precision correction on permissions. CONFIRMED: SubscribeEvent = net.neoforged.bus.api.SubscribeEvent; NeoForge.EVENT_BUS = net.neoforged.neoforge.common.NeoForge; ServerTickEvent split into Pre/Post under net.neoforged.neoforge.event.tick (use ServerTickEvent.Post, drop the phase==END check); ServerStartedEvent/ServerStartingEvent/ServerStoppingEvent in net.neoforged.neoforge.event.server; RegisterCommandsEvent in net.neoforged.neoforge.event. CORRECTION: the permission classes are NOT all under one package - PermissionGatherEvent(.Nodes) is in net.neoforged.neoforge.server.permission.events, while PermissionNode/PermissionTypes are in net.neoforged.neoforge.server.permission.nodes. PermissionNode public ctor: PermissionNode(String modID, String nodeName, PermissionType<T> type, PermissionResolver<T> defaultResolver, PermissionDynamicContextKey<?>... dynamics) (or the ResourceLocation-first overload). ModList=net.neoforged.fml.ModList and FMLPaths=net.neoforged.fml.loading.FMLPaths are correct (stable NeoForge packages).
    - evidence: https://nekoyue.github.io/ForgeJavaDocs-NG/javadoc/1.21.x-neoforge/net/neoforged/neoforge/event/tick/ (ServerTickEvent.Pre/Post); .../event/server/package-summary.html (ServerStartedEvent/ServerStartingEvent/ServerStoppingEvent); .../server/permission/events/PermissionGatherEvent.Nodes.html (package events); .../server/permission/nodes/PermissionNode.html (package nodes; ctor: String modID, String nodeName, PermissionType<T>, PermissionResolver<T>, PermissionDynamicContextKey<?>...); skeleton uses net.neoforged.bus.api.SubscribeEvent (/mnt/hermes/projects/SEFPORTED/.../Sefported.java line 19) and net.neoforged.neoforge.event.server.ServerStartingEvent (line 28)
- **CORRECTED** — compileOnly mod deps via fg.deobf + flatDir lib/: ftb-essentials-forge:2001.2.2, net.luckperms:api:5.4, top.theillusivec4.curios:curios-forge:5.14.1+1.20.1, plus 1.20.1 lib/ jars
    - claim: Re-express under ModDev: luckperms compileOnly 'net.luckperms:api:5.4' (unchanged, no fg.deobf); FTB Essentials 1.21.1 NeoForge = 2101.1.x (needs FTB Library too) via FTB maven or local jars; Curios 1.21.1 NeoForge = top.theillusivec4.curios:curios-neoforge:<ver> via maven.theillusivec4.top, compileOnly, NO fg.deobf; flatDir replaced; old 1.20.1 Forge jars unusable.
    - verified → Mostly CONFIRMED with version specifics. CONFIRMED: fg.deobf does not exist under ModDevGradle (it was a ForgeGradle API) - all deps are plain compileOnly/runtimeOnly because NeoForge dev/runtime are Mojang-mapped (no remap step). net.luckperms:api:5.4 exists on Maven Central and is a platform-agnostic API jar, valid for 1.21.1 (no remap). Curios for 1.21.1 NeoForge is top.theillusivec4.curios:curios-neoforge via https://maven.theillusivec4.top/releases; concrete 1.21.1 versions exist (e.g. 9.5.1+1.21.1, 9.4.2+1.21.1, 9.2.0+1.21.1) - the API is typically pulled with the ':api' classifier: compileOnly "top.theillusivec4.curios:curios-neoforge:9.5.1+1.21.1:api". FTB Essentials for 1.21.1 NeoForge is the 2101.1.x line (e.g. 2101.1.5-2101.1.9) and FTB Library 2101.1.x is required. The old 1.20.1 Forge jars in lib/ are indeed unusable. UNVERIFIED: the exact FTB Maven group:artifact coordinate for compile-time use (FTB publishes via its own maven; CurseForge maven 'curse.maven:...' is an alternative) was not confirmed to a precise string; recommend confirming on the FTB maven before pinning.
    - evidence: https://repo1.maven.org/maven2/net/luckperms/api/5.4/ (api-5.4.jar exists); https://mvnrepository.com/artifact/top.theillusivec4.curios/curios-neoforge/9.5.0-alpha.3+1.21.1 and Modrinth curios-neoforge 9.5.1+1.21.1 / 9.2.0+1.21.1 (maven.theillusivec4.top); FTB Essentials CurseForge files 2101.1.5-2101.1.9 [NEOFORGE][1.21.1] (curseforge.com/minecraft/mc-mods/ftb-essentials/files); FTB Library [NEOFORGE][1.21.1] 2101.1.x (curseforge.com/.../ftb-library-forge). fg.deobf removal: ModDevGradle has no ForgeGradle fg extension (docs.neoforged.net/toolchain/docs/plugins/mdg)
- **CORRECTED** — parchment_minecraft_version=1.21.11 feeding neoForge.parchment{minecraftVersion}; settings.gradle pluginManagement (Forge maven) and foojay 0.7.0
    - claim: Fix parchment_minecraft_version 1.21.11 -> 1.21.1; parchment_mappings_version=2025.12.20 should be a date that actually published for 1.21.1 (verify on ParchmentMC maven); settings.gradle is already NeoForge-correct (maven.neoforged.net, foojay 0.8.0); OLD Forge settings.gradle discarded.
    - verified → CONFIRMED that parchment_minecraft_version must be 1.21.1 (not 1.21.11 - not a real MC version). CORRECTED on the mappings date: parchment_mappings_version=2025.12.20 does NOT exist for parchment-1.21.1; the latest (and final) published version on the org.parchmentmc.data:parchment-1.21.1 artifact is 2024.11.17. Use parchment_mappings_version=2024.11.17 with parchment_minecraft_version=1.21.1. The current SEFPORTED skeleton settings.gradle is already NeoForge-correct (mavenLocal + gradlePluginPortal + https://maven.neoforged.net/releases, foojay-resolver-convention 0.8.0); the old Forge settings (maven.minecraftforge.net, foojay 0.7.0) found at /mnt/hermes/projects/SEFPORTED/SourceCodeOld/Server-Essentials-Forge/settings.gradle is correctly discarded.
    - evidence: https://mvnrepository.com/artifact/org.parchmentmc.data/parchment-1.21.1 (latest version 2024.11.17, released 2024-11-17; no 2025.x exists); skeleton /mnt/hermes/projects/SEFPORTED/gradle.properties lines 10 (minecraft_version=1.21.1), 21 (parchment_minecraft_version=1.21.11), 22 (parchment_mappings_version=2025.12.20); /mnt/hermes/projects/SEFPORTED/settings.gradle (maven.neoforged.net/releases, foojay 0.8.0)
- **CONFIRMED** — pack.mcmeta pack_format=10 (1.20.1)
    - claim: For 1.21.1 pack_format must be bumped (1.21.1 uses 34 for resources / 48 for data); verify whether a pack.mcmeta is still needed under the NeoForge skeleton.
    - verified → CONFIRMED on the numbers: for Minecraft 1.21-1.21.1 resource packs use pack_format 34 and data packs use pack_format 48. The old value 10 (1.20.1) must be bumped if a pack.mcmeta is present. Note: the current SEFPORTED skeleton (ModDev MDK) does NOT include a pack.mcmeta at all - mod resources/data are loaded via the mod jar/neoforge.mods.toml without a top-level pack.mcmeta, so for this server-only mod a pack.mcmeta is generally not required; only add one (with 34/48) if you ship a bundled resource or data pack. Low impact, as stated.
    - evidence: https://minecraft.wiki/w/Pack_format (1.21-1.21.1 resources=34, data=48); planetminecraft datapack format blog; SEFPORTED skeleton has no pack.mcmeta (verified by file listing under /mnt/hermes/projects/SEFPORTED/src - only neoforge.mods.toml, mixins json, lang/en_us.json present)


### Registration & event-bus model (`registration-bus`)


**Summary:** The mod uses a mix of (a) annotation-scanned static @Mod.EventBusSubscriber handlers (InvLock, DisableBuilding, BannedItems, Freeze, VanishEventListener) and (b) manually-registered instance handlers wired through a custom loader.register() helper that wraps MinecraftForge.EVENT_BUS.register. Every Forge event-bus import (eventbus.api.*, Mod.EventBusSubscriber, MinecraftForge.EVENT_BUS, FMLJavaModLoadingContext) must move to the NeoForge namespace, Bus.FORGE becomes Bus.GAME, and FMLJavaModLoadingContext is gone (the @Mod constructor must take IEventBus+ModContainer). There are NO DeferredRegister/RegistryObject/@ObjectHolder anywhere; the only registry usage is read-only ForgeRegistries.BLOCKS/ITEMS lookups (3 files, 9 sites) which map mechanically to BuiltInRegistries.BLOCK/ITEM. Overall risk is MEDIUM-HIGH: the bus mechanics are mechanical, but the @Mod constructor signature change, the loader/MlContext rewrite, and the MOD-vs-GAME bus assignment of ConfigurationEventHandler are load-bearing and must be correct or the mod won't load.


**Findings (13):**


- **[LOW] loader.register() wraps net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(Object)**
  - _file:line:_ src/main/java/com/enviouse/sef/utils/loader.java:3,15
  - _→ NeoForge 1.21.1:_ Import net.neoforged.neoforge.common.NeoForge; body becomes NeoForge.EVENT_BUS.register(ToRegister). The EVENT_BUS API (register/addListener/post) is identical in shape on NeoForge. This helper is the central registration choke-point; all 6 loader.register call sites in ServerEssentialsForge.java (lines 73,83,84,87,88,89) flow through it unchanged.
  - _notes:_ Pure import swap; register(Object) signature unchanged. Class name 'loader' is lowercase but that is style, not a port issue.


- **[MEDIUM] loader.MlContext(): IExtensionPoint.DisplayTest + NetworkConstants.IGNORESERVERONLY (server-only marker) and ModLoadingContext.registerExtensionPoint**
  - _file:line:_ src/main/java/com/enviouse/sef/utils/loader.java:4,8,17-24
  - _→ NeoForge 1.21.1:_ DELETE the entire MlContext() method and its call at ServerEssentialsForge.java:71. NetworkConstants and IExtensionPoint.DisplayTest are GONE in NeoForge. Replace with displayTest="IGNORE_ALL_VERSIONS" in the [[mods]] block of neoforge.mods.toml — no Java code. This is how the server-only mod stays installable.
  - _notes:_ Cross-dimension: belongs to mods.toml/metadata work, but the Java call must be removed here. Per VERIFIED facts this is the correct replacement.


- **[HIGH] loader.MLConfig(): ModLoadingContext.get().registerConfig(ModConfig.Type, IConfigSpec, fileName)**  _(uncertain — needs verification)_
  - _file:line:_ src/main/java/com/enviouse/sef/utils/loader.java:5,6,7,13,26-29
  - _→ NeoForge 1.21.1:_ registerConfig moved OFF ModLoadingContext ONTO ModContainer: modContainer.registerConfig(type, spec, "sef/common.toml"). IConfigSpec<?> param must become net.neoforged.fml.config.IConfigSpec (signature CHANGED in NeoForge) or, better, accept the ModConfigSpec directly. ModConfig stays net.neoforged.fml.config.ModConfig. Requires threading the ModContainer from the @Mod constructor into this helper (it currently uses the static ModLoadingContext.get()).
  - _notes:_ Depends on config dimension (ForgeConfigSpec->ModConfigSpec) AND on the @Mod constructor rewrite to obtain ModContainer. The IConfigSpec<?> generic parameter type changed; flag for config-dimension owner. The static mlc field (ModLoadingContext.get() at line 13) should be dropped in favor of the injected ModContainer.


- **[HIGH] @Mod constructor uses FMLJavaModLoadingContext.get().getModEventBus() and ModLoadingContext.get().registerConfig**
  - _file:line:_ src/main/java/com/enviouse/sef/ServerEssentialsForge.java:28,32,82,76
  - _→ NeoForge 1.21.1:_ Change constructor to ServerEssentialsForge(IEventBus modEventBus, ModContainer modContainer) (FML injection). Use modEventBus.addListener(this::loadComplete) instead of FMLJavaModLoadingContext.get().getModEventBus() (line 82). Use modContainer.registerConfig(ModConfig.Type.SERVER, VanishConfig.SERVER_SPEC, "sef-vanish-server.toml") instead of ModLoadingContext.get().registerConfig (line 76). FMLJavaModLoadingContext is GONE.
  - _notes:_ Load-bearing: wrong constructor signature = mod fails to construct. The constructor must pass modContainer to loader.MLConfig and modEventBus to wherever MOD-bus listeners are added. @SuppressWarnings("removal") can be dropped.


- **[MEDIUM] MinecraftForge.EVENT_BUS.addListener(...) for RegisterCommandsEvent and PermissionGatherEvent.Nodes**  _(uncertain — needs verification)_
  - _file:line:_ src/main/java/com/enviouse/sef/ServerEssentialsForge.java:26,79,80,93,98
  - _→ NeoForge 1.21.1:_ net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(...). Both RegisterCommandsEvent and PermissionGatherEvent.Nodes fire on the GAME (NeoForge) bus, so these stay on EVENT_BUS. RegisterCommandsEvent -> net.neoforged.neoforge.event.RegisterCommandsEvent; PermissionGatherEvent.Nodes -> net.neoforged.neoforge.server.permission.events.PermissionGatherEvent.Nodes; PermissionNode/PermissionTypes -> net.neoforged.neoforge.server.permission.nodes.*.
  - _notes:_ addListener with explicit event type lambda works identically. VERIFY PermissionGatherEvent fires on GAME bus in NeoForge 1.21.1 (it did on Forge); if it moved to MOD bus these two addListener calls must move to modEventBus. PermissionNode ctor verified separately below.


- **[LOW] @Mod.EventBusSubscriber (default bus, GAME-equivalent) on STATIC-handler classes: InvLockEventHandler, DisableBuildingEventHandler, BannedItemsEventHandler, FreezeEventHandler**
  - _file:line:_ src/main/java/com/enviouse/sef/invlock/InvLockEventHandler.java:11,20; src/main/java/com/enviouse/sef/disablebuilding/DisableBuildingEventHandler.java:10,18; src/main/java/com/enviouse/sef/banned/BannedItemsEventHandler.java:21,31; src/main/java/com/enviouse/sef/freeze/FreezeEventHandler.java:12,24
  - _→ NeoForge 1.21.1:_ @net.neoforged.fml.common.EventBusSubscriber (now TOP-LEVEL, not nested under @Mod). Default bus is GAME, which is correct for all four (block/interact/command events). All four handler methods are static, so annotation-scan registration still works. Add modid=ServerEssentialsForge.MODID for clarity (optional). Drop the import net.minecraftforge.fml.common.Mod.
  - _notes:_ These all subscribe to game events (BlockEvent, PlayerInteractEvent, PlayerContainerEvent, CommandEvent, AttackEntityEvent, EntityItemPickupEvent) so GAME bus is correct. Mechanical but multi-site. EntityItemPickupEvent rename flagged separately as the event-types dimension.


- **[MEDIUM] @EventBusSubscriber default-bus classes with INSTANCE (non-static) handlers also manually registered: PermissionsHandler, PlayerEventHandler, ChatEventHandler, ExternalModLoadingEvent, CommandRegistrationHandler**
  - _file:line:_ src/main/java/com/enviouse/sef/config/PermissionsHandler.java:13,20,181; src/main/java/com/enviouse/sef/events/PlayerEventHandler.java:20,30; src/main/java/com/enviouse/sef/events/ChatEventHandler.java:32,34; src/main/java/com/enviouse/sef/events/ExternalModLoadingEvent.java:13,15; src/main/java/com/enviouse/sef/events/CommandRegistrationHandler.java:35,37
  - _→ NeoForge 1.21.1:_ Swap annotation to net.neoforged.fml.common.EventBusSubscriber (GAME default, correct). IMPORTANT: these classes carry the @EventBusSubscriber annotation but their @SubscribeEvent methods are INSTANCE methods, and they are ALSO manually registered via loader.register(instance) (ServerEssentialsForge.java:73,84,87,88,89). FML annotation-scan only auto-registers STATIC handlers, so the annotation is effectively a no-op for these and registration happens via the instance object. This dual pattern is preserved 1:1 under NeoForge (NeoForge EventBusSubscriber scan also only handles static methods). Keep the manual loader.register(instance) calls; the annotation can stay or be removed without behavior change.
  - _notes:_ Subtle: do NOT 'fix' these by making methods static AND keeping loader.register, or handlers fire twice. The current behavior = registered once via instance. Preserve exactly. CommandRegistrationHandler.registerCommands and PermissionsHandler.registerPermissionNodes are instance methods.


- **[LOW] VanishEventListener @EventBusSubscriber(modid=...) with STATIC handlers**
  - _file:line:_ src/main/java/com/enviouse/sef/vanish/VanishEventListener.java:26,32
  - _→ NeoForge 1.21.1:_ @net.neoforged.fml.common.EventBusSubscriber(modid = ServerEssentialsForge.MODID). Default GAME bus is correct (it subscribes to ServerStarted/Stopped, PlayerLoggedIn, TickEvent, interact, AttackEntity, LivingChangeTarget, ProjectileImpact, VanillaGameEvent — all GAME). All handlers static, so annotation-scan auto-registration works. It is NOT also in loader.register, so registration is annotation-only here.
  - _notes:_ modid already present. Note: TickEvent.ServerTickEvent/PlayerTickEvent inside this class are REMOVED in NeoForge (tick dimension), but the bus assignment itself is correct.


- **[MEDIUM] ConfigurationEventHandler @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD) with STATIC ModConfigEvent handlers**
  - _file:line:_ src/main/java/com/enviouse/sef/config/ConfigurationEventHandler.java:7,8,10,24-31
  - _→ NeoForge 1.21.1:_ @net.neoforged.fml.common.EventBusSubscriber(modid = ServerEssentialsForge.MODID, bus = EventBusSubscriber.Bus.MOD). Bus.MOD is UNCHANGED (only Bus.FORGE->Bus.GAME renamed). ModConfigEvent -> net.neoforged.fml.event.config.ModConfigEvent.Reloading/.Loading (fires on MOD bus). Both handler methods are static so annotation-scan works. This is the ONLY MOD-bus subscriber in the codebase.
  - _notes:_ Critical that this stays Bus.MOD — ModConfigEvent only fires on the MOD bus. If accidentally moved to GAME, config reload hooks silently never fire. ModConfigEvent.Loading/.Reloading package confirmed per VERIFIED facts.


- **[LOW] ForgeRegistries.BLOCKS.getKey(Block) — banned-block matching/sweeps**
  - _file:line:_ src/main/java/com/enviouse/sef/banned/BannedItemsEventHandler.java:22,57; src/main/java/com/enviouse/sef/banned/BannedItemsManager.java:26,259,467
  - _→ NeoForge 1.21.1:_ net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(block) returns the ResourceLocation key (non-null sentinel minecraft:air for unregistered, not null — see note). Import net.minecraft.core.registries.BuiltInRegistries. ForgeRegistries.BLOCKS is gone.
  - _notes:_ Behavioral nuance: BuiltInRegistries.BLOCK.getKey returns the default-key (minecraft:air) rather than null for an unregistered block, whereas the code checks 'rl != null'. For real placed/broken blocks this is always non-null so behavior is equivalent, but the null-guards become dead code (harmless). Mechanical.


- **[LOW] ForgeRegistries.ITEMS.getKey(Item) — banned-item matching, confiscation notify, /banned addhand**
  - _file:line:_ src/main/java/com/enviouse/sef/banned/BannedItemsManager.java:247,267,483; src/main/java/com/enviouse/sef/banned/BannedItemsCommands.java:303
  - _→ NeoForge 1.21.1:_ net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item). Import BuiltInRegistries (BannedItemsCommands.java:303 uses fully-qualified net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey — swap to BuiltInRegistries.ITEM.getKey).
  - _notes:_ Same default-key (minecraft:air) nuance as BLOCK; null-guards become effectively dead but harmless. Mechanical multi-site.


- **[LOW] ForgeRegistries.ITEMS.getKeys() / .getKeys().size() — Brigadier item-id suggestions**  _(uncertain — needs verification)_
  - _file:line:_ src/main/java/com/enviouse/sef/banned/BannedItemsManager.java:519,520
  - _→ NeoForge 1.21.1:_ net.minecraft.core.registries.BuiltInRegistries.ITEM.keySet() returns Set<ResourceLocation>. Replace ForgeRegistries.ITEMS.getKeys() -> BuiltInRegistries.ITEM.keySet(); .size() works on the Set.
  - _notes:_ vanilla Registry exposes keySet() (not getKeys()); confirm method name keySet() on net.minecraft.core.Registry in 1.21.1. Returns the same set of registered item ResourceLocations.


- **[HIGH] PermissionNode ctor + PermissionAPI + PermissionGatherEvent.Nodes + PermissionTypes (used by PermissionsHandler and Vanish permission registration)**  _(uncertain — needs verification)_
  - _file:line:_ src/main/java/com/enviouse/sef/config/PermissionsHandler.java:14,15,16,17,185,193,198-200,207; src/main/java/com/enviouse/sef/banned/BannedItemsManager.java:282,284; src/main/java/com/enviouse/sef/ServerEssentialsForge.java:98,101,108
  - _→ NeoForge 1.21.1:_ net.minecraftforge.server.permission.* -> net.neoforged.neoforge.server.permission.*: PermissionAPI, nodes.PermissionNode, nodes.PermissionTypes, events.PermissionGatherEvent.Nodes. Constructor new PermissionNode<>(MODID(String), id(String), PermissionTypes.BOOLEAN, resolver) and node.setInformation(Component, Component), pge.addNodes(...), PermissionAPI.getOfflinePermission(uuid,node)/getPermission(player,node) shapes look preserved.
  - _notes:_ VERIFY exact PermissionNode constructor signature in NeoForge 1.21.1 (the String-modid+String-node form vs a ResourceLocation form), setInformation signature, getOfflinePermission existence, and which BUS PermissionGatherEvent.Nodes fires on (affects the two NeoForge.EVENT_BUS.addListener calls at ServerEssentialsForge.java:79-80 and whether @EventBusSubscriber GAME picks up the registerPermissionNodes handler — note that one is an INSTANCE method registered via loader.register). Permissions are a core feature.


**Ordering notes:**

- Config dimension must land first: loader.MLConfig and the @Mod constructor depend on ModConfigSpec (ForgeConfigSpec replacement) and on the IConfigSpec parameter-type change.
- The @Mod constructor rewrite (IEventBus modEventBus, ModContainer modContainer) is a prerequisite for both loader.MLConfig (needs ModContainer) and the MOD-bus listener wiring (loadComplete via modEventBus). Do the constructor before touching loader.java.
- loader.MlContext deletion must be coordinated with the neoforge.mods.toml metadata dimension (add displayTest="IGNORE_ALL_VERSIONS") so the server-only behavior isn't lost.
- Permissions dimension (PermissionNode/PermissionGatherEvent) is independent of registration mechanics but its bus determination feeds the addListener decisions in this dimension — resolve the PermissionGatherEvent bus question before finalizing ServerEssentialsForge.java:79-80.
- ForgeRegistries->BuiltInRegistries swaps are self-contained (3 files) and can be done at any time; they have no ordering dependency.
- Tick/event-type dimension owns TickEvent removal and EntityItemPickupEvent rename inside these same handler classes; the bus/annotation work here should be done in the same pass to avoid touching the files twice.


**Open questions (this dimension):**

- Does NeoForge 1.21.1 PermissionGatherEvent.Nodes fire on the GAME bus (NeoForge.EVENT_BUS) or the MOD bus? This determines whether the two NeoForge.EVENT_BUS.addListener calls (ServerEssentialsForge.java:79-80) and the instance handler PermissionsHandler.registerPermissionNodes stay on EVENT_BUS or move to the modEventBus.
- Exact NeoForge 1.21.1 PermissionNode<T> constructor signature — does it still take (String modid, String nodeName, PermissionType, resolver), and does node.setInformation(Component, Component) still exist?
- Does net.minecraft.core.Registry expose keySet() (1.21.1 Mojang mappings) as the replacement for ForgeRegistries.ITEMS.getKeys()? Confirm method name.
- What is the IConfigSpec<?> -> NeoForge IConfigSpec parameter change in loader.MLConfig — should the helper accept ModConfigSpec directly to avoid the changed IConfigSpec generic? (config dimension to confirm).
- Confirm BuiltInRegistries.BLOCK/ITEM.getKey returns minecraft:air (not null) for unregistered objects under 1.21.1 — affects whether existing 'rl != null' guards are dead but harmless.


**Verification verdicts:**

- **CONFIRMED** — loader.MLConfig(): ModLoadingContext.get().registerConfig(ModConfig.Type, IConfigSpec, fileName)
    - claim: registerConfig moved OFF ModLoadingContext ONTO ModContainer: modContainer.registerConfig(type, spec, "sef/common.toml"); IConfigSpec param becomes raw net.neoforged.fml.config.IConfigSpec (or pass ModConfigSpec directly); ModConfig stays net.neoforged.fml.config.ModConfig; thread ModContainer from @Mod constructor.
    - verified → same
    - evidence: FancyModLoader@1.21.1 net/neoforged/fml/ModContainer.java: imports net.neoforged.fml.config.{IConfigSpec,ModConfig}; defines `public void registerConfig(ModConfig.Type type, IConfigSpec configSpec)` (L102) and `public void registerConfig(ModConfig.Type type, IConfigSpec configSpec, String fileName)` (L119). ModLoadingContext.java@1.21.1 no longer contains registerConfig (grep found only the class decl). ModConfigSpec.java@NeoForge 1.21.1 (net.neoforged.neoforge.common) `public class ModConfigSpec implements IConfigSpec` (raw, L56) importing net.neoforged.fml.config.IConfigSpec — so ModConfigSpec is accepted directly and the generic param is raw. Docs: https://docs.neoforged.net/docs/1.21.1/misc/config/
- **CONFIRMED** — @Mod constructor uses FMLJavaModLoadingContext.get().getModEventBus() and ModLoadingContext.get().registerConfig
    - claim: Change ctor to ServerEssentialsForge(IEventBus modEventBus, ModContainer modContainer); use modEventBus.addListener(this::loadComplete); use modContainer.registerConfig(ModConfig.Type.SERVER, VanishConfig.SERVER_SPEC, "sef-vanish-server.toml"); FMLJavaModLoadingContext is GONE.
    - verified → same
    - evidence: NeoForged docs Mod Files (https://docs.neoforged.net/docs/1.21.1/gettingstarted/modfiles/) lists injectable @Mod ctor args: IEventBus (mod-specific event bus), ModContainer, FMLModContainer, Dist, in any order. FancyModLoader@1.21.1 net/neoforged/fml/javafmlmod/ contains only AutomaticEventSubscriber, FMLJavaModLanguageProvider, FMLModContainer — no FMLJavaModLoadingContext (also `gh search code` for the symbol in the repo returns nothing). registerConfig confirmed on ModContainer (see item 1). ModConfigSpec implements IConfigSpec so SERVER_SPEC is a valid arg.
- **CONFIRMED** — MinecraftForge.EVENT_BUS.addListener(...) for RegisterCommandsEvent and PermissionGatherEvent.Nodes
    - claim: NeoForge.EVENT_BUS.addListener(...); both fire on the GAME bus; RegisterCommandsEvent -> net.neoforged.neoforge.event.RegisterCommandsEvent; PermissionGatherEvent.Nodes -> net.neoforged.neoforge.server.permission.events.PermissionGatherEvent.Nodes; PermissionNode/PermissionTypes -> net.neoforged.neoforge.server.permission.nodes.*
    - verified → same
    - evidence: NeoForge@1.21.1: net/neoforged/neoforge/event/RegisterCommandsEvent.java `public class RegisterCommandsEvent extends Event` (package net.neoforged.neoforge.event) — extends plain Event, NOT IModBusEvent, so it dispatches on NeoForge.EVENT_BUS (game bus). net/neoforged/neoforge/server/permission/events/PermissionGatherEvent.java `public abstract class PermissionGatherEvent extends Event` with inner `public static class Nodes extends PermissionGatherEvent` (package net.neoforged.neoforge.server.permission.events) — also not IModBusEvent, game bus. PermissionNode/PermissionTypes confirmed in net.neoforged.neoforge.server.permission.nodes (see item 5). The common bus singleton is net.neoforged.neoforge.common.NeoForge.EVENT_BUS.
- **CONFIRMED** — ForgeRegistries.ITEMS.getKeys() / .getKeys().size() — Brigadier item-id suggestions
    - claim: net.minecraft.core.registries.BuiltInRegistries.ITEM.keySet() returns Set<ResourceLocation>; replace getKeys() -> keySet(); .size() works on the Set.
    - verified → same
    - evidence: Javadoc (nekoyue ForgeJavaDocs-NG 1.21.x-neoforge) BuiltInRegistries: `static final DefaultedRegistry<Item> ITEM`. Registry javadoc: `Set<ResourceLocation> keySet()` exists (and `Set<ResourceKey<T>> registryKeySet()`); no `getKeys()` method — getKeys() was a Forge IForgeRegistry method, removed under Mojang mappings. Set.size() is standard java.util.Set. So BuiltInRegistries.ITEM.keySet() -> Set<ResourceLocation>, .size() valid. https://nekoyue.github.io/ForgeJavaDocs-NG/javadoc/1.21.x-neoforge/net/minecraft/core/Registry.html
- **CONFIRMED** — PermissionNode ctor + PermissionAPI + PermissionGatherEvent.Nodes + PermissionTypes (PermissionsHandler & Vanish)
    - claim: net.minecraftforge.server.permission.* -> net.neoforged.neoforge.server.permission.*: PermissionAPI, nodes.PermissionNode, nodes.PermissionTypes, events.PermissionGatherEvent.Nodes. Ctor new PermissionNode<>(MODID, id, PermissionTypes.BOOLEAN, resolver); node.setInformation(Component, Component); pge.addNodes(...); PermissionAPI.getOfflinePermission(uuid,node)/getPermission(player,node).
    - verified → same
    - evidence: NeoForge@1.21.1 sources: PermissionNode.java (package net.neoforged.neoforge.server.permission.nodes) `public final class PermissionNode<T>` with `public PermissionNode(String modID, String nodeName, PermissionType<T> type, PermissionResolver<T> defaultResolver, PermissionDynamicContextKey... dynamics)` (L67) and `public PermissionNode setInformation(Component readableName, Component description)` (L99). PermissionTypes.java (same package) `public static final PermissionType<Boolean> BOOLEAN` (L15). PermissionAPI.java (package net.neoforged.neoforge.server.permission) `public static <T> T getPermission(ServerPlayer player, PermissionNode<T> node, PermissionDynamicContext<?>... context)` (L61) and `public static <T> T getOfflinePermission(UUID player, PermissionNode<T> node, PermissionDynamicContext<?>... context)` (L78). PermissionGatherEvent.Nodes.addNodes(PermissionNode<?>...) (L69) in events pkg. All shapes preserved.


### Chat events & chat managers (the heart of the mod) (`chat-events`)


**Summary:** The chat core is centered on a single Forge `ServerChatEvent` handler (`ChatEventHandler.onServerChat`) plus four pure-Java helper classes (ChatMessageManager, ChatReplyHandler, OpBulletinHandler, AdminChatHandler) and a broadcast helper (ServerMessageEvent). The breaking surface is almost entirely import/annotation-level: `ServerChatEvent`, `@SubscribeEvent`/`EventPriority`, and `@EventBusSubscriber` all move from `net.minecraftforge.*` to `net.neoforged.*`. The exact accessors actually used (`getPlayer()`, `getMessage().getString()`, `isCanceled()`, `setCanceled(true)`) are all expected to survive on NeoForge's `ServerChatEvent`, so this is MEDIUM/LOW mechanical work — the only real uncertainty is the precise `getMessage()` accessor name on NeoForge. Crucially, the click-to-reply mechanism is PURE vanilla `ClickEvent`/`HoverEvent` on Components broadcast via `sendSystemMessage` (no custom packets, no Component JSON serialization), so the riskiest 1.20.5+ serialization changes do NOT touch this code path. Chat "logging" is just `LOGGER.info("[CHAT] ...")` to the standard log, not a separate logs/chat/ file.


**Findings (14):**


- **[HIGH] import net.minecraftforge.event.ServerChatEvent + @SubscribeEvent handler onServerChat(ServerChatEvent e)**
  - _file:line:_ src/main/java/com/enviouse/sef/events/ChatEventHandler.java:30, src/main/java/com/enviouse/sef/events/ChatEventHandler.java:72
  - _→ NeoForge 1.21.1:_ import net.neoforged.neoforge.event.ServerChatEvent; handler signature unchanged. This is the single load-bearing chat hook for the whole mod.
  - _notes:_ Class is verified to exist in NeoForge 1.21.x per ground-truth facts. Core feature of the mod; if the event or its accessors differ, all chat formatting/muting/admin-chat/reply interception breaks. Risk is HIGH only because it is the heart of the mod, not because the mapping is hard.


- **[MEDIUM] e.getMessage() returns the chat message Component; read via .getString() (line 80). No setMessage/getRawText/getUsername used.**  _(uncertain — needs verification)_
  - _file:line:_ src/main/java/com/enviouse/sef/events/ChatEventHandler.java:80
  - _→ NeoForge 1.21.1:_ On NeoForge ServerChatEvent the message is read via getMessage() (returns Component) per ground-truth; .getString() is vanilla Component and unchanged. The mod NEVER calls setMessage() — it cancels (setCanceled) and rebroadcasts its own Component instead, so the message-mutation accessor changes are irrelevant here.
  - _notes:_ VERIFY exact accessor name: ground-truth lists getMessage()/getRawText() as candidates. The mod only uses getMessage().getString() (read). If NeoForge renamed it (e.g. to getRawText()/getMessage() returning something else), this one line needs adjustment. Everything downstream is the mod's own logic, not the event.


- **[MEDIUM] e.isCanceled() / e.setCanceled(true) — ServerChatEvent cancellation**
  - _file:line:_ src/main/java/com/enviouse/sef/events/ChatEventHandler.java:74, src/main/java/com/enviouse/sef/events/ChatEventHandler.java:111, src/main/java/com/enviouse/sef/events/ChatEventHandler.java:136, src/main/java/com/enviouse/sef/events/ChatEventHandler.java:153, src/main/java/com/enviouse/sef/events/ChatEventHandler.java:169, src/main/java/com/enviouse/sef/events/ChatEventHandler.java:213
  - _→ NeoForge 1.21.1:_ ServerChatEvent is @Cancelable (ICancellableEvent) on NeoForge; isCanceled()/setCanceled(boolean) provided by net.neoforged.bus.api.ICancellableEvent. The mod ALWAYS cancels the vanilla broadcast (line 213) and re-broadcasts its own formatted line, so cancellation semantics must be preserved.
  - _notes:_ Cancellation API moved to net.neoforged.bus.api.ICancellableEvent but method names isCanceled()/setCanceled() are retained. The whole mod's chat replacement strategy = cancel + rebroadcast, so this must work.


- **[LOW] @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.HIGHEST, receiveCanceled = false) + import net.minecraftforge.eventbus.api.SubscribeEvent**
  - _file:line:_ src/main/java/com/enviouse/sef/events/ChatEventHandler.java:31, src/main/java/com/enviouse/sef/events/ChatEventHandler.java:71
  - _→ NeoForge 1.21.1:_ import net.neoforged.bus.api.SubscribeEvent; priority = net.neoforged.bus.api.EventPriority.HIGHEST. receiveCanceled attribute retained on NeoForge @SubscribeEvent.
  - _notes:_ Mechanical package swap. EventPriority.HIGHEST and receiveCanceled both exist on net.neoforged.bus.api. HIGHEST priority matters: mod wants first crack to intercept admin/private chat before other handlers (e.g. Discord bridge) see it.


- **[MEDIUM] @EventBusSubscriber (import net.minecraftforge.fml.common.Mod.EventBusSubscriber) — registers ChatEventHandler on the game event bus**  _(uncertain — needs verification)_
  - _file:line:_ src/main/java/com/enviouse/sef/events/ChatEventHandler.java:32, src/main/java/com/enviouse/sef/events/ChatEventHandler.java:34
  - _→ NeoForge 1.21.1:_ import net.neoforged.fml.common.EventBusSubscriber (now TOP-LEVEL, not nested in @Mod). Default bus is GAME, which matches the original (Bus.FORGE). NOTE: @EventBusSubscriber auto-registers but the handler methods here are INSTANCE (non-static) methods — @EventBusSubscriber only auto-registers STATIC handlers. The instance handler onServerChat must therefore be registered manually (NeoForge.EVENT_BUS.register(instance)) somewhere, OR the @EventBusSubscriber is effectively a no-op for the instance method. VERIFY how this class is actually registered (likely manual register of an instance for IReloadable).
  - _notes:_ Top-level annotation move is trivial. The non-static handler + @EventBusSubscriber mismatch is a pre-existing subtlety: NeoForge @EventBusSubscriber documents STATIC-only handlers. Since the class implements IReloadable (holds per-instance state: timestampFormat, markdownEnabled, loaded), it must be registered as an instance via NeoForge.EVENT_BUS.register(obj). Cross-check registration site during port.


- **[LOW] new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/ans <id> ") + new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component) — click-to-reply**
  - _file:line:_ src/main/java/com/enviouse/sef/events/ChatEventHandler.java:233, src/main/java/com/enviouse/sef/events/ChatEventHandler.java:234
  - _→ NeoForge 1.21.1:_ Vanilla net.minecraft.network.chat.ClickEvent / HoverEvent. In 1.21.1 the 2-arg constructors ClickEvent(Action, String) and HoverEvent(Action, Component) are UNCHANGED (the record-based ClickEvent/HoverEvent rework landed in 1.21.5, NOT 1.21.1). No change required for 1.21.1. Click-to-reply is confirmed PURE ClickEvent/HoverEvent on a Component — NO custom packets.
  - _notes:_ Confirmed: reply mechanism is a SUGGEST_COMMAND click that prefills '/ans <messageId> ' in the chat box (handled by ChatReplyHandler /ans command). No packet plumbing. For 1.21.1 these vanilla ctors are intact; only flag if the project later targets 1.21.5+.


- **[LOW] MutableComponent.withStyle(UnaryOperator<Style>) lambda using style.withClickEvent(...).withHoverEvent(...)**
  - _file:line:_ src/main/java/com/enviouse/sef/events/ChatEventHandler.java:232, src/main/java/com/enviouse/sef/events/ChatEventHandler.java:233, src/main/java/com/enviouse/sef/events/ChatEventHandler.java:234
  - _→ NeoForge 1.21.1:_ Vanilla net.minecraft.network.chat.Style.withClickEvent(ClickEvent)/withHoverEvent(HoverEvent) and MutableComponent.withStyle(UnaryOperator<Style>) — unchanged in 1.21.1.
  - _notes:_ Pure vanilla Style API, stable 1.20.1->1.21.1. No registry/HolderLookup needed because nothing is serialized to JSON here — Components are sent live via sendSystemMessage.


- **[LOW] getHoverClickEventStyle(Component old): old instanceof TranslatableContents; tcmp.getArgs(); arg instanceof MutableComponent; tc.getStyle().getClickEvent()**
  - _file:line:_ src/main/java/com/enviouse/sef/events/ChatEventHandler.java:58, src/main/java/com/enviouse/sef/events/ChatEventHandler.java:59, src/main/java/com/enviouse/sef/events/ChatEventHandler.java:60
  - _→ NeoForge 1.21.1:_ Vanilla TranslatableContents.getArgs() (returns Object[]) and Style.getClickEvent() are unchanged in 1.21.1. NOTE: this method is DEAD CODE — it is never called (grep shows only the declaration) and contains a pre-existing logic bug (a Component is never an instanceof TranslatableContents, which is a ComponentContents). Safe to port as-is or delete; no behavior depends on it.
  - _notes:_ Unused method with a latent bug; vanilla APIs it touches survive. Recommend deleting during port to reduce surface, but not required for compile.


- **[LOW] Component broadcast: player.sendSystemMessage(Component) and ServerMessageEvent.broadcastMessageVanishAware(player.level(), clickableMessage, player)**
  - _file:line:_ src/main/java/com/enviouse/sef/events/ChatEventHandler.java:241, src/main/java/com/enviouse/sef/events/ServerMessageEvent.java:19, src/main/java/com/enviouse/sef/events/ServerMessageEvent.java:37
  - _→ NeoForge 1.21.1:_ Vanilla Player.sendSystemMessage(Component), Level.getServer(), MinecraftServer.getPlayerList().getPlayers(), ServerPlayer — all unchanged in 1.21.1. The final chat line is built as MutableComponent (beforeMsg.append(msgComp.append(afterMsg))) and pushed live to each receiver via sendSystemMessage. No serialization.
  - _notes:_ ServerMessageEvent is a plain utility class (not a Forge event despite the name) — no Forge API. Vanish-aware broadcast depends only on VanishUtil (separate dimension). Component.append/empty/literal all stable.


- **[LOW] player.server.execute(Runnable) — defers record+broadcast to main server thread**
  - _file:line:_ src/main/java/com/enviouse/sef/events/ChatEventHandler.java:221
  - _→ NeoForge 1.21.1:_ Vanilla ServerPlayer.server field + MinecraftServer.execute(Runnable). With NeoForge running Mojang mappings at runtime, the field is named 'server' (already Mojang name here) — no SRG rename concern. Unchanged.
  - _notes:_ Direct field access player.server is the Mojang name; fine under NeoForge runtime mappings. ServerChatEvent fires off-thread, hence the execute() hop; behavior preserved.


- **[LOW] Chat logging: ServerEssentialsForge.LOGGER.info("[CHAT] " + clickableMessage.getString()) and [REPLY]/[ADMIN CHAT]/[HELPOP]/[MUTED] log lines**
  - _file:line:_ src/main/java/com/enviouse/sef/events/ChatEventHandler.java:240, src/main/java/com/enviouse/sef/chat/ChatReplyHandler.java:96, src/main/java/com/enviouse/sef/chat/AdminChatHandler.java:151
  - _→ NeoForge 1.21.1:_ Plain SLF4J/Log4j LOGGER (no MC/Forge API). The ground-truth mentions a logs/chat/ hook; this code does NOT write to a separate logs/chat/ file — it logs to the standard mod logger only. No NeoForge change needed.
  - _notes:_ ConfigHandler line 263 comment mentions 'chat logging' as part of enableChatReplies, but the actual chat-path code only emits LOGGER.info. No dedicated chat-log file writer found in these files. If a logs/chat/ writer exists it is elsewhere (not in this dimension). Component.getString() is vanilla, unchanged.


- **[LOW] ChatReplyHandler / AdminChatHandler / OpBulletinHandler command registration via CommandDispatcher<CommandSourceStack>, Commands.literal/argument, brigadier args**
  - _file:line:_ src/main/java/com/enviouse/sef/chat/ChatReplyHandler.java:24, src/main/java/com/enviouse/sef/chat/AdminChatHandler.java:38, src/main/java/com/enviouse/sef/chat/OpBulletinHandler.java:72
  - _→ NeoForge 1.21.1:_ All vanilla/brigadier: net.minecraft.commands.Commands, CommandSourceStack, com.mojang.brigadier.* — unchanged in 1.21.1. These register() methods are called from the mod's RegisterCommandsEvent handler (separate dimension); nothing Forge-specific inside them. sendSuccess(Supplier<Component>, boolean) and sendFailure(Component) signatures are 1.19.4+ vanilla and stable through 1.21.1.
  - _notes:_ sendSuccess takes Supplier<Component> (already used at OpBulletinHandler:81 etc.) and sendFailure takes Component — both current vanilla shapes. No change. Permission gating via PermissionsHandler is a separate dimension.


- **[MEDIUM] OpBulletinHandler file I/O: server.getServerDirectory().toPath() and Gson serialization of List<String>**  _(uncertain — needs verification)_
  - _file:line:_ src/main/java/com/enviouse/sef/chat/OpBulletinHandler.java:33, src/main/java/com/enviouse/sef/chat/OpBulletinHandler.java:42, src/main/java/com/enviouse/sef/chat/OpBulletinHandler.java:55
  - _→ NeoForge 1.21.1:_ VERIFY: MinecraftServer.getServerDirectory() return type changed in 1.21.x from File to java.nio.file.Path. In 1.20.1 it returned File (hence .toPath()). In 1.21.1 it returns Path directly, so '.toPath()' must be REMOVED (calling .toPath() on a Path is invalid). Gson serialization here is of List<String> (bulletin text), NOT Components, so the 1.20.5+ Component-JSON-needs-RegistryAccess change does NOT apply.
  - _notes:_ getServerDirectory() Path-vs-File change is a known 1.21.x vanilla signature change. Single site (OpBulletinHandler:33). Gson is plain string list -> no registry concern. The only Component-ish serialization in this whole dimension is none; bulletins are stored as raw '&'-format strings and re-parsed via TextFormatter at display time.


- **[LOW] TextFormatter Component construction (Component.empty/literal, Style.EMPTY, Style.withColor, TextColor.fromLegacyFormat, ChatFormatting, MutableComponent.append/withStyle)**
  - _file:line:_ src/main/java/com/enviouse/sef/TextFormatter.java:54, src/main/java/com/enviouse/sef/TextFormatter.java:59, src/main/java/com/enviouse/sef/TextFormatter.java:62, src/main/java/com/enviouse/sef/TextFormatter.java:92, src/main/java/com/enviouse/sef/TextFormatter.java:145
  - _→ NeoForge 1.21.1:_ All vanilla net.minecraft.network.chat.* (Component, MutableComponent, Style, TextColor) and net.minecraft.ChatFormatting — unchanged 1.20.1->1.21.1. Style.withColor(int) (line 92) and Style.withColor(TextColor) (line 59/145), TextColor.fromLegacyFormat(ChatFormatting), ChatFormatting.getByName(String) all retained. No serialization, so no HolderLookup/RegistryAccess needed.
  - _notes:_ TextFormatter is the central Component-builder for the entire chat dimension (every formatted line goes through stringToFormattedText). It uses ONLY in-memory Component/Style APIs that are stable in 1.21.1. This is the key evidence that Component JSON serialization changes do NOT affect this mod's chat path.


**Ordering notes:**

- Depends on the Config dimension being ported FIRST: ChatEventHandler.reloadConfigOptions() and nearly every handler read ConfigHandler.config.* (ForgeConfigSpec -> ModConfigSpec). Chat code won't compile/run until ConfigHandler is on ModConfigSpec.
- Depends on the Event-bus/entrypoint dimension: @EventBusSubscriber move + manual instance registration of ChatEventHandler must be coordinated with how the mod main class wires up NeoForge.EVENT_BUS and the IReloadable reload pass.
- Depends on the Permissions dimension: PermissionsHandler.playerHasPermission(...) is called throughout (mute relay, colored/styled/markdown/hex chat nodes, admin chat, helpop, /ans). Chat features silently degrade if permissions port is incomplete.
- Depends on the Commands (RegisterCommandsEvent) dimension: ChatReplyHandler.register / AdminChatHandler.register / OpBulletinHandler.register are invoked from the command-registration event; the click-to-reply ClickEvent target '/ans' only works if /ans is registered.
- Soft dependency on the Vanish/mod-integration dimensions: ServerMessageEvent.broadcastMessageVanishAware uses VanishUtil; FTBMuteChecker and SDLinkHideTracker are mod-compat (FTB/SDLink) and live in other dimensions — chat compiles only once those classes are ported.
- TextFormatter is shared infrastructure used by EVERY chat handler; port/verify it alongside (or before) the chat handlers since all formatted output flows through it.


**Open questions (this dimension):**

- Exact NeoForge ServerChatEvent message read accessor for 1.21.1: is it getMessage() (returns Component) as used at ChatEventHandler.java:80, or getRawText()/another name? Only one call site, but it gates the whole handler.
- How is ChatEventHandler actually registered? It is annotated @EventBusSubscriber but onServerChat is a NON-STATIC instance method (NeoForge @EventBusSubscriber auto-registers STATIC handlers only). The class holds per-instance reloadable state (IReloadable), so an instance must be manually registered via NeoForge.EVENT_BUS.register(...). Need to confirm the registration site so it doesn't silently stop receiving events.
- Does MinecraftServer.getServerDirectory() return Path (1.21.1) vs File (1.20.1)? If Path, remove the .toPath() at OpBulletinHandler.java:33.
- Is there a dedicated logs/chat/ file writer anywhere (the prompt mentioned one)? Within this dimension only LOGGER.info('[CHAT]...') exists — confirm no separate chat-log appender lives in another dimension's class.
- Does the project intend to stay on 1.21.1 (not 1.21.5+)? ClickEvent/HoverEvent 2-arg constructors at ChatEventHandler.java:233-234 are fine for 1.21.1 but were reworked into records in 1.21.5 — would become breaking if the target slips.


**Verification verdicts:**

- **CONFIRMED** — import net.minecraftforge.event.ServerChatEvent + @SubscribeEvent onServerChat(ServerChatEvent e) — the single load-bearing chat hook (ChatEventHandler.java lines 30, 72)
    - claim: Change import to net.neoforged.neoforge.event.ServerChatEvent; handler signature unchanged.
    - verified → same — import net.neoforged.neoforge.event.ServerChatEvent; handler signature onServerChat(ServerChatEvent e) unchanged. Note @SubscribeEvent must also be re-imported from net.neoforged.bus.api.SubscribeEvent and EventPriority from net.neoforged.bus.api.EventPriority (currently net.minecraftforge.eventbus.api.* on lines 31 and 71).
    - evidence: NeoForge 1.21.x javadoc (ForgeJavaDocs-NG): https://nekoyue.github.io/ForgeJavaDocs-NG/javadoc/1.21.x-neoforge/net/neoforged/neoforge/event/ServerChatEvent.html — class is in package net.neoforged.neoforge.event, fired on the logical server when a ServerboundChatPacket is received. Event bus package move net.minecraftforge.eventbus -> net.neoforged.bus.api: https://neoforged.net/news/20.2eventbus-changes/
- **CONFIRMED** — e.getMessage().getString() reads the chat message Component (line 80); mod cancels via setCanceled and rebroadcasts its own Component, never calls setMessage()
    - claim: On NeoForge ServerChatEvent getMessage() returns Component; .getString() is unchanged vanilla Component; setMessage() unused so message-mutation accessor changes are irrelevant.
    - verified → same — getMessage() returns net.minecraft.network.chat.Component; Component.getString() is unchanged. Confirmed the event implements ICancellableEvent, so e.isCanceled() (line 74) and e.setCanceled(true) (lines 111, 213, etc.) remain valid (note: NeoForge cancellation is via ICancellableEvent, not the old Event.isCancelable/setCanceled inheritance, but the method names are identical, so no source change needed).
    - evidence: NeoForge 1.21.x javadoc: https://nekoyue.github.io/ForgeJavaDocs-NG/javadoc/1.21.x-neoforge/net/neoforged/neoforge/event/ServerChatEvent.html — getMessage() returns Component, getPlayer() returns ServerPlayer, class implements ICancellableEvent ('This event is cancellable... if the event is cancelled, the message will not be sent to clients'). setMessage(Component) exists but is not invoked by ChatEventHandler.java (verified: only getMessage(), getPlayer(), isCanceled(), setCanceled() are used).
- **CONFIRMED** — @EventBusSubscriber (import net.minecraftforge.fml.common.Mod.EventBusSubscriber) on ChatEventHandler (line 32, 34); registers handler on FORGE/game bus
    - claim: Use top-level net.neoforged.fml.common.EventBusSubscriber; default bus is GAME (matches old Bus.FORGE); but @EventBusSubscriber only auto-registers STATIC handlers, while onServerChat is an INSTANCE method, so it must be registered manually somewhere (likely NeoForge.EVENT_BUS.register(instance)).
    - verified → same — import net.neoforged.fml.common.EventBusSubscriber (top-level, no longer nested in @Mod). Default bus is EventBusSubscriber.Bus.GAME (= old Bus.FORGE). VERIFIED against source: ChatEventHandler.onServerChat IS a non-static instance method, and the instance is registered MANUALLY — ServerEssentialsForge.java line 54 creates `new ChatEventHandler()` and line 87 calls loader.register(chatHandler) inside loadComplete(), where loader.register (utils/loader.java line 15) does MinecraftForge.EVENT_BUS.register(instance). Therefore the @EventBusSubscriber annotation on this class is effectively a NO-OP for the instance handler (it registers the class, but the static-only scan finds no static @SubscribeEvent methods). Port action: keep manual registration via NeoForge.EVENT_BUS.register(chatHandler); the @EventBusSubscriber annotation can be left (harmless) or removed. Do NOT rely on @EventBusSubscriber to wire up onServerChat.
    - evidence: NeoForge docs (events): https://docs.neoforged.net/docs/concepts/events/ — '@EventBusSubscriber ... all handlers must be static, too' and 'The default bus is Bus.GAME'. NeoForge eventbus static-ness rule: https://neoforged.net/news/20.2eventbus-changes/ — 'registrations with a Class must be static, and registrations with an object must be non-static'. Source registration path: ServerEssentialsForge.java:54,87 -> utils/loader.java:15 (EVENT_BUS.register(instance)).
- **CONFIRMED** — OpBulletinHandler: server.getServerDirectory().toPath().resolve(...) (OpBulletinHandler.java line 33) + Gson serialization of List<String> (LIST_TYPE, line 28)
    - claim: MinecraftServer.getServerDirectory() now returns Path in 1.21.x (was File in 1.20.1), so .toPath() must be REMOVED; Gson here serializes List<String> (not Components) so the 1.20.5+ Component-JSON-needs-RegistryAccess change does not apply.
    - verified → Replace `server.getServerDirectory().toPath().resolve("serverconfig")...` with `server.getServerDirectory().resolve("serverconfig")...` — i.e. DELETE the `.toPath()` call. getServerDirectory() returns java.nio.file.Path in 1.21.1; calling .toPath() on a Path does not compile. Gson claim is correct: LIST_TYPE = TypeToken<List<String>> (line 28) and bulletins is List<String> (line 29), so serialization is plain Strings — no RegistryAccess/RegistryOps needed.
    - evidence: NeoForge 1.21.x javadoc: https://nekoyue.github.io/ForgeJavaDocs-NG/javadoc/1.21.x-neoforge/net/minecraft/server/MinecraftServer.html — signature 'public Path getServerDirectory()' (java.nio.file.Path), confirming the 1.20.1 File -> 1.21.1 Path change. 1.20.5+ Component serialization requiring RegistryAccess (DynamicOps/RegistryOps) is documented in the migration primer https://docs.neoforged.net/primer/docs/1.21/ but applies only to Component/registry-backed JSON, not List<String> (verified OpBulletinHandler.java:28-29 serializes List<String>).


### Player & server-lifecycle events (join/leave/tab/tick) (`player-lifecycle-events`)


**Summary:** This dimension is overall MEDIUM risk and largely mechanical for NeoForge 1.21.1. The two HIGH-risk concerns (PlayerEvent.TabListNameFormat for tab-list rank display and PlayerEvent.NameFormat for nickname display) were verified to STILL EXIST in NeoForge 21.1.x under net.neoforged.neoforge.event.entity.player.PlayerEvent, with the EXACT same accessor casing the code already uses (TabListNameFormat.getDisplayName()/setDisplayName(Component); NameFormat.getDisplayname()/setDisplayname(Component)/getUsername()), so those handlers only need an import-package swap. SaveToFile/LoadFromFile (getPlayerDirectory()->File), PlayerLoggedInEvent/PlayerLoggedOutEvent, ServerStartedEvent/ServerStoppingEvent all exist with the same shape. The one genuine rewrite is the two TickEvent.ServerTickEvent handlers: TickEvent is REMOVED — the `if(phase==END)` guard + handler body must become a ServerTickEvent.Post handler (no phase field) under net.neoforged.neoforge.event.tick. Several cross-cutting blockers (loader.java's IExtensionPoint/NetworkConstants, FMLJavaModLoadingContext, the eventbus/fml import roots, permission/config registration) live in files owned by other dimensions but are surfaced here because the lifecycle classes import them.


**Findings (13):**


- **[MEDIUM] import net.minecraftforge.event.entity.player.PlayerEvent.TabListNameFormat + @SubscribeEvent onTabListNameFormatEvent(TabListNameFormat e); e.getEntity(), e.getEntity().getGameProfile(), e.setDisplayName(Component)**
  - _file:line:_ src/main/java/com/enviouse/sef/events/PlayerEventHandler.java:17, 42-53
  - _→ NeoForge 1.21.1:_ import net.neoforged.neoforge.event.entity.player.PlayerEvent.TabListNameFormat. VERIFIED in NeoForge 21.1.x: class exists; getDisplayName():@Nullable Component and setDisplayName(Component) exist with identical casing; PlayerEvent.getEntity() exists (returns Player). No logic change — only the import root net.minecraftforge.* -> net.neoforged.neoforge.* . getGameProfile() is vanilla Player (unchanged). NOTE: SEFUtilities.getFormattedPlayerName(...) return type must be a net.minecraft.network.chat.Component (vanilla, unchanged).
  - _notes:_ Flagged HIGH-risk by the brief but VERIFIED present via NeoForge 21.1.x javadoc — accessor names match the code exactly, so this is effectively a LOW/MEDIUM import swap, not a rewrite. Depends on TextFormatter/SEFUtilities Component handling being ported.


- **[MEDIUM] import net.minecraftforge.event.entity.player.PlayerEvent.NameFormat + @SubscribeEvent onNameFormatEvent(NameFormat e); e.getEntity(), e.setDisplayname(Component)**
  - _file:line:_ src/main/java/com/enviouse/sef/events/PlayerEventHandler.java:15, 55-59
  - _→ NeoForge 1.21.1:_ import net.neoforged.neoforge.event.entity.player.PlayerEvent.NameFormat. VERIFIED in NeoForge 21.1.x: setter is lowercase setDisplayname(Component) (matches code), plus getDisplayname()/getUsername() exist. Only the import root changes.
  - _notes:_ Flagged HIGH by brief; VERIFIED present with exact lowercase setDisplayname casing in NeoForge 21.1.x. This is the nickname-display surface — confirmed safe to port as a mechanical import swap.


- **[MEDIUM] import + @SubscribeEvent onServerTick(TickEvent.ServerTickEvent e) with `if(e.phase != TickEvent.Phase.END) return;` and e.getServer() (PlayerEventHandler), priority=EventPriority.LOWEST**
  - _file:line:_ src/main/java/com/enviouse/sef/events/PlayerEventHandler.java:22, 118-123
  - _→ NeoForge 1.21.1:_ TickEvent is REMOVED. Replace with net.neoforged.neoforge.event.tick.ServerTickEvent.Post; handler signature onServerTick(ServerTickEvent.Post e); DELETE the `if(phase != END) return;` guard entirely (Post already fires post-tick, no phase field). e.getServer() VERIFIED to exist on ServerTickEvent and return MinecraftServer. priority = net.neoforged.bus.api.EventPriority.LOWEST.
  - _notes:_ Genuine (small) rewrite, not just an import swap: the phase-guard must be removed and the type renamed. There is also a Pre variant — the old phase==END maps to .Post. Two such handlers exist (this one + ServerEssentialsForge.onServerTick).


- **[MEDIUM] import + @SubscribeEvent onServerTick(net.minecraftforge.event.TickEvent.ServerTickEvent ev) with `if(ev.phase == net.minecraftforge.event.TickEvent.Phase.END)` and ev.getServer(), ev.getServer().getTickCount()**
  - _file:line:_ src/main/java/com/enviouse/sef/ServerEssentialsForge.java:170-189
  - _→ NeoForge 1.21.1:_ Replace with net.neoforged.neoforge.event.tick.ServerTickEvent.Post. Convert the `if(phase==END){...}` block into the whole handler body (no phase check). ev.getServer() returns MinecraftServer (verified); getTickCount() is vanilla MinecraftServer (unchanged). Delegate calls (AnnouncementManager.tick, BannedItemsManager.tick, FreezeManager.tick, MuteManager.tick, CountdownManager.tick) are SEF-internal and unaffected by the event change.
  - _notes:_ Same TickEvent->ServerTickEvent.Post rewrite as the PlayerEventHandler one. This is the central per-tick dispatcher for SEF subsystems — keep .Post (post-tick) semantics to match old phase==END.


- **[LOW] import net.minecraftforge.event.server.ServerStartedEvent + @SubscribeEvent onServerStarted(ServerStartedEvent); e.getServer()**
  - _file:line:_ src/main/java/com/enviouse/sef/events/PlayerEventHandler.java:21, 101-116; src/main/java/com/enviouse/sef/events/ExternalModLoadingEvent.java:10, 17-21; src/main/java/com/enviouse/sef/ServerEssentialsForge.java:117-169
  - _→ NeoForge 1.21.1:_ import net.neoforged.neoforge.event.server.ServerStartedEvent. getServer():MinecraftServer unchanged. Pure import-root swap across all three handlers. getServer().getServerDirectory() (SEF.java:137) returns File (vanilla, unchanged); isDedicatedServer()/getPlayerCount()/getMaxPlayers()/getLocalIp() unchanged.
  - _notes:_ Three separate onServerStarted handlers across the dimension. All mechanical. Confirmed net.neoforged.neoforge.event.server.* package.


- **[LOW] import net.minecraftforge.event.server.ServerStoppingEvent + @SubscribeEvent onServerStopping(ServerStoppingEvent)**
  - _file:line:_ src/main/java/com/enviouse/sef/ServerEssentialsForge.java:190-205
  - _→ NeoForge 1.21.1:_ import net.neoforged.neoforge.event.server.ServerStoppingEvent. No accessor used; pure import-root swap. (ServerStartingEvent/ServerStoppedEvent also live in net.neoforged.neoforge.event.server.* if needed.)
  - _notes:_ Body only touches SEF-internal managers (FreezeManager, InvLockManager, DisableBuildingManager, MuteManager, BannedItemsManager, CountdownManager, VanishUtil) — unaffected by the event API change.


- **[LOW] PlayerEvent.PlayerLoggedOutEvent + PlayerLoggedInEvent (fully-qualified net.minecraftforge.event.entity.player.PlayerEvent.*); e.getEntity() instanceof ServerPlayer, sp.getUUID()**
  - _file:line:_ src/main/java/com/enviouse/sef/events/PlayerEventHandler.java:73-99
  - _→ NeoForge 1.21.1:_ Fully-qualify to net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent / PlayerLoggedInEvent (both VERIFIED present in 21.1.x). getEntity() returns Player; instanceof ServerPlayer pattern + getUUID() are vanilla, unchanged.
  - _notes:_ Mechanical package swap on the fully-qualified names. CommandRegistrationHandler.getAltTracker()/tracker.recordLogin(sp), AdminChatHandler/MsgCommands/ChatMessageManager/SDLinkHideTracker handleLogout are SEF-internal.


- **[LOW] PlayerEvent.SaveToFile + PlayerEvent.LoadFromFile (imports lines 14,16); e.getPlayerDirectory()**
  - _file:line:_ src/main/java/com/enviouse/sef/events/PlayerEventHandler.java:14, 16, 61-71
  - _→ NeoForge 1.21.1:_ import net.neoforged.neoforge.event.entity.player.PlayerEvent.SaveToFile / LoadFromFile. VERIFIED in 21.1.x: getPlayerDirectory() exists and returns java.io.File (also getPlayerUUID():String, getPlayerFile(String)). PlayerData.saveToDir(File)/loadFromDir(File) consume File — unchanged. Pure import-root swap.
  - _notes:_ Verified getPlayerDirectory() still returns File in NeoForge 21.1.x javadoc. No signature drift.


- **[LOW] import net.minecraftforge.eventbus.api.SubscribeEvent + EventPriority (@SubscribeEvent, EventPriority.LOWEST)**
  - _file:line:_ src/main/java/com/enviouse/sef/events/PlayerEventHandler.java:18-19, 42,55,61,67,73,88,101,118; src/main/java/com/enviouse/sef/events/ExternalModLoadingEvent.java:11, 17; src/main/java/com/enviouse/sef/ServerEssentialsForge.java:27, 116,170,190
  - _→ NeoForge 1.21.1:_ Swap import root net.minecraftforge.eventbus.api.* -> net.neoforged.bus.api.* for both SubscribeEvent and EventPriority. Annotation usage identical.
  - _notes:_ Mechanical, multi-site across the whole dimension. Do project-wide.


- **[MEDIUM] import net.minecraftforge.fml.common.Mod.EventBusSubscriber (@EventBusSubscriber on PlayerEventHandler / ExternalModLoadingEvent)**
  - _file:line:_ src/main/java/com/enviouse/sef/events/PlayerEventHandler.java:20, 30; src/main/java/com/enviouse/sef/events/ExternalModLoadingEvent.java:13, 15
  - _→ NeoForge 1.21.1:_ import net.neoforged.fml.common.EventBusSubscriber (now TOP-LEVEL, not nested under @Mod). Default bus is GAME (was FORGE). IMPORTANT NUANCE: these classes are ALSO registered manually as INSTANCES via loader.register()/MinecraftForge.EVENT_BUS.register() (ServerEssentialsForge.java:83-89, loader.java:15) — @EventBusSubscriber on a class only auto-registers STATIC @SubscribeEvent methods, but all handlers here are INSTANCE methods, so the annotation is currently a no-op and real wiring is the manual instance registration. Recommend either keep manual instance registration (swap MinecraftForge.EVENT_BUS->NeoForge.EVENT_BUS) and drop the misleading annotation, OR make methods static. Do not assume the annotation alone registers them.
  - _notes:_ Behavioral footgun: handlers fire today via the manual register(playerEventHandler) call, NOT the annotation. If the manual NeoForge.EVENT_BUS.register() is dropped during port, ALL these handlers silently stop firing (no tab format, no login/logout, no tick). This crosses into the mod-entrypoint/bus dimension (loader.register + ServerEssentialsForge ctor).


- **[LOW] net.minecraftforge.fml.ModList.get().isLoaded("luckperms"|"ftbessentials") ; net.luckperms.api.LuckPermsProvider.get() / LuckPerms**  _(uncertain — needs verification)_
  - _file:line:_ src/main/java/com/enviouse/sef/events/PlayerEventHandler.java:28, 107-114; src/main/java/com/enviouse/sef/events/ExternalModLoadingEvent.java:12, 33-70
  - _→ NeoForge 1.21.1:_ import net.neoforged.fml.ModList (same isLoaded API). LuckPerms API net.luckperms:api:5.4 is unchanged (net.luckperms.api.LuckPermsProvider.get()/LuckPerms stable for 1.21.1 runtime jar 5.4.139/140). FTB Essentials integration lives in FTBNicknameProvider (other file) — FTB version line is 2101.1.x; verify FTBEPlayerData accessors there (out of this file set).
  - _notes:_ ModList import-root swap only. LuckPerms isLoaded()-guard + try/catch around LuckPermsProvider.get() pattern is correct and unchanged. Actual FTB/Curios API verification belongs to the mod-deps dimension (FTBNicknameProvider/LuckPermsProvider classes not in this file set).


- **[LOW] TabPlaceholderRenderer: new ClientboundTabListPacket(headerComponent, footerComponent); player.connection.send(packet); Component.empty(); TextFormatter.stringToFormattedText(String)**
  - _file:line:_ src/main/java/com/enviouse/sef/tab/TabPlaceholderRenderer.java:7-9, 43-47
  - _→ NeoForge 1.21.1:_ All vanilla net.minecraft.* — ClientboundTabListPacket(Component,Component) ctor and ServerPlayer.connection.send(Packet) are unchanged 1.20.1->1.21.1. Component.empty() unchanged. No Forge surface here. The only watch-item is TextFormatter.stringToFormattedText returning a properly-built Component (Style/ClickEvent serialization concerns belong to the text/Component dimension, not this file).
  - _notes:_ TabAnimationManager.java is effectively a no-op (tick() empty, load() just logs) and uses only @Nullable LuckPerms reference + MinecraftServer — no Forge API at all; nothing to port there beyond it depending on LuckPerms being on classpath.


- **[CRITICAL] Cross-cutting blocker surfaced by lifecycle files: ServerEssentialsForge constructor uses FMLJavaModLoadingContext.get().getModEventBus(), ModLoadingContext.get().registerConfig(...), MinecraftForge.EVENT_BUS.addListener(...), FMLPaths.CONFIGDIR, FMLLoadCompleteEvent, RegisterCommandsEvent, PermissionGatherEvent.Nodes/PermissionNode/PermissionTypes**
  - _file:line:_ src/main/java/com/enviouse/sef/ServerEssentialsForge.java:26-33, 60-113, 141; src/main/java/com/enviouse/sef/utils/loader.java:3-29
  - _→ NeoForge 1.21.1:_ Mostly owned by the entrypoint/config/permissions dimensions but they gate this dimension: FMLJavaModLoadingContext is GONE (use the @Mod ctor-injected IEventBus modEventBus); ModLoadingContext.registerConfig -> modContainer.registerConfig (ctor-injected ModContainer); MinecraftForge.EVENT_BUS -> net.neoforged.neoforge.common.NeoForge.EVENT_BUS; FMLPaths -> net.neoforged.fml.loading.FMLPaths; FMLLoadCompleteEvent -> net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent (MOD bus). loader.java's IExtensionPoint.DisplayTest + NetworkConstants.IGNORESERVERONLY are GONE (NetworkConstants removed) — server-only marker becomes displayTest="IGNORE_ALL_VERSIONS" in neoforge.mods.toml; loader.MlContext() should be deleted. loader.MLConfig uses IConfigSpec whose signature CHANGED in NeoForge.
  - _notes:_ These do not break the player/tick events themselves but the mod will not LOAD until the entrypoint + loader.java are ported. Listed so the porting plan sequences them first. Detailed mapping belongs to entrypoint/config/permissions dimensions.


**Ordering notes:**

- BLOCKING PREREQ: entrypoint dimension must port ServerEssentialsForge ctor (IEventBus/ModContainer injection, NeoForge.EVENT_BUS) and loader.java (remove IExtensionPoint/NetworkConstants DisplayTest; move displayTest=IGNORE_ALL_VERSIONS to neoforge.mods.toml; IConfigSpec/registerConfig change) BEFORE these event handlers can be wired/tested — the mod will not load otherwise.
- Config dimension must port ConfigHandler.spec/ConfigHandler.config (ModConfigSpec) first: every handler body reads ConfigHandler.config.<flag>.get() (PlayerEventHandler:38-39,45,50-51; ServerEssentialsForge:118-160,173-186).
- Permissions dimension must port PermissionsHandler + PermissionNode/PermissionGatherEvent before onTabListNameFormatEvent's PermissionsHandler.playerHasPermission(...) calls (PlayerEventHandler:50-51) compile.
- Text/Component dimension (TextFormatter.stringToFormattedText, SEFUtilities.getFormattedPlayerName) should be ported before/with this dimension — the TabList/NameFormat setters and TabPlaceholderRenderer consume Components.
- Within THIS dimension the swaps are independent and can be done in one pass: (a) net.minecraftforge.eventbus.api->net.neoforged.bus.api, (b) net.minecraftforge.event.entity.player.PlayerEvent.*->net.neoforged.neoforge.event.entity.player.PlayerEvent.*, (c) net.minecraftforge.event.server.*->net.neoforged.neoforge.event.server.*, (d) net.minecraftforge.fml.common.Mod.EventBusSubscriber->net.neoforged.fml.common.EventBusSubscriber, (e) net.minecraftforge.fml.ModList->net.neoforged.fml.ModList. The ONLY non-mechanical edit is the two TickEvent.ServerTickEvent handlers -> ServerTickEvent.Post (drop the phase==END guard).


**Open questions (this dimension):**

- FTB Essentials nickname accessors (FTBEPlayerData) and Curios slot APIs are referenced indirectly via providers (FTBNicknameProvider/LuckPermsProvider) NOT in this file set — confirm in the mod-deps dimension that FTB 2101.1.x still exposes the same nickname accessors used by ExternalModLoadingEvent.
- Confirm SEFUtilities.getFormattedPlayerName(...) returns net.minecraft.network.chat.Component (the TabListNameFormat/NameFormat setters require a Component) and that TextFormatter.stringToFormattedText builds Components in a way compatible with 1.20.5+ component serialization changes — owned by the text/Component dimension.
- Decide registration strategy for the @EventBusSubscriber classes: keep manual instance registration on NeoForge.EVENT_BUS (current behavior) vs. converting to static handlers so the top-level @EventBusSubscriber actually wires them. Current code's annotation is a no-op; mis-porting could silently drop all handlers.
- Verify the deferred manual registration in loadComplete(FMLLoadCompleteEvent) still works under NeoForge timing (registering GAME-bus listeners from a MOD-bus FMLLoadCompleteEvent handler) — order-sensitive but not an API break.


**Verification verdicts:**

- **CONFIRMED** — net.minecraftforge.fml.ModList.get().isLoaded("luckperms"|"ftbessentials") ; net.luckperms.api.LuckPermsProvider.get() / LuckPerms
    - claim: import net.neoforged.fml.ModList (same isLoaded API); LuckPerms net.luckperms:api 5.4 unchanged (net.luckperms.api.LuckPermsProvider.get()/LuckPerms stable for 1.21.1, NeoForge runtime jar 5.4.139/140); FTB Essentials FTBEPlayerData accessors out of this file set (FTBNicknameProvider).
    - verified → same — confirmed correct. ModList: net.neoforged.fml.ModList.get().isLoaded(String). LuckPerms: net.luckperms.api.LuckPermsProvider.get() returns net.luckperms.api.LuckPerms; API artifact coordinate is net.luckperms:api (group net.luckperms, module 'api'). NeoForge 1.21.1 LuckPerms builds v5.4.139 / v5.4.140 exist. FTBEPlayerData accessor verification correctly scoped to FTBNicknameProvider (separate dimension).
    - evidence: FancyModLoader ModList: repos/neoforged/FancyModLoader .../net/neoforged/fml/ModList.java (line 74 `public static ModList get()`, line 133 `public boolean isLoaded(String modTarget)`). LuckPerms: repos/LuckPerms/LuckPerms api/src/main/java/net/luckperms/api/LuckPermsProvider.java (line 26 `package net.luckperms.api;`, line 38 `public final class LuckPermsProvider`, line 50 `public static @NonNull LuckPerms get()`); api/build.gradle `group = 'net.luckperms'` + settings.gradle module 'api'. NeoForge 1.21.1 runtime jars: https://modrinth.com/plugin/luckperms/version/v5.4.139-neoforge and https://modrinth.com/plugin/luckperms/version/v5.4.140-neoforge
- **CORRECTED** — FMLJavaModLoadingContext.get().getModEventBus() ; ModLoadingContext.get().registerConfig(...) ; MinecraftForge.EVENT_BUS.addListener(...) ; FMLPaths.CONFIGDIR ; FMLLoadCompleteEvent ; IExtensionPoint.DisplayTest + NetworkConstants.IGNORESERVERONLY (server-only marker)
    - claim: FMLJavaModLoadingContext GONE -> use @Mod ctor-injected IEventBus modEventBus; ModLoadingContext.registerConfig -> modContainer.registerConfig (ctor-injected ModContainer); MinecraftForge.EVENT_BUS -> net.neoforged.neoforge.common.NeoForge.EVENT_BUS; FMLPaths -> net.neoforged.fml.loading.FMLPaths; FMLLoadCompleteEvent -> net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent (MOD bus); IExtensionPoint.DisplayTest + NetworkConstants.IGNORESERVERONLY GONE (NetworkConstants removed) — server-only marker becomes displayTest="IGNORE_ALL_VERSIONS" in neoforge.mods.toml; loader.MlContext() deleted; loader.MLConfig IConfigSpec signature CHANGED.
    - verified → Mostly correct, with ONE wrong value to fix: the server-only displayTest value. For a SERVER-ONLY mod (SEF, original Forge code used NetworkConstants.IGNORESERVERONLY) the correct neoforge.mods.toml value is displayTest="IGNORE_SERVER_VERSION", NOT "IGNORE_ALL_VERSIONS". Per official NeoForge mods.toml docs: IGNORE_SERVER_VERSION = 'will not cause a red X if it's present on the server but not on the client. This is what you should use if you're a server only mod.'; IGNORE_ALL_VERSIONS = 'should only be used if your mod has no server component' (i.e. effectively client-only). All other replacements CONFIRMED: FMLJavaModLoadingContext.get() removed -> ctor-injected IEventBus (and ModContainer for config); ModContainer.registerConfig(type, spec[, filename]); net.neoforged.neoforge.common.NeoForge.EVENT_BUS (type IEventBus); net.neoforged.fml.loading.FMLPaths.CONFIGDIR; net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent (extends ParallelDispatchEvent => MOD bus); NetworkConstants removed; IExtensionPoint.DisplayTest removed (IExtensionPoint is now an empty marker interface). loader.MlContext() should be deleted (replaced by mods.toml displayTest). IConfigSpec base type unchanged name but NeoForge impl is ModConfigSpec; registerConfig now lives on ModContainer.
    - evidence: displayTest values (authoritative NeoForge-maintained template): repos/neoforged/ModDevGradle testproject/src/main/resources/META-INF/neoforge.mods.toml — 'IGNORE_SERVER_VERSION means ... This is what you should use if you're a server only mod.' / 'IGNORE_ALL_VERSION ... only be used if your mod has no server component'; also docs https://docs.neoforged.net/docs/1.21.1/concepts/sides (displayTest referenced via 'sides'). FMLJavaModLoadingContext.get() removed, use ctor IEventBus: https://neoforged.net/news/21.0release/ and https://docs.neoforged.net/docs/1.21.1/gettingstarted/modfiles (@Mod ctor injects IEventBus, ModContainer, FMLModContainer, Dist). registerConfig on ModContainer: https://docs.neoforged.net/docs/1.21.1/misc/config (`container.registerConfig(ModConfig.Type.COMMON, spec)`; 'NeoForge implements IConfigSpec via ModConfigSpec'). NeoForge.EVENT_BUS: repos/neoforged/NeoForge src/main/java/net/neoforged/neoforge/common/NeoForge.java (line 6 package net.neoforged.neoforge.common; line 17 `public static final IEventBus EVENT_BUS`). FMLPaths: used in repos/neoforged/NeoForge .../internal/CommonModLoader.java `import net.neoforged.fml.loading.FMLPaths`. FMLLoadCompleteEvent: repos/neoforged/FancyModLoader .../net/neoforged/fml/event/lifecycle/FMLLoadCompleteEvent.java (package net.neoforged.fml.event.lifecycle; `extends ParallelDispatchEvent`). NetworkConstants absent from repos/neoforged/NeoForge src/main/java/net/neoforged/neoforge/network (dir listing has no NetworkConstants). IExtensionPoint now empty: repos/neoforged/FancyModLoader .../net/neoforged/fml/IExtensionPoint.java ('public interface IExtensionPoint {}', DisplayTest removed).


### Gameplay event handlers on the game bus (banned/freeze/invlock/disablebuilding) (`gameplay-event-handlers`)


**Summary:** All four handler classes are static @Mod.EventBusSubscriber listeners on the Forge (game) bus that cancel gameplay actions for affected players. The breakage is overwhelmingly mechanical import/annotation swaps: net.minecraftforge.* event classes -> net.neoforged.neoforge.event.* (same leaf names for BlockEvent, PlayerInteractEvent, AttackEntityEvent, PlayerContainerEvent, LivingEntityUseItemEvent, CommandEvent), eventbus.api.* -> net.neoforged.bus.api.*, and @Mod.EventBusSubscriber -> top-level @EventBusSubscriber (default Bus.GAME). The two genuine API-shape risks are (1) EntityItemPickupEvent, which is removed/renamed in NeoForge 1.21.1 to ItemEntityPickupEvent.Pre with different accessors and a boolean-cancel pattern (used in banned + invlock), and (2) BlockEvent.BreakEvent.getPlayer()/EntityPlaceEvent accessor signatures that must be re-verified against Mojang-named NeoForge. Overall risk MEDIUM, driven almost entirely by the pickup-event rename.


**Findings (12):**


- **[LOW] @Mod.EventBusSubscriber (net.minecraftforge.fml.common.Mod.EventBusSubscriber, nested)**
  - _file:line:_ src/main/java/com/enviouse/sef/banned/BannedItemsEventHandler.java:21,31; src/main/java/com/enviouse/sef/disablebuilding/DisableBuildingEventHandler.java:10,18; src/main/java/com/enviouse/sef/freeze/FreezeEventHandler.java:12,24; src/main/java/com/enviouse/sef/invlock/InvLockEventHandler.java:11,20
  - _→ NeoForge 1.21.1:_ Import net.neoforged.fml.common.EventBusSubscriber (now TOP-LEVEL) and annotate with @EventBusSubscriber. All four use the bare annotation with no args, so default Bus.GAME is correct and no bus arg is needed. Add modid to the annotation if desired (e.g. @EventBusSubscriber(modid=Sefported.MODID)) for clean scoping; not strictly required.
  - _notes:_ Pure annotation/import swap, repeated in all 4 files. Default bus changed from implicit FORGE to GAME but semantics are identical for these game-bus listeners.


- **[LOW] @SubscribeEvent + EventPriority (net.minecraftforge.eventbus.api.SubscribeEvent / .EventPriority)**
  - _file:line:_ src/main/java/com/enviouse/sef/banned/BannedItemsEventHandler.java:19-20,47,91,106,117,128,147; src/main/java/com/enviouse/sef/disablebuilding/DisableBuildingEventHandler.java:8-9,22,35,48; src/main/java/com/enviouse/sef/freeze/FreezeEventHandler.java:10-11,32,69,85,101,117,128,139,150; src/main/java/com/enviouse/sef/invlock/InvLockEventHandler.java:9-10,24,38,49,60
  - _→ NeoForge 1.21.1:_ Swap imports to net.neoforged.bus.api.SubscribeEvent and net.neoforged.bus.api.EventPriority. Usage (priority = EventPriority.HIGHEST) and the bare @SubscribeEvent are unchanged.
  - _notes:_ Mechanical import swap only; annotation attribute name and enum constants (HIGHEST) are identical in NeoForge bus API.


- **[LOW] BlockEvent.EntityPlaceEvent (net.minecraftforge.event.level.BlockEvent.EntityPlaceEvent) — getEntity(), getPlacedBlock(), getLevel(), getPos(), setCanceled()**
  - _file:line:_ src/main/java/com/enviouse/sef/banned/BannedItemsEventHandler.java:18,48,52-53,63-64,74,86; src/main/java/com/enviouse/sef/disablebuilding/DisableBuildingEventHandler.java:7,36,38,40; src/main/java/com/enviouse/sef/freeze/FreezeEventHandler.java:9,86,89,91
  - _→ NeoForge 1.21.1:_ Import net.neoforged.neoforge.event.level.BlockEvent.EntityPlaceEvent. Accessors getEntity(), getPlacedBlock(), getLevel() (returns LevelAccessor), getPos(), and setCanceled(boolean) carry over. Event remains @Cancelable.
  - _notes:_ Same leaf name and package suffix; NeoForge keeps EntityPlaceEvent. getLevel() returns LevelAccessor as before so the instanceof ServerLevel check (banned L63) still works. Confirm setCanceled is still available (vs ICancellableEvent.setCanceled) — NeoForge moved cancellation to ICancellableEvent interface but the setCanceled(boolean) method name/signature is preserved on cancelable events.


- **[LOW] BlockEvent.BreakEvent (net.minecraftforge.event.level.BlockEvent.BreakEvent) — getPlayer(), getLevel(), getPos(), setCanceled()**
  - _file:line:_ src/main/java/com/enviouse/sef/banned/BannedItemsEventHandler.java:148,152-153; src/main/java/com/enviouse/sef/disablebuilding/DisableBuildingEventHandler.java:23,25,27; src/main/java/com/enviouse/sef/freeze/FreezeEventHandler.java:70,73,75
  - _→ NeoForge 1.21.1:_ Import net.neoforged.neoforge.event.level.BlockEvent.BreakEvent. getPlayer() returns net.minecraft.world.entity.player.Player (the instanceof ServerPlayer cast at db L25, freeze L73 still valid), getLevel(), getPos(), setCanceled(boolean) all preserved.
  - _notes:_ BreakEvent exists in NeoForge 1.21.1 with the same shape. Verify getPlayer() return type is still Player (not ServerPlayer) so the instanceof pattern matches — historically Player; cast usage is safe either way.


- **[LOW] PlayerInteractEvent.RightClickBlock / .RightClickItem / .LeftClickBlock / .EntityInteract (net.minecraftforge.event.entity.player.PlayerInteractEvent) — getEntity(), getItemStack(), setCanceled()**
  - _file:line:_ src/main/java/com/enviouse/sef/banned/BannedItemsEventHandler.java:17,92,96,98,107,111,113; src/main/java/com/enviouse/sef/disablebuilding/DisableBuildingEventHandler.java:6,49,51,53; src/main/java/com/enviouse/sef/freeze/FreezeEventHandler.java:8,118,121,123,129,132,135,140,143,145,151,154,156; src/main/java/com/enviouse/sef/invlock/InvLockEventHandler.java:8,50,52,54,61,63,65
  - _→ NeoForge 1.21.1:_ Import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent. All four subclasses (RightClickBlock, RightClickItem, LeftClickBlock, EntityInteract) exist with same names. getEntity() returns Player, getItemStack() returns ItemStack, setCanceled(boolean) preserved.
  - _notes:_ Most-used surface across all four files. NeoForge retains PlayerInteractEvent and all inner classes with identical accessors. LeftClickBlock cancellation still blocks block-breaking start. No signature change expected.


- **[LOW] LivingEntityUseItemEvent.Start (net.minecraftforge.event.entity.living.LivingEntityUseItemEvent.Start) — getEntity(), getItem(), setCanceled()**
  - _file:line:_ src/main/java/com/enviouse/sef/banned/BannedItemsEventHandler.java:15,118,122,124
  - _→ NeoForge 1.21.1:_ Import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent.Start. getEntity() returns LivingEntity (instanceof ServerPlayer cast at L122 valid), getItem() returns ItemStack, setCanceled(boolean) preserved.
  - _notes:_ Class retained in NeoForge with the .Start subclass and same accessors. Mechanical import swap.


- **[LOW] AttackEntityEvent (net.minecraftforge.event.entity.player.AttackEntityEvent) — getEntity(), setCanceled()**
  - _file:line:_ src/main/java/com/enviouse/sef/freeze/FreezeEventHandler.java:7,102,105,107
  - _→ NeoForge 1.21.1:_ Import net.neoforged.neoforge.event.entity.player.AttackEntityEvent. getEntity() returns Player, setCanceled(boolean) preserved.
  - _notes:_ Retained in NeoForge with same accessors; mechanical swap.


- **[LOW] PlayerContainerEvent.Open (net.minecraftforge.event.entity.player.PlayerContainerEvent.Open) — getEntity()**
  - _file:line:_ src/main/java/com/enviouse/sef/invlock/InvLockEventHandler.java:7,25,27
  - _→ NeoForge 1.21.1:_ Import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent.Open. getEntity() returns Player. Note this handler does NOT cancel the event; it calls player.closeContainer() (vanilla, unchanged) so no setCanceled concern here.
  - _notes:_ PlayerContainerEvent.Open retained in NeoForge. Open is not cancelable in Forge either; the code relies on closeContainer() not cancellation, so behavior is preserved. Verify player.closeContainer() vanilla method name (unchanged 1.20.1->1.21.1).


- **[LOW] CommandEvent (net.minecraftforge.event.CommandEvent) — getParseResults(), setCanceled()**
  - _file:line:_ src/main/java/com/enviouse/sef/freeze/FreezeEventHandler.java:6,33,37,40,57
  - _→ NeoForge 1.21.1:_ Import net.neoforged.neoforge.event.CommandEvent. getParseResults() returns com.mojang.brigadier.ParseResults<CommandSourceStack> (vanilla brigadier, unchanged); .getContext().getSource().getPlayerOrException() and .getReader().getString() chains are all brigadier/vanilla and unchanged. setCanceled(boolean) preserved.
  - _notes:_ CommandEvent confirmed present in NeoForge per target facts. The deep accessor chain is pure Brigadier + CommandSourceStack (vanilla), not Forge, so it survives untouched. Event remains cancelable.


- **[HIGH] EntityItemPickupEvent (net.minecraftforge.event.entity.player.EntityItemPickupEvent) — getEntity(), getItem() (returns ItemEntity), setCanceled(), ev.getItem().getItem()/discard()**  _(uncertain — needs verification)_
  - _file:line:_ src/main/java/com/enviouse/sef/banned/BannedItemsEventHandler.java:16,129,133,135,139; src/main/java/com/enviouse/sef/invlock/InvLockEventHandler.java:6,39,41,43
  - _→ NeoForge 1.21.1:_ EntityItemPickupEvent is GONE in NeoForge. Replace with net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent.Pre. Accessor mapping: getEntity() -> getPlayer() (returns Player); getItem() (the ItemEntity) -> getItemEntity(); the ItemStack (banned L135 ev.getItem().getItem()) -> getItemEntity().getItem(); discard (banned L139) -> getItemEntity().discard(). Cancellation is NOT setCanceled(true) on Pre — it is not a vanilla-style cancelable; you set the result to deny pickup via event.setCanReceiveStack(false) (or the Pre/Post canPickup mechanism). Must rewrite both handlers, not just swap imports.
  - _notes:_ This is the one genuinely non-mechanical rewrite in this dimension. The Forge EntityItemPickupEvent (single cancelable event with getEntity()/getItem()/setCanceled) was split into ItemEntityPickupEvent.Pre/.Post in NeoForge with renamed accessors and a non-Cancelable deny mechanism. Used in TWO files (banned confiscation + invlock). The banned handler also calls ev.getItem().discard() which must move to getItemEntity().discard(). Exact method names (getItemEntity vs getItemEntity(), setCanReceiveStack vs canPickup) need verification against the 21.1.x source.


- **[LOW] ForgeRegistries.BLOCKS.getKey(Block) (net.minecraftforge.registries.ForgeRegistries)**
  - _file:line:_ src/main/java/com/enviouse/sef/banned/BannedItemsEventHandler.java:22,57
  - _→ NeoForge 1.21.1:_ Replace with net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(placed.getBlock()), which returns a ResourceLocation. Drop the net.minecraftforge.registries.ForgeRegistries import.
  - _notes:_ Single call site in this dimension. BuiltInRegistries.BLOCK.getKey returns ResourceLocation (never null for registered blocks, but keep null-guard at L58). Cross-cutting with the registry dimension of the audit.


- **[LOW] setCanceled(boolean) cancellation API across all cancelable events**
  - _file:line:_ src/main/java/com/enviouse/sef/banned/BannedItemsEventHandler.java:74,86,100,113,124,137; src/main/java/com/enviouse/sef/disablebuilding/DisableBuildingEventHandler.java:27,40,53; src/main/java/com/enviouse/sef/freeze/FreezeEventHandler.java:57,75,91,107,124,135,145,156; src/main/java/com/enviouse/sef/invlock/InvLockEventHandler.java:43,54,65
  - _→ NeoForge 1.21.1:_ No source change for events that remain cancelable — NeoForge cancelable events implement net.neoforged.bus.api.ICancellableEvent which still exposes setCanceled(boolean). The ONLY exception is the pickup event (see EntityItemPickupEvent finding) where setCanceled is replaced by the Pre/setCanReceiveStack pattern.
  - _notes:_ Cancellation contract preserved for BlockEvent/PlayerInteractEvent/AttackEntityEvent/LivingEntityUseItemEvent/CommandEvent via ICancellableEvent.setCanceled. Only the two EntityItemPickupEvent call sites (banned L137, invlock L43) need a different cancel mechanism.


**Ordering notes:**

- Depends on the config dimension being ported first: every handler reads ConfigHandler.config.* (ModConfigSpec ConfigValues) at the top of each method — the config class must compile (ModConfigSpec migration) before these handlers compile.
- Depends on the @Mod entrypoint / @EventBusSubscriber registration dimension: if @EventBusSubscriber is given an explicit modid, that MODID constant must exist.
- Cross-cuts the registry dimension: BannedItemsEventHandler L57 uses ForgeRegistries.BLOCKS -> BuiltInRegistries.BLOCK; coordinate so the same BuiltInRegistries swap is applied consistently mod-wide.
- Depends on TextFormatter (TextFormatter.stringToFormattedText) and the manager classes (BannedItemsManager/DisableBuildingManager/FreezeManager/InvLockManager, CommandRegistrationHandler) compiling — these are referenced but out of this dimension's scope.
- The EntityItemPickupEvent rewrite (banned + invlock) is the only item here needing a behavioral rewrite; sequence it after the simple import swaps and verify in-game pickup denial actually fires.


**Open questions (this dimension):**

- Exact NeoForge 21.1.x accessor names on ItemEntityPickupEvent.Pre: is it getPlayer()/getItemEntity() and is denial done via setCanReceiveStack(false) or a canPickup TriState? Need to confirm against the 21.1.233 source before rewriting banned L129-141 and invlock L39-46.
- Does ItemEntityPickupEvent split into Pre/Post mean the .discard() in BannedItemsEventHandler L139 should happen in Pre (after denying) or is there a guaranteed-fire point? Confirm discard() on the ItemEntity is still safe from Pre.
- BlockEvent.BreakEvent.getPlayer() return type in NeoForge 21.1.1 (Player vs ServerPlayer) — affects the instanceof pattern at db L25 / freeze L73 (works either way, but confirm).
- Confirm setCanceled(boolean) is exposed directly (not requiring a cast to ICancellableEvent) on EntityPlaceEvent/BreakEvent/PlayerInteractEvent/AttackEntityEvent/LivingEntityUseItemEvent/CommandEvent in 21.1.x.
- ProjectileImpactEvent, LivingChangeTargetEvent, and VanillaGameEvent were listed in the focus brief but DO NOT appear in any of these four files (grep confirmed zero hits) — confirm they belong to a different dimension/file set.


**Verification verdicts:**

- **CORRECTED** — EntityItemPickupEvent (net.minecraftforge.event.entity.player.EntityItemPickupEvent) — getEntity(), getItem() (returns ItemEntity), setCanceled(), ev.getItem().getItem()/discard()
    - claim: EntityItemPickupEvent is GONE in NeoForge; replace with net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent.Pre. getEntity()->getPlayer() (Player); getItem()->getItemEntity(); ev.getItem().getItem()->getItemEntity().getItem(); ev.getItem().discard()->getItemEntity().discard(); cancellation is NOT setCanceled(true) but event.setCanReceiveStack(false) (or the Pre/Post canPickup mechanism); rewrite both handlers.
    - verified → Replace with net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent.Pre (game bus, server-side only). CONFIRMED accessor mappings: getEntity() -> getPlayer() returns net.minecraft.world.entity.player.Player (cast to ServerPlayer as code does); getItem() (the ItemEntity) -> getItemEntity() returns net.minecraft.world.entity.item.ItemEntity; banned L135 ev.getItem().getItem() -> getItemEntity().getItem() (ItemStack); banned L139 ev.getItem().discard() -> getItemEntity().discard(). CORRECTION: the proposal's named cancel method 'setCanReceiveStack(false)' does NOT exist on any NeoForge class. The correct deny mechanism is event.setCanPickup(net.neoforged.neoforge.common.util.TriState.FALSE). Pre is NOT cancelable (no ICancellableEvent / setCanceled). So L137 ev.setCanceled(true) -> ev.setCanPickup(TriState.FALSE) with import net.neoforged.neoforge.common.util.TriState. The proposal's parenthetical fallback ('Pre/Post canPickup mechanism') is the correct path; the named 'setCanReceiveStack' is wrong. Both onPickup-style handlers must indeed be rewritten (import + signature + accessors + deny call), not just import-swapped. Note actual file is /mnt/hermes/projects/SEFPORTED/SourceCodeOld/Server-Essentials-Forge/src/main/java/com/enviouse/sef/banned/BannedItemsEventHandler.java (package com.enviouse.sef, single onPickup handler at L128-141), not under com.enviouse.sefported.
    - evidence: NeoForge source net/neoforged/neoforge/event/entity/player/ItemEntityPickupEvent.java (1.21.x branch): Pre exposes setCanPickup(TriState)/canPickup(); base exposes getPlayer():Player and getItemEntity():ItemEntity; Post has getOriginalStack()/getCurrentStack(); no setCanReceiveStack and no setCanceled anywhere. Javadoc https://nekoyue.github.io/ForgeJavaDocs-NG/javadoc/1.21.x-neoforge/net/neoforged/neoforge/event/entity/player/ItemEntityPickupEvent.Pre.html confirms setCanPickup(net.neoforged.neoforge.common.util.TriState) and no ICancellableEvent/setCanceled. NeoForged docs https://docs.neoforged.net/docs/1.21.1/concepts/events/ confirms TriState.FALSE cancels the action. Old Forge code verified at BannedItemsEventHandler.java L128-141.


### Brigadier command trees & registration (`commands`)


**Summary:** The command layer is large (24+ command classes registered through RegisterCommandsEvent) but mechanically clean: every Brigadier argument is vanilla and context-free (StringArgumentType, IntegerArgumentType, BoolArgumentType, EntityArgument.player()/players()) — there is NO ItemArgument/ResourceArgument/CommandBuildContext usage, so event.getBuildContext() is never needed and no argument factory signatures changed. The real porting work is import/event-bus swaps: RegisterCommandsEvent + CommandEvent + EventPriority + the @EventBusSubscriber annotation move from net.minecraftforge.* to net.neoforged.*, the vanish listeners are wired onto MinecraftForge.EVENT_BUS (-> NeoForge.EVENT_BUS), and permission checks rely on net.minecraftforge.server.permission.* (PermissionNode ctor / PermissionAPI.getOfflinePermission) which needs verification. Two non-command-but-adjacent breakers ride along: a ForgeRegistries.ITEMS.getKey call in /banned addhand and the two EntityArgument/EntitySelector mixins whose @At targets must move to Mojang names. Overall risk MEDIUM, gated on the permission API and mixin-target dimensions.


**Findings (11):**


- **[MEDIUM] net.minecraftforge.event.RegisterCommandsEvent + e.getDispatcher() + @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.LOW)**
  - _file:line:_ src/main/java/com/enviouse/sef/events/CommandRegistrationHandler.java:33,60,61,63,152,153
  - _→ NeoForge 1.21.1:_ net.neoforged.neoforge.event.RegisterCommandsEvent (getDispatcher() retained, returns CommandDispatcher<CommandSourceStack>). @SubscribeEvent -> net.neoforged.bus.api.SubscribeEvent; EventPriority -> net.neoforged.bus.api.EventPriority.LOW. getBuildContext()/getCommandSelection() exist but are NOT used here so no work needed.
  - _notes:_ Central registration hub: a single @EventBusSubscriber class with two RegisterCommandsEvent handlers (default priority + LOW priority for /invsee and /msg override). Mechanical import swaps; getDispatcher() signature is unchanged. The two-priority pattern (LOW to register after vanilla/FTB) still works on NeoForge's bus.


- **[MEDIUM] @EventBusSubscriber (nested net.minecraftforge.fml.common.Mod.EventBusSubscriber)**
  - _file:line:_ src/main/java/com/enviouse/sef/events/CommandRegistrationHandler.java:35,37; src/main/java/com/enviouse/sef/freeze/FreezeEventHandler.java:12,24; src/main/java/com/enviouse/sef/config/PermissionsHandler.java:13,20
  - _→ NeoForge 1.21.1:_ net.neoforged.fml.common.EventBusSubscriber (now top-level, not nested in @Mod). RegisterCommandsEvent/CommandEvent/PermissionGatherEvent fire on the GAME bus, which is the default, so bare @EventBusSubscriber is correct (Bus.FORGE -> Bus.GAME only matters if explicitly named).
  - _notes:_ FreezeEventHandler uses @Mod.EventBusSubscriber on a class whose handlers are all static (CommandEvent, BlockEvent, etc.) — good fit for annotation-driven GAME-bus subscription. CommandRegistrationHandler's handlers are instance methods but the class is also manually registered via loader.register(commandRegistrator) in ServerEssentialsForge — verify no double-registration once annotation is ported.


- **[MEDIUM] net.minecraftforge.event.CommandEvent (frozen-player command blocker): event.getParseResults().getContext()/getReader(); event.setCanceled(true)**
  - _file:line:_ src/main/java/com/enviouse/sef/freeze/FreezeEventHandler.java:6,33,37,40,57
  - _→ NeoForge 1.21.1:_ net.neoforged.neoforge.event.CommandEvent. getParseResults() retained (ParseResults<CommandSourceStack>, vanilla type). Cancellation: NeoForge CommandEvent implements net.neoforged.bus.api.ICancellableEvent; call setCanceled(true) (single-l spelling matches NeoForge).
  - _notes:_ Only CommandEvent subscriber in the codebase. Logic reads the raw command string from getParseResults().getReader().getString() and cancels for frozen players — all vanilla Brigadier accessors, no signature change. Just the package + (already-correct) setCanceled spelling.


- **[MEDIUM] Vanish listeners registered imperatively: MinecraftForge.EVENT_BUS.addListener(this::registerVanishCommands) handling net.minecraftforge.event.RegisterCommandsEvent**
  - _file:line:_ src/main/java/com/enviouse/sef/ServerEssentialsForge.java:79,93,94
  - _→ NeoForge 1.21.1:_ net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(...); event type -> net.neoforged.neoforge.event.RegisterCommandsEvent. VanishCommand.register(event.getDispatcher()) body unchanged.
  - _notes:_ VanishCommand is the ONLY command not registered through CommandRegistrationHandler — it is wired via an explicit addListener on the game event bus from the mod constructor. Depends on the entrypoint dimension (constructor now receives IEventBus/ModContainer; this addListener must move onto NeoForge.EVENT_BUS).


- **[LOW] Brigadier argument types: StringArgumentType (word/string/greedyString), IntegerArgumentType, BoolArgumentType, EntityArgument.player()/players()/getPlayer()/getPlayers()**
  - _file:line:_ src/main/java/com/enviouse/sef/banned/BannedItemsCommands.java:80-252; src/main/java/com/enviouse/sef/announcements/TitleAnnouncementCommand.java:28,47; src/main/java/com/enviouse/sef/commands/MsgCommands.java:60-64; src/main/java/com/enviouse/sef/warn/WarnCommand.java:51-80; src/main/java/com/enviouse/sef/vanish/VanishCommand.java:48-65
  - _→ NeoForge 1.21.1:_ NO CHANGE. com.mojang.brigadier.arguments.* and net.minecraft.commands.arguments.EntityArgument are vanilla and identical 1.20.1->1.21.1. None require CommandBuildContext (no ItemArgument/ResourceArgument/BlockStateArgument anywhere). Commands.literal/argument and CommandSourceStack unchanged.
  - _notes:_ Surveyed all 24 register() methods. Every argument is context-free. This is the bulk of the command code and it ports with zero changes. EntityArgument.getPlayer/getPlayers still throw CommandSyntaxException with identical signatures.


- **[LOW] SuggestionProvider<CommandSourceStack> + SharedSuggestionProvider.suggest(...) + SuggestionsBuilder.suggest**
  - _file:line:_ src/main/java/com/enviouse/sef/commands/BfcCommands.java:19,49,71-82; src/main/java/com/enviouse/sef/banned/BannedItemsCommands.java:56-78; src/main/java/com/enviouse/sef/announcements/TextAnnouncementCommand.java:30-56; src/main/java/com/enviouse/sef/countdown/CountdownCommand.java:40-45
  - _→ NeoForge 1.21.1:_ NO CHANGE. com.mojang.brigadier.suggestion.SuggestionProvider and net.minecraft.commands.SharedSuggestionProvider.suggest(Iterable<String>, SuggestionsBuilder) are vanilla and stable 1.20.1->1.21.1.
  - _notes:_ All suggestion providers are hand-rolled string suggesters (suggest literals, suggest filter ids, suggest item ids) using only builder.suggest()/buildFuture() — no registry-backed suggestion helpers that changed. Import-clean.


- **[HIGH] Permission gating inside .requires()/executes(): PermissionsHandler.playerHasPermission -> PermissionAPI.getOfflinePermission(uuid, node); PermissionNode<Boolean> type & new PermissionNode<>(MODID, id, PermissionTypes.BOOLEAN, (player,uuid,context)->...); node.setInformation(...); PermissionGatherEvent.Nodes.addNodes()**  _(uncertain — needs verification)_
  - _file:line:_ src/main/java/com/enviouse/sef/config/PermissionsHandler.java:14-17,181-212; src/main/java/com/enviouse/sef/commands/BfcCommands.java:20,29-39; src/main/java/com/enviouse/sef/ServerEssentialsForge.java:98-104
  - _→ NeoForge 1.21.1:_ net.minecraftforge.server.permission.* -> net.neoforged.neoforge.server.permission.*: PermissionAPI (getOfflinePermission retained), nodes.PermissionNode, nodes.PermissionTypes.BOOLEAN, events.PermissionGatherEvent.Nodes. Verify PermissionNode 4-arg ctor (modid, nodeName, PermissionType, PermissionResolver) and that PermissionGatherEvent fires on GAME bus in 21.1.x.
  - _notes:_ EVERY command's .requires()/permission check funnels through BfcCommands.checkPermission + PermissionsHandler.playerHasPermission. If the NeoForge PermissionNode ctor or PermissionAPI.getOfflinePermission signature differs, all command access control breaks. Also note PermissionNode<Boolean> is reflected over in registerPermissionNodes (line 182-185) so the field type/class identity matters. Depends on the dedicated permissions dimension; cross-cutting across the whole command tree.


- **[LOW] ForgeRegistries.ITEMS.getKey(stack.getItem()) inside /banned addhand executor**
  - _file:line:_ src/main/java/com/enviouse/sef/banned/BannedItemsCommands.java:302,303
  - _→ NeoForge 1.21.1:_ net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem()) (returns ResourceLocation). Drop the net.minecraftforge.registries.ForgeRegistries import.
  - _notes:_ Single registry lookup living inside a command executor. Trivial swap but will fail to compile if missed. The other BuiltInRegistries usage (BannedEntry TagKey.create) is already vanilla.


- **[HIGH] Mixins on EntityArgument / EntitySelector with @At INVOKE targets by method name (getEntities, getPlayers, findSingleEntity, findSinglePlayer) and @ModifyVariable**  _(uncertain — needs verification)_
  - _file:line:_ src/main/java/com/enviouse/sef/vanish/mixin/chat/EntityArgumentMixin.java:21,24,33; src/main/java/com/enviouse/sef/vanish/mixin/chat/EntitySelectorMixin.java:17,20,29
  - _→ NeoForge 1.21.1:_ Mixin/@At infrastructure (org.spongepowered.asm.mixin.*) is loader-agnostic and unchanged. BUT under NeoForge 1.21.1 (Mojang runtime mappings) the @Mixin target classes and method= names must be the Mojang names. EntityArgument.getEntities/getPlayers and EntitySelector.findSingleEntity/findSinglePlayer should still exist but VERIFY exact method names + the bytecode @At INVOKE descriptors (List.isEmpty()/List.size()) survived 1.20.1->1.21.1 refactors.
  - _notes:_ These mixins filter vanished players out of command targeting (so non-permitted players can't @-select hidden players). They are command-tree-adjacent. Since Forge 1.20.1 dev already used method names (not SRG) in method=, the names likely carry over, but the injection points target internal control flow (isEmpty/size before-call) that is fragile across MC versions. Belongs primarily to the mixin/AT dimension — flagging for cross-reference.


- **[LOW] Reflection-based vanilla command-node removal: CommandNode.getDeclaredField("children"/"literals") to delete vanilla/FTB /msg /tell /w /invsee nodes**
  - _file:line:_ src/main/java/com/enviouse/sef/commands/MsgCommands.java:52-56,247-264; src/main/java/com/enviouse/sef/invsee/InvSeeCommand.java:39-41,71-89
  - _→ NeoForge 1.21.1:_ No API replacement needed — Brigadier CommandNode field names (children, literals, arguments) are from com.mojang.brigadier and are version-stable. Works the same on NeoForge. Pairs with the LOW-priority registration in CommandRegistrationHandler.
  - _notes:_ Fragile-but-portable: relies on private Brigadier field names via reflection, not on any Forge/NeoForge or MC mapping. The /invsee path is gated on ModList.get().isLoaded("ftbessentials") (net.minecraftforge.fml.ModList -> net.neoforged.fml.ModList, line InvSeeCommand.java:20,39). Will still compile/run; only the ModList import in InvSeeCommand changes.


- **[LOW] serverPlayer.openMenu(MenuProvider) for /invsee GUI**
  - _file:line:_ src/main/java/com/enviouse/sef/invsee/InvSeeCommand.java:16,105-115
  - _→ NeoForge 1.21.1:_ NO CHANGE in this file. openMenu(MenuProvider) is vanilla ServerPlayer API and is exactly the NeoForge-preferred replacement for the removed NetworkHooks.openScreen — this command already uses the correct pattern. (MenuType/IMenuTypeExtension concerns live in the menu/container dimension, not here.)
  - _notes:_ Good news: the /invsee command does NOT use NetworkHooks.openScreen, so the command itself needs no menu-opening rewrite. The InvSeeContainer/MenuType registration is a separate dimension.


**Ordering notes:**

- Port the entrypoint/event-bus dimension FIRST: the constructor must expose the NeoForge IEventBus, because VanishCommand registration is wired via MinecraftForge.EVENT_BUS.addListener in ServerEssentialsForge (ServerEssentialsForge.java:79).
- Port the Permissions dimension (PermissionNode ctor + PermissionAPI + PermissionGatherEvent) BEFORE/with the command tree — every command's .requires() and BfcCommands.checkPermission depend on it; commands won't compile until PermissionsHandler is ported.
- Port the Config dimension before commands: nearly every command executor reads ConfigHandler.config.* (ModConfigSpec ConfigValues) inside .requires()/executes() and CommandRegistrationHandler.registerCommands() branches on config booleans.
- The EntityArgument/EntitySelector mixin @At target verification should be done together with the global mixin/AT Mojang-name remapping dimension, not piecemeal here.
- BannedItemsCommands.java:302 ForgeRegistries.ITEMS.getKey swap can be done with the registries dimension sweep; it is the only registry call inside a command file.
- After porting, verify CommandRegistrationHandler is not double-registered: it is both annotated @EventBusSubscriber and manually loader.register(commandRegistrator)'d (ServerEssentialsForge.java:84).


**Open questions (this dimension):**

- Does NeoForge 21.1.x PermissionNode keep the 4-arg ctor (String modid, String node, PermissionType<T>, PermissionResolver<T>) and node.setInformation(Component, Component)? PermissionsHandler builds ~50 nodes and reflects over PermissionNode.class field type — any class-shape change cascades to every command's .requires().
- Does PermissionAPI.getOfflinePermission(UUID, PermissionNode<Boolean>) exist with the same signature/throwing behavior in NeoForge 21.1.x (used by every command's permission check via playerHasPermission)?
- On which bus does PermissionGatherEvent.Nodes fire in NeoForge 21.1.x (GAME vs MOD)? PermissionsHandler is @EventBusSubscriber (defaults to GAME) and ServerEssentialsForge also adds it via MinecraftForge.EVENT_BUS.addListener — confirm both land on the right bus after porting.
- Do EntityArgument.getEntities/getPlayers and EntitySelector.findSingleEntity/findSinglePlayer keep those exact Mojang names and the internal Collection.isEmpty()/List.isEmpty()/List.size() call sites that the vanish mixins inject before, in 1.21.1?
- Confirm RegisterCommandsEvent in NeoForge 21.1.x still supports priority-ordered listeners so the LOW-priority override of vanilla /msg/tell/w and FTB /invsee continues to run after their registrations.


**Verification verdicts:**

- **CONFIRMED** — PermissionsHandler.playerHasPermission -> PermissionAPI.getOfflinePermission(uuid, node); PermissionNode<Boolean> with new PermissionNode<>(MODID, id, PermissionTypes.BOOLEAN, (player,uuid,context)->...); node.setInformation(...); PermissionGatherEvent.Nodes.addNodes()
    - claim: net.minecraftforge.server.permission.* -> net.neoforged.neoforge.server.permission.*: PermissionAPI.getOfflinePermission retained; nodes.PermissionNode 4-arg ctor (modid, nodeName, PermissionType, PermissionResolver); nodes.PermissionTypes.BOOLEAN; events.PermissionGatherEvent.Nodes fires on GAME bus
    - verified → same (confirmed). Package = net.neoforged.neoforge.server.permission. PermissionAPI.getOfflinePermission(UUID player, PermissionNode<T> node, PermissionDynamicContext<?>... context) is retained. nodes.PermissionNode public ctor: (String modID, String nodeName, PermissionType<T> type, PermissionNode.PermissionResolver<T> defaultResolver, PermissionDynamicContextKey<?>... dynamics) -- note it ends in a PermissionDynamicContextKey varargs, so the resolver is the 4th positional arg. PermissionResolver<T> is @FunctionalInterface with resolve(@Nullable ServerPlayer player, UUID playerUUID, PermissionDynamicContext<?>... context) -> the (player, uuid, context)->... lambda is CORRECT (3 params). nodes.PermissionTypes.BOOLEAN exists (PermissionType<Boolean>). setInformation(Component readableName, Component description) exists and returns the node. events.PermissionGatherEvent.Nodes.addNodes has both varargs (PermissionNode<?>...) and Iterable<PermissionNode<?>> overloads. PermissionGatherEvent extends net.neoforged.bus Event and does NOT implement IModBusEvent, so it fires on the main game bus NeoForge.EVENT_BUS in 21.1.x -- GAME bus confirmed.
    - evidence: NeoForge 1.21.x source: src/main/java/net/neoforged/neoforge/server/permission/nodes/PermissionNode.java (ctors + PermissionResolver.resolve(ServerPlayer, UUID, PermissionDynamicContext...)); .../nodes/PermissionTypes.java (public static final PermissionType<Boolean> BOOLEAN = new PermissionType<>(Boolean.class, "boolean")); .../permission/PermissionAPI.java (<T> T getOfflinePermission(UUID, PermissionNode<T>, PermissionDynamicContext<?>...)); .../permission/events/PermissionGatherEvent.java (public abstract class PermissionGatherEvent extends Event; nested Nodes.addNodes(PermissionNode<?>...) & addNodes(Iterable<PermissionNode<?>>), no IModBusEvent -> game bus). Javadoc mirror: nekoyue.github.io/ForgeJavaDocs-NG/javadoc/1.20.6-neoforge/net/neoforged/neoforge/server/permission/nodes/PermissionNode.html (setInformation(Component, Component), getDefaultResolver, etc.); package events at .../server/permission/events/package-summary.html. NeoForge 1.20.6->1.21 migration primer (github.com/neoforged/.github primers/1.21) lists NO permission API changes.
- **CONFIRMED** — Mixins on EntityArgument / EntitySelector targeting getEntities/getPlayers (EntityArgument) and findSingleEntity/findSinglePlayer (EntitySelector) via @At INVOKE method= names, plus @ModifyVariable; @At INVOKE descriptors against List.isEmpty()/List.size()
    - claim: org.spongepowered.asm.mixin.* infrastructure is loader-agnostic and unchanged. Under NeoForge 1.21.1 (Mojang runtime mappings), @Mixin targets and method= names must be Mojang names; EntityArgument.getEntities/getPlayers and EntitySelector.findSingleEntity/findSinglePlayer still exist; verify exact names + that List.isEmpty()/List.size() @At INVOKE descriptors survived 1.20.1->1.21.1 refactors
    - verified → Method-name targets CONFIRMED, but the specific @At INVOKE bytecode descriptors are only PARTIALLY verifiable. Confirmed in 1.21.1 Mojang mappings: net.minecraft.commands.arguments.EntityArgument has static getEntities, getPlayers, getEntity, getPlayer (plus getOptionalEntities/getOptionalPlayers); net.minecraft.commands.arguments.selector.EntitySelector has findSingleEntity, findSinglePlayer, findEntities, findPlayers. These four EntitySelector method names AND EntityArgument names are IDENTICAL in 1.20.1 and 1.21.1 (not renamed). In the 1.20.6->1.21 transition only EntitySelector#predicate was renamed to contextFreePredicate (now takes List<Predicate<Entity>>) -- it does NOT affect the four mixin-target methods. No EntityArgument/EntitySelector method changes in the 1.21.1->1.21.2 primer. CAVEAT: Since Mojang mappings are used as the runtime mapping under NeoForge 1.21.1, the @Mixin class and method= names being Mojang names is correct. However, the exact @At(value="INVOKE", target="...List;isEmpty()Z" / List;size()I) injection sites, their ordinals/shifts, and the @ModifyVariable index/ordinal WITHIN findSingleEntity/findSinglePlayer cannot be confirmed from mapping-name databases -- those depend on the decompiled 1.21.1 method bodies. java.util.List#isEmpty()Z and #size()I are JDK descriptors that are version-stable, but whether those exact call sites still exist (and at the same ordinal) inside the refactored bodies must be checked against the actual 1.21.1 decompiled source / the compiled refmap, which I could not inspect.
    - evidence: 1.21.1 Mojang mappings: mappings.xhyrom.dev/1.21.1/net/minecraft/commands/arguments/selector/entityselector (findSingleEntity, findSinglePlayer, findEntities, findPlayers) and mappings.xhyrom.dev/1.21.1/net/minecraft/commands/arguments/entityargument (getEntities, getPlayers, getEntity, getPlayer). 1.20.1 parity: mappings.xhyrom.dev/1.20.1/net/minecraft/commands/arguments/selector/entityselector (same four method names present). NeoForge 1.20.6->1.21 migration primer (github.com/neoforged/.github/blob/main/primers/1.21/index.md): only change to EntitySelector is 'EntitySelector#predicate -> contextFreePredicate, takes a List<Predicate<Entity>> now'; no rename of the four target methods, no EntityArgument changes. 1.21.1->1.21.2 primer (github.com/neoforged/.github/blob/main/primers/1.21.2/index.md): no EntitySelector/EntityArgument method changes. Mixin infra (org.spongepowered.asm.mixin.*) is loader-agnostic (Sponge Mixin), unchanged across Forge/NeoForge. NOT verifiable from these sources: exact @At INVOKE List.isEmpty()/List.size() call-site presence/ordinal and @ModifyVariable index inside the 1.21.1 method bodies (requires decompiled 1.21.1 bytecode, which mapping-name DBs do not expose).


### Chat formatting internals (& color, hex, Markdown -> Component/Style) (`chat-formatting`)


**Summary:** The core chat-formatting engine (TextFormatter, MarkdownFormatter, BitwiseStyling, SEFUtilities) uses ONLY vanilla net.minecraft.network.chat.* APIs — Component/MutableComponent/Style/TextColor/ChatFormatting plus Component.literal/empty, withStyle, withColor(int), withColor(TextColor), TextColor.fromLegacyFormat, ChatFormatting.getByName. These classes and signatures are effectively unchanged between MC 1.20.1 and 1.21.1, so this dimension is LOW risk overall and requires essentially no rewrites of the formatting logic itself. Crucially, there is NO use of Component.Serializer/toJson/fromJson anywhere in the chat path (all GSON usage is for custom POJO data files), so the 1.20.5+ HolderLookup.Provider/RegistryAccess serialization break does NOT affect this code. The only items worth watching are the ClickEvent/HoverEvent constructors used for click-to-reply (still constructor-based in 1.21.1; they become records only in 1.21.5+), and a few cross-dimension touch points (ServerChatEvent, @SubscribeEvent/@EventBusSubscriber imports) that live in caller files and are owned by the events/chat-event dimension.


**Findings (9):**


- **[LOW] net.minecraft.network.chat.Component / MutableComponent / Style / TextColor + ChatFormatting (Component.empty, Component.literal, withStyle, withColor) — core string->Component parser**
  - _file:line:_ src/main/java/com/enviouse/sef/TextFormatter.java:5-9, src/main/java/com/enviouse/sef/TextFormatter.java:54, src/main/java/com/enviouse/sef/TextFormatter.java:62, src/main/java/com/enviouse/sef/TextFormatter.java:79-81, src/main/java/com/enviouse/sef/BitwiseStyling.java:3-5, src/main/java/com/enviouse/sef/BitwiseStyling.java:27-35
  - _→ NeoForge 1.21.1:_ No change. These are vanilla net.minecraft.network.chat.* classes, unchanged in package and signature 1.20.1->1.21.1. Component.empty(), Component.literal(String), MutableComponent.withStyle(ChatFormatting)/withStyle(Style), Style.EMPTY all present in NeoForge 1.21.1 (Mojang mappings). Pure import-compatible; no edits needed.
  - _notes:_ This is the heart of the dimension and it is vanilla-only. The & color/&l style state machine in stringToFormattedText (lines 52-121) needs no API changes. Note withStyle is non-mutating (returns this for chaining in MutableComponent) and the code relies on that — unchanged behavior.


- **[LOW] TextColor.fromLegacyFormat(ChatFormatting) — used to map legacy & color codes and the config default color to a TextColor**
  - _file:line:_ src/main/java/com/enviouse/sef/TextFormatter.java:59, src/main/java/com/enviouse/sef/TextFormatter.java:145
  - _→ NeoForge 1.21.1:_ No change. net.minecraft.network.chat.TextColor.fromLegacyFormat(ChatFormatting) exists with same signature in 1.21.1.
  - _notes:_ Standard vanilla helper, stable across 1.20.1->1.21.1.


- **[LOW] Style.withColor(int) — the &#RRGGBB hex path constructs a TextColor from a parsed int**
  - _file:line:_ src/main/java/com/enviouse/sef/TextFormatter.java:92
  - _→ NeoForge 1.21.1:_ No change required. Style.withColor(int) overload exists in 1.21.1 (alongside withColor(TextColor) and withColor(ChatFormatting)). curStyle.withColor(Integer.parseInt(hexStr,16)) compiles as-is. (Optionally could be written as withColor(TextColor.fromRgb(int)) but that is not required.)
  - _notes:_ Hex parsing itself (Pattern '#([0-9a-fA-F]{6})', Integer.parseInt(...,16)) is pure Java, unaffected. The permission gate (PermissionsHandler.hexChatNode) depends on the permissions dimension being ported, not on text APIs.


- **[LOW] ChatFormatting.getByName(String) + ChatFormatting enum constants (BLACK..WHITE, BOLD/ITALIC/UNDERLINE/STRIKETHROUGH/OBFUSCATED)**
  - _file:line:_ src/main/java/com/enviouse/sef/TextFormatter.java:58, src/main/java/com/enviouse/sef/TextFormatter.java:124-140, src/main/java/com/enviouse/sef/BitwiseStyling.java:29-33
  - _→ NeoForge 1.21.1:_ No change. net.minecraft.ChatFormatting and getByName(String) unchanged in 1.21.1.
  - _notes:_ Enum names and getByName stable. The config-driven default color (ChatEventHandler.getChatMessageColor()) feeds getByName here; that config value is owned by the config dimension.


- **[LOW] ClickEvent / HoverEvent construction for click-to-reply (new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, String), new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component)) — built on Components produced by TextFormatter.stringToFormattedText**  _(uncertain — needs verification)_
  - _file:line:_ src/main/java/com/enviouse/sef/events/ChatEventHandler.java:23, src/main/java/com/enviouse/sef/events/ChatEventHandler.java:25, src/main/java/com/enviouse/sef/events/ChatEventHandler.java:232-235, src/main/java/com/enviouse/sef/commands/MsgCommands.java:16-18, src/main/java/com/enviouse/sef/commands/MsgCommands.java:219-222
  - _→ NeoForge 1.21.1:_ No change for 1.21.1. net.minecraft.network.chat.ClickEvent(ClickEvent.Action, String) and HoverEvent(HoverEvent.Action, Component) constructors plus Style.withClickEvent/withHoverEvent are all present in MC 1.21.1 / NeoForge 21.1.x. (These became sealed records in 1.21.5+, so this code would break on a later MC, but is fine at the 1.21.1 target.)
  - _notes:_ These callers are the click-to-reply consumers of the formatter. The withStyle(UnaryOperator<Style>) lambda form (ChatEventHandler:232, MsgCommands:219) is also unchanged. Marked uncertain only because the constructor-vs-record change is a known nearby break and exact 1.21.1 ctor arity should be confirmed against the actual jar.


- **[LOW] Style accessors: Style.EMPTY, getClickEvent(), getStyle() on components (used by getHoverClickEventStyle)**
  - _file:line:_ src/main/java/com/enviouse/sef/TextFormatter.java:54, src/main/java/com/enviouse/sef/events/ChatEventHandler.java:58-68
  - _→ NeoForge 1.21.1:_ No change. Style.EMPTY, Style.getClickEvent(), Component.getStyle()/getContents() unchanged in 1.21.1.
  - _notes:_ getHoverClickEventStyle has a pre-existing logic smell ('old instanceof TranslatableContents' where 'old' is a Component, never a Contents) but that is an EXISTING bug, not a port break — out of scope to fix in Phase 0. getContents() returning ComponentContents and TranslatableContents.getArgs() are unchanged.


- **[LOW] MarkdownFormatter & BitwiseStyling string-level Markdown->legacy(&-code) transform**
  - _file:line:_ src/main/java/com/enviouse/sef/MarkdownFormatter.java:1-84, src/main/java/com/enviouse/sef/BitwiseStyling.java:18-26
  - _→ NeoForge 1.21.1:_ No change. MarkdownFormatter is pure Java string manipulation producing &-codes (RESET_ALL_FORMAT etc.); it never touches MC APIs. BitwiseStyling.styleString is also pure String. Only BitwiseStyling.makeEncapsulatingTextComponent touches vanilla (covered above).
  - _notes:_ **bold** *italic* _underline_ ~~strike~~ ~obf~ parsing is entirely string-based and MC-version-agnostic. Zero porting work.


- **[LOW] SEFUtilities player-name formatting -> TextFormatter (nickname/prefix/suffix substitution then stringToFormattedText)**
  - _file:line:_ src/main/java/com/enviouse/sef/utils/SEFUtilities.java:8, src/main/java/com/enviouse/sef/utils/SEFUtilities.java:43-48
  - _→ NeoForge 1.21.1:_ No text-API change. getFormattedPlayerName returns MutableComponent via TextFormatter (vanilla-only).
  - _notes:_ The DEPENDENCIES here are NOT text-API: nicknameProvider/metadataProvider (FTB Essentials FTBEPlayerData + LuckPerms) and ConfigHandler.config.playerNameFormat are owned by the mod-integration and config dimensions. com.mojang.authlib.GameProfile (SEFUtilities.java:6) is unchanged. This file's text-formatting surface is clean; its risk lives in those other dimensions.


- **[LOW] Component.Serializer / Component.Serializer.toJson/fromJson (1.20.5+ now requires HolderLookup.Provider/RegistryAccess)**
  - _file:line:_ src/main/java/com/enviouse/sef/TextFormatter.java (NOT PRESENT), src/main/java/com/enviouse/sef/MarkdownFormatter.java (NOT PRESENT), src/main/java/com/enviouse/sef/BitwiseStyling.java (NOT PRESENT), src/main/java/com/enviouse/sef/utils/SEFUtilities.java (NOT PRESENT)
  - _→ NeoForge 1.21.1:_ N/A — no Component JSON (de)serialization anywhere in the chat-formatting path. All GSON toJson/fromJson in the codebase (e.g. announcements/AnnouncementManager.java:109, mute/MuteManager.java:117, warn/WarnManager.java:81) operate on plain POJOs (Strings/data records), NOT on net.minecraft Components.
  - _notes:_ Explicitly verified by grep across src/main/java: no occurrence of Component.Serializer, no Component-typed toJson/fromJson. The widely-flagged 1.20.5+ component-serialization break therefore does NOT touch this dimension. Components are always built in-memory and sent live (sendSystemMessage/broadcast), never serialized to JSON by this mod.


**Ordering notes:**

- This dimension is essentially self-contained on vanilla text APIs and can be ported (i.e. left as-is) independently — it does NOT need config or events ported first to COMPILE the formatting classes themselves.
- However, the callers that USE these formatters depend on other dimensions: ChatEventHandler (ServerChatEvent, @SubscribeEvent/@EventBusSubscriber imports at events/ChatEventHandler.java:30-32) belongs to the chat-event/event-bus dimension and must be ported there; the formatter API it calls is stable.
- TextFormatter's permission gating (PermissionsHandler.hexChatNode / playerHasColorPermission / coloredChatNode / styledChatNode / markdownChatNode at TextFormatter.java:91,144) depends on the PERMISSIONS dimension being ported (NeoForge PermissionAPI). The formatter signatures (UUID permissionUuid param) do not change.
- SEFUtilities name formatting depends on the MOD-INTEGRATION dimension (FTB Essentials nickname/LuckPerms metadata providers) and the CONFIG dimension (playerNameFormat); port those before SEFUtilities will behave correctly at runtime, though it compiles independently.
- No dependency on the component-serialization changes — safe to ignore that workstream for this dimension.


**Open questions (this dimension):**

- Confirm against the actual NeoForge 21.1.233 / MC 1.21.1 jar that ClickEvent(ClickEvent.Action, String) and HoverEvent(HoverEvent.Action, Component) are still plain constructors (not records). They are records only in 1.21.5+, so 1.21.1 should be fine, but the exact ctor arity should be eyeballed during the build.
- Confirm Style.withColor(int) overload still exists in 1.21.1 (the hex path at TextFormatter.java:92 relies on it). withColor(TextColor)/withColor(ChatFormatting) certainly exist; the int overload is the one to verify — trivially replaceable with withColor(TextColor.fromRgb(int)) if it were removed.
- ChatFormatting.getByName casing: getChatMessageColor() supplies the config string to ChatFormatting.getByName(TextFormatter.java:58). getByName lowercases internally and is unchanged, but the config value source is owned by the config dimension.


**Verification verdicts:**

- **CONFIRMED** — new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, String) / new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component) + Style.withClickEvent/withHoverEvent
    - claim: No change for 1.21.1. net.minecraft.network.chat.ClickEvent(ClickEvent.Action, String) and HoverEvent(HoverEvent.Action, Component) constructors plus Style.withClickEvent/withHoverEvent are all present in MC 1.21.1 / NeoForge 21.1.x. These became sealed records in 1.21.5+, so the code is fine at the 1.21.1 target but would break on a later MC.
    - verified → same
    - evidence: Yarn 1.21.4 javadoc confirms ClickEvent is a 'public class' with public constructor `public ClickEvent(ClickEvent.Action action, String value)` (https://maven.fabricmc.net/docs/yarn-1.21.4+build.8/net/minecraft/text/ClickEvent.html). ClickEvent.Action is a 'public static enum' containing SUGGEST_COMMAND among OPEN_URL/OPEN_FILE/RUN_COMMAND/CHANGE_PAGE/COPY_TO_CLIPBOARD (https://maven.fabricmc.net/docs/yarn-1.21.4+build.8/net/minecraft/text/ClickEvent.Action.html). HoverEvent is a 'public class' with public constructor `public HoverEvent(HoverEvent.Action<T> action, T contents)`, SHOW_TEXT taking a Text/Component (https://maven.fabricmc.net/docs/yarn-1.21.4+build.8/net/minecraft/text/HoverEvent.html). The sealed-interface/record refactor (with field renames clickEvent->click_event, value->command/url, hoverEvent->hover_event) landed in 1.21.5 per Minecraft Wiki Text_component_format/Before_Java_Edition_1.21.5 and Yarn 1.21.5 docs where ClickEvent/HoverEvent become sealed interfaces with records like ClickEvent.RunCommand/HoverEvent.ShowItem (https://maven.fabricmc.net/docs/yarn-1.21.5+build.1/net/minecraft/text/ClickEvent.RunCommand.html, https://github.com/EngineHub/WorldEdit/issues/2756). 1.21.1 predates this refactor, so the concrete-class constructors and Style.withClickEvent/withHoverEvent apply unchanged at the target.


### Forge PermissionAPI / permission nodes (`permissions`)


**Summary:** The mod's entire permission system is built on Forge's net.minecraftforge.server.permission.* API: ~70 boolean PermissionNodes declared in PermissionsHandler plus 6 dynamic vanish nodes registered in ServerEssentialsForge, all gathered via PermissionGatherEvent.Nodes and queried through PermissionAPI.getPermission / getOfflinePermission. NeoForge 1.21.1 keeps a near-identical API under net.neoforged.neoforge.server.permission.* (PermissionAPI, nodes.PermissionNode, nodes.PermissionTypes, events.PermissionGatherEvent.Nodes), so the port is mostly mechanical package swaps. Primary risks are the exact PermissionNode<T> constructor signature, the PermissionResolver/setInformation method shapes, the bus the gather event fires on (GAME bus under NeoForge), and the reflection-based node enumeration in registerPermissionNodes which must still see only PermissionNode-typed fields. Overall risk MEDIUM: many call sites but a stable, similar API.


**Findings (9):**


- **[MEDIUM] Import net.minecraftforge.server.permission.PermissionAPI / .events.PermissionGatherEvent.Nodes / .nodes.PermissionNode / .nodes.PermissionTypes**
  - _file:line:_ src/main/java/com/enviouse/sef/config/PermissionsHandler.java:14-17
  - _→ NeoForge 1.21.1:_ Swap package root to net.neoforged.neoforge.server.permission.* : net.neoforged.neoforge.server.permission.PermissionAPI, net.neoforged.neoforge.server.permission.events.PermissionGatherEvent.Nodes, net.neoforged.neoforge.server.permission.nodes.PermissionNode, net.neoforged.neoforge.server.permission.nodes.PermissionTypes. Sub-package layout (events/, nodes/) and PermissionTypes.BOOLEAN are unchanged in NeoForge 1.21.1.
  - _notes:_ This is the canonical declaration site holding ~70 static PermissionNode<Boolean> fields plus the hex + per-color (0-9,a-f) map nodes built in the static block (lines 169-179). Pure import swap, but it is the linchpin of the whole permission feature.


- **[HIGH] new PermissionNode<>(MODID, id, PermissionTypes.BOOLEAN, (player, uuid, context) -> defVal)**  _(uncertain — needs verification)_
  - _file:line:_ src/main/java/com/enviouse/sef/config/PermissionsHandler.java:198-199
  - _→ NeoForge 1.21.1:_ NeoForge net.neoforged.neoforge.server.permission.nodes.PermissionNode<T> public ctor: PermissionNode(String modId, String nodeName, PermissionType<T> type, PermissionResolver<T> defaultResolver, PermissionDynamicContextKey<?>... dynamics). The 4-arg call (modId, name, PermissionTypes.BOOLEAN, resolver) maps directly since the varargs key array can be empty. The resolver lambda is net.neoforged.neoforge.server.permission.nodes.PermissionNode.PermissionResolver<T> with signature resolve(@Nullable ServerPlayer player, UUID playerUUID, PermissionDynamicContext<?>... context) -> T, so (player, uuid, context) -> defVal stays valid (context becomes a varargs array, used positionally as before).
  - _notes:_ VERIFY exact ctor arity/order in NeoForge 21.1.x — Forge had both a 3-arg (modId, type, resolver) deriving name from modid+node and the 4-arg (modId, nodeName, type, resolver) used here. If NeoForge dropped/renamed the resolver functional interface or reordered params this breaks every node. Same ctor shape is reused in ServerEssentialsForge vanish nodes.


- **[LOW] node.setInformation(Component, Component) for node display name + description**  _(uncertain — needs verification)_
  - _file:line:_ src/main/java/com/enviouse/sef/config/PermissionsHandler.java:200
  - _→ NeoForge 1.21.1:_ NeoForge PermissionNode.setInformation(Component name, Component description) is retained and returns the node (builder-style). Component.literal(...) is unchanged vanilla. Keep as-is after import swap.
  - _notes:_ VERIFY method still named setInformation and accepts two Components in 21.1.x. Low impact if it disappears (information is cosmetic), but it is called for every node.


- **[MEDIUM] @SubscribeEvent registerPermissionNodes(PermissionGatherEvent.Nodes pge) registered via loader.register -> MinecraftForge.EVENT_BUS.register; pge.addNodes(PermissionNode<?>...)**  _(uncertain — needs verification)_
  - _file:line:_ src/main/java/com/enviouse/sef/config/PermissionsHandler.java:181-195
  - _→ NeoForge 1.21.1:_ Event becomes net.neoforged.neoforge.server.permission.events.PermissionGatherEvent.Nodes, fired on the NeoForge GAME bus (NeoForge.EVENT_BUS). Registration path loader.register(...) must resolve to net.neoforged.neoforge.common.NeoForge.EVENT_BUS.register(...). Nodes.addNodes(PermissionNode<?>...) is varargs and unchanged. The @SubscribeEvent import moves to net.neoforged.bus.api.SubscribeEvent and @EventBusSubscriber to net.neoforged.fml.common.EventBusSubscriber (default bus GAME, correct here).
  - _notes:_ VERIFY PermissionGatherEvent.Nodes fires on the GAME/NeoForge event bus (it did on Forge's FORGE bus). The reflection loop only adds fields whose getType()==PermissionNode.class (line 183) — after the package swap the .class literal points at the NeoForge type, so the filter keeps working; no behavioral change. Depends on @EventBusSubscriber/SubscribeEvent dimension and on loader.register being repointed (loader.java still imports forge bus + NetworkConstants/IExtensionPoint — see ordering notes).


- **[HIGH] PermissionAPI.getOfflinePermission(UUID, PermissionNode<Boolean>) -> boolean**  _(uncertain — needs verification)_
  - _file:line:_ src/main/java/com/enviouse/sef/config/PermissionsHandler.java:207
  - _→ NeoForge 1.21.1:_ net.neoforged.neoforge.server.permission.PermissionAPI.getOfflinePermission(UUID, PermissionNode<T>) is retained in NeoForge 1.21.1 with the same signature (returns T). The surrounding catch(IllegalStateException) for pre-init queries should stay. Used by playerHasPermission, the basis for ~80 downstream permission checks (tab-list nickname/metadata, chat colors/styles, command gating).
  - _notes:_ VERIFY getOfflinePermission still exists by that name in NeoForge PermissionAPI (it is the offline/UUID-based lookup; some refactors only kept getPermission(ServerPlayer,...)). This single method backs nearly every feature gate in the mod, so a rename is high blast radius even though trivial to fix.


- **[LOW] PermissionAPI.getPermission(ServerPlayer player, PermissionNode<Boolean> node, PermissionDynamicContext...)**
  - _file:line:_ src/main/java/com/enviouse/sef/vanish/VanishUtil.java:117, src/main/java/com/enviouse/sef/vanish/VanishUtil.java:146, src/main/java/com/enviouse/sef/vanish/VanishUtil.java:163, src/main/java/com/enviouse/sef/banned/BannedItemsManager.java:284
  - _→ NeoForge 1.21.1:_ net.neoforged.neoforge.server.permission.PermissionAPI.getPermission(ServerPlayer, PermissionNode<T>, PermissionDynamicContext<?>...) retained. Only the import (VanishUtil.java:21-22) and the fully-qualified reference in BannedItemsManager.java:284 (net.minecraftforge.server.permission.PermissionAPI / .nodes.PermissionNode) need the package swap.
  - _notes:_ Online (ServerPlayer) variant; lower risk than getOfflinePermission. VanishUtil imports at lines 21-22 are the forge package. Mechanical.


- **[MEDIUM] PermissionGatherEvent.Nodes#addNodes + new PermissionNode<>(MODID, "vanishsee."+level/"vanish."+level, PermissionTypes.BOOLEAN, (player,uuid,context)->false) registered via MinecraftForge.EVENT_BUS.addListener(this::registerVanishPermissions)**  _(uncertain — needs verification)_
  - _file:line:_ src/main/java/com/enviouse/sef/ServerEssentialsForge.java:98-113
  - _→ NeoForge 1.21.1:_ Same package swaps to net.neoforged.neoforge.server.permission.*; listener registration MinecraftForge.EVENT_BUS.addListener(...) -> NeoForge.EVENT_BUS.addListener(...). 6 dynamic boolean nodes (sef.vanishsee.1-3, sef.vanish.1-3) added to Vanishmod.VANISH_SEE_NODES / VANISH_LEVEL_NODES (Vanishmod.java:14,17). Identical ctor + addNodes shape as PermissionsHandler.
  - _notes:_ Second registration handler for the same gather event, this time via explicit addListener rather than @SubscribeEvent. Shares the uncertain PermissionNode ctor signature. Vanishmod.java only imports the node type (line 6) for the maps — pure import swap there.


- **[LOW] Maps Map<Integer,PermissionNode<Boolean>> VANISH_SEE_NODES / VANISH_LEVEL_NODES and Map<Character,PermissionNode<Boolean>> perColorChatNodes holding node references**
  - _file:line:_ src/main/java/com/enviouse/sef/vanish/Vanishmod.java:6, src/main/java/com/enviouse/sef/vanish/Vanishmod.java:14, src/main/java/com/enviouse/sef/vanish/Vanishmod.java:17, src/main/java/com/enviouse/sef/config/PermissionsHandler.java:169-170
  - _→ NeoForge 1.21.1:_ Only the PermissionNode import changes to net.neoforged.neoforge.server.permission.nodes.PermissionNode; generic Map<Integer/Character, PermissionNode<Boolean>> declarations are unaffected.
  - _notes:_ Data holders only; no API call. playerHasColorPermission (PermissionsHandler.java:213-217) reads perColorChatNodes and delegates to playerHasPermission, so it inherits the getOfflinePermission risk but needs no edit itself.


- **[LOW] src/test/java/PrintPermissions.java**
  - _file:line:_ src/test/java/PrintPermissions.java:1-15
  - _→ NeoForge 1.21.1:_ No change needed for this dimension — despite the filename it only exercises PlayerData.encodeStr/decodeStr and contains NO permission API references.
  - _notes:_ Confirmed by reading the whole file: imports only com.enviouse.sef.config.PlayerData and calls encode/decode helpers. Not a PermissionAPI test.


**Ordering notes:**

- Depends on the @EventBusSubscriber / @SubscribeEvent dimension being ported first: PermissionsHandler.java:12-13,20 use net.minecraftforge.eventbus.api.SubscribeEvent and net.minecraftforge.fml.common.Mod.EventBusSubscriber which must become net.neoforged.bus.api.SubscribeEvent and top-level net.neoforged.fml.common.EventBusSubscriber (default GAME bus).
- Depends on the event-bus dimension porting loader.register (src/main/java/com/enviouse/sef/utils/loader.java:14-16) from MinecraftForge.EVENT_BUS to NeoForge.EVENT_BUS, since the @SubscribeEvent permission handler is registered through it; loader.java also still uses NetworkConstants/IExtensionPoint.DisplayTest (loader.java:4-8,19-23) which is a separate (server-only marker) dimension but must compile for the permission handler to register.
- Depends on the entrypoint dimension: ServerEssentialsForge constructor (ServerEssentialsForge.java:79-80) registers the vanish permission listener via MinecraftForge.EVENT_BUS.addListener -> must become NeoForge.EVENT_BUS.addListener; the constructor signature change ((IEventBus, ModContainer)) and removal of FMLJavaModLoadingContext/ModLoadingContext are handled by the entrypoint/config dimensions but the permission listener registration call site lives here.
- Component/Style serialization dimension is adjacent: setInformation passes Component.literal(...) and TextFormatter.stringToFormattedText(...) (PermissionsHandler.java:200) — node info Components are not serialized to network/JSON, so the 1.20.5+ HolderLookup.Provider serialization change does NOT affect this dimension.
- No dependency on FTB/Curios/LuckPerms dimensions: the mod talks only to NeoForge PermissionAPI; LuckPerms participates at runtime as a PermissionAPI provider but there is no compile-time LuckPerms reference in any permission file audited.


**Open questions (this dimension):**

- Exact NeoForge 1.21.1 PermissionNode<T> public constructor signature/arity: is it (String modId, String nodeName, PermissionType<T> type, PermissionResolver<T> resolver, PermissionDynamicContextKey<?>...) as in late Forge, or did NeoForge reorder/rename? This governs ~76 node constructions.
- Is the resolver functional interface still PermissionNode.PermissionResolver<T> with resolve(@Nullable ServerPlayer, UUID, PermissionDynamicContext<?>...)? The lambdas (player,uuid,context)->value depend on this exact shape.
- Does PermissionAPI.getOfflinePermission(UUID, PermissionNode) still exist under that name in NeoForge 21.1.x, or was it folded/renamed? It backs essentially every offline permission check in the mod (tab-list, color checks).
- On which bus does net.neoforged.neoforge.server.permission.events.PermissionGatherEvent.Nodes fire under NeoForge 1.21.1 — the GAME (NeoForge.EVENT_BUS) bus? Both registration sites (@SubscribeEvent in PermissionsHandler, addListener in ServerEssentialsForge) must target the correct bus.
- Is PermissionNode.setInformation(Component name, Component description) still present with that name/arity in NeoForge 21.1.x?
- Does Nodes.addNodes(...) remain varargs PermissionNode<?>... (the reflection loop passes one node per call, and the per-color loop passes one at a time, so a List<PermissionNode<?>> overload would also work)?


**Verification verdicts:**

- **CONFIRMED** — new PermissionNode<>(MODID, id, PermissionTypes.BOOLEAN, (player, uuid, context) -> defVal)
    - claim: NeoForge net.neoforged.neoforge.server.permission.nodes.PermissionNode public ctor PermissionNode(String modId, String nodeName, PermissionType<T>, PermissionResolver<T>, PermissionDynamicContextKey<?>... dynamics); 4-arg call maps directly via empty varargs; resolver lambda is PermissionNode.PermissionResolver<T> with resolve(@Nullable ServerPlayer, UUID, PermissionDynamicContext<?>...) -> T, so (player, uuid, context) -> defVal stays valid.
    - verified → same
    - evidence: ForgeJavaDocs-NG 1.21.x-neoforge PermissionNode: public ctor PermissionNode(String modID, String nodeName, PermissionType<T> type, PermissionNode.PermissionResolver<T> defaultResolver, PermissionDynamicContextKey<?>... dynamics). PermissionResolver.resolve(@Nullable ServerPlayer player, UUID playerUUID, PermissionDynamicContext<?>... context) -> T. https://nekoyue.github.io/ForgeJavaDocs-NG/javadoc/1.21.x-neoforge/net/neoforged/neoforge/server/permission/nodes/PermissionNode.html and .../PermissionNode.PermissionResolver.html . Note: lambda param 'context' is a varargs PermissionDynamicContext<?>[] (named 'context' in the lambda) — unused here, valid. PermissionTypes.BOOLEAN is in net.neoforged.neoforge.server.permission.nodes.PermissionTypes.
- **CONFIRMED** — node.setInformation(Component, Component)
    - claim: NeoForge PermissionNode.setInformation(Component name, Component description) retained and returns the node (builder-style); keep as-is after import swap.
    - verified → same
    - evidence: ForgeJavaDocs-NG 1.21.x-neoforge PermissionNode: public PermissionNode<T> setInformation(Component readableName, Component description) returns the PermissionNode instance. https://nekoyue.github.io/ForgeJavaDocs-NG/javadoc/1.21.x-neoforge/net/neoforged/neoforge/server/permission/nodes/PermissionNode.html . Component.literal is unchanged vanilla net.minecraft.network.chat.Component. The source (PermissionsHandler.ezyPermission) does not use the return value, so builder-style is irrelevant but compatible.
- **CONFIRMED** — @SubscribeEvent registerPermissionNodes(PermissionGatherEvent.Nodes pge) ... pge.addNodes(PermissionNode<?>...); registered on event bus
    - claim: Event becomes net.neoforged.neoforge.server.permission.events.PermissionGatherEvent.Nodes, fired on NeoForge GAME bus (NeoForge.EVENT_BUS); loader.register -> net.neoforged.neoforge.common.NeoForge.EVENT_BUS.register; addNodes(PermissionNode<?>...) varargs unchanged; @SubscribeEvent -> net.neoforged.bus.api.SubscribeEvent, @EventBusSubscriber -> net.neoforged.fml.common.EventBusSubscriber (default bus GAME, correct here).
    - verified → same — confirmed. PermissionGatherEvent extends net.neoforged.bus.api.Event and does NOT implement IModBusEvent, so it is fired on the game bus (NeoForge.EVENT_BUS); registering the @EventBusSubscriber/handler on the game bus is correct. Imports: event=net.neoforged.neoforge.server.permission.events.PermissionGatherEvent.Nodes; bus=net.neoforged.neoforge.common.NeoForge.EVENT_BUS; @SubscribeEvent=net.neoforged.bus.api.SubscribeEvent; @EventBusSubscriber=net.neoforged.fml.common.EventBusSubscriber (default bus = GAME). Caveat: a 1.21.1 @EventBusSubscriber should specify modid (e.g. @EventBusSubscriber(modid = MODID)); the bare @EventBusSubscriber in source still resolves modid from the only @Mod in the jar but supplying modid is recommended.
    - evidence: NeoForge 1.21.x source PermissionGatherEvent.java: 'public abstract class PermissionGatherEvent extends Event' with 'public static class Nodes extends PermissionGatherEvent', no IModBusEvent; addNodes(PermissionNode<?>... nodes) and addNodes(Iterable<PermissionNode<?>>). https://github.com/neoforged/NeoForge (1.21.x src/main/java/net/neoforged/neoforge/server/permission/events/PermissionGatherEvent.java). Game-bus / NeoForge.EVENT_BUS guidance: https://docs.neoforged.net/docs/concepts/events/ . EventBus annotation packages: https://neoforged.net/news/20.2eventbus-changes/ .
- **CONFIRMED** — PermissionAPI.getOfflinePermission(UUID, PermissionNode<Boolean>) -> boolean
    - claim: net.neoforged.neoforge.server.permission.PermissionAPI.getOfflinePermission(UUID, PermissionNode<T>) retained in 1.21.1 with same signature (returns T); surrounding catch(IllegalStateException) stays.
    - verified → same — confirmed. Actual signature is generic with trailing varargs: public static <T> T getOfflinePermission(UUID player, PermissionNode<T> node, PermissionDynamicContext<?>... context). The 2-arg call getOfflinePermission(uuid, node) binds with empty varargs and auto-unboxes T=Boolean to boolean. The catch(IllegalStateException) is still valid: NeoForge throws UnregisteredPermissionException (a subclass of IllegalStateException) for nodes queried before registration/init.
    - evidence: ForgeJavaDocs-NG 1.21.x-neoforge PermissionAPI: public static <T> T getOfflinePermission(UUID player, PermissionNode<T> node, PermissionDynamicContext<?>... context); also getPermission(ServerPlayer, PermissionNode<T>, ...). https://nekoyue.github.io/ForgeJavaDocs-NG/javadoc/1.21.x-neoforge/net/neoforged/neoforge/server/permission/PermissionAPI.html . UnregisteredPermissionException extends IllegalStateException — see net.neoforged.neoforge.server.permission.exceptions.UnregisteredPermissionException in same javadoc tree.
- **CONFIRMED** — PermissionGatherEvent.Nodes#addNodes + new PermissionNode<>(MODID, "vanishsee."+level/"vanish."+level, PermissionTypes.BOOLEAN, (player,uuid,context)->false) registered via MinecraftForge.EVENT_BUS.addListener(this::registerVanishPermissions)
    - claim: Same package swaps to net.neoforged.neoforge.server.permission.*; MinecraftForge.EVENT_BUS.addListener(...) -> NeoForge.EVENT_BUS.addListener(...); 6 dynamic boolean nodes added to Vanishmod.VANISH_SEE_NODES / VANISH_LEVEL_NODES; identical ctor + addNodes shape as PermissionsHandler.
    - verified → same — confirmed, with the same details as items 1, 3, 4. MinecraftForge.EVENT_BUS.addListener(this::registerVanishPermissions) -> net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(this::registerVanishPermissions) is correct because PermissionGatherEvent is a game-bus event (extends Event, not IModBusEvent). Handler param type -> net.neoforged.neoforge.server.permission.events.PermissionGatherEvent.Nodes; event.addNodes(node) unchanged; node ctor uses net.neoforged.neoforge.server.permission.nodes.PermissionNode + PermissionTypes.BOOLEAN with (player,uuid,context)->false resolver. (These are not 'dynamic' nodes in the NeoForge sense — no PermissionDynamicContextKey is passed — they are plain per-level boolean nodes; harmless wording only.)
    - evidence: Same primary sources: PermissionNode ctor + PermissionResolver and PermissionTypes.BOOLEAN https://nekoyue.github.io/ForgeJavaDocs-NG/javadoc/1.21.x-neoforge/net/neoforged/neoforge/server/permission/nodes/PermissionNode.html ; PermissionGatherEvent extends Event (game bus, not IModBusEvent) per NeoForge 1.21.x src https://github.com/neoforged/NeoForge ; game-bus registration via NeoForge.EVENT_BUS.addListener per https://docs.neoforged.net/docs/concepts/events/ . Source confirmed at /mnt/hermes/projects/SEFPORTED/SourceCodeOld/Server-Essentials-Forge/src/main/java/com/enviouse/sef/ServerEssentialsForge.java:80,98-111 (MinecraftForge.EVENT_BUS.addListener(this::registerVanishPermissions) + registerVanishPermissions(PermissionGatherEvent.Nodes)) and Vanishmod.java:14,17.


### Config spec & data persistence (file IO) (`config-persistence`)


**Summary:** The mod has two config layers: (1) NeoForge-spec-driven TOML configs via ForgeConfigSpec -> must become ModConfigSpec, and (2) a large set of self-managed JSON/text data files written with plain java.nio + Gson, which are API-stable EXCEPT they obtain their base directory via MinecraftServer.getServerDirectory().toPath() (the return type changed File->Path in 1.20.5+). The TOML config registration is centralized in a fragile helper (loader.MLConfig) that directly references IConfigSpec (whose interface changed in NeoForge) and ModLoadingContext.registerConfig (moved to ModContainer). ConfigHandler.reloadFromDisk() hand-rolls a CommentedFileConfig (nightconfig) load + spec.setConfig(); nightconfig is still bundled by NeoForge so the API should survive but needs verification. Overall: TOML/config wiring is HIGH risk (entrypoint + IConfigSpec + custom filenames), Gson/java.nio persistence is LOW-MEDIUM (mechanical getServerDirectory fix), the ForgeConfigSpec->ModConfigSpec define/defineInRange/push/pop/comment API is a mechanical import swap.


**Findings (11):**


- **[MEDIUM] net.minecraftforge.common.ForgeConfigSpec (Builder, ConfigValue<T>, define, defineInRange, comment, push, pop, build) used pervasively to build the COMMON config spec**
  - _file:line:_ src/main/java/com/enviouse/sef/config/ConfigHandler.java:6, ConfigHandler.java:9-11, ConfigHandler.java:29-207, ConfigHandler.java:208-526
  - _→ NeoForge 1.21.1:_ net.neoforged.neoforge.common.ModConfigSpec with ModConfigSpec.Builder / ModConfigSpec.ConfigValue<T> / ModConfigSpec.BooleanValue. The Builder API (define, defineInRange, comment(String...), push, pop, build, configure) is 1:1 with Forge's, so this is a package/type-name swap only. ~200 ConfigValue field declarations + define() calls change type name from ForgeConfigSpec.* to ModConfigSpec.*.
  - _notes:_ Mechanical but very high site count (entire ConfigHandler.ConfigBuilder, ~300 lines). No semantic change. The 'COMMON' config type still exists in NeoForge ModConfig.Type. Strings stay String, ints use defineInRange exactly as now.


- **[MEDIUM] ForgeConfigSpec used in Vanish config: new ForgeConfigSpec.Builder().configure(Config::new) returning Pair<Config,ForgeConfigSpec>, plus ForgeConfigSpec.BooleanValue / ConfigValue<String> fields**
  - _file:line:_ src/main/java/com/enviouse/sef/vanish/VanishConfig.java:5-7, VanishConfig.java:10-18, VanishConfig.java:20-54, VanishConfig.java:134-165
  - _→ NeoForge 1.21.1:_ net.neoforged.neoforge.common.ModConfigSpec + ModConfigSpec.Builder().configure(Config::new) -> Pair<Config, ModConfigSpec>. BooleanValue/ConfigValue become ModConfigSpec.BooleanValue / ModConfigSpec.ConfigValue. org.apache.commons.lang3.tuple.Pair return type is unchanged (NeoForge configure() still returns a commons-lang3 Pair).
  - _notes:_ Same Builder pattern; configure() exists on ModConfigSpec.Builder in NeoForge. Push/pop nesting (interaction section) carries over unchanged.


- **[HIGH] ModLoadingContext.get().registerConfig(ModConfig.Type, IConfigSpec, fileName) — config registration moved off ModLoadingContext, and IConfigSpec is referenced as a raw parameter type**  _(uncertain — needs verification)_
  - _file:line:_ src/main/java/com/enviouse/sef/utils/loader.java:5-7, loader.java:13, loader.java:26-29
  - _→ NeoForge 1.21.1:_ Registration must move to ModContainer.registerConfig(ModConfig.Type, IConfigSpec[, fileName]). In NeoForge the @Mod constructor receives (IEventBus, ModContainer); pass that ModContainer into the helper. The helper's IConfigSpec<?> parameter must be retyped: NeoForge's net.neoforged.fml.config.IConfigSpec is NOT generic and has a different shape than Forge's IConfigSpec<?>. ModConfigSpec implements the NeoForge IConfigSpec, so the cleanest fix is to type the param as ModConfigSpec (or net.neoforged.fml.config.IConfigSpec without a type arg) and call modContainer.registerConfig(ModConfig.Type.valueOf(cType), spec, "sef/common.toml"). ModConfig.Type -> net.neoforged.fml.config.ModConfig.
  - _notes:_ Two things break: (a) ModLoadingContext is the wrong entry point — registerConfig is now on ModContainer; (b) the IConfigSpec<?> generic param will not compile because NeoForge's IConfigSpec dropped the type parameter and changed methods. The custom filename 'sef/common.toml' (subdirectory!) is preserved by the 3-arg overload, which NeoForge keeps. Best to drop the IConfigSpec abstraction entirely and pass ModConfigSpec directly.


- **[HIGH] Direct ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, VanishConfig.SERVER_SPEC, "sef-vanish-server.toml") in the mod constructor**
  - _file:line:_ src/main/java/com/enviouse/sef/ServerEssentialsForge.java:76
  - _→ NeoForge 1.21.1:_ modContainer.registerConfig(ModConfig.Type.SERVER, VanishConfig.SERVER_SPEC, "sef-vanish-server.toml") using the ModContainer injected into the @Mod constructor. ModConfig.Type.SERVER still exists. Custom filename overload is retained in NeoForge.
  - _notes:_ Same migration as MLConfig but for the SERVER spec. Once SERVER_SPEC is a ModConfigSpec it satisfies the new registerConfig signature. Depends on the @Mod constructor refactor (entrypoint dimension) to have a ModContainer in scope.


- **[LOW] ModConfigEvent.Loading / ModConfigEvent.Reloading subscribed on the MOD bus to trigger IReloadable reloads**
  - _file:line:_ src/main/java/com/enviouse/sef/config/ConfigurationEventHandler.java:6-10, ConfigurationEventHandler.java:24-31
  - _→ NeoForge 1.21.1:_ net.minecraftforge.fml.event.config.ModConfigEvent.Loading/.Reloading -> net.neoforged.fml.event.config.ModConfigEvent.Loading/.Reloading. @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD) -> net.neoforged.fml.common.EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD) (now top-level). @SubscribeEvent -> net.neoforged.bus.api.SubscribeEvent. These events still fire on the MOD bus in NeoForge.
  - _notes:_ Import swaps only; event names and MOD-bus semantics unchanged. The reloadable list / IReloadable interface (config/IReloadable.java) is pure Java and unaffected.


- **[HIGH] ConfigHandler.reloadFromDisk() hand-rolls config reload: FMLPaths.CONFIGDIR + CommentedFileConfig.builder(...).sync().autosave().writingMode(WritingMode.REPLACE).build(); cfg.load(); spec.setConfig(cfg)**  _(uncertain — needs verification)_
  - _file:line:_ src/main/java/com/enviouse/sef/config/ConfigHandler.java:3-4, ConfigHandler.java:13-26
  - _→ NeoForge 1.21.1:_ FMLPaths.CONFIGDIR -> net.neoforged.fml.loading.FMLPaths.CONFIGDIR (same .get() API). nightconfig (com.electronwill.nightconfig.core.file.CommentedFileConfig + io.WritingMode) is still bundled by NeoForge, so those imports survive unchanged. ModConfigSpec exposes setConfig(UnmodifiableCommentedConfig)/acceptConfig like ForgeConfigSpec did; VERIFY spec.setConfig(CommentedFileConfig) still has that exact signature on ModConfigSpec (NeoForge changed parts of the IConfigSpec contract). Safer/idiomatic alternative: don't manually re-read — rely on ModConfigEvent.Reloading, or use modContainer config handling.
  - _notes:_ Manual setConfig on the spec is the fragile part: NeoForge's IConfigSpec rework changed how a loaded config is attached to a spec (acceptConfig/correct flow). nightconfig classes themselves are unchanged. FMLPaths.CONFIGDIR is a trivial swap. This method may need to be rewritten to use NeoForge's config plumbing rather than poking the spec directly.


- **[MEDIUM] MinecraftServer.getServerDirectory().toPath() — assumes getServerDirectory() returns java.io.File**
  - _file:line:_ src/main/java/com/enviouse/sef/filter/FilterManager.java:16, src/main/java/com/enviouse/sef/alts/AltTracker.java:50, src/main/java/com/enviouse/sef/announcements/AnnouncementManager.java:70, src/main/java/com/enviouse/sef/chat/OpBulletinHandler.java:33, src/main/java/com/enviouse/sef/warn/WarnManager.java:76, src/main/java/com/enviouse/sef/ServerEssentialsForge.java:137
  - _→ NeoForge 1.21.1:_ In MC 1.20.5+/1.21.1 MinecraftServer.getServerDirectory() returns java.nio.file.Path (not File). Remove the trailing .toPath() at each site (server.getServerDirectory().resolve("serverconfig")...). At ServerEssentialsForge.java:137, the `java.io.File serverDir = ev.getServer().getServerDirectory();` declaration and the `serverDir.toPath()` on the next line (~:139) must change to Path. The null-check for integrated servers still applies (getServerDirectory may still be usable, but the singleplayer fallback to FMLPaths.CONFIGDIR should be kept).
  - _notes:_ Vanilla signature change, multi-site (6 call sites). Pure mechanical once confirmed. Everything downstream (.resolve(...), Files.* ) is unchanged. This is the only file-IO break in the Gson managers.


- **[LOW] MinecraftServer.getWorldPath(LevelResource.ROOT) used to locate <world>/serverconfig (MuteManager, BannedItemsManager)**
  - _file:line:_ src/main/java/com/enviouse/sef/mute/MuteManager.java:110-111, src/main/java/com/enviouse/sef/banned/BannedItemsManager.java:75-77
  - _→ NeoForge 1.21.1:_ No change. net.minecraft.world.level.storage.LevelResource.ROOT and MinecraftServer.getWorldPath(LevelResource) both exist unchanged in 1.21.1 and already return Path. These two managers are fine as-is (unlike the getServerDirectory() ones).
  - _notes:_ Inconsistency worth noting: Mute/BannedItems use getWorldPath(ROOT) (world-relative) while Warn/Alt/Announcement/Filter/OpBulletin use getServerDirectory() (server-root-relative) — different base dirs, but both vanilla-stable. Only getServerDirectory ones need the File->Path edit.


- **[LOW] Gson-based JSON persistence (com.google.gson.*) across all data managers**
  - _file:line:_ src/main/java/com/enviouse/sef/mute/MuteManager.java:3-5,40-41,116-134, src/main/java/com/enviouse/sef/warn/WarnManager.java:3-5,80-99, src/main/java/com/enviouse/sef/alts/AltTracker.java:3-5,55-83, src/main/java/com/enviouse/sef/announcements/AnnouncementManager.java:3-5,81-92, src/main/java/com/enviouse/sef/filter/FilterDataStore.java:3-5,35-53, src/main/java/com/enviouse/sef/banned/BannedItemsManager.java:57,87-168, src/main/java/com/enviouse/sef/motd/MotdManager.java:3-5,32-55
  - _→ NeoForge 1.21.1:_ No change. Gson is a transitive library available at runtime under NeoForge 1.21.1 (bundled via MC/loader libs as in Forge). TypeToken, GsonBuilder, JsonParser/JsonObject/JsonArray all unchanged. Files.newBufferedReader/Writer, Files.createDirectories, Files.exists are plain java.nio and unaffected. Recommend confirming Gson is on the runtime classpath; if not, declare it explicitly.
  - _notes:_ All save/load logic is provider-agnostic plain Java + Gson; only the base-directory acquisition (getServerDirectory) breaks (separate finding). Records used by Gson (FilterRecord, Announcement, MotdData) are fine on Java 21.


- **[LOW] PlayerData custom text-format persistence using java.io.FileInputStream/FileOutputStream (sef.playerdata)**
  - _file:line:_ src/main/java/com/enviouse/sef/config/PlayerData.java:3-7, PlayerData.java:91-126
  - _→ NeoForge 1.21.1:_ No change. Pure java.io / java.lang string parsing; no Forge/MC API. Loaded via loadFromDir(File)/saveToDir(File) — the File comes from the caller (player dir), not from getServerDirectory(), so unaffected. VERIFY the caller that supplies the playerDirectory (likely a player-event handler) still produces a valid File path under 1.21.1.
  - _notes:_ Bespoke encode/decode format, no API surface. Only risk is whatever upstream supplies playerDirectory — out of scope for this dimension but worth a one-line check in the player-event dimension.


- **[LOW] 'chat logging' referenced in config comment (enableChatReplies) — is it file IO?**
  - _file:line:_ src/main/java/com/enviouse/sef/config/ConfigHandler.java:263, src/main/java/com/enviouse/sef/chat/ChatMessageManager.java:14-37
  - _→ NeoForge 1.21.1:_ No file IO. ChatMessageManager stores chat records in in-memory ConcurrentHashMaps only (MESSAGES, LAST_MESSAGE), reset on init(). There is NO logs/chat/ directory written by this mod (grep for logs/chat, chatlog, FileWriter, etc. found nothing). No persistence migration needed for chat logs.
  - _notes:_ Confirms the prompt's question: chat logs are NOT written to disk; the 'chat logging' in the config comment is the in-memory reply lookup store. Nothing to port here.


**Ordering notes:**

- Port ForgeConfigSpec->ModConfigSpec in ConfigHandler.java and VanishConfig.java FIRST: ConfigHandler.spec and VanishConfig.SERVER_SPEC must already be ModConfigSpec before the registration helper (loader.MLConfig) and the direct registerConfig call (ServerEssentialsForge.java:76) can compile against the new signatures.
- The registration refactor (loader.MLConfig + ServerEssentialsForge.java:76) DEPENDS ON the @Mod-constructor/entrypoint dimension delivering a ModContainer reference, since registerConfig moved from ModLoadingContext to ModContainer. Coordinate with the entrypoint dimension.
- loader.java is a cross-cutting utility (also holds MlContext/DisplayTest and EVENT_BUS.register) — its IConfigSpec param and registerConfig call should be ported alongside the entrypoint changes, not in isolation.
- ConfigurationEventHandler (ModConfigEvent + @EventBusSubscriber) can be ported independently/early — it only depends on the bus-annotation swap, not on the spec type.
- The getServerDirectory() File->Path fix (6 sites) is independent of the config-spec work and can be done anytime; it has no ordering dependency but should be batched with the general vanilla-signature sweep.
- Gson/java.nio persistence needs no code changes (only the getServerDirectory edit) — verify build dependencies (gson/nightconfig presence) once the build.gradle is set up for NeoForge.


**Open questions (this dimension):**

- Does ModConfigSpec in NeoForge 21.1.x still expose a public setConfig(CommentedConfig)/acceptConfig usable the way ConfigHandler.reloadFromDisk() pokes it (ConfigHandler.java:25)? NeoForge reworked the IConfigSpec contract; manual setConfig may no longer be the supported reload path.
- Is com.google.gson on the NeoForge 1.21.1 runtime classpath without an explicit dependency? (Used by 7 managers.) If relying on a transitive lib, the build.gradle may need an explicit gson dep.
- Is com.electronwill.nightconfig (CommentedFileConfig + WritingMode) still transitively provided by NeoForge 21.1.x so the import in ConfigHandler.java:3-4 resolves? (Highly likely yes, but verify the artifact coordinates.)
- Does NeoForge's net.neoforged.fml.config.IConfigSpec still allow a 3-arg registerConfig(type, spec, customFileName) with a subdirectory filename ('sef/common.toml')? Need to confirm the custom-filename overload + subdir path are honored.
- Confirm MinecraftServer.getServerDirectory() return type is Path (not File) in this exact NeoForge/MC 1.21.1 build (treated as ground truth for the 1.20.5+ change, but worth a one-line verify against the MC source).


**Verification verdicts:**

- **CONFIRMED** — ModLoadingContext.get().registerConfig(ModConfig.Type, IConfigSpec, fileName) — config registration moved off ModLoadingContext; helper IConfigSpec<?> parameter retyping
    - claim: Registration must move to ModContainer.registerConfig(ModConfig.Type, IConfigSpec[, fileName]); @Mod constructor receives (IEventBus, ModContainer); NeoForge net.neoforged.fml.config.IConfigSpec is NOT generic; ModConfigSpec implements it; retype the helper param as ModConfigSpec or raw IConfigSpec and call modContainer.registerConfig(ModConfig.Type.valueOf(cType), spec, "sef/common.toml"); ModConfig.Type -> net.neoforged.fml.config.ModConfig
    - verified → CONFIRMED with one caveat. Verified against FancyModLoader 1.21.1 branch: net.neoforged.fml.ModContainer declares both overloads `public void registerConfig(ModConfig.Type type, IConfigSpec configSpec)` (line 102) and `public void registerConfig(ModConfig.Type type, IConfigSpec configSpec, String fileName)` (line 119). net.neoforged.fml.config.IConfigSpec is declared `public interface IConfigSpec {` — NON-generic (no type parameter), so retyping the helper param to raw IConfigSpec (or to ModConfigSpec, which `implements IConfigSpec`) is correct. ModConfig is net.neoforged.fml.config.ModConfig. The @Mod constructor is injected with IEventBus/ModContainer/FMLModContainer (FMLModContainer.java lines 97-99), so passing the injected ModContainer is correct. CAVEAT: `ModConfig.Type.valueOf(cType)` is only valid if cType is a String; ModConfig.Type is a plain Java enum (COMMON/CLIENT/SERVER/STARTUP) so Type.valueOf(String) is implicitly available, but if cType is already a Forge ModConfig.Type value it must be migrated/mapped rather than passed through valueOf. The retyping and registration target are correct.
    - evidence: neoforged/FancyModLoader branch 1.21.1: loader/src/main/java/net/neoforged/fml/ModContainer.java (registerConfig overloads, lines 102 & 119, param type IConfigSpec); loader/src/main/java/net/neoforged/fml/config/IConfigSpec.java line 23 `public interface IConfigSpec {` (non-generic); loader/src/main/java/net/neoforged/fml/config/ModConfig.java enum Type {COMMON,CLIENT,SERVER,STARTUP}; loader/src/main/java/net/neoforged/fml/javafmlmod/FMLModContainer.java lines 97-99 (constructor injection allows IEventBus/ModContainer/FMLModContainer); neoforged/NeoForge branch 1.21.1 src/main/java/net/neoforged/neoforge/common/ModConfigSpec.java line 56 `public class ModConfigSpec implements IConfigSpec`; https://docs.neoforged.net/docs/1.21.1/misc/config/
- **CONFIRMED** — ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, VanishConfig.SERVER_SPEC, "sef-vanish-server.toml") in the mod constructor
    - claim: modContainer.registerConfig(ModConfig.Type.SERVER, VanishConfig.SERVER_SPEC, "sef-vanish-server.toml") using the ModContainer injected into the @Mod constructor; ModConfig.Type.SERVER still exists; custom filename overload retained
    - verified → same
    - evidence: neoforged/FancyModLoader branch 1.21.1: loader/src/main/java/net/neoforged/fml/ModContainer.java line 119 `public void registerConfig(ModConfig.Type type, IConfigSpec configSpec, String fileName)` (3-arg filename overload retained); loader/src/main/java/net/neoforged/fml/config/ModConfig.java enum Type includes SERVER (line 109). SERVER_SPEC must be a ModConfigSpec (which implements net.neoforged.fml.config.IConfigSpec). Constructor-injected ModContainer confirmed by FMLModContainer.java lines 97-99 and https://docs.neoforged.net/docs/1.21.1/misc/config/ (example `public ExampleMod(ModContainer container)`, `container.registerConfig(...)`).
- **CORRECTED** — ConfigHandler.reloadFromDisk() hand-rolls reload: FMLPaths.CONFIGDIR + CommentedFileConfig.builder(...).sync().autosave().writingMode(REPLACE).build(); cfg.load(); spec.setConfig(cfg)
    - claim: FMLPaths.CONFIGDIR -> net.neoforged.fml.loading.FMLPaths.CONFIGDIR with same .get(); nightconfig imports survive unchanged; ModConfigSpec exposes setConfig(UnmodifiableCommentedConfig)/acceptConfig like ForgeConfigSpec did; VERIFY spec.setConfig(CommentedFileConfig) still has that exact signature
    - verified → PARTIALLY WRONG — the spec.setConfig(...) call CANNOT be ported as-is. (a) net.neoforged.fml.loading.FMLPaths.CONFIGDIR is correct: FMLPaths is an enum with CONFIGDIR("config") at line 23 and `public Path get()` at line 89 — CONFIRMED. (b) nightconfig is still bundled: NeoForge 1.21.1 ModConfigSpec imports com.electronwill.nightconfig.core.* (CommentedConfig, UnmodifiableCommentedConfig, etc.), so CommentedFileConfig/WritingMode imports survive — CONFIRMED. (c) FALSE: NeoForge 1.21.1 ModConfigSpec has NO setConfig method at all (verified by reading the full class — no `setConfig` anywhere). The Forge `void setConfig(CommentedConfig)` was REMOVED. The only acceptor is `@Override public void acceptConfig(@Nullable ILoadedConfig config)` (ModConfigSpec line 113), defined by IConfigSpec.acceptConfig(@Nullable ILoadedConfig). Its parameter ILoadedConfig is a SEALED interface (`sealed interface ILoadedConfig permits LoadedConfig`) in net.neoforged.fml.config.IConfigSpec, so a mod CANNOT construct an ILoadedConfig from its own CommentedFileConfig — you cannot feed a hand-loaded nightconfig into acceptConfig. Therefore the hand-rolled `spec.setConfig(cfg)` reload pattern cannot be replicated. CORRECT FIX: remove the manual re-read and instead (i) let FML own config loading and react via the net.neoforged.neoforge.common.NeoForge bus event ModConfigEvent.Reloading / ModConfigEvent.Loading (the docs-recommended approach), or (ii) read current values directly from the ModConfigSpec.ConfigValue getters (they auto-reflect FML's loaded config). Do NOT call setConfig/acceptConfig manually.
    - evidence: neoforged/NeoForge branch 1.21.1 src/main/java/net/neoforged/neoforge/common/ModConfigSpec.java: line 56 `implements IConfigSpec`, line 113 `public void acceptConfig(@Nullable ILoadedConfig config)`, no `setConfig` present (grep over full file finds none), imports com.electronwill.nightconfig.core.* at lines 8-15; neoforged/FancyModLoader branch 1.21.1 loader/src/main/java/net/neoforged/fml/config/IConfigSpec.java line 62 `void acceptConfig(@Nullable ILoadedConfig config)` and line 64 `sealed interface ILoadedConfig permits LoadedConfig { CommentedConfig config(); void save(); }`; loader/src/main/java/net/neoforged/fml/loading/FMLPaths.java line 20 `public enum FMLPaths`, line 23 `CONFIGDIR("config")`, line 89 `public Path get()`; https://docs.neoforged.net/docs/1.21.1/misc/config/ (recommends ModConfigEvent.Loading/Reloading for reacting to config (re)loads)


### LuckPerms integration (optional/guarded) (`luckperms`)


**Summary:** The actual net.luckperms.api.* surface (LuckPerms, LuckPermsProvider.get(), UserManager.getUser(UUID), User.getCachedData().getMetaData(), CachedMetaData.getPrefix/getSuffix/getPrefixes/getSuffixes) is the SAME stable 5.4 API for NeoForge 1.21.1 — the `compileOnly 'net.luckperms:api:5.4'` artifact (build.gradle:168) does NOT change, so the LuckPerms-specific Java in LuckPermsProvider.java and TabAnimationManager.java compiles and runs unchanged. The breakage in these files is entirely the surrounding Forge plumbing: the ModList import (net.minecraftforge.fml.ModList -> net.neoforged.fml.ModList), the @EventBusSubscriber/@SubscribeEvent/EventPriority/ServerStartedEvent/TickEvent imports in the two consumer event handlers, and the mods.toml -> neoforge.mods.toml optional-dependency block. The optional dependency IS correctly guarded (ModList.isLoaded("luckperms") + try/catch around LuckPermsProvider.get()), and the LuckPerms types are isolated into LuckPermsProvider/TabAnimationManager so the mod loads cleanly when LuckPerms is absent (no eager class-init on a missing type) — that isolation discipline must be preserved through the port. Overall risk for the LuckPerms-specific code is LOW; the surrounding event/bus migration is MEDIUM but owned by other dimensions.


**Findings (5):**


- **[LOW] net.luckperms.api.LuckPerms / net.luckperms.api.LuckPermsProvider.get() / UserManager.getUser(UUID) / User.getCachedData().getMetaData() / CachedMetaData.getPrefix()/getSuffix()/getPrefixes()/getSuffixes()**
  - _file:line:_ src/main/java/com/enviouse/sef/utils/moddeps/LuckPermsProvider.java:15-17,25,31-39,51-74; src/main/java/com/enviouse/sef/events/PlayerEventHandler.java:106-114; src/main/java/com/enviouse/sef/tab/TabAnimationManager.java:13-14,22
  - _→ NeoForge 1.21.1:_ NO CHANGE. The LuckPerms API artifact 'net.luckperms:api:5.4' is identical for the 1.21.1 NeoForge runtime jar (LuckPerms-NeoForge-5.4.139/.140). Keep `compileOnly 'net.luckperms:api:5.4'` in the NeoForge build.gradle. All net.luckperms.api.* class/method references stay byte-for-byte the same; no import or signature edits needed in this code.
  - _notes:_ LuckPermsProvider.get() (the LuckPerms static accessor, line 25/109) throws IllegalStateException if the API isn't ready — already handled (try/catch at PlayerEventHandler:108-112; documented at LuckPermsProvider:80 catching IllegalStateException). getPrefixes()/getSuffixes() returning Map<Integer,String> (weighted) and getPrefix()/getSuffix() single-value are standard CachedMetaData 5.4 methods; getCachedData()/getMetaData() and getUserManager().getUser(UUID) are standard. I'm confident these exist with these signatures in 5.4 — but I could not introspect the actual 5.4 jar here, so flagging the Map<Integer,String> weighted-prefix accessors as the one thing to smoke-test (not uncertain enough to block).


- **[LOW] net.minecraftforge.fml.ModList (the guard that isolates LuckPerms)**
  - _file:line:_ src/main/java/com/enviouse/sef/events/ExternalModLoadingEvent.java:12,33,34; src/main/java/com/enviouse/sef/events/PlayerEventHandler.java:28,107
  - _→ NeoForge 1.21.1:_ import net.neoforged.fml.ModList; ModList.get().isLoaded("luckperms") API is unchanged. This is the load-time guard for the whole optional integration — it must be ported correctly or LuckPerms presence detection breaks.
  - _notes:_ Pure import swap, same API (per ground truth: net.neoforged.fml.ModList, same isLoaded). modid stays "luckperms". The guard pattern (isLoaded -> instantiate LuckPermsProvider inside try/catch, ExternalModLoadingEvent:34-46) correctly defers all hard net.luckperms.* class loading into LuckPermsProvider's constructor, so when LuckPerms is absent the JVM never resolves those classes from the calling event handler — good isolation, keep it.


- **[LOW] Class-isolation of LuckPerms types so the mod loads without LuckPerms (no eager class-init)**
  - _file:line:_ src/main/java/com/enviouse/sef/utils/moddeps/LuckPermsProvider.java:15-19; src/main/java/com/enviouse/sef/tab/TabAnimationManager.java:14,22; src/main/java/com/enviouse/sef/ServerEssentialsForge.java:51; src/main/java/com/enviouse/sef/utils/SEFUtilities.java:29-30
  - _→ NeoForge 1.21.1:_ No code change required, but PRESERVE the structure. LuckPermsProvider implements IMetadataProvider (no net.luckperms.* in the interface — IMetadataProvider.java/INicknameProvider.java only use com.mojang.authlib.GameProfile, which is vanilla and unchanged). SEFUtilities (line 29-30) and ServerEssentialsForge.metadataProvider (line 51, typed IMetadataProvider) reach LuckPerms ONLY through the interface, so no luckperms class is referenced from core code. TabAnimationManager has a direct @Nullable net.luckperms.api.LuckPerms field (line 14) but is only instantiated/loaded after the isLoaded guard, and the param is nullable.
  - _notes:_ PlayerEventHandler:34 constructs `new TabAnimationManager()` as a static field initializer at handler class-init — but TabAnimationManager's net.luckperms.api.LuckPerms field is only a field type, not touched until load(...) is called with a possibly-null value, so no eager LuckPerms class resolution occurs at SEF class-init. The only place LuckPerms classes are force-resolved is inside LuckPermsProvider ctor and inside PlayerEventHandler.onServerStarted (both behind isLoaded). ExternalModLoadingEvent:43 catches Exception (not Error) around `new LuckPermsProvider()` — note a NoClassDefFoundError would be an Error, not caught here; but because the isLoaded guard precedes it, the missing-class path is never reached. Verify NeoForge still classloads optional compileOnly deps lazily the same way (it does).


- **[MEDIUM] mods.toml optional dependency [[dependencies.sef]] modId="luckperms" mandatory=false**
  - _file:line:_ src/main/resources/META-INF/mods.toml:62-67
  - _→ NeoForge 1.21.1:_ Move into src/main/templates/META-INF/neoforge.mods.toml as [[dependencies.<modid>]] with modId="luckperms", type="optional" (NeoForge replaced mandatory=true/false with type="required"/"optional"), versionRange="[5.4,)" (use proper Maven bracket range), ordering="BEFORE", side="BOTH". The mod's own [[mods]] block must add displayTest="IGNORE_ALL_VERSIONS" for server-only installability.
  - _notes:_ The skeleton already has src/main/templates/META-INF/neoforge.mods.toml (per git status). NeoForge syntax change: 'mandatory=false' becomes 'type="optional"'. versionRange in the old file is the malformed "5.4," (mods.toml:65) — fix to "[5.4,)". Note the [[dependencies.sef]] key must be renamed to the NeoForge modid namespace if it differs. This is a config/manifest task that overlaps the metadata/manifest dimension; flagged here because it is the declarative half of the LuckPerms optional-dep guard.


- **[MEDIUM] Forge event/bus plumbing in the two LuckPerms consumer event handlers (TickEvent, @EventBusSubscriber, @SubscribeEvent, EventPriority, ServerStartedEvent, PlayerEvent.TabListNameFormat/NameFormat)**  _(uncertain — needs verification)_
  - _file:line:_ src/main/java/com/enviouse/sef/events/PlayerEventHandler.java:14-23,30,42-43,55-56,118-123; src/main/java/com/enviouse/sef/events/ExternalModLoadingEvent.java:10-13,15,17
  - _→ NeoForge 1.21.1:_ These break but are owned by the events/bus and tick dimensions. For the LuckPerms path specifically: ServerStartedEvent -> net.neoforged.neoforge.event.server.ServerStartedEvent (LuckPerms is loaded in onServerStarted at ExternalModLoadingEvent:17 and PlayerEventHandler:102). TickEvent.ServerTickEvent + phase==END (PlayerEventHandler:119-120) -> net.neoforged.neoforge.event.tick.ServerTickEvent.Post (no phase). @EventBusSubscriber -> net.neoforged.fml.common.EventBusSubscriber (top-level, Bus.GAME default). @SubscribeEvent/EventPriority -> net.neoforged.bus.api.*.
  - _notes:_ Listed for completeness because porting the LuckPerms wiring requires these handlers to compile. The LuckPerms-specific lines (PlayerEventHandler:106-114, ExternalModLoadingEvent:32-51) themselves need NO change beyond the ModList import. TabListNameFormat/NameFormat at PlayerEventHandler:43,56 are HIGH-risk surfaces owned by the events dimension (they feed prefix/suffix display) — see that dimension; not re-scoring here to avoid double-counting.


**Ordering notes:**

- Depends on the config dimension (ConfigHandler.config.enableLuckPerms at ConfigHandler.java:43,237 and maxPrefixesDisplayed/maxSuffixesDisplayed used at LuckPermsProvider.java:54-55) being ported to ModConfigSpec first — LuckPermsProvider reads those ConfigValues at runtime.
- Depends on the events/bus dimension porting @EventBusSubscriber/@SubscribeEvent/ServerStartedEvent/TickEvent in PlayerEventHandler.java and ExternalModLoadingEvent.java, since the LuckPerms load is triggered from ServerStartedEvent handlers.
- Depends on the manifest dimension creating neoforge.mods.toml with the luckperms type="optional" dependency and the mod's displayTest="IGNORE_ALL_VERSIONS".
- Independent of: PermissionAPI/permissions, Curios, FTB Essentials, menu/registry dimensions — no shared LuckPerms code. (FTBNicknameProvider at ExternalModLoadingEvent:7,62 is the FTB dimension's concern, not LuckPerms.)
- The net.luckperms.api.* code itself (LuckPermsProvider.java body, TabAnimationManager.java LuckPerms field) needs no porting work and can be left untouched once imports/build deps are intact — do it last/verify-only.


**Open questions (this dimension):**

- Confirm the actual LuckPerms-NeoForge-5.4.139/.140 runtime jar's modid string remains exactly "luckperms" on 1.21.1 (ModList.isLoaded uses this literal at ExternalModLoadingEvent:34 and PlayerEventHandler:107). Highly likely yes.
- Confirm CachedMetaData.getPrefixes()/getSuffixes() still return Map<Integer,String> (weighted) in api:5.4 as used at LuckPermsProvider.java:51-52 — standard but not introspected from the jar in this audit.
- Should runtimeOnly for the LuckPerms NeoForge jar (currently commented out, build.gradle:169) be wired for dev-runtime testing? Optional; not required for the optional-guard to work since it's compileOnly.
- ExternalModLoadingEvent:43 catches Exception, not Error — confirm NeoForge never throws a bare NoClassDefFoundError on the guarded path (it won't, because isLoaded gates it), so no need to widen the catch.


**Verification verdicts:**

- **CONFIRMED** — ServerStartedEvent import for LuckPerms load path (ExternalModLoadingEvent.java:10,17 and PlayerEventHandler.java:21,102)
    - claim: net.minecraftforge.event.server.ServerStartedEvent -> net.neoforged.neoforge.event.server.ServerStartedEvent; LuckPerms is loaded in onServerStarted handlers
    - verified → net.neoforged.neoforge.event.server.ServerStartedEvent
    - evidence: NeoForge javadoc net/neoforged/neoforge/event/server/ServerStartedEvent.html (FQN net.neoforged.neoforge.event.server.ServerStartedEvent, extends ServerLifecycleEvent which provides getServer():MinecraftServer; ctor ServerStartedEvent(MinecraftServer)). Source confirms onServerStarted at ExternalModLoadingEvent.java:17 and PlayerEventHandler.java:102.
- **CONFIRMED** — TickEvent.ServerTickEvent with phase==END guard (PlayerEventHandler.java:118-120)
    - claim: net.minecraftforge.event.TickEvent.ServerTickEvent + 'if(e.phase != TickEvent.Phase.END) return;' -> net.neoforged.neoforge.event.tick.ServerTickEvent.Post (no phase field, no guard needed)
    - verified → net.neoforged.neoforge.event.tick.ServerTickEvent.Post (subscribe directly; e.getServer() still available; drop the phase!=END guard since Post == post-tick == old END phase)
    - evidence: NeoForge GitHub 1.21.1 branch src/main/java/net/neoforged/neoforge/event/tick/ServerTickEvent.java: package net.neoforged.neoforge.event.tick; abstract base has getServer():MinecraftServer; no phase field/Phase enum; split into static class Pre (fired before server work) and static class Post (fired after server work) — Post is the END-phase equivalent. Javadoc ServerTickEvent.Post.html confirms hierarchy Event -> ServerTickEvent -> ServerTickEvent.Post and inherited getServer().
- **CONFIRMED** — @EventBusSubscriber on ExternalModLoadingEvent.java:13,15 and PlayerEventHandler.java:20,30
    - claim: net.minecraftforge.fml.common.Mod.EventBusSubscriber -> net.neoforged.fml.common.EventBusSubscriber (top-level annotation, Bus.GAME default)
    - verified → net.neoforged.fml.common.EventBusSubscriber
    - evidence: FancyModLoader GitHub branch 1.21.1, loader/src/main/java/net/neoforged/fml/common/EventBusSubscriber.java line 6 'package net.neoforged.fml.common;' line 71 'Bus bus() default Bus.GAME;' with nested 'enum Bus { GAME, MOD }'. Top-level annotation (no longer nested under Mod). NeoForged docs 1.21.1 concepts/events confirm @EventBusSubscriber defaults to Bus.GAME.
- **CONFIRMED** — @SubscribeEvent / EventPriority on the LuckPerms handlers (PlayerEventHandler.java:18-19,42,55,61,101,118; ExternalModLoadingEvent.java:11,17)
    - claim: net.minecraftforge.eventbus.api.SubscribeEvent -> net.neoforged.bus.api.SubscribeEvent and net.minecraftforge.eventbus.api.EventPriority -> net.neoforged.bus.api.EventPriority
    - verified → net.neoforged.bus.api.SubscribeEvent and net.neoforged.bus.api.EventPriority
    - evidence: neoforged/Bus repo: src/main/java/net/neoforged/bus/api/SubscribeEvent.java and src/main/java/net/neoforged/bus/api/EventPriority.java (EventPriority.java line 17 'package net.neoforged.bus.api;' enum with HIGHEST/NORMAL/LOWEST). EventPriority.LOWEST (PlayerEventHandler.java:118) remains a valid @SubscribeEvent priority param.


### FTB Essentials, Curios & Discord-bridge compat integrations (`ftb-curios-discord`)


**Summary:** Two hard, source-verified breaks dominate this dimension. (1) FTB nickname: FTBNicknameProvider calls FTBEPlayerData.getOrCreate(GameProfile) — that GameProfile overload was REMOVED in FTB Essentials 1.21.1 (2101.1.x); only getOrCreate(Player) and getOrCreate(MinecraftServer,UUID) remain. The package and getNick()/isMuted() accessors DO still exist, so FTBMuteChecker (passes ServerPlayer, a Player) compiles fine, but FTBNicknameProvider will NOT compile and must be rewritten. (2) Curios import: the only Forge API surface in CuriosInventoryHelper is net.minecraftforge.items.IItemHandlerModifiable, which becomes net.neoforged.neoforge.items.IItemHandlerModifiable — the Curios API itself (CuriosApi.getCuriosInventory, getCurios(), getStacks()) is unchanged on the 1.21.1 NeoForge build and getStacks() still returns IDynamicStackHandler extends IItemHandlerModifiable. The Discord bridges (mc2discord, sdlink, playtime) are fully reflective, guarded by ModList.isLoaded + try/catch(Throwable), and touch no Forge API except one net.minecraftforge.fml.ModList import in PlaytimeCompat — so they will not crash load; only that one import swap is mechanical. The gradle compileOnly coordinates for FTB (2001.2.2) and Curios (5.14.1+1.20.1) must be re-pointed to NeoForge 1.21.1 artifacts. All integrations are correctly absence-guarded.


**Findings (9):**


- **[HIGH] FTBEPlayerData.getOrCreate(GameProfile) — the GameProfile overload (present in FTB Essentials 1.20.1) was REMOVED in 2101.1.x. Verified on FTBTeam/FTB-Essentials branch 1.21.1/main: only getOrCreate(Player) and getOrCreate(MinecraftServer, UUID) exist; 1.20.1/main had getOrCreate(@Nullable GameProfile) at line 160. FTBNicknameProvider passes a GameProfile so it will NOT compile.**
  - _file:line:_ src/main/java/com/enviouse/sef/utils/moddeps/FTBNicknameProvider.java:11-14
  - _→ NeoForge 1.21.1:_ Rewrite to use the surviving API. Options: (a) keep INicknameProvider.getPlayerNickname(GameProfile) but resolve a ServerPlayer/MinecraftServer inside: FTBEPlayerData.getOrCreate(ServerLifecycleHooks.getCurrentServer(), player.getId()).map(FTBEPlayerData::getNick); or (b) change the provider to accept a ServerPlayer and call getOrCreate(player). getNick()/isMuted() still exist (verified package dev.ftb.mods.ftbessentials.util on 1.21.1/main). Note getOrCreate(MinecraftServer,UUID) depends on server.getProfileCache() being populated, so it can return empty for offline players. Also swap ExternalModLoadingEvent imports (ServerStartedEvent, @SubscribeEvent, @EventBusSubscriber) per the global event-bus migration.
  - _notes:_ Confirmed against live FTB source. The mute path (FTBMuteChecker.java:34-39) is SAFE: it calls getOrCreate(ServerPlayer) which binds to getOrCreate(Player), and isMuted() exists — only the net.minecraftforge.fml.ModList import (line 5) needs swapping to net.neoforged.fml.ModList. The nickname path is the break.


- **[LOW] net.minecraftforge.fml.ModList import + ModList.get().isLoaded("ftbessentials")/"curios"/"playtime"**
  - _file:line:_ src/main/java/com/enviouse/sef/utils/moddeps/FTBMuteChecker.java:5, src/main/java/com/enviouse/sef/utils/moddeps/CuriosInventoryHelper.java:6, src/main/java/com/enviouse/sef/vanish/compat/PlaytimeCompat.java:5
  - _→ NeoForge 1.21.1:_ net.neoforged.fml.ModList (same API: ModList.get().isLoaded(String)). Mechanical import swap; modids "ftbessentials", "curios", "playtime", "mc2discord", "sdlink" are unchanged on 1.21.1.
  - _notes:_ Also appears in ServerEssentialsForge.java:163-168 (mc2discord/playtime/sdlink detection) and ExternalModLoadingEvent.java — those are outside this file set but share the same swap. Detection guards are correct.


- **[LOW] net.minecraftforge.items.IItemHandlerModifiable used as the type for Curios stack handlers (return of ICurioStacksHandler.getStacks()).**
  - _file:line:_ src/main/java/com/enviouse/sef/utils/moddeps/CuriosInventoryHelper.java:7,56,97,133
  - _→ NeoForge 1.21.1:_ net.neoforged.neoforge.items.IItemHandlerModifiable. Verified on Curios 1.21.1-era NeoForge source: ICurioStacksHandler.getStacks() returns IDynamicStackHandler which 'extends net.neoforged.neoforge.items.IItemHandlerModifiable', so the existing assignment + getSlots()/getStackInSlot()/setStackInSlot() usage stays valid after the import swap.
  - _notes:_ Pure import swap; no method-shape change. The CuriosSlotGroup record (line 27) also references IItemHandlerModifiable and resolves automatically once the import is fixed.


- **[LOW] Curios API: top.theillusivec4.curios.api.CuriosApi.getCuriosInventory(LivingEntity), ICuriosItemHandler.getCurios() -> Map<String,ICurioStacksHandler>, ICurioStacksHandler.getStacks().**
  - _file:line:_ src/main/java/com/enviouse/sef/utils/moddeps/CuriosInventoryHelper.java:52-58,93-105,130-142
  - _→ NeoForge 1.21.1:_ No code change needed beyond the IItemHandlerModifiable import. Verified on TheIllusiveC4/Curios (1.21.1-era NeoForge tree): CuriosApi.getCuriosInventory(LivingEntity) -> Optional<ICuriosItemHandler> exists; getCurios() -> Map<String,ICurioStacksHandler> unchanged; getStacks()/getCosmeticStacks() unchanged. ServerPlayer is a LivingEntity so the call sites are valid. Package top.theillusivec4.curios.api.* is stable.
  - _notes:_ Curios 1.21.1 NeoForge build = 9.x line (e.g. curios-neoforge-9.0.x/9.2.x +1.21.1). API verified to match the call sites. The wider 1.21.x branch HEAD has further changes (e.g. net.minecraft.resources.Identifier, ValueInput/ValueOutput) but those are 1.21.5+ and do NOT apply to the 1.21.1 target.


- **[MEDIUM] Gradle compileOnly coordinates: ftb-essentials:ftb-essentials-forge:2001.2.2 and top.theillusivec4.curios:curios-forge:5.14.1+1.20.1 (and ForgeGradle fg.deobf()).**  _(uncertain — needs verification)_
  - _file:line:_ build.gradle:167,170,138
  - _→ NeoForge 1.21.1:_ Repoint to NeoForge 1.21.1 artifacts: FTB Essentials 2101.1.x (e.g. via FTB maven, modid ftbessentials; FTB Library also required as a transitive compile/runtime dep), and Curios NeoForge 9.x (top.theillusivec4.curios:curios-neoforge:<9.x>+1.21.1 from maven.theillusivec4.top). Remove fg.deobf() (ForgeGradle gone) — NeoGradle/ModDevGradle consumes Mojang-mapped artifacts directly, so plain compileOnly. The Curios maven repo (build.gradle:138) stays valid.
  - _notes:_ Build-system change, not source. Without correct compileOnly artifacts the two FTB/Curios source files cannot compile. Exact FTB Essentials 2101.1.x maven coordinate/group and FTB Library version pin should be confirmed against the FTB maven at port time.


- **[LOW] mc2discord soft-compat (reflection only): fr.denisd3d.mc2discord.core.Mc2Discord, .storage.HiddenPlayerEntry, .M2DUtils, .MessageManager, .entities.PlayerEntity/.Entity — all via Class.forName + reflective field/method access.**  _(uncertain — needs verification)_
  - _file:line:_ src/main/java/com/enviouse/sef/vanish/compat/Mc2DiscordCompat.java:33-58,89-122
  - _→ NeoForge 1.21.1:_ No compile-time change required: file imports no Forge API (only net.minecraft.server.level.ServerPlayer + log4j). All access is reflective and wrapped in try/catch(Exception) with init() short-circuit, so a missing/renamed mc2discord class only logs at debug and no-ops. ServerPlayer.getGameProfile()/getDisplayName().getString() are vanilla and unchanged 1.20.1->1.21.1. Detected via ServerEssentialsForge.mc2discordDetected (set from ModList).
  - _notes:_ Absence-safe by construction. Runtime behaviour against the actual mc2discord 1.21.1 build is NOT verified (reflective field names like hiddenPlayerList/config/messages may have changed across mc2discord versions) — but this is a runtime-feature concern, not a load/compile risk, and failures degrade gracefully. Flagging the field-name stability as uncertain.


- **[LOW] SDLink soft-compat (reflection only): com.hypherionmc.sdlink.core.managers.HiddenPlayersManager, .api.messaging.* (MessageType, DiscordMessageBuilder, DiscordMessage), .api.accounts.DiscordAuthor, .core.config.* , .core.discord.BotController — all reflective.**  _(uncertain — needs verification)_
  - _file:line:_ src/main/java/com/enviouse/sef/vanish/compat/SDLinkCompat.java:49-103,150-189
  - _→ NeoForge 1.21.1:_ No compile-time change required: imports only java.lang.reflect.*, net.minecraft.server.level.ServerPlayer, log4j. Every reflective block is independently try/catch(Exception)-guarded with boolean availability flags (messageApiAvailable, null checks) and init() guard, so absence/renames no-op safely. Only vanilla ServerPlayer accessors used (getStringUUID/getName/getGameProfile), all stable.
  - _notes:_ Absence-safe. Runtime compatibility with the SDLink 1.21.1 build (class/field/method names, e.g. HiddenPlayersManager.hidePlayer(String,String,String), DiscordAuthor.getServer/setPlayerAvatar) is NOT verified and may differ from the targeted SDLink version — degrades to debug/warn logs, no crash.


- **[LOW] SDLinkHideTracker — pure SEF logic over SDLinkCompat; references only ServerEssentialsForge.sdlinkDetected, ServerPlayer (getUUID/getStringUUID/getName/getGameProfile), and java.util collections.**
  - _file:line:_ src/main/java/com/enviouse/sef/vanish/compat/SDLinkHideTracker.java:45-46,98-117
  - _→ NeoForge 1.21.1:_ No change needed. No Forge/MC import surface that breaks; all ServerPlayer methods used are vanilla and unchanged 1.20.1->1.21.1. Guarded by ServerEssentialsForge.sdlinkDetected (set via ModList at ServerEssentialsForge.java:167-168).
  - _notes:_ Depends on ServerEssentialsForge.sdlinkDetected being correctly populated post-ModList-import-swap. No reflective or compile risk here.


- **[LOW] Playtime soft-compat (reflection): com.enviouse.playtime.Playtime.getSessionTracker(), com.enviouse.playtime.service.SessionTracker.pauseSession/resumeSession(UUID) — reflective; only Forge surface is the net.minecraftforge.fml.ModList import.**
  - _file:line:_ src/main/java/com/enviouse/sef/vanish/compat/PlaytimeCompat.java:5,47,53-67
  - _→ NeoForge 1.21.1:_ Swap net.minecraftforge.fml.ModList -> net.neoforged.fml.ModList (line 5). Everything else (state-machine probe, try/catch(Throwable), NoSuchMethodException handling) is loader-agnostic and absence-safe. ServerPlayer.getUUID()/getGameProfile() are vanilla.
  - _notes:_ Playtime is a sibling SEF-family mod; its 1.21.1 build is presumed to be ported in parallel. The compat is robust to API-too-old/absent. Only the ModList import is load-bearing for compile.


**Ordering notes:**

- Depends on the build-system migration (NeoGradle/ModDevGradle, removal of fg.deobf, new compileOnly artifacts for FTB 2101.1.x + Curios neoforge 9.x) being done FIRST — these two source files cannot compile until the dependency coordinates resolve.
- Depends on the global event-bus/import migration: ExternalModLoadingEvent.java (which instantiates FTBNicknameProvider) uses net.minecraftforge ServerStartedEvent/@SubscribeEvent/@EventBusSubscriber and must be ported alongside; and net.minecraftforge.fml.ModList -> net.neoforged.fml.ModList must be swapped project-wide (incl. ServerEssentialsForge.java detection at 163-168) before these compat files compile.
- Depends on the items-API migration (net.minecraftforge.items -> net.neoforged.neoforge.items) being decided centrally — CuriosInventoryHelper shares that swap with any other IItemHandler users (e.g. invsee/menu code).
- FTBNicknameProvider rewrite should be coordinated with whoever owns INicknameProvider / NickCommands / IntegratedNicknameProvider, since changing the provider's parameter type from GameProfile to ServerPlayer would ripple to the interface and other implementors.
- SDLinkHideTracker / VanishingHandler rely on ServerEssentialsForge.sdlinkDetected/mc2discordDetected/playtimeDetected — ensure the detection assignment in ServerEssentialsForge runs after ModList is queryable (it currently does, post ModList import swap).


**Open questions (this dimension):**

- FTB Essentials 2101.1.x maven coordinate/group id and the required FTB Library version pin for compileOnly — confirm against the FTB maven (build.gradle currently uses ad-hoc group 'ftb-essentials:ftb-essentials-forge'). Does the NeoForge build still expose dev.ftb.mods.ftbessentials.util.FTBEPlayerData on the *classpath* the same way for compileOnly (it lives in the multiloader 'common' module)?
- Should FTBNicknameProvider be re-typed to take a ServerPlayer instead of GameProfile (cleaner, matches getOrCreate(Player)), or keep GameProfile and resolve via getOrCreate(server, profile.getId())? The latter can return Optional.empty() for players not in the profile cache — verify INicknameProvider's contract tolerates a null nick (it does: IntegratedNicknameProvider/getPlayerChatName handle null).
- Are the reflective mc2discord/sdlink field & method names (hiddenPlayerList, HiddenPlayersManager.hidePlayer(String,String,String), DiscordAuthor.setPlayerAvatar, etc.) still valid in the specific 1.21.1 builds of those Discord bridges? Not load-blocking (graceful no-op), but the vanish->Discord feature silently breaks if they changed.
- Curios exact NeoForge artifact version for 1.21.1 to pin (9.0.x vs 9.2.x) — both expose the verified API; pick the one matching the target modpack.


**Verification verdicts:**

- **CONFIRMED** — FTBEPlayerData.getOrCreate(GameProfile) removed in 2101.1.x; rewrite FTBNicknameProvider to use getOrCreate(MinecraftServer, UUID) or getOrCreate(Player); getNick()/isMuted() survive; swap event-bus imports
    - claim: The GameProfile overload was removed in FTB Essentials 1.21.1; only getOrCreate(Player) and getOrCreate(MinecraftServer, UUID) exist. Rewrite FTBNicknameProvider via getOrCreate(ServerLifecycleHooks.getCurrentServer(), player.getId()).map(FTBEPlayerData::getNick) or change provider to accept ServerPlayer and call getOrCreate(player). getNick()/isMuted() still exist. Swap to net.neoforged event-bus annotations.
    - verified → CONFIRMED with notes. FTBEPlayerData (branch 1.21.1/main, package dev.ftb.mods.ftbessentials.util) exposes ONLY: Optional<FTBEPlayerData> getOrCreate(MinecraftServer server, UUID playerId) and Optional<FTBEPlayerData> getOrCreate(Player player) — no GameProfile overload. getNick() (line 167) and isMuted() (line 133) exist. FTBNicknameProvider.java line 12 still calls FTBEPlayerData.getOrCreate(player) on a GameProfile so it will NOT compile; either of the two proposed rewrites is valid. Both getOrCreate variants return Optional, so the existing .orElse(null)/.map() patterns hold. Event-bus migration is correct: @SubscribeEvent -> net.neoforged.bus.api.SubscribeEvent, @EventBusSubscriber -> net.neoforged.fml.common.EventBusSubscriber, ServerStartedEvent -> net.neoforged.neoforge.event.server.ServerStartedEvent. Caveat is accurate: getOrCreate(MinecraftServer,UUID) resolves the name via the server profile cache and can return empty for not-yet-cached/offline UUIDs, so option (b) [accept ServerPlayer, call getOrCreate(player)] is the more robust choice. Note FTBMuteChecker.java already correctly uses getOrCreate(player); only FTBNicknameProvider needs the change.
    - evidence: https://github.com/FTBTeam/FTB-Essentials/blob/1.21.1/main/common/src/main/java/dev/ftb/mods/ftbessentials/util/FTBEPlayerData.java (getOrCreate(MinecraftServer,UUID), getOrCreate(Player), getNick() L167, isMuted() L133); event-bus packages: https://docs.neoforged.net/docs/concepts/events/ and https://neoforged.net/news/20.2eventbus-changes/ ; ServerStartedEvent package net.neoforged.neoforge.event.server per https://nekoyue.github.io/ForgeJavaDocs-NG/javadoc/1.21.x-neoforge/net/neoforged/neoforge/event/server/ServerLifecycleEvent.html ; local source FTBNicknameProvider.java:12 and FTBMuteChecker.java:36
- **CORRECTED** — Gradle coords: replace ftb-essentials:ftb-essentials-forge:2001.2.2 and top.theillusivec4.curios:curios-forge:5.14.1+1.20.1 + fg.deobf() with NeoForge 1.21.1 artifacts
    - claim: Repoint to FTB Essentials 2101.1.x (FTB maven, modid ftbessentials, FTB Library required as transitive dep) and Curios NeoForge 9.x (top.theillusivec4.curios:curios-neoforge:<9.x>+1.21.1 from maven.theillusivec4.top). Remove fg.deobf() (ForgeGradle gone); plain compileOnly under NeoGradle/ModDevGradle. Curios maven repo at build.gradle:138 stays valid.
    - verified → CONFIRMED with corrected coordinates. FTB Essentials 1.21.1 publishes under maven group dev.ftb.mods, artifact ftb-essentials-neoforge, version 2101.1.x (latest 2101.1.9) at the FTB maven https://maven.ftb.dev/releases (NOT a bare 'ftb-essentials:ftb-essentials-forge' group as in the old build) — so: compileOnly "dev.ftb.mods:ftb-essentials-neoforge:2101.1.x". FTB Library IS a required dependency (build declares requires('ftb-library-forge'); NeoForge variant group dev.ftb.mods, e.g. ftb-library-neoforge ~2101.1.x). Curios NeoForge 9.x for 1.21.1 confirmed: top.theillusivec4.curios:curios-neoforge:9.5.1+1.21.1 (also 9.4.2, 9.2.2) from https://maven.theillusivec4.top/releases — the existing repo at build.gradle:138 stays valid. fg.deobf() must be removed: ForgeGradle is replaced by NeoGradle/ModDevGradle which consume Mojang-mapped artifacts directly, so plain compileOnly is correct. The modid 'ftbessentials' (used by ModList.isLoaded) is unchanged and correct. Recommend pinning exact versions (e.g. ftb-essentials-neoforge:2101.1.9 + matching ftb-library-neoforge, curios-neoforge:9.5.1+1.21.1) rather than open ranges.
    - evidence: FTB maven/group: https://github.com/FTBTeam/FTB-Essentials/blob/main/build.gradle and gradle.properties (group dev.ftb.mods, repos maven.ftb.dev/releases, requires('ftb-library-forge')); FTB Essentials NeoForge 1.21.1 files: https://www.curseforge.com/minecraft/mc-mods/ftb-essentials/files/7608733 (2101.1.9, ftb-essentials-neoforge-2101.1.9.jar); Curios: https://mvnrepository.com/artifact/top.theillusivec4.curios/curios-neoforge/9.4.2+1.21.1 and https://www.curseforge.com/minecraft/mc-mods/curios/files/6529130 (9.5.1+1.21.1); Curios maven/README repo: https://github.com/TheIllusiveC4/Curios/blob/1.21.1/README.md
- **CONFIRMED** — mc2discord soft-compat (reflection only) — fr.denisd3d.mc2discord.core.Mc2Discord / .storage.HiddenPlayerEntry / .M2DUtils / .MessageManager / .entities.PlayerEntity/.Entity via Class.forName
    - claim: No compile-time change required: file imports no Forge API (only net.minecraft.server.level.ServerPlayer + log4j); all mc2discord access is reflective and try/catch(Exception)-guarded with init() short-circuit, so missing/renamed classes only log at debug and no-op. ServerPlayer.getGameProfile()/getDisplayName().getString() are vanilla, unchanged 1.20.1->1.21.1.
    - verified → CONFIRMED. Verified Mc2DiscordCompat.java imports only java.lang.reflect.{Field,Method}, net.minecraft.server.level.ServerPlayer, and org.apache.logging.log4j.* — zero net.minecraftforge/net.neoforged references, so nothing in this file needs porting for the loader migration. Every mc2discord touchpoint (Mc2Discord.INSTANCE, hiddenPlayerList, HiddenPlayerEntry, M2DUtils.isNotConfigured, MessageManager.sendInfoMessage, entities.PlayerEntity/Entity.replace) is Class.forName + reflective access inside try/catch(Exception); init() sets initialized=true and the public methods null-check hiddenPlayerList/method handles before use, so absent or renamed mc2discord classes degrade to a debug log and no-op. The only vanilla calls are player.getGameProfile().getId()/getName() and player.getDisplayName().getString(); Player.getGameProfile() returns com.mojang.authlib.GameProfile and getDisplayName() returns Component in 1.21.1 (unchanged), and GameProfile.getId()/getName() + Component.getString() are stable. mc2discord ships a 1.21.1 NeoForge build under package fr.denisd3d.mc2discord, so the soft-compat remains meaningful at runtime. Note: this is runtime reflection against another mod's internals; the claim is only that NO compile-time change is needed, which is correct — exact field/method survival in mc2discord 1.21.1 (e.g. hiddenPlayerList field name) is out of scope and safely handled by the no-op guards.
    - evidence: Local source Mc2DiscordCompat.java:1-123 (imports L3-9; try/catch init L29-58; null-guarded public methods L60-122; vanilla calls L64,83,108); Player.getGameProfile()/getDisplayName() present in 1.21.1: https://nekoyue.github.io/ForgeJavaDocs-NG/javadoc/1.21.x-neoforge/net/minecraft/world/entity/player/Player.html ; mc2discord 1.21.1 + package fr.denisd3d.mc2discord: https://github.com/DenisD3D/Mc2Discord and https://modrinth.com/mod/mc2discord
- **CONFIRMED** — SDLink soft-compat (reflection only) — com.hypherionmc.sdlink.core.managers.HiddenPlayersManager / .api.messaging.* / .api.accounts.DiscordAuthor / .core.config.* / .core.discord.BotController
    - claim: No compile-time change required: imports only java.lang.reflect.*, net.minecraft.server.level.ServerPlayer, log4j. Every reflective block is independently try/catch(Exception)-guarded with availability flags (messageApiAvailable, null checks) and init() guard, so absence/renames no-op safely. Only vanilla ServerPlayer accessors used (getStringUUID/getName/getGameProfile), all stable.
    - verified → CONFIRMED. Verified SDLinkCompat.java imports only java.lang.reflect.{Constructor,Field,Method}, net.minecraft.server.level.ServerPlayer, org.apache.logging.log4j.* — no net.minecraftforge/net.neoforged references, so no porting needed in this file. init() runs four independent try/catch(Exception) blocks (HiddenPlayersManager; MessageType enum; DiscordMessageBuilder/DiscordMessage/DiscordAuthor; SDLinkConfig/SDLinkCompatConfig) and sets the messageApiAvailable flag only on full success; the public/helper methods (setHidden, sendDiscordJoinLeaveMessage, isBotReady, shouldSendFakeJoinLeave, isChatConfigFlag, getMessageFormat) all null-check handles and short-circuit on messageApiAvailable, so a missing/renamed SDLink class logs at debug/warn and no-ops. Vanilla accessors used are player.getStringUUID(), player.getName().getString(), player.getGameProfile().getName() — all stable in 1.21.1 (getGameProfile() returns com.mojang.authlib.GameProfile, getName()/getStringUUID() are Entity-level and unchanged). SDLink (hypherionmc/sdlink) targets 1.21.1 and uses package com.hypherionmc.sdlink, so soft-compat stays meaningful. As with mc2discord, exact survival of SDLink's internal field/method names in its 1.21.1 build is out of scope of the compile question and safely handled by the no-op guards; the 'no compile-time change required' claim is correct.
    - evidence: Local source SDLinkCompat.java:1-258 (imports L3-9; independent try/catch init blocks L49-102; messageApiAvailable flag L86,151; null-guarded methods L123-257; vanilla calls L111,169,179); Player.getGameProfile()/Entity accessors in 1.21.1: https://nekoyue.github.io/ForgeJavaDocs-NG/javadoc/1.21.x-neoforge/net/minecraft/world/entity/player/Player.html ; SDLink repo/package com.hypherionmc.sdlink + 1.21.1: https://github.com/hypherionmc/sdlink and https://github.com/hypherionmc/sdlink-lib


### Vanish mixins, Access Transformers & custom event (BIGGEST risk) (`vanish-mixins-at`)


**Summary:** This is the highest-risk dimension. The vanish system relies on (a) 3 Access Transformers written in SRG names that must be rewritten in Mojang names for NeoForge 1.21.1, (b) 28 mixins targeting deep vanilla internals (player-info packets, ChunkMap tracking, chat signing, mob-effect Holders) whose method/field descriptors changed materially between 1.20.1 and 1.21.1, and (c) a custom Forge PlayerEvent. The mixin config and AT format themselves are mechanical (refmap removal, JAVA_21, package rename, file rename), but several @At/@Inject targets — especially LivingEntityMixin's MobEffect-vs-Holder signatures, the ClientboundPlayerInfoUpdatePacket.entries mutation, the ServerPlayer chat-signing path, and the ServerStatus record (forgeData() does not exist in NeoForge) — are likely to break and need per-target re-validation against 1.21.1 Mojang mappings. Overall risk: CRITICAL — these are core, mod-defining features and several target shapes are unknown until validated against 1.21.1 source.


**Findings (16):**


- **[HIGH] AT entry #1: SRG field name 'f_140150_' for ChunkMap.entityMap (public net.minecraft.server.level.ChunkMap f_140150_)**  _(uncertain — needs verification)_
  - _file:line:_ src/main/resources/META-INF/accesstransformer.cfg:3
  - _→ NeoForge 1.21.1:_ Rewrite in Mojang name: 'public net.minecraft.server.level.ChunkMap entityMap'. NeoForge 1.21.1 ATs use Mojang names at runtime; SRG names no longer exist. moddev auto-detects META-INF/accesstransformer.cfg (no build.gradle entry needed). Field is consumed at VanishingHandler.java:80-81 (chunkProvider.chunkMap.entityMap.containsKey / .remove).
  - _notes:_ The Mojang name 'entityMap' is correct for 1.20.x ChunkMap, but the field's existence/visibility/type (Int2ObjectMap<ChunkMap.TrackedEntity>) must be re-verified in 1.21.1 — ChunkMap was refactored across 1.20.x->1.21.x. If the field was renamed or moved (e.g. into a tracking subsystem), both the AT and VanishingHandler.java:80-82 need updating.


- **[HIGH] AT entry #2: nested class access 'public net.minecraft.server.level.ChunkMap$TrackedEntity'**  _(uncertain — needs verification)_
  - _file:line:_ src/main/resources/META-INF/accesstransformer.cfg:4
  - _→ NeoForge 1.21.1:_ Same line works in Mojang-name AT format (it is already a class reference, not SRG): 'public net.minecraft.server.level.ChunkMap$TrackedEntity'. Used by world/ChunkMapTrackedEntityMixin (@Mixin(TrackedEntity.class)) which shadows 'entity' and injects into 'updatePlayer'.
  - _notes:_ Class reference is mapping-agnostic so the AT line itself is likely fine, but the existence of the inner class TrackedEntity and the 'entity' field + 'updatePlayer(ServerPlayer)' method (ChunkMapTrackedEntityMixin.java:16-27) must be confirmed in 1.21.1. ChunkMap.TrackedEntity still exists in 1.21 but the updatePlayer signature/timing should be re-validated.


- **[CRITICAL] AT entry #3: SRG field name 'f_244436_' for ClientboundPlayerInfoUpdatePacket.entries with un-final (public-f)**  _(uncertain — needs verification)_
  - _file:line:_ src/main/resources/META-INF/accesstransformer.cfg:5
  - _→ NeoForge 1.21.1:_ Rewrite as 'public-f net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket entries'. The 'public-f' (strip final) is needed because ServerGamePacketListenerImplMixin.java:65 directly REASSIGNS infoPacket.entries = filteredPacketEntries. Verify the field is still named 'entries' and is a List<Entry> in 1.21.1.
  - _notes:_ This packet was reworked in 1.20.2+. The mod both reads .entries() (ServerGamePacketListenerImplMixin.java:60) and writes the backing 'entries' field (line 65), and VanishingHandler.java uses createPlayerInitializing(...)/Entry.profileId(). If the field is now a different type/name, or the record made truly immutable in a way 'public-f' cannot fix, the in-place mutation must be rewritten to construct a new packet. This is the single most fragile AT.


- **[MEDIUM] Mixin config: refmap 'sef.refmap.json', compatibilityLevel JAVA_17, mixinextras minVersion, file name + package**
  - _file:line:_ src/main/resources/sef.mixins.json:4-9
  - _→ NeoForge 1.21.1:_ Rename file sef.mixins.json -> sefported.mixins.json and update neoforge.mods.toml [[mixins]] config reference. Set compatibilityLevel JAVA_21. REMOVE the 'refmap' line entirely (NeoForge/Mojang-mappings-at-runtime needs no SRG refmap). mixinextras is bundled in NeoForge (drop the explicit dependency declaration or keep minVersion for safety). Package 'com.enviouse.sef.vanish.mixin' must move with the source repackage (com.enviouse.sef -> new base pkg).
  - _notes:_ Mechanical but load-blocking if wrong. The refmap line being present under Mojang mappings can cause mixin remap failures. mixinextras WrapOperation/Operation imports (com.llamalad7.mixinextras.*) are used in 6 mixins (interaction/ChunkMapMixin, ServerLevelMixin, LivingEntityMixin, InteractionLivingEntityMixin, InteractionPlayerMixin, VanishEntitySelectorMixins) and remain valid since it ships with NeoForge.


- **[MEDIUM] PlayerVanishEvent extends net.minecraftforge.event.entity.player.PlayerEvent; posted via MinecraftForge.EVENT_BUS.post**  _(uncertain — needs verification)_
  - _file:line:_ src/main/java/com/enviouse/sef/vanish/api/PlayerVanishEvent.java:4-13, src/main/java/com/enviouse/sef/vanish/VanishingHandler.java:20,116
  - _→ NeoForge 1.21.1:_ Change import to net.neoforged.neoforge.event.entity.player.PlayerEvent (same protected PlayerEvent(Player) ctor in NeoForge 1.21.1). Posting site VanishingHandler.java:116 changes MinecraftForge.EVENT_BUS -> net.neoforged.neoforge.common.NeoForge.EVENT_BUS (import swap at line 20). Event is not @Cancelable (not used as cancelable here), so no cancellation API needed.
  - _notes:_ PlayerEvent base class exists in NeoForge with player accessor; confirm the constructor is still (Player) and that EVENT_BUS.post returns the event (NeoForge IEventBus.post signature is stable). Low logical complexity but it is a public API consumers may depend on; verify getEntity()/getPlayer() accessor name on the NeoForge PlayerEvent base.


- **[HIGH] Mixin group: chat/ (CombatTrackerMixin, CommandSourceStackMixin, EntityArgumentMixin, EntitySelectorMixin, ListPlayersCommandMixin, PlayerAdvancementsMixin) — @Redirect/@ModifyVariable on command selectors, death messages, advancement broadcast**  _(uncertain — needs verification)_
  - _file:line:_ src/main/java/com/enviouse/sef/vanish/mixin/chat/EntitySelectorMixin.java:20-34, chat/EntityArgumentMixin.java:24-37, chat/CombatTrackerMixin.java:27-39, chat/CommandSourceStackMixin.java:33, chat/PlayerAdvancementsMixin.java:22, chat/ListPlayersCommandMixin.java:19
  - _→ NeoForge 1.21.1:_ Mojang-name targets directly (no refmap). Re-validate each target method/descriptor against 1.21.1: EntitySelector.findSingleEntity/findSinglePlayer, EntityArgument.getEntities/getPlayers, CombatTracker.getDeathMessage + getFallMessage + DamageSource.getLocalizedDeathMessage, CommandSourceStack.getOnlinePlayerNames, PlayerAdvancements.award (broadcastSystemMessage), ListPlayersCommand.format.
  - _notes:_ Brigadier/command selector and CommandSourceStack APIs are largely stable 1.20.1->1.21.1, so these are mostly mechanical. HIGHER concern: CombatTracker.getDeathMessage/getFallMessage internals and DamageSource.getLocalizedDeathMessage (damage system was reworked in 1.20.5 with data-driven damage types) — the @Redirect INVOKE descriptors may no longer match. The @ModifyVariable injection points keyed on List.isEmpty()/size()/Collection.isEmpty() invocations are fragile to bytecode changes and need recompile-time validation.


- **[HIGH] Mixin group: gui/ (MinecraftServerMixin, ServerStatusPacketListenerImplMixin) — ServerStatus record reconstruction incl. forgeData()**  _(uncertain — needs verification)_
  - _file:line:_ src/main/java/com/enviouse/sef/vanish/mixin/gui/MinecraftServerMixin.java:42,69, gui/ServerStatusPacketListenerImplMixin.java:16-23
  - _→ NeoForge 1.21.1:_ MinecraftServerMixin injects at MinecraftServer.resetStatusCache within runServer/tickServer and rebuilds ServerStatus; ServerStatusPacketListenerImplMixin @Redirects the 'new ClientboundStatusResponsePacket(ServerStatus,String)'. The ServerStatus record component 'forgeData()' (MinecraftServerMixin.java:69) DOES NOT EXIST in NeoForge — NeoForge does not add forgeData to ServerStatus. Must drop that component and use NeoForge's ServerStatus shape (verify whether NeoForge adds its own component or uses vanilla ServerStatus unchanged).
  - _notes:_ The forgeData() call is a guaranteed compile break. ServerStatus.Players record + ClientboundStatusResponsePacket constructor signature must also be re-verified for 1.21.1 (status response was a vanilla record in 1.20.x). Also verify MinecraftServer.resetStatusCache / status field / ANONYMOUS_PLAYER_PROFILE / hidesOnlinePlayers still exist with those Mojang names in 1.21.1.


- **[HIGH] Mixin group: sound/ (EntityMixin) — Entity.move/setPos, isInvisible, broadcastToPlayer, isInvisibleTo, playSound(SoundEvent,FF)**  _(uncertain — needs verification)_
  - _file:line:_ src/main/java/com/enviouse/sef/vanish/mixin/sound/EntityMixin.java:23,30,37,54,63
  - _→ NeoForge 1.21.1:_ Re-validate each Entity target descriptor in 1.21.1 Mojang names: move(MoverType,Vec3) with INVOKE setPos(DDD) ordinal=1; isInvisible()Z; broadcastToPlayer(ServerPlayer)Z; isInvisibleTo(Player)Z; playSound(Lnet/minecraft/sounds/SoundEvent;FF)V. No import package changes (all vanilla).
  - _notes:_ Entity.broadcastToPlayer existed in 1.20.x; its presence/signature in 1.21.1 must be confirmed (entity-tracking visibility logic shifted). The move()->setPos INVOKE ordinal=1 injection point is bytecode-fragile and may shift. playSound(SoundEvent,FF) likely stable. broadcastToPlayer is the riskiest.


- **[MEDIUM] Mixin group: world/ (PlayerMixin, ServerLevelMixin, SleepStatusMixin, ContainerMixin, WardenMixin) — sleeping, entity-tick active-entity tracking, container open/close anims, warden targeting, block-destroy-progress packet**  _(uncertain — needs verification)_
  - _file:line:_ src/main/java/com/enviouse/sef/vanish/mixin/world/PlayerMixin.java:23, world/ServerLevelMixin.java:29,41,46,52,57, world/SleepStatusMixin.java:17, world/ContainerMixin.java:20,26, world/WardenMixin.java:15
  - _→ NeoForge 1.21.1:_ Mojang targets to re-validate: Player.isSleepingLongEnough; ServerLevel.destroyBlockProgress (@WrapOperation on ServerGamePacketListenerImpl.send(Packet)), ServerLevel.tickNonPassenger/tickPassenger (INVOKE Entity.tick()/rideTick()); SleepStatus.update(argsOnly List<ServerPlayer>); Container startOpen/stopOpen (multi-target Barrel/Chest/EnderChest/ShulkerBox); Warden.canTargetEntity (INVOKE isAlliedTo).
  - _notes:_ Most are stable simple-signature methods. Re-verify ServerLevel.tickNonPassenger/tickPassenger still exist with those names (entity ticking has been touched in 1.21) and that ServerLevel.destroyBlockProgress still calls ServerGamePacketListenerImpl.send(Packet) (single-arg send). ContainerMixin relies on BarrelBlockEntity/ChestBlockEntity/ShulkerBoxBlockEntity/PlayerEnderChestContainer all still declaring startOpen/stopOpen(Player).


- **[HIGH] Mixin group: interaction/ (FallOnBlockMixin, InsideBlockMixin, StepOnBlockMixin, InteractionLivingEntityMixin, InteractionPlayerMixin, VibrationSystemMixin, VanishEntitySelectorMixins$AbstractMinecartMixin/$BeehiveBlockMixin) — collision/pickup/projectile/block-contact filtering**  _(uncertain — needs verification)_
  - _file:line:_ src/main/java/com/enviouse/sef/vanish/mixin/interaction/FallOnBlockMixin.java:20, interaction/InsideBlockMixin.java:21, interaction/StepOnBlockMixin.java:22, interaction/InteractionLivingEntityMixin.java:16, interaction/InteractionPlayerMixin.java:25,33,40,47, interaction/VibrationSystemMixin.java:19, interaction/VanishEntitySelectorMixins.java:31,42
  - _→ NeoForge 1.21.1:_ Re-validate Mojang targets: Block.fallOn / entityInside / stepOn descriptors; LivingEntity.isPushable (WrapOp on isSpectator); Player.touch (WrapOp playerTouch), canBeHitByProjectile, isInvulnerableTo(DamageSource), attack (WrapOp Level.getEntitiesOfClass); VibrationSystem.Listener.handleGameEvent; AbstractMinecart.tick (WrapOp Level.getEntities); BeehiveBlock.angerNearbyBees (WrapOp getEntitiesOfClass).
  - _notes:_ AbstractMinecart was significantly refactored in 1.21.1 (new minecart behavior / experimental movement) — the 'tick' method and its getEntities call may have moved; mark uncertain. canBeHitByProjectile and Player.attack interactions touched across 1.20.x->1.21. fallOn/entityInside/stepOn block signatures are stable-ish. VibrationSystem.Listener.handleGameEvent signature should be confirmed. Block-class targets are multi-target so a single descriptor mismatch fails the whole mixin.


- **[CRITICAL] world/LivingEntityMixin: hasEffect(MobEffect), getEffect(MobEffect), updateInvisibilityStatus, getActiveEffectsMap().containsKey(MobEffects.INVISIBILITY), checkFallDamage->ServerLevel.sendParticles**  _(uncertain — needs verification)_
  - _file:line:_ src/main/java/com/enviouse/sef/vanish/mixin/world/LivingEntityMixin.java:37,44,51,54,60
  - _→ NeoForge 1.21.1:_ In 1.20.5+/1.21.1 MobEffect references became Holder<MobEffect>: target descriptors change from (Lnet/minecraft/world/effect/MobEffect;)Z to (Lnet/minecraft/core/Holder;)Z; method params must become Holder<MobEffect>; MobEffects.INVISIBILITY is now a Holder<MobEffect>; getActiveEffectsMap() key type is Holder<MobEffect>. Rewrite hasEffect/getEffect/updateInvisibilityStatus injects and the @WrapOperation INVOKE target accordingly. Verify canBeSeenByAnyone still exists and sendParticles descriptor (DDDIDDDD vs new shape).
  - _notes:_ Near-certain break: the entire MobEffect-as-Holder change in 1.20.5 invalidates these @Inject/@WrapOperation target descriptors and Java param types. spoofVanishedPlayerInvisibility is a core vanish feature. canBeSeenByAnyone and ServerLevel.sendParticles(ParticleOptions,DDD,I,DDDD)I descriptor must also be confirmed against 1.21.1. Highest-confidence breaker in the mixin set.


- **[HIGH] ServerPlayerMixin: sendChatMessage(OutgoingChatMessage,boolean,ChatType.Bound), OutgoingChatMessage.Player.message().link().sender(), ChatType.bind/CHAT/TEAM_MSG_COMMAND_INCOMING, isSpectator**  _(uncertain — needs verification)_
  - _file:line:_ src/main/java/com/enviouse/sef/vanish/mixin/ServerPlayerMixin.java:41-65,68
  - _→ NeoForge 1.21.1:_ Re-validate the chat-signing path in 1.21.1: ServerPlayer.sendChatMessage(OutgoingChatMessage,boolean,ChatType.Bound) signature; OutgoingChatMessage.Player + PlayerChatMessage.link().sender(); ChatType.bind(ResourceKey,RegistryAccess,Component) and the ChatType.CHAT/TEAM_MSG_COMMAND_INCOMING constants. isSpectator()Z HEAD inject is low risk.
  - _notes:_ Chat signing/SignedMessageLink internals were repeatedly reworked across 1.20.x. The accessor chain message().link().sender() and ChatType.bind overloads are the fragile parts. The class also extends Player (abstract mixin) with a synthetic ctor at line 34 — verify Player(Level,BlockPos,float,GameProfile) ctor signature unchanged in 1.21.1 (was stable in 1.20.x).


- **[CRITICAL] ServerGamePacketListenerImplMixin: ClientboundPlayerInfoUpdatePacket.entries()/entries field write/Entry.profileId(), send(Packet)/send(Packet,PacketSendListener), ClientboundTakeItemEntityPacket, ClientboundSystemChatPacket.content(), handlePlayerAction/handleUseItemOn/handleUseItem/handleInteract/tick**  _(uncertain — needs verification)_
  - _file:line:_ src/main/java/com/enviouse/sef/vanish/mixin/ServerGamePacketListenerImplMixin.java:54,60,65,122-124,200-228
  - _→ NeoForge 1.21.1:_ Re-validate against 1.21.1: ServerGamePacketListenerImpl.send(Packet) AND send(Packet,PacketSendListener) — in 1.20.5/1.21 the listener-arg send may have changed to PacketSendListener still present, confirm both overloads exist. ClientboundPlayerInfoUpdatePacket.entries()/Entry.profileId() and the AT-enabled 'entries' field write (line 65) depend on AT entry #3. ClientboundSystemChatPacket.content() + TranslatableContents.getKey()/getArgs() stable-ish. handlePlayerAction/handleUseItemOn/handleUseItem/handleInteract/tick should be confirmed.
  - _notes:_ Multiple risk vectors in one class: (1) the two send() overloads — NeoForge/1.21 may have consolidated PacketSendListener; (2) direct field write infoPacket.entries (line 65) tied to AT; (3) ServerLifecycleHooks import (line 35) must become net.neoforged.neoforge.server.ServerLifecycleHooks; (4) ClientboundTakeItemEntityPacket getPlayerId/getItemId/getAmount accessors. Also imports net.minecraftforge.server.ServerLifecycleHooks at line 35.


- **[CRITICAL] PlayerListMixin: PlayerList.placeNewPlayer (INVOKE broadcastSystemMessage), PlayerList.broadcast(Player,double,double,double,double,ResourceKey<Level>,Packet)**  _(uncertain — needs verification)_
  - _file:line:_ src/main/java/com/enviouse/sef/vanish/mixin/PlayerListMixin.java:27,38-39
  - _→ NeoForge 1.21.1:_ Re-validate PlayerList.placeNewPlayer signature (was placeNewPlayer(Connection,ServerPlayer) in 1.20.1; in 1.20.2+ a CommonListenerCookie 3rd arg was ADDED -> placeNewPlayer(Connection,ServerPlayer,CommonListenerCookie)). The @Inject method descriptor and the Java handler param list MUST add the cookie param. Confirm PlayerList.broadcast(...) and broadcastSystemMessage(Component,boolean) INVOKE target.
  - _notes:_ placeNewPlayer gained a CommonListenerCookie parameter in 1.20.2 — this is a confirmed signature change between 1.20.1 and 1.21.1, so vanishmod$onSendJoinMessage (line 28) will fail to bind unless the cookie param is added. High-certainty breaker driving join-vanish logic.


- **[HIGH] VanishingHandler core packet logic: ClientboundPlayerInfoUpdatePacket.createPlayerInitializing, ClientboundPlayerInfoRemovePacket, ClientboundRemoveEntitiesPacket, ClientboundSetActionBarTextPacket, ServerChunkCache.chunkMap.entityMap (AT), addEntity, refreshTabListName, HoverEvent ctor**  _(uncertain — needs verification)_
  - _file:line:_ src/main/java/com/enviouse/sef/vanish/VanishingHandler.java:63-86,116,43
  - _→ NeoForge 1.21.1:_ Swap MinecraftForge.EVENT_BUS->NeoForge.EVENT_BUS (line 20/116). Re-validate vanilla: createPlayerInitializing(List<ServerPlayer>); ServerChunkCache.chunkMap (field) + .entityMap (AT) + ServerChunkCache.addEntity(Entity); ServerPlayer.refreshTabListName(); HoverEvent(Action,Component) ctor at line 43 (HoverEvent construction changed in 1.20.5+ component serialization — verify Action.SHOW_TEXT ctor still takes Component).
  - _notes:_ Depends on AT entry #1 (entityMap) and #3 (PlayerInfoUpdate). refreshTabListName() and ServerChunkCache.addEntity must be confirmed for 1.21.1. The HoverEvent(HoverEvent.Action, T) ctor is used widely (also TraceHandler.java:24,56,102 and VanishingHandler.java:43) — component HoverEvent/ClickEvent construction was touched in 1.20.5+ and should be verified once (cross-cuts the chat-component dimension).


- **[LOW] Forge import net.minecraftforge.server.ServerLifecycleHooks in mixins/helpers (used to reach current server inside mixin context)**
  - _file:line:_ src/main/java/com/enviouse/sef/vanish/mixin/ServerGamePacketListenerImplMixin.java:35,125, src/main/java/com/enviouse/sef/vanish/mixin/chat/CombatTrackerMixin.java:17,45
  - _→ NeoForge 1.21.1:_ Change import to net.neoforged.neoforge.server.ServerLifecycleHooks; getCurrentServer() API is the same in NeoForge 1.21.1.
  - _notes:_ Pure import swap; API shape (ServerLifecycleHooks.getCurrentServer()) is preserved in NeoForge. Two mixin files reference it.


**Ordering notes:**

- Depends on the Config dimension being ported FIRST: every mixin and VanishingHandler reads VanishConfig.CONFIG.* (ForgeConfigSpec->ModConfigSpec). VanishConfig must compile before any vanish code does.
- Port the 3 ATs (Mojang names) BEFORE compiling VanishingHandler and ChunkMapTrackedEntityMixin/ServerGamePacketListenerImplMixin — those classes won't compile/load without the AT-granted access (entityMap, entries write, TrackedEntity).
- Rename sef.mixins.json->sefported.mixins.json and update neoforge.mods.toml [[mixins]] reference, and repackage com.enviouse.sef.* to the new base package, BEFORE validating individual mixin targets (otherwise the whole config fails to load).
- Validate against 1.21.1 vanilla source in dependency order: first the CONFIRMED-changed shapes (placeNewPlayer cookie, MobEffect->Holder in LivingEntityMixin, ServerStatus.forgeData(), PlayerInfoUpdate entries), then the per-mixin @At descriptors.
- Resolve the HoverEvent/ClickEvent/Component construction question once (cross-cuts the chat-component dimension) since VanishingHandler.java:43 and TraceHandler.java reuse the same ctors — coordinate with the chat-component audit before touching these.
- Swap MinecraftForge.EVENT_BUS->NeoForge.EVENT_BUS and net.minecraftforge.server.ServerLifecycleHooks->net.neoforged.neoforge.server.ServerLifecycleHooks as part of the global import-rewrite pass; these are independent of target-shape validation and can be done early/mechanically.


**Open questions (this dimension):**

- Does ChunkMap still expose a field named 'entityMap' (Int2ObjectMap<TrackedEntity>) in 1.21.1 Mojang mappings, or was it renamed/relocated during ChunkMap refactors? (drives AT #1 + VanishingHandler.java:80-82)
- Is ClientboundPlayerInfoUpdatePacket.entries still a non-final-able List<Entry> field that can be reassigned via 'public-f' AT, or is the record now immutable requiring a rebuild of the packet? (drives AT #3 + ServerGamePacketListenerImplMixin.java:65)
- Confirm PlayerList.placeNewPlayer signature in 1.21.1 — does it carry the CommonListenerCookie 3rd param (added 1.20.2)? PlayerListMixin.java:28 handler must match.
- Do ServerGamePacketListenerImpl.send(Packet) and send(Packet,PacketSendListener) both still exist as distinct overloads in 1.21.1, or has the listener-arg variant changed?
- Confirm the MobEffect->Holder<MobEffect> migration affects LivingEntity.hasEffect/getEffect/updateInvisibilityStatus and getActiveEffectsMap() key type in 1.21.1 (drives the CRITICAL LivingEntityMixin rewrite).
- Does NeoForge's ServerStatus record drop forgeData() entirely, and what replaces it (if anything) for MinecraftServerMixin.java:69 reconstruction?
- Is Entity.broadcastToPlayer(ServerPlayer) still present with that name/signature in 1.21.1? (sound/EntityMixin.java:37)
- Confirm ServerPlayer.sendChatMessage(OutgoingChatMessage,boolean,ChatType.Bound) and PlayerChatMessage.link().sender() / ChatType.bind overloads are unchanged in 1.21.1 chat-signing internals.
- Was AbstractMinecart.tick (and its Level.getEntities call) preserved in 1.21.1 after the minecart movement refactor? (VanishEntitySelectorMixins.java:31)
- Does the NeoForge PlayerEvent base still expose the (Player) protected ctor and which accessor (getEntity vs getPlayer) for PlayerVanishEvent?


**Verification verdicts:**

- **CONFIRMED** — AT entry #1: f_140150_ -> ChunkMap.entityMap (public net.minecraft.server.level.ChunkMap entityMap)
    - claim: Rewrite SRG f_140150_ as Mojang name 'public net.minecraft.server.level.ChunkMap entityMap'; NeoForge 1.21.1 ATs use Mojang names; ModDevGradle auto-detects META-INF/accesstransformer.cfg.
    - verified → public net.minecraft.server.level.ChunkMap entityMap
    - evidence: mappings.dev/1.21.1 ChunkMap: f_140150_ -> Mojang 'entityMap' (private final Int2ObjectMap<ChunkMap$TrackedEntity>). NeoForge javadoc (nekoyue 1.21.x-neoforge ChunkMap) shows field 'entityMap'. NeoForged AT docs (github.com/neoforged/Documentation docs/advanced/accesstransformers.md) give the field directive '<modifier> <FQCN> <fieldName>' with concrete example 'protected-f net.minecraft.server.MinecraftServer random' (Mojang named, not SRG). ModDevGradle auto-includes src/main/resources/META-INF/accesstransformer.cfg per neoforged/ModDevGradle README.
- **CONFIRMED** — AT entry #2: public net.minecraft.server.level.ChunkMap$TrackedEntity (nested class access)
    - claim: Same line works unchanged in Mojang-name AT format; ChunkMap$TrackedEntity exists and is mixin-targeted; mixin shadows 'entity' and injects 'updatePlayer'.
    - verified → same
    - evidence: NeoForge 1.21.x javadoc nekoyue ChunkMap.TrackedEntity.html: nested class exists (package-private), has 'final Entity entity' field and 'public void updatePlayer(ServerPlayer p_140498_)'. Class-reference AT lines are mapping-name agnostic (FQCN with $ separator), so the line is valid as-is in 1.21.1. Matches mixin world/ChunkMapTrackedEntityMixin (@Mixin(TrackedEntity.class), @Shadow Entity entity, @Inject method='updatePlayer').
- **CONFIRMED** — AT entry #3: f_244436_ -> ClientboundPlayerInfoUpdatePacket.entries with un-final (public-f)
    - claim: Rewrite as 'public-f net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket entries'; field still named 'entries', List<Entry>, needs strip-final because mixin reassigns infoPacket.entries.
    - verified → public-f net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket entries
    - evidence: mappings.dev/1.21.1 ClientboundPlayerInfoUpdatePacket: f_244436_ -> Mojang 'entries', declared 'private final List<ClientboundPlayerInfoUpdatePacket$Entry>'. Because it is private+final and ServerGamePacketListenerImplMixin.java:65 does 'infoPacket.entries = filteredPacketEntries', the AT must both widen to public and strip final, i.e. 'public-f'. Confirmed against source line 65.
- **CONFIRMED** — PlayerVanishEvent extends net.minecraftforge...PlayerEvent; posted via MinecraftForge.EVENT_BUS.post
    - claim: Change import to net.neoforged.neoforge.event.entity.player.PlayerEvent (PlayerEvent(Player) ctor exists); change posting to net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post; event not @Cancelable.
    - verified → extends net.neoforged.neoforge.event.entity.player.PlayerEvent (ctor PlayerEvent(Player) exists, extends LivingEvent->EntityEvent->net.neoforged.bus.api.Event); post via net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(event). One nuance: the NeoForge PlayerEvent(Player) ctor is PUBLIC (not protected as proposed) but super(player) still compiles fine.
    - evidence: NeoForge 1.21.x javadoc nekoyue net/neoforged/neoforge/event/entity/player/PlayerEvent.html: package net.neoforged.neoforge.event.entity.player, constructor PlayerEvent(Player) (public), extends LivingEvent. NeoForge.html (net.neoforged.neoforge.common): public static IEventBus EVENT_BUS. docs.neoforged.net/docs/1.21.1/concepts/events confirms NeoForge.EVENT_BUS is the game bus; posting is via IEventBus.post. Source PlayerVanishEvent.java extends PlayerEvent with super(player); VanishingHandler.java:116 posts the event.
- **CONFIRMED** — chat/ group targets: EntitySelector.findSingleEntity/findSinglePlayer, EntityArgument.getEntities/getPlayers, CombatTracker.getDeathMessage+getFallMessage, DamageSource.getLocalizedDeathMessage, CommandSourceStack.getOnlinePlayerNames, PlayerAdvancements.award, ListPlayersCommand.format
    - claim: Mojang-name targets valid in 1.21.1 with listed method names/descriptors.
    - verified → same
    - evidence: nekoyue 1.21.x-neoforge javadocs: EntitySelector.findSingleEntity(CommandSourceStack):Entity and findSinglePlayer(CommandSourceStack):ServerPlayer; EntityArgument.getEntities(CommandContext,String):Collection and getPlayers(...):Collection<ServerPlayer>; CombatTracker.getDeathMessage():Component and getFallMessage(CombatEntry,Entity):Component (matches @Redirect target descriptor in CombatTrackerMixin); DamageSource.getLocalizedDeathMessage(LivingEntity):Component; CommandSourceStack.getOnlinePlayerNames():Collection<String>; PlayerAdvancements.award(AdvancementHolder,String):boolean (method name 'award' unchanged; mixin redirects inner broadcastSystemMessage(Component,Z)). ListPlayersCommand.format not directly fetched but target is name-only @Redirect on PlayerList.getPlayers()Ljava/util/List; which is confirmed on PlayerList javadoc.
- **CORRECTED** — gui/ group: MinecraftServerMixin rebuilds ServerStatus incl. forgeData(); ServerStatusPacketListenerImplMixin redirects new ClientboundStatusResponsePacket
    - claim: forgeData() does not exist in NeoForge; drop that component and use NeoForge's ServerStatus shape.
    - verified → ServerStatus in NeoForge 1.21.1 has 6 record components: description(), players(), version(), favicon(), enforcesSecureChat(), isModded(). There is NO forgeData(). The reconstruction at MinecraftServerMixin.java:69 must change 'mainServerStatus.forgeData()' to 'mainServerStatus.isModded()', and use the canonical ctor ServerStatus(Component, Optional<Players>, Optional<Version>, Optional<Favicon>, boolean enforcesSecureChat, boolean isModded). NOT simply 'drop the component' (the 6-arg constructor still requires the 6th boolean isModded).
    - evidence: nekoyue 1.21.x-neoforge net/minecraft/network/protocol/status/ServerStatus.html: record components description, players(Optional<Players>), version(Optional<Version>), favicon(Optional<Favicon>), enforcesSecureChat(boolean), isModded(boolean); canonical ctor ServerStatus(Component,Optional,Optional,Optional,boolean,boolean); no forgeData accessor. Source MinecraftServerMixin.java:69 currently calls mainServerStatus.forgeData().
- **CONFIRMED** — sound/ EntityMixin: Entity.move(MoverType,Vec3) INVOKE setPos(DDD) ordinal=1, isInvisible()Z, broadcastToPlayer(ServerPlayer)Z, isInvisibleTo(Player)Z, playSound(SoundEvent,FF)V
    - claim: Each Entity target descriptor valid in 1.21.1 Mojang names; no import changes.
    - verified → same
    - evidence: nekoyue 1.21.x-neoforge Entity.html: move(MoverType,Vec3):void; setPos(double,double,double):void; isInvisible():boolean; broadcastToPlayer(ServerPlayer):boolean; isInvisibleTo(Player):boolean; playSound(SoundEvent,float,float):void. All match EntityMixin targets including the playSound(Lnet/minecraft/sounds/SoundEvent;FF)V descriptor. (Note: setPos ordinal=1 inside move is a bytecode-position assumption, not a mapping concern, and is unverifiable from javadocs alone but the descriptors are correct.)
- **CONFIRMED** — world/ group: Player.isSleepingLongEnough; ServerLevel.destroyBlockProgress / tickNonPassenger / tickPassenger; SleepStatus.update(List<ServerPlayer>); Container startOpen/stopOpen; Warden.canTargetEntity (INVOKE isAlliedTo)
    - claim: All Mojang targets valid in 1.21.1.
    - verified → same
    - evidence: nekoyue 1.21.x-neoforge: Player.isSleepingLongEnough():boolean; ServerLevel.destroyBlockProgress(int,BlockPos,int), tickNonPassenger(Entity) [INVOKE Entity.tick()V valid], tickPassenger(Entity,Entity) [INVOKE Entity.rideTick()V]; SleepStatus.update(List<ServerPlayer>):boolean (argsOnly List<ServerPlayer> matches); ChestBlockEntity.startOpen(Player)/stopOpen(Player) (Container interface methods, applies to Barrel/Chest/EnderChest/ShulkerBox); Warden.canTargetEntity(Entity):boolean and inherited isAlliedTo(Entity):boolean. ServerLevelMixin destroyBlockProgress @WrapOperation targets ServerGamePacketListenerImpl.send(Packet)V which is confirmed (see #12).
- **CONFIRMED** — interaction/ group: Block.fallOn/entityInside/stepOn; LivingEntity.isPushable (WrapOp isSpectator); Player.touch (WrapOp playerTouch), canBeHitByProjectile, isInvulnerableTo(DamageSource), attack (WrapOp getEntitiesOfClass); VibrationSystem.Listener.handleGameEvent; AbstractMinecart.tick (WrapOp getEntities); BeehiveBlock.angerNearbyBees (WrapOp getEntitiesOfClass)
    - claim: All Mojang targets valid in 1.21.1.
    - verified → same
    - evidence: nekoyue 1.21.x-neoforge: Block.fallOn(Level,BlockState,BlockPos,Entity,float), stepOn(Level,BlockPos,BlockState,Entity), entityInside(BlockState,Level,BlockPos,Entity) [from BlockBehaviour]; LivingEntity.isPushable()boolean and isSpectator()boolean (Entity); Player.touch(Entity) [INVOKE Entity.playerTouch(Player)V], canBeHitByProjectile():boolean (LivingEntity), isInvulnerableTo(DamageSource):boolean, attack(Entity) [INVOKE Level.getEntitiesOfClass(Class,AABB):List]; VibrationSystem.Listener.handleGameEvent(ServerLevel,Holder<GameEvent>,Context,Vec3):boolean (name-only HEAD inject, OK); AbstractMinecart.tick() exists [INVOKE Level.getEntities(Entity,AABB):List]; BeehiveBlock.angerNearbyBees(Level,BlockPos) [INVOKE Level.getEntitiesOfClass(Class,AABB):List]. Note: VibrationSystem.handleGameEvent now uses Holder<GameEvent> but mixin only matches by method name so unaffected.
- **CONFIRMED** — world/LivingEntityMixin: hasEffect(MobEffect), getEffect(MobEffect), updateInvisibilityStatus WrapOp hasEffect INVOKE, getActiveEffectsMap().containsKey(MobEffects.INVISIBILITY), checkFallDamage -> ServerLevel.sendParticles
    - claim: MobEffect became Holder<MobEffect> in 1.20.5+/1.21.1; descriptors change to (Lnet/minecraft/core/Holder;)...; params become Holder<MobEffect>; MobEffects.INVISIBILITY is Holder<MobEffect>; getActiveEffectsMap key is Holder<MobEffect>; verify canBeSeenByAnyone and sendParticles descriptor.
    - verified → CORRECT and REQUIRED. In 1.21.1: hasEffect(Holder<MobEffect>):boolean, getEffect(Holder<MobEffect>):MobEffectInstance, getActiveEffectsMap():Map<Holder<MobEffect>,MobEffectInstance>, MobEffects.INVISIBILITY is Holder<MobEffect>. The mixin's @Inject method params must change from MobEffect to Holder<MobEffect>, and the @WrapOperation INVOKE target descriptor for hasEffect must become (Lnet/minecraft/core/Holder;)Z (was (Lnet/minecraft/world/effect/MobEffect;)Z). canBeSeenByAnyone():boolean still exists. checkFallDamage WrapOp target ServerLevel.sendParticles(Lnet/minecraft/core/particles/ParticleOptions;DDDIDDDD)I is unchanged and correct. The current source (LivingEntityMixin.java lines 38,45,51-52) using raw MobEffect WILL NOT COMPILE/APPLY in 1.21.1 and must be migrated to Holder<MobEffect>.
    - evidence: NeoForge 1.21.1 LivingEntity javadoc (lexxie.dev/neoforge/1.21.1 + nekoyue 1.21.x-neoforge): hasEffect(Holder<MobEffect>):boolean, getEffect(Holder<MobEffect>):MobEffectInstance, getActiveEffectsMap():Map<Holder<MobEffect>,MobEffectInstance>, canBeSeenByAnyone():boolean, updateInvisibilityStatus():void. ServerLevel.html: <T extends ParticleOptions> int sendParticles(T,double,double,double,int,double,double,double,double) -> descriptor (Lnet/minecraft/core/particles/ParticleOptions;DDDIDDDD)I confirmed. docs.fabricmc.net/develop/entities/effects and NeoForge docs/items/mobeffects confirm the MobEffect->Holder<MobEffect> migration landed in 1.20.5/1.21.
- **CONFIRMED** — ServerPlayerMixin: sendChatMessage(OutgoingChatMessage,boolean,ChatType.Bound), OutgoingChatMessage.Player.message().link().sender(), ChatType.bind/CHAT/TEAM_MSG_COMMAND_INCOMING, isSpectator
    - claim: Re-validate chat-signing path signatures in 1.21.1.
    - verified → same (sendChatMessage and isSpectator confirmed); ChatType.bind/CHAT/TEAM_MSG_COMMAND_INCOMING and OutgoingChatMessage.Player/PlayerChatMessage.link().sender() not individually re-fetched.
    - evidence: nekoyue 1.21.x-neoforge ServerPlayer.html: sendChatMessage(OutgoingChatMessage,boolean,ChatType.Bound):void and isSpectator():boolean both present with exact signatures. The ChatType.bind(ResourceKey,RegistryAccess,Component), ChatType.CHAT/TEAM_MSG_COMMAND_INCOMING constants and OutgoingChatMessage.Player/PlayerChatMessage.link().sender() are part of the 1.19.1+ chat-signing API that is stable through 1.21.1 (no rename evidence found), but I did not individually open the ChatType/OutgoingChatMessage/PlayerChatMessage javadocs in this pass, so treat those sub-members as high-confidence-but-not-line-verified.
- **CONFIRMED** — ServerGamePacketListenerImplMixin: send(Packet) + send(Packet,PacketSendListener); ClientboundPlayerInfoUpdatePacket.entries()/Entry.profileId(); entries field write (AT #3); ClientboundTakeItemEntityPacket; ClientboundSystemChatPacket.content(); TranslatableContents.getKey()/getArgs(); handlePlayerAction/handleUseItemOn/handleUseItem/handleInteract/tick
    - claim: Both send overloads exist in 1.21.1; entries()/profileId()/field-write valid; handle* and tick methods exist.
    - verified → same
    - evidence: ClientboundPlayerInfoUpdatePacket.html (nekoyue 1.21.x-neoforge): entries():List<Entry> and field 'entries' (List<Entry>) both present; Entry record profileId():UUID confirmed (Entry.html). Field write depends on AT #3 which is CONFIRMED. send(Packet)V is referenced and redirected by ServerLevelMixin.destroyBlockProgress @WrapOperation (valid target) and ServerGamePacketListenerImpl in 1.21.1 retains both send(Packet) and send(Packet,PacketSendListener) overloads (PacketSendListener still exists in 1.21.1 net.minecraft.network). ClientboundSystemChatPacket.content():Component and TranslatableContents.getKey()/getArgs() are stable 1.19+ API. handlePlayerAction/handleUseItemOn/handleUseItem/handleInteract/tick are standard ServerGamePacketListenerImpl methods retained in 1.21.1. (PacketSendListener overload and handle* methods not individually javadoc-fetched this pass; send(Packet)V independently confirmed via WrapOperation target validity.)
- **CONFIRMED** — PlayerListMixin: PlayerList.placeNewPlayer (INVOKE broadcastSystemMessage); PlayerList.broadcast(Player,double,double,double,double,ResourceKey<Level>,Packet)
    - claim: placeNewPlayer gained a CommonListenerCookie 3rd arg in 1.20.2+ -> the @Inject descriptor and Java handler must add the cookie param. Confirm broadcast and broadcastSystemMessage(Component,boolean).
    - verified → REQUIRED change confirmed: placeNewPlayer(Connection, ServerPlayer, CommonListenerCookie). The mixin handler vanishmod$onSendJoinMessage(Connection, ServerPlayer, CallbackInfo) at PlayerListMixin.java:28 MUST add a CommonListenerCookie (net.minecraft.server.network.CommonListenerCookie) third param before CallbackInfo, otherwise the @Inject will fail to apply. broadcast(...) and broadcastSystemMessage(Component,boolean) unchanged.
    - evidence: nekoyue 1.21.x-neoforge PlayerList.html: placeNewPlayer(Connection p_11262_, ServerPlayer p_11263_, CommonListenerCookie p_301988_):void; broadcast(Player,double,double,double,double,ResourceKey<Level>,Packet):void; broadcastSystemMessage(Component,boolean):void. Current source PlayerListMixin.java:28 only declares (Connection, ServerPlayer, CallbackInfo) — missing the cookie param added in 1.20.2.
- **CONFIRMED** — VanishingHandler core: createPlayerInitializing, ClientboundPlayerInfoRemovePacket, ServerChunkCache.chunkMap.entityMap (AT) + addEntity, ServerPlayer.refreshTabListName, HoverEvent(Action,Component) ctor; EVENT_BUS swap
    - claim: createPlayerInitializing(List<ServerPlayer>); ServerChunkCache.chunkMap + entityMap (AT) + addEntity(Entity); refreshTabListName(); HoverEvent(Action.SHOW_TEXT, Component) ctor still valid; MinecraftForge.EVENT_BUS -> NeoForge.EVENT_BUS.
    - verified → Mostly CONFIRMED with one signature nuance: createPlayerInitializing takes Collection<ServerPlayer> (not List<ServerPlayer>) in 1.21.1 -- VanishingHandler passes List.of(...) which is a Collection, so it still compiles, but the proposal's 'List<ServerPlayer>' description is imprecise. ServerChunkCache.chunkMap (final field, ChunkMap) + addEntity(Entity):void confirmed; entityMap via AT #1 confirmed. ServerPlayer.refreshTabListName():void confirmed. HoverEvent(HoverEvent.Action<T>, T) ctor still exists in 1.21.1 (Action.SHOW_TEXT is Action<Component>), so new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(note)) compiles -- the breaking sealed/record HoverEvent rewrite is a 1.21.5 change, NOT 1.21.1. EVENT_BUS swap to net.neoforged.neoforge.common.NeoForge.EVENT_BUS confirmed.
    - evidence: nekoyue 1.21.x-neoforge: ClientboundPlayerInfoUpdatePacket.createPlayerInitializing(Collection<ServerPlayer>):ClientboundPlayerInfoUpdatePacket; ServerChunkCache.html: 'final ChunkMap chunkMap' field + addEntity(Entity):void; ServerPlayer.html: refreshTabListName():void; HoverEvent.html: single public ctor HoverEvent(HoverEvent.Action<T> p_130818_, T p_130819_). minecraft.wiki Text_component_format/Before_Java_Edition_1.21.5 + FastAsyncWorldEdit issue #3179 confirm the hover_event/serialization breaking change is 1.21.5, not 1.21.1. NeoForge.EVENT_BUS confirmed (net.neoforged.neoforge.common.NeoForge). VanishingHandler.java:63/66 pass List.of(...), line 116 posts via EVENT_BUS.


### InvSee container/menu (GUI opening) (`invsee-menu`)


**Summary:** The InvSee feature is a server-side AbstractContainerMenu (InvSeeContainer) opened via ServerPlayer.openMenu(MenuProvider) and backed by the vanilla MenuType.GENERIC_9x6 (no custom MenuType is ever registered), so the openMenu path is ALREADY NeoForge-compatible — no NetworkHooks/IForgeMenuType migration is needed here. The two genuinely breaking surfaces are: (1) the Forge items package net.minecraftforge.items.* (IItemHandlerModifiable, SlotItemHandler) which must move to net.neoforged.neoforge.items.* across InvSeeContainer and its Curios helper, and (2) the vanilla ItemStack.setHoverName(Component) API which was REMOVED in the 1.20.5 data-component overhaul and must become stack.set(DataComponents.CUSTOM_NAME, Component). Overall risk MEDIUM: mechanical import/API swaps with no unknown-shape blockers, but the setHoverName change is multi-site and the Curios IItemHandlerModifiable type identity must line up with NeoForge's package.


**Findings (9):**


- **[LOW] net.minecraftforge.items.IItemHandlerModifiable / SlotItemHandler imports**
  - _file:line:_ src/main/java/com/enviouse/sef/invsee/InvSeeContainer.java:18-19
  - _→ NeoForge 1.21.1:_ Swap imports to net.neoforged.neoforge.items.IItemHandlerModifiable and net.neoforged.neoforge.items.SlotItemHandler. Same class/constructor shape: new SlotItemHandler(IItemHandler, int slot, int x, int y). Used at lines 53 (record field type), 207, 211, 319. All other usages (addCuriosSlot param, LockedCuriosSlot ctor) are just the same type and follow automatically.
  - _notes:_ Pure package relocation; NeoForge ports the Forge items API verbatim. Constructor SlotItemHandler(handler, index, x, y) is unchanged. The record CuriosSlotRef(IItemHandlerModifiable handler,...) at line 53 and LockedCuriosSlot at 318-328 also depend on this swap.


- **[MEDIUM] ItemStack.setHoverName(Component) — vanilla MC**
  - _file:line:_ src/main/java/com/enviouse/sef/invsee/InvSeeContainer.java:218,227,236; src/main/java/com/enviouse/sef/invsee/InvSeeLayout.java:19,28,37
  - _→ NeoForge 1.21.1:_ setHoverName was removed in 1.20.5's data-component refactor. Replace each call with stack.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME, component). e.g. pane.set(DataComponents.CUSTOM_NAME, TextFormatter.stringToFormattedText(label)). Add import net.minecraft.core.component.DataComponents.
  - _notes:_ 6 call sites across the two files (3 in InvSeeContainer, 3 in InvSeeLayout). This is the most impactful vanilla break in this dimension. Note: vanilla custom names are rendered italic; if exact display matters they may need component.withStyle(s->s.withItalic(false)), but functionally CUSTOM_NAME is the direct replacement. InvSeeLayout.makeSeparator/makeNavArrow/makeFiller appear unused by InvSeeContainer (which builds its own panes) — confirm dead code but still must compile.


- **[LOW] ServerPlayer.openMenu(MenuProvider) — menu opening path**
  - _file:line:_ src/main/java/com/enviouse/sef/invsee/InvSeeCommand.java:105-115
  - _→ NeoForge 1.21.1:_ NO CHANGE NEEDED. Code already uses viewer.openMenu(MenuProvider) rather than NetworkHooks.openScreen. ServerPlayer.openMenu(MenuProvider) and openMenu(MenuProvider, Consumer<RegistryFriendlyByteBuf>) both exist in NeoForge 1.21.1. MenuProvider.getDisplayName()/createMenu(int, Inventory, Player) signatures unchanged.
  - _notes:_ This dimension's headline risk (NetworkHooks.openScreen removal) does NOT apply: grep confirms no NetworkHooks usage anywhere. The anonymous MenuProvider returns InvSeeContainer which uses a vanilla MenuType, so no extra-data ByteBuf writer is required and no client screen class is needed (vanilla GENERIC_9x6 screen). Server-side-only design holds.


- **[LOW] AbstractContainerMenu(MenuType, int) super ctor with vanilla MenuType.GENERIC_9x6**
  - _file:line:_ src/main/java/com/enviouse/sef/invsee/InvSeeContainer.java:14,56
  - _→ NeoForge 1.21.1:_ NO CHANGE. net.minecraft.world.inventory.MenuType.GENERIC_9x6 and AbstractContainerMenu(MenuType<?>, int containerId) are unchanged in 1.21.1. No DeferredRegister / IMenuTypeExtension.create needed because no custom MenuType is defined.
  - _notes:_ Confirms the prompt's IForgeMenuType->IMenuTypeExtension concern is N/A for this feature. The mod reuses a vanilla menu type, so it stays a pure server-side menu.


- **[LOW] AbstractContainerMenu overrides: quickMoveStack(Player,int), stillValid(Player), clicked(int,int,ClickType,Player), broadcastChanges()**
  - _file:line:_ src/main/java/com/enviouse/sef/invsee/InvSeeContainer.java:245-285
  - _→ NeoForge 1.21.1:_ Signatures unchanged in 1.21.1: quickMoveStack(Player, int)->ItemStack, stillValid(Player)->boolean, clicked(int,int,ClickType,Player)->void, broadcastChanges()->void. Slot.mayPlace(ItemStack)/mayPickup(Player) (lines 300-327) also unchanged. No migration required beyond ensuring the file compiles after the items-package and setHoverName swaps.
  - _notes:_ With Mojang mappings at runtime in NeoForge these method names are exactly the dev names already used here, so no SRG concern. ClickType import (net.minecraft.world.inventory.ClickType) unchanged.


- **[LOW] ServerPlayer.hasDisconnected()**  _(uncertain — needs verification)_
  - _file:line:_ src/main/java/com/enviouse/sef/invsee/InvSeeContainer.java:284
  - _→ NeoForge 1.21.1:_ Confirm hasDisconnected() still exists on ServerPlayer/Player in 1.21.1. If renamed (some refactors expose it via connection state), fall back to checking target.connection==null or !target.server.getPlayerList().getPlayers().contains(target). Low effort either way.
  - _notes:_ hasDisconnected() existed in 1.20.1 on net.minecraft.server.level.ServerPlayer. Believed still present in 1.21.1 but I did not verify against target sources, hence flagged.


- **[HIGH] Curios API: CuriosApi.getCuriosInventory(player) + ICurioStacksHandler.getStacks() returning an IItemHandlerModifiable**  _(uncertain — needs verification)_
  - _file:line:_ src/main/java/com/enviouse/sef/utils/moddeps/CuriosInventoryHelper.java:7,52-57,93-101,130-138
  - _→ NeoForge 1.21.1:_ Move the IItemHandlerModifiable import to net.neoforged.neoforge.items.IItemHandlerModifiable. For the 1.21.1 Curios NeoForge API: CuriosApi.getCuriosInventory(player) likely now returns ICuriosItemHandler directly (not Optional) — VERIFY; .ifPresent(...) usage at 52/93/130 may need to become a null check. ICurioStacksHandler.getStacks() returns IDynamicStackHandler which must extend NeoForge's IItemHandlerModifiable for the assignment to compile.
  - _notes:_ This helper feeds InvSeeContainer's Curios slots (handler() at line 75, getStacks() at 56/97/133). The package swap is mechanical, but the Curios 1.21.1 API shape (Optional vs direct return on getCuriosInventory, and whether getStacks() still yields a NeoForge IItemHandlerModifiable) is not confirmed against the 1.21.1 jar. If getCuriosInventory no longer returns Optional, lines 52/93/130 break. Covered more deeply by the Curios-integration dimension but it is the data source for this menu.


- **[LOW] net.minecraftforge.fml.ModList**
  - _file:line:_ src/main/java/com/enviouse/sef/invsee/InvSeeCommand.java:20,39; src/main/java/com/enviouse/sef/utils/moddeps/CuriosInventoryHelper.java:6,35,69,84,119
  - _→ NeoForge 1.21.1:_ Swap import to net.neoforged.fml.ModList. Same API: ModList.get().isLoaded("modid"). No call-site changes.
  - _notes:_ Used to gate FTB override (InvSeeCommand:39) and Curios presence (helper). Trivial import swap.


- **[LOW] FTB /invsee command-node removal via reflection on CommandNode.children/literals**  _(uncertain — needs verification)_
  - _file:line:_ src/main/java/com/enviouse/sef/invsee/InvSeeCommand.java:71-89
  - _→ NeoForge 1.21.1:_ No Forge/NeoForge API involved — this reflects on Brigadier's com.mojang.brigadier.tree.CommandNode private fields 'children' and 'literals'. Brigadier version shipped with 1.21.1 still has these fields, so it should keep working. Risk is the FTB modid/version (ftbessentials 2101.1.x) actually registering a node named 'invsee' to remove.
  - _notes:_ Brigadier field names are stable across these MC versions; flag only because reflection on private fields is inherently fragile and depends on FTB still naming its command 'invsee'. EntityArgument/Commands/CommandSourceStack imports (lines 11-13) are vanilla, unchanged.


**Ordering notes:**

- Depends on the Curios-integration dimension being resolved first (or jointly): CuriosInventoryHelper.getCuriosSlotGroups feeds the IItemHandlerModifiable handlers consumed at InvSeeContainer.java:66-76; if the Curios API return shape changes, this menu's Curios pages break.
- Depends on the items-package migration (net.minecraftforge.items.* -> net.neoforged.neoforge.items.*) being applied consistently across BOTH InvSeeContainer.java and CuriosInventoryHelper.java, since the IItemHandlerModifiable type must be the same class on both sides of the CuriosSlotRef handoff.
- Depends on config (ConfigHandler.config.* getters at InvSeeContainer.java:60,114,119,124,127,164,166,169 and InvSeeCommand.java:36,39,98) being ported first — the menu reads many invSee* config values at construction.
- Depends on PermissionsHandler (InvSeeCommand.java:47-48) being ported (permissions dimension).
- Depends on TextFormatter.stringToFormattedText (Component construction) being valid post-1.20.5 component serialization changes — used for both titles and item names.
- The vanilla setHoverName->DataComponents.CUSTOM_NAME swap can be done independently/early as a self-contained mechanical pass across InvSeeContainer.java and InvSeeLayout.java.


**Open questions (this dimension):**

- Does net.minecraft.server.level.ServerPlayer.hasDisconnected() still exist in 1.21.1, or was it renamed/replaced (e.g. connection-state check)? Used at InvSeeContainer.java:284.
- In Curios 1.21.1 NeoForge, does CuriosApi.getCuriosInventory(ServerPlayer) still return Optional<ICuriosItemHandler> (so .ifPresent works at CuriosInventoryHelper.java:52/93/130), or does it now return the handler directly/nullable?
- In Curios 1.21.1, does ICurioStacksHandler.getStacks() still return a type assignable to net.neoforged.neoforge.items.IItemHandlerModifiable (i.e. IDynamicStackHandler extends NeoForge IItemHandlerModifiable)?
- Does TextFormatter.stringToFormattedText(...) return net.minecraft.network.chat.Component compatible with DataComponents.CUSTOM_NAME (a Component value)? (cross-dimension: text/Component serialization)
- Is custom-name italic styling acceptable for the glass-pane labels, or should the replacement set withItalic(false) to match prior setHoverName visual?


**Verification verdicts:**

- **CONFIRMED** — ServerPlayer.hasDisconnected()
    - claim: hasDisconnected() still exists on ServerPlayer in 1.21.1; otherwise fall back to connection==null / playerlist check.
    - verified → same
    - evidence: NeoForge javadoc (ForgeJavaDocs-NG) 1.21.x-neoforge (1.21.0-21.0.30-beta) net/minecraft/server/level/ServerPlayer.html documents 'boolean hasDisconnected()' plus 'void disconnect()' and private field 'disconnected'. Also present in 1.20.6-neoforge javadoc at https://nekoyue.github.io/ForgeJavaDocs-NG/javadoc/1.20.6-neoforge/net/minecraft/server/level/ServerPlayer.html . Method has been stable 1.17-1.21.x; 1.21.1 is a patch over 1.21.0 with no change to this Mojang-mapped method. Used in repo at src/main/java/com/enviouse/sef/invsee/InvSeeContainer.java:284. No fallback needed.
- **CORRECTED** — Curios API: CuriosApi.getCuriosInventory(player) + ICurioStacksHandler.getStacks() returning an IItemHandlerModifiable
    - claim: getCuriosInventory likely now returns ICuriosItemHandler directly (not Optional) — .ifPresent usage at 52/93/130 may need to become a null check; IItemHandlerModifiable import must move to net.neoforged.neoforge.items.IItemHandlerModifiable; IDynamicStackHandler must extend NeoForge IItemHandlerModifiable.
    - verified → PARTIALLY CORRECTED. (a) getCuriosInventory still returns Optional<ICuriosItemHandler>, NOT a direct ICuriosItemHandler — so .ifPresent(...) at lines 52/93/130 is CORRECT and must NOT be changed to a null check. (b) The import change IS required: replace net.minecraftforge.items.IItemHandlerModifiable (line 7) with net.neoforged.neoforge.items.IItemHandlerModifiable. (c) getStacks() returns IDynamicStackHandler which 'extends net.neoforged.neoforge.items.IItemHandlerModifiable', so assigning getStacks() to an IItemHandlerModifiable variable compiles (lines 56/97/133). (d) Package note: ICuriosItemHandler now lives in top.theillusivec4.curios.api.type.capability (not ...type.inventory); the repo code only references it implicitly via the lambda param so no source change needed there, but ICurioStacksHandler is correctly imported from top.theillusivec4.curios.api.type.inventory.
    - evidence: Curios 1.21.1 NeoForge source (GitHub TheIllusiveC4/Curios @1.21.1): neoforge/.../api/CuriosApi.java line 235: 'public static Optional<ICuriosItemHandler> getCuriosInventory(LivingEntity livingEntity)'. neoforge/.../api/type/capability/ICuriosItemHandler.java imports net.neoforged.neoforge.items.IItemHandlerModifiable and declares 'Map<String, ICurioStacksHandler> getCurios();'. neoforge/.../api/type/inventory/ICurioStacksHandler.java line 40: 'IDynamicStackHandler getStacks();'. neoforge/.../api/type/inventory/IDynamicStackHandler.java line 29: 'public interface IDynamicStackHandler extends IItemHandlerModifiable' importing net.neoforged.neoforge.items.IItemHandlerModifiable. Repo usage: src/main/java/com/enviouse/sef/utils/moddeps/CuriosInventoryHelper.java lines 7,52-57,93-97,130-137.
- **CONFIRMED** — FTB /invsee command-node removal via reflection on CommandNode.children/literals
    - claim: Reflects on Brigadier com.mojang.brigadier.tree.CommandNode private fields 'children' and 'literals'; fields still exist in 1.21.1 Brigadier so it keeps working; risk is whether ftbessentials 2101.1.x registers an 'invsee' node.
    - verified → same
    - evidence: Brigadier CommandNode source (Mojang/brigadier): declares 'private final Map<String, CommandNode<S>> children = new LinkedHashMap<>();' and 'private final Map<String, LiteralCommandNode<S>> literals = new LinkedHashMap<>();' — both present and reflectable; this is the same Brigadier shipped with MC 1.21.1 (no field rename). FTB-Essentials branch 1.21.1/main: gradle.properties has mod_id=ftbessentials, minecraft_version=1.21.1, mod_version=2101.1.9 (matches proposed 2101.1.x); common/src/main/java/dev/ftb/mods/ftbessentials/commands/groups/AdminCommands.java line 31 registers literal("invsee") (gated by FTBEStartupConfig.INVSEE). So a node named 'invsee' is registered on the dispatcher root and is removable. Repo usage: src/main/java/com/enviouse/sef/invsee/InvSeeCommand.java lines 39-41, 71-88. Caveat: removal must run after FTB registers its commands (registration ordering), and the literals map removal is best-effort; functionally low risk.


## Appendix B — Completeness critic (surfaces between dimensions)


**Missed surfaces (10):**


- **MinecraftServer.getServerDirectory() return-type change: File (1.20.1) -> Path (1.21.1). Breaks ALL call sites. ServerEssentialsForge.java:137 assigns to 'java.io.File serverDir' (incompatible type), then line 139 calls serverDir.toPath(); and six managers call '.getServerDirectory().toPath()' which is a compile error on Path (no toPath()).**
  - _file:line:_ src/main/java/com/enviouse/sef/ServerEssentialsForge.java:137,139; src/main/java/com/enviouse/sef/filter/FilterManager.java:16; src/main/java/com/enviouse/sef/alts/AltTracker.java:50; src/main/java/com/enviouse/sef/announcements/AnnouncementManager.java:70; src/main/java/com/enviouse/sef/chat/OpBulletinHandler.java:33; src/main/java/com/enviouse/sef/warn/WarnManager.java:76
  - _why:_ Direct compile break, 7 call sites, not part of any named dimension (config/persistence dimension covered the file formats, not the server-dir API type flip). Fix: treat result as Path directly and drop .toPath(); update the File local in ServerEssentialsForge.
- **Forge handshake/extension-point API fully removed in NeoForge: IExtensionPoint.DisplayTest + NetworkConstants.IGNORESERVERONLY. This is the IGNORESERVERONLY registration that makes the mod 'server-side-only acceptable to vanilla clients'. NeoForge replaces this with IExtensionPoint.DisplayTest(()->"<version>", (incoming,isNetwork)->true) and the IGNORESERVERONLY constant is gone (use NetworkConstants removed; NeoForge: IExtensionPoint with DisplayTest.IGNORE_ALL_VERSION / build via ModContainer).**
  - _file:line:_ src/main/java/com/enviouse/sef/utils/loader.java:4,8,19-23
  - _why:_ Three removed Forge symbols on the server-side-only marker path. The build/entrypoint dimension covered @Mod and the new neoforge.mods.toml, but this loader.java handshake-registration helper is a distinct surface and is the actual mechanism enforcing server-only display compatibility.
- **Forge TickEvent API replaced in NeoForge 1.21.1. ServerTickEvent and PlayerTickEvent are split into .Pre/.Post subclasses and the 'event.phase' / TickEvent.Phase.END field no longer exists. All three handlers gate on '.phase == END'.**
  - _file:line:_ src/main/java/com/enviouse/sef/ServerEssentialsForge.java:171-172; src/main/java/com/enviouse/sef/events/PlayerEventHandler.java:119-120; src/main/java/com/enviouse/sef/vanish/VanishEventListener.java:72-73
  - _why:_ Compile break on TickEvent.ServerTickEvent/PlayerTickEvent and the .phase field. Subscribe to ServerTickEvent.Post / PlayerTickEvent.Post instead. Not explicitly in the gameplay/lifecycle dimensions as a phase-field break.
- **ForgeRegistries -> NeoForge BuiltInRegistries migration. 9 call sites use ForgeRegistries.ITEMS/BLOCKS .getKey()/.getKeys(). NeoForge keeps ForgeRegistries under a compat layer in some versions but the canonical 1.21.1 path is BuiltInRegistries.ITEM/BLOCK.getKey(...).**
  - _file:line:_ src/main/java/com/enviouse/sef/banned/BannedItemsManager.java:247,259,267,467,483,519,520; src/main/java/com/enviouse/sef/banned/BannedItemsEventHandler.java:57; src/main/java/com/enviouse/sef/banned/BannedItemsCommands.java:303
  - _why:_ Registry-access pattern change. The banned-items feature's id<->object mapping depends on it. Sits between the 'registration & bus' dimension (which covers DeferredRegister/object registration, not reverse lookups) and gameplay handlers.
- **pack.mcmeta pack_format is 10 (1.16.2-1.16.5 era). 1.20.1=15, 1.21.1=34. Must be updated to 34.**
  - _file:line:_ src/main/resources/pack.mcmeta:4
  - _why:_ Resource-pack metadata version. Flagged per instructions. Currently even wrong for 1.20.1 (so the source was already stale here).
- **Legacy Forge mods.toml still present in resources alongside the new templates/neoforge.mods.toml skeleton. It declares modLoader="javafml", loaderVersion="[47,)" (Forge 47.x = MC 1.20.1) and references logoFile="Logo.png". NeoForge requires META-INF/neoforge.mods.toml with loaderVersion "[21,)" and a [[mixins]] block / mixin config reference.**
  - _file:line:_ src/main/resources/META-INF/mods.toml:10,18,22
  - _why:_ If the old mods.toml ships in the jar it will conflict/confuse loading. Build-skeleton dimension noted the new toml exists, but did not flag that the stale Forge one is still in src/main/resources and the Forge loaderVersion bound is wrong for 1.21.1.
- **ServerStatus record constructed with mainServerStatus.forgeData() component. NeoForge's ServerStatus record has no forgeData() accessor (Forge-specific Optional<ServerStatus.ForgeData>); in NeoForge it is absent/renamed. Reconstructing ServerStatus with that component will not compile.**
  - _file:line:_ src/main/java/com/enviouse/sef/vanish/mixin/gui/MinecraftServerMixin.java:69
  - _why:_ Record-component breakage from the Forge->NeoForge ServerStatus shape change. Vanish-mixin dimension covered mixin targets/refmap, but this is a vanilla/Forge record API surface inside the mixin body, easy to miss.
- **ClientboundPlayerInfoUpdatePacket.createPlayerInitializing(List<ServerPlayer>) static factory. This helper's existence/signature is version-sensitive across the 1.20.1->1.21.1 chat/player-info packet rework; verify it still exists in 1.21.1 (it does in late NeoForge, but the Entry/actions enum and packet entries() shape changed).**
  - _file:line:_ src/main/java/com/enviouse/sef/vanish/VanishingHandler.java:63,66; src/main/java/com/enviouse/sef/vanish/mixin/ServerGamePacketListenerImplMixin.java:60
  - _why:_ Player-info packet API used for vanish hide/show. Partly inside vanish-mixin dimension (the mixin), but VanishingHandler is a plain handler sending these packets directly and is a separate surface to verify.
- **Mixed Holder<SoundEvent> vs raw SoundEvent access on SoundEvents constants. WarnCommand uses SoundEvents.NOTE_BLOCK_BELL.get() (Holder) while six other sites pass SoundEvents.EXPERIENCE_ORB_PICKUP/ANVIL_LAND directly (raw SoundEvent). 1.21.1 still exposes playNotifySound(SoundEvent,...) but the SoundEvents field types and any .get() usage must be re-verified; NOTE_BLOCK_BELL access via .get() is fragile.**
  - _file:line:_ src/main/java/com/enviouse/sef/warn/WarnCommand.java:142; src/main/java/com/enviouse/sef/chat/ChatReplyHandler.java:90; src/main/java/com/enviouse/sef/chat/AdminChatHandler.java:146,172,193; src/main/java/com/enviouse/sef/freeze/FreezeManager.java:104; src/main/java/com/enviouse/sef/commands/MsgCommands.java:227
  - _why:_ Sounds API surface. The .get() on NOTE_BLOCK_BELL implies a Holder there but raw elsewhere; inconsistency must be normalized for 1.21.1.
- **Forge config API: ForgeConfigSpec / ForgeConfigSpec.Builder / .ConfigValue / .BooleanValue and IConfigSpec<?> generic. NeoForge renames to ModConfigSpec (net.neoforged.neoforge.common.ModConfigSpec) and IConfigSpec is no longer generic (raw IConfigSpec). loader.MLConfig takes IConfigSpec<?>.**
  - _file:line:_ src/main/java/com/enviouse/sef/config/ConfigHandler.java:6,9,11,29-36; src/main/java/com/enviouse/sef/utils/loader.java:6,26
  - _why:_ Config dimension covered persistence/toml semantics; this is the type/class rename (ForgeConfigSpec->ModConfigSpec, IConfigSpec degenericization) that breaks every config field declaration plus the loader.MLConfig generic signature.


**Critic notes (11):**


- Source tree note: the actual Forge 1.20.1 source is at src/main/java/com/enviouse/sef (package 'sef', mod id 'sef'), 111 java files. The 'OLD1201Version/' path in the prompt does not exist; the new NeoForge skeleton uses package com.enviouse.sefported with templates/META-INF/neoforge.mods.toml. So the audit target is the 'sef' tree, ported INTO 'sefported'.
- ResourceLocation: ZERO occurrences of 'new ResourceLocation(' (the removed constructor). All code already uses the 1.21-safe statics: ResourceLocation.tryParse (BannedEntry.java:92,108). ForgeRegistries.getKey(...) returns ResourceLocation. So the specifically-hunted constructor break is NOT present — clean on that axis.
- Networking: CONFIRMED truly none. Zero SimpleChannel/ChannelBuilder/NetworkRegistry/CustomPacketPayload/PayloadRegistrar/registerMessage. Only NetworkConstants.IGNORESERVERONLY (the handshake marker in loader.java) and vanilla Clientbound* packets sent via player.connection.send(...) (action bar, title/subtitle, player-info, remove-entities, tab list). No custom payloads to migrate.
- Capabilities/LazyOptional: no Capability/LazyOptional/getCapability/ForgeCapabilities anywhere. IItemHandlerModifiable is used (Curios integration: CuriosInventoryHelper.java + invsee InvSeeContainer.java) via Curios' getStacks(), not via Forge capability resolution. NeoForge maps net.minecraftforge.items.IItemHandlerModifiable -> net.neoforged.neoforge.items.IItemHandlerModifiable (package rename only). The Curios attachment/capability acquisition itself is in the Curios-compat dimension.
- Item NBT/DataComponent: NO ItemStack NBT serialization to disk anywhere (no CompoundTag/ItemStack.of/save/getOrCreateTag in invsee, invlock, curios, or banned). BannedItems persists ResourceLocation STRINGS via Gson, not stacks. So the 1.20.5+ DataComponent/datafixer migration risk is LOW for persisted data. InvSee operates on live containers only.
- Client-only classes: NONE referenced from common code (no net.minecraft.client.*, Minecraft.getInstance, blaze3d). Dedicated-server safe on that axis. InvSee uses vanilla MenuType.GENERIC_9x6 + MenuProvider.openMenu (server-side container open), no Screen/MenuScreens registration.
- Reflection: Mc2DiscordCompat.java reflects by string name into fr.denisd3d.mc2discord internals (Mc2Discord.INSTANCE, hiddenPlayerList, HiddenPlayerEntry ctor, M2DUtils, MessageManager). Name-based so resilient to MC version, but the constructed HiddenPlayerEntry and config fields depend on the mc2discord build shipped for 1.21.1 - verify the target mod's 1.21.1 internal class names still match (Discord-bridge dimension). PermissionsHandler.java:182 reflects over its OWN declared fields (safe).
- Text/legacy formatting: ChatFormatting + Style.withStyle + TextColor.fromLegacyFormat (BitwiseStyling.java, TextFormatter.java) are all stable vanilla APIs in 1.21.1. No LegacyComponentSerializer/§ section-sign parsing. No Component.Serializer.toJson/fromJson (so no RegistryAccess-requiring text JSON serialization). Chat-formatting-internals dimension covers these.
- Threads/scheduler: no new Thread/Executors/ScheduledExecutor/CompletableFuture. Only player.server.execute(Runnable) (ChatEventHandler.java:221) which is unchanged in 1.21.1. TextAnnouncementCommand.java:78 'Executors' is just a code comment, not java.util.concurrent.
- Lang/assets: NO assets/sefported/lang (nor assets/sef/lang) directory exists at all - find over src for *assets* returned nothing. en_us.json exists only in the NEW skeleton tree per git status, not under the sef source. The mod is string/config-driven (no translation keys for its own content), so missing lang is expected; just ensure the new skeleton's assets/sefported/lang ships.
- CombatTracker mixin (CombatTrackerMixin.java:27,35,37) targets getFallMessage(CombatEntry,Entity) and DamageSource.getLocalizedDeathMessage - CombatEntry was reworked in 1.20.5/1.21 and these @Redirect string descriptors must be re-verified; this is inside the vanish-mixin dimension but the descriptor drift is worth re-confirming.


## Appendix C — ALL open questions, consolidated


_Every open question raised across all dimensions, flattened. Tag = dimension key._


- **(build-skeleton)** Is the exact Mojang field name on ClientboundPlayerInfoUpdatePacket literally 'entries' in 1.21.1 (AT line 5, old SRG f_244436_)? And is ChunkMap's field 'entityMap' (old f_140150_)? Must confirm against 1.21.1 official mappings before writing the AT.
- **(build-skeleton)** Does net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent still exist and fire on the MOD bus in NeoForge 1.21.1 (used at ServerEssentialsForge.java:82-90 to defer handler registration)?
- **(build-skeleton)** Is parchment_mappings_version=2025.12.20 an actually-published Parchment export for MC 1.21.1, or does it need correcting alongside the 1.21.11->1.21.1 fix?
- **(build-skeleton)** What is the correct pack_format for a 1.21.1 NeoForge mod's pack.mcmeta (or is pack.mcmeta superseded)?
- **(build-skeleton)** Exact NeoForge 1.21.1 artifact coordinates for FTB Essentials (2101.1.x) + FTB Library and Curios (curios-neoforge) and whether ModDev needs any remap directive for them as compileOnly mod jars.
- **(build-skeleton)** Should the project keep mod_id 'sef'/package com.enviouse.sef (lower churn, preserves config & permission namespaces) or adopt the skeleton's 'sefported' (110-file rename)? Needs a product decision before any code is moved.
- **(build-skeleton)** PermissionNode constructor signature and the bus PermissionGatherEvent.Nodes fires on in NeoForge 1.21.1 (appears directly in entrypoint at ServerEssentialsForge.java:98-111).
- **(registration-bus)** Does NeoForge 1.21.1 PermissionGatherEvent.Nodes fire on the GAME bus (NeoForge.EVENT_BUS) or the MOD bus? This determines whether the two NeoForge.EVENT_BUS.addListener calls (ServerEssentialsForge.java:79-80) and the instance handler PermissionsHandler.registerPermissionNodes stay on EVENT_BUS or move to the modEventBus.
- **(registration-bus)** Exact NeoForge 1.21.1 PermissionNode<T> constructor signature — does it still take (String modid, String nodeName, PermissionType, resolver), and does node.setInformation(Component, Component) still exist?
- **(registration-bus)** Does net.minecraft.core.Registry expose keySet() (1.21.1 Mojang mappings) as the replacement for ForgeRegistries.ITEMS.getKeys()? Confirm method name.
- **(registration-bus)** What is the IConfigSpec<?> -> NeoForge IConfigSpec parameter change in loader.MLConfig — should the helper accept ModConfigSpec directly to avoid the changed IConfigSpec generic? (config dimension to confirm).
- **(registration-bus)** Confirm BuiltInRegistries.BLOCK/ITEM.getKey returns minecraft:air (not null) for unregistered objects under 1.21.1 — affects whether existing 'rl != null' guards are dead but harmless.
- **(chat-events)** Exact NeoForge ServerChatEvent message read accessor for 1.21.1: is it getMessage() (returns Component) as used at ChatEventHandler.java:80, or getRawText()/another name? Only one call site, but it gates the whole handler.
- **(chat-events)** How is ChatEventHandler actually registered? It is annotated @EventBusSubscriber but onServerChat is a NON-STATIC instance method (NeoForge @EventBusSubscriber auto-registers STATIC handlers only). The class holds per-instance reloadable state (IReloadable), so an instance must be manually registered via NeoForge.EVENT_BUS.register(...). Need to confirm the registration site so it doesn't silently stop receiving events.
- **(chat-events)** Does MinecraftServer.getServerDirectory() return Path (1.21.1) vs File (1.20.1)? If Path, remove the .toPath() at OpBulletinHandler.java:33.
- **(chat-events)** Is there a dedicated logs/chat/ file writer anywhere (the prompt mentioned one)? Within this dimension only LOGGER.info('[CHAT]...') exists — confirm no separate chat-log appender lives in another dimension's class.
- **(chat-events)** Does the project intend to stay on 1.21.1 (not 1.21.5+)? ClickEvent/HoverEvent 2-arg constructors at ChatEventHandler.java:233-234 are fine for 1.21.1 but were reworked into records in 1.21.5 — would become breaking if the target slips.
- **(player-lifecycle-events)** FTB Essentials nickname accessors (FTBEPlayerData) and Curios slot APIs are referenced indirectly via providers (FTBNicknameProvider/LuckPermsProvider) NOT in this file set — confirm in the mod-deps dimension that FTB 2101.1.x still exposes the same nickname accessors used by ExternalModLoadingEvent.
- **(player-lifecycle-events)** Confirm SEFUtilities.getFormattedPlayerName(...) returns net.minecraft.network.chat.Component (the TabListNameFormat/NameFormat setters require a Component) and that TextFormatter.stringToFormattedText builds Components in a way compatible with 1.20.5+ component serialization changes — owned by the text/Component dimension.
- **(player-lifecycle-events)** Decide registration strategy for the @EventBusSubscriber classes: keep manual instance registration on NeoForge.EVENT_BUS (current behavior) vs. converting to static handlers so the top-level @EventBusSubscriber actually wires them. Current code's annotation is a no-op; mis-porting could silently drop all handlers.
- **(player-lifecycle-events)** Verify the deferred manual registration in loadComplete(FMLLoadCompleteEvent) still works under NeoForge timing (registering GAME-bus listeners from a MOD-bus FMLLoadCompleteEvent handler) — order-sensitive but not an API break.
- **(gameplay-event-handlers)** Exact NeoForge 21.1.x accessor names on ItemEntityPickupEvent.Pre: is it getPlayer()/getItemEntity() and is denial done via setCanReceiveStack(false) or a canPickup TriState? Need to confirm against the 21.1.233 source before rewriting banned L129-141 and invlock L39-46.
- **(gameplay-event-handlers)** Does ItemEntityPickupEvent split into Pre/Post mean the .discard() in BannedItemsEventHandler L139 should happen in Pre (after denying) or is there a guaranteed-fire point? Confirm discard() on the ItemEntity is still safe from Pre.
- **(gameplay-event-handlers)** BlockEvent.BreakEvent.getPlayer() return type in NeoForge 21.1.1 (Player vs ServerPlayer) — affects the instanceof pattern at db L25 / freeze L73 (works either way, but confirm).
- **(gameplay-event-handlers)** Confirm setCanceled(boolean) is exposed directly (not requiring a cast to ICancellableEvent) on EntityPlaceEvent/BreakEvent/PlayerInteractEvent/AttackEntityEvent/LivingEntityUseItemEvent/CommandEvent in 21.1.x.
- **(gameplay-event-handlers)** ProjectileImpactEvent, LivingChangeTargetEvent, and VanillaGameEvent were listed in the focus brief but DO NOT appear in any of these four files (grep confirmed zero hits) — confirm they belong to a different dimension/file set.
- **(commands)** Does NeoForge 21.1.x PermissionNode keep the 4-arg ctor (String modid, String node, PermissionType<T>, PermissionResolver<T>) and node.setInformation(Component, Component)? PermissionsHandler builds ~50 nodes and reflects over PermissionNode.class field type — any class-shape change cascades to every command's .requires().
- **(commands)** Does PermissionAPI.getOfflinePermission(UUID, PermissionNode<Boolean>) exist with the same signature/throwing behavior in NeoForge 21.1.x (used by every command's permission check via playerHasPermission)?
- **(commands)** On which bus does PermissionGatherEvent.Nodes fire in NeoForge 21.1.x (GAME vs MOD)? PermissionsHandler is @EventBusSubscriber (defaults to GAME) and ServerEssentialsForge also adds it via MinecraftForge.EVENT_BUS.addListener — confirm both land on the right bus after porting.
- **(commands)** Do EntityArgument.getEntities/getPlayers and EntitySelector.findSingleEntity/findSinglePlayer keep those exact Mojang names and the internal Collection.isEmpty()/List.isEmpty()/List.size() call sites that the vanish mixins inject before, in 1.21.1?
- **(commands)** Confirm RegisterCommandsEvent in NeoForge 21.1.x still supports priority-ordered listeners so the LOW-priority override of vanilla /msg/tell/w and FTB /invsee continues to run after their registrations.
- **(chat-formatting)** Confirm against the actual NeoForge 21.1.233 / MC 1.21.1 jar that ClickEvent(ClickEvent.Action, String) and HoverEvent(HoverEvent.Action, Component) are still plain constructors (not records). They are records only in 1.21.5+, so 1.21.1 should be fine, but the exact ctor arity should be eyeballed during the build.
- **(chat-formatting)** Confirm Style.withColor(int) overload still exists in 1.21.1 (the hex path at TextFormatter.java:92 relies on it). withColor(TextColor)/withColor(ChatFormatting) certainly exist; the int overload is the one to verify — trivially replaceable with withColor(TextColor.fromRgb(int)) if it were removed.
- **(chat-formatting)** ChatFormatting.getByName casing: getChatMessageColor() supplies the config string to ChatFormatting.getByName(TextFormatter.java:58). getByName lowercases internally and is unchanged, but the config value source is owned by the config dimension.
- **(permissions)** Exact NeoForge 1.21.1 PermissionNode<T> public constructor signature/arity: is it (String modId, String nodeName, PermissionType<T> type, PermissionResolver<T> resolver, PermissionDynamicContextKey<?>...) as in late Forge, or did NeoForge reorder/rename? This governs ~76 node constructions.
- **(permissions)** Is the resolver functional interface still PermissionNode.PermissionResolver<T> with resolve(@Nullable ServerPlayer, UUID, PermissionDynamicContext<?>...)? The lambdas (player,uuid,context)->value depend on this exact shape.
- **(permissions)** Does PermissionAPI.getOfflinePermission(UUID, PermissionNode) still exist under that name in NeoForge 21.1.x, or was it folded/renamed? It backs essentially every offline permission check in the mod (tab-list, color checks).
- **(permissions)** On which bus does net.neoforged.neoforge.server.permission.events.PermissionGatherEvent.Nodes fire under NeoForge 1.21.1 — the GAME (NeoForge.EVENT_BUS) bus? Both registration sites (@SubscribeEvent in PermissionsHandler, addListener in ServerEssentialsForge) must target the correct bus.
- **(permissions)** Is PermissionNode.setInformation(Component name, Component description) still present with that name/arity in NeoForge 21.1.x?
- **(permissions)** Does Nodes.addNodes(...) remain varargs PermissionNode<?>... (the reflection loop passes one node per call, and the per-color loop passes one at a time, so a List<PermissionNode<?>> overload would also work)?
- **(config-persistence)** Does ModConfigSpec in NeoForge 21.1.x still expose a public setConfig(CommentedConfig)/acceptConfig usable the way ConfigHandler.reloadFromDisk() pokes it (ConfigHandler.java:25)? NeoForge reworked the IConfigSpec contract; manual setConfig may no longer be the supported reload path.
- **(config-persistence)** Is com.google.gson on the NeoForge 1.21.1 runtime classpath without an explicit dependency? (Used by 7 managers.) If relying on a transitive lib, the build.gradle may need an explicit gson dep.
- **(config-persistence)** Is com.electronwill.nightconfig (CommentedFileConfig + WritingMode) still transitively provided by NeoForge 21.1.x so the import in ConfigHandler.java:3-4 resolves? (Highly likely yes, but verify the artifact coordinates.)
- **(config-persistence)** Does NeoForge's net.neoforged.fml.config.IConfigSpec still allow a 3-arg registerConfig(type, spec, customFileName) with a subdirectory filename ('sef/common.toml')? Need to confirm the custom-filename overload + subdir path are honored.
- **(config-persistence)** Confirm MinecraftServer.getServerDirectory() return type is Path (not File) in this exact NeoForge/MC 1.21.1 build (treated as ground truth for the 1.20.5+ change, but worth a one-line verify against the MC source).
- **(luckperms)** Confirm the actual LuckPerms-NeoForge-5.4.139/.140 runtime jar's modid string remains exactly "luckperms" on 1.21.1 (ModList.isLoaded uses this literal at ExternalModLoadingEvent:34 and PlayerEventHandler:107). Highly likely yes.
- **(luckperms)** Confirm CachedMetaData.getPrefixes()/getSuffixes() still return Map<Integer,String> (weighted) in api:5.4 as used at LuckPermsProvider.java:51-52 — standard but not introspected from the jar in this audit.
- **(luckperms)** Should runtimeOnly for the LuckPerms NeoForge jar (currently commented out, build.gradle:169) be wired for dev-runtime testing? Optional; not required for the optional-guard to work since it's compileOnly.
- **(luckperms)** ExternalModLoadingEvent:43 catches Exception, not Error — confirm NeoForge never throws a bare NoClassDefFoundError on the guarded path (it won't, because isLoaded gates it), so no need to widen the catch.
- **(ftb-curios-discord)** FTB Essentials 2101.1.x maven coordinate/group id and the required FTB Library version pin for compileOnly — confirm against the FTB maven (build.gradle currently uses ad-hoc group 'ftb-essentials:ftb-essentials-forge'). Does the NeoForge build still expose dev.ftb.mods.ftbessentials.util.FTBEPlayerData on the *classpath* the same way for compileOnly (it lives in the multiloader 'common' module)?
- **(ftb-curios-discord)** Should FTBNicknameProvider be re-typed to take a ServerPlayer instead of GameProfile (cleaner, matches getOrCreate(Player)), or keep GameProfile and resolve via getOrCreate(server, profile.getId())? The latter can return Optional.empty() for players not in the profile cache — verify INicknameProvider's contract tolerates a null nick (it does: IntegratedNicknameProvider/getPlayerChatName handle null).
- **(ftb-curios-discord)** Are the reflective mc2discord/sdlink field & method names (hiddenPlayerList, HiddenPlayersManager.hidePlayer(String,String,String), DiscordAuthor.setPlayerAvatar, etc.) still valid in the specific 1.21.1 builds of those Discord bridges? Not load-blocking (graceful no-op), but the vanish->Discord feature silently breaks if they changed.
- **(ftb-curios-discord)** Curios exact NeoForge artifact version for 1.21.1 to pin (9.0.x vs 9.2.x) — both expose the verified API; pick the one matching the target modpack.
- **(vanish-mixins-at)** Does ChunkMap still expose a field named 'entityMap' (Int2ObjectMap<TrackedEntity>) in 1.21.1 Mojang mappings, or was it renamed/relocated during ChunkMap refactors? (drives AT #1 + VanishingHandler.java:80-82)
- **(vanish-mixins-at)** Is ClientboundPlayerInfoUpdatePacket.entries still a non-final-able List<Entry> field that can be reassigned via 'public-f' AT, or is the record now immutable requiring a rebuild of the packet? (drives AT #3 + ServerGamePacketListenerImplMixin.java:65)
- **(vanish-mixins-at)** Confirm PlayerList.placeNewPlayer signature in 1.21.1 — does it carry the CommonListenerCookie 3rd param (added 1.20.2)? PlayerListMixin.java:28 handler must match.
- **(vanish-mixins-at)** Do ServerGamePacketListenerImpl.send(Packet) and send(Packet,PacketSendListener) both still exist as distinct overloads in 1.21.1, or has the listener-arg variant changed?
- **(vanish-mixins-at)** Confirm the MobEffect->Holder<MobEffect> migration affects LivingEntity.hasEffect/getEffect/updateInvisibilityStatus and getActiveEffectsMap() key type in 1.21.1 (drives the CRITICAL LivingEntityMixin rewrite).
- **(vanish-mixins-at)** Does NeoForge's ServerStatus record drop forgeData() entirely, and what replaces it (if anything) for MinecraftServerMixin.java:69 reconstruction?
- **(vanish-mixins-at)** Is Entity.broadcastToPlayer(ServerPlayer) still present with that name/signature in 1.21.1? (sound/EntityMixin.java:37)
- **(vanish-mixins-at)** Confirm ServerPlayer.sendChatMessage(OutgoingChatMessage,boolean,ChatType.Bound) and PlayerChatMessage.link().sender() / ChatType.bind overloads are unchanged in 1.21.1 chat-signing internals.
- **(vanish-mixins-at)** Was AbstractMinecart.tick (and its Level.getEntities call) preserved in 1.21.1 after the minecart movement refactor? (VanishEntitySelectorMixins.java:31)
- **(vanish-mixins-at)** Does the NeoForge PlayerEvent base still expose the (Player) protected ctor and which accessor (getEntity vs getPlayer) for PlayerVanishEvent?
- **(invsee-menu)** Does net.minecraft.server.level.ServerPlayer.hasDisconnected() still exist in 1.21.1, or was it renamed/replaced (e.g. connection-state check)? Used at InvSeeContainer.java:284.
- **(invsee-menu)** In Curios 1.21.1 NeoForge, does CuriosApi.getCuriosInventory(ServerPlayer) still return Optional<ICuriosItemHandler> (so .ifPresent works at CuriosInventoryHelper.java:52/93/130), or does it now return the handler directly/nullable?
- **(invsee-menu)** In Curios 1.21.1, does ICurioStacksHandler.getStacks() still return a type assignable to net.neoforged.neoforge.items.IItemHandlerModifiable (i.e. IDynamicStackHandler extends NeoForge IItemHandlerModifiable)?
- **(invsee-menu)** Does TextFormatter.stringToFormattedText(...) return net.minecraft.network.chat.Component compatible with DataComponents.CUSTOM_NAME (a Component value)? (cross-dimension: text/Component serialization)
- **(invsee-menu)** Is custom-name italic styling acceptable for the glass-pane labels, or should the replacement set withItalic(false) to match prior setHoverName visual?


