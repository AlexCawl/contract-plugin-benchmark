# Contract Plugin Benchmark

A standalone Android project for comparing the performance and allocations of versioned
Compose contracts. The library contains V1, V2, and V3, each with Size and Appearance contracts
and its own Card implementation. Card1, Card2, and Card3 use Surface and Column with equivalent
default geometry, colors, and interaction feedback.

Comparative performance workloads are a separate step. The infrastructure allocation
benchmark only checks the runner and reporting; it does not measure contract performance.

The project does not apply or depend on a contract compiler plugin, its annotations, local
checkouts, or published artifacts. The standard Kotlin Compose compiler is enabled.

## Modules

- `library`: V1, V2, and V3 contracts, factories, presets, and Card composables. It exports
  Material3 and the current Compose BOM.
- `app`: launcher application; its previous demonstration still needs migration.
- `benchmark`: separate BenchmarkActivity, correctness tests, and AndroidX
  Microbenchmark infrastructure. Legacy UI tests still need migration.

Both app and benchmark depend directly on library.

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
Actual allocation measurements remain a separate step.

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

Use JDK 21, Android SDK Platform 36, and Build Tools 36.0.0. Set ANDROID_HOME or an ignored
local.properties containing sdk.dir. Minimum API is 23; bytecode targets JVM 17.
Versions are centralized in gradle/libs.versions.toml.

Build the library, run its local JVM tests, and check Android lint:

```shell
./gradlew :library:build
```

V2 JVM tests cover factory and implementation behavior, default elevation and border,
nullable interaction and enabled forwarding, callback equality/hashCode, delegated copy
chains, custom interface implementations, and Java overloads.

V3 JVM tests additionally cover rebinding across method dependencies and copy chains,
unchanged-copy equality, null override semantics, callback identity, fresh containers,
one snapshot per copy, manual implementations, and preserved external captures.

The app and legacy UI tests already reference the previous demo/contract API. Migrating
them is separate work, so the full project build and connected UI tests are not part of V2/V3
verification. No visual parity or animation timing measurements have been collected.

## Benchmark infrastructure

After the legacy UI tests are migrated, exercise correctness and infrastructure without
collecting performance metrics:

```shell
./gradlew :benchmark:connectedReleaseAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.androidx.benchmark.dryRunMode.enable=true
```

Run performance measurements on a physical device. The benchmark module retains
AndroidBenchmarkRunner, release tests signed with the debug key, no minification, and the
benchmark plugin's default AOT compilation. Do not disable benchmark environment checks
to obtain emulator timings. Reports appear under
`benchmark/build/outputs/connected_android_test_additional_output/`.
