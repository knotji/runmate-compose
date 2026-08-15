# Today Experience Contract

## Purpose

Today is the health-first home of WholeMate. It presents the minimum complete picture available now without inventing missing health or AI output.

## Information order

1. At-a-glance body picture: three rings for today's Sleep, Heart, and measured signal coverage, with neutral baseline deltas.
2. Health Connect permission and refresh action when attention is required.
3. Latest measured Sleep, Heart Rate, HRV, Respiratory Rate, and Activity records with source and sync time.
4. Deterministic personal-baseline comparisons when sufficient history exists.
5. Seven-day measured chart.

## At-a-glance body picture

- The first loaded hero must answer what is available today without requiring scroll.
- Sleep and Heart tiles use today's seven-day-trend record, never an older 30-day latest value.
- Baseline deltas remain neutral numeric differences and are marked `VS BASE`.
- The three rings are visual grouping, not three scores. Sleep and Heart arcs do not encode progress or health quality; the Signals ring states measured coverage explicitly.
- Missing current or baseline values use `--` or `NO BASELINE` rather than fallback numbers.
- The hero explicitly states that AI interpretation is not active.

## Evidence classes

- `Measured`: Health Connect records.
- `Calculated`: tested personal-baseline arithmetic.
- `User selected`: the goal focus chosen by the user.
- `User reported`: check-in values entered by the user.
- `Unavailable`: no approved evidence exists.

Today contains no AI interpretation, recovery score, diagnosis, or training recommendation in this milestone.

## Interaction budget

Today is primarily a reading surface. It must not present goal selection, stress/mood/energy questionnaires, or repeated setup choices in the main scroll. Future check-ins belong to a deliberate secondary flow with clear value and privacy handling.

## Completion criteria

- Permission can be completed without leaving Today.
- Previous measured content remains visible during refresh and retryable failure.
- Partial data remains visible signal by signal.
- Source application and last sync time are visible.
- Missing baseline or check-in data is explicit.
- All non-measured output declares its evidence class.
- Device QA covers scrolling, touch targets, permission, rotation/process restoration, refresh, partial data, and empty data.
