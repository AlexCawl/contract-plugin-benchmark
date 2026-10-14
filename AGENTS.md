# Repository Guidelines

## Purpose and Boundaries

This independent Android Microbenchmark project compares the performance and usability of
versioned Compose contracts. Every version has exactly two primary contracts: Size for
internal padding, shape/corner rounding, and spacing; Appearance for colors, border, and
elevation. Mix state-dependent and parameterless composable methods in each version.

All versions must resolve equivalent values into the single shared M3 Card renderer in
`library.common`; never implement separate UI per version. Shared immutable CardState has
enabled and pressed. Interaction state comes from the same source passed to M3.

V1 is implemented manually with Taiga-style factories, copy/rememberCopy, wrapped receivers,
and the original ReflectiveEqualsHashCode algorithm. Comparative performance workloads
remain a separate step.

Do not add a contract compiler plugin, its annotations, published artifacts, an included
build, or a dependency on another checkout unless the user explicitly changes this scope.
The standard Kotlin Compose compiler is part of the testing infrastructure.

## Structure

- `library/src/main/`: shared renderer/state, versioned contracts and composable wrappers,
  equality, and the shared demonstration scene. Export Material3 through api.
- `app/src/main/`: launcher MainActivity hosting the demonstration inside MaterialTheme.
- `benchmark/src/main/`: standalone correctness/benchmark test Activity.
- `benchmark/src/androidTest/`: Microbenchmarks and Compose correctness tests.
- `gradle/libs.versions.toml`: dependency and plugin versions.

app uses com.android.application; library and benchmark use com.android.library. Both app
and benchmark depend directly on library. Keep library independent of app and keep the common
renderer independent of versioned contracts. Preserve M3's arbitrary ColumnScope content
and clickable/non-clickable APIs in versioned wrappers.

Keep infrastructure benchmarks separate from correctness tests and the interactive demo;
the placeholder allocation benchmark is not an Appearance comparison.

## Build and Verification

Use JDK 21, Android SDK Platform 36, and Build Tools 36.0.0. Android bytecode targets JVM 17
and minSdk is 23. Use the Gradle wrapper from this project root:

- `./gradlew build :app:assembleDebug :benchmark:assembleReleaseAndroidTest --continue`: verify and assemble without a device.
- `./gradlew :app:installDebug`: install the launcher demonstration.
- `./gradlew :benchmark:connectedReleaseAndroidTest -Pandroid.testInstrumentationRunnerArguments.androidx.benchmark.dryRunMode.enable=true`: exercise correctness and infrastructure on a device or emulator without performance metrics.
- `./gradlew :benchmark:connectedReleaseAndroidTest`: run instrumentation tests and collect benchmark results on a physical device.

Microbenchmarks use AndroidBenchmarkRunner, release tests signed with the debug key,
no minification, and the AndroidX Benchmark plugin's default AOT compilation. Do not disable
benchmark environment checks in committed configuration to obtain emulator timings.

## Change Guidelines

Follow official Kotlin style and the existing com.alexcawl.contract.benchmark packages.
Keep changes focused on the requested comparison. Share equivalent UI and inputs between
implementations; exclude setup, idle waits, and correctness counters from timed work.
State what each benchmark measures and distinguish CPU recomposition/layout from GPU drawing
or frame jank. Add regression tests for non-trivial state transitions, including cancellation
and disabling during a press. Preserve V1's no-key rememberCopy capture semantics.
