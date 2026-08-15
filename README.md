# WholeMate

> **Understand today → understand why → decide what next.**

WholeMate is a personal health companion built toward a Compose Multiplatform architecture. It uses measured health evidence, personal baselines, goals, and explicit user context to create a useful daily picture. It is not an “everything health” dashboard, and it never invents measurements or scores to fill the interface.

## Repository status

The repository now has a real `:shared` Kotlin Multiplatform module with Android and Web/Wasm targets. Health facts, Body Picture policy/models, personal baselines, load-state semantics, tests, and the Today playground UI live in `commonMain`. The production-capable host remains Android; Web is a development playground only. No iOS target exists yet.

## Product questions

Every primary experience must help answer at least one:

1. How am I today?
2. What is shaping this?
3. What should I do next?

The product loop is `Observe → Understand → Decide → Act → Reflect → Learn`. AI may connect the dots but is optional to comprehension.

## Canonical contracts

- [Product constitution](docs/PRODUCT_CONCEPT.md)
- [Information architecture v1](docs/IA_V1.md)
- [Architecture](docs/ARCHITECTURE.md)
- [Roadmap](docs/ROADMAP.md)
- [Decision log](docs/DECISION_LOG.md)
- [Today / Body Picture](docs/TODAY.md)
- [UI system](docs/UI_SYSTEM.md)
- [State and loading](docs/STATE_AND_LOADING.md)
- [Supabase](docs/SUPABASE.md)
- [Recovery compatibility](docs/RECOVERY_CONTRACT.md)
- [Personal baseline](docs/PERSONAL_BASELINE.md)
- [Health capability catalog](docs/HEALTH_DATA_CATALOG.md)
- [Performance](docs/PERFORMANCE.md)

These documents are implementation contracts. Behavior changes update the matching document and tests together.

## Current boundaries

- Health Connect is an Android platform adapter and remains read-only.
- Recovery consumes a versioned compatibility result; it is not recalculated locally.
- Missing data stays missing, and deterministic facts remain useful without Gemini.
- Supabase uses authenticated owner-scoped reads; there are no database migrations or production health writes.
- Secrets, `google-services.json`, signing material, and local configuration remain untracked.

## Android build

```powershell
.\gradlew.bat assembleDebug -PnativeHealthDashboard=true
```

Without the experimental property:

```powershell
.\gradlew.bat assembleDebug
```

## Web development playground

The Web target is for fast shared-UI iteration. It uses `DemoHealthProvider` fixtures only and never presents itself as Health Connect or production health data.

Run the development server and open the browser:

```powershell
.\gradlew.bat :shared:wasmJsBrowserDevelopmentRun
```

Build the static Web distribution:

```powershell
.\gradlew.bat :shared:wasmJsBrowserDistribution
```

The production bundle is written to:

```text
shared/build/dist/wasmJs/productionExecutable
```

The design lab includes the IA v1 shell for Today, Health, Move, and You plus deterministic `available`, `partial`, `missing`, `loading`, and `error` fixtures. The fixture switcher is development chrome, not product UI. The lab has no Supabase authentication, Health Connect emulation, recovery calculation, or production deployment contract. A browser with WasmGC support is required.

## Firebase App Distribution

Keep `app/google-services.json` local. To build and upload the Android test APK:

```powershell
$env:RUNMATE_COMPOSE_TESTERS = 'tester@example.com'
.\scripts\distribute-debug.ps1 -ReleaseNotes 'WholeMate Android test build.'
```

An Android distribution validates only the Android host; it is not evidence that iOS or CMP migration is complete.
