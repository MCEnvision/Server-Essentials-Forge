# Performance Report

Measured on 2026-07-30 with Java `21.0.11`, 20 available processors, Minecraft `1.21.1`, and NeoForge `21.1.235`.

These deterministic metadata and configuration workloads run outside the logical server tick. The final row records the dedicated-server tick profile captured during the release matrix.

| Workload | Operations | Measured | Hard budget | Result |
| --- | ---: | ---: | ---: | --- |
| Generate the complete modular configuration reference | 100 | 322 ms | 10000 ms | pass |
| Generate the complete command reference | 1 | 4 ms | 10000 ms | pass |
| Generate the complete permission reference | 1 | 14 ms | 10000 ms | pass |
| Resolve sealed command catalog entries | 250000 | 22 ms | 5000 ms | pass |
| Resolve typed server control schemas | 250000 | 100 ms | 5000 ms | pass |
| Dedicated server tick profile | 480 ticks | 20.04 TPS | 20 TPS minimum | pass |

The deterministic test fails on a budget breach. File watching remains debounced and performs no per tick filesystem polling. The runtime profile ran for 23.95 seconds on 2026-07-27. Enhanced and fallback clients also remained connected through their bounded smoke windows.
