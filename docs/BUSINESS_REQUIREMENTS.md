# Business Management System (BMS)
## Business Requirements & Workflow Specification

**Version:** 0.1 — proposed baseline  
**Prepared:** 25 August 2026  
**Status:** ready for owner review; no build work approved by this document  
**Companion document:** `SYSTEM_REBUILD_PLAN.md`

## 1. Purpose

This document defines the required behaviour of BMS before development begins. It turns the rebuild plan into testable requirements for one system that manages customers, suppliers, products, services, quotations, sales, purchasing, stock and payments.

Requirements marked **Proposed default** are sensible starting rules based on the legacy Access databases and current application. They must be confirmed or changed by the business owner before the associated module is built.

## 2. Product scope

### In scope for release 1

- user login, role-based permissions and audit history;
- company settings and document numbering;
- customer and supplier management;
- catalogue of products, services and one-off manual lines;
- quotation lifecycle and quote-to-sales-invoice conversion;
- direct sales invoices;
- purchase bills and stock receiving;
- stock ledger, stock adjustment and reorder list;
- payment recording, voiding and balances;
- printable/exportable quotes, sales invoices and purchase bills;
- operational dashboard and reports;
- migration from `Gods.accdb` and `Service.accdb`.

### Excluded from release 1

- multi-company and multi-currency accounting;
- payroll, CRM campaigns, point-of-sale, mobile barcode scanning;
- tax authority e-invoicing or general-ledger integration;
- multiple warehouses/bins, manufacturing, serial/lot tracking;
- customer self-service portal and online payment gateway.

## 3. Business glossary

| Term | Definition |
|---|---|
| Catalogue item | Reusable product or service definition with code, description, price and flags. |
| Product | A sellable/purchasable catalogue item. It may track stock. |
| Service | A sellable/purchasable catalogue item that does not track stock. |
| Manual line | A document-only line, not saved in the catalogue. |
| Quote | A time-limited offer to a customer. It has no financial or stock posting effect. |
| Sales invoice | Customer billing document. Confirmation makes it operationally final and may reduce stock. |
| Purchase bill | Supplier purchase document. Confirmation may add stock and creates a supplier payable. |
| Payment | Money received from a customer or paid to a supplier. |
| Payment allocation | The amount of one payment applied to one invoice/bill. |
| Stock movement | Immutable entry that explains a product quantity increase or decrease. |
| Draft | Editable document not yet posted. |
| Confirmed | Posted document; financial/stock impact is recorded. |
| Cancelled | A confirmed document that was reversed, not erased. |

## 4. Users and access

### 4.1 Roles

| Role | Create/edit draft | Confirm/cancel | Payments | Stock adjustment | Settings/users | Reports |
|---|---:|---:|---:|---:|---:|---:|
| Administrator | all | all | all | all | all | all |
| Sales | customers, quotes, sales invoices | sales invoices only if allowed | receive only if allowed | no | no | sales only |
| Purchasing & Inventory | suppliers, catalogue, purchase bills | purchase bills only if allowed | no | yes if allowed | no | purchase/stock only |
| Finance | customers/suppliers, documents | invoices/bills, cancellation if allowed | receive/pay/void | no | no | financial only |

**Proposed default:** Confirmation, cancellation, price override, stock adjustment and payment void each require an explicit permission, independently assignable by an administrator.

### 4.2 Security requirements

1. Users must log in using a unique username and password.
2. Passwords must be stored only as secure hashes; no plain-text passwords are stored or shown.
3. Inactive users cannot log in.
4. The API must enforce permissions for every operation; hiding a menu is not security.
5. A user can change only their own password unless permitted to administer users.
6. Failed login attempts and significant business actions are auditable.
7. Sessions expire after a configurable period of inactivity. **Proposed default:** 30 minutes.

## 5. Cross-cutting business rules

