package com.felipefzdz.kubernetes.tasks;

import com.felipefzdz.kubernetes.infrastructure.KubernetesTestInvoker;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;

public class DeployToKubernetesTask extends DefaultTask {

    private final KubernetesTestInvoker invoker;

    @Inject
    public DeployToKubernetesTask(KubernetesTestInvoker invoker) {
        this.invoker = invoker;
    }

    @TaskAction
    public void run() {
        invoker.setup();
    }
}
