plugins {
    `kotlin-dsl`
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = "notescribe.android.application"
            implementationClass = "com.uliteamr.notescribe.buildlogic.AndroidApplicationConventionPlugin"
        }
        register("androidLibrary") {
            id = "notescribe.android.library"
            implementationClass = "com.uliteamr.notescribe.buildlogic.AndroidLibraryConventionPlugin"
        }
        register("compose") {
            id = "notescribe.compose"
            implementationClass = "com.uliteamr.notescribe.buildlogic.ComposeConventionPlugin"
        }
    }
}

dependencies {
    implementation(libs.android.gradle)
    implementation(libs.kotlin.gradle)
    implementation(libs.kotlin.compose.gradle)
}
