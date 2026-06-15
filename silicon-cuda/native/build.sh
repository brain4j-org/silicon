#!/usr/bin/env bash
set -euo pipefail

CUDA_HOME="${CUDA_HOME:-/usr/local/cuda}"
OUT_DIR="${OUT_DIR:-out}"
SWIFTC="${SWIFTC:-swiftc}"

compile() {
    local classifier="$1"
    local extension="$2"

    mkdir -p "$OUT_DIR/$classifier"

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
        -o "$OUT_DIR/$classifier/libcuda.$extension"

    echo "Built successfully: $OUT_DIR/$classifier/libcuda.$extension"
}

compile "linux-x64" "so"
compile "linux-arm64" "so"
compile "windows-x64" "dll"
