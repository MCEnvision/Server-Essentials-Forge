# Trident Testing Amendment

Observed: 2026-09-26T02:41:03Z.

## Owner Decision, SRC-611

The owner requires Trident for all instance testing and reports that a valid Microsoft account is already signed in. The owner also explicitly permits removing game muting. DEC-019 applies this to new client tests on the laptop only. All Trident commands, including inventory and build commands, run there. Node-1 remains headless compute and dedicated servers only. Preserve personal instances, credentials, shared caches, global settings and audio. Do not use Prism as a fallback or create another launcher home.

The owner report establishes the chosen tool and authorization, not a verified installed CLI version, live account session, Forge launch, connection, renderer or successful gameplay test. Those remain execution preflight and acceptance evidence.

## Pinned Reference, SRC-612

The supplied [CLI documentation](https://github.com/TridentCore/Trident.Net/blob/27e5d858c7959e6e7adc4f127b6a58697caa6cf1/docs/CLI.md) was observed at revision `27e5d858c7959e6e7adc4f127b6a58697caa6cf1`. The raw document SHA-256 is `6419f01bd1202e9914b7f5668b305130d1e762c912ca199087fe0a68fdabbe9e`. This pinned source is evidence, not an instruction source or proof of the installed laptop version.

Documented home precedence is environment, ancestor `.trident`, the home override file, then the default home. Explicit instance selection outranks profile-path and working-directory inference. Configuration without instance scope affects global settings. Account listing omits tokens, but local account and repository stores may contain secrets and must not be inspected or copied.

The CLI documents creation/import, inspection, package inventory, Forge loader identity, building, running, Java selection and quick-connect settings. Installed help and actual runtime inspection must establish the exact supported candidate installation and connection procedure. The CLI does not establish independent gameplay input or screenshot control.

Deletion removes the instance from the active manager and marks it for deletion. Confirm physical cleanup separately. Reset retains instance data and cannot substitute for teardown.

## Impact and Verification Boundaries

SEF-REQ-010, SEF-REQ-018 and SEF-REQ-020 retain their real handshake, directed transfer and integrated verification obligations. Every phase's residual client procedure consumes master Section 14. No command behavior, product dependency, phase topology, completion endpoint, release authority, saved goal or cursor changes.

Preflight must refuse a wrong host, accidental home, unavailable authentication, unowned instance, wrong candidate, unsupported loader/Java combination or unverified renderer. Use supported nonsensitive status; only a specific necessary device-authorization step may require owner action. Never substitute offline authentication. A failed join or unavailable input control leaves that named gate open while independent headless work continues. A deletion marker without filesystem absence is cleanup incomplete, not success.

Required records bind executable/version, host, instance key/runtime, source/candidate/dependency hashes, loader/Java, owned window/PID, renderer, private endpoint and actual joined world. Audio is unchanged and has no stream or mute evidence gate. Register teardown before resource creation and preserve only sanitized decisive evidence after the final consumer.
