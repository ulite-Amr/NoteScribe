package com.uliteamr.notescribe.buildlogic

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.JavaVersion

internal fun CommonExtension.configureAndroidDefaults() {
    compileSdk = NoteScribeConfig.COMPILE_SDK

    compileOptions.sourceCompatibility = JavaVersion.VERSION_21
    compileOptions.targetCompatibility = JavaVersion.VERSION_21
}
