package com.felipefzdz.platform

class PlatformFuncTest extends BasePlatformFuncTest {

    def "run a test against a healthy platform"() {
        given:
        def port = 8000
        buildFile << """
platform {
    config = file("${resources.absolutePath}/healthy_platform/config.yaml")
    provision = file("${resources.absolutePath}/healthy_platform/provision.sh")
    probe {
        port = ${port} 
        path = "/missing"
        status = 404
    }
}
"""

        successTestOnPort(port)

        when:
        gradleRunner("deployPlatform").build()

        then:
        gradleRunner('test', '-s').build()

        when:
        gradleRunner("cleanupPlatform").build()
        def writer = new StringWriter()
        def proc = ["docker", "ps"].execute()
        proc.consumeProcessOutput(writer, writer)
        proc.waitFor()

        then:
        !writer.toString().contains("cluster-node0")
    }

    def "fails to deploy an unhealthy platform"() {
        when:
        buildFile << """
platform {
    config = file("${resources.absolutePath}/unhealthy_platform/config.yaml")
    provision = file("${resources.absolutePath}/unhealthy_platform/provision.sh")
    probe {
        retries = 2
        delay = 10            
        port = 8000 
        path = "/missing"
        status = 404
    }
}
"""

        then:
        gradleRunner("deployPlatform").buildAndFail()

        and:
        gradleRunner("cleanupPlatform").build()
    }
}

