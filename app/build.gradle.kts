plugins {
    id("notescribe.android.application")
    id("notescribe.compose")
}

val rustProjectDir = rootProject.file("notescribe-core")

val abiToTarget = mapOf(
    "arm64-v8a" to "aarch64-linux-android",
    "armeabi-v7a" to "armv7-linux-androideabi",
    "x86_64" to "x86_64-linux-android",
    "x86" to "i686-linux-android"
)

val ndkVersion = "27.2.12479018"
val localPropsFile = rootProject.file("local.properties")
val sdkDir = if (localPropsFile.exists()) {
    localPropsFile.readLines()
        .firstOrNull { it.startsWith("sdk.dir=") }
        ?.removePrefix("sdk.dir=")
        ?.trim()
} ?: System.getenv("ANDROID_HOME") ?: "/home/amr/Android/Sdk"
val ndkPath = "$sdkDir/ndk/$ndkVersion"

tasks.register<Exec>("cargoBuildHost") {
    inputs.dir(rustProjectDir.resolve("src"))
    inputs.file(rustProjectDir.resolve("Cargo.toml"))
    outputs.file(rustProjectDir.resolve("target/release/libnotescribe_core.so"))
    workingDir(rustProjectDir)
    commandLine("cargo", "build", "--release")
}

abiToTarget.forEach { (abi, _) ->
    tasks.register<Exec>("cargoBuildRelease_$abi") {
        inputs.dir(rustProjectDir.resolve("src"))
        inputs.file(rustProjectDir.resolve("Cargo.toml"))
        outputs.dir(layout.buildDirectory.get().asFile.resolve("jniLibs/$abi"))
        workingDir(rustProjectDir)
        environment("ANDROID_NDK_HOME", ndkPath)
        commandLine(
            "cargo", "ndk",
            "-t", abi,
            "-o", layout.buildDirectory.get().asFile.resolve("jniLibs/$abi").absolutePath,
            "build", "--release"
        )
    }
}

val allCargoTasks = abiToTarget.keys.map { abi -> "cargoBuildRelease_$abi" }

tasks.register("cargoBuildAll") {
    dependsOn(allCargoTasks)
}

tasks.register<Exec>("generateUniFFIBindings") {
    dependsOn("cargoBuildHost")
    inputs.file(rustProjectDir.resolve("target/release/libnotescribe_core.so"))
    val outputDir = layout.buildDirectory.get().asFile.resolve("generated/uniffi")
    outputs.dir(outputDir)
    workingDir(rustProjectDir)
    outputDir.mkdirs()
    commandLine(
        "uniffi-bindgen", "generate",
        "--library", File(rustProjectDir, "target/release/libnotescribe_core.so").absolutePath,
        "--language", "kotlin",
        "--out-dir", outputDir.absolutePath
    )
}

tasks.register<Copy>("copyGeneratedBindings") {
    dependsOn("generateUniFFIBindings")
    from(layout.buildDirectory.dir("generated/uniffi/com/uliteamr/notescribe/core")) {
        include("*.kt")
    }
    into("src/main/java/com/uliteamr/notescribe/core")
}

abiToTarget.forEach { (abi, _) ->
    tasks.register<Copy>("copySo_$abi") {
        dependsOn("cargoBuildRelease_$abi")
        from(layout.buildDirectory.file("jniLibs/$abi")) {
            include("*.so")
        }
        into("src/main/jniLibs/$abi")
    }
}

val allCopySoTasks = abiToTarget.keys.map { abi -> "copySo_$abi" }

tasks.register("copyAllSo") {
    dependsOn(allCopySoTasks)
}

tasks.named("preBuild") {
    dependsOn("cargoBuildAll")
    dependsOn("copyAllSo")
    dependsOn("copyGeneratedBindings")
}

dependencies {
    implementation(libs.jna)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}
