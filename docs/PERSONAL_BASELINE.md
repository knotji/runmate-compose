# Personal Baseline Contract

## Purpose

Personal baselines describe how a measured value differs from the same person's recent records. They are deterministic calculated facts, not recovery scores, medical ranges, or AI interpretations.

## Initial window

- The UI uses the seven calendar-day Health Connect trend.
- Today's value is the comparison value and is always excluded from its own baseline.
- The baseline uses available values from the preceding six calendar days.
- At least three preceding days are required.
- Missing and non-finite values are excluded, never converted to zero.
- If today is missing or fewer than three baseline days exist, return `InsufficientData` with the exact reason.

## Presentation

- Label output as `Calculated`.
- State the baseline average, unit, and number of previous days used.
- Describe only numeric difference; do not label a direction good, bad, healthy, recovered, stressed, or abnormal.
- Do not generate a recommendation from this comparison.

## Current metrics

- Sleep duration in hours.
- Daily average heart rate in beats per minute.

Adding another metric requires a typed source, unit definition, minimum-data rule, tests for missing data, and an explicit product interpretation boundary.
