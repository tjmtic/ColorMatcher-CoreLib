---
description: Run the performance pass (per-platform budgets)
---

Telemetry (non-blocking, silent no-op when unconfigured): before step 1 run `bash .claude/scripts/agent-event.sh run-start performance-specialist "perf"`; as the last action of this command run `bash .claude/scripts/agent-event.sh run-end performance-specialist succeeded` — or `run-end performance-specialist failed` if the phase could not complete.

Precondition: phase is `quality` (runs alongside /workflow:a11y).

1. Launch `performance-specialist` (pass env).
2. Verdict from `08-perf-report.md`:
   - `within-budget` ⇒ mark perf clear; both quality passes clear ⇒ phase → `security`.
   - `over-budget` ⇒ present the per-platform budgets table; user: fix now (→ `build`) or accept overage (record who/which rows).
3. iOS rows marked needs-macOS join the release-gate backlog; simulator/emulator numbers flagged directional.
