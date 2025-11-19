import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "lorry.folder.items.dossiersigma.bottombar"
    compileSdk = 36

    defaultConfig {
        minSdk = 33
        targetSdk = 34

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
        freeCompilerArgs = listOf("-XXLanguage:+PropertyParamAnnotationDefaultTargetMode")
    }

    packaging {
        resources {
            excludes += "/META-INF/LICENSE.md"
            excludes += "/META-INF/LICENSE-notice.md"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // --- Unit tests (testImplementation) ---
    testImplementation(libs.junit)
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
    testImplementation("app.cash.turbine:turbine:1.1.0")        // test des Flow
    testImplementation("io.mockk:mockk:1.13.12")
    //tester si méthode existe
    testImplementation("org.jetbrains.kotlin:kotlin-reflect:2.0.21")

    // --- Android instrumented tests (androidTestImplementation) ---
    androidTestImplementation(libs.androidx.junit)              // AndroidX JUnit 1.2.1
    androidTestImplementation(platform(libs.androidx.compose.bom)) // BOM Compose 2025.04.01
    androidTestImplementation(libs.androidx.ui.test.junit4)     // Tests UI Compose
    debugImplementation(libs.androidx.ui.test.manifest)         // Manifeste de test Compose
    androidTestImplementation("io.mockk:mockk-android:1.13.12")
    androidTestImplementation("app.cash.turbine:turbine:1.1.0")
    androidTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
    //tester si méthode existe
    androidTestImplementation("org.jetbrains.kotlin:kotlin-reflect:2.0.21")

    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.compose.livedata)
    implementation(libs.androidx.lifecycle.service)

    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation(libs.accompanist.flowlayout)
    implementation("me.saket.cascade:cascade:2.3.0")
    implementation("me.saket.cascade:cascade-compose:2.3.0")
    implementation(libs.coil.compose)
    implementation(libs.coil.svg)

    implementation("com.github.yalantis:ucrop:2.2.9-native")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation(libs.kotlinx.serialization.json)
    implementation("androidx.appcompat:appcompat:1.6.1")
//    implementation("com.google.android.material:material:1.12.0")


    /////////////////////////
    // palette de couleurs //
    /////////////////////////
    implementation("com.github.Shivamdhuria:palette:0.0.4")

    //générer un thème
    //https://github.com/KvColorPalette/KvColorPalette-Android
    implementation("com.github.KvColorPalette:KvColorPalette-Android:3.1.0")

    implementation("com.arkivanov.decompose:decompose:2.1.1")
    implementation("com.arkivanov.decompose:extensions-compose-jetbrains:2.1.1")

    //////////
    // koin //
    //////////
    implementation("io.insert-koin:koin-android:4.2.0-alpha1")

    /////////////////////////////////
    // programmation fonctionnelle //
    /////////////////////////////////
    implementation("io.arrow-kt:arrow-core:1.2.4")


}