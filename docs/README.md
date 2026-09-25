# Documentation

## Existing Product

The [README](../README.md) and [technical documentation](../DOCUMENTATION.md) describe the existing Forge product. The network backport below is planned work, not a released capability.

## Network Backport Plan

The [master plan](general/plan.md) defines the complete product contract and links every execution blueprint from Phase 000 through Phase 016.

- [Plan manifest](general/plan.index.json). Registered master and phase files.
- [Execution handoff](general/plan.handoff.json). Deterministic contract projection, not an active execution goal.
- [Shared contract projection](general/shared-contracts.json). Supporting interface and ownership consistency data.
- [Research brief](general/research/brief.md). Scope, decisions, evidence, and remaining runtime verification boundaries.
- [Repository map](general/research/repository-map.md). Observed code relationships and source fingerprints.
- [Evidence index](general/research/evidence.json) and [resolved intake](general/research/intake.json). Supporting source and decision records.
- [Phase 000 verification packet](verification/phase-000/README.md). Pinned action inventory, candidate revalidation, fixture blocker, and cleanup receipt.

The plan targets the Forge backend on `forge-1.20.1` and the Velocity companion on `velocity-latest`. It does not implement either artifact, publish a release, deploy production, or create an execution goal.
