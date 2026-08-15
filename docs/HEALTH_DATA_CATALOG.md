# Health Data Capability Catalog

## Capability boundary, not roadmap

This catalog records what an approved platform provider may expose. It is not a promise to build a dashboard, request every permission, or deliver every listed feature.

Every new capability must pass this sequence before implementation:

```text
User goal -> why the data matters -> decision improved -> consent requirement -> implementation
```

Android capabilities currently come from Health Connect. Other platforms may expose different records and quality. Access is organized into user-visible capability groups with a concrete product use, independent denial handling, and read-only defaults.

## Core health

- Sleep sessions and stages.
- Heart rate and resting heart rate.
- HRV RMSSD.
- Respiratory rate.
- Oxygen saturation.
- Blood pressure.
- Body and skin temperature where supported.

## Daily movement

- Steps.
- Distance.
- Floors climbed and elevation gained.
- Active and total calories.
- Exercise sessions, routes, speed, power, and cycling cadence.
- VO2 max.
- Wheelchair pushes where applicable.

Aggregate APIs are preferred for daily movement totals because Health Connect applies the user's Activity app priority and deduplication rules.

## Body measurements

- Weight, height, body fat, lean body mass, bone mass, and basal metabolic rate.

## Nutrition and hydration

- Nutrition records and hydration.

This group is opt-in and remains separate from general health because it creates a different tracking and consent expectation.

## Reproductive and sexual health

- Menstruation, ovulation, cervical mucus, intermenstrual bleeding, basal body temperature, and sexual activity.

This group is highly sensitive, opt-in, hidden until a relevant feature is intentionally enabled, and never bundled into general onboarding.

## Mindfulness and wellness

- Mindfulness sessions and other wellness records supported by the device/API feature set.
- User-reported mood or stress remains a WholeMate input with its own privacy contract; it is not silently inferred from Health Connect.

## Medical records

Personal Health Records use a distinct feature and permission surface. Allergies, conditions, medications, laboratory results, vaccines, visits, procedures, pregnancy, social history, practitioners, and clinical vital signs are out of the initial wellness scope. They require feature detection, explicit medical use cases, and separate product/legal review.

## Permission behavior

- Request a group only when the user opens its feature and understands the value.
- A denied optional group must not block already-authorized groups.
- Re-check grants before every read because permissions can be revoked.
- Provide Manage access in Settings.
- Keep queries read-only until a write use case is separately approved.
- Default reads cover the platform's permitted history window; older history requires a separate history permission and explanation.

## Candidate evaluation order

Items below are candidates subject to the product gate above, not committed roadmap milestones.

1. Core health already in use on Android: Sleep, HR, HRV, Respiratory, Activity.
2. Daily movement: Steps aggregate implemented; Distance and Calories aggregates next.
3. Resting HR, SpO2, Weight, Body Fat, and VO2 max.
4. Remaining activity/body capabilities behind consent groups.
5. Nutrition, reproductive health, and medical records only after dedicated product decisions.
