# phase 000 evidence manifest

This manifest records the bounded source evidence from `P000-TASK-001` and the candidate and fixture receipts from `P000-TASK-002` and `P000-TASK-003`.
The fixture result records backend and proxy readiness, private routing, a legitimate login, signed chat and command input in bounded runs, the direct forwarding negative, and a later fresh legitimate control with a real SEF `/msg` dispatch. The adapter disconnected the client during both switch directions without its optional reset feature; later manual reconnects reached both backends but do not prove completed switches. The runs lack a single correlation UUID across the required positive and negative sequence, so `SEF-AC-010` remains open. No database behavior, administrative message-forgery rejection, or production compatibility is claimed.

## identity and immutability

| item | value |
| --- | --- |
| target baseline | `1e8bab26d9d6ff6b1bf1d5ef41eb8d6c1a51ad98` |
| pinned reference | `e160a235b19c992b3a23c3a43754e92ad0147948` |
| current phase commit used for h target jar | `05fdad465c22b27761836fa2cfb692ebed742c69` |
| h target jar sha256 | `643cdd0933f7590c69344f2752c0fba6cebb16a93285edef4c83d45ad6bc7f0f` |
| h PCF configuration sha256, both backends | `7ad8bae9f491085770e4dae588c3873ebc90a9122913a0e8223dfd20bab311af` |
| h non-secret Velocity configuration sha256 | `67b35eb6ac3be7156a2d7636a68e18e03b2cf3efddaf12a4508eccd16cf2c2b1` |
| action rows | 738 |
| configuration modules | 62 |
| registered plan files | 18 |
| goal sha256 | `345d32996029ad05732f46715e76803a1fe9cfc33ce516353e5ab5156ca63113` |
| active phase sha256 | `bc8162984c78bd026c6a56a68f6ac4f46ea52d13261e23a46dcf65119aeef221` |

The validator confirmed all registered plan paths exist, every action and module row has an allowed disposition, GUI descriptor rows are excluded, and the immutable goal and cursor hashes are unchanged.

## packet file hashes

| file | sha256 |
| --- | --- |
| `README.md` | `944bb76b7fc4312f5a92878e6e2d843315a075444e45e7b540c404b0528ec761` |
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
| `fixture-register.md` | `885f116943c9194bad14ac8a59b30cd6b9c10dbc7b41f3f03f7eeac6eea4ac2f` |
| `fixture-result.md` | `97a6ef3ade39899045c136295ea926ac62a1b1e59a5bfd0d93c8d15c6ff73216` |
| `cleanup-receipt.md` | `5fdd96b593bb449df959bbd15804ff4af0fde4ff3b26c7a5a54b113e39fdef32` |
