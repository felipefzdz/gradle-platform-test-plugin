package com.felipefzdz.platform;

import com.felipefzdz.platform.extension.PlatformExtensionConfigurer;
import com.felipefzdz.platform.extension.PlatformTestExtension;
import com.felipefzdz.platform.infrastructure.PlatformTestInvoker;
import com.felipefzdz.tasks.CleanupPlatformTask;
import com.felipefzdz.tasks.DeployPlatformTask;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.tasks.testing.Test;

import javax.inject.Inject;

import static com.felipefzdz.platform.tasks.TestTaskConfigurer.configureTestTask;

public class PlatformTestPlugin implements Plugin<Project> {
    private final ObjectFactory objectFactory;

    @Inject
    PlatformTestPlugin(ObjectFactory objectFactory) {
        this.objectFactory = objectFactory;
    }

    @Override
    public void apply(Project project) {
        PlatformTestExtension extension = PlatformExtensionConfigurer.setupExtension(objectFactory, project);
        final PlatformTestInvoker invoker = new PlatformTestInvoker(extension);
        project.getTasks()
                .withType(Test.class)
                .configureEach(task -> configureTestTask(task, extension, invoker, project));
        project.getExtensions().add(PlatformTestExtension.class, PlatformTestExtension.NAME, extension);
        project.getTasks().register("deployPlatform", DeployPlatformTask.class, invoker);
        project.getTasks().register("cleanupPlatform", CleanupPlatformTask.class, invoker);
    }

}
