package com.felipefzdz.kubernetes

class ScriptFuncTest extends BaseFuncTest {

    def "run a test against a healthy scripted deployment"() {
        given:
        def port = 8082
        buildFile << """
test {
    kubernetes {
        deployment {
            script = file("${resources.absolutePath}/scripted_deployment/install.sh")
            edge {
                name = "edge"
                port = 8081
            }
        }
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
        !writer.toString().contains("rancher/k3d-proxy")
        !writer.toString().contains("rancher/k3s")
    }
}

