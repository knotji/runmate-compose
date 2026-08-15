# WholeMate Architecture

## Canonical direction

WholeMate is a Compose Multiplatform product architecture with an Android implementation currently in service. The repository has not completed the source-set migration yet: today it contains an Android application module and Android `main` sources. New product logic must nevertheless be shaped for extraction to shared Kotlin rather than deepening Android coupling.

```text
WholeMate
├── commonMain
│   ├── domain
│   ├── data contracts
│   ├── use cases
│   ├── state
│   ├── presentation models
│   └── design system
├── androidMain
│   ├── Health Connect
│   ├── Android secure storage
│   ├── Firebase / platform services
│   └── Android-specific integrations
└── iosMain (when enabled)
    ├── HealthKit
    ├── Keychain
    └── iOS platform services
```

> **Share product logic, not platform APIs.**

Do not distort Health Connect or HealthKit into a lowest-common-denominator API. Platform adapters produce shared domain facts and explicit capability/quality metadata; shared code owns product decisions, state, presentation models, and deterministic rules.

## Product boundary

WholeMate is not a health-data dashboard. Architecture must support the daily loop `Observe -> Understand -> Decide -> Act -> Reflect -> Learn` and the three primary questions in `PRODUCT_CONCEPT.md`.

Running is an activity signal, not the navigation root. A screen or data source is not promoted merely because a platform API exists.

## Dependency direction

```text
Platform UI -> shared presentation/state -> shared use cases/domain
Platform adapters ---------------------> shared provider contracts
Platform SDKs remain behind adapters
```

- Composables render immutable presentation models and send events.
- Shared product logic depends on interfaces and domain values, never Android SDK types.
- Android adapters own Health Connect, Keystore, intents, Firebase Android services, and lifecycle details.
- Future iOS adapters own HealthKit, Keychain, and iOS lifecycle details.
- Data provenance, freshness, consent, and missingness cross the adapter boundary explicitly.

## Current implementation and migration boundary

The current `:app` module is the Android host. Existing Android code remains valid while capabilities are moved incrementally; a documentation reset does not pretend that `commonMain` or iOS code already exists.

Migration order:

1. Introduce shared domain and data contracts with no Android types.
2. Move deterministic baselines, use cases, and presentation models to shared Kotlin.
3. Keep Health Connect and Android secure storage in Android source sets behind interfaces.
4. Move app state and design tokens when their platform lifecycle behavior is defined.
5. Enable an iOS host only after its provider, consent, secure-storage, and device-QA plan is approved.

Do not pause Android product learning while waiting for iOS, and do not label Android-only code as shared by abstraction alone.

## Greenfield and compatibility boundary

WholeMate is not a source-code port of `runmate-mobile`. Proven information architecture and interactions may be reproduced to reduce relearning, but components, CSS, TypeScript stores, and business logic are not mechanically translated.

Compatibility enters through explicit, versioned contracts. `RunMateRecoverySystem` remains the current production source while compatibility matters; it is not the permanent WholeMate recovery ontology.

## Security and privacy

- Secrets, tokens, signing files, `google-services.json`, and local configuration stay untracked.
- Health values never enter logs or analytics payloads.
- Mobile clients use only public/publishable Supabase credentials with authenticated RLS.
- `SessionVault` is a shared interface: Android uses Keystore-backed encryption; iOS uses Keychain when implemented.
- Provider permissions remain platform-native, purpose-bound, and revocable.

## Repository and release discipline

- Documentation, code, and tests change together when a contract changes.
- Firebase Android builds prove the Android host only; they do not prove CMP portability or iOS behavior.
- Database migrations, health writes, and new production recovery providers require separate authorization.
- Release notes identify real providers and unavailable outputs honestly.
