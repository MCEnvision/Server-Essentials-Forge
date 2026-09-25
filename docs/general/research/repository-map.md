# Repository Map

## Local component map and source register

Reference revision is `e160a235b19c992b3a23c3a43754e92ad0147948` under `/mnt/hermes/projects/SEFPORTED`. Target baseline revision is `1e8bab26d9d6ff6b1bf1d5ef41eb8d6c1a51ad98` under `/mnt/hermes/projects/Sef`. CodeGraph reported both indexes current. It located kernel, teleport, chat and vanish relationships, but repeated targeted queries omitted records and returned unrelated broad method matches. Bounded direct reads filled the specific omitted record, configuration registry, mixin and API import gaps. No static source finding below is runtime verification.

| ID | Reference source locator | OBSERVED finding and implication |
| --- | --- | --- |
| SRC-201 | `src/main/java/com/enviouse/sef/kernel/KernelServices.java:117`, `:179`, `:205`, `:338`, `:347`, `:470`, `:1100` | Economy construction, cost adapters, storage registration, command cost schedule and GUI descriptors are kernel dependencies. Removing only economy commands leaves live services and configuration behind. Preserve ordinary permission, cooldown and confirmation policies while removing monetary policy entirely. |
| SRC-202 | `src/main/java/com/enviouse/sef/config/modules/ModuleConfigRegistry.java:21`, `:213`, `:603` and `ModuleConfigService.java:65` | GUI/economy/HUD/Fancy Tags module declarations exist; many retained modules declare GUI dependencies and generic GUI keys. Removal must rebuild dependencies and generated config, not merely disable modules. Preserve legacy files without reading their monetary values into active state or deleting owner data. |
| SRC-203 | `src/main/java/com/enviouse/sef/teleport/HomeRecord.java:9`, `SavedLocation.java:10`, `SafeTeleportService.java:49` | Home ownership, revision and tombstone exist, but location has dimension plus coordinates and rotation only. There is no server identity. Safe teleport validates local dimensions, loaded chunks, claims, hazards and bounds. A remote home cannot be resolved by sending the existing local record to another server. |
| SRC-204 | `src/main/java/com/enviouse/sef/events/ChatEventHandler.java:70`, `:111`, `:136` | Existing backend chat flow already checks mute and cancels events. Network moderation should feed one authoritative enforcement decision rather than stack contradictory local and network mute managers. Command aliases and third-party chat paths still require coverage. |
| SRC-205 | `src/main/java/com/enviouse/sef/vanish/VanishUtil.java:138`, `:194`, `:209`, `:229` | Visibility depends on observer permissions; current persistence is player NBT and runtime maps. Network revisioned vanish state must become the authority in network mode, with reconnect application before public visibility. |
| SRC-206 | `src/main/java/com/enviouse/sef/vanish/mixin/chat/CommandSourceStackMixin.java:33`, `EntitySelectorMixin.java:20` | Existing hooks filter vanilla online name suggestions and selector targeting. Every copied mixin needs Forge 1.20.1 descriptor validation. Proxy suggestions and SEF own target lists need the same visibility predicate. |
| SRC-207 | `src/main/java/com/enviouse/sef/ServerEssentialsForge.java:73`, `gui/protocol/SefNetwork.java:7`, `inventory/InventoryUtilityCommands.java:19`, `recovery/ItemStackSnapshotCodec.java:26` | NeoForge payload registration, custom configuration tasks, 1.21 data components and registry-aware snapshot serialization cannot be copied unchanged. Remove client protocol entirely; independently adapt retained item and recovery paths to 1.20.1 NBT and Forge events. |
| SRC-208 | Target `src/main/java/com/enviouse/sef/vanish/VanishCommand.java:38`, `events/ChatEventHandler.java:34`, `gradle.properties` | Existing target gives Forge architecture and existing vanish command examples, not parity with the richer source port. Source inventory must classify every feature as retained, removed by owner, unavailable, or requiring port work. |

Owner clarification supplied by the owner removes all custom client interfaces, rendering, Fancy Tags and menu-dependent interfaces. Retained gameplay must not depend on client payload negotiation, custom screens, HUD or alternative menu invention. Vanilla client compatibility still depends on the rest of the operator's modpack. Disguise backend behavior and inventory/workstation menu commands require explicit inventory classification, not an assumption that all code under a feature package survives.

PROPOSED scope inventory rule: Backport existing implemented source behavior after owner exclusions. Do not implement unfinished SEFPORTED roadmap features merely because catalog entries, integration registries or documentation name them. A feature family is not proof every action in it is implemented. Freeze an action-level retained/removed/unavailable inventory before porting.

