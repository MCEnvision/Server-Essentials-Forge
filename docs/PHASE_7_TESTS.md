# Phase 7 inventory and player-utility verification

## Scope

This matrix covers additional virtual workstations, hardened InvSee and ender-chest menus, disposal, kits, inventory utilities, item editing, books, recipe lookup, player-state utilities, gamemode shortcuts, the bounded self-only item shortcut, shortcut ownership, and super-enchanting hardening.

Phase 7 remains server authoritative and server only. Every menu uses a vanilla menu type. Vanilla clients and clients without SEF can join and use every enabled Phase 7 feature.

## Automated gate

Run with Java 21:

```bash
./gradlew test
./gradlew runGameTestServer
./gradlew build
```

Automated tests protect:

1. Phase 7 Brigadier root registration and denial without permissions.
2. Representative kit, inventory, item, player-utility, gamemode, and workstation grammar.
3. `/i` rejection above `itemGiveMaximumAmount`.
4. Kit timestamp, cooldown, policy, and claim-history persistence.
5. Hard bounds on per-player kit-use records.
6. Dynamic per-kit permission id validation.
7. InvSee downgrade after modify revocation and closure after view revocation.
8. Ender-chest closure after permission revocation or configuration revision change.
9. Phase 7 catalog ownership and shortcut-to-canonical ownership.

The GameTest server must pass every registered world fixture. Authenticated transaction, menu, client presentation, optional-mod, and super-enchant rows below remain required.

## Dedicated-server and vanilla-client smoke test

1. Start without LuckPerms, Curios, FTB Essentials, or an SEF client.
2. Verify no `net.minecraft.client` class loads from common initialization.
3. Join with a vanilla 1.21.1 client.
4. Open every enabled workstation, disposal, InvSee, and ender-chest menu.
5. Verify `kits.json` reports a writable state.
6. Run `/sef doctor` and `/sef storage status`.
7. Stop normally, restart, and verify kit definitions and use history.
8. Repeat with LuckPerms and Curios when test artifacts are available.

## Permission, collision, and source matrix

1. Verify every command root is absent without its permission.
2. Grant only self permissions and verify other-player arguments remain unavailable.
3. Grant other-player permissions and test hierarchy, exemption, vanish visibility, and console behavior.
4. Revoke permissions after command-tree delivery and immediately before execution.
5. Install a test mod that owns `/ci`, `/ec`, `/i`, `/wb`, `/gm`, `/gmc`, `/gms`, `/gmsp`, `/gma`, or `/v`.
6. Verify shortcut conflict policy chooses the documented owner and the canonical SEF route remains available.
7. Confirm a shortcut never weakens canonical feature, permission, cooldown, target, or audit policy.
8. Attempt ambiguous nickname targets and oversized selectors. No partial mutation may occur.

## Kit matrix

1. Fill an inventory with representative vanilla items, damage, enchantments, custom names, lore, books, containers, and data components.
2. Create a kit from the inventory.
3. Show, validate, export metadata, edit policy, and delete the kit.
4. Restart and verify complete item-state preservation.
5. Claim with enough capacity and verify every stack exactly once.
6. Claim without enough capacity while `kitDropOverflow` is false. Inventory and claim history must remain unchanged.
7. Enable bounded overflow dropping and verify only the remainder is dropped.
8. Configure cooldown and one-time policy separately and together.
9. Restart during cooldown and verify the same expiry.
10. Reset one player's use without changing another player's state.
11. Enable per-kit permission and verify `sef.kits.<id>` is required.
12. Remove the dynamic permission immediately before claim.
13. Attempt invalid ids, excessive definitions, excessive stacks, corrupt serialized items, missing registry ids, and a future schema.
14. Confirm recovery mode rejects mutation and preserves the damaged source.

## Inventory and live-menu matrix

1. Clear self inventory through `/clearinventory` and `/ci`.
2. Clear an eligible other player only with the other-player permission.
3. Open self and other-player ender chests through `/enderchest` and `/ec`.
4. Revoke view permission with the menu open. The next interaction must close it before mutation.
5. Reload configuration with the menu open. The stale revision must close.
6. Open InvSee read only, then grant and revoke modify permission.
7. Verify downgrade occurs before a click can mutate the target.
8. Revoke view and verify closure.
9. Test target disconnect, death, dimension change, respawn, and reconnect.
10. Test Curios absent and present.
11. Open `/disposal`, move items into it, close it, and verify intentional destruction without duplication or persistence.
12. Force menu closure during server stop and verify no item return or duplicate path violates the documented disposal behavior.

## Item utility matrix

