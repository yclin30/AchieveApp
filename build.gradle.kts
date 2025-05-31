plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    id("com.google.devtools.ksp") version libs.versions.ksp.get() apply false
    id("com.google.dagger.hilt.android") version "2.56.2" apply false // 添加这行

}