1. A confirmed document, posted payment or stock movement is never physically deleted.
2. Draft documents may be deleted only by authorised users; deletion is logged.
3. All documents keep a snapshot of line description, code, unit, price, discount, tax and totals so later catalogue edits do not change history.
4. Monetary and quantity values use four decimal places internally; printed values use the configured display precision. **Proposed default:** 2 decimals for currency display.
5. The server calculates all totals. The web application may preview totals but cannot be the source of truth.
6. A user must see a clear warning and confirm before an irreversible state transition.
7. Every record has created/updated timestamps and the user who performed the material operation where relevant.
8. All list pages support search, filters, pagination, sort and export where the user has report access.
9. Document numbers are unique, assigned by a safe sequence, and never generated from the current maximum number.
10. Dates are stored with a clear business timezone. **Proposed default:** Asia/Phnom_Penh.

## 6. Master-data requirements

### 6.1 Company settings

The administrator can manage:

- legal/company name, address, phones, email, website and tax registration number;
- logo for printed documents;
- base currency, display format and default tax rates;
- payment terms defaults;
- document prefixes and initial sequence numbers;
- low-stock/default reorder policy;
- business timezone and document footer/terms text.

Acceptance criteria:

- A saved setting is used on future documents only; historical printouts retain their stored document/company snapshot where legally needed.
- Settings changes are recorded in audit history.

### 6.2 Customers and suppliers

The system supports separate customer and supplier records. A party may exist in both lists without automatic merging.

Required fields: code, name, contact person, phone, email, address, tax number, notes and active state. Customers additionally support credit limit and default payment terms. Suppliers support preferred payment terms.

Rules:

- Code is unique within its party type; name is required.
- Inactive parties cannot be selected for new documents, but stay visible on old documents.
- A party referenced by confirmed documents cannot be deleted.
- Search matches code, name, contact, phone and email.

### 6.3 Categories and units

- Categories have code, name, description and active state.
- Units have code, name, symbol and active state.
- Categories/units referenced by catalogue items cannot be deleted; they may be deactivated.

### 6.4 Catalogue items

Each item must support:

| Field | Requirement |
|---|---|
| SKU/code | required and unique |
| Barcode | optional and unique when supplied |
| Name and description | name required; description optional |
| Item type | Product, Service, Non-stock product |
| Capability flags | can sell, can purchase, track inventory |
| Category and unit | unit required; category optional |
| Default sale/purchase price | non-negative |
| Minimum stock | non-negative; applicable when inventory tracked |
| Images/attachments | optional |
| Active | deactivation preserves history |

Rules:

- `track inventory` can be true only for a product/non-stock definition chosen to be stocked; service items cannot track inventory.
- At least one of `can sell` or `can purchase` must be true.
- A stock-tracked item has a current balance derived from its movements.
- Deactivating an item blocks it from new lines but never alters existing lines.

### 6.5 Prices and suppliers

- One item may have many suppliers; one can be marked preferred.
- Supplier-specific and customer-specific prices support minimum quantity, effective-from and optional effective-to dates.
- The price resolver uses the valid best matching customer/supplier price, then the default item price, then manual entry.
- A manual override requires a reason if the user lacks price-override authority. **Proposed default:** always ask for a reason when override differs by more than 10%.

## 7. Quote requirements

### 7.1 Quote fields

A quote contains: quote number, date, customer, customer contact, job/reference, service address, expiry date, payment terms, currency, notes, status, lines, subtotal, discount, tax and total.

### 7.2 Quote states

```text
Draft → Submitted → Sent → Accepted ──→ Converted
                   ├→ Rejected
                   ├→ Expired
                   └→ Cancelled
```

State rules:

- **Draft:** editable and deletable by authorised users; no external commitment.
- **Submitted:** internally ready for review; editable only by returning it to Draft.
- **Sent:** issued to customer; no line edits without revision/return-to-draft.
- **Accepted:** customer agrees; eligible for conversion.
- **Rejected/Expired/Cancelled:** no conversion; history retained.
- **Converted:** accepted quote with at least one linked sales invoice. It can still show remaining quantity/value only if partial conversion is enabled.

**Proposed default:** one accepted quote converts to one sales invoice; partial conversion is deferred until it is demonstrably needed.

### 7.3 Quote operations

