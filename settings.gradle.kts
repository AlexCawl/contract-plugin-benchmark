pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "contract-plugin-benchmark"

include("app")
include("microbenchmark")
include("library")
include("macrobenchmark")
