# kmp-app-template — project conventions

A new app created from this template: rename it first (`scripts/rename.sh`, see README).
Style is a tool, not a review subject: ktfmt (kotlinlang) + detekt at zero issues — run
`bash .claude/scripts/format-changed.sh` before you are done, never add a detekt baseline.


## Stack
- Kotlin Multiplatform, Compose Multiplatform + Material 3, shared UI on both targets.
- androidx Lifecycle ViewModel (multiplatform) + StateFlow; events up, state down.
- JetBrains Navigation-Compose; Koin DI; Ktor + kotlinx.serialization; SQLDelight or Room KMP; DataStore KMP; Coil 3; Compose Resources.
- iOS: thin Swift shell (entry + platform services only); framework via embedAndSign.
- Version catalog is the only place versions live. Toolchain matrix (Kotlin/CMP/AGP/Xcode) is pinned; upgrades are deliberate, never drive-by.

## Hard rules
- commonMain first: platform code only via the architect's Platform Surface (expect/actual or injected interface). No platform types in common code.
- Every expect has both actuals in the same PR; asymmetric actual semantics is a review blocker.
- Dispatchers injected; no `Dispatchers.Main` literal in common code; no blocking I/O on main.
- Every commonMain change must pass the iOS compile check (`compileKotlinIosSimulatorArm64`) before merge — it's a PR gate.
- Every screen: survives Android rotation + process death AND iOS backgrounding; handles safe areas; back semantics per UX spec on both platforms.
- All user-facing strings via Compose Resources. Touch targets ≥48dp/44pt (stricter wins). Timestamps stored UTC.
- DB schema change ⇒ migration + migration test, same PR (migrations run on both platforms).
- Cross-platform feature flags are the iOS kill switch — risky features ship dark.
- New deep/universal link, exported component, entitlement, or Platform Surface security actual ⇒ security review flag.
- `!!` is a review blocker.

## Testing
- commonTest first (Turbine, fakes, in-memory DB); platform tests only for platform behavior.
- New stateful composable ⇒ Compose UI test. Parity: unspecified platform divergence is a defect.
- "needs macOS" rows are tracked debt — they clear (run or waive) before any release gate.

## Workflow
- Agentic SDLC in `.claude/`. Start: `/workflow:init`. Advance: `/workflow:next`.
- Artifacts in `docs/workflow/`; don't hand-edit `state.json`.

## Style (see `.claude/docs/kotlin-style.md`)
- ktfmt, kotlinlang style, owns layout. Run `bash .claude/scripts/format-changed.sh` before any task is `in_review`; formatting is never a review finding.
- detekt at `maxIssues: 0` is the lint gate; project rules live in `:detekt-rules` with unit tests. ktlint in any form (standalone, Spotless, `detekt-formatting`) is not used here.
- Nobody adds to `detekt-baseline.xml`. A baseline is generated once by a rule-adoption task and only shrinks; a baseline diff in a feature change-set is a blocker.
- A style comment you would make twice becomes a detekt rule + test, not prose.
- Format-only migration commits go in `.git-blame-ignore-revs` and never share a PR with logic.
