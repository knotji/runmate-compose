# Recovery Contract and Logic Boundary

## Compatibility source

`runmate-mobile` computes `RunMateRecoverySystem` inside the TypeScript client in `src/lib/recoverySystem.ts`. The startup snapshot is stored in browser local storage by `src/lib/recoveryStartupCache.ts`. It is not currently a shared Supabase record or API response.

`RunMateRecoverySystem` is the legacy/current production source. `RecoverySnapshot` is its compatibility contract. Do not duplicate or reinterpret the engine while cross-client compatibility matters; doing so would create independent bug fixes and conflicting guidance.

This boundary does not make the RunMate formula permanent WholeMate architecture. A future WholeMate recovery model may replace it only through an explicit model-version decision, evaluation, migration plan, and honest user-facing provenance.

## Compatibility boundary

`RecoverySnapshot` is the minimum versioned read model Compose may consume. It includes:

- contract and model versions;
- `effectiveLocalDate`, `calculationTimeZone`, and calculation timestamp;
- score state;
- optional recovery, strain, sleep, and energy scores;
- headline and structured reasons;
- used and missing signal names;
- source labels for measured, derived, self-reported, and unavailable factors.

## Validation

Compose rejects a snapshot when:

- its contract version is unsupported;
- its model identity is blank;
- its effective local date does not match today in its declared calculation time zone;
- any score is outside its scale;
- the state is `SCORED` but the recovery score is missing.

Production integration must additionally validate schema shape, authenticated user ownership, freshness, and server/client clock assumptions.

The RunMate compatibility adapter currently enforces `Asia/Bangkok`. That timezone is adapter behavior, not a global WholeMate invariant.

## Integration options

Preferred order:

1. Extract the TypeScript engine into a shared, versioned service/API and return `RecoverySnapshot`.
2. Persist an authenticated snapshot through an explicitly reviewed server-side contract.
3. Use a temporary export bridge for device-only evaluation.

Do not create a Supabase migration, Edge Function, or production write from this Lab without separate authorization and a compatibility plan for Ionic.

## Logic change procedure

Any recovery logic change must include:

1. model-version decision;
2. fixture and boundary tests;
3. before/after examples for missing and partial signals;
4. explicit measured-versus-derived review;
5. stale-data behavior;
6. synchronized contract updates for every client;
7. a research-only evaluation before user-facing promotion when weights or thresholds change.

HRV requires a real HRV record or beat-to-beat intervals. Respiratory rate requires a real record or an approved raw-signal pipeline. Neither may be inferred from ordinary BPM, sleep duration, or SpO2 and labeled as measured.
