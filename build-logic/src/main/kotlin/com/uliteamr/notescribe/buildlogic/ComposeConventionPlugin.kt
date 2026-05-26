package com.uliteamr.notescribe.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.dependencies

/**
 * Convention plugin that configures Jetpack Compose for a module.
 *
 * Applies the Kotlin Compose compiler plugin and adds the standard
 * Compose BOM and UI dependencies to the module's classpath.
 *
 * Usage in a module's build.gradle.kts:
 * ```
 * plugins {
 *     id("notescribe.compose")
 * }
 * ```
 */
class ComposeConventionPlugin : Plugin<Project> {

    /**
     * Configures the given Gradle Project for Jetpack Compose support.
     *
     * Applies the Kotlin Compose compiler plugin and registers Compose, Material3, tooling, and
     * navigation dependencies required by modules that use Jetpack Compose.
     *
     * @param target The Gradle Project to configure.
     */
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

            val libs = rootProject.extensions.getByType(VersionCatalogsExtension::class.java).named("libs")

            dependencies {
                val bom = libs.findLibrary("androidx-compose-bom").get()
                add("implementation", platform(bom))
                add("implementation", "androidx.compose.ui:ui")
                add("implementation", "androidx.compose.ui:ui-graphics")
                add("implementation", "androidx.compose.ui:ui-tooling-preview")
                add("implementation", "androidx.compose.material3:material3")
                add("debugImplementation", "androidx.compose.ui:ui-tooling")
                add("debugImplementation", "androidx.compose.ui:ui-test-manifest")
                val navCompose = libs.findLibrary("androidx-navigation-compose").get()
                add("implementation", navCompose)
            }
        }
    }
}
