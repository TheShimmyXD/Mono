import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// Motor de reglas: Kotlin puro, sin Android (D-02). Se prueba en la JVM en segundos.
plugins {
    alias(libs.plugins.kotlin.jvm)
    // Genera en compilación el código que pasa cada clase @Serializable a JSON y de vuelta.
    alias(libs.plugins.kotlin.serialization)
}

// Bytecode Java 17: lo mismo que la app, para que Android lo pueda usar.
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
    implementation(libs.kotlinx.serialization.json)
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.test {
    useJUnitPlatform()
}
