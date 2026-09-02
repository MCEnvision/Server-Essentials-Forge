# SEF modular configuration reference

This reference is generated from the runtime schema registry. Cooldown durations are controlled only by `sef.cooldown.<action>.<seconds>` permissions.

## admin_panels

Controls admin panels behavior, presentation, bounds, storage, and diagnostics.

File: `config/sef/modules/admin_panels.toml`. Schema version: `1`. Documentation version: `2`. Apply class: `live`. Dependencies: `audit`, `commands`, `core`, `gui`, `messages`, `permissions`. Conflicts: none.

Inspect permission: `sef.commands.config.inspect`. Edit permission: `sef.commands.config.edit`. Migration path: inspect the retained legacy `common.toml` with `/sef config migrate dryrun`, then request a revision-bound confirmation with `/sef config migrate apply <expected_revision>`. Legacy cooldown durations move to `sef.cooldown.<action>.<seconds>` permissions instead of module files.

| Setting | Description | Type | Default | Bounds | Unit | Apply class | Privacy | Example |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `module.enabled` | Enables the module after dependency validation. | boolean | `true` | `true` or `false` | boolean flag | live | public | `module.enabled = true` |
| `module.disabled_behavior` | Defines truthful behavior while this module is disabled. | enum | `command_unavailable` | `command_unavailable`, `provider_fallback`, `read_only` | named option | live | public | `module.disabled_behavior = "command_unavailable"` |
| `gui.mode` | Selects enhanced GUI presentation without changing command access. | enum | `inherit` | `command_only`, `gui_preferred`, `inherit`, `off`, `on` | named option | live | public | `gui.mode = "inherit"` |
| `gui.bare_command_opens` | Allows eligible bare command routes to prefer a dedicated workflow. | boolean | `true` | `true` or `false` | boolean flag | live | public | `gui.bare_command_opens = true` |
| `gui.default_page` | Selects the stable default workflow page. | string | `overview` | 1 through 64 characters, pattern `[a-z0-9_]+` | text | live | public | `gui.default_page = "overview"` |
| `limits.maximum_records` | Bounds persisted records owned by this module. | integer | `1000` | 1 through 100000 | count | live | public | `limits.maximum_records = 1000` |
| `limits.maximum_page_size` | Bounds command and GUI result pages. | integer | `45` | 4 through 100 | count | live | public | `limits.maximum_page_size = 45` |
| `rate.maximum_actions_per_minute` | Bounds admission attempts independently of permission cooldown duration. | integer | `30` | 1 through 10000 | actions per minute | live | public | `rate.maximum_actions_per_minute = 30` |
| `storage.retention_days` | Defines bounded data retention where the module owns persistent records. | integer | `30` | 0 through 3650 | days | live | public | `storage.retention_days = 30` |
| `failure.mode` | Defines provider and validation failure behavior. | enum | `fail_closed` | `fail_closed`, `previous_known_good`, `read_only` | named option | live | public | `failure.mode = "fail_closed"` |
| `audit.class` | Defines the minimum audit class for module actions. | enum | `admin_action` | `admin_action`, `destructive`, `metadata_only`, `none` | named option | live | public | `audit.class = "admin_action"` |
| `audit.redaction` | Defines the module redaction class. | enum | `standard` | `security_critical`, `sensitive`, `standard` | named option | live | public | `audit.redaction = "standard"` |

## aliases

Controls aliases behavior, presentation, bounds, storage, and diagnostics.

File: `config/sef/modules/aliases.toml`. Schema version: `1`. Documentation version: `2`. Apply class: `live`. Dependencies: `audit`, `commands`, `core`, `messages`, `permissions`. Conflicts: none.

Inspect permission: `sef.commands.config.inspect`. Edit permission: `sef.commands.config.edit`. Migration path: inspect the retained legacy `common.toml` with `/sef config migrate dryrun`, then request a revision-bound confirmation with `/sef config migrate apply <expected_revision>`. Legacy cooldown durations move to `sef.cooldown.<action>.<seconds>` permissions instead of module files.

| Setting | Description | Type | Default | Bounds | Unit | Apply class | Privacy | Example |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `module.enabled` | Enables the module after dependency validation. | boolean | `true` | `true` or `false` | boolean flag | live | public | `module.enabled = true` |
| `module.disabled_behavior` | Defines truthful behavior while this module is disabled. | enum | `command_unavailable` | `command_unavailable`, `provider_fallback`, `read_only` | named option | live | public | `module.disabled_behavior = "command_unavailable"` |
| `gui.mode` | Selects enhanced GUI presentation without changing command access. | enum | `inherit` | `command_only`, `gui_preferred`, `inherit`, `off`, `on` | named option | live | public | `gui.mode = "inherit"` |
| `gui.bare_command_opens` | Allows eligible bare command routes to prefer a dedicated workflow. | boolean | `true` | `true` or `false` | boolean flag | live | public | `gui.bare_command_opens = true` |
| `gui.default_page` | Selects the stable default workflow page. | string | `overview` | 1 through 64 characters, pattern `[a-z0-9_]+` | text | live | public | `gui.default_page = "overview"` |
| `limits.maximum_records` | Bounds persisted records owned by this module. | integer | `1000` | 1 through 100000 | count | live | public | `limits.maximum_records = 1000` |
| `limits.maximum_page_size` | Bounds command and GUI result pages. | integer | `45` | 4 through 100 | count | live | public | `limits.maximum_page_size = 45` |
| `rate.maximum_actions_per_minute` | Bounds admission attempts independently of permission cooldown duration. | integer | `30` | 1 through 10000 | actions per minute | live | public | `rate.maximum_actions_per_minute = 30` |
| `storage.retention_days` | Defines bounded data retention where the module owns persistent records. | integer | `30` | 0 through 3650 | days | live | public | `storage.retention_days = 30` |
| `failure.mode` | Defines provider and validation failure behavior. | enum | `fail_closed` | `fail_closed`, `previous_known_good`, `read_only` | named option | live | public | `failure.mode = "fail_closed"` |
| `audit.class` | Defines the minimum audit class for module actions. | enum | `admin_action` | `admin_action`, `destructive`, `metadata_only`, `none` | named option | live | public | `audit.class = "admin_action"` |
| `audit.redaction` | Defines the module redaction class. | enum | `standard` | `security_critical`, `sensitive`, `standard` | named option | live | public | `audit.redaction = "standard"` |
| `runtime.kernel_maximum_aliases` | Maximum operator alias definitions | integer | `256` | 1 through 1024 | count | restart_required | public | `runtime.kernel_maximum_aliases = 256` |

## anvil

Controls anvil behavior, presentation, bounds, storage, and diagnostics.

File: `config/sef/modules/anvil.toml`. Schema version: `1`. Documentation version: `2`. Apply class: `live`. Dependencies: `commands`, `core`, `gui`, `messages`, `permissions`. Conflicts: none.

Inspect permission: `sef.commands.config.inspect`. Edit permission: `sef.commands.config.edit`. Migration path: inspect the retained legacy `common.toml` with `/sef config migrate dryrun`, then request a revision-bound confirmation with `/sef config migrate apply <expected_revision>`. Legacy cooldown durations move to `sef.cooldown.<action>.<seconds>` permissions instead of module files.

| Setting | Description | Type | Default | Bounds | Unit | Apply class | Privacy | Example |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `module.enabled` | Enables the module after dependency validation. | boolean | `true` | `true` or `false` | boolean flag | live | public | `module.enabled = true` |
| `module.disabled_behavior` | Defines truthful behavior while this module is disabled. | enum | `command_unavailable` | `command_unavailable`, `provider_fallback`, `read_only` | named option | live | public | `module.disabled_behavior = "command_unavailable"` |
| `gui.mode` | Selects enhanced GUI presentation without changing command access. | enum | `inherit` | `command_only`, `gui_preferred`, `inherit`, `off`, `on` | named option | live | public | `gui.mode = "inherit"` |
| `gui.bare_command_opens` | Allows eligible bare command routes to prefer a dedicated workflow. | boolean | `true` | `true` or `false` | boolean flag | live | public | `gui.bare_command_opens = true` |
| `gui.default_page` | Selects the stable default workflow page. | string | `overview` | 1 through 64 characters, pattern `[a-z0-9_]+` | text | live | public | `gui.default_page = "overview"` |
| `limits.maximum_records` | Bounds persisted records owned by this module. | integer | `10000` | 1 through 1000000 | count | live | public | `limits.maximum_records = 10000` |
| `limits.maximum_page_size` | Bounds command and GUI result pages. | integer | `45` | 4 through 100 | count | live | public | `limits.maximum_page_size = 45` |
| `rate.maximum_actions_per_minute` | Bounds admission attempts independently of permission cooldown duration. | integer | `300` | 1 through 10000 | actions per minute | live | public | `rate.maximum_actions_per_minute = 300` |
| `storage.retention_days` | Defines bounded data retention where the module owns persistent records. | integer | `30` | 0 through 3650 | days | live | public | `storage.retention_days = 30` |
| `failure.mode` | Defines provider and validation failure behavior. | enum | `previous_known_good` | `fail_closed`, `previous_known_good`, `read_only` | named option | live | public | `failure.mode = "previous_known_good"` |
| `audit.class` | Defines the minimum audit class for module actions. | enum | `metadata_only` | `admin_action`, `destructive`, `metadata_only`, `none` | named option | live | public | `audit.class = "metadata_only"` |
| `audit.redaction` | Defines the module redaction class. | enum | `standard` | `security_critical`, `sensitive`, `standard` | named option | live | public | `audit.redaction = "standard"` |
| `runtime.enable_anvil_alias` | Enable the /av alias for /anvil | boolean | `true` | `true` or `false` | boolean flag | restart_required | public | `runtime.enable_anvil_alias = true` |
| `runtime.enable_anvil_command` | Virtual anvil (/anvil, /av) | boolean | `true` | `true` or `false` | boolean flag | restart_required | public | `runtime.enable_anvil_command = true` |

## audit

Controls audit behavior, presentation, bounds, storage, and diagnostics.

File: `config/sef/modules/audit.toml`. Schema version: `1`. Documentation version: `2`. Apply class: `live`. Dependencies: `core`. Conflicts: none.

Inspect permission: `sef.commands.config.inspect`. Edit permission: `sef.commands.config.edit`. Migration path: inspect the retained legacy `common.toml` with `/sef config migrate dryrun`, then request a revision-bound confirmation with `/sef config migrate apply <expected_revision>`. Legacy cooldown durations move to `sef.cooldown.<action>.<seconds>` permissions instead of module files.

| Setting | Description | Type | Default | Bounds | Unit | Apply class | Privacy | Example |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `module.enabled` | Enables the module after dependency validation. | boolean | `true` | `true` or `false` | boolean flag | live | public | `module.enabled = true` |
| `module.disabled_behavior` | Defines truthful behavior while this module is disabled. | enum | `command_unavailable` | `command_unavailable`, `provider_fallback`, `read_only` | named option | live | public | `module.disabled_behavior = "command_unavailable"` |
| `gui.mode` | Selects enhanced GUI presentation without changing command access. | enum | `inherit` | `command_only`, `gui_preferred`, `inherit`, `off`, `on` | named option | live | public | `gui.mode = "inherit"` |
| `gui.bare_command_opens` | Allows eligible bare command routes to prefer a dedicated workflow. | boolean | `false` | `true` or `false` | boolean flag | live | public | `gui.bare_command_opens = false` |
| `gui.default_page` | Selects the stable default workflow page. | string | `overview` | 1 through 64 characters, pattern `[a-z0-9_]+` | text | live | public | `gui.default_page = "overview"` |
| `limits.maximum_records` | Bounds persisted records owned by this module. | integer | `1000` | 1 through 100000 | count | live | public | `limits.maximum_records = 1000` |
| `limits.maximum_page_size` | Bounds command and GUI result pages. | integer | `45` | 4 through 100 | count | live | public | `limits.maximum_page_size = 45` |
| `rate.maximum_actions_per_minute` | Bounds admission attempts independently of permission cooldown duration. | integer | `300` | 1 through 10000 | actions per minute | live | public | `rate.maximum_actions_per_minute = 300` |
| `storage.retention_days` | Defines bounded data retention where the module owns persistent records. | integer | `90` | 0 through 3650 | days | live | public | `storage.retention_days = 90` |
| `failure.mode` | Defines provider and validation failure behavior. | enum | `previous_known_good` | `fail_closed`, `previous_known_good`, `read_only` | named option | live | public | `failure.mode = "previous_known_good"` |
| `audit.class` | Defines the minimum audit class for module actions. | enum | `metadata_only` | `admin_action`, `destructive`, `metadata_only`, `none` | named option | live | public | `audit.class = "metadata_only"` |
| `audit.redaction` | Defines the module redaction class. | enum | `sensitive` | `security_critical`, `sensitive`, `standard` | named option | live | public | `audit.redaction = "sensitive"` |
| `runtime.security_audit_maximum_file_mi_b` | Maximum size of the active structured security audit file before rotation | integer | `16` | 1 through 1024 | count | restart_required | sensitive | `runtime.security_audit_maximum_file_mi_b = 16` |
| `runtime.security_audit_retention_days` | Days to retain structured SEF security audit files | integer | `30` | 1 through 3650 | days | restart_required | sensitive | `runtime.security_audit_retention_days = 30` |

## back

Controls back behavior, presentation, bounds, storage, and diagnostics.

File: `config/sef/modules/back.toml`. Schema version: `1`. Documentation version: `2`. Apply class: `live`. Dependencies: `commands`, `core`, `gui`, `messages`, `permissions`. Conflicts: none.

Inspect permission: `sef.commands.config.inspect`. Edit permission: `sef.commands.config.edit`. Migration path: inspect the retained legacy `common.toml` with `/sef config migrate dryrun`, then request a revision-bound confirmation with `/sef config migrate apply <expected_revision>`. Legacy cooldown durations move to `sef.cooldown.<action>.<seconds>` permissions instead of module files.

| Setting | Description | Type | Default | Bounds | Unit | Apply class | Privacy | Example |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `module.enabled` | Enables the module after dependency validation. | boolean | `true` | `true` or `false` | boolean flag | live | public | `module.enabled = true` |
| `module.disabled_behavior` | Defines truthful behavior while this module is disabled. | enum | `command_unavailable` | `command_unavailable`, `provider_fallback`, `read_only` | named option | live | public | `module.disabled_behavior = "command_unavailable"` |
| `gui.mode` | Selects enhanced GUI presentation without changing command access. | enum | `inherit` | `command_only`, `gui_preferred`, `inherit`, `off`, `on` | named option | live | public | `gui.mode = "inherit"` |
| `gui.bare_command_opens` | Allows eligible bare command routes to prefer a dedicated workflow. | boolean | `true` | `true` or `false` | boolean flag | live | public | `gui.bare_command_opens = true` |
| `gui.default_page` | Selects the stable default workflow page. | string | `overview` | 1 through 64 characters, pattern `[a-z0-9_]+` | text | live | public | `gui.default_page = "overview"` |
| `limits.maximum_records` | Bounds persisted records owned by this module. | integer | `10000` | 1 through 1000000 | count | live | public | `limits.maximum_records = 10000` |
| `limits.maximum_page_size` | Bounds command and GUI result pages. | integer | `45` | 4 through 100 | count | live | public | `limits.maximum_page_size = 45` |
| `rate.maximum_actions_per_minute` | Bounds admission attempts independently of permission cooldown duration. | integer | `300` | 1 through 10000 | actions per minute | live | public | `rate.maximum_actions_per_minute = 300` |
| `storage.retention_days` | Defines bounded data retention where the module owns persistent records. | integer | `30` | 0 through 3650 | days | live | public | `storage.retention_days = 30` |
| `failure.mode` | Defines provider and validation failure behavior. | enum | `previous_known_good` | `fail_closed`, `previous_known_good`, `read_only` | named option | live | public | `failure.mode = "previous_known_good"` |
| `audit.class` | Defines the minimum audit class for module actions. | enum | `metadata_only` | `admin_action`, `destructive`, `metadata_only`, `none` | named option | live | public | `audit.class = "metadata_only"` |
| `audit.redaction` | Defines the module redaction class. | enum | `standard` | `security_critical`, `sensitive`, `standard` | named option | live | public | `audit.redaction = "standard"` |
| `runtime.enable_back` | Back command and location history | boolean | `true` | `true` or `false` | boolean flag | restart_required | public | `runtime.enable_back = true` |

## backups

Controls backups behavior, presentation, bounds, storage, and diagnostics.

File: `config/sef/modules/backups.toml`. Schema version: `1`. Documentation version: `2`. Apply class: `live`. Dependencies: `audit`, `commands`, `core`, `messages`, `permissions`. Conflicts: none.

Inspect permission: `sef.commands.config.inspect`. Edit permission: `sef.commands.config.edit`. Migration path: inspect the retained legacy `common.toml` with `/sef config migrate dryrun`, then request a revision-bound confirmation with `/sef config migrate apply <expected_revision>`. Legacy cooldown durations move to `sef.cooldown.<action>.<seconds>` permissions instead of module files.

| Setting | Description | Type | Default | Bounds | Unit | Apply class | Privacy | Example |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `module.enabled` | Enables the module after dependency validation. | boolean | `true` | `true` or `false` | boolean flag | live | public | `module.enabled = true` |
| `module.disabled_behavior` | Defines truthful behavior while this module is disabled. | enum | `command_unavailable` | `command_unavailable`, `provider_fallback`, `read_only` | named option | live | public | `module.disabled_behavior = "command_unavailable"` |
| `gui.mode` | Selects enhanced GUI presentation without changing command access. | enum | `inherit` | `command_only`, `gui_preferred`, `inherit`, `off`, `on` | named option | live | public | `gui.mode = "inherit"` |
| `gui.bare_command_opens` | Allows eligible bare command routes to prefer a dedicated workflow. | boolean | `true` | `true` or `false` | boolean flag | live | public | `gui.bare_command_opens = true` |
| `gui.default_page` | Selects the stable default workflow page. | string | `overview` | 1 through 64 characters, pattern `[a-z0-9_]+` | text | live | public | `gui.default_page = "overview"` |
| `limits.maximum_records` | Bounds persisted records owned by this module. | integer | `1000` | 1 through 100000 | count | live | public | `limits.maximum_records = 1000` |
| `limits.maximum_page_size` | Bounds command and GUI result pages. | integer | `45` | 4 through 100 | count | live | public | `limits.maximum_page_size = 45` |
| `rate.maximum_actions_per_minute` | Bounds admission attempts independently of permission cooldown duration. | integer | `30` | 1 through 10000 | actions per minute | live | public | `rate.maximum_actions_per_minute = 30` |
| `storage.retention_days` | Defines bounded data retention where the module owns persistent records. | integer | `90` | 0 through 3650 | days | live | public | `storage.retention_days = 90` |
| `failure.mode` | Defines provider and validation failure behavior. | enum | `fail_closed` | `fail_closed`, `previous_known_good`, `read_only` | named option | live | public | `failure.mode = "fail_closed"` |
| `audit.class` | Defines the minimum audit class for module actions. | enum | `admin_action` | `admin_action`, `destructive`, `metadata_only`, `none` | named option | live | public | `audit.class = "admin_action"` |
| `audit.redaction` | Defines the module redaction class. | enum | `sensitive` | `security_critical`, `sensitive`, `standard` | named option | live | public | `audit.redaction = "sensitive"` |
| `verification.required` | Requires a verified checkpoint before a backup reports success. | boolean | `true` | `true` or `false` | boolean flag | live | public | `verification.required = true` |
| `verification.maximum_minutes` | Bounds provider checkpoint verification. | integer | `30` | 1 through 240 | minutes | live | public | `verification.maximum_minutes = 30` |

## bans

Controls bans behavior, presentation, bounds, storage, and diagnostics.

File: `config/sef/modules/bans.toml`. Schema version: `1`. Documentation version: `2`. Apply class: `live`. Dependencies: `audit`, `commands`, `core`, `gui`, `messages`, `permissions`. Conflicts: none.

Inspect permission: `sef.commands.config.inspect`. Edit permission: `sef.commands.config.edit`. Migration path: inspect the retained legacy `common.toml` with `/sef config migrate dryrun`, then request a revision-bound confirmation with `/sef config migrate apply <expected_revision>`. Legacy cooldown durations move to `sef.cooldown.<action>.<seconds>` permissions instead of module files.

| Setting | Description | Type | Default | Bounds | Unit | Apply class | Privacy | Example |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `module.enabled` | Enables the module after dependency validation. | boolean | `true` | `true` or `false` | boolean flag | live | public | `module.enabled = true` |
| `module.disabled_behavior` | Defines truthful behavior while this module is disabled. | enum | `command_unavailable` | `command_unavailable`, `provider_fallback`, `read_only` | named option | live | public | `module.disabled_behavior = "command_unavailable"` |
| `gui.mode` | Selects enhanced GUI presentation without changing command access. | enum | `inherit` | `command_only`, `gui_preferred`, `inherit`, `off`, `on` | named option | live | public | `gui.mode = "inherit"` |
| `gui.bare_command_opens` | Allows eligible bare command routes to prefer a dedicated workflow. | boolean | `true` | `true` or `false` | boolean flag | live | public | `gui.bare_command_opens = true` |
| `gui.default_page` | Selects the stable default workflow page. | string | `overview` | 1 through 64 characters, pattern `[a-z0-9_]+` | text | live | public | `gui.default_page = "overview"` |
| `limits.maximum_records` | Bounds persisted records owned by this module. | integer | `1000` | 1 through 100000 | count | live | public | `limits.maximum_records = 1000` |
| `limits.maximum_page_size` | Bounds command and GUI result pages. | integer | `45` | 4 through 100 | count | live | public | `limits.maximum_page_size = 45` |
| `rate.maximum_actions_per_minute` | Bounds admission attempts independently of permission cooldown duration. | integer | `30` | 1 through 10000 | actions per minute | live | public | `rate.maximum_actions_per_minute = 30` |
| `storage.retention_days` | Defines bounded data retention where the module owns persistent records. | integer | `90` | 0 through 3650 | days | live | public | `storage.retention_days = 90` |
| `failure.mode` | Defines provider and validation failure behavior. | enum | `fail_closed` | `fail_closed`, `previous_known_good`, `read_only` | named option | live | public | `failure.mode = "fail_closed"` |
| `audit.class` | Defines the minimum audit class for module actions. | enum | `admin_action` | `admin_action`, `destructive`, `metadata_only`, `none` | named option | live | public | `audit.class = "admin_action"` |
| `audit.redaction` | Defines the module redaction class. | enum | `sensitive` | `security_critical`, `sensitive`, `standard` | named option | live | public | `audit.redaction = "sensitive"` |

## building_control

Controls building control behavior, presentation, bounds, storage, and diagnostics.

File: `config/sef/modules/building_control.toml`. Schema version: `1`. Documentation version: `2`. Apply class: `live`. Dependencies: `audit`, `commands`, `core`, `gui`, `messages`, `permissions`. Conflicts: none.

Inspect permission: `sef.commands.config.inspect`. Edit permission: `sef.commands.config.edit`. Migration path: inspect the retained legacy `common.toml` with `/sef config migrate dryrun`, then request a revision-bound confirmation with `/sef config migrate apply <expected_revision>`. Legacy cooldown durations move to `sef.cooldown.<action>.<seconds>` permissions instead of module files.

| Setting | Description | Type | Default | Bounds | Unit | Apply class | Privacy | Example |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `module.enabled` | Enables the module after dependency validation. | boolean | `true` | `true` or `false` | boolean flag | live | public | `module.enabled = true` |
| `module.disabled_behavior` | Defines truthful behavior while this module is disabled. | enum | `command_unavailable` | `command_unavailable`, `provider_fallback`, `read_only` | named option | live | public | `module.disabled_behavior = "command_unavailable"` |
| `gui.mode` | Selects enhanced GUI presentation without changing command access. | enum | `inherit` | `command_only`, `gui_preferred`, `inherit`, `off`, `on` | named option | live | public | `gui.mode = "inherit"` |
| `gui.bare_command_opens` | Allows eligible bare command routes to prefer a dedicated workflow. | boolean | `true` | `true` or `false` | boolean flag | live | public | `gui.bare_command_opens = true` |
| `gui.default_page` | Selects the stable default workflow page. | string | `overview` | 1 through 64 characters, pattern `[a-z0-9_]+` | text | live | public | `gui.default_page = "overview"` |
| `limits.maximum_records` | Bounds persisted records owned by this module. | integer | `1000` | 1 through 100000 | count | live | public | `limits.maximum_records = 1000` |
| `limits.maximum_page_size` | Bounds command and GUI result pages. | integer | `45` | 4 through 100 | count | live | public | `limits.maximum_page_size = 45` |
| `rate.maximum_actions_per_minute` | Bounds admission attempts independently of permission cooldown duration. | integer | `30` | 1 through 10000 | actions per minute | live | public | `rate.maximum_actions_per_minute = 30` |
| `storage.retention_days` | Defines bounded data retention where the module owns persistent records. | integer | `30` | 0 through 3650 | days | live | public | `storage.retention_days = 30` |
| `failure.mode` | Defines provider and validation failure behavior. | enum | `fail_closed` | `fail_closed`, `previous_known_good`, `read_only` | named option | live | public | `failure.mode = "fail_closed"` |
| `audit.class` | Defines the minimum audit class for module actions. | enum | `admin_action` | `admin_action`, `destructive`, `metadata_only`, `none` | named option | live | public | `audit.class = "admin_action"` |
| `audit.redaction` | Defines the module redaction class. | enum | `standard` | `security_critical`, `sensitive`, `standard` | named option | live | public | `audit.redaction = "standard"` |
| `runtime.db_blocked_msg` | Message shown when a player with building disabled tries to build | string | `&cYou are not allowed to build.` | 0 through 8192 characters | text | live | public | `runtime.db_blocked_msg = "&cYou are not allowed to build."` |
| `runtime.db_disabled_msg` | Message shown to admin when re-enabling building. Placeholders: $player, $admin | string | `&aBuilding re-enabled for $player by $admin.` | 0 through 8192 characters | text | live | public | `runtime.db_disabled_msg = "&aBuilding re-enabled for $player by $admin."` |
| `runtime.db_enabled_msg` | Message shown to admin when disabling building. Placeholders: $player, $admin | string | `&cBuilding disabled for $player by $admin.` | 0 through 8192 characters | text | live | public | `runtime.db_enabled_msg = "&cBuilding disabled for $player by $admin."` |
| `runtime.db_player_notify_msg` | Message shown to the player. Placeholders: $status (disabled/enabled), $admin | string | `&cYour building privileges have been $status by $admin.` | 0 through 8192 characters | text | live | public | `runtime.db_player_notify_msg = "&cYour building privileges have been $status by $admin."` |
| `runtime.enable_disable_building` | Building restriction system (/disablebuilding, /db) | boolean | `true` | `true` or `false` | boolean flag | restart_required | public | `runtime.enable_disable_building = true` |

## bundles

Controls bundles behavior, presentation, bounds, storage, and diagnostics.

File: `config/sef/modules/bundles.toml`. Schema version: `1`. Documentation version: `2`. Apply class: `live`. Dependencies: `audit`, `commands`, `core`, `messages`, `permissions`. Conflicts: none.

Inspect permission: `sef.commands.config.inspect`. Edit permission: `sef.commands.config.edit`. Migration path: inspect the retained legacy `common.toml` with `/sef config migrate dryrun`, then request a revision-bound confirmation with `/sef config migrate apply <expected_revision>`. Legacy cooldown durations move to `sef.cooldown.<action>.<seconds>` permissions instead of module files.

| Setting | Description | Type | Default | Bounds | Unit | Apply class | Privacy | Example |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `module.enabled` | Enables the module after dependency validation. | boolean | `true` | `true` or `false` | boolean flag | live | public | `module.enabled = true` |
| `module.disabled_behavior` | Defines truthful behavior while this module is disabled. | enum | `command_unavailable` | `command_unavailable`, `provider_fallback`, `read_only` | named option | live | public | `module.disabled_behavior = "command_unavailable"` |
| `gui.mode` | Selects enhanced GUI presentation without changing command access. | enum | `inherit` | `command_only`, `gui_preferred`, `inherit`, `off`, `on` | named option | live | public | `gui.mode = "inherit"` |
| `gui.bare_command_opens` | Allows eligible bare command routes to prefer a dedicated workflow. | boolean | `true` | `true` or `false` | boolean flag | live | public | `gui.bare_command_opens = true` |
| `gui.default_page` | Selects the stable default workflow page. | string | `overview` | 1 through 64 characters, pattern `[a-z0-9_]+` | text | live | public | `gui.default_page = "overview"` |
| `limits.maximum_records` | Bounds persisted records owned by this module. | integer | `1000` | 1 through 100000 | count | live | public | `limits.maximum_records = 1000` |
| `limits.maximum_page_size` | Bounds command and GUI result pages. | integer | `45` | 4 through 100 | count | live | public | `limits.maximum_page_size = 45` |
| `rate.maximum_actions_per_minute` | Bounds admission attempts independently of permission cooldown duration. | integer | `30` | 1 through 10000 | actions per minute | live | public | `rate.maximum_actions_per_minute = 30` |
| `storage.retention_days` | Defines bounded data retention where the module owns persistent records. | integer | `30` | 0 through 3650 | days | live | public | `storage.retention_days = 30` |
| `failure.mode` | Defines provider and validation failure behavior. | enum | `fail_closed` | `fail_closed`, `previous_known_good`, `read_only` | named option | live | public | `failure.mode = "fail_closed"` |
| `audit.class` | Defines the minimum audit class for module actions. | enum | `admin_action` | `admin_action`, `destructive`, `metadata_only`, `none` | named option | live | public | `audit.class = "admin_action"` |
| `audit.redaction` | Defines the module redaction class. | enum | `standard` | `security_critical`, `sensitive`, `standard` | named option | live | public | `audit.redaction = "standard"` |
| `runtime.kernel_maximum_bundle_depth` | Maximum nested command bundle depth | integer | `4` | 1 through 8 | count | restart_required | public | `runtime.kernel_maximum_bundle_depth = 4` |
| `runtime.kernel_maximum_bundle_steps` | Maximum steps in one command bundle | integer | `64` | 1 through 256 | count | restart_required | public | `runtime.kernel_maximum_bundle_steps = 64` |

## command_spy

Controls command spy behavior, presentation, bounds, storage, and diagnostics.

File: `config/sef/modules/command_spy.toml`. Schema version: `1`. Documentation version: `2`. Apply class: `live`. Dependencies: `audit`, `commands`, `core`, `gui`, `messages`, `permissions`. Conflicts: none.

Inspect permission: `sef.commands.config.inspect`. Edit permission: `sef.commands.config.edit`. Migration path: inspect the retained legacy `common.toml` with `/sef config migrate dryrun`, then request a revision-bound confirmation with `/sef config migrate apply <expected_revision>`. Legacy cooldown durations move to `sef.cooldown.<action>.<seconds>` permissions instead of module files.

| Setting | Description | Type | Default | Bounds | Unit | Apply class | Privacy | Example |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `module.enabled` | Enables the module after dependency validation. | boolean | `true` | `true` or `false` | boolean flag | live | public | `module.enabled = true` |
| `module.disabled_behavior` | Defines truthful behavior while this module is disabled. | enum | `command_unavailable` | `command_unavailable`, `provider_fallback`, `read_only` | named option | live | public | `module.disabled_behavior = "command_unavailable"` |
| `gui.mode` | Selects enhanced GUI presentation without changing command access. | enum | `inherit` | `command_only`, `gui_preferred`, `inherit`, `off`, `on` | named option | live | public | `gui.mode = "inherit"` |
| `gui.bare_command_opens` | Allows eligible bare command routes to prefer a dedicated workflow. | boolean | `true` | `true` or `false` | boolean flag | live | public | `gui.bare_command_opens = true` |
| `gui.default_page` | Selects the stable default workflow page. | string | `overview` | 1 through 64 characters, pattern `[a-z0-9_]+` | text | live | public | `gui.default_page = "overview"` |
| `limits.maximum_records` | Bounds persisted records owned by this module. | integer | `1000` | 1 through 100000 | count | live | public | `limits.maximum_records = 1000` |
| `limits.maximum_page_size` | Bounds command and GUI result pages. | integer | `45` | 4 through 100 | count | live | public | `limits.maximum_page_size = 45` |
| `rate.maximum_actions_per_minute` | Bounds admission attempts independently of permission cooldown duration. | integer | `300` | 1 through 10000 | actions per minute | live | public | `rate.maximum_actions_per_minute = 300` |
| `storage.retention_days` | Defines bounded data retention where the module owns persistent records. | integer | `90` | 0 through 3650 | days | live | public | `storage.retention_days = 90` |
| `failure.mode` | Defines provider and validation failure behavior. | enum | `previous_known_good` | `fail_closed`, `previous_known_good`, `read_only` | named option | live | public | `failure.mode = "previous_known_good"` |
| `audit.class` | Defines the minimum audit class for module actions. | enum | `metadata_only` | `admin_action`, `destructive`, `metadata_only`, `none` | named option | live | public | `audit.class = "metadata_only"` |
| `audit.redaction` | Defines the module redaction class. | enum | `sensitive` | `security_critical`, `sensitive`, `standard` | named option | live | public | `audit.redaction = "sensitive"` |
| `runtime.command_spy_events_per_second` | Maximum command observations delivered to one viewer per second | integer | `100` | 1 through 1000 | count | restart_required | public | `runtime.command_spy_events_per_second = 100` |
| `runtime.command_spy_recent_limit` | Maximum redacted command events retained in memory | integer | `4096` | 32 through 65536 | count | restart_required | public | `runtime.command_spy_recent_limit = 4096` |
| `runtime.command_spy_selected_limit` | Maximum selected UUID filters per observer | integer | `32` | 1 through 256 | count | restart_required | public | `runtime.command_spy_selected_limit = 32` |
| `runtime.enable_command_spy` | Permission controlled command observation | boolean | `true` | `true` or `false` | boolean flag | restart_required | public | `runtime.enable_command_spy = true` |

## commands

Controls commands behavior, presentation, bounds, storage, and diagnostics.

File: `config/sef/modules/commands.toml`. Schema version: `1`. Documentation version: `2`. Apply class: `live`. Dependencies: `core`. Conflicts: none.

Inspect permission: `sef.commands.config.inspect`. Edit permission: `sef.commands.config.edit`. Migration path: inspect the retained legacy `common.toml` with `/sef config migrate dryrun`, then request a revision-bound confirmation with `/sef config migrate apply <expected_revision>`. Legacy cooldown durations move to `sef.cooldown.<action>.<seconds>` permissions instead of module files.

