// The domain: pure Kotlin, no Android, no source. Entities, repository contracts, use cases.
plugins {
    alias(libs.plugins.kotlin.jvm)
}

kotlin {
    jvmToolchain(17)
    compilerOptions {
        // no null checks on parameters, calls and receivers at run time: the compiler already checked
        freeCompilerArgs.addAll("-Xno-param-assertions", "-Xno-call-assertions", "-Xno-receiver-assertions")
    }
}

dependencies {
    implementation(libs.coroutines.core)
    testImplementation(libs.junit)
    testImplementation(libs.coroutines.test)
}
