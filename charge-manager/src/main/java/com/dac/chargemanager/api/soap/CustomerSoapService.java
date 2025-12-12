package com.dac.chargemanager.api.soap;

import com.dac.chargemanager.api.soap.model.CustomerListResponse;
import com.dac.chargemanager.api.soap.model.CustomerRequest;
import com.dac.chargemanager.api.soap.model.CustomerResponse;
import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebResult;
import jakarta.jws.WebService;

/**
 * SOAP Web Service interface for Customer operations.
 * API Layer - exposes customer management operations via SOAP.
 */
@WebService(name = "CustomerService", targetNamespace = "http://chargemanager.dac.com/soap")
public interface CustomerSoapService {

    /**
     * Creates a new customer.
     *
     * @param request the customer data
     * @return the created customer response
     */
    @WebMethod(operationName = "createCustomer")
    @WebResult(name = "CustomerResponse")
    CustomerResponse createCustomer(@WebParam(name = "CustomerRequest") CustomerRequest request);

    /**
     * Retrieves a customer by ID.
     *
     * @param customerId the customer ID
     * @return the customer response
     */
    @WebMethod(operationName = "getCustomer")
    @WebResult(name = "CustomerResponse")
    CustomerResponse getCustomer(@WebParam(name = "customerId") Long customerId);

    /**
     * Retrieves all customers.
     *
     * @return the list of customers
     */
    @WebMethod(operationName = "getAllCustomers")
    @WebResult(name = "CustomerListResponse")
    CustomerListResponse getAllCustomers();

    /**
     * Updates an existing customer.
     *
     * @param customerId the customer ID
     * @param request    the updated customer data
     * @return the updated customer response
     */
    @WebMethod(operationName = "updateCustomer")
    @WebResult(name = "CustomerResponse")
    CustomerResponse updateCustomer(
            @WebParam(name = "customerId") Long customerId,
            @WebParam(name = "CustomerRequest") CustomerRequest request);

    /**
     * Deletes a customer by ID.
     *
     * @param customerId the customer ID
     * @return the response indicating success or failure
     */
    @WebMethod(operationName = "deleteCustomer")
    @WebResult(name = "CustomerResponse")
    CustomerResponse deleteCustomer(@WebParam(name = "customerId") Long customerId);

    /**
     * Health check for the SOAP service.
     *
     * @return status message
     */
    @WebMethod(operationName = "healthCheck")
    @WebResult(name = "status")
    String healthCheck();
}

