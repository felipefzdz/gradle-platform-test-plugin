package com.felipefzdz.platform.tasks;

import com.felipefzdz.base.extension.Probe;
import com.felipefzdz.platform.infrastructure.PlatformTestInvoker;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.internal.ConventionTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.TaskAction;

import java.io.File;

public class DeployPlatformTask extends ConventionTask {

    private File projectDir;

    private Probe probe;

    private RegularFileProperty config;

    private RegularFileProperty provision;

    private String footlooseVersion;

    @TaskAction
    public void run() {
        PlatformTestInvoker.setup(this);
    }

    @Internal
    public File getProjectDir() {
        return projectDir;
    }

    public void setProjectDir(File projectDir) {
        this.projectDir = projectDir;
    }

    @Input
    public Probe getProbe() {
        return probe;
    }

    public void setProbe(Probe probe) {
        this.probe = probe;
    }

    @InputFile
    public RegularFileProperty getConfig() {
        return config;
    }

    @InputFile
    public RegularFileProperty getProvision() {
        return provision;
    }

    @Input
    public String getFootlooseVersion() {
        return footlooseVersion;
    }

    public void setFootlooseVersion(String footlooseVersion) {
        this.footlooseVersion = footlooseVersion;
    }
}
