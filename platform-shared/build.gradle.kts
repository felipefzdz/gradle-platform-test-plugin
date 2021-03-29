plugins {
    `java-gradle-plugin`
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.apache.httpcomponents:httpclient:4.5.13")
    implementation("commons-io:commons-io:2.8.0")
    implementation("com.google.guava:guava:30.1-jre")
}
