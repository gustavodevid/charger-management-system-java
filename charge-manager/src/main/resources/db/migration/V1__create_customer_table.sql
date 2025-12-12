-- V1__create_customer_table.sql
-- Creates the customer table for storing customer information

CREATE TABLE IF NOT EXISTS customer (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    cpf_cnpj VARCHAR(14) NOT NULL UNIQUE,
    phone VARCHAR(20),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- Create indexes for frequently queried columns
CREATE INDEX idx_customer_email ON customer(email);
CREATE INDEX idx_customer_cpf_cnpj ON customer(cpf_cnpj);

-- Add comments for documentation
COMMENT ON TABLE customer IS 'Table storing customer information';
COMMENT ON COLUMN customer.id IS 'Primary key - auto-generated';
COMMENT ON COLUMN customer.name IS 'Customer full name';
COMMENT ON COLUMN customer.email IS 'Customer email address - unique';
COMMENT ON COLUMN customer.cpf_cnpj IS 'Customer CPF (11 digits) or CNPJ (14 digits) - unique';
COMMENT ON COLUMN customer.phone IS 'Customer phone number';
COMMENT ON COLUMN customer.created_at IS 'Record creation timestamp';
COMMENT ON COLUMN customer.updated_at IS 'Record last update timestamp';

