# WholeMate UI System

This file is the implementation contract for visual work. A screen change is incomplete when its values conflict with this document without updating the document in the same commit.

## Product character

WholeMate is calm, precise, supportive, and human. It must not look clinical, gamified, alarmist, or exclusively athletic. Health facts, user reports, calculations, and AI interpretations must remain visually distinguishable.

The existing RunMate mobile visual language is an approved baseline: pale health canvas, teal gradient hero, three circular metrics, high-radius white cards, compact uppercase evidence labels, and stable bottom navigation. Compose may refine accessibility and performance while retaining this familiarity.

## Logo and launcher

- Reuse the approved RunMate launcher artwork from `runmate-mobile`; do not redraw or AI-generate the mark.
- Android launcher resources live in `app/src/main/res/mipmap-*`.
- The logo identifies the product. It must not be used as a generic loading spinner or decorative watermark.
- Minimum clear space around an in-product logo is one quarter of its rendered width.
- Do not recolor the logo unless a formally approved monochrome asset is added.

## Layout

- Base spacing unit: `4dp`.
- Screen horizontal padding: `20dp` on phones; cap content width at `600dp` when tablet support is added.
- Primary vertical rhythm: `16dp`; dense related content may use `8dp` or `12dp`.
- Card inner padding: `18dp`; hero padding: `22dp`.
- Standard card radius: `22dp`; hero radius: `30dp`; chips and progress tracks use pill shapes.
- Minimum interactive target: `48dp`; never rely on the visible icon size as the touch target.
- Edge-to-edge is required. System bars, display cutouts, keyboard, and bottom navigation insets must be consumed once at the app shell.

## Typography

The prototype uses the Android system sans font to avoid an unverified font-loading cost. IBM Plex Sans Thai may be introduced only after Thai glyph rendering, APK size, and cold-start impact are measured.

| Role | Size | Weight | Line height | Usage |
|---|---:|---:|---:|---|
| Display | 28sp | 800 | 34sp | One primary page greeting/title |
| Hero title | 20-24sp | 800 | 26-30sp | At-a-glance body picture headline |
| Section title | 17-18sp | 800 | 24sp | Card title |
| Body | 13-14sp | 400-600 | 19-21sp | Explanations and values |
| Label | 10-11sp | 700-800 | 14sp | Eyebrows, chart labels, badges |
| Metric | 22-28sp | 800 | compact | Scores and selected chart values |

Rules:

- Respect system font scaling. Do not force text into fixed-height containers.
- Use sentence case for prose. Uppercase is reserved for short labels under 30 characters.
- A metric must include its unit or scale near the value.
- Thai and English copy must not be manually letter-spaced at body sizes.

## Color and meaning

- Ink `#142A46`: primary text on light surfaces.
- Muted `#667A91`: secondary text; never use for critical facts below accessible contrast.
- Canvas `#F3F8FC`: Today background.
- Ocean `#197C9B`: primary action and selected navigation.
- Dark canvas `#101A17`: Health diagnostic background.
- Green `#75E6A4`: connected/success, not a recovery judgment by itself.
- Gold `#FFD26F`: attention/preview, not an error.
- Measured badge: green-tinted surface. Derived preview badge: gold-tinted surface.

No score color may be the only carrier of meaning; pair it with a label and explanation.

## Motion and haptics

- Entry animation target: 220-900ms depending on semantic weight; never delay data availability for animation.
- Haptics are reserved for discrete selection, sheet open, chart-day change, and confirmed action.
- Scrolling never emits haptics continuously.
- Honor reduced-motion settings before production promotion.
- Bottom sheets must close with system Back and preserve the underlying screen state.

## Measured versus derived UI

- `Measured`: directly read from Health Connect or another identified source.
- `Derived`: produced by the versioned RunMate model.
- `Preview`: mock or experimental output; it must never masquerade as measured data.
- `Unavailable`: honest missing state. Never synthesize HRV or respiratory rate from insufficient inputs.
