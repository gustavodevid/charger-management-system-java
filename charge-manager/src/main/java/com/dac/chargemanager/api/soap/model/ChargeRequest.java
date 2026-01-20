package com.dac.chargemanager.api.soap.model;

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
    private Long customerId;

    @XmlElement(required = true)
    private BigDecimal value;

    @XmlElement(required = true)
    private String dueDate; // Format: yyyy-MM-dd

    @XmlElement(required = true)
    private String billingType; // PIX, BOLETO, CREDIT_CARD

    @XmlElement
    private String description;

    public ChargeRequest() {
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
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
