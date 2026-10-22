import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

dependencies {
    implementation(project(":shared"))

    implementation(compose.desktop.currentOs)
    implementation(libs.kotlinx.coroutinesSwing)

    implementation(libs.compose.uiToolingPreview)
}

compose.desktop {
    application {
        mainClass = "org.softwareanvil.musa.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "org.softwareanvil.musa"
            packageVersion = "1.0.0"
        }
    }
}

tasks.register<JavaExec>("runDemo") {
    group = "application"
    description = "Runs the desktop app on a throwaway database filled with toy data."
    mainClass.set("org.softwareanvil.musa.MainKt")
    classpath = sourceSets["main"].runtimeClasspath
    systemProperty("musa.demo", "true")
}
