import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// Jugador del PC (F5.4, D-44, D-50): un programa de consola en la JVM con el mismo motor, que se une
// por TCP a la sala del teléfono y juega solo. `application` (de Gradle, sin descargas) crea el lanzador.
plugins {
    alias(libs.plugins.kotlin.jvm)
    application
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(project(":engine"))
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
}

// `./gradlew :terminal:installDist` deja el lanzador en terminal/build/install/mono-pc/bin/mono-pc.
application {
    mainClass.set("com.jacck.mono.terminal.MainKt")
    applicationName = "mono-pc"
}

tasks.test {
    useJUnitPlatform()
}
