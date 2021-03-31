package com.felipefzdz.platform.extension;

import com.felipefzdz.base.extension.Probe;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.model.ObjectFactory;

import javax.inject.Inject;
import java.io.File;

public class PlatformTestExtension {

    public static String NAME = "platform";

    private final Project project;

    private String footlooseVersion = "0.6.3";

    private final Probe probe;

    private final RegularFileProperty config;

    private final RegularFileProperty provision;

    @Inject
    public PlatformTestExtension(Project project, ObjectFactory objectFactory) {
        this.project = project;
        this.probe = objectFactory.newInstance(Probe.class);
        this.config = objectFactory.fileProperty();
        this.provision = objectFactory.fileProperty();
    }

    public String getFootlooseVersion() {
        return footlooseVersion;
    }

    public void setFootlooseVersion(String footlooseVersion) {
        this.footlooseVersion = footlooseVersion;
    }

    public File getProjectDir() {
        return project.getProjectDir();
    }

    public Probe getProbe() {
        return probe;
    }

    public void probe(Action<? super Probe> action) {
        action.execute(probe);
    }

    public RegularFileProperty getConfig() {
        return config;
    }

    public RegularFileProperty getProvision() {
        return provision;
    }
}
