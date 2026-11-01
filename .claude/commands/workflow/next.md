---
description: Advance the workflow (dispatcher — checks gates, launches the right phase)
---

You are the workflow orchestrator. Read `docs/workflow/state.json`, dispatch the next phase. Never skip a gate.

| phase | precondition | action |
|---|---|---|
| `recon` | mode=brownfield | run `/workflow:recon` |
| `requirements` | recon done (or greenfield) | run `/workflow:requirements` |
| `roadmap` | gates.requirements approved | run `/workflow:plan` |
| `ux` | 02-roadmap.md exists | run `/workflow:ux` |
| `design` | 03-ux.md confirmed | run `/workflow:design` |
| `build` | gates.design approved | run `/workflow:build` |
| `review` | all milestone tasks ≥ in_review | run `/workflow:review` |
| `qa` | review passed | run `/workflow:qa` |
| `integration` | qa verdict ship* AND milestone touches the service or wire contract (else record skip, go to quality) | run `/workflow:integration` |
| `quality` | qa verdict ship*; integration clear or skipped | run `/workflow:perf` and `/workflow:a11y` (independent — parallel agents) |
| `security` | perf & a11y clear/accepted; skippable for no-risk-surface change-sets (ask if unsure) | run `/workflow:security` |
| `release` | all quality verdicts clear/accepted | run `/workflow:release` |
| `retro` | gates.release approved & both rollouts begun (or explicit single-store decision recorded) | run `/workflow:retro` |
| `done` | retro written | offer next milestone: phase → `ux` for M<n+1> |

Platform fan-out rule (applies in build/review/qa): a `shared` task is not verifiably complete until both targets compile against it; iOS verification that this environment can't run becomes a tracked "needs macOS" item, not a silent pass. The release gate cannot be presented while unresolved needs-macOS items exist — they must be executed (by the user or a macOS CI lane) or explicitly waived at the gate.

Gate protocol (requirements, design, integration, release): present ≤10-line summary + open questions; ask approve / changes / reject; approve ⇒ record `{by, ts}`, advance; changes ⇒ re-run phase agent with feedback. Never self-approve. The integration gate is conditional: it is presented only when the milestone changed the wire contract — an unchanged contract passes without a stop.

Verdict-driven phases route back to `build` per their commands. Precondition fails ⇒ say what's missing, stop. Append history on phase change.
