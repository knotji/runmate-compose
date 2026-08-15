# Decision Log

Entries are append-only decisions. A superseding entry wins; older entries remain historical context.

## 2026-08-15 - IA v1 uses Today, Health, Move, and You

**Supersedes the Coach top-level destination.** Today owns the whole daily story and its evidence-backed next step. Health owns body evidence over time. Move owns movement/activity. You owns goals, explicit context, reflection, privacy, and account control. Coaching remains contextual product behavior; a future conversation may open from an insight or goal but does not receive a primary tab by default. Android and Web now use the four IA v1 destination labels.

## 2026-08-15 - WholeMate product constitution

WholeMate helps a person understand how they are today, what is shaping that state, and what they can do next. Health data is evidence, not the product. Every primary experience must answer at least one of those questions and participate in the daily loop `Observe -> Understand -> Decide -> Act -> Reflect -> Learn`.

## 2026-08-15 - Compose Multiplatform is the canonical architecture

**Supersedes: Android-only Compose Lab.** Share domain logic, data contracts, use cases, state, presentation models, and design-system semantics in Kotlin Multiplatform where practical. Keep Health Connect, Android secure storage, Firebase Android services, and lifecycle implementation on Android. HealthKit, Keychain, and iOS services belong to a future iOS host when enabled.

The current repository is still physically an Android application module; CMP source-set migration is incremental and must be reported honestly.

## 2026-08-15 - Body Picture keeps three stable v1 rings

The familiar three-ring hero is the approved v1 presentation in stable order: `Recovery / Strain / Sleep`. The rings share layout, not scale: Recovery is a versioned percentage estimate, Sleep is measured duration, and Strain is unavailable until a model contract defines it. Missing values remain `--`; no placeholder score is calculated to fill a position. These labels do not become permanent WholeMate ontology outside this v1 presentation policy.

`ShapingFactRanker` and `NextActionPolicy` are separate deterministic policies. The former ranks baseline evidence; the latter maps evidence/access/freshness to bounded actions. A Recovery number never directly becomes an unapproved recommendation.

## 2026-08-15 - AI is optional to comprehension

Deterministic facts, provenance, freshness, baselines, and missingness must remain useful without Gemini. AI may connect the dots and support decisions, but it never supplies measurements or makes the base product comprehensible.

## 2026-08-15 - RunMate compatibility is transitional

`RunMateRecoverySystem` is the current production source and `RecoverySnapshot` is its compatibility read model. Do not duplicate the engine while cross-client compatibility matters. A future WholeMate recovery model may supersede it through an explicit, versioned, evaluated decision.

## 2026-08-15 - RunMate UX baseline with health-first hierarchy

**Superseded by `IA v1 uses Today, Health, Move, and You` above.** Historical decision: use `runmate-mobile` as an approved visual and interaction baseline and reimplement behavior rather than copying Ionic/CSS/state code. Its former Today, Health, Move, and Coach navigation is not the current IA; coaching now belongs contextually to the product rather than a top-level destination.

## 2026-08-15 - WholeMate working product name

Use **WholeMate** as the user-facing working name. Technical identifiers migrate only through coordinated plans. The registered OAuth callback `com.wholemate.app://auth/callback` is the current integration source of truth.

## 2026-08-15 - Health Connect remains the Android health provider

Use AndroidX Health Connect directly and read records only. Samsung Health Data SDK may be evaluated as an optional Samsung-specific provider but does not replace Health Connect by default.

## 2026-08-15 - No mock health or decision output

Missing stays missing. Never fabricate Recovery, Strain, Energy, training guidance, chart values, HRV, respiratory rate, or other measurements to complete UI.

## Superseded historical decision

### 2026-08-15 - Android-only Compose Lab

Originally selected Jetpack Compose for a Samsung/Health Connect experiment. Superseded by the Compose Multiplatform architecture decision above; retained only to explain the repository's current Android-shaped implementation.
