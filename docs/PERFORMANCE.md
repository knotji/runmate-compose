# Performance Contract

## Budgets

These are initial Android/Samsung budgets and must be replaced with measured baselines before production promotion. Each future platform host defines and measures equivalent budgets on representative hardware; Android results are not evidence of iOS performance.

| Interaction | Target |
|---|---:|
| Shell visible after process start | under 500ms warm, under 1200ms cold |
| First actionable Today content | under 1000ms with valid cache |
| Health refresh feedback | under 100ms |
| Navigation response | under 100ms |
| Bottom sheet first frame | under 150ms |
| Scroll and animation | no sustained jank; target 60Hz/120Hz device cadence |

## Rules

- Platform health-provider and Supabase access run off the rendering path.
- Query only fields and time ranges needed by the screen.
- Keep previous data visible during refresh.
- Avoid allocating formatters, brushes, and large collections per animation frame.
- Use stable immutable UI models; do not expose SDK records directly to composables.
- Lazy containers are required for variable-length screens.
- Images need explicit size bounds and must not be decoded at source resolution when rendered smaller.
- Logging must exclude health payloads, tokens, user identifiers, and signed URLs.

## Measurement

`PerformanceMonitor` logs named duration measurements without payloads. Current trace: `health_dashboard_load`.

Before production consideration, add:

- Android Macrobenchmark startup and frame-timing tests;
- Baseline Profiles for Today and Health navigation;
- release-build measurements on the target Samsung device;
- Compose recomposition inspection for the Today hero and chart;
- APK size tracking;
- cold/warm Health Connect query timing with record counts but no values.

Debug performance is diagnostic only. Decisions must use release-like builds without debugger overhead. Shared logic benchmarks complement but never replace host-level startup, frame, lifecycle, and provider measurements.
