-- V3: Seed core system roles

INSERT INTO roles (name, description) VALUES
('ADMIN', 'Administrator - full access, users, settings, audit review'),
('SALES', 'Sales - customers, quotes, sales invoices, payment entry, sales reports'),
('PURCHASING', 'Purchasing & inventory - suppliers, catalogue, purchase bills, receipts, stock'),
('FINANCE', 'Finance - invoices, payments, credit/refund/void approval, financial reports');
