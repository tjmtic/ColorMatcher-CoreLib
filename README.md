# ColorMatcher-CoreLib

The domain core of **ColorMatcher**, an app that matches polymer clay colors to Pantone and hex
color schemes, and back again. Pure Kotlin Multiplatform (Android, JVM, iOS), no UI. The app itself
is private (tjmtic/ColorMatcher) and consumes this library.

## What is in it

| Package | What |
|---|---|
| `color/` | `Srgb` (hex in/out), sRGB → CIELAB (D65), `deltaE2000` (CIEDE2000) |
| `palette/` | `Swatch`, `Palette` (`CLAY` / `SCHEME` / `LICENSED`), `parseSwatchLines` for `name,#hex[,code]` lists |
| `match/` | `nearest(target, palette)`: the closest swatches by ΔE00, with a `Closeness` band |

One call serves both directions: a sampled clay against a Pantone or hex palette ("what color is
this clay?"), and a hex target against a clay palette ("which clay is this?").

**Pantone data is not included and never will be.** Pantone's color library is licensed; users who
hold a license import their own palette on their device as a `LICENSED` palette.

## Checks

```bash
./gradlew ktfmtCheck :detekt-rules:test detekt :core-lib:jvmTest :core-lib:testDebugUnitTest
```

```bash
./gradlew :core-lib:iosSimulatorArm64Test
```

## Consuming it

`com.abyxcz.colormatcher.core:core-lib` is published to GitHub Packages on a `v*` tag
(`.github/workflows/release.yml`). A sibling checkout at `../ColorMatcher-CoreLib` is compiled in as a
composite build instead.
