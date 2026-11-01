---
description: Run the requirements phase (signal → testable requirements with platform matrix) and its gate
argument-hint: [optional: extra signal/context]
---

Telemetry (non-blocking, silent no-op when unconfigured): before step 1 run `bash .claude/scripts/agent-event.sh run-start requirements-analyst "requirements"`; as the last action of this command run `bash .claude/scripts/agent-event.sh run-end requirements-analyst succeeded` — or `run-end requirements-analyst failed` if the phase could not complete.

Precondition: phase is `requirements`.

1. Gather signal: initiative, $ARGUMENTS, session context, `00-context.md` if present.
2. Launch `requirements-analyst`.
3. Blocking questions returned ⇒ relay, collect, relaunch.
4. Present: FR list with platform-matrix decisions (parity vs divergence), mobile-sweep highlights (offline, process-death/backgrounding, permissions, ATT, store policy), per-platform NFR budgets, out-of-scope, open questions.
5. **GATE — requirements**: approve / changes / reject. Approve ⇒ record, phase → `roadmap`.
