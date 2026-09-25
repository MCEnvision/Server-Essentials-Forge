# Phase 000 fixture cleanup receipt

Cleanup completed after the production Forge, Velocity, and laptop route retry at `P000-TASK-002`.

| resource | result |
| --- | --- |
| `/mnt/hermes/projects/Sef/.test-runs/phase-000/fixture-20260924-a` | absent after exact owned file and directory cleanup |
| `/mnt/hermes/projects/Sef/.test-runs/phase-000/fixture-20260924-b` | absent after exact owned file, symlink, and directory cleanup |
| `/mnt/hermes/projects/Sef/.test-runs/phase-000/fixture-20260925-c` | absent after exact owned file, symlink, and directory cleanup |
| `/mnt/hermes/projects/Sef/.test-runs/phase-000/fixture-20260925-d` | absent after exact owned file, symlink, and directory cleanup |
| `/mnt/hermes/projects/Sef/.test-runs/phase-000/fixture-20260925-e` | absent after exact owned file, symlink, and directory cleanup |
| `/mnt/hermes/projects/Sef/.test-runs/phase-000/fixture-20260925-g` | absent after exact owned file and directory cleanup |
| `/home/envy/.local/share/PrismLauncher/instances/sef-phase-000-client-20250925-c` | absent after exact isolated client cleanup |
| `/home/envy/.local/share/PrismLauncher/instances/sef-phase-000-client-20250925-d` | absent after exact isolated client cleanup |
| `/home/envy/.local/share/PrismLauncher/instances/sef-phase-000-client-20250925-e` | absent after exact isolated client cleanup |
| `/home/envy/.local/share/PrismLauncher/instances/sef-phase-000-client-20250925-g` | absent after exact isolated client cleanup |
| `/mnt/hermes/projects/Sef/runServer` | absent after registered symlink removal |
| `/mnt/hermes/projects/Sef/build` | absent after exact test build output cleanup |
| Forge backend A | controlled termination after complete world save, exited with status 143 |
| Forge backend B | controlled termination after complete world save, exited with status 143 |
| Velocity process | controlled interrupt after clean shutdown, exit status 130 |
| laptop client and audio watcher | disposable client PID `4065055` terminated before cleanup; stream node `151` was muted and disappeared; failed relaunch processes were terminated; personal FutureShops client and stream were preserved |
| shared caches | preserved outside the registered fixture and build paths |
| retained evidence | `fixture-register.md`, `fixture-result.md`, and this receipt |

The follow up g backend processes reached their normal stopping and world save records at `01:56:40` before exit. No audio watcher or owned playback stream was created because the client sound engine disabled itself during startup. The unrelated Docker listeners on `192.168.1.242:25566` and `192.168.1.242:25567` were preserved.

The later cleanup audit found an empty residual directory skeleton under `fixture-20260925-e` that the earlier receipt had incorrectly reported as absent. No process or mount held it. The exact owned skeleton was removed with the same bounded cleanup procedure and rechecked absent. The exact `fixture-20260925-e` and `fixture-20260925-g` node roots, laptop instances, temporary Gradle build output, and task scratch files are now absent. The phase compatibility gate remains open because completed backend switching was not proven; fixture h later supplied a fresh post-negative legitimate login control.

## fixture h cleanup

The h client command and fresh-control evidence was collected before teardown. Both backends received `save-all` and confirmed `Saved the game`; `stop` then saved players and all world dimensions before both processes exited. The proxy was interrupted only after client exit and its test listener closed.

| resource | result |
| --- | --- |
| `/mnt/hermes/projects/Sef/.test-runs/phase-000/fixture-20260925-h` | absent after exact registered root cleanup |
| `/mnt/hermes/projects/Sef/build` | absent after exact test build output cleanup |
| `/home/envy/.local/share/PrismLauncher/instances/sef-phase-000-client-20250925-h` | absent after exact isolated client cleanup |
| `/tmp/sef-phase-000-client-20250925-h-command.png` | absent after exact scratch file removal |
| `/tmp/sef-phase-000-client-20250925-h-launch2.log` | absent after exact scratch file removal |
| Forge backend A PID `3470771` | exited after explicit world save and `stop` |
| Forge backend B PID `3470772` | exited after explicit world save and `stop` |
| Velocity fixture PID `3472789` | exited after interrupt; the test listener closed |
| laptop Minecraft PID `383976` and Prism PID `383206` | exited after exact process termination; X11 window and PipeWire sink input `726` disappeared |
| laptop audio watcher | none was started |
| unrelated Docker listeners on `192.168.1.242:25566` and `192.168.1.242:25567` | preserved |
| unrelated `/home/container` Velocity PID `3453964` | preserved |

The remaining h evidence is limited to the sanitized tracked packet. The h roots, client instance, exact scratch files, test build directory, server processes, proxy process, test listener, X11 window, and owned playback stream were rechecked absent after cleanup.
