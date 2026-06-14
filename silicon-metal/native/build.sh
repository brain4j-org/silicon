#!/usr/bin/env bash
set -euo pipefail

LIB_NAME="libmetal4j.dylib"
OUT_DIR="${OUT_DIR:-out}"

if [ -n "${TARGET_CLASSIFIER:-}" ]; then
    CLASSIFIER="$TARGET_CLASSIFIER"
else
    ARCH="$(uname -m)"
    case "$ARCH" in
      x86_64|amd64)
        CLASSIFIER="macos-x64"
        ;;
      arm64|aarch64)
        CLASSIFIER="macos-arm64"
        ;;
      *)
        echo "Unsupported Metal native architecture: $ARCH" >&2
        exit 1
        ;;
    esac
fi

mkdir -p "$OUT_DIR/$CLASSIFIER"
swiftc -emit-library -o "$OUT_DIR/$CLASSIFIER/$LIB_NAME" src/*.swift -framework Metal -framework Foundation

echo "Built successfully: $OUT_DIR/$CLASSIFIER/$LIB_NAME"
