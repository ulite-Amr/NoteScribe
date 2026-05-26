package com.uliteamr.notescribe.buildlogic

/**
 * Centralized configuration constants for the NoteScribe project.
 *
 * All modules should reference this object for versioning, SDK levels,
 * and shared namespaces to maintain a single source of truth.
 */
object NoteScribeConfig {

    /**
     * The base group identifier for the project.
     * Used as the default namespace and applicationId prefix.
     */
    const val GROUP = "com.uliteamr.notescribe"

    /**
     * Common namespace for shared/library modules.
     */
    const val SHARED_NAMESPACE = "$GROUP.shared"

    // --- SDK Level Constants ---

    /**
     * The compile SDK version for all Android modules.
     */
    const val COMPILE_SDK = 36

    /**
     * The minimum SDK version supported by the application.
     */
    const val MIN_SDK = 26

    /**
     * The target SDK version for the application.
     */
    const val TARGET_SDK = 36

    // --- Versioning Constants ---

    private const val MAJOR = 0
    private const val MINOR = 1
    private const val PATCH = 0

    /**
     * The numeric version code for the Android application.
     * Increment this for every new release to the Play Store.
     */
    const val VERSION_CODE = 1

    /**
     * Generates a semantic version name string based on the current version constants.
     *
     * Example outputs:
     * - Default: "1.0.0"
     * - With suffix: "1.0.0-alpha01"
     *
     * @param major The major version number. Defaults to [MAJOR].
     * @param minor The minor version number. Defaults to [MINOR].
     * @param patch The patch version number. Defaults to [PATCH].
     * @param suffix An optional suffix string (e.g., "alpha", "beta", "rc").
     * @param iteration The iteration number for the suffix. If greater than 0, it is zero-padded.
     * @return A formatted semantic version name string.
     */
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
