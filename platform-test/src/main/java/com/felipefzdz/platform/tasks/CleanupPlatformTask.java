package com.felipefzdz.platform.tasks;

import com.felipefzdz.platform.infrastructure.PlatformTestInvoker;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.internal.ConventionTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.TaskAction;

import java.io.File;

public class CleanupPlatformTask extends ConventionTask {

    private File projectDir;

    private RegularFileProperty config;

    private String footlooseVersion;


    @TaskAction
    public void run() {
        PlatformTestInvoker.cleanup(this);
    }

    @Internal
    public File getProjectDir() {
        return projectDir;
    }

    public void setProjectDir(File projectDir) {
        this.projectDir = projectDir;
    }

    @InputFile
    public RegularFileProperty getConfig() {
        return config;
    }

    @Input
    public String getFootlooseVersion() {
        return footlooseVersion;
    }

    public void setFootlooseVersion(String footlooseVersion) {
        this.footlooseVersion = footlooseVersion;
    }
}
