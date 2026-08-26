# BMS Technical Architecture & Folder Structure

**Status:** approved starting blueprint — build after business-decision review  
**Architecture style:** modular monolith  
**Reason:** BMS is one business system with tightly related transactions. A modular monolith is simpler to learn, test, deploy and operate than microservices, while keeping clear boundaries for future growth.

## 1. Architecture at a glance

```text
Browser
   │ HTTPS / same origin
   ▼
Angular web application
   │ /api with session cookie + CSRF token
   ▼
Spring Boot API ───────► PostgreSQL
   │                         │
   ├── audit log             └── Flyway schema migrations
   └── file/attachment store
```

The browser never connects directly to PostgreSQL. The API is the only service allowed to apply business rules, calculate financial values, post stock movements and access private files.

## 2. Technology choices

| Area | Choice | Why |
|---|---|---|
| Frontend | Angular 22, TypeScript, Angular Material, Tailwind | modern, strongly typed, accessible UI components; consistent with current skills |
| Forms | Signal Forms for new forms | clear reactive state and validation in Angular 22 |
| Backend | Spring Boot 4.1, Java 21 | stable business/API layer with validation and transactions |
| Database | PostgreSQL | reliable relational constraints and transactions for financial/stock data |
| Schema evolution | Flyway | every database change is reviewable and reproducible |
| Security | server-side session + CSRF protection | simple and safe for an internal same-origin business application |
| Deployment | Docker Compose + Nginx | repeatable NAS deployment and a single public origin |
| Tests | Vitest (web), JUnit/Spring tests (API), later Playwright E2E | tests close to each technology and workflow |

## 3. Repository structure

```text
bms/
├── README.md
├── docs/
│   ├── ARCHITECTURE.md              # this document
│   ├── BUSINESS_REQUIREMENTS.md     # approved copy/link of business requirements
│   ├── API_CONVENTIONS.md
│   ├── DATA_MODEL.md
│   ├── DECISION_LOG.md
│   ├── MIGRATION_PLAN.md
│   └── RUNBOOK.md
├── apps/
│   ├── web/                         # Angular application
│   │   ├── src/app/
│   │   │   ├── core/                # singleton app services/infrastructure only
│   │   │   ├── layout/              # shell, navigation, top bar
│   │   │   ├── shared/              # reusable UI and utilities without business ownership
│   │   │   └── features/            # business areas, lazy-loaded
│   │   │       ├── auth/
│   │   │       ├── administration/
│   │   │       ├── parties/
│   │   │       ├── catalogue/
│   │   │       ├── quotes/
│   │   │       ├── sales/
│   │   │       ├── purchasing/
│   │   │       ├── inventory/
│   │   │       ├── payments/
│   │   │       ├── reporting/
│   │   │       └── dashboard/
│   │   └── src/styles/
│   └── api/                         # Spring Boot application
│       └── src/
│           ├── main/java/com/bms/
│           │   ├── common/          # errors, pagination, security helpers, base types
│           │   ├── identity/
│           │   ├── administration/
│           │   ├── parties/
│           │   ├── catalogue/
│           │   ├── quotes/
│           │   ├── sales/
│           │   ├── purchasing/
│           │   ├── inventory/
│           │   ├── payments/
│           │   ├── reporting/
│           │   └── audit/
│           ├── main/resources/db/migration/
│           └── test/java/com/bms/
├── infra/
│   ├── compose/
│   ├── nginx/
│   ├── scripts/
│   └── env/
├── tools/
│   ├── data-migration/
│   └── development/
└── .github/workflows/
```

## 4. Frontend structure

Each feature owns its pages, routes, API client, models and feature-specific UI. Do not create one giant global `services` or `models` folder.

```text
features/quotes/
├── quotes.routes.ts
├── data-access/
│   ├── quote-api.service.ts
│   ├── quote.models.ts
│   └── quote.store.ts                # only when state is shared in this feature
├── pages/
│   ├── quote-list/
│   └── quote-editor/
├── ui/
│   ├── quote-line-editor/
│   └── quote-status-chip/
└── quotes.routes.spec.ts
```

### Frontend rules

1. Use standalone Angular components and lazy-load each business feature.
2. `core` contains only app-wide singletons: auth, HTTP interceptors, guards, configuration, notifications and error handling.
3. `shared` contains generic reusable controls such as confirmation dialog, money input, date range, pagination and empty state. It must not import a business feature.
4. Use Signals for UI state; use `HttpClient` for server communication; do not put authoritative financial or stock calculations only in the browser.
5. Keep page components focused on orchestration. Extract reusable document-line editors, status chips and print views into `ui`.
6. Route guards improve navigation but never replace API authorization.
7. Every component is accessible by keyboard and has correctly labelled form controls and errors.

## 5. Backend structure

Each domain module follows the same internal pattern:

```text
sales/
├── api/                              # REST controller and request/response DTOs
├── application/                      # use cases: create, update, confirm, cancel
├── domain/                           # entities, value types, business policies
├── persistence/                      # repositories and database queries
└── mapper/                           # entity/DTO mapping where it improves clarity
```

### Backend rules

1. Controllers translate HTTP requests only; they do not contain business calculations or transaction orchestration.
2. Application services own use cases and transactions such as `ConfirmSalesInvoice`.
3. Domain policies calculate prices, totals, status transitions and stock effects using precise decimal values.
4. Repositories persist/query data only; they do not decide business rules.
5. API request DTOs are validated at the boundary. Return predictable error responses with a code, message and field errors when applicable.
6. Put cross-cutting concerns in `common`, not duplicated in every module.
7. Modules may call another module through its application-facing service, never directly reach into its repository or entity internals.

