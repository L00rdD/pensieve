// The app: presentation (MVI view models, the three looks' views) and the injectors.
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.l0rdd.pensieve"
    compileSdk = 36
    defaultConfig {
        applicationId = "com.l0rdd.pensieve"
        minSdk = 31
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0"
        // where the house lives; the projector cannot resolve .local names
        buildConfigField("String", "HUB_URL", "\"http://192.168.1.229:8582\"")
        buildConfigField("String", "FEED_URL", "\"http://192.168.1.229/screen.json\"")
        buildConfigField("String", "JELLYFIN_URL", "\"http://192.168.1.229:8096\"")
        buildConfigField("String", "CINEMA_OUTLETS", "\"basketball-neon\"")
    }
    buildFeatures {
        buildConfig = true
    }
    buildTypes {
        release {
            // R8 in full mode (gradle.properties) with the optimizing rules: code inlined and
            // shrunk, resources dropped, no debugging hooks; the debug key, so it installs over
            // the debug build and keeps the projector's preferences
            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = false
            isJniDebuggable = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    sourceSets["main"].java.srcDir("src/main/kotlin")
    sourceSets["test"].java.srcDir("src/test/kotlin")
}

kotlin {
    jvmToolchain(17)
    compilerOptions {
        // no null checks on parameters, calls and receivers at run time: the compiler already checked
        freeCompilerArgs.addAll("-Xno-param-assertions", "-Xno-call-assertions", "-Xno-receiver-assertions")
    }
}

dependencies {
    implementation(project(":domain"))
    implementation(project(":data"))
    implementation(libs.coroutines.android)
    implementation(libs.activity)
    implementation(libs.lifecycle.viewmodel)
    implementation(libs.lifecycle.runtime)
    testImplementation(libs.junit)
    testImplementation(libs.coroutines.test)
}
