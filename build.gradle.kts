plugins {
	application
}

group = "com.example"
version = "0.1.0-SNAPSHOT"
description = "Queries the GitHub workflows of a repository and displays live information."

application {
	mainClass.set("com.example.github_workflow_tool.GithubWorkflowToolApplication")
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
	testImplementation(libs.junit.jupiter)
	testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.withType<Test> {
	useJUnitPlatform()
}
