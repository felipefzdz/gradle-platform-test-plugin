plugins {
    `java-gradle-plugin`
    id("maven-publish")
    id("com.gradle.plugin-publish") version "0.12.0"
}

group = "com.felipefzdz"
version = "0.0.1"

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(gradleApi())
    implementation("org.apache.httpcomponents:httpclient:4.5.13")
    implementation("commons-io:commons-io:2.8.0")
    implementation("com.google.guava:guava:30.1-jre")
}

gradlePlugin {
    val platformTestBase by plugins.creating {
        id = "com.felipefzdz.platform-test-base"
        implementationClass = "com.felipefzdz.platform.base.PlatformTestBasePlugin"
        displayName = "Platform Test Base"
        description = "The Platform Test Base Gradle plugin offers shared functionality for the Platform and Kubernetes Gradle plugins"
    }
}

pluginBundle {
    website = "https://github.com/felipefzdz/gradle-platform-test-plugin"
    vcsUrl = "https://github.com/felipefzdz/gradle-platform-test-plugin.git"
    tags = listOf("e2e", "kubernetes", "testing")
}
