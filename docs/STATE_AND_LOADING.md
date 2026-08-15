# State and Loading Contract

## Ownership

State has one owner at each lifetime:

| Lifetime | Owner | Examples |
|---|---|---|
| Process/session | `RunMateAppStore` | selected destination, authenticated account when added |
| Screen | screen ViewModel | Health query state and recovery snapshot state |
| Transient UI | `rememberSaveable` | selected chart metric and point |
| Durable server | Supabase through a repository | profile and account-backed product data |
| Device health | Health Connect through a repository | sleep, heart rate, workout records |

Composable functions render state and send events. They must not query Health Connect, Supabase, disk, or network directly.

## Canonical load states

Use `LoadState<T>`:

- `Idle`: work has not started.
- `Loading(previous)`: refresh in progress. Keep valid previous content visible.
- `Ready(value, receivedAt)`: current usable content.
- `Empty(reason)`: successful query with no usable data.
- `Failed(message, previous, retryable)`: failure with optional stale content.

Loading and empty are not interchangeable. A spinner must never replace known-good content during background refresh.

## App boot

The eventual production boot sequence is:

1. Render branded shell immediately.
2. Restore encrypted auth session.
3. Render cached, date-valid RecoverySnapshot if present.
4. Start account and Health Connect refresh concurrently.
5. Replace each section independently as its source resolves.
6. Report fully drawn after the first actionable Today state, not after all secondary data.

Boot failure must expose retry/logout as applicable. Never show an indefinite logo animation.

## Refresh and races

- Only the ViewModel/store starts refresh work.
- A new foreground refresh supersedes an older result.
- The ViewModel cancels its previous refresh job before starting a new one, preventing an older query from replacing newer state.
- Pull-to-refresh keeps previous content and exposes a progress indicator.
- Today displays the last successful sync time and the originating application for each available signal.
- Health permission can be granted from Today; the user is not forced through a diagnostic screen.
- Permission changes trigger a fresh permission check before a query.
- Screen re-entry must not create duplicate collectors or concurrent full refreshes.
- Navigation and transient selection survive configuration changes through `SavedStateHandle` or `rememberSaveable`.

## Recovery snapshot states

- `NotConfigured`: no cross-client provider exists; show Unavailable, never calculate or display a substitute.
- `Loading`: provider request in progress.
- `Available`: version, date, ranges, and model identity passed validation.
- `Rejected`: payload exists but violates the contract; do not display its scores.
- `Failed`: provider could not be reached; stale data may be shown only when explicitly marked stale.
