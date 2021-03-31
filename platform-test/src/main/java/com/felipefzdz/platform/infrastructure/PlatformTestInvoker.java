package com.felipefzdz.platform.infrastructure;

import com.felipefzdz.platform.base.infrastructure.HttpProbe;
import com.felipefzdz.platform.base.infrastructure.Shell;
import com.felipefzdz.platform.extension.PlatformTestExtension;
import com.felipefzdz.platform.base.tasks.Invoker;
import com.google.common.base.Stopwatch;
import org.gradle.api.GradleException;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.asList;

public class PlatformTestInvoker implements Invoker {

    private final Logger logger;
    private final PlatformTestExtension extension;
    private final Shell shell;

    public PlatformTestInvoker(PlatformTestExtension extension) {
        this.logger = Logging.getLogger(PlatformTestInvoker.class);
        this.extension = extension;
        this.shell = new Shell(logger, extension.getProjectDir());
    }

    @Override
    public void setup() {
        try {
            Stopwatch stopwatch = Stopwatch.createStarted();
            setupFootloose();
            logger.info("Footloose setup elapsed time: " + stopwatch);
            deployPlatform();
            executeProbe();
            logger.info("Platform available elapsed time: " + stopwatch);
        } catch (RuntimeException e) {
            throw new GradleException("Error while doing the platform setup", e);
        }
    }

    @Override
    public void cleanup() {
        try {
            cleanupFootloose();
        } catch (RuntimeException e) {
            throw new GradleException("Error while doing the platform cleanup", e);
        }
    }

    private void setupFootloose() {
        final List<String> command = join(getFootlooseCommand(extension.getFootlooseVersion(), extension.getConfig()), "create");
        shell.run(command);
    }

    private void cleanupFootloose() {
        shell.run(join(getFootlooseCommand(extension.getFootlooseVersion(), extension.getConfig()), "delete"));
    }

    private void deployPlatform() {
        final File provisionScript = extension.getProvision().get().getAsFile();
        copyFolder(provisionScript.getParentFile());
        executeScript(provisionScript.getName());
    }

    private void copyFolder(File file) {
        shell.run(asList("docker", "cp", file.getAbsolutePath() + "/.", "cluster-node0:/"));
    }

    private void executeScript(String script) {
        shell.run(asList("docker", "exec", "cluster-node0", "sh", script));
    }

    private void executeProbe() {
        final HttpProbe.HttpProbeStatus result = HttpProbe.run(extension.getProbe());
        if (result.failure) {
            generateSupportBundle();
            throw new GradleException("Error while probing the platform", result.maybeException.get());
        }
    }

    private void generateSupportBundle() {
    }

    private List<String> getFootlooseCommand(String footlooseVersion, RegularFileProperty config) {
        return asList("docker", "run", "--rm", "-v", "/var/run/docker.sock:/var/run/docker.sock",
                "-v", config.get().getAsFile().getAbsolutePath() + ":/footloose/footloose.yaml",
                "felipefzdz/docker-footloose:" + footlooseVersion);
    }

    private List<String> join(List<String> firstPartCommand, String... secondPartCommand) {
        return Stream.concat(firstPartCommand.stream(), Stream.of(secondPartCommand))
                .collect(Collectors.toList());
    }

}
