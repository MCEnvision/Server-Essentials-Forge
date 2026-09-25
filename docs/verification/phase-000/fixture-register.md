# phase 000 fixture register

This register is created before any fixture process or candidate file. It binds the task owned resources and teardown scope for `P000-TASK-002`.

## ownership

| item | value |
| --- | --- |
| host | `node-1` |
| task | `P000-TASK-002` |
| node disposable root | `/mnt/hermes/projects/Sef/.test-runs/phase-000/fixture-20260925-e` |
| laptop disposable root | `/home/envy/.local/share/PrismLauncher/instances/sef-phase-000-client-20260925-e` |
| retained evidence | `/mnt/hermes/projects/Sef/docs/verification/phase-000/` |
| proxy process | only a Velocity process started from this register's candidate file |
| backend processes | only Forge server processes started from this register's disposable runtimes |
| client process | only a Forge client started from the registered laptop instance path |
| audio watcher | only a watcher bound to the exact owned client process tree and stream |

## planned resources

The disposable roots may contain downloaded candidate jars, proxy configuration, two backend runtimes, logs, worlds, eula files, generated server properties, an isolated Prism client instance, client logs, and sanitized intermediate receipts. No production path, the existing FutureShops instance, credential, shared database, or sibling checkout is in scope.

Before startup, every created path is checked against this register. Before cleanup, every owned process, watcher, stream, and child path is resolved by exact identity. After the final evidence consumer, owned processes stop and the disposable root is removed. Required sanitized receipts remain under the retained evidence directory. Any residue remains an open phase gate and is not silently deleted.

## launch boundary

The fixture uses pinned external Velocity, Ambassador, and ProxyCompatibleForge artifacts only. It does not create the SEF bridge, shared authority, administrative plugin channel, custom handshake, production topology, or product implementation. The Forge target is tested as a no GUI server fixture. A laptop client is required for signed chat, real player command arguments, and both directions of backend switching. If the exact laptop window, discrete renderer, private route, and muted owned playback stream cannot be proven, those assertions remain unverified.

The `fixture-20260925-e` retry uses the pinned Forge 47.3.12 server installer, the reobfuscated target jar, MixinExtras Forge 0.5.3 on both sides, PCF `MODERN_DEFAULT` forwarding override on both backends, a private Tailscale proxy listener, and a separate Forge client instance. It does not alter the target source, disable a mixin, substitute a Forge version, or touch the existing FutureShops instance.

## follow up retry extension

The later `fixture-20260925-g` retry used the same bounded ownership rules with a freshly built SEF jar. Its node disposable root was `/mnt/hermes/projects/Sef/.test-runs/phase-000/fixture-20260925-g`, and its laptop disposable root was `/home/envy/.local/share/PrismLauncher/instances/sef-phase-000-client-20250925-g`. The extension owned only the proxy, the two Forge processes, the isolated client process, and their children. The pre-existing Docker listeners on `192.168.1.242:25566` and `192.168.1.242:25567` were identified and preserved as out of scope.

The client sound engine failed during startup and no owned playback stream was created. Per the host acceptance boundary, the client was stopped without attempting further interaction. The extension therefore does not claim a backend switch or a reconnect result.

## x11 client retry extension

Before the next laptop attempt, the exact owned paths are registered as `/mnt/hermes/projects/Sef/.test-runs/phase-000/fixture-20260925-h` and `/home/envy/.local/share/PrismLauncher/instances/sef-phase-000-client-20250925-h`. The client will launch through the laptop's existing X11 display with master output zero before startup. Only a client process whose window, discrete renderer, process tree, and muted playback stream are all proven may receive test input. The node root, client instance, proxy, Forge backends, logs, worlds, candidate downloads, and any audio watcher are disposable; only sanitized evidence under this directory is retained.
