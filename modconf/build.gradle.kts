plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

val id = "mmrl_wpd"
val isDebuggableMMRL = false

val versionName = "3.5.4"
val versionCode = 354

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

    packaging {
        resources {
            excludes += "**"
        }
    }
}

dependencies {
    implementation(libs.androidx.ui)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.runtime.livedata)
    implementation(libs.libsu.core)
    implementation(libs.libsu.io)
}


val androidHome: String = System.getenv("ANDROID_HOME")
val appId = providers.exec {
    commandLine(
        "$androidHome/platform-tools/adb.exe",
        "shell",
        "pm list packages -U | grep $targetPackage | cut -f 3 -d \":\""
    )
}.standardOutput.asText.get().trim()

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

val classes = buildDir.resolve("intermediates/aar_main_jar/release/syncReleaseLibJars/classes.jar")

tasks.register("debug") {
    dependsOn("build")

    doLast {
        d8("--output=$buildDir", classes.path)

        val dex = "${buildDir.path}/classes.dex"
        val dexFile = "$androidTmp/${id}.dex"
        val dexFileAtAppDir = "$appDir/${id}.dex"

        adbPush(dex, dexFile)

        adbRootShell("mv", "-f", dexFile, dexFileAtAppDir)

        adbRootShell("chmod", "0444", dexFileAtAppDir)
        adbRootShell("chown", "root:root", dexFileAtAppDir)

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
}

tasks.register("updateModuleProp") {
    doLast {
        val modulePropFile = project.rootDir.resolve("module/module.prop")

        var content = modulePropFile.readText()

        content = content.replace(Regex("version=.*"), "version=$versionName")
        content = content.replace(Regex("versionCode=.*"), "versionCode=$versionCode")

        modulePropFile.writeText(content)
    }
}

tasks.register("copyFiles") {
    dependsOn("build", "updateModuleProp")

    doLast {
        val clazzes = buildDir.resolve("intermediates/aar_main_jar/release/classes.jar")
        d8(
            "--output=${buildDir.path}",
            clazzes.path
        )


        val fixedModId = id.replace(Regex("[^a-zA-Z0-9._]"), "_")
        val moduleFolder = project.rootDir.resolve("module")
        val dexFile = buildDir.resolve("classes.dex")

        dexFile.copyTo(moduleFolder.resolve("common/$fixedModId.dex"), overwrite = true)
    }
}

tasks.register<Zip>("zip") {
    dependsOn("copyFiles")

    archiveFileName.set("${id}_${versionName}.zip")
    destinationDirectory.set(project.rootDir.resolve("out"))

    from(project.rootDir.resolve("module"))
}
