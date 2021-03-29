package com.felipefzdz.kubernetes.tasks;

import com.felipefzdz.kubernetes.extension.KubernetesTestExtension;
import com.felipefzdz.kubernetes.infrastructure.KubernetesTestInvoker;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.tasks.testing.Test;

public final class TestTaskConfigurer {

    private TestTaskConfigurer() {
    }

    public static void configureTestTask(Test test, KubernetesTestExtension extension, KubernetesTestInvoker invoker, Project project) {
        test.getExtensions().add(KubernetesTestExtension.class, KubernetesTestExtension.NAME, extension);
        test.doFirst(new SetupTaskAction(invoker));
        project.getGradle().getTaskGraph().afterTask(new CleanupTaskAction(invoker));
    }

    private static class SetupTaskAction implements Action<Task> {

        private final KubernetesTestInvoker invoker;

        public SetupTaskAction(KubernetesTestInvoker invoker) {
            this.invoker = invoker;
        }

        @Override
        public void execute(Task task) {
            invoker.setup();
        }
    }

    private static class CleanupTaskAction implements Action<Task> {

        private final KubernetesTestInvoker invoker;

        public CleanupTaskAction(KubernetesTestInvoker invoker) {
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
