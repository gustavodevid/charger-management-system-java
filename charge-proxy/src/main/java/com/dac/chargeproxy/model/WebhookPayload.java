package com.dac.chargeproxy.model;

import com.dac.chargeproxy.business.ProxyBusinessRules;

import java.math.BigDecimal;

/**
 * Model class for webhook payload from ASAAS.
 * 
 * Based on ASAAS webhook documentation:
 * https://docs.asaas.com/docs/webhooks
 */
public class WebhookPayload {

    /**
     * The event type from ASAAS.
     */
    private String event;

    /**
     * The payment data.
     */
    private PaymentData payment;

    public WebhookPayload() {
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public PaymentData getPayment() {
        return payment;
    }

    public void setPayment(PaymentData payment) {
        this.payment = payment;
    }

    /**
     * Inner class representing payment data from ASAAS webhook.
     */
    public static class PaymentData {

        private String id;
        private String customer;
        private String dateCreated;
        private String dueDate;
        private BigDecimal value;
        private BigDecimal netValue;
        private String billingType;
        private String status;
        private String description;
        private String externalReference;
        private String invoiceUrl;
        private String bankSlipUrl;
        private String transactionReceiptUrl;
        private String nossoNumero;

        // PIX specific fields
        private String pixTransaction;
        private String pixQrCodeImage;
        private String pixQrCodePayload;

        public PaymentData() {
        }

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

        public String getDateCreated() {
            return dateCreated;
        }

        public void setDateCreated(String dateCreated) {
            this.dateCreated = dateCreated;
        }

        public String getDueDate() {
            return dueDate;
        }

        public void setDueDate(String dueDate) {
            this.dueDate = dueDate;
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

        public String getBillingType() {
            return billingType;
        }

        public void setBillingType(String billingType) {
            this.billingType = billingType;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
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

        public String getPixTransaction() {
            return pixTransaction;
        }

        public void setPixTransaction(String pixTransaction) {
            this.pixTransaction = pixTransaction;
        }

        public String getPixQrCodeImage() {
            return pixQrCodeImage;
        }

        public void setPixQrCodeImage(String pixQrCodeImage) {
            this.pixQrCodeImage = pixQrCodeImage;
        }

        public String getPixQrCodePayload() {
            return pixQrCodePayload;
        }

        public void setPixQrCodePayload(String pixQrCodePayload) {
            this.pixQrCodePayload = pixQrCodePayload;
        }

        /**
         * Returns the internal status mapped from the ASAAS status.
         * @return internal system status
         */
        public String getInternalStatus() {
            return ProxyBusinessRules.mapAsaasStatusToInternal(this.status);
        }
    }

    /**
     * Converts ASAAS status to internal status.
     * Delegates to ProxyBusinessRules for consistent mapping across the application.
     * 
     * @param asaasStatus the ASAAS status to map
     * @return the internal system status
     */
    public static String mapAsaasStatus(String asaasStatus) {
        return ProxyBusinessRules.mapAsaasStatusToInternal(asaasStatus);
    }

    /**
     * Gets the internal status from the payment data.
     * Convenience method that maps the ASAAS status to internal status.
     * 
     * @return the internal system status
     */
    public String getInternalPaymentStatus() {
        if (payment != null && payment.getStatus() != null) {
            return ProxyBusinessRules.mapAsaasStatusToInternal(payment.getStatus());
        }
        return "PENDING";
    }

    @Override
    public String toString() {
        return "WebhookPayload{" +
                "event='" + event + '\'' +
                ", payment=" + (payment != null ? "PaymentData{id=" + payment.getId() + 
                        ", asaasStatus=" + payment.getStatus() + 
                        ", internalStatus=" + getInternalPaymentStatus() + "}" : "null") +
                '}';
    }
}
