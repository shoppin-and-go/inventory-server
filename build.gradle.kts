plugins {
	kotlin("jvm") version "1.9.25"
	kotlin("plugin.spring") version "1.9.25"
	kotlin("plugin.jpa") version "1.9.25"
	id("org.springframework.boot") version "3.3.4"
	id("io.spring.dependency-management") version "1.1.6"
	id("com.google.cloud.tools.jib") version "3.4.3"
}

group = "com.shoppin-and-go"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

val kotestVersion = "5.9.1"
val kotestExtensionSpringVersion = "1.3.0"

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-websocket")
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("com.github.f4b6a3:ulid-creator:5.2.3")
	implementation("com.mysql:mysql-connector-j:8.2.0")
	developmentOnly("org.springframework.boot:spring-boot-devtools")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
	testImplementation("io.kotest:kotest-runner-junit5:${kotestVersion}")
	testImplementation("io.kotest:kotest-assertions-core:${kotestVersion}")
	testImplementation("io.kotest:kotest-property:${kotestVersion}")
	testImplementation("io.mockk:mockk:1.13.12")
	testImplementation("io.kotest.extensions:kotest-extensions-spring:${kotestExtensionSpringVersion}")
	testImplementation("com.navercorp.fixturemonkey:fixture-monkey-starter-kotlin:1.0.25")
	implementation("com.h2database:h2:2.3.232")
	testImplementation("com.ninja-squad:springmockk:3.1.1")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict")
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}

jib {
	from {
		image = "amazoncorretto:21-alpine"
		platforms {
			platform {
				architecture = "arm64"
				os = "linux"
			}
		}
	}
	to {
		image = "public.ecr.aws/e6u1y0g6/shoppin-and-go/inventory-server"
		tags = mutableSetOf("latest", "${version}_${System.currentTimeMillis()}")
		setCredHelper("ecr-login")
	}
	container {
		creationTime = "USE_CURRENT_TIMESTAMP"
		jvmFlags = listOf(
			"-Dfile.encoding=UTF-8",
			"-Xms128m",
			"-Xmx128m",
			"-XX:+UseContainerSupport",
			"-XX:+DisableExplicitGC"
		)
		ports = listOf("8080")

		environment = mapOf(
			"SPRING_PROFILES_ACTIVE" to "dev",
			"SERVER_PORT" to "8080",
		)
	}
}
