# Today Experience Contract

## Purpose

Today is the health-first home of WholeMate. It presents the minimum complete picture available now without inventing missing health or AI output.

## Information order

1. At-a-glance body picture: today's measured Sleep and Heart values, neutral baseline deltas, Mind check-in status, data availability, and selected focus.
2. Health Connect permission and refresh action when attention is required.
3. User-selected focus for the day.
4. Latest measured Sleep, Heart Rate, HRV, Respiratory Rate, and Activity records with source and sync time.
5. Deterministic personal-baseline comparisons when sufficient history exists.
6. Optional user-reported Stress, Mood, and Energy check-in.
7. Seven-day measured chart.

## At-a-glance body picture

- The first loaded hero must answer what is available today without requiring scroll.
- Sleep and Heart tiles use today's seven-day-trend record, never an older 30-day latest value.
- Baseline deltas remain neutral numeric differences and are marked `VS BASE`.
- Mind displays only a user-entered Stress value; it never infers stress from heart or sleep records.
- Missing current or baseline values use `--` or `NO BASELINE` rather than fallback numbers.
- The hero explicitly states that AI interpretation is not active.

## Evidence classes

- `Measured`: Health Connect records.
- `Calculated`: tested personal-baseline arithmetic.
- `User selected`: the goal focus chosen by the user.
- `User reported`: check-in values entered by the user.
- `Unavailable`: no approved evidence exists.

Today contains no AI interpretation, recovery score, diagnosis, or training recommendation in this milestone.

## Daily focus

Available focus domains are Balance, Sleep, Stress, Heart Health, and Movement. Selecting a focus changes future prioritization only. It must not modify, hide, rescale, or reinterpret measured records.

## Mind and body check-in

- Stress, Mood, and Energy are independently optional values on a 1–5 scale.
- Scale direction is stated in the UI.
- Values are explicitly labelled `User reported`.
- Editing a value clears the previous saved timestamp until the user saves again.
- At least one value is required to save.
- The current implementation uses Android saved state and contains no free-text note, cloud sync, or Gemini call.
- Durable sensitive storage requires a separate encrypted-storage and retention decision.

## Completion criteria

- Permission can be completed without leaving Today.
- Previous measured content remains visible during refresh and retryable failure.
- Partial data remains visible signal by signal.
- Source application and last sync time are visible.
- Missing baseline or check-in data is explicit.
- All non-measured output declares its evidence class.
- Device QA covers scrolling, touch targets, permission, rotation/process restoration, refresh, partial data, and empty data.
