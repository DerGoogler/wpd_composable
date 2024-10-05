buildscript {
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
    dependencies {
        classpath(libs.gradle)
        classpath(kotlin("gradle-plugin", version = "1.9.25"))
    }
}