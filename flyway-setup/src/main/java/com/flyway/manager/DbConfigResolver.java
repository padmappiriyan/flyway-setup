package com.flyway.manager;

import java.io.InputStream;
import java.util.Properties;

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
                props.getProperty("password", "")
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to load config for selector: " + dbSelector, e);
        }
    }

    private DbConfig resolveFromSsm(String dbSelector) {
        // AWS SSM implementation - only used when deployed to Lambda
        // Reads from /flyway/{dbSelector}/host, /flyway/{dbSelector}/user, etc.
        throw new UnsupportedOperationException(
            "AWS SSM resolution not implemented yet. Set FLYWAY_ENV=local or remove it for local testing."
        );
    }
}
