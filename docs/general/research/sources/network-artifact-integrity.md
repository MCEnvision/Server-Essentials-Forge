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
