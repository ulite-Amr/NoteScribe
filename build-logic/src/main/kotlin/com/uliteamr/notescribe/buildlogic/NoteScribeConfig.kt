package com.uliteamr.notescribe.buildlogic

object NoteScribeConfig {

    const val GROUP = "com.uliteamr.notescribe"

    const val SHARED_NAMESPACE = "$GROUP.shared"

    const val COMPILE_SDK = 37

    const val MIN_SDK = 26

    const val TARGET_SDK = 37

    private const val MAJOR = 0
    private const val MINOR = 1
    private const val PATCH = 0

    const val VERSION_CODE = 1

    fun versionName(
        major: Int = MAJOR,
        minor: Int = MINOR,
        patch: Int = PATCH,
        suffix: String = "",
        iteration: Int = 0
    ): String {
        val base = "$major.$minor.$patch"

        if (suffix.isBlank()) return base

        val iterationStr = if (iteration > 0) iteration.toString().padStart(2, '0') else ""
        return "$base-$suffix$iterationStr"
    }
}
