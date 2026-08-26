# BMS Data Model

## Core relationships

```text
Customer ──< customer_item_prices >── Item ──< item_suppliers >── Supplier
Quote ──< quote_lines >── Item
Quote ──< quote_invoice_links >── SalesInvoice ──< sales_invoice_lines >── Item
PurchaseBill ──< purchase_bill_lines >── Item
Payment ──< payment_allocations >── SalesInvoice | PurchaseBill
Item ──< stock_movements
User ──< audit_events
```

## Table groups

| Group | Tables |
|---|---|
| Identity | app_users, roles, user_roles |
| Administration | company_settings, document_sequences |
| Parties | customers, suppliers |
| Catalogue | categories, units, items, item_suppliers, customer_item_prices, supplier_item_prices |
| Quotes | quotes, quote_lines, quote_revisions, quote_invoice_links |
| Sales | sales_invoices, sales_invoice_lines |
| Purchasing | purchase_bills, purchase_bill_lines |
| Inventory | stock_movements, optional stock_balances projection |
| Payments | payments, payment_allocations |
| Supporting | attachments, audit_events, idempotency_records |

## Data rules

- All tables have a primary key. Editable aggregates have `version`, `created_at` and `updated_at`.
- Money and quantities use `NUMERIC(19,4)`, never floating point.
- Confirmed document lines store item snapshots; historical values do not follow later item changes.
- Foreign keys, unique document numbers, positive quantities and valid statuses are enforced in PostgreSQL.
- Stock movement, audit and payment history are append-only; correction creates a reversal/void record.
- Flyway owns all schema changes in `apps/bms-api/src/main/resources/db/migration`.
