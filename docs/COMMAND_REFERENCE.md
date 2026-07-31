# Command Reference

This file is generated from the sealed command, shortcut, and GUI descriptor registries. Change registry metadata and run `./gradlew generateProjectReferences` instead of editing this file.

Catalog entries: 694. Shortcut entries: 315. GUI descriptors: 25.

## Commands

### `sef:accessgrant.create`

* Description: Executes the `/accessgrant create` action through the shared policy pipeline.
* Usage: `/accessgrant create`.
* Canonical route: `accessgrant create`.
* Example: `/accessgrant create`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.accessgrant.create`.
* Access class: `owner`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `required_player`.
* Cooldown policy: `sef:accessgrant.create`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:accessgrant.profile.publish`

* Description: Executes the `/accessgrant profile publish` action through the shared policy pipeline.
* Usage: `/accessgrant profile publish`.
* Canonical route: `accessgrant profile publish`.
* Example: `/accessgrant profile publish`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.accessgrant.profile.publish`.
* Access class: `owner`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:accessgrant.profile.publish`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:accessgrant.profile.retire`

* Description: Executes the `/accessgrant profile retire` action through the shared policy pipeline.
* Usage: `/accessgrant profile retire`.
* Canonical route: `accessgrant profile retire`.
* Example: `/accessgrant profile retire`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.accessgrant.profile.retire`.
* Access class: `owner`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:accessgrant.profile.retire`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:accessgrant.reconcile`

* Description: Executes the `/accessgrant reconcile` action through the shared policy pipeline.
* Usage: `/accessgrant reconcile`.
* Canonical route: `accessgrant reconcile`.
* Example: `/accessgrant reconcile`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.accessgrant.reconcile`.
* Access class: `owner`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:accessgrant.reconcile`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:accessgrant.renew`

* Description: Executes the `/accessgrant renew` action through the shared policy pipeline.
* Usage: `/accessgrant renew`.
* Canonical route: `accessgrant renew`.
* Example: `/accessgrant renew`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.accessgrant.renew`.
* Access class: `owner`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:accessgrant.renew`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:accessgrant.resume`

* Description: Executes the `/accessgrant resume` action through the shared policy pipeline.
* Usage: `/accessgrant resume`.
* Canonical route: `accessgrant resume`.
* Example: `/accessgrant resume`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.accessgrant.resume`.
* Access class: `owner`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:accessgrant.resume`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:accessgrant.revoke`

* Description: Executes the `/accessgrant revoke` action through the shared policy pipeline.
* Usage: `/accessgrant revoke`.
* Canonical route: `accessgrant revoke`.
* Example: `/accessgrant revoke`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.accessgrant.revoke`.
* Access class: `owner`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:accessgrant.revoke`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:accessgrant.suspend`

* Description: Executes the `/accessgrant suspend` action through the shared policy pipeline.
* Usage: `/accessgrant suspend`.
* Canonical route: `accessgrant suspend`.
* Example: `/accessgrant suspend`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.accessgrant.suspend`.
* Access class: `owner`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:accessgrant.suspend`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:adminlock.breakglass.close`

* Description: Executes the `/adminlock breakglass close` action through the shared policy pipeline.
* Usage: `/adminlock breakglass close`.
* Canonical route: `adminlock breakglass close`.
* Example: `/adminlock breakglass close`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.adminlock.breakglass.close`.
* Access class: `owner`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:adminlock.breakglass.close`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:adminlock.breakglass.open`

* Description: Executes the `/adminlock breakglass open` action through the shared policy pipeline.
* Usage: `/adminlock breakglass open`.
* Canonical route: `adminlock breakglass open`.
* Example: `/adminlock breakglass open`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.adminlock.breakglass.open`.
* Access class: `owner`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:adminlock.breakglass.open`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:adminlock.breakglass.profile`

* Description: Executes the `/adminlock breakglass profile` action through the shared policy pipeline.
* Usage: `/adminlock breakglass profile`.
* Canonical route: `adminlock breakglass profile`.
* Example: `/adminlock breakglass profile`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.adminlock.breakglass.profile`.
* Access class: `owner`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:adminlock.breakglass.profile`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:adminlock.challenge`

* Description: Executes the `/adminlock challenge` action through the shared policy pipeline.
* Usage: `/adminlock challenge`.
* Canonical route: `adminlock challenge`.
* Example: `/adminlock challenge`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.adminlock.challenge`.
* Access class: `staff`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `self`.
* Cooldown policy: `sef:adminlock.challenge`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:adminlock.invalidate`

* Description: Executes the `/adminlock invalidate` action through the shared policy pipeline.
* Usage: `/adminlock invalidate`.
* Canonical route: `adminlock invalidate`.
* Example: `/adminlock invalidate`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.adminlock.invalidate`.
* Access class: `owner`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:adminlock.invalidate`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:adminlock.lock`

* Description: Executes the `/adminlock lock` action through the shared policy pipeline.
* Usage: `/adminlock lock`.
* Canonical route: `adminlock lock`.
* Example: `/adminlock lock`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.adminlock.lock`.
* Access class: `staff`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `self`.
* Cooldown policy: `sef:adminlock.lock`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:adminlock.release`

* Description: Executes the `/adminlock release` action through the shared policy pipeline.
* Usage: `/adminlock release`.
* Canonical route: `adminlock release`.
* Example: `/adminlock release`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.adminlock.release`.
* Access class: `owner`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:adminlock.release`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:adminlock.require`

* Description: Executes the `/adminlock require` action through the shared policy pipeline.
* Usage: `/adminlock require`.
* Canonical route: `adminlock require`.
* Example: `/adminlock require`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.adminlock.require`.
* Access class: `owner`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:adminlock.require`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:adminlock.session.close`

* Description: Executes the `/adminlock session close` action through the shared policy pipeline.
* Usage: `/adminlock session close`.
* Canonical route: `adminlock session close`.
* Example: `/adminlock session close`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.adminlock.session.close`.
* Access class: `staff`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `self`.
* Cooldown policy: `sef:adminlock.session.close`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:adminlock.session.open`

* Description: Executes the `/adminlock session open` action through the shared policy pipeline.
* Usage: `/adminlock session open`.
* Canonical route: `adminlock session open`.
* Example: `/adminlock session open`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.adminlock.session.open`.
* Access class: `staff`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `self`.
* Cooldown policy: `sef:adminlock.session.open`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:adminlock.unlock`

* Description: Executes the `/adminlock unlock` action through the shared policy pipeline.
* Usage: `/adminlock unlock`.
* Canonical route: `adminlock unlock`.
* Example: `/adminlock unlock`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.adminlock.unlock`.
* Access class: `staff`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `self`.
* Cooldown policy: `sef:adminlock.unlock`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:alias.create`

* Description: Executes the `/sef alias create` action through the shared policy pipeline.
* Usage: `/sef alias create`.
* Canonical route: `sef alias create`.
* Example: `/sef alias create`.
* Convenience roots: none.
* Category: `automation`.
* Feature gate: `sef.automation`.
* Permissions: `sef.commands.alias.create`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:alias.create`.
* Confirmation: not required.
* Audit class: `config_definition`.
* GUI descriptor: `sef:aliases`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:alias.delete`

* Description: Executes the `/sef alias delete` action through the shared policy pipeline.
* Usage: `/sef alias delete`.
* Canonical route: `sef alias delete`.
* Example: `/sef alias delete`.
* Convenience roots: none.
* Category: `automation`.
* Feature gate: `sef.automation`.
* Permissions: `sef.commands.alias.delete`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:alias.delete`.
* Confirmation: not required.
* Audit class: `config_definition`.
* GUI descriptor: `sef:aliases`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:alias.disable`

* Description: Executes the `/sef alias disable` action through the shared policy pipeline.
* Usage: `/sef alias disable`.
* Canonical route: `sef alias disable`.
* Example: `/sef alias disable`.
* Convenience roots: none.
* Category: `automation`.
* Feature gate: `sef.automation`.
* Permissions: `sef.commands.alias.disable`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:alias.disable`.
* Confirmation: not required.
* Audit class: `config_definition`.
* GUI descriptor: `sef:aliases`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:alias.help`

* Description: Executes the `/sef alias help` action through the shared policy pipeline.
* Usage: `/sef alias help`.
* Canonical route: `sef alias help`.
* Example: `/sef alias help`.
* Convenience roots: none.
* Category: `automation`.
* Feature gate: `sef.automation`.
* Permissions: `sef.commands.alias.help`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:alias.help`.
* Confirmation: not required.
* Audit class: `config_definition`.
* GUI descriptor: `sef:aliases`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:alias.inspect`

* Description: Executes the `/sef alias inspect` action through the shared policy pipeline.
* Usage: `/sef alias inspect`.
* Canonical route: `sef alias inspect`.
* Example: `/sef alias inspect`.
* Convenience roots: none.
* Category: `automation`.
* Feature gate: `sef.automation`.
* Permissions: `sef.commands.alias.inspect`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:alias.inspect`.
* Confirmation: not required.
* Audit class: `config_definition`.
* GUI descriptor: `sef:aliases`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:alias.list`

* Description: Executes the `/sef alias list` action through the shared policy pipeline.
* Usage: `/sef alias list`.
* Canonical route: `sef alias list`.
* Example: `/sef alias list`.
* Convenience roots: none.
* Category: `automation`.
* Feature gate: `sef.automation`.
* Permissions: `sef.commands.alias.list`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:alias.list`.
* Confirmation: not required.
* Audit class: `config_definition`.
* GUI descriptor: `sef:aliases`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:alias.publish`

* Description: Executes the `/sef alias publish` action through the shared policy pipeline.
* Usage: `/sef alias publish`.
* Canonical route: `sef alias publish`.
* Example: `/sef alias publish`.
* Convenience roots: none.
* Category: `automation`.
* Feature gate: `sef.automation`.
* Permissions: `sef.commands.alias.publish`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:alias.publish`.
* Confirmation: not required.
* Audit class: `config_definition`.
* GUI descriptor: `sef:aliases`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:alias.rollback`

* Description: Executes the `/sef alias rollback` action through the shared policy pipeline.
* Usage: `/sef alias rollback`.
* Canonical route: `sef alias rollback`.
* Example: `/sef alias rollback`.
* Convenience roots: none.
* Category: `automation`.
* Feature gate: `sef.automation`.
* Permissions: `sef.commands.alias.rollback`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:alias.rollback`.
* Confirmation: not required.
* Audit class: `config_definition`.
* GUI descriptor: `sef:aliases`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:alias.run`

* Description: Executes the `/sef alias run` action through the shared policy pipeline.
* Usage: `/sef alias run`.
* Canonical route: `sef alias run`.
* Example: `/sef alias run`.
* Convenience roots: none.
* Category: `automation`.
* Feature gate: `sef.automation`.
* Permissions: `sef.commands.alias.run`.
* Access class: `trusted_player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:alias.run`.
* Confirmation: not required.
* Audit class: `workflow_execution`.
* GUI descriptor: `sef:aliases`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:alias.validate`

* Description: Executes the `/sef alias validate` action through the shared policy pipeline.
* Usage: `/sef alias validate`.
* Canonical route: `sef alias validate`.
* Example: `/sef alias validate`.
* Convenience roots: none.
* Category: `automation`.
* Feature gate: `sef.automation`.
* Permissions: `sef.commands.alias.validate`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:alias.validate`.
* Confirmation: not required.
* Audit class: `config_definition`.
* GUI descriptor: `sef:aliases`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:announcement.bulletin`

* Description: Executes the `/opbulletin` action through the shared policy pipeline.
* Usage: `/opbulletin`.
* Canonical route: `opbulletin`.
* Example: `/opbulletin`.
* Convenience roots: `opbulletin`.
* Category: `core`.
* Feature gate: `sef.core`.
* Permissions: `sef.opbulletin.manage`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `server`.
* Cooldown policy: `sef:announcement.bulletin`.
* Confirmation: not required.
* Audit class: `config_definition`.
* GUI descriptor: `sef:core`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:announcement.command`

* Description: Executes the `/commandannouncement` action through the shared policy pipeline.
* Usage: `/commandannouncement`.
* Canonical route: `commandannouncement`.
* Example: `/commandannouncement`.
* Convenience roots: `commandannouncement`.
* Category: `core`.
* Feature gate: `sef.core`.
* Permissions: `sef.announcements.command.manage`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `server`.
* Cooldown policy: `sef:announcement.command`.
* Confirmation: not required.
* Audit class: `config_definition`.
* GUI descriptor: `sef:core`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:announcement.countdown`

* Description: Executes the `/countdown` action through the shared policy pipeline.
* Usage: `/countdown`.
* Canonical route: `countdown`.
* Example: `/countdown`.
* Convenience roots: `countdown`.
* Category: `core`.
* Feature gate: `sef.core`.
* Permissions: `sef.commands.countdown`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `server`.
* Cooldown policy: `sef:announcement.countdown`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:core`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:announcement.text`

* Description: Executes the `/textannouncement` action through the shared policy pipeline.
* Usage: `/textannouncement`.
* Canonical route: `textannouncement`.
* Example: `/textannouncement`.
* Convenience roots: `textannouncement`.
* Category: `core`.
* Feature gate: `sef.core`.
* Permissions: `sef.announcements.manage`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `server`.
* Cooldown policy: `sef:announcement.text`.
* Confirmation: not required.
* Audit class: `config_definition`.
* GUI descriptor: `sef:core`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:announcement.title`

* Description: Executes the `/titleannouncement` action through the shared policy pipeline.
* Usage: `/titleannouncement`.
* Canonical route: `titleannouncement`.
* Example: `/titleannouncement`.
* Convenience roots: `titleannouncement`.
* Category: `core`.
* Feature gate: `sef.core`.
* Permissions: `sef.announcements.title`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `bounded_players`.
* Cooldown policy: `sef:announcement.title`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:core`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:announcement.toggle`

* Description: Executes the `/toggle` action through the shared policy pipeline.
* Usage: `/toggle`.
* Canonical route: `toggle`.
* Example: `/toggle`.
* Convenience roots: `toggle`.
* Category: `core`.
* Feature gate: `sef.core`.
* Permissions: `sef.announcements.toggle`.
* Access class: `player`.
* Sources: `player`.
* Target behavior: `self`.
* Cooldown policy: `sef:announcement.toggle`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:core`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:approval.approve`

* Description: Executes the `/approval approve` action through the shared policy pipeline.
* Usage: `/approval approve`.
* Canonical route: `approval approve`.
* Example: `/approval approve`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.approval.approve`.
* Access class: `owner`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:approval.approve`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:approval.list`

* Description: Executes the `/approval` action through the shared policy pipeline.
* Usage: `/approval`.
* Canonical route: `approval`.
* Example: `/approval`.
* Convenience roots: `approvals`.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.approval.list`.
* Access class: `owner`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:approval.list`.
* Confirmation: not required.
* Audit class: `sensitive_access`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:approval.request`

* Description: Executes the `/approval request` action through the shared policy pipeline.
* Usage: `/approval request`.
* Canonical route: `approval request`.
* Example: `/approval request`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.approval.request`.
* Access class: `owner`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:approval.request`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:approval.revoke`

* Description: Executes the `/approval revoke` action through the shared policy pipeline.
* Usage: `/approval revoke`.
* Canonical route: `approval revoke`.
* Example: `/approval revoke`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.approval.revoke`.
* Access class: `owner`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:approval.revoke`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:banned.list`

* Description: Executes the `/banned` action through the shared policy pipeline.
* Usage: `/banned`.
* Canonical route: `banned`.
* Example: `/banned`.
* Convenience roots: `banned`.
* Category: `core`.
* Feature gate: `sef.core`.
* Permissions: `sef.banned.view`.
* Access class: `player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:banned.list`.
* Confirmation: not required.
* Audit class: `sensitive_access`.
* GUI descriptor: `sef:moderation`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:bundle.cancel`

* Description: Executes the `/sef bundle cancel` action through the shared policy pipeline.
* Usage: `/sef bundle cancel`.
* Canonical route: `sef bundle cancel`.
* Example: `/sef bundle cancel`.
* Convenience roots: none.
* Category: `automation`.
* Feature gate: `sef.automation`.
* Permissions: `sef.commands.bundle.cancel`.
* Access class: `staff`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:bundle.cancel`.
* Confirmation: not required.
* Audit class: `workflow_execution`.
* GUI descriptor: `sef:aliases`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:bundle.create`

* Description: Executes the `/sef bundle create` action through the shared policy pipeline.
* Usage: `/sef bundle create`.
* Canonical route: `sef bundle create`.
* Example: `/sef bundle create`.
* Convenience roots: none.
* Category: `automation`.
* Feature gate: `sef.automation`.
* Permissions: `sef.commands.bundle.create`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:bundle.create`.
* Confirmation: not required.
* Audit class: `config_definition`.
* GUI descriptor: `sef:aliases`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:bundle.delete`

* Description: Executes the `/sef bundle delete` action through the shared policy pipeline.
* Usage: `/sef bundle delete`.
* Canonical route: `sef bundle delete`.
* Example: `/sef bundle delete`.
* Convenience roots: none.
* Category: `automation`.
* Feature gate: `sef.automation`.
* Permissions: `sef.commands.bundle.delete`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:bundle.delete`.
* Confirmation: not required.
* Audit class: `config_definition`.
* GUI descriptor: `sef:aliases`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:bundle.disable`

* Description: Executes the `/sef bundle disable` action through the shared policy pipeline.
* Usage: `/sef bundle disable`.
* Canonical route: `sef bundle disable`.
* Example: `/sef bundle disable`.
* Convenience roots: none.
* Category: `automation`.
* Feature gate: `sef.automation`.
* Permissions: `sef.commands.bundle.disable`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:bundle.disable`.
* Confirmation: not required.
* Audit class: `config_definition`.
* GUI descriptor: `sef:aliases`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:bundle.edit`

* Description: Executes the `/sef bundle edit` action through the shared policy pipeline.
* Usage: `/sef bundle edit`.
* Canonical route: `sef bundle edit`.
* Example: `/sef bundle edit`.
* Convenience roots: none.
* Category: `automation`.
* Feature gate: `sef.automation`.
* Permissions: `sef.commands.bundle.edit`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:bundle.edit`.
* Confirmation: not required.
* Audit class: `config_definition`.
* GUI descriptor: `sef:aliases`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:bundle.inspect`

* Description: Executes the `/sef bundle inspect` action through the shared policy pipeline.
* Usage: `/sef bundle inspect`.
* Canonical route: `sef bundle inspect`.
* Example: `/sef bundle inspect`.
* Convenience roots: none.
* Category: `automation`.
* Feature gate: `sef.automation`.
* Permissions: `sef.commands.bundle.inspect`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:bundle.inspect`.
* Confirmation: not required.
* Audit class: `config_definition`.
* GUI descriptor: `sef:aliases`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:bundle.list`

* Description: Executes the `/sef bundle list` action through the shared policy pipeline.
* Usage: `/sef bundle list`.
* Canonical route: `sef bundle list`.
* Example: `/sef bundle list`.
* Convenience roots: none.
* Category: `automation`.
* Feature gate: `sef.automation`.
* Permissions: `sef.commands.bundle.list`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:bundle.list`.
* Confirmation: not required.
* Audit class: `config_definition`.
* GUI descriptor: `sef:aliases`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:bundle.preview`

* Description: Executes the `/sef bundle preview` action through the shared policy pipeline.
* Usage: `/sef bundle preview`.
* Canonical route: `sef bundle preview`.
* Example: `/sef bundle preview`.
* Convenience roots: none.
* Category: `automation`.
* Feature gate: `sef.automation`.
* Permissions: `sef.commands.bundle.preview`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:bundle.preview`.
* Confirmation: not required.
* Audit class: `config_definition`.
* GUI descriptor: `sef:aliases`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:bundle.publish`

* Description: Executes the `/sef bundle publish` action through the shared policy pipeline.
* Usage: `/sef bundle publish`.
* Canonical route: `sef bundle publish`.
* Example: `/sef bundle publish`.
* Convenience roots: none.
* Category: `automation`.
* Feature gate: `sef.automation`.
* Permissions: `sef.commands.bundle.publish`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:bundle.publish`.
* Confirmation: not required.
* Audit class: `config_definition`.
* GUI descriptor: `sef:aliases`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:bundle.recover`

* Description: Executes the `/sef bundle recover` action through the shared policy pipeline.
* Usage: `/sef bundle recover`.
* Canonical route: `sef bundle recover`.
* Example: `/sef bundle recover`.
* Convenience roots: none.
* Category: `automation`.
* Feature gate: `sef.automation`.
* Permissions: `sef.commands.bundle.recover`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:bundle.recover`.
* Confirmation: not required.
* Audit class: `workflow_execution`.
* GUI descriptor: `sef:aliases`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:bundle.rollback`

* Description: Executes the `/sef bundle rollback` action through the shared policy pipeline.
* Usage: `/sef bundle rollback`.
* Canonical route: `sef bundle rollback`.
* Example: `/sef bundle rollback`.
* Convenience roots: none.
* Category: `automation`.
* Feature gate: `sef.automation`.
* Permissions: `sef.commands.bundle.rollback`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:bundle.rollback`.
* Confirmation: not required.
* Audit class: `config_definition`.
* GUI descriptor: `sef:aliases`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:bundle.run`

* Description: Executes the `/sef bundle run` action through the shared policy pipeline.
* Usage: `/sef bundle run`.
* Canonical route: `sef bundle run`.
* Example: `/sef bundle run`.
* Convenience roots: none.
* Category: `automation`.
* Feature gate: `sef.automation`.
* Permissions: `sef.commands.bundle.run`.
* Access class: `staff`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `bounded_players`.
* Cooldown policy: `sef:bundle.run`.
* Confirmation: not required.
* Audit class: `workflow_execution`.
* GUI descriptor: `sef:aliases`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:chat.admin`

* Description: Executes the `/ac` action through the shared policy pipeline.
* Usage: `/ac`.
* Canonical route: `ac`.
* Example: `/ac`.
* Convenience roots: `ac`, `chat`.
* Category: `core`.
* Feature gate: `sef.core`.
* Permissions: `sef.adminchat.use`.
* Access class: `staff`.
* Sources: `player`.
* Target behavior: `none`.
* Cooldown policy: `sef:chat.admin`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:social`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:chat.clear`

* Description: Executes the `/clearchat` action through the shared policy pipeline.
* Usage: `/clearchat`.
* Canonical route: `clearchat`.
* Example: `/clearchat`.
* Convenience roots: `cc`, `clearchat`.
* Category: `core`.
* Feature gate: `sef.core`.
* Permissions: `sef.commands.clearchat`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `server`.
* Cooldown policy: `sef:chat.clear`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:moderation`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:chat.helpop`

* Description: Executes the `/helpop` action through the shared policy pipeline.
* Usage: `/helpop`.
* Canonical route: `helpop`.
* Example: `/helpop`.
* Convenience roots: `helpop`.
* Category: `core`.
* Feature gate: `sef.core`.
* Permissions: `sef.helpop.send`.
* Access class: `player`.
* Sources: `player`.
* Target behavior: `none`.
* Cooldown policy: `sef:chat.helpop`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:social`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:chat.helpop.reply`

* Description: Executes the `/helpopop` action through the shared policy pipeline.
* Usage: `/helpopop`.
* Canonical route: `helpopop`.
* Example: `/helpopop`.
* Convenience roots: `helpopop`.
* Category: `core`.
* Feature gate: `sef.core`.
* Permissions: `sef.helpop.reply`.
* Access class: `staff`.
* Sources: `player`.
* Target behavior: `required_player`.
* Cooldown policy: `sef:chat.helpop.reply`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:social`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:chat.reply`

* Description: Executes the `/ans` action through the shared policy pipeline.
* Usage: `/ans`.
* Canonical route: `ans`.
* Example: `/ans`.
* Convenience roots: `ans`.
* Category: `core`.
* Feature gate: `sef.core`.
* Permissions: `sef.commands.ans`.
* Access class: `player`.
* Sources: `player`.
* Target behavior: `required_player`.
* Cooldown policy: `sef:chat.reply`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:social`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:commandspy.audience`

* Description: Executes the `/sef commandspy everyone` action through the shared policy pipeline.
* Usage: `/sef commandspy everyone`.
* Canonical route: `sef commandspy everyone`.
* Example: `/sef commandspy everyone`.
* Convenience roots: none.
* Category: `observation.command`.
* Feature gate: `sef.observation.command`.
* Permissions: `sef.commands.commandspy`.
* Access class: `administrator`.
* Sources: `player`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:commandspy.audience`.
* Confirmation: not required.
* Audit class: `command_observation`.
* GUI descriptor: `sef:observation`.
* HUD contract: `command_spy`.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:commandspy.filter`

* Description: Executes the `/sef commandspy filter` action through the shared policy pipeline.
* Usage: `/sef commandspy filter`.
* Canonical route: `sef commandspy filter`.
* Example: `/sef commandspy filter`.
* Convenience roots: none.
* Category: `observation.command`.
* Feature gate: `sef.observation.command`.
* Permissions: `sef.commands.commandspy.filter`.
* Access class: `administrator`.
* Sources: `player`.
* Target behavior: `self`.
* Cooldown policy: `sef:commandspy.filter`.
* Confirmation: not required.
* Audit class: `command_observation`.
* GUI descriptor: `sef:observation`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:commandspy.recent`

* Description: Executes the `/sef commandspy recent` action through the shared policy pipeline.
* Usage: `/sef commandspy recent`.
* Canonical route: `sef commandspy recent`.
* Example: `/sef commandspy recent`.
* Convenience roots: none.
* Category: `observation.command`.
* Feature gate: `sef.observation.command`.
* Permissions: `sef.commands.commandspy.recent`.
* Access class: `administrator`.
* Sources: `player`.
* Target behavior: `self`.
* Cooldown policy: `sef:commandspy.recent`.
* Confirmation: not required.
* Audit class: `command_observation`.
* GUI descriptor: `sef:observation`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:commandspy.scope`

* Description: Executes the `/sef commandspy scope` action through the shared policy pipeline.
* Usage: `/sef commandspy scope`.
* Canonical route: `sef commandspy scope`.
* Example: `/sef commandspy scope`.
* Convenience roots: none.
* Category: `observation.command`.
* Feature gate: `sef.observation.command`.
* Permissions: `sef.commands.commandspy`.
* Access class: `administrator`.
* Sources: `player`.
* Target behavior: `self`.
* Cooldown policy: `sef:commandspy.scope`.
* Confirmation: not required.
* Audit class: `command_observation`.
* GUI descriptor: `sef:observation`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:commandspy.selected`

* Description: Executes the `/sef commandspy selected` action through the shared policy pipeline.
* Usage: `/sef commandspy selected`.
* Canonical route: `sef commandspy selected`.
* Example: `/sef commandspy selected`.
* Convenience roots: none.
* Category: `observation.command`.
* Feature gate: `sef.observation.command`.
* Permissions: `sef.commands.commandspy.selected`.
* Access class: `administrator`.
* Sources: `player`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:commandspy.selected`.
* Confirmation: not required.
* Audit class: `command_observation`.
* GUI descriptor: `sef:observation`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:commandspy.status`

* Description: Executes the `/sef commandspy status` action through the shared policy pipeline.
* Usage: `/sef commandspy status`.
* Canonical route: `sef commandspy status`.
* Example: `/sef commandspy status`.
* Convenience roots: none.
* Category: `observation.command`.
* Feature gate: `sef.observation.command`.
* Permissions: `sef.commands.commandspy.status`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:commandspy.status`.
* Confirmation: not required.
* Audit class: `sensitive_access`.
* GUI descriptor: `sef:observation`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:commandspy.toggle`

* Description: Executes the `/sef commandspy toggle` action through the shared policy pipeline.
* Usage: `/sef commandspy toggle`.
* Canonical route: `sef commandspy toggle`.
* Example: `/sef commandspy toggle`.
* Convenience roots: `commandspy`.
* Category: `observation.command`.
* Feature gate: `sef.observation.command`.
* Permissions: `sef.commands.commandspy`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:commandspy.toggle`.
* Confirmation: not required.
* Audit class: `command_observation`.
* GUI descriptor: `sef:observation`.
* HUD contract: `command_spy`.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:config.diff`

* Description: Executes the `/sef config diff` action through the shared policy pipeline.
* Usage: `/sef config diff`.
* Canonical route: `sef config diff`.
* Example: `/sef config diff`.
* Convenience roots: none.
* Category: `config`.
* Feature gate: `sef.config`.
* Permissions: `sef.commands.config.diff`.
* Access class: `owner`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:config.diff`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:settings`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:config.documentation`

* Description: Executes the `/sef config documentation` action through the shared policy pipeline.
* Usage: `/sef config documentation`.
* Canonical route: `sef config documentation`.
* Example: `/sef config documentation`.
* Convenience roots: none.
* Category: `config`.
* Feature gate: `sef.config`.
* Permissions: `sef.commands.config.documentation`.
* Access class: `owner`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:config.documentation`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:settings`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:config.edit`

* Description: Executes the `/sef config set` action through the shared policy pipeline.
* Usage: `/sef config set`.
* Canonical route: `sef config set`.
* Example: `/sef config set`.
* Convenience roots: none.
* Category: `config`.
* Feature gate: `sef.config`.
* Permissions: `sef.commands.config.edit`.
* Access class: `owner`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:config.edit`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:settings`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:config.explain`

* Description: Executes the `/sef config explain` action through the shared policy pipeline.
* Usage: `/sef config explain`.
* Canonical route: `sef config explain`.
* Example: `/sef config explain`.
* Convenience roots: none.
* Category: `config`.
* Feature gate: `sef.config`.
* Permissions: `sef.commands.config.explain`.
* Access class: `owner`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:config.explain`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:settings`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:config.history`

* Description: Executes the `/sef config history` action through the shared policy pipeline.
* Usage: `/sef config history`.
* Canonical route: `sef config history`.
* Example: `/sef config history`.
* Convenience roots: none.
* Category: `config`.
* Feature gate: `sef.config`.
* Permissions: `sef.commands.config.history`.
* Access class: `owner`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:config.history`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:settings`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:config.inspect`

* Description: Executes the `/sef config inspect` action through the shared policy pipeline.
* Usage: `/sef config inspect`.
* Canonical route: `sef config inspect`.
* Example: `/sef config inspect`.
* Convenience roots: none.
* Category: `config`.
* Feature gate: `sef.config`.
* Permissions: `sef.commands.config.inspect`.
* Access class: `owner`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:config.inspect`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:settings`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:config.migrate`

* Description: Executes the `/sef config migrate` action through the shared policy pipeline.
* Usage: `/sef config migrate`.
* Canonical route: `sef config migrate`.
* Example: `/sef config migrate`.
* Convenience roots: none.
* Category: `config`.
* Feature gate: `sef.config`.
* Permissions: `sef.commands.config.migrate`.
* Access class: `owner`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:config.migrate`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:settings`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:config.modules`

* Description: Executes the `/sef config modules` action through the shared policy pipeline.
* Usage: `/sef config modules`.
* Canonical route: `sef config modules`.
* Example: `/sef config modules`.
* Convenience roots: none.
* Category: `config`.
* Feature gate: `sef.config`.
* Permissions: `sef.commands.config.modules`.
* Access class: `staff`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:config.modules`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:settings`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:config.reload`

* Description: Executes the `/sef config reload` action through the shared policy pipeline.
* Usage: `/sef config reload`.
* Canonical route: `sef config reload`.
* Example: `/sef config reload`.
* Convenience roots: none.
* Category: `config`.
* Feature gate: `sef.config`.
* Permissions: `sef.commands.config.reload`.
* Access class: `owner`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:config.reload`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:settings`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:config.rollback`

* Description: Executes the `/sef config rollback` action through the shared policy pipeline.
* Usage: `/sef config rollback`.
* Canonical route: `sef config rollback`.
* Example: `/sef config rollback`.
* Convenience roots: none.
* Category: `config`.
* Feature gate: `sef.config`.
* Permissions: `sef.commands.config.rollback`.
* Access class: `owner`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:config.rollback`.
* Confirmation: not required.
* Audit class: `destructive`.
* GUI descriptor: `sef:settings`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:config.status`

* Description: Executes the `/sef config status` action through the shared policy pipeline.
* Usage: `/sef config status`.
* Canonical route: `sef config status`.
* Example: `/sef config status`.
* Convenience roots: none.
* Category: `config`.
* Feature gate: `sef.config`.
* Permissions: `sef.commands.config.status`.
* Access class: `staff`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:config.status`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:settings`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:config.validate`

* Description: Executes the `/sef config validate` action through the shared policy pipeline.
* Usage: `/sef config validate`.
* Canonical route: `sef config validate`.
* Example: `/sef config validate`.
* Convenience roots: none.
* Category: `config`.
* Feature gate: `sef.config`.
* Permissions: `sef.commands.config.validate`.
* Access class: `owner`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:config.validate`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:settings`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.access_applications.create`

* Description: Executes the `/sef control access_applications create` action through the shared policy pipeline.
* Usage: `/sef control access_applications create`.
* Canonical route: `sef control access_applications create`.
* Example: `/sef control access_applications create`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.access_applications.create`.
* Access class: `player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.access_applications.create`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.access_applications.manage`

* Description: Executes the `/sef control access_applications manage` action through the shared policy pipeline.
* Usage: `/sef control access_applications manage`.
* Canonical route: `sef control access_applications manage`.
* Example: `/sef control access_applications manage`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.access_applications.manage`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.access_applications.manage`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.access_applications.submit`

* Description: Executes the `/accessapply` action through the shared policy pipeline.
* Usage: `/accessapply`.
* Canonical route: `accessapply`.
* Example: `/accessapply`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.access_applications.create`.
* Access class: `player`.
* Sources: `player`.
* Target behavior: `self`.
* Cooldown policy: `sef:control.access_applications.submit`.
* Confirmation: not required.
* Audit class: `workflow_execution`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.access_applications.view`

* Description: Executes the `/sef control access_applications` action through the shared policy pipeline.
* Usage: `/sef control access_applications`.
* Canonical route: `sef control access_applications`.
* Example: `/sef control access_applications`.
* Convenience roots: `access`, `applications`.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.access_applications.view`.
* Access class: `staff`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:control.access_applications.view`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.admin_journal.create`

* Description: Executes the `/sef control admin_journal create` action through the shared policy pipeline.
* Usage: `/sef control admin_journal create`.
* Canonical route: `sef control admin_journal create`.
* Example: `/sef control admin_journal create`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.admin_journal.create`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.admin_journal.create`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.admin_journal.manage`

* Description: Executes the `/sef control admin_journal manage` action through the shared policy pipeline.
* Usage: `/sef control admin_journal manage`.
* Canonical route: `sef control admin_journal manage`.
* Example: `/sef control admin_journal manage`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.admin_journal.manage`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.admin_journal.manage`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.admin_journal.view`

* Description: Executes the `/sef control admin_journal` action through the shared policy pipeline.
* Usage: `/sef control admin_journal`.
* Canonical route: `sef control admin_journal`.
* Example: `/sef control admin_journal`.
* Convenience roots: `adminjournal`.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.admin_journal.view`.
* Access class: `staff`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:control.admin_journal.view`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.admin_lock.create`

* Description: Executes the `/sef control admin_lock create` action through the shared policy pipeline.
* Usage: `/sef control admin_lock create`.
* Canonical route: `sef control admin_lock create`.
* Example: `/sef control admin_lock create`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.admin_lock.create`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.admin_lock.create`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.admin_lock.manage`

* Description: Executes the `/sef control admin_lock manage` action through the shared policy pipeline.
* Usage: `/sef control admin_lock manage`.
* Canonical route: `sef control admin_lock manage`.
* Example: `/sef control admin_lock manage`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.admin_lock.manage`.
* Access class: `owner`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.admin_lock.manage`.
* Confirmation: not required.
* Audit class: `destructive`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.admin_lock.view`

* Description: Executes the `/sef control admin_lock` action through the shared policy pipeline.
* Usage: `/sef control admin_lock`.
* Canonical route: `sef control admin_lock`.
* Example: `/sef control admin_lock`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.admin_lock.view`.
* Access class: `staff`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:control.admin_lock.view`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.admission.create`

* Description: Executes the `/sef control admission create` action through the shared policy pipeline.
* Usage: `/sef control admission create`.
* Canonical route: `sef control admission create`.
* Example: `/sef control admission create`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.admission.create`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.admission.create`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.admission.manage`

* Description: Executes the `/sef control admission manage` action through the shared policy pipeline.
* Usage: `/sef control admission manage`.
* Canonical route: `sef control admission manage`.
* Example: `/sef control admission manage`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.admission.manage`.
* Access class: `owner`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.admission.manage`.
* Confirmation: not required.
* Audit class: `destructive`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.admission.view`

* Description: Executes the `/sef control admission` action through the shared policy pipeline.
* Usage: `/sef control admission`.
* Canonical route: `sef control admission`.
* Example: `/sef control admission`.
* Convenience roots: `admission`.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.admission.view`.
* Access class: `trusted_player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:control.admission.view`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.afk_zones.create`

* Description: Executes the `/sef control afk_zones create` action through the shared policy pipeline.
* Usage: `/sef control afk_zones create`.
* Canonical route: `sef control afk_zones create`.
* Example: `/sef control afk_zones create`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.afk_zones.create`.
* Access class: `player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.afk_zones.create`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.afk_zones.manage`

* Description: Executes the `/sef control afk_zones manage` action through the shared policy pipeline.
* Usage: `/sef control afk_zones manage`.
* Canonical route: `sef control afk_zones manage`.
* Example: `/sef control afk_zones manage`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.afk_zones.manage`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.afk_zones.manage`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.afk_zones.view`

* Description: Executes the `/sef control afk_zones` action through the shared policy pipeline.
* Usage: `/sef control afk_zones`.
* Canonical route: `sef control afk_zones`.
* Example: `/sef control afk_zones`.
* Convenience roots: `afkzone`.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.afk_zones.view`.
* Access class: `player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:control.afk_zones.view`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.alias_diagnostics.create`

* Description: Executes the `/sef control alias_diagnostics create` action through the shared policy pipeline.
* Usage: `/sef control alias_diagnostics create`.
* Canonical route: `sef control alias_diagnostics create`.
* Example: `/sef control alias_diagnostics create`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.alias_diagnostics.create`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.alias_diagnostics.create`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.alias_diagnostics.manage`

* Description: Executes the `/sef control alias_diagnostics manage` action through the shared policy pipeline.
* Usage: `/sef control alias_diagnostics manage`.
* Canonical route: `sef control alias_diagnostics manage`.
* Example: `/sef control alias_diagnostics manage`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.alias_diagnostics.manage`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.alias_diagnostics.manage`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.alias_diagnostics.view`

* Description: Executes the `/sef control alias_diagnostics` action through the shared policy pipeline.
* Usage: `/sef control alias_diagnostics`.
* Canonical route: `sef control alias_diagnostics`.
* Example: `/sef control alias_diagnostics`.
* Convenience roots: `shortcut`.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.alias_diagnostics.view`.
* Access class: `trusted_player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:control.alias_diagnostics.view`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.appeals.create`

* Description: Executes the `/sef control appeals create` action through the shared policy pipeline.
* Usage: `/sef control appeals create`.
* Canonical route: `sef control appeals create`.
* Example: `/sef control appeals create`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.appeals.create`.
* Access class: `player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.appeals.create`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.appeals.manage`

* Description: Executes the `/sef control appeals manage` action through the shared policy pipeline.
* Usage: `/sef control appeals manage`.
* Canonical route: `sef control appeals manage`.
* Example: `/sef control appeals manage`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.appeals.manage`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.appeals.manage`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.appeals.submit`

* Description: Executes the `/appeal` action through the shared policy pipeline.
* Usage: `/appeal`.
* Canonical route: `appeal`.
* Example: `/appeal`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.appeals.create`.
* Access class: `player`.
* Sources: `player`.
* Target behavior: `self`.
* Cooldown policy: `sef:control.appeals.submit`.
* Confirmation: not required.
* Audit class: `workflow_execution`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.appeals.view`

* Description: Executes the `/sef control appeals` action through the shared policy pipeline.
* Usage: `/sef control appeals`.
* Canonical route: `sef control appeals`.
* Example: `/sef control appeals`.
* Convenience roots: `appeal`, `appeals`.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.appeals.view`.
* Access class: `staff`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:control.appeals.view`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.approvals.create`

* Description: Executes the `/sef control approvals create` action through the shared policy pipeline.
* Usage: `/sef control approvals create`.
* Canonical route: `sef control approvals create`.
* Example: `/sef control approvals create`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.approvals.create`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.approvals.create`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.approvals.manage`

* Description: Executes the `/sef control approvals manage` action through the shared policy pipeline.
* Usage: `/sef control approvals manage`.
* Canonical route: `sef control approvals manage`.
* Example: `/sef control approvals manage`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.approvals.manage`.
* Access class: `owner`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.approvals.manage`.
* Confirmation: not required.
* Audit class: `destructive`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.approvals.view`

* Description: Executes the `/sef control approvals` action through the shared policy pipeline.
* Usage: `/sef control approvals`.
* Canonical route: `sef control approvals`.
* Example: `/sef control approvals`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.approvals.view`.
* Access class: `staff`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:control.approvals.view`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.auctions.create`

* Description: Executes the `/sef control auctions create` action through the shared policy pipeline.
* Usage: `/sef control auctions create`.
* Canonical route: `sef control auctions create`.
* Example: `/sef control auctions create`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.auctions.create`.
* Access class: `player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.auctions.create`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.auctions.manage`

* Description: Executes the `/sef control auctions manage` action through the shared policy pipeline.
* Usage: `/sef control auctions manage`.
* Canonical route: `sef control auctions manage`.
* Example: `/sef control auctions manage`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.auctions.manage`.
* Access class: `owner`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.auctions.manage`.
* Confirmation: not required.
* Audit class: `destructive`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.auctions.view`

* Description: Executes the `/sef control auctions` action through the shared policy pipeline.
* Usage: `/sef control auctions`.
* Canonical route: `sef control auctions`.
* Example: `/sef control auctions`.
* Convenience roots: `auction`, `auctionadmin`, `auctions`.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.auctions.view`.
* Access class: `player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:control.auctions.view`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.automod.create`

* Description: Executes the `/sef control automod create` action through the shared policy pipeline.
* Usage: `/sef control automod create`.
* Canonical route: `sef control automod create`.
* Example: `/sef control automod create`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.automod.create`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.automod.create`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.automod.manage`

* Description: Executes the `/sef control automod manage` action through the shared policy pipeline.
* Usage: `/sef control automod manage`.
* Canonical route: `sef control automod manage`.
* Example: `/sef control automod manage`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.automod.manage`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.automod.manage`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.automod.view`

* Description: Executes the `/sef control automod` action through the shared policy pipeline.
* Usage: `/sef control automod`.
* Canonical route: `sef control automod`.
* Example: `/sef control automod`.
* Convenience roots: `automod`.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.automod.view`.
* Access class: `trusted_player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:control.automod.view`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.backups.create`

* Description: Executes the `/sef control backups create` action through the shared policy pipeline.
* Usage: `/sef control backups create`.
* Canonical route: `sef control backups create`.
* Example: `/sef control backups create`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.backups.create`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.backups.create`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.backups.manage`

* Description: Executes the `/sef control backups manage` action through the shared policy pipeline.
* Usage: `/sef control backups manage`.
* Canonical route: `sef control backups manage`.
* Example: `/sef control backups manage`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.backups.manage`.
* Access class: `owner`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.backups.manage`.
* Confirmation: not required.
* Audit class: `destructive`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.backups.view`

* Description: Executes the `/sef control backups` action through the shared policy pipeline.
* Usage: `/sef control backups`.
* Canonical route: `sef control backups`.
* Example: `/sef control backups`.
* Convenience roots: `backup`.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.backups.view`.
* Access class: `staff`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:control.backups.view`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.block_activity.create`

* Description: Executes the `/sef control block_activity create` action through the shared policy pipeline.
* Usage: `/sef control block_activity create`.
* Canonical route: `sef control block_activity create`.
* Example: `/sef control block_activity create`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.block_activity.create`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.block_activity.create`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.block_activity.manage`

* Description: Executes the `/sef control block_activity manage` action through the shared policy pipeline.
* Usage: `/sef control block_activity manage`.
* Canonical route: `sef control block_activity manage`.
* Example: `/sef control block_activity manage`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.block_activity.manage`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.block_activity.manage`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.block_activity.view`

* Description: Executes the `/sef control block_activity` action through the shared policy pipeline.
* Usage: `/sef control block_activity`.
* Canonical route: `sef control block_activity`.
* Example: `/sef control block_activity`.
* Convenience roots: `activityprofile`.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.block_activity.view`.
* Access class: `trusted_player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:control.block_activity.view`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.capability_leases.create`

* Description: Executes the `/sef control capability_leases create` action through the shared policy pipeline.
* Usage: `/sef control capability_leases create`.
* Canonical route: `sef control capability_leases create`.
* Example: `/sef control capability_leases create`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.capability_leases.create`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.capability_leases.create`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.capability_leases.manage`

* Description: Executes the `/sef control capability_leases manage` action through the shared policy pipeline.
* Usage: `/sef control capability_leases manage`.
* Canonical route: `sef control capability_leases manage`.
* Example: `/sef control capability_leases manage`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.capability_leases.manage`.
* Access class: `owner`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.capability_leases.manage`.
* Confirmation: not required.
* Audit class: `destructive`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.capability_leases.view`

* Description: Executes the `/sef control capability_leases` action through the shared policy pipeline.
* Usage: `/sef control capability_leases`.
* Canonical route: `sef control capability_leases`.
* Example: `/sef control capability_leases`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.capability_leases.view`.
* Access class: `staff`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:control.capability_leases.view`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.catalog`

* Description: Executes the `/sef control catalog` action through the shared policy pipeline.
* Usage: `/sef control catalog`.
* Canonical route: `sef control catalog`.
* Example: `/sef control catalog`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.catalog`.
* Access class: `staff`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:control.catalog`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.change_windows.create`

* Description: Executes the `/sef control change_windows create` action through the shared policy pipeline.
* Usage: `/sef control change_windows create`.
* Canonical route: `sef control change_windows create`.
* Example: `/sef control change_windows create`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.change_windows.create`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.change_windows.create`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.change_windows.manage`

* Description: Executes the `/sef control change_windows manage` action through the shared policy pipeline.
* Usage: `/sef control change_windows manage`.
* Canonical route: `sef control change_windows manage`.
* Example: `/sef control change_windows manage`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.change_windows.manage`.
* Access class: `owner`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.change_windows.manage`.
* Confirmation: not required.
* Audit class: `destructive`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.change_windows.view`

* Description: Executes the `/sef control change_windows` action through the shared policy pipeline.
* Usage: `/sef control change_windows`.
* Canonical route: `sef control change_windows`.
* Example: `/sef control change_windows`.
* Convenience roots: `changewindow`.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.change_windows.view`.
* Access class: `trusted_player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:control.change_windows.view`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.chat_channels.create`

* Description: Executes the `/sef control chat_channels create` action through the shared policy pipeline.
* Usage: `/sef control chat_channels create`.
* Canonical route: `sef control chat_channels create`.
* Example: `/sef control chat_channels create`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.chat_channels.create`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.chat_channels.create`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.chat_channels.manage`

* Description: Executes the `/sef control chat_channels manage` action through the shared policy pipeline.
* Usage: `/sef control chat_channels manage`.
* Canonical route: `sef control chat_channels manage`.
* Example: `/sef control chat_channels manage`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.chat_channels.manage`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.chat_channels.manage`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.chat_channels.view`

* Description: Executes the `/sef control chat_channels` action through the shared policy pipeline.
* Usage: `/sef control chat_channels`.
* Canonical route: `sef control chat_channels`.
* Example: `/sef control chat_channels`.
* Convenience roots: `channel`, `channels`.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.chat_channels.view`.
* Access class: `trusted_player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:control.chat_channels.view`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.chat_control.create`

* Description: Executes the `/sef control chat_control create` action through the shared policy pipeline.
* Usage: `/sef control chat_control create`.
* Canonical route: `sef control chat_control create`.
* Example: `/sef control chat_control create`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.chat_control.create`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.chat_control.create`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.chat_control.manage`

* Description: Executes the `/sef control chat_control manage` action through the shared policy pipeline.
* Usage: `/sef control chat_control manage`.
* Canonical route: `sef control chat_control manage`.
* Example: `/sef control chat_control manage`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.chat_control.manage`.
* Access class: `owner`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.chat_control.manage`.
* Confirmation: not required.
* Audit class: `destructive`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.chat_control.view`

* Description: Executes the `/sef control chat_control` action through the shared policy pipeline.
* Usage: `/sef control chat_control`.
* Canonical route: `sef control chat_control`.
* Example: `/sef control chat_control`.
* Convenience roots: `chatcontrol`.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.chat_control.view`.
* Access class: `trusted_player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:control.chat_control.view`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.chunk_pregen.create`

* Description: Executes the `/sef control chunk_pregen create` action through the shared policy pipeline.
* Usage: `/sef control chunk_pregen create`.
* Canonical route: `sef control chunk_pregen create`.
* Example: `/sef control chunk_pregen create`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.chunk_pregen.create`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.chunk_pregen.create`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.chunk_pregen.manage`

* Description: Executes the `/sef control chunk_pregen manage` action through the shared policy pipeline.
* Usage: `/sef control chunk_pregen manage`.
* Canonical route: `sef control chunk_pregen manage`.
* Example: `/sef control chunk_pregen manage`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.chunk_pregen.manage`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.chunk_pregen.manage`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.chunk_pregen.view`

* Description: Executes the `/sef control chunk_pregen` action through the shared policy pipeline.
* Usage: `/sef control chunk_pregen`.
* Canonical route: `sef control chunk_pregen`.
* Example: `/sef control chunk_pregen`.
* Convenience roots: `pregen`.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.chunk_pregen.view`.
* Access class: `trusted_player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:control.chunk_pregen.view`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.chunk_tickets.create`

* Description: Executes the `/sef control chunk_tickets create` action through the shared policy pipeline.
* Usage: `/sef control chunk_tickets create`.
* Canonical route: `sef control chunk_tickets create`.
* Example: `/sef control chunk_tickets create`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.chunk_tickets.create`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.chunk_tickets.create`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.chunk_tickets.manage`

* Description: Executes the `/sef control chunk_tickets manage` action through the shared policy pipeline.
* Usage: `/sef control chunk_tickets manage`.
* Canonical route: `sef control chunk_tickets manage`.
* Example: `/sef control chunk_tickets manage`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.chunk_tickets.manage`.
* Access class: `owner`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.chunk_tickets.manage`.
* Confirmation: not required.
* Audit class: `destructive`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.chunk_tickets.view`

* Description: Executes the `/sef control chunk_tickets` action through the shared policy pipeline.
* Usage: `/sef control chunk_tickets`.
* Canonical route: `sef control chunk_tickets`.
* Example: `/sef control chunk_tickets`.
* Convenience roots: `chunktickets`.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.chunk_tickets.view`.
* Access class: `trusted_player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:control.chunk_tickets.view`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.cleanup.create`

* Description: Executes the `/sef control cleanup create` action through the shared policy pipeline.
* Usage: `/sef control cleanup create`.
* Canonical route: `sef control cleanup create`.
* Example: `/sef control cleanup create`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.cleanup.create`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.cleanup.create`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.cleanup.manage`

* Description: Executes the `/sef control cleanup manage` action through the shared policy pipeline.
* Usage: `/sef control cleanup manage`.
* Canonical route: `sef control cleanup manage`.
* Example: `/sef control cleanup manage`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.cleanup.manage`.
* Access class: `owner`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.cleanup.manage`.
* Confirmation: not required.
* Audit class: `destructive`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.cleanup.view`

* Description: Executes the `/sef control cleanup` action through the shared policy pipeline.
* Usage: `/sef control cleanup`.
* Canonical route: `sef control cleanup`.
* Example: `/sef control cleanup`.
* Convenience roots: `cleanup`.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.cleanup.view`.
* Access class: `trusted_player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:control.cleanup.view`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.command_anomaly.create`

* Description: Executes the `/sef control command_anomaly create` action through the shared policy pipeline.
* Usage: `/sef control command_anomaly create`.
* Canonical route: `sef control command_anomaly create`.
* Example: `/sef control command_anomaly create`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.command_anomaly.create`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.command_anomaly.create`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.command_anomaly.manage`

* Description: Executes the `/sef control command_anomaly manage` action through the shared policy pipeline.
* Usage: `/sef control command_anomaly manage`.
* Canonical route: `sef control command_anomaly manage`.
* Example: `/sef control command_anomaly manage`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.command_anomaly.manage`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.command_anomaly.manage`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.command_anomaly.view`

* Description: Executes the `/sef control command_anomaly` action through the shared policy pipeline.
* Usage: `/sef control command_anomaly`.
* Canonical route: `sef control command_anomaly`.
* Example: `/sef control command_anomaly`.
* Convenience roots: `anomaly`.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.command_anomaly.view`.
* Access class: `trusted_player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:control.command_anomaly.view`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.community_events.create`

* Description: Executes the `/sef control community_events create` action through the shared policy pipeline.
* Usage: `/sef control community_events create`.
* Canonical route: `sef control community_events create`.
* Example: `/sef control community_events create`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.community_events.create`.
* Access class: `player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.community_events.create`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.community_events.join`

* Description: Executes the `/events join` action through the shared policy pipeline.
* Usage: `/events join`.
* Canonical route: `events join`.
* Example: `/events join`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.community_events.create`.
* Access class: `player`.
* Sources: `player`.
* Target behavior: `self`.
* Cooldown policy: `sef:control.community_events.join`.
* Confirmation: not required.
* Audit class: `workflow_execution`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.community_events.leave`

* Description: Executes the `/events leave` action through the shared policy pipeline.
* Usage: `/events leave`.
* Canonical route: `events leave`.
* Example: `/events leave`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.community_events.create`.
* Access class: `player`.
* Sources: `player`.
* Target behavior: `self`.
* Cooldown policy: `sef:control.community_events.leave`.
* Confirmation: not required.
* Audit class: `workflow_execution`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.community_events.manage`

* Description: Executes the `/sef control community_events manage` action through the shared policy pipeline.
* Usage: `/sef control community_events manage`.
* Canonical route: `sef control community_events manage`.
* Example: `/sef control community_events manage`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.community_events.manage`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.community_events.manage`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.community_events.view`

* Description: Executes the `/sef control community_events` action through the shared policy pipeline.
* Usage: `/sef control community_events`.
* Canonical route: `sef control community_events`.
* Example: `/sef control community_events`.
* Convenience roots: `event`, `eventadmin`, `events`.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.community_events.view`.
* Access class: `player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:control.community_events.view`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.config_drift.create`

* Description: Executes the `/sef control config_drift create` action through the shared policy pipeline.
* Usage: `/sef control config_drift create`.
* Canonical route: `sef control config_drift create`.
* Example: `/sef control config_drift create`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.config_drift.create`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.config_drift.create`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.config_drift.manage`

* Description: Executes the `/sef control config_drift manage` action through the shared policy pipeline.
* Usage: `/sef control config_drift manage`.
* Canonical route: `sef control config_drift manage`.
* Example: `/sef control config_drift manage`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.config_drift.manage`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.config_drift.manage`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.config_drift.view`

* Description: Executes the `/sef control config_drift` action through the shared policy pipeline.
* Usage: `/sef control config_drift`.
* Canonical route: `sef control config_drift`.
* Example: `/sef control config_drift`.
* Convenience roots: `drift`.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.config_drift.view`.
* Access class: `trusted_player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:control.config_drift.view`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.daily_rewards.claim`

* Description: Executes the `/daily claim` action through the shared policy pipeline.
* Usage: `/daily claim`.
* Canonical route: `daily claim`.
* Example: `/daily claim`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.daily_rewards.create`.
* Access class: `player`.
* Sources: `player`.
* Target behavior: `self`.
* Cooldown policy: `sef:control.daily_rewards.claim`.
* Confirmation: not required.
* Audit class: `workflow_execution`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.daily_rewards.create`

* Description: Executes the `/sef control daily_rewards create` action through the shared policy pipeline.
* Usage: `/sef control daily_rewards create`.
* Canonical route: `sef control daily_rewards create`.
* Example: `/sef control daily_rewards create`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.daily_rewards.create`.
* Access class: `player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.daily_rewards.create`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.daily_rewards.manage`

* Description: Executes the `/sef control daily_rewards manage` action through the shared policy pipeline.
* Usage: `/sef control daily_rewards manage`.
* Canonical route: `sef control daily_rewards manage`.
* Example: `/sef control daily_rewards manage`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.daily_rewards.manage`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.daily_rewards.manage`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.daily_rewards.view`

* Description: Executes the `/sef control daily_rewards` action through the shared policy pipeline.
* Usage: `/sef control daily_rewards`.
* Canonical route: `sef control daily_rewards`.
* Example: `/sef control daily_rewards`.
* Convenience roots: `daily`.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.daily_rewards.view`.
* Access class: `player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:control.daily_rewards.view`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.datapacks.create`

* Description: Executes the `/sef control datapacks create` action through the shared policy pipeline.
* Usage: `/sef control datapacks create`.
* Canonical route: `sef control datapacks create`.
* Example: `/sef control datapacks create`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.datapacks.create`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.datapacks.create`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.datapacks.manage`

* Description: Executes the `/sef control datapacks manage` action through the shared policy pipeline.
* Usage: `/sef control datapacks manage`.
* Canonical route: `sef control datapacks manage`.
* Example: `/sef control datapacks manage`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.datapacks.manage`.
* Access class: `owner`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.datapacks.manage`.
* Confirmation: not required.
* Audit class: `destructive`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.datapacks.view`

* Description: Executes the `/sef control datapacks` action through the shared policy pipeline.
* Usage: `/sef control datapacks`.
* Canonical route: `sef control datapacks`.
* Example: `/sef control datapacks`.
* Convenience roots: `datapacks`.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.datapacks.view`.
* Access class: `trusted_player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:control.datapacks.view`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.death_compass.clear`

* Description: Executes the `/deathlocation clear` action through the shared policy pipeline.
* Usage: `/deathlocation clear`.
* Canonical route: `deathlocation clear`.
* Example: `/deathlocation clear`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.death_compass.create`.
* Access class: `player`.
* Sources: `player`.
* Target behavior: `self`.
* Cooldown policy: `sef:control.death_compass.clear`.
* Confirmation: not required.
* Audit class: `workflow_execution`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.death_compass.create`

* Description: Executes the `/sef control death_compass create` action through the shared policy pipeline.
* Usage: `/sef control death_compass create`.
* Canonical route: `sef control death_compass create`.
* Example: `/sef control death_compass create`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.death_compass.create`.
* Access class: `player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.death_compass.create`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.death_compass.manage`

* Description: Executes the `/sef control death_compass manage` action through the shared policy pipeline.
* Usage: `/sef control death_compass manage`.
* Canonical route: `sef control death_compass manage`.
* Example: `/sef control death_compass manage`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.death_compass.manage`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.death_compass.manage`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.death_compass.view`

* Description: Executes the `/sef control death_compass` action through the shared policy pipeline.
* Usage: `/sef control death_compass`.
* Canonical route: `sef control death_compass`.
* Example: `/sef control death_compass`.
* Convenience roots: `deathcompass`, `deathlocation`.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.death_compass.view`.
* Access class: `player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:control.death_compass.view`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.dependency_graph.create`

* Description: Executes the `/sef control dependency_graph create` action through the shared policy pipeline.
* Usage: `/sef control dependency_graph create`.
* Canonical route: `sef control dependency_graph create`.
* Example: `/sef control dependency_graph create`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.dependency_graph.create`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.dependency_graph.create`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.dependency_graph.manage`

* Description: Executes the `/sef control dependency_graph manage` action through the shared policy pipeline.
* Usage: `/sef control dependency_graph manage`.
* Canonical route: `sef control dependency_graph manage`.
* Example: `/sef control dependency_graph manage`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.dependency_graph.manage`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.dependency_graph.manage`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.dependency_graph.view`

* Description: Executes the `/sef control dependency_graph` action through the shared policy pipeline.
* Usage: `/sef control dependency_graph`.
* Canonical route: `sef control dependency_graph`.
* Example: `/sef control dependency_graph`.
* Convenience roots: `featuregraph`.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.dependency_graph.view`.
* Access class: `trusted_player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:control.dependency_graph.view`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.discipline.create`

* Description: Executes the `/sef control discipline create` action through the shared policy pipeline.
* Usage: `/sef control discipline create`.
* Canonical route: `sef control discipline create`.
* Example: `/sef control discipline create`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.discipline.create`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.discipline.create`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.discipline.manage`

* Description: Executes the `/sef control discipline manage` action through the shared policy pipeline.
* Usage: `/sef control discipline manage`.
* Canonical route: `sef control discipline manage`.
* Example: `/sef control discipline manage`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.discipline.manage`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.discipline.manage`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.discipline.view`

* Description: Executes the `/sef control discipline` action through the shared policy pipeline.
* Usage: `/sef control discipline`.
* Canonical route: `sef control discipline`.
* Example: `/sef control discipline`.
* Convenience roots: `discipline`.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.discipline.view`.
* Access class: `staff`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:control.discipline.view`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.display_ownership.create`

* Description: Executes the `/sef control display_ownership create` action through the shared policy pipeline.
* Usage: `/sef control display_ownership create`.
* Canonical route: `sef control display_ownership create`.
* Example: `/sef control display_ownership create`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.display_ownership.create`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.display_ownership.create`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.display_ownership.manage`

* Description: Executes the `/sef control display_ownership manage` action through the shared policy pipeline.
* Usage: `/sef control display_ownership manage`.
* Canonical route: `sef control display_ownership manage`.
* Example: `/sef control display_ownership manage`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.display_ownership.manage`.
* Access class: `owner`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.display_ownership.manage`.
* Confirmation: not required.
* Audit class: `destructive`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.display_ownership.view`

* Description: Executes the `/sef control display_ownership` action through the shared policy pipeline.
* Usage: `/sef control display_ownership`.
* Canonical route: `sef control display_ownership`.
* Example: `/sef control display_ownership`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.display_ownership.view`.
* Access class: `trusted_player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:control.display_ownership.view`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.display_profiles.create`

* Description: Executes the `/sef control display_profiles create` action through the shared policy pipeline.
* Usage: `/sef control display_profiles create`.
* Canonical route: `sef control display_profiles create`.
* Example: `/sef control display_profiles create`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.display_profiles.create`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.display_profiles.create`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.display_profiles.manage`

* Description: Executes the `/sef control display_profiles manage` action through the shared policy pipeline.
* Usage: `/sef control display_profiles manage`.
* Canonical route: `sef control display_profiles manage`.
* Example: `/sef control display_profiles manage`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.display_profiles.manage`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.display_profiles.manage`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.display_profiles.view`

* Description: Executes the `/sef control display_profiles` action through the shared policy pipeline.
* Usage: `/sef control display_profiles`.
* Canonical route: `sef control display_profiles`.
* Example: `/sef control display_profiles`.
* Convenience roots: `bossbars`, `displayprofile`, `sidebar`.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.display_profiles.view`.
* Access class: `trusted_player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:control.display_profiles.view`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.evidence.create`

* Description: Executes the `/sef control evidence create` action through the shared policy pipeline.
* Usage: `/sef control evidence create`.
* Canonical route: `sef control evidence create`.
* Example: `/sef control evidence create`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.evidence.create`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.evidence.create`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.evidence.manage`

* Description: Executes the `/sef control evidence manage` action through the shared policy pipeline.
* Usage: `/sef control evidence manage`.
* Canonical route: `sef control evidence manage`.
* Example: `/sef control evidence manage`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.evidence.manage`.
* Access class: `owner`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.evidence.manage`.
* Confirmation: not required.
* Audit class: `destructive`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.evidence.view`

* Description: Executes the `/sef control evidence` action through the shared policy pipeline.
* Usage: `/sef control evidence`.
* Canonical route: `sef control evidence`.
* Example: `/sef control evidence`.
* Convenience roots: `evidence`.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.evidence.view`.
* Access class: `staff`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:control.evidence.view`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.friends.accept`

* Description: Executes the `/friend accept` action through the shared policy pipeline.
* Usage: `/friend accept`.
* Canonical route: `friend accept`.
* Example: `/friend accept`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.friends.create`.
* Access class: `player`.
* Sources: `player`.
* Target behavior: `self`.
* Cooldown policy: `sef:control.friends.accept`.
* Confirmation: not required.
* Audit class: `workflow_execution`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.friends.create`

* Description: Executes the `/sef control friends create` action through the shared policy pipeline.
* Usage: `/sef control friends create`.
* Canonical route: `sef control friends create`.
* Example: `/sef control friends create`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.friends.create`.
* Access class: `player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.friends.create`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.friends.manage`

* Description: Executes the `/sef control friends manage` action through the shared policy pipeline.
* Usage: `/sef control friends manage`.
* Canonical route: `sef control friends manage`.
* Example: `/sef control friends manage`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.friends.manage`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.friends.manage`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.friends.remove`

* Description: Executes the `/friend remove` action through the shared policy pipeline.
* Usage: `/friend remove`.
* Canonical route: `friend remove`.
* Example: `/friend remove`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.friends.create`.
* Access class: `player`.
* Sources: `player`.
* Target behavior: `self`.
* Cooldown policy: `sef:control.friends.remove`.
* Confirmation: not required.
* Audit class: `workflow_execution`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.friends.request`

* Description: Executes the `/friend add` action through the shared policy pipeline.
* Usage: `/friend add`.
* Canonical route: `friend add`.
* Example: `/friend add`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.friends.create`.
* Access class: `player`.
* Sources: `player`.
* Target behavior: `self`.
* Cooldown policy: `sef:control.friends.request`.
* Confirmation: not required.
* Audit class: `workflow_execution`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.friends.view`

* Description: Executes the `/sef control friends` action through the shared policy pipeline.
* Usage: `/sef control friends`.
* Canonical route: `sef control friends`.
* Example: `/sef control friends`.
* Convenience roots: `friend`, `friends`, `trust`, `untrust`.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.friends.view`.
* Access class: `player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:control.friends.view`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.graves.claim`

* Description: Executes the `/grave claim` action through the shared policy pipeline.
* Usage: `/grave claim`.
* Canonical route: `grave claim`.
* Example: `/grave claim`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.graves.create`.
* Access class: `player`.
* Sources: `player`.
* Target behavior: `self`.
* Cooldown policy: `sef:control.graves.claim`.
* Confirmation: not required.
* Audit class: `workflow_execution`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.graves.create`

* Description: Executes the `/sef control graves create` action through the shared policy pipeline.
* Usage: `/sef control graves create`.
* Canonical route: `sef control graves create`.
* Example: `/sef control graves create`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.graves.create`.
* Access class: `player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.graves.create`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.graves.locate`

* Description: Executes the `/grave locate` action through the shared policy pipeline.
* Usage: `/grave locate`.
* Canonical route: `grave locate`.
* Example: `/grave locate`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.graves.create`.
* Access class: `player`.
* Sources: `player`.
* Target behavior: `self`.
* Cooldown policy: `sef:control.graves.locate`.
* Confirmation: not required.
* Audit class: `workflow_execution`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.graves.manage`

* Description: Executes the `/sef control graves manage` action through the shared policy pipeline.
* Usage: `/sef control graves manage`.
* Canonical route: `sef control graves manage`.
* Example: `/sef control graves manage`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.graves.manage`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.graves.manage`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.graves.unlock`

* Description: Executes the `/grave unlock` action through the shared policy pipeline.
* Usage: `/grave unlock`.
* Canonical route: `grave unlock`.
* Example: `/grave unlock`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.graves.create`.
* Access class: `player`.
* Sources: `player`.
* Target behavior: `self`.
* Cooldown policy: `sef:control.graves.unlock`.
* Confirmation: not required.
* Audit class: `workflow_execution`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.graves.view`

* Description: Executes the `/sef control graves` action through the shared policy pipeline.
* Usage: `/sef control graves`.
* Canonical route: `sef control graves`.
* Example: `/sef control graves`.
* Convenience roots: `grave`, `graves`.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.graves.view`.
* Access class: `player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:control.graves.view`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.guardrails.create`

* Description: Executes the `/sef control guardrails create` action through the shared policy pipeline.
* Usage: `/sef control guardrails create`.
* Canonical route: `sef control guardrails create`.
* Example: `/sef control guardrails create`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.guardrails.create`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.guardrails.create`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.guardrails.manage`

* Description: Executes the `/sef control guardrails manage` action through the shared policy pipeline.
* Usage: `/sef control guardrails manage`.
* Canonical route: `sef control guardrails manage`.
* Example: `/sef control guardrails manage`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.guardrails.manage`.
* Access class: `owner`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.guardrails.manage`.
* Confirmation: not required.
* Audit class: `destructive`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.guardrails.view`

* Description: Executes the `/sef control guardrails` action through the shared policy pipeline.
* Usage: `/sef control guardrails`.
* Canonical route: `sef control guardrails`.
* Example: `/sef control guardrails`.
* Convenience roots: `guardrail`.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.guardrails.view`.
* Access class: `trusted_player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:control.guardrails.view`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.incidents.create`

* Description: Executes the `/sef control incidents create` action through the shared policy pipeline.
* Usage: `/sef control incidents create`.
* Canonical route: `sef control incidents create`.
* Example: `/sef control incidents create`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.incidents.create`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.incidents.create`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.incidents.manage`

* Description: Executes the `/sef control incidents manage` action through the shared policy pipeline.
* Usage: `/sef control incidents manage`.
* Canonical route: `sef control incidents manage`.
* Example: `/sef control incidents manage`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.incidents.manage`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.incidents.manage`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.incidents.view`

* Description: Executes the `/sef control incidents` action through the shared policy pipeline.
* Usage: `/sef control incidents`.
* Canonical route: `sef control incidents`.
* Example: `/sef control incidents`.
* Convenience roots: `incident`.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.incidents.view`.
* Access class: `staff`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:control.incidents.view`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.interaction_blocks.create`

* Description: Executes the `/sef control interaction_blocks create` action through the shared policy pipeline.
* Usage: `/sef control interaction_blocks create`.
* Canonical route: `sef control interaction_blocks create`.
* Example: `/sef control interaction_blocks create`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.interaction_blocks.create`.
* Access class: `player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.interaction_blocks.create`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.interaction_blocks.manage`

* Description: Executes the `/sef control interaction_blocks manage` action through the shared policy pipeline.
* Usage: `/sef control interaction_blocks manage`.
* Canonical route: `sef control interaction_blocks manage`.
* Example: `/sef control interaction_blocks manage`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.interaction_blocks.manage`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.interaction_blocks.manage`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.interaction_blocks.set`

* Description: Executes the `/blocks add` action through the shared policy pipeline.
* Usage: `/blocks add`.
* Canonical route: `blocks add`.
* Example: `/blocks add`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.interaction_blocks.create`.
* Access class: `player`.
* Sources: `player`.
* Target behavior: `self`.
* Cooldown policy: `sef:control.interaction_blocks.set`.
* Confirmation: not required.
* Audit class: `workflow_execution`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.interaction_blocks.view`

* Description: Executes the `/sef control interaction_blocks` action through the shared policy pipeline.
* Usage: `/sef control interaction_blocks`.
* Canonical route: `sef control interaction_blocks`.
* Example: `/sef control interaction_blocks`.
* Convenience roots: `block`, `blocks`, `unblock`.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.interaction_blocks.view`.
* Access class: `player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:control.interaction_blocks.view`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.inventory_recovery.create`

* Description: Executes the `/sef control inventory_recovery create` action through the shared policy pipeline.
* Usage: `/sef control inventory_recovery create`.
* Canonical route: `sef control inventory_recovery create`.
* Example: `/sef control inventory_recovery create`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.inventory_recovery.create`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.inventory_recovery.create`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.inventory_recovery.manage`

* Description: Executes the `/sef control inventory_recovery manage` action through the shared policy pipeline.
* Usage: `/sef control inventory_recovery manage`.
* Canonical route: `sef control inventory_recovery manage`.
* Example: `/sef control inventory_recovery manage`.
* Convenience roots: `inventoryrestore`.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.inventory_recovery.manage`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.inventory_recovery.manage`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.inventory_recovery.view`

* Description: Executes the `/sef control inventory_recovery` action through the shared policy pipeline.
* Usage: `/sef control inventory_recovery`.
* Canonical route: `sef control inventory_recovery`.
* Example: `/sef control inventory_recovery`.
* Convenience roots: `inventoryhistory`.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.inventory_recovery.view`.
* Access class: `trusted_player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:control.inventory_recovery.view`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.invites.create`

* Description: Executes the `/sef control invites create` action through the shared policy pipeline.
* Usage: `/sef control invites create`.
* Canonical route: `sef control invites create`.
* Example: `/sef control invites create`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.invites.create`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.invites.create`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.invites.manage`

* Description: Executes the `/sef control invites manage` action through the shared policy pipeline.
* Usage: `/sef control invites manage`.
* Canonical route: `sef control invites manage`.
* Example: `/sef control invites manage`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.invites.manage`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.invites.manage`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.invites.redeem`

* Description: Executes the `/invite redeem` action through the shared policy pipeline.
* Usage: `/invite redeem`.
* Canonical route: `invite redeem`.
* Example: `/invite redeem`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.invites.create`.
* Access class: `player`.
* Sources: `player`.
* Target behavior: `self`.
* Cooldown policy: `sef:control.invites.redeem`.
* Confirmation: not required.
* Audit class: `workflow_execution`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.invites.view`

* Description: Executes the `/sef control invites` action through the shared policy pipeline.
* Usage: `/sef control invites`.
* Canonical route: `sef control invites`.
* Example: `/sef control invites`.
* Convenience roots: `invites`.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.invites.view`.
* Access class: `trusted_player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:control.invites.view`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.knowledge.bookmark`

* Description: Executes the `/knowledge bookmark` action through the shared policy pipeline.
* Usage: `/knowledge bookmark`.
* Canonical route: `knowledge bookmark`.
* Example: `/knowledge bookmark`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.knowledge.create`.
* Access class: `player`.
* Sources: `player`.
* Target behavior: `self`.
* Cooldown policy: `sef:control.knowledge.bookmark`.
* Confirmation: not required.
* Audit class: `workflow_execution`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.knowledge.create`

* Description: Executes the `/sef control knowledge create` action through the shared policy pipeline.
* Usage: `/sef control knowledge create`.
* Canonical route: `sef control knowledge create`.
* Example: `/sef control knowledge create`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.knowledge.create`.
* Access class: `player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.knowledge.create`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.knowledge.manage`

* Description: Executes the `/sef control knowledge manage` action through the shared policy pipeline.
* Usage: `/sef control knowledge manage`.
* Canonical route: `sef control knowledge manage`.
* Example: `/sef control knowledge manage`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.knowledge.manage`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.knowledge.manage`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.knowledge.view`

* Description: Executes the `/sef control knowledge` action through the shared policy pipeline.
* Usage: `/sef control knowledge`.
* Canonical route: `sef control knowledge`.
* Example: `/sef control knowledge`.
* Convenience roots: `guide`, `guideadmin`, `knowledge`.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.knowledge.view`.
* Access class: `player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:control.knowledge.view`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.lost_found.create`

* Description: Executes the `/sef control lost_found create` action through the shared policy pipeline.
* Usage: `/sef control lost_found create`.
* Canonical route: `sef control lost_found create`.
* Example: `/sef control lost_found create`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.lost_found.create`.
* Access class: `player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.lost_found.create`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.lost_found.manage`

* Description: Executes the `/sef control lost_found manage` action through the shared policy pipeline.
* Usage: `/sef control lost_found manage`.
* Canonical route: `sef control lost_found manage`.
* Example: `/sef control lost_found manage`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.lost_found.manage`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.lost_found.manage`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.lost_found.view`

* Description: Executes the `/sef control lost_found` action through the shared policy pipeline.
* Usage: `/sef control lost_found`.
* Canonical route: `sef control lost_found`.
* Example: `/sef control lost_found`.
* Convenience roots: `lostfound`, `lostfoundadmin`.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.lost_found.view`.
* Access class: `player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:control.lost_found.view`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.maintenance.create`

* Description: Executes the `/sef control maintenance create` action through the shared policy pipeline.
* Usage: `/sef control maintenance create`.
* Canonical route: `sef control maintenance create`.
* Example: `/sef control maintenance create`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.maintenance.create`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.maintenance.create`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.maintenance.manage`

* Description: Executes the `/sef control maintenance manage` action through the shared policy pipeline.
* Usage: `/sef control maintenance manage`.
* Canonical route: `sef control maintenance manage`.
* Example: `/sef control maintenance manage`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.maintenance.manage`.
* Access class: `owner`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.maintenance.manage`.
* Confirmation: not required.
* Audit class: `destructive`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.maintenance.view`

* Description: Executes the `/sef control maintenance` action through the shared policy pipeline.
* Usage: `/sef control maintenance`.
* Canonical route: `sef control maintenance`.
* Example: `/sef control maintenance`.
* Convenience roots: `maintenance`.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.maintenance.view`.
* Access class: `trusted_player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:control.maintenance.view`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.mentions.create`

* Description: Executes the `/sef control mentions create` action through the shared policy pipeline.
* Usage: `/sef control mentions create`.
* Canonical route: `sef control mentions create`.
* Example: `/sef control mentions create`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.mentions.create`.
* Access class: `player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.mentions.create`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.mentions.manage`

* Description: Executes the `/sef control mentions manage` action through the shared policy pipeline.
* Usage: `/sef control mentions manage`.
* Canonical route: `sef control mentions manage`.
* Example: `/sef control mentions manage`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.mentions.manage`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.mentions.manage`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.mentions.set`

* Description: Executes the `/mentions mode` action through the shared policy pipeline.
* Usage: `/mentions mode`.
* Canonical route: `mentions mode`.
* Example: `/mentions mode`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.mentions.create`.
* Access class: `player`.
* Sources: `player`.
* Target behavior: `self`.
* Cooldown policy: `sef:control.mentions.set`.
* Confirmation: not required.
* Audit class: `workflow_execution`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.mentions.view`

* Description: Executes the `/sef control mentions` action through the shared policy pipeline.
* Usage: `/sef control mentions`.
* Canonical route: `sef control mentions`.
* Example: `/sef control mentions`.
* Convenience roots: `mentions`.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.mentions.view`.
* Access class: `player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:control.mentions.view`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.mod_health.create`

* Description: Executes the `/sef control mod_health create` action through the shared policy pipeline.
* Usage: `/sef control mod_health create`.
* Canonical route: `sef control mod_health create`.
* Example: `/sef control mod_health create`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.mod_health.create`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.mod_health.create`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.mod_health.manage`

* Description: Executes the `/sef control mod_health manage` action through the shared policy pipeline.
* Usage: `/sef control mod_health manage`.
* Canonical route: `sef control mod_health manage`.
* Example: `/sef control mod_health manage`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.mod_health.manage`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.mod_health.manage`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.mod_health.view`

* Description: Executes the `/sef control mod_health` action through the shared policy pipeline.
* Usage: `/sef control mod_health`.
* Canonical route: `sef control mod_health`.
* Example: `/sef control mod_health`.
* Convenience roots: `modhealth`.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.mod_health.view`.
* Access class: `trusted_player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:control.mod_health.view`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.onboarding.complete`

* Description: Executes the `/onboarding step` action through the shared policy pipeline.
* Usage: `/onboarding step`.
* Canonical route: `onboarding step`.
* Example: `/onboarding step`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.onboarding.create`.
* Access class: `player`.
* Sources: `player`.
* Target behavior: `self`.
* Cooldown policy: `sef:control.onboarding.complete`.
* Confirmation: not required.
* Audit class: `workflow_execution`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.onboarding.create`

* Description: Executes the `/sef control onboarding create` action through the shared policy pipeline.
* Usage: `/sef control onboarding create`.
* Canonical route: `sef control onboarding create`.
* Example: `/sef control onboarding create`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.onboarding.create`.
* Access class: `player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.onboarding.create`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.onboarding.dismiss`

* Description: Executes the `/onboarding dismiss` action through the shared policy pipeline.
* Usage: `/onboarding dismiss`.
* Canonical route: `onboarding dismiss`.
* Example: `/onboarding dismiss`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.onboarding.create`.
* Access class: `player`.
* Sources: `player`.
* Target behavior: `self`.
* Cooldown policy: `sef:control.onboarding.dismiss`.
* Confirmation: not required.
* Audit class: `workflow_execution`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.onboarding.manage`

* Description: Executes the `/sef control onboarding manage` action through the shared policy pipeline.
* Usage: `/sef control onboarding manage`.
* Canonical route: `sef control onboarding manage`.
* Example: `/sef control onboarding manage`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.onboarding.manage`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.onboarding.manage`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.onboarding.view`

* Description: Executes the `/sef control onboarding` action through the shared policy pipeline.
* Usage: `/sef control onboarding`.
* Canonical route: `sef control onboarding`.
* Example: `/sef control onboarding`.
* Convenience roots: `onboarding`.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.onboarding.view`.
* Access class: `player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:control.onboarding.view`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.operational_snapshots.create`

* Description: Executes the `/sef control operational_snapshots create` action through the shared policy pipeline.
* Usage: `/sef control operational_snapshots create`.
* Canonical route: `sef control operational_snapshots create`.
* Example: `/sef control operational_snapshots create`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.operational_snapshots.create`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.operational_snapshots.create`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.operational_snapshots.manage`

* Description: Executes the `/sef control operational_snapshots manage` action through the shared policy pipeline.
* Usage: `/sef control operational_snapshots manage`.
* Canonical route: `sef control operational_snapshots manage`.
* Example: `/sef control operational_snapshots manage`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.operational_snapshots.manage`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.operational_snapshots.manage`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.operational_snapshots.view`

* Description: Executes the `/sef control operational_snapshots` action through the shared policy pipeline.
* Usage: `/sef control operational_snapshots`.
* Canonical route: `sef control operational_snapshots`.
* Example: `/sef control operational_snapshots`.
* Convenience roots: `statesnapshot`.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.operational_snapshots.view`.
* Access class: `trusted_player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:control.operational_snapshots.view`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.parcels.create`

* Description: Executes the `/sef control parcels create` action through the shared policy pipeline.
* Usage: `/sef control parcels create`.
* Canonical route: `sef control parcels create`.
* Example: `/sef control parcels create`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.parcels.create`.
* Access class: `player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.parcels.create`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.parcels.manage`

* Description: Executes the `/sef control parcels manage` action through the shared policy pipeline.
* Usage: `/sef control parcels manage`.
* Canonical route: `sef control parcels manage`.
* Example: `/sef control parcels manage`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.parcels.manage`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.parcels.manage`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.parcels.view`

* Description: Executes the `/sef control parcels` action through the shared policy pipeline.
* Usage: `/sef control parcels`.
* Canonical route: `sef control parcels`.
* Example: `/sef control parcels`.
* Convenience roots: `parcel`, `parceladmin`, `parcels`.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.parcels.view`.
* Access class: `player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:control.parcels.view`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.performance.create`

* Description: Executes the `/sef control performance create` action through the shared policy pipeline.
* Usage: `/sef control performance create`.
* Canonical route: `sef control performance create`.
* Example: `/sef control performance create`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.performance.create`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.performance.create`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.performance.manage`

* Description: Executes the `/sef control performance manage` action through the shared policy pipeline.
* Usage: `/sef control performance manage`.
* Canonical route: `sef control performance manage`.
* Example: `/sef control performance manage`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.performance.manage`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.performance.manage`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.performance.view`

* Description: Executes the `/sef control performance` action through the shared policy pipeline.
* Usage: `/sef control performance`.
* Canonical route: `sef control performance`.
* Example: `/sef control performance`.
* Convenience roots: `performance`.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.performance.view`.
* Access class: `trusted_player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:control.performance.view`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.permission_impact.create`

* Description: Executes the `/sef control permission_impact create` action through the shared policy pipeline.
* Usage: `/sef control permission_impact create`.
* Canonical route: `sef control permission_impact create`.
* Example: `/sef control permission_impact create`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.permission_impact.create`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.permission_impact.create`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.permission_impact.manage`

* Description: Executes the `/sef control permission_impact manage` action through the shared policy pipeline.
* Usage: `/sef control permission_impact manage`.
* Canonical route: `sef control permission_impact manage`.
* Example: `/sef control permission_impact manage`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.permission_impact.manage`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.permission_impact.manage`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.permission_impact.view`

* Description: Executes the `/sef control permission_impact` action through the shared policy pipeline.
* Usage: `/sef control permission_impact`.
* Canonical route: `sef control permission_impact`.
* Example: `/sef control permission_impact`.
* Convenience roots: `permissionimpact`.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.permission_impact.view`.
* Access class: `trusted_player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:control.permission_impact.view`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.player_impact.create`

* Description: Executes the `/sef control player_impact create` action through the shared policy pipeline.
* Usage: `/sef control player_impact create`.
* Canonical route: `sef control player_impact create`.
* Example: `/sef control player_impact create`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.player_impact.create`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.player_impact.create`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.player_impact.manage`

* Description: Executes the `/sef control player_impact manage` action through the shared policy pipeline.
* Usage: `/sef control player_impact manage`.
* Canonical route: `sef control player_impact manage`.
* Example: `/sef control player_impact manage`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.player_impact.manage`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.player_impact.manage`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.player_impact.view`

* Description: Executes the `/sef control player_impact` action through the shared policy pipeline.
* Usage: `/sef control player_impact`.
* Canonical route: `sef control player_impact`.
* Example: `/sef control player_impact`.
* Convenience roots: `impactpreview`.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.player_impact.view`.
* Access class: `trusted_player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:control.player_impact.view`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.player_warp_review.create`

* Description: Executes the `/sef control player_warp_review create` action through the shared policy pipeline.
* Usage: `/sef control player_warp_review create`.
* Canonical route: `sef control player_warp_review create`.
* Example: `/sef control player_warp_review create`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.player_warp_review.create`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.player_warp_review.create`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.player_warp_review.manage`

* Description: Executes the `/sef control player_warp_review manage` action through the shared policy pipeline.
* Usage: `/sef control player_warp_review manage`.
* Canonical route: `sef control player_warp_review manage`.
* Example: `/sef control player_warp_review manage`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.player_warp_review.manage`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.player_warp_review.manage`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.player_warp_review.view`

* Description: Executes the `/sef control player_warp_review` action through the shared policy pipeline.
* Usage: `/sef control player_warp_review`.
* Canonical route: `sef control player_warp_review`.
* Example: `/sef control player_warp_review`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.player_warp_review.view`.
* Access class: `trusted_player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:control.player_warp_review.view`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.playtime_rewards.claim`

* Description: Executes the `/rewards claim` action through the shared policy pipeline.
* Usage: `/rewards claim`.
* Canonical route: `rewards claim`.
* Example: `/rewards claim`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.playtime_rewards.create`.
* Access class: `player`.
* Sources: `player`.
* Target behavior: `self`.
* Cooldown policy: `sef:control.playtime_rewards.claim`.
* Confirmation: not required.
* Audit class: `workflow_execution`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.playtime_rewards.create`

* Description: Executes the `/sef control playtime_rewards create` action through the shared policy pipeline.
* Usage: `/sef control playtime_rewards create`.
* Canonical route: `sef control playtime_rewards create`.
* Example: `/sef control playtime_rewards create`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.playtime_rewards.create`.
* Access class: `player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.playtime_rewards.create`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.playtime_rewards.manage`

* Description: Executes the `/sef control playtime_rewards manage` action through the shared policy pipeline.
* Usage: `/sef control playtime_rewards manage`.
* Canonical route: `sef control playtime_rewards manage`.
* Example: `/sef control playtime_rewards manage`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.playtime_rewards.manage`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.playtime_rewards.manage`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.playtime_rewards.view`

* Description: Executes the `/sef control playtime_rewards` action through the shared policy pipeline.
* Usage: `/sef control playtime_rewards`.
* Canonical route: `sef control playtime_rewards`.
* Example: `/sef control playtime_rewards`.
* Convenience roots: `playtimerewards`, `rewards`.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.playtime_rewards.view`.
* Access class: `player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:control.playtime_rewards.view`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.policy_lab.create`

* Description: Executes the `/sef control policy_lab create` action through the shared policy pipeline.
* Usage: `/sef control policy_lab create`.
* Canonical route: `sef control policy_lab create`.
* Example: `/sef control policy_lab create`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.policy_lab.create`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.policy_lab.create`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.policy_lab.manage`

* Description: Executes the `/sef control policy_lab manage` action through the shared policy pipeline.
* Usage: `/sef control policy_lab manage`.
* Canonical route: `sef control policy_lab manage`.
* Example: `/sef control policy_lab manage`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.policy_lab.manage`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.policy_lab.manage`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.policy_lab.view`

* Description: Executes the `/sef control policy_lab` action through the shared policy pipeline.
* Usage: `/sef control policy_lab`.
* Canonical route: `sef control policy_lab`.
* Example: `/sef control policy_lab`.
* Convenience roots: `policylab`.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.policy_lab.view`.
* Access class: `trusted_player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:control.policy_lab.view`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.polls.create`

* Description: Executes the `/sef control polls create` action through the shared policy pipeline.
* Usage: `/sef control polls create`.
* Canonical route: `sef control polls create`.
* Example: `/sef control polls create`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.polls.create`.
* Access class: `player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.polls.create`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.polls.manage`

* Description: Executes the `/sef control polls manage` action through the shared policy pipeline.
* Usage: `/sef control polls manage`.
* Canonical route: `sef control polls manage`.
* Example: `/sef control polls manage`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.polls.manage`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.polls.manage`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.polls.view`

* Description: Executes the `/sef control polls` action through the shared policy pipeline.
* Usage: `/sef control polls`.
* Canonical route: `sef control polls`.
* Example: `/sef control polls`.
* Convenience roots: `poll`, `polladmin`, `polls`.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.polls.view`.
* Access class: `player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:control.polls.view`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.polls.vote`

* Description: Executes the `/poll vote` action through the shared policy pipeline.
* Usage: `/poll vote`.
* Canonical route: `poll vote`.
* Example: `/poll vote`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.polls.create`.
* Access class: `player`.
* Sources: `player`.
* Target behavior: `self`.
* Cooldown policy: `sef:control.polls.vote`.
* Confirmation: not required.
* Audit class: `workflow_execution`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.portal_policy.create`

* Description: Executes the `/sef control portal_policy create` action through the shared policy pipeline.
* Usage: `/sef control portal_policy create`.
* Canonical route: `sef control portal_policy create`.
* Example: `/sef control portal_policy create`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.portal_policy.create`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.portal_policy.create`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.portal_policy.manage`

* Description: Executes the `/sef control portal_policy manage` action through the shared policy pipeline.
* Usage: `/sef control portal_policy manage`.
* Canonical route: `sef control portal_policy manage`.
* Example: `/sef control portal_policy manage`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.portal_policy.manage`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.portal_policy.manage`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.portal_policy.view`

* Description: Executes the `/sef control portal_policy` action through the shared policy pipeline.
* Usage: `/sef control portal_policy`.
* Canonical route: `sef control portal_policy`.
* Example: `/sef control portal_policy`.
* Convenience roots: `portal`.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.portal_policy.view`.
* Access class: `trusted_player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:control.portal_policy.view`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.privacy.create`

* Description: Executes the `/sef control privacy create` action through the shared policy pipeline.
* Usage: `/sef control privacy create`.
* Canonical route: `sef control privacy create`.
* Example: `/sef control privacy create`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.privacy.create`.
* Access class: `player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.privacy.create`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.privacy.manage`

* Description: Executes the `/sef control privacy manage` action through the shared policy pipeline.
* Usage: `/sef control privacy manage`.
* Canonical route: `sef control privacy manage`.
* Example: `/sef control privacy manage`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.privacy.manage`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.privacy.manage`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.privacy.request`

* Description: Executes the `/privacy request` action through the shared policy pipeline.
* Usage: `/privacy request`.
* Canonical route: `privacy request`.
* Example: `/privacy request`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.privacy.create`.
* Access class: `player`.
* Sources: `player`.
* Target behavior: `self`.
* Cooldown policy: `sef:control.privacy.request`.
* Confirmation: not required.
* Audit class: `workflow_execution`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.privacy.view`

* Description: Executes the `/sef control privacy` action through the shared policy pipeline.
* Usage: `/sef control privacy`.
* Canonical route: `sef control privacy`.
* Example: `/sef control privacy`.
* Convenience roots: `mydata`, `privacy`, `privacycenter`, `privacyrequests`.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.privacy.view`.
* Access class: `staff`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:control.privacy.view`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.queue.create`

* Description: Executes the `/sef control queue create` action through the shared policy pipeline.
* Usage: `/sef control queue create`.
* Canonical route: `sef control queue create`.
* Example: `/sef control queue create`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.queue.create`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.queue.create`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.queue.manage`

* Description: Executes the `/sef control queue manage` action through the shared policy pipeline.
* Usage: `/sef control queue manage`.
* Canonical route: `sef control queue manage`.
* Example: `/sef control queue manage`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.queue.manage`.
* Access class: `owner`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.queue.manage`.
* Confirmation: not required.
* Audit class: `destructive`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.queue.view`

* Description: Executes the `/sef control queue` action through the shared policy pipeline.
* Usage: `/sef control queue`.
* Canonical route: `sef control queue`.
* Example: `/sef control queue`.
* Convenience roots: `queue`.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.queue.view`.
* Access class: `trusted_player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:control.queue.view`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.reports.create`

* Description: Executes the `/sef control reports create` action through the shared policy pipeline.
* Usage: `/sef control reports create`.
* Canonical route: `sef control reports create`.
* Example: `/sef control reports create`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.reports.create`.
* Access class: `player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.reports.create`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.reports.manage`

* Description: Executes the `/sef control reports manage` action through the shared policy pipeline.
* Usage: `/sef control reports manage`.
* Canonical route: `sef control reports manage`.
* Example: `/sef control reports manage`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.reports.manage`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.reports.manage`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.reports.submit`

* Description: Executes the `/report` action through the shared policy pipeline.
* Usage: `/report`.
* Canonical route: `report`.
* Example: `/report`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.reports.create`.
* Access class: `player`.
* Sources: `player`.
* Target behavior: `self`.
* Cooldown policy: `sef:control.reports.submit`.
* Confirmation: not required.
* Audit class: `workflow_execution`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.reports.view`

* Description: Executes the `/sef control reports` action through the shared policy pipeline.
* Usage: `/sef control reports`.
* Canonical route: `sef control reports`.
* Example: `/sef control reports`.
* Convenience roots: `report`, `reports`.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.reports.view`.
* Access class: `staff`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:control.reports.view`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.resource_governor.create`

* Description: Executes the `/sef control resource_governor create` action through the shared policy pipeline.
* Usage: `/sef control resource_governor create`.
* Canonical route: `sef control resource_governor create`.
* Example: `/sef control resource_governor create`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.resource_governor.create`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.resource_governor.create`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.resource_governor.manage`

* Description: Executes the `/sef control resource_governor manage` action through the shared policy pipeline.
* Usage: `/sef control resource_governor manage`.
* Canonical route: `sef control resource_governor manage`.
* Example: `/sef control resource_governor manage`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.resource_governor.manage`.
* Access class: `owner`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.resource_governor.manage`.
* Confirmation: not required.
* Audit class: `destructive`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.resource_governor.view`

* Description: Executes the `/sef control resource_governor` action through the shared policy pipeline.
* Usage: `/sef control resource_governor`.
* Canonical route: `sef control resource_governor`.
* Example: `/sef control resource_governor`.
* Convenience roots: `governor`.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.resource_governor.view`.
* Access class: `trusted_player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:control.resource_governor.view`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.resource_packs.create`

* Description: Executes the `/sef control resource_packs create` action through the shared policy pipeline.
* Usage: `/sef control resource_packs create`.
* Canonical route: `sef control resource_packs create`.
* Example: `/sef control resource_packs create`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.resource_packs.create`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.resource_packs.create`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.resource_packs.manage`

* Description: Executes the `/sef control resource_packs manage` action through the shared policy pipeline.
* Usage: `/sef control resource_packs manage`.
* Canonical route: `sef control resource_packs manage`.
* Example: `/sef control resource_packs manage`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.resource_packs.manage`.
* Access class: `owner`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.resource_packs.manage`.
* Confirmation: not required.
* Audit class: `destructive`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.resource_packs.view`

* Description: Executes the `/sef control resource_packs` action through the shared policy pipeline.
* Usage: `/sef control resource_packs`.
* Canonical route: `sef control resource_packs`.
* Example: `/sef control resource_packs`.
* Convenience roots: `resourcepack`.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.resource_packs.view`.
* Access class: `trusted_player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:control.resource_packs.view`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.resource_worlds.create`

* Description: Executes the `/sef control resource_worlds create` action through the shared policy pipeline.
* Usage: `/sef control resource_worlds create`.
* Canonical route: `sef control resource_worlds create`.
* Example: `/sef control resource_worlds create`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.resource_worlds.create`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.resource_worlds.create`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.resource_worlds.manage`

* Description: Executes the `/sef control resource_worlds manage` action through the shared policy pipeline.
* Usage: `/sef control resource_worlds manage`.
* Canonical route: `sef control resource_worlds manage`.
* Example: `/sef control resource_worlds manage`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.resource_worlds.manage`.
* Access class: `owner`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.resource_worlds.manage`.
* Confirmation: not required.
* Audit class: `destructive`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.resource_worlds.view`

* Description: Executes the `/sef control resource_worlds` action through the shared policy pipeline.
* Usage: `/sef control resource_worlds`.
* Canonical route: `sef control resource_worlds`.
* Example: `/sef control resource_worlds`.
* Convenience roots: `resourceworld`.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.resource_worlds.view`.
* Access class: `trusted_player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:control.resource_worlds.view`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.restart_coordinator.create`

* Description: Executes the `/sef control restart_coordinator create` action through the shared policy pipeline.
* Usage: `/sef control restart_coordinator create`.
* Canonical route: `sef control restart_coordinator create`.
* Example: `/sef control restart_coordinator create`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.restart_coordinator.create`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.restart_coordinator.create`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.restart_coordinator.manage`

* Description: Executes the `/sef control restart_coordinator manage` action through the shared policy pipeline.
* Usage: `/sef control restart_coordinator manage`.
* Canonical route: `sef control restart_coordinator manage`.
* Example: `/sef control restart_coordinator manage`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.restart_coordinator.manage`.
* Access class: `owner`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.restart_coordinator.manage`.
* Confirmation: not required.
* Audit class: `destructive`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.restart_coordinator.view`

* Description: Executes the `/sef control restart_coordinator` action through the shared policy pipeline.
* Usage: `/sef control restart_coordinator`.
* Canonical route: `sef control restart_coordinator`.
* Example: `/sef control restart_coordinator`.
* Convenience roots: `restart`.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.restart_coordinator.view`.
* Access class: `trusted_player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:control.restart_coordinator.view`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.rollouts.create`

* Description: Executes the `/sef control rollouts create` action through the shared policy pipeline.
* Usage: `/sef control rollouts create`.
* Canonical route: `sef control rollouts create`.
* Example: `/sef control rollouts create`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.rollouts.create`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.rollouts.create`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.rollouts.manage`

* Description: Executes the `/sef control rollouts manage` action through the shared policy pipeline.
* Usage: `/sef control rollouts manage`.
* Canonical route: `sef control rollouts manage`.
* Example: `/sef control rollouts manage`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.rollouts.manage`.
* Access class: `owner`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.rollouts.manage`.
* Confirmation: not required.
* Audit class: `destructive`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.rollouts.view`

* Description: Executes the `/sef control rollouts` action through the shared policy pipeline.
* Usage: `/sef control rollouts`.
* Canonical route: `sef control rollouts`.
* Example: `/sef control rollouts`.
* Convenience roots: `rollout`.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.rollouts.view`.
* Access class: `trusted_player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:control.rollouts.view`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.rules.accept`

* Description: Executes the `/rules accept` action through the shared policy pipeline.
* Usage: `/rules accept`.
* Canonical route: `rules accept`.
* Example: `/rules accept`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.rules.create`.
* Access class: `player`.
* Sources: `player`.
* Target behavior: `self`.
* Cooldown policy: `sef:control.rules.accept`.
* Confirmation: not required.
* Audit class: `workflow_execution`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.rules.create`

* Description: Executes the `/sef control rules create` action through the shared policy pipeline.
* Usage: `/sef control rules create`.
* Canonical route: `sef control rules create`.
* Example: `/sef control rules create`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.rules.create`.
* Access class: `player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.rules.create`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.rules.manage`

* Description: Executes the `/sef control rules manage` action through the shared policy pipeline.
* Usage: `/sef control rules manage`.
* Canonical route: `sef control rules manage`.
* Example: `/sef control rules manage`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.rules.manage`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.rules.manage`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.rules.view`

* Description: Executes the `/sef control rules` action through the shared policy pipeline.
* Usage: `/sef control rules`.
* Canonical route: `sef control rules`.
* Example: `/sef control rules`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.rules.view`.
* Access class: `player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:control.rules.view`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.server_calendar.create`

* Description: Executes the `/sef control server_calendar create` action through the shared policy pipeline.
* Usage: `/sef control server_calendar create`.
* Canonical route: `sef control server_calendar create`.
* Example: `/sef control server_calendar create`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.server_calendar.create`.
* Access class: `player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.server_calendar.create`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.server_calendar.manage`

* Description: Executes the `/sef control server_calendar manage` action through the shared policy pipeline.
* Usage: `/sef control server_calendar manage`.
* Canonical route: `sef control server_calendar manage`.
* Example: `/sef control server_calendar manage`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.server_calendar.manage`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.server_calendar.manage`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.server_calendar.subscribe`

* Description: Executes the `/calendar subscribe` action through the shared policy pipeline.
* Usage: `/calendar subscribe`.
* Canonical route: `calendar subscribe`.
* Example: `/calendar subscribe`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.server_calendar.create`.
* Access class: `player`.
* Sources: `player`.
* Target behavior: `self`.
* Cooldown policy: `sef:control.server_calendar.subscribe`.
* Confirmation: not required.
* Audit class: `workflow_execution`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.server_calendar.view`

* Description: Executes the `/sef control server_calendar` action through the shared policy pipeline.
* Usage: `/sef control server_calendar`.
* Canonical route: `sef control server_calendar`.
* Example: `/sef control server_calendar`.
* Convenience roots: `calendar`.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.server_calendar.view`.
* Access class: `player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:control.server_calendar.view`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.server_presentation.create`

* Description: Executes the `/sef control server_presentation create` action through the shared policy pipeline.
* Usage: `/sef control server_presentation create`.
* Canonical route: `sef control server_presentation create`.
* Example: `/sef control server_presentation create`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.server_presentation.create`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.server_presentation.create`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.server_presentation.manage`

* Description: Executes the `/sef control server_presentation manage` action through the shared policy pipeline.
* Usage: `/sef control server_presentation manage`.
* Canonical route: `sef control server_presentation manage`.
* Example: `/sef control server_presentation manage`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.server_presentation.manage`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.server_presentation.manage`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.server_presentation.view`

* Description: Executes the `/sef control server_presentation` action through the shared policy pipeline.
* Usage: `/sef control server_presentation`.
* Canonical route: `sef control server_presentation`.
* Example: `/sef control server_presentation`.
* Convenience roots: `serverpresentation`.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.server_presentation.view`.
* Access class: `trusted_player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:control.server_presentation.view`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.session_quarantine.create`

* Description: Executes the `/sef control session_quarantine create` action through the shared policy pipeline.
* Usage: `/sef control session_quarantine create`.
* Canonical route: `sef control session_quarantine create`.
* Example: `/sef control session_quarantine create`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.session_quarantine.create`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.session_quarantine.create`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.session_quarantine.manage`

* Description: Executes the `/sef control session_quarantine manage` action through the shared policy pipeline.
* Usage: `/sef control session_quarantine manage`.
* Canonical route: `sef control session_quarantine manage`.
* Example: `/sef control session_quarantine manage`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.session_quarantine.manage`.
* Access class: `owner`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.session_quarantine.manage`.
* Confirmation: not required.
* Audit class: `destructive`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.session_quarantine.view`

* Description: Executes the `/sef control session_quarantine` action through the shared policy pipeline.
* Usage: `/sef control session_quarantine`.
* Canonical route: `sef control session_quarantine`.
* Example: `/sef control session_quarantine`.
* Convenience roots: `quarantine`.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.session_quarantine.view`.
* Access class: `staff`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:control.session_quarantine.view`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.sleep_vote.create`

* Description: Executes the `/sef control sleep_vote create` action through the shared policy pipeline.
* Usage: `/sef control sleep_vote create`.
* Canonical route: `sef control sleep_vote create`.
* Example: `/sef control sleep_vote create`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.sleep_vote.create`.
* Access class: `player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.sleep_vote.create`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.sleep_vote.manage`

* Description: Executes the `/sef control sleep_vote manage` action through the shared policy pipeline.
* Usage: `/sef control sleep_vote manage`.
* Canonical route: `sef control sleep_vote manage`.
* Example: `/sef control sleep_vote manage`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.sleep_vote.manage`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.sleep_vote.manage`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.sleep_vote.view`

* Description: Executes the `/sef control sleep_vote` action through the shared policy pipeline.
* Usage: `/sef control sleep_vote`.
* Canonical route: `sef control sleep_vote`.
* Example: `/sef control sleep_vote`.
* Convenience roots: `sleepvote`.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.sleep_vote.view`.
* Access class: `player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:control.sleep_vote.view`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.sleep_vote.vote`

* Description: Executes the `/sleepvote yes` action through the shared policy pipeline.
* Usage: `/sleepvote yes`.
* Canonical route: `sleepvote yes`.
* Example: `/sleepvote yes`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.sleep_vote.create`.
* Access class: `player`.
* Sources: `player`.
* Target behavior: `self`.
* Cooldown policy: `sef:control.sleep_vote.vote`.
* Confirmation: not required.
* Audit class: `workflow_execution`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.spawn_ecology.create`

* Description: Executes the `/sef control spawn_ecology create` action through the shared policy pipeline.
* Usage: `/sef control spawn_ecology create`.
* Canonical route: `sef control spawn_ecology create`.
* Example: `/sef control spawn_ecology create`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.spawn_ecology.create`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.spawn_ecology.create`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.spawn_ecology.manage`

* Description: Executes the `/sef control spawn_ecology manage` action through the shared policy pipeline.
* Usage: `/sef control spawn_ecology manage`.
* Canonical route: `sef control spawn_ecology manage`.
* Example: `/sef control spawn_ecology manage`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.spawn_ecology.manage`.
* Access class: `owner`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.spawn_ecology.manage`.
* Confirmation: not required.
* Audit class: `destructive`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.spawn_ecology.view`

* Description: Executes the `/sef control spawn_ecology` action through the shared policy pipeline.
* Usage: `/sef control spawn_ecology`.
* Canonical route: `sef control spawn_ecology`.
* Example: `/sef control spawn_ecology`.
* Convenience roots: `spawnpolicy`.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.spawn_ecology.view`.
* Access class: `trusted_player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:control.spawn_ecology.view`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.staff_duty.create`

* Description: Executes the `/sef control staff_duty create` action through the shared policy pipeline.
* Usage: `/sef control staff_duty create`.
* Canonical route: `sef control staff_duty create`.
* Example: `/sef control staff_duty create`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.staff_duty.create`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.staff_duty.create`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.staff_duty.manage`

* Description: Executes the `/sef control staff_duty manage` action through the shared policy pipeline.
* Usage: `/sef control staff_duty manage`.
* Canonical route: `sef control staff_duty manage`.
* Example: `/sef control staff_duty manage`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.staff_duty.manage`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.staff_duty.manage`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.staff_duty.view`

* Description: Executes the `/sef control staff_duty` action through the shared policy pipeline.
* Usage: `/sef control staff_duty`.
* Canonical route: `sef control staff_duty`.
* Example: `/sef control staff_duty`.
* Convenience roots: `staffduty`, `staffshift`.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.staff_duty.view`.
* Access class: `trusted_player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:control.staff_duty.view`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.staff_notes.create`

* Description: Executes the `/sef control staff_notes create` action through the shared policy pipeline.
* Usage: `/sef control staff_notes create`.
* Canonical route: `sef control staff_notes create`.
* Example: `/sef control staff_notes create`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.staff_notes.create`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.staff_notes.create`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.staff_notes.manage`

* Description: Executes the `/sef control staff_notes manage` action through the shared policy pipeline.
* Usage: `/sef control staff_notes manage`.
* Canonical route: `sef control staff_notes manage`.
* Example: `/sef control staff_notes manage`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.staff_notes.manage`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.staff_notes.manage`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.staff_notes.view`

* Description: Executes the `/sef control staff_notes` action through the shared policy pipeline.
* Usage: `/sef control staff_notes`.
* Canonical route: `sef control staff_notes`.
* Example: `/sef control staff_notes`.
* Convenience roots: `staffnote`, `staffnotes`.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.staff_notes.view`.
* Access class: `staff`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:control.staff_notes.view`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.status`

* Description: Executes the `/sef control status` action through the shared policy pipeline.
* Usage: `/sef control status`.
* Canonical route: `sef control status`.
* Example: `/sef control status`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.status`.
* Access class: `staff`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:control.status`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.tickets.create`

* Description: Executes the `/sef control tickets create` action through the shared policy pipeline.
* Usage: `/sef control tickets create`.
* Canonical route: `sef control tickets create`.
* Example: `/sef control tickets create`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.tickets.create`.
* Access class: `player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.tickets.create`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.tickets.manage`

* Description: Executes the `/sef control tickets manage` action through the shared policy pipeline.
* Usage: `/sef control tickets manage`.
* Canonical route: `sef control tickets manage`.
* Example: `/sef control tickets manage`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.tickets.manage`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.tickets.manage`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.tickets.submit`

* Description: Executes the `/ticket` action through the shared policy pipeline.
* Usage: `/ticket`.
* Canonical route: `ticket`.
* Example: `/ticket`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.tickets.create`.
* Access class: `player`.
* Sources: `player`.
* Target behavior: `self`.
* Cooldown policy: `sef:control.tickets.submit`.
* Confirmation: not required.
* Audit class: `workflow_execution`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.tickets.view`

* Description: Executes the `/sef control tickets` action through the shared policy pipeline.
* Usage: `/sef control tickets`.
* Canonical route: `sef control tickets`.
* Example: `/sef control tickets`.
* Convenience roots: `ticket`, `tickets`.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.tickets.view`.
* Access class: `staff`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:control.tickets.view`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.trades.create`

* Description: Executes the `/sef control trades create` action through the shared policy pipeline.
* Usage: `/sef control trades create`.
* Canonical route: `sef control trades create`.
* Example: `/sef control trades create`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.trades.create`.
* Access class: `player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.trades.create`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.trades.manage`

* Description: Executes the `/sef control trades manage` action through the shared policy pipeline.
* Usage: `/sef control trades manage`.
* Canonical route: `sef control trades manage`.
* Example: `/sef control trades manage`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.trades.manage`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.trades.manage`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.trades.view`

* Description: Executes the `/sef control trades` action through the shared policy pipeline.
* Usage: `/sef control trades`.
* Canonical route: `sef control trades`.
* Example: `/sef control trades`.
* Convenience roots: `trade`, `tradeadmin`, `trades`.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.trades.view`.
* Access class: `player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:control.trades.view`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.waypoints.create`

* Description: Executes the `/sef control waypoints create` action through the shared policy pipeline.
* Usage: `/sef control waypoints create`.
* Canonical route: `sef control waypoints create`.
* Example: `/sef control waypoints create`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.waypoints.create`.
* Access class: `player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.waypoints.create`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.waypoints.go`

* Description: Executes the `/waypoint go` action through the shared policy pipeline.
* Usage: `/waypoint go`.
* Canonical route: `waypoint go`.
* Example: `/waypoint go`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.waypoints.create`.
* Access class: `player`.
* Sources: `player`.
* Target behavior: `self`.
* Cooldown policy: `sef:control.waypoints.go`.
* Confirmation: not required.
* Audit class: `workflow_execution`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.waypoints.manage`

* Description: Executes the `/sef control waypoints manage` action through the shared policy pipeline.
* Usage: `/sef control waypoints manage`.
* Canonical route: `sef control waypoints manage`.
* Example: `/sef control waypoints manage`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.waypoints.manage`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.waypoints.manage`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.waypoints.remove`

* Description: Executes the `/waypoint remove` action through the shared policy pipeline.
* Usage: `/waypoint remove`.
* Canonical route: `waypoint remove`.
* Example: `/waypoint remove`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.waypoints.create`.
* Access class: `player`.
* Sources: `player`.
* Target behavior: `self`.
* Cooldown policy: `sef:control.waypoints.remove`.
* Confirmation: not required.
* Audit class: `workflow_execution`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.waypoints.set`

* Description: Executes the `/waypoint set` action through the shared policy pipeline.
* Usage: `/waypoint set`.
* Canonical route: `waypoint set`.
* Example: `/waypoint set`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.waypoints.create`.
* Access class: `player`.
* Sources: `player`.
* Target behavior: `self`.
* Cooldown policy: `sef:control.waypoints.set`.
* Confirmation: not required.
* Audit class: `workflow_execution`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.waypoints.view`

* Description: Executes the `/sef control waypoints` action through the shared policy pipeline.
* Usage: `/sef control waypoints`.
* Canonical route: `sef control waypoints`.
* Example: `/sef control waypoints`.
* Convenience roots: `waypoint`, `waypoints`.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.waypoints.view`.
* Access class: `player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:control.waypoints.view`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.weekly_rewards.claim`

* Description: Executes the `/weekly claim` action through the shared policy pipeline.
* Usage: `/weekly claim`.
* Canonical route: `weekly claim`.
* Example: `/weekly claim`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.weekly_rewards.create`.
* Access class: `player`.
* Sources: `player`.
* Target behavior: `self`.
* Cooldown policy: `sef:control.weekly_rewards.claim`.
* Confirmation: not required.
* Audit class: `workflow_execution`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.weekly_rewards.create`

* Description: Executes the `/sef control weekly_rewards create` action through the shared policy pipeline.
* Usage: `/sef control weekly_rewards create`.
* Canonical route: `sef control weekly_rewards create`.
* Example: `/sef control weekly_rewards create`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.weekly_rewards.create`.
* Access class: `player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.weekly_rewards.create`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.weekly_rewards.manage`

* Description: Executes the `/sef control weekly_rewards manage` action through the shared policy pipeline.
* Usage: `/sef control weekly_rewards manage`.
* Canonical route: `sef control weekly_rewards manage`.
* Example: `/sef control weekly_rewards manage`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.weekly_rewards.manage`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.weekly_rewards.manage`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.weekly_rewards.view`

* Description: Executes the `/sef control weekly_rewards` action through the shared policy pipeline.
* Usage: `/sef control weekly_rewards`.
* Canonical route: `sef control weekly_rewards`.
* Example: `/sef control weekly_rewards`.
* Convenience roots: `weekly`.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.weekly_rewards.view`.
* Access class: `player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:control.weekly_rewards.view`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.world_border.create`

* Description: Executes the `/sef control world_border create` action through the shared policy pipeline.
* Usage: `/sef control world_border create`.
* Canonical route: `sef control world_border create`.
* Example: `/sef control world_border create`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.world_border.create`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.world_border.create`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.world_border.manage`

* Description: Executes the `/sef control world_border manage` action through the shared policy pipeline.
* Usage: `/sef control world_border manage`.
* Canonical route: `sef control world_border manage`.
* Example: `/sef control world_border manage`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.world_border.manage`.
* Access class: `owner`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.world_border.manage`.
* Confirmation: not required.
* Audit class: `destructive`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.world_border.view`

* Description: Executes the `/sef control world_border` action through the shared policy pipeline.
* Usage: `/sef control world_border`.
* Canonical route: `sef control world_border`.
* Example: `/sef control world_border`.
* Convenience roots: `borderprofile`.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.world_border.view`.
* Access class: `trusted_player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:control.world_border.view`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.world_policy.create`

* Description: Executes the `/sef control world_policy create` action through the shared policy pipeline.
* Usage: `/sef control world_policy create`.
* Canonical route: `sef control world_policy create`.
* Example: `/sef control world_policy create`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.world_policy.create`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.world_policy.create`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.world_policy.manage`

* Description: Executes the `/sef control world_policy manage` action through the shared policy pipeline.
* Usage: `/sef control world_policy manage`.
* Canonical route: `sef control world_policy manage`.
* Example: `/sef control world_policy manage`.
* Convenience roots: none.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.world_policy.manage`.
* Access class: `owner`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:control.world_policy.manage`.
* Confirmation: not required.
* Audit class: `destructive`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:control.world_policy.view`

* Description: Executes the `/sef control world_policy` action through the shared policy pipeline.
* Usage: `/sef control world_policy`.
* Canonical route: `sef control world_policy`.
* Example: `/sef control world_policy`.
* Convenience roots: `worldpolicy`.
* Category: `control`.
* Feature gate: `sef.control`.
* Permissions: `sef.commands.control.world_policy.view`.
* Access class: `trusted_player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:control.world_policy.view`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:control`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:core.colors`

* Description: Executes the `/sef colors` action through the shared policy pipeline.
* Usage: `/sef colors`.
* Canonical route: `sef colors`.
* Example: `/sef colors`.
* Convenience roots: `colors`.
* Category: `sef`.
* Feature gate: `sef.core`.
* Permissions: `sef.commands.sef.allowed`, `sef.commands.sef.colors`.
* Access class: `player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:core.colors`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:core`.
* HUD contract: informational command has no persistent hud.
* Quota contract: command does not create retained records or variable fan out.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:core.commands`

* Description: Executes the `/sef commands` action through the shared policy pipeline.
* Usage: `/sef commands`.
* Canonical route: `sef commands`.
* Example: `/sef commands`.
* Convenience roots: none.
* Category: `sef`.
* Feature gate: `sef.core`.
* Permissions: `sef.commands.sef.allowed`, `sef.commands.sef.commands`.
* Access class: `player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:core.commands`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:core`.
* HUD contract: catalog view has no persistent hud.
* Quota contract: command does not create retained records or variable fan out.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:core.conflicts`

* Description: Executes the `/sef conflicts` action through the shared policy pipeline.
* Usage: `/sef conflicts`.
* Canonical route: `sef conflicts`.
* Example: `/sef conflicts`.
* Convenience roots: none.
* Category: `sef`.
* Feature gate: `sef.core`.
* Permissions: `sef.commands.sef.allowed`, `sef.commands.sef.conflicts`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:core.conflicts`.
* Confirmation: not required.
* Audit class: `sensitive_access`.
* GUI descriptor: `sef:core`.
* HUD contract: diagnostic view has no persistent hud.
* Quota contract: command does not create retained records or variable fan out.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:core.doctor`

* Description: Executes the `/sef doctor` action through the shared policy pipeline.
* Usage: `/sef doctor`.
* Canonical route: `sef doctor`.
* Example: `/sef doctor`.
* Convenience roots: none.
* Category: `sef`.
* Feature gate: `sef.core`.
* Permissions: `sef.commands.sef.allowed`, `sef.commands.sef.doctor`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:core.doctor`.
* Confirmation: not required.
* Audit class: `sensitive_access`.
* GUI descriptor: `sef:core`.
* HUD contract: diagnostic view has no persistent hud.
* Quota contract: command does not create retained records or variable fan out.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:core.info`

* Description: Executes the `/sef info` action through the shared policy pipeline.
* Usage: `/sef info`.
* Canonical route: `sef info`.
* Example: `/sef info`.
* Convenience roots: none.
* Category: `sef`.
* Feature gate: `sef.core`.
* Permissions: `sef.commands.sef.allowed`, `sef.commands.sef.info`.
* Access class: `player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:core.info`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:core`.
* HUD contract: informational command has no persistent hud.
* Quota contract: command does not create retained records or variable fan out.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:core.reload`

* Description: Executes the `/sef reload` action through the shared policy pipeline.
* Usage: `/sef reload`.
* Canonical route: `sef reload`.
* Example: `/sef reload`.
* Convenience roots: none.
* Category: `sef`.
* Feature gate: `sef.core`.
* Permissions: `sef.commands.sef.allowed`, `sef.commands.sef.reload`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:core.reload`.
* Confirmation: not required.
* Audit class: `config_definition`.
* GUI descriptor: `sef:core`.
* HUD contract: reload result is immediate.
* Quota contract: command does not create retained records or variable fan out.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:core.test`

* Description: Executes the `/sef test` action through the shared policy pipeline.
* Usage: `/sef test`.
* Canonical route: `sef test`.
* Example: `/sef test`.
* Convenience roots: none.
* Category: `sef`.
* Feature gate: `sef.core`.
* Permissions: `sef.commands.sef.allowed`, `sef.commands.sef.test`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:core.test`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:core`.
* HUD contract: debug command has no persistent hud.
* Quota contract: command does not create retained records or variable fan out.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:disguise.ability`

* Description: Executes the `/disguise ability` action through the shared policy pipeline.
* Usage: `/disguise ability`.
* Canonical route: `disguise ability`.
* Example: `/disguise ability`.
* Convenience roots: `dability`.
* Category: `disguise`.
* Feature gate: `sef.disguise`.
* Permissions: `sef.commands.disguise.ability`.
* Access class: `trusted_player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `self`.
* Cooldown policy: `sef:disguise.ability`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:identity`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:disguise.clear`

* Description: Executes the `/disguise clear` action through the shared policy pipeline.
* Usage: `/disguise clear`.
* Canonical route: `disguise clear`.
* Example: `/disguise clear`.
* Convenience roots: `undisguise`.
* Category: `disguise`.
* Feature gate: `sef.disguise`.
* Permissions: `sef.commands.disguise.clear`.
* Access class: `trusted_player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `self`.
* Cooldown policy: `sef:disguise.clear`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:identity`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:disguise.conflicts`

* Description: Executes the `/disguise conflicts` action through the shared policy pipeline.
* Usage: `/disguise conflicts`.
* Canonical route: `disguise conflicts`.
* Example: `/disguise conflicts`.
* Convenience roots: none.
* Category: `disguise`.
* Feature gate: `sef.disguise`.
* Permissions: `sef.commands.disguise.conflicts`.
* Access class: `staff`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `self`.
* Cooldown policy: `sef:disguise.conflicts`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:identity`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:disguise.inspect`

* Description: Executes the `/disguise inspect` action through the shared policy pipeline.
* Usage: `/disguise inspect`.
* Canonical route: `disguise inspect`.
* Example: `/disguise inspect`.
* Convenience roots: none.
* Category: `disguise`.
* Feature gate: `sef.disguise`.
* Permissions: `sef.commands.disguise.inspect`.
* Access class: `staff`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:disguise.inspect`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:identity`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:disguise.list`

* Description: Executes the `/disguise list` action through the shared policy pipeline.
* Usage: `/disguise list`.
* Canonical route: `disguise list`.
* Example: `/disguise list`.
* Convenience roots: none.
* Category: `disguise`.
* Feature gate: `sef.disguise`.
* Permissions: `sef.commands.disguise.list`.
* Access class: `player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `self`.
* Cooldown policy: `sef:disguise.list`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:identity`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:disguise.options`

* Description: Executes the `/disguise options` action through the shared policy pipeline.
* Usage: `/disguise options`.
* Canonical route: `disguise options`.
* Example: `/disguise options`.
* Convenience roots: none.
* Category: `disguise`.
* Feature gate: `sef.disguise`.
* Permissions: `sef.commands.disguise.options`.
* Access class: `staff`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `self`.
* Cooldown policy: `sef:disguise.options`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:identity`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:disguise.preset.manage`

* Description: Executes the `/disguise presets` action through the shared policy pipeline.
* Usage: `/disguise presets`.
* Canonical route: `disguise presets`.
* Example: `/disguise presets`.
* Convenience roots: none.
* Category: `disguise`.
* Feature gate: `sef.disguise`.
* Permissions: `sef.commands.disguise.preset.manage`.
* Access class: `staff`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `self`.
* Cooldown policy: `sef:disguise.preset.manage`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:identity`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:disguise.preview`

* Description: Executes the `/disguise preview` action through the shared policy pipeline.
* Usage: `/disguise preview`.
* Canonical route: `disguise preview`.
* Example: `/disguise preview`.
* Convenience roots: none.
* Category: `disguise`.
* Feature gate: `sef.disguise`.
* Permissions: `sef.commands.disguise.preview`.
* Access class: `staff`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `self`.
* Cooldown policy: `sef:disguise.preview`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:identity`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:disguise.set.mob`

* Description: Executes the `/disguise mob` action through the shared policy pipeline.
* Usage: `/disguise mob`.
* Canonical route: `disguise mob`.
* Example: `/disguise mob`.
* Convenience roots: `disguise`.
* Category: `disguise`.
* Feature gate: `sef.disguise`.
* Permissions: `sef.commands.disguise.mob`.
* Access class: `trusted_player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `self`.
* Cooldown policy: `sef:disguise.set.mob`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:identity`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:disguise.set.player`

* Description: Executes the `/disguise player` action through the shared policy pipeline.
* Usage: `/disguise player`.
* Canonical route: `disguise player`.
* Example: `/disguise player`.
* Convenience roots: none.
* Category: `disguise`.
* Feature gate: `sef.disguise`.
* Permissions: `sef.commands.disguise.player`.
* Access class: `trusted_player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `self`.
* Cooldown policy: `sef:disguise.set.player`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:identity`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:disguise.set.preset`

* Description: Executes the `/disguise preset` action through the shared policy pipeline.
* Usage: `/disguise preset`.
* Canonical route: `disguise preset`.
* Example: `/disguise preset`.
* Convenience roots: none.
* Category: `disguise`.
* Feature gate: `sef.disguise`.
* Permissions: `sef.commands.disguise.preset`.
* Access class: `trusted_player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `self`.
* Cooldown policy: `sef:disguise.set.preset`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:identity`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:disguise.status`

* Description: Executes the `/disguise status` action through the shared policy pipeline.
* Usage: `/disguise status`.
* Canonical route: `disguise status`.
* Example: `/disguise status`.
* Convenience roots: none.
* Category: `disguise`.
* Feature gate: `sef.disguise`.
* Permissions: `sef.commands.disguise.status`.
* Access class: `player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `self`.
* Cooldown policy: `sef:disguise.status`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:identity`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:economy.admin.freeze`

* Description: Executes the `/eco freeze` action through the shared policy pipeline.
* Usage: `/eco freeze`.
* Canonical route: `eco freeze`.
* Example: `/eco freeze`.
* Convenience roots: none.
* Category: `economy`.
* Feature gate: `sef.economy`.
* Permissions: `sef.commands.eco.freeze`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `required_player`.
* Cooldown policy: `sef:economy.admin.freeze`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:economy`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:economy.admin.give`

* Description: Executes the `/eco give` action through the shared policy pipeline.
* Usage: `/eco give`.
* Canonical route: `eco give`.
* Example: `/eco give`.
* Convenience roots: none.
* Category: `economy`.
* Feature gate: `sef.economy`.
* Permissions: `sef.commands.eco.give`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `required_player`.
* Cooldown policy: `sef:economy.admin.give`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:economy`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:economy.admin.history`

* Description: Executes the `/eco history` action through the shared policy pipeline.
* Usage: `/eco history`.
* Canonical route: `eco history`.
* Example: `/eco history`.
* Convenience roots: none.
* Category: `economy`.
* Feature gate: `sef.economy`.
* Permissions: `sef.commands.eco.history`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `required_player`.
* Cooldown policy: `sef:economy.admin.history`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:economy`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:economy.admin.import`

* Description: Executes the `/eco import` action through the shared policy pipeline.
* Usage: `/eco import`.
* Canonical route: `eco import`.
* Example: `/eco import`.
* Convenience roots: none.
* Category: `economy`.
* Feature gate: `sef.economy`.
* Permissions: `sef.commands.eco.import`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `server`.
* Cooldown policy: `sef:economy.admin.import`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:economy`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:economy.admin.reset`

* Description: Executes the `/eco reset` action through the shared policy pipeline.
* Usage: `/eco reset`.
* Canonical route: `eco reset`.
* Example: `/eco reset`.
* Convenience roots: none.
* Category: `economy`.
* Feature gate: `sef.economy`.
* Permissions: `sef.commands.eco.reset`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `required_player`.
* Cooldown policy: `sef:economy.admin.reset`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:economy`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:economy.admin.set`

* Description: Executes the `/eco set` action through the shared policy pipeline.
* Usage: `/eco set`.
* Canonical route: `eco set`.
* Example: `/eco set`.
* Convenience roots: none.
* Category: `economy`.
* Feature gate: `sef.economy`.
* Permissions: `sef.commands.eco.set`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `required_player`.
* Cooldown policy: `sef:economy.admin.set`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:economy`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:economy.admin.take`

* Description: Executes the `/eco take` action through the shared policy pipeline.
* Usage: `/eco take`.
* Canonical route: `eco take`.
* Example: `/eco take`.
* Convenience roots: none.
* Category: `economy`.
* Feature gate: `sef.economy`.
* Permissions: `sef.commands.eco.take`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `required_player`.
* Cooldown policy: `sef:economy.admin.take`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:economy`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:economy.admin.unfreeze`

* Description: Executes the `/eco unfreeze` action through the shared policy pipeline.
* Usage: `/eco unfreeze`.
* Canonical route: `eco unfreeze`.
* Example: `/eco unfreeze`.
* Convenience roots: none.
* Category: `economy`.
* Feature gate: `sef.economy`.
* Permissions: `sef.commands.eco.unfreeze`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `required_player`.
* Cooldown policy: `sef:economy.admin.unfreeze`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:economy`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:economy.balance`

* Description: Executes the `/balance` action through the shared policy pipeline.
* Usage: `/balance`.
* Canonical route: `balance`.
* Example: `/balance`.
* Convenience roots: `bal`, `balance`, `money`.
* Category: `economy`.
* Feature gate: `sef.economy`.
* Permissions: `sef.commands.balance`.
* Access class: `player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:economy.balance`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:economy`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:economy.pay`

* Description: Executes the `/pay` action through the shared policy pipeline.
* Usage: `/pay`.
* Canonical route: `pay`.
* Example: `/pay`.
* Convenience roots: `pay`.
* Category: `economy`.
* Feature gate: `sef.economy`.
* Permissions: `sef.commands.pay`.
* Access class: `player`.
* Sources: `player`.
* Target behavior: `required_player`.
* Cooldown policy: `sef:economy.pay`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:economy`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:economy.pay.confirm`

* Description: Executes the `/payconfirmtoggle` action through the shared policy pipeline.
* Usage: `/payconfirmtoggle`.
* Canonical route: `payconfirmtoggle`.
* Example: `/payconfirmtoggle`.
* Convenience roots: `payconfirmtoggle`.
* Category: `economy`.
* Feature gate: `sef.economy`.
* Permissions: `sef.commands.payconfirmtoggle`.
* Access class: `player`.
* Sources: `player`.
* Target behavior: `self`.
* Cooldown policy: `sef:economy.pay.confirm`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:economy`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:economy.pay.toggle`

* Description: Executes the `/paytoggle` action through the shared policy pipeline.
* Usage: `/paytoggle`.
* Canonical route: `paytoggle`.
* Example: `/paytoggle`.
* Convenience roots: `paytoggle`.
* Category: `economy`.
* Feature gate: `sef.economy`.
* Permissions: `sef.commands.paytoggle`.
* Access class: `player`.
* Sources: `player`.
* Target behavior: `self`.
* Cooldown policy: `sef:economy.pay.toggle`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:economy`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:economy.sell`

* Description: Executes the `/sell` action through the shared policy pipeline.
* Usage: `/sell`.
* Canonical route: `sell`.
* Example: `/sell`.
* Convenience roots: `sell`.
* Category: `economy`.
* Feature gate: `sef.economy`.
* Permissions: `sef.commands.sell`.
* Access class: `player`.
* Sources: `player`.
* Target behavior: `self`.
* Cooldown policy: `sef:economy.sell`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:economy`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:economy.sign.balance`

* Description: Executes the `/sign balance` action through the shared policy pipeline.
* Usage: `/sign balance`.
* Canonical route: `sign balance`.
* Example: `/sign balance`.
* Convenience roots: none.
* Category: `economy.signs`.
* Feature gate: `sef.economy.signs`.
* Permissions: `sef.economy.sign.balance.use`.
* Access class: `player`.
* Sources: `player`.
* Target behavior: `self`.
* Cooldown policy: `sef:economy.sign.balance`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:economy`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: false.
* Shared pipeline: required.

### `sef:economy.sign.buy`

* Description: Executes the `/sign buy` action through the shared policy pipeline.
* Usage: `/sign buy`.
* Canonical route: `sign buy`.
* Example: `/sign buy`.
* Convenience roots: none.
* Category: `economy.signs`.
* Feature gate: `sef.economy.signs`.
* Permissions: `sef.economy.sign.buy.use`.
* Access class: `player`.
* Sources: `player`.
* Target behavior: `self`.
* Cooldown policy: `sef:economy.sign.buy`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:economy`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: false.
* Shared pipeline: required.

### `sef:economy.sign.create.balance`

* Description: Executes the `/sign create balance` action through the shared policy pipeline.
* Usage: `/sign create balance`.
* Canonical route: `sign create balance`.
* Example: `/sign create balance`.
* Convenience roots: none.
* Category: `economy.signs`.
* Feature gate: `sef.economy.signs`.
* Permissions: `sef.economy.sign.balance.create`.
* Access class: `administrator`.
* Sources: `player`.
* Target behavior: `server`.
* Cooldown policy: `sef:economy.sign.create.balance`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:economy`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: false.
* Shared pipeline: required.

### `sef:economy.sign.create.buy`

* Description: Executes the `/sign create buy` action through the shared policy pipeline.
* Usage: `/sign create buy`.
* Canonical route: `sign create buy`.
* Example: `/sign create buy`.
* Convenience roots: none.
* Category: `economy.signs`.
* Feature gate: `sef.economy.signs`.
* Permissions: `sef.economy.sign.buy.create`.
* Access class: `administrator`.
* Sources: `player`.
* Target behavior: `server`.
* Cooldown policy: `sef:economy.sign.create.buy`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:economy`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: false.
* Shared pipeline: required.

### `sef:economy.sign.create.disposal`

* Description: Executes the `/sign create disposal` action through the shared policy pipeline.
* Usage: `/sign create disposal`.
* Canonical route: `sign create disposal`.
* Example: `/sign create disposal`.
* Convenience roots: none.
* Category: `economy.signs`.
* Feature gate: `sef.economy.signs`.
* Permissions: `sef.economy.sign.disposal.create`.
* Access class: `administrator`.
* Sources: `player`.
* Target behavior: `server`.
* Cooldown policy: `sef:economy.sign.create.disposal`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:economy`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: false.
* Shared pipeline: required.

### `sef:economy.sign.create.free`

* Description: Executes the `/sign create free` action through the shared policy pipeline.
* Usage: `/sign create free`.
* Canonical route: `sign create free`.
* Example: `/sign create free`.
* Convenience roots: none.
* Category: `economy.signs`.
* Feature gate: `sef.economy.signs`.
* Permissions: `sef.economy.sign.free.create`.
* Access class: `administrator`.
* Sources: `player`.
* Target behavior: `server`.
* Cooldown policy: `sef:economy.sign.create.free`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:economy`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: false.
* Shared pipeline: required.

### `sef:economy.sign.create.heal`

* Description: Executes the `/sign create heal` action through the shared policy pipeline.
* Usage: `/sign create heal`.
* Canonical route: `sign create heal`.
* Example: `/sign create heal`.
* Convenience roots: none.
* Category: `economy.signs`.
* Feature gate: `sef.economy.signs`.
* Permissions: `sef.economy.sign.heal.create`.
* Access class: `administrator`.
* Sources: `player`.
* Target behavior: `server`.
* Cooldown policy: `sef:economy.sign.create.heal`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:economy`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: false.
* Shared pipeline: required.

### `sef:economy.sign.create.kit`

* Description: Executes the `/sign create kit` action through the shared policy pipeline.
* Usage: `/sign create kit`.
* Canonical route: `sign create kit`.
* Example: `/sign create kit`.
* Convenience roots: none.
* Category: `economy.signs`.
* Feature gate: `sef.economy.signs`.
* Permissions: `sef.economy.sign.kit.create`.
* Access class: `administrator`.
* Sources: `player`.
* Target behavior: `server`.
* Cooldown policy: `sef:economy.sign.create.kit`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:economy`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: false.
* Shared pipeline: required.

### `sef:economy.sign.create.repair`

* Description: Executes the `/sign create repair` action through the shared policy pipeline.
* Usage: `/sign create repair`.
* Canonical route: `sign create repair`.
* Example: `/sign create repair`.
* Convenience roots: none.
* Category: `economy.signs`.
* Feature gate: `sef.economy.signs`.
* Permissions: `sef.economy.sign.repair.create`.
* Access class: `administrator`.
* Sources: `player`.
* Target behavior: `server`.
* Cooldown policy: `sef:economy.sign.create.repair`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:economy`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: false.
* Shared pipeline: required.

### `sef:economy.sign.create.sell`

* Description: Executes the `/sign create sell` action through the shared policy pipeline.
* Usage: `/sign create sell`.
* Canonical route: `sign create sell`.
* Example: `/sign create sell`.
* Convenience roots: none.
* Category: `economy.signs`.
* Feature gate: `sef.economy.signs`.
* Permissions: `sef.economy.sign.sell.create`.
* Access class: `administrator`.
* Sources: `player`.
* Target behavior: `server`.
* Cooldown policy: `sef:economy.sign.create.sell`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:economy`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: false.
* Shared pipeline: required.

### `sef:economy.sign.create.time`

* Description: Executes the `/sign create time` action through the shared policy pipeline.
* Usage: `/sign create time`.
* Canonical route: `sign create time`.
* Example: `/sign create time`.
* Convenience roots: none.
* Category: `economy.signs`.
* Feature gate: `sef.economy.signs`.
* Permissions: `sef.economy.sign.time.create`.
* Access class: `administrator`.
* Sources: `player`.
* Target behavior: `server`.
* Cooldown policy: `sef:economy.sign.create.time`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:economy`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: false.
* Shared pipeline: required.

### `sef:economy.sign.create.trade`

* Description: Executes the `/sign create trade` action through the shared policy pipeline.
* Usage: `/sign create trade`.
* Canonical route: `sign create trade`.
* Example: `/sign create trade`.
* Convenience roots: none.
* Category: `economy.signs`.
* Feature gate: `sef.economy.signs`.
* Permissions: `sef.economy.sign.trade.create`.
* Access class: `administrator`.
* Sources: `player`.
* Target behavior: `server`.
* Cooldown policy: `sef:economy.sign.create.trade`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:economy`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: false.
* Shared pipeline: required.

### `sef:economy.sign.create.warp`

* Description: Executes the `/sign create warp` action through the shared policy pipeline.
* Usage: `/sign create warp`.
* Canonical route: `sign create warp`.
* Example: `/sign create warp`.
* Convenience roots: none.
* Category: `economy.signs`.
* Feature gate: `sef.economy.signs`.
* Permissions: `sef.economy.sign.warp.create`.
* Access class: `administrator`.
* Sources: `player`.
* Target behavior: `server`.
* Cooldown policy: `sef:economy.sign.create.warp`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:economy`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: false.
* Shared pipeline: required.

### `sef:economy.sign.create.weather`

* Description: Executes the `/sign create weather` action through the shared policy pipeline.
* Usage: `/sign create weather`.
* Canonical route: `sign create weather`.
* Example: `/sign create weather`.
* Convenience roots: none.
* Category: `economy.signs`.
* Feature gate: `sef.economy.signs`.
* Permissions: `sef.economy.sign.weather.create`.
* Access class: `administrator`.
* Sources: `player`.
* Target behavior: `server`.
* Cooldown policy: `sef:economy.sign.create.weather`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:economy`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: false.
* Shared pipeline: required.

### `sef:economy.sign.disposal`

* Description: Executes the `/sign disposal` action through the shared policy pipeline.
* Usage: `/sign disposal`.
* Canonical route: `sign disposal`.
* Example: `/sign disposal`.
* Convenience roots: none.
* Category: `economy.signs`.
* Feature gate: `sef.economy.signs`.
* Permissions: `sef.economy.sign.disposal.use`.
* Access class: `player`.
* Sources: `player`.
* Target behavior: `self`.
* Cooldown policy: `sef:economy.sign.disposal`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:economy`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: false.
* Shared pipeline: required.

### `sef:economy.sign.free`

* Description: Executes the `/sign free` action through the shared policy pipeline.
* Usage: `/sign free`.
* Canonical route: `sign free`.
* Example: `/sign free`.
* Convenience roots: none.
* Category: `economy.signs`.
* Feature gate: `sef.economy.signs`.
* Permissions: `sef.economy.sign.free.use`.
* Access class: `player`.
* Sources: `player`.
* Target behavior: `self`.
* Cooldown policy: `sef:economy.sign.free`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:economy`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: false.
* Shared pipeline: required.

### `sef:economy.sign.heal`

* Description: Executes the `/sign heal` action through the shared policy pipeline.
* Usage: `/sign heal`.
* Canonical route: `sign heal`.
* Example: `/sign heal`.
* Convenience roots: none.
* Category: `economy.signs`.
* Feature gate: `sef.economy.signs`.
* Permissions: `sef.economy.sign.heal.use`.
* Access class: `player`.
* Sources: `player`.
* Target behavior: `self`.
* Cooldown policy: `sef:economy.sign.heal`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:economy`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: false.
* Shared pipeline: required.

### `sef:economy.sign.kit`

* Description: Executes the `/sign kit` action through the shared policy pipeline.
* Usage: `/sign kit`.
* Canonical route: `sign kit`.
* Example: `/sign kit`.
* Convenience roots: none.
* Category: `economy.signs`.
* Feature gate: `sef.economy.signs`.
* Permissions: `sef.economy.sign.kit.use`.
* Access class: `player`.
* Sources: `player`.
* Target behavior: `self`.
* Cooldown policy: `sef:economy.sign.kit`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:economy`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: false.
* Shared pipeline: required.

### `sef:economy.sign.manage`

* Description: Executes the `/eco sign` action through the shared policy pipeline.
* Usage: `/eco sign`.
* Canonical route: `eco sign`.
* Example: `/eco sign`.
* Convenience roots: none.
* Category: `economy.signs`.
* Feature gate: `sef.economy.signs`.
* Permissions: `sef.economy.sign.manage`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `server`.
* Cooldown policy: `sef:economy.sign.manage`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:economy`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:economy.sign.repair`

* Description: Executes the `/sign repair` action through the shared policy pipeline.
* Usage: `/sign repair`.
* Canonical route: `sign repair`.
* Example: `/sign repair`.
* Convenience roots: none.
* Category: `economy.signs`.
* Feature gate: `sef.economy.signs`.
* Permissions: `sef.economy.sign.repair.use`.
* Access class: `player`.
* Sources: `player`.
* Target behavior: `self`.
* Cooldown policy: `sef:economy.sign.repair`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:economy`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: false.
* Shared pipeline: required.

### `sef:economy.sign.sell`

* Description: Executes the `/sign sell` action through the shared policy pipeline.
* Usage: `/sign sell`.
* Canonical route: `sign sell`.
* Example: `/sign sell`.
* Convenience roots: none.
* Category: `economy.signs`.
* Feature gate: `sef.economy.signs`.
* Permissions: `sef.economy.sign.sell.use`.
* Access class: `player`.
* Sources: `player`.
* Target behavior: `self`.
* Cooldown policy: `sef:economy.sign.sell`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:economy`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: false.
* Shared pipeline: required.

### `sef:economy.sign.time`

* Description: Executes the `/sign time` action through the shared policy pipeline.
* Usage: `/sign time`.
* Canonical route: `sign time`.
* Example: `/sign time`.
* Convenience roots: none.
* Category: `economy.signs`.
* Feature gate: `sef.economy.signs`.
* Permissions: `sef.economy.sign.time.use`.
* Access class: `player`.
* Sources: `player`.
* Target behavior: `self`.
* Cooldown policy: `sef:economy.sign.time`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:economy`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: false.
* Shared pipeline: required.

### `sef:economy.sign.trade`

* Description: Executes the `/sign trade` action through the shared policy pipeline.
* Usage: `/sign trade`.
* Canonical route: `sign trade`.
* Example: `/sign trade`.
* Convenience roots: none.
* Category: `economy.signs`.
* Feature gate: `sef.economy.signs`.
* Permissions: `sef.economy.sign.trade.use`.
* Access class: `player`.
* Sources: `player`.
* Target behavior: `self`.
* Cooldown policy: `sef:economy.sign.trade`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:economy`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: false.
* Shared pipeline: required.

### `sef:economy.sign.warp`

* Description: Executes the `/sign warp` action through the shared policy pipeline.
* Usage: `/sign warp`.
* Canonical route: `sign warp`.
* Example: `/sign warp`.
* Convenience roots: none.
* Category: `economy.signs`.
* Feature gate: `sef.economy.signs`.
* Permissions: `sef.economy.sign.warp.use`.
* Access class: `player`.
* Sources: `player`.
* Target behavior: `self`.
* Cooldown policy: `sef:economy.sign.warp`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:economy`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: false.
* Shared pipeline: required.

### `sef:economy.sign.weather`

* Description: Executes the `/sign weather` action through the shared policy pipeline.
* Usage: `/sign weather`.
* Canonical route: `sign weather`.
* Example: `/sign weather`.
* Convenience roots: none.
* Category: `economy.signs`.
* Feature gate: `sef.economy.signs`.
* Permissions: `sef.economy.sign.weather.use`.
* Access class: `player`.
* Sources: `player`.
* Target behavior: `self`.
* Cooldown policy: `sef:economy.sign.weather`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:economy`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: false.
* Shared pipeline: required.

### `sef:economy.top`

* Description: Executes the `/balancetop` action through the shared policy pipeline.
* Usage: `/balancetop`.
* Canonical route: `balancetop`.
* Example: `/balancetop`.
* Convenience roots: `balancetop`, `baltop`.
* Category: `economy`.
* Feature gate: `sef.economy`.
* Permissions: `sef.commands.balancetop`.
* Access class: `player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:economy.top`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:economy`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:economy.worth`

* Description: Executes the `/worth` action through the shared policy pipeline.
* Usage: `/worth`.
* Canonical route: `worth`.
* Example: `/worth`.
* Convenience roots: `worth`.
* Category: `economy`.
* Feature gate: `sef.economy`.
* Permissions: `sef.commands.worth`.
* Access class: `player`.
* Sources: `player`.
* Target behavior: `self`.
* Cooldown policy: `sef:economy.worth`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:economy`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:economy.worth.set`

* Description: Executes the `/setworth` action through the shared policy pipeline.
* Usage: `/setworth`.
* Canonical route: `setworth`.
* Example: `/setworth`.
* Convenience roots: `setworth`.
* Category: `economy`.
* Feature gate: `sef.economy`.
* Permissions: `sef.commands.setworth`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:economy.worth.set`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:economy`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:enchant.apply`

* Description: Executes the `/sef enchant` action through the shared policy pipeline.
* Usage: `/sef enchant`.
* Canonical route: `sef enchant`.
* Example: `/sef enchant`.
* Convenience roots: `enchant`.
* Category: `enchant.admin`.
* Feature gate: `sef.enchant.admin`.
* Permissions: `sef.commands.enchant`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `required_player`.
* Cooldown policy: `sef:enchant.apply`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:workstations`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:enchant.clear`

* Description: Executes the `/sef enchant clear` action through the shared policy pipeline.
* Usage: `/sef enchant clear`.
* Canonical route: `sef enchant clear`.
* Example: `/sef enchant clear`.
* Convenience roots: none.
* Category: `enchant.admin`.
* Feature gate: `sef.enchant.admin`.
* Permissions: `sef.commands.enchant.clear`.
* Access class: `owner`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `required_player`.
* Cooldown policy: `sef:enchant.clear`.
* Confirmation: not required.
* Audit class: `destructive`.
* GUI descriptor: `sef:workstations`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:enchant.remove`

* Description: Executes the `/sef enchant remove` action through the shared policy pipeline.
* Usage: `/sef enchant remove`.
* Canonical route: `sef enchant remove`.
* Example: `/sef enchant remove`.
* Convenience roots: none.
* Category: `enchant.admin`.
* Feature gate: `sef.enchant.admin`.
* Permissions: `sef.commands.enchant.remove`.
* Access class: `owner`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `required_player`.
* Cooldown policy: `sef:enchant.remove`.
* Confirmation: not required.
* Audit class: `destructive`.
* GUI descriptor: `sef:workstations`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:fake.join`

* Description: Executes the `/fakejoin` action through the shared policy pipeline.
* Usage: `/fakejoin`.
* Canonical route: `fakejoin`.
* Example: `/fakejoin`.
* Convenience roots: `fakejoin`.
* Category: `fake`.
* Feature gate: `sef.fake`.
* Permissions: `sef.commands.fakejoin`.
* Access class: `staff`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:fake.join`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:identity`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:fake.leave`

* Description: Executes the `/fakeleave` action through the shared policy pipeline.
* Usage: `/fakeleave`.
* Canonical route: `fakeleave`.
* Example: `/fakeleave`.
* Convenience roots: `fakeleave`.
* Category: `fake`.
* Feature gate: `sef.fake`.
* Permissions: `sef.commands.fakeleave`.
* Access class: `staff`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:fake.leave`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:identity`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:fake.message`

* Description: Executes the `/fakemessage` action through the shared policy pipeline.
* Usage: `/fakemessage`.
* Canonical route: `fakemessage`.
* Example: `/fakemessage`.
* Convenience roots: `fakemessage`.
* Category: `fake`.
* Feature gate: `sef.fake`.
* Permissions: `sef.commands.fakemessage`.
* Access class: `staff`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:fake.message`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:identity`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:fake.profile`

* Description: Executes the `/sef fake profile` action through the shared policy pipeline.
* Usage: `/sef fake profile`.
* Canonical route: `sef fake profile`.
* Example: `/sef fake profile`.
* Convenience roots: none.
* Category: `fake`.
* Feature gate: `sef.fake`.
* Permissions: `sef.commands.fake.profile`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:fake.profile`.
* Confirmation: not required.
* Audit class: `config_definition`.
* GUI descriptor: `sef:identity`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:fake.rank_message`

* Description: Executes the `/fakerankmessage` action through the shared policy pipeline.
* Usage: `/fakerankmessage`.
* Canonical route: `fakerankmessage`.
* Example: `/fakerankmessage`.
* Convenience roots: `fakerankmessage`.
* Category: `fake`.
* Feature gate: `sef.fake`.
* Permissions: `sef.commands.fakerankmessage`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:fake.rank_message`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:identity`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:fake.scene`

* Description: Executes the `/sef fake scene` action through the shared policy pipeline.
* Usage: `/sef fake scene`.
* Canonical route: `sef fake scene`.
* Example: `/sef fake scene`.
* Convenience roots: none.
* Category: `fake`.
* Feature gate: `sef.fake`.
* Permissions: `sef.commands.fake.scene`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:fake.scene`.
* Confirmation: not required.
* Audit class: `config_definition`.
* GUI descriptor: `sef:identity`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:fake.schedule`

* Description: Executes the `/sef fake schedule` action through the shared policy pipeline.
* Usage: `/sef fake schedule`.
* Canonical route: `sef fake schedule`.
* Example: `/sef fake schedule`.
* Convenience roots: none.
* Category: `fake`.
* Feature gate: `sef.fake`.
* Permissions: `sef.commands.fake.schedule`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:fake.schedule`.
* Confirmation: not required.
* Audit class: `config_definition`.
* GUI descriptor: `sef:identity`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:filter.add`

* Description: Executes the `/sef filter add` action through the shared policy pipeline.
* Usage: `/sef filter add`.
* Canonical route: `sef filter add`.
* Example: `/sef filter add`.
* Convenience roots: none.
* Category: `sef`.
* Feature gate: `sef.filter`.
* Permissions: `sef.commands.sef.allowed`, `sef.filter.manage`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:filter.add`.
* Confirmation: not required.
* Audit class: `config_definition`.
* GUI descriptor: `sef:core`.
* HUD contract: filter changes are immediate.
* Quota contract: command does not create retained records or variable fan out.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:filter.list`

* Description: Executes the `/sef filter list` action through the shared policy pipeline.
* Usage: `/sef filter list`.
* Canonical route: `sef filter list`.
* Example: `/sef filter list`.
* Convenience roots: none.
* Category: `sef`.
* Feature gate: `sef.filter`.
* Permissions: `sef.commands.sef.allowed`, `sef.filter.manage`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:filter.list`.
* Confirmation: not required.
* Audit class: `sensitive_access`.
* GUI descriptor: `sef:core`.
* HUD contract: filter diagnostics have no persistent hud.
* Quota contract: command does not create retained records or variable fan out.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:filter.remove`

* Description: Executes the `/sef filter remove` action through the shared policy pipeline.
* Usage: `/sef filter remove`.
* Canonical route: `sef filter remove`.
* Example: `/sef filter remove`.
* Convenience roots: none.
* Category: `sef`.
* Feature gate: `sef.filter`.
* Permissions: `sef.commands.sef.allowed`, `sef.filter.manage`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:filter.remove`.
* Confirmation: not required.
* Audit class: `config_definition`.
* GUI descriptor: `sef:core`.
* HUD contract: filter changes are immediate.
* Quota contract: command does not create retained records or variable fan out.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:gamemode.adventure`

* Description: Executes the `/gma` action through the shared policy pipeline.
* Usage: `/gma`.
* Canonical route: `gma`.
* Example: `/gma`.
* Convenience roots: `gma`.
* Category: `gamemode`.
* Feature gate: `sef.gamemode`.
* Permissions: `sef.commands.gamemode.adventure`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:gamemode.adventure`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:utilities`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:gamemode.creative`

* Description: Executes the `/gmc` action through the shared policy pipeline.
* Usage: `/gmc`.
* Canonical route: `gmc`.
* Example: `/gmc`.
* Convenience roots: `gmc`.
* Category: `gamemode`.
* Feature gate: `sef.gamemode`.
* Permissions: `sef.commands.gamemode.creative`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:gamemode.creative`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:utilities`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:gamemode.set`

* Description: Executes the `/gamemode` action through the shared policy pipeline.
* Usage: `/gamemode`.
* Canonical route: `gamemode`.
* Example: `/gamemode`.
* Convenience roots: `gm`.
* Category: `gamemode`.
* Feature gate: `sef.gamemode`.
* Permissions: `sef.commands.gamemode`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:gamemode.set`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:utilities`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:gamemode.spectator`

* Description: Executes the `/gmsp` action through the shared policy pipeline.
* Usage: `/gmsp`.
* Canonical route: `gmsp`.
* Example: `/gmsp`.
* Convenience roots: `gmsp`.
* Category: `gamemode`.
* Feature gate: `sef.gamemode`.
* Permissions: `sef.commands.gamemode.spectator`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:gamemode.spectator`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:utilities`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:gamemode.survival`

* Description: Executes the `/gms` action through the shared policy pipeline.
* Usage: `/gms`.
* Canonical route: `gms`.
* Example: `/gms`.
* Convenience roots: `gms`.
* Category: `gamemode`.
* Feature gate: `sef.gamemode`.
* Permissions: `sef.commands.gamemode.survival`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:gamemode.survival`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:utilities`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:gui.client.status`

* Description: Executes the `/sef client status` action through the shared policy pipeline.
* Usage: `/sef client status`.
* Canonical route: `sef client status`.
* Example: `/sef client status`.
* Convenience roots: none.
* Category: `sef`.
* Feature gate: `sef.core`.
* Permissions: `sef.commands.sef.allowed`, `sef.commands.sef.info`.
* Access class: `player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:gui.client.status`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:gui`.
* HUD contract: client status is immediate.
* Quota contract: command does not create retained records or variable fan out.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:gui.dashboard.open`

* Description: Executes the `/sef dashboard` action through the shared policy pipeline.
* Usage: `/sef dashboard`.
* Canonical route: `sef dashboard`.
* Example: `/sef dashboard`.
* Convenience roots: none.
* Category: `sef`.
* Feature gate: `sef.gui`.
* Permissions: `sef.commands.sef.allowed`, `sef.kernel.gui.use`.
* Access class: `player`.
* Sources: `player`.
* Target behavior: `none`.
* Cooldown policy: `sef:gui.dashboard.open`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:gui`.
* HUD contract: dashboard state is sent through the client protocol.
* Quota contract: command does not create retained records or variable fan out.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:gui.preference`

* Description: Executes the `/sef gui` action through the shared policy pipeline.
* Usage: `/sef gui`.
* Canonical route: `sef gui`.
* Example: `/sef gui`.
* Convenience roots: none.
* Category: `gui.policy`.
* Feature gate: `sef.gui.policy`.
* Permissions: `sef.commands.gui.preference`.
* Access class: `player`.
* Sources: `player`.
* Target behavior: `self`.
* Cooldown policy: `sef:gui.preference`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:settings`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:gui.preference.blur`

* Description: Executes the `/sef client preference blur` action through the shared policy pipeline.
* Usage: `/sef client preference blur`.
* Canonical route: `sef client preference blur`.
* Example: `/sef client preference blur`.
* Convenience roots: none.
* Category: `core`.
* Feature gate: `sef.core`.
* Permissions: `sef.commands.sef.client.preferences`.
* Access class: `player`.
* Sources: `player`.
* Target behavior: `self`.
* Cooldown policy: `sef:gui.preference.blur`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:settings`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:gui.preference.hud`

* Description: Executes the `/sef client preference hud` action through the shared policy pipeline.
* Usage: `/sef client preference hud`.
* Canonical route: `sef client preference hud`.
* Example: `/sef client preference hud`.
* Convenience roots: none.
* Category: `core`.
* Feature gate: `sef.core`.
* Permissions: `sef.commands.sef.client.preferences`.
* Access class: `player`.
* Sources: `player`.
* Target behavior: `self`.
* Cooldown policy: `sef:gui.preference.hud`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:settings`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:gui.preference.mode`

* Description: Executes the `/sef client preference mode` action through the shared policy pipeline.
* Usage: `/sef client preference mode`.
* Canonical route: `sef client preference mode`.
* Example: `/sef client preference mode`.
* Convenience roots: none.
* Category: `core`.
* Feature gate: `sef.core`.
* Permissions: `sef.commands.sef.client.preferences`.
* Access class: `player`.
* Sources: `player`.
* Target behavior: `self`.
* Cooldown policy: `sef:gui.preference.mode`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:settings`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:gui.preference.motion`

* Description: Executes the `/sef client preference motion` action through the shared policy pipeline.
* Usage: `/sef client preference motion`.
* Canonical route: `sef client preference motion`.
* Example: `/sef client preference motion`.
* Convenience roots: none.
* Category: `core`.
* Feature gate: `sef.core`.
* Permissions: `sef.commands.sef.client.preferences`.
* Access class: `player`.
* Sources: `player`.
* Target behavior: `self`.
* Cooldown policy: `sef:gui.preference.motion`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:settings`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:gui.preference.page_size`

* Description: Executes the `/sef client preference page_size` action through the shared policy pipeline.
* Usage: `/sef client preference page_size`.
* Canonical route: `sef client preference page_size`.
* Example: `/sef client preference page_size`.
* Convenience roots: none.
* Category: `core`.
* Feature gate: `sef.core`.
* Permissions: `sef.commands.sef.client.preferences`.
* Access class: `player`.
* Sources: `player`.
* Target behavior: `self`.
* Cooldown policy: `sef:gui.preference.page_size`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:settings`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:gui.preference.pause`

* Description: Executes the `/sef client preference pause` action through the shared policy pipeline.
* Usage: `/sef client preference pause`.
* Canonical route: `sef client preference pause`.
* Example: `/sef client preference pause`.
* Convenience roots: none.
* Category: `core`.
* Feature gate: `sef.core`.
* Permissions: `sef.commands.sef.client.preferences`.
* Access class: `player`.
* Sources: `player`.
* Target behavior: `self`.
* Cooldown policy: `sef:gui.preference.pause`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:settings`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:gui.reminder.dismiss`

* Description: Executes the `/sef client reminder dismiss` action through the shared policy pipeline.
* Usage: `/sef client reminder dismiss`.
* Canonical route: `sef client reminder dismiss`.
* Example: `/sef client reminder dismiss`.
* Convenience roots: none.
* Category: `sef`.
* Feature gate: `sef.core`.
* Permissions: `sef.commands.sef.allowed`, `sef.commands.sef.info`.
* Access class: `player`.
* Sources: `player`.
* Target behavior: `none`.
* Cooldown policy: `sef:gui.reminder.dismiss`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:gui`.
* HUD contract: reminder preference is persisted.
* Quota contract: command does not create retained records or variable fan out.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:guis.action`

* Description: Executes the `/sef guis action` action through the shared policy pipeline.
* Usage: `/sef guis action`.
* Canonical route: `sef guis action`.
* Example: `/sef guis action`.
* Convenience roots: none.
* Category: `gui.policy`.
* Feature gate: `sef.gui.policy`.
* Permissions: `sef.commands.guis.action`.
* Access class: `owner`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:guis.action`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:settings`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:guis.auto`

* Description: Executes the `/sef guis auto` action through the shared policy pipeline.
* Usage: `/sef guis auto`.
* Canonical route: `sef guis auto`.
* Example: `/sef guis auto`.
* Convenience roots: none.
* Category: `gui.policy`.
* Feature gate: `sef.gui.policy`.
* Permissions: `sef.commands.guis.auto`.
* Access class: `owner`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:guis.auto`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:settings`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:guis.close`

* Description: Executes the `/sef guis close` action through the shared policy pipeline.
* Usage: `/sef guis close`.
* Canonical route: `sef guis close`.
* Example: `/sef guis close`.
* Convenience roots: none.
* Category: `gui.policy`.
* Feature gate: `sef.gui.policy`.
* Permissions: `sef.commands.guis.close`.
* Access class: `owner`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:guis.close`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:settings`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:guis.coverage`

* Description: Executes the `/sef guis coverage` action through the shared policy pipeline.
* Usage: `/sef guis coverage`.
* Canonical route: `sef guis coverage`.
* Example: `/sef guis coverage`.
* Convenience roots: none.
* Category: `gui.policy`.
* Feature gate: `sef.gui.policy`.
* Permissions: `sef.commands.guis.coverage`.
* Access class: `staff`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:guis.coverage`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:settings`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:guis.disable`

* Description: Executes the `/sef guis off` action through the shared policy pipeline.
* Usage: `/sef guis off`.
* Canonical route: `sef guis off`.
* Example: `/sef guis off`.
* Convenience roots: none.
* Category: `gui.policy`.
* Feature gate: `sef.gui.policy`.
* Permissions: `sef.commands.guis.disable`.
* Access class: `owner`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:guis.disable`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:settings`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:guis.doctor`

* Description: Executes the `/sef guis doctor` action through the shared policy pipeline.
* Usage: `/sef guis doctor`.
* Canonical route: `sef guis doctor`.
* Example: `/sef guis doctor`.
* Convenience roots: none.
* Category: `gui.policy`.
* Feature gate: `sef.gui.policy`.
* Permissions: `sef.commands.guis.doctor`.
* Access class: `owner`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:guis.doctor`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:settings`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:guis.enable`

* Description: Executes the `/sef guis on` action through the shared policy pipeline.
* Usage: `/sef guis on`.
* Canonical route: `sef guis on`.
* Example: `/sef guis on`.
* Convenience roots: none.
* Category: `gui.policy`.
* Feature gate: `sef.gui.policy`.
* Permissions: `sef.commands.guis.enable`.
* Access class: `owner`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:guis.enable`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:settings`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:guis.explain`

* Description: Executes the `/sef guis explain` action through the shared policy pipeline.
* Usage: `/sef guis explain`.
* Canonical route: `sef guis explain`.
* Example: `/sef guis explain`.
* Convenience roots: none.
* Category: `gui.policy`.
* Feature gate: `sef.gui.policy`.
* Permissions: `sef.commands.guis.explain`.
* Access class: `owner`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:guis.explain`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:settings`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:guis.module`

* Description: Executes the `/sef guis module` action through the shared policy pipeline.
* Usage: `/sef guis module`.
* Canonical route: `sef guis module`.
* Example: `/sef guis module`.
* Convenience roots: none.
* Category: `gui.policy`.
* Feature gate: `sef.gui.policy`.
* Permissions: `sef.commands.guis.module`.
* Access class: `owner`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:guis.module`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:settings`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:guis.reload`

* Description: Executes the `/sef guis reload` action through the shared policy pipeline.
* Usage: `/sef guis reload`.
* Canonical route: `sef guis reload`.
* Example: `/sef guis reload`.
* Convenience roots: none.
* Category: `gui.policy`.
* Feature gate: `sef.gui.policy`.
* Permissions: `sef.commands.guis.reload`.
* Access class: `owner`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:guis.reload`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:settings`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:guis.sessions`

* Description: Executes the `/sef guis sessions` action through the shared policy pipeline.
* Usage: `/sef guis sessions`.
* Canonical route: `sef guis sessions`.
* Example: `/sef guis sessions`.
* Convenience roots: none.
* Category: `gui.policy`.
* Feature gate: `sef.gui.policy`.
* Permissions: `sef.commands.guis.sessions`.
* Access class: `owner`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:guis.sessions`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:settings`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:guis.status`

* Description: Executes the `/sef guis status` action through the shared policy pipeline.
* Usage: `/sef guis status`.
* Canonical route: `sef guis status`.
* Example: `/sef guis status`.
* Convenience roots: none.
* Category: `gui.policy`.
* Feature gate: `sef.gui.policy`.
* Permissions: `sef.commands.guis.status`.
* Access class: `staff`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:guis.status`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:settings`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:identity.alts`

* Description: Executes the `/checkalts` action through the shared policy pipeline.
* Usage: `/checkalts`.
* Canonical route: `checkalts`.
* Example: `/checkalts`.
* Convenience roots: `checkalts`.
* Category: `core`.
* Feature gate: `sef.core`.
* Permissions: `sef.commands.checkalts`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:identity.alts`.
* Confirmation: not required.
* Audit class: `sensitive_access`.
* GUI descriptor: `sef:identity`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:identity.nick`

* Description: Executes the `/nick` action through the shared policy pipeline.
* Usage: `/nick`.
* Canonical route: `nick`.
* Example: `/nick`.
* Convenience roots: `nick`.
* Category: `core`.
* Feature gate: `sef.core`.
* Permissions: `sef.commands.nick`.
* Access class: `player`.
* Sources: `player`.
* Target behavior: `self`.
* Cooldown policy: `sef:identity.nick`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:identity`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:identity.nick.others`

* Description: Executes the `/nickfor` action through the shared policy pipeline.
* Usage: `/nickfor`.
* Canonical route: `nickfor`.
* Example: `/nickfor`.
* Convenience roots: `nickfor`.
* Category: `core`.
* Feature gate: `sef.core`.
* Permissions: `sef.commands.nick.others`.
* Access class: `staff`.
* Sources: `player`.
* Target behavior: `required_player`.
* Cooldown policy: `sef:identity.nick.others`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:identity`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:identity.whois`

* Description: Executes the `/whois` action through the shared policy pipeline.
* Usage: `/whois`.
* Canonical route: `whois`.
* Example: `/whois`.
* Convenience roots: `whois`.
* Category: `core`.
* Feature gate: `sef.core`.
* Permissions: `sef.commands.whois`.
* Access class: `player`.
* Sources: `player`.
* Target behavior: `required_player`.
* Cooldown policy: `sef:identity.whois`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:identity`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:inventory.book`

* Description: Executes the `/book` action through the shared policy pipeline.
* Usage: `/book`.
* Canonical route: `book`.
* Example: `/book`.
* Convenience roots: `book`.
* Category: `inventory`.
* Feature gate: `sef.inventory`.
* Permissions: `sef.commands.book`.
* Access class: `player`.
* Sources: `player`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:inventory.book`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:inventory`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:inventory.clear`

* Description: Executes the `/clear` action through the shared policy pipeline.
* Usage: `/clear`.
* Canonical route: `clear`.
* Example: `/clear`.
* Convenience roots: `ci`, `clearinventory`.
* Category: `inventory`.
* Feature gate: `sef.inventory`.
* Permissions: `sef.commands.clearinventory`.
* Access class: `player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:inventory.clear`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:inventory`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:inventory.condense`

* Description: Executes the `/condense` action through the shared policy pipeline.
* Usage: `/condense`.
* Canonical route: `condense`.
* Example: `/condense`.
* Convenience roots: `condense`.
* Category: `inventory`.
* Feature gate: `sef.inventory`.
* Permissions: `sef.commands.condense`.
* Access class: `player`.
* Sources: `player`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:inventory.condense`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:inventory`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:inventory.disposal`

* Description: Executes the `/disposal` action through the shared policy pipeline.
* Usage: `/disposal`.
* Canonical route: `disposal`.
* Example: `/disposal`.
* Convenience roots: `disposal`.
* Category: `inventory`.
* Feature gate: `sef.inventory`.
* Permissions: `sef.commands.disposal`.
* Access class: `player`.
* Sources: `player`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:inventory.disposal`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:inventory`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:inventory.enderchest`

* Description: Executes the `/enderchest` action through the shared policy pipeline.
* Usage: `/enderchest`.
* Canonical route: `enderchest`.
* Example: `/enderchest`.
* Convenience roots: `ec`, `enderchest`.
* Category: `inventory`.
* Feature gate: `sef.inventory`.
* Permissions: `sef.commands.enderchest`.
* Access class: `player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:inventory.enderchest`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:inventory`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:inventory.hat`

* Description: Executes the `/hat` action through the shared policy pipeline.
* Usage: `/hat`.
* Canonical route: `hat`.
* Example: `/hat`.
* Convenience roots: `hat`.
* Category: `inventory`.
* Feature gate: `sef.inventory`.
* Permissions: `sef.commands.hat`.
* Access class: `player`.
* Sources: `player`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:inventory.hat`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:inventory`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:inventory.itemdb`

* Description: Executes the `/itemdb` action through the shared policy pipeline.
* Usage: `/itemdb`.
* Canonical route: `itemdb`.
* Example: `/itemdb`.
* Convenience roots: `itemdb`.
* Category: `inventory`.
* Feature gate: `sef.inventory`.
* Permissions: `sef.commands.itemdb`.
* Access class: `player`.
* Sources: `player`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:inventory.itemdb`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:inventory`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:inventory.itemlore`

* Description: Executes the `/itemlore` action through the shared policy pipeline.
* Usage: `/itemlore`.
* Canonical route: `itemlore`.
* Example: `/itemlore`.
* Convenience roots: `itemlore`.
* Category: `inventory`.
* Feature gate: `sef.inventory`.
* Permissions: `sef.commands.itemlore`.
* Access class: `player`.
* Sources: `player`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:inventory.itemlore`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:inventory`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:inventory.itemname`

* Description: Executes the `/itemname` action through the shared policy pipeline.
* Usage: `/itemname`.
* Canonical route: `itemname`.
* Example: `/itemname`.
* Convenience roots: `itemname`.
* Category: `inventory`.
* Feature gate: `sef.inventory`.
* Permissions: `sef.commands.itemname`.
* Access class: `player`.
* Sources: `player`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:inventory.itemname`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:inventory`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:inventory.more`

* Description: Executes the `/more` action through the shared policy pipeline.
* Usage: `/more`.
* Canonical route: `more`.
* Example: `/more`.
* Convenience roots: `more`.
* Category: `inventory`.
* Feature gate: `sef.inventory`.
* Permissions: `sef.commands.more`.
* Access class: `player`.
* Sources: `player`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:inventory.more`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:inventory`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:inventory.recipe`

* Description: Executes the `/recipe` action through the shared policy pipeline.
* Usage: `/recipe`.
* Canonical route: `recipe`.
* Example: `/recipe`.
* Convenience roots: `recipe`.
* Category: `inventory`.
* Feature gate: `sef.inventory`.
* Permissions: `sef.commands.recipe`.
* Access class: `player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:inventory.recipe`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:inventory`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:inventory.view`

* Description: Executes the `/invsee` action through the shared policy pipeline.
* Usage: `/invsee`.
* Canonical route: `invsee`.
* Example: `/invsee`.
* Convenience roots: `invsee`.
* Category: `inventory`.
* Feature gate: `sef.inventory`.
* Permissions: `sef.commands.invsee.view`.
* Access class: `administrator`.
* Sources: `player`.
* Target behavior: `required_player`.
* Cooldown policy: `sef:inventory.view`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:inventory`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:item.give.others`

* Description: Executes the `/give` action through the shared policy pipeline.
* Usage: `/give`.
* Canonical route: `give`.
* Example: `/give`.
* Convenience roots: `give`.
* Category: `item.give`.
* Feature gate: `sef.item.give`.
* Permissions: `sef.commands.item.give.others`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `required_player`.
* Cooldown policy: `sef:item.give.others`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:inventory`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:item.give.self`

* Description: Executes the `/i` action through the shared policy pipeline.
* Usage: `/i`.
* Canonical route: `i`.
* Example: `/i`.
* Convenience roots: `i`.
* Category: `item.self`.
* Feature gate: `sef.item.self`.
* Permissions: `sef.commands.item.give.self`.
* Access class: `player`.
* Sources: `player`.
* Target behavior: `self`.
* Cooldown policy: `sef:item.give.self`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:inventory`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:kit.claim`

* Description: Executes the `/kit` action through the shared policy pipeline.
* Usage: `/kit`.
* Canonical route: `kit`.
* Example: `/kit`.
* Convenience roots: `kit`.
* Category: `kits`.
* Feature gate: `sef.kits`.
* Permissions: `sef.commands.kit`.
* Access class: `player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:kit.claim`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:kits`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:kit.create`

* Description: Executes the `/createkit` action through the shared policy pipeline.
* Usage: `/createkit`.
* Canonical route: `createkit`.
* Example: `/createkit`.
* Convenience roots: `createkit`.
* Category: `kits`.
* Feature gate: `sef.kits`.
* Permissions: `sef.commands.createkit`.
* Access class: `player`.
* Sources: `player`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:kit.create`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:kits`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:kit.delete`

* Description: Executes the `/delkit` action through the shared policy pipeline.
* Usage: `/delkit`.
* Canonical route: `delkit`.
* Example: `/delkit`.
* Convenience roots: `delkit`.
* Category: `kits`.
* Feature gate: `sef.kits`.
* Permissions: `sef.commands.delkit`.
* Access class: `player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:kit.delete`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:kits`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:kit.edit`

* Description: Executes the `/kit edit` action through the shared policy pipeline.
* Usage: `/kit edit`.
* Canonical route: `kit edit`.
* Example: `/kit edit`.
* Convenience roots: none.
* Category: `kits`.
* Feature gate: `sef.kits`.
* Permissions: `sef.commands.kit.edit`.
* Access class: `player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:kit.edit`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:kits`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:kit.export`

* Description: Executes the `/kit export` action through the shared policy pipeline.
* Usage: `/kit export`.
* Canonical route: `kit export`.
* Example: `/kit export`.
* Convenience roots: none.
* Category: `kits`.
* Feature gate: `sef.kits`.
* Permissions: `sef.commands.kit.export`.
* Access class: `player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:kit.export`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:kits`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:kit.list`

* Description: Executes the `/kits` action through the shared policy pipeline.
* Usage: `/kits`.
* Canonical route: `kits`.
* Example: `/kits`.
* Convenience roots: `kits`.
* Category: `kits`.
* Feature gate: `sef.kits`.
* Permissions: `sef.commands.kits`.
* Access class: `player`.
* Sources: `player`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:kit.list`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:kits`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:kit.reset`

* Description: Executes the `/kitreset` action through the shared policy pipeline.
* Usage: `/kitreset`.
* Canonical route: `kitreset`.
* Example: `/kitreset`.
* Convenience roots: `kitreset`.
* Category: `kits`.
* Feature gate: `sef.kits`.
* Permissions: `sef.commands.kitreset`.
* Access class: `player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:kit.reset`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:kits`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:kit.show`

* Description: Executes the `/showkit` action through the shared policy pipeline.
* Usage: `/showkit`.
* Canonical route: `showkit`.
* Example: `/showkit`.
* Convenience roots: `showkit`.
* Category: `kits`.
* Feature gate: `sef.kits`.
* Permissions: `sef.commands.showkit`.
* Access class: `player`.
* Sources: `player`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:kit.show`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:kits`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:kit.validate`

* Description: Executes the `/kit validate` action through the shared policy pipeline.
* Usage: `/kit validate`.
* Canonical route: `kit validate`.
* Example: `/kit validate`.
* Convenience roots: none.
* Category: `kits`.
* Feature gate: `sef.kits`.
* Permissions: `sef.commands.kit.validate`.
* Access class: `player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:kit.validate`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:kits`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:logging.disable`

* Description: Executes the `/sef logging disable` action through the shared policy pipeline.
* Usage: `/sef logging disable`.
* Canonical route: `sef logging disable`.
* Example: `/sef logging disable`.
* Convenience roots: none.
* Category: `logging`.
* Feature gate: `sef.logging`.
* Permissions: `sef.commands.logging.disable`.
* Access class: `owner`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `server`.
* Cooldown policy: `sef:logging.disable`.
* Confirmation: not required.
* Audit class: `file_log_control`.
* GUI descriptor: `sef:observation`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:logging.doctor`

* Description: Executes the `/sef logging doctor` action through the shared policy pipeline.
* Usage: `/sef logging doctor`.
* Canonical route: `sef logging doctor`.
* Example: `/sef logging doctor`.
* Convenience roots: none.
* Category: `logging`.
* Feature gate: `sef.logging`.
* Permissions: `sef.commands.logging.doctor`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `server`.
* Cooldown policy: `sef:logging.doctor`.
* Confirmation: not required.
* Audit class: `sensitive_access`.
* GUI descriptor: `sef:observation`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:logging.enable`

* Description: Executes the `/sef logging enable` action through the shared policy pipeline.
* Usage: `/sef logging enable`.
* Canonical route: `sef logging enable`.
* Example: `/sef logging enable`.
* Convenience roots: none.
* Category: `logging`.
* Feature gate: `sef.logging`.
* Permissions: `sef.commands.logging.enable`.
* Access class: `owner`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `server`.
* Cooldown policy: `sef:logging.enable`.
* Confirmation: not required.
* Audit class: `file_log_control`.
* GUI descriptor: `sef:observation`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:logging.export`

* Description: Executes the `/sef logging export` action through the shared policy pipeline.
* Usage: `/sef logging export`.
* Canonical route: `sef logging export`.
* Example: `/sef logging export`.
* Convenience roots: none.
* Category: `logging`.
* Feature gate: `sef.logging`.
* Permissions: `sef.commands.logging.export`.
* Access class: `owner`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `server`.
* Cooldown policy: `sef:logging.export`.
* Confirmation: not required.
* Audit class: `file_log_control`.
* GUI descriptor: `sef:observation`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:logging.filter.action`

* Description: Executes the `/sef logging filter action` action through the shared policy pipeline.
* Usage: `/sef logging filter action`.
* Canonical route: `sef logging filter action`.
* Example: `/sef logging filter action`.
* Convenience roots: none.
* Category: `logging`.
* Feature gate: `sef.logging`.
* Permissions: `sef.commands.logging.filter.action`.
* Access class: `owner`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `server`.
* Cooldown policy: `sef:logging.filter.action`.
* Confirmation: not required.
* Audit class: `file_log_control`.
* GUI descriptor: `sef:observation`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:logging.filter.capture`

* Description: Executes the `/sef logging filter mode capture` action through the shared policy pipeline.
* Usage: `/sef logging filter mode capture`.
* Canonical route: `sef logging filter mode capture`.
* Example: `/sef logging filter mode capture`.
* Convenience roots: none.
* Category: `logging`.
* Feature gate: `sef.logging`.
* Permissions: `sef.commands.logging.filter.capture`.
* Access class: `owner`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `server`.
* Cooldown policy: `sef:logging.filter.capture`.
* Confirmation: not required.
* Audit class: `file_log_control`.
* GUI descriptor: `sef:observation`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:logging.filter.list`

* Description: Executes the `/sef logging filter list` action through the shared policy pipeline.
* Usage: `/sef logging filter list`.
* Canonical route: `sef logging filter list`.
* Example: `/sef logging filter list`.
* Convenience roots: none.
* Category: `logging`.
* Feature gate: `sef.logging`.
* Permissions: `sef.commands.logging.filter.list`.
* Access class: `owner`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `server`.
* Cooldown policy: `sef:logging.filter.list`.
* Confirmation: not required.
* Audit class: `file_log_control`.
* GUI descriptor: `sef:observation`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:logging.filter.root`

* Description: Executes the `/sef logging filter root` action through the shared policy pipeline.
* Usage: `/sef logging filter root`.
* Canonical route: `sef logging filter root`.
* Example: `/sef logging filter root`.
* Convenience roots: none.
* Category: `logging`.
* Feature gate: `sef.logging`.
* Permissions: `sef.commands.logging.filter.root`.
* Access class: `owner`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `server`.
* Cooldown policy: `sef:logging.filter.root`.
* Confirmation: not required.
* Audit class: `file_log_control`.
* GUI descriptor: `sef:observation`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:logging.filter.view`

* Description: Executes the `/sef logging filter mode view` action through the shared policy pipeline.
* Usage: `/sef logging filter mode view`.
* Canonical route: `sef logging filter mode view`.
* Example: `/sef logging filter mode view`.
* Convenience roots: none.
* Category: `logging`.
* Feature gate: `sef.logging`.
* Permissions: `sef.commands.logging.filter.view`.
* Access class: `administrator`.
* Sources: `player`.
* Target behavior: `self`.
* Cooldown policy: `sef:logging.filter.view`.
* Confirmation: not required.
* Audit class: `command_observation`.
* GUI descriptor: `sef:observation`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:logging.flush`

* Description: Executes the `/sef logging flush` action through the shared policy pipeline.
* Usage: `/sef logging flush`.
* Canonical route: `sef logging flush`.
* Example: `/sef logging flush`.
* Convenience roots: none.
* Category: `logging`.
* Feature gate: `sef.logging`.
* Permissions: `sef.commands.logging.flush`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `server`.
* Cooldown policy: `sef:logging.flush`.
* Confirmation: not required.
* Audit class: `file_log_control`.
* GUI descriptor: `sef:observation`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:logging.format.reset`

* Description: Executes the `/sef logging format reset` action through the shared policy pipeline.
* Usage: `/sef logging format reset`.
* Canonical route: `sef logging format reset`.
* Example: `/sef logging format reset`.
* Convenience roots: none.
* Category: `logging`.
* Feature gate: `sef.logging`.
* Permissions: `sef.commands.logging.format.reset`.
* Access class: `owner`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `server`.
* Cooldown policy: `sef:logging.format.reset`.
* Confirmation: not required.
* Audit class: `file_log_control`.
* GUI descriptor: `sef:observation`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:logging.format.set`

* Description: Executes the `/sef logging format set` action through the shared policy pipeline.
* Usage: `/sef logging format set`.
* Canonical route: `sef logging format set`.
* Example: `/sef logging format set`.
* Convenience roots: none.
* Category: `logging`.
* Feature gate: `sef.logging`.
* Permissions: `sef.commands.logging.format.set`.
* Access class: `owner`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `server`.
* Cooldown policy: `sef:logging.format.set`.
* Confirmation: not required.
* Audit class: `file_log_control`.
* GUI descriptor: `sef:observation`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:logging.format.show`

* Description: Executes the `/sef logging format show` action through the shared policy pipeline.
* Usage: `/sef logging format show`.
* Canonical route: `sef logging format show`.
* Example: `/sef logging format show`.
* Convenience roots: none.
* Category: `logging`.
* Feature gate: `sef.logging`.
* Permissions: `sef.commands.logging.format.show`.
* Access class: `owner`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `server`.
* Cooldown policy: `sef:logging.format.show`.
* Confirmation: not required.
* Audit class: `file_log_control`.
* GUI descriptor: `sef:observation`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:logging.format.validate`

* Description: Executes the `/sef logging format validate` action through the shared policy pipeline.
* Usage: `/sef logging format validate`.
* Canonical route: `sef logging format validate`.
* Example: `/sef logging format validate`.
* Convenience roots: none.
* Category: `logging`.
* Feature gate: `sef.logging`.
* Permissions: `sef.commands.logging.format.validate`.
* Access class: `owner`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `server`.
* Cooldown policy: `sef:logging.format.validate`.
* Confirmation: not required.
* Audit class: `file_log_control`.
* GUI descriptor: `sef:observation`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:logging.live`

* Description: Executes the `/sef logging live` action through the shared policy pipeline.
* Usage: `/sef logging live`.
* Canonical route: `sef logging live`.
* Example: `/sef logging live`.
* Convenience roots: none.
* Category: `logging`.
* Feature gate: `sef.logging`.
* Permissions: `sef.commands.logging.live`.
* Access class: `administrator`.
* Sources: `player`.
* Target behavior: `self`.
* Cooldown policy: `sef:logging.live`.
* Confirmation: not required.
* Audit class: `command_observation`.
* GUI descriptor: `sef:observation`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:logging.recent`

* Description: Executes the `/sef logging recent` action through the shared policy pipeline.
* Usage: `/sef logging recent`.
* Canonical route: `sef logging recent`.
* Example: `/sef logging recent`.
* Convenience roots: none.
* Category: `logging`.
* Feature gate: `sef.logging`.
* Permissions: `sef.commands.logging.recent`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `server`.
* Cooldown policy: `sef:logging.recent`.
* Confirmation: not required.
* Audit class: `sensitive_access`.
* GUI descriptor: `sef:observation`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:logging.reload`

* Description: Executes the `/sef logging reload` action through the shared policy pipeline.
* Usage: `/sef logging reload`.
* Canonical route: `sef logging reload`.
* Example: `/sef logging reload`.
* Convenience roots: none.
* Category: `logging`.
* Feature gate: `sef.logging`.
* Permissions: `sef.commands.logging.enable`.
* Access class: `owner`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `server`.
* Cooldown policy: `sef:logging.reload`.
* Confirmation: not required.
* Audit class: `file_log_control`.
* GUI descriptor: `sef:observation`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:logging.repair`

* Description: Executes the `/sef logging repair` action through the shared policy pipeline.
* Usage: `/sef logging repair`.
* Canonical route: `sef logging repair`.
* Example: `/sef logging repair`.
* Convenience roots: none.
* Category: `logging`.
* Feature gate: `sef.logging`.
* Permissions: `sef.commands.logging.repair`.
* Access class: `owner`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `server`.
* Cooldown policy: `sef:logging.repair`.
* Confirmation: not required.
* Audit class: `file_log_control`.
* GUI descriptor: `sef:observation`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:logging.retention.preview`

* Description: Executes the `/sef logging retention preview` action through the shared policy pipeline.
* Usage: `/sef logging retention preview`.
* Canonical route: `sef logging retention preview`.
* Example: `/sef logging retention preview`.
* Convenience roots: none.
* Category: `logging`.
* Feature gate: `sef.logging`.
* Permissions: `sef.commands.logging.retention.preview`.
* Access class: `owner`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `server`.
* Cooldown policy: `sef:logging.retention.preview`.
* Confirmation: not required.
* Audit class: `file_log_control`.
* GUI descriptor: `sef:observation`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:logging.retention.run`

* Description: Executes the `/sef logging retention run` action through the shared policy pipeline.
* Usage: `/sef logging retention run`.
* Canonical route: `sef logging retention run`.
* Example: `/sef logging retention run`.
* Convenience roots: none.
* Category: `logging`.
* Feature gate: `sef.logging`.
* Permissions: `sef.commands.logging.retention.run`.
* Access class: `owner`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `server`.
* Cooldown policy: `sef:logging.retention.run`.
* Confirmation: not required.
* Audit class: `file_log_control`.
* GUI descriptor: `sef:observation`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:logging.rotate`

* Description: Executes the `/sef logging rotate` action through the shared policy pipeline.
* Usage: `/sef logging rotate`.
* Canonical route: `sef logging rotate`.
* Example: `/sef logging rotate`.
* Convenience roots: none.
* Category: `logging`.
* Feature gate: `sef.logging`.
* Permissions: `sef.commands.logging.rotate`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `server`.
* Cooldown policy: `sef:logging.rotate`.
* Confirmation: not required.
* Audit class: `file_log_control`.
* GUI descriptor: `sef:observation`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:logging.search`

* Description: Executes the `/sef logging search` action through the shared policy pipeline.
* Usage: `/sef logging search`.
* Canonical route: `sef logging search`.
* Example: `/sef logging search`.
* Convenience roots: none.
* Category: `logging`.
* Feature gate: `sef.logging`.
* Permissions: `sef.commands.logging.search`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `server`.
* Cooldown policy: `sef:logging.search`.
* Confirmation: not required.
* Audit class: `sensitive_access`.
* GUI descriptor: `sef:observation`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:logging.session.current`

* Description: Executes the `/sef logging session current` action through the shared policy pipeline.
* Usage: `/sef logging session current`.
* Canonical route: `sef logging session current`.
* Example: `/sef logging session current`.
* Convenience roots: none.
* Category: `logging`.
* Feature gate: `sef.logging`.
* Permissions: `sef.commands.logging.session.current`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `server`.
* Cooldown policy: `sef:logging.session.current`.
* Confirmation: not required.
* Audit class: `sensitive_access`.
* GUI descriptor: `sef:observation`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:logging.session.list`

* Description: Executes the `/sef logging session list` action through the shared policy pipeline.
* Usage: `/sef logging session list`.
* Canonical route: `sef logging session list`.
* Example: `/sef logging session list`.
* Convenience roots: none.
* Category: `logging`.
* Feature gate: `sef.logging`.
* Permissions: `sef.commands.logging.session.list`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `server`.
* Cooldown policy: `sef:logging.session.list`.
* Confirmation: not required.
* Audit class: `sensitive_access`.
* GUI descriptor: `sef:observation`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:logging.stats`

* Description: Executes the `/sef logging stats` action through the shared policy pipeline.
* Usage: `/sef logging stats`.
* Canonical route: `sef logging stats`.
* Example: `/sef logging stats`.
* Convenience roots: none.
* Category: `logging`.
* Feature gate: `sef.logging`.
* Permissions: `sef.commands.logging.stats`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `server`.
* Cooldown policy: `sef:logging.stats`.
* Confirmation: not required.
* Audit class: `sensitive_access`.
* GUI descriptor: `sef:observation`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:logging.status`

* Description: Executes the `/sef logging status` action through the shared policy pipeline.
* Usage: `/sef logging status`.
* Canonical route: `sef logging status`.
* Example: `/sef logging status`.
* Convenience roots: `loggerspy`.
* Category: `logging`.
* Feature gate: `sef.logging`.
* Permissions: `sef.commands.logging.status`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `server`.
* Cooldown policy: `sef:logging.status`.
* Confirmation: not required.
* Audit class: `file_log_control`.
* GUI descriptor: `sef:observation`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_existing`.
* Player facing: true.
* Shared pipeline: required.

### `sef:logging.stream.configure`

* Description: Executes the `/sef logging stream enable` action through the shared policy pipeline.
* Usage: `/sef logging stream enable`.
* Canonical route: `sef logging stream enable`.
* Example: `/sef logging stream enable`.
* Convenience roots: none.
* Category: `logging`.
* Feature gate: `sef.logging`.
* Permissions: `sef.commands.logging.stream.configure`.
* Access class: `owner`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `server`.
* Cooldown policy: `sef:logging.stream.configure`.
* Confirmation: not required.
* Audit class: `file_log_control`.
* GUI descriptor: `sef:observation`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:logging.stream.list`

* Description: Executes the `/sef logging stream list` action through the shared policy pipeline.
* Usage: `/sef logging stream list`.
* Canonical route: `sef logging stream list`.
* Example: `/sef logging stream list`.
* Convenience roots: none.
* Category: `logging`.
* Feature gate: `sef.logging`.
* Permissions: `sef.commands.logging.stream.list`.
* Access class: `owner`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `server`.
* Cooldown policy: `sef:logging.stream.list`.
* Confirmation: not required.
* Audit class: `file_log_control`.
* GUI descriptor: `sef:observation`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:moderation.ban`

* Description: Executes the `/ban` action through the shared policy pipeline.
* Usage: `/ban`.
* Canonical route: `ban`.
* Example: `/ban`.
* Convenience roots: `ban`.
* Category: `moderation`.
* Feature gate: `sef.moderation`.
* Permissions: `sef.commands.ban`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `required_player`.
* Cooldown policy: `sef:moderation.ban`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:moderation`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:moderation.ban_ip`

* Description: Executes the `/ban-ip` action through the shared policy pipeline.
* Usage: `/ban-ip`.
* Canonical route: `ban-ip`.
* Example: `/ban-ip`.
* Convenience roots: `ban-ip`, `banip`.
* Category: `moderation`.
* Feature gate: `sef.moderation`.
* Permissions: `sef.commands.banip`.
* Access class: `owner`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `bounded_players`.
* Cooldown policy: `sef:moderation.ban_ip`.
* Confirmation: not required.
* Audit class: `network_address_action`.
* GUI descriptor: `sef:moderation`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:moderation.clearwarnings`

* Description: Executes the `/clearwarnings` action through the shared policy pipeline.
* Usage: `/clearwarnings`.
* Canonical route: `clearwarnings`.
* Example: `/clearwarnings`.
* Convenience roots: `clearwarnings`.
* Category: `moderation`.
* Feature gate: `sef.moderation`.
* Permissions: `sef.commands.warn`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:moderation.clearwarnings`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:moderation`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:moderation.deljail`

* Description: Executes the `/deljail` action through the shared policy pipeline.
* Usage: `/deljail`.
* Canonical route: `deljail`.
* Example: `/deljail`.
* Convenience roots: `deljail`.
* Category: `moderation.jails`.
* Feature gate: `sef.moderation.jails`.
* Permissions: `sef.commands.deljail`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `server`.
* Cooldown policy: `sef:moderation.deljail`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:moderation`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:moderation.disablebuilding`

* Description: Executes the `/disablebuilding` action through the shared policy pipeline.
* Usage: `/disablebuilding`.
* Canonical route: `disablebuilding`.
* Example: `/disablebuilding`.
* Convenience roots: `db`, `disablebuilding`.
* Category: `moderation`.
* Feature gate: `sef.moderation`.
* Permissions: `sef.commands.disablebuilding`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:moderation.disablebuilding`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:moderation`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:moderation.freeze`

* Description: Executes the `/freeze` action through the shared policy pipeline.
* Usage: `/freeze`.
* Canonical route: `freeze`.
* Example: `/freeze`.
* Convenience roots: `freeze`.
* Category: `moderation`.
* Feature gate: `sef.moderation`.
* Permissions: `sef.commands.freeze`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:moderation.freeze`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:moderation`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:moderation.freezelist`

* Description: Executes the `/freezelist` action through the shared policy pipeline.
* Usage: `/freezelist`.
* Canonical route: `freezelist`.
* Example: `/freezelist`.
* Convenience roots: `freezelist`.
* Category: `moderation`.
* Feature gate: `sef.moderation`.
* Permissions: `sef.commands.freeze`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `server`.
* Cooldown policy: `sef:moderation.freezelist`.
* Confirmation: not required.
* Audit class: `sensitive_access`.
* GUI descriptor: `sef:moderation`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:moderation.invlock`

* Description: Executes the `/invlock` action through the shared policy pipeline.
* Usage: `/invlock`.
* Canonical route: `invlock`.
* Example: `/invlock`.
* Convenience roots: `invlock`.
* Category: `moderation`.
* Feature gate: `sef.moderation`.
* Permissions: `sef.commands.invlock`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:moderation.invlock`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:moderation`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:moderation.jail`

* Description: Executes the `/jail` action through the shared policy pipeline.
* Usage: `/jail`.
* Canonical route: `jail`.
* Example: `/jail`.
* Convenience roots: `jail`.
* Category: `moderation.jails`.
* Feature gate: `sef.moderation.jails`.
* Permissions: `sef.commands.jail`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `required_player`.
* Cooldown policy: `sef:moderation.jail`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:moderation`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:moderation.jailedplayers`

* Description: Executes the `/jailedplayers` action through the shared policy pipeline.
* Usage: `/jailedplayers`.
* Canonical route: `jailedplayers`.
* Example: `/jailedplayers`.
* Convenience roots: `jailedplayers`.
* Category: `moderation.jails`.
* Feature gate: `sef.moderation.jails`.
* Permissions: `sef.commands.jailedplayers`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `server`.
* Cooldown policy: `sef:moderation.jailedplayers`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:moderation`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:moderation.jails`

* Description: Executes the `/jails` action through the shared policy pipeline.
* Usage: `/jails`.
* Canonical route: `jails`.
* Example: `/jails`.
* Convenience roots: `jails`.
* Category: `moderation.jails`.
* Feature gate: `sef.moderation.jails`.
* Permissions: `sef.commands.jails`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `server`.
* Cooldown policy: `sef:moderation.jails`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:moderation`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:moderation.kick`

* Description: Executes the `/kick` action through the shared policy pipeline.
* Usage: `/kick`.
* Canonical route: `kick`.
* Example: `/kick`.
* Convenience roots: `kick`.
* Category: `moderation`.
* Feature gate: `sef.moderation`.
* Permissions: `sef.commands.kick`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `required_player`.
* Cooldown policy: `sef:moderation.kick`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:moderation`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:moderation.kick_all`

* Description: Executes the `/kickall` action through the shared policy pipeline.
* Usage: `/kickall`.
* Canonical route: `kickall`.
* Example: `/kickall`.
* Convenience roots: `kickall`.
* Category: `moderation`.
* Feature gate: `sef.moderation`.
* Permissions: `sef.commands.kickall`.
* Access class: `owner`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `bounded_players`.
* Cooldown policy: `sef:moderation.kick_all`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:moderation`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:moderation.kick_ip`

* Description: Executes the `/kick-ip` action through the shared policy pipeline.
* Usage: `/kick-ip`.
* Canonical route: `kick-ip`.
* Example: `/kick-ip`.
* Convenience roots: `kick-ip`, `kickip`.
* Category: `moderation`.
* Feature gate: `sef.moderation`.
* Permissions: `sef.commands.kickip`.
* Access class: `owner`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `bounded_players`.
* Cooldown policy: `sef:moderation.kick_ip`.
* Confirmation: not required.
* Audit class: `network_address_action`.
* GUI descriptor: `sef:moderation`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:moderation.kick_self`

* Description: Executes the `/kickme` action through the shared policy pipeline.
* Usage: `/kickme`.
* Canonical route: `kickme`.
* Example: `/kickme`.
* Convenience roots: `kickme`.
* Category: `moderation`.
* Feature gate: `sef.moderation`.
* Permissions: `sef.commands.kickme`.
* Access class: `player`.
* Sources: `player`.
* Target behavior: `self`.
* Cooldown policy: `sef:moderation.kick_self`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:moderation`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:moderation.mute`

* Description: Executes the `/mute` action through the shared policy pipeline.
* Usage: `/mute`.
* Canonical route: `mute`.
* Example: `/mute`.
* Convenience roots: `mute`.
* Category: `moderation`.
* Feature gate: `sef.moderation`.
* Permissions: `sef.commands.mute`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:moderation.mute`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:moderation`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:moderation.mutelist`

* Description: Executes the `/mutelist` action through the shared policy pipeline.
* Usage: `/mutelist`.
* Canonical route: `mutelist`.
* Example: `/mutelist`.
* Convenience roots: `mutelist`.
* Category: `moderation`.
* Feature gate: `sef.moderation`.
* Permissions: `sef.commands.mute`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `server`.
* Cooldown policy: `sef:moderation.mutelist`.
* Confirmation: not required.
* Audit class: `sensitive_access`.
* GUI descriptor: `sef:moderation`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:moderation.pardon`

* Description: Executes the `/pardon` action through the shared policy pipeline.
* Usage: `/pardon`.
* Canonical route: `pardon`.
* Example: `/pardon`.
* Convenience roots: `pardon`, `unban`.
* Category: `moderation`.
* Feature gate: `sef.moderation`.
* Permissions: `sef.commands.pardon`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `required_player`.
* Cooldown policy: `sef:moderation.pardon`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:moderation`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:moderation.pardon_ip`

* Description: Executes the `/pardon-ip` action through the shared policy pipeline.
* Usage: `/pardon-ip`.
* Canonical route: `pardon-ip`.
* Example: `/pardon-ip`.
* Convenience roots: `pardon-ip`, `unban-ip`, `unbanip`.
* Category: `moderation`.
* Feature gate: `sef.moderation`.
* Permissions: `sef.commands.pardonip`.
* Access class: `owner`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `server`.
* Cooldown policy: `sef:moderation.pardon_ip`.
* Confirmation: not required.
* Audit class: `network_address_action`.
* GUI descriptor: `sef:moderation`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:moderation.setjail`

* Description: Executes the `/setjail` action through the shared policy pipeline.
* Usage: `/setjail`.
* Canonical route: `setjail`.
* Example: `/setjail`.
* Convenience roots: `setjail`.
* Category: `moderation.jails`.
* Feature gate: `sef.moderation.jails`.
* Permissions: `sef.commands.setjail`.
* Access class: `administrator`.
* Sources: `player`.
* Target behavior: `server`.
* Cooldown policy: `sef:moderation.setjail`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:moderation`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:moderation.tempban`

* Description: Executes the `/tempban` action through the shared policy pipeline.
* Usage: `/tempban`.
* Canonical route: `tempban`.
* Example: `/tempban`.
* Convenience roots: `tempban`.
* Category: `moderation`.
* Feature gate: `sef.moderation`.
* Permissions: `sef.commands.tempban`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `required_player`.
* Cooldown policy: `sef:moderation.tempban`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:moderation`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:moderation.tempban_ip`

* Description: Executes the `/tempban-ip` action through the shared policy pipeline.
* Usage: `/tempban-ip`.
* Canonical route: `tempban-ip`.
* Example: `/tempban-ip`.
* Convenience roots: `tempban-ip`, `tempbanip`.
* Category: `moderation`.
* Feature gate: `sef.moderation`.
* Permissions: `sef.commands.tempbanip`.
* Access class: `owner`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `bounded_players`.
* Cooldown policy: `sef:moderation.tempban_ip`.
* Confirmation: not required.
* Audit class: `network_address_action`.
* GUI descriptor: `sef:moderation`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:moderation.unfreeze`

* Description: Executes the `/unfreeze` action through the shared policy pipeline.
* Usage: `/unfreeze`.
* Canonical route: `unfreeze`.
* Example: `/unfreeze`.
* Convenience roots: `unfreeze`.
* Category: `moderation`.
* Feature gate: `sef.moderation`.
* Permissions: `sef.commands.unfreeze`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:moderation.unfreeze`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:moderation`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:moderation.unjail`

* Description: Executes the `/unjail` action through the shared policy pipeline.
* Usage: `/unjail`.
* Canonical route: `unjail`.
* Example: `/unjail`.
* Convenience roots: `unjail`.
* Category: `moderation.jails`.
* Feature gate: `sef.moderation.jails`.
* Permissions: `sef.commands.unjail`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `required_player`.
* Cooldown policy: `sef:moderation.unjail`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:moderation`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:moderation.unmute`

* Description: Executes the `/unmute` action through the shared policy pipeline.
* Usage: `/unmute`.
* Canonical route: `unmute`.
* Example: `/unmute`.
* Convenience roots: `unmute`.
* Category: `moderation`.
* Feature gate: `sef.moderation`.
* Permissions: `sef.commands.unmute`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:moderation.unmute`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:moderation`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:moderation.warn`

* Description: Executes the `/warn` action through the shared policy pipeline.
* Usage: `/warn`.
* Canonical route: `warn`.
* Example: `/warn`.
* Convenience roots: `warn`.
* Category: `moderation`.
* Feature gate: `sef.moderation`.
* Permissions: `sef.commands.warn`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:moderation.warn`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:moderation`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:moderation.warns`

* Description: Executes the `/warns` action through the shared policy pipeline.
* Usage: `/warns`.
* Canonical route: `warns`.
* Example: `/warns`.
* Convenience roots: `warns`.
* Category: `moderation`.
* Feature gate: `sef.moderation`.
* Permissions: `sef.commands.warns`.
* Access class: `player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:moderation.warns`.
* Confirmation: not required.
* Audit class: `sensitive_access`.
* GUI descriptor: `sef:moderation`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:motd.reload`

* Description: Executes the `/sef motd reload` action through the shared policy pipeline.
* Usage: `/sef motd reload`.
* Canonical route: `sef motd reload`.
* Example: `/sef motd reload`.
* Convenience roots: none.
* Category: `sef`.
* Feature gate: `sef.motd`.
* Permissions: `sef.commands.sef.allowed`, `sef.motd.manage`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:motd.reload`.
* Confirmation: not required.
* Audit class: `config_definition`.
* GUI descriptor: `sef:core`.
* HUD contract: motd reload is immediate.
* Quota contract: command does not create retained records or variable fan out.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:motd.set`

* Description: Executes the `/sef motd set` action through the shared policy pipeline.
* Usage: `/sef motd set`.
* Canonical route: `sef motd set`.
* Example: `/sef motd set`.
* Convenience roots: none.
* Category: `sef`.
* Feature gate: `sef.motd`.
* Permissions: `sef.commands.sef.allowed`, `sef.motd.manage`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:motd.set`.
* Confirmation: not required.
* Audit class: `config_definition`.
* GUI descriptor: `sef:core`.
* HUD contract: motd changes are immediate.
* Quota contract: command does not create retained records or variable fan out.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:motd.show`

* Description: Executes the `/sef motd show` action through the shared policy pipeline.
* Usage: `/sef motd show`.
* Canonical route: `sef motd show`.
* Example: `/sef motd show`.
* Convenience roots: none.
* Category: `sef`.
* Feature gate: `sef.motd`.
* Permissions: `sef.commands.sef.allowed`, `sef.motd.manage`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:motd.show`.
* Confirmation: not required.
* Audit class: `sensitive_access`.
* GUI descriptor: `sef:core`.
* HUD contract: motd view has no persistent hud.
* Quota contract: command does not create retained records or variable fan out.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:panel.draft.control_add`

* Description: Executes the `/sef panel draft control add` action through the shared policy pipeline.
* Usage: `/sef panel draft control add`.
* Canonical route: `sef panel draft control add`.
* Example: `/sef panel draft control add`.
* Convenience roots: none.
* Category: `core`.
* Feature gate: `sef.core`.
* Permissions: `sef.commands.panel.draft`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:panel.draft.control_add`.
* Confirmation: not required.
* Audit class: `config_definition`.
* GUI descriptor: `sef:panels`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:panel.draft.control_remove`

* Description: Executes the `/sef panel draft control remove` action through the shared policy pipeline.
* Usage: `/sef panel draft control remove`.
* Canonical route: `sef panel draft control remove`.
* Example: `/sef panel draft control remove`.
* Convenience roots: none.
* Category: `core`.
* Feature gate: `sef.core`.
* Permissions: `sef.commands.panel.draft`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:panel.draft.control_remove`.
* Confirmation: not required.
* Audit class: `config_definition`.
* GUI descriptor: `sef:panels`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:panel.draft.create`

* Description: Executes the `/sef panel draft create` action through the shared policy pipeline.
* Usage: `/sef panel draft create`.
* Canonical route: `sef panel draft create`.
* Example: `/sef panel draft create`.
* Convenience roots: none.
* Category: `core`.
* Feature gate: `sef.core`.
* Permissions: `sef.commands.panel.draft`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:panel.draft.create`.
* Confirmation: not required.
* Audit class: `config_definition`.
* GUI descriptor: `sef:panels`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:panel.draft.delete`

* Description: Executes the `/sef panel draft delete` action through the shared policy pipeline.
* Usage: `/sef panel draft delete`.
* Canonical route: `sef panel draft delete`.
* Example: `/sef panel draft delete`.
* Convenience roots: none.
* Category: `core`.
* Feature gate: `sef.core`.
* Permissions: `sef.commands.panel.draft`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:panel.draft.delete`.
* Confirmation: not required.
* Audit class: `config_definition`.
* GUI descriptor: `sef:panels`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:panel.inspect`

* Description: Executes the `/sef panel inspect` action through the shared policy pipeline.
* Usage: `/sef panel inspect`.
* Canonical route: `sef panel inspect`.
* Example: `/sef panel inspect`.
* Convenience roots: none.
* Category: `core`.
* Feature gate: `sef.core`.
* Permissions: `sef.commands.panel.inspect`.
* Access class: `staff`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:panel.inspect`.
* Confirmation: not required.
* Audit class: `sensitive_access`.
* GUI descriptor: `sef:panels`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:panel.list`

* Description: Executes the `/sef panel list` action through the shared policy pipeline.
* Usage: `/sef panel list`.
* Canonical route: `sef panel list`.
* Example: `/sef panel list`.
* Convenience roots: none.
* Category: `core`.
* Feature gate: `sef.core`.
* Permissions: `sef.commands.panel.list`.
* Access class: `staff`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:panel.list`.
* Confirmation: not required.
* Audit class: `sensitive_access`.
* GUI descriptor: `sef:panels`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:panel.preview`

* Description: Executes the `/sef panel preview` action through the shared policy pipeline.
* Usage: `/sef panel preview`.
* Canonical route: `sef panel preview`.
* Example: `/sef panel preview`.
* Convenience roots: none.
* Category: `core`.
* Feature gate: `sef.core`.
* Permissions: `sef.commands.panel.preview`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:panel.preview`.
* Confirmation: not required.
* Audit class: `workflow_execution`.
* GUI descriptor: `sef:panels`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:panel.publish`

* Description: Executes the `/sef panel publish` action through the shared policy pipeline.
* Usage: `/sef panel publish`.
* Canonical route: `sef panel publish`.
* Example: `/sef panel publish`.
* Convenience roots: none.
* Category: `core`.
* Feature gate: `sef.core`.
* Permissions: `sef.commands.panel.publish`.
* Access class: `owner`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:panel.publish`.
* Confirmation: not required.
* Audit class: `config_definition`.
* GUI descriptor: `sef:panels`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:panel.rollback`

* Description: Executes the `/sef panel rollback` action through the shared policy pipeline.
* Usage: `/sef panel rollback`.
* Canonical route: `sef panel rollback`.
* Example: `/sef panel rollback`.
* Convenience roots: none.
* Category: `core`.
* Feature gate: `sef.core`.
* Permissions: `sef.commands.panel.rollback`.
* Access class: `owner`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:panel.rollback`.
* Confirmation: not required.
* Audit class: `config_definition`.
* GUI descriptor: `sef:panels`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:panel.run`

* Description: Executes the `/sef panel run` action through the shared policy pipeline.
* Usage: `/sef panel run`.
* Canonical route: `sef panel run`.
* Example: `/sef panel run`.
* Convenience roots: none.
* Category: `core`.
* Feature gate: `sef.core`.
* Permissions: `sef.commands.panel.run`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:panel.run`.
* Confirmation: not required.
* Audit class: `workflow_execution`.
* GUI descriptor: `sef:panels`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:profile.create`

* Description: Executes the `/sef profile create` action through the shared policy pipeline.
* Usage: `/sef profile create`.
* Canonical route: `sef profile create`.
* Example: `/sef profile create`.
* Convenience roots: none.
* Category: `automation`.
* Feature gate: `sef.automation`.
* Permissions: `sef.commands.profile.create`.
* Access class: `owner`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:profile.create`.
* Confirmation: not required.
* Audit class: `config_definition`.
* GUI descriptor: `sef:aliases`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:profile.delete`

* Description: Executes the `/sef profile delete` action through the shared policy pipeline.
* Usage: `/sef profile delete`.
* Canonical route: `sef profile delete`.
* Example: `/sef profile delete`.
* Convenience roots: none.
* Category: `automation`.
* Feature gate: `sef.automation`.
* Permissions: `sef.commands.profile.delete`.
* Access class: `owner`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:profile.delete`.
* Confirmation: not required.
* Audit class: `config_definition`.
* GUI descriptor: `sef:aliases`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:profile.enable`

* Description: Executes the `/sef profile enable` action through the shared policy pipeline.
* Usage: `/sef profile enable`.
* Canonical route: `sef profile enable`.
* Example: `/sef profile enable`.
* Convenience roots: none.
* Category: `automation`.
* Feature gate: `sef.automation`.
* Permissions: `sef.commands.profile.enable`.
* Access class: `owner`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:profile.enable`.
* Confirmation: not required.
* Audit class: `config_definition`.
* GUI descriptor: `sef:aliases`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:profile.execute`

* Description: Executes the `/sef profile execute` action through the shared policy pipeline.
* Usage: `/sef profile execute`.
* Canonical route: `sef profile execute`.
* Example: `/sef profile execute`.
* Convenience roots: none.
* Category: `automation`.
* Feature gate: `sef.automation`.
* Permissions: `sef.commands.profile.execute`.
* Access class: `owner`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `bounded_players`.
* Cooldown policy: `sef:profile.execute`.
* Confirmation: not required.
* Audit class: `workflow_execution`.
* GUI descriptor: `sef:aliases`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:profile.inspect`

* Description: Executes the `/sef profile inspect` action through the shared policy pipeline.
* Usage: `/sef profile inspect`.
* Canonical route: `sef profile inspect`.
* Example: `/sef profile inspect`.
* Convenience roots: none.
* Category: `automation`.
* Feature gate: `sef.automation`.
* Permissions: `sef.commands.profile.inspect`.
* Access class: `owner`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:profile.inspect`.
* Confirmation: not required.
* Audit class: `config_definition`.
* GUI descriptor: `sef:aliases`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:profile.list`

* Description: Executes the `/sef profile list` action through the shared policy pipeline.
* Usage: `/sef profile list`.
* Canonical route: `sef profile list`.
* Example: `/sef profile list`.
* Convenience roots: none.
* Category: `automation`.
* Feature gate: `sef.automation`.
* Permissions: `sef.commands.profile.list`.
* Access class: `owner`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:profile.list`.
* Confirmation: not required.
* Audit class: `config_definition`.
* GUI descriptor: `sef:aliases`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:profile.publish`

* Description: Executes the `/sef profile publish` action through the shared policy pipeline.
* Usage: `/sef profile publish`.
* Canonical route: `sef profile publish`.
* Example: `/sef profile publish`.
* Convenience roots: none.
* Category: `automation`.
* Feature gate: `sef.automation`.
* Permissions: `sef.commands.profile.publish`.
* Access class: `owner`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:profile.publish`.
* Confirmation: not required.
* Audit class: `config_definition`.
* GUI descriptor: `sef:aliases`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:profile.reference`

* Description: Executes the `/sef profile reference` action through the shared policy pipeline.
* Usage: `/sef profile reference`.
* Canonical route: `sef profile reference`.
* Example: `/sef profile reference`.
* Convenience roots: none.
* Category: `automation`.
* Feature gate: `sef.automation`.
* Permissions: `sef.commands.profile.reference`.
* Access class: `owner`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:profile.reference`.
* Confirmation: not required.
* Audit class: `config_definition`.
* GUI descriptor: `sef:aliases`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:profile.rollback`

* Description: Executes the `/sef profile rollback` action through the shared policy pipeline.
* Usage: `/sef profile rollback`.
* Canonical route: `sef profile rollback`.
* Example: `/sef profile rollback`.
* Convenience roots: none.
* Category: `automation`.
* Feature gate: `sef.automation`.
* Permissions: `sef.commands.profile.rollback`.
* Access class: `owner`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:profile.rollback`.
* Confirmation: not required.
* Audit class: `config_definition`.
* GUI descriptor: `sef:aliases`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:profile.test`

* Description: Executes the `/sef profile test` action through the shared policy pipeline.
* Usage: `/sef profile test`.
* Canonical route: `sef profile test`.
* Example: `/sef profile test`.
* Convenience roots: none.
* Category: `automation`.
* Feature gate: `sef.automation`.
* Permissions: `sef.commands.profile.test`.
* Access class: `owner`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:profile.test`.
* Confirmation: not required.
* Audit class: `config_definition`.
* GUI descriptor: `sef:aliases`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:profile.validate`

* Description: Executes the `/sef profile validate` action through the shared policy pipeline.
* Usage: `/sef profile validate`.
* Canonical route: `sef profile validate`.
* Example: `/sef profile validate`.
* Convenience roots: none.
* Category: `automation`.
* Feature gate: `sef.automation`.
* Permissions: `sef.commands.profile.validate`.
* Access class: `owner`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:profile.validate`.
* Confirmation: not required.
* Audit class: `config_definition`.
* GUI descriptor: `sef:aliases`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:run.server`

* Description: Executes the `/run` action through the shared policy pipeline.
* Usage: `/run`.
* Canonical route: `run`.
* Example: `/run`.
* Convenience roots: `run`.
* Category: `run`.
* Feature gate: `sef.run`.
* Permissions: `sef.commands.run`.
* Access class: `owner`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `server`.
* Cooldown policy: `sef:run.server`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:core`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:silent.actor`

* Description: Executes the `/silent` action through the shared policy pipeline.
* Usage: `/silent`.
* Canonical route: `silent`.
* Example: `/silent`.
* Convenience roots: `silent`.
* Category: `run`.
* Feature gate: `sef.run`.
* Permissions: `sef.commands.silent.actor`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:silent.actor`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:core`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:silent.server`

* Description: Executes the `/silent server` action through the shared policy pipeline.
* Usage: `/silent server`.
* Canonical route: `silent server`.
* Example: `/silent server`.
* Convenience roots: none.
* Category: `run`.
* Feature gate: `sef.run`.
* Permissions: `sef.commands.silent.server`.
* Access class: `owner`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `server`.
* Cooldown policy: `sef:silent.server`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:core`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:social.connection`

* Description: Executes the `/connectionmessage` action through the shared policy pipeline.
* Usage: `/connectionmessage`.
* Canonical route: `connectionmessage`.
* Example: `/connectionmessage`.
* Convenience roots: `joinmessage`, `leavemessage`.
* Category: `social`.
* Feature gate: `sef.social.connection`.
* Permissions: `sef.commands.connectionmessage.inspect`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `required_player`.
* Cooldown policy: `sef:social.connection`.
* Confirmation: not required.
* Audit class: `sensitive_access`.
* GUI descriptor: `sef:social`.
* HUD contract: social state is shown through immediate command feedback.
* Quota contract: social collections are bounded by repository and quota policy.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:social.identity`

* Description: Executes the `/sef identity` action through the shared policy pipeline.
* Usage: `/sef identity`.
* Canonical route: `sef identity`.
* Example: `/sef identity`.
* Convenience roots: none.
* Category: `social`.
* Feature gate: `sef.social`.
* Permissions: `sef.commands.sef.identity.coverage`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:social.identity`.
* Confirmation: not required.
* Audit class: `sensitive_access`.
* GUI descriptor: `sef:social`.
* HUD contract: social state is shown through immediate command feedback.
* Quota contract: social collections are bounded by repository and quota policy.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:social.ignore`

* Description: Executes the `/ignore` action through the shared policy pipeline.
* Usage: `/ignore`.
* Canonical route: `ignore`.
* Example: `/ignore`.
* Convenience roots: `ignore`, `ignorelist`.
* Category: `social`.
* Feature gate: `sef.social`.
* Permissions: `sef.commands.ignore`.
* Access class: `player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:social.ignore`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:social`.
* HUD contract: social state is shown through immediate command feedback.
* Quota contract: social collections are bounded by repository and quota policy.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:social.mail`

* Description: Executes the `/mail` action through the shared policy pipeline.
* Usage: `/mail`.
* Canonical route: `mail`.
* Example: `/mail`.
* Convenience roots: `mail`.
* Category: `social`.
* Feature gate: `sef.social.mail`.
* Permissions: `sef.commands.mail`.
* Access class: `player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:social.mail`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:social`.
* HUD contract: social state is shown through immediate command feedback.
* Quota contract: social collections are bounded by repository and quota policy.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:social.message`

* Description: Executes the `/msg` action through the shared policy pipeline.
* Usage: `/msg`.
* Canonical route: `msg`.
* Example: `/msg`.
* Convenience roots: `msg`, `pchat`, `r`, `reply`, `tell`, `w`, `whisper`.
* Category: `social`.
* Feature gate: `sef.social`.
* Permissions: `sef.commands.msg`.
* Access class: `player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `required_player`.
* Cooldown policy: `sef:social.message`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:social`.
* HUD contract: social state is shown through immediate command feedback.
* Quota contract: social collections are bounded by repository and quota policy.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:social.message.toggle`

* Description: Executes the `/msgtoggle` action through the shared policy pipeline.
* Usage: `/msgtoggle`.
* Canonical route: `msgtoggle`.
* Example: `/msgtoggle`.
* Convenience roots: `msgtoggle`.
* Category: `social`.
* Feature gate: `sef.social`.
* Permissions: `sef.commands.msgtoggle`.
* Access class: `player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `self`.
* Cooldown policy: `sef:social.message.toggle`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:social`.
* HUD contract: social state is shown through immediate command feedback.
* Quota contract: social collections are bounded by repository and quota policy.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:social.reminder`

* Description: Executes the `/reminder` action through the shared policy pipeline.
* Usage: `/reminder`.
* Canonical route: `reminder`.
* Example: `/reminder`.
* Convenience roots: `reminder`, `reminders`, `welcome`.
* Category: `social`.
* Feature gate: `sef.social.reminders`.
* Permissions: `sef.commands.reminders`.
* Access class: `player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:social.reminder`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:social`.
* HUD contract: social state is shown through immediate command feedback.
* Quota contract: social collections are bounded by repository and quota policy.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:social.reply.toggle`

* Description: Executes the `/rtoggle` action through the shared policy pipeline.
* Usage: `/rtoggle`.
* Canonical route: `rtoggle`.
* Example: `/rtoggle`.
* Convenience roots: `rtoggle`.
* Category: `social`.
* Feature gate: `sef.social`.
* Permissions: `sef.commands.rtoggle`.
* Access class: `player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `self`.
* Cooldown policy: `sef:social.reply.toggle`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:social`.
* HUD contract: social state is shown through immediate command feedback.
* Quota contract: social collections are bounded by repository and quota policy.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:social.spy`

* Description: Executes the `/socialspy` action through the shared policy pipeline.
* Usage: `/socialspy`.
* Canonical route: `socialspy`.
* Example: `/socialspy`.
* Convenience roots: `socialspy`.
* Category: `social`.
* Feature gate: `sef.social.spy`.
* Permissions: `sef.commands.socialspy`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:social.spy`.
* Confirmation: not required.
* Audit class: `sensitive_access`.
* GUI descriptor: `sef:social`.
* HUD contract: `social_spy`.
* Quota contract: social collections are bounded by repository and quota policy.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:social.text`

* Description: Executes the `/customtext` action through the shared policy pipeline.
* Usage: `/customtext`.
* Canonical route: `customtext`.
* Example: `/customtext`.
* Convenience roots: `booktext`, `customtext`, `info`, `rules`.
* Category: `social`.
* Feature gate: `sef.social.text`.
* Permissions: `sef.commands.customtext`.
* Access class: `player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:social.text`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:social`.
* HUD contract: social state is shown through immediate command feedback.
* Quota contract: social collections are bounded by repository and quota policy.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:storage.export`

* Description: Executes the `/sef storage export` action through the shared policy pipeline.
* Usage: `/sef storage export`.
* Canonical route: `sef storage export`.
* Example: `/sef storage export`.
* Convenience roots: none.
* Category: `sef`.
* Feature gate: `sef.storage`.
* Permissions: `sef.commands.sef.allowed`, `sef.storage.export`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:storage.export`.
* Confirmation: not required.
* Audit class: `sensitive_access`.
* GUI descriptor: `sef:core`.
* HUD contract: storage exports are reported through command output.
* Quota contract: command does not create retained records or variable fan out.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:storage.status`

* Description: Executes the `/sef storage status` action through the shared policy pipeline.
* Usage: `/sef storage status`.
* Canonical route: `sef storage status`.
* Example: `/sef storage status`.
* Convenience roots: none.
* Category: `sef`.
* Feature gate: `sef.storage`.
* Permissions: `sef.commands.sef.allowed`, `sef.storage.status`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:storage.status`.
* Confirmation: not required.
* Audit class: `sensitive_access`.
* GUI descriptor: `sef:core`.
* HUD contract: storage diagnostics have no persistent hud.
* Quota contract: command does not create retained records or variable fan out.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:sudo.chat`

* Description: Executes the `/sudo chat` action through the shared policy pipeline.
* Usage: `/sudo chat`.
* Canonical route: `sudo chat`.
* Example: `/sudo chat`.
* Convenience roots: none.
* Category: `sudo`.
* Feature gate: `sef.sudo`.
* Permissions: `sef.commands.sudo.chat`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `required_player`.
* Cooldown policy: `sef:sudo.chat`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:identity`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:sudo.consent`

* Description: Executes the `/sudo consent` action through the shared policy pipeline.
* Usage: `/sudo consent`.
* Canonical route: `sudo consent`.
* Example: `/sudo consent`.
* Convenience roots: none.
* Category: `sudo`.
* Feature gate: `sef.sudo`.
* Permissions: `sef.commands.sudo.consent`.
* Access class: `player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:sudo.consent`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:identity`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:sudo.dryrun`

* Description: Executes the `/sudo dryrun` action through the shared policy pipeline.
* Usage: `/sudo dryrun`.
* Canonical route: `sudo dryrun`.
* Example: `/sudo dryrun`.
* Convenience roots: none.
* Category: `sudo`.
* Feature gate: `sef.sudo`.
* Permissions: `sef.commands.sudo.dryrun`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:sudo.dryrun`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:identity`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:sudo.lock`

* Description: Executes the `/sudo lock` action through the shared policy pipeline.
* Usage: `/sudo lock`.
* Canonical route: `sudo lock`.
* Example: `/sudo lock`.
* Convenience roots: none.
* Category: `sudo`.
* Feature gate: `sef.sudo`.
* Permissions: `sef.commands.sudo.lock`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `required_player`.
* Cooldown policy: `sef:sudo.lock`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:identity`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:sudo.policy`

* Description: Executes the `/sudo policy` action through the shared policy pipeline.
* Usage: `/sudo policy`.
* Canonical route: `sudo policy`.
* Example: `/sudo policy`.
* Convenience roots: none.
* Category: `sudo`.
* Feature gate: `sef.sudo`.
* Permissions: `sef.commands.sudo.policy`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:sudo.policy`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:identity`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:sudo.run`

* Description: Executes the `/sudo run` action through the shared policy pipeline.
* Usage: `/sudo run`.
* Canonical route: `sudo run`.
* Example: `/sudo run`.
* Convenience roots: `sudo`.
* Category: `sudo`.
* Feature gate: `sef.sudo`.
* Permissions: `sef.commands.sudo.run`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `required_player`.
* Cooldown policy: `sef:sudo.run`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:identity`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:tags.archive`

* Description: Executes the `/sef tags archive` action through the shared policy pipeline.
* Usage: `/sef tags archive`.
* Canonical route: `sef tags archive`.
* Example: `/sef tags archive`.
* Convenience roots: none.
* Category: `fancy_tags`.
* Feature gate: `sef.fancy_tags`.
* Permissions: `sef.commands.tags.archive`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:tags.archive`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:tags`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:tags.assign.default`

* Description: Executes the `/sef tags assign default` action through the shared policy pipeline.
* Usage: `/sef tags assign default`.
* Canonical route: `sef tags assign default`.
* Example: `/sef tags assign default`.
* Convenience roots: none.
* Category: `fancy_tags`.
* Feature gate: `sef.fancy_tags`.
* Permissions: `sef.commands.tags.assign.default`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:tags.assign.default`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:tags`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:tags.assign.group`

* Description: Executes the `/sef tags assign group` action through the shared policy pipeline.
* Usage: `/sef tags assign group`.
* Canonical route: `sef tags assign group`.
* Example: `/sef tags assign group`.
* Convenience roots: none.
* Category: `fancy_tags`.
* Feature gate: `sef.fancy_tags`.
* Permissions: `sef.commands.tags.assign.group`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:tags.assign.group`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:tags`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:tags.assign.player`

* Description: Executes the `/sef tags assign player` action through the shared policy pipeline.
* Usage: `/sef tags assign player`.
* Canonical route: `sef tags assign player`.
* Example: `/sef tags assign player`.
* Convenience roots: none.
* Category: `fancy_tags`.
* Feature gate: `sef.fancy_tags`.
* Permissions: `sef.commands.tags.assign.player`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:tags.assign.player`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:tags`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:tags.assign.team`

* Description: Executes the `/sef tags assign team` action through the shared policy pipeline.
* Usage: `/sef tags assign team`.
* Canonical route: `sef tags assign team`.
* Example: `/sef tags assign team`.
* Convenience roots: none.
* Category: `fancy_tags`.
* Feature gate: `sef.fancy_tags`.
* Permissions: `sef.commands.tags.assign.team`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:tags.assign.team`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:tags`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:tags.assignments.group`

* Description: Executes the `/sef tags assignments group` action through the shared policy pipeline.
* Usage: `/sef tags assignments group`.
* Canonical route: `sef tags assignments group`.
* Example: `/sef tags assignments group`.
* Convenience roots: none.
* Category: `fancy_tags`.
* Feature gate: `sef.fancy_tags`.
* Permissions: `sef.commands.tags.assignments.group`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:tags.assignments.group`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:tags`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:tags.assignments.player`

* Description: Executes the `/sef tags assignments player` action through the shared policy pipeline.
* Usage: `/sef tags assignments player`.
* Canonical route: `sef tags assignments player`.
* Example: `/sef tags assignments player`.
* Convenience roots: none.
* Category: `fancy_tags`.
* Feature gate: `sef.fancy_tags`.
* Permissions: `sef.commands.tags.assignments.player`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:tags.assignments.player`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:tags`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:tags.assignments.tag`

* Description: Executes the `/sef tags assignments tag` action through the shared policy pipeline.
* Usage: `/sef tags assignments tag`.
* Canonical route: `sef tags assignments tag`.
* Example: `/sef tags assignments tag`.
* Convenience roots: none.
* Category: `fancy_tags`.
* Feature gate: `sef.fancy_tags`.
* Permissions: `sef.commands.tags.assignments.tag`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:tags.assignments.tag`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:tags`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:tags.audit`

* Description: Executes the `/sef tags audit` action through the shared policy pipeline.
* Usage: `/sef tags audit`.
* Canonical route: `sef tags audit`.
* Example: `/sef tags audit`.
* Convenience roots: none.
* Category: `fancy_tags`.
* Feature gate: `sef.fancy_tags`.
* Permissions: `sef.commands.tags.audit`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:tags.audit`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:tags`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:tags.backup.create`

* Description: Executes the `/sef tags backup create` action through the shared policy pipeline.
* Usage: `/sef tags backup create`.
* Canonical route: `sef tags backup create`.
* Example: `/sef tags backup create`.
* Convenience roots: none.
* Category: `fancy_tags`.
* Feature gate: `sef.fancy_tags`.
* Permissions: `sef.commands.tags.backup.create`.
* Access class: `owner`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:tags.backup.create`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:tags`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:tags.backup.preview`

* Description: Executes the `/sef tags backup preview` action through the shared policy pipeline.
* Usage: `/sef tags backup preview`.
* Canonical route: `sef tags backup preview`.
* Example: `/sef tags backup preview`.
* Convenience roots: none.
* Category: `fancy_tags`.
* Feature gate: `sef.fancy_tags`.
* Permissions: `sef.commands.tags.backup.preview`.
* Access class: `owner`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:tags.backup.preview`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:tags`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:tags.cache.invalidate`

* Description: Executes the `/sef tags cache invalidate` action through the shared policy pipeline.
* Usage: `/sef tags cache invalidate`.
* Canonical route: `sef tags cache invalidate`.
* Example: `/sef tags cache invalidate`.
* Convenience roots: none.
* Category: `fancy_tags`.
* Feature gate: `sef.fancy_tags`.
* Permissions: `sef.commands.tags.cache.invalidate`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:tags.cache.invalidate`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:tags`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:tags.cache.status`

* Description: Executes the `/sef tags cache status` action through the shared policy pipeline.
* Usage: `/sef tags cache status`.
* Canonical route: `sef tags cache status`.
* Example: `/sef tags cache status`.
* Convenience roots: none.
* Category: `fancy_tags`.
* Feature gate: `sef.fancy_tags`.
* Permissions: `sef.commands.tags.cache.status`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:tags.cache.status`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:tags`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:tags.category.create`

* Description: Executes the `/sef tags category create` action through the shared policy pipeline.
* Usage: `/sef tags category create`.
* Canonical route: `sef tags category create`.
* Example: `/sef tags category create`.
* Convenience roots: none.
* Category: `fancy_tags`.
* Feature gate: `sef.fancy_tags`.
* Permissions: `sef.commands.tags.category.create`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:tags.category.create`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:tags`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:tags.category.delete`

* Description: Executes the `/sef tags category delete` action through the shared policy pipeline.
* Usage: `/sef tags category delete`.
* Canonical route: `sef tags category delete`.
* Example: `/sef tags category delete`.
* Convenience roots: none.
* Category: `fancy_tags`.
* Feature gate: `sef.fancy_tags`.
* Permissions: `sef.commands.tags.category.delete`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:tags.category.delete`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:tags`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:tags.category.edit`

* Description: Executes the `/sef tags category edit` action through the shared policy pipeline.
* Usage: `/sef tags category edit`.
* Canonical route: `sef tags category edit`.
* Example: `/sef tags category edit`.
* Convenience roots: none.
* Category: `fancy_tags`.
* Feature gate: `sef.fancy_tags`.
* Permissions: `sef.commands.tags.category.edit`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:tags.category.edit`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:tags`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:tags.category.list`

* Description: Executes the `/sef tags category list` action through the shared policy pipeline.
* Usage: `/sef tags category list`.
* Canonical route: `sef tags category list`.
* Example: `/sef tags category list`.
* Convenience roots: none.
* Category: `fancy_tags`.
* Feature gate: `sef.fancy_tags`.
* Permissions: `sef.commands.tags.category.list`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:tags.category.list`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:tags`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:tags.create`

* Description: Executes the `/sef tags create` action through the shared policy pipeline.
* Usage: `/sef tags create`.
* Canonical route: `sef tags create`.
* Example: `/sef tags create`.
* Convenience roots: none.
* Category: `fancy_tags`.
* Feature gate: `sef.fancy_tags`.
* Permissions: `sef.commands.tags.create`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:tags.create`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:tags`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:tags.delete`

* Description: Executes the `/sef tags delete` action through the shared policy pipeline.
* Usage: `/sef tags delete`.
* Canonical route: `sef tags delete`.
* Example: `/sef tags delete`.
* Convenience roots: none.
* Category: `fancy_tags`.
* Feature gate: `sef.fancy_tags`.
* Permissions: `sef.commands.tags.delete`.
* Access class: `owner`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:tags.delete`.
* Confirmation: not required.
* Audit class: `destructive`.
* GUI descriptor: `sef:tags`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:tags.doctor`

* Description: Executes the `/sef tags doctor` action through the shared policy pipeline.
* Usage: `/sef tags doctor`.
* Canonical route: `sef tags doctor`.
* Example: `/sef tags doctor`.
* Convenience roots: none.
* Category: `fancy_tags`.
* Feature gate: `sef.fancy_tags`.
* Permissions: `sef.commands.tags.doctor`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:tags.doctor`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:tags`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:tags.duplicate`

* Description: Executes the `/sef tags duplicate` action through the shared policy pipeline.
* Usage: `/sef tags duplicate`.
* Canonical route: `sef tags duplicate`.
* Example: `/sef tags duplicate`.
* Convenience roots: none.
* Category: `fancy_tags`.
* Feature gate: `sef.fancy_tags`.
* Permissions: `sef.commands.tags.duplicate`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:tags.duplicate`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:tags`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:tags.edit`

* Description: Executes the `/sef tags edit` action through the shared policy pipeline.
* Usage: `/sef tags edit`.
* Canonical route: `sef tags edit`.
* Example: `/sef tags edit`.
* Convenience roots: none.
* Category: `fancy_tags`.
* Feature gate: `sef.fancy_tags`.
* Permissions: `sef.commands.tags.edit`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:tags.edit`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:tags`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:tags.export.manifest`

* Description: Executes the `/sef tags export manifest` action through the shared policy pipeline.
* Usage: `/sef tags export manifest`.
* Canonical route: `sef tags export manifest`.
* Example: `/sef tags export manifest`.
* Convenience roots: none.
* Category: `fancy_tags`.
* Feature gate: `sef.fancy_tags`.
* Permissions: `sef.commands.tags.export.manifest`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:tags.export.manifest`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:tags`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:tags.export.png`

* Description: Executes the `/sef tags export png` action through the shared policy pipeline.
* Usage: `/sef tags export png`.
* Canonical route: `sef tags export png`.
* Example: `/sef tags export png`.
* Convenience roots: none.
* Category: `fancy_tags`.
* Feature gate: `sef.fancy_tags`.
* Permissions: `sef.commands.tags.export.png`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:tags.export.png`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:tags`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:tags.export.project`

* Description: Executes the `/sef tags export project` action through the shared policy pipeline.
* Usage: `/sef tags export project`.
* Canonical route: `sef tags export project`.
* Example: `/sef tags export project`.
* Convenience roots: none.
* Category: `fancy_tags`.
* Feature gate: `sef.fancy_tags`.
* Permissions: `sef.commands.tags.export.project`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:tags.export.project`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:tags`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:tags.gc.preview`

* Description: Executes the `/sef tags gc preview` action through the shared policy pipeline.
* Usage: `/sef tags gc preview`.
* Canonical route: `sef tags gc preview`.
* Example: `/sef tags gc preview`.
* Convenience roots: none.
* Category: `fancy_tags`.
* Feature gate: `sef.fancy_tags`.
* Permissions: `sef.commands.tags.gc.preview`.
* Access class: `owner`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:tags.gc.preview`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:tags`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:tags.gc.run`

* Description: Executes the `/sef tags gc run` action through the shared policy pipeline.
* Usage: `/sef tags gc run`.
* Canonical route: `sef tags gc run`.
* Example: `/sef tags gc run`.
* Convenience roots: none.
* Category: `fancy_tags`.
* Feature gate: `sef.fancy_tags`.
* Permissions: `sef.commands.tags.gc.run`.
* Access class: `owner`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:tags.gc.run`.
* Confirmation: not required.
* Audit class: `destructive`.
* GUI descriptor: `sef:tags`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:tags.hide`

* Description: Executes the `/sef tags hide` action through the shared policy pipeline.
* Usage: `/sef tags hide`.
* Canonical route: `sef tags hide`.
* Example: `/sef tags hide`.
* Convenience roots: none.
* Category: `fancy_tags`.
* Feature gate: `sef.fancy_tags`.
* Permissions: `sef.commands.tags.hide`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:tags.hide`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:tags`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:tags.import.approve`

* Description: Executes the `/sef tags import approve` action through the shared policy pipeline.
* Usage: `/sef tags import approve`.
* Canonical route: `sef tags import approve`.
* Example: `/sef tags import approve`.
* Convenience roots: none.
* Category: `fancy_tags`.
* Feature gate: `sef.fancy_tags`.
* Permissions: `sef.commands.tags.import.approve`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:tags.import.approve`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:tags`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:tags.import.inspect`

* Description: Executes the `/sef tags import inspect` action through the shared policy pipeline.
* Usage: `/sef tags import inspect`.
* Canonical route: `sef tags import inspect`.
* Example: `/sef tags import inspect`.
* Convenience roots: none.
* Category: `fancy_tags`.
* Feature gate: `sef.fancy_tags`.
* Permissions: `sef.commands.tags.import.inspect`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:tags.import.inspect`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:tags`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:tags.import.reject`

* Description: Executes the `/sef tags import reject` action through the shared policy pipeline.
* Usage: `/sef tags import reject`.
* Canonical route: `sef tags import reject`.
* Example: `/sef tags import reject`.
* Convenience roots: none.
* Category: `fancy_tags`.
* Feature gate: `sef.fancy_tags`.
* Permissions: `sef.commands.tags.import.reject`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:tags.import.reject`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:tags`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:tags.import.scan`

* Description: Executes the `/sef tags import scan` action through the shared policy pipeline.
* Usage: `/sef tags import scan`.
* Canonical route: `sef tags import scan`.
* Example: `/sef tags import scan`.
* Convenience roots: none.
* Category: `fancy_tags`.
* Feature gate: `sef.fancy_tags`.
* Permissions: `sef.commands.tags.import.scan`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:tags.import.scan`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:tags`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:tags.import.url`

* Description: Executes the `/sef tags import url` action through the shared policy pipeline.
* Usage: `/sef tags import url`.
* Canonical route: `sef tags import url`.
* Example: `/sef tags import url`.
* Convenience roots: none.
* Category: `fancy_tags`.
* Feature gate: `sef.fancy_tags`.
* Permissions: `sef.commands.tags.import.url`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:tags.import.url`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:tags`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:tags.integrity.check`

* Description: Executes the `/sef tags integrity check` action through the shared policy pipeline.
* Usage: `/sef tags integrity check`.
* Canonical route: `sef tags integrity check`.
* Example: `/sef tags integrity check`.
* Convenience roots: none.
* Category: `fancy_tags`.
* Feature gate: `sef.fancy_tags`.
* Permissions: `sef.commands.tags.integrity.check`.
* Access class: `owner`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:tags.integrity.check`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:tags`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:tags.integrity.repair`

* Description: Executes the `/sef tags integrity repair` action through the shared policy pipeline.
* Usage: `/sef tags integrity repair`.
* Canonical route: `sef tags integrity repair`.
* Example: `/sef tags integrity repair`.
* Convenience roots: none.
* Category: `fancy_tags`.
* Feature gate: `sef.fancy_tags`.
* Permissions: `sef.commands.tags.integrity.repair`.
* Access class: `owner`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:tags.integrity.repair`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:tags`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:tags.lease.override`

* Description: Executes the `/sef tags lease release` action through the shared policy pipeline.
* Usage: `/sef tags lease release`.
* Canonical route: `sef tags lease release`.
* Example: `/sef tags lease release`.
* Convenience roots: none.
* Category: `fancy_tags`.
* Feature gate: `sef.fancy_tags`.
* Permissions: `sef.commands.tags.lease.override`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:tags.lease.override`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:tags`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:tags.lease.view`

* Description: Executes the `/sef tags lease status` action through the shared policy pipeline.
* Usage: `/sef tags lease status`.
* Canonical route: `sef tags lease status`.
* Example: `/sef tags lease status`.
* Convenience roots: none.
* Category: `fancy_tags`.
* Feature gate: `sef.fancy_tags`.
* Permissions: `sef.commands.tags.lease.view`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:tags.lease.view`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:tags`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:tags.list`

* Description: Executes the `/sef tags list` action through the shared policy pipeline.
* Usage: `/sef tags list`.
* Canonical route: `sef tags list`.
* Example: `/sef tags list`.
* Convenience roots: none.
* Category: `fancy_tags`.
* Feature gate: `sef.fancy_tags`.
* Permissions: `sef.commands.tags.list`.
* Access class: `player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:tags.list`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:tags`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:tags.moderation.clear`

* Description: Executes the `/sef tags moderation clear` action through the shared policy pipeline.
* Usage: `/sef tags moderation clear`.
* Canonical route: `sef tags moderation clear`.
* Example: `/sef tags moderation clear`.
* Convenience roots: none.
* Category: `fancy_tags`.
* Feature gate: `sef.fancy_tags`.
* Permissions: `sef.commands.tags.moderation.clear`.
* Access class: `owner`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:tags.moderation.clear`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:tags`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:tags.moderation.queue`

* Description: Executes the `/sef tags moderation queue` action through the shared policy pipeline.
* Usage: `/sef tags moderation queue`.
* Canonical route: `sef tags moderation queue`.
* Example: `/sef tags moderation queue`.
* Convenience roots: none.
* Category: `fancy_tags`.
* Feature gate: `sef.fancy_tags`.
* Permissions: `sef.commands.tags.moderation.queue`.
* Access class: `owner`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:tags.moderation.queue`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:tags`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:tags.moderation.suspend`

* Description: Executes the `/sef tags moderation suspend` action through the shared policy pipeline.
* Usage: `/sef tags moderation suspend`.
* Canonical route: `sef tags moderation suspend`.
* Example: `/sef tags moderation suspend`.
* Convenience roots: none.
* Category: `fancy_tags`.
* Feature gate: `sef.fancy_tags`.
* Permissions: `sef.commands.tags.moderation.suspend`.
* Access class: `owner`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:tags.moderation.suspend`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:tags`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:tags.palette.create`

* Description: Executes the `/sef tags palette create` action through the shared policy pipeline.
* Usage: `/sef tags palette create`.
* Canonical route: `sef tags palette create`.
* Example: `/sef tags palette create`.
* Convenience roots: none.
* Category: `fancy_tags`.
* Feature gate: `sef.fancy_tags`.
* Permissions: `sef.commands.tags.palette.create`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:tags.palette.create`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:tags`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:tags.palette.delete`

* Description: Executes the `/sef tags palette delete` action through the shared policy pipeline.
* Usage: `/sef tags palette delete`.
* Canonical route: `sef tags palette delete`.
* Example: `/sef tags palette delete`.
* Convenience roots: none.
* Category: `fancy_tags`.
* Feature gate: `sef.fancy_tags`.
* Permissions: `sef.commands.tags.palette.delete`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:tags.palette.delete`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:tags`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:tags.palette.edit`

* Description: Executes the `/sef tags palette edit` action through the shared policy pipeline.
* Usage: `/sef tags palette edit`.
* Canonical route: `sef tags palette edit`.
* Example: `/sef tags palette edit`.
* Convenience roots: none.
* Category: `fancy_tags`.
* Feature gate: `sef.fancy_tags`.
* Permissions: `sef.commands.tags.palette.edit`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:tags.palette.edit`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:tags`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:tags.palette.list`

* Description: Executes the `/sef tags palette list` action through the shared policy pipeline.
* Usage: `/sef tags palette list`.
* Canonical route: `sef tags palette list`.
* Example: `/sef tags palette list`.
* Convenience roots: none.
* Category: `fancy_tags`.
* Feature gate: `sef.fancy_tags`.
* Permissions: `sef.commands.tags.palette.list`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:tags.palette.list`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:tags`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:tags.publish`

* Description: Executes the `/sef tags publish` action through the shared policy pipeline.
* Usage: `/sef tags publish`.
* Canonical route: `sef tags publish`.
* Example: `/sef tags publish`.
* Convenience roots: none.
* Category: `fancy_tags`.
* Feature gate: `sef.fancy_tags`.
* Permissions: `sef.commands.tags.publish`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:tags.publish`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:tags`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:tags.reload`

* Description: Executes the `/sef tags reload` action through the shared policy pipeline.
* Usage: `/sef tags reload`.
* Canonical route: `sef tags reload`.
* Example: `/sef tags reload`.
* Convenience roots: none.
* Category: `fancy_tags`.
* Feature gate: `sef.fancy_tags`.
* Permissions: `sef.commands.tags.reload`.
* Access class: `owner`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:tags.reload`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:tags`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:tags.report`

* Description: Executes the `/sef tags report` action through the shared policy pipeline.
* Usage: `/sef tags report`.
* Canonical route: `sef tags report`.
* Example: `/sef tags report`.
* Convenience roots: none.
* Category: `fancy_tags`.
* Feature gate: `sef.fancy_tags`.
* Permissions: `sef.commands.tags.report`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:tags.report`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:tags`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:tags.restore`

* Description: Executes the `/sef tags restore` action through the shared policy pipeline.
* Usage: `/sef tags restore`.
* Canonical route: `sef tags restore`.
* Example: `/sef tags restore`.
* Convenience roots: none.
* Category: `fancy_tags`.
* Feature gate: `sef.fancy_tags`.
* Permissions: `sef.commands.tags.restore`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:tags.restore`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:tags`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:tags.revision.list`

* Description: Executes the `/sef tags revision list` action through the shared policy pipeline.
* Usage: `/sef tags revision list`.
* Canonical route: `sef tags revision list`.
* Example: `/sef tags revision list`.
* Convenience roots: none.
* Category: `fancy_tags`.
* Feature gate: `sef.fancy_tags`.
* Permissions: `sef.commands.tags.revision.list`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:tags.revision.list`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:tags`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:tags.revision.restore`

* Description: Executes the `/sef tags revision restore` action through the shared policy pipeline.
* Usage: `/sef tags revision restore`.
* Canonical route: `sef tags revision restore`.
* Example: `/sef tags revision restore`.
* Convenience roots: none.
* Category: `fancy_tags`.
* Feature gate: `sef.fancy_tags`.
* Permissions: `sef.commands.tags.revision.restore`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:tags.revision.restore`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:tags`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:tags.revision.view`

* Description: Executes the `/sef tags revision view` action through the shared policy pipeline.
* Usage: `/sef tags revision view`.
* Canonical route: `sef tags revision view`.
* Example: `/sef tags revision view`.
* Convenience roots: none.
* Category: `fancy_tags`.
* Feature gate: `sef.fancy_tags`.
* Permissions: `sef.commands.tags.revision.view`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:tags.revision.view`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:tags`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:tags.status`

* Description: Executes the `/sef tags status` action through the shared policy pipeline.
* Usage: `/sef tags status`.
* Canonical route: `sef tags status`.
* Example: `/sef tags status`.
* Convenience roots: `fancytags`.
* Category: `fancy_tags`.
* Feature gate: `sef.fancy_tags`.
* Permissions: `sef.commands.tags.status`.
* Access class: `player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:tags.status`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:tags`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:tags.template.create`

* Description: Executes the `/sef tags template create` action through the shared policy pipeline.
* Usage: `/sef tags template create`.
* Canonical route: `sef tags template create`.
* Example: `/sef tags template create`.
* Convenience roots: none.
* Category: `fancy_tags`.
* Feature gate: `sef.fancy_tags`.
* Permissions: `sef.commands.tags.template.create`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:tags.template.create`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:tags`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:tags.template.delete`

* Description: Executes the `/sef tags template delete` action through the shared policy pipeline.
* Usage: `/sef tags template delete`.
* Canonical route: `sef tags template delete`.
* Example: `/sef tags template delete`.
* Convenience roots: none.
* Category: `fancy_tags`.
* Feature gate: `sef.fancy_tags`.
* Permissions: `sef.commands.tags.template.delete`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:tags.template.delete`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:tags`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:tags.template.edit`

* Description: Executes the `/sef tags template edit` action through the shared policy pipeline.
* Usage: `/sef tags template edit`.
* Canonical route: `sef tags template edit`.
* Example: `/sef tags template edit`.
* Convenience roots: none.
* Category: `fancy_tags`.
* Feature gate: `sef.fancy_tags`.
* Permissions: `sef.commands.tags.template.edit`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:tags.template.edit`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:tags`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:tags.template.list`

* Description: Executes the `/sef tags template list` action through the shared policy pipeline.
* Usage: `/sef tags template list`.
* Canonical route: `sef tags template list`.
* Example: `/sef tags template list`.
* Convenience roots: none.
* Category: `fancy_tags`.
* Feature gate: `sef.fancy_tags`.
* Permissions: `sef.commands.tags.template.list`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:tags.template.list`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:tags`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:tags.transfer.status`

* Description: Executes the `/sef tags transfer status` action through the shared policy pipeline.
* Usage: `/sef tags transfer status`.
* Canonical route: `sef tags transfer status`.
* Example: `/sef tags transfer status`.
* Convenience roots: none.
* Category: `fancy_tags`.
* Feature gate: `sef.fancy_tags`.
* Permissions: `sef.commands.tags.transfer.status`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:tags.transfer.status`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:tags`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:tags.unassign`

* Description: Executes the `/sef tags unassign` action through the shared policy pipeline.
* Usage: `/sef tags unassign`.
* Canonical route: `sef tags unassign`.
* Example: `/sef tags unassign`.
* Convenience roots: none.
* Category: `fancy_tags`.
* Feature gate: `sef.fancy_tags`.
* Permissions: `sef.commands.tags.unassign`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:tags.unassign`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:tags`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:tags.validate`

* Description: Executes the `/sef tags validate` action through the shared policy pipeline.
* Usage: `/sef tags validate`.
* Canonical route: `sef tags validate`.
* Example: `/sef tags validate`.
* Convenience roots: none.
* Category: `fancy_tags`.
* Feature gate: `sef.fancy_tags`.
* Permissions: `sef.commands.tags.validate`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:tags.validate`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:tags`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:tags.view`

* Description: Executes the `/sef tags view` action through the shared policy pipeline.
* Usage: `/sef tags view`.
* Canonical route: `sef tags view`.
* Example: `/sef tags view`.
* Convenience roots: none.
* Category: `fancy_tags`.
* Feature gate: `sef.fancy_tags`.
* Permissions: `sef.commands.tags.view`.
* Access class: `player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:tags.view`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:tags`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: true.
* Shared pipeline: required.

### `sef:teleport.back`

* Description: Executes the `/back` action through the shared policy pipeline.
* Usage: `/back`.
* Canonical route: `back`.
* Example: `/back`.
* Convenience roots: `back`.
* Category: `teleports`.
* Feature gate: `sef.teleport.back`.
* Permissions: `sef.commands.back`.
* Access class: `player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `self`.
* Cooldown policy: `sef:teleport.back`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:teleports`.
* HUD contract: teleport state is shown through immediate command feedback.
* Quota contract: teleport collections and target fan out have bounded configuration limits.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:teleport.direct`

* Description: Executes the `/tp` action through the shared policy pipeline.
* Usage: `/tp`.
* Canonical route: `tp`.
* Example: `/tp`.
* Convenience roots: `tp`.
* Category: `teleports`.
* Feature gate: `sef.teleport.direct`.
* Permissions: `sef.commands.tp`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `required_player`.
* Cooldown policy: `sef:teleport.direct`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:teleports`.
* HUD contract: teleport state is shown through immediate command feedback.
* Quota contract: teleport collections and target fan out have bounded configuration limits.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:teleport.direct.all`

* Description: Executes the `/tpall` action through the shared policy pipeline.
* Usage: `/tpall`.
* Canonical route: `tpall`.
* Example: `/tpall`.
* Convenience roots: `tpall`.
* Category: `teleports`.
* Feature gate: `sef.teleport.direct`.
* Permissions: `sef.commands.tpall`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `bounded_players`.
* Cooldown policy: `sef:teleport.direct.all`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:teleports`.
* HUD contract: teleport state is shown through immediate command feedback.
* Quota contract: teleport collections and target fan out have bounded configuration limits.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:teleport.direct.here`

* Description: Executes the `/tphere` action through the shared policy pipeline.
* Usage: `/tphere`.
* Canonical route: `tphere`.
* Example: `/tphere`.
* Convenience roots: `tphere`.
* Category: `teleports`.
* Feature gate: `sef.teleport.direct`.
* Permissions: `sef.commands.tphere`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `required_player`.
* Cooldown policy: `sef:teleport.direct.here`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:teleports`.
* HUD contract: teleport state is shown through immediate command feedback.
* Quota contract: teleport collections and target fan out have bounded configuration limits.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:teleport.direct.offline`

* Description: Executes the `/tpoffline` action through the shared policy pipeline.
* Usage: `/tpoffline`.
* Canonical route: `tpoffline`.
* Example: `/tpoffline`.
* Convenience roots: `tpoffline`.
* Category: `teleports`.
* Feature gate: `sef.teleport.direct`.
* Permissions: `sef.commands.tpoffline`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `required_player`.
* Cooldown policy: `sef:teleport.direct.offline`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:teleports`.
* HUD contract: teleport state is shown through immediate command feedback.
* Quota contract: teleport collections and target fan out have bounded configuration limits.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:teleport.direct.override`

* Description: Executes the `/tpo` action through the shared policy pipeline.
* Usage: `/tpo`.
* Canonical route: `tpo`.
* Example: `/tpo`.
* Convenience roots: `tpo`, `tpohere`.
* Category: `teleports`.
* Feature gate: `sef.teleport.direct`.
* Permissions: `sef.commands.tpo`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `required_player`.
* Cooldown policy: `sef:teleport.direct.override`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:teleports`.
* HUD contract: teleport state is shown through immediate command feedback.
* Quota contract: teleport collections and target fan out have bounded configuration limits.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:teleport.direct.position`

* Description: Executes the `/tppos` action through the shared policy pipeline.
* Usage: `/tppos`.
* Canonical route: `tppos`.
* Example: `/tppos`.
* Convenience roots: `tppos`.
* Category: `teleports`.
* Feature gate: `sef.teleport.direct`.
* Permissions: `sef.commands.tppos`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `self`.
* Cooldown policy: `sef:teleport.direct.position`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:teleports`.
* HUD contract: teleport state is shown through immediate command feedback.
* Quota contract: teleport collections and target fan out have bounded configuration limits.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:teleport.home.admin`

* Description: Executes the `/homeadmin` action through the shared policy pipeline.
* Usage: `/homeadmin`.
* Canonical route: `homeadmin`.
* Example: `/homeadmin`.
* Convenience roots: `homeadmin`.
* Category: `teleports`.
* Feature gate: `sef.teleport.homes`.
* Permissions: `sef.commands.homeadmin`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `required_player`.
* Cooldown policy: `sef:teleport.home.admin`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:teleports`.
* HUD contract: teleport state is shown through immediate command feedback.
* Quota contract: teleport collections and target fan out have bounded configuration limits.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:teleport.home.delete`

* Description: Executes the `/delhome` action through the shared policy pipeline.
* Usage: `/delhome`.
* Canonical route: `delhome`.
* Example: `/delhome`.
* Convenience roots: `delhome`.
* Category: `teleports`.
* Feature gate: `sef.teleport.homes`.
* Permissions: `sef.commands.delhome`.
* Access class: `player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `self`.
* Cooldown policy: `sef:teleport.home.delete`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:teleports`.
* HUD contract: teleport state is shown through immediate command feedback.
* Quota contract: teleport collections and target fan out have bounded configuration limits.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:teleport.home.list`

* Description: Executes the `/homes` action through the shared policy pipeline.
* Usage: `/homes`.
* Canonical route: `homes`.
* Example: `/homes`.
* Convenience roots: `homes`.
* Category: `teleports`.
* Feature gate: `sef.teleport.homes`.
* Permissions: `sef.commands.homes`.
* Access class: `player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:teleport.home.list`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:teleports`.
* HUD contract: teleport state is shown through immediate command feedback.
* Quota contract: teleport collections and target fan out have bounded configuration limits.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:teleport.home.rename`

* Description: Executes the `/renamehome` action through the shared policy pipeline.
* Usage: `/renamehome`.
* Canonical route: `renamehome`.
* Example: `/renamehome`.
* Convenience roots: `renamehome`.
* Category: `teleports`.
* Feature gate: `sef.teleport.homes`.
* Permissions: `sef.commands.renamehome`.
* Access class: `player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `self`.
* Cooldown policy: `sef:teleport.home.rename`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:teleports`.
* HUD contract: teleport state is shown through immediate command feedback.
* Quota contract: teleport collections and target fan out have bounded configuration limits.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:teleport.home.set`

* Description: Executes the `/sethome` action through the shared policy pipeline.
* Usage: `/sethome`.
* Canonical route: `sethome`.
* Example: `/sethome`.
* Convenience roots: `sethome`.
* Category: `teleports`.
* Feature gate: `sef.teleport.homes`.
* Permissions: `sef.commands.sethome`.
* Access class: `player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `self`.
* Cooldown policy: `sef:teleport.home.set`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:teleports`.
* HUD contract: teleport state is shown through immediate command feedback.
* Quota contract: teleport collections and target fan out have bounded configuration limits.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:teleport.home.use`

* Description: Executes the `/home` action through the shared policy pipeline.
* Usage: `/home`.
* Canonical route: `home`.
* Example: `/home`.
* Convenience roots: `home`.
* Category: `teleports`.
* Feature gate: `sef.teleport.homes`.
* Permissions: `sef.commands.home`.
* Access class: `player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `self`.
* Cooldown policy: `sef:teleport.home.use`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:teleports`.
* HUD contract: teleport state is shown through immediate command feedback.
* Quota contract: teleport collections and target fan out have bounded configuration limits.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:teleport.player_warp.create`

* Description: Executes the `/setpwarp` action through the shared policy pipeline.
* Usage: `/setpwarp`.
* Canonical route: `setpwarp`.
* Example: `/setpwarp`.
* Convenience roots: `setpwarp`.
* Category: `teleports`.
* Feature gate: `sef.teleport.player_warps`.
* Permissions: `sef.commands.setpwarp`.
* Access class: `player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `self`.
* Cooldown policy: `sef:teleport.player_warp.create`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:teleports`.
* HUD contract: teleport state is shown through immediate command feedback.
* Quota contract: teleport collections and target fan out have bounded configuration limits.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:teleport.player_warp.delete`

* Description: Executes the `/delpwarp` action through the shared policy pipeline.
* Usage: `/delpwarp`.
* Canonical route: `delpwarp`.
* Example: `/delpwarp`.
* Convenience roots: `delpwarp`.
* Category: `teleports`.
* Feature gate: `sef.teleport.player_warps`.
* Permissions: `sef.commands.delpwarp`.
* Access class: `player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `self`.
* Cooldown policy: `sef:teleport.player_warp.delete`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:teleports`.
* HUD contract: teleport state is shown through immediate command feedback.
* Quota contract: teleport collections and target fan out have bounded configuration limits.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:teleport.player_warp.list`

* Description: Executes the `/pwarps` action through the shared policy pipeline.
* Usage: `/pwarps`.
* Canonical route: `pwarps`.
* Example: `/pwarps`.
* Convenience roots: `playerwarps`, `pwarps`, `pws`.
* Category: `teleports`.
* Feature gate: `sef.teleport.player_warps`.
* Permissions: `sef.commands.pwarps`.
* Access class: `player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:teleport.player_warp.list`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:teleports`.
* HUD contract: teleport state is shown through immediate command feedback.
* Quota contract: teleport collections and target fan out have bounded configuration limits.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:teleport.player_warp.manage`

* Description: Executes the `/pwarp info` action through the shared policy pipeline.
* Usage: `/pwarp info`.
* Canonical route: `pwarp info`.
* Example: `/pwarp info`.
* Convenience roots: none.
* Category: `teleport.player_warps`.
* Feature gate: `sef.teleport.player_warps`.
* Permissions: `sef.playerwarps.edit`.
* Access class: `player`.
* Sources: `player`.
* Target behavior: `self`.
* Cooldown policy: `sef:teleport.player_warp.manage`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:teleports`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:teleport.player_warp.moderate`

* Description: Executes the `/pwarp moderate` action through the shared policy pipeline.
* Usage: `/pwarp moderate`.
* Canonical route: `pwarp moderate`.
* Example: `/pwarp moderate`.
* Convenience roots: none.
* Category: `teleports`.
* Feature gate: `sef.teleport.player_warps`.
* Permissions: `sef.playerwarps.moderate`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `required_player`.
* Cooldown policy: `sef:teleport.player_warp.moderate`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:teleports`.
* HUD contract: teleport state is shown through immediate command feedback.
* Quota contract: teleport collections and target fan out have bounded configuration limits.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:teleport.player_warp.rename`

* Description: Executes the `/renamepwarp` action through the shared policy pipeline.
* Usage: `/renamepwarp`.
* Canonical route: `renamepwarp`.
* Example: `/renamepwarp`.
* Convenience roots: `renamepwarp`.
* Category: `teleports`.
* Feature gate: `sef.teleport.player_warps`.
* Permissions: `sef.commands.renamepwarp`.
* Access class: `player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `self`.
* Cooldown policy: `sef:teleport.player_warp.rename`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:teleports`.
* HUD contract: teleport state is shown through immediate command feedback.
* Quota contract: teleport collections and target fan out have bounded configuration limits.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:teleport.player_warp.use`

* Description: Executes the `/pwarp` action through the shared policy pipeline.
* Usage: `/pwarp`.
* Canonical route: `pwarp`.
* Example: `/pwarp`.
* Convenience roots: `playerwarp`, `pw`, `pwarp`.
* Category: `teleports`.
* Feature gate: `sef.teleport.player_warps`.
* Permissions: `sef.commands.pwarp`.
* Access class: `player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `self`.
* Cooldown policy: `sef:teleport.player_warp.use`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:teleports`.
* HUD contract: teleport state is shown through immediate command feedback.
* Quota contract: teleport collections and target fan out have bounded configuration limits.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:teleport.random`

* Description: Executes the `/rtp` action through the shared policy pipeline.
* Usage: `/rtp`.
* Canonical route: `rtp`.
* Example: `/rtp`.
* Convenience roots: `rtp`, `tpr`.
* Category: `teleports`.
* Feature gate: `sef.teleport.random`.
* Permissions: `sef.commands.rtp`.
* Access class: `player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `self`.
* Cooldown policy: `sef:teleport.random`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:teleports`.
* HUD contract: teleport state is shown through immediate command feedback.
* Quota contract: teleport collections and target fan out have bounded configuration limits.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:teleport.random.set`

* Description: Executes the `/settpr` action through the shared policy pipeline.
* Usage: `/settpr`.
* Canonical route: `settpr`.
* Example: `/settpr`.
* Convenience roots: `settpr`.
* Category: `teleports`.
* Feature gate: `sef.teleport.random`.
* Permissions: `sef.commands.settpr`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `self`.
* Cooldown policy: `sef:teleport.random.set`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:teleports`.
* HUD contract: teleport state is shown through immediate command feedback.
* Quota contract: teleport collections and target fan out have bounded configuration limits.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:teleport.request.accept`

* Description: Executes the `/tpaccept` action through the shared policy pipeline.
* Usage: `/tpaccept`.
* Canonical route: `tpaccept`.
* Example: `/tpaccept`.
* Convenience roots: `tpaccept`.
* Category: `teleports`.
* Feature gate: `sef.teleport.requests`.
* Permissions: `sef.commands.tpaccept`.
* Access class: `player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:teleport.request.accept`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:teleports`.
* HUD contract: teleport state is shown through immediate command feedback.
* Quota contract: teleport collections and target fan out have bounded configuration limits.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:teleport.request.all`

* Description: Executes the `/tpaall` action through the shared policy pipeline.
* Usage: `/tpaall`.
* Canonical route: `tpaall`.
* Example: `/tpaall`.
* Convenience roots: `tpaall`.
* Category: `teleports`.
* Feature gate: `sef.teleport.requests`.
* Permissions: `sef.commands.tpaall`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `bounded_players`.
* Cooldown policy: `sef:teleport.request.all`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:teleports`.
* HUD contract: teleport state is shown through immediate command feedback.
* Quota contract: teleport collections and target fan out have bounded configuration limits.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:teleport.request.auto`

* Description: Executes the `/tpautoaccept` action through the shared policy pipeline.
* Usage: `/tpautoaccept`.
* Canonical route: `tpautoaccept`.
* Example: `/tpautoaccept`.
* Convenience roots: `tpautoaccept`.
* Category: `teleports`.
* Feature gate: `sef.teleport.requests`.
* Permissions: `sef.commands.tpautoaccept`.
* Access class: `trusted_player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `self`.
* Cooldown policy: `sef:teleport.request.auto`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:teleports`.
* HUD contract: teleport state is shown through immediate command feedback.
* Quota contract: teleport collections and target fan out have bounded configuration limits.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:teleport.request.block`

* Description: Executes the `/tpblock` action through the shared policy pipeline.
* Usage: `/tpblock`.
* Canonical route: `tpblock`.
* Example: `/tpblock`.
* Convenience roots: `tpblock`, `tpunblock`.
* Category: `teleports`.
* Feature gate: `sef.teleport.requests`.
* Permissions: `sef.commands.tpblock`.
* Access class: `player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `required_player`.
* Cooldown policy: `sef:teleport.request.block`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:teleports`.
* HUD contract: teleport state is shown through immediate command feedback.
* Quota contract: teleport collections and target fan out have bounded configuration limits.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:teleport.request.blocked`

* Description: Executes the `/tpblocked` action through the shared policy pipeline.
* Usage: `/tpblocked`.
* Canonical route: `tpblocked`.
* Example: `/tpblocked`.
* Convenience roots: `tpblocked`.
* Category: `teleport.requests`.
* Feature gate: `sef.teleport.requests`.
* Permissions: `sef.commands.tpblock`.
* Access class: `player`.
* Sources: `player`.
* Target behavior: `self`.
* Cooldown policy: `sef:teleport.request.blocked`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:teleports`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:teleport.request.cancel`

* Description: Executes the `/tpcancel` action through the shared policy pipeline.
* Usage: `/tpcancel`.
* Canonical route: `tpcancel`.
* Example: `/tpcancel`.
* Convenience roots: `tpcancel`.
* Category: `teleports`.
* Feature gate: `sef.teleport.requests`.
* Permissions: `sef.commands.tpcancel`.
* Access class: `player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:teleport.request.cancel`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:teleports`.
* HUD contract: teleport state is shown through immediate command feedback.
* Quota contract: teleport collections and target fan out have bounded configuration limits.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:teleport.request.deny`

* Description: Executes the `/tpdeny` action through the shared policy pipeline.
* Usage: `/tpdeny`.
* Canonical route: `tpdeny`.
* Example: `/tpdeny`.
* Convenience roots: `tpdeny`.
* Category: `teleports`.
* Feature gate: `sef.teleport.requests`.
* Permissions: `sef.commands.tpdeny`.
* Access class: `player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:teleport.request.deny`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:teleports`.
* HUD contract: teleport state is shown through immediate command feedback.
* Quota contract: teleport collections and target fan out have bounded configuration limits.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:teleport.request.here`

* Description: Executes the `/tpahere` action through the shared policy pipeline.
* Usage: `/tpahere`.
* Canonical route: `tpahere`.
* Example: `/tpahere`.
* Convenience roots: `tpahere`.
* Category: `teleports`.
* Feature gate: `sef.teleport.requests`.
* Permissions: `sef.commands.tpahere`.
* Access class: `player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `required_player`.
* Cooldown policy: `sef:teleport.request.here`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:teleports`.
* HUD contract: teleport state is shown through immediate command feedback.
* Quota contract: teleport collections and target fan out have bounded configuration limits.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:teleport.request.list`

* Description: Executes the `/tprequests` action through the shared policy pipeline.
* Usage: `/tprequests`.
* Canonical route: `tprequests`.
* Example: `/tprequests`.
* Convenience roots: `tprequests`.
* Category: `teleports`.
* Feature gate: `sef.teleport.requests`.
* Permissions: `sef.commands.tprequests`.
* Access class: `player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `self`.
* Cooldown policy: `sef:teleport.request.list`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:teleports`.
* HUD contract: teleport state is shown through immediate command feedback.
* Quota contract: teleport collections and target fan out have bounded configuration limits.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:teleport.request.to`

* Description: Executes the `/tpa` action through the shared policy pipeline.
* Usage: `/tpa`.
* Canonical route: `tpa`.
* Example: `/tpa`.
* Convenience roots: `tpa`.
* Category: `teleports`.
* Feature gate: `sef.teleport.requests`.
* Permissions: `sef.commands.tpa`.
* Access class: `player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `required_player`.
* Cooldown policy: `sef:teleport.request.to`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:teleports`.
* HUD contract: teleport state is shown through immediate command feedback.
* Quota contract: teleport collections and target fan out have bounded configuration limits.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:teleport.request.toggle`

* Description: Executes the `/tptoggle` action through the shared policy pipeline.
* Usage: `/tptoggle`.
* Canonical route: `tptoggle`.
* Example: `/tptoggle`.
* Convenience roots: `tptoggle`.
* Category: `teleports`.
* Feature gate: `sef.teleport.requests`.
* Permissions: `sef.commands.tptoggle`.
* Access class: `player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `self`.
* Cooldown policy: `sef:teleport.request.toggle`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:teleports`.
* HUD contract: teleport state is shown through immediate command feedback.
* Quota contract: teleport collections and target fan out have bounded configuration limits.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:teleport.spawn`

* Description: Executes the `/spawn` action through the shared policy pipeline.
* Usage: `/spawn`.
* Canonical route: `spawn`.
* Example: `/spawn`.
* Convenience roots: `spawn`.
* Category: `teleports`.
* Feature gate: `sef.teleport.spawn`.
* Permissions: `sef.commands.spawn`.
* Access class: `player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `self`.
* Cooldown policy: `sef:teleport.spawn`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:teleports`.
* HUD contract: teleport state is shown through immediate command feedback.
* Quota contract: teleport collections and target fan out have bounded configuration limits.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:teleport.spawn.info`

* Description: Executes the `/spawninfo` action through the shared policy pipeline.
* Usage: `/spawninfo`.
* Canonical route: `spawninfo`.
* Example: `/spawninfo`.
* Convenience roots: `spawninfo`.
* Category: `teleports`.
* Feature gate: `sef.teleport.spawn`.
* Permissions: `sef.commands.spawninfo`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:teleport.spawn.info`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:teleports`.
* HUD contract: teleport state is shown through immediate command feedback.
* Quota contract: teleport collections and target fan out have bounded configuration limits.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:teleport.spawn.set`

* Description: Executes the `/setspawn` action through the shared policy pipeline.
* Usage: `/setspawn`.
* Canonical route: `setspawn`.
* Example: `/setspawn`.
* Convenience roots: `setspawn`.
* Category: `teleports`.
* Feature gate: `sef.teleport.spawn`.
* Permissions: `sef.commands.setspawn`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `self`.
* Cooldown policy: `sef:teleport.spawn.set`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:teleports`.
* HUD contract: teleport state is shown through immediate command feedback.
* Quota contract: teleport collections and target fan out have bounded configuration limits.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:teleport.warp.delete`

* Description: Executes the `/delwarp` action through the shared policy pipeline.
* Usage: `/delwarp`.
* Canonical route: `delwarp`.
* Example: `/delwarp`.
* Convenience roots: `delwarp`.
* Category: `teleports`.
* Feature gate: `sef.teleport.warps`.
* Permissions: `sef.commands.delwarp`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:teleport.warp.delete`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:teleports`.
* HUD contract: teleport state is shown through immediate command feedback.
* Quota contract: teleport collections and target fan out have bounded configuration limits.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:teleport.warp.list`

* Description: Executes the `/warps` action through the shared policy pipeline.
* Usage: `/warps`.
* Canonical route: `warps`.
* Example: `/warps`.
* Convenience roots: `warps`.
* Category: `teleports`.
* Feature gate: `sef.teleport.warps`.
* Permissions: `sef.commands.warps`.
* Access class: `player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:teleport.warp.list`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:teleports`.
* HUD contract: teleport state is shown through immediate command feedback.
* Quota contract: teleport collections and target fan out have bounded configuration limits.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:teleport.warp.manage`

* Description: Executes the `/warpinfo` action through the shared policy pipeline.
* Usage: `/warpinfo`.
* Canonical route: `warpinfo`.
* Example: `/warpinfo`.
* Convenience roots: `warpinfo`.
* Category: `teleports`.
* Feature gate: `sef.teleport.warps`.
* Permissions: `sef.commands.warpinfo`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:teleport.warp.manage`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:teleports`.
* HUD contract: teleport state is shown through immediate command feedback.
* Quota contract: teleport collections and target fan out have bounded configuration limits.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:teleport.warp.rename`

* Description: Executes the `/renamewarp` action through the shared policy pipeline.
* Usage: `/renamewarp`.
* Canonical route: `renamewarp`.
* Example: `/renamewarp`.
* Convenience roots: `renamewarp`.
* Category: `teleports`.
* Feature gate: `sef.teleport.warps`.
* Permissions: `sef.commands.renamewarp`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `none`.
* Cooldown policy: `sef:teleport.warp.rename`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:teleports`.
* HUD contract: teleport state is shown through immediate command feedback.
* Quota contract: teleport collections and target fan out have bounded configuration limits.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:teleport.warp.set`

* Description: Executes the `/setwarp` action through the shared policy pipeline.
* Usage: `/setwarp`.
* Canonical route: `setwarp`.
* Example: `/setwarp`.
* Convenience roots: `setwarp`.
* Category: `teleports`.
* Feature gate: `sef.teleport.warps`.
* Permissions: `sef.commands.setwarp`.
* Access class: `administrator`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `self`.
* Cooldown policy: `sef:teleport.warp.set`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:teleports`.
* HUD contract: teleport state is shown through immediate command feedback.
* Quota contract: teleport collections and target fan out have bounded configuration limits.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:teleport.warp.use`

* Description: Executes the `/warp` action through the shared policy pipeline.
* Usage: `/warp`.
* Canonical route: `warp`.
* Example: `/warp`.
* Convenience roots: `warp`.
* Category: `teleports`.
* Feature gate: `sef.teleport.warps`.
* Permissions: `sef.commands.warp`.
* Access class: `player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `self`.
* Cooldown policy: `sef:teleport.warp.use`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:teleports`.
* HUD contract: teleport state is shown through immediate command feedback.
* Quota contract: teleport collections and target fan out have bounded configuration limits.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:utility.afk`

* Description: Executes the `/afk` action through the shared policy pipeline.
* Usage: `/afk`.
* Canonical route: `afk`.
* Example: `/afk`.
* Convenience roots: `afk`.
* Category: `utilities`.
* Feature gate: `sef.utilities`.
* Permissions: `sef.commands.afk`.
* Access class: `player`.
* Sources: `player`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:utility.afk`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:utilities`.
* HUD contract: `afk`.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:utility.bottom`

* Description: Executes the `/bottom` action through the shared policy pipeline.
* Usage: `/bottom`.
* Canonical route: `bottom`.
* Example: `/bottom`.
* Convenience roots: `bottom`.
* Category: `utilities`.
* Feature gate: `sef.utilities`.
* Permissions: `sef.commands.bottom`.
* Access class: `player`.
* Sources: `player`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:utility.bottom`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:utilities`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:utility.compass`

* Description: Executes the `/compass` action through the shared policy pipeline.
* Usage: `/compass`.
* Canonical route: `compass`.
* Example: `/compass`.
* Convenience roots: `compass`.
* Category: `utilities`.
* Feature gate: `sef.utilities`.
* Permissions: `sef.commands.compass`.
* Access class: `player`.
* Sources: `player`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:utility.compass`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:utilities`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:utility.depth`

* Description: Executes the `/depth` action through the shared policy pipeline.
* Usage: `/depth`.
* Canonical route: `depth`.
* Example: `/depth`.
* Convenience roots: `depth`.
* Category: `utilities`.
* Feature gate: `sef.utilities`.
* Permissions: `sef.commands.depth`.
* Access class: `player`.
* Sources: `player`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:utility.depth`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:utilities`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:utility.exp`

* Description: Executes the `/exp` action through the shared policy pipeline.
* Usage: `/exp`.
* Canonical route: `exp`.
* Example: `/exp`.
* Convenience roots: `exp`.
* Category: `utilities`.
* Feature gate: `sef.utilities`.
* Permissions: `sef.commands.exp`.
* Access class: `player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:utility.exp`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:utilities`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:utility.feed`

* Description: Executes the `/feed` action through the shared policy pipeline.
* Usage: `/feed`.
* Canonical route: `feed`.
* Example: `/feed`.
* Convenience roots: `feed`.
* Category: `utilities`.
* Feature gate: `sef.utilities`.
* Permissions: `sef.commands.feed`.
* Access class: `player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:utility.feed`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:utilities`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:utility.fly`

* Description: Executes the `/fly` action through the shared policy pipeline.
* Usage: `/fly`.
* Canonical route: `fly`.
* Example: `/fly`.
* Convenience roots: `fly`.
* Category: `utilities`.
* Feature gate: `sef.utilities`.
* Permissions: `sef.commands.fly`.
* Access class: `player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:utility.fly`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:utilities`.
* HUD contract: `fly`.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:utility.getpos`

* Description: Executes the `/getpos` action through the shared policy pipeline.
* Usage: `/getpos`.
* Canonical route: `getpos`.
* Example: `/getpos`.
* Convenience roots: `getpos`.
* Category: `utilities`.
* Feature gate: `sef.utilities`.
* Permissions: `sef.commands.getpos`.
* Access class: `player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:utility.getpos`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:utilities`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:utility.god`

* Description: Executes the `/god` action through the shared policy pipeline.
* Usage: `/god`.
* Canonical route: `god`.
* Example: `/god`.
* Convenience roots: `god`.
* Category: `utilities`.
* Feature gate: `sef.utilities`.
* Permissions: `sef.commands.god`.
* Access class: `player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:utility.god`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:utilities`.
* HUD contract: `god`.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:utility.heal`

* Description: Executes the `/heal` action through the shared policy pipeline.
* Usage: `/heal`.
* Canonical route: `heal`.
* Example: `/heal`.
* Convenience roots: `heal`.
* Category: `utilities`.
* Feature gate: `sef.utilities`.
* Permissions: `sef.commands.heal`.
* Access class: `player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:utility.heal`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:utilities`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:utility.jump`

* Description: Executes the `/jump` action through the shared policy pipeline.
* Usage: `/jump`.
* Canonical route: `jump`.
* Example: `/jump`.
* Convenience roots: `jump`.
* Category: `utilities`.
* Feature gate: `sef.utilities`.
* Permissions: `sef.commands.jump`.
* Access class: `player`.
* Sources: `player`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:utility.jump`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:utilities`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:utility.near`

* Description: Executes the `/near` action through the shared policy pipeline.
* Usage: `/near`.
* Canonical route: `near`.
* Example: `/near`.
* Convenience roots: `near`.
* Category: `utilities`.
* Feature gate: `sef.utilities`.
* Permissions: `sef.commands.near`.
* Access class: `player`.
* Sources: `player`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:utility.near`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:utilities`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:utility.ptime`

* Description: Executes the `/ptime` action through the shared policy pipeline.
* Usage: `/ptime`.
* Canonical route: `ptime`.
* Example: `/ptime`.
* Convenience roots: `ptime`.
* Category: `utilities`.
* Feature gate: `sef.utilities`.
* Permissions: `sef.commands.ptime`.
* Access class: `player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:utility.ptime`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:utilities`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:utility.pweather`

* Description: Executes the `/pweather` action through the shared policy pipeline.
* Usage: `/pweather`.
* Canonical route: `pweather`.
* Example: `/pweather`.
* Convenience roots: `pweather`.
* Category: `utilities`.
* Feature gate: `sef.utilities`.
* Permissions: `sef.commands.pweather`.
* Access class: `player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:utility.pweather`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:utilities`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:utility.rest`

* Description: Executes the `/rest` action through the shared policy pipeline.
* Usage: `/rest`.
* Canonical route: `rest`.
* Example: `/rest`.
* Convenience roots: `rest`.
* Category: `utilities`.
* Feature gate: `sef.utilities`.
* Permissions: `sef.commands.rest`.
* Access class: `player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:utility.rest`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:utilities`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:utility.speed`

* Description: Executes the `/speed` action through the shared policy pipeline.
* Usage: `/speed`.
* Canonical route: `speed`.
* Example: `/speed`.
* Convenience roots: `speed`.
* Category: `utilities`.
* Feature gate: `sef.utilities`.
* Permissions: `sef.commands.speed`.
* Access class: `player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:utility.speed`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:utilities`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:utility.suicide`

* Description: Executes the `/suicide` action through the shared policy pipeline.
* Usage: `/suicide`.
* Canonical route: `suicide`.
* Example: `/suicide`.
* Convenience roots: `suicide`.
* Category: `utilities.suicide`.
* Feature gate: `sef.utilities.suicide`.
* Permissions: `sef.commands.suicide`.
* Access class: `player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:utility.suicide`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:utilities`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:utility.top`

* Description: Executes the `/top` action through the shared policy pipeline.
* Usage: `/top`.
* Canonical route: `top`.
* Example: `/top`.
* Convenience roots: `top`.
* Category: `utilities`.
* Feature gate: `sef.utilities`.
* Permissions: `sef.commands.top`.
* Access class: `player`.
* Sources: `player`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:utility.top`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:utilities`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:vanish.manage`

* Description: Executes the `/vanish` action through the shared policy pipeline.
* Usage: `/vanish`.
* Canonical route: `vanish`.
* Example: `/vanish`.
* Convenience roots: `v`, `vanish`.
* Category: `core`.
* Feature gate: `sef.core`.
* Permissions: `sef.vanish.3`.
* Access class: `trusted_player`.
* Sources: `console`, `player`, `rcon`.
* Target behavior: `optional_player`.
* Cooldown policy: `sef:vanish.manage`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:identity`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:workstation.anvil`

* Description: Executes the `/sef workstation anvil` action through the shared policy pipeline.
* Usage: `/sef workstation anvil`.
* Canonical route: `sef workstation anvil`.
* Example: `/sef workstation anvil`.
* Convenience roots: `anvil`, `av`.
* Category: `workstations`.
* Feature gate: `sef.workstation.anvil`.
* Permissions: `sef.commands.anvil`.
* Access class: `player`.
* Sources: `player`.
* Target behavior: `self`.
* Cooldown policy: `sef:workstation.anvil`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:workstations`.
* HUD contract: virtual workstation actions are immediate.
* Quota contract: virtual workstation actions do not retain records or fan out.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:workstation.cartographytable`

* Description: Executes the `/cartographytable` action through the shared policy pipeline.
* Usage: `/cartographytable`.
* Canonical route: `cartographytable`.
* Example: `/cartographytable`.
* Convenience roots: `cartographytable`.
* Category: `workstation.additional`.
* Feature gate: `sef.workstation.additional`.
* Permissions: `sef.commands.cartographytable`.
* Access class: `player`.
* Sources: `player`.
* Target behavior: `self`.
* Cooldown policy: `sef:workstation.cartographytable`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:workstations`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:workstation.craft`

* Description: Executes the `/sef workstation craft` action through the shared policy pipeline.
* Usage: `/sef workstation craft`.
* Canonical route: `sef workstation craft`.
* Example: `/sef workstation craft`.
* Convenience roots: `c`, `craft`.
* Category: `workstations`.
* Feature gate: `sef.workstation.craft`.
* Permissions: `sef.commands.craft`.
* Access class: `player`.
* Sources: `player`.
* Target behavior: `self`.
* Cooldown policy: `sef:workstation.craft`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:workstations`.
* HUD contract: virtual workstation actions are immediate.
* Quota contract: virtual workstation actions do not retain records or fan out.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:workstation.enchant`

* Description: Executes the `/sef workstation enchant` action through the shared policy pipeline.
* Usage: `/sef workstation enchant`.
* Canonical route: `sef workstation enchant`.
* Example: `/sef workstation enchant`.
* Convenience roots: `enchantingtable`, `et`.
* Category: `workstations`.
* Feature gate: `sef.workstation.enchant`.
* Permissions: `sef.commands.enchantingtable`.
* Access class: `player`.
* Sources: `player`.
* Target behavior: `self`.
* Cooldown policy: `sef:workstation.enchant`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:workstations`.
* HUD contract: virtual workstation actions are immediate.
* Quota contract: virtual workstation actions do not retain records or fan out.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:workstation.grindstone`

* Description: Executes the `/grindstone` action through the shared policy pipeline.
* Usage: `/grindstone`.
* Canonical route: `grindstone`.
* Example: `/grindstone`.
* Convenience roots: `grindstone`.
* Category: `workstation.additional`.
* Feature gate: `sef.workstation.additional`.
* Permissions: `sef.commands.grindstone`.
* Access class: `player`.
* Sources: `player`.
* Target behavior: `self`.
* Cooldown policy: `sef:workstation.grindstone`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:workstations`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:workstation.loom`

* Description: Executes the `/loom` action through the shared policy pipeline.
* Usage: `/loom`.
* Canonical route: `loom`.
* Example: `/loom`.
* Convenience roots: `loom`.
* Category: `workstation.additional`.
* Feature gate: `sef.workstation.additional`.
* Permissions: `sef.commands.loom`.
* Access class: `player`.
* Sources: `player`.
* Target behavior: `self`.
* Cooldown policy: `sef:workstation.loom`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:workstations`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:workstation.repair`

* Description: Executes the `/sef workstation repair` action through the shared policy pipeline.
* Usage: `/sef workstation repair`.
* Canonical route: `sef workstation repair`.
* Example: `/sef workstation repair`.
* Convenience roots: `repair`.
* Category: `workstations`.
* Feature gate: `sef.workstation.repair`.
* Permissions: `sef.commands.repair`.
* Access class: `trusted_player`.
* Sources: `player`.
* Target behavior: `self`.
* Cooldown policy: `sef:workstation.repair`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:workstations`.
* HUD contract: virtual workstation actions are immediate.
* Quota contract: virtual workstation actions do not retain records or fan out.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:workstation.smithingtable`

* Description: Executes the `/smithingtable` action through the shared policy pipeline.
* Usage: `/smithingtable`.
* Canonical route: `smithingtable`.
* Example: `/smithingtable`.
* Convenience roots: `smithingtable`.
* Category: `workstation.additional`.
* Feature gate: `sef.workstation.additional`.
* Permissions: `sef.commands.smithingtable`.
* Access class: `player`.
* Sources: `player`.
* Target behavior: `self`.
* Cooldown policy: `sef:workstation.smithingtable`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:workstations`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:workstation.stonecutter`

* Description: Executes the `/stonecutter` action through the shared policy pipeline.
* Usage: `/stonecutter`.
* Canonical route: `stonecutter`.
* Example: `/stonecutter`.
* Convenience roots: `stonecutter`.
* Category: `workstation.additional`.
* Feature gate: `sef.workstation.additional`.
* Permissions: `sef.commands.stonecutter`.
* Access class: `player`.
* Sources: `player`.
* Target behavior: `self`.
* Cooldown policy: `sef:workstation.stonecutter`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:workstations`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:workstation.super_enchant`

* Description: Executes the `/sef workstation super_enchant` action through the shared policy pipeline.
* Usage: `/sef workstation super_enchant`.
* Canonical route: `sef workstation super_enchant`.
* Example: `/sef workstation super_enchant`.
* Convenience roots: `set`, `superenchantingtable`.
* Category: `workstations`.
* Feature gate: `sef.workstation.super_enchant`.
* Permissions: `sef.commands.superenchantingtable`.
* Access class: `trusted_player`.
* Sources: `player`.
* Target behavior: `self`.
* Cooldown policy: `sef:workstation.super_enchant`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:workstations`.
* HUD contract: virtual workstation actions are immediate.
* Quota contract: virtual workstation actions do not retain records or fan out.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

### `sef:workstation.super_enchant.mutate`

* Description: Executes the `/sef workstation super_enchant mutate` action through the shared policy pipeline.
* Usage: `/sef workstation super_enchant mutate`.
* Canonical route: `sef workstation super_enchant mutate`.
* Example: `/sef workstation super_enchant mutate`.
* Convenience roots: none.
* Category: `workstation.super_enchant`.
* Feature gate: `sef.workstation.super_enchant`.
* Permissions: `sef.commands.superenchantingtable`.
* Access class: `trusted_player`.
* Sources: `player`.
* Target behavior: `self`.
* Cooldown policy: `sef:workstation.super_enchant.mutate`.
* Confirmation: not required.
* Audit class: `admin_action`.
* GUI descriptor: `sef:workstations`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `canonical_only`.
* Player facing: false.
* Shared pipeline: required.

### `sef:workstation.workbench`

* Description: Executes the `/workbench` action through the shared policy pipeline.
* Usage: `/workbench`.
* Canonical route: `workbench`.
* Example: `/workbench`.
* Convenience roots: `wb`, `workbench`.
* Category: `workstation.additional`.
* Feature gate: `sef.workstation.additional`.
* Permissions: `sef.commands.workbench`.
* Access class: `player`.
* Sources: `player`.
* Target behavior: `self`.
* Cooldown policy: `sef:workstation.workbench`.
* Confirmation: not required.
* Audit class: `metadata_only`.
* GUI descriptor: `sef:workstations`.
* HUD contract: state is shown through immediate command feedback.
* Quota contract: domain collections and projections have finite hard bounds.
* Conflict policy: `prefer_sef`.
* Player facing: true.
* Shared pipeline: required.

## Shortcuts

| Root | Action | Adapter | Additional permission | Collision policy | Structural revision |
| --- | --- | --- | --- | --- | ---: |
| `ac` | `sef:chat.admin` | `none` | none | `prefer_sef` | 1 |
| `access` | `sef:control.access_applications.view` | `none` | none | `canonical_only` | 1 |
| `activityprofile` | `sef:control.block_activity.view` | `none` | none | `canonical_only` | 1 |
| `adminjournal` | `sef:control.admin_journal.view` | `none` | none | `canonical_only` | 1 |
| `admission` | `sef:control.admission.view` | `none` | none | `canonical_only` | 1 |
| `afk` | `sef:utility.afk` | `none` | none | `prefer_sef` | 1 |
| `afkzone` | `sef:control.afk_zones.view` | `none` | none | `canonical_only` | 1 |
| `anomaly` | `sef:control.command_anomaly.view` | `none` | none | `canonical_only` | 1 |
| `ans` | `sef:chat.reply` | `none` | none | `prefer_sef` | 1 |
| `anvil` | `sef:workstation.anvil` | `none` | none | `prefer_sef` | 1 |
| `appeal` | `sef:control.appeals.view` | `none` | none | `canonical_only` | 1 |
| `appeals` | `sef:control.appeals.view` | `none` | none | `canonical_only` | 1 |
| `applications` | `sef:control.access_applications.view` | `none` | none | `canonical_only` | 1 |
| `approvals` | `sef:approval.list` | `none` | none | `prefer_sef` | 1 |
| `auction` | `sef:control.auctions.view` | `none` | none | `canonical_only` | 1 |
| `auctionadmin` | `sef:control.auctions.view` | `none` | none | `canonical_only` | 1 |
| `auctions` | `sef:control.auctions.view` | `none` | none | `canonical_only` | 1 |
| `automod` | `sef:control.automod.view` | `none` | none | `canonical_only` | 1 |
| `av` | `sef:workstation.anvil` | `none` | none | `prefer_sef` | 1 |
| `back` | `sef:teleport.back` | `none` | none | `prefer_sef` | 1 |
| `backup` | `sef:control.backups.view` | `none` | none | `canonical_only` | 1 |
| `bal` | `sef:economy.balance` | `none` | none | `prefer_sef` | 1 |
| `balance` | `sef:economy.balance` | `none` | none | `prefer_sef` | 1 |
| `balancetop` | `sef:economy.top` | `none` | none | `prefer_sef` | 1 |
| `baltop` | `sef:economy.top` | `none` | none | `prefer_sef` | 1 |
| `ban` | `sef:moderation.ban` | `none` | none | `prefer_sef` | 1 |
| `ban-ip` | `sef:moderation.ban_ip` | `none` | none | `prefer_sef` | 1 |
| `banip` | `sef:moderation.ban_ip` | `none` | none | `prefer_sef` | 1 |
| `banned` | `sef:banned.list` | `none` | none | `prefer_sef` | 1 |
| `block` | `sef:control.interaction_blocks.view` | `none` | none | `canonical_only` | 1 |
| `blocks` | `sef:control.interaction_blocks.view` | `none` | none | `canonical_only` | 1 |
| `book` | `sef:inventory.book` | `none` | none | `prefer_sef` | 1 |
| `booktext` | `sef:social.text` | `none` | none | `prefer_sef` | 1 |
| `borderprofile` | `sef:control.world_border.view` | `none` | none | `canonical_only` | 1 |
| `bossbars` | `sef:control.display_profiles.view` | `none` | none | `canonical_only` | 1 |
| `bottom` | `sef:utility.bottom` | `none` | none | `prefer_sef` | 1 |
| `c` | `sef:workstation.craft` | `none` | none | `prefer_sef` | 1 |
| `calendar` | `sef:control.server_calendar.view` | `none` | none | `canonical_only` | 1 |
| `cartographytable` | `sef:workstation.cartographytable` | `none` | none | `prefer_sef` | 1 |
| `cc` | `sef:chat.clear` | `none` | none | `prefer_sef` | 1 |
| `changewindow` | `sef:control.change_windows.view` | `none` | none | `canonical_only` | 1 |
| `channel` | `sef:control.chat_channels.view` | `none` | none | `canonical_only` | 1 |
| `channels` | `sef:control.chat_channels.view` | `none` | none | `canonical_only` | 1 |
| `chat` | `sef:chat.admin` | `none` | none | `prefer_sef` | 1 |
| `chatcontrol` | `sef:control.chat_control.view` | `none` | none | `canonical_only` | 1 |
| `checkalts` | `sef:identity.alts` | `none` | none | `prefer_sef` | 1 |
| `chunktickets` | `sef:control.chunk_tickets.view` | `none` | none | `canonical_only` | 1 |
| `ci` | `sef:inventory.clear` | `none` | none | `prefer_sef` | 1 |
| `cleanup` | `sef:control.cleanup.view` | `none` | none | `canonical_only` | 1 |
| `clearchat` | `sef:chat.clear` | `none` | none | `prefer_sef` | 1 |
| `clearinventory` | `sef:inventory.clear` | `none` | none | `prefer_sef` | 1 |
| `clearwarnings` | `sef:moderation.clearwarnings` | `none` | none | `prefer_sef` | 1 |
| `colors` | `sef:core.colors` | `none` | `sef.commands.colors` | `canonical_only` | 1 |
| `commandannouncement` | `sef:announcement.command` | `none` | none | `prefer_sef` | 1 |
| `commandspy` | `sef:commandspy.toggle` | `none` | none | `prefer_sef` | 1 |
| `compass` | `sef:utility.compass` | `none` | none | `prefer_sef` | 1 |
| `condense` | `sef:inventory.condense` | `none` | none | `prefer_sef` | 1 |
| `countdown` | `sef:announcement.countdown` | `none` | none | `prefer_sef` | 1 |
| `craft` | `sef:workstation.craft` | `none` | none | `prefer_sef` | 1 |
| `createkit` | `sef:kit.create` | `none` | none | `prefer_sef` | 1 |
| `customtext` | `sef:social.text` | `none` | none | `prefer_sef` | 1 |
| `dability` | `sef:disguise.ability` | `none` | none | `prefer_sef` | 1 |
| `daily` | `sef:control.daily_rewards.view` | `none` | none | `canonical_only` | 1 |
| `datapacks` | `sef:control.datapacks.view` | `none` | none | `canonical_only` | 1 |
| `db` | `sef:moderation.disablebuilding` | `none` | none | `prefer_sef` | 1 |
| `deathcompass` | `sef:control.death_compass.view` | `none` | none | `canonical_only` | 1 |
| `deathlocation` | `sef:control.death_compass.view` | `none` | none | `canonical_only` | 1 |
| `delhome` | `sef:teleport.home.delete` | `none` | none | `prefer_sef` | 1 |
| `deljail` | `sef:moderation.deljail` | `none` | none | `prefer_sef` | 1 |
| `delkit` | `sef:kit.delete` | `none` | none | `prefer_sef` | 1 |
| `delpwarp` | `sef:teleport.player_warp.delete` | `none` | none | `prefer_sef` | 1 |
| `delwarp` | `sef:teleport.warp.delete` | `none` | none | `prefer_sef` | 1 |
| `depth` | `sef:utility.depth` | `none` | none | `prefer_sef` | 1 |
| `disablebuilding` | `sef:moderation.disablebuilding` | `none` | none | `prefer_sef` | 1 |
| `discipline` | `sef:control.discipline.view` | `none` | none | `canonical_only` | 1 |
| `disguise` | `sef:disguise.set.mob` | `none` | none | `prefer_sef` | 1 |
| `displayprofile` | `sef:control.display_profiles.view` | `none` | none | `canonical_only` | 1 |
| `disposal` | `sef:inventory.disposal` | `none` | none | `prefer_sef` | 1 |
| `drift` | `sef:control.config_drift.view` | `none` | none | `canonical_only` | 1 |
| `ec` | `sef:inventory.enderchest` | `none` | none | `prefer_sef` | 1 |
| `enchant` | `sef:enchant.apply` | `none` | none | `prefer_sef` | 1 |
| `enchantingtable` | `sef:workstation.enchant` | `none` | none | `prefer_sef` | 1 |
| `enderchest` | `sef:inventory.enderchest` | `none` | none | `prefer_sef` | 1 |
| `et` | `sef:workstation.enchant` | `none` | none | `prefer_sef` | 1 |
| `event` | `sef:control.community_events.view` | `none` | none | `canonical_only` | 1 |
| `eventadmin` | `sef:control.community_events.view` | `none` | none | `canonical_only` | 1 |
| `events` | `sef:control.community_events.view` | `none` | none | `canonical_only` | 1 |
| `evidence` | `sef:control.evidence.view` | `none` | none | `canonical_only` | 1 |
| `exp` | `sef:utility.exp` | `none` | none | `prefer_sef` | 1 |
| `fakejoin` | `sef:fake.join` | `none` | none | `prefer_sef` | 1 |
| `fakeleave` | `sef:fake.leave` | `none` | none | `prefer_sef` | 1 |
| `fakemessage` | `sef:fake.message` | `none` | none | `prefer_sef` | 1 |
| `fakerankmessage` | `sef:fake.rank_message` | `none` | none | `prefer_sef` | 1 |
| `fancytags` | `sef:tags.status` | `none` | none | `canonical_only` | 1 |
| `featuregraph` | `sef:control.dependency_graph.view` | `none` | none | `canonical_only` | 1 |
| `feed` | `sef:utility.feed` | `none` | none | `prefer_sef` | 1 |
| `fly` | `sef:utility.fly` | `none` | none | `prefer_sef` | 1 |
| `freeze` | `sef:moderation.freeze` | `none` | none | `prefer_sef` | 1 |
| `freezelist` | `sef:moderation.freezelist` | `none` | none | `prefer_sef` | 1 |
| `friend` | `sef:control.friends.view` | `none` | none | `canonical_only` | 1 |
| `friends` | `sef:control.friends.view` | `none` | none | `canonical_only` | 1 |
| `getpos` | `sef:utility.getpos` | `none` | none | `prefer_sef` | 1 |
| `give` | `sef:item.give.others` | `none` | none | `prefer_sef` | 1 |
| `gm` | `sef:gamemode.set` | `none` | none | `prefer_sef` | 1 |
| `gma` | `sef:gamemode.adventure` | `none` | none | `prefer_sef` | 1 |
| `gmc` | `sef:gamemode.creative` | `none` | none | `prefer_sef` | 1 |
| `gms` | `sef:gamemode.survival` | `none` | none | `prefer_sef` | 1 |
| `gmsp` | `sef:gamemode.spectator` | `none` | none | `prefer_sef` | 1 |
| `god` | `sef:utility.god` | `none` | none | `prefer_sef` | 1 |
| `governor` | `sef:control.resource_governor.view` | `none` | none | `canonical_only` | 1 |
| `grave` | `sef:control.graves.view` | `none` | none | `canonical_only` | 1 |
| `graves` | `sef:control.graves.view` | `none` | none | `canonical_only` | 1 |
| `grindstone` | `sef:workstation.grindstone` | `none` | none | `prefer_sef` | 1 |
| `guardrail` | `sef:control.guardrails.view` | `none` | none | `canonical_only` | 1 |
| `guide` | `sef:control.knowledge.view` | `none` | none | `canonical_only` | 1 |
| `guideadmin` | `sef:control.knowledge.view` | `none` | none | `canonical_only` | 1 |
| `hat` | `sef:inventory.hat` | `none` | none | `prefer_sef` | 1 |
| `heal` | `sef:utility.heal` | `none` | none | `prefer_sef` | 1 |
| `helpop` | `sef:chat.helpop` | `none` | none | `prefer_sef` | 1 |
| `helpopop` | `sef:chat.helpop.reply` | `none` | none | `prefer_sef` | 1 |
| `home` | `sef:teleport.home.use` | `none` | none | `prefer_sef` | 1 |
| `homeadmin` | `sef:teleport.home.admin` | `none` | none | `prefer_sef` | 1 |
| `homes` | `sef:teleport.home.list` | `none` | none | `prefer_sef` | 1 |
| `i` | `sef:item.give.self` | `none` | none | `prefer_sef` | 1 |
| `ignore` | `sef:social.ignore` | `none` | none | `prefer_sef` | 1 |
| `ignorelist` | `sef:social.ignore` | `none` | none | `prefer_sef` | 1 |
| `impactpreview` | `sef:control.player_impact.view` | `none` | none | `canonical_only` | 1 |
| `incident` | `sef:control.incidents.view` | `none` | none | `canonical_only` | 1 |
| `info` | `sef:social.text` | `none` | none | `prefer_sef` | 1 |
| `inventoryhistory` | `sef:control.inventory_recovery.view` | `none` | none | `canonical_only` | 1 |
| `inventoryrestore` | `sef:control.inventory_recovery.manage` | `none` | none | `canonical_only` | 1 |
| `invites` | `sef:control.invites.view` | `none` | none | `canonical_only` | 1 |
| `invlock` | `sef:moderation.invlock` | `none` | none | `prefer_sef` | 1 |
| `invsee` | `sef:inventory.view` | `none` | none | `prefer_sef` | 1 |
| `itemdb` | `sef:inventory.itemdb` | `none` | none | `prefer_sef` | 1 |
| `itemlore` | `sef:inventory.itemlore` | `none` | none | `prefer_sef` | 1 |
| `itemname` | `sef:inventory.itemname` | `none` | none | `prefer_sef` | 1 |
| `jail` | `sef:moderation.jail` | `none` | none | `prefer_sef` | 1 |
| `jailedplayers` | `sef:moderation.jailedplayers` | `none` | none | `prefer_sef` | 1 |
| `jails` | `sef:moderation.jails` | `none` | none | `prefer_sef` | 1 |
| `joinmessage` | `sef:social.connection` | `none` | none | `prefer_sef` | 1 |
| `jump` | `sef:utility.jump` | `none` | none | `prefer_sef` | 1 |
| `kick` | `sef:moderation.kick` | `none` | none | `prefer_sef` | 1 |
| `kick-ip` | `sef:moderation.kick_ip` | `none` | none | `prefer_sef` | 1 |
| `kickall` | `sef:moderation.kick_all` | `none` | none | `prefer_sef` | 1 |
| `kickip` | `sef:moderation.kick_ip` | `none` | none | `prefer_sef` | 1 |
| `kickme` | `sef:moderation.kick_self` | `none` | none | `prefer_sef` | 1 |
| `kit` | `sef:kit.claim` | `none` | none | `prefer_sef` | 1 |
| `kitreset` | `sef:kit.reset` | `none` | none | `prefer_sef` | 1 |
| `kits` | `sef:kit.list` | `none` | none | `prefer_sef` | 1 |
| `knowledge` | `sef:control.knowledge.view` | `none` | none | `canonical_only` | 1 |
| `leavemessage` | `sef:social.connection` | `none` | none | `prefer_sef` | 1 |
| `loggerspy` | `sef:logging.status` | `none` | none | `prefer_existing` | 1 |
| `loom` | `sef:workstation.loom` | `none` | none | `prefer_sef` | 1 |
| `lostfound` | `sef:control.lost_found.view` | `none` | none | `canonical_only` | 1 |
| `lostfoundadmin` | `sef:control.lost_found.view` | `none` | none | `canonical_only` | 1 |
| `mail` | `sef:social.mail` | `none` | none | `prefer_sef` | 1 |
| `maintenance` | `sef:control.maintenance.view` | `none` | none | `canonical_only` | 1 |
| `mentions` | `sef:control.mentions.view` | `none` | none | `canonical_only` | 1 |
| `modhealth` | `sef:control.mod_health.view` | `none` | none | `canonical_only` | 1 |
| `money` | `sef:economy.balance` | `none` | none | `prefer_sef` | 1 |
| `more` | `sef:inventory.more` | `none` | none | `prefer_sef` | 1 |
| `msg` | `sef:social.message` | `none` | none | `prefer_sef` | 1 |
| `msgtoggle` | `sef:social.message.toggle` | `none` | none | `prefer_sef` | 1 |
| `mute` | `sef:moderation.mute` | `none` | none | `prefer_sef` | 1 |
| `mutelist` | `sef:moderation.mutelist` | `none` | none | `prefer_sef` | 1 |
| `mydata` | `sef:control.privacy.view` | `none` | none | `canonical_only` | 1 |
| `near` | `sef:utility.near` | `none` | none | `prefer_sef` | 1 |
| `nick` | `sef:identity.nick` | `none` | none | `prefer_sef` | 1 |
| `nickfor` | `sef:identity.nick.others` | `none` | none | `prefer_sef` | 1 |
| `onboarding` | `sef:control.onboarding.view` | `none` | none | `canonical_only` | 1 |
| `opbulletin` | `sef:announcement.bulletin` | `none` | none | `prefer_sef` | 1 |
| `parcel` | `sef:control.parcels.view` | `none` | none | `canonical_only` | 1 |
| `parceladmin` | `sef:control.parcels.view` | `none` | none | `canonical_only` | 1 |
| `parcels` | `sef:control.parcels.view` | `none` | none | `canonical_only` | 1 |
| `pardon` | `sef:moderation.pardon` | `none` | none | `prefer_sef` | 1 |
| `pardon-ip` | `sef:moderation.pardon_ip` | `none` | none | `prefer_sef` | 1 |
| `pay` | `sef:economy.pay` | `none` | none | `prefer_sef` | 1 |
| `payconfirmtoggle` | `sef:economy.pay.confirm` | `none` | none | `prefer_sef` | 1 |
| `paytoggle` | `sef:economy.pay.toggle` | `none` | none | `prefer_sef` | 1 |
| `pchat` | `sef:social.message` | `none` | none | `prefer_sef` | 1 |
| `performance` | `sef:control.performance.view` | `none` | none | `canonical_only` | 1 |
| `permissionimpact` | `sef:control.permission_impact.view` | `none` | none | `canonical_only` | 1 |
| `playerwarp` | `sef:teleport.player_warp.use` | `none` | none | `prefer_sef` | 1 |
| `playerwarps` | `sef:teleport.player_warp.list` | `none` | none | `prefer_sef` | 1 |
| `playtimerewards` | `sef:control.playtime_rewards.view` | `none` | none | `canonical_only` | 1 |
| `policylab` | `sef:control.policy_lab.view` | `none` | none | `canonical_only` | 1 |
| `poll` | `sef:control.polls.view` | `none` | none | `canonical_only` | 1 |
| `polladmin` | `sef:control.polls.view` | `none` | none | `canonical_only` | 1 |
| `polls` | `sef:control.polls.view` | `none` | none | `canonical_only` | 1 |
| `portal` | `sef:control.portal_policy.view` | `none` | none | `canonical_only` | 1 |
| `pregen` | `sef:control.chunk_pregen.view` | `none` | none | `canonical_only` | 1 |
| `privacy` | `sef:control.privacy.view` | `none` | none | `canonical_only` | 1 |
| `privacycenter` | `sef:control.privacy.view` | `none` | none | `canonical_only` | 1 |
| `privacyrequests` | `sef:control.privacy.view` | `none` | none | `canonical_only` | 1 |
| `ptime` | `sef:utility.ptime` | `none` | none | `prefer_sef` | 1 |
| `pw` | `sef:teleport.player_warp.use` | `none` | none | `prefer_sef` | 1 |
| `pwarp` | `sef:teleport.player_warp.use` | `none` | none | `prefer_sef` | 1 |
| `pwarps` | `sef:teleport.player_warp.list` | `none` | none | `prefer_sef` | 1 |
| `pweather` | `sef:utility.pweather` | `none` | none | `prefer_sef` | 1 |
| `pws` | `sef:teleport.player_warp.list` | `none` | none | `prefer_sef` | 1 |
| `quarantine` | `sef:control.session_quarantine.view` | `none` | none | `canonical_only` | 1 |
| `queue` | `sef:control.queue.view` | `none` | none | `canonical_only` | 1 |
| `r` | `sef:social.message` | `none` | none | `prefer_sef` | 1 |
| `recipe` | `sef:inventory.recipe` | `none` | none | `prefer_sef` | 1 |
| `reminder` | `sef:social.reminder` | `none` | none | `prefer_sef` | 1 |
| `reminders` | `sef:social.reminder` | `none` | none | `prefer_sef` | 1 |
| `renamehome` | `sef:teleport.home.rename` | `none` | none | `prefer_sef` | 1 |
| `renamepwarp` | `sef:teleport.player_warp.rename` | `none` | none | `prefer_sef` | 1 |
| `renamewarp` | `sef:teleport.warp.rename` | `none` | none | `prefer_sef` | 1 |
| `repair` | `sef:workstation.repair` | `none` | none | `prefer_sef` | 1 |
| `reply` | `sef:social.message` | `none` | none | `prefer_sef` | 1 |
| `report` | `sef:control.reports.view` | `none` | none | `canonical_only` | 1 |
| `reports` | `sef:control.reports.view` | `none` | none | `canonical_only` | 1 |
| `resourcepack` | `sef:control.resource_packs.view` | `none` | none | `canonical_only` | 1 |
| `resourceworld` | `sef:control.resource_worlds.view` | `none` | none | `canonical_only` | 1 |
| `rest` | `sef:utility.rest` | `none` | none | `prefer_sef` | 1 |
| `restart` | `sef:control.restart_coordinator.view` | `none` | none | `canonical_only` | 1 |
| `rewards` | `sef:control.playtime_rewards.view` | `none` | none | `canonical_only` | 1 |
| `rollout` | `sef:control.rollouts.view` | `none` | none | `canonical_only` | 1 |
| `rtoggle` | `sef:social.reply.toggle` | `none` | none | `prefer_sef` | 1 |
| `rtp` | `sef:teleport.random` | `none` | none | `prefer_sef` | 1 |
| `rules` | `sef:social.text` | `none` | none | `prefer_sef` | 1 |
| `run` | `sef:run.server` | `none` | none | `prefer_sef` | 1 |
| `sell` | `sef:economy.sell` | `none` | none | `prefer_sef` | 1 |
| `serverpresentation` | `sef:control.server_presentation.view` | `none` | none | `canonical_only` | 1 |
| `set` | `sef:workstation.super_enchant` | `none` | none | `prefer_sef` | 1 |
| `sethome` | `sef:teleport.home.set` | `none` | none | `prefer_sef` | 1 |
| `setjail` | `sef:moderation.setjail` | `none` | none | `prefer_sef` | 1 |
| `setpwarp` | `sef:teleport.player_warp.create` | `none` | none | `prefer_sef` | 1 |
| `setspawn` | `sef:teleport.spawn.set` | `none` | none | `prefer_sef` | 1 |
| `settpr` | `sef:teleport.random.set` | `none` | none | `prefer_sef` | 1 |
| `setwarp` | `sef:teleport.warp.set` | `none` | none | `prefer_sef` | 1 |
| `setworth` | `sef:economy.worth.set` | `none` | none | `prefer_sef` | 1 |
| `shortcut` | `sef:control.alias_diagnostics.view` | `none` | none | `canonical_only` | 1 |
| `showkit` | `sef:kit.show` | `none` | none | `prefer_sef` | 1 |
| `sidebar` | `sef:control.display_profiles.view` | `none` | none | `canonical_only` | 1 |
| `silent` | `sef:silent.actor` | `none` | none | `prefer_sef` | 1 |
| `sleepvote` | `sef:control.sleep_vote.view` | `none` | none | `canonical_only` | 1 |
| `smithingtable` | `sef:workstation.smithingtable` | `none` | none | `prefer_sef` | 1 |
| `socialspy` | `sef:social.spy` | `none` | none | `prefer_sef` | 1 |
| `spawn` | `sef:teleport.spawn` | `none` | none | `prefer_sef` | 1 |
| `spawninfo` | `sef:teleport.spawn.info` | `none` | none | `prefer_sef` | 1 |
| `spawnpolicy` | `sef:control.spawn_ecology.view` | `none` | none | `canonical_only` | 1 |
| `speed` | `sef:utility.speed` | `none` | none | `prefer_sef` | 1 |
| `staffduty` | `sef:control.staff_duty.view` | `none` | none | `canonical_only` | 1 |
| `staffnote` | `sef:control.staff_notes.view` | `none` | none | `canonical_only` | 1 |
| `staffnotes` | `sef:control.staff_notes.view` | `none` | none | `canonical_only` | 1 |
| `staffshift` | `sef:control.staff_duty.view` | `none` | none | `canonical_only` | 1 |
| `statesnapshot` | `sef:control.operational_snapshots.view` | `none` | none | `canonical_only` | 1 |
| `stonecutter` | `sef:workstation.stonecutter` | `none` | none | `prefer_sef` | 1 |
| `sudo` | `sef:sudo.run` | `none` | none | `prefer_sef` | 1 |
| `suicide` | `sef:utility.suicide` | `none` | none | `prefer_sef` | 1 |
| `superenchantingtable` | `sef:workstation.super_enchant` | `none` | none | `prefer_sef` | 1 |
| `tell` | `sef:social.message` | `none` | none | `prefer_sef` | 1 |
| `tempban` | `sef:moderation.tempban` | `none` | none | `prefer_sef` | 1 |
| `tempban-ip` | `sef:moderation.tempban_ip` | `none` | none | `prefer_sef` | 1 |
| `tempbanip` | `sef:moderation.tempban_ip` | `none` | none | `prefer_sef` | 1 |
| `textannouncement` | `sef:announcement.text` | `none` | none | `prefer_sef` | 1 |
| `ticket` | `sef:control.tickets.view` | `none` | none | `canonical_only` | 1 |
| `tickets` | `sef:control.tickets.view` | `none` | none | `canonical_only` | 1 |
| `titleannouncement` | `sef:announcement.title` | `none` | none | `prefer_sef` | 1 |
| `toggle` | `sef:announcement.toggle` | `none` | none | `prefer_sef` | 1 |
| `top` | `sef:utility.top` | `none` | none | `prefer_sef` | 1 |
| `tp` | `sef:teleport.direct` | `none` | none | `prefer_sef` | 1 |
| `tpa` | `sef:teleport.request.to` | `none` | none | `prefer_sef` | 1 |
| `tpaall` | `sef:teleport.request.all` | `none` | none | `prefer_sef` | 1 |
| `tpaccept` | `sef:teleport.request.accept` | `none` | none | `prefer_sef` | 1 |
| `tpahere` | `sef:teleport.request.here` | `none` | none | `prefer_sef` | 1 |
| `tpall` | `sef:teleport.direct.all` | `none` | none | `prefer_sef` | 1 |
| `tpautoaccept` | `sef:teleport.request.auto` | `none` | none | `prefer_sef` | 1 |
| `tpblock` | `sef:teleport.request.block` | `none` | none | `prefer_sef` | 1 |
| `tpblocked` | `sef:teleport.request.blocked` | `none` | none | `prefer_sef` | 1 |
| `tpcancel` | `sef:teleport.request.cancel` | `none` | none | `prefer_sef` | 1 |
| `tpdeny` | `sef:teleport.request.deny` | `none` | none | `prefer_sef` | 1 |
| `tphere` | `sef:teleport.direct.here` | `none` | none | `prefer_sef` | 1 |
| `tpo` | `sef:teleport.direct.override` | `none` | none | `prefer_sef` | 1 |
| `tpoffline` | `sef:teleport.direct.offline` | `none` | none | `prefer_sef` | 1 |
| `tpohere` | `sef:teleport.direct.override` | `none` | none | `prefer_sef` | 1 |
| `tppos` | `sef:teleport.direct.position` | `none` | none | `prefer_sef` | 1 |
| `tpr` | `sef:teleport.random` | `none` | none | `prefer_sef` | 1 |
| `tprequests` | `sef:teleport.request.list` | `none` | none | `prefer_sef` | 1 |
| `tptoggle` | `sef:teleport.request.toggle` | `none` | none | `prefer_sef` | 1 |
| `tpunblock` | `sef:teleport.request.block` | `none` | none | `prefer_sef` | 1 |
| `trade` | `sef:control.trades.view` | `none` | none | `canonical_only` | 1 |
| `tradeadmin` | `sef:control.trades.view` | `none` | none | `canonical_only` | 1 |
| `trades` | `sef:control.trades.view` | `none` | none | `canonical_only` | 1 |
| `trust` | `sef:control.friends.view` | `none` | none | `canonical_only` | 1 |
| `unban` | `sef:moderation.pardon` | `none` | none | `prefer_sef` | 1 |
| `unban-ip` | `sef:moderation.pardon_ip` | `none` | none | `prefer_sef` | 1 |
| `unbanip` | `sef:moderation.pardon_ip` | `none` | none | `prefer_sef` | 1 |
| `unblock` | `sef:control.interaction_blocks.view` | `none` | none | `canonical_only` | 1 |
| `undisguise` | `sef:disguise.clear` | `none` | none | `prefer_sef` | 1 |
| `unfreeze` | `sef:moderation.unfreeze` | `none` | none | `prefer_sef` | 1 |
| `unjail` | `sef:moderation.unjail` | `none` | none | `prefer_sef` | 1 |
| `unmute` | `sef:moderation.unmute` | `none` | none | `prefer_sef` | 1 |
| `untrust` | `sef:control.friends.view` | `none` | none | `canonical_only` | 1 |
| `v` | `sef:vanish.manage` | `none` | none | `prefer_sef` | 1 |
| `vanish` | `sef:vanish.manage` | `none` | none | `prefer_sef` | 1 |
| `w` | `sef:social.message` | `none` | none | `prefer_sef` | 1 |
| `warn` | `sef:moderation.warn` | `none` | none | `prefer_sef` | 1 |
| `warns` | `sef:moderation.warns` | `none` | none | `prefer_sef` | 1 |
| `warp` | `sef:teleport.warp.use` | `none` | none | `prefer_sef` | 1 |
| `warpinfo` | `sef:teleport.warp.manage` | `none` | none | `prefer_sef` | 1 |
| `warps` | `sef:teleport.warp.list` | `none` | none | `prefer_sef` | 1 |
| `waypoint` | `sef:control.waypoints.view` | `none` | none | `canonical_only` | 1 |
| `waypoints` | `sef:control.waypoints.view` | `none` | none | `canonical_only` | 1 |
| `wb` | `sef:workstation.workbench` | `none` | none | `prefer_sef` | 1 |
| `weekly` | `sef:control.weekly_rewards.view` | `none` | none | `canonical_only` | 1 |
| `welcome` | `sef:social.reminder` | `none` | none | `prefer_sef` | 1 |
| `whisper` | `sef:social.message` | `none` | none | `prefer_sef` | 1 |
| `whois` | `sef:identity.whois` | `none` | none | `prefer_sef` | 1 |
| `workbench` | `sef:workstation.workbench` | `none` | none | `prefer_sef` | 1 |
| `worldpolicy` | `sef:control.world_policy.view` | `none` | none | `canonical_only` | 1 |
| `worth` | `sef:economy.worth` | `none` | none | `prefer_sef` | 1 |

## GUI descriptors

### `sef:aliases`

* Title: Aliases.
* Permission: `sef.kernel.gui.use`.
* Fallback route: `sef commands`.
* Fallback usage: `/sef commands`.
| Control | Action | Permission | Icon | Target policy | Execution context | Audience | Maximum targets |
| --- | --- | --- | --- | --- | --- | --- | ---: |

### `sef:control`

* Title: Control.
* Permission: `sef.kernel.gui.use`.
* Fallback route: `sef control catalog`.
* Fallback usage: `/sef control catalog`.
| Control | Action | Permission | Icon | Target policy | Execution context | Audience | Maximum targets |
| --- | --- | --- | --- | --- | --- | --- | ---: |

### `sef:core`

* Title: Core.
* Permission: `sef.kernel.gui.use`.
* Fallback route: `sef commands`.
* Fallback usage: `/sef commands`.
| Control | Action | Permission | Icon | Target policy | Execution context | Audience | Maximum targets |
| --- | --- | --- | --- | --- | --- | --- | ---: |

### `sef:economy`

* Title: Economy.
* Permission: `sef.kernel.gui.use`.
* Fallback route: `balance`.
* Fallback usage: `/balance`.
| Control | Action | Permission | Icon | Target policy | Execution context | Audience | Maximum targets |
| --- | --- | --- | --- | --- | --- | --- | ---: |

### `sef:gui`

* Title: Gui.
* Permission: `sef.kernel.gui.use`.
* Fallback route: `sef dashboard`.
* Fallback usage: `/sef dashboard`.
| Control | Action | Permission | Icon | Target policy | Execution context | Audience | Maximum targets |
| --- | --- | --- | --- | --- | --- | --- | ---: |
| `help` | `sef:core.commands` | `sef.commands.sef.commands` | `minecraft:knowledge_book` | `self` | `actor` | `self` | 1 |
| `homes` | `sef:teleport.home.list` | `sef.commands.homes` | `minecraft:red_bed` | `self` | `actor` | `self` | 1 |
| `requests` | `sef:teleport.request.list` | `sef.commands.tprequests` | `minecraft:paper` | `self` | `actor` | `self` | 1 |
| `warps` | `sef:teleport.warp.list` | `sef.commands.warps` | `minecraft:ender_pearl` | `none` | `actor` | `self` | 1 |

### `sef:gui.help`

* Title: Gui help.
* Permission: `sef.commands.sef.commands`.
* Fallback route: `sef commands`.
* Fallback usage: `/sef commands`.
| Control | Action | Permission | Icon | Target policy | Execution context | Audience | Maximum targets |
| --- | --- | --- | --- | --- | --- | --- | ---: |

### `sef:gui.homes`

* Title: Gui homes.
* Permission: `sef.commands.homes`.
* Fallback route: `homes`.
* Fallback usage: `/homes`.
| Control | Action | Permission | Icon | Target policy | Execution context | Audience | Maximum targets |
| --- | --- | --- | --- | --- | --- | --- | ---: |
| `home` | `sef:teleport.home.use` | `sef.commands.home` | `minecraft:red_bed` | `self` | `actor` | `self` | 1 |

### `sef:gui.players`

* Title: Gui players.
* Permission: `sef.kernel.panel.use`.
* Fallback route: `list`.
* Fallback usage: `/list`.
| Control | Action | Permission | Icon | Target policy | Execution context | Audience | Maximum targets |
| --- | --- | --- | --- | --- | --- | --- | ---: |

### `sef:gui.staff`

* Title: Gui staff.
* Permission: `sef.kernel.panel.use`.
* Fallback route: `sef doctor`.
* Fallback usage: `/sef doctor`.
| Control | Action | Permission | Icon | Target policy | Execution context | Audience | Maximum targets |
| --- | --- | --- | --- | --- | --- | --- | ---: |

### `sef:gui.teleport_requests`

* Title: Gui teleport requests.
* Permission: `sef.commands.tprequests`.
* Fallback route: `tprequests`.
* Fallback usage: `/tprequests`.
| Control | Action | Permission | Icon | Target policy | Execution context | Audience | Maximum targets |
| --- | --- | --- | --- | --- | --- | --- | ---: |
| `accept` | `sef:teleport.request.accept` | `sef.commands.tpaccept` | `minecraft:lime_dye` | `selected_visible_player` | `target_actor` | `selected_visible_players` | 1 |
| `cancel` | `sef:teleport.request.cancel` | `sef.commands.tpcancel` | `minecraft:barrier` | `selected_visible_player` | `target_actor` | `selected_visible_players` | 1 |
| `deny` | `sef:teleport.request.deny` | `sef.commands.tpdeny` | `minecraft:red_dye` | `selected_visible_player` | `target_actor` | `selected_visible_players` | 1 |

### `sef:gui.warps`

* Title: Gui warps.
* Permission: `sef.commands.warps`.
* Fallback route: `warps`.
* Fallback usage: `/warps`.
| Control | Action | Permission | Icon | Target policy | Execution context | Audience | Maximum targets |
| --- | --- | --- | --- | --- | --- | --- | ---: |
| `player_warp` | `sef:teleport.player_warp.use` | `sef.commands.pwarp` | `minecraft:lodestone` | `self` | `actor` | `self` | 1 |
| `warp` | `sef:teleport.warp.use` | `sef.commands.warp` | `minecraft:ender_pearl` | `self` | `actor` | `self` | 1 |

### `sef:identity`

* Title: Identity.
* Permission: `sef.kernel.gui.use`.
* Fallback route: `nick`.
* Fallback usage: `/nick`.
| Control | Action | Permission | Icon | Target policy | Execution context | Audience | Maximum targets |
| --- | --- | --- | --- | --- | --- | --- | ---: |

### `sef:integrations`

* Title: Integrations.
* Permission: `sef.kernel.gui.use`.
* Fallback route: `sef doctor`.
* Fallback usage: `/sef doctor`.
| Control | Action | Permission | Icon | Target policy | Execution context | Audience | Maximum targets |
| --- | --- | --- | --- | --- | --- | --- | ---: |

### `sef:inventory`

* Title: Inventory.
* Permission: `sef.kernel.gui.use`.
* Fallback route: `itemdb`.
* Fallback usage: `/itemdb`.
| Control | Action | Permission | Icon | Target policy | Execution context | Audience | Maximum targets |
| --- | --- | --- | --- | --- | --- | --- | ---: |

### `sef:kits`

* Title: Kits.
* Permission: `sef.kernel.gui.use`.
* Fallback route: `kits`.
* Fallback usage: `/kits`.
| Control | Action | Permission | Icon | Target policy | Execution context | Audience | Maximum targets |
| --- | --- | --- | --- | --- | --- | --- | ---: |

### `sef:moderation`

* Title: Moderation.
* Permission: `sef.kernel.gui.use`.
* Fallback route: `warns`.
* Fallback usage: `/warns`.
| Control | Action | Permission | Icon | Target policy | Execution context | Audience | Maximum targets |
| --- | --- | --- | --- | --- | --- | --- | ---: |

### `sef:observation`

* Title: Observation.
* Permission: `sef.kernel.gui.use`.
* Fallback route: `sef commandspy status`.
* Fallback usage: `/sef commandspy status`.
| Control | Action | Permission | Icon | Target policy | Execution context | Audience | Maximum targets |
| --- | --- | --- | --- | --- | --- | --- | ---: |

### `sef:panels`

* Title: Panels.
* Permission: `sef.kernel.gui.use`.
* Fallback route: `sef panel list`.
* Fallback usage: `/sef panel list`.
| Control | Action | Permission | Icon | Target policy | Execution context | Audience | Maximum targets |
| --- | --- | --- | --- | --- | --- | --- | ---: |

### `sef:protection`

* Title: Protection.
* Permission: `sef.kernel.gui.use`.
* Fallback route: `sef commands`.
* Fallback usage: `/sef commands`.
| Control | Action | Permission | Icon | Target policy | Execution context | Audience | Maximum targets |
| --- | --- | --- | --- | --- | --- | --- | ---: |

### `sef:settings`

* Title: Settings.
* Permission: `sef.kernel.gui.use`.
* Fallback route: `sef doctor`.
* Fallback usage: `/sef doctor`.
| Control | Action | Permission | Icon | Target policy | Execution context | Audience | Maximum targets |
| --- | --- | --- | --- | --- | --- | --- | ---: |

### `sef:social`

* Title: Social.
* Permission: `sef.kernel.gui.use`.
* Fallback route: `msg`.
* Fallback usage: `/msg`.
| Control | Action | Permission | Icon | Target policy | Execution context | Audience | Maximum targets |
| --- | --- | --- | --- | --- | --- | --- | ---: |

### `sef:tags`

* Title: Tags.
* Permission: `sef.kernel.gui.use`.
* Fallback route: `sef commands`.
* Fallback usage: `/sef commands`.
| Control | Action | Permission | Icon | Target policy | Execution context | Audience | Maximum targets |
| --- | --- | --- | --- | --- | --- | --- | ---: |

### `sef:teleports`

* Title: Teleports.
* Permission: `sef.kernel.gui.use`.
* Fallback route: `homes`.
* Fallback usage: `/homes`.
| Control | Action | Permission | Icon | Target policy | Execution context | Audience | Maximum targets |
| --- | --- | --- | --- | --- | --- | --- | ---: |

### `sef:utilities`

* Title: Utilities.
* Permission: `sef.kernel.gui.use`.
* Fallback route: `getpos`.
* Fallback usage: `/getpos`.
| Control | Action | Permission | Icon | Target policy | Execution context | Audience | Maximum targets |
| --- | --- | --- | --- | --- | --- | --- | ---: |

### `sef:workstations`

* Title: Workstations.
* Permission: `sef.kernel.gui.use`.
* Fallback route: `sef commands`.
* Fallback usage: `/sef commands`.
| Control | Action | Permission | Icon | Target policy | Execution context | Audience | Maximum targets |
| --- | --- | --- | --- | --- | --- | --- | ---: |

## Command only GUI descriptors
