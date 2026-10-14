# Contract Plugin Benchmark

A standalone Android project for comparing the performance and usability of different
contract implementations in Compose. Every version has exactly two primary contracts:
Size for geometry and Appearance for drawing style. All versions resolve their contracts
into explicit values for one shared Material3 Card renderer.

The current implementation contains V1 and a runnable demonstration. Comparative
performance workloads are a separate step. The infrastructure allocation benchmark only
checks the runner and reporting; it does not measure contract performance.

The project does not apply or depend on a contract compiler plugin, its annotations, local
checkouts, or published artifacts. The standard Kotlin Compose compiler is enabled.

## Modules

- `library`: shared Card renderer and state in `library.common`, manually implemented
  Taiga-style contracts and wrappers in `library.v1`, and the interactive scene in
  `library.demo`. It exports Material3 and the current Compose BOM.
- `app`: Android application with a launcher MainActivity hosting the scene in MaterialTheme.
- `benchmark`: Android library with its own BenchmarkActivity, Compose correctness tests,
  and AndroidX Microbenchmark infrastructure.

Both app and benchmark depend directly on library. The common renderer has no dependency
on V1; a versioned wrapper only observes shared interaction state, resolves its contracts,
and forwards the values and content to that renderer.

## Card contracts

All contract methods are composable. Some accept immutable CardState (`enabled`, `pressed`);
others have no input parameters, so both signatures can be compared across versions.

| Contract | Method | Responsibility |
| --- | --- | --- |
| Size | `shape(state): Shape` | Shape and corner rounding |
| Size | `contentPadding(state): PaddingValues` | Internal padding |
| Size | `spacing(): Dp` | Vertical spacing between content elements |
| Appearance | `colors(state): CardColors` | Container and inherited content colors |
| Appearance | `border(state): BorderStroke?` | Border |
| Appearance | `elevation(): CardElevation` | M3 elevation, including interaction feedback |

Defaults use the current MaterialTheme through CardDefaults, with no border, zero internal
padding, and zero spacing. Each version must map the same inputs to the same renderer.
The renderer calls M3 Card and hosts arbitrary content in a shared Column with the resolved
padding and spacing; its ColumnScope is passed to the caller.

`library.v1.Card` has non-clickable and clickable overloads. They retain M3's modifier,
content, and (for the clickable overload) onClick, enabled, and interactionSource parameters,
replacing explicit styling with Size and Appearance. Text, badges, icons, and their typography
belong to caller-supplied content.

The clickable wrapper creates a remembered MutableInteractionSource when none is supplied.
The shared state collector observes the exact source passed to M3. Press, release, and cancel
update both contracts; disabling the card makes its effective pressed state false and M3
prevents clicks. Non-clickable cards resolve contracts with enabled=true and pressed=false.

For example, inside a MaterialTheme composition:

```kotlin
val size = Size.Default.rememberCopy(
    contentPadding = { state -> PaddingValues(if (state.pressed) 12.dp else 16.dp) },
    spacing = { 8.dp },
)
val appearance = Appearance.Default.rememberCopy(
    colors = { state ->
        wrapped.colors(state).copy(
            containerColor = if (state.pressed) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.secondaryContainer
            },
        )
    },
)
Card(onClick = { /* caller-owned action */ }, size = size, appearance = appearance) {
    Text("Arbitrary content")
    Text("Geometry and style come from V1")
}
```

Import Card, Size, Appearance, and rememberCopy from
`com.alexcawl.contract.benchmark.library.v1`.

## V1 semantics

Both contracts provide a Default companion, inline factories with composable receiver
lambdas, and copy/rememberCopy overrides with a Wrapper.wrapped receiver. Use
`wrapped.method(...)` to call the original implementation; calling the overridden method
on this recurses.

rememberCopy deliberately uses remember without keys. It retains the first receiver and
override captures. Read a captured State or rememberUpdatedState when values must change,
or key the surrounding composition when a new copy is needed. Composable default methods
continue to read the current MaterialTheme.

ReflectiveEqualsHashCode retains the original V1 algorithm: it compares every declared
captured field of anonymous implementations of the same class, including treating two
null reference fields in distinct instances as unequal. Equality describes implementation
classes and captures, rather than the values returned by composable methods.

## Requirements and demo

Use JDK 21, the included Gradle wrapper, Android SDK Platform 36, and Build Tools 36.0.0.
Set ANDROID_HOME or an ignored local.properties containing sdk.dir. Minimum API is 23;
bytecode targets JVM 17. Versions are centralized in gradle/libs.versions.toml.

Run the app from Android Studio, or install and open its debug build:

```shell
./gradlew :app:installDebug
adb shell am start -n com.alexcawl.contract.benchmark.app/.MainActivity
```

The scene shows a standard non-clickable card, an interactive card with V1 overrides,
and a disabled card using the same overrides. Holding the interactive card changes padding,
rounding, background, and border. Clicks update a local counter; the switch disables
interaction. The scene and its correctness counters are never benchmark workloads.

## Verification and benchmarks

Build the project, demo APK, and release instrumentation APK:

```shell
./gradlew build :app:assembleDebug :benchmark:assembleReleaseAndroidTest --continue
```

Run correctness tests and exercise the infrastructure without collecting metrics:

```shell
./gradlew :benchmark:connectedReleaseAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.androidx.benchmark.dryRunMode.enable=true
```

Tests cover factory defaults, receiver calls, copying, V1 equality, theme changes,
content layout and rendering, real press/release/cancel transitions, disabling during
a press, and demonstration interaction.

Run the existing infrastructure benchmark on a physical device:

```shell
./gradlew :benchmark:connectedReleaseAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=com.alexcawl.contract.benchmark.test.InfrastructureBenchmark
```

The benchmark module retains AndroidBenchmarkRunner, release tests signed with the debug key,
no minification, and the benchmark plugin's default AOT compilation. Do not disable environment
checks for emulator timing. Dry-run execution on an emulator only checks correctness and
execution, not performance. Normal benchmark reports appear under
`benchmark/build/outputs/connected_android_test_additional_output/`.
