# State and Loading Contract

## Ownership

State has one owner at each lifetime:

| Lifetime | Owner | Examples |
|---|---|---|
| App/session | shared app-state owner | selected destination and authenticated account identity |
| Screen | shared or platform screen-state owner | query and recovery snapshot state |
| Transient UI | platform saveable-state adapter | selected chart metric and point |
| Durable server | Supabase through a repository | profile and account-backed product data |
| Device health | platform provider through a repository | Health Connect on Android; future HealthKit on iOS |

UI functions render state and send events. They must not query platform health APIs, Supabase, disk, or network directly.

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
4. Start account and platform-health refresh concurrently.
5. Replace each section independently as its source resolves.
6. Report fully drawn after the first actionable Today state, not after all secondary data.

Boot failure must expose retry/logout as applicable. Never show an indefinite logo animation.

## Refresh and races

- Only the designated state owner starts refresh work.
- A new foreground refresh supersedes an older result.
- The state owner cancels or supersedes its previous refresh before starting a new one, preventing an older query from replacing newer state.
- Pull-to-refresh keeps previous content and exposes a progress indicator.
- Today displays the last successful sync time and the originating application for each available signal.
- Health permission can be granted from Today; the user is not forced through a diagnostic screen.
- Permission changes trigger a fresh permission check before a query.
- Screen re-entry must not create duplicate collectors or concurrent full refreshes.
- Navigation and transient selection survive supported platform lifecycle restoration where appropriate.

Android currently implements lifecycle restoration with `SavedStateHandle` and `rememberSaveable`. These are Android implementation details, not shared contracts; other hosts use their platform adapters.

## Recovery snapshot states

- `NotConfigured`: no cross-client provider exists; show Unavailable, never calculate or display a substitute.
- `Loading`: provider request in progress.
- `Available`: version, date, ranges, and model identity passed validation.
- `Rejected`: payload exists but violates the contract; do not display its scores.
- `Failed`: provider could not be reached; stale data may be shown only when explicitly marked stale.
