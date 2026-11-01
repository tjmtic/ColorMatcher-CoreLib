---
description: Run the implementation phase — dispatch platform-tagged tasks to KMP engineers
argument-hint: [optional: specific task IDs]
---

Telemetry (non-blocking, silent no-op when unconfigured): before step 1 run `bash .claude/scripts/agent-event.sh run-start senior-kmp-engineer "build"`; as the last action of this command run `bash .claude/scripts/agent-event.sh run-end senior-kmp-engineer succeeded` — or `run-end senior-kmp-engineer failed` if the phase could not complete.

Precondition: phase is `build`, design gate approved.

1. Runnable = `todo`/`changes_requested` with deps `done`/`in_review`. Restrict to $ARGUMENTS if given.
2. Dispatch by role tag: `junior-kmp-engineer` / `senior-kmp-engineer` (ios-lane tasks always senior). Pass task ID, contract excerpt, file list, platform tag, and `state.json.env` so the agent knows what it can verify here.
3. Parallelism: independent screens and independent lanes parallelize; both actuals of one expect go to one agent (or strictly dep-linked); scaffold task (greenfield) blocks everything.
4. Fan-out bookkeeping: any commonMain change must log the iOS compile check result (or "needs macOS") in the build log — collect these into a running needs-macOS list in state.
5. `blocked` ⇒ answer from artifacts if unambiguous, else surface to user; redispatch.
6. Before handing to review: `bash .claude/scripts/format-changed.sh --check`. If dirty, run the formatter, commit as its own change, and note which task skipped the loop in the build log — formatting is automated, not a bounce.
7. All milestone tasks `in_review` ⇒ phase → `review`. Else report board by lane.

Greenfield note: first task is the CMP scaffold (Gradle + version catalog, composeApp module with android/ios targets, thin Swift shell + Xcode project, theme, navigation skeleton, ktfmt (kotlinlang) + detekt + `:detekt-rules` + pre-commit hook per `.claude/docs/kotlin-style.md`, CI skeleton incl. `ktfmtCheck`, `detekt` and the iOS compile check) — senior, everything depends on it.
