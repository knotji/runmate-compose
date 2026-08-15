# WholeMate Roadmap

## Purpose

This roadmap builds the WholeMate daily loop while migrating the current Android host toward the canonical Compose Multiplatform architecture. Completion requires real-device evidence; compilation alone is not acceptance.

`PRODUCT_CONCEPT.md` is the product constitution. Architecture and feature work may not reduce WholeMate to a metric dashboard.

## Product and delivery gates

Every slice must state:

1. which question it answers: how am I, what is shaping this, or what next;
2. where it sits in `Observe -> Understand -> Decide -> Act -> Reflect -> Learn`;
3. its evidence classes, missingness, freshness, and consent;
4. what belongs in shared product logic versus a platform adapter;
5. automated and physical-device acceptance criteria.

Missing stays missing. AI is optional to comprehension. Health-provider reads remain read-only until a separate write use case is approved.

## Current factual baseline

- [x] Android application host using Jetpack Compose.
- [x] Health Connect permission/read flow for Sleep, HR, HRV RMSSD, respiratory rate, steps, and activity.
- [x] Explicit loading, permission, unavailable, empty, partial, and error presentation.
- [x] Google OAuth PKCE, Android Keystore-backed session restore/refresh/logout, owner profile, and read-only history.
- [x] Current Android implementation has Today, Health, Move, and Coach navigation on one visual system.
- [x] IA v1 target is locked as Today, Health, Move, and You; code migration remains pending.
- [x] Initial Recovery, Strain, and Sleep hero without fabricated unavailable scores.
- [x] Deterministic personal baselines with missing-data rules.
- [x] Firebase App Distribution and Samsung tester flow.
- [x] Kotlin Multiplatform `:shared` module with `commonMain`, `commonTest`, Android, and Web/Wasm targets exists.
- [x] Health facts, Body Picture policy/models, personal baselines, load state, and their tests are shared.
- [x] Web Today development playground covers available, partial, missing, loading, and error demo states.
- [ ] iOS source set/host exists.
- [ ] Remaining approved shared use cases and design tokens are extracted from Android code.
- [ ] Physical-device acceptance record covers the current real-data Today flow.

The Web target is development tooling, not a production platform or a substitute for Android device acceptance.

## Milestone 1 — Canonical contracts and shared seams

Goal: stop adding Android coupling to product logic.

- [x] Reset product, architecture, Today, state, recovery, provider, and UI contracts to WholeMate + CMP terminology.
- [x] Define provider-neutral typed health facts with provenance, freshness, and explicit absence.
- Define `SessionVault`, health-provider, recovery-provider, and owner-history interfaces without platform SDK types.
- [x] Define an adaptive `BodyPictureModel` containing three or four typed signal presentations.
- [x] Keep the first signal selection `Recovery / Strain / Sleep` while removing semantic dependence on fixed UI slots.

Acceptance:

- [x] Shared contracts import no Android or Health Connect types.
- [x] The current Android UI renders the same honest values through those contracts.
- [x] Tests cover missing, partial, stale, timezone, ordering, permission, and provider-identity differences.

## Milestone 2 — CMP source-set migration

Goal: establish real shared Kotlin without pretending platform APIs are portable.

- [x] Introduce the Kotlin Multiplatform module/source-set layout.
- [x] Move health facts, deterministic personal baselines, load-state semantics, Body Picture policy/models, and tests to `commonMain`/`commonTest`.
- [x] Keep Health Connect, Keystore, Android OAuth/deep links, Firebase Android services, permissions, and Android lifecycle restoration in the Android host.
- [x] Add a Web/Wasm playground using visibly labeled demo data only.
- Add architecture checks that prevent Android dependencies from entering common code.
- Define `iosMain` interfaces/stubs only when they clarify a real boundary; do not fabricate a working HealthKit client.

Acceptance:

- [x] Common tests run on Android/JVM and Web/Wasm.
- [x] Android debug build and existing tests remain green.
- [x] No platform API is hidden behind a misleading lowest-common-denominator abstraction.

## Milestone 3 — Useful Today without AI

Goal: answer “How am I today?” and “What is shaping this?” with deterministic evidence.