| Setting | Description | Type | Default | Bounds | Unit | Apply class | Privacy | Example |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `module.enabled` | Enables the module after dependency validation. | boolean | `true` | `true` or `false` | boolean flag | live | public | `module.enabled = true` |
| `module.disabled_behavior` | Defines truthful behavior while this module is disabled. | enum | `command_unavailable` | `command_unavailable`, `provider_fallback`, `read_only` | named option | live | public | `module.disabled_behavior = "command_unavailable"` |
| `gui.mode` | Selects enhanced GUI presentation without changing command access. | enum | `inherit` | `command_only`, `gui_preferred`, `inherit`, `off`, `on` | named option | live | public | `gui.mode = "inherit"` |
| `gui.bare_command_opens` | Allows eligible bare command routes to prefer a dedicated workflow. | boolean | `true` | `true` or `false` | boolean flag | live | public | `gui.bare_command_opens = true` |
| `gui.default_page` | Selects the stable default workflow page. | string | `overview` | 1 through 64 characters, pattern `[a-z0-9_]+` | text | live | public | `gui.default_page = "overview"` |
| `limits.maximum_records` | Bounds persisted records owned by this module. | integer | `10000` | 1 through 1000000 | count | live | public | `limits.maximum_records = 10000` |
| `limits.maximum_page_size` | Bounds command and GUI result pages. | integer | `45` | 4 through 100 | count | live | public | `limits.maximum_page_size = 45` |
| `rate.maximum_actions_per_minute` | Bounds admission attempts independently of permission cooldown duration. | integer | `300` | 1 through 10000 | actions per minute | live | public | `rate.maximum_actions_per_minute = 300` |
| `storage.retention_days` | Defines bounded data retention where the module owns persistent records. | integer | `30` | 0 through 3650 | days | live | public | `storage.retention_days = 30` |
| `failure.mode` | Defines provider and validation failure behavior. | enum | `previous_known_good` | `fail_closed`, `previous_known_good`, `read_only` | named option | live | public | `failure.mode = "previous_known_good"` |
| `audit.class` | Defines the minimum audit class for module actions. | enum | `metadata_only` | `admin_action`, `destructive`, `metadata_only`, `none` | named option | live | public | `audit.class = "metadata_only"` |
| `audit.redaction` | Defines the module redaction class. | enum | `standard` | `security_critical`, `sensitive`, `standard` | named option | live | public | `audit.redaction = "standard"` |
| `runtime.kernel_location_history_entries` | Maximum stored location history entries per player | integer | `20` | 1 through 100 | count | restart_required | public | `runtime.kernel_location_history_entries = 20` |
| `runtime.kernel_maximum_target_steps` | Maximum expanded target steps in one bundle | integer | `2000` | 1 through 100000 | count | restart_required | public | `runtime.kernel_maximum_target_steps = 2000` |
| `runtime.kernel_maximum_targets` | Maximum targets resolved by one bundle | integer | `100` | 1 through 1000 | count | restart_required | public | `runtime.kernel_maximum_targets = 100` |
| `runtime.kernel_persistent_cooldown_minimum_seconds` | Persist cooldowns with at least this many seconds remaining | integer | `60` | 0 through 86400 | seconds | restart_required | public | `runtime.kernel_persistent_cooldown_minimum_seconds = 60` |
| `runtime.kernel_repository_flush_seconds` | Maximum seconds dirty kernel repositories remain only in memory | integer | `30` | 1 through 600 | seconds | restart_required | public | `runtime.kernel_repository_flush_seconds = 30` |

## community

Controls community behavior, presentation, bounds, storage, and diagnostics.

File: `config/sef/modules/community.toml`. Schema version: `1`. Documentation version: `2`. Apply class: `live`. Dependencies: `audit`, `commands`, `core`, `gui`, `messages`, `permissions`. Conflicts: none.

Inspect permission: `sef.commands.config.inspect`. Edit permission: `sef.commands.config.edit`. Migration path: inspect the retained legacy `common.toml` with `/sef config migrate dryrun`, then request a revision-bound confirmation with `/sef config migrate apply <expected_revision>`. Legacy cooldown durations move to `sef.cooldown.<action>.<seconds>` permissions instead of module files.

| Setting | Description | Type | Default | Bounds | Unit | Apply class | Privacy | Example |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `module.enabled` | Enables the module after dependency validation. | boolean | `true` | `true` or `false` | boolean flag | live | public | `module.enabled = true` |
| `module.disabled_behavior` | Defines truthful behavior while this module is disabled. | enum | `command_unavailable` | `command_unavailable`, `provider_fallback`, `read_only` | named option | live | public | `module.disabled_behavior = "command_unavailable"` |
| `gui.mode` | Selects enhanced GUI presentation without changing command access. | enum | `inherit` | `command_only`, `gui_preferred`, `inherit`, `off`, `on` | named option | live | public | `gui.mode = "inherit"` |
| `gui.bare_command_opens` | Allows eligible bare command routes to prefer a dedicated workflow. | boolean | `true` | `true` or `false` | boolean flag | live | public | `gui.bare_command_opens = true` |
| `gui.default_page` | Selects the stable default workflow page. | string | `overview` | 1 through 64 characters, pattern `[a-z0-9_]+` | text | live | public | `gui.default_page = "overview"` |
| `limits.maximum_records` | Bounds persisted records owned by this module. | integer | `10000` | 1 through 1000000 | count | live | public | `limits.maximum_records = 10000` |
| `limits.maximum_page_size` | Bounds command and GUI result pages. | integer | `45` | 4 through 100 | count | live | public | `limits.maximum_page_size = 45` |
| `rate.maximum_actions_per_minute` | Bounds admission attempts independently of permission cooldown duration. | integer | `300` | 1 through 10000 | actions per minute | live | public | `rate.maximum_actions_per_minute = 300` |
| `storage.retention_days` | Defines bounded data retention where the module owns persistent records. | integer | `30` | 0 through 3650 | days | live | public | `storage.retention_days = 30` |
| `failure.mode` | Defines provider and validation failure behavior. | enum | `previous_known_good` | `fail_closed`, `previous_known_good`, `read_only` | named option | live | public | `failure.mode = "previous_known_good"` |
| `audit.class` | Defines the minimum audit class for module actions. | enum | `metadata_only` | `admin_action`, `destructive`, `metadata_only`, `none` | named option | live | public | `audit.class = "metadata_only"` |
| `audit.redaction` | Defines the module redaction class. | enum | `standard` | `security_critical`, `sensitive`, `standard` | named option | live | public | `audit.redaction = "standard"` |
| `runtime.enable_custom_text` | Rules, info, and custom text pages | boolean | `true` | `true` or `false` | boolean flag | restart_required | public | `runtime.enable_custom_text = true` |

## connection_messages

Controls connection messages behavior, presentation, bounds, storage, and diagnostics.

File: `config/sef/modules/connection_messages.toml`. Schema version: `1`. Documentation version: `2`. Apply class: `live`. Dependencies: `commands`, `core`, `messages`, `permissions`. Conflicts: none.

Inspect permission: `sef.commands.config.inspect`. Edit permission: `sef.commands.config.edit`. Migration path: inspect the retained legacy `common.toml` with `/sef config migrate dryrun`, then request a revision-bound confirmation with `/sef config migrate apply <expected_revision>`. Legacy cooldown durations move to `sef.cooldown.<action>.<seconds>` permissions instead of module files.

| Setting | Description | Type | Default | Bounds | Unit | Apply class | Privacy | Example |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `module.enabled` | Enables the module after dependency validation. | boolean | `true` | `true` or `false` | boolean flag | live | public | `module.enabled = true` |
| `module.disabled_behavior` | Defines truthful behavior while this module is disabled. | enum | `command_unavailable` | `command_unavailable`, `provider_fallback`, `read_only` | named option | live | public | `module.disabled_behavior = "command_unavailable"` |
| `gui.mode` | Selects enhanced GUI presentation without changing command access. | enum | `inherit` | `command_only`, `gui_preferred`, `inherit`, `off`, `on` | named option | live | public | `gui.mode = "inherit"` |
| `gui.bare_command_opens` | Allows eligible bare command routes to prefer a dedicated workflow. | boolean | `true` | `true` or `false` | boolean flag | live | public | `gui.bare_command_opens = true` |
| `gui.default_page` | Selects the stable default workflow page. | string | `overview` | 1 through 64 characters, pattern `[a-z0-9_]+` | text | live | public | `gui.default_page = "overview"` |
| `limits.maximum_records` | Bounds persisted records owned by this module. | integer | `10000` | 1 through 1000000 | count | live | public | `limits.maximum_records = 10000` |
| `limits.maximum_page_size` | Bounds command and GUI result pages. | integer | `45` | 4 through 100 | count | live | public | `limits.maximum_page_size = 45` |
| `rate.maximum_actions_per_minute` | Bounds admission attempts independently of permission cooldown duration. | integer | `300` | 1 through 10000 | actions per minute | live | public | `rate.maximum_actions_per_minute = 300` |
| `storage.retention_days` | Defines bounded data retention where the module owns persistent records. | integer | `30` | 0 through 3650 | days | live | public | `storage.retention_days = 30` |
| `failure.mode` | Defines provider and validation failure behavior. | enum | `previous_known_good` | `fail_closed`, `previous_known_good`, `read_only` | named option | live | public | `failure.mode = "previous_known_good"` |
| `audit.class` | Defines the minimum audit class for module actions. | enum | `metadata_only` | `admin_action`, `destructive`, `metadata_only`, `none` | named option | live | public | `audit.class = "metadata_only"` |
| `audit.redaction` | Defines the module redaction class. | enum | `standard` | `security_critical`, `sensitive`, `standard` | named option | live | public | `audit.redaction = "standard"` |
| `runtime.default_join_message` | Default real join template | string | `&e{player} joined the game` | 0 through 8192 characters | text | live | public | `runtime.default_join_message = "&e{player} joined the game"` |
| `runtime.default_leave_message` | Default real leave template | string | `&e{player} left the game` | 0 through 8192 characters | text | live | public | `runtime.default_leave_message = "&e{player} left the game"` |
| `runtime.enable_connection_messages` | Custom real join and leave messages | boolean | `true` | `true` or `false` | boolean flag | restart_required | public | `runtime.enable_connection_messages = true` |

## core

Controls core behavior, presentation, bounds, storage, and diagnostics.

File: `config/sef/modules/core.toml`. Schema version: `1`. Documentation version: `2`. Apply class: `restart_required`. Dependencies: none. Conflicts: none.

Inspect permission: `sef.commands.config.inspect`. Edit permission: `sef.commands.config.edit`. Migration path: inspect the retained legacy `common.toml` with `/sef config migrate dryrun`, then request a revision-bound confirmation with `/sef config migrate apply <expected_revision>`. Legacy cooldown durations move to `sef.cooldown.<action>.<seconds>` permissions instead of module files.

| Setting | Description | Type | Default | Bounds | Unit | Apply class | Privacy | Example |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `module.enabled` | Enables the module after dependency validation. | boolean | `true` | `true` or `false` | boolean flag | live | public | `module.enabled = true` |
| `module.disabled_behavior` | Defines truthful behavior while this module is disabled. | enum | `command_unavailable` | `command_unavailable`, `provider_fallback`, `read_only` | named option | live | public | `module.disabled_behavior = "command_unavailable"` |
| `gui.mode` | Selects enhanced GUI presentation without changing command access. | enum | `inherit` | `command_only`, `gui_preferred`, `inherit`, `off`, `on` | named option | live | public | `gui.mode = "inherit"` |
| `gui.bare_command_opens` | Allows eligible bare command routes to prefer a dedicated workflow. | boolean | `false` | `true` or `false` | boolean flag | live | public | `gui.bare_command_opens = false` |
| `gui.default_page` | Selects the stable default workflow page. | string | `overview` | 1 through 64 characters, pattern `[a-z0-9_]+` | text | live | public | `gui.default_page = "overview"` |
| `limits.maximum_records` | Bounds persisted records owned by this module. | integer | `10000` | 1 through 1000000 | count | live | public | `limits.maximum_records = 10000` |
| `limits.maximum_page_size` | Bounds command and GUI result pages. | integer | `45` | 4 through 100 | count | live | public | `limits.maximum_page_size = 45` |
| `rate.maximum_actions_per_minute` | Bounds admission attempts independently of permission cooldown duration. | integer | `300` | 1 through 10000 | actions per minute | live | public | `rate.maximum_actions_per_minute = 300` |
| `storage.retention_days` | Defines bounded data retention where the module owns persistent records. | integer | `30` | 0 through 3650 | days | live | public | `storage.retention_days = 30` |
| `failure.mode` | Defines provider and validation failure behavior. | enum | `previous_known_good` | `fail_closed`, `previous_known_good`, `read_only` | named option | live | public | `failure.mode = "previous_known_good"` |
| `audit.class` | Defines the minimum audit class for module actions. | enum | `metadata_only` | `admin_action`, `destructive`, `metadata_only`, `none` | named option | live | public | `audit.class = "metadata_only"` |
| `audit.redaction` | Defines the module redaction class. | enum | `standard` | `security_critical`, `sensitive`, `standard` | named option | live | public | `audit.redaction = "standard"` |
| `runtime.admin_chat_disabled_msg` | Message when admin chat is disabled | string | `&cAdmin chat disabled. &7You are now in public chat.` | 0 through 8192 characters | text | live | public | `runtime.admin_chat_disabled_msg = "&cAdmin chat disabled. &7You are now in public chat."` |
| `runtime.admin_chat_enabled_msg` | Message when admin chat is enabled | string | `&aAdmin chat enabled. &7Your messages will only be seen by operators.` | 0 through 8192 characters | text | live | public | `runtime.admin_chat_enabled_msg = "&aAdmin chat enabled. &7Your messages will only be seen by operators."` |
| `runtime.auto_enable_chat_nickname_command` | When true, enables the integrated nickname-related commands if FTB essentials is not present | boolean | `true` | `true` or `false` | boolean flag | live | public | `runtime.auto_enable_chat_nickname_command = true` |
| `runtime.banned_block_scan_budget` | Maximum block positions inspected per server tick by the background scanner | integer | `512` | 1 through 65536 | count | live | public | `runtime.banned_block_scan_budget = 512` |
| `runtime.banned_block_scan_interval` | Default interval in ticks between banned-block sweeps. Override at runtime with /banned setinterval. 20 = 1s, 40 = 2s. | integer | `40` | 1 through 24000 | count | live | public | `runtime.banned_block_scan_interval = 40` |
| `runtime.banned_block_scan_radius` | Default radius around players to scan for banned blocks (smaller = less TPS impact). Override at runtime with /banned setradius | integer | `6` | 1 through 20 | count | live | public | `runtime.banned_block_scan_radius = 6` |
| `runtime.banned_inventory_scan_interval` | Minimum ticks between fallback banned inventory scans | integer | `20` | 1 through 24000 | count | live | public | `runtime.banned_inventory_scan_interval = 20` |
| `runtime.banned_item_removed_msg` | Message shown to a player when a banned item is confiscated.   Placeholders: $item, $reason, $by, $remaining.   Leave empty to use the built-in default. | string | `` | 0 through 8192 characters | text | live | public | `runtime.banned_item_removed_msg = ""` |
| `runtime.clear_chat_all_success_msg` | Message shown to admin when clearing all non-OP chats. Placeholder: $admin | string | `&aChat cleared for all non-OP players by $admin.` | 0 through 8192 characters | text | live | public | `runtime.clear_chat_all_success_msg = "&aChat cleared for all non-OP players by $admin."` |
| `runtime.clear_chat_line_count` | Number of blank lines to send to clear chat | integer | `100` | 1 through 500 | count | live | public | `runtime.clear_chat_line_count = 100` |
| `runtime.clear_chat_self_msg` | Message shown to the player whose chat was cleared | string | `&7Your chat has been cleared by an operator.` | 0 through 8192 characters | text | live | public | `runtime.clear_chat_self_msg = "&7Your chat has been cleared by an operator."` |
| `runtime.clear_chat_success_msg` | Message shown to admin when clearing a specific player's chat. Placeholder: $player | string | `&aChat cleared for $player.` | 0 through 8192 characters | text | live | public | `runtime.clear_chat_success_msg = "&aChat cleared for $player."` |
| `runtime.command_announcement_allow_leading_slash` | Allow a stored command announcement to begin with a slash | boolean | `false` | `true` or `false` | boolean flag | live | public | `runtime.command_announcement_allow_leading_slash = false` |
| `runtime.command_announcement_allow_selectors` | Allow entity selectors in stored command announcements | boolean | `false` | `true` or `false` | boolean flag | live | public | `runtime.command_announcement_allow_selectors = false` |
| `runtime.command_announcement_allowed_commands` | Comma separated command roots allowed for scheduled command announcements. An empty value denies every command. | string | `` | 0 through 8192 characters | text | live | sensitive | `runtime.command_announcement_allowed_commands = ""` |
| `runtime.command_announcement_denied_commands` | Comma separated command roots denied for command announcements even when the allowlist contains them or a wildcard. | string | `commandannouncement,sudo,execute,op,deop,stop,reload,ban,ban-ip,pardon,pardon-ip,whitelist,luckperms,lp,sef` | 0 through 8192 characters | text | live | sensitive | `runtime.command_announcement_denied_commands = "commandannouncement,sudo,execute,op,deop,stop,reload,ban,ban-ip,pardon,pardon-ip,whitelist,luckperms,lp,sef"` |
| `runtime.command_announcement_maximum_command_length` | Maximum command length accepted for command announcements. | integer | `512` | 1 through 8192 | count | live | public | `runtime.command_announcement_maximum_command_length = 512` |
| `runtime.disabled_teleport_actions` | Comma separated canonical action ids to disable without removing their saved data | string | `` | 0 through 8192 characters | text | live | public | `runtime.disabled_teleport_actions = ""` |
| `runtime.enable_banned_block_scanning` | Enable bounded background scanning for banned blocks. Event driven placement enforcement remains active when disabled. | boolean | `false` | `true` or `false` | boolean flag | restart_required | public | `runtime.enable_banned_block_scanning = false` |
| `runtime.enable_command_announcements` | Allow stored command announcements to execute. Disabled by default because commands run with server authority. | boolean | `false` | `true` or `false` | boolean flag | restart_required | public | `runtime.enable_command_announcements = false` |
| `runtime.enable_disguises` | Legacy disguise enablement mirror. The disguise module.enabled setting is authoritative. | boolean | `true` | `true` or `false` | boolean flag | restart_required | public | `runtime.enable_disguises = true` |
| `runtime.enable_fancy_tags` | Legacy Fancy Tags enablement mirror. The fancy_tags module.enabled setting is authoritative. | boolean | `true` | `true` or `false` | boolean flag | restart_required | public | `runtime.enable_fancy_tags = true` |
| `runtime.enable_metadata_in_tab_list` | Enables or disables prefixes&suffixes in the tab list | boolean | `true` | `true` or `false` | boolean flag | restart_required | public | `runtime.enable_metadata_in_tab_list = true` |
| `runtime.enable_nicknames_in_tab_list` | Enables or disables nicknames in the tab list | boolean | `true` | `true` or `false` | boolean flag | restart_required | public | `runtime.enable_nicknames_in_tab_list = true` |
| `runtime.help_op_reply_sent_msg` | Message when helpop reply is sent. Placeholder: $player | string | `&aReply sent to $player` | 0 through 8192 characters | text | live | public | `runtime.help_op_reply_sent_msg = "&aReply sent to $player"` |
| `runtime.help_op_sent_msg` | Message when helpop is sent. No placeholders. | string | `&aMessage sent to all online operators. If there is no one online make a discord ticket.` | 0 through 8192 characters | text | live | public | `runtime.help_op_sent_msg = "&aMessage sent to all online operators. If there is no one online make a discord ticket."` |
| `runtime.no_permission_msg` | Message when player lacks permission | string | `&cYou don't have permission to do that.` | 0 through 8192 characters | text | live | public | `runtime.no_permission_msg = "&cYou don't have permission to do that."` |
| `runtime.player_offline_msg` | Message when target player is offline | string | `&cThat player is offline.` | 0 through 8192 characters | text | live | public | `runtime.player_offline_msg = "&cThat player is offline."` |

## craft

Controls craft behavior, presentation, bounds, storage, and diagnostics.

File: `config/sef/modules/craft.toml`. Schema version: `1`. Documentation version: `2`. Apply class: `live`. Dependencies: `commands`, `core`, `gui`, `messages`, `permissions`. Conflicts: none.

Inspect permission: `sef.commands.config.inspect`. Edit permission: `sef.commands.config.edit`. Migration path: inspect the retained legacy `common.toml` with `/sef config migrate dryrun`, then request a revision-bound confirmation with `/sef config migrate apply <expected_revision>`. Legacy cooldown durations move to `sef.cooldown.<action>.<seconds>` permissions instead of module files.

| Setting | Description | Type | Default | Bounds | Unit | Apply class | Privacy | Example |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `module.enabled` | Enables the module after dependency validation. | boolean | `true` | `true` or `false` | boolean flag | live | public | `module.enabled = true` |
| `module.disabled_behavior` | Defines truthful behavior while this module is disabled. | enum | `command_unavailable` | `command_unavailable`, `provider_fallback`, `read_only` | named option | live | public | `module.disabled_behavior = "command_unavailable"` |
| `gui.mode` | Selects enhanced GUI presentation without changing command access. | enum | `inherit` | `command_only`, `gui_preferred`, `inherit`, `off`, `on` | named option | live | public | `gui.mode = "inherit"` |
| `gui.bare_command_opens` | Allows eligible bare command routes to prefer a dedicated workflow. | boolean | `true` | `true` or `false` | boolean flag | live | public | `gui.bare_command_opens = true` |
| `gui.default_page` | Selects the stable default workflow page. | string | `overview` | 1 through 64 characters, pattern `[a-z0-9_]+` | text | live | public | `gui.default_page = "overview"` |
| `limits.maximum_records` | Bounds persisted records owned by this module. | integer | `10000` | 1 through 1000000 | count | live | public | `limits.maximum_records = 10000` |
| `limits.maximum_page_size` | Bounds command and GUI result pages. | integer | `45` | 4 through 100 | count | live | public | `limits.maximum_page_size = 45` |
| `rate.maximum_actions_per_minute` | Bounds admission attempts independently of permission cooldown duration. | integer | `300` | 1 through 10000 | actions per minute | live | public | `rate.maximum_actions_per_minute = 300` |
| `storage.retention_days` | Defines bounded data retention where the module owns persistent records. | integer | `30` | 0 through 3650 | days | live | public | `storage.retention_days = 30` |
| `failure.mode` | Defines provider and validation failure behavior. | enum | `previous_known_good` | `fail_closed`, `previous_known_good`, `read_only` | named option | live | public | `failure.mode = "previous_known_good"` |
| `audit.class` | Defines the minimum audit class for module actions. | enum | `metadata_only` | `admin_action`, `destructive`, `metadata_only`, `none` | named option | live | public | `audit.class = "metadata_only"` |
| `audit.redaction` | Defines the module redaction class. | enum | `standard` | `security_critical`, `sensitive`, `standard` | named option | live | public | `audit.redaction = "standard"` |
| `runtime.enable_craft_alias` | Enable the /c alias for /craft | boolean | `true` | `true` or `false` | boolean flag | restart_required | public | `runtime.enable_craft_alias = true` |
| `runtime.enable_crafting_table_command` | Virtual crafting table (/craft, /c) | boolean | `true` | `true` or `false` | boolean flag | restart_required | public | `runtime.enable_crafting_table_command = true` |

## direct_teleport

Controls direct teleport behavior, presentation, bounds, storage, and diagnostics.

File: `config/sef/modules/direct_teleport.toml`. Schema version: `1`. Documentation version: `2`. Apply class: `live`. Dependencies: `commands`, `core`, `gui`, `messages`, `permissions`. Conflicts: none.

Inspect permission: `sef.commands.config.inspect`. Edit permission: `sef.commands.config.edit`. Migration path: inspect the retained legacy `common.toml` with `/sef config migrate dryrun`, then request a revision-bound confirmation with `/sef config migrate apply <expected_revision>`. Legacy cooldown durations move to `sef.cooldown.<action>.<seconds>` permissions instead of module files.

| Setting | Description | Type | Default | Bounds | Unit | Apply class | Privacy | Example |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `module.enabled` | Enables the module after dependency validation. | boolean | `true` | `true` or `false` | boolean flag | live | public | `module.enabled = true` |
| `module.disabled_behavior` | Defines truthful behavior while this module is disabled. | enum | `command_unavailable` | `command_unavailable`, `provider_fallback`, `read_only` | named option | live | public | `module.disabled_behavior = "command_unavailable"` |
| `gui.mode` | Selects enhanced GUI presentation without changing command access. | enum | `inherit` | `command_only`, `gui_preferred`, `inherit`, `off`, `on` | named option | live | public | `gui.mode = "inherit"` |
| `gui.bare_command_opens` | Allows eligible bare command routes to prefer a dedicated workflow. | boolean | `true` | `true` or `false` | boolean flag | live | public | `gui.bare_command_opens = true` |
| `gui.default_page` | Selects the stable default workflow page. | string | `overview` | 1 through 64 characters, pattern `[a-z0-9_]+` | text | live | public | `gui.default_page = "overview"` |
| `limits.maximum_records` | Bounds persisted records owned by this module. | integer | `10000` | 1 through 1000000 | count | live | public | `limits.maximum_records = 10000` |
| `limits.maximum_page_size` | Bounds command and GUI result pages. | integer | `45` | 4 through 100 | count | live | public | `limits.maximum_page_size = 45` |
| `rate.maximum_actions_per_minute` | Bounds admission attempts independently of permission cooldown duration. | integer | `300` | 1 through 10000 | actions per minute | live | public | `rate.maximum_actions_per_minute = 300` |
| `storage.retention_days` | Defines bounded data retention where the module owns persistent records. | integer | `30` | 0 through 3650 | days | live | public | `storage.retention_days = 30` |
| `failure.mode` | Defines provider and validation failure behavior. | enum | `previous_known_good` | `fail_closed`, `previous_known_good`, `read_only` | named option | live | public | `failure.mode = "previous_known_good"` |
| `audit.class` | Defines the minimum audit class for module actions. | enum | `metadata_only` | `admin_action`, `destructive`, `metadata_only`, `none` | named option | live | public | `audit.class = "metadata_only"` |
| `audit.redaction` | Defines the module redaction class. | enum | `standard` | `security_critical`, `sensitive`, `standard` | named option | live | public | `audit.redaction = "standard"` |
| `runtime.enable_direct_teleport` | Staff direct teleport commands | boolean | `true` | `true` or `false` | boolean flag | restart_required | public | `runtime.enable_direct_teleport = true` |
| `runtime.enable_teleport_essentials` | Teleport essentials platform | boolean | `true` | `true` or `false` | boolean flag | restart_required | public | `runtime.enable_teleport_essentials = true` |
| `runtime.own_vanilla_teleport_root` | Replace the vanilla /tp root with the SEF direct teleport command | boolean | `false` | `true` or `false` | boolean flag | live | public | `runtime.own_vanilla_teleport_root = false` |
| `runtime.teleport_allow_hazards` | Allow lava, fire, cactus, magma, and similar destinations | boolean | `false` | `true` or `false` | boolean flag | live | public | `runtime.teleport_allow_hazards = false` |
| `runtime.teleport_allow_in_combat` | Allow normal user teleports while in combat | boolean | `false` | `true` or `false` | boolean flag | live | public | `runtime.teleport_allow_in_combat = false` |
| `runtime.teleport_allow_nether_roof` | Allow destinations on the Nether roof | boolean | `false` | `true` or `false` | boolean flag | live | public | `runtime.teleport_allow_nether_roof = false` |
| `runtime.teleport_cancel_on_damage` | Cancel an active teleport warmup when the player takes damage | boolean | `true` | `true` or `false` | boolean flag | live | public | `runtime.teleport_cancel_on_damage = true` |
| `runtime.teleport_cancel_on_movement` | Cancel an active teleport warmup when the player moves | boolean | `true` | `true` or `false` | boolean flag | live | public | `runtime.teleport_cancel_on_movement = true` |
| `runtime.teleport_cost` | Shared economy cost for user teleport actions. A positive value fails closed until an economy provider is installed | decimal | `0.0` | 0.0 through 1.0E9 | decimal value | live | public | `runtime.teleport_cost = 0.0` |
| `runtime.teleport_invulnerability_ticks` | Damage immunity ticks after a successful user teleport | integer | `20` | 0 through 200 | count | live | public | `runtime.teleport_invulnerability_ticks = 20` |
| `runtime.teleport_maximum_chunks` | Maximum already loaded chunks inspected by one teleport | integer | `9` | 1 through 256 | count | live | public | `runtime.teleport_maximum_chunks = 9` |
| `runtime.teleport_maximum_pending_requests` | Maximum incoming or outgoing requests per player | integer | `10` | 1 through 100 | count | live | public | `runtime.teleport_maximum_pending_requests = 10` |
| `runtime.teleport_maximum_safe_checks` | Maximum block positions inspected by one teleport | integer | `512` | 1 through 100000 | count | live | public | `runtime.teleport_maximum_safe_checks = 512` |
| `runtime.teleport_ownership_mode` | Ownership mode for homes and server warps. Values are sef, external, coexist, or import_once | string | `sef` | 0 through 8192 characters | text | restart_required | public | `runtime.teleport_ownership_mode = "sef"` |
| `runtime.teleport_safe_search_radius` | Maximum horizontal and vertical safe destination search radius | integer | `4` | 0 through 32 | count | live | public | `runtime.teleport_safe_search_radius = 4` |
| `runtime.teleport_warmup_seconds` | Shared warmup for user teleport actions | integer | `0` | 0 through 3600 | seconds | live | public | `runtime.teleport_warmup_seconds = 0` |

## disguise

Controls disguise behavior, presentation, bounds, storage, and diagnostics.

File: `config/sef/modules/disguise.toml`. Schema version: `1`. Documentation version: `2`. Apply class: `live`. Dependencies: `audit`, `commands`, `core`, `gui`, `messages`, `permissions`. Conflicts: none.

Inspect permission: `sef.commands.config.inspect`. Edit permission: `sef.commands.config.edit`. Migration path: inspect the retained legacy `common.toml` with `/sef config migrate dryrun`, then request a revision-bound confirmation with `/sef config migrate apply <expected_revision>`. Legacy cooldown durations move to `sef.cooldown.<action>.<seconds>` permissions instead of module files.

| Setting | Description | Type | Default | Bounds | Unit | Apply class | Privacy | Example |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `module.enabled` | Enables the module after dependency validation. | boolean | `true` | `true` or `false` | boolean flag | live | public | `module.enabled = true` |
| `module.disabled_behavior` | Defines truthful behavior while this module is disabled. | enum | `command_unavailable` | `command_unavailable`, `provider_fallback`, `read_only` | named option | live | public | `module.disabled_behavior = "command_unavailable"` |
| `gui.mode` | Selects enhanced GUI presentation without changing command access. | enum | `inherit` | `command_only`, `gui_preferred`, `inherit`, `off`, `on` | named option | live | public | `gui.mode = "inherit"` |
| `gui.bare_command_opens` | Allows eligible bare command routes to prefer a dedicated workflow. | boolean | `true` | `true` or `false` | boolean flag | live | public | `gui.bare_command_opens = true` |
| `gui.default_page` | Selects the stable default workflow page. | string | `overview` | 1 through 64 characters, pattern `[a-z0-9_]+` | text | live | public | `gui.default_page = "overview"` |
| `limits.maximum_records` | Bounds persisted records owned by this module. | integer | `1000` | 1 through 100000 | count | live | public | `limits.maximum_records = 1000` |
| `limits.maximum_page_size` | Bounds command and GUI result pages. | integer | `45` | 4 through 100 | count | live | public | `limits.maximum_page_size = 45` |
| `rate.maximum_actions_per_minute` | Bounds admission attempts independently of permission cooldown duration. | integer | `30` | 1 through 10000 | actions per minute | live | public | `rate.maximum_actions_per_minute = 30` |
| `storage.retention_days` | Defines bounded data retention where the module owns persistent records. | integer | `30` | 0 through 3650 | days | live | public | `storage.retention_days = 30` |
| `failure.mode` | Defines provider and validation failure behavior. | enum | `fail_closed` | `fail_closed`, `previous_known_good`, `read_only` | named option | live | public | `failure.mode = "fail_closed"` |
| `audit.class` | Defines the minimum audit class for module actions. | enum | `admin_action` | `admin_action`, `destructive`, `metadata_only`, `none` | named option | live | public | `audit.class = "admin_action"` |
| `audit.redaction` | Defines the module redaction class. | enum | `standard` | `security_critical`, `sensitive`, `standard` | named option | live | public | `audit.redaction = "standard"` |
| `runtime.disguise_abilities_enabled` | Enable allowlisted server authoritative disguise abilities | boolean | `false` | `true` or `false` | boolean flag | live | public | `runtime.disguise_abilities_enabled = false` |
| `runtime.disguise_blaze_allow_pvp` | Allow the curated Blaze fireball to damage another player | boolean | `false` | `true` or `false` | boolean flag | live | public | `runtime.disguise_blaze_allow_pvp = false` |
| `runtime.disguise_blaze_fire_seconds` | Fire duration applied by the curated Blaze fireball | integer | `5` | 0 through 60 | seconds | live | public | `runtime.disguise_blaze_fire_seconds = 5` |
| `runtime.disguise_blaze_fireball_damage` | Maximum direct damage for the curated Blaze fireball | decimal | `5.0` | 0.0 through 100.0 | decimal value | live | public | `runtime.disguise_blaze_fireball_damage = 5.0` |
| `runtime.disguise_blaze_maximum_range` | Maximum travel range of the curated Blaze fireball | integer | `48` | 4 through 128 | count | live | public | `runtime.disguise_blaze_maximum_range = 48` |
| `runtime.disguise_clear_on_death` | Clear active disguises when the subject dies | boolean | `true` | `true` or `false` | boolean flag | live | public | `runtime.disguise_clear_on_death = true` |
| `runtime.disguise_clear_on_logout` | Clear active disguises when the subject disconnects | boolean | `true` | `true` or `false` | boolean flag | live | public | `runtime.disguise_clear_on_logout = true` |
| `runtime.disguise_maximum_active` | Maximum simultaneous active disguises | integer | `256` | 1 through 4096 | count | live | public | `runtime.disguise_maximum_active = 256` |
| `runtime.disguise_sounds_enabled` | Enable viewer filtered disguise sound profiles | boolean | `true` | `true` or `false` | boolean flag | live | public | `runtime.disguise_sounds_enabled = true` |
| `runtime.disguise_traits_enabled` | Enable allowlisted server authoritative disguise traits | boolean | `false` | `true` or `false` | boolean flag | live | public | `runtime.disguise_traits_enabled = false` |
| `runtime.disguise_vanilla_proxy_enabled` | Enable bounded vanilla client mob proxy rendering | boolean | `true` | `true` or `false` | boolean flag | live | public | `runtime.disguise_vanilla_proxy_enabled = true` |

