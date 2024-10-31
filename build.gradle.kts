plugins {
    java
}

group = "ru.moonlight"
version = "0.0.1"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.converterJackson)
    implementation(libs.jackson)
    implementation(libs.jsoup)

    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
}