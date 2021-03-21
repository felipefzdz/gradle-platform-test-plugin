package com.felipefzdz.platform.tasks;

import com.felipefzdz.platform.extension.PlatformTestExtension;
import com.felipefzdz.platform.infrastructure.PlatformTestInvoker;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.tasks.testing.Test;

public final class TestTaskConfigurer {

    private TestTaskConfigurer() {
    }

    public static void configureTestTask(Test test, PlatformTestExtension extension, PlatformTestInvoker invoker, Project project) {
        test.getExtensions().add(PlatformTestExtension.class, PlatformTestExtension.NAME, extension);
        test.doFirst(new SetupTaskAction(invoker));
        project.getGradle().getTaskGraph().afterTask(new CleanupTaskAction(invoker));
    }

    private static class SetupTaskAction implements Action<Task> {

        private final PlatformTestInvoker invoker;

        public SetupTaskAction(PlatformTestInvoker invoker) {
            this.invoker = invoker;
        }

        @Override
        public void execute(Task task) {
            invoker.setup();
        }
    }

    private static class CleanupTaskAction implements Action<Task> {

        private final PlatformTestInvoker invoker;

        public CleanupTaskAction(PlatformTestInvoker invoker) {
            this.invoker = invoker;
        }

        @Override
        public void execute(Task task) {
            if (task instanceof Test) {
                invoker.cleanup();
            }
        }
    }

}