## displays

Controls displays behavior, presentation, bounds, storage, and diagnostics.

File: `config/sef/modules/displays.toml`. Schema version: `1`. Documentation version: `2`. Apply class: `live`. Dependencies: `audit`, `commands`, `core`, `gui`, `messages`, `permissions`. Conflicts: none.

Inspect permission: `sef.commands.config.inspect`. Edit permission: `sef.commands.config.edit`. Migration path: inspect the retained legacy `common.toml` with `/sef config migrate dryrun`, then request a revision-bound confirmation with `/sef config migrate apply <expected_revision>`. Legacy cooldown durations move to `sef.cooldown.<action>.<seconds>` permissions instead of module files.

| Setting | Description | Type | Default | Bounds | Unit | Apply class | Privacy | Example |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `module.enabled` | Enables the module after dependency validation. | boolean | `true` | `true` or `false` | boolean flag | live | public | `module.enabled = true` |
| `module.disabled_behavior` | Defines truthful behavior while this module is disabled. | enum | `command_unavailable` | `command_unavailable`, `provider_fallback`, `read_only` | named option | live | public | `module.disabled_behavior = "command_unavailable"` |
| `gui.mode` | Selects enhanced GUI presentation without changing command access. | enum | `inherit` | `command_only`, `gui_preferred`, `inherit`, `off`, `on` | named option | live | public | `gui.mode = "inherit"` |
| `gui.bare_command_opens` | Allows eligible bare command routes to prefer a dedicated workflow. | boolean | `true` | `true` or `false` | boolean flag | live | public | `gui.bare_command_opens = true` |
| `gui.default_page` | Selects the stable default workflow page. | string | `overview` | 1 through 64 characters, pattern `[a-z0-9_]+` | text | live | public | `gui.default_page = "overview"` |
| `limits.maximum_records` | Bounds persisted records owned by this module. | integer | `10000` | 1 through 1000000 | count | live | public | `limits.maximum_records = 10000` |
| `limits.maximum_page_size` | Bounds command and GUI result pages. | integer | `45` | 4 through 100 | count | live | public | `limits.maximum_page_size = 45` |
| `rate.maximum_actions_per_minute` | Bounds admission attempts independently of permission cooldown duration. | integer | `300` | 1 through 10000 | actions per minute | live | public | `rate.maximum_actions_per_minute = 300` |
| `storage.retention_days` | Defines bounded data retention where the module owns persistent records. | integer | `30` | 0 through 3650 | days | live | public | `storage.retention_days = 30` |
| `failure.mode` | Defines provider and validation failure behavior. | enum | `previous_known_good` | `fail_closed`, `previous_known_good`, `read_only` | named option | live | public | `failure.mode = "previous_known_good"` |
| `audit.class` | Defines the minimum audit class for module actions. | enum | `metadata_only` | `admin_action`, `destructive`, `metadata_only`, `none` | named option | live | public | `audit.class = "metadata_only"` |
| `audit.redaction` | Defines the module redaction class. | enum | `standard` | `security_critical`, `sensitive`, `standard` | named option | live | public | `audit.redaction = "standard"` |
| `runtime.announcement_confirm_format` | Format for announcement added confirmation. Placeholders: $id, $interval, $message | string | `&aAdded announcement: &e$id &7(every $interval)` | 0 through 8192 characters | text | live | public | `runtime.announcement_confirm_format = "&aAdded announcement: &e$id &7(every $interval)"` |
| `runtime.announcement_interval_seconds` | Interval in seconds between announcements | integer | `300` | -9223372036854775808 through 9223372036854775807 | seconds | live | public | `runtime.announcement_interval_seconds = 300` |
| `runtime.announcement_list_header_cmd` | Header for command announcement list | string | `&6━━━━━━━━ Command Announcements ━━━━━━━━` | 0 through 8192 characters | text | live | public | `runtime.announcement_list_header_cmd = "&6━━━━━━━━ Command Announcements ━━━━━━━━"` |
| `runtime.announcement_list_header_text` | Header for text announcement list | string | `&6━━━━━━━━ Text Announcements ━━━━━━━━` | 0 through 8192 characters | text | live | public | `runtime.announcement_list_header_text = "&6━━━━━━━━ Text Announcements ━━━━━━━━"` |
| `runtime.announcement_use_random_order` | When true, announcements are chosen randomly; otherwise in order | boolean | `false` | `true` or `false` | boolean flag | live | public | `runtime.announcement_use_random_order = false` |
| `runtime.apply_motd_on_startup` | Automatically apply the configured MOTD when the server starts | boolean | `true` | `true` or `false` | boolean flag | live | public | `runtime.apply_motd_on_startup = true` |
| `runtime.countdown_chat_format` | Chat-line format used when /countdown's chat_too argument is true.   Placeholders: $message, $time, $colored_time, $color.   Leave empty to use the built-in default. | string | `` | 0 through 8192 characters | text | live | public | `runtime.countdown_chat_format = ""` |
| `runtime.countdown_subtitle_format` | Format for the subtitle shown to players on each countdown beat.   Placeholders: $message, $time, $colored_time, $color.   Leave empty to use the built-in default ($colored_time). | string | `` | 0 through 8192 characters | text | live | public | `runtime.countdown_subtitle_format = ""` |
| `runtime.countdown_title_format` | Format for the title shown to players on each countdown beat.   Placeholders: $message, $time, $colored_time, $color.   Leave empty to use the built-in default ($message). | string | `` | 0 through 8192 characters | text | live | public | `runtime.countdown_title_format = ""` |
| `runtime.enable_announcements` | Scheduled announcements + announcement commands | boolean | `true` | `true` or `false` | boolean flag | restart_required | public | `runtime.enable_announcements = true` |
| `runtime.enable_countdown` | Countdown broadcaster (/countdown) | boolean | `true` | `true` or `false` | boolean flag | restart_required | public | `runtime.enable_countdown = true` |
| `runtime.enable_motd_system` | MOTD system (/sef motd ...) | boolean | `true` | `true` or `false` | boolean flag | restart_required | public | `runtime.enable_motd_system = true` |
| `runtime.toggle_list_header` | Header for toggle list | string | `&6━━━━━━ Toggleable Announcements ━━━━━━` | 0 through 8192 characters | text | live | public | `runtime.toggle_list_header = "&6━━━━━━ Toggleable Announcements ━━━━━━"` |
| `runtime.toggle_off_text` | Text shown when toggle is OFF | string | `&c[OFF]` | 0 through 8192 characters | text | live | public | `runtime.toggle_off_text = "&c[OFF]"` |
| `runtime.toggle_on_text` | Text shown when toggle is ON | string | `&a[ON]` | 0 through 8192 characters | text | live | public | `runtime.toggle_on_text = "&a[ON]"` |

## economy

Controls economy behavior, presentation, bounds, storage, and diagnostics.

File: `config/sef/modules/economy.toml`. Schema version: `1`. Documentation version: `2`. Apply class: `live`. Dependencies: `commands`, `core`, `gui`, `messages`, `permissions`. Conflicts: none.

Inspect permission: `sef.commands.config.inspect`. Edit permission: `sef.commands.config.edit`. Migration path: inspect the retained legacy `common.toml` with `/sef config migrate dryrun`, then request a revision-bound confirmation with `/sef config migrate apply <expected_revision>`. Legacy cooldown durations move to `sef.cooldown.<action>.<seconds>` permissions instead of module files.

| Setting | Description | Type | Default | Bounds | Unit | Apply class | Privacy | Example |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `module.enabled` | Enables the module after dependency validation. | boolean | `true` | `true` or `false` | boolean flag | live | public | `module.enabled = true` |
| `module.disabled_behavior` | Defines truthful behavior while this module is disabled. | enum | `command_unavailable` | `command_unavailable`, `provider_fallback`, `read_only` | named option | live | public | `module.disabled_behavior = "command_unavailable"` |
| `gui.mode` | Selects enhanced GUI presentation without changing command access. | enum | `inherit` | `command_only`, `gui_preferred`, `inherit`, `off`, `on` | named option | live | public | `gui.mode = "inherit"` |
| `gui.bare_command_opens` | Allows eligible bare command routes to prefer a dedicated workflow. | boolean | `true` | `true` or `false` | boolean flag | live | public | `gui.bare_command_opens = true` |
| `gui.default_page` | Selects the stable default workflow page. | string | `overview` | 1 through 64 characters, pattern `[a-z0-9_]+` | text | live | public | `gui.default_page = "overview"` |
| `limits.maximum_records` | Bounds persisted records owned by this module. | integer | `10000` | 1 through 1000000 | count | live | public | `limits.maximum_records = 10000` |
| `limits.maximum_page_size` | Bounds command and GUI result pages. | integer | `45` | 4 through 100 | count | live | public | `limits.maximum_page_size = 45` |
| `rate.maximum_actions_per_minute` | Bounds admission attempts independently of permission cooldown duration. | integer | `300` | 1 through 10000 | actions per minute | live | public | `rate.maximum_actions_per_minute = 300` |
| `storage.retention_days` | Defines bounded data retention where the module owns persistent records. | integer | `30` | 0 through 3650 | days | live | public | `storage.retention_days = 30` |
| `failure.mode` | Defines provider and validation failure behavior. | enum | `previous_known_good` | `fail_closed`, `previous_known_good`, `read_only` | named option | live | public | `failure.mode = "previous_known_good"` |
| `audit.class` | Defines the minimum audit class for module actions. | enum | `metadata_only` | `admin_action`, `destructive`, `metadata_only`, `none` | named option | live | public | `audit.class = "metadata_only"` |
| `audit.redaction` | Defines the module redaction class. | enum | `standard` | `security_critical`, `sensitive`, `standard` | named option | live | public | `audit.redaction = "standard"` |
| `runtime.economy_allow_offline_payments` | Allow payments to unambiguous known offline identities | boolean | `true` | `true` or `false` | boolean flag | restart_required | public | `runtime.economy_allow_offline_payments = true` |
| `runtime.economy_allow_self_payments` | Allow a player to pay the same account | boolean | `false` | `true` or `false` | boolean flag | restart_required | public | `runtime.economy_allow_self_payments = false` |
| `runtime.economy_balance_top_page_size` | Entries shown per balance top page | integer | `10` | 1 through 100 | count | restart_required | public | `runtime.economy_balance_top_page_size = 10` |
| `runtime.economy_command_costs` | Comma separated action cost mappings. Components are fixed, use, target, distance, and item. Example sef:teleport.spawn=5.00,sef:teleport.spawn@distance=0.01 | string | `` | 0 through 8192 characters | text | restart_required | public | `runtime.economy_command_costs = ""` |
| `runtime.economy_confirmation_threshold` | Payments at or above this value require confirmation when the player preference is enabled. Zero disables the threshold | integer | `100000` | 0 through 9000000000000000 | count | restart_required | public | `runtime.economy_confirmation_threshold = 100000` |
| `runtime.economy_currency` | Stable currency identifier | string | `coin` | 0 through 8192 characters | text | restart_required | public | `runtime.economy_currency = "coin"` |
| `runtime.economy_currency_symbol` | Display only currency prefix | string | `$` | 0 through 8192 characters | text | restart_required | public | `runtime.economy_currency_symbol = "$"` |
| `runtime.economy_default_balance` | Opening account balance in minor units | integer | `0` | -9000000000000000 through 9000000000000000 | count | restart_required | public | `runtime.economy_default_balance = 0` |
| `runtime.economy_enabled_sign_types` | Comma separated enabled sign types | string | `balance,buy,sell,trade,free,disposal,kit,heal,repair,time,weather,warp` | 0 through 8192 characters | text | restart_required | public | `runtime.economy_enabled_sign_types = "balance,buy,sell,trade,free,disposal,kit,heal,repair,time,weather,warp"` |
| `runtime.economy_external_provider` | Registered external provider id. Empty selects the highest priority adapter | string | `` | 0 through 8192 characters | text | restart_required | public | `runtime.economy_external_provider = ""` |
| `runtime.economy_history_page_size` | Transactions shown per history page | integer | `10` | 1 through 100 | count | restart_required | public | `runtime.economy_history_page_size = 10` |
| `runtime.economy_maximum_accounts` | Maximum native accounts | integer | `100000` | 1 through 1000000 | count | restart_required | public | `runtime.economy_maximum_accounts = 100000` |
| `runtime.economy_maximum_balance` | Maximum account balance in minor units | integer | `1000000000000000` | 1 through 9000000000000000 | count | restart_required | public | `runtime.economy_maximum_balance = 1000000000000000` |
| `runtime.economy_maximum_import_accounts` | Maximum accounts accepted by one import once operation | integer | `100000` | 1 through 1000000 | count | restart_required | public | `runtime.economy_maximum_import_accounts = 100000` |
| `runtime.economy_maximum_ledger_entries` | Maximum retained native ledger entries | integer | `100000` | 100 through 1000000 | count | restart_required | public | `runtime.economy_maximum_ledger_entries = 100000` |
| `runtime.economy_maximum_pending_costs` | Maximum crash recoverable pending command costs | integer | `10000` | 1 through 100000 | count | restart_required | public | `runtime.economy_maximum_pending_costs = 10000` |
| `runtime.economy_maximum_signs` | Maximum registered economy sign sides | integer | `100000` | 1 through 1000000 | count | restart_required | public | `runtime.economy_maximum_signs = 100000` |
| `runtime.economy_maximum_transaction` | Maximum value of one transaction in minor units | integer | `1000000000000` | 1 through 9000000000000000 | count | restart_required | public | `runtime.economy_maximum_transaction = 1000000000000` |
| `runtime.economy_maximum_worth_entries` | Maximum server defined item worth entries | integer | `10000` | 1 through 100000 | count | restart_required | public | `runtime.economy_maximum_worth_entries = 10000` |
| `runtime.economy_minimum_balance` | Minimum account balance in minor units. Use zero to disallow debt | integer | `0` | -9000000000000000 through 9000000000000000 | count | restart_required | public | `runtime.economy_minimum_balance = 0` |
| `runtime.economy_minor_units` | Decimal minor units used when parsing and formatting | integer | `2` | 0 through 8 | count | restart_required | public | `runtime.economy_minor_units = 2` |
| `runtime.economy_provider_mode` | native, external, disabled, or import_once | string | `native` | 0 through 8192 characters | text | restart_required | public | `runtime.economy_provider_mode = "native"` |
| `runtime.enable_economy` | Native or adapter backed economy, worth, sell, and command costs | boolean | `true` | `true` or `false` | boolean flag | restart_required | public | `runtime.enable_economy = true` |

## economy_signs

Controls economy signs behavior, presentation, bounds, storage, and diagnostics.

File: `config/sef/modules/economy_signs.toml`. Schema version: `1`. Documentation version: `2`. Apply class: `live`. Dependencies: `commands`, `core`, `economy`, `gui`, `messages`, `permissions`. Conflicts: none.

Inspect permission: `sef.commands.config.inspect`. Edit permission: `sef.commands.config.edit`. Migration path: inspect the retained legacy `common.toml` with `/sef config migrate dryrun`, then request a revision-bound confirmation with `/sef config migrate apply <expected_revision>`. Legacy cooldown durations move to `sef.cooldown.<action>.<seconds>` permissions instead of module files.

| Setting | Description | Type | Default | Bounds | Unit | Apply class | Privacy | Example |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `module.enabled` | Enables the module after dependency validation. | boolean | `true` | `true` or `false` | boolean flag | live | public | `module.enabled = true` |
| `module.disabled_behavior` | Defines truthful behavior while this module is disabled. | enum | `command_unavailable` | `command_unavailable`, `provider_fallback`, `read_only` | named option | live | public | `module.disabled_behavior = "command_unavailable"` |
| `gui.mode` | Selects enhanced GUI presentation without changing command access. | enum | `inherit` | `command_only`, `gui_preferred`, `inherit`, `off`, `on` | named option | live | public | `gui.mode = "inherit"` |
| `gui.bare_command_opens` | Allows eligible bare command routes to prefer a dedicated workflow. | boolean | `true` | `true` or `false` | boolean flag | live | public | `gui.bare_command_opens = true` |
| `gui.default_page` | Selects the stable default workflow page. | string | `overview` | 1 through 64 characters, pattern `[a-z0-9_]+` | text | live | public | `gui.default_page = "overview"` |
| `limits.maximum_records` | Bounds persisted records owned by this module. | integer | `10000` | 1 through 1000000 | count | live | public | `limits.maximum_records = 10000` |
| `limits.maximum_page_size` | Bounds command and GUI result pages. | integer | `45` | 4 through 100 | count | live | public | `limits.maximum_page_size = 45` |
| `rate.maximum_actions_per_minute` | Bounds admission attempts independently of permission cooldown duration. | integer | `300` | 1 through 10000 | actions per minute | live | public | `rate.maximum_actions_per_minute = 300` |
| `storage.retention_days` | Defines bounded data retention where the module owns persistent records. | integer | `30` | 0 through 3650 | days | live | public | `storage.retention_days = 30` |
| `failure.mode` | Defines provider and validation failure behavior. | enum | `previous_known_good` | `fail_closed`, `previous_known_good`, `read_only` | named option | live | public | `failure.mode = "previous_known_good"` |
| `audit.class` | Defines the minimum audit class for module actions. | enum | `metadata_only` | `admin_action`, `destructive`, `metadata_only`, `none` | named option | live | public | `audit.class = "metadata_only"` |
| `audit.redaction` | Defines the module redaction class. | enum | `standard` | `security_critical`, `sensitive`, `standard` | named option | live | public | `audit.redaction = "standard"` |
| `runtime.economy_sign_claim_seconds` | Seconds a placed sign remains claimable by its placer | integer | `300` | 10 through 3600 | seconds | restart_required | public | `runtime.economy_sign_claim_seconds = 300` |
| `runtime.economy_sign_maximum_quantity` | Maximum items in one economy sign transaction | integer | `2304` | 1 through 100000 | count | restart_required | public | `runtime.economy_sign_maximum_quantity = 2304` |
| `runtime.economy_sign_maximum_value` | Maximum economy sign transaction value in minor units | integer | `1000000000` | 1 through 9000000000000000 | count | restart_required | public | `runtime.economy_sign_maximum_value = 1000000000` |
| `runtime.enable_economy_signs` | Server authoritative vanilla economy signs | boolean | `true` | `true` or `false` | boolean flag | restart_required | public | `runtime.enable_economy_signs = true` |

## enchanting

Controls enchanting behavior, presentation, bounds, storage, and diagnostics.

File: `config/sef/modules/enchanting.toml`. Schema version: `1`. Documentation version: `2`. Apply class: `live`. Dependencies: `commands`, `core`, `gui`, `messages`, `permissions`. Conflicts: none.

Inspect permission: `sef.commands.config.inspect`. Edit permission: `sef.commands.config.edit`. Migration path: inspect the retained legacy `common.toml` with `/sef config migrate dryrun`, then request a revision-bound confirmation with `/sef config migrate apply <expected_revision>`. Legacy cooldown durations move to `sef.cooldown.<action>.<seconds>` permissions instead of module files.

| Setting | Description | Type | Default | Bounds | Unit | Apply class | Privacy | Example |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `module.enabled` | Enables the module after dependency validation. | boolean | `true` | `true` or `false` | boolean flag | live | public | `module.enabled = true` |
| `module.disabled_behavior` | Defines truthful behavior while this module is disabled. | enum | `command_unavailable` | `command_unavailable`, `provider_fallback`, `read_only` | named option | live | public | `module.disabled_behavior = "command_unavailable"` |
| `gui.mode` | Selects enhanced GUI presentation without changing command access. | enum | `inherit` | `command_only`, `gui_preferred`, `inherit`, `off`, `on` | named option | live | public | `gui.mode = "inherit"` |
| `gui.bare_command_opens` | Allows eligible bare command routes to prefer a dedicated workflow. | boolean | `true` | `true` or `false` | boolean flag | live | public | `gui.bare_command_opens = true` |
| `gui.default_page` | Selects the stable default workflow page. | string | `overview` | 1 through 64 characters, pattern `[a-z0-9_]+` | text | live | public | `gui.default_page = "overview"` |
| `limits.maximum_records` | Bounds persisted records owned by this module. | integer | `10000` | 1 through 1000000 | count | live | public | `limits.maximum_records = 10000` |
| `limits.maximum_page_size` | Bounds command and GUI result pages. | integer | `45` | 4 through 100 | count | live | public | `limits.maximum_page_size = 45` |
| `rate.maximum_actions_per_minute` | Bounds admission attempts independently of permission cooldown duration. | integer | `300` | 1 through 10000 | actions per minute | live | public | `rate.maximum_actions_per_minute = 300` |
| `storage.retention_days` | Defines bounded data retention where the module owns persistent records. | integer | `30` | 0 through 3650 | days | live | public | `storage.retention_days = 30` |
| `failure.mode` | Defines provider and validation failure behavior. | enum | `previous_known_good` | `fail_closed`, `previous_known_good`, `read_only` | named option | live | public | `failure.mode = "previous_known_good"` |
| `audit.class` | Defines the minimum audit class for module actions. | enum | `metadata_only` | `admin_action`, `destructive`, `metadata_only`, `none` | named option | live | public | `audit.class = "metadata_only"` |
| `audit.redaction` | Defines the module redaction class. | enum | `standard` | `security_critical`, `sensitive`, `standard` | named option | live | public | `audit.redaction = "standard"` |
| `runtime.enable_enchanting_table_alias` | Enable the /et alias for /enchantingtable | boolean | `true` | `true` or `false` | boolean flag | restart_required | public | `runtime.enable_enchanting_table_alias = true` |
| `runtime.enable_enchanting_table_command` | Virtual enchanting table (/enchantingtable, /et) | boolean | `true` | `true` or `false` | boolean flag | restart_required | public | `runtime.enable_enchanting_table_command = true` |

## fake_actions

Controls fake actions behavior, presentation, bounds, storage, and diagnostics.

File: `config/sef/modules/fake_actions.toml`. Schema version: `1`. Documentation version: `2`. Apply class: `live`. Dependencies: `audit`, `commands`, `core`, `messages`, `permissions`. Conflicts: none.

Inspect permission: `sef.commands.config.inspect`. Edit permission: `sef.commands.config.edit`. Migration path: inspect the retained legacy `common.toml` with `/sef config migrate dryrun`, then request a revision-bound confirmation with `/sef config migrate apply <expected_revision>`. Legacy cooldown durations move to `sef.cooldown.<action>.<seconds>` permissions instead of module files.

| Setting | Description | Type | Default | Bounds | Unit | Apply class | Privacy | Example |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `module.enabled` | Enables the module after dependency validation. | boolean | `true` | `true` or `false` | boolean flag | live | public | `module.enabled = true` |
| `module.disabled_behavior` | Defines truthful behavior while this module is disabled. | enum | `command_unavailable` | `command_unavailable`, `provider_fallback`, `read_only` | named option | live | public | `module.disabled_behavior = "command_unavailable"` |
| `gui.mode` | Selects enhanced GUI presentation without changing command access. | enum | `inherit` | `command_only`, `gui_preferred`, `inherit`, `off`, `on` | named option | live | public | `gui.mode = "inherit"` |
| `gui.bare_command_opens` | Allows eligible bare command routes to prefer a dedicated workflow. | boolean | `true` | `true` or `false` | boolean flag | live | public | `gui.bare_command_opens = true` |
| `gui.default_page` | Selects the stable default workflow page. | string | `overview` | 1 through 64 characters, pattern `[a-z0-9_]+` | text | live | public | `gui.default_page = "overview"` |
| `limits.maximum_records` | Bounds persisted records owned by this module. | integer | `1000` | 1 through 100000 | count | live | public | `limits.maximum_records = 1000` |
| `limits.maximum_page_size` | Bounds command and GUI result pages. | integer | `45` | 4 through 100 | count | live | public | `limits.maximum_page_size = 45` |
| `rate.maximum_actions_per_minute` | Bounds admission attempts independently of permission cooldown duration. | integer | `30` | 1 through 10000 | actions per minute | live | public | `rate.maximum_actions_per_minute = 30` |
| `storage.retention_days` | Defines bounded data retention where the module owns persistent records. | integer | `30` | 0 through 3650 | days | live | public | `storage.retention_days = 30` |
| `failure.mode` | Defines provider and validation failure behavior. | enum | `fail_closed` | `fail_closed`, `previous_known_good`, `read_only` | named option | live | public | `failure.mode = "fail_closed"` |
| `audit.class` | Defines the minimum audit class for module actions. | enum | `admin_action` | `admin_action`, `destructive`, `metadata_only`, `none` | named option | live | public | `audit.class = "admin_action"` |
| `audit.redaction` | Defines the module redaction class. | enum | `standard` | `security_critical`, `sensitive`, `standard` | named option | live | public | `audit.redaction = "standard"` |
| `runtime.fake_chat_format` | Unsigned fake chat presentation. Placeholders are prefix, suffix, username, nickname, and message. | string | `{prefix}{nickname}{suffix}&7: &f{message}` | 0 through 8192 characters | text | live | public | `runtime.fake_chat_format = "{prefix}{nickname}{suffix}&7: &f{message}"` |
| `runtime.fake_join_format` | Fake join presentation. Placeholders are prefix, suffix, username, and nickname. | string | `&e{nickname} joined the game` | 0 through 8192 characters | text | live | public | `runtime.fake_join_format = "&e{nickname} joined the game"` |
| `runtime.fake_leave_format` | Fake leave presentation. Placeholders are prefix, suffix, username, and nickname. | string | `&e{nickname} left the game` | 0 through 8192 characters | text | live | public | `runtime.fake_leave_format = "&e{nickname} left the game"` |
| `runtime.fake_maximum_message_length` | Maximum fake message length before formatting. | integer | `256` | 1 through 2048 | count | live | public | `runtime.fake_maximum_message_length = 256` |

## fancy_tags

Controls fancy tags behavior, presentation, bounds, storage, and diagnostics.

File: `config/sef/modules/fancy_tags.toml`. Schema version: `1`. Documentation version: `2`. Apply class: `live`. Dependencies: `audit`, `commands`, `core`, `gui`, `messages`, `permissions`. Conflicts: none.

Inspect permission: `sef.commands.config.inspect`. Edit permission: `sef.commands.config.edit`. Migration path: inspect the retained legacy `common.toml` with `/sef config migrate dryrun`, then request a revision-bound confirmation with `/sef config migrate apply <expected_revision>`. Legacy cooldown durations move to `sef.cooldown.<action>.<seconds>` permissions instead of module files.

| Setting | Description | Type | Default | Bounds | Unit | Apply class | Privacy | Example |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `module.enabled` | Enables the module after dependency validation. | boolean | `true` | `true` or `false` | boolean flag | live | public | `module.enabled = true` |
| `module.disabled_behavior` | Defines truthful behavior while this module is disabled. | enum | `command_unavailable` | `command_unavailable`, `provider_fallback`, `read_only` | named option | live | public | `module.disabled_behavior = "command_unavailable"` |
| `gui.mode` | Selects enhanced GUI presentation without changing command access. | enum | `inherit` | `command_only`, `gui_preferred`, `inherit`, `off`, `on` | named option | live | public | `gui.mode = "inherit"` |
| `gui.bare_command_opens` | Allows eligible bare command routes to prefer a dedicated workflow. | boolean | `true` | `true` or `false` | boolean flag | live | public | `gui.bare_command_opens = true` |
| `gui.default_page` | Selects the stable default workflow page. | string | `overview` | 1 through 64 characters, pattern `[a-z0-9_]+` | text | live | public | `gui.default_page = "overview"` |
| `limits.maximum_records` | Bounds persisted records owned by this module. | integer | `10000` | 1 through 1000000 | count | live | public | `limits.maximum_records = 10000` |
| `limits.maximum_page_size` | Bounds command and GUI result pages. | integer | `45` | 4 through 100 | count | live | public | `limits.maximum_page_size = 45` |
| `rate.maximum_actions_per_minute` | Bounds admission attempts independently of permission cooldown duration. | integer | `300` | 1 through 10000 | actions per minute | live | public | `rate.maximum_actions_per_minute = 300` |
| `storage.retention_days` | Defines bounded data retention where the module owns persistent records. | integer | `30` | 0 through 3650 | days | live | public | `storage.retention_days = 30` |
| `failure.mode` | Defines provider and validation failure behavior. | enum | `previous_known_good` | `fail_closed`, `previous_known_good`, `read_only` | named option | live | public | `failure.mode = "previous_known_good"` |
| `audit.class` | Defines the minimum audit class for module actions. | enum | `metadata_only` | `admin_action`, `destructive`, `metadata_only`, `none` | named option | live | public | `audit.class = "metadata_only"` |
| `audit.redaction` | Defines the module redaction class. | enum | `standard` | `security_critical`, `sensitive`, `standard` | named option | live | public | `audit.redaction = "standard"` |
| `runtime.fancy_tags_allow_local_overlays_connected` | Allow clearly local only enhanced client tag overlays while connected | boolean | `false` | `true` or `false` | boolean flag | restart_required | public | `runtime.fancy_tags_allow_local_overlays_connected = false` |
| `runtime.fancy_tags_enhanced_rendering` | Allow negotiated enhanced clients to receive authorized static artwork | boolean | `true` | `true` or `false` | boolean flag | restart_required | public | `runtime.fancy_tags_enhanced_rendering = true` |
| `runtime.fancy_tags_import_settle_seconds` | Required stable file interval before an inbox candidate is reviewable | integer | `2` | 0 through 300 | seconds | restart_required | public | `runtime.fancy_tags_import_settle_seconds = 2` |
| `runtime.fancy_tags_maximum_assignments` | Maximum tag assignments | integer | `16384` | 1 through 65536 | count | restart_required | public | `runtime.fancy_tags_maximum_assignments = 16384` |
| `runtime.fancy_tags_maximum_assignments_per_target` | Maximum enabled assignments for one target | integer | `32` | 1 through 1024 | count | restart_required | public | `runtime.fancy_tags_maximum_assignments_per_target = 32` |
| `runtime.fancy_tags_maximum_categories` | Maximum tag categories | integer | `128` | 1 through 512 | count | restart_required | public | `runtime.fancy_tags_maximum_categories = 128` |
| `runtime.fancy_tags_maximum_decoded_bytes` | Maximum decoded rgba bytes | integer | `1048576` | 4096 through 4194304 | bytes | restart_required | public | `runtime.fancy_tags_maximum_decoded_bytes = 1048576` |
| `runtime.fancy_tags_maximum_encoded_bytes` | Maximum canonical encoded image bytes | integer | `262144` | 1024 through 1048576 | bytes | restart_required | public | `runtime.fancy_tags_maximum_encoded_bytes = 262144` |
| `runtime.fancy_tags_maximum_height` | Maximum decoded image height | integer | `64` | 1 through 256 | count | restart_required | public | `runtime.fancy_tags_maximum_height = 64` |
| `runtime.fancy_tags_maximum_import_candidates` | Maximum bounded import inbox candidates | integer | `128` | 1 through 512 | count | restart_required | public | `runtime.fancy_tags_maximum_import_candidates = 128` |
| `runtime.fancy_tags_maximum_pixels` | Maximum decoded image pixels | integer | `16384` | 1 through 65536 | count | restart_required | public | `runtime.fancy_tags_maximum_pixels = 16384` |
| `runtime.fancy_tags_maximum_revisions_per_tag` | Maximum retained artwork revisions per tag | integer | `20` | 1 through 100 | count | restart_required | public | `runtime.fancy_tags_maximum_revisions_per_tag = 20` |
| `runtime.fancy_tags_maximum_store_bytes` | Maximum content addressed object store bytes | integer | `1073741824` | 1048576 through 8589934592 | bytes | restart_required | public | `runtime.fancy_tags_maximum_store_bytes = 1073741824` |
| `runtime.fancy_tags_maximum_tags` | Maximum tag definitions | integer | `1024` | 1 through 4096 | count | restart_required | public | `runtime.fancy_tags_maximum_tags = 1024` |
| `runtime.fancy_tags_maximum_width` | Maximum decoded image width | integer | `256` | 1 through 512 | count | restart_required | public | `runtime.fancy_tags_maximum_width = 256` |
| `runtime.fancy_tags_prototype_enabled` | Enable the Phase 9 bounded static Fancy Tags transport prototype | boolean | `true` | `true` or `false` | boolean flag | restart_required | public | `runtime.fancy_tags_prototype_enabled = true` |
| `runtime.fancy_tags_prototype_maximum_bytes` | Maximum bytes accepted by the prototype tag transfer | integer | `262144` | 1024 through 1048576 | bytes | restart_required | public | `runtime.fancy_tags_prototype_maximum_bytes = 262144` |
| `runtime.fancy_tags_server_inbox_enabled` | Allow the fixed owned import inbox workflow | boolean | `false` | `true` or `false` | boolean flag | restart_required | public | `runtime.fancy_tags_server_inbox_enabled = false` |

## freeze

Controls freeze behavior, presentation, bounds, storage, and diagnostics.

File: `config/sef/modules/freeze.toml`. Schema version: `1`. Documentation version: `2`. Apply class: `live`. Dependencies: `audit`, `commands`, `core`, `gui`, `messages`, `permissions`. Conflicts: none.

Inspect permission: `sef.commands.config.inspect`. Edit permission: `sef.commands.config.edit`. Migration path: inspect the retained legacy `common.toml` with `/sef config migrate dryrun`, then request a revision-bound confirmation with `/sef config migrate apply <expected_revision>`. Legacy cooldown durations move to `sef.cooldown.<action>.<seconds>` permissions instead of module files.

