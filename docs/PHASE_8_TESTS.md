# Phase 8 economy and sign verification

Final status, 2026-07-27: Complete. This file preserves earlier verification history. Pending or blocked labels below describe the earlier run and are superseded by the final matrix in `SEF2_ACCEPTANCE.md`.

## Scope

This matrix covers economy provider ownership, exact money parsing, native accounts and ledger persistence, payments, administration, worth and sales, command-cost reservation, import once, and all twelve server-authoritative economy sign types.

Phase 8 remains server only. Economy signs use vanilla sign interaction and vanilla menus. Vanilla 1.21.1 clients can join and use every enabled Phase 8 route.

## Automated gate

Run with Java 21:

```bash
./gradlew test
./gradlew runGameTestServer
./gradlew build
```

Automated tests protect:

1. Exact decimal parsing, formatting, precision rejection, exponent rejection, negative-value policy, and overflow bounds.
2. Idempotent native mutation and mismatched-key rejection.
3. Transfer, set, reset, signed-value persistence, account freeze, and ledger reload.
4. Crash recovery of uncommitted native command-cost holds.
5. Cached balance-top snapshots and invalidation after balance mutation.
6. External provider absence, provider failure without shadow accounts, import-adapter requirements, and import-once ownership transition.
7. Pre-import persistence, timestamped backup creation, aggregate report creation, and second-import rejection.
8. Fixed, per-use, per-target, per-distance, and per-item command-cost quotes, including hard maximum rejection.
9. Native synchronous reservation boundaries, external crash-safe reservation contracts, and cost bypass without changing configured policy cost.
10. Brigadier roots, aliases, per-subcommand permissions, and payment and adjustment confirmation literals.
11. Strict economy-sign parsing and invalid syntax rejection.
12. Sign-side persistence, creator UUID, fingerprint, revision, adoption, and removal behavior.
13. Component-safe buy, sell, trade, and free inventory transactions.
14. Provider failure and full-inventory GameTest rollback without item or value loss.

## Provider ownership matrix

1. Start in `native` mode without optional integrations.
2. Create accounts, restart, and verify balances, freezes, preferences, worth, and history.
3. Register one healthy external adapter and start in `external` mode.
4. Verify no native account is created by balance, pay, administration, cost, or sign actions.
5. Remove or fail the configured adapter. Economy-dependent startup must fail clearly.
6. Configure `disabled` while economy modules are disabled. No economy route or charge may mutate state.
7. Configure `import_once` without an importer. Startup must fail.
8. Configure an importer and verify no active economy exists before a successful import.
9. Complete the import and verify native becomes the only owner.
10. Attempt a second import or later external synchronization. Both must fail.

## Permission and target matrix

1. Verify every player, administrative, sign-use, sign-create, sign-manage, hierarchy-bypass, exemption-bypass, payment-bypass, and cost-bypass permission independently.
2. Grant self balance without other-balance and verify explicit player syntax is unavailable.
3. Test online, known offline, unknown, ambiguous nickname, vanished, exempt, higher-rank, equal-rank, and self targets.
4. Revoke permission after command-tree delivery and immediately before execution.
5. Verify console and RCON can use only routes whose source and permission policy allow them.
6. Verify command blocks do not gain administrative economy access from vanilla operator level.
7. Confirm `/eco sign adopt` applies target hierarchy and exemption policy.

## Money and ledger matrix

1. Test zero, minimum, maximum, one unit over maximum, excessive fractional precision, exponent notation, leading sign, whitespace, and nonnumeric input.
2. Verify formatting never participates in arithmetic.
3. Replay an identical idempotency key and verify one balance change and one ledger transaction.
4. Replay the key with a different account, amount, reason, currency, or metadata and verify conflict.
5. Fill account, ledger, pending-cost, worth, and transaction caps.
6. Verify a rejected mutation changes no account, ledger, revision, cache, or pending hold.
7. Restart after deposit, withdrawal, transfer, set, reset, freeze, and unfreeze.
8. Corrupt the repository and verify recovery mode refuses mutation without overwriting evidence.

## Payment and administration matrix

1. Pay an online eligible player.
2. Pay an unambiguous known offline player with configuration and permission enabled.
3. Disable incoming payments, ignore the sender, and remove offline-payment permission.
4. Test the corresponding separately permissioned bypasses.
5. Attempt self pay with its default denial and with deliberate enablement.
6. Cross `confirmationThreshold`, then change target or amount before `confirm`.
7. Wait beyond 30 seconds and verify expiry.
8. Fill confirmation capacity and verify bounded refusal.
9. Run give, take, and set below and above the threshold.
10. Run reset without and with its exact confirmation.
11. Freeze an account and verify ordinary transfer and cost withdrawal fail.
12. Unfreeze it and verify normal use resumes.
13. Inspect paged history and verify transaction actor, reason, exact amount, and resulting balance.

## Worth and inventory matrix

