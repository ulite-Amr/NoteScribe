package com.uliteamr.notescribe.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project
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

            dependencies {
                add("implementation", platform("androidx.compose:compose-bom:2026.02.01"))
                add("implementation", "androidx.compose.ui:ui")
                add("implementation", "androidx.compose.ui:ui-graphics")
                add("implementation", "androidx.compose.ui:ui-tooling-preview")
                add("implementation", "androidx.compose.material3:material3")
                add("debugImplementation", "androidx.compose.ui:ui-tooling")
                add("debugImplementation", "androidx.compose.ui:ui-test-manifest")
                add("implementation", "androidx.navigation:navigation-compose:2.9.8")
            }
        }
    }
}
