# Architecture

## Goal

RunMate Compose is an isolated Android-first client experiment. It may read production-compatible data, but it must not silently become a second recovery engine or introduce production persistence.

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
- `health`: direct read-only Health Connect integration and UI model mapping.
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
