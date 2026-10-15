# Contract Plugin Benchmark

An independent Android project measuring V1, V2, and V3 Compose contracts. Each version has
Size and Appearance contracts and its own Card1/Card2/Card3 implementation. Surface, Column,
and elevation animation remain separate in those composables.

The project does not depend on the contract compiler plugin, its annotations, published
artifacts, or another checkout. Only the standard Kotlin Compose compiler is enabled.

## Modules and application

- `library`: contracts in library.v1/v2/v3 and the original Card implementations. Exports Material3.
- `app`: ActivityV1, ActivityV2, and ActivityV3. Each scene is a private composable in its
  Activity file, called only by that Activity. V1 is the launcher; start V2/V3 directly.
- `benchmark`: separate V1ContractBenchmark/V2ContractBenchmark/V3ContractBenchmark and
  V1ContractHeapDumpTest/V2ContractHeapDumpTest/V3ContractHeapDumpTest classes.
- `macrobenchmark`: self-instrumenting com.android.test module targeting app's benchmark build.

There are no shared scenes, factory wrappers, input-holder types, or contract correctness tests.
Scenes and benchmarks call the versioned factories and copy extensions directly, with identical
tokens and content repeated explicitly. The library contains only contracts and Card renderers.

Each Activity hosts 1000 identically sized cards in a LazyColumn. Visible/prefetched items
remember their own Size and Appearance copies from that Activity's base contracts by base
and revision. Both copy calls supply every callback. Scrolling away can dispose copies;
this is not 1000 eagerly retained contracts.

The appearance button alternates the same colors, shape, border, arrangement, and alignment
in every version. The switch changes enabled and disabled colors. The same interaction source
feeds Surface and pressed-state semantics. Both palettes use zero elevation for equivalent
visual behavior; original preset animations remain unchanged in Card1/Card2/Card3.

There is no navigation between benchmark Activities. Macrobenchmark starts its selected
Activity directly in a fresh process.

## Contract API

| Method | V1 | V2 | V3 |
| --- | --- | --- | --- |
| `contentArrangement(): Arrangement.Vertical` | Composable | Ordinary Kotlin | Ordinary Kotlin |
| `contentAlignment(): Alignment.Horizontal` | Composable | Ordinary Kotlin | Ordinary Kotlin |
| `shape(): Shape` | Composable | Ordinary Kotlin | Ordinary Kotlin |
| `containerColor(enabled: Boolean): Color` | Composable | Ordinary Kotlin | Ordinary Kotlin |
| `contentColor(enabled: Boolean): Color` | Composable | Ordinary Kotlin | Ordinary Kotlin |
| `shadowElevation(...): Dp` | Composable; receives enabled and InteractionSource | Ordinary Kotlin; receives enabled and nullable Interaction | Same signature as V2 |
| `border(): BorderStroke?` | Composable | Ordinary Kotlin | Ordinary Kotlin |
| `contract(): Contract` | Not provided | Not provided | Fresh callback snapshot |

Size presets use Arrangement.Top and Alignment.Start. Appearance presets use the current
MaterialTheme, with no border and Filled Card elevation targets. V1 uses CardDefaults;
V2 and V3 inline the Material3 1.4.0 Filled Card tokens.

Card1, Card2, and Card3 retain onClick, size, appearance, modifier, enabled, interactionSource,
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

V2 manually reproduces the historical architecture from contract-plugin commit
`4bfa8d0`. Size2Impl and Appearance2Impl are named final classes that store ordinary
callbacks without a receiver. Their explicit equals/hashCode compare those callbacks,
rather than the values returned by methods; hashing uses multiplier 31 without reflection.

Factories return the interface type. Appearance2 supplies factory defaults matching its
default shadowElevation and border bodies. Factories and copy extensions have JvmOverloads;
Size2's factory has no default parameters, so only its copy generates additional overloads.

