package com.felipefzdz.kubernetes

class ScriptFuncTest extends BaseKubernetesFuncTest {

    def "run a test against a healthy scripted deployment"() {
        given:
        def port = 8082
        buildFile << """
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
"""

        successTestOnPort(port)

        when:
        gradleRunner("deployKubernetes").build()

        then:
        gradleRunner('test', '-s').build()

        when:
        gradleRunner("cleanupKubernetes").build()
        def writer = new StringWriter()
        def proc = ["docker", "ps"].execute()
        proc.consumeProcessOutput(writer, writer)
        proc.waitFor()

        then:
        !writer.toString().contains("rancher/k3d-proxy")
        !writer.toString().contains("rancher/k3s")
    }
}

