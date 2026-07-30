import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.ksp)
}

// ── API 키 로딩 ───────────────────────────────────────────────────
// 키는 소스에 두지 않고 프로젝트 루트 local.properties(.gitignore 대상)에서 읽는다.
// 필요한 키 목록은 local.properties.example 참고.
// 키가 없어도 빌드는 통과하며, 해당 API 호출만 실패한다.
val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}

fun apiKey(name: String): String = localProperties.getProperty(name) ?: ""

android {
    namespace = "com.jonghyeok.ezegot"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.jonghyeok.ezegot"
        minSdk = 33
        compileSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        // 서울 열린데이터광장 – 전체 역 목록, 실시간 도착 정보
        buildConfigField("String", "SEOUL_OPEN_API_KEY", "\"${apiKey("SEOUL_OPEN_API_KEY")}\"")
        // 서울 열린데이터광장 – 역별 시간표(SearchSTNTimeTableByFRCodeService)
        buildConfigField("String", "SEOUL_TIMETABLE_API_KEY", "\"${apiKey("SEOUL_TIMETABLE_API_KEY")}\"")
        // 서울 교통 데이터(t-data) – 역 위경도
        buildConfigField("String", "TAIMS_API_KEY", "\"${apiKey("TAIMS_API_KEY")}\"")
        // 공공데이터포털 TAGO – 시간표 폴백
        buildConfigField("String", "DATA_GO_KR_SERVICE_KEY", "\"${apiKey("DATA_GO_KR_SERVICE_KEY")}\"")

        // Google Maps – AndroidManifest의 ${MAPS_API_KEY} 자리에 주입된다.
        // BuildConfig가 아니라 매니페스트 플레이스홀더인 이유는, 지도 SDK가
        // 매니페스트 meta-data에서 키를 직접 읽기 때문이다.
        manifestPlaceholders["MAPS_API_KEY"] = apiKey("MAPS_API_KEY")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.3"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/DEPENDENCIES"
        }
    }
}

dependencies {
    // Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.kotlinx.coroutines.play.services)

    // Test
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Network
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.converter.simplexml)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.gson)

    // Hilt DI
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.hilt.work)
    ksp(libs.hilt.compiler.androidx)

    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // Google Maps
    implementation(libs.play.services.maps)
    implementation(libs.play.services.location)
    implementation(libs.maps.compose)

    // Glance AppWidget
    implementation(libs.glance.appwidget)
    implementation(libs.glance.material3)

    // WorkManager
    implementation(libs.work.runtime.ktx)
}