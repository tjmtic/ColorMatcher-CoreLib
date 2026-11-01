# UX Spec — {initiative} / {milestone}

## Navigation Map
{destinations, args, deep/universal links; back semantics: predictive back (A) and swipe-back (i) both defined per screen}

## Screens
### S1: {name}  (FRs: {list})
- Purpose / primary action:
- Layout (Material 3 + ASCII wireframe):
- States: **loading** | **content** | **empty** | **error**(+retry) | **offline**
- Platform adaptation: {default "none" — every divergence gets a reason: safe areas, haptics, pickers, share sheet, keyboard}
- Inputs & validation (+ error copy):
- A11y (feeds TalkBack AND VoiceOver): descriptions, headings, traversal, targets ≥48dp/44pt, state announcements
- Copy (final strings → Compose Resources):

## Theming
{M3 tokens; dynamic color (A) + static fallback palette (i); dark both; Dynamic Type / font-scale behavior}

## Component Inventory
| Component | Reused / New | Native escape hatch tempted? (flag for architect) |
|---|---|---|
