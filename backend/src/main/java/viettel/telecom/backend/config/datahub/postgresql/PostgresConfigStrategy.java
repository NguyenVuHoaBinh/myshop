package viettel.telecom.backend.config.datahub.postgresql;

import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import viettel.telecom.backend.config.datahub.DataHubConfigStrategy;
import viettel.telecom.backend.config.datahub.RDBMSConfigConstants;
import viettel.telecom.backend.exception.InvalidDatabaseConfigException;
import viettel.telecom.backend.exception.YamlSerializationException;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component("postgresConfigStrategy")
public class PostgresConfigStrategy implements DataHubConfigStrategy {

    @Override
    public String createConfigContent(Map<String, String> dbParams, String dataHubUrl) {
        try {
            validateDbParams(dbParams);

            PostgresConfigBuilder builder = new PostgresConfigBuilder();

            // Configure the source
            builder.withSource(
                    "postgres",
                    dbParams.get(RDBMSConfigConstants.HOST_PORT),
                    dbParams.get(RDBMSConfigConstants.DATABASE),
                    dbParams.get(RDBMSConfigConstants.USERNAME),
                    dbParams.get(RDBMSConfigConstants.PASSWORD)
            );

            // Optional pattern configurations
            addPattern(builder, RDBMSConfigConstants.DATABASE_PATTERN,
                    dbParams.get("database_pattern_allow"),
                    dbParams.get("database_pattern_deny")
            );
            addPattern(builder, RDBMSConfigConstants.TABLE_PATTERN,
                    dbParams.get("table_pattern_allow"),
                    dbParams.get("table_pattern_deny")
            );

            // Configure the sink
            builder.withSink("datahub-rest", dataHubUrl);

            // Generate YAML
            return serializeToYaml(builder.build());

        } catch (IllegalArgumentException e) {
            throw new InvalidDatabaseConfigException("Invalid database parameters: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new YamlSerializationException("Failed to create configuration content: " + e.getMessage(), e);
        }
    }

    /**
     * Validates the required database parameters.
     *
     * @param dbParams The database parameters map.
     */
    private void validateDbParams(Map<String, String> dbParams) {
        if (dbParams == null ||
                !dbParams.containsKey(RDBMSConfigConstants.HOST_PORT) || dbParams.get(RDBMSConfigConstants.HOST_PORT).isBlank() ||
                !dbParams.containsKey(RDBMSConfigConstants.DATABASE) || dbParams.get(RDBMSConfigConstants.DATABASE).isBlank() ||
                !dbParams.containsKey(RDBMSConfigConstants.USERNAME) || dbParams.get(RDBMSConfigConstants.USERNAME).isBlank() ||
                !dbParams.containsKey(RDBMSConfigConstants.PASSWORD) || dbParams.get(RDBMSConfigConstants.PASSWORD).isBlank()) {
            throw new InvalidDatabaseConfigException("Missing required database parameters (host, database, username, or password).");
        }
    }

    /**
     * Adds a pattern configuration to the builder if allow or deny patterns are provided.
     *
     * @param builder      The PostgresConfigBuilder instance.
     * @param patternName  The name of the pattern (e.g., "database_pattern", "table_pattern").
     * @param allowPatterns Comma-separated allow patterns.
     * @param denyPatterns  Comma-separated deny patterns.
     */
    private void addPattern(PostgresConfigBuilder builder, String patternName, String allowPatterns, String denyPatterns) {
        List<String> allow = allowPatterns != null ? Arrays.asList(allowPatterns.split(",")) : null;
        List<String> deny = denyPatterns != null ? Arrays.asList(denyPatterns.split(",")) : null;

        if ((allow != null && !allow.isEmpty()) || (deny != null && !deny.isEmpty())) {
            builder.withPattern(patternName, allow, deny);
        }
    }

    /**
     * Serializes a configuration map to YAML format.
     *
     * @param config The configuration map.
     * @return The YAML string.
     */
    private String serializeToYaml(Map<String, Object> config) {
        try {
            DumperOptions options = new DumperOptions();
            options.setPrettyFlow(true);
            options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
            options.setExplicitStart(false); // Prevents "---"

            Yaml yaml = new Yaml(options);
            return yaml.dump(config);
        } catch (Exception e) {
            throw new YamlSerializationException("Failed to serialize configuration to YAML", e);
        }
    }
}
