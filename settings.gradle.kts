plugins {
    id("com.gradle.enterprise") version "3.5"
}

rootProject.name = "gradle-platform-test-plugin"
include("kubernetes-test")
include("platform-test")
include("platform-test-base")

gradleEnterprise {
    buildScan {
        publishAlways()
        termsOfServiceUrl = "https://gradle.com/terms-of-service"
        termsOfServiceAgree = "yes"
    }
}
