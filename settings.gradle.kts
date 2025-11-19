pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement { }

rootProject.name = "Dossier Sigma Master"
include(":app")

includeBuild("../../IdeaProjects/Périscope")
include(":bottombar")
