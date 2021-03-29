package com.felipefzdz.kubernetes;

import com.felipefzdz.kubernetes.extension.KubernetesExtensionConfigurer;
import com.felipefzdz.kubernetes.extension.KubernetesTestExtension;
import com.felipefzdz.kubernetes.infrastructure.KubernetesTestInvoker;
import com.felipefzdz.tasks.CleanupPlatformTask;
import com.felipefzdz.tasks.DeployPlatformTask;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.tasks.testing.Test;

import javax.inject.Inject;

import static com.felipefzdz.kubernetes.tasks.TestTaskConfigurer.configureTestTask;

public class KubernetesTestPlugin implements Plugin<Project> {
    private final ObjectFactory objectFactory;

    @Inject
    KubernetesTestPlugin(ObjectFactory objectFactory) {
        this.objectFactory = objectFactory;
    }

    @Override
    public void apply(Project project) {
        KubernetesTestExtension extension = KubernetesExtensionConfigurer.setupExtension(objectFactory, project);
        final KubernetesTestInvoker invoker = new KubernetesTestInvoker(extension);
        project.getTasks()
                .withType(Test.class)
                .configureEach(task -> configureTestTask(task, extension, invoker, project));
        project.getExtensions().add(KubernetesTestExtension.class, KubernetesTestExtension.NAME, extension);
        project.getTasks().register("deployToKubernetes", DeployPlatformTask.class, invoker);
        project.getTasks().register("cleanupKubernetes", CleanupPlatformTask.class, invoker);
    }

}
