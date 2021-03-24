package com.felipefzdz.platform

class PlatformFuncTest extends BaseFuncTest {

    def "run a test against a healthy platform"() {
        given:
        def port = 8000
        buildFile << """
test {
    platform {
        config = file("${resources.absolutePath}/healthy_platform/config.yaml")
        provision = file("${resources.absolutePath}/healthy_platform/provision.sh")
        probe {
            port = ${port} 
            path = "/missing"
            status = 404
        }
    }
}    
"""

        successTestOnPort(port)

        expect:
        gradleRunner().build()

        when:
        def writer = new StringWriter()
        def proc = ["docker", "ps"].execute()
        proc.consumeProcessOutput(writer, writer)
        proc.waitFor()

        then:
        !writer.toString().contains("cluster-node0")
    }

    def "clean the footloose container even when the test fails"() {
        given:
        buildFile << """
test {
    platform {
        config = file("${resources.absolutePath}/healthy_platform/config.yaml")
        provision = file("${resources.absolutePath}/healthy_platform/provision.sh")
        probe {
            port = 8000 
            path = "/missing"
            status = 404
        }
    }
}    
"""

        writeTestSource """
            package acme

            class SuccessfulTest extends spock.lang.Specification {

                def successTest() {
                    given:
                    def getConnection = new URL('http://localhost:8000').openConnection()
                    
                    when:
                    getConnection.requestMethod = 'GET'
                    
                    then:
                    getConnection.responseCode == 201
                }
            }
        """

        expect:
        gradleRunner().buildAndFail()

        when:
        def writer = new StringWriter()
        def proc = ["docker", "ps"].execute()
        proc.consumeProcessOutput(writer, writer)
        proc.waitFor()

        then:
        !writer.toString().contains("cluster-node0")
    }

    def "run a test against an unhealthy platform"() {
        given:
        buildFile << """
test {
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
} 
"""

        writeTestSource """
            package acme

            class SuccessfulTest extends spock.lang.Specification {

                def successTest() {
                    given:
                    def getConnection = new URL('http://localhost:8000').openConnection()
                    
                    when:
                    getConnection.requestMethod = 'GET'
                    
                    then:
                    getConnection.responseCode == 200
                }
            }
        """

        expect:
        gradleRunner().buildAndFail()
    }

    def "run a test starting and cleaning up the workloads manually"() {
        given:
        def port = 8000
        buildFile << """
test {
    platform {
        config = file("${resources.absolutePath}/healthy_platform/config.yaml")
        provision = file("${resources.absolutePath}/healthy_platform/provision.sh")
        probe {
            port = ${port} 
            path = "/missing"
            status = 404
        }
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
}

