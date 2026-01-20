package com.dac.chargeproxy.client.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;

/**
 * DTO for ASAAS Payment creation request.
 * 
 * Based on ASAAS API documentation:
 * POST /payments
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AsaasPaymentRequest {

    /**
     * Customer ID in ASAAS (required).
     */
    private String customer;

    /**
     * Billing type: BOLETO, CREDIT_CARD, PIX, UNDEFINED (required).
     */
    private String billingType;

    /**
     * Payment value (required).
     */
    private BigDecimal value;

    /**
     * Due date in format yyyy-MM-dd (required).
     */
    private String dueDate;

    /**
     * Payment description (optional).
     */
    private String description;

    /**
     * External reference - our internal ID (optional).
     */
    private String externalReference;

    /**
     * Days after due date for automatic cancellation (optional).
     */
    private Integer daysAfterDueDateToRegistrationCancellation;

    /**
     * Interest percentage per month for late payment (optional).
     */
    private BigDecimal interest;

    /**
     * Fine percentage for late payment (optional).
     */
    private BigDecimal fine;

    /**
     * Postal service - send bank slip via mail (optional).
     */
    private Boolean postalService;

    public AsaasPaymentRequest() {
    }

    // Builder pattern for fluent creation
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final AsaasPaymentRequest request = new AsaasPaymentRequest();

        public Builder customer(String customer) {
            request.customer = customer;
            return this;
        }

        public Builder billingType(String billingType) {
            request.billingType = billingType;
            return this;
        }

        public Builder value(BigDecimal value) {
            request.value = value;
            return this;
        }

        public Builder dueDate(String dueDate) {
            request.dueDate = dueDate;
            return this;
        }

        public Builder description(String description) {
            request.description = description;
            return this;
        }

        public Builder externalReference(String externalReference) {
            request.externalReference = externalReference;
            return this;
        }

        public AsaasPaymentRequest build() {
            return request;
        }
    }

    // Getters and Setters

    public String getCustomer() {
        return customer;
    }

    public void setCustomer(String customer) {
        this.customer = customer;
    }

    public String getBillingType() {
        return billingType;
    }

    public void setBillingType(String billingType) {
        this.billingType = billingType;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getExternalReference() {
        return externalReference;
    }

    public void setExternalReference(String externalReference) {
        this.externalReference = externalReference;
    }

    public Integer getDaysAfterDueDateToRegistrationCancellation() {
        return daysAfterDueDateToRegistrationCancellation;
    }

    public void setDaysAfterDueDateToRegistrationCancellation(Integer daysAfterDueDateToRegistrationCancellation) {
        this.daysAfterDueDateToRegistrationCancellation = daysAfterDueDateToRegistrationCancellation;
    }

    public BigDecimal getInterest() {
        return interest;
    }

    public void setInterest(BigDecimal interest) {
        this.interest = interest;
    }

    public BigDecimal getFine() {
        return fine;
    }

    public void setFine(BigDecimal fine) {
        this.fine = fine;
    }

    public Boolean getPostalService() {
        return postalService;
    }

    public void setPostalService(Boolean postalService) {
        this.postalService = postalService;
    }
}
