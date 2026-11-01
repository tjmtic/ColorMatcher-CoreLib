# Architecture — {initiative} / {milestone}

## Module Design
{modules/source sets touched or created; dependency rules; commonMain share target}

## UI Contracts (per screen)
### {Screen}
```kotlin
data class {X}UiState(…) // all five UX states representable
sealed interface {X}Event
```
- ViewModel deps, state-restoration strategy (Android process death / iOS backgrounding — explicit per platform)

## Data Contracts
{repository interfaces; DB schema + migrations (run on BOTH platforms); DTOs/mappers; error taxonomy; sync}

## Platform Surface (keep small — every entry justified)
| expect / interface | androidMain actual | iosMain actual | Task(s) |
|---|---|---|---|

## iOS Shell (enumerated responsibilities)
{entry, ComposeUIViewController host, platform services, push registration — nothing else}

## Cross-cutting
{navigation graph, Koin modules, cross-platform feature flags, Compose Resources}

## ADRs
### ADR-1: {decision} — Context / Options / Decision / Consequences
{native escape hatches (UIKitView/AndroidView) ALWAYS get an ADR}

## Test Strategy
{commonTest first; platform tests only for platform behavior; what needs macOS}

## Traceability
| FR | Screen | Task(s) | Contract | Platform lane |
|---|---|---|---|---|
