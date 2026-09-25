# Phase 000 fixture cleanup receipt

Cleanup completed after the production Forge, Velocity, and laptop route retry at `P000-TASK-002`.

| resource | result |
| --- | --- |
| `/mnt/hermes/projects/Sef/.test-runs/phase-000/fixture-20260924-a` | absent after exact owned file and directory cleanup |
| `/mnt/hermes/projects/Sef/.test-runs/phase-000/fixture-20260924-b` | absent after exact owned file, symlink, and directory cleanup |
| `/mnt/hermes/projects/Sef/.test-runs/phase-000/fixture-20260925-c` | absent after exact owned file, symlink, and directory cleanup |
| `/mnt/hermes/projects/Sef/.test-runs/phase-000/fixture-20260925-d` | absent after exact owned file, symlink, and directory cleanup |
| `/mnt/hermes/projects/Sef/.test-runs/phase-000/fixture-20260925-e` | absent after exact owned file, symlink, and directory cleanup |
| `/home/envy/.local/share/PrismLauncher/instances/sef-phase-000-client-20250925-c` | absent after exact isolated client cleanup |
| `/home/envy/.local/share/PrismLauncher/instances/sef-phase-000-client-20250925-d` | absent after exact isolated client cleanup |
| `/home/envy/.local/share/PrismLauncher/instances/sef-phase-000-client-20250925-e` | absent after exact isolated client cleanup |
| `/mnt/hermes/projects/Sef/runServer` | absent after registered symlink removal |
| `/mnt/hermes/projects/Sef/build` | absent after exact test build output cleanup |
| Forge backend A | controlled termination after complete world save, exited with status 143 |
| Forge backend B | controlled termination after complete world save, exited with status 143 |
| Velocity process | controlled interrupt after clean shutdown, exit status 130 |
| laptop client and audio watcher | disposable client PID `4065055` terminated before cleanup; stream node `151` was muted and disappeared; failed relaunch processes were terminated; personal FutureShops client and stream were preserved |
| shared caches | preserved outside the registered fixture and build paths |
| retained evidence | `fixture-register.md`, `fixture-result.md`, and this receipt |

The exact `fixture-20260925-e` node root, laptop instance, temporary Gradle build output, and task scratch files are absent. The phase compatibility gate remains open because backend switching, B to A transfer, and the fresh post-negative legitimate control were not proven.
