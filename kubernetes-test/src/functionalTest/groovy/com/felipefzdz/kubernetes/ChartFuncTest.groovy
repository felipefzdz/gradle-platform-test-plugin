package com.felipefzdz.kubernetes

class ChartFuncTest extends BaseFuncTest {

    def "run a test against a helm chart based deployment"() {
        given:
        def repoContainerId = startPrivateHelmRepo()

        and:
        addChartToRepo()

        and:
        def port = 8082
        buildFile << """
test {
    kubernetes {
        deployment {
            chart {
                repo = "chartmuseum"
                name = "mychart"
                version = "0.1.0"
                release = "mychart"
                user = "myUser"
                password = "myPassword"
            } 
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

        and:
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

        cleanup:
        ["docker", "stop", repoContainerId].execute().waitForProcessOutput(System.out, System.err)
    }

    private String startPrivateHelmRepo() {
        def out = new StringWriter()
        ["docker", "run", "-d", "--rm", "-p", "8080:8080", "-e", "PORT=8080", "-e", "DEBUG=1",
         "-e", "STORAGE=local", "-e", "STORAGE_LOCAL_ROOTDIR=/charts",
         "-e", "BASIC_AUTH_USER=myUser", "-e", "BASIC_AUTH_PASS=myPassword",
         "-v", "${resources.absolutePath}/helm_chart_deployment/charts:/charts",
         "chartmuseum/chartmuseum:latest"].execute().waitForProcessOutput(out, System.err)
        out.toString().trim()
    }

    void addChartToRepo() {
        ["docker", "run", "--rm", "-v", "${resources.absolutePath}/helm_chart_deployment:/config",
         "dtzar/helm-kubectl", "/bin/sh", "add_chart_to_repo.sh"].execute().waitForProcessOutput(System.out, System.err)
    }
}