1. Set and remove worth for namespaced vanilla items.
2. Check worth for hand, explicit item, and inventory.
3. Sell a plain stack by hand, item, and full inventory.
4. Attempt damaged, named, enchanted, container-bearing, written-book, and otherwise component-bearing stacks.
5. Change inventory after quote and before commit.
6. Fill the recipient balance to its maximum.
7. Force provider failure after validation.
8. Verify every failed sale restores exact slots, counts, and components and credits nothing.
9. Verify successful inventory sale removes each eligible item once and appends one exact credit.

## Command-cost matrix

1. Configure each component separately and then combine all five for one action.
2. Verify self actions count as one target and fan-out actions use the bounded target count.
3. Verify same-dimension teleports use ceiling block distance.
4. Verify item quantity uses validated amount metadata.
5. Verify a calculated quote above `maximumTransaction` fails before reservation.
6. Verify `/sef commands` displays the configured components.
7. Start a warmup and cancel through movement, damage, logout, destination change, permission loss, and feature disablement. Default policy must refund.
8. Force action failure after reservation and verify refund plus cooldown release.
9. Kill the process with an uncommitted hold and restart. Startup must refund it once.
10. Grant cost bypass and verify no hold or ledger cost appears.
11. Inspect security audit context for exact amount, reservation UUID, and native cost transaction UUID.

## Economy-sign matrix

1. Test front and back sign sides at the same position.
2. Create and use each of `balance`, `buy`, `sell`, `trade`, `free`, `disposal`, `kit`, `heal`, `repair`, `time`, `weather`, and `warp`.
3. Test type disabled, create denied, use denied, owner mismatch, owner bypass, and expired placement claim.
4. Edit every line and verify fingerprint invalidation before use.
5. Break, replace, piston-move where supported, and explode a registered sign.
6. Unload the chunk during interaction and verify no deferred mutation survives.
7. Test invalid item ids, invalid options, control characters, excessive quantity, excessive value, zero and negative disallowed values, and extra lines.
8. Fill inventory before buy, trade, and free.
9. Remove required items before sell and trade.
10. Exhaust balance before buy, heal, repair, time, weather, kit, and warp.
11. Force provider failure and linked-command failure.
12. Verify deterministic restoration of inventory and balance for every failed path.
13. Use `/eco sign list`, `info`, `remove <id> confirm`, and `adopt`, then restart.
14. Confirm disposal intentionally destroys contents only after the player places them in its vanilla menu.

## Import-once and recovery matrix

1. Populate an external fixture with exact minimum, maximum, and ordinary balances.
2. Compare preview account count and total with export.
3. Change export after preview and verify rejection.
4. Include duplicate UUIDs, invalid balances, excess accounts, and arithmetic overflow.
5. Verify the current empty native store is synchronously persisted before import.
6. Verify a `preimport` backup exists before account mutation.
7. Force report creation failure and economy persistence failure separately.
8. Verify failed commit removes the incomplete report, restores empty in-memory accounts, and leaves the backup intact.
9. Verify success writes one aggregate report and one native import record.
10. Restart and confirm native ownership, balances, ledger, and report hash.
11. Restore the backup on a stopped staging server and verify the documented recovery procedure.

## Performance and concurrency

1. Populate 100,000 accounts and profile first and repeated balance-top reads.
2. Verify repeated reads do not sort the live account map until balance revision changes.
3. Run concurrent idempotent adapter calls against the provider conformance harness.
4. Exercise the maximum pending-cost and sign capacities.
5. Rapidly interact with one sign from multiple players and verify serialized server-authoritative outcomes.
6. Profile sign interaction, cost quote, account mutation, and audit recording for tick-thread stalls.
7. Run periodic flush during payments, sales, sign transactions, and import preview.
8. Stop normally with dirty economy and sign state, then repeat with forced termination.

## Completion record

Automated and headless verification completed on 2026-07-26 against source checkpoint `5b63bd728b81a19e6309953565608c732d258dd9`:

1. `./gradlew test` passed all 234 tests.
2. `./gradlew runGameTestServer` passed all 11 required GameTests.
3. `./gradlew build` passed.
4. `./gradlew runServer` reached `Done`, accepted `/sef doctor`, `/sef storage status`, and `/eco import status`, then stopped normally with all dimensions saved.
5. `/sef doctor` reported 224 catalog entries, 483 capabilities, 175 shortcuts, 224 policies, nine repositories, zero restart-required changes, healthy security audit, inactive recovery mode, and no kernel errors.
6. Economy and economy-sign storage reported a truthful missing or new state. Import status reported that no import was configured or completed.
7. `build/libs/sef-1.0-SNAPSHOT.jar` passed ZIP integrity inspection. Its SHA-256 is `04e8f3cfdd274416cb1405baf7a1c0815bf1e9074d6544bffde4644e4072cd44`.
8. The final JAR contains the economy implementation and both economy GameTests.
9. Phase 8 source has no `net.minecraft.client` import.
10. `git diff --check` passed.

Authenticated multiplayer, a real external adapter, a real unmodified vanilla client, deliberate forced-crash and filesystem-failure cases, and profiler rows remain manual release gates. These open rows do not invalidate implementation coverage, but they prevent public release approval.

Do not approve a public release while a required row is untested or failing.
