plugins {
    kotlin("multiplatform")
    kotlin("native.cocoapods")
    kotlin("plugin.serialization")
    id("com.android.library")
    id("kotlinx-serialization")
    id("app.cash.sqldelight")
    id("co.touchlab.skie") version "0.5.6"
}
kotlin {
    androidTarget()
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    applyDefaultHierarchyTemplate()
    cocoapods {
        summary = "Some description for the Shared Module"
        homepage = "Link to the Shared Module homepage"
        version = "1.0"
        ios.deploymentTarget = "17.0.1"
        podfile = project.file("../iosApp/Podfile")
        extraSpecAttributes["resources"] = "['src/commonMain/resources/**']"
        framework {
            baseName = "shared"
            binaryOption("bundleId", "com.example.emobilitychargingstations")
        }
    }

    sourceSets {
        val ktorVersion = "2.3.9"
        val sqlDelightVersion = "2.0.0"
        val koinVersion = "3.5.0"
        val arrowVersion = "1.2.0"

        val commonMain by getting {
            resources.srcDirs("resources")
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
                implementation(kotlin("stdlib-common"))
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
                implementation("app.cash.sqldelight:coroutines-extensions:2.0.0-alpha05")
                implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
                implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
                implementation("io.ktor:ktor-client-core:$ktorVersion")
                implementation("io.insert-koin:koin-core:$koinVersion")
                implementation("io.insert-koin:koin-test:$koinVersion")
                implementation("io.arrow-kt:arrow-core:$arrowVersion")
                implementation("io.arrow-kt:arrow-fx-coroutines:$arrowVersion")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val androidMain by getting {
            dependencies {
                implementation(kotlin("stdlib-common"))
                implementation("app.cash.sqldelight:android-driver:$sqlDelightVersion")
                implementation("io.ktor:ktor-client-okhttp:$ktorVersion")
            }
        }
        val iosX64Main by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting
        val iosMain by getting {
            dependsOn(commonMain)
            iosX64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)
            dependencies{
                implementation("app.cash.sqldelight:native-driver:$sqlDelightVersion")
                implementation("io.ktor:ktor-client-darwin:$ktorVersion")
            }
        }
        val iosX64Test by getting
        val iosArm64Test by getting
        val iosSimulatorArm64Test by getting
        val iosTest by getting {
            dependsOn(commonTest)
            iosX64Test.dependsOn(this)
            iosArm64Test.dependsOn(this)
            iosSimulatorArm64Test.dependsOn(this)
        }
    }
}

sqldelight {
    databases {
        create("StationsDatabase") {
            packageName.set("com.emobilitychargingstations.database")
        }
    }
}

android {
    sourceSets["main"].resources.srcDir("src/commonMain/resources")
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(17))
        }
    }
    kotlin {
        jvmToolchain(17)
    }

    namespace = "com.comsystoreply.emobilitychargingstations"
    compileSdk = 35
    defaultConfig {
        minSdk = 31
    }
}
dependencies {
    implementation("androidx.core:core-ktx:+")
}
