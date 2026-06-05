import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "3.3.5"
    id("io.spring.dependency-management") version "1.1.6"
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"
    kotlin("plugin.jpa") version "1.9.25"
}

group = "ru.autoenterprise"
version = "0.1.0-SNAPSHOT"

extra["testcontainers.version"] = "1.21.4"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")
    implementation("org.thymeleaf.extras:thymeleaf-extras-springsecurity6")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation(kotlin("reflect"))

    runtimeOnly("org.postgresql:postgresql")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    jvmToolchain(21)
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "21"
    }
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()

    val dockerDesktopSocketCandidates = listOf(
        file("${System.getProperty("user.home")}/.docker/run/docker.sock"),
        file("${System.getProperty("user.home")}/Library/Containers/com.docker.docker/Data/docker.raw.sock"),
    )
    val dockerDesktopSocket = dockerDesktopSocketCandidates.firstOrNull { it.exists() }
    val dockerHost =
        System.getenv("DOCKER_HOST")
            ?.takeIf(String::isNotBlank)
            ?: dockerDesktopSocket?.let { socket -> "unix://${socket.absolutePath}" }

    dockerHost?.let { resolvedDockerHost ->
        environment("DOCKER_HOST", resolvedDockerHost)
        systemProperty("docker.host", resolvedDockerHost)
        systemProperty(
            "docker.client.strategy",
            "org.testcontainers.dockerclient.EnvironmentAndSystemPropertyClientProviderStrategy",
        )
    }
    val dockerSocketOverride =
        System.getenv("TESTCONTAINERS_DOCKER_SOCKET_OVERRIDE")
            ?.takeIf(String::isNotBlank)
            ?: dockerDesktopSocket?.let { "/var/run/docker.sock" }
    dockerSocketOverride?.let { dockerSocket ->
        environment("TESTCONTAINERS_DOCKER_SOCKET_OVERRIDE", dockerSocket)
        systemProperty("testcontainers.docker.socket.override", dockerSocket)
    }
}
