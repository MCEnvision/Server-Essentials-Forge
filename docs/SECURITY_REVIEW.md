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

The audit writer now uses a platform native descriptor provider and fails closed when descriptor identity or link metadata is unavailable. Linux and macOS retain an opened directory descriptor for anchored `openat` traversal with `O_NOFOLLOW`, architecture aware `fstat` identity and link checks, and native append writes. Windows retains an opened directory handle, walks each existing parent with reparse point checks, compares reopened parent identity with the retained handle, disables write sharing for append handles, and uses `GetFileInformationByHandleEx` identity and link checks. The provider is implemented against the JNA API supplied by the pinned NeoForge runtime and is not embedded in the mod JAR. Hosted run `33725475213` passed the native provider and disposable writer probe, packaged GameTests, dedicated server smoke, unit, dependency, and artifact checks on Linux, macOS, and Windows for candidate commit `28cd87884479ac6cdb8a291142989b507f08ea9d`. The probe now uploads per operation opened object identity traces with before and after identity, regular file type, link count, native flush, and same object assertions. It also proves that a rejected directory target preserves the prior valid audit bytes. The same run launches the packaged fallback client on all three operating systems. Ubuntu reaches the LWJGL backend and GUI atlas. macOS and Windows record explicit hosted runner display blockers, `NSGL` and `WGL`, without converting either operating system into an unsupported target. The complete `EXT-001` packet remains open because direct graphical client access and the full platform specific failure preservation matrix still require suitable disposable fixtures.

The default Windows NIO provider is no longer used for the security sensitive append. The Windows provider opens the file with native sharing and reparse controls, validates the opened handle identity, and flushes the descriptor before closing. The hosted Windows run verified the provider against Java 21 and the pinned candidate jar. Authoritative NeoForge runtime provenance, advisory applicability, and compatible remediation remain separate `EXT-002` gates.

An owner desktop probe using a real graphical Minecraft session reached Minecraft 1.21.1 and the LWJGL backend, but the installed NeoForge runtime was 21.1.248 while the candidate requires exactly 21.1.235. NeoForge correctly refused to load the candidate before mod initialization. The temporary candidate and logs were removed. This is evidence of a missing exact runtime fixture under `EXT-001`, not an unsupported operating system.

A second disposable owner desktop probe cloned the Prism instance without changing the installed instance, downloaded the exact NeoForge 21.1.235 runtime, and launched the candidate artifact with SHA-256 `da76826dd757e9f52b1cfc61fb9902812a77c328321edae14428f329c2a16eb0`. The graphical Linux client reached Minecraft 1.21.1, LWJGL 3.3.3, `ServerEssentialsForge (NeoForge 1.21.1 port) initialized`, configuration loading, and the GUI atlas marker. The clone, candidate, logs, and process were removed after the bounded probe. This closes the direct Linux desktop row for the targeted candidate evidence, while the macOS and Windows exact graphical rows and the complete platform-specific failure matrix remain part of `EXT-001`.

Hosted matrix run `33732626666` then reran the complete candidate audit from commit `2cb01b8ecf91eaa90d0f0284e6f1bc2b52058323`. Linux, macOS, and Windows all passed the Java 21 native writer probe, opened object identity trace, object swap control, failure preservation, rotation, restart, dedicated server smoke, dependency insight, packaged JAR inspection, and sanitized evidence upload. Linux reached packaged client startup, LWJGL, and the GUI atlas. macOS and Windows recorded explicit hosted display blockers while their server and native writer gates passed. The Windows client evidence reader now retries transient file locks after process-tree shutdown, so a held `latest.log` cannot bypass or weaken the client verification.

