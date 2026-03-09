package com.flyway.manager;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class RollbackExecutor {

    private final DbConfig dbConfig;
    private final Flyway flyway;
    private final String localRollbackDir;

    public RollbackExecutor(DbConfig dbConfig, Flyway flyway, String localRollbackDir) {
        this.dbConfig = dbConfig;
        this.flyway = flyway;
        this.localRollbackDir = localRollbackDir;
    }

    public Map<String, Object> execute() {
        Map<String, Object> result = new LinkedHashMap<>();

        MigrationInfo current = flyway.info().current();
        if (current == null || current.getVersion() == null) {
            result.put("success", false);
            result.put("error", "No migrations applied. Nothing to rollback.");
            return result;
        }

        String version = current.getVersion().toString();
        String rollbackFile = "db/rollback/R" + version + "__undo_" + findDescription(current) + ".sql";

        // Try exact match first, then scan for any R{version}__* file
        String sql = loadRollbackSql(rollbackFile);
        if (sql == null) {
            sql = scanForRollbackSql(version);
        }

        if (sql == null) {
            result.put("success", false);
            result.put("error", "No rollback script found for version " + version + ". Expected: " + rollbackFile);
            return result;
        }

        try (Connection conn = DriverManager.getConnection(
                dbConfig.getJdbcUrl(), dbConfig.getUser(), dbConfig.getPassword());
                Statement stmt = conn.createStatement()) {

            // Execute rollback SQL
            for (String statement : sql.split(";")) {
                String trimmed = statement.trim();
                if (!trimmed.isEmpty()) {
                    stmt.execute(trimmed);
                }
            }

            // Remove the rolled-back migration from flyway history
            // Delete the entry from flyway_schema_history then repair
            stmt.execute("DELETE FROM flyway_schema_history WHERE version = '" + version + "'");
            flyway.repair();

            result.put("success", true);
            result.put("rolledBackVersion", version);
            result.put("rollbackScript", rollbackFile);

            MigrationInfo newCurrent = flyway.info().current();
            result.put("currentVersion", newCurrent != null ? newCurrent.getVersion().toString() : "none");

        } catch (Exception e) {
            result.put("success", false);
            result.put("error", "Rollback failed: " + e.getMessage());
        }

        return result;
    }

    private String findDescription(MigrationInfo info) {
        return info.getDescription().toLowerCase().replace(" ", "_");
    }

    private String readFile(File file) {
        try {
            return java.nio.file.Files.readString(file.toPath());
        } catch (Exception e) {
            return null;
        }
    }

    private String loadRollbackSql(String path) {
        // If we have a local directory (e.g. from S3), look there first
        if (localRollbackDir != null) {
            // path is like "db/rollback/R1__undo.sql", we want just the filename
            String fileName = path.substring(path.lastIndexOf("/") + 1);
            File localFile = new File(localRollbackDir, fileName);
            if (localFile.exists()) {
                return readFile(localFile);
            }
        }

        // Fallback to classpath
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(path)) {
            if (is == null)
                return null;
            return new BufferedReader(new InputStreamReader(is))
                    .lines().collect(Collectors.joining("\n"));
        } catch (Exception e) {
            return null;
        }
    }

    private String scanForRollbackSql(String version) {
        // Try common naming pattern: R{version}__undo_*.sql
        // We scan a few known patterns
        String prefix = "db/rollback/R" + version + "__";
        String[] candidates = {
                prefix + "undo.sql",
        };
        for (String candidate : candidates) {
            String sql = loadRollbackSql(candidate);
            if (sql != null)
                return sql;
        }
        return null;
    }
}
