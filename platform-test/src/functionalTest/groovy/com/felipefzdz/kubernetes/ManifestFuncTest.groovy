package com.felipefzdz.kubernetes

import org.rauschig.jarchivelib.ArchiveFormat
import org.rauschig.jarchivelib.Archiver
import org.rauschig.jarchivelib.ArchiverFactory

class ManifestFuncTest extends BaseKubernetesFuncTest {

    def "run a test against a healthy deployment"() {
        given:
        def port = 8082
        buildFile << """
kubernetes {
    deployment {
        manifests = files("${resources.absolutePath}/manifests_deployment/deployments.yaml", "${resources.absolutePath}/manifests_deployment/services.yaml")
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
        gradleRunner("deployToKubernetes").build()

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

    def "run a test against an unhealthy deployment"() {
        given:
        buildFile << """
test {
    kubernetes {
        deployment {
            manifests = files("${resources.absolutePath}/unhealthy_deployment/deployments.yaml", "${resources.absolutePath}/unhealthy_deployment/services.yaml")
            edge {
                name = "edge"
                port = 8081
            }
        }
        namespace = "kubernetes-test-plugin"
        probe {
            retries = 2
            delay = 10            
        }
    }
}    
"""

        writeTestSource """
            package acme

            class SuccessfulTest extends spock.lang.Specification {

                def successTest() {
                    given:
                    def getConnection = new URL('http://localhost:8081').openConnection()
                    
                    when:
                    getConnection.requestMethod = 'GET'
                    
                    then:
                    getConnection.responseCode == 200
                }
            }
        """

        expect:
        gradleRunner("deployToKubernetes").buildAndFail()
        gradleRunner("cleanupKubernetes").build()

        when:
        Archiver archiver = ArchiverFactory.createArchiver(ArchiveFormat.TAR);
        def supportBundle = new File(testProjectDir.root, "support-bundle.tar")
        archiver.extract(supportBundle, testProjectDir.root)
        def podsOutput = new File(testProjectDir.root, "namespace/get-pods.json").text

        then:
        podsOutput.contains("crccheck/hello-world:xxx")
        podsOutput.contains("\"namespace\": \"kubernetes-test-plugin\"")
    }
}

