---
description: Run the security review phase (skippable for no-risk change-sets)
---

Telemetry (non-blocking, silent no-op when unconfigured): before step 1 run `bash .claude/scripts/agent-event.sh run-start security-reviewer "security"`; as the last action of this command run `bash .claude/scripts/agent-event.sh run-end security-reviewer succeeded` — or `run-end security-reviewer failed` if the phase could not complete.

Precondition: phase is `security`.

1. Skip check: no changes to link/input handling, storage, auth, network config, Platform Surface security actuals, manifest/entitlements, or dependencies ⇒ propose skip. Agreed ⇒ `10-security.md` = "skipped: <reason>", phase → `release`.
2. Else launch `security-reviewer`.
3. `clear` ⇒ `release`. `clear-after-fixes` ⇒ tasks, → `build`, re-review after. `block` ⇒ present findings, → `build`.
