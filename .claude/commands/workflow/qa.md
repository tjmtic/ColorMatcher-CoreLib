---
description: Run the dual-platform QA phase
---

Telemetry (non-blocking, silent no-op when unconfigured): before step 1 run `bash .claude/scripts/agent-event.sh run-start qa-engineer "qa"`; as the last action of this command run `bash .claude/scripts/agent-event.sh run-end qa-engineer succeeded` — or `run-end qa-engineer failed` if the phase could not complete.

Precondition: phase is `qa`.

1. Launch `qa-engineer` (pass `state.json.env`).
2. Verdict from `07-test-report.md`:
   - `ship` ⇒ phase → `quality`.
   - `ship-with-known-issues` ⇒ present per-platform issue list; user accepts (→ `quality`) or fixes (→ tasks, phase → `build`).
   - `no-ship` ⇒ defects → tasks (role+platform routed), phase → `build`, present defect table.
3. Always show: FR matrix (Android|iOS columns), parity findings, and the needs-macOS backlog with its manual scripts — remind the user these must clear (run or waive) before the release gate.
