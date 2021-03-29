package com.felipefzdz.platform;

import com.felipefzdz.platform.extension.PlatformExtensionConfigurer;
import com.felipefzdz.platform.extension.PlatformTestExtension;
import com.felipefzdz.platform.infrastructure.PlatformTestInvoker;
import com.felipefzdz.tasks.CleanupPlatformTask;
import com.felipefzdz.tasks.DeployPlatformTask;
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
        PlatformTestExtension extension = PlatformExtensionConfigurer.setupExtension(objectFactory, project);
        PlatformTestInvoker invoker = new PlatformTestInvoker(extension);
        project.getExtensions().add(PlatformTestExtension.class, PlatformTestExtension.NAME, extension);
        project.getTasks().register("deployPlatform", DeployPlatformTask.class, invoker);
        project.getTasks().register("cleanupPlatform", CleanupPlatformTask.class, invoker);
    }

}
