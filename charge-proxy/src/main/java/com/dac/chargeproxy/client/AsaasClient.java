package com.dac.chargeproxy.client;

import com.dac.chargeproxy.soap.model.ChargeRequest;
import com.dac.chargeproxy.soap.model.ChargeResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Client for ASAAS API integration.
 * Currently implements a stub version - will be integrated with real ASAAS API in iteration 2.
 */
@Component
public class AsaasClient {

    private static final Logger logger = LoggerFactory.getLogger(AsaasClient.class);

    @Value("${asaas.api.base-url}")
    private String baseUrl;

    @Value("${asaas.api.access-token:}")
    private String accessToken;

    // In-memory storage for stub implementation
    private final Map<String, ChargeResponse> chargeStorage = new ConcurrentHashMap<>();

    /**
     * Creates a charge in ASAAS (stub implementation).
     * In iteration 2, this will call the real ASAAS API.
     *
     * @param request the charge request
     * @return the charge response
     */
    public ChargeResponse createCharge(ChargeRequest request) {
        logger.info("STUB: Creating charge in ASAAS for customer: {}", request.getCustomerId());

        // Generate a mock charge ID
        String chargeId = "chg_" + UUID.randomUUID().toString().substring(0, 8);

        ChargeResponse response = ChargeResponse.success(
                chargeId,
                "PENDING",
                request.getCustomerId(),
                request.getValue(),
                request.getDueDate(),
                request.getBillingType()
        );

        // Set payment-specific fields based on billing type
        switch (request.getBillingType().toUpperCase()) {
            case "PIX":
                response.setPixCode("00020126580014br.gov.bcb.pix0136" + UUID.randomUUID().toString());
                break;
            case "BOLETO":
                response.setBoletoCode("23793.38128 60000.000003 00000.000400 1 84340000010000");
                break;
            case "CREDIT_CARD":
                response.setInvoiceUrl("https://sandbox.asaas.com/i/" + chargeId);
                break;
        }

        // Store for later retrieval
        chargeStorage.put(chargeId, response);

        logger.info("STUB: Charge created with ID: {}", chargeId);
        return response;
    }

    /**
     * Gets a charge from ASAAS (stub implementation).
     *
     * @param chargeId the charge ID
     * @return the charge response
     */
    public ChargeResponse getCharge(String chargeId) {
        logger.info("STUB: Getting charge from ASAAS: {}", chargeId);

        ChargeResponse response = chargeStorage.get(chargeId);
        if (response == null) {
            return ChargeResponse.error("Charge not found: " + chargeId);
        }

        return response;
    }

    /**
     * Cancels a charge in ASAAS (stub implementation).
     *
     * @param chargeId the charge ID
     * @return the charge response
     */
    public ChargeResponse cancelCharge(String chargeId) {
        logger.info("STUB: Cancelling charge in ASAAS: {}", chargeId);

        ChargeResponse response = chargeStorage.get(chargeId);
        if (response == null) {
            return ChargeResponse.error("Charge not found: " + chargeId);
        }

        response.setStatus("CANCELED");
        chargeStorage.put(chargeId, response);

        logger.info("STUB: Charge cancelled: {}", chargeId);
        return response;
    }
}

