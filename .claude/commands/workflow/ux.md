---
description: Run the UX phase (shared design + explicit platform adaptations) for the current milestone
---

Telemetry (non-blocking, silent no-op when unconfigured): before step 1 run `bash .claude/scripts/agent-event.sh run-start ux-designer "ux"`; as the last action of this command run `bash .claude/scripts/agent-event.sh run-end ux-designer succeeded` — or `run-end ux-designer failed` if the phase could not complete.

Precondition: phase is `ux`.

1. Launch `ux-designer` for `state.json.milestone`.
2. Self-check `03-ux.md`: every milestone FR has a screen/flow; every screen has all five states AND a platform-adaptation row (even if "no divergence"); back semantics defined for both platforms; dual a11y annotations present; final copy written. Send back if not.
3. Present: navigation map, screen list, and — most importantly — the divergence list (should be short; challenge each entry). Confirm with user.
4. Phase → `design`.
