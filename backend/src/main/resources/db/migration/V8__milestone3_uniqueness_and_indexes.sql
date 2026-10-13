-- V8: Add Unique Constraints for Invoice and Bill per Resident per Billing Cycle
ALTER TABLE invoices DROP CONSTRAINT IF EXISTS uk_invoices_cycle_resident;
ALTER TABLE invoices ADD CONSTRAINT uk_invoices_cycle_resident UNIQUE (billing_cycle_id, resident_id);

ALTER TABLE bills DROP CONSTRAINT IF EXISTS uk_bills_cycle_resident;
ALTER TABLE bills ADD CONSTRAINT uk_bills_cycle_resident UNIQUE (billing_cycle_id, resident_id);
