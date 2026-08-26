# BMS Implementation Task Backlog

**Purpose:** a junior-friendly, ordered task list. Complete one task at a time. Do not start a task marked as blocked.

## Working rules

- Create a Git branch for each task: `feature/BMS-###-short-name`.
- Every task must have a short pull request description, tests, and a manual check before it is marked done.
- Keep commits small and named by task ID, for example: `BMS-012 add customer migration`.
- Do not build a screen that has no API contract or database support.
- When a task feels too large, stop and split it before coding.

## Milestone 0 — Development foundation

**Local development assumption:** PostgreSQL, Java, Node.js, Angular tooling, and Git are already installed on the developer computer. Use a local development database with non-production data. Docker, Nginx, staging, and production deployment are intentionally deferred to Milestone 8.

| ID | Task | Depends on | Done when |
|---|---|---|---|
| BMS-001 | Create Git repository and initial README | — | Repository has `main`, `.gitignore`, README, and the existing docs committed. |
| BMS-002 | Add project folder structure | BMS-001 | `apps/web`, `apps/bms-api`, `infra/compose`, and `infra/nginx` exist with README files explaining their purpose. |
| BMS-003 | Create local PostgreSQL development database | BMS-002 | A local `bms_dev` database and non-production application account exist; connection values are kept outside Git. |
| BMS-004 | Create Spring Boot API application | BMS-002 | API starts locally and exposes unauthenticated `GET /actuator/health` returning UP. |
| BMS-005 | Connect API to PostgreSQL | BMS-003, BMS-004 | API starts against the Compose database using environment variables only. |
| BMS-006 | Add Flyway and baseline migration | BMS-005 | A fresh database is migrated automatically and Flyway history records the migration. |
| BMS-007 | Create Angular web application | BMS-002 | Web app runs locally and displays a basic BMS page. |
| BMS-008 | Configure web-to-API local development proxy | BMS-004, BMS-007 | Browser can call an API health endpoint without CORS errors. |
| BMS-009 | Add code formatting and basic quality checks | BMS-004, BMS-007 | One command checks API tests and one command checks web tests/linting. |
| BMS-010A | Create local environment template | BMS-003, BMS-005, BMS-007 | `.env.example` documents every required local variable with safe placeholder values; `.env` is ignored by Git. |
| BMS-010B | Create local developer start guide | BMS-010A | README gives exact commands to start, stop, reset, and verify the local web, API, and database services. |
| BMS-010C | Document local configuration and secret rules | BMS-010A | Local variable names are documented; passwords and secret files are excluded from Git. |

**Milestone review:** from a clean clone, another developer can create the local database and start the API and web app by following the README.

## Milestone 1 — Identity, authorization, and audit

| ID | Task | Depends on | Done when |
|---|---|---|---|
| BMS-011 | Create user, role, and user-role migrations | BMS-006 | Tables, keys, uniqueness rules, and timestamps exist; migration runs on a clean database. |
| BMS-012 | Seed administrator role and development administrator | BMS-011 | A documented development-only account can log in; production credentials are not in source control. |
| BMS-013 | Implement password hashing and login endpoint | BMS-011 | Correct credentials log in; incorrect and inactive-user attempts fail safely. |
| BMS-014 | Implement session, logout, and CSRF protection | BMS-013 | Login creates an HttpOnly session; logout invalidates it; state-changing requests without CSRF are rejected. |
| BMS-015 | Define permission names and endpoint authorization | BMS-011 | Permissions such as customer read/create/update are checked by API tests. |
| BMS-016 | Implement current-user endpoint | BMS-014, BMS-015 | `GET /api/v1/me` returns identity and permissions for a logged-in user only. |
| BMS-017 | Create audit-event migration and audit service | BMS-006 | Auditable operations create immutable events without secrets. |
| BMS-018 | Build Angular login page and auth guard | BMS-007, BMS-013, BMS-016 | A user can log in, log out, and cannot open protected routes while logged out. |
| BMS-019 | Build application shell and permission-aware navigation | BMS-018 | Navigation is shown only for allowed modules; API remains the authority. |

**Milestone review:** demonstrate login, a forbidden API request (403), logout, and an audit event.

## Milestone 2 — Shared application capabilities

| ID | Task | Depends on | Done when |
|---|---|---|---|
| BMS-020 | Implement common API error envelope | BMS-004 | Validation, missing-resource, forbidden, and conflict errors match `API_CONVENTIONS.md`. |
| BMS-021 | Add optimistic locking/version conflict handling | BMS-020 | Two updates of the same editable record cause a clear 409 conflict for the stale request. |
| BMS-022 | Implement document-number sequence service | BMS-006 | Concurrent requests receive unique numbers; numbers never use `MAX()+1`. |
| BMS-023 | Implement idempotency-record storage and service | BMS-006 | Repeating a request with the same key returns the original result and does not run the action twice. |
| BMS-024 | Create reusable Angular table, pagination, empty-state, and confirmation components | BMS-007 | Components work in a sample page and are keyboard accessible. |
| BMS-025 | Create company-settings migration and API | BMS-020, BMS-017 | An authorized admin can read/update settings; changes are audited. |
| BMS-026 | Create company-settings Angular page | BMS-025, BMS-024 | Admin can save and reload settings with field validation. |

