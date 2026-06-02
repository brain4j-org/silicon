#!/bin/bash

LIB_NAME="libmetal4j.dylib"
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

OUT_DIR="${OUT_DIR:-out/$CLASSIFIER}"
SOURCES="src/**"

mkdir -p "$OUT_DIR"
swiftc -emit-library -o "$OUT_DIR/$LIB_NAME" $SOURCES -framework Metal -framework Foundation

if [ $? -eq 0 ]; then
  echo "Built successfully: $OUT_DIR/$LIB_NAME"
else
  echo "Error during build"
fi
