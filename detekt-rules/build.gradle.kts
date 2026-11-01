plugins { alias(libs.plugins.kotlinJvm) }

// Project rules for detekt, unit-tested. Tooling only: consumed through `detektPlugins`, never
// on a product classpath. A review nit made twice becomes a rule here, with a test.
kotlin { jvmToolchain(17) }

dependencies {
    compileOnly(libs.detekt.api)
    testImplementation(libs.detekt.api)
    testImplementation(libs.detekt.test)
    testImplementation(libs.kotlin.test)
}

tasks.test { useJUnitPlatform() }
