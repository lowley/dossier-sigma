pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }

    plugins {
        id("com.google.devtools.ksp") version "2.4.0-2.0.0" apply false
        id("com.android.application") version "8.10.0" // ou l'alias du catalogue
        kotlin("android") version "2.4.0"
    }
}

dependencyResolutionManagement { }

rootProject.name = "Dossier Sigma Master"
include(":app")


