Objective:
Complete every mandatory requirement and every stable-release gate through implemented, integrated, exercised, and highest-fidelity verified real behavior. Successful completion is permitted only when runtime verification and the final plan-wide audit pass, the artifact is bound to the frozen candidate commit, the endpoint is satisfied, and no known mandatory repository-owned defect remains.

Immediate checkpoint:
Active phase: SEFAUD-PHASE-000
Active phase plan: /mnt/hermes/projects/SEFPORTED/docs/general/phases/plan-phase-000.md
Active phase entry action: Execute P000-TASK-001 to freeze the execution revision, candidate lineage, dirty state, and artifact inputs while preserving `.playwright-mcp/`.

Perform one bounded inspection of the registered plan set and existing evidence. It ends as soon as each mandatory criterion is classified as implemented with valid evidence, incomplete, evidence invalidated or stale, or externally blocked. Immediately execute the first incomplete or stale-evidence criterion. The map is not a deliverable. Do not stop after producing the map, do not repeatedly rebuild unchanged evidence, and do not produce a narrative audit before implementation.

Authoritative plan:
Plan: /mnt/hermes/projects/SEFPORTED/docs/general/plan.md
Plan SHA-256: 46e8974a1fee4c900c99e7d259e1308877add7cfeee94627fc3fdf517bb7479d
Plan manifest: /mnt/hermes/projects/SEFPORTED/docs/general/plan.index.json
Plan set SHA-256: 893d53768d91c9704c97cc286404028b7e92c846c13d470fb3acfc925e829167
Phase plans directory: /mnt/hermes/projects/SEFPORTED/docs/general/phases
The master, manifest, and eight phase files form the complete registered plan set and its creation-time provenance.

Scope includes `SEFAUD-REQ-001` through `SEFAUD-REQ-009` and all eight phases. Owner decisions `DEC-001` through `DEC-008` are resolved and locked. Optional and future work `FUT-001` through `FUT-003` is excluded. Preserve non-goals and compatibility boundaries.

Completion endpoint: At one frozen SEF 2 candidate revision, every mandatory audit and remediation matrix is complete, all confirmed in-scope defects are repaired, highest-fidelity clean-checkout and runtime verification passes, documentation and evidence match the built artifact, and no known critical or high exploitable vulnerability, authorization bypass, sensitive-data leak, executable administrator-command defect, UI-blocking defect, persistence-integrity defect, or mandatory backend-integration defect remains.

Repository root: /mnt/hermes/projects/SEFPORTED
Observed checkout branch: envy/sef2_complete
Observed checkout commit: ffc105af52e592b980541745275e9c1a1b3d50b6
Authoritative remote:
origin
https://github.com/MCEnvision/Server-Essentials-Forge.git
Observed local default branch: forge-1.20.1
Observed local default-branch commit: 1e8bab26d9d6ff6b1bf1d5ef41eb8d6c1a51ad98
Observed local remote-tracking ref: origin/forge-1.20.1
Observed local remote-tracking commit: 1e8bab26d9d6ff6b1bf1d5ef41eb8d6c1a51ad98
Current remote default-branch head: 1e8bab26d9d6ff6b1bf1d5ef41eb8d6c1a51ad98
Remote-head evidence: Read-only git ls-remote and hosting service repository evidence observed 2026-09-01.
Authoritative working baseline: established.
Applicable implementation branch: envy/sef2_complete at ffc105af52e592b980541745275e9c1a1b3d50b6, tracking origin/envy/sef2_complete at the same commit
Applicable open pull request: none identified at checkpoint

Execution behavior:
Verify the authoritative plan, repository identity, package metadata, and remote describe the same project. At resumption, verify `origin` is the intended repository, then refresh and inspect the default branch: fetch `origin` without altering the remote. Verify the fetched remote-tracking ref against the current remote default-branch head. Classify the local default branch as equal, behind, ahead, or diverged. Fast-forward only when safe. Do not reset, force, discard, or overwrite unexpected history. Search local branches and remote branches and repository-wide open pull requests. Resume the applicable active branch; otherwise create an implementation branch from the verified authoritative baseline. Do not invent a new branch when an applicable active branch exists. Create or resume the implementation branch before modifying tracked files. Do not commit directly to the default branch; use safe fast-forward or authorized pull-request integration.

