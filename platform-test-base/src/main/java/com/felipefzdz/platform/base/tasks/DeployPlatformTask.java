package com.felipefzdz.platform.base.tasks;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;

public class DeployPlatformTask extends DefaultTask {

    private final Invoker invoker;

    @Inject
    public DeployPlatformTask(Invoker invoker) {
        this.invoker = invoker;
    }

    @TaskAction
    public void run() {
        invoker.setup();
    }
}
