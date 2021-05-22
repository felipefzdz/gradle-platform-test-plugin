package com.felipefzdz.kubernetes.tasks;

import com.felipefzdz.base.extension.Probe;
import com.felipefzdz.kubernetes.extension.Deployment;
import com.felipefzdz.kubernetes.infrastructure.KubernetesTestInvoker;
import org.gradle.api.internal.ConventionTask;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;

import java.io.File;

public class DeployKubernetesTask extends ConventionTask {

    private File projectDir;

    private Probe probe;

    private String k3dVersion;

    private Property<String> namespace;

    private Deployment deployment;

    @TaskAction
    public void run() {
        KubernetesTestInvoker.setup(this);
    }

    @Input
    public File getProjectDir() {
        return projectDir;
    }

    public void setProjectDir(File projectDir) {
        this.projectDir = projectDir;
    }

    @Input
    public Probe getProbe() {
        return probe;
    }

    public void setProbe(Probe probe) {
        this.probe = probe;
    }
    @Input
    public String getK3dVersion() {
        return k3dVersion;
    }

    public void setK3dVersion(String k3dVersion) {
        this.k3dVersion = k3dVersion;
    }
    @Input
    public Property<String> getNamespace() {
        return namespace;
    }

    public void setNamespace(Property<String> namespace) {
        this.namespace = namespace;
    }
    @Input
    public Deployment getDeployment() {
        return deployment;
    }

    public void setDeployment(Deployment deployment) {
        this.deployment = deployment;
    }
}
