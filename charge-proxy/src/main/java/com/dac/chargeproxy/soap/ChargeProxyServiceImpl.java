package com.dac.chargeproxy.soap;

import com.dac.chargeproxy.business.ProxyBusinessException;
import com.dac.chargeproxy.business.ProxyBusinessRules;
import com.dac.chargeproxy.client.AsaasClient;
import com.dac.chargeproxy.soap.model.ChargeRequest;
import com.dac.chargeproxy.soap.model.ChargeResponse;
import jakarta.jws.WebService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

/**
 * Implementation of the Charge Proxy SOAP service.
 * Applies internal business rules before forwarding to ASAAS.
 * Uses Spring DI for service injection.
 */
@Component
@WebService(
        serviceName = "ChargeProxyService",
        portName = "ChargeProxyPort",
        targetNamespace = "http://chargeproxy.dac.com/soap",
        endpointInterface = "com.dac.chargeproxy.soap.ChargeProxyService"
)
public class ChargeProxyServiceImpl implements ChargeProxyService {

    private static final Logger logger = LoggerFactory.getLogger(ChargeProxyServiceImpl.class);

    @Autowired
    private AsaasClient asaasClient;

    /**
     * Default constructor required by JAX-WS.
     * Spring autowiring is handled via SpringBeanAutowiringSupport.
     */
    public ChargeProxyServiceImpl() {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
    }

    /**
     * Constructor for manual dependency injection (testing).
     */
    public ChargeProxyServiceImpl(AsaasClient asaasClient) {
        this.asaasClient = asaasClient;
    }

    /**
     * Ensures Spring beans are injected.
     */
    private void ensureInjection() {
        if (asaasClient == null) {
            SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        }
    }

    @Override
    public ChargeResponse createCharge(ChargeRequest request) {
        ensureInjection();
        logger.info("Creating charge for customer: {}, value: {}, type: {}",
                request.getCustomerId(), request.getValue(), request.getBillingType());

        try {
            // Apply business rules validation
            ProxyBusinessRules.validateChargeRequest(request);

            // Call ASAAS client (stub implementation for now)
            ChargeResponse response = asaasClient.createCharge(request);

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
        ensureInjection();
        logger.info("Getting charge: {}", chargeId);

        try {
            if (chargeId == null || chargeId.trim().isEmpty()) {
                return ChargeResponse.error("[INVALID_CHARGE_ID] Charge ID is required");
            }

            ChargeResponse response = asaasClient.getCharge(chargeId);

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
        ensureInjection();
        logger.info("Cancelling charge: {}", chargeId);

        try {
            if (chargeId == null || chargeId.trim().isEmpty()) {
                return ChargeResponse.error("[INVALID_CHARGE_ID] Charge ID is required");
            }

            ChargeResponse response = asaasClient.cancelCharge(chargeId);

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
        ensureInjection();
        logger.debug("Health check called");
        return "Charge Proxy Service is UP - " + java.time.LocalDateTime.now();
    }
}
