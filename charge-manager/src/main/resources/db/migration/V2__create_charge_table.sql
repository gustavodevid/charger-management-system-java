-- V2__create_charge_table.sql
-- Creates the charge table for storing charge/payment information

CREATE TABLE IF NOT EXISTS charge (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL REFERENCES customer(id),
    external_id VARCHAR(100),
    value DECIMAL(15, 2) NOT NULL,
    due_date DATE NOT NULL,
    billing_type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    description VARCHAR(500),
    pix_code VARCHAR(500),
    boleto_code VARCHAR(100),
    invoice_url VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT chk_billing_type CHECK (billing_type IN ('PIX', 'BOLETO', 'CREDIT_CARD')),
    CONSTRAINT chk_status CHECK (status IN ('PENDING', 'REGISTERED', 'CANCELED', 'PAID'))
);

-- Create indexes for frequently queried columns
CREATE INDEX idx_charge_customer_id ON charge(customer_id);
CREATE INDEX idx_charge_external_id ON charge(external_id);
CREATE INDEX idx_charge_status ON charge(status);
CREATE INDEX idx_charge_due_date ON charge(due_date);

-- Add comments for documentation
COMMENT ON TABLE charge IS 'Table storing charge/payment information';
COMMENT ON COLUMN charge.id IS 'Primary key - auto-generated';
COMMENT ON COLUMN charge.customer_id IS 'Foreign key to customer table';
COMMENT ON COLUMN charge.external_id IS 'External ID from payment gateway (ASAAS)';
COMMENT ON COLUMN charge.value IS 'Charge value in BRL';
COMMENT ON COLUMN charge.due_date IS 'Payment due date';
COMMENT ON COLUMN charge.billing_type IS 'Payment type: PIX, BOLETO, or CREDIT_CARD';
COMMENT ON COLUMN charge.status IS 'Charge status: PENDING, REGISTERED, CANCELED, or PAID';
COMMENT ON COLUMN charge.description IS 'Optional charge description';
COMMENT ON COLUMN charge.pix_code IS 'PIX code for PIX payments';
COMMENT ON COLUMN charge.boleto_code IS 'Boleto barcode for BOLETO payments';
COMMENT ON COLUMN charge.invoice_url IS 'Invoice URL for CREDIT_CARD payments';
COMMENT ON COLUMN charge.created_at IS 'Record creation timestamp';
COMMENT ON COLUMN charge.updated_at IS 'Record last update timestamp';