## 6. Domain boundaries

| Module | Owns | Must not own |
|---|---|---|
| Identity | users, roles, sessions, permissions | customer data/business documents |
| Administration | company settings, sequences, reference configuration | transaction posting |
| Parties | customers and suppliers | document balances |
| Catalogue | items, categories, units, price lists, supplier links | stock balance/movements |
| Quotes | quote lifecycle, revisions, conversion request | invoice confirmation or stock posting |
| Sales | sales invoices and lines | direct stock table updates |
| Purchasing | purchase bills and lines | direct stock table updates |
| Inventory | movement ledger, balances, adjustments, cost policy | customer/supplier document UI |
| Payments | payments and allocations | invoice line calculations |
| Reporting | read-only projections | source-of-truth writes |
| Audit | immutable events | authorization decision-making |

**Critical flow:** Sales/Purchasing asks Inventory to post a movement within the same transaction. Payments asks Sales/Purchasing for confirmed open documents, then updates balances through controlled application services.

## 7. Data architecture

Use PostgreSQL as the source of truth. Its responsibilities are enforcing IDs, uniqueness, foreign keys, valid values and transaction durability.

Core table groups:

```text
identity:       app_users, roles, user_roles
administration: company_settings, document_sequences
parties:        customers, suppliers
catalogue:      categories, units, items, item_suppliers, customer_prices, supplier_prices
quotes:         quotes, quote_lines, quote_revisions, quote_invoice_links
sales:          sales_invoices, sales_invoice_lines
purchasing:     purchase_bills, purchase_bill_lines
inventory:      stock_movements, stock_balances (optional cached projection)
payments:       payments, payment_allocations
audit:          audit_events
files:          attachments
```

### Database rules

- Use `NUMERIC(19,4)` for money and quantities; never floating-point for financial calculations.
- Write append-only movements/audit records; corrections create new reversing entries.
- Use foreign keys and database checks for local integrity. Use service transactions for multi-record state changes.
- Store `created_at`, `updated_at` and a version column on editable aggregates.
- Index document numbers, external references, statuses/dates, foreign keys and search columns based on actual query needs.
- Every schema change is a new Flyway migration; never edit an applied migration.

## 8. API conventions

Base path: `/api/v1`.

```text
GET    /api/v1/quotes
POST   /api/v1/quotes
GET    /api/v1/quotes/{id}
PUT    /api/v1/quotes/{id}
POST   /api/v1/quotes/{id}/submit
POST   /api/v1/quotes/{id}/accept
POST   /api/v1/quotes/{id}/convert-to-sales-invoice
```

Use plural nouns for resources and explicit action endpoints only for meaningful state changes. List endpoints use `page`, `size`, `sort` and defined filter parameters. Return ISO-8601 dates/times and consistent JSON error envelopes.

All confirm/post/void endpoints accept an idempotency key. Retrying the same request must return the original successful result rather than post a second time.

## 9. Security architecture

- Same-origin Angular/Nginx/API deployment.
- Server-side session cookie: `HttpOnly`, `Secure` in HTTPS, `SameSite=Lax` unless stricter configuration works for the deployment.
- CSRF token required for state-changing browser requests.
- Spring Security authorizes every endpoint based on permissions.
- Password hashes, database password and environment secrets never enter Git or client-side bundles.
- Attachments are validated by type and size and served only after authorization.
- Audit all sensitive actions, but never record passwords, session tokens or secrets.

## 10. Environments and deployment

| Environment | Purpose | Data |
|---|---|---|
| Local | developer learning/building | fake or scrubbed sample data |
| Test/Staging | integration, migration rehearsal, user acceptance | anonymised copy or approved test data |
| Production | live business use | real data, restricted access |

Each environment has its own `.env` file outside source control. Docker Compose starts PostgreSQL, API and Nginx/web. Production is upgraded only after backup, migration validation and health checks.

## 11. Testing strategy

```text
Unit tests          calculations, state transitions, policies
Integration tests   API + PostgreSQL + Flyway + transaction behaviour
Component tests     Angular page/form behaviour and accessibility
E2E tests           quote → sale → payment; purchase → stock; cancellation/void
Restore drill       backup can start a working non-production system
```

The first tests written for each financial workflow must cover duplicate confirm requests, invalid status transitions, negative stock policy, price/tax calculations, cancellation reversals and payment over-allocation.

## 12. Development order

1. Foundation: repository, Compose, CI, API skeleton, Angular shell, security, roles, audit, settings, Flyway and tests.
2. Master data: customers, suppliers, units, categories, catalogue and price rules.
3. Quotes: list/editor, status transitions, printable document and conversion.
4. Sales: invoice editor, confirmation, payment status and stock integration.
5. Purchasing and inventory: purchase bill, movement ledger, adjustments, reorder list and cost policy.
6. Payments and reporting: allocations, voiding, statements, dashboard and exports.
7. Migration, pilot and production cutover.

No phase begins until the previous phase has automated tests and the relevant business owner acceptance.

## 13. What we will create first

The first actual code milestone will create only the foundation folders/apps and a healthy empty application. It will not attempt invoices or stock before the security, database migration and testing base are working.

Before generating the project, copy the approved requirements into `bms/docs/BUSINESS_REQUIREMENTS.md` and complete the decision log. Then create the Angular and Spring Boot applications using their official generators, rather than hand-creating framework configuration.
