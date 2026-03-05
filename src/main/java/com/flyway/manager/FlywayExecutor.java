package com.flyway.manager;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationInfoService;
import org.flywaydb.core.api.output.MigrateResult;
import org.flywaydb.core.api.output.ValidateResult;
import org.flywaydb.core.api.output.BaselineResult;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.util.*;

import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Object;

public class FlywayExecutor {

    private final DbConfig dbConfig;
    private final Flyway flyway;

    public FlywayExecutor(DbConfig dbConfig) {
        this(dbConfig, "classpath:db/migration", null);
    }

    public FlywayExecutor(DbConfig dbConfig, String location, String baselineVersion) {
        this.dbConfig = dbConfig;
        String finalLocation = (location != null && !location.isEmpty()) ? location : "classpath:db/migration";

        // If location starts with s3:, we manually download the files to /tmp
        // because flyway-s3 is a paid Teams feature.
        if (finalLocation.startsWith("s3:")) {
            finalLocation = downloadScriptsFromS3(finalLocation);
        }

        var config = Flyway.configure()
                .dataSource(dbConfig.getJdbcUrl(), dbConfig.getUser(), dbConfig.getPassword())
                .locations(finalLocation);

        // Ensure we allow out of order and mixed for robustness in this setup
        config.baselineOnMigrate(true);

        if (baselineVersion != null && !baselineVersion.isEmpty()) {
            config.baselineVersion(baselineVersion)
                    .baselineDescription("Baseline at version " + baselineVersion);
        }

        this.flyway = config.load();
    }

    private String downloadScriptsFromS3(String s3Location) {
        try {
            // s3Location is like s3:bucket-name/path/to/scripts/
            String path = s3Location.substring(3);
            int firstSlash = path.indexOf("/");
            String bucketName = path.substring(0, firstSlash);
            String prefix = path.substring(firstSlash + 1);

            File localDir = new File("/tmp/flyway-migrations-" + System.currentTimeMillis());
            if (!localDir.exists())
                localDir.mkdirs();

            S3Client s3 = S3Client.create();
            ListObjectsV2Response listResponse = s3.listObjectsV2(ListObjectsV2Request.builder()
                    .bucket(bucketName)
                    .prefix(prefix)
                    .build());

            for (S3Object s3Object : listResponse.contents()) {
                if (s3Object.key().endsWith(".sql")) {
                    String fileName = s3Object.key().substring(s3Object.key().lastIndexOf("/") + 1);
                    File localFile = new File(localDir, fileName);

                    ResponseInputStream<GetObjectResponse> s3ObjectStream = s3.getObject(GetObjectRequest.builder()
                            .bucket(bucketName)
                            .key(s3Object.key())
                            .build());

                    try (FileOutputStream out = new FileOutputStream(localFile)) {
                        s3ObjectStream.transferTo(out);
                    }
                    System.out.println("Downloaded from S3: " + s3Object.key() + " -> " + localFile.getAbsolutePath());
                }
            }
            // Return the filesystem path for Flyway
            return "filesystem:" + localDir.getAbsolutePath();
        } catch (Exception e) {
            throw new RuntimeException("Failed to download migrations from S3: " + e.getMessage(), e);
        }
    }

    public Map<String, Object> status() {
        MigrationInfoService info = flyway.info();
        Map<String, Object> result = new LinkedHashMap<>();

        MigrationInfo current = info.current();
        result.put("currentVersion", current != null ? current.getVersion().toString() : "none");
        result.put("schemaName", dbConfig.getDatabase());

        List<Map<String, String>> migrations = new ArrayList<>();
        for (MigrationInfo mi : info.all()) {
            Map<String, String> entry = new LinkedHashMap<>();
            entry.put("version", mi.getVersion() != null ? mi.getVersion().toString() : "n/a");
            entry.put("description", mi.getDescription());
            entry.put("type", mi.getType().name());
            entry.put("state", mi.getState().getDisplayName());
            entry.put("installedOn", mi.getInstalledOn() != null ? mi.getInstalledOn().toString() : "n/a");
            migrations.add(entry);
        }
        result.put("migrations", migrations);
        result.put("pendingCount", info.pending().length);
        return result;
    }

    public Map<String, Object> validate() {
        Map<String, Object> result = new LinkedHashMap<>();
        try {
            ValidateResult vr = flyway.validateWithResult();
            result.put("valid", vr.validationSuccessful);
            result.put("errorCount", vr.invalidMigrations.size());
            List<String> errors = new ArrayList<>();
            vr.invalidMigrations.forEach(m -> errors.add(m.version + " - " + m.errorDetails.errorMessage));
            result.put("errors", errors);
        } catch (Exception e) {
            result.put("valid", false);
            result.put("errorCount", 1);
            result.put("errors", List.of(e.getMessage()));
        }
        return result;
    }

    public Map<String, Object> migrate() {
        MigrateResult mr = flyway.migrate();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("migrationsExecuted", mr.migrationsExecuted);
        result.put("schemaVersion", mr.targetSchemaVersion);
        result.put("success", mr.success);

        List<Map<String, String>> details = new ArrayList<>();
        mr.migrations.forEach(m -> {
            Map<String, String> entry = new LinkedHashMap<>();
            entry.put("version", m.version);
            entry.put("description", m.description);
            entry.put("executionTime", m.executionTime + "ms");
            details.add(entry);
        });
        result.put("details", details);
        return result;
    }

    public Map<String, Object> baseline() {
        BaselineResult br = flyway.baseline();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("baselineVersion", br.baselineVersion);
        result.put("success", br.successfullyBaselined);
        return result;
    }

    public Map<String, Object> rollback() {
        RollbackExecutor rollbackExecutor = new RollbackExecutor(dbConfig, flyway);
        return rollbackExecutor.execute();
    }

    public Map<String, Object> repair() {
        flyway.repair();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("repaired", true);
        return result;
    }

    public Flyway getFlyway() {
        return flyway;
    }
}
