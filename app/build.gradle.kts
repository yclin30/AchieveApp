plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.devtools.ksp") version libs.versions.ksp.get()
    id("com.google.dagger.hilt.android") version "2.56.2" apply false

}

android {
    namespace = "com.yclin.achieveapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.yclin.achieveapp"
        minSdk = 30
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        // 确保与Kotlin 2.0.0兼容的编译器版本
        kotlinCompilerExtensionVersion = "1.5.10"
    }
}

dependencies {
    // 添加javax.inject依赖
    implementation(libs.javax.inject)
    // 基础依赖
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Compose 依赖
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)  // 添加图标扩展

    // 导航
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.espresso.core)
    implementation(libs.generativeai)

    // 测试
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Room 数据库
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler) // 使用 KSP 处理器

    //noinspection GradleDependency
    implementation(libs.dagger.hilt.android.v248)
    ksp(libs.dagger.hilt.android.compiler.v248)

    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp.logging)
    implementation(libs.gson)
}