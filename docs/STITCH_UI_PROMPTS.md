# BMS Stitch UI Brief & Prompts

Use this document to generate BMS UI screens in Google Stitch.

## What to give Stitch

1. Create a new Stitch project named **Business Management System (BMS)**.
2. Start with the project brief below as a text prompt. Do **not** upload the whole architecture document or database schema.
3. Generate one screen at a time using the screen prompts below.
4. When you approve the first 2–3 screens, use Stitch’s design-system/theme controls so all later screens use the same visual language. Export/save its `DESIGN.md` when available.
5. Upload an image only when you have a useful reference: your company logo, an approved colour/brand reference, or a hand-drawn/screenshot layout. Stitch accepts common raster images such as PNG, JPG and WebP; it also supports text, image and code inputs. [Google Stitch overview](https://developers.googleblog.com/en/stitch-a-new-way-to-design-uis/)

The most useful existing BMS reference is `FRONTEND_FLOW.md`. Read it yourself while prompting, but paste only the relevant screen prompt into Stitch.

## Project brief — paste once

```text
Design a desktop-first internal Business Management System (BMS) for a small business that sells physical products and services.

The application helps staff manage customers, suppliers, product/service catalogue items, quotations, sales invoices, purchase bills, stock inventory, payments, and reports.

Primary users are Sales, Purchasing & Inventory, Finance, and Administrators. It is a serious operational tool used daily, not a marketing website.

Use a clear application shell with a collapsible left sidebar, compact top bar, page title, breadcrumb where useful, consistent tables, filters, forms, confirmation dialogs, status badges, and empty/loading/error states. Prioritize scanability, efficient data entry, and low error risk. Use realistic sample business data, USD currency, and English text. Design for desktop screens first, with responsive behaviour that can work on tablets.

The navigation follows this business flow: Dashboard; Sales (Customers, Quotes, Sales Invoices, Customer Payments); Purchasing (Suppliers, Purchase Bills, Supplier Payments); Inventory (Catalogue, Stock Overview, Stock Movements, Stock Adjustments); Reports; Administration (Company Settings, Users & Roles, Document Numbering).

Do not design database settings, developer tools, marketing sections, social feeds, or unnecessary analytics. Important financial actions such as Confirm, Cancel, Void and Stock Adjustment must look deliberate and require confirmation.
```

## Generate screens in this order

1. Sign-in
2. Dashboard
3. Customer list and customer detail
4. Quote list and quote editor
5. Sales invoice editor
6. Customer payment allocation
7. Supplier list and purchase bill editor
8. Inventory stock overview and adjustment dialog
9. Reports
10. Administration: users and roles

Do not ask Stitch to design every screen in one large prompt. Generate a screen, review it, improve it, then generate the next one using the same design system.

## Prompt 1 — Sign-in

```text
Create a desktop sign-in screen for the BMS internal business application.

Page purpose: let authorised employees sign in quickly and securely.

Layout: use a clean split layout. The left side contains a restrained BMS brand area with the product name “Business Management System” and a short line: “Manage sales, purchases, inventory, and payments in one place.” The right side contains a focused sign-in card.

Sign-in card: title “Welcome back”, supporting text, username or email field, password field with show/hide control, Remember me checkbox, primary Sign in button, and a subtle Forgot password link. Show an example inline validation state beneath the password field. Include a small help/contact line at the bottom. No public sign-up button because this is an internal company system.

Make it professional, calm, accessible, and optimized for daily staff use.
```

## Prompt 2 — Dashboard

```text
Create the desktop dashboard for BMS after an administrator signs in.

Use the standard application shell: collapsible left sidebar, compact top bar with global search, notifications, and user profile menu. Highlight Dashboard in the sidebar.

Main content: page title “Dashboard”, a date-range selector, and a clear primary action area with “New Quote”, “New Sales Invoice”, and “New Purchase Bill”.

Show four summary cards: Sales This Month, Outstanding Receivables, Outstanding Payables, and Low Stock Items. Below, show an “Action Required” panel for overdue invoices, quotes expiring soon, and low-stock products. Include a small sales trend chart, a recent documents table with type/status/party/amount/date, and a low-stock table with item, on-hand quantity, minimum quantity, and reorder action.

The screen must feel useful for daily operations: concise numbers, readable status badges, and clear next actions. Avoid decorative charts or marketing-style cards.
```

## Prompt 3 — Quote list

```text
Create a desktop Quote List screen for BMS in the Sales section.

Use the same application shell. The Sales navigation group is expanded and Quotes is active.

Header: breadcrumb “Sales / Quotes”, title “Quotes”, short supporting text, and primary button “New Quote”.

Include a filter toolbar with free-text search, status filter, customer filter, date range, and a clear filters action. Use a dense but readable data table with columns: Quote Number, Customer, Job/Reference, Quote Date, Expiry Date, Total, Status, and Actions. Include realistic rows with Draft, Sent, Accepted, Rejected, Expired and Converted status badges. Show pagination and total result count.

Add an empty-state variant suggestion for when no quotes match filters. Provide row actions for View, Edit Draft, Duplicate, and Convert Accepted Quote. Ensure destructive actions are not prominent.
```

## Prompt 4 — Quote editor

```text
Create a desktop Quote Editor screen for BMS. This is a high-efficiency business form used by Sales staff.

Use the standard application shell. Header shows breadcrumb “Sales / Quotes / New Quote”, title “New Quote”, Draft status badge, and actions: Save Draft, Preview, and Submit Quote.

Top form section: Quote Date, Expiry Date, Customer searchable selector, Contact, Job/Reference, Service Address, Payment Terms, and Notes. Place related fields in a clear two-column layout.

Main section: editable quote-lines table with columns Item, Description, Unit, Quantity, Unit Price, Discount, Tax, Line Total, and remove action. Include an “Add Item” button and “Add Manual Line” secondary action. Show an item-picker interaction hint.

Right-side or bottom summary: Subtotal, Discount, Tax, Grand Total. Include a clear note that stock is not affected by a quote. Add an unsaved-changes indicator and concise inline validation states. This screen must make entering many line items fast and safe.
```

## Prompt 5 — Sales invoice editor

```text
Create a desktop Sales Invoice Editor for BMS. Use the same application shell and visual system as the quote editor.

Header: breadcrumb “Sales / Sales Invoices / New Sales Invoice”, title “New Sales Invoice”, Draft status badge, actions Save Draft, Preview, and a deliberate primary action “Confirm Invoice”.

Top fields: Invoice Date, Due Date, Customer, optional Source Quote, Customer PO Number, Contact/Job, Service Address, Payment Terms, Notes, and Attachments.

Line table: Item, Description, Unit, Quantity, Available Stock, Unit Price, Discount, Tax, Line Total, remove action. Use clear visual feedback when a stocked item has insufficient available quantity. Permit product, service, and manual lines.

Summary panel: Subtotal, Discount, Tax, Grand Total, Paid Amount, Balance. Include an explicit information panel: “Confirming this invoice will reduce stock for stock-tracked product lines.” The confirmation action must look important but not dangerous.
```

## Prompt 6 — Inventory stock overview

```text
Create a desktop Inventory Stock Overview screen for BMS.

Use the same application shell. Inventory is expanded and Stock Overview is active.

Header: breadcrumb “Inventory / Stock Overview”, title “Stock Overview”, short description, actions “Stock Adjustment” and “Export”.

Use a filter/search bar with search, category, supplier, stock status, and active/inactive item filters. Main table columns: SKU, Item Name, Category, On Hand, Available, Minimum Stock, Reorder Quantity, Preferred Supplier, Last Movement, Status, and Actions. Show clear low-stock highlighting without making the whole page alarming.

Include a right-side or lower “Reorder Suggestions” card and a compact recent stock-movement list. Show an empty state and explain that quantities change only through purchases, sales, and authorised adjustments.
```

## Prompt 7 — Payment allocation

```text
Create a desktop Customer Payment Allocation screen for BMS.

Use the application shell under Sales / Customer Payments. Header title “Receive Customer Payment” with Cancel and Post Payment actions.

Top form: Customer selector, Payment Date, Payment Method, Reference Number, Amount Received, and Notes.

Below, show a table of the selected customer’s confirmed open invoices: Invoice Number, Invoice Date, Due Date, Original Amount, Outstanding Balance, and Allocate Amount input. Include a clear allocation summary that shows Received Amount, Allocated Amount, and Unallocated Amount. Display an alert if allocation exceeds the received amount or an invoice balance.

Posting a payment is a controlled financial action, so show a small confirmation summary before the Post Payment button. Design for accuracy and fast keyboard-based entry.
```

## Prompt 8 — Users and roles

```text
Create a desktop Administration screen for BMS titled “Users & Roles”.

Use the standard application shell with Administration active. Header contains title, description, search, role/status filters, and an “Invite User” button.

Main table columns: Name, Username/Email, Roles, Status, Last Sign-in, and Actions. Include status badges for Active and Inactive.

Show an invite/edit-user side panel or dialog with name, username/email, selected roles, active switch, and a permission summary. Present four business roles: Administrator, Sales, Purchasing & Inventory, and Finance. Include a concise warning that access changes take effect immediately. Keep the experience simple; avoid overwhelming permission matrices on the main screen.
```

## Refinement prompts

After Stitch generates a screen, use short specific improvements such as:

```text
Keep the current BMS design system. Make the data table denser for daily desktop use, while preserving readable row height and clear status badges.
```

```text
Keep the current layout. Make the Confirm Invoice action more deliberate by adding a confirmation summary panel and reducing visual emphasis on destructive Cancel actions.
```

```text
Apply the same sidebar, spacing, table, form, button, status-badge, and empty-state patterns from the approved Dashboard and Quote List screens.
```

## What to upload later

After you approve a visual direction, upload only these assets:

- company logo in PNG/SVG format;
- approved screenshots or hand-drawn wireframes in PNG/JPG/WebP;
- Stitch-exported `DESIGN.md` so future screens remain consistent.

Do not upload real customer lists, invoices, passwords, database backups, `.env` files, or the Access databases to a design tool.

