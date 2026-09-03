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

The audit writer now uses a platform native descriptor provider and fails closed when descriptor identity or link metadata is unavailable. Linux and macOS retain an opened directory descriptor for anchored `openat` traversal with `O_NOFOLLOW`, architecture aware `fstat` identity and link checks, and native append writes. Windows retains an opened directory handle, walks each existing parent with reparse point checks, compares reopened parent identity with the retained handle, disables write sharing for append handles, and uses `GetFileInformationByHandleEx` identity and link checks. The provider is implemented against the JNA API supplied by the pinned NeoForge runtime and is not embedded in the mod JAR. Hosted run `33708259557` passed the native provider and disposable writer probe, unit, GameTest, dependency, and artifact smoke on Linux, macOS, and Windows for the current candidate commit `ca0a8198b286757059935b1632d24b97e63df718`. The complete `EXT-001` packet still requires direct server and client workflows and fixture level opened object evidence before this row closes.

The default Windows NIO provider is no longer used for the security sensitive append. The Windows provider opens the file with native sharing and reparse controls, validates the opened handle identity, and flushes the descriptor before closing. The hosted Windows run verified the provider against Java 21 and the pinned candidate jar. Authoritative NeoForge runtime provenance, advisory applicability, and compatible remediation remain separate `EXT-002` gates.

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

The current candidate runtime report resolves JNA `5.14.0` and JNA Platform `5.14.0` through `net.neoforged:minecraft-dependencies:1.21.1`, which is brought by the pinned NeoForge `21.1.235` runtime. The mod declares both coordinates as `compileOnly`, and the candidate JAR contains no JNA, Netty, Log4j, Commons, Guava, or Plexus classes. This establishes the candidate graph and duplicate absence, but it does not by itself establish advisory applicability or a safe platform upgrade.

The candidate artifact is `sef-2.0.0.jar`, 3,386,731 bytes, with SHA 256 `ea805475a9f692de52b052587cb5e47324b4a083b8261ff429fe477c623248c7` and SHA 512 `a33e083f10a3ab6476c1965e689ffdd5cd22c7773ce0f1428e0a8c0cb9bb3aca4cbe2557d2b89384430a0b08c0f742e81dadb7cc45e0828e852dde5c4b80ddeb`. The hosted candidate artifact is bound to commit `ca0a8198b286757059935b1632d24b97e63df718`. Hosted matrix run `33708259557` inspected the matching candidate on Linux, macOS, and Windows. The JAR does not embed Netty, Log4j, Commons, Guava, or Plexus libraries, so a Gradle resolution override cannot remediate the libraries supplied by an installed NeoForge runtime.

The authoritative NeoForged `minecraft-dependencies:1.21.1` module is published in the NeoForged `mojang-meta` repository at `https://maven.neoforged.net/mojang-meta/net/neoforged/minecraft-dependencies/1.21.1/`. The downloaded module metadata has SHA 256 `211b1f95714cf1fb6f4a45612dd4bf731fb09795c30d4fb5f23c9fada6173332` and SHA 512 `494dedd664aee48322439906da6f882e79ce01bea3956e07b66bc9b332a5e747ba7c8646a3ee652636e62df7fdc22cbb6af0dba539989c617f4dd1f5d41e4956`. Its published POM has SHA 256 `7709b32b651ba2d32ad2aa74c677c4cc88728d7f6ab05ecbc75fc709eb367183` and SHA 512 `220df35690cfc4bc44cb3cba21b79c59172641fee4c8a9a5a3d01e0db8df66e6462ae3c7e27dc112a929ef0ac502b5d3eab1ebad51fe8dfd1ce7b0e8f6113fad`. The module strictly supplies JNA and JNA Platform `5.14.0`, Netty `4.1.97.Final`, Commons IO `2.15.1`, Commons Compress `1.26.0`, Log4j `2.22.1`, and Guava `32.1.2-jre`. This confirms platform ownership and provenance for the candidate graph, but it does not close advisory applicability or provide a compatible remediation inside the pinned NeoForge `21.1.235` boundary. `EXT-002` therefore remains blocked.

## Verification result

The repaired worktree passes all 530 unit tests, the 41 required GameTests, the Gradle build, generated reference checks, headless client startup, and a dedicated server smoke that reached `Done` and saved dimensions before the expected bounded timeout. Hosted run `33707453263` passed the build and analysis checks, hosted run `33707453285` passed CodeQL, and hosted run `33707453265` passed the exact candidate artifact, native audit provider, disposable writer probe, dependency insight, unit, build, and GameTest checks on Linux, macOS, and Windows. Mixin configuration remains required with `defaultRequire` set to one. Client references are confined to client sources and client mixins. Optional dependencies remain compile only.

No confirmed command authorization bypass, backdoor like route, or sensitive data leak remains in the reviewed repaired scope. The opened descriptor identity repair is implemented and its basic native runtime smoke passes on all three mandatory operating systems. Full `EXT-001` remains open for the complete host workflow and fixture packet. Dependency closure remains blocked because the current NeoForge platform supplies the affected runtime libraries and the candidate JAR does not replace them. The dependency row requires graph, advisory, and installed runtime verification rather than a development only override.

## Bounded limitations and downstream work

This review does not claim completion of later persistence completeness, universal GUI coverage, UI polish, full lifecycle convergence, clean checkout, or final documentation phases. Full provider staging, interactive GUI and mixed enhanced or fallback client behavior, LuckPerms staging, admission capacity, and disguise animation remain mapped runtime work in later phases. A later source, configuration, dependency, resource, test harness, mixin, access transformer, or packaging change invalidates the affected evidence rows and requires the mapped rerun.

Historical findings and prior snapshots remain in `audit.md` and older acceptance records. They are comparison material only and do not replace the current Phase 001 evidence.
