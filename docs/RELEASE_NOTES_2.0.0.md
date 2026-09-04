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

At the verification revision recorded below, the automated unit, GameTest, build, dedicated server startup, headless client startup, generated reference, security, and artifact checks passed. The audit writer now uses a platform native descriptor provider on Linux, macOS, and Windows through the JNA API supplied by the pinned NeoForge runtime. Hosted matrix run `33707453265` passed the same candidate artifact, native provider smoke, and disposable writer probe on all three operating systems for commit `d0911559c48fe81216e58a5a28a5c7ccaa987777`. Full runtime evidence, dependency closure, interactive multiplayer, optional integration, GUI visual, upgrade, and rollback rows remain open. Public publication must wait for those gates and a verified broker preview for both platforms.
