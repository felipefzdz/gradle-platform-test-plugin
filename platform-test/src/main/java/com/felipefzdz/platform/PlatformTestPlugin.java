package com.felipefzdz.platform;

import com.felipefzdz.base.extension.Probe;
import com.felipefzdz.kubernetes.extension.Deployment;
import com.felipefzdz.kubernetes.extension.KubernetesExtensionConfigurer;
import com.felipefzdz.kubernetes.extension.KubernetesTestExtension;
import com.felipefzdz.kubernetes.tasks.CleanupKubernetesTask;
import com.felipefzdz.kubernetes.tasks.DeployKubernetesTask;
import com.felipefzdz.platform.extension.PlatformExtensionConfigurer;
import com.felipefzdz.platform.extension.PlatformTestExtension;
import com.felipefzdz.platform.tasks.CleanupPlatformTask;
import com.felipefzdz.platform.tasks.DeployPlatformTask;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.internal.ConventionMapping;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;

import javax.inject.Inject;
import java.io.File;
import java.util.concurrent.Callable;

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
        project.getExtensions().add(PlatformTestExtension.class, PlatformTestExtension.NAME, extension);
        project.getTasks().register("deployPlatform", DeployPlatformTask.class);
        project.getTasks().register("cleanupPlatform", CleanupPlatformTask.class);
        project.getTasks().withType(DeployPlatformTask.class).configureEach(task -> configureDeployPlatformTask(task, project, extension));
        project.getTasks().withType(CleanupPlatformTask.class).configureEach(task -> configureCleanupPlatformTask(task, project, extension));
    }

    private void configureDeployPlatformTask(DeployPlatformTask task, Project project, PlatformTestExtension extension) {
        ConventionMapping taskMapping = task.getConventionMapping();
        taskMapping.map("projectDir", (Callable<File>) project::getProjectDir);
        taskMapping.map("config", (Callable<RegularFileProperty>) extension::getConfig);
        taskMapping.map("provision", (Callable<RegularFileProperty>) extension::getProvision);
        taskMapping.map("probe", (Callable<Probe>) extension::getProbe);
        taskMapping.map("footlooseVersion", (Callable<String>) extension::getFootlooseVersion);
    }

    private void configureCleanupPlatformTask(CleanupPlatformTask task, Project project, PlatformTestExtension extension) {
        ConventionMapping taskMapping = task.getConventionMapping();
        taskMapping.map("projectDir", (Callable<File>) project::getProjectDir);
        taskMapping.map("config", (Callable<RegularFileProperty>) extension::getConfig);
        taskMapping.map("footlooseVersion", (Callable<String>) extension::getFootlooseVersion);
    }

    private void configureKubernetes(Project project) {
        KubernetesTestExtension extension = KubernetesExtensionConfigurer.setupExtension(objectFactory, project);
        project.getExtensions().add(KubernetesTestExtension.class, KubernetesTestExtension.NAME, extension);
        project.getTasks().register("deployKubernetes", DeployKubernetesTask.class);
        project.getTasks().register("cleanupKubernetes", CleanupKubernetesTask.class);
        project.getTasks().withType(DeployKubernetesTask.class).configureEach(task -> configureDeployKubernetesTask(task, project, extension));
        project.getTasks().withType(CleanupKubernetesTask.class).configureEach(task -> configureCleanupKubernetesTask(task, project, extension));
    }

    private void configureDeployKubernetesTask(DeployKubernetesTask task, Project project, KubernetesTestExtension extension) {
        ConventionMapping taskMapping = task.getConventionMapping();
        taskMapping.map("projectDir", (Callable<File>) project::getProjectDir);
        taskMapping.map("probe", (Callable<Probe>) extension::getProbe);
        taskMapping.map("k3dVersion", (Callable<String>) extension::getK3dVersion);
        taskMapping.map("namespace", (Callable<Property<String>>) extension::getNamespace);
        taskMapping.map("deployment", (Callable<Deployment>) extension::getDeployment);
    }

    private void configureCleanupKubernetesTask(CleanupKubernetesTask task, Project project, KubernetesTestExtension extension) {
        ConventionMapping taskMapping = task.getConventionMapping();
        taskMapping.map("projectDir", (Callable<File>) project::getProjectDir);
        taskMapping.map("k3dVersion", (Callable<String>) extension::getK3dVersion);
    }

}
