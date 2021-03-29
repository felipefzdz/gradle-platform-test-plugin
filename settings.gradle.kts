plugins {
    id("com.gradle.enterprise") version "3.5"
}

rootProject.name = "gradle-platform-test-plugin"
include("platform-shared")
include("kubernetes-test")
include("platform-test")

gradleEnterprise {
    buildScan {
        publishAlways()
        termsOfServiceUrl = "https://gradle.com/terms-of-service"
        termsOfServiceAgree = "yes"
    }
}
