package com.dac.chargemanager.api.soap.model;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * SOAP request model for Customer operations.
 */
@XmlRootElement(name = "CustomerRequest")
@XmlAccessorType(XmlAccessType.FIELD)
public class CustomerRequest {

    @XmlElement(required = true)
    private String name;

    @XmlElement(required = true)
    private String email;

    @XmlElement(required = true)
    private String cpfCnpj;

    @XmlElement
    private String phone;

    public CustomerRequest() {
    }

    public CustomerRequest(String name, String email, String cpfCnpj, String phone) {
        this.name = name;
        this.email = email;
        this.cpfCnpj = cpfCnpj;
        this.phone = phone;
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
}

