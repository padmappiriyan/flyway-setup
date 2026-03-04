package com.flyway.manager;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flyway.manager.request.MigrationRequest;
import com.flyway.manager.response.MigrationResponse;

/**
 * Local runner for testing Flyway operations without Lambda.
 * Usage: mvn exec:java -Dexec.args='{"dbSelector":"local-fresh","action":"status"}'
 * Or:    java -cp target/flyway-manager-1.0.0.jar com.flyway.manager.LocalRunner '{"dbSelector":"local-fresh","action":"migrate"}'
 */
public class LocalRunner {

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.err.println("Usage: LocalRunner '<json-request>'");
            System.err.println("Example: LocalRunner '{\"dbSelector\":\"local-fresh\",\"action\":\"status\"}'");
            System.exit(1);
        }

        ObjectMapper mapper = new ObjectMapper();
        MigrationRequest request = mapper.readValue(args[0], MigrationRequest.class);

        System.out.println("=== Flyway Manager - Local Runner ===");
        System.out.println("DB Selector: " + request.getDbSelector());
        System.out.println("Action:      " + request.getAction());
        if (request.getBaselineVersion() != null) {
            System.out.println("Baseline:    " + request.getBaselineVersion());
        }
        System.out.println("=====================================\n");

        LambdaHandler handler = new LambdaHandler();
        MigrationResponse response = handler.execute(request);

        String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(response);
        System.out.println(json);
    }
}
