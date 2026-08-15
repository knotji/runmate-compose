# Decision Log

## 2026-08-15 - WholeMate working product name

Use **WholeMate** as the working user-facing name for the health-first experiment. It represents whole-body and mind support while retaining the companion meaning of “Mate.” Keep the existing package, Firebase app, repository, and internal identifiers until formal brand clearance and a planned identity migration.

## 2026-08-15 - Health-first product axis

Compose is designed as a personal health application rather than a running application with health cards. Running remains an activity type. Today, navigation, data ownership, and future recommendations must make sense across sleep, cardiovascular, respiratory, recovery, and multiple activity types.

## 2026-08-15 - Greenfield capability slices

Do not copy or mechanically port `runmate-mobile` into this repository. Build one independently testable Compose capability at a time. The existing application may supply behavioral baselines and data-contract compatibility requirements, but not implementation structure or business-logic duplication. Approved shared brand assets are the only default exception.

## 2026-08-15 - Android-only Compose Lab

Use Jetpack Compose rather than Flutter for the first experiment because RunMate is currently Android/Samsung/Health Connect heavy and already owns Kotlin native integrations. This does not decide the future iOS strategy.

## 2026-08-15 - Health Connect remains the primary health provider

Use AndroidX Health Connect directly and read records only. Samsung Health Data SDK may be evaluated as an optional Samsung-specific provider, but it does not replace Health Connect and does not expose public HRV or respiratory-rate types needed by this experiment.

## 2026-08-15 - No duplicate recovery engine

Production recovery is TypeScript client logic. Compose defines a versioned `RecoverySnapshot` consumer contract but keeps its provider unconfigured until a shared source is explicitly approved.

## 2026-08-15 - No mock health or decision output

Today renders only records returned by Health Connect. When data is absent, it shows an explicit empty, permission, unavailable, loading, or error state. Recovery, Strain, Energy, training guidance, and fallback chart values remain absent until an approved real provider exists.
