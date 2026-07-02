#!/usr/bin/env bash
set -euo pipefail

LIB_NAME="libmetal"
OUT_DIR="${OUT_DIR:-out}"

TARGETS=(
    "arm64-apple-macos15.0 macos-arm64"
    "x86_64-apple-macos15.0 macos-x64"
)

for entry in "${TARGETS[@]}"; do
    read -r TARGET CLASSIFIER <<< "$entry"

    OUTPUT="$OUT_DIR/$CLASSIFIER/$LIB_NAME.dylib"
    mkdir -p "$(dirname "$OUTPUT")"

    swiftc \
        -emit-library \
        -target "$TARGET" \
        -o "$OUTPUT" \
        src/*.swift \
        -framework Metal \
        -framework Foundation

    echo "Built successfully: $OUTPUT"
done