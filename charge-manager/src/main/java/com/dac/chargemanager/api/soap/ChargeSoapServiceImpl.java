package com.dac.chargemanager.api.soap;

import com.dac.chargemanager.api.soap.model.ChargeListResponse;
import com.dac.chargemanager.api.soap.model.ChargeRequest;
import com.dac.chargemanager.api.soap.model.ChargeResponse;
import com.dac.chargemanager.api.soap.model.ChargeUpdateRequest;
import com.dac.chargemanager.business.dto.ChargeDTO;
import com.dac.chargemanager.business.exception.BusinessException;
import com.dac.chargemanager.business.exception.ResourceNotFoundException;
import com.dac.chargemanager.business.service.ChargeService;
import com.dac.chargemanager.infra.client.ChargeProxyClient;
import jakarta.jws.WebService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of the Charge SOAP service.
 * Uses Spring DI for service injection.
 */
@Component
@WebService(
        serviceName = "ChargeService",
        portName = "ChargePort",
        targetNamespace = "http://chargemanager.dac.com/soap",
        endpointInterface = "com.dac.chargemanager.api.soap.ChargeSoapService"
)
public class ChargeSoapServiceImpl implements ChargeSoapService {

    private static final Logger logger = LoggerFactory.getLogger(ChargeSoapServiceImpl.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Autowired
    private ChargeService chargeService;

    @Autowired
    private ChargeProxyClient proxyClient;

    public ChargeSoapServiceImpl() {
        // Enable Spring autowiring for JAX-WS endpoint
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
    }

    public ChargeSoapServiceImpl(ChargeService chargeService, ChargeProxyClient proxyClient) {
        this.chargeService = chargeService;
        this.proxyClient = proxyClient;
    }

    /**
     * Ensures Spring beans are injected.
     */
    private void ensureInjection() {
        if (chargeService == null || proxyClient == null) {
            SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        }
    }

    @Override
    public ChargeResponse createCharge(ChargeRequest request) {
        ensureInjection();
        logger.info("SOAP createCharge - customerId: {}, value: {}, type: {}", 
                request.getCustomerId(), request.getValue(), request.getBillingType());

        try {
            // Create charge DTO
            ChargeDTO dto = new ChargeDTO();
            dto.setCustomerId(request.getCustomerId());
            dto.setValue(request.getValue());
            dto.setDueDate(LocalDate.parse(request.getDueDate(), DATE_FORMATTER));
            dto.setBillingType(request.getBillingType());
            dto.setDescription(request.getDescription());

            // Create charge in database (status: PENDING)
            ChargeDTO created = chargeService.createCharge(dto);
            logger.info("Charge created with ID: {}", created.getId());

            // Call proxy to register with payment gateway
            ChargeProxyClient.ProxyChargeResponse proxyResponse = proxyClient.createCharge(
                    created.getCustomerId().toString(),
                    created.getValue(),
                    created.getDueDate().format(DATE_FORMATTER),
                    created.getBillingType(),
                    created.getDescription()
            );

            if (proxyResponse.isSuccess()) {
                // Update charge with external ID and payment info
                ChargeDTO updated = chargeService.updateChargeFromProxy(
                        created.getId(),
                        proxyResponse.getChargeId(),
                        proxyResponse.getPixCode(),
                        proxyResponse.getBoletoCode(),
                        proxyResponse.getInvoiceUrl()
                );
                logger.info("Charge {} registered with external ID: {}", created.getId(), proxyResponse.getChargeId());
                return toResponse(updated);
            } else {
                // Return the charge without external registration
                logger.warn("Proxy registration failed: {}", proxyResponse.getErrorMessage());
                ChargeResponse response = toResponse(created);
                response.setErrorMessage("Charge created but proxy registration failed: " + proxyResponse.getErrorMessage());
                return response;
            }

        } catch (BusinessException e) {
            logger.warn("Business error creating charge: {}", e.getMessage());
            return ChargeResponse.error(e.getMessage());
        } catch (ResourceNotFoundException e) {
            logger.warn("Resource not found: {}", e.getMessage());
            return ChargeResponse.error(e.getMessage());
        } catch (Exception e) {
            logger.error("Error creating charge", e);
            return ChargeResponse.error("Failed to create charge: " + e.getMessage());
        }
    }

    @Override
    public ChargeResponse getCharge(Long chargeId) {
        ensureInjection();
        logger.info("SOAP getCharge - id: {}", chargeId);

        try {
            ChargeDTO charge = chargeService.getChargeById(chargeId);
            return toResponse(charge);

        } catch (ResourceNotFoundException e) {
            logger.warn("Charge not found: {}", chargeId);
            return ChargeResponse.error(e.getMessage());
        } catch (Exception e) {
            logger.error("Error getting charge", e);
            return ChargeResponse.error("Failed to get charge: " + e.getMessage());
        }
    }

    @Override
    public ChargeResponse getChargeByExternalId(String externalId) {
        ensureInjection();
        logger.info("SOAP getChargeByExternalId - externalId: {}", externalId);

        try {
            ChargeDTO charge = chargeService.getChargeByExternalId(externalId);
            return toResponse(charge);

        } catch (ResourceNotFoundException e) {
            logger.warn("Charge not found with external ID: {}", externalId);
            return ChargeResponse.error(e.getMessage());
        } catch (Exception e) {
            logger.error("Error getting charge by external ID", e);
            return ChargeResponse.error("Failed to get charge: " + e.getMessage());
        }
    }

    @Override
    public ChargeListResponse getChargesByCustomer(Long customerId) {
        ensureInjection();
        logger.info("SOAP getChargesByCustomer - customerId: {}", customerId);

        try {
            List<ChargeDTO> charges = chargeService.getChargesByCustomerId(customerId);
            List<ChargeResponse> responses = charges.stream()
                    .map(this::toResponse)
                    .collect(Collectors.toList());

            logger.info("Found {} charges for customer {}", responses.size(), customerId);
            return ChargeListResponse.success(responses);

        } catch (ResourceNotFoundException e) {
            logger.warn("Customer not found: {}", customerId);
            return ChargeListResponse.error(e.getMessage());
        } catch (Exception e) {
            logger.error("Error getting charges by customer", e);
            return ChargeListResponse.error("Failed to get charges: " + e.getMessage());
        }
    }

    @Override
    public ChargeListResponse getAllCharges() {
        ensureInjection();
        logger.info("SOAP getAllCharges");

        try {
            List<ChargeDTO> charges = chargeService.getAllCharges();
            List<ChargeResponse> responses = charges.stream()
                    .map(this::toResponse)
                    .collect(Collectors.toList());

            logger.info("Found {} charges", responses.size());
            return ChargeListResponse.success(responses);

        } catch (Exception e) {
            logger.error("Error getting all charges", e);
            return ChargeListResponse.error("Failed to get charges: " + e.getMessage());
        }
    }

    @Override
    public ChargeResponse updateCharge(Long chargeId, ChargeUpdateRequest request) {
        ensureInjection();
        logger.info("SOAP updateCharge - id: {}", chargeId);

        try {
            if (request == null || !request.hasUpdates()) {
                return ChargeResponse.error("No fields provided for update");
            }

            // Build DTO with fields to update
            ChargeDTO updates = new ChargeDTO();
            updates.setValue(request.getValue());
            updates.setBillingType(request.getBillingType());
            updates.setDescription(request.getDescription());

            // Parse dueDate if provided
            if (request.getDueDate() != null && !request.getDueDate().isEmpty()) {
                updates.setDueDate(LocalDate.parse(request.getDueDate(), DATE_FORMATTER));
            }

            ChargeDTO updated = chargeService.updateCharge(chargeId, updates);
            logger.info("Charge {} updated successfully", chargeId);
            return toResponse(updated);

        } catch (ResourceNotFoundException e) {
            logger.warn("Charge not found: {}", chargeId);
            return ChargeResponse.error(e.getMessage());
        } catch (BusinessException e) {
            logger.warn("Business error updating charge: {}", e.getMessage());
            return ChargeResponse.error(e.getMessage());
        } catch (Exception e) {
            logger.error("Error updating charge", e);
            return ChargeResponse.error("Failed to update charge: " + e.getMessage());
        }
    }

    @Override
    public ChargeResponse updateChargeStatus(Long chargeId, String status) {
        ensureInjection();
        logger.info("SOAP updateChargeStatus - id: {}, status: {}", chargeId, status);

        try {
            ChargeDTO updated = chargeService.updateStatus(chargeId, status);
            logger.info("Charge {} status updated to {}", chargeId, status);
            return toResponse(updated);

        } catch (ResourceNotFoundException e) {
            logger.warn("Charge not found: {}", chargeId);
            return ChargeResponse.error(e.getMessage());
        } catch (BusinessException e) {
            logger.warn("Business error updating status: {}", e.getMessage());
            return ChargeResponse.error(e.getMessage());
        } catch (Exception e) {
            logger.error("Error updating charge status", e);
            return ChargeResponse.error("Failed to update charge status: " + e.getMessage());
        }
    }

    @Override
    public ChargeResponse updateChargeStatusByExternalId(String externalId, String status) {
        ensureInjection();
        logger.info("SOAP updateChargeStatusByExternalId - externalId: {}, status: {}", externalId, status);

        try {
            ChargeDTO updated = chargeService.updateStatusByExternalId(externalId, status);
            logger.info("Charge with external ID {} status updated to {}", externalId, status);
            return toResponse(updated);

        } catch (ResourceNotFoundException e) {
            logger.warn("Charge not found with external ID: {}", externalId);
            return ChargeResponse.error(e.getMessage());
        } catch (BusinessException e) {
            logger.warn("Business error updating status: {}", e.getMessage());
            return ChargeResponse.error(e.getMessage());
        } catch (Exception e) {
            logger.error("Error updating charge status by external ID", e);
            return ChargeResponse.error("Failed to update charge status: " + e.getMessage());
        }
    }

    @Override
    public ChargeResponse cancelCharge(Long chargeId) {
        ensureInjection();
        logger.info("SOAP cancelCharge - id: {}", chargeId);

        try {
            // Get charge to check for external ID
            ChargeDTO charge = chargeService.getChargeById(chargeId);

            // If charge has external ID, cancel in proxy
            if (charge.getExternalId() != null && !charge.getExternalId().isEmpty()) {
                ChargeProxyClient.ProxyChargeResponse proxyResponse = proxyClient.cancelCharge(charge.getExternalId());
                if (!proxyResponse.isSuccess()) {
                    logger.warn("Proxy cancellation failed: {}", proxyResponse.getErrorMessage());
                }
            }

            // Cancel in database
            ChargeDTO canceled = chargeService.cancelCharge(chargeId);
            logger.info("Charge {} canceled", chargeId);
            return toResponse(canceled);

        } catch (ResourceNotFoundException e) {
            logger.warn("Charge not found: {}", chargeId);
            return ChargeResponse.error(e.getMessage());
        } catch (BusinessException e) {
            logger.warn("Business error canceling charge: {}", e.getMessage());
            return ChargeResponse.error(e.getMessage());
        } catch (Exception e) {
            logger.error("Error canceling charge", e);
            return ChargeResponse.error("Failed to cancel charge: " + e.getMessage());
        }
    }

    /**
     * Converts ChargeDTO to ChargeResponse.
     */
    private ChargeResponse toResponse(ChargeDTO dto) {
        return ChargeResponse.success(
                dto.getId(),
                dto.getCustomerId(),
                dto.getCustomerName(),
                dto.getCustomerEmail(),
                dto.getExternalId(),
                dto.getValue(),
                dto.getDueDate() != null ? dto.getDueDate().format(DATE_FORMATTER) : null,
                dto.getBillingType(),
                dto.getStatus(),
                dto.getPixCode(),
                dto.getBoletoCode(),
                dto.getInvoiceUrl(),
                dto.getCreatedAt() != null ? dto.getCreatedAt().toString() : null,
                dto.getUpdatedAt() != null ? dto.getUpdatedAt().toString() : null
        );
    }
}
