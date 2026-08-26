# Unified Commercial System — Rebuild Plan

**Prepared:** 25 August 2026  
**Status:** discovery complete; implementation not yet authorised  
**Purpose:** replace the two Microsoft Access applications with one reliable web system for goods, services, quotations, purchasing, inventory, invoicing, payments, and reporting.

## 1. Executive decision

Build **one system**, not a new version of either legacy file alone.

The previous applications represent two complementary business flows:

| Legacy database | What it does | Important capability to retain |
|---|---|---|
| `Gods.accdb` | Sale and purchase of physical goods | product catalogue, suppliers, purchasing, stock levels, replenishment, orders, invoices |
| `Service.accdb` | Sale of services and some products | quotation, approval/rejection, quote-to-invoice conversion, service/product line items |
| Current web project | Modern goods-focused implementation | secure login, customer/supplier/item management, sales/purchase invoices, payments, stock ledger, audit log, reports, deployment |

The target is a **unified commercial operations system**. A document can contain product, service, or manual lines; only stock-tracked products affect inventory. Quotes are first-class documents and can convert to sales invoices without retyping information.

## 2. What was reviewed

### Current project

The workspace contains a deployable Angular 22 + Spring Boot 4.1 + Java 21 + PostgreSQL application. It uses Flyway migrations, Docker Compose, Nginx, server-side sessions with CSRF protection, roles (`ADMIN`, `EDITOR`), audit events, and automated NAS deployment/backup guidance.

Implemented modules found in the source:

- authentication, user administration, audit events, company settings and lookup data;
- customers, suppliers, categories, units, items and item images;
- supplier associations, customer-specific and supplier-specific prices;
- sales and purchase invoices, manual invoice lines, printing and attachments;
- payment posting/voiding, stock adjustments and stock movement history;
- dashboard and operational reports.

Its important current business rules are sound and should be preserved: immutable item snapshots on documents, draft documents do not move stock, confirmation is transactional, cancellation creates reversing stock movements, and confirmed invoices/posted payments are not physically deleted.

Current gap: the service Access application’s **quotation lifecycle** is not represented as a first-class workflow. The current model also calls both sales and purchases “invoices,” which is workable technically but less clear for users.

### `Gods.accdb` (goods/inventory legacy system)

It contains customers, employees, shippers, suppliers, products, categories, inventory, inventory shrinkage, sales orders/order details, purchase orders/purchase order details, invoices, statuses, sales reports and settings.

Observed saved-query flows:

- product list excludes discontinued products;
- inventory summary calculates on-hand, available, current level, target/reorder level and reorder quantity;
- purchasing joins purchase orders to purchase details and supports receiving/posted-to-inventory state;
- sales orders contain product lines with allocation, shipment, invoicing, back-order and insufficient-inventory indicators;
- invoices are generated from orders;
- sales analysis groups submitted sales by period, employee, customer, product and category.

The database contains 56 user forms and 12 reports, including dashboards for active orders and items to reorder, order/purchase-order detail screens, inventory shrinkage, and invoice/customer/supplier reports. Its data volume is modest (for example: 45 products, 26 customers, 15 order-detail rows, 5 invoices; purchase tables currently empty), making a careful migration practical.

### `Service.accdb` (service/quote legacy system)

It contains customers, employees, line-item codes, quotes, quote line items, invoices, invoice line items, quote-to-invoice links, statuses, sales reports and settings.

Observed saved-query flows:

- one catalogue (`LineItemCodes`) classifies each entry as a service and/or product, with default description and price;
- quotes support new, submitted, approved, rejected, expired and invoiced states;
- quotes include job, service address, contact, expiry, payment terms and separate service/product tax totals;
- approved/active quotes can be duplicated and converted to invoices through `QuoteInvoices`;
- invoices can be paid/unpaid/past-due and contain product/service lines;
- reporting filters submitted invoices by customer, salesperson, product/service and calendar period.

The database contains 53 user forms and 10 reports, including quote, invoice, active/approved/expired/rejected quote and unpaid/past-due invoice screens. Its data volume is small (8 quotes, 3 invoices, 29 quote lines, 12 invoice lines), so its main value is the business process rather than the data quantity.

### Review limitation

I inspected every table, relationship name, saved query, object inventory and the current source/migrations. The Access forms/reports were inventoried by name but their visual layouts and embedded macro behavior were not rendered. The target plan therefore treats their visible workflow names and saved-query rules as evidence, and requires user acceptance testing before retiring Access.

