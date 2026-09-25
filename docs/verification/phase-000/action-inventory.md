# phase 000 action inventory index

The complete pinned action inventory is split into bounded files so each review remains readable. Together the eight command files contain all 738 generated command action rows from `docs/COMMAND_REFERENCE.md` at reference commit `e160a235b19c992b3a23c3a43754e92ad0147948`. The module file contains all 62 configuration module rows.

| rows | file |
| ---: | --- |
| 1 through 100 | [action inventory 001](action-inventory-001.md) |
| 101 through 200 | [action inventory 002](action-inventory-002.md) |
| 201 through 300 | [action inventory 003](action-inventory-003.md) |
| 301 through 400 | [action inventory 004](action-inventory-004.md) |
| 401 through 500 | [action inventory 005](action-inventory-005.md) |
| 501 through 600 | [action inventory 006](action-inventory-006.md) |
| 601 through 700 | [action inventory 007](action-inventory-007.md) |
| 701 through 738 | [action inventory 008](action-inventory-008.md) |
| 62 modules | [configuration modules](configuration-modules.md) |

Every row contains an exact generated reference locator, registry boundary, observed status, one owner-approved disposition, feature and permission closure, test locator or explicit absence, and the static evidence limit. The [source boundary](source-boundary.md) explains the pinned identity, exclusions, classification rules, and why static rows do not close runtime compatibility.

## classification summary

The bounded classifier assigns economy actions and their transitive configuration to economy removal, custom GUI, HUD, Fancy Tags, GUI policy, and menu dependent actions to interface removal, disguise actions to adapted server-only review, and all other registered server actions to retained. No action or module row is unclassified. A later phase may not reinterpret an excluded row without an authorized plan amendment.

The exact action id test scan across the pinned `src/test/java` tree found action identifiers in 18 test files. Rows without an exact match explicitly say `none located by exact action id scan`; this is an identified coverage gap, not a claim that the feature is broken or runtime verified.
