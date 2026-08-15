# WholeMate Product Concept

## Working name

**WholeMate** is a working product name: a companion that helps a person understand the whole body and mind, not only workouts. It requires formal trademark, store, domain, and localization clearance before a production launch.

The existing Android package, Firebase application, repository, and internal Kotlin package names remain unchanged during the experiment so the rename does not break installation identity, Health Connect permission history, or distribution. Technical identifiers may be migrated only after the product name is approved.

## Product promise

> WholeMate learns from the health signals a person already has, understands their goals and context, and explains what may deserve attention next.

WholeMate is health-first. Running is one activity signal. The product remains useful for someone who walks, cycles, lifts, rests, manages stress, improves sleep, or has no workout on a given day.

## Health map

The product organizes goals and evidence into connected domains:

| Domain | Example measured signals | Example goals |
|---|---|---|
| Sleep | duration, timing, consistency, stages when available | sleep longer, stabilize bedtime, wake refreshed |
| Cardiovascular | heart rate, resting patterns, HRV when available | improve aerobic health, notice unusual change |
| Respiratory | respiratory rate and trends when available | understand overnight change, support recovery context |
| Movement | steps, workouts, duration, intensity when available | move consistently, build strength, prepare for an event |
| Recovery | approved shared recovery facts, fatigue feedback | balance effort and rest, recover from a demanding period |
| Mind and stress | user check-ins, perceived stress, mood, workload context | reduce stress, protect focus, recognize difficult patterns |
| Habits and context | sleep routine, alcohol, illness, travel, medication notes when explicitly supplied | learn which behaviors support personal wellbeing |

No domain receives a fabricated score merely to complete the interface.

## Goal model

A goal is not only a target number. Each goal contains:

- domain and user intent;
- desired outcome and optional target;
- time horizon;
- measured evidence that can support it;
- user-reported context;
- confidence and data sufficiency;
- small actions the user accepts;
- progress review and feedback.

Goals may influence what WholeMate prioritizes, but must never alter measured records. Conflicting goals—such as increasing training while reducing exhaustion—must be shown as a tradeoff rather than silently optimized.

## AI reasoning contract

Gemini is an interpretation and conversation layer, not a source of physiological measurements.

```text
Health Connect + explicit user check-ins
                 ↓
Typed, timestamped health timeline
                 ↓
Deterministic facts, baselines, and data-quality checks
                 ↓
Gemini interpretation with evidence references
                 ↓
Insight, clarifying question, or suggested next action
```

Every output is classified:

- `Measured`: returned by an approved data provider.
- `Calculated`: deterministic, versioned, and tested.
- `Observed pattern`: supported by the user's history and a stated time window.
- `AI interpretation`: a bounded explanation or hypothesis with confidence.
- `User reported`: mood, stress, symptoms, or context entered by the user.
- `Missing`: unavailable; never inferred as a numeric measurement.

Gemini may connect evidence, summarize change, ask a useful question, and personalize language. It may not invent measurements, diagnose a condition, silently convert correlation into causation, or present a medical claim as fact.

## Today experience

Today answers in this order:

1. What measured signals are available and how fresh are they?
2. What changed relative to the person's own baseline?
3. Which active body or mind goal may be affected?
4. Is there enough evidence for an insight?
5. What small next step or question is appropriate?

When evidence is insufficient, the correct answer is `Not enough data yet` plus a useful way to improve context—not a synthetic readiness score.

## Mental health and stress boundary

- Stress and mood begin as optional user-reported check-ins unless an approved measured provider exists.
- Language is supportive and non-judgmental; streak loss and alarmist scoring are avoided.
- High-risk or crisis language must trigger a dedicated safety response and appropriate local help guidance, not ordinary coaching.
- Sensitive notes require explicit consent, minimal retention, deletion controls, and no health values in logs.
- WholeMate supports reflection and behavior change; it is not a replacement for a clinician or emergency service.

## Initial product slices

1. Health timeline: typed Sleep, Heart Rate, HRV, Respiratory, and Activity events.
2. Personal baselines: transparent time windows, freshness, and minimum-data rules.
3. Goals: one body or mind goal with progress evidence and a manual check-in.
4. Stress check-in: perceived stress, mood, energy, optional note, and privacy controls.
5. Gemini insight prototype: evidence-bounded summary with citations back to local facts.
6. Feedback: useful/not useful, what changed, and whether the suggested action was attempted.

## Success criteria

WholeMate succeeds when a user can understand a meaningful health change, see the evidence behind it, connect it to a personal goal, and decide what to do next—even without continuous 24-hour sensing.
