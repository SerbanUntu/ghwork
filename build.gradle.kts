import kotlin.collections.listOf

plugins {
	application
	checkstyle
}

group = "com.ghwork"
version = "0.1.1"
description = "Queries the GitHub workflows of a repository and displays live information."

checkstyle {
	toolVersion = "13.7.0"
    sourceSets = listOf(project.sourceSets.main.get())
}

tasks.withType<Checkstyle> {
	maxWarnings = 0
}

application {
	mainClass.set("com.ghwork.GhworkApplication")
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
	testImplementation(libs.wiremock)
	testImplementation(libs.log4j.core.test)
	testImplementation(libs.jqwik)
	testRuntimeOnly(libs.junit.platform.launcher)
    implementation(libs.log4j.api)
    implementation(libs.log4j.core)
    implementation(libs.spring.context)
}

tasks.withType<Test> {
	useJUnitPlatform()
}