1. Create from scratch, duplicate an earlier quote, or create from a customer/item selection.
2. Add catalogue or manual lines; select default price and tax, then permit authorised override.
3. Preview/print/download PDF before sending.
4. Mark sent, accepted or rejected with date, user and optional note.
5. Automatically mark an open sent quote as expired when the expiry date passes; it can be renewed by creating a revision.
6. Convert accepted quote to a draft sales invoice; copy snapshots and preserve a source link.

Acceptance criteria:

- Converting a quote never changes the quote total or line history.
- Two users cannot create duplicate sales invoices from the same quote through double-click/retry.
- Quote lines never affect stock or receivable balances.

## 8. Sales-invoice requirements

### 8.1 Sales invoice fields

Required: invoice number, date, customer, due date/payment terms, currency, status and at least one valid line. Optional: quote reference, contact/job, service address, customer PO number, notes, attachments, discount and tax.

Each line contains: sequence, catalogue item reference (optional), snapshot code/description/unit, quantity, unit price, discount type/value/amount, tax rate/amount, total, cost snapshot and margin snapshot when available.

### 8.2 Sales invoice state flow

```text
Draft → Confirmed → Part paid / Paid
                 └→ Cancelled (reversal)
```

Payment status is calculated: Unpaid, Part paid, Paid, or Overdue. It is not manually edited.

### 8.3 Sales invoice rules

1. Only Draft invoices can be edited or deleted.
2. Confirmation requires a customer, date, at least one positive-quantity line and valid totals.
3. Confirming a stock-tracked product line creates one stock-out ledger entry per line, in the same database transaction.
4. Service, non-stock and manual lines never create stock movement.
5. **Proposed default:** confirmation is blocked when available stock would become negative. An authorised override can be added only after owner approval.
6. Confirmed invoices may be printed and emailed/exported; printing does not change status.
7. Cancellation requires a reason. It reverses relevant stock movements and updates invoice balance/status; it does not delete lines.
8. A credit note/refund workflow is excluded from Release 1. If a confirmed invoice is already paid, cancellation must be blocked pending a controlled credit/refund policy.

Acceptance criteria:

- The same confirm request cannot post twice.
- Catalogue price/name changes after confirmation do not change the invoice.
- Cancelling a confirmed invoice exactly reverses the inventory impact once.

## 9. Purchase-bill requirements

### 9.1 Purchase bill fields

Required: purchase-bill number, supplier, supplier reference number (when available), date, due date/payment terms, currency, status and one or more valid lines. Optional: notes, attachments, discount and tax.

Lines contain the same core snapshots as sales lines, including actual unit cost.

### 9.2 Purchase bill flow

```text
Draft → Confirmed → Part paid / Paid
                 └→ Cancelled (reversal)
```

### 9.3 Purchase rules

1. Only Draft purchase bills are editable/deletable.
2. Confirming a stock-tracked product line creates stock-in movements.
3. Confirmed purchase costs update the selected costing policy. **Proposed default:** weighted-average cost.
4. Service/non-stock/manual lines do not create stock movement.
5. Cancellation requires a reason and creates reversing stock movements; cancellation is blocked if reversing stock would violate the stock policy.
6. Payments made to suppliers are recorded and allocated in the same payment module.

### 9.4 Purchase order and receipt decision

Release 1 assumes that purchase document confirmation means goods were received. If the business places orders before receiving goods, a later release requires three objects: Purchase Order, Goods Receipt and Purchase Bill. This must be confirmed before work starts.

## 10. Inventory requirements

### 10.1 Stock movement ledger

Permitted movement types:

- opening balance;
- purchase receipt/in;
- sale/out;
- adjustment in;
- adjustment out/shrinkage;
- cancellation/reversal in;
- cancellation/reversal out.

Every movement stores item, quantity delta, movement date, source type/number/line, user, timestamp and note/reason when manual.

### 10.2 Inventory screens

1. **Stock overview:** item, SKU, on hand, available, minimum level, reorder quantity/value, preferred supplier and last movement.
2. **Movement ledger:** date range, item, movement type, source document, user and quantity filters.
3. **Adjustment:** item, increase/decrease, quantity, date and mandatory reason.
4. **Reorder list:** stock below minimum, recommended reorder quantity and preferred supplier.

