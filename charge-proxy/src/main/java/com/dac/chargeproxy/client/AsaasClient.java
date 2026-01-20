package com.dac.chargeproxy.client;

import com.dac.chargeproxy.business.ProxyBusinessRules;
import com.dac.chargeproxy.client.dto.AsaasErrorResponse;
import com.dac.chargeproxy.client.dto.AsaasPaymentRequest;
import com.dac.chargeproxy.client.dto.AsaasPaymentResponse;
import com.dac.chargeproxy.client.dto.AsaasPixQrCodeResponse;
import com.dac.chargeproxy.soap.model.ChargeRequest;
import com.dac.chargeproxy.soap.model.ChargeResponse;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.classic.methods.HttpDelete;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Client for ASAAS API integration using Apache HttpClient 5.
 * 
 * Supports two modes:
 * - REAL: Makes actual HTTP calls to ASAAS API
 * - STUB: Uses in-memory storage for testing
 * 
 * The mode is determined by the 'mode' constructor parameter.
 */
public class AsaasClient implements Closeable {

    private static final Logger logger = LoggerFactory.getLogger(AsaasClient.class);

    private static final String USER_AGENT = "ChargeManagementSystem/1.0";
    private static final String PAYMENTS_ENDPOINT = "/payments";

    private final String baseUrl;
    private final String accessToken;
    private final boolean useRealApi;
    private final CloseableHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final Random random;

    // In-memory storage for STUB mode
    private final Map<String, StubChargeData> chargeStorage = new ConcurrentHashMap<>();

    /**
     * Creates an AsaasClient with the specified mode.
     * 
     * @param baseUrl the base URL of the ASAAS API
     * @param accessToken the API access token
     * @param mode the operation mode: "REAL" for actual API calls, "STUB" for testing
     */
    public AsaasClient(String baseUrl, String accessToken, String mode) {
        this.baseUrl = baseUrl;
        this.accessToken = accessToken;
        this.useRealApi = "REAL".equalsIgnoreCase(mode);
        this.httpClient = HttpClients.createDefault();
        this.objectMapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.random = new Random();

        logger.info("AsaasClient initialized - baseUrl: {}, mode: {}", baseUrl, useRealApi ? "REAL" : "STUB");
    }

