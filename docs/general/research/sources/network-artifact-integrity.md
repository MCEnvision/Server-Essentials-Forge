# Network Reference Artifact Integrity

Observed 2026-09-24T22:22:14Z on node-1. Source role audit_evidence. Candidate artifacts were streamed from authoritative public URLs through SHA-256 and SHA-512. No downloaded code ran and no artifact files or processes were retained. Reported SHA-256 or SHA-512 values matched upstream metadata. This is integrity evidence only, not compatibility or security acceptance. Exact installed operator versions remain unverified.

## EXT-003. Velocity 4.2.0 build 30 stable reference artifact on Java 25

Source: https://fill-data.papermc.io/v1/objects/35a5596a5468a035d8a32c8de5ebb0dc6b8d8f0cc3ff5169d514aca762af8aa8/velocity-4.2.0-30.jar

SHA-256: `35a5596a5468a035d8a32c8de5ebb0dc6b8d8f0cc3ff5169d514aca762af8aa8`.

SHA-512: `4acbb519a3c371e55ebe4298368bd2adf55368e450280674306088932cbac31125b096913a98510417e13b9971a35549a2fe42050627e6b20247b1cb2a8b06df`.

Confirm the exact distributed Velocity license notices and dependency inventory in readiness.

## EXT-004. Ambassador 1.4.5 Forge handshake reference adapter

Source: https://cdn.modrinth.com/data/cOj6YqJM/versions/YeQbhgna/Ambassador-Velocity-1.4.5-all.jar

SHA-256: `7c57a649c3948672cbef35a3baa8f73b046ac509ea8ffa6ee3008890d36f2c17`.

SHA-512: `214081b498644c5c640e1aea23deff0469d1c842e1142aae742143c8738a971a4898ead72da1605f1c2c202c4f1be8bcf28acc2787cbcede2a86d642ae48f81d`.

Upstream declares LGPL-2.1-or-later. Keep separately installed and audit exact bundled notices.

## EXT-005. ProxyCompatibleForge 1.3.1 Forge 1.20.1 forwarding reference adapter

Source: https://cdn.modrinth.com/data/vDyrHl8l/versions/qiZ49HIW/proxy-compatible-forge-1.3.1.jar

SHA-256: `ea541aff6276970d98506965c8e71bd4ad7329452f631b2f45eb08edea425271`.

SHA-512: `dba5b70fedcb0f5fdc014801ab43e6d048f24b531de8aea5b70ea223760b66ae9fba3033c2ccd9d78793b9ea2f9d3b47ae1f4caa95aa129d989f97e63636b4ab`.

Upstream LGPL-2.1-or-later default with imported-code exceptions; preserve the exact distributed license inventory.

## EXT-010. Forge Client Reset Packet Forward file 4657349

Source: https://www.curseforge.com/minecraft/mc-mods/forge-client-reset-packet-forward/files/4657349

Retrieved artifact: https://edge.forgecdn.net/files/4657/349/ForgeClientResetPacket-0.3.0.jar

Observed 2026-09-25T18:57:20Z on node-1. The 12,847-byte JAR was downloaded for hash and archive inspection without execution. It declares `clientresetpacket` with embedded version `0.0.6`, while the CurseForge release file is labeled `0.3.0`. Its project page lists Forge 1.20 and 1.20.1 and marks the project client-only. It was not installed on a backend or bundled into SEF. The temporary downloaded JAR must be removed after the maintenance audit.

SHA-256: `12afe21a807f540566f77c53bd721217be383b60fbede3cc1928ba2e091cbf33`.

SHA-512: `608b04c45a2b98217f8a8800f69e1768d396b5957c46beff1ecbedae0ec7669771c637ad79fa637ed0090a355f96ecc213d3a57ee90b8e69dbe4c74b5260bbc9`.

The CurseForge project page declares MIT, while the JAR metadata links to the original project's license. Verify exact notices and current advisories before operator distribution. Hashes and metadata do not prove successful in-session switching or compatibility with a particular modpack.

## Revalidation 2026-09-25 21:29 UTC

The four pinned artifacts were fetched again from their recorded sources into the registered Phase 000 fixture and inspected without execution. SHA-256 and SHA-512 for Velocity 4.2.0 build 30, Ambassador 1.4.5, ProxyCompatibleForge 1.3.1, and client reset file 4657349 match the values above. The Modrinth version records for Ambassador and ProxyCompatibleForge independently report their exact version identifiers and SHA-512 values. The official Velocity artifact manifest reports implementation version 4.2.0.

