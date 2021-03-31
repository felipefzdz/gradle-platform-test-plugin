package com.felipefzdz.platform

import org.apache.commons.io.FileUtils
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import java.lang.management.ManagementFactory

class BasePlatformFuncTest extends Specification {
    @Rule
    TemporaryFolder testProjectDir = new TemporaryFolder()

    File buildFile
    File resources
    File localBuildCacheDirectory

    def setup() {
        localBuildCacheDirectory = testProjectDir.newFolder('local-cache')
        testProjectDir.newFile('settings.gradle') << """
        buildCache {
            local {
                directory '${localBuildCacheDirectory.toURI()}'
            }
        }
    """
        buildFile = testProjectDir.newFile('build.gradle')
        buildFile << """
plugins {
  id "com.felipefzdz.platform-test"
  id "groovy"
}

repositories {
    mavenCentral()
}


dependencies {
    implementation "org.codehaus.groovy:groovy-all:2.5.8"
    testImplementation "org.spockframework:spock-core:1.3-groovy-2.5"
}
        """
        FileUtils.copyDirectory(new File("build/resources"), testProjectDir.root)
        resources = new File(testProjectDir.root, "functionalTest")

    }

    def writeTestSource(String source) {
        String packageName = (source =~ /package\s+([\w.]+)/)[0][1]
        String className = (source =~ /(class|interface)\s+(\w+)\s+/)[0][2]
        String sourceFilePackage = "src/test/groovy/${packageName.replace('.', '/')}"
        new File(testProjectDir.root, sourceFilePackage).mkdirs()
        testProjectDir.newFile("$sourceFilePackage/${className}.groovy") << source
    }

    def gradleRunner(String... arguments = ['test', '-s']) {
        GradleRunner.create()
                .withDebug(ManagementFactory.getRuntimeMXBean().getInputArguments().toString().indexOf("-agentlib:jdwp") > 0)
                .withProjectDir(testProjectDir.root)
                .withArguments(arguments)
                .withPluginClasspath()
                .forwardOutput()
    }

    def successTestOnPort(int port) {
        writeTestSource """
            package acme

            class SuccessfulTest extends spock.lang.Specification {

                def successTest() {
                    given:
                    def getConnection = new URL('http://localhost:${port}').openConnection()
                    
                    when:
                    getConnection.requestMethod = 'GET'
                    
                    then:
                    getConnection.responseCode == 200
                }
            }
        """
    }
}
