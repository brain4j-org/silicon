# Silicon

**A unified, low-level GPU computing API. Write once, run anywhere.**

> ⚠️ Silicon is under active development: the API is stabilizing, but breaking changes may still occur.

## What is Silicon?

Silicon is a cross-platform GPU computing API designed to unify multiple
backend technologies (such as **CUDA**, **Metal**, and **OpenCL**) under a
single, consistent programming model.

The goal is simple:  
**write your GPU code once and run it across different vendors and platforms,
without sacrificing performance or safety.**

Silicon focuses on providing a *thin abstraction layer* over native GPU APIs,
while handling the most error-prone aspects of GPU programming for you.

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
You get predictable performance across platforms, without opaque runtime magic.

### 3. Simple, consistent API
Silicon offers a **clean and concise API** that can be learned in minutes.

Once you understand Silicon’s core concepts, switching between CUDA, Metal,
or OpenCL backends requires no changes to your application logic.

## Who is Silicon for?

- Developers who need **portable GPU compute**
- Engine and system programmers
- Developers who want **control**, not black-box abstractions
- Anyone tired of rewriting the same kernels for different platforms

## Features

| Feature                    | CUDA | Metal | OpenCL |
|----------------------------|------|-------|--------|
| Async kernel dispatch      | ✅    | ✅     | ✅      |
| Explicit memory management | ✅    | ✅     | ✅      |
| Command queues             | ✅    | ✅     | ✅      |
| Slang JIT Cross Compile    | ✅    | ✅     | ✅      | 
| Device information API     | ✅    | ✅     | ✅      |
| FP16 support (half)        | ✅    | ✅     | ✅      |


## Contributing

If you like the project and would like to contribute, please refer to the
[Contributing Guide](CONTRIBUTING.md).