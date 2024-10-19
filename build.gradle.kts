import com.epages.restdocs.apispec.gradle.OpenApi3Task

plugins {
	kotlin("jvm") version "1.9.25"
	kotlin("plugin.spring") version "1.9.25"
	kotlin("plugin.jpa") version "1.9.25"
	id("org.springframework.boot") version "3.3.4"
	id("io.spring.dependency-management") version "1.1.6"
	id("com.google.cloud.tools.jib") version "3.4.3"
	id("com.epages.restdocs-api-spec") version "0.18.2"
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
	testImplementation("org.springframework.restdocs:spring-restdocs-mockmvc")
	testImplementation("com.epages:restdocs-api-spec-mockmvc:0.18.2")
	implementation("com.h2database:h2:2.3.232")
	testImplementation("com.ninja-squad:springmockk:3.1.1")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict")
	}
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

openapi3 {
	title = "Shoppin&Go Inventory API"
	format = "yaml"
}

tasks.withType<Test> {
	useJUnitPlatform()
}

tasks.register<Delete>("cleanGeneratedSnippets") {
	delete("build/generated-snippets")
}

tasks.register<Copy>("copyOasToSwagger") {
	delete("src/main/resources/static/swagger-ui/openapi3.yaml")
	from("build/api-spec/openapi3.yaml")
	into("src/main/resources/static/swagger-ui/.")
}

tasks.withType<OpenApi3Task>().configureEach {
	dependsOn("cleanGeneratedSnippets")
	finalizedBy("copyOasToSwagger")
}
