package viettel.telecom.backend.service.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class LogManagementService {

    private static final Logger logger = LoggerFactory.getLogger(LogManagementService.class);

    private static final String LOG_DIRECTORY = "logs";
    private static final String LOG_FILE_NAME = "application.log";

    /**
     * Writes a log entry to the log file.
     *
     * @param level   The log level (e.g., INFO, ERROR).
     * @param message The log message.
     */
    public void writeLog(String level, String message) {
        try {
            Path logFile = ensureLogFile();
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            String logEntry = String.format("[%s] [%s] %s", timestamp, level, message);

            Files.writeString(logFile, logEntry + System.lineSeparator(), StandardOpenOption.APPEND);
            logger.info("Log written: {}", logEntry);
        } catch (IOException e) {
            logger.error("Failed to write log entry: {}", e.getMessage());
        }
    }

    /**
     * Retrieves logs from the log file.
     *
     * @param level The log level to filter by (optional).
     * @return A list of log entries matching the criteria.
     */
    public List<String> getLogs(String level) {
        try {
            Path logFile = Paths.get(LOG_DIRECTORY, LOG_FILE_NAME);
            if (!Files.exists(logFile)) {
                logger.warn("Log file does not exist.");
                return List.of("No logs available.");
            }

            try (Stream<String> lines = Files.lines(logFile)) {
                return lines
                        .filter(line -> level == null || line.contains("[" + level + "]"))
                        .collect(Collectors.toList());
            }
        } catch (IOException e) {
            logger.error("Failed to read logs: {}", e.getMessage());
            return List.of("Error reading logs.");
        }
    }

    /**
     * Ensures the log file exists and creates it if necessary.
     *
     * @return The path to the log file.
     * @throws IOException If the file cannot be created.
     */
    private Path ensureLogFile() throws IOException {
        Path logDir = Paths.get(LOG_DIRECTORY);
        if (!Files.exists(logDir)) {
            Files.createDirectories(logDir);
        }

        Path logFile = logDir.resolve(LOG_FILE_NAME);
        if (!Files.exists(logFile)) {
            Files.createFile(logFile);
        }

        return logFile;
    }
}