## 3. Target objectives

1. Run goods and service sales in one browser-based system.
2. Create accurate quotations, gain approval, and convert approved quotes to invoices.
3. Purchase and sell stock-tracked products with a complete inventory ledger.
4. Preserve financial and stock history; never silently rewrite confirmed business documents.
5. Make daily work fast: customer search, item picker, defaults, clear status, printable documents, payment capture.
6. Provide reliable operational visibility: unpaid balances, overdue invoices, quote pipeline, sales, margin, stock/reorder and purchasing.
7. Secure access by role and make important changes auditable.
8. Be deployable on the NAS with backup, restore and upgrade procedures that can be tested.

## 4. Target scope and terminology

Use these user-facing names:

| Term | Meaning |
|---|---|
| Quote | non-posting sales offer with validity period; no stock movement or receivable |
| Sales invoice | customer billing document; confirmation posts receivable and, for stocked products, inventory outflow |
| Purchase bill | supplier purchase document; confirmation posts payable and, for stocked products, inventory inflow |
| Catalogue item | reusable product or service definition |
| Manual line | one-off charge/description retained only on its document |
| Stock movement | immutable ledger entry that explains every quantity change |

The initial release should cover one legal company and one base currency. Multi-company, multi-currency, tax filing/e-invoicing, accounting-ledger integration, warehouse/bin locations and mobile scanning are deliberately deferred until the core workflow is stable.

## 5. Roles and permissions

Start with four explicit roles instead of only `ADMIN` and `EDITOR`:

| Role | Main authority |
|---|---|
| Administrator | users, settings, all data, audit review, migration/maintenance |
| Sales | customers, quotes, sales invoices, payment entry (if approved), sales reports |
| Purchasing & inventory | suppliers, catalogue, purchase bills, receipts, stock adjustments, inventory reports |
| Finance | invoices/bills, payments, credit/refund/void approval, financial reports |

An individual may hold multiple roles. Permission checks must be enforced by the API, not just hidden in the Angular navigation.

## 6. Required workflows

### A. Catalogue and prices

1. Create a category and unit.
2. Create an item: `PRODUCT`, `SERVICE`, or `NON_STOCK`.
3. Set whether it can be sold, purchased and/or inventory-tracked.
4. Enter standard prices, preferred supplier and customer-specific prices where needed.
5. Deactivate rather than delete a referenced item.

### B. Quote to sale

```text
Draft quote → Submit → Send to customer → Accepted / Rejected / Expired
Accepted quote → Convert to draft sales invoice → Confirm → Receive payment(s) → Paid
```

- A quote has a customer, contact/job/service location, expiry date, terms, notes and snapshot line items.
- Conversion copies the approved quote and links source quote to destination invoice.
- A quote may be converted once by default. If split or partial invoicing is needed, enable it explicitly and display invoiced quantities.
- Editing an accepted quote creates a new revision or returns it to draft; it must not silently change the accepted commercial offer.

### C. Direct sale

```text
Draft sales invoice → Review/confirm → Stock decreases for stocked product lines → Payment(s) → Paid / Part paid / Overdue
```

- Service and manual lines never move stock.
- Confirmation checks stock policy. The business must choose either: block negative stock (recommended) or allow it only for authorised users with a warning.
- Cancellation is an auditable reversal, not deletion.

### D. Purchase and receiving

```text
Draft purchase bill/order → Confirm receipt → Stock increases for stocked product lines → Supplier payment(s) → Paid
```

For the first release, combine purchase order and supplier bill only if they always occur together. If ordering and receiving are separate real-world events, implement purchase order → partial receipt → supplier bill as a later controlled phase.

### E. Inventory control

```text
Opening balance / purchase receipt / sale confirmation / authorised adjustment / cancellation reversal
                                  ↓
                         immutable stock ledger
                                  ↓
                    current stock + reorder dashboard
```

Every adjustment requires a reason, date and user. Current stock is derived from or transactionally maintained from ledger movements; it is never edited directly. Reorder suggestions use minimum stock, preferred supplier and latest/standard purchase price.

### F. Payments and credit control

- Post one or many payments against a sales invoice or purchase bill.
- Enforce amount > 0 and prohibit overpayment unless a credit-balance design is approved.
- Void, do not delete, erroneous payments; retain who/when/why.
- Calculate payment status from confirmed, non-void payments.
- Show overdue status from due date and outstanding balance.

## 7. Target domain model

Use a common document backbone and separate business documents where their behaviour differs.

