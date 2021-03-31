package com.felipefzdz.platform.infrastructure;

import com.felipefzdz.base.infrastructure.HttpProbe;
import com.felipefzdz.base.infrastructure.Shell;
import com.felipefzdz.platform.tasks.CleanupPlatformTask;
import com.felipefzdz.platform.tasks.DeployPlatformTask;
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

public class PlatformTestInvoker {

    private static final Logger logger = Logging.getLogger(PlatformTestInvoker.class);

    public static void setup(DeployPlatformTask task) {
        try {
            Stopwatch stopwatch = Stopwatch.createStarted();
            Shell shell = new Shell(logger, task.getProjectDir());
            setupFootloose(task, shell);
            logger.info("Footloose setup elapsed time: " + stopwatch);
            deployPlatform(task, shell);
            executeProbe(task);
            logger.info("Platform available elapsed time: " + stopwatch);
        } catch (RuntimeException e) {
            throw new GradleException("Error while doing the platform setup", e);
        }
    }

    public static void cleanup(CleanupPlatformTask task) {
        try {
            Shell shell = new Shell(logger, task.getProjectDir());
            cleanupFootloose(task, shell);
        } catch (RuntimeException e) {
            throw new GradleException("Error while doing the platform cleanup", e);
        }
    }

    private static void cleanupFootloose(CleanupPlatformTask task, Shell shell) {
        shell.run(join(getFootlooseCommand(task.getFootlooseVersion(), task.getConfig()), "delete"));
    }

    private static void setupFootloose(DeployPlatformTask task, Shell shell) {
        final List<String> command = join(getFootlooseCommand(task.getFootlooseVersion(), task.getConfig()), "create");
        shell.run(command);
    }

    private static void deployPlatform(DeployPlatformTask task, Shell shell) {
        final File provisionScript = task.getProvision().get().getAsFile();
        copyFolder(provisionScript.getParentFile(), shell);
        executeScript(provisionScript.getName(), shell);
    }

    private static void copyFolder(File file, Shell shell) {
        shell.run(asList("docker", "cp", file.getAbsolutePath() + "/.", "cluster-node0:/"));
    }

    private static void executeScript(String script, Shell shell) {
        shell.run(asList("docker", "exec", "cluster-node0", "sh", script));
    }

    private static void executeProbe(DeployPlatformTask task) {
        final HttpProbe.HttpProbeStatus result = HttpProbe.run(task.getProbe());
        if (result.failure) {
            throw new GradleException("Error while probing the platform", result.maybeException.get());
        }
    }

    private static List<String> getFootlooseCommand(String footlooseVersion, RegularFileProperty config) {
        return asList("docker", "run", "--rm", "-v", "/var/run/docker.sock:/var/run/docker.sock",
                "-v", config.get().getAsFile().getAbsolutePath() + ":/footloose/footloose.yaml",
                "felipefzdz/docker-footloose:" + footlooseVersion);
    }

    private static List<String> join(List<String> firstPartCommand, String... secondPartCommand) {
        return Stream.concat(firstPartCommand.stream(), Stream.of(secondPartCommand))
                .collect(Collectors.toList());
    }
}