The candidate license records remain separate. Velocity identifies GPL-3.0 and its artifact contains license and notice files. Ambassador identifies LGPL-2.1-or-later in Modrinth metadata and its `non-api` source branch; no license or notice file was found in the candidate JAR. ProxyCompatibleForge identifies LGPL-2.1-or-later in Modrinth metadata and LGPL-2.1 in `META-INF/mods.toml`; its candidate contains a README but no license or notice file. Its README acknowledges FabricProxy-Lite and CrossStitch. The reset project and source fork identify MIT, while the selected JAR's `META-INF/mods.toml` points to the original project's license and declares `clientresetpacket` version `0.0.6`. The external file is named 0.3.0 and CurseForge marks it client-only for Forge 1.20 and 1.20.1. Preserve this metadata difference in the runtime manifest; do not infer installed capability from the filename or an on-disk JAR.

At this observation time, the GitHub advisory pages for PaperMC/Velocity, adde0109/Ambassador, adde0109/Proxy-Compatible-Forge, FoxyCraftNetwork/Forge-Client-Reset-Packet, and Just-Chaldea/Forge-Client-Reset-Packet showed no published repository advisories. This records those repository pages only and is not a complete vulnerability assessment of transitive dependencies or other advisory databases. Recheck before distribution.

Sources checked: [Ambassador 1.4.5](https://modrinth.com/plugin/ambassador/version/YeQbhgna), [ProxyCompatibleForge](https://modrinth.com/mod/proxy-compatible-forge), [client reset file 4657349](https://www.curseforge.com/minecraft/mc-mods/forge-client-reset-packet-forward/files/4657349), [Velocity advisories](https://github.com/PaperMC/Velocity/security/advisories), [Ambassador advisories](https://github.com/adde0109/Ambassador/security/advisories), [ProxyCompatibleForge advisories](https://github.com/adde0109/Proxy-Compatible-Forge/security/advisories), [reset fork advisories](https://github.com/FoxyCraftNetwork/Forge-Client-Reset-Packet/security/advisories), and [reset upstream advisories](https://github.com/Just-Chaldea/Forge-Client-Reset-Packet/security/advisories).

This is candidate-integrity and metadata evidence only. The proxy and backend fixtures have not started, and the laptop client acceptance remains deferred while the owner uses a personal client. The selected reset artifact must only be installed in the registered isolated client profile.

## Phase 000 revalidation, 2026-09-25 21:38 UTC

The pinned Forge 1.20.1-47.3.12 installer was fetched from the Forge Maven repository into the registered disposable fixture. Its SHA-256 is `6117bf266bf8395cc216a9f9438c26f15cbede3b9b416857bbf70e07c15840f7` and its SHA-512 is `f6fe497099dd40bd317f7e2586fff2b65c72391c7d95feb9b994fda1c1cb91ac57622955d63c50da4c6bc53c52e121a3256b23bf54bffc878c6f9b4653ae37ee`.

MixinExtras Forge 0.5.3 was fetched from Maven Central into the same fixture. Its SHA-256 is `89d60f6bf1f29664319acfa80e777abc03fde674370af52e94a9a2e452b98833` and its SHA-512 is `573fc5ed12ae10074db54d0713b2b0ba154fa94734fa8b1202e9888b3b5667a418eb177cd9110a8320470393b06afdfc308d32073e983cfd20f85800d6dc0684`. The archive includes `LICENSE_MixinExtras`, matching the pinned upstream MIT license. The [upstream README](https://github.com/LlamaLad7/MixinExtras/blob/master/README.MD) documents Maven Central and the Forge artifact coordinate, and the [signed 0.5.3 release](https://github.com/LlamaLad7/MixinExtras/releases/tag/0.5.3) identifies commit `d2e3450`. The [upstream GitHub advisory page](https://github.com/LlamaLad7/MixinExtras/security/advisories) showed no published repository advisories at the observation time. These checks cover artifact identity, its included notice, and that repository page only, not the full dependency graph or all vulnerability sources.

The reobfuscated SEF artifact was built from checkout commit `e59d90c95375936349222ef7667510d029c628e4`. `build/libs/sef-1.20.1-1.1.jar` has SHA-256 `90ff6e7f3c7ad243b119f320ac280eee64888bff5c076c50f28832e955be07ec` and SHA-512 `bea7b30a1d1c6fd0d8c9c1bcc25c676fb839bcc99fe777a7b360a3e6de4128fb1157b3ea2ea867692bb95f118aed877566092b109724ba54b27e2ad1ec0a3b2f`. Static archive-name inspection found no classes under Minecraft client or Blaze3D packages; server-side tab-list packet handling remains in the archive. This is not a runtime compatibility result.
