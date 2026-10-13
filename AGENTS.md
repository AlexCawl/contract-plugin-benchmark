# Repository Guidelines

## Purpose and Boundaries

This independent Android Microbenchmark project will compare approaches to implementing
Appearance objects and using them in Compose. The initial scaffold intentionally contains
no Appearance implementations, calendar UI, or LazyColumn workloads.

Do not add a contract compiler plugin, its annotations, published artifacts, an included
build, or a dependency on another checkout unless the user explicitly changes this scope.
The standard Kotlin Compose compiler is part of the testing infrastructure.

## Structure

- `app/src/main/`: Compose test host, shared UI, and future implementations under test.
- `benchmark/src/androidTest/`: Microbenchmarks and Compose correctness tests.
- `gradle/libs.versions.toml`: dependency and plugin versions.

Both modules use `com.android.library`. Keep infrastructure benchmarks separate from
correctness tests; the placeholder allocation benchmark is not an Appearance comparison.

## Build and Verification

Use JDK 21, Android SDK Platform 36, and Build Tools 36.0.0. Android bytecode targets JVM 17
and minSdk is 23. Use the Gradle wrapper from this project root:

- `./gradlew build :benchmark:assembleReleaseAndroidTest --continue`: verify and assemble without a device.
- `./gradlew :benchmark:connectedReleaseAndroidTest -Pandroid.testInstrumentationRunnerArguments.androidx.benchmark.dryRunMode.enable=true`: exercise the scaffold on a device or emulator without performance metrics.
- `./gradlew :benchmark:connectedReleaseAndroidTest`: run instrumentation tests and collect benchmark results on a physical device.

Microbenchmarks use `AndroidBenchmarkRunner`, release tests signed with the debug key,
no minification, and the AndroidX Benchmark plugin's default AOT compilation. Do not disable
benchmark environment checks in committed configuration to obtain emulator timings.

## Change Guidelines

Follow official Kotlin style and the existing `com.alexcawl.contract.benchmark` packages.
Keep changes focused on the requested comparison. Share equivalent UI and inputs between
implementations; exclude setup, idle waits, and correctness counters from timed work.
State what each benchmark measures and distinguish CPU recomposition/layout from GPU drawing
or frame jank. Add regression tests for non-trivial state transitions.