| Setting | Description | Type | Default | Bounds | Unit | Apply class | Privacy | Example |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `module.enabled` | Enables the module after dependency validation. | boolean | `true` | `true` or `false` | boolean flag | live | public | `module.enabled = true` |
| `module.disabled_behavior` | Defines truthful behavior while this module is disabled. | enum | `command_unavailable` | `command_unavailable`, `provider_fallback`, `read_only` | named option | live | public | `module.disabled_behavior = "command_unavailable"` |
| `gui.mode` | Selects enhanced GUI presentation without changing command access. | enum | `inherit` | `command_only`, `gui_preferred`, `inherit`, `off`, `on` | named option | live | public | `gui.mode = "inherit"` |
| `gui.bare_command_opens` | Allows eligible bare command routes to prefer a dedicated workflow. | boolean | `true` | `true` or `false` | boolean flag | live | public | `gui.bare_command_opens = true` |
| `gui.default_page` | Selects the stable default workflow page. | string | `overview` | 1 through 64 characters, pattern `[a-z0-9_]+` | text | live | public | `gui.default_page = "overview"` |
| `limits.maximum_records` | Bounds persisted records owned by this module. | integer | `1000` | 1 through 100000 | count | live | public | `limits.maximum_records = 1000` |
| `limits.maximum_page_size` | Bounds command and GUI result pages. | integer | `45` | 4 through 100 | count | live | public | `limits.maximum_page_size = 45` |
| `rate.maximum_actions_per_minute` | Bounds admission attempts independently of permission cooldown duration. | integer | `30` | 1 through 10000 | actions per minute | live | public | `rate.maximum_actions_per_minute = 30` |
| `storage.retention_days` | Defines bounded data retention where the module owns persistent records. | integer | `90` | 0 through 3650 | days | live | public | `storage.retention_days = 90` |
| `failure.mode` | Defines provider and validation failure behavior. | enum | `fail_closed` | `fail_closed`, `previous_known_good`, `read_only` | named option | live | public | `failure.mode = "fail_closed"` |
| `audit.class` | Defines the minimum audit class for module actions. | enum | `admin_action` | `admin_action`, `destructive`, `metadata_only`, `none` | named option | live | public | `audit.class = "admin_action"` |
| `audit.redaction` | Defines the module redaction class. | enum | `sensitive` | `security_critical`, `sensitive`, `standard` | named option | live | public | `audit.redaction = "sensitive"` |
| `runtime.enable_freeze_system` | Freeze system (/freeze, /unfreeze) | boolean | `true` | `true` or `false` | boolean flag | restart_required | public | `runtime.enable_freeze_system = true` |
| `runtime.freeze_action_blocked_msg` | Message when a frozen player tries to interact/mine/etc | string | `&cYou are frozen and cannot do that.` | 0 through 8192 characters | text | live | public | `runtime.freeze_action_blocked_msg = "&cYou are frozen and cannot do that."` |
| `runtime.freeze_admin_notify_format` | Notification sent to admins when a player is frozen. Placeholders: $player, $admin, $reason, $duration | string | `&e$admin &7has frozen &e$player &7for &e$duration&7. Reason: &f$reason` | 0 through 8192 characters | text | live | public | `runtime.freeze_admin_notify_format = "&e$admin &7has frozen &e$player &7for &e$duration&7. Reason: &f$reason"` |
| `runtime.freeze_allow_chat` | Allow frozen players to chat (so they can respond to the admin) | boolean | `true` | `true` or `false` | boolean flag | live | public | `runtime.freeze_allow_chat = true` |
| `runtime.freeze_command_blocked_msg` | Message when a frozen player tries to use a command | string | `&cYou are frozen and cannot use commands. Please respond in chat.` | 0 through 8192 characters | text | live | public | `runtime.freeze_command_blocked_msg = "&cYou are frozen and cannot use commands. Please respond in chat."` |
| `runtime.freeze_message_to_player` | Message sent to the player when they are frozen. Placeholders: $reason, $admin, $duration | string | `&c&l⚠ YOU HAVE BEEN FROZEN ⚠
&7Reason: &f$reason
&7Frozen by: &e$admin
&7Duration: &e$duration
&7&oPlease respond to the admin in chat.` | 0 through 8192 characters | text | live | public | `runtime.freeze_message_to_player = "&c&l⚠ YOU HAVE BEEN FROZEN ⚠\n&7Reason: &f$reason\n&7Frozen by: &e$admin\n&7Duration: &e$duration\n&7&oPlease respond to the admin in chat."` |
| `runtime.freeze_play_sound` | Play a sound when a player is frozen | boolean | `true` | `true` or `false` | boolean flag | live | public | `runtime.freeze_play_sound = true` |
| `runtime.freeze_reason_format` | Format of the reason displayed. Placeholder: $reason | string | `&c&lFROZEN &7- &f$reason` | 0 through 8192 characters | text | live | public | `runtime.freeze_reason_format = "&c&lFROZEN &7- &f$reason"` |
| `runtime.freeze_reminder_format` | Periodic reminder message to frozen players. Placeholders: $reason, $admin | string | `&c&l⚠ You are still frozen! &7Reason: &f$reason &7- Please respond in chat.` | 0 through 8192 characters | text | live | public | `runtime.freeze_reminder_format = "&c&l⚠ You are still frozen! &7Reason: &f$reason &7- Please respond in chat."` |
| `runtime.freeze_reminder_interval_seconds` | How often (seconds) to remind frozen players (0 = no reminders) | integer | `15` | 0 through 3600 | seconds | live | public | `runtime.freeze_reminder_interval_seconds = 15` |

## gamemode

Controls gamemode behavior, presentation, bounds, storage, and diagnostics.

File: `config/sef/modules/gamemode.toml`. Schema version: `1`. Documentation version: `2`. Apply class: `live`. Dependencies: `commands`, `core`, `gui`, `messages`, `permissions`. Conflicts: none.

Inspect permission: `sef.commands.config.inspect`. Edit permission: `sef.commands.config.edit`. Migration path: inspect the retained legacy `common.toml` with `/sef config migrate dryrun`, then request a revision-bound confirmation with `/sef config migrate apply <expected_revision>`. Legacy cooldown durations move to `sef.cooldown.<action>.<seconds>` permissions instead of module files.

| Setting | Description | Type | Default | Bounds | Unit | Apply class | Privacy | Example |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `module.enabled` | Enables the module after dependency validation. | boolean | `true` | `true` or `false` | boolean flag | live | public | `module.enabled = true` |
| `module.disabled_behavior` | Defines truthful behavior while this module is disabled. | enum | `command_unavailable` | `command_unavailable`, `provider_fallback`, `read_only` | named option | live | public | `module.disabled_behavior = "command_unavailable"` |
| `gui.mode` | Selects enhanced GUI presentation without changing command access. | enum | `inherit` | `command_only`, `gui_preferred`, `inherit`, `off`, `on` | named option | live | public | `gui.mode = "inherit"` |
| `gui.bare_command_opens` | Allows eligible bare command routes to prefer a dedicated workflow. | boolean | `true` | `true` or `false` | boolean flag | live | public | `gui.bare_command_opens = true` |
| `gui.default_page` | Selects the stable default workflow page. | string | `overview` | 1 through 64 characters, pattern `[a-z0-9_]+` | text | live | public | `gui.default_page = "overview"` |
| `limits.maximum_records` | Bounds persisted records owned by this module. | integer | `10000` | 1 through 1000000 | count | live | public | `limits.maximum_records = 10000` |
| `limits.maximum_page_size` | Bounds command and GUI result pages. | integer | `45` | 4 through 100 | count | live | public | `limits.maximum_page_size = 45` |
| `rate.maximum_actions_per_minute` | Bounds admission attempts independently of permission cooldown duration. | integer | `300` | 1 through 10000 | actions per minute | live | public | `rate.maximum_actions_per_minute = 300` |
| `storage.retention_days` | Defines bounded data retention where the module owns persistent records. | integer | `30` | 0 through 3650 | days | live | public | `storage.retention_days = 30` |
| `failure.mode` | Defines provider and validation failure behavior. | enum | `previous_known_good` | `fail_closed`, `previous_known_good`, `read_only` | named option | live | public | `failure.mode = "previous_known_good"` |
| `audit.class` | Defines the minimum audit class for module actions. | enum | `metadata_only` | `admin_action`, `destructive`, `metadata_only`, `none` | named option | live | public | `audit.class = "metadata_only"` |
| `audit.redaction` | Defines the module redaction class. | enum | `standard` | `security_critical`, `sensitive`, `standard` | named option | live | public | `audit.redaction = "standard"` |
| `runtime.enable_gamemode_shortcuts` | Bounded gamemode shortcut family | boolean | `true` | `true` or `false` | boolean flag | restart_required | public | `runtime.enable_gamemode_shortcuts = true` |

## gui

Controls gui behavior, presentation, bounds, storage, and diagnostics.

File: `config/sef/modules/gui.toml`. Schema version: `1`. Documentation version: `2`. Apply class: `live`. Dependencies: `core`. Conflicts: none.

Inspect permission: `sef.commands.config.inspect`. Edit permission: `sef.commands.config.edit`. Migration path: inspect the retained legacy `common.toml` with `/sef config migrate dryrun`, then request a revision-bound confirmation with `/sef config migrate apply <expected_revision>`. Legacy cooldown durations move to `sef.cooldown.<action>.<seconds>` permissions instead of module files.

| Setting | Description | Type | Default | Bounds | Unit | Apply class | Privacy | Example |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `module.enabled` | Enables the module after dependency validation. | boolean | `true` | `true` or `false` | boolean flag | live | public | `module.enabled = true` |
| `module.disabled_behavior` | Defines truthful behavior while this module is disabled. | enum | `command_unavailable` | `command_unavailable`, `provider_fallback`, `read_only` | named option | live | public | `module.disabled_behavior = "command_unavailable"` |
| `gui.mode` | Selects enhanced GUI presentation without changing command access. | enum | `auto` | `auto`, `off`, `on` | named option | live | public | `gui.mode = "auto"` |
| `gui.bare_command_opens` | Allows eligible bare command routes to prefer a dedicated workflow. | boolean | `true` | `true` or `false` | boolean flag | live | public | `gui.bare_command_opens = true` |
| `gui.default_page` | Selects the stable default workflow page. | string | `overview` | 1 through 64 characters, pattern `[a-z0-9_]+` | text | live | public | `gui.default_page = "overview"` |
| `limits.maximum_records` | Bounds persisted records owned by this module. | integer | `10000` | 1 through 1000000 | count | live | public | `limits.maximum_records = 10000` |
| `limits.maximum_page_size` | Bounds command and GUI result pages. | integer | `45` | 4 through 100 | count | live | public | `limits.maximum_page_size = 45` |
| `rate.maximum_actions_per_minute` | Bounds admission attempts independently of permission cooldown duration. | integer | `300` | 1 through 10000 | actions per minute | live | public | `rate.maximum_actions_per_minute = 300` |
| `storage.retention_days` | Defines bounded data retention where the module owns persistent records. | integer | `30` | 0 through 3650 | days | live | public | `storage.retention_days = 30` |
| `failure.mode` | Defines provider and validation failure behavior. | enum | `previous_known_good` | `fail_closed`, `previous_known_good`, `read_only` | named option | live | public | `failure.mode = "previous_known_good"` |
| `audit.class` | Defines the minimum audit class for module actions. | enum | `metadata_only` | `admin_action`, `destructive`, `metadata_only`, `none` | named option | live | public | `audit.class = "metadata_only"` |
| `audit.redaction` | Defines the module redaction class. | enum | `standard` | `security_critical`, `sensitive`, `standard` | named option | live | public | `audit.redaction = "standard"` |
| `sessions.maximum_open_per_player` | Bounds privileged enhanced workflow sessions. | integer | `1` | 1 through 4 | count | live | public | `sessions.maximum_open_per_player = 1` |
| `sessions.timeout_seconds` | Expires inactive enhanced workflow sessions. | integer | `120` | 10 through 1800 | seconds | live | public | `sessions.timeout_seconds = 120` |
| `sessions.maximum_draft_bytes` | Bounds server held typed draft state per player. | integer | `262144` | 4096 through 1048576 | bytes | live | public | `sessions.maximum_draft_bytes = 262144` |
| `reminder.enabled` | Enables the optional client enhancement reminder. | boolean | `true` | `true` or `false` | boolean flag | live | public | `reminder.enabled = true` |
| `reminder.minimum_interval_hours` | Bounds repeated enhancement reminders. | integer | `24` | 1 through 8760 | hours | live | public | `reminder.minimum_interval_hours = 24` |
| `runtime.enable_enhanced_gui` | Enable optional client capability negotiation and enhanced screens | boolean | `false` | `true` or `false` | boolean flag | restart_required | public | `runtime.enable_enhanced_gui = false` |
| `runtime.gui_maximum_panel_entries` | Maximum entries encoded into one server page | integer | `45` | 1 through 100 | count | live | public | `runtime.gui_maximum_panel_entries = 45` |
| `runtime.gui_panel_requests_per_second` | Maximum accepted GUI requests from one connection each second | integer | `20` | 1 through 100 | count | live | public | `runtime.gui_panel_requests_per_second = 20` |
| `runtime.gui_panel_session_seconds` | Lifetime of one server authoritative panel snapshot | integer | `60` | 10 through 600 | seconds | live | public | `runtime.gui_panel_session_seconds = 60` |
| `runtime.gui_reminder_audience` | Reminder audience. all, players, or staff | string | `all` | 0 through 8192 characters | text | live | public | `runtime.gui_reminder_audience = "all"` |
| `runtime.gui_reminder_delay_seconds` | Delay after login before a fallback reminder | integer | `5` | 0 through 3600 | seconds | live | public | `runtime.gui_reminder_delay_seconds = 5` |
| `runtime.gui_reminder_enabled` | Remind command fallback players that the optional client exists | boolean | `true` | `true` or `false` | boolean flag | live | public | `runtime.gui_reminder_enabled = true` |
| `runtime.gui_reminder_frequency_hours` | Minimum hours between reminders. Zero means once per configured revision | integer | `0` | 0 through 8760 | hours | live | public | `runtime.gui_reminder_frequency_hours = 0` |
| `runtime.gui_reminder_revision` | Increment to show a once per revision reminder again | integer | `1` | 1 through 1000000 | count | live | public | `runtime.gui_reminder_revision = 1` |

## homes

Controls homes behavior, presentation, bounds, storage, and diagnostics.

File: `config/sef/modules/homes.toml`. Schema version: `1`. Documentation version: `2`. Apply class: `live`. Dependencies: `commands`, `core`, `gui`, `messages`, `permissions`. Conflicts: none.

Inspect permission: `sef.commands.config.inspect`. Edit permission: `sef.commands.config.edit`. Migration path: inspect the retained legacy `common.toml` with `/sef config migrate dryrun`, then request a revision-bound confirmation with `/sef config migrate apply <expected_revision>`. Legacy cooldown durations move to `sef.cooldown.<action>.<seconds>` permissions instead of module files.

| Setting | Description | Type | Default | Bounds | Unit | Apply class | Privacy | Example |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `module.enabled` | Enables the module after dependency validation. | boolean | `true` | `true` or `false` | boolean flag | live | public | `module.enabled = true` |
| `module.disabled_behavior` | Defines truthful behavior while this module is disabled. | enum | `command_unavailable` | `command_unavailable`, `provider_fallback`, `read_only` | named option | live | public | `module.disabled_behavior = "command_unavailable"` |
| `gui.mode` | Selects enhanced GUI presentation without changing command access. | enum | `inherit` | `command_only`, `gui_preferred`, `inherit`, `off`, `on` | named option | live | public | `gui.mode = "inherit"` |
| `gui.bare_command_opens` | Allows eligible bare command routes to prefer a dedicated workflow. | boolean | `true` | `true` or `false` | boolean flag | live | public | `gui.bare_command_opens = true` |
| `gui.default_page` | Selects the stable default workflow page. | string | `overview` | 1 through 64 characters, pattern `[a-z0-9_]+` | text | live | public | `gui.default_page = "overview"` |
| `limits.maximum_records` | Bounds persisted records owned by this module. | integer | `10000` | 1 through 1000000 | count | live | public | `limits.maximum_records = 10000` |
| `limits.maximum_page_size` | Bounds command and GUI result pages. | integer | `45` | 4 through 100 | count | live | public | `limits.maximum_page_size = 45` |
| `rate.maximum_actions_per_minute` | Bounds admission attempts independently of permission cooldown duration. | integer | `300` | 1 through 10000 | actions per minute | live | public | `rate.maximum_actions_per_minute = 300` |
| `storage.retention_days` | Defines bounded data retention where the module owns persistent records. | integer | `30` | 0 through 3650 | days | live | public | `storage.retention_days = 30` |
| `failure.mode` | Defines provider and validation failure behavior. | enum | `previous_known_good` | `fail_closed`, `previous_known_good`, `read_only` | named option | live | public | `failure.mode = "previous_known_good"` |
| `audit.class` | Defines the minimum audit class for module actions. | enum | `metadata_only` | `admin_action`, `destructive`, `metadata_only`, `none` | named option | live | public | `audit.class = "metadata_only"` |
| `audit.redaction` | Defines the module redaction class. | enum | `standard` | `security_critical`, `sensitive`, `standard` | named option | live | public | `audit.redaction = "standard"` |
| `runtime.default_home_limit` | Default home quota before permission or metadata tiers | integer | `1` | 0 through 1000 | count | live | public | `runtime.default_home_limit = 1` |
| `runtime.default_home_name` | Home name used when no name is supplied | string | `home` | 0 through 8192 characters | text | live | public | `runtime.default_home_name = "home"` |
| `runtime.default_home_per_dimension_limit` | Default home quota in one dimension | integer | `1000` | 0 through 1000 | count | live | public | `runtime.default_home_per_dimension_limit = 1000` |
| `runtime.enable_homes` | Home commands | boolean | `true` | `true` or `false` | boolean flag | restart_required | public | `runtime.enable_homes = true` |

## hud

Controls hud behavior, presentation, bounds, storage, and diagnostics.

File: `config/sef/modules/hud.toml`. Schema version: `1`. Documentation version: `2`. Apply class: `live`. Dependencies: `core`. Conflicts: none.

Inspect permission: `sef.commands.config.inspect`. Edit permission: `sef.commands.config.edit`. Migration path: inspect the retained legacy `common.toml` with `/sef config migrate dryrun`, then request a revision-bound confirmation with `/sef config migrate apply <expected_revision>`. Legacy cooldown durations move to `sef.cooldown.<action>.<seconds>` permissions instead of module files.

| Setting | Description | Type | Default | Bounds | Unit | Apply class | Privacy | Example |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `module.enabled` | Enables the module after dependency validation. | boolean | `true` | `true` or `false` | boolean flag | live | public | `module.enabled = true` |
| `module.disabled_behavior` | Defines truthful behavior while this module is disabled. | enum | `command_unavailable` | `command_unavailable`, `provider_fallback`, `read_only` | named option | live | public | `module.disabled_behavior = "command_unavailable"` |
| `gui.mode` | Selects enhanced GUI presentation without changing command access. | enum | `inherit` | `command_only`, `gui_preferred`, `inherit`, `off`, `on` | named option | live | public | `gui.mode = "inherit"` |
| `gui.bare_command_opens` | Allows eligible bare command routes to prefer a dedicated workflow. | boolean | `true` | `true` or `false` | boolean flag | live | public | `gui.bare_command_opens = true` |
| `gui.default_page` | Selects the stable default workflow page. | string | `overview` | 1 through 64 characters, pattern `[a-z0-9_]+` | text | live | public | `gui.default_page = "overview"` |
| `limits.maximum_records` | Bounds persisted records owned by this module. | integer | `10000` | 1 through 1000000 | count | live | public | `limits.maximum_records = 10000` |
| `limits.maximum_page_size` | Bounds command and GUI result pages. | integer | `45` | 4 through 100 | count | live | public | `limits.maximum_page_size = 45` |
| `rate.maximum_actions_per_minute` | Bounds admission attempts independently of permission cooldown duration. | integer | `300` | 1 through 10000 | actions per minute | live | public | `rate.maximum_actions_per_minute = 300` |
| `storage.retention_days` | Defines bounded data retention where the module owns persistent records. | integer | `30` | 0 through 3650 | days | live | public | `storage.retention_days = 30` |
| `failure.mode` | Defines provider and validation failure behavior. | enum | `previous_known_good` | `fail_closed`, `previous_known_good`, `read_only` | named option | live | public | `failure.mode = "previous_known_good"` |
| `audit.class` | Defines the minimum audit class for module actions. | enum | `metadata_only` | `admin_action`, `destructive`, `metadata_only`, `none` | named option | live | public | `audit.class = "metadata_only"` |
| `audit.redaction` | Defines the module redaction class. | enum | `standard` | `security_critical`, `sensitive`, `standard` | named option | live | public | `audit.redaction = "standard"` |
| `runtime.enable_custom_tab_header_footer` | Server-side custom tab header/footer rendering (opt-in; configure formats in the tab section) | boolean | `false` | `true` or `false` | boolean flag | restart_required | public | `runtime.enable_custom_tab_header_footer = false` |
| `runtime.enable_tab_list_integration` | Custom tab list information | boolean | `true` | `true` or `false` | boolean flag | restart_required | public | `runtime.enable_tab_list_integration = true` |
| `runtime.max_prefixes_displayed` | Maximum number of prefixes to show (weighted order) | integer | `1` | 0 through 50 | count | live | public | `runtime.max_prefixes_displayed = 1` |
| `runtime.max_suffixes_displayed` | Maximum number of suffixes to show (weighted order) | integer | `1` | 0 through 50 | count | live | public | `runtime.max_suffixes_displayed = 1` |
| `runtime.tab_footer_format` | Tab footer format (placeholders: {server_ip}, {online}, {max}) | string | `` | 0 through 8192 characters | text | live | public | `runtime.tab_footer_format = ""` |
| `runtime.tab_header_format` | Tab header format (placeholders: {server_ip}, {online}, {max}) | string | `` | 0 through 8192 characters | text | live | public | `runtime.tab_header_format = ""` |
| `runtime.tab_update_interval_ticks` | Minimum ticks between tab header and footer refreshes | integer | `20` | 1 through 1200 | count | live | public | `runtime.tab_update_interval_ticks = 20` |

## integrations

Controls integrations behavior, presentation, bounds, storage, and diagnostics.

File: `config/sef/modules/integrations.toml`. Schema version: `1`. Documentation version: `2`. Apply class: `restart_required`. Dependencies: `core`. Conflicts: none.

Inspect permission: `sef.commands.config.inspect`. Edit permission: `sef.commands.config.edit`. Migration path: inspect the retained legacy `common.toml` with `/sef config migrate dryrun`, then request a revision-bound confirmation with `/sef config migrate apply <expected_revision>`. Legacy cooldown durations move to `sef.cooldown.<action>.<seconds>` permissions instead of module files.

| Setting | Description | Type | Default | Bounds | Unit | Apply class | Privacy | Example |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `module.enabled` | Enables the module after dependency validation. | boolean | `true` | `true` or `false` | boolean flag | live | public | `module.enabled = true` |
| `module.disabled_behavior` | Defines truthful behavior while this module is disabled. | enum | `command_unavailable` | `command_unavailable`, `provider_fallback`, `read_only` | named option | live | public | `module.disabled_behavior = "command_unavailable"` |
| `gui.mode` | Selects enhanced GUI presentation without changing command access. | enum | `inherit` | `command_only`, `gui_preferred`, `inherit`, `off`, `on` | named option | live | public | `gui.mode = "inherit"` |
| `gui.bare_command_opens` | Allows eligible bare command routes to prefer a dedicated workflow. | boolean | `false` | `true` or `false` | boolean flag | live | public | `gui.bare_command_opens = false` |
| `gui.default_page` | Selects the stable default workflow page. | string | `overview` | 1 through 64 characters, pattern `[a-z0-9_]+` | text | live | public | `gui.default_page = "overview"` |
| `limits.maximum_records` | Bounds persisted records owned by this module. | integer | `10000` | 1 through 1000000 | count | live | public | `limits.maximum_records = 10000` |
| `limits.maximum_page_size` | Bounds command and GUI result pages. | integer | `45` | 4 through 100 | count | live | public | `limits.maximum_page_size = 45` |
| `rate.maximum_actions_per_minute` | Bounds admission attempts independently of permission cooldown duration. | integer | `300` | 1 through 10000 | actions per minute | live | public | `rate.maximum_actions_per_minute = 300` |
| `storage.retention_days` | Defines bounded data retention where the module owns persistent records. | integer | `30` | 0 through 3650 | days | live | public | `storage.retention_days = 30` |
| `failure.mode` | Defines provider and validation failure behavior. | enum | `previous_known_good` | `fail_closed`, `previous_known_good`, `read_only` | named option | live | public | `failure.mode = "previous_known_good"` |
| `audit.class` | Defines the minimum audit class for module actions. | enum | `metadata_only` | `admin_action`, `destructive`, `metadata_only`, `none` | named option | live | public | `audit.class = "metadata_only"` |
| `audit.redaction` | Defines the module redaction class. | enum | `standard` | `security_critical`, `sensitive`, `standard` | named option | live | public | `audit.redaction = "standard"` |
| `providers.timeout_milliseconds` | Bounds optional provider calls. | integer | `3000` | 100 through 30000 | milliseconds | live | public | `providers.timeout_milliseconds = 3000` |
| `runtime.enable_discord_bot_integration` | Discord bot integration (off by default; requires a bot token) | boolean | `false` | `true` or `false` | boolean flag | restart_required | public | `runtime.enable_discord_bot_integration = false` |
| `runtime.enable_ftb_essentials` | Enables or disables FTB essentials nickname integration | boolean | `true` | `true` or `false` | boolean flag | restart_required | public | `runtime.enable_ftb_essentials = true` |
| `runtime.enable_luck_perms` | Enables or disables LuckPerms integration | boolean | `true` | `true` or `false` | boolean flag | restart_required | public | `runtime.enable_luck_perms = true` |

## inventory

Controls inventory behavior, presentation, bounds, storage, and diagnostics.

File: `config/sef/modules/inventory.toml`. Schema version: `1`. Documentation version: `2`. Apply class: `live`. Dependencies: `commands`, `core`, `gui`, `messages`, `permissions`. Conflicts: none.

Inspect permission: `sef.commands.config.inspect`. Edit permission: `sef.commands.config.edit`. Migration path: inspect the retained legacy `common.toml` with `/sef config migrate dryrun`, then request a revision-bound confirmation with `/sef config migrate apply <expected_revision>`. Legacy cooldown durations move to `sef.cooldown.<action>.<seconds>` permissions instead of module files.

| Setting | Description | Type | Default | Bounds | Unit | Apply class | Privacy | Example |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `module.enabled` | Enables the module after dependency validation. | boolean | `true` | `true` or `false` | boolean flag | live | public | `module.enabled = true` |
| `module.disabled_behavior` | Defines truthful behavior while this module is disabled. | enum | `command_unavailable` | `command_unavailable`, `provider_fallback`, `read_only` | named option | live | public | `module.disabled_behavior = "command_unavailable"` |
| `gui.mode` | Selects enhanced GUI presentation without changing command access. | enum | `inherit` | `command_only`, `gui_preferred`, `inherit`, `off`, `on` | named option | live | public | `gui.mode = "inherit"` |
| `gui.bare_command_opens` | Allows eligible bare command routes to prefer a dedicated workflow. | boolean | `true` | `true` or `false` | boolean flag | live | public | `gui.bare_command_opens = true` |
| `gui.default_page` | Selects the stable default workflow page. | string | `overview` | 1 through 64 characters, pattern `[a-z0-9_]+` | text | live | public | `gui.default_page = "overview"` |
| `limits.maximum_records` | Bounds persisted records owned by this module. | integer | `10000` | 1 through 1000000 | count | live | public | `limits.maximum_records = 10000` |
| `limits.maximum_page_size` | Bounds command and GUI result pages. | integer | `45` | 4 through 100 | count | live | public | `limits.maximum_page_size = 45` |
| `rate.maximum_actions_per_minute` | Bounds admission attempts independently of permission cooldown duration. | integer | `300` | 1 through 10000 | actions per minute | live | public | `rate.maximum_actions_per_minute = 300` |
| `storage.retention_days` | Defines bounded data retention where the module owns persistent records. | integer | `30` | 0 through 3650 | days | live | public | `storage.retention_days = 30` |
| `failure.mode` | Defines provider and validation failure behavior. | enum | `previous_known_good` | `fail_closed`, `previous_known_good`, `read_only` | named option | live | public | `failure.mode = "previous_known_good"` |
| `audit.class` | Defines the minimum audit class for module actions. | enum | `metadata_only` | `admin_action`, `destructive`, `metadata_only`, `none` | named option | live | public | `audit.class = "metadata_only"` |
| `audit.redaction` | Defines the module redaction class. | enum | `standard` | `security_critical`, `sensitive`, `standard` | named option | live | public | `audit.redaction = "standard"` |
| `runtime.enable_inv_see` | Inventory viewer (/invsee) | boolean | `true` | `true` or `false` | boolean flag | restart_required | public | `runtime.enable_inv_see = true` |
| `runtime.enable_inventory_utilities` | Inventory, ender chest, disposal, and safe item utility commands | boolean | `true` | `true` or `false` | boolean flag | restart_required | public | `runtime.enable_inventory_utilities = true` |
| `runtime.inv_see_armor_label` | Name shown on the glass pane separator for armor section | string | `&9Armor` | 0 through 8192 characters | text | live | public | `runtime.inv_see_armor_label = "&9Armor"` |
| `runtime.inv_see_audit_modifications` | Write inventory modification metadata to the structured security audit | boolean | `true` | `true` or `false` | boolean flag | live | sensitive | `runtime.inv_see_audit_modifications = true` |
| `runtime.inv_see_curios_label` | Name shown on the glass pane separator for curios section | string | `&dCurios` | 0 through 8192 characters | text | live | public | `runtime.inv_see_curios_label = "&dCurios"` |
| `runtime.inv_see_disable_ftb_invsee` | Register the SEF route when another mod already owns invsee | boolean | `true` | `true` or `false` | boolean flag | restart_required | public | `runtime.inv_see_disable_ftb_invsee = true` |
| `runtime.inv_see_main_inv_label` | Name shown on the glass pane separator for main inventory section | string | `&aInventory` | 0 through 8192 characters | text | live | public | `runtime.inv_see_main_inv_label = "&aInventory"` |
| `runtime.inv_see_next_page_label` | Name shown on the next page arrow item | string | `&eNext Page >>>` | 0 through 8192 characters | text | live | public | `runtime.inv_see_next_page_label = "&eNext Page >>>"` |
| `runtime.inv_see_offhand_label` | Name shown on the glass pane separator for offhand section | string | `&6Offhand` | 0 through 8192 characters | text | live | public | `runtime.inv_see_offhand_label = "&6Offhand"` |
| `runtime.inv_see_offline_enabled` | Enable the versioned offline player inventory adapter | boolean | `false` | `true` or `false` | boolean flag | live | public | `runtime.inv_see_offline_enabled = false` |
| `runtime.inv_see_offline_maximum_backups` | Maximum retained offline inventory recovery backups per player | integer | `16` | 1 through 128 | count | live | public | `runtime.inv_see_offline_maximum_backups = 16` |
| `runtime.inv_see_offline_maximum_file_ki_b` | Maximum compressed offline player data file size | integer | `4096` | 64 through 16384 | count | live | public | `runtime.inv_see_offline_maximum_file_ki_b = 4096` |
| `runtime.inv_see_prev_page_label` | Name shown on the previous page arrow item | string | `&e<<< Previous Page` | 0 through 8192 characters | text | live | public | `runtime.inv_see_prev_page_label = "&e<<< Previous Page"` |
| `runtime.inv_see_read_only` | When true, players cannot move items in the InvSee GUI (view-only mode) | boolean | `false` | `true` or `false` | boolean flag | live | public | `runtime.inv_see_read_only = false` |
| `runtime.inv_see_title` | Title of the InvSee GUI. Placeholder: $player | string | `&e$player's Inventory` | 0 through 8192 characters | text | live | public | `runtime.inv_see_title = "&e$player's Inventory"` |

## inventory_lock

Controls inventory lock behavior, presentation, bounds, storage, and diagnostics.

File: `config/sef/modules/inventory_lock.toml`. Schema version: `1`. Documentation version: `2`. Apply class: `live`. Dependencies: `audit`, `commands`, `core`, `gui`, `messages`, `permissions`. Conflicts: none.

Inspect permission: `sef.commands.config.inspect`. Edit permission: `sef.commands.config.edit`. Migration path: inspect the retained legacy `common.toml` with `/sef config migrate dryrun`, then request a revision-bound confirmation with `/sef config migrate apply <expected_revision>`. Legacy cooldown durations move to `sef.cooldown.<action>.<seconds>` permissions instead of module files.

| Setting | Description | Type | Default | Bounds | Unit | Apply class | Privacy | Example |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `module.enabled` | Enables the module after dependency validation. | boolean | `true` | `true` or `false` | boolean flag | live | public | `module.enabled = true` |
| `module.disabled_behavior` | Defines truthful behavior while this module is disabled. | enum | `command_unavailable` | `command_unavailable`, `provider_fallback`, `read_only` | named option | live | public | `module.disabled_behavior = "command_unavailable"` |
| `gui.mode` | Selects enhanced GUI presentation without changing command access. | enum | `inherit` | `command_only`, `gui_preferred`, `inherit`, `off`, `on` | named option | live | public | `gui.mode = "inherit"` |
| `gui.bare_command_opens` | Allows eligible bare command routes to prefer a dedicated workflow. | boolean | `true` | `true` or `false` | boolean flag | live | public | `gui.bare_command_opens = true` |
| `gui.default_page` | Selects the stable default workflow page. | string | `overview` | 1 through 64 characters, pattern `[a-z0-9_]+` | text | live | public | `gui.default_page = "overview"` |
| `limits.maximum_records` | Bounds persisted records owned by this module. | integer | `1000` | 1 through 100000 | count | live | public | `limits.maximum_records = 1000` |
| `limits.maximum_page_size` | Bounds command and GUI result pages. | integer | `45` | 4 through 100 | count | live | public | `limits.maximum_page_size = 45` |
| `rate.maximum_actions_per_minute` | Bounds admission attempts independently of permission cooldown duration. | integer | `30` | 1 through 10000 | actions per minute | live | public | `rate.maximum_actions_per_minute = 30` |
| `storage.retention_days` | Defines bounded data retention where the module owns persistent records. | integer | `30` | 0 through 3650 | days | live | public | `storage.retention_days = 30` |
| `failure.mode` | Defines provider and validation failure behavior. | enum | `fail_closed` | `fail_closed`, `previous_known_good`, `read_only` | named option | live | public | `failure.mode = "fail_closed"` |
| `audit.class` | Defines the minimum audit class for module actions. | enum | `admin_action` | `admin_action`, `destructive`, `metadata_only`, `none` | named option | live | public | `audit.class = "admin_action"` |
| `audit.redaction` | Defines the module redaction class. | enum | `standard` | `security_critical`, `sensitive`, `standard` | named option | live | public | `audit.redaction = "standard"` |
| `runtime.enable_inv_lock` | Inventory lock (/invlock) | boolean | `true` | `true` or `false` | boolean flag | restart_required | public | `runtime.enable_inv_lock = true` |
| `runtime.inv_lock_admin_lock_msg` | Message shown to admin when locking. Placeholder: $player | string | `&eLocked inventory for $player.` | 0 through 8192 characters | text | live | public | `runtime.inv_lock_admin_lock_msg = "&eLocked inventory for $player."` |
| `runtime.inv_lock_admin_unlock_msg` | Message shown to admin when unlocking. Placeholder: $player | string | `&eUnlocked inventory for $player.` | 0 through 8192 characters | text | live | public | `runtime.inv_lock_admin_unlock_msg = "&eUnlocked inventory for $player."` |
| `runtime.inv_lock_blocked_msg` | Message shown when a locked player tries to use their inventory | string | `&cYour inventory is locked.` | 0 through 8192 characters | text | live | public | `runtime.inv_lock_blocked_msg = "&cYour inventory is locked."` |
| `runtime.inv_lock_locked_msg` | Message shown to the player when their inventory is locked. Placeholder: $admin | string | `&c$admin has locked your inventory.` | 0 through 8192 characters | text | live | public | `runtime.inv_lock_locked_msg = "&c$admin has locked your inventory."` |
| `runtime.inv_lock_unlocked_msg` | Message shown to the player when their inventory is unlocked. Placeholder: $admin | string | `&a$admin has unlocked your inventory.` | 0 through 8192 characters | text | live | public | `runtime.inv_lock_unlocked_msg = "&a$admin has unlocked your inventory."` |

