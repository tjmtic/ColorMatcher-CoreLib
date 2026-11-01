# Requirements — {initiative}

## Problem
{who hurts, how, evidence}

## Functional Requirements
### FR-1: {title}
As a {persona}, I want {capability} so that {outcome}.
- Platforms: {both | android | ios} · Parity: {required | divergence OK: reason}
- AC-1.1: Given {…} When {…} Then {…}

## Mobile Requirement Sweep (decide every row; split cells where platforms differ)
| Concern | Android | iOS |
|---|---|---|
| OS floor / devices / orientation | minSdk … | deployment target … |
| Offline & sync | | |
| Process death / backgrounding-jetsam | | |
| Permissions + denial (incl. ATT) | | |
| Deep/universal links, interruptions | | |
| Localization / RTL / dark / font scaling | | |
| Accessibility (TalkBack / VoiceOver operable) | | |
| Store policy (data safety / privacy labels, sign-in rules) | | |

## Non-Functional Requirements (per platform)
| ID | Category | Android target | iOS target |
|---|---|---|---|
| NFR-1 | Cold start | ≤ {n} ms p90 on {device} | ≤ {n} ms p90 on {device} |
| NFR-2 | Jank | ≤ {n}% janky frames | ≤ {n}% |
| NFR-3 | Size | AAB ≤ {n} MB | ipa ≤ {n} MB |
| NFR-4 | Stability | crash-free ≥ {n}% | crash-free ≥ {n}% |

## Out of Scope
## Open Questions & Working Assumptions
