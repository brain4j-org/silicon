#!/usr/bin/env bash
set -euo pipefail

CUDA_HOME="${CUDA_HOME:-/usr/local/cuda}"
OUT_DIR="${OUT_DIR:-out}"
SWIFTC="${SWIFTC:-swiftc}"

if [ -n "${TARGET_CLASSIFIER:-}" ]; then
    CLASSIFIER="$TARGET_CLASSIFIER"
else
    ARCH="$(uname -m)"
    case "$ARCH" in
      x86_64|amd64)
        CLASSIFIER="linux-x64"
        ;;
      aarch64|arm64)
        CLASSIFIER="linux-arm64"
        ;;
      *)
        echo "Unsupported CUDA native architecture: $ARCH" >&2
        exit 1
        ;;
    esac
fi

mkdir -p "$OUT_DIR/$CLASSIFIER"

"$SWIFTC" -emit-library \
  src/CudaDevice.swift src/CudaUtils.swift src/CudaContext.swift src/CudaBuffer.swift src/CudaStream.swift src/CudaModule.swift src/CudaFunction.swift \
  -I Modules \
  -I "$CUDA_HOME/include" \
  -L "$CUDA_HOME/lib64" \
  -lcuda \
  -o "$OUT_DIR/$CLASSIFIER/libcuda4j.so"
