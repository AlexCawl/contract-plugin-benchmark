# Contract Plugin Benchmark

A standalone Android Microbenchmark project for comparing approaches to implementing
Appearance objects and using them in Compose. It does not apply or depend on a contract
compiler plugin, annotation library, local checkout, or published contract artifacts.
The standard Kotlin Compose compiler is enabled for Compose code.

This initial scaffold contains no Appearance implementations or comparative workloads.
It provides a Compose host, a UI smoke test, and an infrastructure benchmark that allocates
an object. The infrastructure benchmark only verifies that the runner and reporting work;
its results do not describe Appearance performance.

## Modules

- `app`: Android library containing the empty `BenchmarkActivity` test host and a placeholder
  Compose scene. Future Appearance implementations and shared UI belong here.
- `benchmark`: Android library containing AndroidX Microbenchmark and Compose instrumentation
  tests. It depends only on `app` and standard testing libraries.

Both modules use `com.android.library`; `app` is not a standalone launcher application.
Tests use the release variant, debug signing, no minification, and the benchmark plugin's
default AOT compilation.

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

## Check the scaffold

Run both tests on a device or emulator without collecting performance results:

```shell
./gradlew :benchmark:connectedReleaseAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.androidx.benchmark.dryRunMode.enable=true
```

Run only the Compose smoke test:

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
