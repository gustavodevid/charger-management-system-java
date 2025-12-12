package com.dac.chargemanager.api.soap.model;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * SOAP response model for Customer operations.
 */
@XmlRootElement(name = "CustomerResponse")
@XmlAccessorType(XmlAccessType.FIELD)
public class CustomerResponse {

    @XmlElement
    private Long id;

    @XmlElement
    private String name;

    @XmlElement
    private String email;

    @XmlElement
    private String cpfCnpj;

    @XmlElement
    private String phone;

    @XmlElement
    private String createdAt;

    @XmlElement
    private String updatedAt;

    @XmlElement
    private boolean success;

    @XmlElement
    private String errorMessage;

    public CustomerResponse() {
    }

    public static CustomerResponse success(Long id, String name, String email, String cpfCnpj, 
                                           String phone, String createdAt, String updatedAt) {
        CustomerResponse response = new CustomerResponse();
        response.setSuccess(true);
        response.setId(id);
        response.setName(name);
        response.setEmail(email);
        response.setCpfCnpj(cpfCnpj);
        response.setPhone(phone);
        response.setCreatedAt(createdAt);
        response.setUpdatedAt(updatedAt);
        return response;
    }

    public static CustomerResponse error(String errorMessage) {
        CustomerResponse response = new CustomerResponse();
        response.setSuccess(false);
        response.setErrorMessage(errorMessage);
        return response;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCpfCnpj() {
        return cpfCnpj;
    }

    public void setCpfCnpj(String cpfCnpj) {
        this.cpfCnpj = cpfCnpj;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
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

