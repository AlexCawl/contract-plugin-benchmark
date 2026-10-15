# Repository Guidelines

## Purpose and Boundaries

This independent Android project measures V1/V2 Compose contracts. Each version has Size
(Column arrangement/alignment) and Appearance (shape, colors, border, elevation). V1 methods
are composable; V2 methods are ordinary Kotlin callbacks.

Keep Card1 and Card2 independent, including Surface, Column, and elevation animation.
Keep each scene as a private composable in its Activity file, called only by that Activity.
Repeat identical tokens and UI content explicitly. Do not add shared scenes, factory wrappers,
input-holder types, common benchmark base classes, or contract correctness tests.
Exception: private inline appearance/size helpers with value defaults may prepare Microbenchmark
source contracts and heap samples. In create measurements, pass every argument explicitly from tokens prepared
outside timed work.
Scene-specific private composable helpers may remember full copies from Presets.default.
Keep token calculation in each Scene and call those helpers inside each lazy item.

Preserve the contract APIs, V1 inline factories, wrapped receiver copy/rememberCopy and reflection,
and V2 callback rebinding/snapshot/equality semantics. Do not add the contract
compiler plugin, annotations, published artifacts, included builds, or another checkout dependency.

## Structure

- `library/src/main/`: only library.v1/v2 contracts and Card composables. Export Material3 through api.
- `app/src/main/`: independent Activity1 and Activity2, each with its own private scene. V1 is the launcher.
- `app/src/benchmark/`: profileable manifest overlay for the non-debuggable benchmark build.
- `benchmark/src/androidTest/`: separate Size1Benchmark/Appearance1Benchmark/Size2Benchmark/Appearance2Benchmark and Size1HeapDumpTest/Appearance1HeapDumpTest/Size2HeapDumpTest/Appearance2HeapDumpTest classes.
- `benchmark/src/androidTest/` also contains HeapDumpUtils.kt in the common test package for shared capture steps.
- `macrobenchmark/src/main/`: self-instrumenting test APK targeting app's benchmark variant,
  with separate Activity1Macrobenchmark and Activity2Macrobenchmark classes.
- `gradle/libs.versions.toml`: dependency and plugin versions.

App uses com.android.application; library and benchmark use com.android.library;
macrobenchmark uses com.android.test. App and benchmark depend directly on library.
Library must stay independent of app. Do not introduce a common renderer or exported scene API.

## Measurement Rules

Each scene has 1000 lazy items and the same two palettes, geometry, and content. Each visible
item remembers full Size and Appearance copies from its version's Presets.default, keyed by
the default contract and revision. Read defaults in the Scene, then use private rememberSize1/2
and rememberAppearance1/2 helpers in the same Activity file to build each item's copies.
Supply all two Size
callbacks or all five Appearance callbacks in full-copy cases. Pass the same interaction source
to Surface and its consumers. Preserve the resource tags used by Macrobenchmark.

Each Microbenchmark class measures one version and contract through three @Test methods:
create, copy, and copyAll. Prepare tokens and sources outside timed work. Call inline appearance/size
helpers for creation and copy extensions directly inside timed blocks. Create callbacks there
and consume results with BlackHole. Callbacks must
capture local token values, never the benchmark instance. Do not interpret allocationCount as bytes.

Each heap class covers one version and contract, with four opt-in @Test methods:
create/copy/copyAll/chain32. Check heapDump=true in @Before with assumeTrue; otherwise skip.
Build each operation's sample directly in its @Test method, using one private inline creation
factory with value defaults per class. Do not add operation builders or dispatch.
Keep only the final sample rooted in that class's static current field; clear it in finally.
Dump in @After after the test method returns so construction locals cannot root sources;
skip the dump when current is null, including tests skipped by @Before. Delegate capture to the
ordinary internal dumpHeap(fileName) in HeapDumpUtils.kt, passing only the file name. Keep current
cleanup in each class's finally block. Keep heap captures
outside timed work, with the same token values and callback captures as object benchmarks.

## Build and Verification

Use JDK 21, Android SDK Platform 36, Build Tools 36.0.0, and JVM target 17. App/library/benchmark
minSdk is 23; Macrobenchmark requires API 31. Run from this root:

- `./gradlew build :app:assembleDebug :app:assembleBenchmark :benchmark:assembleReleaseAndroidTest :macrobenchmark:assembleBenchmark --continue --max-workers=1`: build, lint, and benchmark APKs without a device.
- `./gradlew :app:installDebug`: install the app; start each Activity with an explicit Intent.
- `./gradlew :benchmark:connectedReleaseAndroidTest -Pandroid.testInstrumentationRunnerArguments.androidx.benchmark.dryRunMode.enable=true`: Microbenchmark smoke checks, without performance results.
- `./gradlew :macrobenchmark:connectedBenchmarkAndroidTest -Pandroid.testInstrumentationRunnerArguments.androidx.benchmark.dryRunMode.enable=true`: Macrobenchmark smoke checks.
- `./gradlew :benchmark:connectedReleaseAndroidTest`: physical-device object measurements for all four contract/version classes. Use the class argument to select Size1Benchmark, Appearance1Benchmark, Size2Benchmark, or Appearance2Benchmark.
- `./gradlew :macrobenchmark:connectedBenchmarkAndroidTest`: physical-device startup/frame measurements.
- `./gradlew :macrobenchmark:connectedBenchmarkAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.alexcawl.contract.benchmark.macro.Activity1Macrobenchmark`: three Activity1 scenarios. Select Activity2Macrobenchmark similarly.
- `./gradlew :macrobenchmark:connectedBenchmarkAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.alexcawl.contract.benchmark.macro.Activity2Macrobenchmark#scroll`: one Activity2 scenario.
- `./gradlew :benchmark:connectedReleaseAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.alexcawl.contract.benchmark.test.v1.Size1HeapDumpTest -Pandroid.testInstrumentationRunnerArguments.heapDump=true`: four Size heap samples for V1. Select Appearance or V2 similarly, or pass all four class names separated by commas for 16 dumps. Append #copyAll to a class name to select one operation.

Microbenchmarks use AndroidBenchmarkRunner, release tests, debug signing, no minification,
and default AndroidX AOT compilation. Keep the Compose compiler for V1's composable callbacks;
Compose correctness-test libraries and a test Activity are not needed. Macrobenchmarks retain
CompilationMode.Full(), ten iterations, and independent Activity launches in fresh processes.
Activity1Macrobenchmark and Activity2Macrobenchmark are ordinary JUnit 4 classes, each targeting
only its corresponding Activity through an explicit ComponentName. Each contains coldStartup,
scroll, and copyAllUpdates: six scenarios total. Keep their rules and helpers local to each class;
do not add parameterization or a common base class.
Do not suppress environment checks to obtain emulator performance numbers. Heap captures stay
opt-in; allow sufficient device storage for full-process dumps.

## Change Guidelines

Follow official Kotlin style and existing com.alexcawl.contract.benchmark packages. Verify changes
with build/lint and dry runs of the existing measurement scenarios. Do not add contract correctness
test suites. Distinguish factory/copy times, allocation counts, shallow/retained heap size,
startup, and frame metrics. Update README commands when class names or measurement setup change.
