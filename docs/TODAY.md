# Today Experience Contract

## Purpose

Today is the health-first home of WholeMate. It presents the minimum complete picture available now without inventing missing health or AI output.

Today is intentionally short: date, evidence-status badge, one Body Picture hero, and the smallest useful path toward understanding or action. Detailed records, baselines, and seven-day charts live under Health.

## Information order

1. **Body Picture — How am I today?** Up to three or four useful signals selected from available evidence and active goals. The UI renders only signals with approved evidence; Recovery and Strain do not receive reserved positions while unavailable.
2. **What is shaping today — Why?** The smallest deterministic explanation supported by the available evidence, with a contextual route to evidence detail in Health.
3. **What next — What can I do?** One deterministic action, an access/refresh resolution, or an honest statement that there is not enough evidence.

This semantic skeleton remains present in Ready, Partial, Missing, Loading, and Error. Permission, refresh, and provider resolution appear inside the relevant block; they do not replace the whole daily story or become a second information hierarchy.

## At-a-glance body picture

- The first loaded hero must answer what is available today without requiring scroll.
- Signal models carry label, value, evidence class, freshness, availability, and action target; the UI must not hard-code product meaning to a fixed ring position.
- A current-day signal never silently substitutes an older latest value.
- Baseline deltas remain neutral numeric differences and are marked `VS BASE`.
- Metric circles are adaptive presentation, not fixed Recovery/Strain/Sleep positions. Unavailable Recovery or Strain is omitted from the glanceable picture; WholeMate never derives either score locally without an approved versioned model.
- Missing current or baseline values use `--` or `NO BASELINE` rather than fallback numbers.
- Deterministic facts remain comprehensible when AI interpretation is unavailable.

## Evidence and presentation boundary

```text
Platform provider records -> HealthFact -> BodyPicturePolicy -> BodyPictureModel -> UI
```

- `HealthFact` is typed deterministic evidence, not a UI dictionary. It contains observation time, source, freshness, and a domain-specific value.
- Provider SDK records stop before `HealthFact`; Android Today never imports or interprets Health Connect record types.
- `BodyPicturePolicy` decides which signals belong today. `BodyPictureModel` only describes the selected picture.
- The UI renders the ordered signal list. Reordering or replacing a signal does not require changing the composable.
- `AIInterpretation` remains an output classification but is not a `HealthFact` implementation.

Absence is typed rather than represented by a nullable number alone: `Available`, `Missing`, `NotPermitted`, `NotSupported`, `NotConnected`, `Stale`, and `InsufficientData`. Each state preserves its distinct product meaning and maps to explicit copy; no state receives a fallback metric.

## Evidence classes

- `Measured`: Health Connect records.
- `Calculated`: tested personal-baseline arithmetic.
- `User selected`: the goal focus chosen by the user.
- `User reported`: check-in values entered by the user.
- `Unavailable`: no approved evidence exists.

Today contains no unapproved AI interpretation, recovery score, diagnosis, or training recommendation in this milestone.

## Interaction budget

Today is primarily a reading surface. It must not present goal selection, stress/mood/energy questionnaires, or repeated setup choices in the main scroll. Future check-ins belong to a deliberate secondary flow and must advance the Daily Loop with clear value and privacy handling.

## Completion criteria

- Permission can be completed without leaving Today.
- Previous measured content remains visible during refresh and retryable failure.
- Partial data remains visible signal by signal.
- Source application and last sync time are visible.
- Missing baseline or check-in data is explicit.
- All non-measured output declares its evidence class.
- Device QA covers scrolling, touch targets, permission, rotation/process restoration, refresh, partial data, and empty data.
