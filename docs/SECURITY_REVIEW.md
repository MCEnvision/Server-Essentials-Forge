# Security Review

## Scope

Review range: commit `81a2e5a` through the current `envy/sef2_complete` working tree.

Reviewed areas include delegated sudo, command indirection, permissions, optional provider fallback, enhanced protocol payloads, configuration paths and migration, Fancy Tags content, disguise projection, server control execution, persistence, audit, and generated references.

## Trust boundaries

1. Client payloads and GUI drafts are untrusted.
2. Command text, selectors, targets, aliases, bundles, profiles, and shortcut roots are untrusted until parsed and authorized on the logical server.
3. Permission and quota providers may be absent, stale, or unavailable.
4. Configuration files, migration input, watcher events, imports, and recovery files may be malformed or replaced.
5. Optional mod implementations may be missing or change behavior.
6. Player names, messages, templates, artwork, registry ids, addresses, and persisted metadata are untrusted data.
7. Filesystem paths and destructive server control providers require fixed ownership and explicit confirmation.

## Fixed findings

| Severity | Finding | Resolution |
| --- | --- | --- |
| Critical | Delegated sudo could not safely distinguish the adjacent greedy command argument from the delegation mode | The Brigadier tree now uses explicit `respect` and `delegate` literals, exact compatibility boolean literals, bounded profiles, and full dispatcher GameTest coverage |
| Critical | A future dated one use grant could be consumed before its valid interval | Grant consumption now requires the complete valid interval and remains single use |
| Critical | Configuration replacement did not reject detectable hard linked inputs | Module, backup, migration, and documentation paths reject detectable multiple hard links |
| High | Configuration rewrites could weaken an operator file mode | Atomic replacement preserves the existing POSIX permissions and never broadens a stricter mode |
| High | Legacy migration was only a label and had no transaction or recovery boundary | Migration now binds source and revision, stages all candidates, validates the full graph, retains exact backups, restores on failure, retains `common.toml`, and writes a final marker |
| High | A symlinked migration backup root could redirect recovery output | Every owned backup directory component is created or validated without following a symbolic link |
| Medium | Registry generated documentation was nondeterministic across JVM processes | Set projections are sorted and drift tests compare exact tracked output |

## Security properties verified by automated tests

1. One use delegated permission scope and expiry.
2. Respect mode does not grant persistent authority.
3. Delegated root, redirect, fork, asynchronous work, target identity, command tree revision, permission, confirmation, and provider checks.
4. Permission wildcard diagnostics and finite cooldown fallback.
5. Payload bounds, unknown identifiers, sequence and session validation for covered protocol families.
6. Configuration traversal, symbolic link, detectable hard link, invalid encoding, malformed syntax, stale revision, backup conflict, source drift, and recovery behavior for covered paths.
7. Structured audit persistence and redacted sensitive command observation.
8. Storage schema, backup, quarantine, unknown field, and bounded repository behavior for covered domains.

## Final verification

1. Permission mutation, provider refresh, provider absence, provider failure, explicit denial, wildcard, finite fallback, and revision invalidation pass deterministic coverage.
2. Enhanced and fallback clients join the same GUI-enabled server. The fallback client has no SEF classes, receives no enhanced session, retains command access, and receives fallback guidance.
3. GUI workflows enforce session, sequence, feature, permission, policy, target, control, and form revisions. Typed fields, command shape, confirmation, replay, size, and rate bounds fail closed.
4. Optional integrations pass present and absent startup. Adapter outage, removal, unsupported state, malformed values, ownership conflicts, and bounded fallback have deterministic coverage.
5. Atomic publication, read-only failure, malformed data, interrupted state, corruption quarantine, symlink refusal, detectable hard-link refusal, backup, rollback, and recovery fixtures pass.
6. Privacy tests cover command, GUI, HUD, audit, optional log, export, hidden identity, address, private-message, and secret-argument projections.
7. High-risk control providers require typed inputs, source policy, exact permissions, revision-bound preview, confirmation, governor admission, audit, and recovery state.
8. Deterministic maximum-operation budgets pass. The dedicated server held `20.04` TPS across `480` profiled ticks. Enhanced and fallback clients remained connected through their smoke windows.
9. Dependency declarations, licenses, final JAR contents, secret filenames, generated references, provenance, and the complete diff pass inspection.

No unresolved critical, high, medium, or low security finding remains in the reviewed scope.
