package com.flyway.manager.response;

import java.util.List;
import java.util.Map;

public class MigrationResponse {
    private boolean success;
    private String action;
    private String dbSelector;
    private long executionTimeMs;
    private Map<String, Object> result;
    private String error;

    public MigrationResponse() {}

    public static MigrationResponse ok(String action, String dbSelector, long executionTimeMs, Map<String, Object> result) {
        MigrationResponse r = new MigrationResponse();
        r.success = true;
        r.action = action;
        r.dbSelector = dbSelector;
        r.executionTimeMs = executionTimeMs;
        r.result = result;
        return r;
    }

    public static MigrationResponse fail(String action, String dbSelector, long executionTimeMs, String error) {
        MigrationResponse r = new MigrationResponse();
        r.success = false;
        r.action = action;
        r.dbSelector = dbSelector;
        r.executionTimeMs = executionTimeMs;
        r.error = error;
        return r;
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getDbSelector() { return dbSelector; }
    public void setDbSelector(String dbSelector) { this.dbSelector = dbSelector; }
    public long getExecutionTimeMs() { return executionTimeMs; }
    public void setExecutionTimeMs(long executionTimeMs) { this.executionTimeMs = executionTimeMs; }
    public Map<String, Object> getResult() { return result; }
    public void setResult(Map<String, Object> result) { this.result = result; }
    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
}
