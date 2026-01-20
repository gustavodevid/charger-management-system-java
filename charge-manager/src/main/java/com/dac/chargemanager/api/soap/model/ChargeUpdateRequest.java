package com.dac.chargemanager.api.soap.model;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.math.BigDecimal;

/**
 * SOAP request model for updating a charge.
 * All fields are optional - only provided fields will be updated.
 */
@XmlRootElement(name = "ChargeUpdateRequest")
@XmlAccessorType(XmlAccessType.FIELD)
public class ChargeUpdateRequest {

    @XmlElement
    private BigDecimal value;

    @XmlElement
    private String dueDate; // Format: yyyy-MM-dd

    @XmlElement
    private String billingType; // PIX, BOLETO, CREDIT_CARD

    @XmlElement
    private String description;

    public ChargeUpdateRequest() {
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

    /**
     * Checks if any field has been provided for update.
     */
    public boolean hasUpdates() {
        return value != null || dueDate != null || billingType != null || description != null;
    }
}
