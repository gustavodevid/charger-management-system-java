package com.dac.chargeproxy.client;

import com.dac.chargeproxy.client.dto.AsaasPaymentRequest;
import com.dac.chargeproxy.client.dto.AsaasPaymentResponse;
import com.dac.chargeproxy.client.dto.AsaasPixQrCodeResponse;
import feign.Headers;
import feign.Param;
import feign.RequestLine;

/**
 * OpenFeign declarative client for ASAAS Payment API.
 * 
 * This interface defines the contract for communicating with ASAAS REST API.
 * Feign generates the implementation at runtime.
 */
@Headers({"Content-Type: application/json", "User-Agent: ChargeManagementSystem/1.0"})
public interface AsaasFeignClient {

    /**
     * Creates a new payment in ASAAS.
     *
     * @param request the payment request data
     * @return the created payment response
     */
    @RequestLine("POST /payments")
    AsaasPaymentResponse createPayment(AsaasPaymentRequest request);

    /**
     * Retrieves a payment by ID.
     *
     * @param paymentId the payment ID
     * @return the payment response
     */
    @RequestLine("GET /payments/{paymentId}")
    AsaasPaymentResponse getPayment(@Param("paymentId") String paymentId);

    /**
     * Deletes (cancels) a payment.
     *
     * @param paymentId the payment ID
     * @return the deleted payment response
     */
    @RequestLine("DELETE /payments/{paymentId}")
    AsaasPaymentResponse deletePayment(@Param("paymentId") String paymentId);

    /**
     * Retrieves the PIX QR Code for a payment.
     *
     * @param paymentId the payment ID
     * @return the PIX QR Code response
     */
    @RequestLine("GET /payments/{paymentId}/pixQrCode")
    AsaasPixQrCodeResponse getPixQrCode(@Param("paymentId") String paymentId);
}
