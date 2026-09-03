# SEF 2.0 Release Workflow

This procedure prepares the SEF 2.0 universal JAR for Minecraft 1.21.1 and NeoForge 21.1.235, then submits the identical validated artifact to Modrinth and CurseForge through the release broker. It does not publish automatically.

## Release inputs

Use semantic version `2.0.0`, Java 21, the checked in Gradle wrapper, Minecraft `1.21.1`, loader `neoforge`, and NeoForge `21.1.235`. Release from the approved phase commit only. Do not release from a dirty worktree or an unapproved branch.

## Project mappings

The verified platform mappings are:

- CurseForge project `1484800`: https://www.curseforge.com/minecraft/mc-mods/server-essentials-forge
- Modrinth project `ZYRnLzkm`: https://modrinth.com/mod/server-essentials-forge

The Modrinth project is currently a draft with no release file. Project creation and release upload are separate approvals.

## Automated preparation

Run the manual GitHub Actions workflow named `release` with:

```text
version: 2.0.0
ref: envy/sef2_complete
```

The workflow runs unit tests, all registered GameTests, the Gradle build, metadata checks, JAR inspection, and SHA-256 and SHA-512 hashing. It uploads the JAR and release manifest as workflow evidence. It never reads platform tokens and never uploads to either platform.

The Phase 001 security gate also requires the resolved dependency graphs and remote alert snapshot to be captured at the release commit. Do not treat an open remote alert as dismissed. Record candidate configuration, packaged reachability, and the installed runtime provider in the release evidence. A development resolution override is not release remediation when the universal JAR does not embed or replace the platform library. Release preparation remains blocked until the dependency runtime decision is approved and verified.

Capture the dependency and platform ownership manifest at the same candidate revision:

```bash
./gradlew generateAuditDependencyManifest --no-configuration-cache -PsefAuditCandidateCommit="$(git rev-parse HEAD)" --console=plain
```

On Windows PowerShell, run `gradlew.bat generateAuditDependencyManifest --no-configuration-cache "-PsefAuditCandidateCommit=$((git rev-parse HEAD).Trim())" --console=plain`. Retain `build/audit/platform-dependency-manifest.txt` with the restricted release evidence. The manifest is sanitized, includes normalized dependency paths and artifact digests, identifies the NeoForge runtime owner for JNA and JNA Platform, and fails to provide closure if that owner cannot be resolved. It does not replace authoritative advisory, installed-runtime, or compatible-remediation evidence required by `EXT-002`.

The equivalent local checks are:

```bash
./gradlew clean test -Pmod_version=2.0.0 --no-daemon
./gradlew runGameTestServer -Pmod_version=2.0.0 --no-daemon
./gradlew build -Pmod_version=2.0.0 --no-daemon
```

The cross platform audit also stages the exact candidate JAR in fresh game directory `mods` folders and runs `runCandidateGameTestServer` with `-PsefCandidateGameDirectory`. That run disables the local source mod and requires all 41 packaged GameTests to pass on Linux, macOS, and Windows. It then starts `runCandidateServer` from a separate disposable directory and requires the same packaged JAR to load, reach the dedicated server `Done` line, and stop cleanly. The runtime manifests record the candidate mods path, dedicated server evidence, artifact digests, and the native writer identity trace. The trace records before and after opened object identity, type, link count, native flush, and same object assertions, while the probe verifies that a rejected target preserves prior valid bytes. The separate, nonrequired client evidence workflow launches `runFallbackClient` with the packaged JAR on each operating system. Ubuntu must reach the LWJGL backend and GUI atlas. A macOS or Windows runner without a graphical session records `client_display_blocked=true` with the exact GLFW failure and fails that evidence workflow, not the backend audit gate, while remaining a supported platform claim. Server and client logs are sanitized for hosted runner paths before upload, and raw logs are never uploaded.

Inspect `build/libs/sef-2.0.0.jar`, its metadata, its contents, and both checksum files before continuing.

## Acceptance gate

Do not prepare a public release while the Phase 14 acceptance ledger contains open interactive, multiplayer, optional integration, upgrade, or rollback rows. Automated tests and headless startup do not replace those rows. The current ledger remains in progress, so this branch is a release candidate preparation source, not public release approval.

## Platform preparation

The release broker owns platform credentials and returns sanitized metadata. Run these commands from the repository root after the artifact and acceptance gates pass:

```bash
sudo -u codexgateway-release /usr/local/bin/codexgateway-release inspect sefported
sudo -u codexgateway-release /usr/local/bin/codexgateway-release preview sefported --changelog docs/RELEASE_NOTES_2.0.0.md --platform both --channel release --environment client --environment server
```

The preview must show version `2.0.0`, game version `1.21.1`, loader `neoforge`, both client and server environments, the exact artifact SHA-512, and verified Modrinth and CurseForge project ids. If either project is unmapped, stop and create or bind that project through the approved platform project workflow before publishing.

Publishing requires a new explicit owner confirmation of the exact preview id and confirmation code. Only then run:

```bash
sudo -u codexgateway-release /usr/local/bin/codexgateway-release publish <preview-id> --confirm <code>
```

Inspect the returned status and every platform result. A partial failure must be reported and retried with the same preview, never by creating a second upload. Verify the resulting file metadata and hashes after publication.

## Rollback

If validation fails, keep the artifact unpublished and correct the source or metadata. If one platform accepts the file and another fails, leave the successful upload intact, retry the failed target with the same preview, and record the final platform state. Do not delete or replace a published file without owner approval and a replacement artifact with a new version.
