pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }

    plugins {
        id("com.google.devtools.ksp") version "2.2.0-2.0.2" apply false
        id("com.android.application") version "8.10.0" // ou l'alias du catalogue
        kotlin("android") version "2.2.0"
    }
}

dependencyResolutionManagement { }

rootProject.name = "Dossier Sigma"
include(":app")


