package com.dac.chargeproxy.soap;

import com.dac.chargeproxy.soap.model.ChargeRequest;
import com.dac.chargeproxy.soap.model.ChargeResponse;
import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebResult;
import jakarta.jws.WebService;
import jakarta.jws.soap.SOAPBinding;

/**
 * SOAP Web Service interface for Charge Proxy operations.
 * This service acts as a proxy between Charge Manager and ASAAS payment gateway.
 * Uses SOAP RPC/Literal style for communication with Charge Manager.
 */
@WebService(name = "ChargeProxyService", targetNamespace = "http://chargeproxy.dac.com/soap")
@SOAPBinding(style = SOAPBinding.Style.RPC)
public interface ChargeProxyService {

    /**
     * Creates a new charge in the payment gateway.
     *
     * @param request the charge request
     * @return the charge response
     */
    @WebMethod(operationName = "createCharge")
    @WebResult(name = "ChargeResponse")
    ChargeResponse createCharge(@WebParam(name = "ChargeRequest") ChargeRequest request);

    /**
     * Retrieves a charge by ID.
     *
     * @param chargeId the charge ID
     * @return the charge response
     */
    @WebMethod(operationName = "getCharge")
    @WebResult(name = "ChargeResponse")
    ChargeResponse getCharge(@WebParam(name = "chargeId") String chargeId);

    /**
     * Cancels a charge by ID.
     *
     * @param chargeId the charge ID
     * @return the charge response
     */
    @WebMethod(operationName = "cancelCharge")
    @WebResult(name = "ChargeResponse")
    ChargeResponse cancelCharge(@WebParam(name = "chargeId") String chargeId);

    /**
     * Health check for the SOAP service.
     *
     * @return status message
     */
    @WebMethod(operationName = "healthCheck")
    @WebResult(name = "status")
    String healthCheck();
}

