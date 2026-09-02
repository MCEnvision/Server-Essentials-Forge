# Phase 10 Verification

Final status, 2026-07-27: Complete. This file preserves earlier verification history. Pending or blocked labels below describe the earlier run and are superseded by the final matrix in `SEF2_ACCEPTANCE.md`.

## Scope

Phase 10 replaces command only GUI placeholders with a server generated route for every player facing catalog action. It also adds persisted GUI preferences, reusable category pages, revisioned administrative panel drafts and publications, typed panel controls, bounded batch planning, command profile draft validation, and descriptor linting.

## Automated verification

Run with Java 21:

```bash
JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 ./gradlew test --no-daemon
```

The Phase 10 implementation passes the complete 263 test suite.

The focused tests cover:

- One GUI route and command fallback for every player facing catalog action.
- HUD descriptor or explicit no HUD rationale coverage.
- Zero command only panel descriptors.
- Vanilla descriptor linting.
- Administrative panel draft conflicts, publication, revision history, rollback, persistence, and forged action rejection.
- Separate list, inspect, run, draft, publish, and rollback permissions in the Brigadier tree.
- GUI preference permission isolation and persistence.
- Small cohort same tick admission, paced execution selection, exact context permissions, frozen targets, and all or nothing oversized cohort rejection.
- Typed command profile draft compilation and unsafe context rejection.
- Protocol capability masking when command mode, pause controls, or HUD controls are disabled.

## Runtime verification still required before release approval

The final release audit must repeat the Phase 9 mixed client matrix with every Phase 10 category visible. It must also inspect panel refresh after publication, permission revocation while a screen is open, narration, keyboard focus, supported GUI scales, long localized labels, overlay coexistence, and command fallback parity. These checks are release gates and are not inferred from unit tests.