Copy defaults are bound references such as this::shape and this::containerColor. Copying
works with any implementation of the interface. Unspecified methods continue to execute
on the original receiver, even when other methods are overridden in the copy. Copy chains
retain this delegation, and original.copy() can compare unequal to original because
method references replace the original callbacks. Explicitly supplying the same callbacks
preserves callback equality. V2 has no wrapper receiver or rememberCopy helper.

Appearance2Presets.default directly resolves MaterialTheme.shapes.medium and four theme colors.
The container uses surfaceContainerHighest. The disabled container composites surfaceVariant
at alpha 0.38 over surfaceContainerHighest; disabled content also uses alpha 0.38.
Content color is resolved from surfaceContainerHighest with ColorScheme.contentColorFor.
The preset uses shape, containerColor, contentColor, disabledContainerColor, and disabledContentColor
as remember keys, creating a new contract when any of those values changes. Callbacks capture
the individual values directly. Existing copies remain bound to their original
contract; recreate remembered copies with remember(original), as in the example below,
to follow theme changes. V2 contract methods never call composable APIs themselves.

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

## V3 semantics

V3 manually follows the current contract-plugin runtime architecture. Its factories accept
ordinary receiver callbacks such as Size3.() -> Arrangement.Vertical and
Appearance3.(Boolean) -> Color. Private final implementations invoke callbacks with the
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
For example, using V3 imports:

```kotlin
val original = Appearance3(
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
does not cast them to the private Impl. V3 follows the current factory/copy API without
JvmOverloads or versioned ABI overloads. It has no wrapper receiver or rememberCopy helper.

V3 presets use the same explicit tokens and five remember keys as V2. Callback captures
contain only shape and the four resolved colors. A theme change produces a new preset;
remember copies with their source as a key to pick up those new captures.

## V2/V3 elevation

Appearance2/Appearance3.shadowElevation(enabled, interaction) returns a target Dp. A null interaction
means idle. Default targets are 0.dp when disabled, 1.dp for hover, 6.dp for drag, and 0.dp
for idle, press, focus, or other interactions.

The independent private helpers in Card2 and Card3 collect active press, hover, focus, and drag
interactions. End and cancel events remove the matching start interaction. Each helper passes
the latest active interaction, or null, to its Appearance and animates the returned target
with V1's timings and easing.
Disabling snaps to the contract's returned target, including a custom nonzero disabled target.
V1's shadowElevation(enabled, interactionSource) and its animation remain unchanged.

## Build and verification

Use JDK 21, Android SDK Platform 36, Build Tools 36.0.0, and JVM target 17. Set ANDROID_HOME
or an ignored local.properties containing sdk.dir. App/library/benchmark minSdk is 23;
Macrobenchmark minSdk is 31. Dependency versions live in gradle/libs.versions.toml.

Build, run lint, and assemble all benchmark APKs without a device:

```shell
./gradlew build :app:assembleDebug :app:assembleBenchmark \
  :benchmark:assembleReleaseAndroidTest :macrobenchmark:assembleBenchmark --continue --max-workers=1
