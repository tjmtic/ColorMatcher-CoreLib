---
description: Run the dual-store release phase and the release gate
---

Telemetry (non-blocking, silent no-op when unconfigured): before step 1 run `bash .claude/scripts/agent-event.sh run-start release-engineer "release"`; as the last action of this command run `bash .claude/scripts/agent-event.sh run-end release-engineer succeeded` — or `run-end release-engineer failed` if the phase could not complete.

Precondition: phase is `release`; QA ship* accepted, perf/a11y clear/accepted, security clear/skipped.

1. **Needs-macOS backlog check first**: list every unresolved item (iOS test rows, VoiceOver scripts, perf measurements, upgrade-path install). The user runs them (or a macOS CI lane does) or explicitly waives each — record waivers with who/why. Do not present the gate with silent unknowns.
2. Launch `release-engineer`; when `11-release-plan.md` exists, launch `tech-writer` (parallel-safe).
3. Launch `electrician` when the milestone consumes a service (`.claude/docs/circuits.md`): the app-side circuits — real device on a real network reaching the deployed service URL, background push arriving on a real Android and a real iPhone, cold-start stream reconnect, client config/secrets on-device — into `docs/workflow/<milestone>-09-circuits.md`. `dead` ⇒ `senior`/`ops` tasks, no gate; `conducts` ⇒ its substitution ledger joins the gate for acceptance by name.
4. Verify user-only pre-flight: Play data-safety deltas, App Store privacy labels, signing secrets both stores, demo account for App Review.
5. **GATE — release** (two sub-decisions, approved independently):
   - **Play**: track plan, staged % schedule, halt criteria → approve / changes / hold.
   - **App Store**: submission package, review notes, phased-release plan, rejection contingency → approve / changes / hold.
   Never publish, promote, or submit yourself — user executes or explicitly authorizes each step. If one store is held, apply the roadmap's lag policy (hold both vs ship-dark) and record the decision.
6. User confirms rollout(s) started ⇒ record `state.json.gates.release` (with per-store status), phase → `retro`. Restate both stores' halt/pause criteria.
