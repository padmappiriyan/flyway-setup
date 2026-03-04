# Deployment

## Architecture

```
GitHub Actions → Build fat JAR → Deploy to AWS Lambda
                                      ↓
                              Lambda invoked manually
                                      ↓
                              Reads DB config from SSM Parameter Store
                                      ↓
                              Executes Flyway action against RDS MySQL
                                      ↓
                              Returns JSON result
```

## AWS Setup (One-time)

### 1. SSM Parameter Store

Store DB credentials per environment:

```
/flyway/production/host     = your-rds-endpoint.amazonaws.com
/flyway/production/port     = 3306
/flyway/production/database = myapp_production
/flyway/production/user     = flyway_admin
/flyway/production/password = <secure-string>
```

### 2. Lambda Function

- Runtime: Java 21
- Handler: `com.flyway.manager.LambdaHandler::handleRequest`
- Memory: 512 MB (recommended)
- Timeout: 300 seconds (5 min - migrations can take time)
- Environment variable: `FLYWAY_ENV=aws`
- VPC: Same VPC as your RDS instance
- IAM Role: Needs `ssm:GetParameter` permission for `/flyway/*`

### 3. Lambda Invocation

```bash
aws lambda invoke \
  --function-name flyway-manager \
  --payload '{"dbSelector":"production","action":"status"}' \
  --cli-binary-format raw-in-base64-out \
  response.json

cat response.json
```

## GitHub Actions

The workflow at `.github/workflows/deploy.yml`:
1. Triggers on push to `main`
2. Builds the fat JAR with Maven
3. Deploys to Lambda using AWS CLI

### Required GitHub Secrets

| Secret | Description |
|--------|-------------|
| `AWS_ACCESS_KEY_ID` | IAM user access key |
| `AWS_SECRET_ACCESS_KEY` | IAM user secret key |
| `AWS_REGION` | e.g., `ap-southeast-1` |
| `LAMBDA_FUNCTION_NAME` | e.g., `flyway-manager` |

## Production Workflow

1. Add new migration: `V4__add_phone_to_users.sql`
2. Add rollback: `R4__undo_add_phone_to_users.sql`
3. Push to main → GitHub Actions deploys new JAR to Lambda
4. Invoke Lambda: `{"dbSelector":"production","action":"validate"}` → check first
5. Invoke Lambda: `{"dbSelector":"production","action":"migrate"}` → apply
6. Invoke Lambda: `{"dbSelector":"production","action":"status"}` → verify
