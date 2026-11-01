---
description: Initialize the KMP agentic SDLC workflow for a new initiative
argument-hint: [one-line description of the initiative]
---

Initialize the KMP workflow for: $ARGUMENTS

1. Create `docs/workflow/` if missing.
2. Detect mode: existing KMP sources (multiplatform plugin in build files) ⇒ `brownfield`; empty/new ⇒ `greenfield`. Confirm with the user. Greenfield also confirm: app name/package/bundle id, Android minSdk (default 26), iOS deployment target (default 15.0), and acceptance of the stack defaults in `.claude/agents/kmp-architect.md`.
3. Record environment capability once: can this machine run K/N compiles? Xcode/simulators? Note in state as `env: {kotlinNative: bool, macos: bool}` — downstream phases use it to decide what gets verified here vs marked "needs macOS".
4. Write `docs/workflow/state.json`:

```json
{
  "initiative": "$ARGUMENTS",
  "mode": "<greenfield|brownfield>",
  "phase": "recon",
  "milestone": null,
  "env": { "kotlinNative": null, "macos": null },
  "gates": { "requirements": null, "design": null, "release": null },
  "tasks": [],
  "history": [{ "ts": "<now>", "event": "initialized" }]
}
```

5. Brownfield: launch `kmp-codebase-analyst` → `00-context.md`, then phase → `requirements`. Greenfield: phase → `requirements` (first build task scaffolds the CMP project per architect design).
6. Ask the user for all raw signal (problem, users, per-platform expectations, constraints), then point at `/workflow:next`.
