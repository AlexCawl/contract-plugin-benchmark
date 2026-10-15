# Contract Plugin Benchmark

An independent Android project measuring V1 and V2 Compose contracts. Each version has
Size and Appearance contracts and its own Card1/Card2 implementation. Surface, Column,
and elevation animation remain separate in those composables.

The project does not depend on the contract compiler plugin, its annotations, published
artifacts, or another checkout. Only the standard Kotlin Compose compiler is enabled.

## Modules and application

- `library`: contracts in library.v1/v2 and the original Card implementations. Exports Material3.
- `app`: Activity1 and Activity2. Each scene is a private composable in its
  Activity file, called only by that Activity. V1 is the launcher; start V2 directly.
- `microbenchmark`: separate Size1Benchmark/Appearance1Benchmark/Size2Benchmark/Appearance2Benchmark and
  Size1HeapDumpTest/Appearance1HeapDumpTest/Size2HeapDumpTest/Appearance2HeapDumpTest classes.
- `macrobenchmark`: self-instrumenting com.android.test module targeting app's benchmark build,
  with separate Activity1Macrobenchmark and Activity2Macrobenchmark classes.

There are no shared scenes, input-holder types, or contract correctness tests. Private inline
appearance/size helpers with value defaults prepare Microbenchmark source contracts outside timed work
and create contracts inside create measurements with explicit precomputed arguments.
Scenes use their version's Presets.default as copy sources; object benchmarks use the versioned
factories and copy extensions. Tokens and content are repeated explicitly. The library contains
contracts, their presets, and Card renderers.

Each Activity hosts 1000 identically sized cards in a LazyColumn. Visible/prefetched items
remember their own Size and Appearance copies from their version's Presets.default, keyed by
the default contract and revision. Private composable rememberSize1/2 and rememberAppearance1/2
helpers in each Activity file build those copies inside each item; shared token calculation
stays in the Scene. Both copy calls supply every callback. Scrolling away can dispose copies;
this is not 1000 eagerly retained contracts.

The appearance button alternates the same colors, shape, border, arrangement, and alignment
in every version. The switch changes enabled and disabled colors. The same interaction source
feeds Surface and pressed-state semantics. Both palettes use zero elevation for equivalent
visual behavior; original preset animations remain unchanged in Card1/Card2.

There is no navigation between benchmark Activities. Macrobenchmark starts its selected
Activity directly in a fresh process.

## Contract API

| Method | V1 | V2 |
| --- | --- | --- |
| `contentArrangement(): Arrangement.Vertical` | Composable | Ordinary Kotlin |
| `contentAlignment(): Alignment.Horizontal` | Composable | Ordinary Kotlin |
| `shape(): Shape` | Composable | Ordinary Kotlin |
| `containerColor(enabled: Boolean): Color` | Composable | Ordinary Kotlin |
| `contentColor(enabled: Boolean): Color` | Composable | Ordinary Kotlin |
| `shadowElevation(...): Dp` | Composable; receives enabled and InteractionSource | Ordinary Kotlin; receives enabled and nullable Interaction |
| `border(): BorderStroke?` | Composable | Ordinary Kotlin |
| `contract(): Contract` | Not provided | Fresh callback snapshot |

Size presets use Arrangement.Top and Alignment.Start. Appearance presets use the current
MaterialTheme, with no border and Filled Card elevation targets. V1 uses CardDefaults;
V2 inlines the Material3 1.4.0 Filled Card tokens.

Card1 and Card2 retain onClick, size, appearance, modifier, enabled, interactionSource,
and arbitrary ColumnScope content. Each passes the same interaction source to its Surface
and elevation logic. Typography and other content belong to the caller.

## V1 semantics

V1 is unchanged. Size1 and Appearance1 use inline factories with composable receiver
lambdas and anonymous implementations extending ReflectiveEqualsHashCode.
Size1Presets.default and Appearance1Presets.default are composable getters.

Copy overrides receive Wrapper.wrapped. Use wrapped.method(...) to call the original
implementation; calling the overridden method on this recurses.

V1 rememberCopy deliberately uses remember without keys. It retains the first receiver and
override captures. Read a captured State or rememberUpdatedState when values must change.
Composable preset methods continue to read MaterialTheme at their call sites.

ReflectiveEqualsHashCode preserves the original algorithm: it compares declared captured
fields of anonymous implementations of the same class, including treating two null
reference fields in distinct instances as unequal.

## V2 semantics

