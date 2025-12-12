#!/bin/bash

LIB_NAME="libmetal4j.dylib"
SOURCES="src/**" # MetalBuffer.swift MetalCommandQueue.swift
swiftc -emit-library -o $LIB_NAME $SOURCES -framework Metal -framework Foundation

if [ $? -eq 0 ]; then
  echo "✅ Built successfully: $LIB_NAME"
else
  echo "❌ Error during build"
fi
