import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.BOOLEAN
import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.buildKonfig)
}

val localProps = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}

kotlin {
    android {
        namespace = "llc.bokadev.kompass.shared"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        compilerOptions {
            jvmTarget = JvmTarget.JVM_17
        }
        androidResources {
            enable = true
        }
        withHostTest {}
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.media)
            implementation(libs.koin.android)
            implementation(libs.play.services.maps)
            implementation(libs.ktor.client.okhttp)
            implementation(libs.kotlinx.coroutines.android)
        }

        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.navigation.compose)
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor)
            implementation(libs.supabase.postgrest)
            implementation(libs.supabase.auth)
            implementation(libs.supabase.storage)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kermit)
            implementation(libs.ktor.client.logging)
        }

        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.turbine)
        }
    }
}

buildkonfig {
    packageName = "llc.bokadev.kompass"

    defaultConfigs {
        buildConfigField(STRING, "SUPABASE_URL", localProps.getProperty("SUPABASE_URL", ""))
        buildConfigField(STRING, "SUPABASE_ANON_KEY", localProps.getProperty("SUPABASE_ANON_KEY", ""))
        buildConfigField(BOOLEAN, "USE_LOCAL_SUPABASE", localProps.getProperty("USE_LOCAL_SUPABASE", "false"))
        buildConfigField(STRING, "SUPABASE_ANON_KEY_LOCAL", localProps.getProperty("SUPABASE_ANON_KEY_LOCAL", ""))
        buildConfigField(STRING, "SUPABASE_LOCAL_HOST", "localhost")
        buildConfigField(STRING, "ANALYTICS_BACKEND_BASE_URL", localProps.getProperty("ANALYTICS_BACKEND_BASE_URL", ""))
        buildConfigField(STRING, "GOOGLE_MAPS_API_KEY", localProps.getProperty("GOOGLE_MAPS_API_KEY", ""))
        buildConfigField(STRING, "APP_VERSION", "1.0")
    }

    targetConfigs {
        create("android") {
            buildConfigField(STRING, "SUPABASE_LOCAL_HOST", "10.0.2.2")
        }
    }
}

dependencies {
    androidRuntimeClasspath(libs.compose.uiTooling)
}