## Milestone 3 — Master data (first complete vertical slice)

| ID | Task | Depends on | Done when |
|---|---|---|---|
| BMS-027 | Create customer migration | BMS-006 | Customer fields, unique code, active state, version, and indexes exist. |
| BMS-028 | Implement customer API | BMS-027, BMS-020, BMS-021, BMS-017 | Authorized users can create, list, filter, view, update, and deactivate customers. |
| BMS-029 | Test customer API rules | BMS-028 | Tests cover validation, unique code, permissions, inactive state, and version conflict. |
| BMS-030 | Build customer list page | BMS-024, BMS-028 | Search, pagination, status filter, and Create button work. |
| BMS-031 | Build customer editor page | BMS-030 | Create/edit validation, save feedback, and unsaved-change warning work. |
| BMS-032 | Create supplier migration, API, tests, and screens | BMS-031 | Supplier management has the same quality and behaviour as customer management. |
| BMS-033 | Create unit and category management | BMS-024, BMS-020 | Units/categories can be created, listed, edited, and deactivated; referenced records cannot be deleted. |
| BMS-034 | Create item migration and API | BMS-033 | Item validation enforces unique SKU/barcode, item type/capability rules, and active state. |
| BMS-035 | Build item list and editor screens | BMS-034 | Item search, filters, creation, editing, and validation work. |
| BMS-036 | Add item supplier and customer/supplier price APIs | BMS-034 | Effective dates, minimum quantity, preferred supplier, and validation work. |
| BMS-037 | Build item prices and suppliers UI | BMS-036 | Authorized user can manage supplier links and price entries. |
| BMS-038 | Implement and test price resolver | BMS-036 | Tests prove valid best-match price, fallback default price, and manual-entry behaviour. |

**Milestone review:** create a customer, supplier, unit, category, item, and customer price; verify the price resolver selects the expected price.

## Milestone 4 — Quotes

| ID | Task | Depends on | Done when |
|---|---|---|---|
| BMS-039 | Create quote and quote-line migrations | BMS-006, BMS-034 | Tables preserve document and item snapshots, totals, statuses, versions, and links. |
| BMS-040 | Implement server-side document calculation policy | BMS-039 | Tests cover quantity, fixed/percent discount, tax, rounding, and document totals. |
| BMS-041 | Implement quote draft API | BMS-039, BMS-040, BMS-022 | Authorized users can create/update/delete drafts; API calculates totals. |
| BMS-042 | Implement quote state transitions | BMS-041, BMS-017 | Submit, send, accept, reject, cancel, and expiry rules are enforced and audited. |
| BMS-043 | Build quote list page | BMS-024, BMS-041 | List can search/filter by customer, date, and status. |
| BMS-044 | Build quote editor page | BMS-043, BMS-038, BMS-040 | Users can add catalogue/manual lines, preview calculated totals, and save a draft. |
| BMS-045 | Build quote actions and status timeline | BMS-044, BMS-042 | Only permitted valid actions appear; action requires confirmation and results are visible. |
| BMS-046 | Create quote PDF/print view | BMS-045, BMS-025 | Printable quote contains required company, customer, line, total, term, and status information. |

## Milestone 5 — Sales invoices and stock-out

| ID | Task | Depends on | Done when |
|---|---|---|---|
| BMS-047 | Create sales-invoice, lines, and quote-link migrations | BMS-039 | Schema preserves snapshots, totals, source links, status, and balance. |
| BMS-048 | Create inventory movement migration | BMS-006, BMS-034 | Append-only movements store item, quantity delta, source, date, actor, and reason. |
| BMS-049 | Implement inventory posting service | BMS-048 | Service posts movements atomically and prevents invalid/duplicate postings. |
| BMS-050 | Implement sales invoice draft and quote conversion APIs | BMS-047, BMS-040, BMS-042 | One accepted quote converts to one draft invoice; retry/double click cannot duplicate it. |
| BMS-051 | Implement sales confirmation and cancellation | BMS-049, BMS-050, BMS-023 | Confirm creates stock-out movements once; cancellation creates one reversal and requires a reason. |
| BMS-052 | Test sales and inventory transaction rules | BMS-051 | Tests cover negative-stock block, duplicates, invalid transitions, snapshots, and reversal. |
| BMS-053 | Build sales invoice list/editor/actions | BMS-024, BMS-051 | User can create, confirm, cancel, and view invoice status within permission rules. |
| BMS-054 | Build stock overview and movement-ledger pages | BMS-049, BMS-024 | Balances and movement history can be filtered and traced to their source document. |

