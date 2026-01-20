package com.dac.chargemanager.api.soap.model;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * SOAP response model for listing charges.
 */
@XmlRootElement(name = "ChargeListResponse")
@XmlAccessorType(XmlAccessType.FIELD)
public class ChargeListResponse {

    @XmlElement(name = "charge")
    private List<ChargeResponse> charges;

    @XmlElement
    private boolean success;

    @XmlElement
    private String errorMessage;

    @XmlElement
    private int totalCount;

    public ChargeListResponse() {
        this.charges = new ArrayList<>();
    }

    public static ChargeListResponse success(List<ChargeResponse> charges) {
        ChargeListResponse response = new ChargeListResponse();
        response.setSuccess(true);
        response.setCharges(charges);
        response.setTotalCount(charges.size());
        return response;
    }

    public static ChargeListResponse error(String errorMessage) {
        ChargeListResponse response = new ChargeListResponse();
        response.setSuccess(false);
        response.setErrorMessage(errorMessage);
        return response;
    }

    public List<ChargeResponse> getCharges() {
        return charges;
    }

    public void setCharges(List<ChargeResponse> charges) {
        this.charges = charges;
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

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }
}