A prior merge-revision matrix run `33738411285` tested synthetic merge commit `646234240649a3552c50970a32b9beae9a0313a3`, which corresponded to branch head `830fb9af4cbe79694314cfebd2b6863311a1f55c`. Candidate build, Linux, macOS, and Windows runtime jobs, CI, CodeQL, and the independent review all passed at that revision. Each dedicated server reached `Done`, remained alive until the workflow sent `stop`, emitted `Stopping server`, and exited cleanly. The packaged client probe required the GUI atlas marker and five seconds of continued process liveness before accepting startup. Ubuntu passed startup, LWJGL, SEF initialization, and the GUI atlas. macOS was classified only by the known `NSGL` pixel-format signature, and Windows only by the known `WGL` OpenGL-driver signature. Both remain mandatory supported operating systems. The candidate artifact stayed bound to the merge commit and retained SHA-256 `da76826dd757e9f52b1cfc61fb9902812a77c328321edae14428f329c2a16eb0` and SHA-512 `ebef0a8b845576ad6a5fb635bd6ac5b79bd4de813b180ce5e2600e8397743b8871d3a5955eaa70437dbb064bd3af50e6c06904187b6556e309035b050cbafd2c`. The complete `EXT-001` failure-injection and direct graphical evidence packet remains open.

## Authority and backdoor review

The command catalog rejects duplicate canonical routes, validates permission and audit metadata, and requires the shared execution pipeline before sealing. `KernelCommandExecutor` performs source classification, delegation scope, permission, control authorization, cost, quota, cooldown, lease, journal, and audit checks immediately before the action. The administrative GUI does not accept client command text. It constructs an action from a server projected catalog definition and fixed server side arguments, then rechecks panel revision, panel permission, control context, and current action permission before invoking the normal Brigadier route.

The review found no alternate command execution sink that accepts arbitrary client supplied text. Direct dispatcher call sites are limited to server generated canonical routes or bounded legacy and integration paths already covered by their own authority checks. The retained legacy sudo class is not registered.

## Privacy and data handling

Command observation uses redaction before persistence and export. Structured audit events bound strings, maps, lists, identifiers, revisions, and correlation fields. Mandatory security audit remains independent from optional file logging filters. The repaired admin chat path no longer writes message content to ordinary logs. Repository and artifact scans found no credential material, private key material, private host path, debug output, or secret filename in the repaired source or JAR.

## Dependency and artifact evidence

The latest read only remote snapshot, captured on 2026-09-02, contains 26 open Dependabot alerts, 12 high, 13 medium, and 1 low. Code scanning and secret scanning each contain zero open alerts. The alerts remain open remotely because this audit does not dismiss or mutate repository alert state. The snapshot is for the repository default branch, which is the legacy Forge 1.20.1 branch, while this candidate targets NeoForge 1.21.1. Candidate graph and packaged reachability evidence are required to determine applicability and repair.

The alert rows below preserve every current advisory as a separate disposition input. The patched version is the first version reported by GitHub, not a proposed upgrade. The candidate status remains blocked until the platform runtime owner, installed runtime artifact, affected API reachability, authoritative advisory applicability, provenance, and compatible remediation are proven through `EXT-002`.