## items

Controls items behavior, presentation, bounds, storage, and diagnostics.

File: `config/sef/modules/items.toml`. Schema version: `1`. Documentation version: `2`. Apply class: `live`. Dependencies: `commands`, `core`, `gui`, `messages`, `permissions`. Conflicts: none.

Inspect permission: `sef.commands.config.inspect`. Edit permission: `sef.commands.config.edit`. Migration path: inspect the retained legacy `common.toml` with `/sef config migrate dryrun`, then request a revision-bound confirmation with `/sef config migrate apply <expected_revision>`. Legacy cooldown durations move to `sef.cooldown.<action>.<seconds>` permissions instead of module files.

| Setting | Description | Type | Default | Bounds | Unit | Apply class | Privacy | Example |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `module.enabled` | Enables the module after dependency validation. | boolean | `true` | `true` or `false` | boolean flag | live | public | `module.enabled = true` |
| `module.disabled_behavior` | Defines truthful behavior while this module is disabled. | enum | `command_unavailable` | `command_unavailable`, `provider_fallback`, `read_only` | named option | live | public | `module.disabled_behavior = "command_unavailable"` |
| `gui.mode` | Selects enhanced GUI presentation without changing command access. | enum | `inherit` | `command_only`, `gui_preferred`, `inherit`, `off`, `on` | named option | live | public | `gui.mode = "inherit"` |
| `gui.bare_command_opens` | Allows eligible bare command routes to prefer a dedicated workflow. | boolean | `true` | `true` or `false` | boolean flag | live | public | `gui.bare_command_opens = true` |
| `gui.default_page` | Selects the stable default workflow page. | string | `overview` | 1 through 64 characters, pattern `[a-z0-9_]+` | text | live | public | `gui.default_page = "overview"` |
| `limits.maximum_records` | Bounds persisted records owned by this module. | integer | `10000` | 1 through 1000000 | count | live | public | `limits.maximum_records = 10000` |
| `limits.maximum_page_size` | Bounds command and GUI result pages. | integer | `45` | 4 through 100 | count | live | public | `limits.maximum_page_size = 45` |
| `rate.maximum_actions_per_minute` | Bounds admission attempts independently of permission cooldown duration. | integer | `300` | 1 through 10000 | actions per minute | live | public | `rate.maximum_actions_per_minute = 300` |
| `storage.retention_days` | Defines bounded data retention where the module owns persistent records. | integer | `30` | 0 through 3650 | days | live | public | `storage.retention_days = 30` |
| `failure.mode` | Defines provider and validation failure behavior. | enum | `previous_known_good` | `fail_closed`, `previous_known_good`, `read_only` | named option | live | public | `failure.mode = "previous_known_good"` |
| `audit.class` | Defines the minimum audit class for module actions. | enum | `metadata_only` | `admin_action`, `destructive`, `metadata_only`, `none` | named option | live | public | `audit.class = "metadata_only"` |
| `audit.redaction` | Defines the module redaction class. | enum | `standard` | `security_critical`, `sensitive`, `standard` | named option | live | public | `audit.redaction = "standard"` |
| `runtime.enable_item_shortcut` | Bounded self only item shortcut | boolean | `true` | `true` or `false` | boolean flag | restart_required | public | `runtime.enable_item_shortcut = true` |
| `runtime.item_give_maximum_amount` | Maximum amount accepted by the self only item shortcut | integer | `64` | 1 through 6400 | count | live | public | `runtime.item_give_maximum_amount = 64` |

## jails

Controls jails behavior, presentation, bounds, storage, and diagnostics.

File: `config/sef/modules/jails.toml`. Schema version: `1`. Documentation version: `2`. Apply class: `live`. Dependencies: `audit`, `commands`, `core`, `gui`, `messages`, `permissions`. Conflicts: none.

Inspect permission: `sef.commands.config.inspect`. Edit permission: `sef.commands.config.edit`. Migration path: inspect the retained legacy `common.toml` with `/sef config migrate dryrun`, then request a revision-bound confirmation with `/sef config migrate apply <expected_revision>`. Legacy cooldown durations move to `sef.cooldown.<action>.<seconds>` permissions instead of module files.

| Setting | Description | Type | Default | Bounds | Unit | Apply class | Privacy | Example |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `module.enabled` | Enables the module after dependency validation. | boolean | `true` | `true` or `false` | boolean flag | live | public | `module.enabled = true` |
| `module.disabled_behavior` | Defines truthful behavior while this module is disabled. | enum | `command_unavailable` | `command_unavailable`, `provider_fallback`, `read_only` | named option | live | public | `module.disabled_behavior = "command_unavailable"` |
| `gui.mode` | Selects enhanced GUI presentation without changing command access. | enum | `inherit` | `command_only`, `gui_preferred`, `inherit`, `off`, `on` | named option | live | public | `gui.mode = "inherit"` |
| `gui.bare_command_opens` | Allows eligible bare command routes to prefer a dedicated workflow. | boolean | `true` | `true` or `false` | boolean flag | live | public | `gui.bare_command_opens = true` |
| `gui.default_page` | Selects the stable default workflow page. | string | `overview` | 1 through 64 characters, pattern `[a-z0-9_]+` | text | live | public | `gui.default_page = "overview"` |
| `limits.maximum_records` | Bounds persisted records owned by this module. | integer | `1000` | 1 through 100000 | count | live | public | `limits.maximum_records = 1000` |
| `limits.maximum_page_size` | Bounds command and GUI result pages. | integer | `45` | 4 through 100 | count | live | public | `limits.maximum_page_size = 45` |
| `rate.maximum_actions_per_minute` | Bounds admission attempts independently of permission cooldown duration. | integer | `30` | 1 through 10000 | actions per minute | live | public | `rate.maximum_actions_per_minute = 30` |
| `storage.retention_days` | Defines bounded data retention where the module owns persistent records. | integer | `90` | 0 through 3650 | days | live | public | `storage.retention_days = 90` |
| `failure.mode` | Defines provider and validation failure behavior. | enum | `fail_closed` | `fail_closed`, `previous_known_good`, `read_only` | named option | live | public | `failure.mode = "fail_closed"` |
| `audit.class` | Defines the minimum audit class for module actions. | enum | `admin_action` | `admin_action`, `destructive`, `metadata_only`, `none` | named option | live | public | `audit.class = "admin_action"` |
| `audit.redaction` | Defines the module redaction class. | enum | `sensitive` | `security_critical`, `sensitive`, `standard` | named option | live | public | `audit.redaction = "sensitive"` |
| `runtime.enable_jails` | Persistent jail definitions and sentences | boolean | `true` | `true` or `false` | boolean flag | restart_required | public | `runtime.enable_jails = true` |

## kicks

Controls kicks behavior, presentation, bounds, storage, and diagnostics.

File: `config/sef/modules/kicks.toml`. Schema version: `1`. Documentation version: `2`. Apply class: `live`. Dependencies: `audit`, `commands`, `core`, `gui`, `messages`, `permissions`. Conflicts: none.

Inspect permission: `sef.commands.config.inspect`. Edit permission: `sef.commands.config.edit`. Migration path: inspect the retained legacy `common.toml` with `/sef config migrate dryrun`, then request a revision-bound confirmation with `/sef config migrate apply <expected_revision>`. Legacy cooldown durations move to `sef.cooldown.<action>.<seconds>` permissions instead of module files.

| Setting | Description | Type | Default | Bounds | Unit | Apply class | Privacy | Example |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `module.enabled` | Enables the module after dependency validation. | boolean | `true` | `true` or `false` | boolean flag | live | public | `module.enabled = true` |
| `module.disabled_behavior` | Defines truthful behavior while this module is disabled. | enum | `command_unavailable` | `command_unavailable`, `provider_fallback`, `read_only` | named option | live | public | `module.disabled_behavior = "command_unavailable"` |
| `gui.mode` | Selects enhanced GUI presentation without changing command access. | enum | `inherit` | `command_only`, `gui_preferred`, `inherit`, `off`, `on` | named option | live | public | `gui.mode = "inherit"` |
| `gui.bare_command_opens` | Allows eligible bare command routes to prefer a dedicated workflow. | boolean | `true` | `true` or `false` | boolean flag | live | public | `gui.bare_command_opens = true` |
| `gui.default_page` | Selects the stable default workflow page. | string | `overview` | 1 through 64 characters, pattern `[a-z0-9_]+` | text | live | public | `gui.default_page = "overview"` |
| `limits.maximum_records` | Bounds persisted records owned by this module. | integer | `1000` | 1 through 100000 | count | live | public | `limits.maximum_records = 1000` |
| `limits.maximum_page_size` | Bounds command and GUI result pages. | integer | `45` | 4 through 100 | count | live | public | `limits.maximum_page_size = 45` |
| `rate.maximum_actions_per_minute` | Bounds admission attempts independently of permission cooldown duration. | integer | `30` | 1 through 10000 | actions per minute | live | public | `rate.maximum_actions_per_minute = 30` |
| `storage.retention_days` | Defines bounded data retention where the module owns persistent records. | integer | `90` | 0 through 3650 | days | live | public | `storage.retention_days = 90` |
| `failure.mode` | Defines provider and validation failure behavior. | enum | `fail_closed` | `fail_closed`, `previous_known_good`, `read_only` | named option | live | public | `failure.mode = "fail_closed"` |
| `audit.class` | Defines the minimum audit class for module actions. | enum | `admin_action` | `admin_action`, `destructive`, `metadata_only`, `none` | named option | live | public | `audit.class = "admin_action"` |
| `audit.redaction` | Defines the module redaction class. | enum | `sensitive` | `security_critical`, `sensitive`, `standard` | named option | live | public | `audit.redaction = "sensitive"` |

## kits

Controls kits behavior, presentation, bounds, storage, and diagnostics.

File: `config/sef/modules/kits.toml`. Schema version: `1`. Documentation version: `2`. Apply class: `live`. Dependencies: `commands`, `core`, `gui`, `messages`, `permissions`. Conflicts: none.

Inspect permission: `sef.commands.config.inspect`. Edit permission: `sef.commands.config.edit`. Migration path: inspect the retained legacy `common.toml` with `/sef config migrate dryrun`, then request a revision-bound confirmation with `/sef config migrate apply <expected_revision>`. Legacy cooldown durations move to `sef.cooldown.<action>.<seconds>` permissions instead of module files.

| Setting | Description | Type | Default | Bounds | Unit | Apply class | Privacy | Example |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `module.enabled` | Enables the module after dependency validation. | boolean | `true` | `true` or `false` | boolean flag | live | public | `module.enabled = true` |
| `module.disabled_behavior` | Defines truthful behavior while this module is disabled. | enum | `command_unavailable` | `command_unavailable`, `provider_fallback`, `read_only` | named option | live | public | `module.disabled_behavior = "command_unavailable"` |
| `gui.mode` | Selects enhanced GUI presentation without changing command access. | enum | `inherit` | `command_only`, `gui_preferred`, `inherit`, `off`, `on` | named option | live | public | `gui.mode = "inherit"` |
| `gui.bare_command_opens` | Allows eligible bare command routes to prefer a dedicated workflow. | boolean | `true` | `true` or `false` | boolean flag | live | public | `gui.bare_command_opens = true` |
| `gui.default_page` | Selects the stable default workflow page. | string | `overview` | 1 through 64 characters, pattern `[a-z0-9_]+` | text | live | public | `gui.default_page = "overview"` |
| `limits.maximum_records` | Bounds persisted records owned by this module. | integer | `10000` | 1 through 1000000 | count | live | public | `limits.maximum_records = 10000` |
| `limits.maximum_page_size` | Bounds command and GUI result pages. | integer | `45` | 4 through 100 | count | live | public | `limits.maximum_page_size = 45` |
| `rate.maximum_actions_per_minute` | Bounds admission attempts independently of permission cooldown duration. | integer | `300` | 1 through 10000 | actions per minute | live | public | `rate.maximum_actions_per_minute = 300` |
| `storage.retention_days` | Defines bounded data retention where the module owns persistent records. | integer | `30` | 0 through 3650 | days | live | public | `storage.retention_days = 30` |
| `failure.mode` | Defines provider and validation failure behavior. | enum | `previous_known_good` | `fail_closed`, `previous_known_good`, `read_only` | named option | live | public | `failure.mode = "previous_known_good"` |
| `audit.class` | Defines the minimum audit class for module actions. | enum | `metadata_only` | `admin_action`, `destructive`, `metadata_only`, `none` | named option | live | public | `audit.class = "metadata_only"` |
| `audit.redaction` | Defines the module redaction class. | enum | `standard` | `security_critical`, `sensitive`, `standard` | named option | live | public | `audit.redaction = "standard"` |
| `runtime.enable_kits` | Versioned item kit repository and commands | boolean | `true` | `true` or `false` | boolean flag | restart_required | public | `runtime.enable_kits = true` |
| `runtime.kit_drop_overflow` | Drop kit overflow into the world. Disabled keeps grants atomic. | boolean | `false` | `true` or `false` | boolean flag | live | public | `runtime.kit_drop_overflow = false` |
| `runtime.kit_require_per_kit_permission` | Require each kit permission through LuckPerms when available | boolean | `false` | `true` or `false` | boolean flag | live | public | `runtime.kit_require_per_kit_permission = false` |
| `runtime.maximum_kit_items` | Maximum item stacks in one kit | integer | `256` | 1 through 1024 | count | restart_required | public | `runtime.maximum_kit_items = 256` |
| `runtime.maximum_kit_uses_per_player` | Maximum retained kit use records per player | integer | `256` | 1 through 1024 | count | restart_required | public | `runtime.maximum_kit_uses_per_player = 256` |
| `runtime.maximum_kits` | Maximum stored kit definitions | integer | `128` | 1 through 1024 | count | restart_required | public | `runtime.maximum_kits = 128` |

## logger

Controls logger behavior, presentation, bounds, storage, and diagnostics.

File: `config/sef/modules/logger.toml`. Schema version: `1`. Documentation version: `2`. Apply class: `live`. Dependencies: `audit`, `commands`, `core`, `messages`, `permissions`. Conflicts: none.

Inspect permission: `sef.commands.config.inspect`. Edit permission: `sef.commands.config.edit`. Migration path: inspect the retained legacy `common.toml` with `/sef config migrate dryrun`, then request a revision-bound confirmation with `/sef config migrate apply <expected_revision>`. Legacy cooldown durations move to `sef.cooldown.<action>.<seconds>` permissions instead of module files.

| Setting | Description | Type | Default | Bounds | Unit | Apply class | Privacy | Example |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `module.enabled` | Enables the module after dependency validation. | boolean | `true` | `true` or `false` | boolean flag | live | public | `module.enabled = true` |
| `module.disabled_behavior` | Defines truthful behavior while this module is disabled. | enum | `command_unavailable` | `command_unavailable`, `provider_fallback`, `read_only` | named option | live | public | `module.disabled_behavior = "command_unavailable"` |
| `gui.mode` | Selects enhanced GUI presentation without changing command access. | enum | `inherit` | `command_only`, `gui_preferred`, `inherit`, `off`, `on` | named option | live | public | `gui.mode = "inherit"` |
| `gui.bare_command_opens` | Allows eligible bare command routes to prefer a dedicated workflow. | boolean | `true` | `true` or `false` | boolean flag | live | public | `gui.bare_command_opens = true` |
| `gui.default_page` | Selects the stable default workflow page. | string | `overview` | 1 through 64 characters, pattern `[a-z0-9_]+` | text | live | public | `gui.default_page = "overview"` |
| `limits.maximum_records` | Bounds persisted records owned by this module. | integer | `1000` | 1 through 100000 | count | live | public | `limits.maximum_records = 1000` |
| `limits.maximum_page_size` | Bounds command and GUI result pages. | integer | `45` | 4 through 100 | count | live | public | `limits.maximum_page_size = 45` |
| `rate.maximum_actions_per_minute` | Bounds admission attempts independently of permission cooldown duration. | integer | `300` | 1 through 10000 | actions per minute | live | public | `rate.maximum_actions_per_minute = 300` |
| `storage.retention_days` | Defines bounded data retention where the module owns persistent records. | integer | `90` | 0 through 3650 | days | live | public | `storage.retention_days = 90` |
| `failure.mode` | Defines provider and validation failure behavior. | enum | `previous_known_good` | `fail_closed`, `previous_known_good`, `read_only` | named option | live | public | `failure.mode = "previous_known_good"` |
| `audit.class` | Defines the minimum audit class for module actions. | enum | `metadata_only` | `admin_action`, `destructive`, `metadata_only`, `none` | named option | live | public | `audit.class = "metadata_only"` |
| `audit.redaction` | Defines the module redaction class. | enum | `sensitive` | `security_critical`, `sensitive`, `standard` | named option | live | public | `audit.redaction = "sensitive"` |
| `runtime.file_logging_batch_records` | Maximum records written per batch | integer | `128` | 1 through 1024 | count | live | public | `runtime.file_logging_batch_records = 128` |
| `runtime.file_logging_connection_events` | Capture redacted player connection events when file logging is active | boolean | `false` | `true` or `false` | boolean flag | live | public | `runtime.file_logging_connection_events = false` |
| `runtime.file_logging_enabled` | Start the optional command log writer | boolean | `false` | `true` or `false` | boolean flag | live | public | `runtime.file_logging_enabled = false` |
| `runtime.file_logging_flush_interval_millis` | Maximum delay before flushing a nonempty batch | integer | `1000` | 50 through 60000 | count | live | public | `runtime.file_logging_flush_interval_millis = 1000` |
| `runtime.file_logging_maximum_archives` | Maximum retained command archives | integer | `100` | 1 through 10000 | count | live | public | `runtime.file_logging_maximum_archives = 100` |
| `runtime.file_logging_maximum_file_age_hours` | Maximum active stream age before rotation | integer | `24` | 1 through 720 | hours | live | public | `runtime.file_logging_maximum_file_age_hours = 24` |
| `runtime.file_logging_maximum_file_mi_b` | Maximum active stream size before rotation | integer | `64` | 1 through 1024 | count | live | public | `runtime.file_logging_maximum_file_mi_b = 64` |
| `runtime.file_logging_maximum_record_bytes` | Maximum encoded bytes in one record | integer | `16384` | 1024 through 1048576 | bytes | live | public | `runtime.file_logging_maximum_record_bytes = 16384` |
| `runtime.file_logging_maximum_total_mi_b` | Maximum retained command archive bytes | integer | `1024` | 1 through 1048576 | count | live | public | `runtime.file_logging_maximum_total_mi_b = 1024` |
| `runtime.file_logging_queue_capacity` | Bounded event queue capacity | integer | `8192` | 128 through 65536 | count | live | public | `runtime.file_logging_queue_capacity = 8192` |
| `runtime.file_logging_retention_days` | Maximum archive age | integer | `30` | 1 through 3650 | days | live | public | `runtime.file_logging_retention_days = 30` |
| `runtime.file_logging_shutdown_timeout_seconds` | Maximum bounded shutdown drain time | integer | `10` | 1 through 60 | seconds | live | public | `runtime.file_logging_shutdown_timeout_seconds = 10` |
| `runtime.file_logging_text_mirror` | Write a stripped human readable mirror beside JSON Lines | boolean | `false` | `true` or `false` | boolean flag | live | public | `runtime.file_logging_text_mirror = false` |

## mail

Controls mail behavior, presentation, bounds, storage, and diagnostics.

File: `config/sef/modules/mail.toml`. Schema version: `1`. Documentation version: `2`. Apply class: `live`. Dependencies: `commands`, `core`, `gui`, `messages`, `permissions`. Conflicts: none.

Inspect permission: `sef.commands.config.inspect`. Edit permission: `sef.commands.config.edit`. Migration path: inspect the retained legacy `common.toml` with `/sef config migrate dryrun`, then request a revision-bound confirmation with `/sef config migrate apply <expected_revision>`. Legacy cooldown durations move to `sef.cooldown.<action>.<seconds>` permissions instead of module files.

| Setting | Description | Type | Default | Bounds | Unit | Apply class | Privacy | Example |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `module.enabled` | Enables the module after dependency validation. | boolean | `true` | `true` or `false` | boolean flag | live | public | `module.enabled = true` |
| `module.disabled_behavior` | Defines truthful behavior while this module is disabled. | enum | `command_unavailable` | `command_unavailable`, `provider_fallback`, `read_only` | named option | live | public | `module.disabled_behavior = "command_unavailable"` |
| `gui.mode` | Selects enhanced GUI presentation without changing command access. | enum | `inherit` | `command_only`, `gui_preferred`, `inherit`, `off`, `on` | named option | live | public | `gui.mode = "inherit"` |
| `gui.bare_command_opens` | Allows eligible bare command routes to prefer a dedicated workflow. | boolean | `true` | `true` or `false` | boolean flag | live | public | `gui.bare_command_opens = true` |
| `gui.default_page` | Selects the stable default workflow page. | string | `overview` | 1 through 64 characters, pattern `[a-z0-9_]+` | text | live | public | `gui.default_page = "overview"` |
| `limits.maximum_records` | Bounds persisted records owned by this module. | integer | `10000` | 1 through 1000000 | count | live | public | `limits.maximum_records = 10000` |
| `limits.maximum_page_size` | Bounds command and GUI result pages. | integer | `45` | 4 through 100 | count | live | public | `limits.maximum_page_size = 45` |
| `rate.maximum_actions_per_minute` | Bounds admission attempts independently of permission cooldown duration. | integer | `300` | 1 through 10000 | actions per minute | live | public | `rate.maximum_actions_per_minute = 300` |
| `storage.retention_days` | Defines bounded data retention where the module owns persistent records. | integer | `30` | 0 through 3650 | days | live | public | `storage.retention_days = 30` |
| `failure.mode` | Defines provider and validation failure behavior. | enum | `previous_known_good` | `fail_closed`, `previous_known_good`, `read_only` | named option | live | public | `failure.mode = "previous_known_good"` |
| `audit.class` | Defines the minimum audit class for module actions. | enum | `metadata_only` | `admin_action`, `destructive`, `metadata_only`, `none` | named option | live | public | `audit.class = "metadata_only"` |
| `audit.redaction` | Defines the module redaction class. | enum | `standard` | `security_critical`, `sensitive`, `standard` | named option | live | public | `audit.redaction = "standard"` |
| `runtime.enable_mail` | Offline UUID addressed mail | boolean | `true` | `true` or `false` | boolean flag | restart_required | public | `runtime.enable_mail = true` |
| `runtime.mail_maximum_length` | Maximum mail body length | integer | `2048` | 1 through 16384 | count | live | public | `runtime.mail_maximum_length = 2048` |
| `runtime.mail_retention_days` | Mail expiry in days | integer | `30` | 1 through 3650 | days | live | public | `runtime.mail_retention_days = 30` |

## messages

Controls messages behavior, presentation, bounds, storage, and diagnostics.

File: `config/sef/modules/messages.toml`. Schema version: `1`. Documentation version: `2`. Apply class: `live`. Dependencies: `core`. Conflicts: none.

Inspect permission: `sef.commands.config.inspect`. Edit permission: `sef.commands.config.edit`. Migration path: inspect the retained legacy `common.toml` with `/sef config migrate dryrun`, then request a revision-bound confirmation with `/sef config migrate apply <expected_revision>`. Legacy cooldown durations move to `sef.cooldown.<action>.<seconds>` permissions instead of module files.

| Setting | Description | Type | Default | Bounds | Unit | Apply class | Privacy | Example |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `module.enabled` | Enables the module after dependency validation. | boolean | `true` | `true` or `false` | boolean flag | live | public | `module.enabled = true` |
| `module.disabled_behavior` | Defines truthful behavior while this module is disabled. | enum | `command_unavailable` | `command_unavailable`, `provider_fallback`, `read_only` | named option | live | public | `module.disabled_behavior = "command_unavailable"` |
| `gui.mode` | Selects enhanced GUI presentation without changing command access. | enum | `inherit` | `command_only`, `gui_preferred`, `inherit`, `off`, `on` | named option | live | public | `gui.mode = "inherit"` |
| `gui.bare_command_opens` | Allows eligible bare command routes to prefer a dedicated workflow. | boolean | `true` | `true` or `false` | boolean flag | live | public | `gui.bare_command_opens = true` |
| `gui.default_page` | Selects the stable default workflow page. | string | `overview` | 1 through 64 characters, pattern `[a-z0-9_]+` | text | live | public | `gui.default_page = "overview"` |
| `limits.maximum_records` | Bounds persisted records owned by this module. | integer | `10000` | 1 through 1000000 | count | live | public | `limits.maximum_records = 10000` |
| `limits.maximum_page_size` | Bounds command and GUI result pages. | integer | `45` | 4 through 100 | count | live | public | `limits.maximum_page_size = 45` |
| `rate.maximum_actions_per_minute` | Bounds admission attempts independently of permission cooldown duration. | integer | `300` | 1 through 10000 | actions per minute | live | public | `rate.maximum_actions_per_minute = 300` |
| `storage.retention_days` | Defines bounded data retention where the module owns persistent records. | integer | `30` | 0 through 3650 | days | live | public | `storage.retention_days = 30` |
| `failure.mode` | Defines provider and validation failure behavior. | enum | `previous_known_good` | `fail_closed`, `previous_known_good`, `read_only` | named option | live | public | `failure.mode = "previous_known_good"` |
| `audit.class` | Defines the minimum audit class for module actions. | enum | `metadata_only` | `admin_action`, `destructive`, `metadata_only`, `none` | named option | live | public | `audit.class = "metadata_only"` |
| `audit.redaction` | Defines the module redaction class. | enum | `standard` | `security_critical`, `sensitive`, `standard` | named option | live | public | `audit.redaction = "standard"` |
| `format.prefix` | Defines the bounded default message prefix. | string | `&8[&6SEF&8]&r ` | 0 through 128 characters | text | live | public | `format.prefix = "&8[&6SEF&8]&r "` |
| `runtime.admin_chat_format` | Format for Admin Chat messages. Placeholders: $sender, $message | string | `&4&lAdmin Chat &e$sender&7:&r $message` | 0 through 8192 characters | text | live | public | `runtime.admin_chat_format = "&4&lAdmin Chat &e$sender&7:&r $message"` |
| `runtime.banned_announce_format` | Server-wide message format when a banned entry has announce=true and a hit occurs.   Placeholders: $player, $item, $reason.   Leave empty to use the built-in default. | string | `` | 0 through 8192 characters | text | live | public | `runtime.banned_announce_format = ""` |
| `runtime.chat_message_color` | Sets the global color of the chat messages    Choose one of the following:     AQUA, RED, LIGHT_PURPLE, YELLOW, WHITE, BLACK, GOLD, GRAY, BLUE, GREEN,     DARK_GRAY, DARK_AQUA, DARK_RED, DARK_PURPLE, DARK_GREEN, DARK_BLUE | string | `WHITE` | 0 through 8192 characters | text | live | public | `runtime.chat_message_color = "WHITE"` |
| `runtime.chat_message_format` | Controls the chat message format     $time is replaced by the timestamp field or nothing if disabled     $name is replaced by the user's name, or nickname if they have one     colors can be uses in the formatting string. for a global message color see next section     $msg is replaced by the username's message (if you use it more then once it WILL break this mod) | string | `$time | $name: $msg` | 0 through 8192 characters | text | live | public | `runtime.chat_message_format = "$time | $name: $msg"` |
| `runtime.enable_admin_chat_sound` | Play sound for Admin Chat messages | boolean | `false` | `true` or `false` | boolean flag | restart_required | public | `runtime.enable_admin_chat_sound = false` |
| `runtime.enable_chat_formatting` | Master toggle for SEF's chat formatting (prefix/suffix/color/timestamp). When false, chat uses the vanilla format. Note: click_to_respond needs this ON to attach clickable replies. | boolean | `true` | `true` or `false` | boolean flag | restart_required | public | `runtime.enable_chat_formatting = true` |
| `runtime.enable_colors_command` | /colors command | boolean | `true` | `true` or `false` | boolean flag | restart_required | public | `runtime.enable_colors_command = true` |
| `runtime.enable_filter_system` | Word filter system (/sef filter ...) | boolean | `true` | `true` or `false` | boolean flag | restart_required | public | `runtime.enable_filter_system = true` |
| `runtime.enable_help_op_sound` | Play sound for HelpOp notifications | boolean | `true` | `true` or `false` | boolean flag | restart_required | public | `runtime.enable_help_op_sound = true` |
| `runtime.enable_markdown` | Enables or disables markdown styling | boolean | `true` | `true` or `false` | boolean flag | restart_required | public | `runtime.enable_markdown = true` |
| `runtime.enable_msg_sound` | Play sound when receiving a private message | boolean | `true` | `true` or `false` | boolean flag | restart_required | public | `runtime.enable_msg_sound = true` |
| `runtime.enable_reply_sound` | Play sound when someone replies to your message | boolean | `true` | `true` or `false` | boolean flag | restart_required | public | `runtime.enable_reply_sound = true` |
| `runtime.enable_timestamp` | Enables or disables the filling in of timestamps | boolean | `true` | `true` or `false` | boolean flag | restart_required | public | `runtime.enable_timestamp = true` |
| `runtime.help_op_reply_format` | Format for HelpOp replies to players. Placeholders: $message | string | `&l&cHelpOp &4OP&f Replied&7:&r&7 $message` | 0 through 8192 characters | text | live | public | `runtime.help_op_reply_format = "&l&cHelpOp &4OP&f Replied&7:&r&7 $message"` |
| `runtime.help_op_reply_hover` | Hover text for HelpOp reply. Placeholder: $player | string | `&7Click to reply to $player` | 0 through 8192 characters | text | live | public | `runtime.help_op_reply_hover = "&7Click to reply to $player"` |
| `runtime.help_op_request_format` | Format for HelpOp requests to operators. Placeholders: $sender, $message | string | `&l&cHelpOp &fFrom &e$sender&7:&r&7 $message` | 0 through 8192 characters | text | live | public | `runtime.help_op_request_format = "&l&cHelpOp &fFrom &e$sender&7:&r&7 $message"` |
| `runtime.meta_join_separator` | Separator inserted between multiple prefixes/suffixes when combined | string | ` ` | 0 through 8192 characters | text | live | public | `runtime.meta_join_separator = " "` |
| `runtime.player_name_format` | Controls the chat message format     $prefix is replaced by the user's prefix or nothing if the user has no prefix     $suffix is replaced by the user's suffix or nothing if the user has no suffix     $name is replaced by the user's name, or nickname if they have one | string | `$prefix$name$suffix` | 0 through 8192 characters | text | live | public | `runtime.player_name_format = "$prefix$name$suffix"` |
| `runtime.timestamp_format` | Timestamp format following the java SimpleDateFormat     Read more here: https://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html | string | `HH:mm` | 0 through 8192 characters | text | live | public | `runtime.timestamp_format = "HH:mm"` |
| `runtime.unfreeze_admin_notify_format` | Notification sent to admins when a player is unfrozen. Placeholders: $player, $admin | string | `&e$admin &7has unfrozen &e$player&7.` | 0 through 8192 characters | text | live | public | `runtime.unfreeze_admin_notify_format = "&e$admin &7has unfrozen &e$player&7."` |
| `runtime.unfreeze_message_to_player` | Message sent to the player when unfrozen. Placeholder: $admin | string | `&a&lYou have been unfrozen by &e$admin&a&l.` | 0 through 8192 characters | text | live | public | `runtime.unfreeze_message_to_player = "&a&lYou have been unfrozen by &e$admin&a&l."` |

## moderation

Controls moderation behavior, presentation, bounds, storage, and diagnostics.

File: `config/sef/modules/moderation.toml`. Schema version: `1`. Documentation version: `2`. Apply class: `live`. Dependencies: `audit`, `commands`, `core`, `gui`, `messages`, `permissions`. Conflicts: none.

Inspect permission: `sef.commands.config.inspect`. Edit permission: `sef.commands.config.edit`. Migration path: inspect the retained legacy `common.toml` with `/sef config migrate dryrun`, then request a revision-bound confirmation with `/sef config migrate apply <expected_revision>`. Legacy cooldown durations move to `sef.cooldown.<action>.<seconds>` permissions instead of module files.

