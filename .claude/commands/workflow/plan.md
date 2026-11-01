---
description: Run the roadmap phase (paired release trains for Play + App Store)
---

Telemetry (non-blocking, silent no-op when unconfigured): before step 1 run `bash .claude/scripts/agent-event.sh run-start product-manager "plan"`; as the last action of this command run `bash .claude/scripts/agent-event.sh run-end product-manager succeeded` — or `run-end product-manager failed` if the phase could not complete.

Precondition: phase is `roadmap`, requirements gate approved.

1. Launch `product-manager`.
2. Present milestones with FRs, paired track targets (Play/TestFlight), demo scripts, dark-ship flag list, and the one-store-lags policy (≤12 lines). Flag "Proposed additions" for decision.
3. Confirm ordering + lag policy with the user (lightweight; design gate covers the plan).
4. `milestone` → "M1", phase → `ux`.
