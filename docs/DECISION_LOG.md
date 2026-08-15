# Decision Log

## 2026-08-15 - Android-only Compose Lab

Use Jetpack Compose rather than Flutter for the first experiment because RunMate is currently Android/Samsung/Health Connect heavy and already owns Kotlin native integrations. This does not decide the future iOS strategy.

## 2026-08-15 - Health Connect remains the primary health provider

Use AndroidX Health Connect directly and read records only. Samsung Health Data SDK may be evaluated as an optional Samsung-specific provider, but it does not replace Health Connect and does not expose public HRV or respiratory-rate types needed by this experiment.

## 2026-08-15 - No duplicate recovery engine

Production recovery is TypeScript client logic. Compose defines a versioned `RecoverySnapshot` consumer contract but keeps its provider unconfigured until a shared source is explicitly approved.

## 2026-08-15 - Honest preview boundary

Today UI may mock Recovery, Strain, Energy, and training guidance only with visible Preview/Derived labels. Sleep, heart rate, and workout values are Measured only when returned by Health Connect.