V2 manually follows the current contract-plugin runtime architecture. Its factories accept
ordinary receiver callbacks such as Size2.() -> Arrangement.Vertical and
Appearance2.(Boolean) -> Color. Private final implementations invoke callbacks with the
current instance as this. Required methods need callbacks; shadowElevation and border
accept nullable callbacks defaulting to null, which invokes the original interface body.
A callback returning null still overrides border.

Each interface exposes a nested immutable Contract class and contract(). Implementations
store callbacks directly, and each contract() call constructs a fresh container with
ordinary reference equality. Copy calls contract() exactly once, merges the stored callback
references with supplied overrides, and calls the factory. At source level, each copy
constructs one temporary Contract container and one new Impl, without callback wrappers.
Actual allocation counts depend on ART and are reported by the Microbenchmark cases below.

All copy arguments are nullable and default to null, meaning preserve the source callback.
Passing null cannot reset an override to its interface default. Equality and hashing compare
stored callbacks, with multiplier 31 and zero for absent callbacks. An unchanged copy is a
distinct instance equal to its source, with the same hash code.

Preserved callbacks run with the new copy as this, so calls to other contract methods see
the copy's overrides. Explicitly captured external objects keep their original references.
For example, using V2 imports:

```kotlin
val original = Appearance2(
    shape = { RectangleShape },
    containerColor = { enabled -> if (enabled) Color.Red else Color.Gray },
    contentColor = { enabled -> containerColor(enabled) },
)
val copied = original.copy(containerColor = { Color.Blue })
check(copied.contentColor(true) == Color.Blue)
check(original.contentColor(true) == Color.Red)
check(original.copy() == original)
```

Manual implementations must supply their transferable callbacks through contract(); copy
does not cast them to the private Impl. V2 follows the current factory/copy API without
JvmOverloads or versioned ABI overloads. It has no wrapper receiver or rememberCopy helper.

Appearance2Presets.default resolves MaterialTheme.shapes.medium and four theme colors.
The container uses surfaceContainerHighest. The disabled container composites surfaceVariant
at alpha 0.38 over surfaceContainerHighest; disabled content also uses alpha 0.38.
Content color is resolved from surfaceContainerHighest with ColorScheme.contentColorFor.
The preset uses shape, containerColor, contentColor, disabledContainerColor, and disabledContentColor
as remember keys. Callback captures contain only those individual values. A theme change
produces a new preset; remember copies with their source as a key to pick up those new captures.

Inside a MaterialTheme composition, using imports from
`com.alexcawl.contract.benchmark.library.v2`:

```kotlin
val size = Size2Presets.default
val original = Appearance2Presets.default
val appearance = remember(original) {
    original.copy(border = { BorderStroke(1.dp, Color.Blue) })
}
Card2(onClick = { /* caller-owned action */ }, size = size, appearance = appearance) {
    Text("Arbitrary content")
}
```

## V2 elevation

Appearance2.shadowElevation(enabled, interaction) returns a target Dp. A null interaction
means idle. Default targets are 0.dp when disabled, 1.dp for hover, 6.dp for drag, and 0.dp
for idle, press, focus, or other interactions.

The private helper in Card2 collects active press, hover, focus, and drag
interactions. End and cancel events remove the matching start interaction. The helper passes
the latest active interaction, or null, to Appearance2 and animates the returned target
with V1's timings and easing.
Disabling snaps to the contract's returned target, including a custom nonzero disabled target.
V1's shadowElevation(enabled, interactionSource) and its animation remain unchanged.

## Build and verification

Use JDK 21, Android SDK Platform 36, Build Tools 36.0.0, and JVM target 17. Set ANDROID_HOME
or an ignored local.properties containing sdk.dir. App/library/microbenchmark minSdk is 23;
Macrobenchmark minSdk is 31. Dependency versions live in gradle/libs.versions.toml.

Build, run lint, and assemble all benchmark APKs without a device:

```shell
./gradlew build :app:assembleDebug :app:assembleBenchmark \
  :microbenchmark:assembleReleaseAndroidTest :macrobenchmark:assembleBenchmark --continue --max-workers=1
```

Install the app and start each screen independently:

```shell
./gradlew :app:installDebug
adb shell am force-stop com.alexcawl.contract.benchmark.app
adb shell am start -W -n com.alexcawl.contract.benchmark.app/.v1.Activity1
adb shell am force-stop com.alexcawl.contract.benchmark.app
adb shell am start -W -n com.alexcawl.contract.benchmark.app/.v2.Activity2
```