| Setting | Description | Type | Default | Bounds | Unit | Apply class | Privacy | Example |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `module.enabled` | Enables the module after dependency validation. | boolean | `true` | `true` or `false` | boolean flag | live | public | `module.enabled = true` |
| `module.disabled_behavior` | Defines truthful behavior while this module is disabled. | enum | `command_unavailable` | `command_unavailable`, `provider_fallback`, `read_only` | named option | live | public | `module.disabled_behavior = "command_unavailable"` |
| `gui.mode` | Selects enhanced GUI presentation without changing command access. | enum | `inherit` | `command_only`, `gui_preferred`, `inherit`, `off`, `on` | named option | live | public | `gui.mode = "inherit"` |
| `gui.bare_command_opens` | Allows eligible bare command routes to prefer a dedicated workflow. | boolean | `true` | `true` or `false` | boolean flag | live | public | `gui.bare_command_opens = true` |
| `gui.default_page` | Selects the stable default workflow page. | string | `overview` | 1 through 64 characters, pattern `[a-z0-9_]+` | text | live | public | `gui.default_page = "overview"` |
| `limits.maximum_records` | Bounds persisted records owned by this module. | integer | `1000` | 1 through 100000 | count | live | public | `limits.maximum_records = 1000` |
| `limits.maximum_page_size` | Bounds command and GUI result pages. | integer | `45` | 4 through 100 | count | live | public | `limits.maximum_page_size = 45` |
| `rate.maximum_actions_per_minute` | Bounds admission attempts independently of permission cooldown duration. | integer | `300` | 1 through 10000 | actions per minute | live | public | `rate.maximum_actions_per_minute = 300` |
| `storage.retention_days` | Defines bounded data retention where the module owns persistent records. | integer | `90` | 0 through 3650 | days | live | public | `storage.retention_days = 90` |
| `failure.mode` | Defines provider and validation failure behavior. | enum | `previous_known_good` | `fail_closed`, `previous_known_good`, `read_only` | named option | live | public | `failure.mode = "previous_known_good"` |
| `audit.class` | Defines the minimum audit class for module actions. | enum | `metadata_only` | `admin_action`, `destructive`, `metadata_only`, `none` | named option | live | public | `audit.class = "metadata_only"` |
| `audit.redaction` | Defines the module redaction class. | enum | `sensitive` | `security_critical`, `sensitive`, `standard` | named option | live | public | `audit.redaction = "sensitive"` |
| `runtime.enable_banned_items` | Banned items system (/banned ...) | boolean | `true` | `true` or `false` | boolean flag | restart_required | public | `runtime.enable_banned_items = true` |
| `runtime.enable_clear_chat` | Clear chat (/cc, /clearchat) | boolean | `true` | `true` or `false` | boolean flag | restart_required | public | `runtime.enable_clear_chat = true` |
| `runtime.enable_moderation_essentials` | Ban, kick, IP moderation, and moderation history | boolean | `true` | `true` or `false` | boolean flag | restart_required | public | `runtime.enable_moderation_essentials = true` |
| `runtime.moderation_address_provider` | Address provider. direct, trusted_proxy, external, or disabled. Restart required. | string | `direct` | 0 through 8192 characters | text | restart_required | sensitive | `runtime.moderation_address_provider = "direct"` |
| `runtime.moderation_allow_literal_console_addresses` | Permit console to enter a literal address | boolean | `true` | `true` or `false` | boolean flag | live | sensitive | `runtime.moderation_allow_literal_console_addresses = true` |
| `runtime.moderation_allow_literal_player_addresses` | Permit players with the literal address permission to enter a literal address | boolean | `false` | `true` or `false` | boolean flag | live | sensitive | `runtime.moderation_allow_literal_player_addresses = false` |
| `runtime.moderation_confirmation_seconds` | Lifetime of destructive mass action confirmation tokens | integer | `30` | 10 through 300 | seconds | live | public | `runtime.moderation_confirmation_seconds = 30` |
| `runtime.moderation_default_kick_reason` | Disconnect reason used when no reason is supplied | string | `Removed by an administrator.` | 0 through 8192 characters | text | live | public | `runtime.moderation_default_kick_reason = "Removed by an administrator."` |
| `runtime.moderation_fail_on_shared_proxy` | Disable shared address actions when a likely unconfigured proxy is detected | boolean | `true` | `true` or `false` | boolean flag | live | public | `runtime.moderation_fail_on_shared_proxy = true` |
| `runtime.moderation_maximum_mass_targets` | Maximum sessions affected by one bounded moderation action | integer | `100` | 1 through 1000 | count | live | public | `runtime.moderation_maximum_mass_targets = 100` |
| `runtime.moderation_maximum_reason_length` | Maximum stored moderation reason length | integer | `512` | 1 through 2048 | count | live | public | `runtime.moderation_maximum_reason_length = 512` |
| `runtime.moderation_shared_address_hard_cap` | Maximum sessions resolved from one address | integer | `10` | 1 through 100 | count | live | sensitive | `runtime.moderation_shared_address_hard_cap = 10` |

## mutes

Controls mutes behavior, presentation, bounds, storage, and diagnostics.

File: `config/sef/modules/mutes.toml`. Schema version: `1`. Documentation version: `2`. Apply class: `live`. Dependencies: `audit`, `commands`, `core`, `gui`, `messages`, `permissions`. Conflicts: none.

Inspect permission: `sef.commands.config.inspect`. Edit permission: `sef.commands.config.edit`. Migration path: inspect the retained legacy `common.toml` with `/sef config migrate dryrun`, then request a revision-bound confirmation with `/sef config migrate apply <expected_revision>`. Legacy cooldown durations move to `sef.cooldown.<action>.<seconds>` permissions instead of module files.

| Setting | Description | Type | Default | Bounds | Unit | Apply class | Privacy | Example |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `module.enabled` | Enables the module after dependency validation. | boolean | `true` | `true` or `false` | boolean flag | live | public | `module.enabled = true` |
| `module.disabled_behavior` | Defines truthful behavior while this module is disabled. | enum | `command_unavailable` | `command_unavailable`, `provider_fallback`, `read_only` | named option | live | public | `module.disabled_behavior = "command_unavailable"` |
| `gui.mode` | Selects enhanced GUI presentation without changing command access. | enum | `inherit` | `command_only`, `gui_preferred`, `inherit`, `off`, `on` | named option | live | public | `gui.mode = "inherit"` |
| `gui.bare_command_opens` | Allows eligible bare command routes to prefer a dedicated workflow. | boolean | `true` | `true` or `false` | boolean flag | live | public | `gui.bare_command_opens = true` |
| `gui.default_page` | Selects the stable default workflow page. | string | `overview` | 1 through 64 characters, pattern `[a-z0-9_]+` | text | live | public | `gui.default_page = "overview"` |
| `limits.maximum_records` | Bounds persisted records owned by this module. | integer | `1000` | 1 through 100000 | count | live | public | `limits.maximum_records = 1000` |
| `limits.maximum_page_size` | Bounds command and GUI result pages. | integer | `45` | 4 through 100 | count | live | public | `limits.maximum_page_size = 45` |
| `rate.maximum_actions_per_minute` | Bounds admission attempts independently of permission cooldown duration. | integer | `300` | 1 through 10000 | actions per minute | live | public | `rate.maximum_actions_per_minute = 300` |
| `storage.retention_days` | Defines bounded data retention where the module owns persistent records. | integer | `90` | 0 through 3650 | days | live | public | `storage.retention_days = 90` |
| `failure.mode` | Defines provider and validation failure behavior. | enum | `previous_known_good` | `fail_closed`, `previous_known_good`, `read_only` | named option | live | public | `failure.mode = "previous_known_good"` |
| `audit.class` | Defines the minimum audit class for module actions. | enum | `metadata_only` | `admin_action`, `destructive`, `metadata_only`, `none` | named option | live | public | `audit.class = "metadata_only"` |
| `audit.redaction` | Defines the module redaction class. | enum | `sensitive` | `security_critical`, `sensitive`, `standard` | named option | live | public | `audit.redaction = "sensitive"` |
| `runtime.enable_ftb_mute_integration` | Enable checking FTB Essentials mute status to block muted players from chatting | boolean | `true` | `true` or `false` | boolean flag | restart_required | public | `runtime.enable_ftb_mute_integration = true` |
| `runtime.enable_mute_system` | Persistent mute system (/mute, /unmute, /mutelist) | boolean | `true` | `true` or `false` | boolean flag | restart_required | public | `runtime.enable_mute_system = true` |
| `runtime.mute_admin_notify_format` | Notification sent to admins when a player is muted. Placeholders: $player, $admin, $reason, $duration | string | `&e$admin &7has muted &e$player &7for &e$duration&7. Reason: &f$reason` | 0 through 8192 characters | text | live | public | `runtime.mute_admin_notify_format = "&e$admin &7has muted &e$player &7for &e$duration&7. Reason: &f$reason"` |
| `runtime.mute_already_muted_msg` | Message when trying to mute an already muted player. Placeholder: $player | string | `&c$player is already muted.` | 0 through 8192 characters | text | live | public | `runtime.mute_already_muted_msg = "&c$player is already muted."` |
| `runtime.mute_confirm_format` | Confirmation message to the admin who muted. Placeholders: $player, $admin, $reason, $duration | string | `&aMuted &e$player &afor &e$duration&a. Reason: &7$reason` | 0 through 8192 characters | text | live | public | `runtime.mute_confirm_format = "&aMuted &e$player &afor &e$duration&a. Reason: &7$reason"` |
| `runtime.mute_list_empty_msg` | Message when no one is currently muted | string | `&7No players are currently muted.` | 0 through 8192 characters | text | live | public | `runtime.mute_list_empty_msg = "&7No players are currently muted."` |
| `runtime.mute_list_entry_format` | Format for each muted player entry. Placeholders: $player, $admin, $reason, $remaining, $duration | string | `&7- &e$player &7by &e$admin &7| Reason: &f$reason &7| Remaining: &e$remaining &7/ &e$duration` | 0 through 8192 characters | text | live | public | `runtime.mute_list_entry_format = "&7- &e$player &7by &e$admin &7| Reason: &f$reason &7| Remaining: &e$remaining &7/ &e$duration"` |
| `runtime.mute_list_header_format` | Header for /mutelist. No placeholders. | string | `&6━━━━ Currently Muted Players ━━━━` | 0 through 8192 characters | text | live | public | `runtime.mute_list_header_format = "&6━━━━ Currently Muted Players ━━━━"` |
| `runtime.mute_not_muted_msg` | Message when trying to unmute a player who isn't muted. Placeholder: $player | string | `&c$player is not muted.` | 0 through 8192 characters | text | live | public | `runtime.mute_not_muted_msg = "&c$player is not muted."` |
| `runtime.mute_notify_player_format` | Message sent to the player when muted. Placeholders: $admin, $reason, $duration | string | `&c&l⚠ YOU HAVE BEEN MUTED ⚠\n&7Reason: &f$reason\n&7Muted by: &e$admin\n&7Duration: &e$duration` | 0 through 8192 characters | text | live | public | `runtime.mute_notify_player_format = "&c&l⚠ YOU HAVE BEEN MUTED ⚠\\n&7Reason: &f$reason\\n&7Muted by: &e$admin\\n&7Duration: &e$duration"` |
| `runtime.muted_message_op_format` | Format for relaying muted messages to operators. Placeholders: $username, $message | string | `&c&lMuted Message &7From $username:&r $message` | 0 through 8192 characters | text | live | public | `runtime.muted_message_op_format = "&c&lMuted Message &7From $username:&r $message"` |
| `runtime.muted_player_chat_msg` | Message shown to a muted player when they try to chat (permanent mute). No placeholders. | string | `&cYou are muted and cannot send messages.` | 0 through 8192 characters | text | live | public | `runtime.muted_player_chat_msg = "&cYou are muted and cannot send messages."` |
| `runtime.muted_player_chat_msg_with_remaining` | Message shown to a muted player when they try to chat (timed mute). Placeholder: $remaining | string | `&cYou are muted and cannot send messages. &7Time remaining: &e$remaining` | 0 through 8192 characters | text | live | public | `runtime.muted_player_chat_msg_with_remaining = "&cYou are muted and cannot send messages. &7Time remaining: &e$remaining"` |
| `runtime.muted_player_message` | Message shown to the muted player when they try to chat | string | `&cYou are muted and cannot send messages.` | 0 through 8192 characters | text | live | public | `runtime.muted_player_message = "&cYou are muted and cannot send messages."` |
| `runtime.send_muted_message_to_ops` | When true, muted messages are relayed to online operators so they can see what the muted player tried to say | boolean | `true` | `true` or `false` | boolean flag | live | public | `runtime.send_muted_message_to_ops = true` |
| `runtime.unmute_admin_notify_format` | Notification sent to admins when a player is unmuted. Placeholders: $player, $admin | string | `&e$admin &7has unmuted &e$player&7.` | 0 through 8192 characters | text | live | public | `runtime.unmute_admin_notify_format = "&e$admin &7has unmuted &e$player&7."` |
| `runtime.unmute_confirm_format` | Confirmation message to the admin who unmuted. Placeholders: $player, $admin | string | `&aUnmuted &e$player&a.` | 0 through 8192 characters | text | live | public | `runtime.unmute_confirm_format = "&aUnmuted &e$player&a."` |
| `runtime.unmute_notify_player_format` | Message sent to the player when unmuted. Placeholder: $admin | string | `&a&lYou have been unmuted by &e$admin&a&l.` | 0 through 8192 characters | text | live | public | `runtime.unmute_notify_player_format = "&a&lYou have been unmuted by &e$admin&a&l."` |

## nicknames

Controls nicknames behavior, presentation, bounds, storage, and diagnostics.

File: `config/sef/modules/nicknames.toml`. Schema version: `1`. Documentation version: `2`. Apply class: `live`. Dependencies: `commands`, `core`, `gui`, `messages`, `permissions`. Conflicts: none.

Inspect permission: `sef.commands.config.inspect`. Edit permission: `sef.commands.config.edit`. Migration path: inspect the retained legacy `common.toml` with `/sef config migrate dryrun`, then request a revision-bound confirmation with `/sef config migrate apply <expected_revision>`. Legacy cooldown durations move to `sef.cooldown.<action>.<seconds>` permissions instead of module files.

