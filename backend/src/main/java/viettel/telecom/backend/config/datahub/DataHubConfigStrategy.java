package viettel.telecom.backend.config.datahub;


import java.util.Map;

public interface DataHubConfigStrategy {
    String createConfigContent(Map<String, String> dbParams, String dataHubUrl);
}

