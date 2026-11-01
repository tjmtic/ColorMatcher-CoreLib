---
description: Run the integration review phase (staff review + fix loop)
---

Telemetry (non-blocking, silent no-op when unconfigured): before step 1 run `bash .claude/scripts/agent-event.sh run-start staff-reviewer "review"`; as the last action of this command run `bash .claude/scripts/agent-event.sh run-end staff-reviewer succeeded` — or `run-end staff-reviewer failed` if the phase could not complete.

Precondition: phase is `review`, all milestone tasks `in_review`.

1. Launch `staff-reviewer`.
2. `changes_requested` emitted ⇒ phase → `build`; report which tasks bounced and why (note platform lane); offer `/workflow:build` now.
3. All `done` ⇒ summarize verdicts, residual risks, and the needs-macOS list (≤10 lines), phase → `qa`.