```

Install the app and start each screen independently:

```shell
./gradlew :app:installDebug
adb shell am force-stop com.alexcawl.contract.benchmark.app
adb shell am start -W -n com.alexcawl.contract.benchmark.app/.v1.ActivityV1
adb shell am force-stop com.alexcawl.contract.benchmark.app
adb shell am start -W -n com.alexcawl.contract.benchmark.app/.v2.ActivityV2
adb shell am force-stop com.alexcawl.contract.benchmark.app
adb shell am start -W -n com.alexcawl.contract.benchmark.app/.v3.ActivityV3
```

These adb timings are smoke checks, not substitutes for Macrobenchmark results.

Exercise the existing benchmark scenarios on a device or emulator without collecting performance
results. Heap captures remain opt-in:

```shell
./gradlew :benchmark:connectedReleaseAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.androidx.benchmark.dryRunMode.enable=true
./gradlew :macrobenchmark:connectedBenchmarkAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.androidx.benchmark.dryRunMode.enable=true
```

This project measures contracts; it does not test their correctness. Build, lint, and dry runs
verify that the measurement scenarios remain runnable.

## Microbenchmark: creation and copy

Run all three version classes on a physical device:

```shell
./gradlew :benchmark:connectedReleaseAndroidTest
```

Select one version when needed:

```shell
./gradlew :benchmark:connectedReleaseAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=com.alexcawl.contract.benchmark.test.v1.V1ContractBenchmark
```

Use V2ContractBenchmark or V3ContractBenchmark similarly. Each class has six cases: Size and
Appearance, each with create, unchanged copy, and full copy (18 cases total):

- `create`: direct factory invocation, including callback construction.
- `copy`: unchanged copy using that version's default-copy implementation.
- `copyAll`: direct copy with every callback replaced. Size supplies contentArrangement and
  contentAlignment; Appearance supplies shape, containerColor, contentColor, shadowElevation, and border.

Tokens and source contracts are prepared outside timed work. Factories/copy are called directly
inside measured blocks. Their callbacks capture local token values, not the benchmark instance.
BlackHole consumes the result so it cannot be discarded as unused.

AndroidX warms up and reports timeNs and allocationCount. Allocations are counts of allocated
objects, not bytes or retained size. These cases measure warmed-up construction/copy, not
composable method invocation, MaterialTheme resolution, remember hits, or Activity startup.

Microbenchmarks retain AndroidBenchmarkRunner, release tests, debug signing, no minification,
and default AndroidX AOT compilation. The Compose compiler remains necessary for V1's composable
callbacks; Compose correctness-test libraries and a test Activity are not needed.

## Macrobenchmark: startup and frames

Run on a physical device with API 31 or later:

```shell
./gradlew :macrobenchmark:connectedBenchmarkAndroidTest
```

Each version retains three scenarios, each with ten iterations and CompilationMode.Full():

- `coldStartup`: StartupTimingMetric, including initial display and ReportDrawn completion.
- `scroll`: FrameTimingMetric over five forward and five backward flings at the same speed.
- `copyAllUpdates`: FrameTimingMetric over ten appearance changes, rebuilding visible copies.

Each iteration starts its Activity through an explicit Intent. Cold startup uses StartupMode.COLD;
frame tests kill the target process and launch outside measured work. Resource tags and revision
labels remain identical across the three private scenes for UI Automator synchronization.

The app benchmark build is non-debuggable, profileable, unminified, debug-signed, and includes
ProfileInstaller. The Macrobenchmark APK is a separate self-instrumenting process. Frame metrics
cover the UI journey, including Compose, layout, drawing, ripple, and rendering. They are not
isolated timings of copy or CPU recomposition; inspect Perfetto to attribute differences.
Full AOT and no R8 are controlled defaults, not predictions for every production build.

## Memory: heap dumps

Capture all three version classes separately from timed benchmarks:

```shell
./gradlew :benchmark:connectedReleaseAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=com.alexcawl.contract.benchmark.test.v1.V1ContractHeapDumpTest,com.alexcawl.contract.benchmark.test.v2.V2ContractHeapDumpTest,com.alexcawl.contract.benchmark.test.v3.V3ContractHeapDumpTest \
  -Pandroid.testInstrumentationRunnerArguments.heapDump=true
```

The class filter can select just one version. Each class produces eight named HPROF files:
Size and Appearance, with create, copy, copyAll, and chain32 (24 files total). Chain32 performs
32 full copies, supplying every callback each time. Keep at least 2 GB free for a full run;
these are full-process dumps rather than files containing only the sample.

Only the corresponding class's static current field holds the final contract. Construction
returns before dumping, so earlier sources stay live only when the final contract references
them. Each capture invokes GC and clears current in finally.

Open a dump in Android Studio Profiler and inspect V1ContractHeapDumpTest.current,
V2ContractHeapDumpTest.current, or V3ContractHeapDumpTest.current:

- Shallow Size is the final contract object's own Java heap size.
- Retained Size includes objects dominated by that contract: callbacks, wrappers, and source
  contracts without other live roots. Shared shapes and cached lambdas need not contribute.

V3's temporary Contract snapshot may be collected before capture. Its allocation belongs in
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
