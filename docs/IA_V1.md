# WholeMate Information Architecture v1

## Status and scope

This is the canonical product IA target for the next UI slice. It defines screen ownership and rough structure before visual design or navigation implementation.

The current Android code still labels its fourth destination `Coach`. That is implementation debt, not the IA target. No shared architecture, provider, recovery logic, or new health capability is authorized by this document.

## IA test

Every primary block must answer at least one:

1. **How am I today?**
2. **What is shaping this?**
3. **What can I do next?**

If a block only exposes data because it exists, it does not belong in the primary IA.

## Top-level destinations

```text
Today        Health          Move           You
today        body over time  movement       goals and context
why          evidence        activities     reflection
next         provenance      training       privacy and account
```

### Today — What matters now?

Owns the coherent daily story:

- Body Picture: three or four selected signals.
- What is shaping today: at most the most relevant deterministic facts.
- What next: one supported action, question, or honest `not enough evidence` state.
- Freshness, completeness, and an unobtrusive refresh/access path.

Does not own long charts, raw record lists, setup questionnaires, goal management, generic chat, or every available metric.

### Health — What is happening over time?

Owns health evidence and explanation depth:

- domain overview prioritized by relevance, not provider API order;
- trends and personal-baseline comparisons;
- metric detail with provenance, freshness, missingness, and data quality;
- connected data sources and permission management.

Does not prescribe training, duplicate Today's daily story, or present a catalog of unsupported metrics as features.

### Move — How am I moving?

Owns movement and training evidence:

- today's movement summary;
- activity history and activity detail;
- movement consistency and load facts when approved;
- planning/tools only when their contracts exist.

Running is one activity type. Move does not own whole-person recovery, sleep interpretation, or general health trends.

### You — What matters for me?

Owns explicit personal context and control:

- active goals and priorities;
- optional check-ins and reflections;
- relevant habits/context supplied by the person;
- account, privacy, data sources/access, and preferences.

You is not a social profile, achievement page, or dumping ground for product features. Sensitive context remains optional and purpose-bound.

## Where Coach went

`Coach` is not a v1 top-level destination.

- Daily interpretation and the next supported action belong on Today.
- Deeper evidence explanation opens contextually from Today or Health.
- Accepted actions and later reflection connect Today with You.
- A future conversation surface may be launched from a specific insight or goal; it does not receive a permanent tab merely because AI exists.

This preserves coaching as product behavior without turning WholeMate into an AI-chat product.

## Screen inventory

Priority meanings: `Now` is required to lock v1 layout; `Next` is a secondary screen whose entry point must be reserved; `Later` is not part of the first UI implementation.

| Destination | Screen | Priority | Job | Primary question |
|---|---|---:|---|---|
| Global | Login / session restore | Existing | Establish identity without showing product navigation | prerequisite |
| Today | Today overview | Now | Summarize body picture, shaping facts, and next step | all three |
| Today | Evidence detail | Next | Explain why a fact appeared and link to source metric | why |
| Today | Access resolution | Existing | Explain missing permission/provider and recovery path | today |
| Health | Health overview | Now | Prioritize relevant domains and recent change | today, why |
| Health | Metric trend/detail | Next | Show history, baseline, provenance, freshness, quality | why |
| Health | Data sources and access | Next | Show connected providers and permission state | why/next |
| Move | Move overview | Now | Summarize movement and latest activity | today |
| Move | Activity history | Next | Review measured activities over time | why |
| Move | Activity detail | Next | Explain one activity without whole-health inference | why |
| You | You overview | Now | Show active goal, context status, and personal controls | next |
| You | Goals | Next | Choose and review one explicit health priority | next |
| You | Check-in / reflection | Next | Add optional context or reflect on an accepted action | why/next |
| You | Privacy, account, and access | Next | Control identity, sensitive data, providers, and deletion | next |
| Contextual | Conversation | Later | Discuss a specific evidence-backed insight or goal | why/next |

## Rough wireframes

These describe hierarchy, not visual style or final copy.

### Today

