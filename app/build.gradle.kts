import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "lorry.folder.items.dossiersigma"
    compileSdk = 35

    defaultConfig {
        applicationId = "lorry.folder.items.dossiersigma"
        minSdk = 33
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    tasks.withType<KotlinCompile>().configureEach {
        compilerOptions {
            freeCompilerArgs.add("-Xcontext-receivers")
        }
    }

//    tasks.withType<KotlinCompile>().configureEach {
//        compilerOptions {
//            freeCompilerArgs.add("-Xcontext-parameters")
//        }
//    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)
    implementation(libs.accompanist.flowlayout)
    implementation("me.saket.cascade:cascade:2.3.0")
    implementation("me.saket.cascade:cascade-compose:2.3.0")
    implementation(libs.coil.compose)
    implementation(libs.coil.svg)

//    implementation(libs.compressor)

    implementation("com.squareup.okhttp3:okhttp:4.10.0")
    implementation("org.mp4parser:isoparser:1.9.27")
    implementation("com.github.mjeanroy:exiftool-lib:2.6.0")
    implementation("com.robertlevonyan.compose:buttontogglegroup:1.2.0")

    implementation("com.github.yalantis:ucrop:2.2.9-native")
    implementation("commons-net:commons-net:3.11.1")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.github.skydoves:colorpicker-compose:1.1.2")
    implementation(libs.kotlinx.serialization.json)
    implementation("com.google.android.material:material:1.12.0")
    //memoEditor
    implementation("com.mohamedrejeb.richeditor:richeditor-compose:1.0.0-rc13")
    implementation("com.github.Shivamdhuria:palette:0.0.4")

    implementation("androidx.datastore:datastore-preferences:1.1.1")
    implementation("androidx.appcompat:appcompat:1.6.1")

    //pull to refresh
    //https://github.com/MateriiApps/pullrefresh
    implementation("dev.materii.pullrefresh:pullrefresh:1.4.0-beta03")

    //FAB matérial 3
    //https://github.com/ch4rl3x/SpeedDialFloatingActionButton
    implementation ("de.charlex.compose:speeddial-bottomappbar-material3:1.1.1")

    //FAB material 2
    //https://github.com/ch4rl3x/SpeedDialFloatingActionButton
    implementation("de.charlex.compose:speeddial:1.1.1")

    //générer un thème
    //https://github.com/KvColorPalette/KvColorPalette-Android
    implementation("com.github.KvColorPalette:KvColorPalette-Android:3.1.0")

    implementation("com.arkivanov.decompose:decompose:2.1.1")
    implementation("com.arkivanov.decompose:extensions-compose-jetbrains:2.1.1")
}
