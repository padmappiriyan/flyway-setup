package com.flyway.manager.request;

public class MigrationRequest {
    private String dbSelector;
    private String action;
    private String baselineVersion; // optional: version to baseline at (for existing DBs)
    private String migrationLocation; // optional: e.g. s3:my-bucket/migrations or classpath:db/migration

    public MigrationRequest() {
    }

    public MigrationRequest(String dbSelector, String action) {
        this.dbSelector = dbSelector;
        this.action = action;
    }

    public String getDbSelector() {
        return dbSelector;
    }

    public void setDbSelector(String dbSelector) {
        this.dbSelector = dbSelector;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getBaselineVersion() {
        return baselineVersion;
    }

    public void setBaselineVersion(String baselineVersion) {
        this.baselineVersion = baselineVersion;
    }

    public String getMigrationLocation() {
        return migrationLocation;
    }

    public void setMigrationLocation(String migrationLocation) {
        this.migrationLocation = migrationLocation;
    }
}