```text
┌─────────────────────────────────────┐
│ Date                         status │
│ Today                               │
│ short state sentence                │
├─────────────────────────────────────┤
│ BODY PICTURE                        │
│   signal     signal      signal     │
│ freshness / completeness            │
├─────────────────────────────────────┤
│ WHAT IS SHAPING TODAY               │
│ strongest fact + evidence class     │
│ second fact only when useful        │
│                         View why →   │
├─────────────────────────────────────┤
│ WHAT NEXT                           │
│ one action / question / no evidence │
│ [primary action]       [not now]    │
└─────────────────────────────────────┘
 Today      Health       Move       You
```

Rules: Body Picture remains above the fold; shaping and next-step blocks disappear honestly when unsupported; Today never becomes a long metric feed.

### Health

```text
┌─────────────────────────────────────┐
│ Health                              │
│ What is changing in your body       │
├─────────────────────────────────────┤
│ NEEDS ATTENTION / RECENT CHANGE     │
│ prioritized domain summary          │
├─────────────────────────────────────┤
│ YOUR HEALTH                         │
│ Sleep        Heart        Breathing │
│ Recovery*    Body*        Other*    │
│ *only when relevant and supported   │
├─────────────────────────────────────┤
│ TRENDS                              │
│ selected trend preview              │
├─────────────────────────────────────┤
│ DATA QUALITY & SOURCES          →   │
└─────────────────────────────────────┘
```

Rules: relevance determines ordering; missing domains do not receive fake preview cards; provider management stays secondary.

### Move

```text
┌─────────────────────────────────────┐
│ Move                                │
│ How you are moving                  │
├─────────────────────────────────────┤
│ TODAY                               │
│ steps / active time / latest move   │
├─────────────────────────────────────┤
│ RECENT ACTIVITIES                   │
│ activity row                        │
│ activity row                        │
│                       See history → │
├─────────────────────────────────────┤
│ PATTERN                             │
│ deterministic movement fact or none │
├─────────────────────────────────────┤
│ TOOLS                               │
│ only approved planning capabilities │
└─────────────────────────────────────┘
```

Rules: no runner-only default; no recovery recommendation derived inside Move; an empty workout day can still show ordinary movement honestly.

### You

```text
┌─────────────────────────────────────┐
│ You                                 │
│ What matters for you                │
├─────────────────────────────────────┤
│ CURRENT FOCUS                       │
│ active goal or choose one           │
│                         Review →    │
├─────────────────────────────────────┤
│ CONTEXT                             │
│ last optional check-in / add context│
│ [Check in]          [Reflect]       │
├─────────────────────────────────────┤
│ YOUR CONTROLS                       │
│ Goals                               │
│ Data sources and access             │
│ Privacy and account                 │
└─────────────────────────────────────┘
```

Rules: no questionnaire on entry; context collection states why it helps before asking; account/settings remain reachable without dominating the screen.

## Cross-destination flows

```text
Morning understanding:
Today Body Picture -> shaping fact -> evidence detail in Health -> back to Today -> optional next action

Missing evidence:
Today explicit absence -> access resolution -> platform permission/provider flow -> return with previous state preserved

Movement review:
Today movement signal -> Move overview -> activity detail -> back without changing Today's interpretation

Personal context:
Today useful question -> You check-in -> confirmation -> Today refreshes only the affected context

Reflection:
Today accepted action -> later You reflection -> deterministic history -> future interpretation input
```

## Global state expectations

Every top-level destination specifies:

- first load without blanking the shell;
- refresh with previous content;
- available and partial evidence;
- empty/missing versus not permitted/not supported/not connected;
- retryable error;
- signed-out/session-expired routing;
- Web demo labeling versus Android real-provider provenance.

## Explicit non-goals for IA v1

- no fifth destination;
- no standalone AI/Coach tab;
- no medical-records hub;
- no social feed, challenges, badges, or streak pressure;
- no RunMate screen-for-screen port;
- no capability surfaced only because the shared layer or provider can represent it;
- no visual-system redesign in this slice.

## IA acceptance before visual implementation

- Each block has one owner and one product question.
- Today remains the only owner of the whole daily story.
- Health, Move, and You can be useful without AI.
- Removing unavailable data does not collapse navigation or invent substitutes.
- A new metric can enter through relevance policy without creating a destination.
- Web and Android use the same information hierarchy even when their providers and states differ.