| Disposition | Families and coupling exceptions |
| --- | --- |
| Remove | Economy accounts, money transfers, balances, shops/signs, providers/importers, command monetary costs and economy configuration. GUI screens, protocol, session negotiation, custom HUD, Fancy Tags, GUI preferences/admin panels and menu-dependent interfaces. Remove their catalog, command, config, permission, resource and test registrations too. |
| Retain implemented server behavior | Chat formatting and moderation, bans, mutes, warnings, freeze, building restrictions, item restrictions, command logging and audit, identity and permissions, ordinary command policy, announcements and MOTD, tab text, social and mail commands, homes/warps/teleports, command-based kits/player/item utilities, configuration and durable storage, recovery and administrative commands where existing implementations are complete and do not depend on excluded menus. |
| Review action by action | Inventory inspection, ender chest and workstation opening commands depend on menus and are excluded under the resolved interface-removal boundary; unrelated item command functionality may remain. Disguise source has both enhanced projection and an existing vanilla proxy path (`DisguiseProxyService.java:215`), but enhanced rendering must go. Retain a preexisting server-only path only if explicitly classified as compatible with the removal boundary and verified. Never invent replacement disguise rendering or an alternative interface. Advanced server controls with unavailable provider/action handlers remain unavailable. |

The last row is an inventory requirement, not authorization to promote ambiguous features. The broad family list came from a bounded source path listing, not exhaustive execution of each family.

## Local dependency fingerprints

All reference paths below are relative to `/mnt/hermes/projects/SEFPORTED` at the revision above. These hashes identify relevant observation inputs; they are not goal locks or authoring authority.

```text
11346487b8a1ce941364412f6f1fe378ce24cd309c73d34ee744a5ce75c44ca4 src/main/java/com/enviouse/sef/kernel/KernelServices.java
216de82e8c90d1e2c6b22cb08a7739bd0eb06516f4eb31af0581d7091d9b0838 src/main/java/com/enviouse/sef/config/modules/ModuleConfigRegistry.java
8f0476c1b6c6cc8501eb9350e1ffb101ca3664da9c5654b32c1b0730f66b249c src/main/java/com/enviouse/sef/config/modules/ModuleConfigService.java
3a92bb0e4ac531f3e4018559f368adb4cd82ce03f5f085c85c3a06c25c2e6200 src/main/java/com/enviouse/sef/teleport/HomeRecord.java
c6238f9ee02aa13892f7ed608020a7fc20ae27f680d1567f250e0d531093f8f2 src/main/java/com/enviouse/sef/teleport/SavedLocation.java
561421f4101de6d9e4df94054c61d950f0218b052e836b09f8d64d071425670c src/main/java/com/enviouse/sef/teleport/SafeTeleportService.java
0a37601661cc500d866055e2739a71a0adb70e0c5dacf32d5c8c6441b1c8e9bc src/main/java/com/enviouse/sef/events/ChatEventHandler.java
d1aeaed95ae94d6205562fa9f4b682b34c266d654d3babb2432c324d0369882b src/main/java/com/enviouse/sef/vanish/VanishUtil.java
9b45944afed26a81f14e3d6f121400bd2070fa410386610ca9e823bccd9982de src/main/java/com/enviouse/sef/vanish/mixin/chat/CommandSourceStackMixin.java
1c5c1a2ce272cc4aee5b61dab2fdef4f90b16d0191084c39e27830f42e3c87d0 src/main/java/com/enviouse/sef/vanish/mixin/chat/EntitySelectorMixin.java
d5a6aab5581a43b0a5636db83887901035ffe188d2e99ab95a69822d7ae8637d src/main/java/com/enviouse/sef/ServerEssentialsForge.java
92a6193e9e6af297f9564a76a03cc22e2c6d9110166c13e0d0743b15f7a281a0 src/main/java/com/enviouse/sef/inventory/InventoryUtilityCommands.java
c7adf71f7f3d6951ab0df2439b24c472eef3081117e5a9402df222e4c0f133fb src/main/java/com/enviouse/sef/recovery/ItemStackSnapshotCodec.java
583957793cc02a2d1e79e22567a2822dc596d8874c9a0a8f0b37fd1618792a1d src/main/java/com/enviouse/sef/gui/protocol/SefNetwork.java
08f497d6359ca6f3916a3044db719a6579e64331d6a655fad0717b6bc3746c9e gradle.properties
```

Target fingerprints relative to `/mnt/hermes/projects/Sef`:

```text
8b08b24b5923ec4c95b1ee021741f51180591ee548cb89eabe1e16844483e49a gradle.properties
429d15ed8e6da35cb364ff6307a147ef67205b04574ec8686f9460f1b6ed314c src/main/java/com/enviouse/sef/events/ChatEventHandler.java
18791ec17f7349d208ed1734e03510382bfb6ad5c92ac2fee25cc0c4fd532c25 src/main/java/com/enviouse/sef/vanish/VanishCommand.java
```

## Expanded RTP Evidence

CodeGraph identified `CoreTeleportCommands.randomTeleport` at lines 370 through 439 as radius-based RNG with bounded loaded-chunk checks, not hierarchical allocation. Source SHA256 is `41bc31fe7d9fd325b748838a23f6256d6aaf131ae0c4b7185911e9496296d659`. `SafeTeleportService` remains the safety dependency, SHA256 `561421f4101de6d9e4df94054c61d950f0218b052e836b09f8d64d071425670c`. The graph reported 29 callers and TeleportGameTests; it did not report direct RTP coverage, which does not prove tests are absent. A broad query returned unrelated support, and an oversized class query required a targeted method query plus a bounded filename search. No stale-index warning appeared. The complete new grid, concurrency, recovery, routing and proof design is in [expanded observations](sources/expanded-observations.md).
