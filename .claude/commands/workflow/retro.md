---
description: Run the retrospective and fold lessons back into project context
---

Telemetry (non-blocking, silent no-op when unconfigured): before step 1 run `bash .claude/scripts/agent-event.sh run-start retro-analyst "retro"`; as the last action of this command run `bash .claude/scripts/agent-event.sh run-end retro-analyst succeeded` — or `run-end retro-analyst failed` if the phase could not complete.

Precondition: phase is `retro`.

1. Launch `retro-analyst`. Pass post-release signal if the user has it (per-store crash clusters, Vitals/Organizer, halts/pauses, review rejections).
2. Present force-ranked proposals; apply accepted ones. Pay special attention to needs-macOS items that became defects — that's the argument for a macOS CI lane; surface it if the pattern shows.
3. Phase → `done`. Another milestone ⇒ offer: `milestone` → next, phase → `ux`.
