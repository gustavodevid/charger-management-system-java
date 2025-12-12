package com.dac.chargeproxy.soap.model;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.math.BigDecimal;

/**
 * SOAP request model for creating a charge.
 */
@XmlRootElement(name = "ChargeRequest")
@XmlAccessorType(XmlAccessType.FIELD)
public class ChargeRequest {

    @XmlElement(required = true)
    private String customerId;

    @XmlElement(required = true)
    private BigDecimal value;

    @XmlElement(required = true)
    private String dueDate;

    @XmlElement(required = true)
    private String billingType; // PIX, BOLETO, CREDIT_CARD

    @XmlElement
    private String description;

    public ChargeRequest() {
    }

    public ChargeRequest(String customerId, BigDecimal value, String dueDate, String billingType, String description) {
        this.customerId = customerId;
        this.value = value;
        this.dueDate = dueDate;
        this.billingType = billingType;
        this.description = description;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

