package com.felipefzdz.kubernetes.tasks;

import com.felipefzdz.kubernetes.infrastructure.KubernetesTestInvoker;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;

public class CleanupTask extends DefaultTask {

    private final KubernetesTestInvoker invoker;

    @Inject
    public CleanupTask(KubernetesTestInvoker invoker) {
        this.invoker = invoker;
    }

    @TaskAction
    public void run() {
        invoker.cleanup();
    }
}