```text
Party (Customer | Supplier) ──< customer/supplier price >── CatalogueItem
CatalogueItem ──< ItemSupplier >── Supplier

Quote ──< QuoteLine >── CatalogueItem (optional)
Quote ──< QuoteRevision
Quote ──< SalesInvoice (source link)

SalesInvoice ──< SalesInvoiceLine >── CatalogueItem (optional)
PurchaseBill ──< PurchaseBillLine >── CatalogueItem (optional)
SalesInvoice / PurchaseBill ──< PaymentAllocation >── Payment

CatalogueItem ──< StockMovement >── source document/line
All important entities ──< AuditEvent
```

Key modelling rules:

- Store snapshot code, name, description, unit, tax, quantity, price, discount, cost and totals on every document line.
- Use `NUMERIC(19,4)` for quantities/money calculations and define one rounding policy per document currency.
- Use database constraints for invalid states, unique document numbers and foreign keys; use transactional services for cross-row workflows.
- Use optimistic locking/version columns on editable drafts to prevent users overwriting one another.
- Keep attachments outside the main business row; store metadata and use managed object/file storage when attachment volume grows.
- Maintain document sequence tables; never derive a number using `MAX(...) + 1`.

## 8. State model and non-negotiable controls

| Object | States | Key rule |
|---|---|---|
| Quote | Draft, Submitted, Sent, Accepted, Rejected, Expired, Cancelled | only draft can be freely edited |
| Sales invoice | Draft, Confirmed, Cancelled | confirmation posts stock/receivable atomically |
| Purchase bill | Draft, Confirmed, Cancelled | confirmation posts stock/payable atomically |
| Payment | Posted, Voided | never delete posted payment |
| Catalogue item | Active, Inactive | inactive items remain visible in history |
| Stock movement | Posted, Reversed (or reversal row) | stock history is append-only |

Required controls:

- server-side validation and role checks for every state transition;
- idempotency protection for confirm/post actions to avoid double posting;
- immutable audit log capturing actor, time, action, target, before/after summary and request correlation ID;
- no hard delete of confirmed documents, posted movements or posted payments;
- database backup before deployment and tested restore at least quarterly;
- automated unit, integration and end-to-end tests for financial calculations and every state transition.

## 9. Recommended technical architecture

Retain the existing technology direction, with a clean rebuild branch/repository:

- **Frontend:** Angular 22 standalone, lazy-loaded feature areas, Angular Material/Tailwind, Signals for local UI state and Signal Forms for new forms.
- **Backend:** Spring Boot 4.1, Java 21, Spring Security session authentication with CSRF, validation and transactional service layer.
- **Database:** PostgreSQL; Flyway migrations committed with the application.
- **Delivery:** Docker Compose + Nginx on the NAS; GitHub Actions deployment; health checks and backup before replacement.
- **Observability:** structured application logs, audit events, health endpoint, backup result notification and an administrator support page.

Organise backend modules by domain (`quotes`, `sales`, `purchasing`, `inventory`, `payments`, `catalogue`, `parties`, `reporting`, `identity`) rather than by technical layer alone. On the frontend, each feature owns routes, pages, API client, models and tests.

## 10. Reports and dashboard (first release)

- sales by date/customer/item/category/salesperson;
- quote pipeline: draft, sent, accepted, rejected, expired, conversion rate and value;
- customer statement and overdue receivables;
- supplier payable and purchases by supplier/item;
- stock on hand, low stock/reorder suggestion, stock movement ledger and adjustment report;
- gross margin by sales invoice/item (only where cost data is trustworthy);
- payment register and audit activity.

Every report needs filters, pagination/export and a definition of whether it uses draft, confirmed, cancelled, voided or paid documents. Do not mix these silently.

## 11. Data migration plan

Do not import directly into production.

1. **Freeze and back up:** make read-only copies of both `.accdb` files and record file hashes, extraction date and owner.
2. **Profile:** export each legacy table to staging CSV/JSON; measure blanks, duplicates, bad dates, invalid references and attachment volumes.
3. **Map:** create a signed field-by-field mapping. Examples: `Products` + service `LineItemCodes` → `catalogue_items`; `Orders`/`OrderDetails` → historical sales documents; `Quotes`/`QuoteLineItems` → quotes; `Inventory`/`InventoryShrinkage` → opening balance plus stock adjustments.
4. **Clean:** normalise customer/supplier duplicates; preserve original legacy ID, database name and document number in migration-reference fields.
5. **Dry run:** load staging, validate counts/totals and generate exception reports. Never invent missing values; exceptions need a business decision.
6. **User acceptance:** compare selected legacy invoices, quotes, customer balances and stock totals with the new system.
7. **Cutover:** take final backup, freeze Access entry, rerun the migration, reconcile, obtain business sign-off, then allow production entry.
8. **Archive:** retain the Access files read-only for the agreed legal retention period and document how to open them.

