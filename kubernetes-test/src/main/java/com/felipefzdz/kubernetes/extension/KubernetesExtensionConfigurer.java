package com.felipefzdz.kubernetes.extension;

import com.felipefzdz.extension.Probe;
import org.gradle.api.Project;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;

public class KubernetesExtensionConfigurer {

    private KubernetesExtensionConfigurer() {
    }

    public static KubernetesTestExtension setupExtension(ObjectFactory objectFactory, Project project) {
        KubernetesTestExtension extension = objectFactory.newInstance(KubernetesTestExtension.class, project, objectFactory);
        defineConventions(extension.getNamespace(), extension.getProbe(), extension.getDeployment().getEdge());
        return extension;
    }

    private static void defineConventions(Property<String> namespace, Probe probe, Edge edge) {
        namespace.convention("default");
        probe.getRetries().convention(20);
        probe.getDelay().convention(10);
        probe.getPort().convention(8081);
        probe.getStatus().convention(200);
        probe.getPath().convention("");
        edge.getName().convention("edge");
        edge.getPort().convention(80);
    }
}
