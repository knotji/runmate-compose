# WholeMate Architecture

## Canonical direction

WholeMate is a Compose Multiplatform product architecture with an Android implementation currently in service and a Web/Wasm development playground. The `:shared` module now contains real `commonMain`, `commonTest`, Android, and Wasm source sets. Web is not a production platform.

```text
WholeMate
├── commonMain
│   ├── domain
│   ├── data contracts
│   ├── use cases
│   ├── state
│   ├── presentation models
│   ├── shared Today playground UI
│   └── explicitly development-only demo fixtures
├── androidMain
│   ├── Health Connect
│   ├── Android secure storage
│   ├── Firebase / platform services
│   └── Android-specific integrations
├── wasmJsMain
│   └── browser entry point
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

The current `:app` module is the Android host. The `:shared` module owns deterministic health facts, provenance/freshness/absence semantics, Body Picture policy/models, personal baselines, load state, shared tests, and the browser playground UI. Health Connect repository mapping, permissions, lifecycle, OAuth, secure storage, and Android navigation remain in `:app`.

The Web target uses `DemoHealthProvider` only. Its UI must visibly identify demo data and may not mimic Health Connect authorization, claim real measurements, add Web-specific recovery behavior, or become a separate Web product roadmap.

Migration order:

1. Continue moving deterministic use cases and platform-neutral presentation logic only after their contracts stabilize.
2. Keep Health Connect, Android secure storage, permissions, OAuth, and lifecycle in the Android host.
3. Use Web fixtures to iterate shared UI states without inventing provider behavior.
4. Move additional state/design tokens only when their platform lifecycle behavior is defined.
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
