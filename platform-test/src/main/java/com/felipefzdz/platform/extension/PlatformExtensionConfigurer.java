package com.felipefzdz.platform.extension;

import com.felipefzdz.platform.base.extension.Probe;
import org.gradle.api.Project;
import org.gradle.api.model.ObjectFactory;

public class PlatformExtensionConfigurer {

    private PlatformExtensionConfigurer() {
    }

    public static PlatformTestExtension setupExtension(ObjectFactory objectFactory, Project project) {
        PlatformTestExtension extension = objectFactory.newInstance(PlatformTestExtension.class, project, objectFactory);
        defineConventions(extension.getProbe());
        return extension;
    }

    private static void defineConventions(Probe probe) {
        probe.getRetries().convention(20);
        probe.getDelay().convention(10);
        probe.getPort().convention(8081);
        probe.getStatus().convention(200);
        probe.getPath().convention("");
    }
}
