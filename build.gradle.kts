plugins {
	kotlin("jvm") version "1.9.25"
	kotlin("plugin.spring") version "1.9.25"
	id("org.springframework.boot") version "3.3.4"
	id("io.spring.dependency-management") version "1.1.6"
	kotlin("plugin.jpa") version "1.9.25"
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
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("com.github.f4b6a3:ulid-creator:5.2.3")
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
