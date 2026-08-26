# BMS Operations Runbook

## Daily operations

- Check application health, failed-login alerts, low stock and overdue documents.
- Confirm the scheduled database backup completed. Do not expose PostgreSQL or pgAdmin publicly.

## Deployment

1. Confirm CI/tests pass and take a backup.
2. Apply the approved release through Docker Compose.
3. Verify API/web health checks, login and a read-only smoke test.
4. Review logs without printing secrets.
5. If migration or health checks fail, stop and restore the previous known-good release/database according to the rollback plan.

## Backup and restore

- Back up PostgreSQL daily and retain backups outside the application directory.
- Perform a restore test in non-production at least quarterly.
- Record date, operator, backup identifier, restore result and follow-up actions.

## Incident response

1. Preserve logs and state; do not delete evidence.
2. Restrict access or disable a compromised user if needed.
3. Tell the administrator what is affected and what was done.
4. Restore only after identifying the correct backup and confirming its target environment.
5. Record the incident and preventative action in the decision/change log.
