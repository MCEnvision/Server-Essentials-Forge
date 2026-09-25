# SEF Backport and Network Scope

Source role: owner_request. Decisions observed September 24, 2026. This is the sanitized product-scope record, not implementation evidence.

## Approved Product

Backport implemented retained SEFPORTED behavior to the existing Forge Minecraft 1.20.1 checkout. Remove the economy and the entire custom interface and client-dependent feature layer, including menu-dependent utilities. Ordinary text commands, native rich chat, tab presentation and visibility remain within their explicit requirements. Remove economy commands, configuration, storage, dependencies and shared execution logic rather than hiding them behind a disabled flag.

Build the small Velocity companion on `velocity-latest` in the same repository, with the backend on `forge-1.20.1`. Preserve the read-only reference checkout and its unrelated plans, goal and changes. The companion handles network-wide commands, locations, disconnections and login bans. Backends own world actions, coordinates, signed-chat mute enforcement and observer-aware hiding. Shared durable state includes bans, absolute mute expirations, vanish, server-qualified homes and operation outcomes.

Administrative communication must resist forged clients, replay and stale sessions and work with empty backends. No arbitrary console relay is authorized. Homes retain server, world, dimension, position and rotation; a player on another backend can request a home and arrive at its qualified destination through a validated transfer.

## Runtime and Storage

The approved `/mnt/hermes/projects/SefVelocity` directory may be used for the companion build as a linked worktree of this repository. This exact-path placement exception preserves `/mnt/hermes/projects/Sef` as the canonical plan and repository anchor. Initial setup checks that the directory is empty and unclaimed; later reuse verifies the established worktree, branch and active ownership. Preserve user content and remove only owned disposable children after their final consumers. This approval does not authorize a duplicate clone or removal of the owner directory.

The selected topology is one Velocity proxy and three Forge backends. Java 25 is the reported proxy runtime; its installed build remains a readiness check. Forge 1.20.1 retains Java 17. All backends initially use the same pack. DEC-007 allows differing packs only where the running client is compatible, with safe refusal otherwise.

The delegated architecture uses proxy-owned SQLite for low-volume network authority and authenticated backend synchronization. A separate MySQL or MariaDB dataset stores comprehensive audit history under the later approved audit decisions. Existing unrelated databases are not implicit targets. A shared SQLite file, additional proxies, Redis and unrelated services are not required by this scope.

## Endpoint

The product endpoint is verified paired artifacts, complete operator and developer documentation, approved sequential integration into the two selected product branches, post-integration verification and signed phase tags. Production rollout, public uploads, firewall changes, credential collection and unrelated repository changes are outside the endpoint.