## Milestone 6 — Purchasing and stock-in

| ID | Task | Depends on | Done when |
|---|---|---|---|
| BMS-055 | Create purchase-bill migrations | BMS-006, BMS-034 | Bill/line schema supports supplier, costs, snapshots, totals, statuses, and balances. |
| BMS-056 | Implement purchase draft and confirmation APIs | BMS-055, BMS-049, BMS-040, BMS-023 | Confirmation posts stock-in movements once and updates costing policy. |
| BMS-057 | Implement purchase cancellation and tests | BMS-056 | Cancellation requires a reason, posts the correct reversal, and obeys stock policy. |
| BMS-058 | Build purchase list/editor/actions | BMS-024, BMS-056 | Authorized users can create, confirm, cancel, and trace a purchase bill. |
| BMS-059 | Implement stock adjustment API and UI | BMS-049, BMS-024 | Authorized adjustment requires non-zero quantity and mandatory reason; movement is audited. |
| BMS-060 | Build reorder list | BMS-054 | List shows items below minimum stock, recommended quantity, and preferred supplier. |

## Milestone 7 — Payments and reporting

| ID | Task | Depends on | Done when |
|---|---|---|---|
| BMS-061 | Create payment and allocation migrations | BMS-047, BMS-055 | Schema records direction, status, allocations, void reason, and audit fields. |
| BMS-062 | Implement payment posting and allocation API | BMS-061, BMS-023 | Only confirmed documents in correct direction/currency can be allocated; overpayment is blocked. |
| BMS-063 | Implement payment voiding and tests | BMS-062 | Void requires permission/reason and exactly restores document balances. |
| BMS-064 | Build payment list and editor | BMS-024, BMS-062 | User can post a payment and select valid allocations. |
| BMS-065 | Build receivables/payables and customer/supplier statements | BMS-062 | Totals reconcile to confirmed documents and non-void allocations. |
| BMS-066 | Build sales, purchase, stock, payment, and audit reports | BMS-054, BMS-058, BMS-064 | Each report has documented filters, permissions, and source rules. |
| BMS-067 | Add CSV and PDF/print exports | BMS-066 | Exports use the same filters and permissions as the visible report. |
| BMS-068 | Build dashboard | BMS-065, BMS-066 | Permitted widgets show correct figures and shortcuts. |

## Milestone 8 — Migration, release, and handover

| ID | Task | Depends on | Done when |
|---|---|---|---|
| BMS-069 | Create legacy field-mapping workbook | — | Every legacy field has a BMS destination, transformation rule, owner, and exception handling. |
| BMS-070 | Build repeatable staging migration scripts | BMS-069, BMS-038, BMS-062 | Scripts import sample legacy data and preserve `legacy_source`, `legacy_id`, and legacy numbers. |
| BMS-071 | Create migration reconciliation report | BMS-070 | Counts, document totals, stock balances, and exceptions are compared against sources. |
| BMS-072 | Perform two migration rehearsals | BMS-071 | Both rehearsal reports are reviewed and exceptions are resolved/approved. |
| BMS-073 | Containerise API and web application | BMS-004, BMS-007 | Dockerfiles build the API and Angular production bundle successfully; no secrets are in images. |
| BMS-074 | Create production Docker Compose and Nginx configuration | BMS-073 | PostgreSQL, API, and web/Nginx services run behind one HTTPS-ready origin with persistent storage and health checks. |
| BMS-075 | Create staging environment and deploy rehearsal | BMS-074 | A staging deployment uses separate secrets and anonymised/test data; update and rollback are documented. |
| BMS-076 | Automate backup and document restore procedure | BMS-075 | Daily backup is configured; a non-production restore test succeeds. |
| BMS-077 | Add CI workflow | BMS-009, BMS-073 | A push/PR runs API tests, web checks, and container builds automatically. |
| BMS-078 | Create end-to-end workflow tests | BMS-068 | Tests cover quote-to-sale-to-payment, purchase-to-stock, cancellation, and authorization. |
| BMS-079 | Run pilot acceptance checklist | BMS-078 | All items in the business acceptance checklist pass or have approved exceptions. |
| BMS-080 | Production cutover and handover | BMS-072, BMS-076, BMS-079 | Final migration reconciles, backup is verified, users are trained, and support ownership is recorded. |

## First task to start now

Start with **BMS-001**. Do not jump to invoice or stock features. When BMS-001 through BMS-010 are complete, begin BMS-011.
