#!/usr/bin/env bash
set -euo pipefail

CUDA_HOME="${CUDA_HOME:-/usr/local/cuda}"
OUT_DIR="${OUT_DIR:-out}"
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

mkdir -p "$OUT_DIR/$CLASSIFIER"

swiftc -emit-library \
  src/CudaDevice.swift src/CudaUtils.swift src/CudaContext.swift src/CudaBuffer.swift src/CudaStream.swift src/CudaModule.swift src/CudaFunction.swift \
  -I Modules \
  -I "$CUDA_HOME/include" \
  -L "$CUDA_HOME/lib64" \
  -lcuda \
  -o "$OUT_DIR/$CLASSIFIER/libcuda4j.so"
