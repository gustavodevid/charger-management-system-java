package com.dac.chargemanager.infra.entity;

/**
 * Enum representing the possible statuses of a charge.
 */
public enum ChargeStatus {
    
    /**
     * Charge created but not yet registered in the payment gateway.
     */
    PENDING("Pendente"),
    
    /**
     * Charge registered in the payment gateway, awaiting payment.
     */
    REGISTERED("Registrada"),
    
    /**
     * Charge was canceled.
     */
    CANCELED("Cancelada"),
    
    /**
     * Charge was paid successfully.
     */
    PAID("Paga");

    private final String description;

    ChargeStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Converts a string to ChargeStatus enum.
     * 
     * @param status the status string
     * @return the ChargeStatus enum value
     * @throws IllegalArgumentException if the status is invalid
     */
    public static ChargeStatus fromString(String status) {
        if (status == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }
        try {
            return ChargeStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid charge status: " + status);
        }
    }
}
