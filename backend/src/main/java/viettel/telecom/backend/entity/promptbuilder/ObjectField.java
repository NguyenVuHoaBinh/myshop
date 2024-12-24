package viettel.telecom.backend.entity.promptbuilder;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class ObjectField {

    @JsonProperty("objectName")
    private String objectName;

    @JsonProperty("databaseName")
    private String databaseName;

    @JsonProperty("fields")
    private List<String> fields;

    // Getters and setters
    public String getObjectName() {
        return objectName;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public List<String> getFields() {
        return fields;
    }

    public void setFields(List<String> fields) {
        this.fields = fields;
    }
}
