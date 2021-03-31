package com.felipefzdz.platform;

import com.felipefzdz.kubernetes.extension.KubernetesExtensionConfigurer;
import com.felipefzdz.kubernetes.extension.KubernetesTestExtension;
import com.felipefzdz.kubernetes.infrastructure.KubernetesTestInvoker;
import com.felipefzdz.base.tasks.CleanupPlatformTask;
import com.felipefzdz.base.tasks.DeployPlatformTask;
import com.felipefzdz.platform.extension.PlatformExtensionConfigurer;
import com.felipefzdz.platform.extension.PlatformTestExtension;
import com.felipefzdz.platform.infrastructure.PlatformTestInvoker;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.model.ObjectFactory;

import javax.inject.Inject;

public class PlatformTestPlugin implements Plugin<Project> {
    private final ObjectFactory objectFactory;

    @Inject
    PlatformTestPlugin(ObjectFactory objectFactory) {
        this.objectFactory = objectFactory;
    }

    @Override
    public void apply(Project project) {
        configurePlatform(project);
        configureKubernetes(project);
    }

    private void configurePlatform(Project project) {
        PlatformTestExtension extension = PlatformExtensionConfigurer.setupExtension(objectFactory, project);
        PlatformTestInvoker invoker = new PlatformTestInvoker(extension);
        project.getExtensions().add(PlatformTestExtension.class, PlatformTestExtension.NAME, extension);
        project.getTasks().register("deployPlatform", DeployPlatformTask.class, invoker);
        project.getTasks().register("cleanupPlatform", CleanupPlatformTask.class, invoker);
    }

    private void configureKubernetes(Project project) {
        KubernetesTestExtension extension = KubernetesExtensionConfigurer.setupExtension(objectFactory, project);
        final KubernetesTestInvoker invoker = new KubernetesTestInvoker(extension);
        project.getExtensions().add(KubernetesTestExtension.class, KubernetesTestExtension.NAME, extension);
        project.getTasks().register("deployToKubernetes", DeployPlatformTask.class, invoker);
        project.getTasks().register("cleanupKubernetes", CleanupPlatformTask.class, invoker);
    }

}
