# phase 000 evidence manifest

This manifest records the bounded source evidence from `P000-TASK-001` and the candidate and fixture receipts from `P000-TASK-002` and `P000-TASK-003`.
The fixture result records production backend and proxy readiness, private routing, a legitimate client login, signed chat, client input, a failed backend switch, and a fresh direct forwarding negative. It does not claim successful two way switching, a fresh post-negative login, database behavior, forged message rejection, or production compatibility.

## identity and immutability

| item | value |
| --- | --- |
| target baseline | `1e8bab26d9d6ff6b1bf1d5ef41eb8d6c1a51ad98` |
| pinned reference | `e160a235b19c992b3a23c3a43754e92ad0147948` |
| action rows | 738 |
| configuration modules | 62 |
| registered plan files | 18 |
| goal sha256 | `345d32996029ad05732f46715e76803a1fe9cfc33ce516353e5ab5156ca63113` |
| active phase sha256 | `bc8162984c78bd026c6a56a68f6ac4f46ea52d13261e23a46dcf65119aeef221` |

The validator confirmed all registered plan paths exist, every action and module row has an allowed disposition, GUI descriptor rows are excluded, and the immutable goal and cursor hashes are unchanged.

## packet file hashes

| file | sha256 |
| --- | --- |
| `README.md` | `c3c8fdd36618368b1313b1f80c88f21f8aab6da35aacef50aeb65cf1b02132fb` |
| `source-boundary.md` | `4f2bb12397919fe2b9cee33678dcd4247e8433625fdae7da58c255e469534f99` |
| `action-inventory.md` | `17824445d8834c4c81b3970c1fe8f27329b0b048106874b3bd2207d06c809c84` |
| `action-inventory-001.md` | `cc1f381d74c3029b8261fce351fc812ea1466764080dd8646b0903da07d8b203` |
| `action-inventory-002.md` | `2ca210a041959c54ec7ff3ca2dc0fc3656519910d0747b77da5e92addbcafc6b` |
| `action-inventory-003.md` | `f50705f36ba55c08b33bc6157fff8c116095e30f80a5137e10c1a2fb12989e06` |
| `action-inventory-004.md` | `53020c1ed548849c0f3ca4d188e3bffe2ee941451b824fd4b8ed2570a25bec98` |
| `action-inventory-005.md` | `66e5c1f077f8dd136b90512e3c49bb0111f6330632f6d2b1093a74fe5543a06a` |
| `action-inventory-006.md` | `edf10765b276c57ab41f5bc30a09d7268d2434cee112aaa46462f66cde77da87` |
| `action-inventory-007.md` | `812892a7febcb89f3321cc14021ffc63da7011b9e1ebd69968e4d5e7a850bb15` |
| `action-inventory-008.md` | `fc1d514c58014a7c6be4edee67ab1db41211713dcca3cc22a70e965575f65850` |
| `configuration-modules.md` | `f1aecc92b33097211046121b07ac55105cf17000f67929fc3c6ffa7ca60a076c` |
| `fixture-register.md` | `b955d7466e85ae7ad0935b09637ca9a7e9ff7cca622bc3c4caa8229e7976582e` |
| `fixture-result.md` | `6d400d67911d63a9fe602b83505a9cbc0b933d20082b64fae6c1710969ed54f0` |
| `cleanup-receipt.md` | `74c01ce5938542a99bb3d1350ec75471efc36f1d05e940069655d9d7510260a8` |
