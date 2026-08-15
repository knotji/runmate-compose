# Architecture

## Goal

RunMate Compose is an isolated Android-first client experiment. It may read production-compatible data, but it must not silently become a second recovery engine or introduce production persistence.

## Product axis: health first

The application is organized around understanding personal health over time. Running is an activity signal inside that system, not the root navigation or the default explanation for every metric.

- Today summarizes measured health availability and freshness.
- Health owns sleep, cardiovascular, respiratory, recovery-source, and trend capabilities.
- Activities may include running, walking, cycling, strength, and other Health Connect exercise types.
- A future recommendation must be derived from approved health and activity contracts; the UI must not assume the user is a runner.
- Navigation and naming should remain useful on a day with no run or workout.

## Greenfield implementation boundary

`runmate-compose` is not a source-code port of `runmate-mobile`. Capabilities are designed and implemented incrementally inside this repository.

- Do not copy screens, components, stores, CSS, TypeScript business logic, or project structure from `runmate-mobile`.
- Use `runmate-mobile` only to understand verified user behavior, existing data contracts, compatibility constraints, and baseline results.
- Reuse an asset only when it is an approved shared brand asset, such as the RunMate launcher logo, and record its origin.
- Integrate through explicit versioned contracts rather than translating implementation details between languages.
- A capability enters Compose only when its own scope, states, tests, and device acceptance criteria are defined.
- Do not pursue screen-for-screen parity unless a later product decision explicitly requires it.

## Layers

```text
Compose UI
  -> screen events
App store / screen ViewModels
  -> use-case and refresh coordination
Repositories / providers
  -> Health Connect, future Supabase auth/data, RecoverySnapshot provider
Android platform
```

Dependencies point inward. SDK types stop at repository boundaries.

## Current modules

- `core/state`: reusable load-state semantics.
- `core/performance`: payload-free timing instrumentation.
- `state`: app/session navigation state.
- `today`: SavedState-backed daily focus and optional user-reported check-in state.
- `health`: typed Sleep, Heart Rate, HRV, Respiratory Rate, and generic Activity models plus direct read-only Health Connect mapping. Display strings are produced outside the repository.
- `recovery`: versioned cross-client contract; the provider is intentionally unconfigured.
- `ui`: Today decision screen and Health diagnostics.

## Security and privacy

- `google-services.json`, `local.properties`, tokens, signing files, and environment files remain untracked.
- Health data stays on device in the current Lab.
- No health values are logged.
- Future Supabase access uses the public anon key plus authenticated Row Level Security; no service-role key belongs in the app.
- Authentication tokens must use encrypted platform storage, not plain preferences.

## Supabase/auth next slice

Auth is intentionally not wired in this foundation commit because no shared RecoverySnapshot source exists yet and production credentials must not be copied from another checkout. The implementation sequence is:

1. provide local public URL/anon-key configuration through ignored developer properties;
2. add an auth repository and encrypted session storage;
3. implement login/restore/logout states in `RunMateAppStore`;
4. read only existing authorized data;
5. keep RecoverySnapshot unavailable until an approved provider exists.

## Git and release discipline

- `main` is releasable Lab infrastructure; work branches use `agent/<description>`.
- Every functional change updates relevant contracts and tests.
- Firebase builds are experiments, not production RunMate releases.
- `nativeHealthDashboard` remains false by default.
- Release notes must identify the real providers used and explicitly call out any unavailable product output.
