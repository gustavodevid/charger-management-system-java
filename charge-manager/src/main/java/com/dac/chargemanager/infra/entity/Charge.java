package com.dac.chargemanager.infra.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity representing a charge/payment.
 */
public class Charge {

    private Long id;
    private Long customerId;
    private String externalId;
    private BigDecimal value;
    private LocalDate dueDate;
    private String billingType;
    private ChargeStatus status;
    private String description;
    private String pixCode;
    private String boletoCode;
    private String invoiceUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Charge() {
        this.status = ChargeStatus.PENDING;
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public String getBillingType() {
        return billingType;
    }

    public void setBillingType(String billingType) {
        this.billingType = billingType;
    }

    public ChargeStatus getStatus() {
        return status;
    }

    public void setStatus(ChargeStatus status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPixCode() {
        return pixCode;
    }

    public void setPixCode(String pixCode) {
        this.pixCode = pixCode;
    }

    public String getBoletoCode() {
        return boletoCode;
    }

    public void setBoletoCode(String boletoCode) {
        this.boletoCode = boletoCode;
    }

    public String getInvoiceUrl() {
        return invoiceUrl;
    }

    public void setInvoiceUrl(String invoiceUrl) {
        this.invoiceUrl = invoiceUrl;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "Charge{" +
                "id=" + id +
                ", customerId=" + customerId +
                ", externalId='" + externalId + '\'' +
                ", value=" + value +
                ", dueDate=" + dueDate +
                ", billingType='" + billingType + '\'' +
                ", status=" + status +
                '}';
    }
}
