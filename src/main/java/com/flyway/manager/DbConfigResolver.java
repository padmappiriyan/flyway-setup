package com.flyway.manager;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetParametersByPathRequest;
import software.amazon.awssdk.services.ssm.model.Parameter;

public class DbConfigResolver {

    private static final String ENV_KEY = "FLYWAY_ENV";

    public DbConfig resolve(String dbSelector) {
        String env = System.getenv(ENV_KEY);
        if ("aws".equalsIgnoreCase(env)) {
            return resolveFromSsm(dbSelector);
        }
        return resolveFromProperties(dbSelector);
    }

    private DbConfig resolveFromProperties(String dbSelector) {
        String path = "db-configs/" + dbSelector + ".properties";
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(path)) {
            if (is == null) {
                throw new RuntimeException("Config not found: " + path);
            }
            Properties props = new Properties();
            props.load(is);
            return new DbConfig(
                    props.getProperty("host", "localhost"),
                    Integer.parseInt(props.getProperty("port", "3306")),
                    props.getProperty("database"),
                    props.getProperty("user", "root"),
                    props.getProperty("password", ""));
        } catch (Exception e) {
            throw new RuntimeException("Failed to load config for selector: " + dbSelector, e);
        }
    }

    private DbConfig resolveFromSsm(String dbSelector) {
        System.out.println("Fetching config from AWS Parameter Store for: " + dbSelector);
        try (SsmClient ssm = SsmClient.create()) {
            String path = String.format("/flyway/%s/", dbSelector);

            GetParametersByPathRequest request = GetParametersByPathRequest.builder()
                    .path(path)
                    .recursive(true)
                    .withDecryption(true)
                    .build();

            var response = ssm.getParametersByPath(request);
            Map<String, String> values = new HashMap<>();

            for (Parameter p : response.parameters()) {
                String key = p.name().substring(path.length());
                values.put(key, p.value());
            }

            if (values.isEmpty()) {
                throw new RuntimeException("No SSM parameters found at: " + path);
            }

            return new DbConfig(
                    values.getOrDefault("host", "localhost"),
                    Integer.parseInt(values.getOrDefault("port", "3306")),
                    values.get("database"),
                    values.getOrDefault("user", "root"),
                    values.getOrDefault("password", ""));
        } catch (Exception e) {
            throw new RuntimeException("SSM Resolution Failed: " + e.getMessage(), e);
        }
    }
}
