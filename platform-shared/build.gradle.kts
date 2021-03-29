plugins {
    `java-library`
    `maven-publish`
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

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "com.felipefzdz"
            artifactId = "platform-shared"
            version = "0.1"

            from(components["java"])
        }
    }
}
