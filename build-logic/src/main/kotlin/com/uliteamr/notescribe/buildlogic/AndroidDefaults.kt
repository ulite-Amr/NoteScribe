package com.uliteamr.notescribe.buildlogic

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.JavaVersion

/**
 * Applies common Android SDK and Java configuration from [NoteScribeConfig].
 *
 * Handles properties available on [CommonExtension] that are identical
 * for both application and library modules:
 * - [CommonExtension.compileSdk]
 * - [CommonExtension.compileOptions]
 *
 * Module-specific configuration (defaultConfig, buildFeatures) is handled
 * individually in [AndroidApplicationConventionPlugin] and
 * [AndroidLibraryConventionPlugin].
 */
internal fun CommonExtension.configureAndroidDefaults() {
    compileSdk = NoteScribeConfig.COMPILE_SDK

    compileOptions.sourceCompatibility = JavaVersion.VERSION_21
    compileOptions.targetCompatibility = JavaVersion.VERSION_21
}
