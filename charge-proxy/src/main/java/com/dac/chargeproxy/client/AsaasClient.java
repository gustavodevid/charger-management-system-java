package com.dac.chargeproxy.client;

import com.dac.chargeproxy.client.dto.AsaasPaymentRequest;
import com.dac.chargeproxy.client.dto.AsaasPaymentResponse;
import com.dac.chargeproxy.client.dto.AsaasPixQrCodeResponse;
import com.dac.chargeproxy.soap.model.ChargeRequest;
import com.dac.chargeproxy.soap.model.ChargeResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Client for ASAAS API integration using OpenFeign.
 * 
 * Supports two modes:
 * - REAL: Uses OpenFeign client to make actual HTTP calls to ASAAS API
 * - STUB: Uses in-memory storage for testing
 * 
 * The mode is determined by the 'mode' constructor parameter.
 */
public class AsaasClient {

    private static final Logger logger = LoggerFactory.getLogger(AsaasClient.class);

    private final AsaasFeignClient feignClient;
    private final boolean useRealApi;

    // In-memory storage for STUB mode
    private final Map<String, StubChargeData> chargeStorage = new ConcurrentHashMap<>();

    /**
     * Creates an AsaasClient with the OpenFeign client.
     * 
     * @param feignClient the OpenFeign client for ASAAS API
     * @param mode the operation mode: "REAL" for actual API calls, "STUB" for testing
     */
    public AsaasClient(AsaasFeignClient feignClient, String mode) {
        this.feignClient = feignClient;
        this.useRealApi = "REAL".equalsIgnoreCase(mode);

        logger.info("AsaasClient initialized - mode: {}", useRealApi ? "REAL (OpenFeign)" : "STUB");
    }

    /**
     * Creates a charge in ASAAS.
     *
     * @param request the charge request
     * @return the charge response
     */
    public ChargeResponse createCharge(ChargeRequest request) {
        if (useRealApi) {
            return createChargeReal(request);
        } else {
            return createChargeStub(request);
        }
    }

    /**
     * Gets a charge from ASAAS.
     *
     * @param chargeId the charge ID
     * @return the charge response
     */
    public ChargeResponse getCharge(String chargeId) {
        if (useRealApi) {
            return getChargeReal(chargeId);
        } else {
            return getChargeStub(chargeId);
        }
    }

    /**
     * Cancels a charge in ASAAS.
     *
     * @param chargeId the charge ID
     * @return the charge response
     */
    public ChargeResponse cancelCharge(String chargeId) {
        if (useRealApi) {
            return cancelChargeReal(chargeId);
        } else {
            return cancelChargeStub(chargeId);
        }
    }

    // ========== REAL API IMPLEMENTATION (OpenFeign) ==========

    private ChargeResponse createChargeReal(ChargeRequest request) {
        logger.info("Creating charge in ASAAS API (OpenFeign) for customer: {}", request.getCustomerId());

        try {
            // Build ASAAS request
            AsaasPaymentRequest asaasRequest = AsaasPaymentRequest.builder()
                    .customer(request.getCustomerId())
                    .billingType(request.getBillingType().toUpperCase())
                    .value(request.getValue())
                    .dueDate(request.getDueDate())
                    .description(request.getDescription())
                    .build();

            // Make API call using Feign
            AsaasPaymentResponse asaasResponse = feignClient.createPayment(asaasRequest);
            
            logger.info("Charge created successfully - ID: {}, Status: {}", 
                    asaasResponse.getId(), asaasResponse.getStatus());

            ChargeResponse chargeResponse = mapAsaasResponseToChargeResponse(asaasResponse);
            
            // Fetch PIX QR Code if applicable
            if ("PIX".equalsIgnoreCase(asaasResponse.getBillingType()) && asaasResponse.getId() != null) {
                fetchAndSetPixQrCode(asaasResponse.getId(), chargeResponse);
            }
            
            return chargeResponse;

        } catch (AsaasErrorDecoder.AsaasApiException e) {
            logger.error("ASAAS API error creating charge: {}", e.getMessage());
            return handleAsaasError(e);
        } catch (Exception e) {
            logger.error("Error creating charge in ASAAS: ", e);
            return ChargeResponse.error("Failed to create charge: " + e.getMessage());
        }
    }

