import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

val nativeHealthDashboardEnabled = providers.gradleProperty("nativeHealthDashboard")
    .map(String::toBoolean)
    .orElse(false)
val labVersionCode = providers.gradleProperty("labVersionCode").map(String::toInt).orElse(1)
val supabaseUrl = providers.gradleProperty("wholeMateSupabaseUrl")
    .orElse(providers.environmentVariable("WHOLEMATE_SUPABASE_URL"))
    .orElse("")
val supabasePublishableKey = providers.gradleProperty("wholeMateSupabasePublishableKey")
    .orElse(providers.environmentVariable("WHOLEMATE_SUPABASE_PUBLISHABLE_KEY"))
    .orElse("")

fun quotedBuildConfig(value: String): String =
    "\"${value.replace("\\", "\\\\").replace("\"", "\\\"")}\""

android {
    namespace = "com.runmate.compose"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.runmate.compose"
        minSdk = 26
        targetSdk = 36
        versionCode = labVersionCode.get()
        versionName = "0.1.0-poc"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("boolean", "NATIVE_HEALTH_DASHBOARD", nativeHealthDashboardEnabled.get().toString())
        buildConfigField("String", "SUPABASE_URL", quotedBuildConfig(supabaseUrl.get()))
        buildConfigField("String", "SUPABASE_PUBLISHABLE_KEY", quotedBuildConfig(supabasePublishableKey.get()))
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    packaging.resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2025.08.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation("androidx.activity:activity-compose:1.10.1")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.9.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.2")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.health.connect:connect-client:1.1.0")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
}
