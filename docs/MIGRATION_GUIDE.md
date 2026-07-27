# Migration Guide

## Scope

This guide covers migration from the legacy monolithic `config/sef/common.toml` surface to the typed files under `config/sef/modules`.

The migration never deletes or rewrites `common.toml`. It maps only fields with an exact typed destination. It reports every retained field and every legacy cooldown field that must move to a Phase 13O permission.

## Before migration

1. Stop the server and make a recoverable backup of the world, `config/sef`, and the world `serverconfig/sef` directory.
2. Start with the old `common.toml` still present.
3. Confirm `/sef config status` has no active validation error.
4. Run `/sef config migrate dryrun`.
5. Review every mapped field, retained field, ignored cooldown, dependency error, and source fingerprint.
6. Resolve invalid values and dependency conflicts before publication.

The dry run never prints configuration values. It identifies fields by stable paths and destinations.

## Publication

Run:

```text
/sef config migrate apply <expected_revision>
```

The command reports the number of mapped and retained fields and issues a single use confirmation token. Complete the exact confirmation command within 60 seconds.

The confirmation binds:

1. The authenticated actor.
2. The configuration revision.
3. The command policy revision.
4. The SHA 256 fingerprint of `common.toml`.
5. The stable migration action.

Publication performs these steps:

1. Parse the current legacy file with bounded UTF 8 input.
2. Build immutable typed candidates for all 62 modules.
3. Write all candidates to the fixed owned staging directory.
4. Parse every staged file again.
5. Validate the complete dependency and conflict graph.
6. Retain exact common and module recovery backups.
7. Recheck that no legacy or module input changed.
8. Replace each module through an atomic owned file write.
9. Publish one new live configuration revision.
10. Write `config/sef/modules/migration.toml` only after publication succeeds.
11. Remove the known staging files.

If replacement or publication fails, every replaced module is restored from the exact in memory original and the previous configuration is republished.

## Recovery files

Migration recovery files are stored under:

```text
config/sef/backups/configuration/modular-migration-1
```

The directory contains the exact pre migration `common.toml` and every pre migration module file. A conflicting existing backup blocks publication rather than overwriting recovery evidence.

`config/sef/modules/migration.toml` records the source fingerprint, publication revision, mapped field count, retained legacy file state, and each mapped source and destination.

## Cooldown migration

Legacy operator cooldown durations are ignored by modular migration. Cooldown authority comes from permissions:

```text
sef.cooldown.<action>.<seconds>
```

Use `/sef cooldown status`, `/sef cooldown explain`, and the generated permission reference to review the effective value. Preserve finite fallback behavior before removing an old operator value.

## Repeating migration

A valid migration marker makes a repeated migration an unchanged success. A changed source fingerprint, stale revision, malformed marker, modified module input, symlink, detectable hard link, oversized file, invalid encoding, or conflicting recovery backup blocks publication.

## Rollback

For one module, use its retained history:

```text
/sef config history <module>
/sef config rollback <module> <revision>
```

For complete migration recovery:

1. Stop the server.
2. Copy the current `config/sef` directory to a separate recovery location.
3. Verify the files under `config/sef/backups/configuration/modular-migration-1`.
4. Restore the required module files while the server is stopped.
5. Keep `common.toml` and the migration backup.
6. Start the previous approved JAR if the new JAR is being rolled back.
7. Run configuration, storage, permission, and feature diagnostics before reopening the server.

Do not delete the migration marker or recovery directory merely to force a second migration. Repair the specific invalid state and retain evidence.
