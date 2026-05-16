<div align="center">
  <h1>Silicon</h1>
</div>

<h4 align="center">High-performance, cross-platform GPU computing API.</h4>

<p align="center">
    <img alt="Java 22" src="https://img.shields.io/badge/java-22-red">
    <img alt="GitHub Commit Activity" src="https://img.shields.io/github/commit-activity/m/brain4j-org/silicon"/>
    <img alt="Github Last Commit" src="https://img.shields.io/github/last-commit/brain4j-org/silicon"/>
    <img alt="License" src="https://img.shields.io/github/license/brain4j-org/silicon">
</p>


Write GPU code once and execute it across CUDA, Metal, and OpenCL backends
through a consistent low-level compute API.

---

## Install

**Note**: Silicon requires Java 22, as it heavily relies on the Java Panama API.

Silicon is avilable on the official Brain4J [repository](https://repo.brain4j.org/).

```gradle
repositories {
    mavenCentral()
    maven { url 'https://repo.brain4j.org/snapshots' }
    maven { url 'https://repo.brain4j.org/releases' }
}

dependencies {
    implementation "org.silicon:silicon-api:1.0.0-SNAPSHOT" // base API, mandatory
    // implementation "org.silicon:silicon-cuda:1.0.0-SNAPSHOT" // if you have/use CUDA
    // implementation "org.silicon:silicon-metal:1.0.0-SNAPSHOT" // if you have/use Metal
    // implementation "org.silicon:silicon-opencl:1.0.0-SNAPSHOT" // if want OpenCL
}
```

## Why Silicon?

### 1. Memory safety by design
GPU programming is notoriously error-prone: use-after-free, double-free,
invalid buffer access, and undefined behavior are common issues.

Silicon is designed to be **memory-safe**, preventing entire classes of bugs
through strict resource ownership and lifetime management, without relying on
manual bookkeeping.

### 2. Performance without compromise
Silicon does **not** trade performance for portability.

Its abstraction layer is intentionally minimal, allowing backend-specific
optimizations while keeping overhead as close to zero as possible.

### 3. Simple, consistent API
Silicon offers a **clean and concise API** that can be learned in minutes.

Once you understand Silicon’s core concepts, switching between CUDA, Metal,
or OpenCL backends requires no changes to your application logic.

### 4. Unified multi-backend compute

Modern GPU programming is heavily fragmented.

CUDA, Metal, and OpenCL all expose different APIs, shader languages,
toolchains, memory models, and execution semantics. Supporting multiple
platforms often means duplicating kernels, maintaining separate backends,
and introducing platform-specific bugs.

Silicon reduces this complexity through a unified compute API and
Slang-based cross-compilation, allowing the same application logic and
kernel code to run across different GPU architectures with minimal
backend-specific changes.

## Features

| Feature                              | CUDA | Metal | OpenCL |
|--------------------------------------|------|--------|---------|
| Unified compute API                  | ✅   | ✅     | ✅      |
| Slang cross-compilation              | ✅   | ✅     | ❌      |
| Runtime kernel compilation           | ✅   | ✅     | ✅      |
| Explicit GPU memory management       | ✅   | ✅     | ✅      |
| Async execution & synchronization    | ✅   | ✅     | ✅      |
| FP16 (`half`) support                | ✅   | ✅     | ✅      |
| Device capability querying           | ✅   | ✅     | ✅      |

## Contributing

If you like the project and would like to contribute, please refer to the
[Contributing Guide](CONTRIBUTING.md).