    /**
     * Creates an AsaasClient in REAL mode (default).
     */
    public AsaasClient(String baseUrl, String accessToken) {
        this(baseUrl, accessToken, "REAL");
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

    // ========== REAL API IMPLEMENTATION ==========

    private ChargeResponse createChargeReal(ChargeRequest request) {
        logger.info("Creating charge in ASAAS API for customer: {}", request.getCustomerId());

        try {
            // Build ASAAS request
            AsaasPaymentRequest asaasRequest = AsaasPaymentRequest.builder()
                    .customer(request.getCustomerId())
                    .billingType(request.getBillingType().toUpperCase())
                    .value(request.getValue())
                    .dueDate(request.getDueDate())
                    .description(request.getDescription())
                    .build();

            String requestBody = objectMapper.writeValueAsString(asaasRequest);
            logger.debug("ASAAS request body: {}", requestBody);

            // Make HTTP POST request
            HttpPost httpPost = new HttpPost(baseUrl + PAYMENTS_ENDPOINT);
            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setHeader("access_token", accessToken);
            httpPost.setHeader("User-Agent", USER_AGENT);
            httpPost.setEntity(new StringEntity(requestBody, ContentType.APPLICATION_JSON));

            return httpClient.execute(httpPost, response -> {
                int statusCode = response.getCode();
                String responseBody = EntityUtils.toString(response.getEntity());
                logger.debug("ASAAS response: {} - {}", statusCode, responseBody);

                if (statusCode >= 200 && statusCode < 300) {
                    AsaasPaymentResponse asaasResponse = objectMapper.readValue(responseBody, AsaasPaymentResponse.class);
                    return mapAsaasResponseToChargeResponse(asaasResponse);
                } else {
                    return handleAsaasError(statusCode, responseBody);
                }
            });

        } catch (Exception e) {
            logger.error("Error creating charge in ASAAS: ", e);
            return ChargeResponse.error("Failed to create charge: " + e.getMessage());
        }
    }

    private ChargeResponse getChargeReal(String chargeId) {
        logger.info("Getting charge from ASAAS API: {}", chargeId);

        try {
            HttpGet httpGet = new HttpGet(baseUrl + PAYMENTS_ENDPOINT + "/" + chargeId);
            httpGet.setHeader("access_token", accessToken);
            httpGet.setHeader("User-Agent", USER_AGENT);

            return httpClient.execute(httpGet, response -> {
                int statusCode = response.getCode();
                String responseBody = EntityUtils.toString(response.getEntity());
                logger.debug("ASAAS response: {} - {}", statusCode, responseBody);

                if (statusCode >= 200 && statusCode < 300) {
                    AsaasPaymentResponse asaasResponse = objectMapper.readValue(responseBody, AsaasPaymentResponse.class);
                    ChargeResponse chargeResponse = mapAsaasResponseToChargeResponse(asaasResponse);
                    
                    // If it's a PIX payment, try to get the QR Code
                    if ("PIX".equalsIgnoreCase(asaasResponse.getBillingType()) && asaasResponse.getId() != null) {
                        fetchAndSetPixQrCode(asaasResponse.getId(), chargeResponse);
                    }
                    
                    return chargeResponse;
                } else {
                    return handleAsaasError(statusCode, responseBody);
                }
            });

        } catch (Exception e) {
            logger.error("Error getting charge from ASAAS: ", e);
            return ChargeResponse.error("Failed to get charge: " + e.getMessage());
        }
    }

    private ChargeResponse cancelChargeReal(String chargeId) {
        logger.info("Cancelling charge in ASAAS API: {}", chargeId);

        try {
            HttpDelete httpDelete = new HttpDelete(baseUrl + PAYMENTS_ENDPOINT + "/" + chargeId);
            httpDelete.setHeader("access_token", accessToken);
            httpDelete.setHeader("User-Agent", USER_AGENT);

            return httpClient.execute(httpDelete, response -> {
                int statusCode = response.getCode();
                String responseBody = EntityUtils.toString(response.getEntity());
                logger.debug("ASAAS response: {} - {}", statusCode, responseBody);

                if (statusCode >= 200 && statusCode < 300) {
                    AsaasPaymentResponse asaasResponse = objectMapper.readValue(responseBody, AsaasPaymentResponse.class);
                    return mapAsaasResponseToChargeResponse(asaasResponse);
                } else {
                    return handleAsaasError(statusCode, responseBody);
                }
            });

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
            HttpGet httpGet = new HttpGet(baseUrl + PAYMENTS_ENDPOINT + "/" + paymentId + "/pixQrCode");
            httpGet.setHeader("access_token", accessToken);
            httpGet.setHeader("User-Agent", USER_AGENT);

            httpClient.execute(httpGet, pixResponse -> {
                int statusCode = pixResponse.getCode();
                if (statusCode >= 200 && statusCode < 300) {
                    String pixBody = EntityUtils.toString(pixResponse.getEntity());
                    AsaasPixQrCodeResponse qrCode = objectMapper.readValue(pixBody, AsaasPixQrCodeResponse.class);
                    if (qrCode.getPayload() != null) {
                        response.setPixCode(qrCode.getPayload());
                    }
                }
                return null;
            });
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
     * Handles ASAAS API errors.
     */
    private ChargeResponse handleAsaasError(int statusCode, String responseBody) {
        String errorMessage;

        try {
            AsaasErrorResponse errorResponse = objectMapper.readValue(responseBody, AsaasErrorResponse.class);
            errorMessage = errorResponse.getFormattedMessage();
        } catch (Exception e) {
            errorMessage = "HTTP " + statusCode + ": " + responseBody;
        }

        logger.error("ASAAS API error: {} - {}", statusCode, errorMessage);

        return switch (statusCode) {
            case 400 -> ChargeResponse.error("[VALIDATION_ERROR] " + errorMessage);
            case 401 -> ChargeResponse.error("[AUTH_ERROR] Invalid or expired access token");
            case 404 -> ChargeResponse.error("[NOT_FOUND] Charge not found");
            case 500 -> ChargeResponse.error("[ASAAS_ERROR] Internal server error at ASAAS");
            default -> ChargeResponse.error("[ERROR] " + errorMessage);
        };
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

    @Override
    public void close() {
        try {
            if (httpClient != null) {
                httpClient.close();
                logger.info("AsaasClient HTTP client closed");
            }
        } catch (Exception e) {
            logger.warn("Error closing HTTP client: {}", e.getMessage());
        }
    }
}
