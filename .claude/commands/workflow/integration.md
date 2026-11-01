---
description: Run the integration phase — prove the app and the companion service work TOGETHER, and gate any wire-contract change
---

Telemetry (non-blocking, silent no-op when unconfigured): before step 1 run `bash .claude/scripts/agent-event.sh run-start backend-engineer "integration"`; as the last action of this command run `bash .claude/scripts/agent-event.sh run-end backend-engineer succeeded` — or `run-end backend-engineer failed` if the phase could not complete.

Precondition: phase is `integration` — the milestone touches the service or the wire contract. A milestone that touches neither skips this phase entirely (record the skip with its reason); do not run it for completeness.

This phase exists for one bug class: both sides pass their own tests and disagree on the wire. Per-repo suites cannot catch it, so nothing here re-runs what build/review/qa already proved — every check below exercises the app and the service against EACH OTHER.

1. Launch `plumber` FIRST — the pipe audit (`.claude/docs/smart-endpoints-dumb-pipes.md`) across this repo's client and the service repo, producing `docs/workflow/<milestone>-08-plumbing.md`. Verdict `fix` ⇒ the seam does not run: findings filed (`senior`/`junior`) for the app side, or handed to the service repo's set for theirs; phase → `build`. `clear` ⇒ continue.
2. Stand up the service from the milestone's build (local instance, config validated at boot). If it cannot boot from a clean checkout, that is verdict `fix` already — stop and route.
3. Launch `backend-engineer` to replay the service's in-process API tests against the RUNNING instance — the rehearsal becomes the performance. Same DTO module, real transport.
4. Launch `senior-kmp-engineer` to run the app's client contract tests against the same instance, then exercise the milestone's event flows end to end: ingest → publish → app receives, including the failure legs (service down, duplicate delivery, stale contract version).
5. Contract check: diff the shared contract module against the last approved version. Unchanged ⇒ note it and continue.
6. **GATE — integration** (only when the contract diff is non-empty): present the wire-contract diff in ≤10 lines — new/changed endpoints, DTO changes, error-taxonomy changes, versioning impact on already-shipped app versions. Ask approve / changes / reject. Approve ⇒ record `{by, ts}`; changes ⇒ route the contract task back with feedback. A contract change never passes silently — that silence is the bug class this station exists to kill.
7. Verdict: `clear` (all joint checks pass, gate approved or not needed) ⇒ record, phase → `quality`. `fix` ⇒ file the failing checks as tasks tagged `backend` or `shared` per which side owns the break, phase → `build`. A break at the seam with unclear ownership goes to BOTH agents as one task — the seam itself has no owner by design, and splitting it invents one.
