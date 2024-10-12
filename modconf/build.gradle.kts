plugins {
    alias(libs.plugins.android.application)
    // id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

val id = "mmrl_wpd"
val isDebuggableMMRL = true

val versionName = "3.5.5"
val versionCode = 355

android {
    namespace = "com.dergoogler.modconf.$id"
    compileSdk = 34

    defaultConfig {
        minSdk = 21
        multiDexEnabled = false
    }

    buildFeatures {
        compose = true
    }

    buildTypes {
        release {
            isShrinkResources = false
            multiDexEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
        }
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.15"
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }
    packaging.resources.excludes += setOf(
        "META-INF/**",
        "okhttp3/**",
        "kotlin/**",
        "org/**",
        "**.properties",
        "**.bin",
        "**/*.proto"
    )
}

dependencies {
    compileOnly(libs.androidx.ui)
    compileOnly(libs.androidx.material3)
    compileOnly(libs.androidx.ui.tooling.preview)
    compileOnly(libs.androidx.runtime.livedata)
    compileOnly(libs.libsu.core)
    compileOnly(libs.libsu.io)
    compileOnly(libs.androidx.hilt.navigation.compose)
    compileOnly(libs.androidx.navigation.runtime.ktx)
    compileOnly(libs.androidx.navigation.compose)
}


val androidHome: String = System.getenv("ANDROID_HOME")
//val appId = providers.exec {
//    commandLine(
//        "$androidHome/platform-tools/adb.exe",
//        "shell",
//        "pm list packages -U | grep $targetPackage | cut -f 3 -d \":\""
//    )
//}.standardOutput.asText.get().trim()

val adbBin: String = "$androidHome/platform-tools/adb.exe"
val androidTmp: String = "/data/local/tmp"
val d8Bin: String = "$androidHome/build-tools/34.0.0/d8.bat"
val buildDir: File = project.layout.buildDirectory.get().asFile
val targetPackage = if (isDebuggableMMRL) "com.dergoogler.mmrl.debug" else "com.dergoogler.mmrl"
val appDir: String = "/data/data/$targetPackage/files"

fun d8(vararg cmd: String) {
    exec {
        commandLine(d8Bin, *cmd)
    }
}

fun adbShell(vararg cmd: String) {
    exec {
        commandLine(adbBin, "shell", *cmd)
    }
}

fun adbPush(vararg cmd: String) {
    exec {
        commandLine(adbBin, "push", *cmd)
    }
}

fun adbRootShell(vararg cmd: String) {
    exec {
        commandLine(adbBin, "shell", "su", "-c", "\"${cmd.joinToString(" ")}\"")
    }
}

fun runDebugInMMRL() {
    adbRootShell(
        "am",
        "start",
        "-a",
        "android.intent.action.MAIN",
        "-n",
        "$targetPackage/com.dergoogler.mmrl.ui.activity.ModConfActivity",
        "--es",
        "MOD_ID",
        id,
        "--ez",
        "DEBUG",
        "true"
    )
}

val classes = buildDir.resolve("intermediates/aar_main_jar/release/syncReleaseLibJars/classes.jar")


val apkFile = "$androidTmp/${id}.apk"
val apkFileAtAppDir = "$appDir/${id}.apk"
val dexFile = "$androidTmp/${id}.dex"
val dexFileAtAppDir = "$appDir/${id}.dex"

tasks.register("clean-modconf") {
    doLast {
        // Removing old files to avoid conflict
        adbRootShell("rm", "-rf", apkFile, apkFileAtAppDir, dexFile, dexFileAtAppDir)
    }
}

tasks.register("debug-apk") {
    dependsOn("clean-modconf", "build", "assembleRelease")

    doLast {
        val apk = "${buildDir.path}/outputs/apk/release/modconf-release-unsigned.apk"

        adbPush(apk, apkFile)

        adbRootShell("mv", "-f", apkFile, apkFileAtAppDir)

        adbRootShell("chmod", "0444", apkFileAtAppDir)
        adbRootShell("chown", "root:root", apkFileAtAppDir)

        runDebugInMMRL()
    }
}

tasks.register("debug-dex") {
    dependsOn("clean-modconf", "build")

    doLast {
        d8("--output=$buildDir", classes.path)

        val dex = "${buildDir.path}/classes.dex"

        adbPush(dex, dexFile)

        adbRootShell("mv", "-f", dexFile, dexFileAtAppDir)

        adbRootShell("chmod", "0444", dexFileAtAppDir)
        adbRootShell("chown", "root:root", dexFileAtAppDir)

        runDebugInMMRL()
    }
}

tasks.register("updateModuleProp") {
    dependsOn("build", "assembleRelease")

    doLast {
        val modulePropFile = project.rootDir.resolve("module/module.prop")

        var content = modulePropFile.readText()

        content = content.replace(Regex("version=.*"), "version=$versionName")
        content = content.replace(Regex("versionCode=.*"), "versionCode=$versionCode")

        modulePropFile.writeText(content)
    }
}

tasks.register("copyFiles") {
    dependsOn("updateModuleProp")

    doLast {
        val fixedModId = id.replace(Regex("[^a-zA-Z0-9._]"), "_")
        val moduleFolder = project.rootDir.resolve("module")
        val dexFile = buildDir.resolve("outputs/apk/release/modconf-release-unsigned.apk")

        dexFile.copyTo(moduleFolder.resolve("common/$fixedModId.apk"), overwrite = true)
    }
}

tasks.register<Zip>("zip") {
    dependsOn("copyFiles")

    archiveFileName.set("${id}_${versionName}.zip")
    destinationDirectory.set(project.rootDir.resolve("out"))

    from(project.rootDir.resolve("module"))
}
