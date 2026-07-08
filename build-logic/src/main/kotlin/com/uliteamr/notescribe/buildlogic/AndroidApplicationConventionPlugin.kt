package com.uliteamr.notescribe.buildlogic

import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import java.io.File
import java.util.Properties

class AndroidApplicationConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.android.application")

            extensions.configure<ApplicationExtension> {
                configureAndroidDefaults()

                buildFeatures {
                    buildConfig = true
                    compose = true
                }

                namespace = NoteScribeConfig.GROUP

                defaultConfig {
                    applicationId = NoteScribeConfig.GROUP
                    minSdk = NoteScribeConfig.MIN_SDK
                    targetSdk = NoteScribeConfig.TARGET_SDK
                    versionCode = NoteScribeConfig.VERSION_CODE
                    versionName = NoteScribeConfig.versionName(suffix = "Alpha", iteration = 1)
                }

                flavorDimensions += "abi"

                productFlavors {
                    create("universal") {
                        dimension = "abi"
                        buildConfigField("String", "ARCHITECTURE", "\"universal\"")
                    }

                    create("arm64") {
                        dimension = "abi"
                        ndk {
                            abiFilters.clear()
                            abiFilters.add("arm64-v8a")
                        }
                        buildConfigField("String", "ARCHITECTURE", "\"arm64-v8a\"")
                    }

                    create("armeabi") {
                        dimension = "abi"
                        ndk {
                            abiFilters.clear()
                            abiFilters.add("armeabi-v7a")
                        }
                        buildConfigField("String", "ARCHITECTURE", "\"armeabi-v7a\"")
                    }

                    create("x86") {
                        dimension = "abi"
                        ndk {
                            abiFilters.clear()
                            abiFilters.add("x86")
                        }
                        buildConfigField("String", "ARCHITECTURE", "\"x86\"")
                    }

                    create("x86_64") {
                        dimension = "abi"
                        ndk {
                            abiFilters.clear()
                            abiFilters.add("x86_64")
                        }
                        buildConfigField("String", "ARCHITECTURE", "\"x86_64\"")
                    }
                }

                signingConfigs {
                    // Based on klyx-dev/klyx (https://github.com/klyx-dev/klyx) by Vivek
                    create("release") {
                        val isCI = System.getenv("GITHUB_ACTIONS")?.toBoolean() ?: false
                        val propPath = if (isCI) {
                            "/tmp/sign.properties"
                        } else {
                            File(System.getProperty("user.home"), ".sign/sign.properties").absolutePath
                        }
                        val propFile = File(propPath)
                        if (propFile.exists()) {
                            val properties = Properties()
                            propFile.inputStream().use { properties.load(it) }
                            keyAlias = properties.getProperty("keyAlias").also {
                                if (it.isNullOrBlank()) throw GradleException("keyAlias is missing in $propPath")
                            }
                            keyPassword = properties.getProperty("keyPassword").also {
                                if (it.isNullOrBlank()) throw GradleException("keyPassword is missing in $propPath")
                            }
                            storeFile = if (isCI) {
                                File("/tmp/release.keystore")
                            } else {
                                val path = properties.getProperty("storeFile")
                                if (path.isNullOrBlank()) throw GradleException("storeFile is missing in $propPath")
                                val file = File(path)
                                if (!file.exists() || !file.canRead()) throw GradleException("storeFile not found or not readable: $path")
                                file
                            }
                            storePassword = properties.getProperty("storePassword").also {
                                if (it.isNullOrBlank()) throw GradleException("storePassword is missing in $propPath")
                            }
                        } else {
                            println("Sign properties not found at $propPath — release signing will fail if required.")
                        }
                    }

                    create("debugkey") {
                        keyAlias = "debug"
                        keyPassword = "debugpass"
                        storeFile = file("keystore/debug.keystore")
                        storePassword = "debugpass"
                    }
                }

                buildTypes {
                    getByName("release") {
                        isMinifyEnabled = true
                        isShrinkResources = true
                        proguardFiles(
                            getDefaultProguardFile("proguard-android-optimize.txt"),
                            "proguard-rules.pro"
                        )
                        signingConfig = signingConfigs.getByName("release")
                    }

                    getByName("debug") {
                        applicationIdSuffix = ".debug"
                        versionNameSuffix = "-DEBUG"
                        isDebuggable = true
                        signingConfig = signingConfigs.getByName("debugkey")
                    }
                }
            }
        }
    }
}
