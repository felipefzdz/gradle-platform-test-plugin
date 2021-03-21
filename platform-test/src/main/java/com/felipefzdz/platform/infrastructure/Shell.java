package com.felipefzdz.platform.infrastructure;

import org.gradle.api.logging.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class Shell {

    private final Logger logger;
    private final File projectDir;

    public Shell(Logger logger, File projectDir) {
        this.logger = logger;
        this.projectDir = projectDir;
    }

    public String run(List<String> command) {
        logger.info("Command to be executed: " + String.join(" ", command));
        ProcessBuilder builder = new ProcessBuilder(command)
                .directory(projectDir)
                .redirectOutput(ProcessBuilder.Redirect.PIPE)
                .redirectErrorStream(true);
        builder.environment().clear();

        builder.redirectErrorStream(true);

        try {
            StringBuilder processOutput = new StringBuilder();

            try (BufferedReader processOutputReader = new BufferedReader(
                    new InputStreamReader(builder.start().getInputStream()))) {
                String readLine;
                while ((readLine = processOutputReader.readLine()) != null) {
                    processOutput.append(readLine).append(System.lineSeparator());
                }
                builder.start().waitFor();
            }
            final String output = processOutput.toString().trim();
            logger.info("Command executed with output: " + output);
            return output;
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
