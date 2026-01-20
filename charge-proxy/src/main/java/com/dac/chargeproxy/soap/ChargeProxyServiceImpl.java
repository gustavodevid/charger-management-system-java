package com.dac.chargeproxy.soap;

import com.dac.chargeproxy.business.ProxyBusinessException;
import com.dac.chargeproxy.business.ProxyBusinessRules;
import com.dac.chargeproxy.client.AsaasClient;
import com.dac.chargeproxy.config.ServiceLocator;
import com.dac.chargeproxy.soap.model.ChargeRequest;
import com.dac.chargeproxy.soap.model.ChargeResponse;
import jakarta.jws.WebService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the Charge Proxy SOAP service.
 * Applies internal business rules before forwarding to ASAAS.
 */
@WebService(
        serviceName = "ChargeProxyService",
        portName = "ChargeProxyPort",
        targetNamespace = "http://chargeproxy.dac.com/soap",
        endpointInterface = "com.dac.chargeproxy.soap.ChargeProxyService"
)
public class ChargeProxyServiceImpl implements ChargeProxyService {

    private static final Logger logger = LoggerFactory.getLogger(ChargeProxyServiceImpl.class);

    private AsaasClient asaasClient;

    /**
     * Default constructor required by JAX-WS.
     * AsaasClient will be obtained from ServiceLocator.
     */
    public ChargeProxyServiceImpl() {
        // AsaasClient will be lazily initialized from ServiceLocator
    }

    /**
     * Constructor for manual dependency injection.
     */
    public ChargeProxyServiceImpl(AsaasClient asaasClient) {
        this.asaasClient = asaasClient;
    }

    private AsaasClient getAsaasClient() {
        if (asaasClient == null) {
            asaasClient = ServiceLocator.getAsaasClient();
        }
        return asaasClient;
    }

    @Override
    public ChargeResponse createCharge(ChargeRequest request) {
        logger.info("Creating charge for customer: {}, value: {}, type: {}",
                request.getCustomerId(), request.getValue(), request.getBillingType());

        try {
            // Apply business rules validation
            ProxyBusinessRules.validateChargeRequest(request);

            // Call ASAAS client (stub implementation for now)
            ChargeResponse response = getAsaasClient().createCharge(request);

            // Map ASAAS status to internal status
            if (response.isSuccess() && response.getStatus() != null) {
                String internalStatus = ProxyBusinessRules.mapAsaasStatusToInternal(response.getStatus());
                response.setStatus(internalStatus);
            }

            logger.info("Charge created successfully: {}", response.getChargeId());
            return response;

        } catch (ProxyBusinessException e) {
            logger.warn("Business rule validation failed: {} - {}", e.getErrorCode(), e.getMessage());
            return ChargeResponse.error("[" + e.getErrorCode() + "] " + e.getMessage());

        } catch (Exception e) {
            logger.error("Error creating charge: ", e);
            return ChargeResponse.error("Failed to create charge: " + e.getMessage());
        }
    }

    @Override
    public ChargeResponse getCharge(String chargeId) {
        logger.info("Getting charge: {}", chargeId);

        try {
            if (chargeId == null || chargeId.trim().isEmpty()) {
                return ChargeResponse.error("[INVALID_CHARGE_ID] Charge ID is required");
            }

            ChargeResponse response = getAsaasClient().getCharge(chargeId);

            // Map ASAAS status to internal status
            if (response.isSuccess() && response.getStatus() != null) {
                String internalStatus = ProxyBusinessRules.mapAsaasStatusToInternal(response.getStatus());
                response.setStatus(internalStatus);
            }

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
            if (chargeId == null || chargeId.trim().isEmpty()) {
                return ChargeResponse.error("[INVALID_CHARGE_ID] Charge ID is required");
            }

            ChargeResponse response = getAsaasClient().cancelCharge(chargeId);

            // Map ASAAS status to internal status
            if (response.isSuccess() && response.getStatus() != null) {
                String internalStatus = ProxyBusinessRules.mapAsaasStatusToInternal(response.getStatus());
                response.setStatus(internalStatus);
            }

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
}
