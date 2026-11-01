---
description: Run the architecture phase for the current milestone and its approval gate
---

Telemetry (non-blocking, silent no-op when unconfigured): before step 1 run `bash .claude/scripts/agent-event.sh run-start kmp-architect "design"`; as the last action of this command run `bash .claude/scripts/agent-event.sh run-end kmp-architect succeeded` — or `run-end kmp-architect failed` if the phase could not complete.

Precondition: phase is `design`.

1. Launch `kmp-architect` for the milestone.
2. Self-check `04-architecture.md` + `04-tasks.md`: every FR → task; every screen's five states representable in its UiState; Platform Surface section complete with both actuals specified per expect; iOS Shell section enumerated; no dependency cycles; junior tasks S-sized in commonMain/androidMain only; ios-lane tasks tagged senior; migrations planned. Send back before bothering the user.
3. Present: module summary, **Platform Surface** (the expect/actual list — smaller is better, challenge growth), key ADRs, task table (ID, title, role, platform, size, deps).
4. **GATE — design**: approve / changes / reject. Approve ⇒ record, phase → `build`.