    private ChargeResponse getChargeReal(String chargeId) {
        logger.info("Getting charge from ASAAS API (OpenFeign): {}", chargeId);

        try {
            AsaasPaymentResponse asaasResponse = feignClient.getPayment(chargeId);
            
            ChargeResponse chargeResponse = mapAsaasResponseToChargeResponse(asaasResponse);
            
            // Fetch PIX QR Code if applicable
            if ("PIX".equalsIgnoreCase(asaasResponse.getBillingType()) && asaasResponse.getId() != null) {
                fetchAndSetPixQrCode(asaasResponse.getId(), chargeResponse);
            }
            
            return chargeResponse;

        } catch (AsaasErrorDecoder.AsaasApiException e) {
            logger.error("ASAAS API error getting charge: {}", e.getMessage());
            return handleAsaasError(e);
        } catch (Exception e) {
            logger.error("Error getting charge from ASAAS: ", e);
            return ChargeResponse.error("Failed to get charge: " + e.getMessage());
        }
    }

    private ChargeResponse cancelChargeReal(String chargeId) {
        logger.info("Cancelling charge in ASAAS API (OpenFeign): {}", chargeId);

        try {
            AsaasPaymentResponse asaasResponse = feignClient.deletePayment(chargeId);
            return mapAsaasResponseToChargeResponse(asaasResponse);

        } catch (AsaasErrorDecoder.AsaasApiException e) {
            logger.error("ASAAS API error cancelling charge: {}", e.getMessage());
            return handleAsaasError(e);
        } catch (Exception e) {
            logger.error("Error cancelling charge in ASAAS: ", e);
            return ChargeResponse.error("Failed to cancel charge: " + e.getMessage());
        }
    }

    /**
     * Fetches PIX QR Code for a payment and sets it on the response.
     */
    private void fetchAndSetPixQrCode(String paymentId, ChargeResponse response) {
        try {
            AsaasPixQrCodeResponse qrCode = feignClient.getPixQrCode(paymentId);
            if (qrCode != null && qrCode.getPayload() != null) {
                response.setPixCode(qrCode.getPayload());
                logger.debug("PIX QR Code fetched for payment: {}", paymentId);
            }
        } catch (Exception e) {
            logger.warn("Could not fetch PIX QR Code for payment {}: {}", paymentId, e.getMessage());
        }
    }

    /**
     * Maps ASAAS payment response to our ChargeResponse.
     */
    private ChargeResponse mapAsaasResponseToChargeResponse(AsaasPaymentResponse asaasResponse) {
        ChargeResponse response = ChargeResponse.success(
                asaasResponse.getId(),
                asaasResponse.getStatus(),
                asaasResponse.getCustomer(),
                asaasResponse.getValue(),
                asaasResponse.getDueDate(),
                asaasResponse.getBillingType()
        );

        // Set additional fields
        if (asaasResponse.getInvoiceUrl() != null) {
            response.setInvoiceUrl(asaasResponse.getInvoiceUrl());
        }
        if (asaasResponse.getBankSlipUrl() != null) {
            response.setBoletoCode(asaasResponse.getNossoNumero());
            response.setInvoiceUrl(asaasResponse.getBankSlipUrl());
        }

        logger.info("Charge mapped successfully: id={}, status={}", asaasResponse.getId(), asaasResponse.getStatus());
        return response;
    }

