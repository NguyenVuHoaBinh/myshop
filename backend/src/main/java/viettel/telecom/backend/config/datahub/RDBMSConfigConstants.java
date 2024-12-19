package viettel.telecom.backend.config.datahub;

public class RDBMSConfigConstants {
    private RDBMSConfigConstants() {
        // Private constructor to prevent instantiation
    }
    public static final String SOURCE = "source";
    public static final String TYPE = "type";
    public static final String CONFIG = "config";
    public static final String HOST_PORT = "host_port";
    public static final String DATABASE = "database";
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    public static final String SINK = "sink";
    public static final String SERVER = "server";

    public static final String INCLUDE_TABLES = "include.tables";
    public static final String INCLUDE_VIEWS = "include.views";
    public static final String PROFILING_ENABLED = "profiling.enabled";
    public static final String STATEFUL_INGESTION_ENABLED = "stateful.ingestion.enabled";

    public static final String DATABASE_PATTERN = "database_pattern";
    public static final String TABLE_PATTERN = "table_pattern";
    public static final String ALLOW = "allow";
    public static final String DENY = "deny";
}
