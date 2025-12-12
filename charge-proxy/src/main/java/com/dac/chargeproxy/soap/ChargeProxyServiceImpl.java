package com.dac.chargeproxy.soap;

import com.dac.chargeproxy.client.AsaasClient;
import com.dac.chargeproxy.soap.model.ChargeRequest;
import com.dac.chargeproxy.soap.model.ChargeResponse;
import jakarta.jws.WebService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Implementation of the Charge Proxy SOAP service.
 * Currently uses a stub client - will be integrated with ASAAS in iteration 2.
 */
@Service
@WebService(
        serviceName = "ChargeProxyService",
        portName = "ChargeProxyPort",
        targetNamespace = "http://chargeproxy.dac.com/soap",
        endpointInterface = "com.dac.chargeproxy.soap.ChargeProxyService"
)
public class ChargeProxyServiceImpl implements ChargeProxyService {

    private static final Logger logger = LoggerFactory.getLogger(ChargeProxyServiceImpl.class);

    private final AsaasClient asaasClient;

    public ChargeProxyServiceImpl(AsaasClient asaasClient) {
        this.asaasClient = asaasClient;
    }

    @Override
    public ChargeResponse createCharge(ChargeRequest request) {
        logger.info("Creating charge for customer: {}, value: {}, type: {}",
                request.getCustomerId(), request.getValue(), request.getBillingType());

        try {
            // Validate billing type
            if (!isValidBillingType(request.getBillingType())) {
                return ChargeResponse.error("Invalid billing type. Must be PIX, BOLETO, or CREDIT_CARD");
            }

            // Call ASAAS client (stub implementation for now)
            ChargeResponse response = asaasClient.createCharge(request);

            logger.info("Charge created successfully: {}", response.getChargeId());
            return response;

        } catch (Exception e) {
            logger.error("Error creating charge: ", e);
            return ChargeResponse.error("Failed to create charge: " + e.getMessage());
        }
    }

    @Override
    public ChargeResponse getCharge(String chargeId) {
        logger.info("Getting charge: {}", chargeId);

        try {
            ChargeResponse response = asaasClient.getCharge(chargeId);
            logger.info("Charge retrieved: {}", chargeId);
            return response;

        } catch (Exception e) {
            logger.error("Error getting charge: ", e);
            return ChargeResponse.error("Failed to get charge: " + e.getMessage());
        }
    }

    @Override
    public ChargeResponse cancelCharge(String chargeId) {
        logger.info("Cancelling charge: {}", chargeId);

        try {
            ChargeResponse response = asaasClient.cancelCharge(chargeId);
            logger.info("Charge cancelled: {}", chargeId);
            return response;

        } catch (Exception e) {
            logger.error("Error cancelling charge: ", e);
            return ChargeResponse.error("Failed to cancel charge: " + e.getMessage());
        }
    }

    @Override
    public String healthCheck() {
        logger.debug("Health check called");
        return "Charge Proxy Service is UP - " + java.time.LocalDateTime.now();
    }

    private boolean isValidBillingType(String billingType) {
        return "PIX".equalsIgnoreCase(billingType) ||
               "BOLETO".equalsIgnoreCase(billingType) ||
               "CREDIT_CARD".equalsIgnoreCase(billingType);
    }
}

