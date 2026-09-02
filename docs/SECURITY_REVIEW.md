# Security Review

## Current Phase 001 review

This review covers the repaired security and supply chain worktree on branch `envy/phase-001-security`. The frozen Phase 000 base is commit `6fa2dde1e69578c9629eaa6629b224ed06eddd3f`, with tree `5a46c4d3290f4220deddee3d0d912766fbd1a86d`. The current uncommitted repair diff is recorded by SHA 256 `99e8344a8a729a7881124efa3f34341d4ff9f52ca6e64705ebc138c3dad03fcc`. The complete sanitized evidence is held in the Phase 001 task records for Tasks 001 through 013.

The review covers command authority, permissions, delegation, aliases, bundles, profiles, sudo, scheduled work, server controls, GUI actions, payloads, sessions, configuration, migration, recovery, filesystem roots, Fancy Tags content, optional integrations, mixins, access transformers, audit, logs, privacy projections, dependencies, and the packaged JAR.

## Confirmed findings repaired in this phase

| Finding | Repair | Regression evidence |
| --- | --- | --- |
| Raw admin chat content could enter the server log before command handling | The interception log records only actor identity and message length. | Full unit suite, redaction tests, and source canary scan |
| An oversized existing Fancy Tags object could be read before its limit was enforced | Content addressed reads use the configured bounded read path before integrity processing. | Oversized object rejection test |
| An audit active file or rotation path could be substituted with a symbolic link | Audit roots, active files, rotation paths, and retained files are validated without following symbolic links. | Active audit symlink sentinel test |
| A configuration root beneath a symbolic link could redirect module state | Module, history, backup, write, and recovery directories use safe directory creation and validation. | Configuration parent symlink test |
| Applicable dependency alerts were constrained by strict platform requests | Netty, Log4j, Commons Lang, and Plexus Utils resolve to the compatible patched versions recorded in the dependency evidence. | Resolved graph, dependency insight, build, GameTest, server smoke, and JAR inspection |

## Authority and backdoor review

The command catalog rejects duplicate canonical routes, validates permission and audit metadata, and requires the shared execution pipeline before sealing. `KernelCommandExecutor` performs source classification, delegation scope, permission, control authorization, cost, quota, cooldown, lease, journal, and audit checks immediately before the action. The administrative GUI does not accept client command text. It constructs an action from a server projected catalog definition and fixed server side arguments, then rechecks panel revision, panel permission, control context, and current action permission before invoking the normal Brigadier route.

The review found no alternate command execution sink that accepts arbitrary client supplied text. Direct dispatcher call sites are limited to server generated canonical routes or bounded legacy and integration paths already covered by their own authority checks. The retained legacy sudo class is not registered.

## Privacy and data handling

Command observation uses redaction before persistence and export. Structured audit events bound strings, maps, lists, identifiers, revisions, and correlation fields. Mandatory security audit remains independent from optional file logging filters. The repaired admin chat path no longer writes message content to ordinary logs. Repository and artifact scans found no credential material, private key material, private host path, debug output, or secret filename in the repaired source or JAR.

## Dependency and artifact evidence

The latest read only remote snapshot contains 26 open Dependabot alerts, 12 high, 13 medium, and 1 low. Code scanning and secret scanning each contain zero open alerts. The alerts remain open remotely because this audit does not dismiss or mutate repository alert state. Candidate graph and packaged reachability evidence determine applicability and repair.

The candidate artifact is `build/libs/sef-2.0.0.jar`, 3,369,325 bytes, SHA 256 `e503039d9b492ab9f9571d63b8bc31fc54cd1f3ea24252c6e700ffcf81a4905c`, and SHA 512 `ac42f00b22961d83fcdb37b6976fcb2175db0df6e04886377a2db1afac0651747648d8d810f39b647016aea97602062c452594daf45e323834c4cbd43e0fe770`. The JAR has no duplicate entries, embedded Netty, Log4j, Commons, Guava, or Plexus package entries, or secret filename entries.

## Verification result

The repaired worktree passes 519 unit tests with zero failures, errors, or skips, the 41 required GameTests, the Gradle build, and a dedicated server smoke that reached `Done` and saved dimensions before the expected bounded timeout. Mixin configuration remains required with `defaultRequire` set to one. Client references are confined to client sources and client mixins. Optional dependencies remain compile only.

No confirmed Phase 001 authorization bypass, backdoor like route, sensitive data leak, or applicable critical or high exploitable dependency vulnerability remains in the reviewed repaired scope.

## Bounded limitations and downstream work

This review does not claim completion of later persistence completeness, universal GUI coverage, UI polish, full lifecycle convergence, clean checkout, or final documentation phases. Full provider staging, interactive GUI and mixed enhanced or fallback client behavior, LuckPerms staging, admission capacity, and disguise animation remain mapped runtime work in later phases. A later source, configuration, dependency, resource, test harness, mixin, access transformer, or packaging change invalidates the affected evidence rows and requires the mapped rerun.

Historical findings and prior snapshots remain in `audit.md` and older acceptance records. They are comparison material only and do not replace the current Phase 001 evidence.
