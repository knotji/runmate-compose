# WholeMate Product Constitution

## Canonical product promise

> **WholeMate helps you understand how you are today, what is shaping that state, and what you can do next.**

WholeMate is a personal health companion. It combines measured health data, personal baselines, goals, and explicit user context into a coherent daily picture.

Health data is evidence, not the product. WholeMate does not exist to display every metric a provider exposes.

Every primary experience must help answer at least one question:

1. How am I today?
2. What is shaping this?
3. What should I do next?

Running, workouts, sleep, recovery, stress, nutrition, and other health domains are inputs into one whole-person model. No domain is the product identity.

## Product laws

1. Evidence before interpretation.
2. Personal context before population judgment.
3. Missing stays missing.
4. Every insight must improve understanding or action.
5. Whole-person health, not workout-first health.

When evidence is insufficient, WholeMate says so. It never invents a measurement or score to complete the interface.

## Daily loop

```text
Observe -> Understand -> Decide -> Act -> Reflect -> Learn
```

The loop is the feature filter:

- **Observe:** collect an approved measurement or explicit user report.
- **Understand:** describe what changed, its freshness, and its relationship to personal context.
- **Decide:** offer a bounded choice or useful question only when evidence supports it.
- **Act:** let the person choose a small, realistic next step.
- **Reflect:** ask whether the action helped without judgment or forced streaks.
- **Learn:** use the result as future personal context, never as fabricated physiology.

Example:

```text
Sleep 5h 42m + resting HR above personal baseline + reported stress high
  -> "Your sleep was shorter than usual and resting HR is elevated."
  -> "Keep today lighter?"
  -> User chooses an easy day
  -> Tomorrow: "Did the lighter day help?"
```

A proposed capability that cannot identify its place in this loop is not ready for the primary product experience.

## Body Picture

The Today v1 hero keeps `Recovery / Strain / Sleep` in stable positions so the user can learn the glanceable layout. Their semantics stay typed and distinct: percentage estimate, unavailable strain contract, and measured sleep duration. This is an approved first configuration and familiar UI pattern, not permanent product ontology.

The selected signals may change as goals, evidence, and approved models evolve—for example `Sleep / Energy / Stress / Movement` or `Recovery / Sleep / Resting HR / Activity`. The component is reusable; its semantics come from typed presentation models and must not be hard-coded into shared product logic.

## Goals and health domains

A goal includes user intent, desired outcome, time horizon, supporting measured evidence, explicit context, confidence/data sufficiency, accepted actions, and reflection. Goals prioritize the Body Picture, Today guidance, and You context; they never alter measured records.

Health domains are a capability map, not a dashboard menu or delivery roadmap. Sleep, cardiovascular, respiratory, movement, recovery, mind/stress, habits, and nutrition enter the product only when they improve a user decision.

Conflicting goals must be shown as tradeoffs rather than silently optimized.

## AI reasoning contract

Gemini is an optional interpretation and conversation layer, never a source of physiological measurements.

```text
Approved providers + explicit user context
                 ↓
Typed, timestamped health timeline
                 ↓
Deterministic facts, baselines, and quality checks
                 ↓
Optional AI interpretation with evidence references
                 ↓
Insight, clarifying question, or suggested next action
```

Every output is classified as `Measured`, `Calculated`, `Observed pattern`, `AI interpretation`, `User reported`, or `Missing`.

AI is optional to comprehension. Without Gemini, WholeMate must still provide useful deterministic facts such as `Sleep ↓ 1h 12m vs baseline` and `Resting HR ↑ 6 bpm vs baseline`.

Gemini may connect evidence, summarize change, ask a useful question, and personalize language. It may not invent measurements, diagnose, turn correlation into causation, or present medical claims as fact.

## Navigation meaning

- **Today — What matters today?** A short Body Picture and the most relevant next understanding or action.
- **Health — What is happening in my body over time?** Evidence, trends, provenance, and data quality.
- **Move — How am I moving and training?** Activity and training as one health domain.
- **You — What matters for me?** Goals, explicit context, reflection, privacy, and personal control.

Interpretation and action are product behavior rather than a permanent Coach destination: daily guidance belongs on Today, deeper explanation opens from its evidence, and future conversation is launched contextually from an insight or goal.

## Mental health and stress boundary

- Stress and mood begin as optional user reports unless an approved measured provider exists.
- Language is supportive and non-judgmental; avoid alarmist scoring and streak punishment.
- Crisis language requires a dedicated safety response and local-help guidance, not ordinary coaching.
- Sensitive notes require explicit consent, minimal retention, deletion controls, and no health values in logs.
- WholeMate supports reflection and behavior change; it is not a clinician or emergency service.

## Working identity

**WholeMate** is the working product name pending trademark, store, domain, and localization clearance. Product naming is canonical even while technical identifiers are migrated deliberately. The currently registered OAuth callback is `com.wholemate.app://auth/callback`; changing it requires coordinated application and Supabase configuration.

## Success criterion

WholeMate succeeds when a person can understand a meaningful change, see the evidence and uncertainty behind it, connect it to personal context, and choose what to do next—even without continuous 24-hour sensing or AI availability.
