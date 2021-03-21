package com.felipefzdz.platform.tasks;

import com.felipefzdz.platform.infrastructure.PlatformTestInvoker;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;

public class CleanupPlatformTask extends DefaultTask {

    private final PlatformTestInvoker invoker;

    @Inject
    public CleanupPlatformTask(PlatformTestInvoker invoker) {
        this.invoker = invoker;
    }

    @TaskAction
    public void run() {
        invoker.cleanup();
    }
}
