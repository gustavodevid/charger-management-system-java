package com.dac.chargeproxy.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;

/**
 * DTO for ASAAS Payment response.
 * 
 * Based on ASAAS API documentation.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AsaasPaymentResponse {

    /**
     * Payment ID in ASAAS.
     */
    private String id;

    /**
     * Customer ID in ASAAS.
     */
    private String customer;

    /**
     * Payment status.
     * Possible values: PENDING, RECEIVED, CONFIRMED, OVERDUE, REFUNDED, 
     * RECEIVED_IN_CASH, REFUND_REQUESTED, REFUND_IN_PROGRESS, CHARGEBACK_REQUESTED,
     * CHARGEBACK_DISPUTE, AWAITING_CHARGEBACK_REVERSAL, DUNNING_REQUESTED,
     * DUNNING_RECEIVED, AWAITING_RISK_ANALYSIS
     */
    private String status;

    /**
     * Billing type: BOLETO, CREDIT_CARD, PIX, UNDEFINED.
     */
    private String billingType;

    /**
     * Payment value.
     */
    private BigDecimal value;

    /**
     * Net value after fees.
     */
    private BigDecimal netValue;

    /**
     * Original value (before discounts).
     */
    private BigDecimal originalValue;

    /**
     * Due date in format yyyy-MM-dd.
     */
    private String dueDate;

    /**
     * Payment date (when paid).
     */
    private String paymentDate;

    /**
     * Client payment date (when the client made the payment).
     */
    private String clientPaymentDate;

    /**
     * Date when payment was created.
     */
    private String dateCreated;

    /**
     * Payment description.
     */
    private String description;

    /**
     * External reference (our internal ID).
     */
    private String externalReference;

    /**
     * Invoice URL (for viewing payment details).
     */
    private String invoiceUrl;

    /**
     * Bank slip URL (for BOLETO payments).
     */
    private String bankSlipUrl;

    /**
     * Transaction receipt URL.
     */
    private String transactionReceiptUrl;

    /**
     * Bank slip line (for BOLETO payments).
     */
    private String nossoNumero;

    /**
     * Indicates if the payment was deleted.
     */
    private Boolean deleted;

    /**
     * Indicates if the payment can be anticipated.
     */
    private Boolean anticipated;

    /**
     * Indicates if the payment was anticipable.
     */
    private Boolean anticipable;

    /**
     * Credit date (when the value will be available in the account).
     */
    private String creditDate;

    /**
     * Estimated credit date.
     */
    private String estimatedCreditDate;

    /**
     * Installment ID (if part of an installment).
     */
    private String installment;

    /**
     * PIX transaction ID.
     */
    private String pixTransaction;

    /**
     * PIX QR Code ID.
     */
    private String pixQrCodeId;

    // Getters and Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCustomer() {
        return customer;
    }

    public void setCustomer(String customer) {
        this.customer = customer;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public BigDecimal getNetValue() {
        return netValue;
    }

    public void setNetValue(BigDecimal netValue) {
        this.netValue = netValue;
    }

    public BigDecimal getOriginalValue() {
        return originalValue;
    }

    public void setOriginalValue(BigDecimal originalValue) {
        this.originalValue = originalValue;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public String getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(String paymentDate) {
        this.paymentDate = paymentDate;
    }

    public String getClientPaymentDate() {
        return clientPaymentDate;
    }

    public void setClientPaymentDate(String clientPaymentDate) {
        this.clientPaymentDate = clientPaymentDate;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
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

    public String getInvoiceUrl() {
        return invoiceUrl;
    }

    public void setInvoiceUrl(String invoiceUrl) {
        this.invoiceUrl = invoiceUrl;
    }

    public String getBankSlipUrl() {
        return bankSlipUrl;
    }

    public void setBankSlipUrl(String bankSlipUrl) {
        this.bankSlipUrl = bankSlipUrl;
    }

    public String getTransactionReceiptUrl() {
        return transactionReceiptUrl;
    }

    public void setTransactionReceiptUrl(String transactionReceiptUrl) {
        this.transactionReceiptUrl = transactionReceiptUrl;
    }

    public String getNossoNumero() {
        return nossoNumero;
    }

    public void setNossoNumero(String nossoNumero) {
        this.nossoNumero = nossoNumero;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    public Boolean getAnticipated() {
        return anticipated;
    }

    public void setAnticipated(Boolean anticipated) {
        this.anticipated = anticipated;
    }

    public Boolean getAnticipable() {
        return anticipable;
    }

    public void setAnticipable(Boolean anticipable) {
        this.anticipable = anticipable;
    }

    public String getCreditDate() {
        return creditDate;
    }

    public void setCreditDate(String creditDate) {
        this.creditDate = creditDate;
    }

    public String getEstimatedCreditDate() {
        return estimatedCreditDate;
    }

    public void setEstimatedCreditDate(String estimatedCreditDate) {
        this.estimatedCreditDate = estimatedCreditDate;
    }

    public String getInstallment() {
        return installment;
    }

    public void setInstallment(String installment) {
        this.installment = installment;
    }

    public String getPixTransaction() {
        return pixTransaction;
    }

    public void setPixTransaction(String pixTransaction) {
        this.pixTransaction = pixTransaction;
    }

    public String getPixQrCodeId() {
        return pixQrCodeId;
    }

    public void setPixQrCodeId(String pixQrCodeId) {
        this.pixQrCodeId = pixQrCodeId;
    }

    @Override
    public String toString() {
        return "AsaasPaymentResponse{" +
                "id='" + id + '\'' +
                ", customer='" + customer + '\'' +
                ", status='" + status + '\'' +
                ", billingType='" + billingType + '\'' +
                ", value=" + value +
                ", dueDate='" + dueDate + '\'' +
                '}';
    }
}