Read the active phase blueprint. Execute tasks in dependency order, repair defects, produce required evidence, and pass exit criteria and evidence gates. For each defect, determine root cause, make the smallest correct repair, add regression coverage, rerun narrow and higher-level gates, inspect adjacent behavior, and continue through subsequent mandatory work. A technical blocker is work: record it and choose the next action advancing a mandatory criterion.

Never stack phase branches. Complete verification, available independent review, pull-request integration, candidate and authoritative default branch checks, and the signed phase tag before the next phase. Then reread the next contiguous phase file and continue under the same immutable goal. Only the final phase and its plan-wide Definition of Done and final proof may satisfy full-plan completion. Documentation changes do not substitute for implementation.

Guardrails and authority:
Treat `/mnt/hermes/projects/SEFPORTED/docs/plan/goal.md` as immutable create-once execution authority. Never refresh, rewrite, rebind, overwrite, or replace the saved goal. Never invoke or run Plan Creator or Goal Creator, or spawn their authors. `plan.md`, `plan.index.json`, and `plan.handoff.json` remain live planning artifacts. Plan set digests are creation-time provenance, not runtime locks. A plan or handoff digest change does not invalidate the goal or require refresh or rebinding. Inspect and classify current plan changes, then reread the current authoritative plan set. Routine progress, evidence, status, clarification, and phase-transition changes continue without owner input. Material product-contract change alone routes to `PLAN_REVISION_REQUIRED`.

Never weaken, skip, disable, delete, or narrow valid tests. Never suppress a valid failure, ignore a required exit code, reduce a required threshold, or allow a required check to fail. Never add a production bypass solely for tests or substitute mocked behavior for required real behavior. If a test contradicts the contract or plan, prove the contradiction and replace it with equal or stronger coverage. Do not rerun the same unchanged failing check more than twice without changing code, configuration, environment, instrumentation, or the diagnostic hypothesis. Avoid repeating failed approaches.

Use disposable synthetic identities, worlds, data, provider fixtures, and approved test surfaces. Do not publish, deploy, use production worlds, disclose credentials, or introduce secret-bearing files. Preserve compatibility, side boundaries, packet validation, least privilege, and unrelated state, including `.playwright-mcp/`. Historical evidence is stale unless bound to the frozen revision; parser, catalog, or command-root presence never substitutes for exercised behavior.

Verification and stopping:
Run every phase-required formatting, static analysis, unit, data-generation, GameTest, build, dedicated-server, client, multiplayer, reconnect, integration, recovery, performance, clean-checkout, artifact, documentation, and security gate at the specified fidelity. Keep evidence revision-bound and invalidate stale evidence after affecting changes. Finish with `git status`, `git diff --check`, and `git log`; inspect the complete diff and built artifact for unrelated output, absolute paths, debug residue, generated drift, and secret-bearing files; verify the authoritative remote branch and frozen candidate binding.

Permitted terminal states: `SUCCESS` only after the completion endpoint and final plan-wide audit pass. `PLAN_REVISION_REQUIRED` reports affected stable IDs and the owner decision, and applies only to a material product-contract change in scope, endpoint, cost, licensing, public behavior, trust boundaries, destructive behavior, credentials, external communication, or irreversible remote state. `GOAL_REVISION_CONFLICT` reports the expected goal digest and observed goal digest only if the saved goal changes, without repair, refresh, or rebinding. `OWNER_INPUT_REQUIRED — REPOSITORY MISMATCH` applies only when project identity fails. `REPOSITORY_STATE_CONFLICT` applies only to unsafe or irreconcilable history.

Before returning either repository state, attempt or exhaust safe non-destructive inspection of repository metadata and live remote evidence. Plan or handoff digest drift is not a stopping or terminal state. No other early stopping state is permitted; incomplete implementation, defects, failed checks, uncertainty, phase completion, commits, pushes, pull requests, compilation, unit tests, documentation, long runtime, or compaction require continued work.

Continuity:
Maintain the requirement map and ledger as temporary internal continuity state. Do not commit or publish them to `plan.md`, `status.md`, issues, pull requests, or repository documentation unless the plan explicitly requires an evidence artifact. Record the active phase ID and file, active task, frozen revision, valid evidence, stale evidence, confirmed defects, attempted fixes, completed phase gates, unresolved mandatory criteria, and the next contiguous phase. On resume, recovery, compaction, handoff, or detected plan change, reread the current authoritative plan set, verify the immutable goal, reconstruct only missing continuity state, and continue from the first unfinished mandatory action. Never refresh the goal at a phase transition.
