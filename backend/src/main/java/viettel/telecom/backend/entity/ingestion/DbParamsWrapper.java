package viettel.telecom.backend.entity.ingestion;

import java.util.Map;

public class DbParamsWrapper {

    private Map<String, String> dbParams;

    public Map<String, String> getDbParams() {
        return dbParams;
    }

    public void setDbParams(Map<String, String> dbParams) {
        this.dbParams = dbParams;
    }
}
