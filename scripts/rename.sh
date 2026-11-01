#!/bin/bash
# Turns a fresh copy of kmp-app-template into your app. Run once, from the repo root:
#   scripts/rename.sh "Display Name" com.example.app owner/repo
# Rewrites the package (sources, applicationId, namespaces, iOS bundle id, Compose resources),
# moves the source directories, and renames the project and its .studio/target.json.
set -euo pipefail

if [ $# -ne 3 ]; then
    echo "usage: $0 \"Display Name\" com.example.app owner/repo" >&2
    exit 2
fi
NAME="$1"
PKG="$2"
REPO="$3"
[[ "$PKG" =~ ^[a-z][a-z0-9_]*(\.[a-z][a-z0-9_]*)+$ ]] || { echo "not a package name: $PKG" >&2; exit 2; }
[[ "$REPO" =~ ^[A-Za-z0-9_.-]+/[A-Za-z0-9_.-]+$ ]] || { echo "not owner/repo: $REPO" >&2; exit 2; }
[[ "$NAME" != *"|"* ]] || { echo "display name may not contain |" >&2; exit 2; }
cd "$(dirname "$0")/.."
git grep -q "com.abyxcz.template" -- ':!scripts/rename.sh' || { echo "already renamed" >&2; exit 1; }

OLD_PKG="com.abyxcz.template"
OLD_DIR="com/abyxcz/template"
NEW_DIR="${PKG//.//}"
SLUG="${REPO#*/}"

# Tracked text files only; the workflow set (.claude) and the fleet detekt rules are not the app's.
FILES=$(git ls-files -- ':!.claude' ':!detekt-rules' ':!scripts/rename.sh' ':!gradle/wrapper' ':!*.jar' ':!*.png')
for f in $FILES; do
    OLD_PKG="$OLD_PKG" PKG="$PKG" NAME="$NAME" REPO="$REPO" SLUG="$SLUG" perl -pi -e '
        s/\Q$ENV{OLD_PKG}\E/$ENV{PKG}/g;
        s/tjmtic\/kmp-app-template/$ENV{REPO}/g;
        s/kmp-app-template/$ENV{SLUG}/g;
        s/KMP Template/$ENV{NAME}/g;
    ' "$f"
done

# Move each Kotlin source root's package directory.
for root in $(find composeApp shared -type d -path "*/kotlin/$OLD_DIR" -not -path "*/build/*"); do
    base="${root%/$OLD_DIR}"
    mkdir -p "$base/$NEW_DIR"
    git mv "$root"/* "$base/$NEW_DIR"/ 2>/dev/null || mv "$root"/* "$base/$NEW_DIR"/
    rmdir -p "$root" 2>/dev/null || true
done

echo "Renamed to $NAME ($PKG, $REPO)."
echo "Next: set DEVELOPMENT_TEAM in iosApp/project.yml (then xcodegen generate) or pass it to xcodebuild,"
echo "and build: ./gradlew ktfmtCheck detekt :shared:jvmTest :composeApp:assembleDebug"
