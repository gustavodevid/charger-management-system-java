package com.dac.chargemanager.api.soap.model;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.math.BigDecimal;

/**
 * SOAP response model for charge operations.
 */
@XmlRootElement(name = "ChargeResponse")
@XmlAccessorType(XmlAccessType.FIELD)
public class ChargeResponse {

    @XmlElement
    private Long id;

    @XmlElement
    private Long customerId;

    @XmlElement
    private String customerName;

    @XmlElement
    private String customerEmail;

    @XmlElement
    private String externalId;

    @XmlElement
    private BigDecimal value;

    @XmlElement
    private String dueDate;

    @XmlElement
    private String billingType;

    @XmlElement
    private String status;

    @XmlElement
    private String description;

    @XmlElement
    private String pixCode;

    @XmlElement
    private String boletoCode;

    @XmlElement
    private String invoiceUrl;

    @XmlElement
    private String createdAt;

    @XmlElement
    private String updatedAt;

    @XmlElement
    private boolean success;

    @XmlElement
    private String errorMessage;

    public ChargeResponse() {
    }

    public static ChargeResponse success(Long id, Long customerId, String customerName, String customerEmail,
                                         String externalId, BigDecimal value, String dueDate, String billingType,
                                         String status, String pixCode, String boletoCode, String invoiceUrl,
                                         String createdAt, String updatedAt) {
        ChargeResponse response = new ChargeResponse();
        response.setSuccess(true);
        response.setId(id);
        response.setCustomerId(customerId);
        response.setCustomerName(customerName);
        response.setCustomerEmail(customerEmail);
        response.setExternalId(externalId);
        response.setValue(value);
        response.setDueDate(dueDate);
        response.setBillingType(billingType);
        response.setStatus(status);
        response.setPixCode(pixCode);
        response.setBoletoCode(boletoCode);
        response.setInvoiceUrl(invoiceUrl);
        response.setCreatedAt(createdAt);
        response.setUpdatedAt(updatedAt);
        return response;
    }

    public static ChargeResponse error(String errorMessage) {
        ChargeResponse response = new ChargeResponse();
        response.setSuccess(false);
        response.setErrorMessage(errorMessage);
        return response;
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

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
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

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
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

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
