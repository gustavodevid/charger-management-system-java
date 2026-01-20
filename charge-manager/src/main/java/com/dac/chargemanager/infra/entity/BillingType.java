package com.dac.chargemanager.infra.entity;

/**
 * Enum representing the billing/payment type.
 */
public enum BillingType {
    
    PIX("PIX"),
    BOLETO("Boleto Bancário"),
    CREDIT_CARD("Cartão de Crédito");
    
    private final String description;
    
    BillingType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * Converts a string to BillingType.
     * 
     * @param value the string value
     * @return the corresponding BillingType
     * @throws IllegalArgumentException if value is invalid
     */
    public static BillingType fromString(String value) {
        if (value == null) {
            return null;
        }
        try {
            return BillingType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid billing type: " + value + ". Must be PIX, BOLETO, or CREDIT_CARD");
        }
    }
}
