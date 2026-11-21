plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

android {
    namespace = "cl.duoc.kaiju"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "cl.duoc.kaiju"
        minSdk = 30
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // Firma del APK. LLAVE ACADEMICA: se sube al repositorio solo porque la rubrica lo pide.
    // Nunca usar esta llave ni esta clave en una app real.
    signingConfigs {
        create("release") {
            storeFile = file("kaiju-release.jks")
            storePassword = "KaijuDuoc2026"
            keyAlias = "kaiju"
            keyPassword = "KaijuDuoc2026"
        }
    }

    buildTypes {
        debug {
            enableUnitTestCoverage = true
        }
        release {
            signingConfig = signingConfigs.getByName("release")
            optimization {
                // R8 desactivado: con la optimizacion activa el APK se cerraba al abrir (IllegalAccessError)
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    testImplementation(libs.junit)
    // Pruebas unitarias: Kotest, MockK y corrutinas de prueba
    testImplementation(libs.kotest.runner.junit5)
    testImplementation(libs.kotest.assertions.core)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.androidx.camera.mlkit.vision)
    implementation(libs.mlkit.barcode.scanning)
    // Conexion con el backend: Retrofit y conversor Gson
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
}

// Obligatorio para que las pruebas de Kotest corran sobre la plataforma JUnit
tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
