#!/usr/bin/env bash
# agent-event.sh — report this workflow session's activity to the studio harness.
#
# The harness (gcp-node-service) shows every agent's live state on /agents.
# In-process loops are visible through their tracked jobs; THIS set runs as
# Claude sessions in a target repo, so it reports by appending one JSON line
# per event to the harness's agent-events spool. Contract:
# <harness>/docs/agent-events.md — this script is just that append.
#
#   agent-event.sh run-start <agent-slug> [phase]
#   agent-event.sh phase     <agent-slug> <phase>
#   agent-event.sh run-end   <agent-slug> <succeeded|failed|cancelled> [detail]
#   agent-event.sh note      <agent-slug> <detail>
#
# Configuration is stamped at deploy time by the harness's deploy-fleet script
# into .claude/agent-events.env (AGENT_EVENTS_DIR, AGENT_ID_PREFIX). An
# undeployed or unstamped copy exits 0 silently: telemetry must never block
# the work it describes, so every failure path here is a quiet success.
set -u

here="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
env_file="$here/agent-events.env"
[ -f "$env_file" ] || exit 0
# shellcheck disable=SC1090
. "$env_file" || exit 0
[ -n "${AGENT_EVENTS_DIR:-}" ] && [ -d "$AGENT_EVENTS_DIR" ] || exit 0
[ -n "${AGENT_ID_PREFIX:-}" ] || exit 0

event="${1:-}"; slug="${2:-}"
case "$event" in run-start|phase|run-end|note) ;; *) exit 0 ;; esac
[ -n "$slug" ] || exit 0

# Third arg is a status on run-end, a phase otherwise; fourth is detail.
phase=""; status=""; detail="${4:-}"
if [ "$event" = "run-end" ]; then status="${3:-succeeded}"; else phase="${3:-}"; fi
if [ "$event" = "note" ]; then detail="${3:-}"; phase=""; fi

# Minimal JSON string escaping (backslash, quote, newline→space); values here
# are our own short phrases, not user data.
esc() { printf '%s' "$1" | tr '\n' ' ' | sed -e 's/\\/\\\\/g' -e 's/"/\\"/g' | cut -c1-500; }

ts="$(date -u +%FT%TZ)"
line="{\"ts\":\"$ts\",\"agentId\":\"${AGENT_ID_PREFIX}$(esc "$slug")\",\"event\":\"$event\""
[ -n "$phase" ]  && line="$line,\"phase\":\"$(esc "$phase")\""
[ -n "$status" ] && line="$line,\"status\":\"$(esc "$status")\""
[ -n "$detail" ] && line="$line,\"detail\":\"$(esc "$detail")\""
line="$line}"

# Leading newline per the contract: a torn earlier line must not weld to ours.
printf '\n%s\n' "$line" >> "$AGENT_EVENTS_DIR/events-$(date -u +%F).ndjson" 2>/dev/null || true
exit 0