- Add per-signal source, freshness, quality, and explicit absence.
- Make Body Picture selection data/goal-aware through an approved deterministic rule.
- Add neutral baseline comparisons and a concise “what changed” explanation.
- Keep detailed records and diagnostic charts under Health.
- Keep setup choices and questionnaires out of the primary Today scroll.

Acceptance:

- Today remains useful with Gemini disabled and with partial provider data.
- Every visible claim traces to a measured fact, tested calculation, or explicit user report.
- Samsung QA covers fresh, stale, partial, empty, revoked-permission, offline, and error states.

## IA/UI implementation sequence

The canonical inventory and rough wireframes live in `IA_V1.md`.

1. Implement the four-destination Web design lab shell: Today, Health, Move, You.
2. Stabilize Today hierarchy before detailed visual polish.
3. Implement Health, Move, and You hierarchy with deterministic demo states.
4. Map existing shared models only after screen ownership is stable.
5. Port the locked hierarchy to Android and validate behavior/device constraints.

Do not add shared architecture, a fifth tab, provider-driven features, or a generic AI conversation surface during this sequence.

## Milestone 4 — First complete Daily Loop

Goal: prove companion behavior rather than dashboard breadth.

- Approve one body or mind goal and one optional, privacy-bounded context check-in.
- Produce one deterministic understanding statement from evidence and personal baseline.
- Offer one bounded next action the user can accept or decline.
- Ask for a later reflection and retain only the context needed to learn.
- Keep crisis/safety behavior separate from ordinary coaching.

Acceptance:

- One real user can complete Observe through Reflect without AI.
- Missing evidence prevents unsupported advice.
- The user can inspect provenance, skip context, and delete the report/reflection.

## Milestone 5 — Recovery compatibility

Goal: consume current RunMate recovery output without making it permanent WholeMate ontology.

- Approve a single external producer of versioned `RecoverySnapshot`.
- Carry `effectiveLocalDate`, `calculationTimeZone`, model version, freshness, reasons, and used/missing signals.
- Enforce `Asia/Bangkok` only in the RunMate compatibility adapter.
- Compare identical fixtures/accounts across RunMate and WholeMate.

Acceptance:

- Compatible clients show identical source output and provenance.
- Invalid/stale snapshots are rejected or visibly stale.
- No duplicate recovery formula exists in WholeMate.

## Milestone 6 — Optional AI interpretation

Goal: let Gemini connect approved facts without becoming a measurement or availability dependency.

- Provide only typed evidence, explicit context, quality, and allowed actions.
- Require evidence references and classify output as AI interpretation.
- Fall back to deterministic comprehension when AI is unavailable.
- Capture useful/not useful feedback and whether an accepted action helped.

Acceptance:

- AI cannot invent numeric physiology or override missingness.
- Offline/disabled AI leaves Today and the Daily Loop understandable.
- Safety, privacy, prompt-injection, and retention reviews pass before promotion.

## Milestone 7 — Platform expansion decision

Goal: decide when an iOS host is justified by product evidence.

- Define HealthKit capability/consent mapping and Keychain implementation.
- Verify shared presentation/state assumptions against iOS lifecycle and navigation.
- Establish iOS performance, accessibility, and device-QA budgets.
- Enable the host only with a bounded, testable product slice.

An iOS client is enabled work when justified, not forbidden work and not a claim of current completion.

## Continuous performance and privacy

- Keep previous content visible during refresh and prevent stale race replacement.
- Add Android Macrobenchmark and Baseline Profile once flows stabilize.
- Measure release-like builds on representative devices per platform.
- Keep health values, tokens, identifiers, and sensitive notes out of logs.
- Verify RLS with isolated accounts before broadening Supabase reads.

## Not scheduled without a separate decision

- Rewriting the RunMate recovery formula as an incidental migration.
- Samsung Health Data SDK replacing Health Connect by default.
- Blanket health permissions or feature work driven only by provider API breadth.
- Production database migrations, health writes, or mechanical `runmate-mobile` source porting.
- A general-purpose health dashboard or AI chat tab without the Daily Loop.

## Next executable slice

Implement the four-destination shell in the Web design lab using the IA v1 hierarchy: Today, Health, Move, and You. Start with deterministic demo states and rough interaction structure; do not add visual polish, shared architecture, provider capabilities, or recovery changes in this slice.
