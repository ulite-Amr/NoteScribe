package com.uliteamr.notescribe.buildlogic

import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidLibraryConventionPlugin : Plugin<Project> {

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