### 10.3 Inventory rules

- Current stock is derived from ledger movements or maintained atomically from them; users never type a stock balance directly.
- The system must not allow an adjustment quantity of zero.
- Adjustment requires stock-adjustment permission and a reason.
- Cost adjustments are out of scope unless the owner approves a stock valuation procedure.
- Historical movements cannot be edited; correction is a new compensating movement.

## 11. Payment requirements

### 11.1 Payment structure

A payment has payment number, direction (customer receipt/supplier payment), date, payer/payee, method, amount, reference number, notes, status and one or more allocations.

Supported methods: cash, bank transfer, card, cheque and other.

### 11.2 Payment rules

1. A payment amount must be greater than zero.
2. It may be allocated to one or more confirmed documents in the same direction and currency.
3. Allocation total cannot exceed payment total.
4. **Proposed default:** allocation cannot exceed a document’s outstanding balance; unapplied credit is not allowed in Release 1.
5. A posted payment cannot be edited or deleted. Void it with a mandatory reason and permission.
6. Voiding reverses allocations and recalculates affected document balances.
7. A document’s payment status is recalculated from non-void allocations.

Acceptance criteria:

- Two simultaneous payment posts cannot cause a document to be overpaid.
- Voiding restores the exact previous outstanding balance.

## 12. Calculation policy

All calculations are rounded consistently by the server.

For each line:

```text
line subtotal   = quantity × unit price
line discount   = percent/fixed discount, capped at line subtotal
tax base        = line subtotal − line discount
line tax        = tax base × tax rate
line total      = tax base + line tax
```

For a document:

```text
subtotal        = sum(line subtotal)
discount amount = document discount, capped at eligible subtotal
tax amount      = sum(line tax) after the chosen discount allocation policy
grand total     = subtotal − discount amount + tax amount + shipping/charges
balance         = grand total − posted payment allocations
```

**Owner decision required:** whether document-level discount is applied before or after line tax, and whether tax is inclusive or exclusive by default. Until confirmed, use **tax exclusive, discount before tax**.

## 13. Screen requirements

### 13.1 Navigation

Main navigation: Dashboard, Sales, Quotes, Customers, Purchases, Suppliers, Catalogue, Inventory, Payments, Reports, Administration. Menu visibility follows permission.

### 13.2 Common list-page behaviour

Every module list provides:

- prominent Create button where authorised;
- free-text search plus meaningful filters;
- status badges and date/amount columns;
- saved/current filter state while navigating back from detail;
- pagination and sort;
- no destructive action in a single click.

### 13.3 Detail/editor behaviour

- Unsaved-change warning before leaving a draft editor.
- Inline calculation preview and field-level validation.
- Item picker filters by allowed capability (sell/purchase) and active status.
- Status timeline/audit panel on confirmed/cancelled documents.
- Action buttons appear only when the transition is valid and permitted.
- Print/PDF is visually consistent and contains company details, party details, document number/date, lines, totals, terms and status watermark where needed.

### 13.4 Dashboard

Show only information permitted for the user role:

- sales this period and outstanding receivables;
- quote pipeline and expiring quotes;
- purchases/payables;
- low-stock/reorder items;
- recent documents/payments and action shortcuts.

## 14. Reports and exports

| Report | Minimum filters | Source rule |
|---|---|---|
| Sales analysis | date, customer, item/category, salesperson | confirmed sales invoices only |
| Quote pipeline | date, status, customer, salesperson | quote status/value history |
| Customer statement | customer, as-of date | confirmed sales + non-void receipts |
| Receivables ageing | as-of date, customer | outstanding confirmed sales invoices |
| Purchase analysis | date, supplier, item/category | confirmed purchase bills only |
| Supplier payable | supplier, as-of date | outstanding confirmed purchase bills |
| Stock overview/reorder | item/category/supplier | current stock and minimum level |
| Stock movement ledger | date, item, type, source | append-only movement records |
| Payment register | date, direction, method, party | posted/voided clearly distinguished |
| Audit log | date, user, action, entity | audit events only |

Exports must respect the same permissions and filters visible on screen. **Proposed default:** CSV and print/PDF export for Release 1.

