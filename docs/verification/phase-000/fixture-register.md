# phase 000 fixture register

This register is created before any fixture process or candidate file. It binds the task owned resources and teardown scope for `P000-TASK-002`.

## ownership

| item | value |
| --- | --- |
| host | `node-1` |
| task | `P000-TASK-002` |
| disposable root | `/mnt/hermes/projects/Sef/.test-runs/phase-000/fixture-20260924-b` |
| retained evidence | `/mnt/hermes/projects/Sef/docs/verification/phase-000/` |
| proxy process | only a Velocity process started from this register's candidate file |
| backend processes | only Forge server processes started from this register's disposable runtimes |
| client process | no client launch on `node-1`; laptop gate requires separate owner desktop control |
| audio watcher | none on `node-1`; laptop watcher must bind one exact owned client stream |

## planned resources

The disposable root may contain downloaded candidate jars, proxy configuration, two backend runtimes, logs, worlds, eula files, generated server properties, and sanitized intermediate receipts. No production path, personal instance, credential, shared database, or sibling checkout is in scope.

Before startup, every created path is checked against this register. Before cleanup, every owned process, watcher, stream, and child path is resolved by exact identity. After the final evidence consumer, owned processes stop and the disposable root is removed. Required sanitized receipts remain under the retained evidence directory. Any residue remains an open phase gate and is not silently deleted.

## launch boundary

The fixture uses pinned external Velocity, Ambassador, and ProxyCompatibleForge artifacts only. It does not create the SEF bridge, shared authority, administrative plugin channel, custom handshake, production topology, or product implementation. The Forge target is tested as a no GUI server fixture. A laptop client is required for signed chat, real player command arguments, and both directions of backend switching. If the exact laptop window, discrete renderer, private route, and muted owned playback stream cannot be proven, those assertions remain unverified.

The `fixture-20260924-b` retry uses the pinned Forge 47.3.12 server installer and the reobfuscated target jar to distinguish a userdev launch mapping failure from a production server boot failure. It does not alter the target source, disable a mixin, or substitute a Forge version.
