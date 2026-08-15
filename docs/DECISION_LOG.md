# Decision Log

Entries are append-only decisions. A superseding entry wins; older entries remain historical context.

## 2026-08-15 - WholeMate product constitution

WholeMate helps a person understand how they are today, what is shaping that state, and what they can do next. Health data is evidence, not the product. Every primary experience must answer at least one of those questions and participate in the daily loop `Observe -> Understand -> Decide -> Act -> Reflect -> Learn`.

## 2026-08-15 - Compose Multiplatform is the canonical architecture

**Supersedes: Android-only Compose Lab.** Share domain logic, data contracts, use cases, state, presentation models, and design-system semantics in Kotlin Multiplatform where practical. Keep Health Connect, Android secure storage, Firebase Android services, and lifecycle implementation on Android. HealthKit, Keychain, and iOS services belong to a future iOS host when enabled.

The current repository is still physically an Android application module; CMP source-set migration is incremental and must be reported honestly.

## 2026-08-15 - Body Picture is adaptive

The familiar three-ring hero remains an approved initial presentation, currently `Recovery / Strain / Sleep`. Those labels are not permanent WholeMate ontology. The Body Picture selects three or four useful signals from available evidence and the person's goals through typed presentation models.

## 2026-08-15 - AI is optional to comprehension

Deterministic facts, provenance, freshness, baselines, and missingness must remain useful without Gemini. AI may connect the dots and support decisions, but it never supplies measurements or makes the base product comprehensible.

## 2026-08-15 - RunMate compatibility is transitional

`RunMateRecoverySystem` is the current production source and `RecoverySnapshot` is its compatibility read model. Do not duplicate the engine while cross-client compatibility matters. A future WholeMate recovery model may supersede it through an explicit, versioned, evaluated decision.

## 2026-08-15 - RunMate UX baseline with health-first hierarchy

Use `runmate-mobile` as an approved visual and interaction baseline. Reimplement behavior rather than copying Ionic/CSS/state code. Today, Health, Move, and Coach retain their navigation, with Coach defined as the interpretation/action layer rather than only chat.

## 2026-08-15 - WholeMate working product name

Use **WholeMate** as the user-facing working name. Technical identifiers migrate only through coordinated plans. The registered OAuth callback `com.wholemate.app://auth/callback` is the current integration source of truth.

## 2026-08-15 - Health Connect remains the Android health provider

Use AndroidX Health Connect directly and read records only. Samsung Health Data SDK may be evaluated as an optional Samsung-specific provider but does not replace Health Connect by default.

## 2026-08-15 - No mock health or decision output

Missing stays missing. Never fabricate Recovery, Strain, Energy, training guidance, chart values, HRV, respiratory rate, or other measurements to complete UI.

## Superseded historical decision

### 2026-08-15 - Android-only Compose Lab

Originally selected Jetpack Compose for a Samsung/Health Connect experiment. Superseded by the Compose Multiplatform architecture decision above; retained only to explain the repository's current Android-shaped implementation.