| Alert | Package | Severity | Advisory | First patched version | Candidate status |
| ---: | --- | --- | --- | --- | --- |
| 26 | `org.apache.logging.log4j:log4j-api` | medium | `GHSA-qv9r-c865-cp47` | `2.25.5` | platform runtime, `EXT-002` |
| 25 | `io.netty:netty-codec` | high | `GHSA-558v-64gr-wgg4` | `4.1.136.Final` | platform runtime, `EXT-002` |
| 24 | `io.netty:netty-handler` | high | `GHSA-c653-97m9-rcg9` | `4.1.135.Final` | platform runtime, `EXT-002` |
| 23 | `io.netty:netty-transport-native-epoll` | medium | `GHSA-w573-9ffj-6ff9` | `4.1.135.Final` | platform runtime, `EXT-002` |
| 22 | `io.netty:netty-handler` | high | `GHSA-x4gw-5cx5-pgmh` | `4.1.135.Final` | platform runtime, `EXT-002` |
| 21 | `io.netty:netty-handler` | high | `GHSA-3qp7-7mw8-wx86` | `4.1.135.Final` | platform runtime, `EXT-002` |
| 20 | `io.netty:netty-codec` | high | `GHSA-mj4r-2hfc-f8p6` | `4.1.133.Final` | platform runtime, `EXT-002` |
| 19 | `org.apache.logging.log4j:log4j-core` | medium | `GHSA-6hg6-v5c8-fphq` | `2.25.4` | platform runtime, `EXT-002` |
| 18 | `org.apache.logging.log4j:log4j-core` | medium | `GHSA-3pxv-7cmr-fjr4` | `2.25.4` | platform runtime, `EXT-002` |
| 17 | `org.codehaus.plexus:plexus-utils` | high | `GHSA-6fmv-xxpf-w3cw` | `3.6.1` | build or platform path, `EXT-002` |
| 16 | `org.apache.logging.log4j:log4j-core` | medium | `GHSA-vc5p-v9hr-52mj` | `2.25.3` | platform runtime, `EXT-002` |
| 15 | `io.netty:netty-codec` | medium | `GHSA-3p8m-j85q-pgmj` | `4.1.125.Final` | platform runtime, `EXT-002` |
| 14 | `org.apache.commons:commons-lang3` | medium | `GHSA-j288-q9x7-2f5v` | `3.18.0` | platform runtime, `EXT-002` |
| 13 | `io.netty:netty-common` | medium | `GHSA-389x-839f-4rhx` | `4.1.118.Final` | platform runtime, `EXT-002` |
| 12 | `io.netty:netty-common` | medium | `GHSA-xq3w-v528-46rv` | `4.1.115.Final` | platform runtime, `EXT-002` |
| 11 | `commons-io:commons-io` | high | `GHSA-78wr-2p64-hpwj` | `2.14.0` | platform runtime, `EXT-002` |
| 10 | `org.apache.commons:commons-compress` | medium | `GHSA-4g9r-vxhx-9pgx` | `1.26.0` | platform runtime, `EXT-002` |
| 9 | `org.apache.commons:commons-compress` | medium | `GHSA-4265-ccf5-phj5` | `1.26.0` | platform runtime, `EXT-002` |
| 8 | `com.google.guava:guava` | low | `GHSA-5mg8-w23w-74h3` | `32.0.0-android` | platform runtime, `EXT-002` |
| 7 | `com.google.guava:guava` | medium | `GHSA-7g45-4rm6-3mm3` | `32.0.0-android` | platform runtime, `EXT-002` |
| 6 | `io.netty:netty-handler` | medium | `GHSA-6mjq-h674-j845` | `4.1.94.Final` | platform runtime, `EXT-002` |
| 5 | `org.apache.commons:commons-compress` | high | `GHSA-mc84-pj99-q6hh` | `1.21` | platform runtime, `EXT-002` |
| 4 | `org.apache.commons:commons-compress` | high | `GHSA-xqfj-vm6h-2x34` | `1.21` | platform runtime, `EXT-002` |
| 3 | `org.apache.commons:commons-compress` | high | `GHSA-crv7-7245-f45f` | `1.21` | platform runtime, `EXT-002` |
| 2 | `org.apache.commons:commons-compress` | high | `GHSA-7hfm-57qf-j43q` | `1.21` | platform runtime, `EXT-002` |
| 1 | `org.apache.commons:commons-compress` | high | `GHSA-53x6-4x5p-rrvv` | `1.19` | platform runtime, `EXT-002` |

### Candidate reachability and platform ownership

The hosted candidate dependency manifest for commit `650156da5c49c5b96c1521c82a6c8fae3cdf4246` was compared with the 26 alert rows and the source import surface. The candidate JAR scan found no entries under `io/netty`, `org/apache/logging/log4j`, `org/apache/commons`, `commons-io`, `com/google/common`, `org/codehaus/plexus`, `com/sun/jna`, or `jnidispatch`. The mod directly uses Netty only through `io.netty.buffer.Unpooled`, Guava through `Sets`, Commons Lang through `Pair`, and Log4j through `LogManager` and `Logger` in optional compatibility classes. There are no mod imports of the affected Netty decoders or handlers, Commons archive and XML readers, Plexus extraction helpers, Log4j appenders or layouts, or Log4j `MapMessage` JSON serialization.

