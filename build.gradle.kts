import org.jetbrains.compose.compose
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.20"
    id("org.jetbrains.compose") version "0.2.0-build132"
}

group = "me.alx"
version = "1.0"

repositories {
    jcenter()
    mavenCentral()
    maven {
        url = uri("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}

dependencies {
    implementation(compose.desktop.currentOs)
    // https://mvnrepository.com/artifact/com.drewnoakes/metadata-extractor
    implementation ("com.drewnoakes", "metadata-extractor","2.15.0")


}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "13"
}

compose.desktop {
    application {
        mainClass = "MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Exe, TargetFormat.Deb)
            packageName = "lab2"
        }
    }
}