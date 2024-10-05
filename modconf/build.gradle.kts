plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

val id = "mmrl_wpd"
val targetPackage = "com.dergoogler.mmrl.debug"


val versionCode = 354
val versionName = "3.5.4"

android {
    namespace = "com.dergoogler.modconf.$id"
    compileSdk = 34

    defaultConfig {
        minSdk = 21
        targetSdk = 34


        multiDexEnabled = false
    }

    buildFeatures {
        prefab = true
        compose = true
    }

    buildTypes {
        release {

            isShrinkResources = false
            multiDexEnabled = false
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


val androidHome = System.getenv("ANDROID_HOME")
val appId = providers.exec {
    commandLine(
        "$androidHome\\platform-tools\\adb.exe",
        "shell",
        "pm list packages -U | grep $targetPackage | cut -f 3 -d \":\""
    )
}.standardOutput.asText.get().trim()


tasks.register("pushDex") {
    dependsOn("buildDex")
//    commandLine("$ANDROID_HOME\\platform-tools\\adb.exe", "push", "$buildDir\\classes.dex", "/data/local/tmp/${ID}.dex")
}

tasks.register("moveDex") {
    dependsOn("pushDex")
//    commandLine ("$ANDROID_HOME\\platform-tools\\adb.exe", "shell", "su", "-c \"mv -f /data/local/tmp/${ID}.dex /data/data/$TARGET_PACKAGE/files/${ID}.dex\"")
}

tasks.register("push") {
    dependsOn("moveDex")
//    commandLine ("$ANDROID_HOME\\platform-tools\\adb.exe", "shell", "su", "-c \"chown $appId:$appId /data/data/$TARGET_PACKAGE/files/${ID}.dex\"")
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
    dependsOn("updateModuleProp")

    val buildDir = project.layout.buildDirectory.get().asFile.path


    doLast {

        exec {
            val clazzes =
                project.layout.buildDirectory.get().asFile.resolve("intermediates\\aar_main_jar\\release\\classes.jar")
            commandLine(
                "$androidHome\\build-tools\\34.0.0\\d8.bat",
                "--output=$buildDir",
                clazzes.path
            )
        }


        val fixedModId = id.replace(Regex("[^a-zA-Z0-9._]"), "_")
        val moduleFolder = project.rootDir.resolve("module")
        val dexFile =
            project.layout.buildDirectory.get().asFile.resolve("$buildDir/classes.dex")

        dexFile.copyTo(moduleFolder.resolve("common/$fixedModId.dex"), overwrite = true)
    }
}

tasks.register<Zip>("zip") {
    dependsOn("copyFiles")

    archiveFileName.set("${id}_${versionName}.zip")
    destinationDirectory.set(project.rootDir.resolve("out"))

    from(project.rootDir.resolve("module"))
}

afterEvaluate {
    tasks["assembleRelease"].finalizedBy("updateModuleProp", "copyFiles", "zip")
}