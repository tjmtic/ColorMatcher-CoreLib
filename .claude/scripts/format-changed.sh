#!/usr/bin/env bash
# format-changed.sh — ktfmt (kotlinlang style) over the Kotlin files this
# branch changed, and nothing else. The loop engineers run before `in_review`,
# the pre-commit hook, and (with --check) the reviewer's first question.
#
#   format-changed.sh                 format files changed vs. the base ref
#   format-changed.sh --check         exit 1 if any changed file would change
#   format-changed.sh --all           whole tree (migration PRs only)
#   format-changed.sh --base <ref>    diff against <ref> (default: origin/main, then main)
#   format-changed.sh --install-hook  write .git/hooks/pre-commit that runs this
#
# Engine: the `ktfmt` CLI when it is on PATH (milliseconds; `brew install
# ktfmt`), otherwise the Gradle plugin's ktfmtPrecommit/ktfmtCheck tasks.
# Files are fed in chunks so a large tree never overflows the argument list
# (the failure Block hit at 60k files). Policy: .claude/docs/kotlin-style.md.
set -euo pipefail

mode=format; base=""; all=0
while [ $# -gt 0 ]; do
  case "$1" in
    --check) mode=check ;;
    --all) all=1 ;;
    --base) shift; base="${1:-}" ;;
    --install-hook)
      root="$(git rev-parse --show-toplevel)"
      hook="$root/.git/hooks/pre-commit"
      cat > "$hook" <<'HOOK'
#!/usr/bin/env bash
# Installed by .claude/scripts/format-changed.sh --install-hook
set -e
root="$(git rev-parse --show-toplevel)"
bash "$root/.claude/scripts/format-changed.sh"
# Re-stage anything the formatter touched that was already staged.
git diff --name-only --cached -z -- '*.kt' '*.kts' | xargs -0 git add -- 2>/dev/null || true
HOOK
      chmod +x "$hook"
      echo "installed $hook"; exit 0 ;;
    -h|--help) sed -n '2,15p' "$0"; exit 0 ;;
    *) echo "unknown arg: $1" >&2; exit 2 ;;
  esac
  shift
done

root="$(git rev-parse --show-toplevel)"
cd "$root"

if [ -z "$base" ]; then
  if git rev-parse --verify -q origin/main >/dev/null; then base=origin/main
  elif git rev-parse --verify -q main >/dev/null; then base=main
  else base=HEAD; fi
fi

# Changed = committed on this branch since the merge-base, plus staged, plus
# unstaged, plus untracked. Deleted files are dropped by the existence test.
list_files() {
  if [ "$all" = 1 ]; then
    git ls-files -z -- '*.kt' '*.kts'
  else
    {
      git diff --name-only -z "$(git merge-base "$base" HEAD)" -- '*.kt' '*.kts'
      git diff --name-only -z --cached -- '*.kt' '*.kts'
      git diff --name-only -z -- '*.kt' '*.kts'
      git ls-files -z --others --exclude-standard -- '*.kt' '*.kts'
    } | tr '\0' '\n' | sort -u | tr '\n' '\0'
  fi | while IFS= read -r -d '' f; do
    [ -f "$f" ] || continue
    case "$f" in */build/*) continue ;; esac
    printf '%s\0' "$f"
  done
}

files_tmp="$(mktemp)"; trap 'rm -f "$files_tmp"' EXIT
list_files > "$files_tmp"
count=$(tr -cd '\0' < "$files_tmp" | wc -c | tr -d ' ')
if [ "$count" = 0 ]; then echo "format-changed: no Kotlin files changed"; exit 0; fi

if command -v ktfmt >/dev/null 2>&1; then
  if [ "$mode" = check ]; then
    # --dry-run prints the files that would change; non-empty output is drift.
    out="$(xargs -0 -n 200 ktfmt --kotlinlang-style --dry-run < "$files_tmp")"
    if [ -n "$out" ]; then
      echo "format-changed: NOT formatted (run: bash .claude/scripts/format-changed.sh)"; echo "$out"; exit 1
    fi
    echo "format-changed: $count file(s) clean"
  else
    xargs -0 -n 200 ktfmt --kotlinlang-style < "$files_tmp"
    echo "format-changed: formatted $count file(s)"
  fi
else
  # Gradle fallback: slower, but always present. --include-only takes a
  # colon-separated list; chunk it the same way.
  task=ktfmtPrecommit; [ "$mode" = check ] && task=ktfmtCheck
  status=0
  tr '\0' '\n' < "$files_tmp" | xargs -n 200 | while read -r chunk; do
    joined="$(printf '%s' "$chunk" | tr ' ' ':')"
    ./gradlew -q "$task" --include-only="$joined" || status=1
  done || status=1
  [ "$status" = 0 ] || { echo "format-changed: NOT formatted"; exit 1; }
  echo "format-changed: $count file(s) via gradle $task"
fi
