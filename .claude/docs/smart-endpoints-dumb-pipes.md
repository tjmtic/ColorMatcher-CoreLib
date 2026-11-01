# Smart endpoints, dumb pipes — the checklist

The principle: the two ends of a seam (an app, a service) own every rule;
what runs between them only carries facts. A pipe that "knows" something is
a third endpoint nobody designed, tested, or owns — and the bug class it
breeds (both sides correct, the seam wrong) is exactly the one the
integration phase exists to catch. The plumber checks the pipes BEFORE the
joint run so the run proves behavior, not plumbing.

## What a pipe may contain (the allowed list)

Transport, encoding, and the things that protect the transport:
- serialization of contract types (no transformation beyond encode/decode)
- authentication and authorization of the CALL (who may speak), rate
  limiting, TLS, compression, connection lifecycle (reconnect, drain)
- delivery mechanics per the lifecycle table (a leg fires or it does not —
  the DECISION comes from the table/planner at the endpoint, the pipe
  executes it)
- observability of the pipe itself (bytes, latency, refusals, counters)

Anything not on this list that lives in a transport layer, interceptor,
serializer, gateway, push adapter, or client transport wrapper is a finding.

## The rules, per side

**Service (the smart endpoint for state)**
- The service is the single source of truth for state: it reduces, orders
  by its own clock, converges, expires. Nothing between it and the app
  reinterprets a fact.
- Idempotency is decided at the endpoint by the client-generated key; the
  pipe never invents keys, never dedupes on its own, never retries with
  semantics.
- Errors are a taxonomy (codes + retryable), not prose: the client may only
  branch on codes. A message string a client must parse is a smart pipe.
- Push payloads are NOTIFICATIONS (`payloadMinimum`), never state. State
  travels on the connected leg or is fetched from the anchor — a push that
  carries state is a second, unversioned state channel.

**App (the smart endpoint for intent)**
- The app owns intent and presentation: when to OPEN/RENEW/CLOSE, cadence
  (renew at ttl/3), what to show. It generates event ids at the edge.
- The app RECONCILES to the service's anchor (`state` in ingest results,
  snapshots on the stream); it never replays events as commands and never
  re-derives server rules (no client-side expiry deciding "they stopped
  looking" — the anchor says so).
- The app tolerates the additive-v1 rule (unknown types/fields ignored) and
  branches on error CODES only.

**The contract (the only coupling)**
- Types only. A function in the contract module that both sides CALL is
  shared logic across the pipe — a smart pipe wearing a module name. Pure
  encode/decode helpers are fine; rules are not.
- No side depends on the other's internals: URLs beyond the contract's
  routes, timing assumptions, log formats, counter names, metrics endpoints.
  A test that reads `/metrics-lite` to observe the other side is a hidden
  channel — allowed in tests only if labeled a substitution, never in
  product code.
- Versioning is additive and unknown-tolerant on both sides; the service
  learns a new type before any client sends it.

## What the plumber produces

`NN-plumbing.md` on each side (service: `docs/workflow/08-plumbing.md`;
app: `<milestone>-08-plumbing.md`), one table:

| pipe | what it carries | logic found in it | belongs to | severity | fix |

plus an "allowed-list audit" section listing every transport component and
confirming each stays inside the allowed list, and a verdict: `clear` (no
logic in any pipe; endpoints own their rules) or `fix` (findings filed as
role-tagged tasks — a rule that lives in a pipe moves to the endpoint that
owns it; never to the other pipe).

Severity for THIS seam: High = a rule in a pipe that both sides' tests
cannot see (the seam-only bug class); Medium = duplicated rule (both ends
implement the same logic — will drift); Low = hygiene (a hidden channel in
tests, a message string branched on).