These adb timings are smoke checks, not substitutes for Macrobenchmark results.

Exercise the existing benchmark scenarios on a device or emulator without collecting performance
results. Heap captures remain opt-in:

```shell
./gradlew :microbenchmark:connectedReleaseAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.androidx.benchmark.dryRunMode.enable=true
./gradlew :macrobenchmark:connectedBenchmarkAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.androidx.benchmark.dryRunMode.enable=true
```

This project measures contracts; it does not test their correctness. Build, lint, and dry runs
verify that the measurement scenarios remain runnable.

## Microbenchmark: creation and copy

Run all four contract/version classes on a physical device:

```shell
./gradlew :microbenchmark:connectedReleaseAndroidTest
```

Select one contract/version when needed:

```shell
./gradlew :microbenchmark:connectedReleaseAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=com.alexcawl.contract.benchmark.test.v1.Size1Benchmark
```

Select Appearance1Benchmark, Size2Benchmark, or Appearance2Benchmark similarly.
Each class covers one contract and version, with three @Test methods (12 cases total):

- `create`: factory invocation through the inline appearance/size helper, including callback construction.
- `copy`: unchanged copy using that version's default-copy implementation.
- `copyAll`: direct copy with every callback replaced. Size supplies contentArrangement and
  contentAlignment; Appearance supplies shape, containerColor, contentColor, shadowElevation, and border.

Tokens and source contracts are prepared outside timed work. Creation helpers are inlined into
measured blocks with all token arguments passed explicitly; copy extensions are called directly.
Their callbacks capture local token values, not the benchmark instance.
BlackHole consumes the result so it cannot be discarded as unused.

AndroidX warms up and reports timeNs and allocationCount. Allocations are counts of allocated
objects, not bytes or retained size. These cases measure warmed-up construction/copy, not
composable method invocation, MaterialTheme resolution, remember hits, or Activity startup.

Microbenchmarks retain AndroidBenchmarkRunner, release tests, debug signing, no minification,
and default AndroidX AOT compilation. The Compose compiler remains necessary for V1's composable
callbacks; Compose correctness-test libraries and a test Activity are not needed.

### Allocated bytes per operation

Validate the deprecated ART allocation-size counter on the physical device before collecting bytes:

```shell
./gradlew :microbenchmark:connectedReleaseAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=com.alexcawl.contract.benchmark.test.AllocationBytesCaptureTest
```

The calibration warms up the counter, then checks three rounds of empty work, 1024-byte and
2048-byte arrays, and allocations excluded by pause/resume. Empty work must allocate at most
1 byte per operation; array-size differences and pause exclusion must agree within 1%.
A failed calibration means this device's byte measurements must not be used.

Collect bytes in a separate run of the same 12 contract cases:

```shell
./gradlew :microbenchmark:connectedReleaseAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.allocationBytes=true
```

With `allocationBytes=true`, each contract benchmark replaces its primary timing metric with
`AllocationBytesCapture`. AndroidX divides the captured bytes by the number of operations and
reports `allocationBytes` in bytes per operation in its JSON. Do not divide the result again.
AndroidX still collects `allocationCount` in its separate allocation phase. Without the argument,
the original `timeNs` and `allocationCount` measurements remain enabled.

The metric uses `Debug.startAllocCounting()`, `Debug.stopAllocCounting()`, and
`Debug.getGlobalAllocSize()`, subtracting allocations made while measurement is paused.
These APIs are deprecated and the signed 32-bit byte counter can overflow in long or large
allocation batches. The counter covers Java-heap allocations from all process threads, including
temporary objects and possible background work; it does not measure native allocations or
live/retained size. It cannot be reconstructed from `allocationCount` or an HPROF snapshot.

Use the ordinary run for timing comparisons. Dry-run mode skips custom metric collection, so
adding `allocationBytes=true` to a dry run checks configuration and scenarios only; the calibration
test calls the capture directly and checks the counter itself.

## Macrobenchmark: startup and frames

Run on a physical device with API 31 or later:

```shell
./gradlew :macrobenchmark:connectedBenchmarkAndroidTest
```

Activity1Macrobenchmark targets Activity1; Activity2Macrobenchmark targets Activity2.
Both are ordinary JUnit 4 classes. Each has three scenarios (six total), with ten iterations
and CompilationMode.Full():

