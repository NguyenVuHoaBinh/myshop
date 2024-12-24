package viettel.telecom.backend.service.datahub;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import viettel.telecom.backend.config.datahub.DataHubConfigStrategy;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

@Service
public class ConfigGeneratorService {

    private static final Logger logger = LoggerFactory.getLogger(ConfigGeneratorService.class);
    private static final String DEFAULT_CONFIG_FILE_NAME = "dynamic_ingestion.yml";

    private final ApplicationContext applicationContext;

    public ConfigGeneratorService(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * Validates the provided database parameters.
     *
     * @param dbParams The database connection parameters.
     * @throws IllegalArgumentException if validation fails.
     */
    public void validateDbParams(Map<String, String> dbParams) {
        if (dbParams == null || dbParams.isEmpty() || !dbParams.containsKey("dbType")) {
            throw new IllegalArgumentException("Database parameters are invalid or missing required 'dbType' key.");
        }
        logger.info("Database parameters validated successfully.");
    }

    /**
     * Generates the YAML configuration content based on the dbType using the appropriate strategy.
     *
     * @param dbParams   The database connection parameters.
     * @param dataHubUrl The URL of the DataHub server.
     * @return The YAML configuration content.
     */
    public String createConfigContent(Map<String, String> dbParams, String dataHubUrl) {
        validateDbParams(dbParams);

        String dbType = dbParams.get("dbType").toLowerCase();
        logger.info("Creating configuration content for dbType: {}", dbType);

        try {
            DataHubConfigStrategy configStrategy = applicationContext.getBean(dbType + "ConfigStrategy", DataHubConfigStrategy.class);
            String configContent = configStrategy.createConfigContent(dbParams, dataHubUrl);
            logger.info("Configuration content generated successfully for dbType: {}", dbType);
            return configContent;
        } catch (Exception e) {
            throw new IllegalStateException("Error creating configuration content for dbType: " + dbType, e);
        }
    }

    /**
     * Writes the configuration content to a file.
     *
     * @param fileName The name of the file to write to.
     * @param content  The content to write.
     */
    public void writeConfigToFile(String fileName, String content) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            writer.write(content);
            logger.info("Configuration content written successfully to file: {}", fileName);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to write configuration file: " + fileName, e);
        }
    }

    /**
     * A convenience method to create and write the config to a default file.
     *
     * @param dbParams   The database connection parameters.
     * @param dataHubUrl The URL of the DataHub server.
     */
    public void createAndWriteConfig(Map<String, String> dbParams, String dataHubUrl) {
        String configContent = createConfigContent(dbParams, dataHubUrl);
        writeConfigToFile(DEFAULT_CONFIG_FILE_NAME, configContent);
    }
}
