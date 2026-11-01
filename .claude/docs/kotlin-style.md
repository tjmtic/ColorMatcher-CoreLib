# Kotlin style — a tool, not a review subject

Formatting is decided by a formatter with no options, linting by detekt at
zero issues, and neither is something an engineer or a reviewer spends a
turn on. This is the fleet standard every repo running this workflow set
converges on; the harness's `/codebases` board reports drift from it.

Adapted from Block's migration (engineering.block.xyz, "Adopting ktfmt and
detekt"): one formatter that fixes everything it flags, linting split out
into detekt, custom rules as tested detekt rules, baselines for rule
rollout, and a changed-files pre-commit hook. Their style choice (Google
internal, 2-space) is swapped for kotlinlang (4-space, 100 columns) because
that is what Google's *Android* Kotlin style guide, Android Studio's default
formatter, AndroidX, and every repo in this ecosystem already use.

## The standard

| concern | tool | pinned |
|---|---|---|
| formatter | ktfmt, **kotlinlang style**, via `com.ncorti.ktfmt.gradle` | plugin 0.27.0 (engine 0.64) |
| linter | detekt, `buildUponDefaultConfig`, `maxIssues: 0` | 1.23.8 |
| Compose rules | `io.nlopez.compose.rules:detekt` | 0.6.6 |
| project rules | `:detekt-rules` module, unit-tested, on `detektPlugins` | per repo (ecosystem artifact pending) |
| Android Lint | stays, separate job — it checks platform usage, not style | AGP's |
| **not used** | ktlint (standalone, Spotless-wrapped, or `detekt-formatting`) | — |

Task names the agents rely on: `ktfmtFormat` / `ktfmtCheck` (per source set
in KMP: `ktfmtCheckCommonMain`, …), `ktfmtPrecommit --include-only=…`,
`detekt`, `detektBaseline` (migration only).

## Rules for every agent in this set

1. **Format before you are done.** Run `bash .claude/scripts/format-changed.sh`
   after each increment and before setting `in_review`. Never "match the
   surrounding style" by hand; match structure and naming, let ktfmt own
   layout.
2. **Formatting is never a finding.** Not a nit, not a comment. If a
   reviewer wants to complain about layout, the real finding is that the
   format check is missing from someone's loop.
3. **Never add to a baseline.** `detekt-baseline.xml` is generated once when
   a rule is adopted, by the migration task, and only shrinks afterwards.
   An agent that baselines a failure to get past it has failed the task.
   A baseline diff inside a feature change-set is a review blocker.
4. **Codify the nit.** A style or convention comment you would make twice
   becomes a detekt rule with a unit test in `:detekt-rules`, proposed in
   the review report, not prose in a review.
5. **Rule adoption is its own task**: enable the rule, generate the
   baseline, open the paydown as follow-ups. Never inside a feature task.
6. **Migration PRs are format-only.** A ktfmt rollout touches every line
   and carries no behaviour; it never shares a PR with logic. Record the
   commit in `.git-blame-ignore-revs`.

## Gradle wiring (root `build.gradle.kts`)

```kotlin
plugins {
    alias(libs.plugins.ktfmt) apply false      // ktfmt = { id = "com.ncorti.ktfmt.gradle", version.ref = "ktfmt" }
    alias(libs.plugins.detekt) apply false
}

subprojects {
    apply(plugin = "com.ncorti.ktfmt.gradle")
    extensions.configure<com.ncorti.ktfmt.gradle.KtfmtExtension> { kotlinLangStyle() }

    apply(plugin = "io.gitlab.arturbosch.detekt")
    dependencies {
        "detektPlugins"(libs.detekt.compose.rules)
        "detektPlugins"(project(":detekt-rules"))
    }
    extensions.configure<io.gitlab.arturbosch.detekt.extensions.DetektExtension> {
        source.setFrom(files("src"))          // KMP: walk every source set, not src/main
        config.setFrom(files("$rootDir/config/detekt/detekt.yml"))
        baseline = file("$rootDir/config/detekt/baseline.xml")   // may not exist; that is the goal
        buildUponDefaultConfig = true
        parallel = true
        basePath = rootDir.absolutePath
    }
}
```

`.editorconfig` so the IDE approximates ktfmt while typing (ktfmt itself
reads none of this unless invoked with `--enable-editorconfig`):

```
[*.{kt,kts}]
indent_size = 4
ij_continuation_indent_size = 4
max_line_length = 100
ij_kotlin_allow_trailing_comma = true
ij_kotlin_allow_trailing_comma_on_call_site = true
```

Install the ktfmt IntelliJ plugin and select kotlinlang style; that is the
only IDE-side setting, and it is why the IDE and the CLI stop disagreeing.

## Local loop and CI

- Pre-commit: `bash .claude/scripts/format-changed.sh --install-hook` once
  per checkout. The hook formats only files changed against `main`
  (milliseconds through the ktfmt CLI when installed, the Gradle task when
  not), chunked so a 60k-file tree cannot overflow the argument list.
- CI, every PR, in this order: `ktfmtCheck` → `:detekt-rules:test` →
  `detekt` → Android Lint → tests → `compileKotlinIosSimulatorArm64`.
  Format first because it is the cheapest and the one nobody should have to
  read a log for.

## Why not ktlint, given the reference repos use it

Google's KMP sample (Fruitties) runs Spotless+ktlint; nowinandroid runs
ktlint with an `.editorconfig` that **disables eleven ktlint rules** to make
its own code pass, plus an IDE codestyle XML to keep Android Studio in
agreement. That is the configuration churn and IDE/CLI drift Block left, and
it is the wrong trade for agents: a linter that flags what it cannot fix
produces loops, a formatter that fixes everything produces diffs. Both
references are 4-space / 100 columns, which kotlinlang ktfmt preserves, so
the code shape we copy from them is unchanged; only the enforcement tool is.

## Brownfield migration recipe (one repo at a time, small PRs)

1. **Inventory**: which formatter, which linter, baseline, hook, CI tasks.
   The recon agent reports this as "Style Toolchain"; `/codebases` shows it.
2. **Add ktfmt** (plugin + `kotlinLangStyle()`), keep the old tool for one
   PR so nothing is unguarded.
3. **Format-only PR**: `./gradlew ktfmtFormat`, commit, add the sha to
   `.git-blame-ignore-revs`. No other change in this PR.
4. **Remove the old formatter**: ktlint plugin / Spotless ktlint step /
   `detekt-formatting` from `detektPlugins`, and every ktlint key in
   `.editorconfig`.
5. **detekt to zero**: if detekt is new here, adopt with a baseline in this
   PR and file the paydown; if it exists, confirm `maxIssues: 0` and that
   `:detekt-rules` has tests that CI runs before `detekt`.
6. **Hook + CI**: install the hook, add `ktfmtCheck` to the PR job.
7. **Order across the fleet**: SameMoon (already detekt-only, needs only
   the formatter) → ViewPoint (drop `detekt-formatting`) → the six
   detekt-only libs → the ktlint repos (BackgroundJob, Notification,
   WeatherConditions + CoreLib) → the repos with nothing (SpeedCamera, fuse,
   Sensor). Libraries consumed through `includeBuild` see no behaviour
   change from a format-only PR, so ordering is about review load only.
