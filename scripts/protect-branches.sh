#!/bin/bash
# Branch protection for a repo made from this template (GitHub does not copy it from the
# template). Run once after creating the repo and pushing develop:
#   scripts/protect-branches.sh owner/repo
# main    — the release branch: both CI lanes required.
# develop — the studio's merge target: the Linux lane required (PRs into develop skip macOS).
# No required reviews: the studio merges on green CI; the human gate is the develop → main PR.
set -euo pipefail
REPO="${1:?usage: $0 owner/repo}"

protect() { # branch, contexts...
    local branch="$1"; shift
    local contexts
    contexts=$(printf '"%s",' "$@"); contexts="[${contexts%,}]"
    gh api -X PUT "repos/$REPO/branches/$branch/protection" --input - >/dev/null <<JSON
{
  "required_status_checks": { "strict": false, "contexts": $contexts },
  "enforce_admins": false,
  "required_pull_request_reviews": null,
  "restrictions": null,
  "allow_force_pushes": false,
  "allow_deletions": false
}
JSON
    echo "$REPO@$branch: requires $*"
}

protect main "Checks (Linux)" "iOS (macOS)"
protect develop "Checks (Linux)"
