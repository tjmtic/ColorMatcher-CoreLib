import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    `maven-publish`
}

// The domain core: pure Kotlin, no UI, no platform types. Apps reach the platform through the
// ports in `ports/`, implemented by adapters in the app repo. commonTest runs on the JVM (fast,
// the studio's section test) and on iOS.
group = "com.abyxcz.colormatcher.core"

version = (findProperty("libVersion") as String?) ?: "0.1.0"

kotlin {
    androidTarget {
        compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }
        publishLibraryVariants("release")
    }
    jvm()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies { implementation(libs.kotlinx.coroutines.core) }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}

android {
    namespace = "com.abyxcz.colormatcher.core"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig { minSdk = libs.versions.android.minSdk.get().toInt() }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

// Published on a v* tag by .github/workflows/release.yml. Consumers resolve
// com.abyxcz.colormatcher.core:core-lib from GitHub Packages (a read:packages token), or compile
// a sibling checkout of this repo as a composite build.
publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/tjmtic/ColorMatcher-CoreLib")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}
