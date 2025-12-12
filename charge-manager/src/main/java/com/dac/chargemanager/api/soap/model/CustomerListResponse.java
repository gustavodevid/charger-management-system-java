package com.dac.chargemanager.api.soap.model;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.util.ArrayList;
import java.util.List;

/**
 * SOAP response model for listing customers.
 */
@XmlRootElement(name = "CustomerListResponse")
@XmlAccessorType(XmlAccessType.FIELD)
public class CustomerListResponse {

    @XmlElement(name = "customer")
    private List<CustomerResponse> customers;

    @XmlElement
    private boolean success;

    @XmlElement
    private String errorMessage;

    @XmlElement
    private int totalCount;

    public CustomerListResponse() {
        this.customers = new ArrayList<>();
    }

    public static CustomerListResponse success(List<CustomerResponse> customers) {
        CustomerListResponse response = new CustomerListResponse();
        response.setSuccess(true);
        response.setCustomers(customers);
        response.setTotalCount(customers.size());
        return response;
    }

    public static CustomerListResponse error(String errorMessage) {
        CustomerListResponse response = new CustomerListResponse();
        response.setSuccess(false);
        response.setErrorMessage(errorMessage);
        return response;
    }

    public List<CustomerResponse> getCustomers() {
        return customers;
    }

    public void setCustomers(List<CustomerResponse> customers) {
        this.customers = customers;
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

