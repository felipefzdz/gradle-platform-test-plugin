package com.felipefzdz.platform.tasks;

import com.felipefzdz.base.extension.Probe;
import com.felipefzdz.platform.infrastructure.PlatformTestInvoker;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.internal.ConventionTask;
import org.gradle.api.tasks.TaskAction;
import org.gradle.work.DisableCachingByDefault;

import java.io.File;

@DisableCachingByDefault
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

    public File getProjectDir() {
        return projectDir;
    }

    public void setProjectDir(File projectDir) {
        this.projectDir = projectDir;
    }

    public Probe getProbe() {
        return probe;
    }

    public void setProbe(Probe probe) {
        this.probe = probe;
    }

    public RegularFileProperty getConfig() {
        return config;
    }

    public void setConfig(RegularFileProperty config) {
        this.config = config;
    }

    public RegularFileProperty getProvision() {
        return provision;
    }

    public void setProvision(RegularFileProperty provision) {
        this.provision = provision;
    }

    public String getFootlooseVersion() {
        return footlooseVersion;
    }

    public void setFootlooseVersion(String footlooseVersion) {
        this.footlooseVersion = footlooseVersion;
    }
}
