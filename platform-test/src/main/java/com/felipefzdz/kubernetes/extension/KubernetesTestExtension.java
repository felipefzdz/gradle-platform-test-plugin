package com.felipefzdz.kubernetes.extension;

import com.felipefzdz.base.extension.Probe;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;

import javax.inject.Inject;
import java.io.File;

public class KubernetesTestExtension {

    public static String NAME = "kubernetes";

    private final Project project;

    private String k3dVersion = "v4.0.0";

    private final Probe probe;

    private final Deployment deployment;

    private final Property<String> namespace;

    @Inject
    public KubernetesTestExtension(Project project, ObjectFactory objectFactory) {
        this.project = project;
        this.probe = objectFactory.newInstance(Probe.class);
        this.namespace = objectFactory.property(String.class);
        this.deployment = objectFactory.newInstance(Deployment.class);
    }

    public String getK3dVersion() {
        return k3dVersion;
    }

    public void setK3dVersion(String k3dVersion) {
        this.k3dVersion = k3dVersion;
    }

    public File getProjectDir() {
        return project.getProjectDir();
    }

    public Probe getProbe() {
        return probe;
    }

    public void probe(Action<? super Probe> action) {
        action.execute(probe);
    }

    public Property<String> getNamespace() {
        return namespace;
    }

    public Deployment getDeployment() {
        return deployment;
    }

    public void deployment(Action<? super Deployment> action) {
        action.execute(deployment);
    }
}
