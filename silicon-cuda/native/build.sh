#!/usr/bin/env bash
set -euo pipefail

CUDA_HOME="${CUDA_HOME:-/usr/local/cuda}"
OUT_DIR="${OUT_DIR:-out}"
SWIFTC="${SWIFTC:-swiftc}"

ARCH=$(uname -m)
case "$ARCH" in
    x86_64)  CLASSIFIER="linux-x64" ;;
    aarch64|arm64) CLASSIFIER="linux-arm64" ;;
    *) echo "Unsupported architecture: $ARCH"; exit 1 ;;
esac

OUTPUT="$OUT_DIR/$CLASSIFIER/libcuda4j.so"

mkdir -p "$(dirname "$OUTPUT")"

"$SWIFTC" -emit-library \
    src/CudaDevice.swift \
    src/CudaUtils.swift \
    src/CudaContext.swift \
    src/CudaBuffer.swift \
    src/CudaStream.swift \
    src/CudaModule.swift \
    src/CudaFunction.swift \
    -I Modules \
    -I "$CUDA_HOME/include" \
    -L "$CUDA_HOME/lib64" \
    -lcuda \
    -o "$OUTPUT"

echo "Built successfully: $OUTPUT"