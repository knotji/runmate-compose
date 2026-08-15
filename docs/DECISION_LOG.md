# Decision Log

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
