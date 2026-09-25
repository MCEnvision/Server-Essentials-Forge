# Configurable Lifecycle Messages

Observed September 24, 2026, at 23:08 UTC. Source role: owner_request with supporting repository observations.

## Approved Requirement

Provide editable main-configuration templates for separate join, leave, first welcome and welcome-back families. Include variant lists and safe placeholders such as `{player_name}` using the approved shared native chat style. This is a configuration editor, not a graphical screen or client dependency. Economy remains excluded.

## Existing Source Evidence

CodeGraph returned `ConnectionMessageService`, `MessageService` and `ReminderService` in the read-only SEFPORTED reference revision recorded in the source register.

- `src/main/java/com/enviouse/sef/social/ConnectionMessageService.java:20` allows the existing `player`, `username`, `uuid` and `world` placeholders. Lines 28 through 59 choose per-player or default join/leave templates, compile and render them, fall back to vanilla on failure and retain subject identity for visibility. Three vanish mixin callers were identified. SHA-256: `bada9042040c07df0c1238223867ae2c3f54d77895f08f603ef41210270cb7d9`.
- `src/main/java/com/enviouse/sef/message/MessageService.java:19` parses brace placeholders and bounds templates to 4,096 characters and output to 16,384 characters. The graph identifies fourteen compile callers and `MessageServiceTest`. SHA-256: `fff18201b312b2fcbd2339a740fc24ce74f0c392092a7ef925f3451db7b10c4b`.
- `src/main/java/com/enviouse/sef/social/ReminderService.java` provides login, scheduling and delivery called by `PlayerEventHandler`; social commands also expose first-join reminder preview and delivery. SHA-256: `ad8464152ece3a2575cbc137f19de3aa689ae5ee252f22a3393b273f9086a1e2`.

These observations identify reusable concepts, not proof of the new network lifecycle, UUID history, `{player_name}` or variants. No reference source or runtime was changed.

## Required Behavior

Use typed, bounded, independently enabled families, documented defaults, variant selection and audience policy. Compile startup and reload proposals into immutable snapshots. Invalid reload preserves the last valid generation and reports the setting and error. Placeholders are allowlisted literal replacements without expression evaluation, arbitrary commands or external expansion.

Authenticated UUID history distinguishes first and returning visits durably in local and network scopes. Announcements follow successful admission. Backend switches, failed connections, retries, duplicate callbacks and stale sessions cannot produce false first visits or repeated welcomes. Delivery ambiguity after a crash remains explicit because client display and database state are not one transaction; uncertain historical announcements are not replayed as new events.

Default join and leave to visibility-filtered public audiences, and first/returning welcomes to the subject. Suppress vanished subjects and hidden counts for unauthorized observers, including hover values. Network mode has one lifecycle authority and suppresses duplicate backend and vanilla messages. Previous-visit placeholders use pre-update historical values with explicit missing-data behavior.

The catalog includes player name, display name, UUID, server name and ID, world and dimension, visible online counts and authorized previous-visit information. Public templates exclude addresses, secrets, sensitive coordinates and hidden presence. Test variant selection, repeat avoidance, multiline and output bounds, literal brace escaping, atomic reload, permission-protected preview, migration and console output. Reuse the shared renderer rather than creating another formatting engine. The master and Phase 006 define the exact current schema and limits.
