#!/bin/bash
set -e
echo "=== Compile and run AllTests (sh) ==="

# Move to scripts dir (in case run from elsewhere), then compile, then run from project root
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$SCRIPT_DIR" || exit 1

./compile.sh

echo "Running AllTests..."
pushd .. >/dev/null
java -cp build AllTests
RC=$?
popd >/dev/null
exit $RC