| Setting | Description | Type | Default | Bounds | Unit | Apply class | Privacy | Example |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `module.enabled` | Enables the module after dependency validation. | boolean | `true` | `true` or `false` | boolean flag | live | public | `module.enabled = true` |
| `module.disabled_behavior` | Defines truthful behavior while this module is disabled. | enum | `command_unavailable` | `command_unavailable`, `provider_fallback`, `read_only` | named option | live | public | `module.disabled_behavior = "command_unavailable"` |
| `gui.mode` | Selects enhanced GUI presentation without changing command access. | enum | `inherit` | `command_only`, `gui_preferred`, `inherit`, `off`, `on` | named option | live | public | `gui.mode = "inherit"` |
| `gui.bare_command_opens` | Allows eligible bare command routes to prefer a dedicated workflow. | boolean | `true` | `true` or `false` | boolean flag | live | public | `gui.bare_command_opens = true` |
| `gui.default_page` | Selects the stable default workflow page. | string | `overview` | 1 through 64 characters, pattern `[a-z0-9_]+` | text | live | public | `gui.default_page = "overview"` |
| `limits.maximum_records` | Bounds persisted records owned by this module. | integer | `10000` | 1 through 1000000 | count | live | public | `limits.maximum_records = 10000` |
| `limits.maximum_page_size` | Bounds command and GUI result pages. | integer | `45` | 4 through 100 | count | live | public | `limits.maximum_page_size = 45` |
| `rate.maximum_actions_per_minute` | Bounds admission attempts independently of permission cooldown duration. | integer | `300` | 1 through 10000 | actions per minute | live | public | `rate.maximum_actions_per_minute = 300` |
| `storage.retention_days` | Defines bounded data retention where the module owns persistent records. | integer | `30` | 0 through 3650 | days | live | public | `storage.retention_days = 30` |
| `failure.mode` | Defines provider and validation failure behavior. | enum | `previous_known_good` | `fail_closed`, `previous_known_good`, `read_only` | named option | live | public | `failure.mode = "previous_known_good"` |
| `audit.class` | Defines the minimum audit class for module actions. | enum | `metadata_only` | `admin_action`, `destructive`, `metadata_only`, `none` | named option | live | public | `audit.class = "metadata_only"` |
| `audit.redaction` | Defines the module redaction class. | enum | `standard` | `security_critical`, `sensitive`, `standard` | named option | live | public | `audit.redaction = "standard"` |
| `runtime.enable_chat_nickname_command` | Integrated /nick command (off by default; conflicts with FTB Essentials' /nick. Ignored if autoIntegratedNicknames is on) | boolean | `false` | `true` or `false` | boolean flag | restart_required | public | `runtime.enable_chat_nickname_command = false` |
| `runtime.enable_whois_command` | Integrated /whois command (ignored if autoIntegratedNicknames is on) | boolean | `true` | `true` or `false` | boolean flag | restart_required | public | `runtime.enable_whois_command = true` |
| `runtime.maximum_nickname_length` | Maximum allowed nickname length (for integrated nickname commands) | integer | `50` | 1 through 500 | count | live | public | `runtime.maximum_nickname_length = 50` |
| `runtime.minimum_nickname_length` | Minimum allowed nickname length (for integrated nickname commands) | integer | `1` | 1 through 500 | count | live | public | `runtime.minimum_nickname_length = 1` |
| `runtime.nickname_allow_duplicate_with_username_hover` | Allow duplicate display names. Vanilla hover text shows the authenticated username, and identity lookup remains fail closed when a nickname is ambiguous. | boolean | `false` | `true` or `false` | boolean flag | live | public | `runtime.nickname_allow_duplicate_with_username_hover = false` |
| `runtime.nickname_unique_known_profiles` | Require nicknames to be unique among all profiles known to the integrated nickname store | boolean | `true` | `true` or `false` | boolean flag | live | public | `runtime.nickname_unique_known_profiles = true` |
| `runtime.nickname_unique_online` | Require nicknames to be unique among online players | boolean | `true` | `true` or `false` | boolean flag | live | public | `runtime.nickname_unique_online = true` |

## performance

Controls performance behavior, presentation, bounds, storage, and diagnostics.

File: `config/sef/modules/performance.toml`. Schema version: `1`. Documentation version: `2`. Apply class: `live`. Dependencies: `audit`, `commands`, `core`, `messages`, `permissions`. Conflicts: none.

Inspect permission: `sef.commands.config.inspect`. Edit permission: `sef.commands.config.edit`. Migration path: inspect the retained legacy `common.toml` with `/sef config migrate dryrun`, then request a revision-bound confirmation with `/sef config migrate apply <expected_revision>`. Legacy cooldown durations move to `sef.cooldown.<action>.<seconds>` permissions instead of module files.

| Setting | Description | Type | Default | Bounds | Unit | Apply class | Privacy | Example |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `module.enabled` | Enables the module after dependency validation. | boolean | `true` | `true` or `false` | boolean flag | live | public | `module.enabled = true` |
| `module.disabled_behavior` | Defines truthful behavior while this module is disabled. | enum | `command_unavailable` | `command_unavailable`, `provider_fallback`, `read_only` | named option | live | public | `module.disabled_behavior = "command_unavailable"` |
| `gui.mode` | Selects enhanced GUI presentation without changing command access. | enum | `inherit` | `command_only`, `gui_preferred`, `inherit`, `off`, `on` | named option | live | public | `gui.mode = "inherit"` |
| `gui.bare_command_opens` | Allows eligible bare command routes to prefer a dedicated workflow. | boolean | `true` | `true` or `false` | boolean flag | live | public | `gui.bare_command_opens = true` |
| `gui.default_page` | Selects the stable default workflow page. | string | `overview` | 1 through 64 characters, pattern `[a-z0-9_]+` | text | live | public | `gui.default_page = "overview"` |
| `limits.maximum_records` | Bounds persisted records owned by this module. | integer | `10000` | 1 through 1000000 | count | live | public | `limits.maximum_records = 10000` |
| `limits.maximum_page_size` | Bounds command and GUI result pages. | integer | `45` | 4 through 100 | count | live | public | `limits.maximum_page_size = 45` |
| `rate.maximum_actions_per_minute` | Bounds admission attempts independently of permission cooldown duration. | integer | `300` | 1 through 10000 | actions per minute | live | public | `rate.maximum_actions_per_minute = 300` |
| `storage.retention_days` | Defines bounded data retention where the module owns persistent records. | integer | `30` | 0 through 3650 | days | live | public | `storage.retention_days = 30` |
| `failure.mode` | Defines provider and validation failure behavior. | enum | `previous_known_good` | `fail_closed`, `previous_known_good`, `read_only` | named option | live | public | `failure.mode = "previous_known_good"` |
| `audit.class` | Defines the minimum audit class for module actions. | enum | `metadata_only` | `admin_action`, `destructive`, `metadata_only`, `none` | named option | live | public | `audit.class = "metadata_only"` |
| `audit.redaction` | Defines the module redaction class. | enum | `standard` | `security_critical`, `sensitive`, `standard` | named option | live | public | `audit.redaction = "standard"` |
| `sampling.maximum_milliseconds_per_minute` | Bounds active profiler overhead. | integer | `1000` | 10 through 10000 | actions per minute | live | public | `sampling.maximum_milliseconds_per_minute = 1000` |
| `sampling.maximum_chunks` | Bounds one diagnostic sampling scope. | integer | `4096` | 16 through 100000 | count | live | public | `sampling.maximum_chunks = 4096` |

## permissions

Controls permissions behavior, presentation, bounds, storage, and diagnostics.

File: `config/sef/modules/permissions.toml`. Schema version: `1`. Documentation version: `2`. Apply class: `live`. Dependencies: `core`. Conflicts: none.

Inspect permission: `sef.commands.config.inspect`. Edit permission: `sef.commands.config.edit`. Migration path: inspect the retained legacy `common.toml` with `/sef config migrate dryrun`, then request a revision-bound confirmation with `/sef config migrate apply <expected_revision>`. Legacy cooldown durations move to `sef.cooldown.<action>.<seconds>` permissions instead of module files.

| Setting | Description | Type | Default | Bounds | Unit | Apply class | Privacy | Example |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `module.enabled` | Enables the module after dependency validation. | boolean | `true` | `true` or `false` | boolean flag | live | public | `module.enabled = true` |
| `module.disabled_behavior` | Defines truthful behavior while this module is disabled. | enum | `command_unavailable` | `command_unavailable`, `provider_fallback`, `read_only` | named option | live | public | `module.disabled_behavior = "command_unavailable"` |
| `gui.mode` | Selects enhanced GUI presentation without changing command access. | enum | `inherit` | `command_only`, `gui_preferred`, `inherit`, `off`, `on` | named option | live | public | `gui.mode = "inherit"` |
| `gui.bare_command_opens` | Allows eligible bare command routes to prefer a dedicated workflow. | boolean | `false` | `true` or `false` | boolean flag | live | public | `gui.bare_command_opens = false` |
| `gui.default_page` | Selects the stable default workflow page. | string | `overview` | 1 through 64 characters, pattern `[a-z0-9_]+` | text | live | public | `gui.default_page = "overview"` |
| `limits.maximum_records` | Bounds persisted records owned by this module. | integer | `10000` | 1 through 1000000 | count | live | public | `limits.maximum_records = 10000` |
| `limits.maximum_page_size` | Bounds command and GUI result pages. | integer | `45` | 4 through 100 | count | live | public | `limits.maximum_page_size = 45` |
| `rate.maximum_actions_per_minute` | Bounds admission attempts independently of permission cooldown duration. | integer | `300` | 1 through 10000 | actions per minute | live | public | `rate.maximum_actions_per_minute = 300` |
| `storage.retention_days` | Defines bounded data retention where the module owns persistent records. | integer | `30` | 0 through 3650 | days | live | public | `storage.retention_days = 30` |
| `failure.mode` | Defines provider and validation failure behavior. | enum | `previous_known_good` | `fail_closed`, `previous_known_good`, `read_only` | named option | live | public | `failure.mode = "previous_known_good"` |
| `audit.class` | Defines the minimum audit class for module actions. | enum | `metadata_only` | `admin_action`, `destructive`, `metadata_only`, `none` | named option | live | public | `audit.class = "metadata_only"` |
| `audit.redaction` | Defines the module redaction class. | enum | `standard` | `security_critical`, `sensitive`, `standard` | named option | live | public | `audit.redaction = "standard"` |

## player_utilities

Controls player utilities behavior, presentation, bounds, storage, and diagnostics.

File: `config/sef/modules/player_utilities.toml`. Schema version: `1`. Documentation version: `2`. Apply class: `live`. Dependencies: `commands`, `core`, `gui`, `messages`, `permissions`. Conflicts: none.

Inspect permission: `sef.commands.config.inspect`. Edit permission: `sef.commands.config.edit`. Migration path: inspect the retained legacy `common.toml` with `/sef config migrate dryrun`, then request a revision-bound confirmation with `/sef config migrate apply <expected_revision>`. Legacy cooldown durations move to `sef.cooldown.<action>.<seconds>` permissions instead of module files.

| Setting | Description | Type | Default | Bounds | Unit | Apply class | Privacy | Example |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `module.enabled` | Enables the module after dependency validation. | boolean | `true` | `true` or `false` | boolean flag | live | public | `module.enabled = true` |
| `module.disabled_behavior` | Defines truthful behavior while this module is disabled. | enum | `command_unavailable` | `command_unavailable`, `provider_fallback`, `read_only` | named option | live | public | `module.disabled_behavior = "command_unavailable"` |
| `gui.mode` | Selects enhanced GUI presentation without changing command access. | enum | `inherit` | `command_only`, `gui_preferred`, `inherit`, `off`, `on` | named option | live | public | `gui.mode = "inherit"` |
| `gui.bare_command_opens` | Allows eligible bare command routes to prefer a dedicated workflow. | boolean | `true` | `true` or `false` | boolean flag | live | public | `gui.bare_command_opens = true` |
| `gui.default_page` | Selects the stable default workflow page. | string | `overview` | 1 through 64 characters, pattern `[a-z0-9_]+` | text | live | public | `gui.default_page = "overview"` |
| `limits.maximum_records` | Bounds persisted records owned by this module. | integer | `10000` | 1 through 1000000 | count | live | public | `limits.maximum_records = 10000` |
| `limits.maximum_page_size` | Bounds command and GUI result pages. | integer | `45` | 4 through 100 | count | live | public | `limits.maximum_page_size = 45` |
| `rate.maximum_actions_per_minute` | Bounds admission attempts independently of permission cooldown duration. | integer | `300` | 1 through 10000 | actions per minute | live | public | `rate.maximum_actions_per_minute = 300` |
| `storage.retention_days` | Defines bounded data retention where the module owns persistent records. | integer | `30` | 0 through 3650 | days | live | public | `storage.retention_days = 30` |
| `failure.mode` | Defines provider and validation failure behavior. | enum | `previous_known_good` | `fail_closed`, `previous_known_good`, `read_only` | named option | live | public | `failure.mode = "previous_known_good"` |
| `audit.class` | Defines the minimum audit class for module actions. | enum | `metadata_only` | `admin_action`, `destructive`, `metadata_only`, `none` | named option | live | public | `audit.class = "metadata_only"` |
| `audit.redaction` | Defines the module redaction class. | enum | `standard` | `security_critical`, `sensitive`, `standard` | named option | live | public | `audit.redaction = "standard"` |
| `runtime.enable_player_utilities` | Player state and position utility commands | boolean | `true` | `true` or `false` | boolean flag | restart_required | public | `runtime.enable_player_utilities = true` |
| `runtime.enable_suicide_command` | Enable the self only suicide command | boolean | `false` | `true` or `false` | boolean flag | restart_required | public | `runtime.enable_suicide_command = false` |
| `runtime.maximum_fly_speed` | Maximum fly speed multiplier accepted by the speed command | decimal | `10.0` | 0.1 through 10.0 | decimal value | live | public | `runtime.maximum_fly_speed = 10.0` |
| `runtime.maximum_walk_speed` | Maximum walk speed multiplier accepted by the speed command | decimal | `10.0` | 0.1 through 10.0 | decimal value | live | public | `runtime.maximum_walk_speed = 10.0` |

## player_warps

Controls player warps behavior, presentation, bounds, storage, and diagnostics.

File: `config/sef/modules/player_warps.toml`. Schema version: `1`. Documentation version: `2`. Apply class: `live`. Dependencies: `commands`, `core`, `gui`, `messages`, `permissions`. Conflicts: none.

Inspect permission: `sef.commands.config.inspect`. Edit permission: `sef.commands.config.edit`. Migration path: inspect the retained legacy `common.toml` with `/sef config migrate dryrun`, then request a revision-bound confirmation with `/sef config migrate apply <expected_revision>`. Legacy cooldown durations move to `sef.cooldown.<action>.<seconds>` permissions instead of module files.

| Setting | Description | Type | Default | Bounds | Unit | Apply class | Privacy | Example |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `module.enabled` | Enables the module after dependency validation. | boolean | `true` | `true` or `false` | boolean flag | live | public | `module.enabled = true` |
| `module.disabled_behavior` | Defines truthful behavior while this module is disabled. | enum | `command_unavailable` | `command_unavailable`, `provider_fallback`, `read_only` | named option | live | public | `module.disabled_behavior = "command_unavailable"` |
| `gui.mode` | Selects enhanced GUI presentation without changing command access. | enum | `inherit` | `command_only`, `gui_preferred`, `inherit`, `off`, `on` | named option | live | public | `gui.mode = "inherit"` |
| `gui.bare_command_opens` | Allows eligible bare command routes to prefer a dedicated workflow. | boolean | `true` | `true` or `false` | boolean flag | live | public | `gui.bare_command_opens = true` |
| `gui.default_page` | Selects the stable default workflow page. | string | `overview` | 1 through 64 characters, pattern `[a-z0-9_]+` | text | live | public | `gui.default_page = "overview"` |
| `limits.maximum_records` | Bounds persisted records owned by this module. | integer | `10000` | 1 through 1000000 | count | live | public | `limits.maximum_records = 10000` |
| `limits.maximum_page_size` | Bounds command and GUI result pages. | integer | `45` | 4 through 100 | count | live | public | `limits.maximum_page_size = 45` |
| `rate.maximum_actions_per_minute` | Bounds admission attempts independently of permission cooldown duration. | integer | `300` | 1 through 10000 | actions per minute | live | public | `rate.maximum_actions_per_minute = 300` |
| `storage.retention_days` | Defines bounded data retention where the module owns persistent records. | integer | `30` | 0 through 3650 | days | live | public | `storage.retention_days = 30` |
| `failure.mode` | Defines provider and validation failure behavior. | enum | `previous_known_good` | `fail_closed`, `previous_known_good`, `read_only` | named option | live | public | `failure.mode = "previous_known_good"` |
| `audit.class` | Defines the minimum audit class for module actions. | enum | `metadata_only` | `admin_action`, `destructive`, `metadata_only`, `none` | named option | live | public | `audit.class = "metadata_only"` |
| `audit.redaction` | Defines the module redaction class. | enum | `standard` | `security_critical`, `sensitive`, `standard` | named option | live | public | `audit.redaction = "standard"` |
| `runtime.default_player_warp_limit` | Default player warp quota before permission or metadata tiers | integer | `5` | 0 through 1000 | count | live | public | `runtime.default_player_warp_limit = 5` |
| `runtime.enable_player_warps` | Player hosted warp commands | boolean | `true` | `true` or `false` | boolean flag | restart_required | public | `runtime.enable_player_warps = true` |
| `runtime.player_warp_transfer_expiry_seconds` | Lifetime of a two party player warp transfer offer | integer | `300` | 10 through 3600 | seconds | live | public | `runtime.player_warp_transfer_expiry_seconds = 300` |

## privacy

Controls privacy behavior, presentation, bounds, storage, and diagnostics.

File: `config/sef/modules/privacy.toml`. Schema version: `1`. Documentation version: `2`. Apply class: `live`. Dependencies: `audit`, `commands`, `core`, `messages`, `permissions`. Conflicts: none.

Inspect permission: `sef.commands.config.inspect`. Edit permission: `sef.commands.config.edit`. Migration path: inspect the retained legacy `common.toml` with `/sef config migrate dryrun`, then request a revision-bound confirmation with `/sef config migrate apply <expected_revision>`. Legacy cooldown durations move to `sef.cooldown.<action>.<seconds>` permissions instead of module files.

| Setting | Description | Type | Default | Bounds | Unit | Apply class | Privacy | Example |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `module.enabled` | Enables the module after dependency validation. | boolean | `true` | `true` or `false` | boolean flag | live | public | `module.enabled = true` |
| `module.disabled_behavior` | Defines truthful behavior while this module is disabled. | enum | `command_unavailable` | `command_unavailable`, `provider_fallback`, `read_only` | named option | live | public | `module.disabled_behavior = "command_unavailable"` |
| `gui.mode` | Selects enhanced GUI presentation without changing command access. | enum | `inherit` | `command_only`, `gui_preferred`, `inherit`, `off`, `on` | named option | live | public | `gui.mode = "inherit"` |
| `gui.bare_command_opens` | Allows eligible bare command routes to prefer a dedicated workflow. | boolean | `true` | `true` or `false` | boolean flag | live | public | `gui.bare_command_opens = true` |
| `gui.default_page` | Selects the stable default workflow page. | string | `overview` | 1 through 64 characters, pattern `[a-z0-9_]+` | text | live | public | `gui.default_page = "overview"` |
| `limits.maximum_records` | Bounds persisted records owned by this module. | integer | `1000` | 1 through 100000 | count | live | public | `limits.maximum_records = 1000` |
| `limits.maximum_page_size` | Bounds command and GUI result pages. | integer | `45` | 4 through 100 | count | live | public | `limits.maximum_page_size = 45` |
| `rate.maximum_actions_per_minute` | Bounds admission attempts independently of permission cooldown duration. | integer | `300` | 1 through 10000 | actions per minute | live | public | `rate.maximum_actions_per_minute = 300` |
| `storage.retention_days` | Defines bounded data retention where the module owns persistent records. | integer | `90` | 0 through 3650 | days | live | public | `storage.retention_days = 90` |
| `failure.mode` | Defines provider and validation failure behavior. | enum | `previous_known_good` | `fail_closed`, `previous_known_good`, `read_only` | named option | live | public | `failure.mode = "previous_known_good"` |
| `audit.class` | Defines the minimum audit class for module actions. | enum | `metadata_only` | `admin_action`, `destructive`, `metadata_only`, `none` | named option | live | public | `audit.class = "metadata_only"` |
| `audit.redaction` | Defines the module redaction class. | enum | `sensitive` | `security_critical`, `sensitive`, `standard` | named option | live | public | `audit.redaction = "sensitive"` |
| `export.maximum_bytes` | Bounds one privacy export. | integer | `8388608` | 1024 through 67108864 | bytes | live | public | `export.maximum_bytes = 8388608` |
| `requests.maximum_open_per_player` | Bounds simultaneous privacy requests. | integer | `3` | 1 through 20 | count | live | public | `requests.maximum_open_per_player = 3` |
| `runtime.alt_tracking_collect_addresses` | Collect address derived alternate account data. This is privacy sensitive and disabled by default. | boolean | `false` | `true` or `false` | boolean flag | live | sensitive | `runtime.alt_tracking_collect_addresses = false` |
| `runtime.alt_tracking_hash_addresses` | Store a one way server local hash instead of raw addresses | boolean | `true` | `true` or `false` | boolean flag | live | sensitive | `runtime.alt_tracking_hash_addresses = true` |
| `runtime.alt_tracking_retention_days` | Days to retain alternate account observations before automatic purge | integer | `30` | 1 through 3650 | days | live | public | `runtime.alt_tracking_retention_days = 30` |
| `runtime.check_alts_entry_format` | Format for each alt entry. Placeholders: $name, $uuid, $lastseen | string | `&7- &e$name &7($uuid) Last seen: $lastseen` | 0 through 8192 characters | text | live | public | `runtime.check_alts_entry_format = "&7- &e$name &7($uuid) Last seen: $lastseen"` |
| `runtime.check_alts_header_format` | Header for alts list. Placeholders: $player, $ip | string | `&6━━━━ Alts for $player ($ip) ━━━━` | 0 through 8192 characters | text | live | public | `runtime.check_alts_header_format = "&6━━━━ Alts for $player ($ip) ━━━━"` |
| `runtime.check_alts_no_alts_msg` | Message when no alts found. Placeholder: $player | string | `&7No alternate accounts found for $player.` | 0 through 8192 characters | text | live | public | `runtime.check_alts_no_alts_msg = "&7No alternate accounts found for $player."` |
| `runtime.enable_check_alts` | Alt-account checker (/checkalts) | boolean | `true` | `true` or `false` | boolean flag | restart_required | public | `runtime.enable_check_alts = true` |

## private_messages

Controls private messages behavior, presentation, bounds, storage, and diagnostics.

File: `config/sef/modules/private_messages.toml`. Schema version: `1`. Documentation version: `2`. Apply class: `live`. Dependencies: `commands`, `core`, `gui`, `messages`, `permissions`. Conflicts: none.

Inspect permission: `sef.commands.config.inspect`. Edit permission: `sef.commands.config.edit`. Migration path: inspect the retained legacy `common.toml` with `/sef config migrate dryrun`, then request a revision-bound confirmation with `/sef config migrate apply <expected_revision>`. Legacy cooldown durations move to `sef.cooldown.<action>.<seconds>` permissions instead of module files.

| Setting | Description | Type | Default | Bounds | Unit | Apply class | Privacy | Example |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `module.enabled` | Enables the module after dependency validation. | boolean | `true` | `true` or `false` | boolean flag | live | public | `module.enabled = true` |
| `module.disabled_behavior` | Defines truthful behavior while this module is disabled. | enum | `command_unavailable` | `command_unavailable`, `provider_fallback`, `read_only` | named option | live | public | `module.disabled_behavior = "command_unavailable"` |
| `gui.mode` | Selects enhanced GUI presentation without changing command access. | enum | `inherit` | `command_only`, `gui_preferred`, `inherit`, `off`, `on` | named option | live | public | `gui.mode = "inherit"` |
| `gui.bare_command_opens` | Allows eligible bare command routes to prefer a dedicated workflow. | boolean | `true` | `true` or `false` | boolean flag | live | public | `gui.bare_command_opens = true` |
| `gui.default_page` | Selects the stable default workflow page. | string | `overview` | 1 through 64 characters, pattern `[a-z0-9_]+` | text | live | public | `gui.default_page = "overview"` |
| `limits.maximum_records` | Bounds persisted records owned by this module. | integer | `10000` | 1 through 1000000 | count | live | public | `limits.maximum_records = 10000` |
| `limits.maximum_page_size` | Bounds command and GUI result pages. | integer | `45` | 4 through 100 | count | live | public | `limits.maximum_page_size = 45` |
| `rate.maximum_actions_per_minute` | Bounds admission attempts independently of permission cooldown duration. | integer | `300` | 1 through 10000 | actions per minute | live | public | `rate.maximum_actions_per_minute = 300` |
| `storage.retention_days` | Defines bounded data retention where the module owns persistent records. | integer | `30` | 0 through 3650 | days | live | public | `storage.retention_days = 30` |
| `failure.mode` | Defines provider and validation failure behavior. | enum | `previous_known_good` | `fail_closed`, `previous_known_good`, `read_only` | named option | live | public | `failure.mode = "previous_known_good"` |
| `audit.class` | Defines the minimum audit class for module actions. | enum | `metadata_only` | `admin_action`, `destructive`, `metadata_only`, `none` | named option | live | public | `audit.class = "metadata_only"` |
| `audit.redaction` | Defines the module redaction class. | enum | `standard` | `security_critical`, `sensitive`, `standard` | named option | live | public | `audit.redaction = "standard"` |
| `runtime.click_to_message_hover` | Hover text for click to message. Placeholder: $player | string | `&dClick to message $player` | 0 through 8192 characters | text | live | public | `runtime.click_to_message_hover = "&dClick to message $player"` |
| `runtime.click_to_reply_hover` | Hover text for click to reply. Placeholder: $player | string | `&eClick to reply` | 0 through 8192 characters | text | live | public | `runtime.click_to_reply_hover = "&eClick to reply"` |
| `runtime.enable_chat_replies` | Click-to-reply system (/ans, clickable chat messages, chat logging). Requires chat_formatting = true (SEF must own the chat line to make it clickable). | boolean | `true` | `true` or `false` | boolean flag | restart_required | public | `runtime.enable_chat_replies = true` |
| `runtime.enable_messaging_system` | Private messaging system (/msg, /r, /tell, /w) | boolean | `true` | `true` or `false` | boolean flag | restart_required | public | `runtime.enable_messaging_system = true` |
| `runtime.message_not_found_msg` | Message when reply target message not found | string | `&cMessage not found or too old to reply to.` | 0 through 8192 characters | text | live | public | `runtime.message_not_found_msg = "&cMessage not found or too old to reply to."` |
| `runtime.msg_received_format` | Format for incoming private messages. Placeholders: $sender, $receiver, $message | string | `&d&lFrom &d$sender&7: &r&7$message` | 0 through 8192 characters | text | live | public | `runtime.msg_received_format = "&d&lFrom &d$sender&7: &r&7$message"` |
| `runtime.msg_sent_format` | Format for outgoing private messages. Placeholders: $sender, $receiver, $message | string | `&d&lTo &d$receiver&7: &r&7$message` | 0 through 8192 characters | text | live | public | `runtime.msg_sent_format = "&d&lTo &d$receiver&7: &r&7$message"` |
| `runtime.no_reply_target_msg` | Message when there's no one to reply to | string | `&cNo one to reply to.` | 0 through 8192 characters | text | live | public | `runtime.no_reply_target_msg = "&cNo one to reply to."` |
| `runtime.private_message_maximum_length` | Maximum private message length | integer | `2048` | 1 through 16384 | count | live | public | `runtime.private_message_maximum_length = 2048` |
| `runtime.reply_body_format` | Format for the reply body line (the replier's message). Placeholders: $replier, $message   $replier will include the player's rank/prefix/suffix from LuckPerms if available | string | `$replier&7: &r$message` | 0 through 8192 characters | text | live | public | `runtime.reply_body_format = "$replier&7: &r$message"` |
| `runtime.reply_header_format` | Format for reply header. Placeholders: $replier, $original_sender, $summary | string | `    &f&l┌────&r &7Replying to $original_sender&7: &7$summary` | 0 through 8192 characters | text | live | public | `runtime.reply_header_format = "    &f&l┌────&r &7Replying to $original_sender&7: &7$summary"` |
| `runtime.reply_summary_length` | Maximum length of message summary shown in reply headers (0 = no limit) | integer | `50` | 0 through 500 | count | live | public | `runtime.reply_summary_length = 50` |

## random_teleport

Controls random teleport behavior, presentation, bounds, storage, and diagnostics.

File: `config/sef/modules/random_teleport.toml`. Schema version: `1`. Documentation version: `2`. Apply class: `live`. Dependencies: `commands`, `core`, `gui`, `messages`, `permissions`. Conflicts: none.

Inspect permission: `sef.commands.config.inspect`. Edit permission: `sef.commands.config.edit`. Migration path: inspect the retained legacy `common.toml` with `/sef config migrate dryrun`, then request a revision-bound confirmation with `/sef config migrate apply <expected_revision>`. Legacy cooldown durations move to `sef.cooldown.<action>.<seconds>` permissions instead of module files.

| Setting | Description | Type | Default | Bounds | Unit | Apply class | Privacy | Example |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `module.enabled` | Enables the module after dependency validation. | boolean | `true` | `true` or `false` | boolean flag | live | public | `module.enabled = true` |
| `module.disabled_behavior` | Defines truthful behavior while this module is disabled. | enum | `command_unavailable` | `command_unavailable`, `provider_fallback`, `read_only` | named option | live | public | `module.disabled_behavior = "command_unavailable"` |
| `gui.mode` | Selects enhanced GUI presentation without changing command access. | enum | `inherit` | `command_only`, `gui_preferred`, `inherit`, `off`, `on` | named option | live | public | `gui.mode = "inherit"` |
| `gui.bare_command_opens` | Allows eligible bare command routes to prefer a dedicated workflow. | boolean | `true` | `true` or `false` | boolean flag | live | public | `gui.bare_command_opens = true` |
| `gui.default_page` | Selects the stable default workflow page. | string | `overview` | 1 through 64 characters, pattern `[a-z0-9_]+` | text | live | public | `gui.default_page = "overview"` |
| `limits.maximum_records` | Bounds persisted records owned by this module. | integer | `10000` | 1 through 1000000 | count | live | public | `limits.maximum_records = 10000` |
| `limits.maximum_page_size` | Bounds command and GUI result pages. | integer | `45` | 4 through 100 | count | live | public | `limits.maximum_page_size = 45` |
| `rate.maximum_actions_per_minute` | Bounds admission attempts independently of permission cooldown duration. | integer | `300` | 1 through 10000 | actions per minute | live | public | `rate.maximum_actions_per_minute = 300` |
| `storage.retention_days` | Defines bounded data retention where the module owns persistent records. | integer | `30` | 0 through 3650 | days | live | public | `storage.retention_days = 30` |
| `failure.mode` | Defines provider and validation failure behavior. | enum | `previous_known_good` | `fail_closed`, `previous_known_good`, `read_only` | named option | live | public | `failure.mode = "previous_known_good"` |
| `audit.class` | Defines the minimum audit class for module actions. | enum | `metadata_only` | `admin_action`, `destructive`, `metadata_only`, `none` | named option | live | public | `audit.class = "metadata_only"` |
| `audit.redaction` | Defines the module redaction class. | enum | `standard` | `security_critical`, `sensitive`, `standard` | named option | live | public | `audit.redaction = "standard"` |
| `runtime.enable_random_teleport` | Random teleport commands | boolean | `true` | `true` or `false` | boolean flag | restart_required | public | `runtime.enable_random_teleport = true` |
| `runtime.random_teleport_allowed_dimensions` | Comma separated dimension identifiers allowed for random teleport | string | `minecraft:overworld` | 0 through 8192 characters | text | live | public | `runtime.random_teleport_allowed_dimensions = "minecraft:overworld"` |
| `runtime.random_teleport_maximum_attempts` | Maximum random candidates inspected per request | integer | `32` | 1 through 256 | count | live | public | `runtime.random_teleport_maximum_attempts = 32` |
| `runtime.random_teleport_maximum_radius` | Maximum random teleport radius from the configured center | integer | `5000` | 1 through 20000 | count | live | public | `runtime.random_teleport_maximum_radius = 5000` |
| `runtime.random_teleport_minimum_radius` | Minimum random teleport radius from the configured center | integer | `256` | 0 through 20000 | count | live | public | `runtime.random_teleport_minimum_radius = 256` |

## reminders

Controls reminders behavior, presentation, bounds, storage, and diagnostics.

File: `config/sef/modules/reminders.toml`. Schema version: `1`. Documentation version: `2`. Apply class: `live`. Dependencies: `commands`, `core`, `messages`, `permissions`. Conflicts: none.

Inspect permission: `sef.commands.config.inspect`. Edit permission: `sef.commands.config.edit`. Migration path: inspect the retained legacy `common.toml` with `/sef config migrate dryrun`, then request a revision-bound confirmation with `/sef config migrate apply <expected_revision>`. Legacy cooldown durations move to `sef.cooldown.<action>.<seconds>` permissions instead of module files.

| Setting | Description | Type | Default | Bounds | Unit | Apply class | Privacy | Example |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `module.enabled` | Enables the module after dependency validation. | boolean | `true` | `true` or `false` | boolean flag | live | public | `module.enabled = true` |
| `module.disabled_behavior` | Defines truthful behavior while this module is disabled. | enum | `command_unavailable` | `command_unavailable`, `provider_fallback`, `read_only` | named option | live | public | `module.disabled_behavior = "command_unavailable"` |
| `gui.mode` | Selects enhanced GUI presentation without changing command access. | enum | `inherit` | `command_only`, `gui_preferred`, `inherit`, `off`, `on` | named option | live | public | `gui.mode = "inherit"` |
| `gui.bare_command_opens` | Allows eligible bare command routes to prefer a dedicated workflow. | boolean | `true` | `true` or `false` | boolean flag | live | public | `gui.bare_command_opens = true` |
| `gui.default_page` | Selects the stable default workflow page. | string | `overview` | 1 through 64 characters, pattern `[a-z0-9_]+` | text | live | public | `gui.default_page = "overview"` |
| `limits.maximum_records` | Bounds persisted records owned by this module. | integer | `10000` | 1 through 1000000 | count | live | public | `limits.maximum_records = 10000` |
| `limits.maximum_page_size` | Bounds command and GUI result pages. | integer | `45` | 4 through 100 | count | live | public | `limits.maximum_page_size = 45` |
| `rate.maximum_actions_per_minute` | Bounds admission attempts independently of permission cooldown duration. | integer | `300` | 1 through 10000 | actions per minute | live | public | `rate.maximum_actions_per_minute = 300` |
| `storage.retention_days` | Defines bounded data retention where the module owns persistent records. | integer | `30` | 0 through 3650 | days | live | public | `storage.retention_days = 30` |
| `failure.mode` | Defines provider and validation failure behavior. | enum | `previous_known_good` | `fail_closed`, `previous_known_good`, `read_only` | named option | live | public | `failure.mode = "previous_known_good"` |
| `audit.class` | Defines the minimum audit class for module actions. | enum | `metadata_only` | `admin_action`, `destructive`, `metadata_only`, `none` | named option | live | public | `audit.class = "metadata_only"` |
| `audit.redaction` | Defines the module redaction class. | enum | `standard` | `security_critical`, `sensitive`, `standard` | named option | live | public | `audit.redaction = "standard"` |
| `runtime.enable_reminders` | Welcome, onboarding, and reminder delivery | boolean | `true` | `true` or `false` | boolean flag | restart_required | public | `runtime.enable_reminders = true` |
| `runtime.optional_client_reminder` | Command fallback reminder for players without an enhanced client | string | `&6This server supports optional SEF enhanced menus. &fEvery feature still works through commands. &eUse /sef commands &ffor available commands.` | 0 through 8192 characters | text | live | public | `runtime.optional_client_reminder = "&6This server supports optional SEF enhanced menus. &fEvery feature still works through commands. &eUse /sef commands &ffor available commands."` |

## repair

Controls repair behavior, presentation, bounds, storage, and diagnostics.

File: `config/sef/modules/repair.toml`. Schema version: `1`. Documentation version: `2`. Apply class: `live`. Dependencies: `commands`, `core`, `gui`, `messages`, `permissions`. Conflicts: none.

Inspect permission: `sef.commands.config.inspect`. Edit permission: `sef.commands.config.edit`. Migration path: inspect the retained legacy `common.toml` with `/sef config migrate dryrun`, then request a revision-bound confirmation with `/sef config migrate apply <expected_revision>`. Legacy cooldown durations move to `sef.cooldown.<action>.<seconds>` permissions instead of module files.

| Setting | Description | Type | Default | Bounds | Unit | Apply class | Privacy | Example |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `module.enabled` | Enables the module after dependency validation. | boolean | `true` | `true` or `false` | boolean flag | live | public | `module.enabled = true` |
| `module.disabled_behavior` | Defines truthful behavior while this module is disabled. | enum | `command_unavailable` | `command_unavailable`, `provider_fallback`, `read_only` | named option | live | public | `module.disabled_behavior = "command_unavailable"` |
| `gui.mode` | Selects enhanced GUI presentation without changing command access. | enum | `inherit` | `command_only`, `gui_preferred`, `inherit`, `off`, `on` | named option | live | public | `gui.mode = "inherit"` |
| `gui.bare_command_opens` | Allows eligible bare command routes to prefer a dedicated workflow. | boolean | `true` | `true` or `false` | boolean flag | live | public | `gui.bare_command_opens = true` |
| `gui.default_page` | Selects the stable default workflow page. | string | `overview` | 1 through 64 characters, pattern `[a-z0-9_]+` | text | live | public | `gui.default_page = "overview"` |
| `limits.maximum_records` | Bounds persisted records owned by this module. | integer | `10000` | 1 through 1000000 | count | live | public | `limits.maximum_records = 10000` |
| `limits.maximum_page_size` | Bounds command and GUI result pages. | integer | `45` | 4 through 100 | count | live | public | `limits.maximum_page_size = 45` |
| `rate.maximum_actions_per_minute` | Bounds admission attempts independently of permission cooldown duration. | integer | `300` | 1 through 10000 | actions per minute | live | public | `rate.maximum_actions_per_minute = 300` |
| `storage.retention_days` | Defines bounded data retention where the module owns persistent records. | integer | `30` | 0 through 3650 | days | live | public | `storage.retention_days = 30` |
| `failure.mode` | Defines provider and validation failure behavior. | enum | `previous_known_good` | `fail_closed`, `previous_known_good`, `read_only` | named option | live | public | `failure.mode = "previous_known_good"` |
| `audit.class` | Defines the minimum audit class for module actions. | enum | `metadata_only` | `admin_action`, `destructive`, `metadata_only`, `none` | named option | live | public | `audit.class = "metadata_only"` |
| `audit.redaction` | Defines the module redaction class. | enum | `standard` | `security_critical`, `sensitive`, `standard` | named option | live | public | `audit.redaction = "standard"` |
| `runtime.enable_repair_command` | Held item repair command (/repair) | boolean | `true` | `true` or `false` | boolean flag | restart_required | public | `runtime.enable_repair_command = true` |
| `runtime.repair_not_held_message` | Message shown when no item is held | string | `&cHold the item you want to repair in your main hand.` | 0 through 8192 characters | text | live | public | `runtime.repair_not_held_message = "&cHold the item you want to repair in your main hand."` |
| `runtime.repair_not_needed_message` | Message shown when the held item is not damaged | string | `&eThat item does not need to be repaired.` | 0 through 8192 characters | text | live | public | `runtime.repair_not_needed_message = "&eThat item does not need to be repaired."` |
| `runtime.repair_success_message` | Message shown after repairing the held item. Placeholder: $item | string | `&aRepaired &e$item&a.` | 0 through 8192 characters | text | live | public | `runtime.repair_success_message = "&aRepaired &e$item&a."` |

## run_and_silent

Controls run and silent behavior, presentation, bounds, storage, and diagnostics.

File: `config/sef/modules/run_and_silent.toml`. Schema version: `1`. Documentation version: `2`. Apply class: `live`. Dependencies: `audit`, `commands`, `core`, `messages`, `permissions`. Conflicts: none.

Inspect permission: `sef.commands.config.inspect`. Edit permission: `sef.commands.config.edit`. Migration path: inspect the retained legacy `common.toml` with `/sef config migrate dryrun`, then request a revision-bound confirmation with `/sef config migrate apply <expected_revision>`. Legacy cooldown durations move to `sef.cooldown.<action>.<seconds>` permissions instead of module files.

| Setting | Description | Type | Default | Bounds | Unit | Apply class | Privacy | Example |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `module.enabled` | Enables the module after dependency validation. | boolean | `true` | `true` or `false` | boolean flag | live | public | `module.enabled = true` |
| `module.disabled_behavior` | Defines truthful behavior while this module is disabled. | enum | `command_unavailable` | `command_unavailable`, `provider_fallback`, `read_only` | named option | live | public | `module.disabled_behavior = "command_unavailable"` |
| `gui.mode` | Selects enhanced GUI presentation without changing command access. | enum | `inherit` | `command_only`, `gui_preferred`, `inherit`, `off`, `on` | named option | live | public | `gui.mode = "inherit"` |
| `gui.bare_command_opens` | Allows eligible bare command routes to prefer a dedicated workflow. | boolean | `true` | `true` or `false` | boolean flag | live | public | `gui.bare_command_opens = true` |
| `gui.default_page` | Selects the stable default workflow page. | string | `overview` | 1 through 64 characters, pattern `[a-z0-9_]+` | text | live | public | `gui.default_page = "overview"` |
| `limits.maximum_records` | Bounds persisted records owned by this module. | integer | `1000` | 1 through 100000 | count | live | public | `limits.maximum_records = 1000` |
| `limits.maximum_page_size` | Bounds command and GUI result pages. | integer | `45` | 4 through 100 | count | live | public | `limits.maximum_page_size = 45` |
| `rate.maximum_actions_per_minute` | Bounds admission attempts independently of permission cooldown duration. | integer | `30` | 1 through 10000 | actions per minute | live | public | `rate.maximum_actions_per_minute = 30` |
| `storage.retention_days` | Defines bounded data retention where the module owns persistent records. | integer | `90` | 0 through 3650 | days | live | public | `storage.retention_days = 90` |
| `failure.mode` | Defines provider and validation failure behavior. | enum | `fail_closed` | `fail_closed`, `previous_known_good`, `read_only` | named option | live | public | `failure.mode = "fail_closed"` |
| `audit.class` | Defines the minimum audit class for module actions. | enum | `admin_action` | `admin_action`, `destructive`, `metadata_only`, `none` | named option | live | public | `audit.class = "admin_action"` |
| `audit.redaction` | Defines the module redaction class. | enum | `sensitive` | `security_critical`, `sensitive`, `standard` | named option | live | public | `audit.redaction = "sensitive"` |
| `runtime.run_allowed_commands` | Comma separated roots allowed through direct run and silent server execution. Empty denies every root. | string | `` | 0 through 8192 characters | text | live | sensitive | `runtime.run_allowed_commands = ""` |
| `runtime.run_denied_commands` | Comma separated roots denied through direct run and silent server execution. | string | `run,silent,sudo,op,deop,stop,reload` | 0 through 8192 characters | text | live | sensitive | `runtime.run_denied_commands = "run,silent,sudo,op,deop,stop,reload"` |
| `runtime.silent_actor_allowed_commands` | Comma separated roots allowed through silent actor execution. Empty denies every root. | string | `` | 0 through 8192 characters | text | live | sensitive | `runtime.silent_actor_allowed_commands = ""` |
| `runtime.silent_actor_denied_commands` | Comma separated roots denied through silent actor execution. | string | `run,silent,sudo` | 0 through 8192 characters | text | live | sensitive | `runtime.silent_actor_denied_commands = "run,silent,sudo"` |

## server_control

Controls server control behavior, presentation, bounds, storage, and diagnostics.

File: `config/sef/modules/server_control.toml`. Schema version: `1`. Documentation version: `2`. Apply class: `live`. Dependencies: `audit`, `commands`, `core`, `gui`, `messages`, `permissions`. Conflicts: none.

Inspect permission: `sef.commands.config.inspect`. Edit permission: `sef.commands.config.edit`. Migration path: inspect the retained legacy `common.toml` with `/sef config migrate dryrun`, then request a revision-bound confirmation with `/sef config migrate apply <expected_revision>`. Legacy cooldown durations move to `sef.cooldown.<action>.<seconds>` permissions instead of module files.

| Setting | Description | Type | Default | Bounds | Unit | Apply class | Privacy | Example |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `module.enabled` | Enables the module after dependency validation. | boolean | `true` | `true` or `false` | boolean flag | live | public | `module.enabled = true` |
| `module.disabled_behavior` | Defines truthful behavior while this module is disabled. | enum | `command_unavailable` | `command_unavailable`, `provider_fallback`, `read_only` | named option | live | public | `module.disabled_behavior = "command_unavailable"` |
| `gui.mode` | Selects enhanced GUI presentation without changing command access. | enum | `inherit` | `command_only`, `gui_preferred`, `inherit`, `off`, `on` | named option | live | public | `gui.mode = "inherit"` |
| `gui.bare_command_opens` | Allows eligible bare command routes to prefer a dedicated workflow. | boolean | `true` | `true` or `false` | boolean flag | live | public | `gui.bare_command_opens = true` |
| `gui.default_page` | Selects the stable default workflow page. | string | `overview` | 1 through 64 characters, pattern `[a-z0-9_]+` | text | live | public | `gui.default_page = "overview"` |
| `limits.maximum_records` | Bounds persisted records owned by this module. | integer | `1000` | 1 through 100000 | count | live | public | `limits.maximum_records = 1000` |
| `limits.maximum_page_size` | Bounds command and GUI result pages. | integer | `45` | 4 through 100 | count | live | public | `limits.maximum_page_size = 45` |
| `rate.maximum_actions_per_minute` | Bounds admission attempts independently of permission cooldown duration. | integer | `30` | 1 through 10000 | actions per minute | live | public | `rate.maximum_actions_per_minute = 30` |
| `storage.retention_days` | Defines bounded data retention where the module owns persistent records. | integer | `90` | 0 through 3650 | days | live | public | `storage.retention_days = 90` |
| `failure.mode` | Defines provider and validation failure behavior. | enum | `fail_closed` | `fail_closed`, `previous_known_good`, `read_only` | named option | live | public | `failure.mode = "fail_closed"` |
| `audit.class` | Defines the minimum audit class for module actions. | enum | `admin_action` | `admin_action`, `destructive`, `metadata_only`, `none` | named option | live | public | `audit.class = "admin_action"` |
| `audit.redaction` | Defines the module redaction class. | enum | `sensitive` | `security_critical`, `sensitive`, `standard` | named option | live | public | `audit.redaction = "sensitive"` |

## social

Controls social behavior, presentation, bounds, storage, and diagnostics.

File: `config/sef/modules/social.toml`. Schema version: `1`. Documentation version: `2`. Apply class: `live`. Dependencies: `commands`, `core`, `messages`, `permissions`. Conflicts: none.

Inspect permission: `sef.commands.config.inspect`. Edit permission: `sef.commands.config.edit`. Migration path: inspect the retained legacy `common.toml` with `/sef config migrate dryrun`, then request a revision-bound confirmation with `/sef config migrate apply <expected_revision>`. Legacy cooldown durations move to `sef.cooldown.<action>.<seconds>` permissions instead of module files.

| Setting | Description | Type | Default | Bounds | Unit | Apply class | Privacy | Example |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `module.enabled` | Enables the module after dependency validation. | boolean | `true` | `true` or `false` | boolean flag | live | public | `module.enabled = true` |
| `module.disabled_behavior` | Defines truthful behavior while this module is disabled. | enum | `command_unavailable` | `command_unavailable`, `provider_fallback`, `read_only` | named option | live | public | `module.disabled_behavior = "command_unavailable"` |
| `gui.mode` | Selects enhanced GUI presentation without changing command access. | enum | `inherit` | `command_only`, `gui_preferred`, `inherit`, `off`, `on` | named option | live | public | `gui.mode = "inherit"` |
| `gui.bare_command_opens` | Allows eligible bare command routes to prefer a dedicated workflow. | boolean | `true` | `true` or `false` | boolean flag | live | public | `gui.bare_command_opens = true` |
| `gui.default_page` | Selects the stable default workflow page. | string | `overview` | 1 through 64 characters, pattern `[a-z0-9_]+` | text | live | public | `gui.default_page = "overview"` |
| `limits.maximum_records` | Bounds persisted records owned by this module. | integer | `10000` | 1 through 1000000 | count | live | public | `limits.maximum_records = 10000` |
| `limits.maximum_page_size` | Bounds command and GUI result pages. | integer | `45` | 4 through 100 | count | live | public | `limits.maximum_page_size = 45` |
| `rate.maximum_actions_per_minute` | Bounds admission attempts independently of permission cooldown duration. | integer | `300` | 1 through 10000 | actions per minute | live | public | `rate.maximum_actions_per_minute = 300` |
| `storage.retention_days` | Defines bounded data retention where the module owns persistent records. | integer | `30` | 0 through 3650 | days | live | public | `storage.retention_days = 30` |
| `failure.mode` | Defines provider and validation failure behavior. | enum | `previous_known_good` | `fail_closed`, `previous_known_good`, `read_only` | named option | live | public | `failure.mode = "previous_known_good"` |
| `audit.class` | Defines the minimum audit class for module actions. | enum | `metadata_only` | `admin_action`, `destructive`, `metadata_only`, `none` | named option | live | public | `audit.class = "metadata_only"` |
| `audit.redaction` | Defines the module redaction class. | enum | `standard` | `security_critical`, `sensitive`, `standard` | named option | live | public | `audit.redaction = "standard"` |
| `runtime.enable_admin_chat` | Admin chat system (/ac, /chat admin) | boolean | `true` | `true` or `false` | boolean flag | restart_required | public | `runtime.enable_admin_chat = true` |
| `runtime.enable_help_op` | /helpop command for players to request operator help | boolean | `true` | `true` or `false` | boolean flag | restart_required | public | `runtime.enable_help_op = true` |
| `runtime.enable_op_bulletin` | Operator bulletin system (/opbulletin) | boolean | `true` | `true` or `false` | boolean flag | restart_required | public | `runtime.enable_op_bulletin = true` |
| `runtime.enable_social_essentials` | Social, identity, mail, and connection message platform | boolean | `true` | `true` or `false` | boolean flag | restart_required | public | `runtime.enable_social_essentials = true` |

## social_spy

Controls social spy behavior, presentation, bounds, storage, and diagnostics.

File: `config/sef/modules/social_spy.toml`. Schema version: `1`. Documentation version: `2`. Apply class: `live`. Dependencies: `commands`, `core`, `messages`, `permissions`. Conflicts: none.

Inspect permission: `sef.commands.config.inspect`. Edit permission: `sef.commands.config.edit`. Migration path: inspect the retained legacy `common.toml` with `/sef config migrate dryrun`, then request a revision-bound confirmation with `/sef config migrate apply <expected_revision>`. Legacy cooldown durations move to `sef.cooldown.<action>.<seconds>` permissions instead of module files.

| Setting | Description | Type | Default | Bounds | Unit | Apply class | Privacy | Example |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `module.enabled` | Enables the module after dependency validation. | boolean | `true` | `true` or `false` | boolean flag | live | public | `module.enabled = true` |
| `module.disabled_behavior` | Defines truthful behavior while this module is disabled. | enum | `command_unavailable` | `command_unavailable`, `provider_fallback`, `read_only` | named option | live | public | `module.disabled_behavior = "command_unavailable"` |
| `gui.mode` | Selects enhanced GUI presentation without changing command access. | enum | `inherit` | `command_only`, `gui_preferred`, `inherit`, `off`, `on` | named option | live | public | `gui.mode = "inherit"` |
| `gui.bare_command_opens` | Allows eligible bare command routes to prefer a dedicated workflow. | boolean | `true` | `true` or `false` | boolean flag | live | public | `gui.bare_command_opens = true` |
| `gui.default_page` | Selects the stable default workflow page. | string | `overview` | 1 through 64 characters, pattern `[a-z0-9_]+` | text | live | public | `gui.default_page = "overview"` |
| `limits.maximum_records` | Bounds persisted records owned by this module. | integer | `10000` | 1 through 1000000 | count | live | public | `limits.maximum_records = 10000` |
| `limits.maximum_page_size` | Bounds command and GUI result pages. | integer | `45` | 4 through 100 | count | live | public | `limits.maximum_page_size = 45` |
| `rate.maximum_actions_per_minute` | Bounds admission attempts independently of permission cooldown duration. | integer | `300` | 1 through 10000 | actions per minute | live | public | `rate.maximum_actions_per_minute = 300` |
| `storage.retention_days` | Defines bounded data retention where the module owns persistent records. | integer | `30` | 0 through 3650 | days | live | public | `storage.retention_days = 30` |
| `failure.mode` | Defines provider and validation failure behavior. | enum | `previous_known_good` | `fail_closed`, `previous_known_good`, `read_only` | named option | live | public | `failure.mode = "previous_known_good"` |
| `audit.class` | Defines the minimum audit class for module actions. | enum | `metadata_only` | `admin_action`, `destructive`, `metadata_only`, `none` | named option | live | public | `audit.class = "metadata_only"` |
| `audit.redaction` | Defines the module redaction class. | enum | `standard` | `security_critical`, `sensitive`, `standard` | named option | live | public | `audit.redaction = "standard"` |
| `runtime.enable_social_spy` | Permission controlled private message observation | boolean | `true` | `true` or `false` | boolean flag | restart_required | public | `runtime.enable_social_spy = true` |
| `runtime.social_spy_events_per_second` | Maximum social spy events delivered to one observer per second | integer | `100` | 1 through 1000 | count | live | public | `runtime.social_spy_events_per_second = 100` |
| `runtime.social_spy_format` | Typed social spy template. Placeholders are {from}, {to}, {message}, {route}, and {timestamp} | string | `&8[&b{from}&8] &7-> &8[&d{to}&8]&7: &f{message}` | 0 through 8192 characters | text | live | public | `runtime.social_spy_format = "&8[&b{from}&8] &7-> &8[&d{to}&8]&7: &f{message}"` |
| `runtime.social_spy_recent_limit` | Maximum already authorized social spy events retained per observer session | integer | `50` | 0 through 500 | count | live | public | `runtime.social_spy_recent_limit = 50` |

## spawn

Controls spawn behavior, presentation, bounds, storage, and diagnostics.

File: `config/sef/modules/spawn.toml`. Schema version: `1`. Documentation version: `2`. Apply class: `live`. Dependencies: `commands`, `core`, `gui`, `messages`, `permissions`. Conflicts: none.

Inspect permission: `sef.commands.config.inspect`. Edit permission: `sef.commands.config.edit`. Migration path: inspect the retained legacy `common.toml` with `/sef config migrate dryrun`, then request a revision-bound confirmation with `/sef config migrate apply <expected_revision>`. Legacy cooldown durations move to `sef.cooldown.<action>.<seconds>` permissions instead of module files.

| Setting | Description | Type | Default | Bounds | Unit | Apply class | Privacy | Example |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `module.enabled` | Enables the module after dependency validation. | boolean | `true` | `true` or `false` | boolean flag | live | public | `module.enabled = true` |
| `module.disabled_behavior` | Defines truthful behavior while this module is disabled. | enum | `command_unavailable` | `command_unavailable`, `provider_fallback`, `read_only` | named option | live | public | `module.disabled_behavior = "command_unavailable"` |
| `gui.mode` | Selects enhanced GUI presentation without changing command access. | enum | `inherit` | `command_only`, `gui_preferred`, `inherit`, `off`, `on` | named option | live | public | `gui.mode = "inherit"` |
| `gui.bare_command_opens` | Allows eligible bare command routes to prefer a dedicated workflow. | boolean | `true` | `true` or `false` | boolean flag | live | public | `gui.bare_command_opens = true` |
| `gui.default_page` | Selects the stable default workflow page. | string | `overview` | 1 through 64 characters, pattern `[a-z0-9_]+` | text | live | public | `gui.default_page = "overview"` |
| `limits.maximum_records` | Bounds persisted records owned by this module. | integer | `10000` | 1 through 1000000 | count | live | public | `limits.maximum_records = 10000` |
| `limits.maximum_page_size` | Bounds command and GUI result pages. | integer | `45` | 4 through 100 | count | live | public | `limits.maximum_page_size = 45` |
| `rate.maximum_actions_per_minute` | Bounds admission attempts independently of permission cooldown duration. | integer | `300` | 1 through 10000 | actions per minute | live | public | `rate.maximum_actions_per_minute = 300` |
| `storage.retention_days` | Defines bounded data retention where the module owns persistent records. | integer | `30` | 0 through 3650 | days | live | public | `storage.retention_days = 30` |
| `failure.mode` | Defines provider and validation failure behavior. | enum | `previous_known_good` | `fail_closed`, `previous_known_good`, `read_only` | named option | live | public | `failure.mode = "previous_known_good"` |
| `audit.class` | Defines the minimum audit class for module actions. | enum | `metadata_only` | `admin_action`, `destructive`, `metadata_only`, `none` | named option | live | public | `audit.class = "metadata_only"` |
| `audit.redaction` | Defines the module redaction class. | enum | `standard` | `security_critical`, `sensitive`, `standard` | named option | live | public | `audit.redaction = "standard"` |
| `runtime.enable_spawn_commands` | Spawn commands | boolean | `true` | `true` or `false` | boolean flag | restart_required | public | `runtime.enable_spawn_commands = true` |

## sudo

Controls sudo behavior, presentation, bounds, storage, and diagnostics.

File: `config/sef/modules/sudo.toml`. Schema version: `1`. Documentation version: `3`. Apply class: `live`. Dependencies: `audit`, `commands`, `core`, `messages`, `permissions`. Conflicts: none.

Inspect permission: `sef.commands.config.inspect`. Edit permission: `sef.commands.config.edit`. Migration path: inspect the retained legacy `common.toml` with `/sef config migrate dryrun`, then request a revision-bound confirmation with `/sef config migrate apply <expected_revision>`. Legacy cooldown durations move to `sef.cooldown.<action>.<seconds>` permissions instead of module files.

| Setting | Description | Type | Default | Bounds | Unit | Apply class | Privacy | Example |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `module.enabled` | Enables the module after dependency validation. | boolean | `true` | `true` or `false` | boolean flag | live | public | `module.enabled = true` |
| `module.disabled_behavior` | Defines truthful behavior while this module is disabled. | enum | `command_unavailable` | `command_unavailable`, `provider_fallback`, `read_only` | named option | live | public | `module.disabled_behavior = "command_unavailable"` |
| `gui.mode` | Selects enhanced GUI presentation without changing command access. | enum | `inherit` | `command_only`, `gui_preferred`, `inherit`, `off`, `on` | named option | live | public | `gui.mode = "inherit"` |
| `gui.bare_command_opens` | Allows eligible bare command routes to prefer a dedicated workflow. | boolean | `true` | `true` or `false` | boolean flag | live | public | `gui.bare_command_opens = true` |
| `gui.default_page` | Selects the stable default workflow page. | string | `overview` | 1 through 64 characters, pattern `[a-z0-9_]+` | text | live | public | `gui.default_page = "overview"` |
| `limits.maximum_records` | Bounds persisted records owned by this module. | integer | `1000` | 1 through 100000 | count | live | public | `limits.maximum_records = 1000` |
| `limits.maximum_page_size` | Bounds command and GUI result pages. | integer | `45` | 4 through 100 | count | live | public | `limits.maximum_page_size = 45` |
| `rate.maximum_actions_per_minute` | Bounds admission attempts independently of permission cooldown duration. | integer | `30` | 1 through 10000 | actions per minute | live | public | `rate.maximum_actions_per_minute = 30` |
| `storage.retention_days` | Defines bounded data retention where the module owns persistent records. | integer | `90` | 0 through 3650 | days | live | public | `storage.retention_days = 90` |
| `failure.mode` | Defines provider and validation failure behavior. | enum | `fail_closed` | `fail_closed`, `previous_known_good`, `read_only` | named option | live | public | `failure.mode = "fail_closed"` |
| `audit.class` | Defines the minimum audit class for module actions. | enum | `admin_action` | `admin_action`, `destructive`, `metadata_only`, `none` | named option | live | public | `audit.class = "admin_action"` |
| `audit.redaction` | Defines the module redaction class. | enum | `sensitive` | `security_critical`, `sensitive`, `standard` | named option | live | public | `audit.redaction = "sensitive"` |
| `delegation.enabled` | Enables separately authorized one execution delegated sudo grants. | boolean | `false` | `true` or `false` | boolean flag | live | public | `delegation.enabled = false` |
| `delegation.compatibility_boolean_syntax` | Allows the compatibility boolean delegated mode syntax. | boolean | `true` | `true` or `false` | boolean flag | live | public | `delegation.compatibility_boolean_syntax = true` |
| `delegation.require_target_consent` | Requires target consent specifically for delegated sudo. | boolean | `false` | `true` or `false` | boolean flag | live | public | `delegation.require_target_consent = false` |
| `delegation.allow_self_delegation` | Allows separately permitted delegated execution against the issuer. | boolean | `false` | `true` or `false` | boolean flag | live | public | `delegation.allow_self_delegation = false` |
| `delegation.maximum_temporary_vanilla_permission_level` | Bounds temporary vanilla command requirements for one dispatch. | integer | `2` | 0 through 2 | level | live | public | `delegation.maximum_temporary_vanilla_permission_level = 2` |
| `delegation.grant_lifetime_seconds` | Bounds an admitted grant lifetime before dispatch. | integer | `15` | 1 through 60 | seconds | live | public | `delegation.grant_lifetime_seconds = 15` |
| `delegation.confirmation_required` | Requires exact single use confirmation before grant publication. | boolean | `true` | `true` or `false` | boolean flag | live | public | `delegation.confirmation_required = true` |
| `delegation.notify_target` | Notifies the effective target after delegated execution. | boolean | `true` | `true` or `false` | boolean flag | live | public | `delegation.notify_target = true` |
| `delegation.allow_unknown_external_permission_checks` | Allows unknown provider checks that cannot prove scoped authority. | boolean | `false` | `true` or `false` | boolean flag | live | public | `delegation.allow_unknown_external_permission_checks = false` |
| `delegation.allow_redirects` | Allows only statically admitted command redirects. | boolean | `false` | `true` or `false` | boolean flag | live | public | `delegation.allow_redirects = false` |
| `delegation.allow_forks` | Allows only bounded and previewed command forks. | boolean | `false` | `true` or `false` | boolean flag | live | public | `delegation.allow_forks = false` |
| `delegation.allow_async` | Allows profiles that retain execution for asynchronous work. | boolean | `false` | `true` or `false` | boolean flag | live | public | `delegation.allow_async = false` |
| `delegation.allowed_roots` | Lists roots eligible for a published delegation profile. | string | `effect` | 0 through 4096 characters | text | live | public | `delegation.allowed_roots = "effect"` |
| `delegation.denied_roots` | Adds delegated roots denied beyond the code hard deny set. | string | `op,deop,stop,reload,sudo,run,silent,execute,function,schedule` | 0 through 4096 characters | text | live | public | `delegation.denied_roots = "op,deop,stop,reload,sudo,run,silent,execute,function,schedule"` |
| `runtime.enable_sudo` | /sudo command | boolean | `false` | `true` or `false` | boolean flag | restart_required | sensitive | `runtime.enable_sudo = false` |
| `runtime.sudo_allowed_commands` | Comma separated command roots allowed through sudo. An empty value denies every command. | string | `msg,tell,w,r,me` | 0 through 8192 characters | text | live | sensitive | `runtime.sudo_allowed_commands = "msg,tell,w,r,me"` |
| `runtime.sudo_denied_commands` | Comma separated command roots denied even when allowedCommands contains them or a wildcard. | string | `sudo,execute,op,deop,stop,reload,ban,ban-ip,pardon,pardon-ip,whitelist,luckperms,lp,sef` | 0 through 8192 characters | text | live | sensitive | `runtime.sudo_denied_commands = "sudo,execute,op,deop,stop,reload,ban,ban-ip,pardon,pardon-ip,whitelist,luckperms,lp,sef"` |
| `runtime.sudo_executed_msg` | Message shown to the admin. Placeholders: $player, $command, $admin | string | `&aForced $player to execute: &7/$command` | 0 through 8192 characters | text | live | sensitive | `runtime.sudo_executed_msg = "&aForced $player to execute: &7/$command"` |
| `runtime.sudo_maximum_command_length` | Maximum command length accepted by sudo. | integer | `512` | 1 through 8192 | count | live | sensitive | `runtime.sudo_maximum_command_length = 512` |
| `runtime.sudo_notify_msg` | Message shown to the target player. Placeholders: $admin, $command | string | `&c$admin forced you to run: &7/$command` | 0 through 8192 characters | text | live | sensitive | `runtime.sudo_notify_msg = "&c$admin forced you to run: &7/$command"` |
| `runtime.sudo_notify_target` | Notify the target when an administrator uses sudo. | boolean | `true` | `true` or `false` | boolean flag | live | sensitive | `runtime.sudo_notify_target = true` |

## super_enchanting

Controls super enchanting behavior, presentation, bounds, storage, and diagnostics.

File: `config/sef/modules/super_enchanting.toml`. Schema version: `1`. Documentation version: `2`. Apply class: `live`. Dependencies: `commands`, `core`, `enchanting`, `gui`, `messages`, `permissions`. Conflicts: none.

Inspect permission: `sef.commands.config.inspect`. Edit permission: `sef.commands.config.edit`. Migration path: inspect the retained legacy `common.toml` with `/sef config migrate dryrun`, then request a revision-bound confirmation with `/sef config migrate apply <expected_revision>`. Legacy cooldown durations move to `sef.cooldown.<action>.<seconds>` permissions instead of module files.

| Setting | Description | Type | Default | Bounds | Unit | Apply class | Privacy | Example |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `module.enabled` | Enables the module after dependency validation. | boolean | `true` | `true` or `false` | boolean flag | live | public | `module.enabled = true` |
| `module.disabled_behavior` | Defines truthful behavior while this module is disabled. | enum | `command_unavailable` | `command_unavailable`, `provider_fallback`, `read_only` | named option | live | public | `module.disabled_behavior = "command_unavailable"` |
| `gui.mode` | Selects enhanced GUI presentation without changing command access. | enum | `inherit` | `command_only`, `gui_preferred`, `inherit`, `off`, `on` | named option | live | public | `gui.mode = "inherit"` |
| `gui.bare_command_opens` | Allows eligible bare command routes to prefer a dedicated workflow. | boolean | `true` | `true` or `false` | boolean flag | live | public | `gui.bare_command_opens = true` |
| `gui.default_page` | Selects the stable default workflow page. | string | `overview` | 1 through 64 characters, pattern `[a-z0-9_]+` | text | live | public | `gui.default_page = "overview"` |
| `limits.maximum_records` | Bounds persisted records owned by this module. | integer | `10000` | 1 through 1000000 | count | live | public | `limits.maximum_records = 10000` |
| `limits.maximum_page_size` | Bounds command and GUI result pages. | integer | `45` | 4 through 100 | count | live | public | `limits.maximum_page_size = 45` |
| `rate.maximum_actions_per_minute` | Bounds admission attempts independently of permission cooldown duration. | integer | `300` | 1 through 10000 | actions per minute | live | public | `rate.maximum_actions_per_minute = 300` |
| `storage.retention_days` | Defines bounded data retention where the module owns persistent records. | integer | `30` | 0 through 3650 | days | live | public | `storage.retention_days = 30` |
| `failure.mode` | Defines provider and validation failure behavior. | enum | `previous_known_good` | `fail_closed`, `previous_known_good`, `read_only` | named option | live | public | `failure.mode = "previous_known_good"` |
| `audit.class` | Defines the minimum audit class for module actions. | enum | `metadata_only` | `admin_action`, `destructive`, `metadata_only`, `none` | named option | live | public | `audit.class = "metadata_only"` |
| `audit.redaction` | Defines the module redaction class. | enum | `standard` | `security_critical`, `sensitive`, `standard` | named option | live | public | `audit.redaction = "standard"` |
| `safety.minimum_level` | Defines the smallest nonzero level selected by the virtual workstation. | integer | `1` | 1 through 1000000 | level | live | public | `safety.minimum_level = 1` |
| `safety.maximum_level` | Defines the administrative enchantment hard ceiling. | integer | `1000` | 1 through 1000000 | level | live | public | `safety.maximum_level = 1000` |
| `safety.allow_unsafe_levels` | Allows separately permitted levels above the enchantment vanilla maximum. | boolean | `true` | `true` or `false` | boolean flag | live | public | `safety.allow_unsafe_levels = true` |
| `safety.allow_arbitrary_items` | Allows separately permitted arbitrary item enchanting. | boolean | `true` | `true` or `false` | boolean flag | live | public | `safety.allow_arbitrary_items = true` |
| `safety.allow_incompatible` | Allows separately permitted incompatible combinations. | boolean | `true` | `true` or `false` | boolean flag | live | public | `safety.allow_incompatible = true` |
| `shortcuts.enable_set` | Requests the collision aware set shortcut. | boolean | `true` | `true` or `false` | boolean flag | restart_required | public | `shortcuts.enable_set = true` |
| `confirmation.seconds` | Defines the lifetime of a destructive enchantment confirmation token. | integer | `60` | 5 through 600 | count | live | public | `confirmation.seconds = 60` |
| `runtime.enable_administrative_enchanting` | Enable the permission gated administrative enchant command. | boolean | `true` | `true` or `false` | boolean flag | restart_required | public | `runtime.enable_administrative_enchanting = true` |
| `runtime.enable_super_enchanting_table_alias` | Enable the /set alias for /superenchantingtable | boolean | `true` | `true` or `false` | boolean flag | restart_required | public | `runtime.enable_super_enchanting_table_alias = true` |
| `runtime.enable_super_enchanting_table_command` | Super enchanting table (/superenchantingtable, /set) | boolean | `true` | `true` or `false` | boolean flag | restart_required | public | `runtime.enable_super_enchanting_table_command = true` |
| `runtime.super_enchanting_allow_unsafe` | Show enchantments that do not normally support the held item and allow incompatible combinations | boolean | `false` | `true` or `false` | boolean flag | live | public | `runtime.super_enchanting_allow_unsafe = false` |
| `runtime.super_enchanting_max_level` | Highest level the super enchanting table can apply. | integer | `1000` | 1 through 1000000 | level | live | public | `runtime.super_enchanting_max_level = 1000` |
| `runtime.super_enchanting_min_level` | Lowest nonzero level the super enchanting table can apply. | integer | `1` | 1 through 1000000 | level | live | public | `runtime.super_enchanting_min_level = 1` |

## teleport_requests

Controls teleport requests behavior, presentation, bounds, storage, and diagnostics.

File: `config/sef/modules/teleport_requests.toml`. Schema version: `1`. Documentation version: `2`. Apply class: `live`. Dependencies: `commands`, `core`, `gui`, `messages`, `permissions`. Conflicts: none.

Inspect permission: `sef.commands.config.inspect`. Edit permission: `sef.commands.config.edit`. Migration path: inspect the retained legacy `common.toml` with `/sef config migrate dryrun`, then request a revision-bound confirmation with `/sef config migrate apply <expected_revision>`. Legacy cooldown durations move to `sef.cooldown.<action>.<seconds>` permissions instead of module files.

| Setting | Description | Type | Default | Bounds | Unit | Apply class | Privacy | Example |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `module.enabled` | Enables the module after dependency validation. | boolean | `true` | `true` or `false` | boolean flag | live | public | `module.enabled = true` |
| `module.disabled_behavior` | Defines truthful behavior while this module is disabled. | enum | `command_unavailable` | `command_unavailable`, `provider_fallback`, `read_only` | named option | live | public | `module.disabled_behavior = "command_unavailable"` |
| `gui.mode` | Selects enhanced GUI presentation without changing command access. | enum | `inherit` | `command_only`, `gui_preferred`, `inherit`, `off`, `on` | named option | live | public | `gui.mode = "inherit"` |
| `gui.bare_command_opens` | Allows eligible bare command routes to prefer a dedicated workflow. | boolean | `true` | `true` or `false` | boolean flag | live | public | `gui.bare_command_opens = true` |
| `gui.default_page` | Selects the stable default workflow page. | string | `overview` | 1 through 64 characters, pattern `[a-z0-9_]+` | text | live | public | `gui.default_page = "overview"` |
| `limits.maximum_records` | Bounds persisted records owned by this module. | integer | `10000` | 1 through 1000000 | count | live | public | `limits.maximum_records = 10000` |
| `limits.maximum_page_size` | Bounds command and GUI result pages. | integer | `45` | 4 through 100 | count | live | public | `limits.maximum_page_size = 45` |
| `rate.maximum_actions_per_minute` | Bounds admission attempts independently of permission cooldown duration. | integer | `300` | 1 through 10000 | actions per minute | live | public | `rate.maximum_actions_per_minute = 300` |
| `storage.retention_days` | Defines bounded data retention where the module owns persistent records. | integer | `30` | 0 through 3650 | days | live | public | `storage.retention_days = 30` |
| `failure.mode` | Defines provider and validation failure behavior. | enum | `previous_known_good` | `fail_closed`, `previous_known_good`, `read_only` | named option | live | public | `failure.mode = "previous_known_good"` |
| `audit.class` | Defines the minimum audit class for module actions. | enum | `metadata_only` | `admin_action`, `destructive`, `metadata_only`, `none` | named option | live | public | `audit.class = "metadata_only"` |
| `audit.redaction` | Defines the module redaction class. | enum | `standard` | `security_critical`, `sensitive`, `standard` | named option | live | public | `audit.redaction = "standard"` |
| `runtime.enable_teleport_requests` | Teleport request commands | boolean | `true` | `true` or `false` | boolean flag | restart_required | public | `runtime.enable_teleport_requests = true` |
| `runtime.teleport_request_expiry_seconds` | Lifetime of a pending teleport request | integer | `60` | 1 through 3600 | seconds | live | public | `runtime.teleport_request_expiry_seconds = 60` |

## vanish

Controls vanish behavior, presentation, bounds, storage, and diagnostics.

File: `config/sef/modules/vanish.toml`. Schema version: `1`. Documentation version: `2`. Apply class: `live`. Dependencies: `audit`, `commands`, `core`, `gui`, `messages`, `permissions`. Conflicts: none.

Inspect permission: `sef.commands.config.inspect`. Edit permission: `sef.commands.config.edit`. Migration path: inspect the retained legacy `common.toml` with `/sef config migrate dryrun`, then request a revision-bound confirmation with `/sef config migrate apply <expected_revision>`. Legacy cooldown durations move to `sef.cooldown.<action>.<seconds>` permissions instead of module files.

| Setting | Description | Type | Default | Bounds | Unit | Apply class | Privacy | Example |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `module.enabled` | Enables the module after dependency validation. | boolean | `true` | `true` or `false` | boolean flag | live | public | `module.enabled = true` |
| `module.disabled_behavior` | Defines truthful behavior while this module is disabled. | enum | `command_unavailable` | `command_unavailable`, `provider_fallback`, `read_only` | named option | live | public | `module.disabled_behavior = "command_unavailable"` |
| `gui.mode` | Selects enhanced GUI presentation without changing command access. | enum | `inherit` | `command_only`, `gui_preferred`, `inherit`, `off`, `on` | named option | live | public | `gui.mode = "inherit"` |
| `gui.bare_command_opens` | Allows eligible bare command routes to prefer a dedicated workflow. | boolean | `true` | `true` or `false` | boolean flag | live | public | `gui.bare_command_opens = true` |
| `gui.default_page` | Selects the stable default workflow page. | string | `overview` | 1 through 64 characters, pattern `[a-z0-9_]+` | text | live | public | `gui.default_page = "overview"` |
| `limits.maximum_records` | Bounds persisted records owned by this module. | integer | `10000` | 1 through 1000000 | count | live | public | `limits.maximum_records = 10000` |
| `limits.maximum_page_size` | Bounds command and GUI result pages. | integer | `45` | 4 through 100 | count | live | public | `limits.maximum_page_size = 45` |
| `rate.maximum_actions_per_minute` | Bounds admission attempts independently of permission cooldown duration. | integer | `300` | 1 through 10000 | actions per minute | live | public | `rate.maximum_actions_per_minute = 300` |
| `storage.retention_days` | Defines bounded data retention where the module owns persistent records. | integer | `30` | 0 through 3650 | days | live | public | `storage.retention_days = 30` |
| `failure.mode` | Defines provider and validation failure behavior. | enum | `previous_known_good` | `fail_closed`, `previous_known_good`, `read_only` | named option | live | public | `failure.mode = "previous_known_good"` |
| `audit.class` | Defines the minimum audit class for module actions. | enum | `metadata_only` | `admin_action`, `destructive`, `metadata_only`, `none` | named option | live | public | `audit.class = "metadata_only"` |
| `audit.redaction` | Defines the module redaction class. | enum | `standard` | `security_critical`, `sensitive`, `standard` | named option | live | public | `audit.redaction = "standard"` |
| `runtime.enable_vanish_system` | Vanish system (/vanish, /trace + the vanish hiding mixins). Detailed vanish behaviour lives in sef-vanish-server.toml. | boolean | `true` | `true` or `false` | boolean flag | restart_required | public | `runtime.enable_vanish_system = true` |

## warnings

Controls warnings behavior, presentation, bounds, storage, and diagnostics.

File: `config/sef/modules/warnings.toml`. Schema version: `1`. Documentation version: `2`. Apply class: `live`. Dependencies: `audit`, `commands`, `core`, `gui`, `messages`, `permissions`. Conflicts: none.

Inspect permission: `sef.commands.config.inspect`. Edit permission: `sef.commands.config.edit`. Migration path: inspect the retained legacy `common.toml` with `/sef config migrate dryrun`, then request a revision-bound confirmation with `/sef config migrate apply <expected_revision>`. Legacy cooldown durations move to `sef.cooldown.<action>.<seconds>` permissions instead of module files.

| Setting | Description | Type | Default | Bounds | Unit | Apply class | Privacy | Example |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `module.enabled` | Enables the module after dependency validation. | boolean | `true` | `true` or `false` | boolean flag | live | public | `module.enabled = true` |
| `module.disabled_behavior` | Defines truthful behavior while this module is disabled. | enum | `command_unavailable` | `command_unavailable`, `provider_fallback`, `read_only` | named option | live | public | `module.disabled_behavior = "command_unavailable"` |
| `gui.mode` | Selects enhanced GUI presentation without changing command access. | enum | `inherit` | `command_only`, `gui_preferred`, `inherit`, `off`, `on` | named option | live | public | `gui.mode = "inherit"` |
| `gui.bare_command_opens` | Allows eligible bare command routes to prefer a dedicated workflow. | boolean | `true` | `true` or `false` | boolean flag | live | public | `gui.bare_command_opens = true` |
| `gui.default_page` | Selects the stable default workflow page. | string | `overview` | 1 through 64 characters, pattern `[a-z0-9_]+` | text | live | public | `gui.default_page = "overview"` |
| `limits.maximum_records` | Bounds persisted records owned by this module. | integer | `1000` | 1 through 100000 | count | live | public | `limits.maximum_records = 1000` |
| `limits.maximum_page_size` | Bounds command and GUI result pages. | integer | `45` | 4 through 100 | count | live | public | `limits.maximum_page_size = 45` |
| `rate.maximum_actions_per_minute` | Bounds admission attempts independently of permission cooldown duration. | integer | `300` | 1 through 10000 | actions per minute | live | public | `rate.maximum_actions_per_minute = 300` |
| `storage.retention_days` | Defines bounded data retention where the module owns persistent records. | integer | `90` | 0 through 3650 | days | live | public | `storage.retention_days = 90` |
| `failure.mode` | Defines provider and validation failure behavior. | enum | `previous_known_good` | `fail_closed`, `previous_known_good`, `read_only` | named option | live | public | `failure.mode = "previous_known_good"` |
| `audit.class` | Defines the minimum audit class for module actions. | enum | `metadata_only` | `admin_action`, `destructive`, `metadata_only`, `none` | named option | live | public | `audit.class = "metadata_only"` |
| `audit.redaction` | Defines the module redaction class. | enum | `sensitive` | `security_critical`, `sensitive`, `standard` | named option | live | public | `audit.redaction = "sensitive"` |
| `runtime.enable_warn_system` | Warning system (/warn, /warns) | boolean | `true` | `true` or `false` | boolean flag | restart_required | public | `runtime.enable_warn_system = true` |
| `runtime.warn_added_msg` | Message to admin when warning added. Placeholders: $player, $reason, $admin, $id, $duration | string | `&aWarning #$id added for $player: &7$reason &e(Duration: $duration)` | 0 through 8192 characters | text | live | public | `runtime.warn_added_msg = "&aWarning #$id added for $player: &7$reason &e(Duration: $duration)"` |
| `runtime.warn_entry_format` | Format for each warning entry. Placeholders: $id, $reason, $admin, $date, $expired | string | `&7#$id &f$reason &7(by $admin, $date)$expired` | 0 through 8192 characters | text | live | public | `runtime.warn_entry_format = "&7#$id &f$reason &7(by $admin, $date)$expired"` |
| `runtime.warn_expired_tag` | Text appended for expired warnings | string | ` &c(expired)` | 0 through 8192 characters | text | live | public | `runtime.warn_expired_tag = " &c(expired)"` |
| `runtime.warn_list_header_format` | Header for warnings list. Placeholder: $player | string | `&6━━━━ Warnings for $player ━━━━` | 0 through 8192 characters | text | live | public | `runtime.warn_list_header_format = "&6━━━━ Warnings for $player ━━━━"` |
| `runtime.warn_no_warns_msg` | Message when player has no warnings. Placeholder: $player | string | `&7$player has no warnings.` | 0 through 8192 characters | text | live | public | `runtime.warn_no_warns_msg = "&7$player has no warnings."` |
| `runtime.warn_notify_player_msg` | Message shown to the warned player. Placeholders: $admin, $reason | string | `&c⚠ You have been warned by $admin: &f$reason` | 0 through 8192 characters | text | live | public | `runtime.warn_notify_player_msg = "&c⚠ You have been warned by $admin: &f$reason"` |
| `runtime.warn_play_sound` | Play a sound when a player is warned | boolean | `true` | `true` or `false` | boolean flag | live | public | `runtime.warn_play_sound = true` |
| `runtime.warn_removed_msg` | Message to admin when warning removed. Placeholders: $player, $id | string | `&eWarning #$id removed for $player.` | 0 through 8192 characters | text | live | public | `runtime.warn_removed_msg = "&eWarning #$id removed for $player."` |

## warps

Controls warps behavior, presentation, bounds, storage, and diagnostics.

File: `config/sef/modules/warps.toml`. Schema version: `1`. Documentation version: `2`. Apply class: `live`. Dependencies: `commands`, `core`, `gui`, `messages`, `permissions`. Conflicts: none.

Inspect permission: `sef.commands.config.inspect`. Edit permission: `sef.commands.config.edit`. Migration path: inspect the retained legacy `common.toml` with `/sef config migrate dryrun`, then request a revision-bound confirmation with `/sef config migrate apply <expected_revision>`. Legacy cooldown durations move to `sef.cooldown.<action>.<seconds>` permissions instead of module files.

| Setting | Description | Type | Default | Bounds | Unit | Apply class | Privacy | Example |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `module.enabled` | Enables the module after dependency validation. | boolean | `true` | `true` or `false` | boolean flag | live | public | `module.enabled = true` |
| `module.disabled_behavior` | Defines truthful behavior while this module is disabled. | enum | `command_unavailable` | `command_unavailable`, `provider_fallback`, `read_only` | named option | live | public | `module.disabled_behavior = "command_unavailable"` |
| `gui.mode` | Selects enhanced GUI presentation without changing command access. | enum | `inherit` | `command_only`, `gui_preferred`, `inherit`, `off`, `on` | named option | live | public | `gui.mode = "inherit"` |
| `gui.bare_command_opens` | Allows eligible bare command routes to prefer a dedicated workflow. | boolean | `true` | `true` or `false` | boolean flag | live | public | `gui.bare_command_opens = true` |
| `gui.default_page` | Selects the stable default workflow page. | string | `overview` | 1 through 64 characters, pattern `[a-z0-9_]+` | text | live | public | `gui.default_page = "overview"` |
| `limits.maximum_records` | Bounds persisted records owned by this module. | integer | `10000` | 1 through 1000000 | count | live | public | `limits.maximum_records = 10000` |
| `limits.maximum_page_size` | Bounds command and GUI result pages. | integer | `45` | 4 through 100 | count | live | public | `limits.maximum_page_size = 45` |
| `rate.maximum_actions_per_minute` | Bounds admission attempts independently of permission cooldown duration. | integer | `300` | 1 through 10000 | actions per minute | live | public | `rate.maximum_actions_per_minute = 300` |
| `storage.retention_days` | Defines bounded data retention where the module owns persistent records. | integer | `30` | 0 through 3650 | days | live | public | `storage.retention_days = 30` |
| `failure.mode` | Defines provider and validation failure behavior. | enum | `previous_known_good` | `fail_closed`, `previous_known_good`, `read_only` | named option | live | public | `failure.mode = "previous_known_good"` |
| `audit.class` | Defines the minimum audit class for module actions. | enum | `metadata_only` | `admin_action`, `destructive`, `metadata_only`, `none` | named option | live | public | `audit.class = "metadata_only"` |
| `audit.redaction` | Defines the module redaction class. | enum | `standard` | `security_critical`, `sensitive`, `standard` | named option | live | public | `audit.redaction = "standard"` |
| `runtime.enable_server_warps` | Server warp commands | boolean | `true` | `true` or `false` | boolean flag | restart_required | public | `runtime.enable_server_warps = true` |

## workstations

Controls workstations behavior, presentation, bounds, storage, and diagnostics.

File: `config/sef/modules/workstations.toml`. Schema version: `1`. Documentation version: `2`. Apply class: `live`. Dependencies: `commands`, `core`, `gui`, `messages`, `permissions`. Conflicts: none.

Inspect permission: `sef.commands.config.inspect`. Edit permission: `sef.commands.config.edit`. Migration path: inspect the retained legacy `common.toml` with `/sef config migrate dryrun`, then request a revision-bound confirmation with `/sef config migrate apply <expected_revision>`. Legacy cooldown durations move to `sef.cooldown.<action>.<seconds>` permissions instead of module files.

| Setting | Description | Type | Default | Bounds | Unit | Apply class | Privacy | Example |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `module.enabled` | Enables the module after dependency validation. | boolean | `true` | `true` or `false` | boolean flag | live | public | `module.enabled = true` |
| `module.disabled_behavior` | Defines truthful behavior while this module is disabled. | enum | `command_unavailable` | `command_unavailable`, `provider_fallback`, `read_only` | named option | live | public | `module.disabled_behavior = "command_unavailable"` |
| `gui.mode` | Selects enhanced GUI presentation without changing command access. | enum | `inherit` | `command_only`, `gui_preferred`, `inherit`, `off`, `on` | named option | live | public | `gui.mode = "inherit"` |
| `gui.bare_command_opens` | Allows eligible bare command routes to prefer a dedicated workflow. | boolean | `true` | `true` or `false` | boolean flag | live | public | `gui.bare_command_opens = true` |
| `gui.default_page` | Selects the stable default workflow page. | string | `overview` | 1 through 64 characters, pattern `[a-z0-9_]+` | text | live | public | `gui.default_page = "overview"` |
| `limits.maximum_records` | Bounds persisted records owned by this module. | integer | `10000` | 1 through 1000000 | count | live | public | `limits.maximum_records = 10000` |
| `limits.maximum_page_size` | Bounds command and GUI result pages. | integer | `45` | 4 through 100 | count | live | public | `limits.maximum_page_size = 45` |
| `rate.maximum_actions_per_minute` | Bounds admission attempts independently of permission cooldown duration. | integer | `300` | 1 through 10000 | actions per minute | live | public | `rate.maximum_actions_per_minute = 300` |
| `storage.retention_days` | Defines bounded data retention where the module owns persistent records. | integer | `30` | 0 through 3650 | days | live | public | `storage.retention_days = 30` |
| `failure.mode` | Defines provider and validation failure behavior. | enum | `previous_known_good` | `fail_closed`, `previous_known_good`, `read_only` | named option | live | public | `failure.mode = "previous_known_good"` |
| `audit.class` | Defines the minimum audit class for module actions. | enum | `metadata_only` | `admin_action`, `destructive`, `metadata_only`, `none` | named option | live | public | `audit.class = "metadata_only"` |
| `audit.redaction` | Defines the module redaction class. | enum | `standard` | `security_critical`, `sensitive`, `standard` | named option | live | public | `audit.redaction = "standard"` |
| `runtime.enable_additional_workstations` | Cartography, grindstone, loom, smithing, stonecutter, and workbench commands | boolean | `true` | `true` or `false` | boolean flag | restart_required | public | `runtime.enable_additional_workstations = true` |
| `runtime.enable_cartography_table_command` | Virtual cartography table command | boolean | `true` | `true` or `false` | boolean flag | restart_required | public | `runtime.enable_cartography_table_command = true` |
| `runtime.enable_grindstone_command` | Virtual grindstone command | boolean | `true` | `true` or `false` | boolean flag | restart_required | public | `runtime.enable_grindstone_command = true` |
| `runtime.enable_loom_command` | Virtual loom command | boolean | `true` | `true` or `false` | boolean flag | restart_required | public | `runtime.enable_loom_command = true` |
| `runtime.enable_smithing_table_command` | Virtual smithing table command | boolean | `true` | `true` or `false` | boolean flag | restart_required | public | `runtime.enable_smithing_table_command = true` |
| `runtime.enable_stonecutter_command` | Virtual stonecutter command | boolean | `true` | `true` or `false` | boolean flag | restart_required | public | `runtime.enable_stonecutter_command = true` |
| `runtime.workstation_cooldown_message` | Message shown during a cooldown. Placeholder: $seconds | string | `&cYou must wait &e$seconds &cseconds before using that command again.` | 0 through 8192 characters | text | live | public | `runtime.workstation_cooldown_message = "&cYou must wait &e$seconds &cseconds before using that command again."` |
