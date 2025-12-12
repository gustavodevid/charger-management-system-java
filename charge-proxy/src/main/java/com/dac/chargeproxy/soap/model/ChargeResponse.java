package com.dac.chargeproxy.soap.model;

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
    private String chargeId;

    @XmlElement
    private String status;

    @XmlElement
    private String customerId;

    @XmlElement
    private BigDecimal value;

    @XmlElement
    private String dueDate;

    @XmlElement
    private String billingType;

    @XmlElement
    private String invoiceUrl;

    @XmlElement
    private String pixCode;

    @XmlElement
    private String boletoCode;

    @XmlElement
    private boolean success;

    @XmlElement
    private String errorMessage;

    public ChargeResponse() {
    }

    public static ChargeResponse success(String chargeId, String status, String customerId, 
                                          BigDecimal value, String dueDate, String billingType) {
        ChargeResponse response = new ChargeResponse();
        response.setSuccess(true);
        response.setChargeId(chargeId);
        response.setStatus(status);
        response.setCustomerId(customerId);
        response.setValue(value);
        response.setDueDate(dueDate);
        response.setBillingType(billingType);
        return response;
    }

    public static ChargeResponse error(String errorMessage) {
        ChargeResponse response = new ChargeResponse();
        response.setSuccess(false);
        response.setErrorMessage(errorMessage);
        return response;
    }

    public String getChargeId() {
        return chargeId;
    }

    public void setChargeId(String chargeId) {
        this.chargeId = chargeId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
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

    public String getInvoiceUrl() {
        return invoiceUrl;
    }

    public void setInvoiceUrl(String invoiceUrl) {
        this.invoiceUrl = invoiceUrl;
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

