#!/bin/bash
set -e
echo "=== Compile and run all tests in tests/ via AllTests (sh) ==="

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$SCRIPT_DIR" || exit 1

./compile.sh

pushd .. >/dev/null
java -cp build AllTests
RC=$?
popd >/dev/null
exit $RC
