plugins {
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"
    kotlin("plugin.jpa") version "1.9.25" // 
    id("org.springframework.boot") version "3.4.2" // Versão estável 
    id("io.spring.dependency-management") version "1.1.7" // 
}

group = "com.example"
version = "0.0.1-SNAPSHOT"
description = "DF nas Mãos - Gestão de Obras e Proposições" // 

java { toolchain { languageVersion = JavaLanguageVersion.of(21) } } // 

repositories { mavenCentral() } // 

dependencies {
    // Spring Boot Core
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-security")

    //Fotos
    implementation("net.coobird:thumbnailator:0.4.20")
    // Banco de Dados e Geoposicionamento
    runtimeOnly("org.postgresql:postgresql")
// Versão exata compatível com o Spring Boot 3.4.2
    implementation("org.hibernate.orm:hibernate-spatial:6.6.5.Final")    // Segurança (JWT)
    implementation("com.auth0:java-jwt:4.4.0")

    // Documentação e APIs [cite: 2]
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.14")
    implementation("org.apache.commons:commons-lang3:3.18.0")
    implementation("com.google.maps:google-maps-services:2.2.0")
    implementation("com.squareup.okio:okio-jvm:3.4.0")
    // Kotlin
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

    // Testes
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("io.mockk:mockk:1.13.8")
}

kotlin { compilerOptions { freeCompilerArgs.addAll("-Xjsr305=strict") } }

tasks.withType<Test> { useJUnitPlatform() }
tasks.withType<org.springframework.boot.gradle.tasks.run.BootRun> {
    val envFile = project.file(".env")
    if (envFile.exists()) {
        println("------- CARREGANDO CONFIGURAÇÕES DO .ENV -------")
        envFile.useLines { lines ->
            lines.forEach { line ->
                if (line.isNotBlank() && !line.startsWith("#")) {
                    val parts = line.split("=", limit = 2)
                    if (parts.size == 2) {
                        val key = parts[0].trim()
                        val value = parts[1].trim()
                        println("Variável encontrada: $key")
                        environment(key, value)
                    }
                }
            }
        }
        println("-----------------------------------------------")
    } else {
        println("------- AVISO: ARQUIVO .ENV NÃO ENCONTRADO -------")
    }
}