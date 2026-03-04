# Test Scenarios

## Scenario 1: Fresh Database

**Use case**: Brand new database, no existing tables. Start Flyway from scratch.

**Database**: `flyway_fresh` (empty)

### Steps

1. **Status** - Shows no migrations applied
2. **Migrate** - Applies V1 (users), V2 (orders), V3 (add email) in order
3. **Status** - Shows all 3 migrations as "Applied"
4. **Validate** - Confirms applied migrations match available scripts
5. **Rollback** - Undoes V3 (drops the email column)
6. **Status** - Shows V1, V2 applied; V3 pending
7. **Migrate** - Re-applies V3

### Key takeaway
Standard greenfield workflow: migrate → validate → rollback if needed.

---

## Scenario 2: Existing Database (Pre-Flyway Adoption)

**Use case**: Production database already has tables created manually (before Flyway was adopted). You want to bring it under Flyway management without re-running old migrations.

**Database**: `flyway_existing` (has `users` and `orders` tables with data)

### Steps

1. **Status** - Flyway sees no history table. Shows all migrations as pending.
2. **Baseline at V2** - Tells Flyway "the DB is already at V2". Creates `flyway_schema_history` table with a baseline entry at V2. V1 and V2 are now marked as "Below Baseline" and won't be re-executed.
3. **Status** - Shows V1 (Below Baseline), V2 (Baseline), V3 (Pending)
4. **Migrate** - Only applies V3 (add email column). Skips V1 and V2.
5. **Status** - All migrations resolved: V1 (Below Baseline), V2 (Baseline), V3 (Applied)
6. **Validate** - Everything clean

### Key takeaway
`baseline` is the standard way to adopt Flyway on an existing database. Set baseline version to the migration version that matches your current schema state.

---

## Migration Files

| File | Action |
|------|--------|
| `V1__create_users_table.sql` | Creates `users` table |
| `V2__create_orders_table.sql` | Creates `orders` table (FK to users) |
| `V3__add_email_to_users.sql` | Adds `email` column to `users` |

## Rollback Files

| File | Undoes |
|------|--------|
| `R1__undo_create_users_table.sql` | Drops `users` table |
| `R2__undo_create_orders_table.sql` | Drops `orders` table |
| `R3__undo_add_email_to_users.sql` | Drops `email` column from `users` |

Rollback is manual (Community Edition). Each R file must be maintained alongside its V file.
