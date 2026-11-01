pluginManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

rootProject.name = "kmp-app-template"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

// ---- ViewPoint support libraries ---------------------------------------------------------
// Uncomment the ones this app uses, then depend on them through the catalog (viewpoint-*).
// A sibling checkout at ../<dir> is compiled in as a composite build (edit lib and app
// together). Without one (CI, a fresh clone) or with -PuseLocalLibs=false, the published
// artifact comes from GitHub Packages, which needs a token with read:packages: GPR_USER /
// GPR_KEY in the environment, or gpr.user / gpr.key in ~/.gradle/gradle.properties.
// Each library has three names that disagree (dir / repo / group): join on group:artifact.
// A composite build tolerates exactly one AGP version: keep `agp` in lockstep with the libs.
data class ViewPointLib(val dir: String, val packagesRepo: String?)

val viewpointLibs =
    listOf<ViewPointLib>(
        // ViewPointLib("ViewPoint-CameraLib", "abyxcz/KMP-CameraLib"), // com.abyxcz.camera:camera-lib
        // ViewPointLib("ViewPoint-UILib", "abyxcz/KMP-UILib"), // com.abyxcz.viewpoint.ui.library:ui-lib
        // ViewPointLib("ViewPoint-PermissionsLib", "abyxcz/KMP-PermissionsLib"), // com.abyxcz.viewpoint.permissions:permissions-lib
        // ViewPointLib("ViewPoint-LocationLib", "tjmtic/KMP-Locations"), // com.abyxcz.viewpoint.location:location-lib
        // ViewPointLib("ViewPoint-StorageLib", "abyxcz/ViewPoint-StorageLib"), // com.abyxcz.viewpoint.storage:storage-lib
        // ViewPointLib("ViewPoint-HapticLib", "tjmtic/KMP-Haptic"), // com.abyxcz.viewpoint.haptic:haptic-lib
        // ViewPointLib("Viewpoint-Sensor", null), // com.viewpoint:sensor — not published: sibling checkout only
    )

val useLocalLibs = providers.gradleProperty("useLocalLibs").map { it.toBoolean() }.getOrElse(true)

viewpointLibs.forEach { lib ->
    val checkout = settingsDir.resolve("../${lib.dir}")
    if (useLocalLibs && checkout.isDirectory) includeBuild(checkout)
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        viewpointLibs.mapNotNull { it.packagesRepo }.forEach { repo ->
            maven {
                name = "GitHubPackages-${repo.substringAfter('/')}"
                url = uri("https://maven.pkg.github.com/$repo")
                credentials {
                    username = System.getenv("GPR_USER") ?: providers.gradleProperty("gpr.user").orNull ?: ""
                    password = System.getenv("GPR_KEY") ?: providers.gradleProperty("gpr.key").orNull ?: ""
                }
            }
        }
    }
}

include(":composeApp", ":shared", ":detekt-rules")
