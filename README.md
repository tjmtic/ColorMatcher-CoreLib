# kmp-app-template

The starting point for every KMP app in the fleet: Compose Multiplatform on Android and iOS,
the fleet style standard, CI, ViewPoint library wiring, and the manifest the studio builds from.
One command gives a new app that builds, passes its checks and runs on both platforms, so no
agent ever has to invent a Gradle project.

## Start an app

```bash
gh repo create owner/my-app --template tjmtic/kmp-app-template --public --clone
```

```bash
cd my-app && scripts/rename.sh "My App" com.example.myapp owner/my-app && git commit -am "Rename from template" && git push
```

```bash
git push origin main:develop && scripts/protect-branches.sh owner/my-app
```

`develop` is where the studio merges; `main` is the release branch. Branch protection is not
copied from a template, so `protect-branches.sh` sets it: both CI lanes on `main`, the Linux
lane on `develop`, no required reviews (the studio merges on green CI; the human gate is the
`develop` → `main` PR).

The studio merges only where the app opts in: set `"autoMerge": true` in `.studio/target.json`
(the template ships `false`), and the studio needs `EDIT_LOOP_AUTO_MERGE=1`. It then squash-merges
its own PR into `develop` once every CI check is green, its reviewer step accepted, and no review
requests changes. It never merges into `main`: a release is you merging the `develop` → `main`
promotion PR, and the release train departs from `main`.

## What is in it

| Path | What |
|---|---|
| `composeApp/` | The app: shared Compose UI (`App.kt`), Android `MainActivity`, iOS `MainViewController`; strings in Compose Resources |
| `shared/` | Pure logic, no UI or platform types; `commonTest` runs on the JVM (fast) and the iOS simulator |
| `iosApp/` | Thin Swift shell hosting the Compose UI. `project.yml` is the XcodeGen spec; the generated `iosApp.xcodeproj` is committed, so building needs only Xcode |
| `detekt-rules/` | Project rules for detekt, unit-tested (`ForbiddenNotNullAssertion`: `!!` is a review blocker) |
| `config/detekt/detekt.yml` | detekt at zero issues, plus the Compose rules; no baseline |
| `.studio/target.json` | How the studio works on this repo: sections, test tasks, merge branch (`develop`), release branch (`main`) |
| `.claude/` | The kmp-agentic-sdlc workflow set, at the commit in `.claude/.workflow-set-sha` |
| `scripts/rename.sh` | Turns the template into your app |
| `scripts/protect-branches.sh` | Branch protection for the new repo (templates do not carry it) |

Versions live only in `gradle/libs.versions.toml`: AGP 8.13.2 (in lockstep with the fleet —
a composite build tolerates one AGP), Kotlin 2.4.10, Compose Multiplatform 1.11.1. No
`iosX64`: Compose Multiplatform 1.11 publishes none, so the simulator builds arm64 only.

## Checks

```bash
./gradlew ktfmtCheck :detekt-rules:test detekt :shared:jvmTest :composeApp:testDebugUnitTest :composeApp:assembleDebug
```

```bash
./gradlew :shared:iosSimulatorArm64Test
```

CI runs the first line plus an iOS compile check on Linux for every push and PR, and on macOS
the iOS simulator tests and an unsigned archive for pushes to `main`/`develop` and PRs into
`main`. Format before you are done with `bash .claude/scripts/format-changed.sh`
(`--install-hook` once per checkout installs it as a pre-commit hook).

## ViewPoint libraries

`settings.gradle.kts` lists them, commented out; `gradle/libs.versions.toml` has their
coordinates (`viewpoint-*`). Uncomment the ones you use and depend on them as usual. A sibling
checkout (`../ViewPoint-CameraLib`, …) is compiled in as a composite build; without one — CI, a
fresh clone — or with `-PuseLocalLibs=false`, the published artifact comes from GitHub Packages,
which needs a token with `read:packages` (`GPR_USER`/`GPR_KEY`, or `gpr.user`/`gpr.key` in
`~/.gradle/gradle.properties`). The sensor library is not published: sibling checkout only.

## iOS signing

`DEVELOPMENT_TEAM` is empty on purpose. Set it in `iosApp/project.yml` and run
`xcodegen generate` in `iosApp/`, or pass `DEVELOPMENT_TEAM=…` to `xcodebuild`.
