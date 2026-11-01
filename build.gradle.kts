import io.gitlab.arturbosch.detekt.extensions.DetektExtension

plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinJvm) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.ktfmt) apply false
    alias(libs.plugins.detekt) apply false
}

// Fleet style standard: ktfmt (kotlinlang) owns layout, detekt at zero issues owns lint, and
// project rules live, tested, in :detekt-rules. Formatting is never a review subject.
subprojects {
    // A failing test prints its assertion message (kotlin.test: "Expected <x>, actual <y>"), not
    // just the exception class and line: the studio's repair loop reads this log, and so do people.
    tasks.withType<AbstractTestTask>().configureEach {
        testLogging {
            events("failed")
            exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
            showStackTraces = false
        }
    }

    apply(plugin = "com.ncorti.ktfmt.gradle")
    extensions.configure<com.ncorti.ktfmt.gradle.KtfmtExtension> { kotlinLangStyle() }
    // Generated sources (Compose resources' Res, …) are registered in source sets; not ours.
    tasks.withType<com.ncorti.ktfmt.gradle.tasks.KtfmtBaseTask>().configureEach {
        exclude { it.file.invariantSeparatorsPath.contains("/build/") }
    }

    apply(plugin = "io.gitlab.arturbosch.detekt")
    dependencies {
        "detektPlugins"(rootProject.libs.compose.rules.detekt)
        // Every project, :detekt-rules included, so the `fleet:` config block validates there too.
        "detektPlugins"(project(":detekt-rules"))
    }
    extensions.configure<DetektExtension> {
        source.setFrom(files("src")) // KMP: every source set, not src/main
        config.setFrom(files("$rootDir/config/detekt/detekt.yml"))
        buildUponDefaultConfig = true
        parallel = true
        basePath = rootDir.absolutePath
    }
}