Because legacy record counts are small, manual reconciliation of every financial document is realistic and recommended.

## 12. Delivery roadmap and gates

| Phase | Deliverable | Exit criteria |
|---|---|---|
| 0. Discovery | approved glossary, process maps, roles, policies, migration mapping | owner signs the scope and unanswered decisions |
| 1. Foundation | identity, roles, settings, audit, migrations, CI, backup/restore drill | production-like environment passes security and restore tests |
| 2. Master data | parties, catalogue, prices, import tools | users can maintain validated active/inactive data |
| 3. Quote & sale | quote lifecycle, conversion, sales invoices, print/PDF | accepted quote converts correctly; no duplicate posting |
| 4. Purchase & inventory | purchase workflow, stock ledger, adjustments, reorder | every stock change has a traceable source |
| 5. Payments & reports | allocations, statements, dashboards, exports | balances and selected legacy totals reconcile |
| 6. Migration & pilot | rehearsal, training, controlled pilot | business acceptance and rollback plan approved |
| 7. Go-live | final migration, monitoring, support | reconciled opening position and stabilisation complete |

Do not schedule Phase 6 until the critical workflow tests and backup restore drill are passing.

## 13. Acceptance scenarios

The system is ready for pilot only when it can demonstrate all of these:

1. Create a service/product quote, submit it, accept it and convert it to a correct draft sales invoice.
2. Confirm a sales invoice containing stocked product, service and manual lines; only the stocked product changes stock.
3. Cancel that confirmed sale; an equal and opposite stock movement exists and history remains intact.
4. Confirm a purchase bill; stock rises and subsequent item cost/reporting follows the chosen cost policy.
5. Post two payments and void one; invoice balance and audit trail are correct.
6. Block an unauthorised user from settings, user management, price changes and protected state transitions.
7. Restore a backup into a non-production environment and verify data plus application start-up.
8. Reconcile migrated customer count, catalogue count, active quote total, invoice total and stock opening balance to approved legacy reports.

## 14. Recommendations

1. **Add quotations before expanding more screens.** Quote-to-invoice is the material legacy capability still missing.
2. **Use one catalogue with capability flags.** A product/service distinction alone is not enough: support sellable, purchasable and inventory-tracked flags.
3. **Choose a stock-costing policy before coding reports.** Weighted average is a practical default; FIFO should be selected only if the business or regulation requires it.
4. **Do not treat a “purchase invoice” as a sales invoice type in the UI.** Internally shared concepts are fine, but users should see “Sales invoice” and “Purchase bill/order.”
5. **Define negative-stock, tax, approval, numbering, payment and credit-note policies in writing.** These decisions change data model and workflow and cannot safely be guessed.
6. **Prefer import tools and traceable migration references over a one-time opaque script.** It makes repeat rehearsals and error correction safe.
7. **Keep the present app as reference until acceptance is complete.** Rebuild alongside it; do not overwrite the working production path.

## 15. Decisions required from the business owner

These are the only decisions that should be made before implementation begins:

1. Is the system for one company only at launch? What are its currency and tax rules?
2. Can sales create negative stock, or must confirmation be blocked?
3. Do purchases need separate purchase order, receiving and supplier-bill stages, including partial receipts?
4. Can one accepted quote generate multiple invoices, and are partial invoices required?
5. What is the required approval authority for discounts, prices, quotes, invoice cancellation, payment voids and stock adjustments?
6. Which document number formats are legally/business required?
7. Are customer credit limits and overdue blocks required at launch?
8. What reports and document layouts are mandatory on day one?
9. Are the legacy records needed as full history in the new system or only as a read-only archive plus opening balances?

## 16. Research basis

The plan aligns with the official technical guidance used by the current stack: PostgreSQL constraints protect row-level integrity and foreign-key relationships; Flyway migrations are version-controlled, ordered and repeatable across environments. See [PostgreSQL constraints documentation](https://www.postgresql.org/docs/current/ddl-constraints.html) and [Flyway migrations documentation](https://documentation.red-gate.com/flyway/flyway-concepts/migrations).

