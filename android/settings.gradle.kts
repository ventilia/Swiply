pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "swiply"

include(":app")
include(":core:core-model")
include(":core:core-common")
include(":core:core-ui")
include(":core:core-network")
include(":core:core-database")
include(":core:core-datastore")
include(":feature:feature-onboarding")
include(":feature:feature-auth")
include(":feature:feature-profile")
include(":feature:feature-discovery")
include(":feature:feature-match")
include(":feature:feature-chat")
include(":feature:feature-settings")