1. Use `/more` on stackable, unstackable, empty-hand, damaged, and component-bearing items.
2. Use `/condense` with valid reversible recipes, mixed inventory, no recipe, and insufficient result capacity.
3. Verify a failed condensation leaves the original inventory unchanged.
4. Use `/hat` with an empty head slot and with existing armor. Verify a safe swap.
5. Set, replace, clear, and bound item names.
6. Add, set, remove, clear, and bound lore lines.
7. Inspect vanilla and namespaced items with `/itemdb`.
8. Edit writable and written books within page and length bounds.
9. Attempt control characters, excessive pages, excessive lines, invalid components, and nonbook input.
10. Query recipes for known and unknown items without mutating inventory.

## Item shortcut matrix

1. Run `/i cobblestone 64`.
2. Run `/i minecraft:cobblestone 64`.
3. Verify both resolve to the same item and canonical action.
4. Omit amount and verify the documented default.
5. Attempt zero, negative, nonnumeric, and `itemGiveMaximumAmount + 1`.
6. Attempt a missing registry id.
7. Fill inventory and verify a failed grant rolls back.
8. Attempt to append a player or selector argument. `/i` must remain self only.
9. Run from console and command block. Player-only source policy must refuse it.
10. Revoke permission after suggestions and immediately before mutation.

## Player utility matrix

1. Toggle AFK and verify logout cleanup.
2. Feed, heal, and rest self and eligible targets.
3. Toggle fly and god mode.
4. Revoke permission while active and verify state is removed by reconciliation.
5. Set walk and fly speeds at minimum, maximum, and outside bounds.
6. Add, set, and inspect experience without numeric overflow.
7. Set, reset, and query personal time and weather.
8. Revoke their permissions and verify personal overrides clear.
9. Test near, getpos, compass, depth, top, bottom, and jump in each vanilla dimension.
10. Verify vanished targets and protected targets remain unavailable.
11. Test dangerous top or bottom destinations. Shared destination safety must reject hazards and unloaded work beyond the budget.
12. Keep suicide disabled by default. When deliberately enabled, verify it is self only and separately permissioned.

## Gamemode matrix

1. Use `/gm creative`, `/gm survival`, `/gm spectator`, and `/gm adventure` on self.
2. Use `/gmc`, `/gms`, `/gmsp`, and `/gma` on self.
3. Use each route with one eligible explicit player.
4. Verify self permission does not grant other-player access.
5. Test hierarchy, exemption, vanished target, ambiguous nickname, and console target behavior.
6. Confirm aliases and `/gm` use the same canonical audit and target policy.
7. Revoke permission immediately before mutation.

## Workstation and super-enchant matrix

1. Open cartography table, grindstone, loom, smithing table, stonecutter, workbench, and `/wb`.
2. Verify every menu renders as its vanilla equivalent on a vanilla client.
3. Exercise normal recipes and item return behavior.
4. Verify canonical `/sef workstation` routes and shortcuts share cooldown state.
5. Disable one workstation and verify its mutation path is unavailable.
6. Open super enchanting with no item, an invalid item, and a valid enchantable item.
7. Test configured minimum and maximum enchant levels.
8. Test safe and explicitly unsafe policy separately.
9. Remove or replace an enchantment registry entry in a controlled fixture and verify fail-closed handling.
10. Change configuration or permission while the menu is open and verify stale mutation refusal.
11. Attempt incompatible, treasure, curse, over-limit, and missing enchantments according to the configured policy.
12. Inspect the final item for duplicate enchantments, invalid levels, lost components, or client and server disagreement.

## Performance and recovery

1. Populate the maximum kit definition and use-history limits.
2. Profile claim validation with maximum allowed kit size.
3. Repeat rapid live-menu clicks while revoking permission.
4. Verify player-state reconciliation performs no filesystem work.
5. Corrupt `kits.json`, restart, and verify quarantine or recovery without overwrite.
6. Test normal shutdown and forced process termination with dirty kit state.
7. Verify the next periodic or shutdown snapshot preserves a mutation made during an earlier asynchronous write.

## Completion record

Automated and headless results must be recorded here after the final source commit, including:

1. Commit.
2. Artifact path and SHA-256.
3. Java, Minecraft, and NeoForge versions.
4. Optional integration set.
5. Configuration used.
6. Unit-test count and result.
7. GameTest count and result.
8. Build result.
9. Dedicated-server and vanilla-client result.
10. Diagnostics output summary.
11. JAR and dependency inspection.
12. Remaining manual rows.

Do not approve a public release while a required row is untested or failing.
