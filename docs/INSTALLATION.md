# Installation Guide

## Supported platform

SEF targets Minecraft `1.21.1`, NeoForge `21.1.235`, and Java `21`. Use the universal JAR produced by the checked in Gradle wrapper.

## Server only mode

1. Install NeoForge `21.1.235` on the server.
2. Place the SEF JAR in the server `mods` directory.
3. Start the server once, wait for the ready message, and stop it normally.
4. Keep `gui.enabled = false` in `config/sef/common.toml`.
5. Review `config/sef/modules/*.toml`, permission defaults, storage paths, privacy settings, and disabled by default features.
6. Start the server and run `/sef doctor`, `/sef storage status`, and `/sef config status`.

Players do not install SEF in this mode. Every supported action remains available through Brigadier commands and text feedback.

## Optional enhanced GUI mode

1. Complete the server only installation.
2. Set `gui.enabled = true` in `config/sef/common.toml`. This controls optional payload registration and requires a restart.
3. Set `gui.mode = "auto"` or `gui.mode = "on"` in `config/sef/modules/gui.toml`.
4. Install the same SEF JAR on clients that should receive negotiated screens, HUD state, Fancy Tags rendering, or client presentation preferences.
5. Restart the server and enhanced clients.
6. Run `/sef guis status` and `/sef client status` to inspect the negotiated state.

Vanilla clients and clients without SEF may still join. They receive command fallback and never receive SEF payloads. An incompatible or missing enhanced client is not a reason to disconnect a player.

## Optional integrations

LuckPerms, FTB Essentials, and Curios are optional. SEF must start when each is absent. Enable an integration only after installing a compatible provider and checking `/sef doctor`.

Provider loss never grants authority. Sensitive provider failures, unknown permission checks, missing inventory adapters, and unavailable command adapters fail closed or retain the documented previous known good state.

LuckPerms NeoForge `5.4.140` failed player login in the historical NeoForge `21.1.233` matrix. That result does not establish behavior on NeoForge `21.1.235`. Use a LuckPerms and NeoForge pair verified together, or run without LuckPerms until a compatible pair is selected. Do not suppress or patch around another mod's failed login listener.

## First start checklist

1. Confirm the server reached the ready state without linkage, mixin, registry, configuration, or storage errors.
2. Confirm `config/sef/modules` contains `index.toml` and all 62 owned module files.
3. Review [the configuration reference](CONFIGURATION_REFERENCE.md), [the command reference](COMMAND_REFERENCE.md), and [the permission reference](PERMISSION_REFERENCE.md).
4. Assign permissions explicitly. Do not use a broad wildcard until its quota and dynamic permission effects have been reviewed.
5. Keep delegated sudo, external command profiles, file logging, Fancy Tags imports, disguise abilities, destructive server controls, and optional enhanced payloads disabled until their policy is configured.
6. Back up the world, `config/sef`, and the world `serverconfig/sef` directory before migration or an upgrade.

## Updating

1. Stop the server normally.
2. Back up the current JAR, `config/sef`, and world SEF data.
3. Replace the JAR without deleting existing configuration or storage.
4. Start once and inspect configuration documentation upgrades and retained recovery backups.
5. Run the diagnostics and the relevant feature checks.
6. Keep the prior JAR and backups until restart, reconnect, persistence, and rollback behavior is verified.

See [the migration guide](MIGRATION_GUIDE.md) for the monolithic configuration transition.
