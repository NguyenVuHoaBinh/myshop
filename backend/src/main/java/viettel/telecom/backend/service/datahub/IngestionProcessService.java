package viettel.telecom.backend.service.datahub;

import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import viettel.telecom.backend.entity.ingestion.IngestionResult;

import java.io.*;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class IngestionProcessService {

    private static final Logger logger = LoggerFactory.getLogger(IngestionProcessService.class);
    private static final String OUTPUT_LOG_FILE = "ingestion_output.log";
    private static final int PROCESS_TIMEOUT_SECONDS = 60;

    /**
     * Starts the ingestion process based on the operating system.
     *
     * @return The started Process.
     * @throws IllegalStateException if the process fails to start.
     */
    public Process startProcess() {
        String os = System.getProperty("os.name").toLowerCase();
        String projectDir = System.getProperty("user.dir");

        logger.info("Starting ingestion process on OS: {}", os);

        try {
            String command = getCommandForOS(os, projectDir);

            ProcessBuilder processBuilder = createProcessBuilder(os, command);
            processBuilder.redirectErrorStream(true); // Combine stdout and stderr

            Process process = processBuilder.start();

            logger.info("Ingestion process started successfully with command: {}", command);
            return process;
        } catch (IOException e) {
            throw new IllegalStateException("Ingestion process failed to start: " + e.getMessage(), e);
        }
    }

    /**
     * Processes the output of the ingestion process and logs failures and warnings.
     *
     * @param process The Process to be monitored.
     * @return An IngestionResult containing details of the ingestion process.
     * @throws IllegalStateException if the process is interrupted or terminates with a non-zero exit code.
     */
    public IngestionResult processOutput(Process process) {
        IngestionResult result = new IngestionResult();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            AtomicBoolean hasFailures = new AtomicBoolean(false);
            AtomicBoolean hasWarnings = new AtomicBoolean(false);

            reader.lines().forEach(line -> {
                logger.info("Process Output: {}", line);

                // Detect failures and warnings while excluding false positives
                if (line.contains("failure") && !line.contains("[]")) {
                    result.getFailures().put(line, "Failure detected");
                    logger.warn("Failure detected: {}", line);
                    hasFailures.set(true);
                } else if (line.contains("warning") && !line.contains("[]")) {
                    result.getWarnings().put(line, "Warning detected");
                    logger.warn("Warning detected: {}", line);
                    hasWarnings.set(true);
                }
            });

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new IllegalStateException("Process exited with error code: " + exitCode);
            }

            // Set an appropriate message
            if (hasFailures.get()) {
                result.setMessage("Ingestion completed with failures.");
            } else if (hasWarnings.get()) {
                result.setMessage("Ingestion completed with warnings.");
            } else {
                result.setMessage("Ingestion pipeline completed successfully.");
            }

        } catch (Exception e) {
            logger.error("Error processing process output: {}", e.getMessage(), e);
            result.setMessage("Ingestion pipeline failed due to an error.");
            throw new IllegalStateException("Failed to process output", e);
        }
        return result;
    }



    /**
     * Determines the appropriate command for the OS.
     *
     * @param os The operating system name.
     * @param projectDir The project directory.
     * @return The command to execute.
     */
    private String getCommandForOS(String os, String projectDir) {
        if (os.contains("win")) {
            return Paths.get(projectDir, "run_ingestion.bat").toAbsolutePath().toString();
        } else {
            return Paths.get(projectDir, "run_ingestion.sh").toAbsolutePath().toString();
        }
    }

    /**
     * Creates a ProcessBuilder for the given OS and command.
     *
     * @param os The operating system name.
     * @param command The command to execute.
     * @return A configured ProcessBuilder instance.
     */
    private ProcessBuilder createProcessBuilder(String os, String command) {
        return os.contains("win")
                ? new ProcessBuilder("cmd.exe", "/c", command)
                : new ProcessBuilder(command);
    }

    /**
     * Processes a single line of output, updating the result object and logging.
     *
     * @param line The line of output.
     * @param result The IngestionResult to update.
     * @param writer The BufferedWriter to write to the output file.
     */
    private void processLine(String line, IngestionResult result, BufferedWriter writer) {
        try {
            writer.write(line);
            writer.newLine();
            logger.debug("Ingestion process output: {}", line);

            if (line.contains("failure")) {
                result.getFailures().put(line, "Failure detected");
                logger.warn("Failure detected: {}", line);
            } else if (line.contains("warning")) {
                result.getWarnings().put(line, "Warning detected");
                logger.warn("Warning detected: {}", line);
            }

        } catch (IOException e) {
            throw new UncheckedIOException("Error writing to output file", e);
        }
    }

    /**
     * Waits for the process to complete within the timeout period.
     *
     * @param process The process to wait for.
     */
    private void waitForProcessCompletion(Process process) {
        try {
            boolean processCompleted = process.waitFor(PROCESS_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (!processCompleted) {
                process.destroyForcibly();
                throw new IllegalStateException("Ingestion pipeline timed out.");
            }

            int exitCode = process.exitValue();
            if (exitCode != 0) {
                throw new IllegalStateException("Ingestion pipeline failed with exit code " + exitCode);
            }

            logger.info("Ingestion process completed successfully with exit code 0.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Ingestion pipeline interrupted: " + e.getMessage(), e);
        }
    }
}
