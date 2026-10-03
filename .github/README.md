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

Silicon requires Java 22, for the Java Panama API.

Silicon is avilable on the official Brain4J [repository](https://repo.brain4j.org/).

```gradle
def siliconVersion = "0.2.0"

repositories {
    mavenCentral()
    maven { url 'https://repo.brain4j.org/snapshots' }
    maven { url 'https://repo.brain4j.org/releases' }
}

dependencies {
    implementation "org.silicon:silicon-api:$siliconVersion" // base API, mandatory

    // Choose the backend you need
    // implementation "org.silicon:silicon-cuda:$siliconVersion"
    // implementation "org.silicon:silicon-metal:$siliconVersion"
    // implementation "org.silicon:silicon-opencl:$siliconVersion"
}
```

## Documentation

Full documentation is available at [silicon.brain4j.org](https://silicon.brain4j.org).

## Features

| Feature                           | CUDA | Metal | OpenCL |
|-----------------------------------|------|-------|--------|
| Unified compute API               | ✅   | ✅    | ✅     |
| Slang cross-compilation           | ✅   | ✅    | ❌     |
| Runtime kernel compilation        | ✅   | ✅    | ✅     |
| Explicit GPU memory management    | ✅   | ✅    | ✅     |
| Async execution & synchronization | ✅   | ✅    | ✅     |
| FP16 (`half`) support             | ✅   | ✅    | ✅     |
| Device capability querying        | ✅   | ✅    | ✅     |
| Native Memory Pool                | ❌   | ❌    | ❌     |

## Platform support

| Backend | Linux         | Windows       | macOS         |
|---------|---------------|---------------|---------------|
| CUDA    | x64 / arm64   | x64           | Not supported |
| Metal   | Not supported | Not supported | x64 / arm64   |
| OpenCL  | x64 / arm64   | x64           | x64 / arm64   |

Slang compilation uses `slangc` from `PATH` by default. Override it with the
`SLANGC` environment variable or `-Dsilicon.slangc=/path/to/slangc`. The shader
cache can be overridden with `-Dsilicon.slang.cache=/path/to/cache`.

## Contributing

If you like the project and would like to contribute, please refer to the
[Contributing Guide](CONTRIBUTING.md).

## License

Silicon is licensed under Apache 2.0 LICENSE.