---
description: Run KMP codebase reconnaissance (brownfield phase 0)
---

Telemetry (non-blocking, silent no-op when unconfigured): before step 1 run `bash .claude/scripts/agent-event.sh run-start kmp-codebase-analyst "recon"`; as the last action of this command run `bash .claude/scripts/agent-event.sh run-end kmp-codebase-analyst succeeded` — or `run-end kmp-codebase-analyst failed` if the phase could not complete.

Precondition: phase is `recon`.

1. Launch `kmp-codebase-analyst` with the initiative from `state.json`.
2. Record env capabilities in `state.json.env` (K/N toolchain? macOS/Xcode?) based on what the analyst could actually run.
3. Summarize Risks and Open Questions (≤10 lines) — lead with toolchain-matrix problems and any missing macOS CI lane.
4. Phase → `requirements`, append history.
