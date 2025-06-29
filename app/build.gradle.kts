import com.android.build.api.dsl.JniLibsPackaging
import com.android.build.api.dsl.Ndk
import com.android.build.gradle.internal.api.artifact.SourceArtifactType
import org.jetbrains.kotlin.gradle.utils.IMPLEMENTATION

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("kotlin-kapt")
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

        ndk {
            abiFilters += listOf("arm64-v8a") // Cible uniquement l'architecture ARM64
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            sourceSets {
                getByName("release") {
                    jniLibs.srcDirs("src/main/jniLibs")

                }
            }

            debug {
                sourceSets {
                    getByName("debug") {
                        jniLibs.srcDirs("src/main/jniLibs")
                    }
                }
            }

        }
    }
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

    ndkVersion = "28.0.12916984" // Mettez la version correcte de votre NDK

//    externalNativeBuild {
//        cmake {
//            path = file("src/main/cpp/CMakeLists.txt") // Assurez-vous que ce fichier existe
//            version = "3.31.4" // Vérifiez la version CMake installée
//        }
//    }

    packagingOptions {
        jniLibs {
            useLegacyPackaging = true
            pickFirst("lib/arm64-v8a/libbento4.so")
        }
    }
    
    repositories{
        flatDir {
            dirs("libs")
        }
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
    kapt(libs.hilt.android.compiler)
    implementation(libs.accompanist.flowlayout)
    implementation("me.saket.cascade:cascade:2.3.0")
    implementation("me.saket.cascade:cascade-compose:2.3.0")
    implementation(libs.coil.compose)
    
//    implementation(libs.compressor)
    implementation(mapOf("name" to "ffmpeg-kit-full-gpl-6.0-2.LTS", "ext" to "aar"))
    implementation(files("libs/smart-exception-common-0.2.1.jar"))
    implementation(files("libs/smart-exception-java-0.2.1.jar"))
    
    
    implementation("com.squareup.okhttp3:okhttp:4.10.0")
    implementation("org.mp4parser:isoparser:1.9.27")
    implementation("com.github.mjeanroy:exiftool-lib:2.6.0")
    implementation("com.robertlevonyan.compose:buttontogglegroup:1.2.0")

    implementation("com.github.yalantis:ucrop:2.2.9-native")
    implementation("commons-net:commons-net:3.11.1")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.github.skydoves:colorpicker-compose:1.1.2")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
}

// Allow references to generated code
kapt {
    correctErrorTypes = true
}