- `coldStartup`: StartupTimingMetric, including initial display and ReportDrawn completion.
- `scroll`: FrameTimingMetric over five forward and five backward flings at the same speed.
- `copyAllUpdates`: FrameTimingMetric over ten appearance changes, rebuilding visible copies.

Select one Activity's class, or one scenario through ClassName#method:

```shell
./gradlew :macrobenchmark:connectedBenchmarkAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=com.alexcawl.contract.benchmark.macro.Activity1Macrobenchmark

./gradlew :macrobenchmark:connectedBenchmarkAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=com.alexcawl.contract.benchmark.macro.Activity2Macrobenchmark#scroll
```

Each iteration starts its Activity through an explicit Intent. Cold startup uses StartupMode.COLD;
frame tests kill the target process and launch outside measured work. Resource tags and revision
labels remain identical across the two private scenes for UI Automator synchronization.

The app benchmark build is non-debuggable, profileable, unminified, debug-signed, and includes
ProfileInstaller. The Macrobenchmark APK is a separate self-instrumenting process. Frame metrics
cover the UI journey, including Compose, layout, drawing, ripple, and rendering. They are not
isolated timings of copy or CPU recomposition; inspect Perfetto to attribute differences.
Full AOT and no R8 are controlled defaults, not predictions for every production build.

## Memory: heap dumps

Capture all four contract/version classes separately from timed benchmarks:

```shell
./gradlew :microbenchmark:connectedReleaseAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=com.alexcawl.contract.benchmark.test.v1.Size1HeapDumpTest,com.alexcawl.contract.benchmark.test.v1.Appearance1HeapDumpTest,com.alexcawl.contract.benchmark.test.v2.Size2HeapDumpTest,com.alexcawl.contract.benchmark.test.v2.Appearance2HeapDumpTest \
  -Pandroid.testInstrumentationRunnerArguments.heapDump=true
```

Each class covers one contract and version, with four separate @Test methods:
create, copy, copyAll, and chain32. There is no parameterized runner or operation dispatch.
Each class has one private inline appearance/size factory with value defaults; copy and chain
operations live directly in their @Test methods.
Without heapDump=true, @Before skips each method through assumeTrue before constructing samples.
Each class produces four named HPROF files (16 files total), preserving names such as
V1_Size_copyAll.hprof. Chain32 performs
32 full copies, supplying every callback each time. Keep at least 2 GB free for a full run;
these are full-process dumps rather than files containing only the sample.

The class filter can select one contract/version, or one method:

```shell
./gradlew :microbenchmark:connectedReleaseAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=com.alexcawl.contract.benchmark.test.v1.Size1HeapDumpTest#copyAll \
  -Pandroid.testInstrumentationRunnerArguments.heapDump=true
```

Only the corresponding class's static current field holds the final contract. The @After hook
dumps after the test method returns, so construction locals cannot root earlier sources.
Earlier sources stay live only when the final contract references them. The hook skips dumping
when current is null, including tests skipped by @Before. The ordinary internal dumpHeap(fileName)
function in HeapDumpUtils.kt handles the output directory, GC, HPROF capture, validation, and
instrumentation status. Each hook passes only the file name and clears current in finally,
including when capture fails.

Open a dump in Android Studio Profiler and inspect the corresponding static field:
Size1HeapDumpTest.current, Appearance1HeapDumpTest.current, Size2HeapDumpTest.current,
or Appearance2HeapDumpTest.current:

- Shallow Size is the final contract object's own Java heap size.
- Retained Size includes objects dominated by that contract: callbacks, wrappers, and source
  contracts without other live roots. Shared shapes and cached lambdas need not contribute.

V2's temporary Contract snapshot may be collected before capture. Its allocation belongs in
operation allocation counts, not live retained size. Results depend on ART, architecture, and
callback captures; these samples do not measure the entire UI heap.

## Reports

Gradle copies JSON, traces, and opt-in HPROF files below each test module's
build/outputs/connected_android_test_additional_output/. AGP chooses variant/device subdirectories.
When running instrumentation without additionalTestOutputDir, heap captures fall back to the test
APK's external-files heap-dumps directory; paths also appear in instrumentation status.

Compare on the same physical device, OS, refresh rate, and build configuration. Inspect repeated
runs and traces before attributing small differences to contract architecture. Do not suppress
benchmark environment checks for emulator performance numbers. Dry runs are infrastructure checks;
physical measurements and manual Profiler analysis remain separate steps.
