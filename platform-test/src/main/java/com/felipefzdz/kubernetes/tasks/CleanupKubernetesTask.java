package com.felipefzdz.kubernetes.tasks;

import com.felipefzdz.kubernetes.infrastructure.KubernetesTestInvoker;
import org.gradle.api.internal.ConventionTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.TaskAction;

import java.io.File;

public class CleanupKubernetesTask extends ConventionTask {

    private File projectDir;

    private String k3dVersion;

    @TaskAction
    public void run() {
        KubernetesTestInvoker.cleanup(this);
    }

    @Internal
    public File getProjectDir() {
        return projectDir;
    }

    public void setProjectDir(File projectDir) {
        this.projectDir = projectDir;
    }

    @Input
    public String getK3dVersion() {
        return k3dVersion;
    }

    public void setK3dVersion(String k3dVersion) {
        this.k3dVersion = k3dVersion;
    }
}
