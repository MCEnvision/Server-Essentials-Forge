# phase 000 source boundary

## identity

The target is `/mnt/hermes/projects/Sef` at commit `1e8bab26d9d6ff6b1bf1d5ef41eb8d6c1a51ad98` for the approved Forge 1.20.1 baseline. The pinned read only reference is the Git object `e160a235b19c992b3a23c3a43754e92ad0147948` from `/mnt/hermes/projects/SEFPORTED`. The reference checkout currently has later worktree state and unrelated uncommitted files, so no checkout, reset, cleanup, or edit was performed there. All source observations in this phase use `git show` against the pinned object.

The target uses Minecraft 1.20.1, Forge 47.3.12, official 1.20.1 mappings, Java 17, and mod version 1.1. The reference declares the expanded SEF source surface at the pinned revision. Static identity and source fingerprints are evidence only. No runtime compatibility, signed chat, player forwarding, backend switching, or production behavior is claimed by this task.

## pinned source fingerprints

| pinned reference path | sha256 |
| --- | --- |
| `src/main/java/com/enviouse/sef/kernel/KernelServices.java` | `11346487b8a1ce941364412f6f1fe378ce24cd309c73d34ee744a5ce75c44ca4` |
| `src/main/java/com/enviouse/sef/config/modules/ModuleConfigRegistry.java` | `216de82e8c90d1e2c6b22cb08a7739bd0eb06516f4eb31af0581d7091d9b0838` |
| `src/main/java/com/enviouse/sef/config/modules/ModuleConfigService.java` | `8f0476c1b6c6cc8501eb9350e1ffb101ca3664da9c5654b32c1b0730f66b249c` |
| `src/main/java/com/enviouse/sef/teleport/HomeRecord.java` | `3a92bb0e4ac531f3e4018559f368adb4cd82ce03f5f085c85c3a06c25c2e6200` |
| `src/main/java/com/enviouse/sef/teleport/SavedLocation.java` | `c6238f9ee02aa13892f7ed608020a7fc20ae27f680d1567f250e0d531093f8f2` |
| `src/main/java/com/enviouse/sef/teleport/SafeTeleportService.java` | `561421f4101de6d9e4df94054c61d950f0218b052e836b09f8d64d071425670c` |
| `src/main/java/com/enviouse/sef/events/ChatEventHandler.java` | `0a37601661cc500d866055e2739a71a0adb70e0c5dacf32d5c8c6441b1c8e9bc` |
| `src/main/java/com/enviouse/sef/vanish/VanishUtil.java` | `d1aeaed95ae94d6205562fa9f4b682b34c266d654d3babb2432c324d0369882b` |
| `src/main/java/com/enviouse/sef/vanish/mixin/chat/CommandSourceStackMixin.java` | `9b45944afed26a81f14e3d6f121400bd2070fa410386610ca9e823bccd9982de` |
| `src/main/java/com/enviouse/sef/vanish/mixin/chat/EntitySelectorMixin.java` | `1c5c1a2ce272cc4aee5b61dab2fdef4f90b16d0191084c39e27830f42e3c87d0` |
| `src/main/java/com/enviouse/sef/ServerEssentialsForge.java` | `d5a6aab5581a43b0a5636db83887901035ffe188d2e99ab95a69822d7ae8637d` |
| `src/main/java/com/enviouse/sef/inventory/InventoryUtilityCommands.java` | `92a6193e9e6af297f9564a76a03cc22e2c6d9110166c13e0d0743b15f7a281a0` |
| `src/main/java/com/enviouse/sef/recovery/ItemStackSnapshotCodec.java` | `c7adf71f7f3d6951ab0df2439b24c472eef3081117e5a9402df222e4c0f133fb` |
| `src/main/java/com/enviouse/sef/gui/protocol/SefNetwork.java` | `583957793cc02a2d1e79e22567a2822dc596d8874c9a0a8f0b37fd1618792a1d` |
| `gradle.properties` | `08f497d6359ca6f3916a3044db719a6579e64331d6a655fad0717b6bc3746c9e` |

Target baseline fingerprints used for comparison are `gradle.properties` `8b08b24b5923ec4c95b1ee021741f51180591ee548cb89eabe1e16844483e49a`, `src/main/java/com/enviouse/sef/events/ChatEventHandler.java` `429d15ed8e6da35cb364ff6307a147ef67205b04574ec8686f9460f1b6ed314c`, and `src/main/java/com/enviouse/sef/vanish/VanishCommand.java` `18791ec17f7349d208ed1734e03510382bfb6ad5c92ac2fee25cc0c4fd532c25`.

## owner boundary

The inventory applies the settled owner decisions. Economy is removed transitively, including accounts, balances, transfers, shops and signs, providers and importers, command costs, configuration, permissions, resources, and tests. Custom client interfaces, screens, HUD, Fancy Tags, client rendering, GUI protocol and menu dependent utilities are removed. No SEF client installation is required. Retained server behavior must use native Forge events, server commands, durable storage, and rich vanilla chat. Disguise is adapted only if its existing server only proxy path is proven; enhanced projection and rendering remain excluded.

## bounded discovery

The pinned reference contains 382 Java source files, 168 Java test files, 738 generated command action rows, 62 configuration module rows, and the generated command and configuration references used for this inventory. Each action and module row is assigned one disposition. Command rows retain their generated reference locator, shared registry boundary, feature gate, permission metadata, and explicit static evidence limit. Module rows retain their configuration reference locator and dependency closure. Exact action specific test locators are recorded when the bounded pinned test scan finds them; otherwise the row explicitly records that no exact locator was found.

The generated command registry is not treated as runtime proof. A row marked registered means the pinned reference exposes a concrete registry and dispatcher action; it does not prove the Forge 1.20.1 port or real behavior. Later phases must add adapter tests, GameTests, dedicated runtime evidence, and client or proxy evidence where required. Catalog names without a registered action would be classified as unimplemented future scope, but the pinned generated references supplied no unclassified row after the second catalog and module pass.

## dispositions

| disposition | meaning |
| --- | --- |
| retained | implemented server action eligible for the backport, subject to later adapter and runtime proof |
| adapted | server only path requires a dedicated compatibility and security proof before retention |
| economy removal | monetary action or transitive economy dependency removed by owner decision |
| interface removal | custom client, HUD, Fancy Tags, GUI protocol, or menu dependent action removed by owner decision |
| unimplemented future scope | no executable handler or registration proof at the pinned revision |

## evidence limits

This task owns `SEF-REQ-002` and `P000-TASK-001` only. It produces source inventory and boundary evidence. It does not start external fixture processes, access databases, launch clients, mutate `/mnt/hermes/projects/SEFPORTED`, create a bridge, or close `SEF-REQ-010`. The next task must use this inventory to choose a representative existing command before any external feasibility claim.

See the split action inventory files in this directory for every pinned command row and the configuration module inventory.
