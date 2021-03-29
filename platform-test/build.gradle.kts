plugins {
    `java-gradle-plugin`
    `groovy`
    `kotlin-dsl`
    id("maven-publish")
    id("com.gradle.plugin-publish") version "0.12.0"
}

group = "com.felipefzdz"
version = "0.0.5"

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.codehaus.groovy:groovy:2.5.12")
    implementation("org.apache.httpcomponents:httpclient:4.5.13")
    implementation("commons-io:commons-io:2.8.0")
    implementation("com.google.guava:guava:30.1-jre")
    implementation(project(":platform-shared"))
    testImplementation("org.spockframework:spock-core:1.3-groovy-2.5")
}

gradlePlugin {
    val platformTest by plugins.creating {
        id = "com.felipefzdz.platform-test"
        implementationClass = "com.felipefzdz.platform.PlatformTestPlugin"
        displayName = "Platform Test"
        description = "The Platform Test Gradle plugin manages the lifecycle of a platform purposed for E2E testing."
    }
}

pluginBundle {
    website = "https://github.com/felipefzdz/gradle-platform-test-plugin"
    vcsUrl = "https://github.com/felipefzdz/gradle-platform-test-plugin.git"
    tags = listOf("e2e", "kubernetes", "testing")
}

val functionalTestSourceSet = sourceSets.create("functionalTest") {
}

gradlePlugin.testSourceSets(functionalTestSourceSet)
configurations["functionalTestImplementation"].extendsFrom(configurations["testImplementation"])

val functionalTest by tasks.registering(Test::class) {
    testClassesDirs = functionalTestSourceSet.output.classesDirs
    classpath = functionalTestSourceSet.runtimeClasspath
}

tasks.check {
    dependsOn(functionalTest)
}

tasks.named("publishPlugins").configure {
    dependsOn(functionalTest)
}
