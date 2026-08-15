# RunMate Compose Lab

An isolated Android Jetpack Compose experiment for RunMate. It does not replace or modify `runmate-mobile`.

## Engineering contracts

- [Architecture](docs/ARCHITECTURE.md)
- [Roadmap](docs/ROADMAP.md)
- [UI system](docs/UI_SYSTEM.md)
- [State and loading](docs/STATE_AND_LOADING.md)
- [Recovery contract](docs/RECOVERY_CONTRACT.md)
- [Performance](docs/PERFORMANCE.md)
- [Decision log](docs/DECISION_LOG.md)

These documents are part of the implementation contract. Changes to layout tokens, loading behavior, state ownership, recovery semantics, or performance targets must update the matching document and tests in the same change.

## Experiment question

Does a native Compose health surface feel better and make Health Connect state easier to maintain enough to justify a second UI stack?

## Guardrails

- Read-only Health Connect access.
- Recovery is consumed from existing RunMate output; it is not recalculated here.
- No database migrations, production writes, unrelated refactors, or cleanup.
- The native dashboard is disabled by default.

## Run

```powershell
.\gradlew.bat assembleDebug -PnativeHealthDashboard=true
```

Without the property, the application builds with the experiment disabled:

```powershell
.\gradlew.bat assembleDebug
```

## PoC definition of done

1. Capture the Ionic baseline on the target Samsung device.
2. Open the native dashboard from Ionic.
3. Show Health Connect permission/state and real sleep, heart-rate, and workout data.
4. Support refresh plus loading, empty, and error states.
5. Return to Ionic with its state intact.
6. Pass tests and debug/release builds.

The current Lab reads Health Connect data directly. Today shows only records returned by Health Connect; it does not render fallback chart values, mock recovery/strain/energy scores, or generated training guidance. `RecoverySnapshot` remains the boundary for a future shared provider without duplicating the production engine.

## Firebase App Distribution

Keep `app/google-services.json` local; it is ignored by Git. To build an experiment-enabled APK with a fresh version code and upload it to the matching Compose Lab Firebase app:

```powershell
$env:RUNMATE_COMPOSE_TESTERS = 'tester@example.com'
.\scripts\distribute-debug.ps1 -ReleaseNotes 'Compose Health Connect data read.'
```
