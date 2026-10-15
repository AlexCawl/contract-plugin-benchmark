# Repository Guidelines

## Purpose and Boundaries

This independent Android project measures V1/V2 Compose contracts. Each version has Size
(Column arrangement/alignment) and Appearance (shape, colors, border, elevation). V1 methods
are composable; V2 methods are ordinary Kotlin callbacks.

Keep Card1 and Card2 independent, including Surface, Column, and elevation animation.
Keep each scene as a private composable in its Activity file, called only by that Activity.
Repeat identical tokens and UI content explicitly. Do not add shared scenes, factory wrappers,
input-holder types, common benchmark base classes, or contract correctness tests.

Preserve the contract APIs, V1 inline factories, wrapped receiver copy/rememberCopy and reflection,
and V2 callback rebinding/snapshot/equality semantics. Do not add the contract
compiler plugin, annotations, published artifacts, included builds, or another checkout dependency.

## Structure

- `library/src/main/`: only library.v1/v2 contracts and Card composables. Export Material3 through api.
- `app/src/main/`: independent ActivityV1 and ActivityV2, each with its own private scene. V1 is the launcher.
- `app/src/benchmark/`: profileable manifest overlay for the non-debuggable benchmark build.
- `benchmark/src/androidTest/`: V1ContractBenchmark/V2ContractBenchmark and separate V1SizeHeapDumpTest/V1AppearanceHeapDumpTest/V2SizeHeapDumpTest/V2AppearanceHeapDumpTest classes.
- `macrobenchmark/src/main/`: self-instrumenting test APK targeting app's benchmark variant.
- `gradle/libs.versions.toml`: dependency and plugin versions.

App uses com.android.application; library and benchmark use com.android.library;
macrobenchmark uses com.android.test. App and benchmark depend directly on library.
Library must stay independent of app. Do not introduce a common renderer or exported scene API.

## Measurement Rules

Each scene has 1000 lazy items and the same two palettes, geometry, and content. Each visible
item remembers full Size and Appearance copies by base and revision. Supply all two Size
callbacks or all five Appearance callbacks in full-copy cases. Pass the same interaction source
to Surface and its consumers. Preserve the resource tags used by Macrobenchmark.

Each Microbenchmark class measures only its own version: create, unchanged copy, full copy
for both contracts. Prepare tokens and sources outside timed work. Call factories/copy directly
inside timed blocks, create callbacks there, and consume results with BlackHole. Callbacks must
capture local token values, never the benchmark instance. Do not interpret allocationCount as bytes.

Each heap class covers one version and contract, with four opt-in @Test methods:
create/copy/copyAll/chain32. Check heapDump=true in @Before with assumeTrue; otherwise skip.
Build each operation's sample in its own method, without parameterized tests or operation dispatch.
Keep only the final sample rooted in that class's static current field; clear it in finally.
Return from construction before dumping so locals cannot root sources. Keep heap captures
outside timed work, with the same token values and callback captures as object benchmarks.

## Build and Verification

Use JDK 21, Android SDK Platform 36, Build Tools 36.0.0, and JVM target 17. App/library/benchmark
minSdk is 23; Macrobenchmark requires API 31. Run from this root:

- `./gradlew build :app:assembleDebug :app:assembleBenchmark :benchmark:assembleReleaseAndroidTest :macrobenchmark:assembleBenchmark --continue --max-workers=1`: build, lint, and benchmark APKs without a device.
- `./gradlew :app:installDebug`: install the app; start each Activity with an explicit Intent.
- `./gradlew :benchmark:connectedReleaseAndroidTest -Pandroid.testInstrumentationRunnerArguments.androidx.benchmark.dryRunMode.enable=true`: Microbenchmark smoke checks, without performance results.
- `./gradlew :macrobenchmark:connectedBenchmarkAndroidTest -Pandroid.testInstrumentationRunnerArguments.androidx.benchmark.dryRunMode.enable=true`: Macrobenchmark smoke checks.
- `./gradlew :benchmark:connectedReleaseAndroidTest`: physical-device object measurements for both version classes. Use the class argument to select V1ContractBenchmark or V2ContractBenchmark.
- `./gradlew :macrobenchmark:connectedBenchmarkAndroidTest`: physical-device startup/frame measurements.
- `./gradlew :benchmark:connectedReleaseAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.alexcawl.contract.benchmark.test.v1.V1SizeHeapDumpTest -Pandroid.testInstrumentationRunnerArguments.heapDump=true`: four Size heap samples for V1. Select Appearance or V2 similarly, or pass all four class names separated by commas for 16 dumps. Append #copyAll to a class name to select one operation.

Microbenchmarks use AndroidBenchmarkRunner, release tests, debug signing, no minification,
and default AndroidX AOT compilation. Keep the Compose compiler for V1's composable callbacks;
Compose correctness-test libraries and a test Activity are not needed. Macrobenchmarks retain
CompilationMode.Full(), ten iterations, and independent Activity launches in fresh processes.
Do not suppress environment checks to obtain emulator performance numbers. Heap captures stay
opt-in; allow sufficient device storage for full-process dumps.

## Change Guidelines

Follow official Kotlin style and existing com.alexcawl.contract.benchmark packages. Verify changes
with build/lint and dry runs of the existing measurement scenarios. Do not add contract correctness
test suites. Distinguish factory/copy times, allocation counts, shallow/retained heap size,
startup, and frame metrics. Update README commands when class names or measurement setup change.
