package viettel.telecom.backend.config.datahub.postgresql;

import viettel.telecom.backend.config.datahub.RDBMSConfigConstants;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PostgresConfigBuilder {

    private final Map<String, Object> config = new HashMap<>();

    public PostgresConfigBuilder withSource(String type, String hostPort, String database, String username, String password) {
        Map<String, Object> sourceConfig = new HashMap<>();
        sourceConfig.put(RDBMSConfigConstants.TYPE, type);

        Map<String, Object> sourceDetails = new HashMap<>();
        sourceDetails.put(RDBMSConfigConstants.HOST_PORT, hostPort);
        sourceDetails.put(RDBMSConfigConstants.DATABASE, database);
        sourceDetails.put(RDBMSConfigConstants.USERNAME, username);
        sourceDetails.put(RDBMSConfigConstants.PASSWORD, password);

        sourceConfig.put(RDBMSConfigConstants.CONFIG, sourceDetails);
        config.put(RDBMSConfigConstants.SOURCE, sourceConfig);
        return this;
    }

    public PostgresConfigBuilder withBooleanParam(String paramName, boolean value) {
        if (!config.containsKey(RDBMSConfigConstants.SOURCE)) {
            throw new IllegalStateException("Source must be defined before adding parameters.");
        }
        Map<String, Object> sourceConfig = (Map<String, Object>) ((Map<String, Object>) config.get(RDBMSConfigConstants.SOURCE)).get(RDBMSConfigConstants.CONFIG);
        sourceConfig.put(paramName, value);
        return this;
    }

    public PostgresConfigBuilder withPattern(String patternName, List<String> allowPatterns, List<String> denyPatterns) {
        if (!config.containsKey(RDBMSConfigConstants.SOURCE)) {
            throw new IllegalStateException("Source must be defined before adding patterns.");
        }

        Map<String, Object> patternConfig = new HashMap<>();
        if (allowPatterns != null && !allowPatterns.isEmpty()) {
            patternConfig.put(RDBMSConfigConstants.ALLOW, allowPatterns);
        }
        if (denyPatterns != null && !denyPatterns.isEmpty()) {
            patternConfig.put(RDBMSConfigConstants.DENY, denyPatterns);
        }

        Map<String, Object> sourceConfig = (Map<String, Object>) ((Map<String, Object>) config.get(RDBMSConfigConstants.SOURCE)).get(RDBMSConfigConstants.CONFIG);
        sourceConfig.put(patternName, patternConfig);
        return this;
    }

    public PostgresConfigBuilder withSink(String type, String serverUrl) {
        Map<String, Object> sinkConfig = new HashMap<>();
        sinkConfig.put(RDBMSConfigConstants.TYPE, type);

        Map<String, Object> sinkDetails = new HashMap<>();
        sinkDetails.put(RDBMSConfigConstants.SERVER, serverUrl);

        sinkConfig.put(RDBMSConfigConstants.CONFIG, sinkDetails);
        config.put(RDBMSConfigConstants.SINK, sinkConfig);
        return this;
    }

    public Map<String, Object> build() {
        return config;
    }
}