## 15. Audit, retention and attachments

The audit log must record login-related events, create/update/delete of drafts, confirmations, cancellations, price overrides, stock adjustments, payment posts/voids, user/role changes and settings changes.

Each event includes actor, timestamp, action, target type/ID, outcome and a safe before/after summary. Sensitive values such as passwords must never appear in audit records.

Attachments require filename, type, size, creator and time. **Proposed default:** 10 MB maximum per file; allow PDF, image and common office formats; reject executable files.

## 16. Migration requirements

1. Preserve `legacy_source`, `legacy_id` and legacy document number on each migrated record.
2. Import into a staging environment first; never test migration on production data.
3. Create a written mapping workbook before transformation.
4. Reconcile source vs target counts by entity and source document totals by document.
5. Create exception lists for incomplete/invalid data; every exception is corrected, archived or explicitly excluded with owner approval.
6. Import attachments only after determining their business value and storage size.
7. Complete at least two migration rehearsals before final cutover.
8. Keep Access databases read-only after cutover and retain them under the agreed backup/retention policy.

## 17. Non-functional requirements

| Area | Requirement |
|---|---|
| Performance | Typical list/search response under 2 seconds on the local network for Release 1 data volume. |
| Reliability | Posting workflows are transactional; a failed request leaves no partial stock/payment result. |
| Backup | Automated database backup daily; verify a restore quarterly at minimum. |
| Availability | Graceful health checks and clear outage/error message. |
| Browser support | Current Chrome, Edge and Firefox desktop versions; responsive tablet layout where practical. |
| Accessibility | Keyboard-operable forms, labelled controls, readable contrast and clear validation messages. |
| Language | English interface first; architecture must allow Khmer localisation later. |
| Deployment | Docker Compose on the NAS, application/API behind Nginx, database not publicly exposed. |

## 18. Testable acceptance checklist

Before pilot launch, demonstrate:

- an authorised salesperson creates, sends, accepts and converts a quote;
- an unauthorised user cannot accept a quote, change settings or view restricted reports;
- a sales invoice with product/service/manual lines changes stock only for the product;
- a duplicate confirm request cannot create duplicate stock movements;
- cancelled sale/purchase documents create exactly one compensating stock effect;
- a payment can be allocated, and a void restores the balance correctly;
- inactive items/parties cannot be added to new documents but remain visible in history;
- invoice/quote PDFs show correct line totals, tax, discount, company/customer information and status;
- filtered report totals agree with underlying confirmed documents;
- migrated counts and selected legacy totals reconcile to signed-off migration reports;
- a backup restore starts the application successfully in a non-production environment.

## 19. Owner decision register

The following decisions block implementation of the affected areas. The proposed default lets review proceed but is not final policy.

| ID | Decision | Proposed default | Needed before |
|---|---|---|---|
| D-01 | Launch legal entities/currency | one company, one base currency | foundation |
| D-02 | Tax policy | tax exclusive; discount before tax | quote/invoice work |
| D-03 | Negative stock policy | block confirmation below zero | sales/inventory work |
| D-04 | Inventory costing | weighted average | purchase/margin work |
| D-05 | Purchase flow | bill confirmation means received | purchase work |
| D-06 | Quote conversion | one quote → one invoice | quote work |
| D-07 | Payment overpayment/credit | do not permit unapplied credit | payment work |
| D-08 | Required approvers/limits | separate permissions by action | roles/workflow work |
| D-09 | Number formats | confirm sale, quote, purchase and payment prefixes | settings work |
| D-10 | Mandatory reports/layouts | owner to provide samples/priority | reporting work |
| D-11 | Legacy history | migrate approved history plus retain archive | migration work |
| D-12 | Language | English first, Khmer-ready | UI work |

## 20. Sign-off

The business owner should review this specification, resolve the decision register, and then approve it as the baseline for design and development. Any later change must be recorded with its reason, affected workflow, data impact, cost/risk and approval.

| Role | Name | Signature/date |
|---|---|---|
| Business owner |  |  |
| Finance approver |  |  |
| Operations/inventory approver |  |  |
| Technical lead |  |  |