    /**
     * Handles ASAAS API errors and converts to ChargeResponse.
     */
    private ChargeResponse handleAsaasError(AsaasErrorDecoder.AsaasApiException e) {
        if (e.isValidationError()) {
            return ChargeResponse.error("[VALIDATION_ERROR] " + e.getMessage());
        } else if (e.isAuthError()) {
            return ChargeResponse.error("[AUTH_ERROR] Invalid or expired access token");
        } else if (e.isNotFound()) {
            return ChargeResponse.error("[NOT_FOUND] Charge not found");
        } else if (e.isServerError()) {
            return ChargeResponse.error("[ASAAS_ERROR] Internal server error at ASAAS");
        }
        return ChargeResponse.error("[ERROR] " + e.getMessage());
    }

    // ========== STUB IMPLEMENTATION (for testing) ==========

    private static class StubChargeData {
        String chargeId;
        String asaasStatus;
        String customerId;
        BigDecimal value;
        String dueDate;
        String billingType;
        String pixCode;
        String boletoCode;
        String invoiceUrl;

        ChargeResponse toResponse() {
            ChargeResponse response = ChargeResponse.success(
                    chargeId, asaasStatus, customerId, value, dueDate, billingType
            );
            response.setPixCode(pixCode);
            response.setBoletoCode(boletoCode);
            response.setInvoiceUrl(invoiceUrl);
            return response;
        }
    }

    private ChargeResponse createChargeStub(ChargeRequest request) {
        logger.info("STUB: Creating charge for customer: {}", request.getCustomerId());

        String chargeId = "pay_" + UUID.randomUUID().toString().substring(0, 16);

        StubChargeData data = new StubChargeData();
        data.chargeId = chargeId;
        data.asaasStatus = "PENDING";
        data.customerId = request.getCustomerId();
        data.value = request.getValue();
        data.dueDate = request.getDueDate();
        data.billingType = request.getBillingType();

        switch (request.getBillingType().toUpperCase()) {
            case "PIX":
                data.pixCode = "00020126580014br.gov.bcb.pix0136" + UUID.randomUUID().toString();
                break;
            case "BOLETO":
                data.boletoCode = "23793.38128 60000.000003 00000.000400 1 84340000010000";
                break;
            case "CREDIT_CARD":
                data.invoiceUrl = "https://sandbox.asaas.com/i/" + chargeId;
                break;
        }

        chargeStorage.put(chargeId, data);
        logger.info("STUB: Charge created: {}", chargeId);
        return data.toResponse();
    }

    private ChargeResponse getChargeStub(String chargeId) {
        logger.info("STUB: Getting charge: {}", chargeId);

        StubChargeData data = chargeStorage.get(chargeId);
        if (data == null) {
            return ChargeResponse.error("Charge not found: " + chargeId);
        }

        return data.toResponse();
    }

    private ChargeResponse cancelChargeStub(String chargeId) {
        logger.info("STUB: Cancelling charge: {}", chargeId);

        StubChargeData data = chargeStorage.get(chargeId);
        if (data == null) {
            return ChargeResponse.error("Charge not found: " + chargeId);
        }

        data.asaasStatus = "DELETED";
        chargeStorage.put(chargeId, data);

        logger.info("STUB: Charge cancelled: {}", chargeId);
        return data.toResponse();
    }

    /**
     * Simulates a status update (STUB mode only).
     */
    public ChargeResponse simulateStatusUpdate(String chargeId, String newAsaasStatus) {
        if (useRealApi) {
            logger.warn("simulateStatusUpdate called in REAL mode - ignoring");
            return ChargeResponse.error("Status simulation not available in REAL mode");
        }

        StubChargeData data = chargeStorage.get(chargeId);
        if (data == null) {
            return ChargeResponse.error("Charge not found: " + chargeId);
        }

        String oldStatus = data.asaasStatus;
        data.asaasStatus = newAsaasStatus;
        chargeStorage.put(chargeId, data);

        logger.info("STUB: Status updated for {}: {} -> {}", chargeId, oldStatus, newAsaasStatus);
        return data.toResponse();
    }

    /**
     * Returns whether the client is using real API mode.
     */
    public boolean isRealApiMode() {
        return useRealApi;
    }
}
