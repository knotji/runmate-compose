# WholeMate Roadmap

## Purpose

This roadmap turns the Compose Lab into a sequence of reversible product experiments. Completion means the acceptance criteria are demonstrated on the target Samsung device; a successful build alone is not enough.

The product direction is health-first. Running remains supported as one activity type, while the system, navigation, and Today experience are built around broader health signals.

The working product concept and AI/mental-health boundaries are defined in `PRODUCT_CONCEPT.md`.

## Product principles

- Show measured health records only. Missing data stays missing.
- Do not duplicate the production recovery engine in Kotlin.
- Keep Health Connect access read-only until a separate write use case is approved.
- Make every milestone independently releasable and removable.
- Update the relevant contract and tests with every behavior change.
- Build each capability as a greenfield Compose implementation; do not copy or mechanically port `runmate-mobile` source.

## Current baseline

- [x] Isolated Jetpack Compose Android application.
- [x] Health Connect permission and read flow.
- [x] Sleep, heart rate, HRV RMSSD, respiratory rate, and workout reads.
- [x] Measured seven-day Sleep and HR charts without fallback values.
- [x] Explicit loading, permission, unavailable, empty-value, and error presentation.
- [x] Mock Recovery, Strain, Energy, training guidance, insights, and charts removed from Today.
- [x] Firebase App Distribution flow and Samsung tester established.
- [ ] Physical-device acceptance pass recorded for the real-data Today screen.
- [x] Sleep and Heart Rate use typed domain models; display formatting is outside the repository.

## Milestone 1 - Typed health domain

Goal: stop treating health measurements as display strings.

- [x] Introduce typed Sleep and Heart Rate models.
- [ ] Introduce typed HRV, Respiratory Rate, and Workout models.
- Preserve record identifiers, source metadata, start/end time, zone, and last-synced time where available.
- Format values only in the UI mapper.
- Represent a successful query with no record separately from a failed query.
- Keep valid content visible during refresh using `LoadState.Loading(previous)`.
- Prevent an older refresh from replacing a newer result.

Acceptance:

- Unit tests cover mapping, missing records, units, timezone boundaries, and stale refresh races.
- Today renders the same measured values as the Health diagnostic screen.
- Pull-to-refresh does not blank previously loaded content.
- No Health Connect SDK record type escapes the health repository layer.

## Milestone 2 - Real Today signals

Goal: make Today useful without pretending that a recovery decision exists.

- Add per-signal freshness and origin labels.
- Add honest partial-data presentation; one missing signal must not hide the others.
- Add Sleep, HR, HRV, respiratory, and workout detail views.
- Improve seven-day chart accessibility, selection, and empty-day behavior.
- Add a visible last-sync timestamp and manual refresh result.

Acceptance:

- Every displayed number traces to a Health Connect record.
- No score, recommendation, trend, or explanatory claim is generated without an approved rule and test.
- TalkBack describes chart metric, selected date, value, unit, and missing days.
- Samsung device QA covers fresh, partial, empty, revoked-permission, offline, and error states.

## Milestone 3 - Shared RecoverySnapshot

Goal: display the same recovery result in Ionic and Compose.

Dependencies:

- Approve a single recovery source of truth outside the Compose UI.
- Define owner, versioning, freshness, and failure behavior.

Work:

- Produce the versioned `RecoverySnapshot` from the approved shared provider.
- Validate date, model version, ranges, signal sources, and reasons before display.
- Add cache rules that never relabel stale output as current.
- Compare identical fixtures and real accounts across Ionic and Compose.

Acceptance:

- Ionic and Compose show identical score, state, reasons, and effective date.
- Invalid or stale snapshots are rejected or visibly marked stale.
- No recovery formula exists in the Compose repository.

## Milestone 4 - Account and shared history

Goal: connect the Lab to existing user-owned data without changing production data semantics.

- Add local public Supabase configuration; keep secrets untracked.
- Implement login, encrypted session persistence, restore, logout, and expired-session states.
- Read existing authorized profile and history through repositories.
- Verify Row Level Security with two isolated test accounts.
- Do not add migrations or production writes as part of the read-only milestone.

Acceptance:

- Restart restores the correct account without exposing tokens.
- Logout clears local session and user-scoped cache.
- Cross-account reads fail in integration testing.

## Milestone 5 - Performance and resilience

Goal: prove that the native experience remains fast under realistic data and lifecycle conditions.

- Add Macrobenchmark for cold start, Today open, refresh, chart interaction, and Today/Health navigation.
- Add a Baseline Profile after flows stabilize.
- Measure recomposition counts and remove avoidable work from hot UI paths.
- Test process death, rotation, background/foreground, permission revocation, and large record sets.
- Keep instrumentation payload-free; health values must never enter logs.

Acceptance:

- Meet the budgets in `PERFORMANCE.md` on the target Samsung device.
- No duplicate full refresh on recomposition or tab return.
- No visible blanking during background refresh.

## Milestone 6 - Product decision

Goal: decide the role of Compose using evidence rather than rewrite momentum.

Run the same flows on the same Samsung device in Ionic and Compose:

- cold open;
- permission grant and revoke;
- first measured data appearance;
- refresh while data is visible;
- seven-day chart interaction;
- Today/Health navigation;
- empty and error recovery.

Decision rules:

- Better user experience and simpler health/state code, but insufficient product gain: keep Compose as a native island.
- Clear user, developer, and product advantage: evaluate Compose as the long-term Android client.
- Visual improvement only: continue polishing Ionic and keep the Lab isolated.

## Not scheduled

- Rewriting the recovery formula in Kotlin.
- Samsung Health Data SDK as a replacement for Health Connect.
- Compose Multiplatform or an iOS client.
- Production database migrations or health-data writes.
- Full migration away from `runmate-mobile`.
- Screen-for-screen or source-for-source parity with `runmate-mobile`.

## Next executable slice

Milestone 1 begins with typed health models and UI mapping. Keep this slice limited to Sleep and Heart Rate first, verify it, then extend the same contract to HRV, respiratory rate, and workout.