| Alert rows | Hosted resolved coordinate | Mod-owned reachability | Candidate graph result | Platform disposition |
| --- | --- | --- | --- | --- |
| 1 to 5, 9 to 10 | `org.apache.commons:commons-compress:1.26.0` | No direct import. Archive handling is platform and loader owned. | At or above every first patched version in these rows. | Candidate version is outside the listed vulnerable ranges. Preserve the platform provenance row and reopen if NeoForge changes the supplied version. |
| 6 | `io.netty:netty-handler:4.1.97.Final` | No direct handler import. | Above the `4.1.94.Final` first patched version for this row. | Candidate version is outside this row's vulnerable range. |
| 7 to 8 | `com.google.guava:guava:32.1.2-jre` | `Sets` only, with no temporary-directory or serialization helper use. | Above the `32.0.0-android` first patched version. | Candidate version is outside the listed vulnerable ranges. |
| 11 | `commons-io:commons-io:2.15.1` | No direct import. | Above the `2.14.0` first patched version. | Candidate version is outside this row's vulnerable range. |
| 12 to 13 | `io.netty:netty-common:4.1.97.Final` | No direct common API import. | Below both `4.1.115.Final` and `4.1.118.Final` first patched versions. | Platform-owned runtime row remains open under `EXT-002`; affected runtime API reachability and compatible remediation are not yet closed. |
| 14 | `org.apache.commons:commons-lang3:3.14.0` | `Pair` only. No long-input recursive utility is called by the mod. | Below `3.18.0` first patched version. | Platform-owned version is in range, while advisory applicability to the direct `Pair` use and the installed loader path remains an `EXT-002` gate. |
| 15, 20, 25 | `io.netty:netty-codec:4.1.97.Final` | No decoder import. Packet buffers use `Unpooled` only. | Below `4.1.125.Final`, `4.1.133.Final`, and `4.1.136.Final` first patched versions. | Platform-owned runtime row remains open under `EXT-002`; decoder reachability and compatible remediation require authoritative NeoForge evidence. |
| 16, 18 to 19 | `org.apache.logging.log4j:log4j-core:2.22.1` | Optional integrations use ordinary logger methods only. | Below every first patched version in these rows. | Platform-owned runtime row remains open under `EXT-002`; SocketAppender, XML layout, and hostname verification configuration reachability require authoritative runtime evidence. |
| 17 | `org.codehaus.plexus:plexus-utils:3.3.0` | No direct source import or packaged class. | Below `3.6.1` first patched version. | Build and loader ownership, extraction reachability, and compatible remediation remain an `EXT-002` gate. |
| 21 to 22, 24 | `io.netty:netty-handler:4.1.97.Final` | No direct handler import. | Below `4.1.135.Final` first patched version. | Platform-owned runtime row remains open under `EXT-002`; SNI, subnet filter, and trust-manager reachability require authoritative runtime evidence. |
| 23 | `io.netty:netty-transport-native-epoll:4.1.97.Final` | No direct native transport import. | Below `4.1.135.Final` first patched version. | Platform-owned native artifact row remains open under `EXT-002`; operating-system packaging, descriptor handling, and compatible remediation require authoritative runtime evidence. |
| 26 | `org.apache.logging.log4j:log4j-api:2.22.1` | Logger methods only. No `MapMessage` or JSON layout use. | Below `2.25.5` first patched version. | Platform-owned runtime row remains open under `EXT-002`; advisory applicability and compatible remediation require authoritative runtime configuration evidence. |

This matrix closes the candidate graph, packaged absence, and direct source reachability checks where the resolved version and API surface support that conclusion. It does not collapse the separate installed-runtime, authoritative advisory, provenance, or compatible-remediation gates. Those gates remain open for the platform-owned versions that are inside an alert range, and no platform pin or alert state is changed by this review.

The current candidate runtime report resolves JNA `5.14.0` and JNA Platform `5.14.0` through `net.neoforged:minecraft-dependencies:1.21.1`, which is brought by the pinned NeoForge `21.1.235` runtime. The mod declares both coordinates as `compileOnly`, and the candidate JAR contains no JNA, Netty, Log4j, Commons, Guava, or Plexus classes. This establishes the candidate graph and duplicate absence, but it does not by itself establish advisory applicability or a safe platform upgrade.

The candidate artifact is `sef-2.0.0.jar`, 3,389,485 bytes, with SHA 256 `da76826dd757e9f52b1cfc61fb9902812a77c328321edae14428f329c2a16eb0` and SHA 512 `ebef0a8b845576ad6a5fb635bd6ac5b79bd4de813b180ce5e2600e8397743b8871d3a5955eaa70437dbb064bd3af50e6c06904187b6556e309035b050cbafd2c`. The hosted candidate artifact is bound to commit `28cd87884479ac6cdb8a291142989b507f08ea9d`. Hosted matrix run `33725475213` inspected the matching candidate on Linux, macOS, and Windows. The JAR does not embed Netty, Log4j, Commons, Guava, or Plexus libraries, so a Gradle resolution override cannot remediate the libraries supplied by an installed NeoForge runtime.

