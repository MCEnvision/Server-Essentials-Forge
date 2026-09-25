# phase 000 external fixture result

## result

`P000-TASK-002` is blocked at the Forge backend boot stage. The pinned external candidate bytes were revalidated before launch, and the target jar built with Java 17. The first disposable Forge backend loaded Forge 47.3.12, ProxyCompatibleForge 1.3.1, and the target SEF jar, then failed during Mixin application before server readiness. Dependent proxy login, signed chat, command, switching, and direct spoof assertions were not run and remain unverified.

## candidate revalidation

| candidate | sha256 | sha512 | result |
| --- | --- | --- | --- |
| Velocity 4.2.0 build 30 | `35a5596a5468a035d8a32c8de5ebb0dc6b8d8f0cc3ff5169d514aca762af8aa8` | `4acbb519a3c371e55ebe4298368bd2adf55368e450280674306088932cbac31125b096913a98510417e13b9971a35549a2fe42050627e6b20247b1cb2a8b06df` | match |
| Ambassador 1.4.5 | `7c57a649c3948672cbef35a3baa8f73b046ac509ea8ffa6ee3008890d36f2c17` | `214081b498644c5c640e1aea23deff0469d1c842e1142aae742143c8738a971a4898ead72da1605f1c2c202c4f1be8bcf28acc2787cbcede2a86d642ae48f81d` | match |
| ProxyCompatibleForge 1.3.1 | `ea541aff6276970d98506965c8e71bd4ad7329452f631b2f45eb08edea425271` | `dba5b70fedcb0f5fdc014801ab43e6d048f24b531de8aea5b70ea223760b66ae9fba3033c2ccd9d78793b9ea2f9d3b47ae1f4caa95aa129d989f97e63636b4ab` | match |

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
| backend path | `/mnt/hermes/projects/Sef/.test-runs/phase-000/fixture-20260924-a/backend-a` |
| EULA | `eula=true` read back before launch |
| SEF jar | `506486` bytes, source build artifact |
| stage | Forge ModLauncher Mixin preparation |
| decisive log | `backend-a/logs/latest.log:18` |
| failure | `InvalidMixinException`, `sef.mixins.json:gui.MinecraftServerMixin`, shadow method `m_6846_` was not located in `net.minecraft.server.MinecraftServer` |
| terminal state | process exited with status 1 before server readiness |

The same failure appears in `backend-a/logs/debug.log:231`. The fixture did not disable the failing mixin, remove the target mod, substitute a Forge version, or add a custom handshake, because any such change would no longer test the pinned target baseline.

## consequence

This is a concrete matrix blocker, not a passing compatibility result. The source build can produce a jar, but the current target baseline cannot reach a dedicated Forge server world with its existing server mixin set. Phase 000 cannot close `SEF-AC-010` until a corrected target baseline or an authorized plan amendment supplies a compatible server startup path. No proxy or client assertion is inferred from the failed boot.
