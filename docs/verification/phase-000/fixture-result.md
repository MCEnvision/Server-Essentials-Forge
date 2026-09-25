# Phase 000 external fixture result

## Result

The production distribution retry reached readiness on both Forge backends and the pinned Velocity proxy loaded Ambassador. The earlier userdev launch remains a failed development path because it stopped during Mixin application, but it does not represent the reobfuscated production server artifact. The client gate is still open. No player login, signed chat, command argument, backend switch, or forged administrative message was asserted because the authorized laptop control endpoint was unavailable from `node-1`.

## candidate revalidation

| Candidate | SHA256 | SHA512 | Result |
| --- | --- | --- | --- |
| Velocity 4.2.0 build 30 | `35a5596a5468a035d8a32c8de5ebb0dc6b8d8f0cc3ff5169d514aca762af8aa8` | `4acbb519a3c371e55ebe4298368bd2adf55368e450280674306088932cbac31125b096913a98510417e13b9971a35549a2fe42050627e6b20247b1cb2a8b06df` | Match |
| Ambassador 1.4.5 | `7c57a649c3948672cbef35a3baa8f73b046ac509ea8ffa6ee3008890d36f2c17` | `214081b498644c5c640e1aea23deff0469d1c842e1142aae742143c8738a971a4898ead72da1605f1c2c202c4f1be8bcf28acc2787cbcede2a86d642ae48f81d` | Match |
| ProxyCompatibleForge 1.3.1 | `ea541aff6276970d98506965c8e71bd4ad7329452f631b2f45eb08edea425271` | `dba5b70fedcb0f5fdc014801ab43e6d048f24b531de8aea5b70ea223760b66ae9fba3033c2ccd9d78793b9ea2f9d3b47ae1f4caa95aa129d989f97e63636b4ab` | Match |

The jar metadata inspection found Velocity license and notice entries, PCF `META-INF/README.md`, and the declared external adapter artifacts. This is notice and byte evidence only. It is not a compatibility or advisory approval.

The revalidation endpoints reported Velocity build 30 as `STABLE`, Ambassador `1.4.5` as `listed`, and ProxyCompatibleForge `1.3.1` as `listed`. Public GitHub security advisory endpoints returned `404` for Ambassador and ProxyCompatibleForge, so no advisory clean claim is made. This is an availability observation, not a security guarantee.

| candidate | notice and metadata entries observed |
| --- | --- |
| Velocity | `LICENSE.txt`, `META-INF/LICENSE`, `META-INF/LICENSE.txt`, `META-INF/NOTICE`, `META-INF/NOTICE.md`, dependency license files for Configurate, and `META-INF/jline/README.md` |
| Ambassador | no license or notice file entry exposed by the downloaded jar archive; upstream project notice remains required for any later distribution review |
| ProxyCompatibleForge | `META-INF/README.md`, manifest implementation `1.3.1`, and imported license preamble described by the pinned upstream source record |

## boot receipt

| field | value |
| --- | --- |
| host | `node-1` |
| runtime | Java 17.0.19, Forge 47.3.12, Minecraft 1.20.1 |
| target build | `sef-1.20.1-1.1.jar`, Gradle `jar`, successful |
| target SHA256 | `0ed3ef7716845ef25100690635cc6932b018d48c4a3e5b361264d70449533511` |
| Forge installer SHA256 | `6117bf266bf8395cc216a9f9438c26f15cbede3b9b416857bbf70e07c15840f7` |
| MixinExtras Forge 0.5.3 SHA256 | `89d60f6bf1f29664319acfa80e777abc03fde674370af52e94a9a2e452b98833` |
| ProxyCompatibleForge SHA256 | `ea541aff6276970d98506965c8e71bd4ad7329452f631b2f45eb08edea425271` |
| backend A | `backend-a`, `127.0.0.1:25566`, `Done`, SEF initialized |
| backend B | `backend-b`, `127.0.0.1:25567`, `Done`, SEF initialized |
| EULA | `eula=true` read back before launch |
| decisive logs | `backend-a/logs/latest.log:54`, `backend-b/logs/latest.log:51` |

Both servers initialized MixinExtras, ProxyCompatibleForge, and SEF. The Forge runtime requires `mixinextras-forge-0.5.3.jar`; the plain common artifact is not sufficient for this dedicated server distribution.

## Proxy receipt

| field | value |
| --- | --- |
| runtime | Java 25.0.3, Velocity 4.2.0 build 30 |
| listener | `127.0.0.1:25565` |
| plugin | Ambassador 1.4.5, loaded successfully |
| plugin SHA256 | `7c57a649c3948672cbef35a3baa8f73b046ac509ea8ffa6ee3008890d36f2c17` |
| backend forwarding | modern forwarding secret configured in both backend PCF files |
| startup | `Done (0.86s)!` |

The proxy and both backends were simultaneously listening. This proves process startup and configuration loading only. It does not prove a real client session or administrative channel security.

## Client and security gates

The laptop gate could not be opened because the authorized `envision` control endpoint was not reachable from `node-1`. Therefore these assertions remain unverified and are not inferred from port readiness:

* a real Forge client login through Velocity
* signed chat delivery and backend mute enforcement
* representative command arguments and server-side command behavior
* A to B and B to A transfers
* forged client administrative payload rejection
* modpack compatibility refusal and its user-facing explanation

## Userdev failed attempt

The same failure appears in `backend-a/logs/debug.log:231`. The fixture did not disable the failing mixin, remove the target mod, substitute a Forge version, or add a custom handshake, because any such change would no longer test the pinned target baseline.

## consequence

Phase 000 still cannot close `SEF-AC-010`. Production server and proxy startup are now proven, but the required client and security assertions need the authorized laptop. No SEF bridge compatibility, shared storage behavior, or production readiness is inferred from this fixture.
