# SEF 2.0.0 Release Candidate Notes

Status: release preparation only. These notes are not a public release declaration.

Target: Minecraft `1.21.1`, NeoForge `21.1.235`, Java `21`.

## Highlights

SEF 2.0 extends the server essentials foundation with server authoritative enhanced GUIs, Fancy Tags, disguise projections, typed server controls, modular configuration, recovery workflows, permission derived cooldowns, and expanded teleport, moderation, economy, inventory, and automation coverage.

## Fixed in this release line

The current verified change set includes fixes for delegated sudo parsing and timing, configuration migration recovery, LuckPerms precedence and failure handling, GUI blur and tooltip duplication, disguise identity and animation paths, offline action authorization, full server admission exemptions, server control runtime honesty, crash recovery claims, legacy enforcement preservation, signed chat provenance, jail and economy sign transitions, Fancy Tags ownership and retention, command catalog parity, `/feed` zero saturation behavior, teleport safety and quotas, and player warp management permission boundaries.

## Compatibility

The artifact is a universal NeoForge JAR. The server remains authoritative. Vanilla and clients without SEF retain command access because the enhanced protocol is optional. LuckPerms, FTB Essentials, and Curios remain optional integrations.

## Migration

Back up the world, `config/sef`, and world SEF storage. Review `/sef config migrate dryrun` before applying any legacy configuration migration. Follow [the migration guide](MIGRATION_GUIDE.md) for confirmation, backup, rollback, and recovery procedures.

## Verification status

The automated unit, GameTest, build, dedicated server startup, headless client startup, generated reference, security, and artifact checks pass on the current source revision. The artifact is `build/libs/sef-2.0.0.jar`, 3,370,948 bytes, with SHA-256 `146e843e1193886e00ef74df2938f6286a46b34c864d9c0a4b4e2cb29e7ccd31` and SHA-512 `d2da6c40953d310f5e8dd021784b002f2f816ebf4d85aceb29b4a00730ec070c9e0e7069452843dc3d0a9f3f5ec1a07b62986495547a96b9886146a3d9067323`. Portable opened audit descriptor identity, Windows secure filesystem support, and dependency closure remain blocked pending owner approved runtime decisions. The Phase 14 ledger still has interactive multiplayer, optional integration, GUI visual, upgrade, and rollback rows open. Public publication must wait for those gates and a verified broker preview for both platforms.
