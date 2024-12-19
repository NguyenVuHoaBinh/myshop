package viettel.telecom.backend.config.datahub.postgresql;

import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import viettel.telecom.backend.config.datahub.RDBMSConfigConstants;
import viettel.telecom.backend.exception.InvalidDatabaseConfigException;
import viettel.telecom.backend.exception.YamlSerializationException;
import viettel.telecom.backend.config.datahub.DataHubConfigStrategy;

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

            builder.withSource(
                    "postgres",
                    dbParams.get(RDBMSConfigConstants.HOST_PORT),
                    dbParams.get(RDBMSConfigConstants.DATABASE),
                    dbParams.get(RDBMSConfigConstants.USERNAME),
                    dbParams.get(RDBMSConfigConstants.PASSWORD)
            );

            addBooleanParameter(builder, RDBMSConfigConstants.INCLUDE_TABLES, dbParams.get(RDBMSConfigConstants.INCLUDE_TABLES));
            addBooleanParameter(builder, RDBMSConfigConstants.INCLUDE_VIEWS, dbParams.get(RDBMSConfigConstants.INCLUDE_VIEWS));
            addBooleanParameter(builder, RDBMSConfigConstants.PROFILING_ENABLED, dbParams.get(RDBMSConfigConstants.PROFILING_ENABLED));
            addBooleanParameter(builder, RDBMSConfigConstants.STATEFUL_INGESTION_ENABLED, dbParams.get(RDBMSConfigConstants.STATEFUL_INGESTION_ENABLED));

            addPattern(builder, RDBMSConfigConstants.DATABASE_PATTERN,
                    dbParams.get("database_pattern_allow"),
                    dbParams.get("database_pattern_deny")
            );
            addPattern(builder, RDBMSConfigConstants.TABLE_PATTERN,
                    dbParams.get("table_pattern_allow"),
                    dbParams.get("table_pattern_deny")
            );

            builder.withSink("datahub-rest", dataHubUrl);

            return serializeToYaml(builder.build());

        } catch (IllegalArgumentException e) {
            throw new InvalidDatabaseConfigException("Invalid database parameters: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new YamlSerializationException("Failed to create configuration content: " + e.getMessage(), e);
        }
    }

    private void validateDbParams(Map<String, String> dbParams) {
        if (dbParams.get(RDBMSConfigConstants.HOST_PORT) == null ||
                dbParams.get(RDBMSConfigConstants.DATABASE) == null ||
                dbParams.get(RDBMSConfigConstants.USERNAME) == null ||
                dbParams.get(RDBMSConfigConstants.PASSWORD) == null) {
            throw new InvalidDatabaseConfigException("Missing required database parameters (host, database, username, or password).");
        }
    }

    private void addBooleanParameter(PostgresConfigBuilder builder, String paramName, String paramValue) {
        if (paramValue != null && !paramValue.isEmpty()) {
            builder.withBooleanParam(paramName, Boolean.parseBoolean(paramValue));
        }
    }

    private void addPattern(PostgresConfigBuilder builder, String patternName, String allowPatterns, String denyPatterns) {
        List<String> allow = allowPatterns != null ? Arrays.asList(allowPatterns.split(",")) : null;
        List<String> deny = denyPatterns != null ? Arrays.asList(denyPatterns.split(",")) : null;
        builder.withPattern(patternName, allow, deny);
    }

    private String serializeToYaml(Map<String, Object> configMap) {
        try {
            DumperOptions options = new DumperOptions();
            options.setPrettyFlow(true);
            options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);

            Yaml yaml = new Yaml(options);
            return yaml.dump(configMap);
        } catch (Exception e) {
            throw new YamlSerializationException("Error serializing configuration to YAML", e);
        }
    }
}
