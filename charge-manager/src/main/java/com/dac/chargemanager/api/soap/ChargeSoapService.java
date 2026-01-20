package com.dac.chargemanager.api.soap;

import com.dac.chargemanager.api.soap.model.ChargeListResponse;
import com.dac.chargemanager.api.soap.model.ChargeRequest;
import com.dac.chargemanager.api.soap.model.ChargeResponse;
import com.dac.chargemanager.api.soap.model.ChargeUpdateRequest;
import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebResult;
import jakarta.jws.WebService;

/**
 * SOAP Web Service interface for Charge operations.
 */
@WebService(name = "ChargeService", targetNamespace = "http://chargemanager.dac.com/soap")
public interface ChargeSoapService {

    /**
     * Creates a new charge and sends to the payment proxy.
     */
    @WebMethod(operationName = "createCharge")
    @WebResult(name = "ChargeResponse")
    ChargeResponse createCharge(@WebParam(name = "ChargeRequest") ChargeRequest request);

    /**
     * Gets a charge by ID.
     */
    @WebMethod(operationName = "getCharge")
    @WebResult(name = "ChargeResponse")
    ChargeResponse getCharge(@WebParam(name = "chargeId") Long chargeId);

    /**
     * Gets a charge by external ID.
     */
    @WebMethod(operationName = "getChargeByExternalId")
    @WebResult(name = "ChargeResponse")
    ChargeResponse getChargeByExternalId(@WebParam(name = "externalId") String externalId);

    /**
     * Gets all charges for a customer.
     */
    @WebMethod(operationName = "getChargesByCustomer")
    @WebResult(name = "ChargeListResponse")
    ChargeListResponse getChargesByCustomer(@WebParam(name = "customerId") Long customerId);

    /**
     * Gets all charges.
     */
    @WebMethod(operationName = "getAllCharges")
    @WebResult(name = "ChargeListResponse")
    ChargeListResponse getAllCharges();

    /**
     * Updates a charge (value, dueDate, billingType, description).
     * Rules:
     * - value: can only be updated if status = PENDING
     * - dueDate: can be updated if status = PENDING or REGISTERED
     * - billingType: can only be updated if status = PENDING
     * - description: can always be updated
     */
    @WebMethod(operationName = "updateCharge")
    @WebResult(name = "ChargeResponse")
    ChargeResponse updateCharge(
            @WebParam(name = "chargeId") Long chargeId,
            @WebParam(name = "ChargeUpdateRequest") ChargeUpdateRequest request);

    /**
     * Updates the status of a charge.
     */
    @WebMethod(operationName = "updateChargeStatus")
    @WebResult(name = "ChargeResponse")
    ChargeResponse updateChargeStatus(
            @WebParam(name = "chargeId") Long chargeId,
            @WebParam(name = "status") String status);

    /**
     * Updates the status of a charge by external ID.
     */
    @WebMethod(operationName = "updateChargeStatusByExternalId")
    @WebResult(name = "ChargeResponse")
    ChargeResponse updateChargeStatusByExternalId(
            @WebParam(name = "externalId") String externalId,
            @WebParam(name = "status") String status);

    /**
     * Cancels a charge.
     */
    @WebMethod(operationName = "cancelCharge")
    @WebResult(name = "ChargeResponse")
    ChargeResponse cancelCharge(@WebParam(name = "chargeId") Long chargeId);
}
