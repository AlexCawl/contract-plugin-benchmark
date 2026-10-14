# Contract Plugin Benchmark

A standalone Android Microbenchmark project for comparing approaches to implementing
Appearance objects and using them in Compose. It does not apply or depend on a contract
compiler plugin, annotation library, local checkout, or published contract artifacts.
The standard Kotlin Compose compiler is enabled for Compose code.

The project contains a shared ActionCard renderer and a manually implemented V1 contract
API based on the Taiga UI approach. Comparative performance workloads are a separate step.
The infrastructure benchmark only verifies that the runner and reporting work;
its object allocation results do not describe contract performance.

## Modules

- `library`: Android library containing shared state in `library.common` and V1 Size/Appearance
  contracts, factories, copying, and reflective equality in `library.v1`.
- `app`: Android library containing the `BenchmarkActivity` test host, an interactive
  demonstration scene, the shared renderer in `app.common`, and its V1 composable wrapper
  in `app.v1`. It exports `library` through an `api` dependency.
- `benchmark`: Android library containing AndroidX Microbenchmark and Compose instrumentation
  tests. It depends on `app` and standard testing libraries, and accesses contracts through
  the API dependency exported by `app`.

All three modules use `com.android.library`; `app` is not a standalone launcher application.
Tests use the release variant, debug signing, no minification, and the benchmark plugin's
default AOT compilation.

## ActionCard and V1

`app.common.ActionCard` takes every visual parameter explicitly and has no V1 contract dependency.
It renders a title, optional description and badge, and optional leading/trailing slots.
Slots receive their resolved color. `library.common.ActionCardState` carries `enabled` and `selected`;
disabled cards do not invoke their click handler. Titles and badges use one line, descriptions
use up to two, and overflowing text is ellipsized. Null optional elements reserve no space.

`app.v1.ActionCard` resolves two stable contracts from `library.v1` and delegates to the common renderer:

- `ActionCardSize`: minimum height, padding, spacing, slot sizes, and badge padding.
- `ActionCardAppearance`: shapes, background brushes, border, slot colors, and all three text styles.

The package roots are `com.alexcawl.contract.benchmark.app` and
`com.alexcawl.contract.benchmark.library`. Dependency direction is `benchmark` → `app` → `library`;
the shared state resides in `library` so contracts can be compiled independently of the UI.

Both contracts expose composable state-dependent methods, a `Default` companion, an inline
factory with composable receiver lambdas, and `copy`/`rememberCopy` with a `Wrapper.wrapped`
receiver. Call `wrapped.method(state)` inside an override to access the original; calling
the overridden method on `this` recurses.

The V1 implementation deliberately retains the supplied `ReflectiveEqualsHashCode`
algorithm, including comparing all declared captured fields and treating two null reference
fields in distinct instances as unequal. Equality depends on the anonymous implementation
class and captures, rather than the values returned by composable methods.

`rememberCopy` uses `remember` without keys: it retains the first receiver and override
lambdas. Read a captured `State` or `rememberUpdatedState` for changing values, or explicitly
key the surrounding composition if the copy must be recreated. The demonstration shows
default, selected/customized, and disabled cards without introducing measured workloads.

## Requirements

- JDK 21 and the included Gradle 9.6.1 wrapper.
- Android SDK Platform 36 and Build Tools 36.0.0.
- `ANDROID_HOME` pointing to the SDK, or an ignored `local.properties` containing
  `sdk.dir=/path/to/android/sdk`.
- Android device or emulator with API 23 or higher for instrumentation tests.

Versions are centralized in `gradle/libs.versions.toml`: Kotlin 2.4.20, AGP 9.4.1,
Compose BOM 2026.02.01, AndroidX Benchmark 1.5.0, and Espresso 3.7.0.
AGP supplies built-in Kotlin support; the root Kotlin JVM plugin declaration selects the
same Kotlin version for the Android modules without applying a legacy Kotlin Android plugin.

## Build

Build and assemble the test APK without a connected device:

```shell
./gradlew build :benchmark:assembleReleaseAndroidTest --continue
```

## Check correctness and infrastructure

Run the correctness tests and infrastructure benchmark on a device or emulator without
collecting performance results:

```shell
./gradlew :benchmark:connectedReleaseAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.androidx.benchmark.dryRunMode.enable=true
```

Run only the demonstration scene test:

```shell
./gradlew :benchmark:connectedReleaseAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=com.alexcawl.contract.benchmark.test.BenchmarkSceneTest
```

Run the infrastructure benchmark on a physical device:

```shell
./gradlew :benchmark:connectedReleaseAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=com.alexcawl.contract.benchmark.test.InfrastructureBenchmark
```

Performance measurements require a physical device for meaningful results. Dry-run mode
checks execution without collecting timing or allocation results. Normal benchmark results
appear in the test log and JSON reports under
`benchmark/build/outputs/connected_android_test_additional_output/`.
