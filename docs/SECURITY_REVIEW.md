# Security Review

## Current Phase 001 review

This review covers the repaired security and supply chain worktree on branch `envy/phase-001-security`. The frozen Phase 000 base is commit `6fa2dde1e69578c9629eaa6629b224ed06eddd3f`, with tree `5a46c4d3290f4220deddee3d0d912766fbd1a86d`. The complete sanitized evidence is held in the Phase 001 task records for Tasks 001 through 013.

The review covers command authority, permissions, delegation, aliases, bundles, profiles, sudo, scheduled work, server controls, GUI actions, payloads, sessions, configuration, migration, recovery, filesystem roots, Fancy Tags content, optional integrations, mixins, access transformers, audit, logs, privacy projections, dependencies, and the packaged JAR.

## Confirmed findings repaired in this phase

| Finding | Repair | Regression evidence |
| --- | --- | --- |
| Raw admin chat content could enter the server log before command handling | The interception log records only actor identity and message length. | Full unit suite, redaction tests, and source canary scan |
| An oversized existing Fancy Tags object could be read before its limit was enforced | Content addressed reads use the configured bounded read path before integrity processing. | Oversized object rejection test |
| An audit active file or rotation path could be substituted with a symbolic link or hard link | Audit roots, active files, rotation paths, and retained files are validated without following links. Appends use a securely held directory handle and unsupported link count providers fail closed. | Active audit symlink and hard link sentinel tests |
| A configuration root beneath a symbolic link could redirect module state | Module, history, backup, write, and recovery directories use safe directory creation and validation. | Configuration parent symlink test |
| Candidate dependency resolution did not change the installed platform runtime | A temporary development resolution override was removed after review because the universal JAR does not embed or replace NeoForge supplied libraries. | Unpatched graph comparison, dependency insight, packaged JAR inspection, and independent review |

## Open review blockers

The audit writer now uses a platform native descriptor provider and fails closed when descriptor identity or link metadata is unavailable. Linux and macOS use anchored `openat` traversal with `O_NOFOLLOW`, nonblocking descriptor opens, descriptor `fstat` identity and link checks, and native append writes. Windows walks each existing parent with reparse point checks before validating or appending through `CreateFile`, with delete sharing disabled while the handle is open and `GetFileInformationByHandleEx` identity and link checks. The provider is implemented against the JNA API supplied by the pinned NeoForge runtime and is not embedded in the mod JAR. Linux unit and server evidence is passing; macOS and Windows runtime evidence remain mandatory before this row closes.

The default Windows NIO provider is no longer used for the security sensitive append. The Windows provider opens the file with native sharing and reparse controls, validates the opened handle identity, and flushes the descriptor before closing. A Windows staging run is still required to verify the installed NeoForge runtime supplies the pinned JNA API and that the provider behaves correctly on the supported filesystem.

## Authority and backdoor review

The command catalog rejects duplicate canonical routes, validates permission and audit metadata, and requires the shared execution pipeline before sealing. `KernelCommandExecutor` performs source classification, delegation scope, permission, control authorization, cost, quota, cooldown, lease, journal, and audit checks immediately before the action. The administrative GUI does not accept client command text. It constructs an action from a server projected catalog definition and fixed server side arguments, then rechecks panel revision, panel permission, control context, and current action permission before invoking the normal Brigadier route.

The review found no alternate command execution sink that accepts arbitrary client supplied text. Direct dispatcher call sites are limited to server generated canonical routes or bounded legacy and integration paths already covered by their own authority checks. The retained legacy sudo class is not registered.

## Privacy and data handling

Command observation uses redaction before persistence and export. Structured audit events bound strings, maps, lists, identifiers, revisions, and correlation fields. Mandatory security audit remains independent from optional file logging filters. The repaired admin chat path no longer writes message content to ordinary logs. Repository and artifact scans found no credential material, private key material, private host path, debug output, or secret filename in the repaired source or JAR.

## Dependency and artifact evidence

The latest read only remote snapshot contains 26 open Dependabot alerts, 12 high, 13 medium, and 1 low. Code scanning and secret scanning each contain zero open alerts. The alerts remain open remotely because this audit does not dismiss or mutate repository alert state. The snapshot is for the repository default branch, which is the legacy Forge 1.20.1 branch, while this candidate targets NeoForge 1.21.1. Candidate graph and packaged reachability evidence are required to determine applicability and repair.

The candidate artifact is `build/libs/sef-2.0.0.jar`, 3,383,143 bytes, with SHA 256 `8778dd80c569f82e820d2329b5475b17f36883cad30e6855b02d9eade1b86f32` and SHA 512 `c1bbbef07e04a2bdf15fd575d45bdf29f7d81e6be2d2f0c12902e840a7f3727943b955a449af19899fea2f860c22bcc507394f16c3283ba6abeb47db9cbed5aa`. The JAR does not embed Netty, Log4j, Commons, Guava, or Plexus libraries, so a Gradle resolution override cannot remediate the libraries supplied by an installed NeoForge runtime.

## Verification result

The repaired worktree passes all 523 unit tests, the 41 required GameTests, the Gradle build, generated reference checks, headless client startup, and a dedicated server smoke that reached `Done` and saved dimensions before the expected bounded timeout. Mixin configuration remains required with `defaultRequire` set to one. Client references are confined to client sources and client mixins. Optional dependencies remain compile only.

No confirmed command authorization bypass, backdoor like route, or sensitive data leak remains in the reviewed repaired scope. The opened descriptor identity repair is implemented for the cross platform provider, with macOS and Windows runtime evidence still open. Dependency closure remains blocked because the current NeoForge platform supplies the affected runtime libraries and the candidate JAR does not replace them. The dependency row requires graph, advisory, and installed runtime verification rather than a development only override.

## Bounded limitations and downstream work

This review does not claim completion of later persistence completeness, universal GUI coverage, UI polish, full lifecycle convergence, clean checkout, or final documentation phases. Full provider staging, interactive GUI and mixed enhanced or fallback client behavior, LuckPerms staging, admission capacity, and disguise animation remain mapped runtime work in later phases. A later source, configuration, dependency, resource, test harness, mixin, access transformer, or packaging change invalidates the affected evidence rows and requires the mapped rerun.

Historical findings and prior snapshots remain in `audit.md` and older acceptance records. They are comparison material only and do not replace the current Phase 001 evidence.
