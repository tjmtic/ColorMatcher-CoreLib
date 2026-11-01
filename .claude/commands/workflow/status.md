---
description: Show workflow state, gates, per-platform verdicts, and what happens next
---

Read `docs/workflow/state.json` and artifacts. Report concisely:

1. Initiative, mode, phase, milestone, env (macOS available?).
2. Gates: approved-by+date or pending.
3. Task board in build/review: counts by status and by platform lane (shared/android/ios); `blocked` tasks with reasons.
4. Latest verdicts with platform columns: review, test report (note any iOS "needs macOS" backlog), perf, a11y, security.
5. Outstanding "needs macOS" items — these accumulate toward the release gate; surface them every time.
6. Exactly what `/workflow:next` does from here.

Do not modify anything.
