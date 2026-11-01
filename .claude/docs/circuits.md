# Circuits — does it conduct?

Everything before release proves the code: QA proves the contract,
integration proves both sides agree on the wire, the plumber proves the
logic sits where it belongs. None of them prove that the DEPLOYED thing,
with REAL credentials, over REAL networks, to REAL devices, actually
conducts. That is the electrician's question, and it is asked with a meter,
not a review: one action at one end, one observed effect at the other,
under real power.

## The circuit inventory — derived, never hand-listed

Every external connection the system depends on to work in the world,
enumerated from: the architecture's config surface, the deploy config, the
client's `ServiceClientConfig`, and the lifecycle table's delivery legs.
Typical circuits for an app ⇄ service seam:

| circuit | switch | light |
|---|---|---|
| device → service URL | from a real device on a real network, `GET /health` | 200 through TLS / DNS / ingress |
| cold start | scale to zero, then open the connected stream | snapshot arrives inside the platform's stream timeout |
| service → durable store | write through the real store, restart the revision, read back | the thing the operator floor says must survive (push tokens) survives |
| service → push leg → real device (per platform) | register the device's real token, trigger a push-worthy transition from the other device, phone backgrounded | the notification arrives; `deliveries_out.<leg>` moved |
| secrets | boot the deployed revision | fail-fast names nothing missing; every configured leg logs enabled |
| pre-auth guard, from outside | N+1 junk-token requests from a real IP | the N+1th is 429 and the durable store's read count did not move |
| rollback | deploy N, roll back to N-1 | `/health` (or a version marker) reports N-1; streams reconnect |
| platform observability | trigger one event | it appears in the platform's log viewer / metrics, not only on stdout |
| telemetry to the studio | a workflow run in the deployed repo | the run lands in the studio's agent-events spool |

## Three states, not two

- **live** — flipped under real power, light observed. Evidence: the
  command, the timestamp, what was observed.
- **dead** — flipped, no light. Evidence + the smallest wiring fix, owned by
  the operator (or the engineer whose lane it is). A dead circuit blocks the
  release gate.
- **substituted** — could not be flipped under real power here; a proxy
  stood in (a local jar, a fake sender, a counter). Evidence of the proxy,
  and — the point — exactly WHAT CLOSES IT: a credential, an account, a
  device, a platform. Substituted circuits do not block the gate but must be
  accepted BY NAME at it; the electrician's standing job is driving this
  count to zero.

A circuit proven only on localhost is `substituted` for the deployed
environment — a jar on 127.0.0.1 proves the binary, not the wiring.

## The rule that makes the seat safe

The electrician is the only seat that touches real credentials, real cloud,
and real devices. Therefore: **exercise, never configure.** Send clearly
marked test signals on a TEST scope (never a real user's pair), read the
meters, file what is dead. Never edit a deploy config, never rotate a key,
never touch real user data. The operator wires; the electrician certifies —
and the same hands must not do both (the QA-vs-engineer rule, applied to
infrastructure).

## Output

`09-circuits.md` (service) / `<milestone>-09-circuits.md` (app): the
circuit table with state + evidence + what-closes-it, a substitution ledger
(count, and the shortest path to zero), and the verdict: `conducts` (no dead
circuits; substitutions listed for the gate) or `dead` (with the wiring
tasks filed as `ops`).
