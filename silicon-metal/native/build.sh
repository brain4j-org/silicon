#!/usr/bin/env bash
set -euo pipefail

LIB_NAME="
"
OUT_DIR="${OUT_DIR:-out}"

ARCH=$(uname -m)
case "$ARCH" in
    x86_64)  CLASSIFIER="macos-x64" ;;
    arm64)   CLASSIFIER="macos-arm64" ;;
    *) echo "Unsupported architecture: $ARCH"; exit 1 ;;
esac

OUTPUT="$OUT_DIR/$CLASSIFIER/$LIB_NAME.dylib"

mkdir -p "$(dirname "$OUTPUT")"

swiftc \
    -emit-library \
    -o "$OUTPUT" \
    src/*.swift \
    -framework Metal \
    -framework Foundation

echo "Built successfully: $OUTPUT"