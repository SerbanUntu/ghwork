plugins {
	application
	checkstyle
}

group = "com.example"
version = "0.1.0"
description = "Queries the GitHub workflows of a repository and displays live information."

checkstyle {
	toolVersion = "13.7.0"
}

tasks.withType<Checkstyle> {
	maxWarnings = 0
}

application {
	mainClass.set("com.example.ghwork.GhworkApplication")
}

tasks.named<Jar>("jar") {
	manifest {
		attributes["Main-Class"] = application.mainClass.get()
	}
}

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation(libs.gson)
	implementation(libs.jansi)
	testImplementation(libs.junit.jupiter)
	testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.withType<Test> {
	useJUnitPlatform()
}
