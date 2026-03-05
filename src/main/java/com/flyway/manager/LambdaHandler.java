package com.flyway.manager;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.flyway.manager.request.MigrationRequest;
import com.flyway.manager.response.MigrationResponse;

import java.util.Map;

public class LambdaHandler implements RequestHandler<MigrationRequest, MigrationResponse> {

    private final DbConfigResolver configResolver = new DbConfigResolver();

    @Override
    public MigrationResponse handleRequest(MigrationRequest request, Context context) {
        return execute(request);
    }

    public MigrationResponse execute(MigrationRequest request) {
        long start = System.currentTimeMillis();
        String action = request.getAction();
        String dbSelector = request.getDbSelector();

        try {
            DbConfig dbConfig = configResolver.resolve(dbSelector);

            FlywayExecutor executor = new FlywayExecutor(
                    dbConfig,
                    request.getMigrationLocation(),
                    request.getBaselineVersion());

            Map<String, Object> result = switch (action.toLowerCase()) {
                case "status" -> executor.status();
                case "validate" -> executor.validate();
                case "migrate" -> executor.migrate();
                case "baseline" -> executor.baseline();
                case "rollback" -> executor.rollback();
                case "repair" -> executor.repair();
                default -> throw new IllegalArgumentException("Unknown action: " + action
                        + ". Valid actions: status, validate, migrate, baseline, rollback, repair");
            };

            long elapsed = System.currentTimeMillis() - start;
            return MigrationResponse.ok(action, dbSelector, elapsed, result);

        } catch (Exception e) {
            long elapsed = System.currentTimeMillis() - start;
            return MigrationResponse.fail(action, dbSelector, elapsed, e.getMessage());
        }
    }
}