The authoritative NeoForged `minecraft-dependencies:1.21.1` module is published in the NeoForged `mojang-meta` repository at `https://maven.neoforged.net/mojang-meta/net/neoforged/minecraft-dependencies/1.21.1/`. The downloaded module metadata has SHA 256 `211b1f95714cf1fb6f4a45612dd4bf731fb09795c30d4fb5f23c9fada6173332` and SHA 512 `494dedd664aee48322439906da6f882e79ce01bea3956e07b66bc9b332a5e747ba7c8646a3ee652636e62df7fdc22cbb6af0dba539989c617f4dd1f5d41e4956`. Its published POM has SHA 256 `7709b32b651ba2d32ad2aa74c677c4cc88728d7f6ab05ecbc75fc709eb367183` and SHA 512 `220df35690cfc4bc44cb3cba21b79c59172641fee4c8a9a5a3d01e0db8df66e6462ae3c7e27dc112a929ef0ac502b5d3eab1ebad51fe8dfd1ce7b0e8f6113fad`. The module strictly supplies JNA and JNA Platform `5.14.0`, Netty `4.1.97.Final`, Commons IO `2.15.1`, Commons Compress `1.26.0`, Log4j `2.22.1`, and Guava `32.1.2-jre`. This confirms platform ownership and provenance for the candidate graph, but it does not close advisory applicability or provide a compatible remediation inside the pinned NeoForge `21.1.235` boundary. `EXT-002` therefore remains blocked.

## Verification result

The repaired worktree passes all 530 unit tests, the 41 required GameTests, the Gradle build, generated reference checks, headless client startup, and a dedicated server smoke that reached `Done` and saved dimensions before the expected bounded timeout. Hosted run `33725475148` passed the build checks, hosted run `33725475185` passed CodeQL, and hosted run `33725475213` passed the exact candidate artifact, native audit provider, per operation identity trace, failure preservation probe, dependency insight, unit, build, packaged GameTests, dedicated server smoke, and packaged client startup smoke on Linux, macOS, and Windows. The client manifest records full startup evidence on Ubuntu and explicit graphical display blockers on macOS and Windows. Mixin configuration remains required with `defaultRequire` set to one. Client references are confined to client sources and client mixins. Optional dependencies remain compile only.

The prior merge-revision verification `33738411285` also proved that the client stability gate rejects a process that dies after printing the atlas marker and that the server gate rejects a process that reaches `Done` without staying alive for the explicit stop command. Its uploaded manifests contained normalized paths only, matching artifact hashes, and no detected hosted paths or secret-like strings. The current workflow additionally keeps display-blocked client evidence as a failing gate until a suitable graphical fixture is available.

No confirmed command authorization bypass, backdoor like route, or sensitive data leak remains in the reviewed repaired scope. The opened descriptor identity repair is implemented and its native runtime smoke, identity trace, and rejected target preservation check pass on all three mandatory operating systems. Full `EXT-001` remains open for direct client workflows and the complete platform specific failure preservation matrix. Dependency closure remains blocked because the current NeoForge platform supplies the affected runtime libraries and the candidate JAR does not replace them. The dependency row requires graph, advisory, and installed runtime verification rather than a development only override.

## Bounded limitations and downstream work

This review does not claim completion of later persistence completeness, universal GUI coverage, UI polish, full lifecycle convergence, clean checkout, or final documentation phases. Full provider staging, interactive GUI and mixed enhanced or fallback client behavior, LuckPerms staging, admission capacity, and disguise animation remain mapped runtime work in later phases. A later source, configuration, dependency, resource, test harness, mixin, access transformer, or packaging change invalidates the affected evidence rows and requires the mapped rerun.

Historical findings and prior snapshots remain in `audit.md` and older acceptance records. They are comparison material only and do not replace the current Phase 001 evidence.
