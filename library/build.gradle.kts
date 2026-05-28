plugins {
    alias(libs.plugins.android.library)
    kotlin("plugin.serialization") version "2.3.20"
    id("maven-publish")
}

android {
    namespace = "com.ayazalvi.framework"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }
    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }

    defaultConfig {
        minSdk = 26

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    viewBinding.enable = true

}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                // Tells Maven to include the AAR file of your library
                from(components["release"])

                groupId = "com.github.ayazalvi"
                artifactId = "framework"
                version = "1.0.1" // Change this for every new release
            }
        }
    }
}

dependencies {
    implementation(libs.androidx.activity.ktx)
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.11.0")
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}