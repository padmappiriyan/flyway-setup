# Local Testing

## Quick Start

```bash
# Build
mvn clean package

# Setup local database
mysql -u root -p < scripts/init-existing-db.sql
```

## Manual Commands

Run individual actions using `LocalRunner`:

```bash
JAR=target/flyway-manager-1.0.0.jar

# Status
java -cp $JAR com.flyway.manager.LocalRunner '{"dbSelector":"local-fresh","action":"status"}'

# Migrate
java -cp $JAR com.flyway.manager.LocalRunner '{"dbSelector":"local-fresh","action":"migrate"}'

# Validate
java -cp $JAR com.flyway.manager.LocalRunner '{"dbSelector":"local-fresh","action":"validate"}'

# Baseline (for existing DB at version 2)
java -cp $JAR com.flyway.manager.LocalRunner '{"dbSelector":"local-existing","action":"baseline","baselineVersion":"2"}'

# Rollback (undo latest migration)
java -cp $JAR com.flyway.manager.LocalRunner '{"dbSelector":"local-fresh","action":"rollback"}'

# Repair (fix flyway history table)
java -cp $JAR com.flyway.manager.LocalRunner '{"dbSelector":"local-fresh","action":"repair"}'
```

## Request Format

```json
{
  "dbSelector": "local-fresh",
  "action": "migrate",
  "baselineVersion": "2"       // optional, only for baseline action
}
```

## Response Format

```json
{
  "success": true,
  "action": "migrate",
  "dbSelector": "local-fresh",
  "executionTimeMs": 245,
  "result": { ... },
  "error": null
}
```
