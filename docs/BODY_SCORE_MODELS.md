# WholeMate Experimental Body Score Models

## Status and safety boundary

These models are experimental wellness indexes, not diagnoses, medical-device outputs, or reproductions of WHOOP algorithms. The Recovery ring is a **derived recovery estimate**, not physiological truth. Every calculated fact must retain model version and calculated provenance. Missing input never becomes a real zero and is never imputed.

The initial versions are:

- `wholemate-recovery-v1-experimental`

Neither replaces the versioned RunMate compatibility snapshot. Promotion requires research evaluation, fixture review, calibration review, and an explicit product decision.

## Recovery v1

Recovery requires:

- today's measured sleep duration;
- six valid prior daily sleep observations; and
- at least one autonomic heart signal with six valid prior observations: measured HRV RMSSD, measured resting heart rate, or derived sleeping-heart-rate average.

Derived sleeping heart rate requires at least 30 timestamped heart-rate samples inside the selected sleep interval. Measured resting heart rate takes precedence over derived sleeping heart rate. HRV and the selected heart-rate signal are both used when both are available.

Each current signal is compared with its own personal baseline. At baseline the component is 50. Sleep and HRV are higher-is-better; resting or sleeping heart rate is lower-is-better.

```text
sleep component = clamp(50 + 50 * ((sleep / sleep baseline - 1) / 0.25), 0, 100)
HRV component   = clamp(50 + 50 * ((HRV / HRV baseline - 1) / 0.20), 0, 100)
HR component    = clamp(50 - 50 * ((HR / HR baseline - 1) / 0.10), 0, 100)

Recovery = 35% sleep + 40% HRV + 25% heart rate
```

Unavailable component weights are removed and the remaining weights are normalized by their sum. With only sleep and sleeping HR available, effective weights are `0.35 / 0.60 = 58.3%` and `0.25 / 0.60 = 41.7%`. The result is rounded to the nearest integer and clamped to 0-100.

The tolerances `25%`, `20%`, `10%` and weights `35/40/25` are deterministic WholeMate product heuristics. They are not clinical thresholds or physiologically validated cutoffs.

### Locked device fixture

The first Samsung sleeping-HR fallback is locked as a regression fixture:

| Input | Current | Six-day baseline | Component |
| --- | ---: | ---: | ---: |
| Sleep duration | 6.72 h | about 6.67 h | about 51 |
| Derived sleeping HR | 53.27 bpm | about 51.11 bpm | about 29 |

Without HRV or measured resting-HR records, normalized weighting produces `42`. This proves implementation stability, not model validity. Tests additionally require neutral output at baseline, the expected direction when sleeping HR changes, insufficient data when required evidence is absent, and 0-100 clamps at extremes.

Product language must remain restrained: `Recovery 42 - below your usual baseline today`. When structured component evidence is available, presentation may explain that sleeping HR was above the recent baseline while sleep duration was close to usual. Until evaluation, avoid claims such as `poor recovery` or `your body is stressed`.

## Sleep ring semantics

The Today ring displays measured duration, for example `6h 43m`; it does not expose the Recovery model's sleep component. Until a versioned sleep-need or goal contract exists, its ring is a data-presence treatment rather than a percentage gauge. A 24-hour denominator has no product meaning and must not be used.

## Strain is not defined yet

WholeMate has not approved a canonical Strain meaning, formula, scale, or maximum. Exercise load, all-day activity, cardiovascular load, and training load are not interchangeable. Today therefore displays `--` / unavailable until a separately versioned Strain model contract is approved; steps or duration must not be converted into a placeholder score.

## Evidence rationale

- Health Connect defines HRV RMSSD and resting heart rate as distinct record types; generic heart-rate samples are not relabeled as measured resting heart rate.
- Recovery is multi-dimensional and varies within individuals, so this model uses personal baselines and refuses sleep-only scoring.

References:

- Android Health Connect record reference: https://developer.android.com/reference/androidx/health/connect/client/records/Record
- Recovery and Performance in Sport consensus statement: https://pubmed.ncbi.nlm.nih.gov/29345524/
