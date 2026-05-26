package com.uliteamr.notescribe.buildlogic

import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

/**
 * Pre-configured convention plugin for Android library modules.
 *
 * Applies the Android library plugin and configures the
 * [LibraryExtension] using [NoteScribeConfig] constants.
 * This is intended for shared or feature modules.
 *
 * Usage in a module's build.gradle.kts:
 * ```
 * plugins {
 *     id("notescribe.android.library")
 * }
 * ```
 */
class AndroidLibraryConventionPlugin : Plugin<Project> {

    /**
     * Applies the Android library plugin to the given project and configures its Android
     * LibraryExtension with shared defaults for library modules.
     *
     * The configuration enables Jetpack Compose, sets the module namespace to
     * NoteScribeConfig.SHARED_NAMESPACE, applies common Android defaults, and sets the
     * minimum SDK to NoteScribeConfig.MIN_SDK.
     *
     * @param target The Gradle Project to apply the Android library plugin and configuration to.
     */
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.library")
            }

            configure<LibraryExtension> {
                configureAndroidDefaults()

                buildFeatures {
                    compose = true
                }

                namespace = NoteScribeConfig.SHARED_NAMESPACE

                defaultConfig {
                    minSdk = NoteScribeConfig.MIN_SDK
                }
            }
        }
    }
}
