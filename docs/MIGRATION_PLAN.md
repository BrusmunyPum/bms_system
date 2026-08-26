# BMS Legacy Data Migration Plan

Sources: `Gods.accdb` (goods, purchasing and inventory) and `Service.accdb` (services, quotes and invoices).

## Process

1. Freeze copies of both Access files; record hash, owner and extraction date.
2. Export tables to a staging format and profile duplicates, blanks, broken references and attachments.
3. Create a field-by-field mapping workbook and retain `legacy_source`, `legacy_id` and legacy document number.
4. Clean/merge records only with a documented business decision.
5. Run migration in staging, then reconcile counts, document totals and stock opening balances.
6. Produce an exception list; every item is fixed, archived or explicitly excluded.
7. Complete two rehearsal migrations and user acceptance checks.
8. Back up, freeze Access entry, run final migration, reconcile and sign off before go-live.
9. Retain Access files read-only for the agreed retention period.

## Initial mapping

| Legacy source | BMS destination |
|---|---|
| Gods Products / Service LineItemCodes | Items |
| Legacy Customers / Suppliers | Customers / Suppliers |
| Service Quotes / QuoteLineItems | Quotes / Quote lines |
| Gods Orders/Invoices and Service Invoices | Sales invoices/lines after validation |
| Gods PurchaseOrders | Purchase bills/lines where valid |
| Gods Inventory/Shrinkage | Approved opening balances and stock adjustments |
