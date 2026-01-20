package com.dac.chargemanager.business.service;

import com.dac.chargemanager.business.dto.ChargeDTO;
import com.dac.chargemanager.business.dto.CustomerDTO;
import com.dac.chargemanager.business.event.ChargeEvent;
import com.dac.chargemanager.business.event.ChargeEventPublisher;
import com.dac.chargemanager.business.exception.BusinessException;
import com.dac.chargemanager.business.exception.ResourceNotFoundException;
import com.dac.chargemanager.infra.entity.Charge;
import com.dac.chargemanager.infra.entity.ChargeStatus;
import com.dac.chargemanager.infra.repository.ChargeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service layer for Charge business logic.
 */
public class ChargeService {

    private static final Logger logger = LoggerFactory.getLogger(ChargeService.class);

    private final ChargeRepository chargeRepository;
    private final CustomerService customerService;
    private final ChargeEventPublisher eventPublisher;

    public ChargeService(ChargeRepository chargeRepository, CustomerService customerService,
                         ChargeEventPublisher eventPublisher) {
        this.chargeRepository = chargeRepository;
        this.customerService = customerService;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Creates a new charge.
     * 
     * @param dto the charge data
     * @return the created charge with payment info
     */
    public ChargeDTO createCharge(ChargeDTO dto) {
        logger.info("Creating charge for customer: {}, value: {}, type: {}", 
                dto.getCustomerId(), dto.getValue(), dto.getBillingType());

        // Validate customer exists
        CustomerDTO customer = customerService.getCustomerById(dto.getCustomerId());

        // Validate billing type
        if (!isValidBillingType(dto.getBillingType())) {
            throw new BusinessException("Invalid billing type. Must be PIX, BOLETO, or CREDIT_CARD");
        }

        // Create charge entity
        Charge charge = toEntity(dto);
        charge.setStatus(ChargeStatus.PENDING);

        // Save to database
        Charge savedCharge = chargeRepository.save(charge);
        logger.info("Charge created with ID: {}", savedCharge.getId());

        // Convert to DTO with customer info
        ChargeDTO result = toDTO(savedCharge);
        result.setCustomerName(customer.getName());
        result.setCustomerEmail(customer.getEmail());

        // Publish event
        eventPublisher.publish(new ChargeEvent(
                ChargeEvent.EventType.CHARGE_CREATED,
                result,
                null,
                ChargeStatus.PENDING
        ));

        return result;
    }

    /**
     * Updates a charge with external ID and payment info from the proxy.
     */
    public ChargeDTO updateChargeFromProxy(Long chargeId, String externalId, 
                                           String pixCode, String boletoCode, String invoiceUrl) {
        logger.info("Updating charge {} with external ID: {}", chargeId, externalId);

        Charge charge = chargeRepository.findById(chargeId)
                .orElseThrow(() -> new ResourceNotFoundException("Charge", chargeId));

        ChargeStatus oldStatus = charge.getStatus();
        
        charge.setExternalId(externalId);
        charge.setPixCode(pixCode);
        charge.setBoletoCode(boletoCode);
        charge.setInvoiceUrl(invoiceUrl);
        charge.setStatus(ChargeStatus.REGISTERED);

        Charge updatedCharge = chargeRepository.update(charge);
        ChargeDTO result = toDTO(updatedCharge);

        // Get customer info
        CustomerDTO customer = customerService.getCustomerById(charge.getCustomerId());
        result.setCustomerName(customer.getName());
        result.setCustomerEmail(customer.getEmail());

        // Publish event if status changed
        if (oldStatus != ChargeStatus.REGISTERED) {
            eventPublisher.publish(new ChargeEvent(
                    ChargeEvent.EventType.STATUS_CHANGED,
                    result,
                    oldStatus,
                    ChargeStatus.REGISTERED
            ));
        }

        return result;
    }

    /**
     * Updates the status of a charge.
     */
    public ChargeDTO updateStatus(Long id, String newStatus) {
        logger.info("Updating charge {} status to: {}", id, newStatus);

        Charge charge = chargeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Charge", id));

        ChargeStatus oldStatus = charge.getStatus();
        ChargeStatus status = ChargeStatus.fromString(newStatus);

        // Validate status transition
        validateStatusTransition(oldStatus, status);

        charge.setStatus(status);
        Charge updatedCharge = chargeRepository.update(charge);
        ChargeDTO result = toDTO(updatedCharge);

        // Get customer info
        CustomerDTO customer = customerService.getCustomerById(charge.getCustomerId());
        result.setCustomerName(customer.getName());
        result.setCustomerEmail(customer.getEmail());

        // Publish event
        eventPublisher.publish(new ChargeEvent(
                ChargeEvent.EventType.STATUS_CHANGED,
                result,
                oldStatus,
                status
        ));

        logger.info("Charge {} status updated from {} to {}", id, oldStatus, status);
        return result;
    }

    /**
     * Updates the status of a charge by external ID.
     */
    public ChargeDTO updateStatusByExternalId(String externalId, String newStatus) {
        logger.info("Updating charge with external ID {} status to: {}", externalId, newStatus);

        Charge charge = chargeRepository.findByExternalId(externalId)
                .orElseThrow(() -> new ResourceNotFoundException("Charge with external ID: " + externalId));

        ChargeStatus oldStatus = charge.getStatus();
        ChargeStatus status = ChargeStatus.fromString(newStatus);

        // Validate status transition
        validateStatusTransition(oldStatus, status);

        charge.setStatus(status);
        Charge updatedCharge = chargeRepository.update(charge);
        ChargeDTO result = toDTO(updatedCharge);

        // Get customer info
        CustomerDTO customer = customerService.getCustomerById(charge.getCustomerId());
        result.setCustomerName(customer.getName());
        result.setCustomerEmail(customer.getEmail());

        // Publish event
        eventPublisher.publish(new ChargeEvent(
                ChargeEvent.EventType.STATUS_CHANGED,
                result,
                oldStatus,
                status
        ));

        logger.info("Charge {} status updated from {} to {}", charge.getId(), oldStatus, status);
        return result;
    }

    /**
     * Updates a charge with the provided fields.
     * 
     * Rules:
     * - value: can only be updated if status = PENDING
     * - dueDate: can be updated if status = PENDING or REGISTERED
     * - billingType: can only be updated if status = PENDING
     * - description: can always be updated
     * 
     * @param id the charge ID
     * @param updates the fields to update (null fields are ignored)
     * @return the updated charge
     */
    public ChargeDTO updateCharge(Long id, ChargeDTO updates) {
        logger.info("Updating charge: {}", id);

        Charge charge = chargeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Charge", id));

        ChargeStatus currentStatus = charge.getStatus();

        // Cannot update PAID or CANCELED charges (except description)
        if (currentStatus == ChargeStatus.PAID || currentStatus == ChargeStatus.CANCELED) {
            // Only description can be updated
            if (updates.getValue() != null || updates.getDueDate() != null || updates.getBillingType() != null) {
                throw new BusinessException("Cannot update a " + currentStatus.getDescription().toLowerCase() + " charge. Only description can be modified.");
            }
        }

        boolean hasChanges = false;

        // Update value - only if PENDING
        if (updates.getValue() != null) {
            if (currentStatus != ChargeStatus.PENDING) {
                throw new BusinessException("Value can only be updated when charge status is PENDING");
            }
            if (updates.getValue().compareTo(java.math.BigDecimal.ZERO) <= 0) {
                throw new BusinessException("Value must be greater than zero");
            }
            charge.setValue(updates.getValue());
            hasChanges = true;
            logger.debug("Updating value to: {}", updates.getValue());
        }

        // Update dueDate - only if PENDING or REGISTERED
        if (updates.getDueDate() != null) {
            if (currentStatus != ChargeStatus.PENDING && currentStatus != ChargeStatus.REGISTERED) {
                throw new BusinessException("Due date can only be updated when charge status is PENDING or REGISTERED");
            }
            charge.setDueDate(updates.getDueDate());
            hasChanges = true;
            logger.debug("Updating dueDate to: {}", updates.getDueDate());
        }

        // Update billingType - only if PENDING
        if (updates.getBillingType() != null) {
            if (currentStatus != ChargeStatus.PENDING) {
                throw new BusinessException("Billing type can only be updated when charge status is PENDING");
            }
            if (!isValidBillingType(updates.getBillingType())) {
                throw new BusinessException("Invalid billing type. Must be PIX, BOLETO, or CREDIT_CARD");
            }
            charge.setBillingType(updates.getBillingType().toUpperCase());
            hasChanges = true;
            logger.debug("Updating billingType to: {}", updates.getBillingType());
        }

        // Update description - always allowed
        if (updates.getDescription() != null) {
            charge.setDescription(updates.getDescription());
            hasChanges = true;
            logger.debug("Updating description");
        }

        if (!hasChanges) {
            logger.warn("No changes provided for charge {}", id);
            throw new BusinessException("No valid fields provided for update");
        }

        // Save changes
        Charge updatedCharge = chargeRepository.update(charge);
        ChargeDTO result = toDTO(updatedCharge);

        // Get customer info
        CustomerDTO customer = customerService.getCustomerById(charge.getCustomerId());
        result.setCustomerName(customer.getName());
        result.setCustomerEmail(customer.getEmail());

        // Publish event
        eventPublisher.publish(new ChargeEvent(
                ChargeEvent.EventType.CHARGE_UPDATED,
                result,
                currentStatus,
                currentStatus
        ));

        logger.info("Charge {} updated successfully", id);
        return result;
    }

    /**
     * Cancels a charge.
     */
    public ChargeDTO cancelCharge(Long id) {
        logger.info("Cancelling charge: {}", id);

        Charge charge = chargeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Charge", id));

        if (charge.getStatus() == ChargeStatus.PAID) {
            throw new BusinessException("Cannot cancel a paid charge");
        }

        if (charge.getStatus() == ChargeStatus.CANCELED) {
            throw new BusinessException("Charge is already canceled");
        }

        ChargeStatus oldStatus = charge.getStatus();
        charge.setStatus(ChargeStatus.CANCELED);
        Charge updatedCharge = chargeRepository.update(charge);
        ChargeDTO result = toDTO(updatedCharge);

        // Get customer info
        CustomerDTO customer = customerService.getCustomerById(charge.getCustomerId());
        result.setCustomerName(customer.getName());
        result.setCustomerEmail(customer.getEmail());

        // Publish event
        eventPublisher.publish(new ChargeEvent(
                ChargeEvent.EventType.CHARGE_CANCELED,
                result,
                oldStatus,
                ChargeStatus.CANCELED
        ));

        logger.info("Charge {} canceled", id);
        return result;
    }

    /**
     * Gets a charge by ID.
     */
    public ChargeDTO getChargeById(Long id) {
        logger.debug("Fetching charge with ID: {}", id);

        Charge charge = chargeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Charge", id));

        ChargeDTO result = toDTO(charge);

        // Get customer info
        CustomerDTO customer = customerService.getCustomerById(charge.getCustomerId());
        result.setCustomerName(customer.getName());
        result.setCustomerEmail(customer.getEmail());

        return result;
    }

    /**
     * Gets a charge by external ID.
     */
    public ChargeDTO getChargeByExternalId(String externalId) {
        logger.debug("Fetching charge with external ID: {}", externalId);

        Charge charge = chargeRepository.findByExternalId(externalId)
                .orElseThrow(() -> new ResourceNotFoundException("Charge with external ID: " + externalId));

        ChargeDTO result = toDTO(charge);

        // Get customer info
        CustomerDTO customer = customerService.getCustomerById(charge.getCustomerId());
        result.setCustomerName(customer.getName());
        result.setCustomerEmail(customer.getEmail());

        return result;
    }

    /**
     * Gets all charges for a customer.
     */
    public List<ChargeDTO> getChargesByCustomerId(Long customerId) {
        logger.debug("Fetching charges for customer: {}", customerId);

        // Validate customer exists
        CustomerDTO customer = customerService.getCustomerById(customerId);

        return chargeRepository.findByCustomerId(customerId)
                .stream()
                .map(charge -> {
                    ChargeDTO dto = toDTO(charge);
                    dto.setCustomerName(customer.getName());
                    dto.setCustomerEmail(customer.getEmail());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    /**
     * Gets all charges.
     */
    public List<ChargeDTO> getAllCharges() {
        logger.debug("Fetching all charges");

        return chargeRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Validates billing type.
     */
    private boolean isValidBillingType(String billingType) {
        return "PIX".equalsIgnoreCase(billingType) ||
               "BOLETO".equalsIgnoreCase(billingType) ||
               "CREDIT_CARD".equalsIgnoreCase(billingType);
    }

    /**
     * Validates status transition.
     */
    private void validateStatusTransition(ChargeStatus from, ChargeStatus to) {
        // PAID and CANCELED are final states
        if (from == ChargeStatus.PAID && to != ChargeStatus.PAID) {
            throw new BusinessException("Cannot change status of a paid charge");
        }
        if (from == ChargeStatus.CANCELED && to != ChargeStatus.CANCELED) {
            throw new BusinessException("Cannot change status of a canceled charge");
        }
    }

    /**
     * Converts entity to DTO.
     */
    private ChargeDTO toDTO(Charge charge) {
        ChargeDTO dto = new ChargeDTO();
        dto.setId(charge.getId());
        dto.setCustomerId(charge.getCustomerId());
        dto.setExternalId(charge.getExternalId());
        dto.setValue(charge.getValue());
        dto.setDueDate(charge.getDueDate());
        dto.setBillingType(charge.getBillingType());
        dto.setStatus(charge.getStatus().name());
        dto.setDescription(charge.getDescription());
        dto.setPixCode(charge.getPixCode());
        dto.setBoletoCode(charge.getBoletoCode());
        dto.setInvoiceUrl(charge.getInvoiceUrl());
        dto.setCreatedAt(charge.getCreatedAt());
        dto.setUpdatedAt(charge.getUpdatedAt());
        return dto;
    }

    /**
     * Converts DTO to entity.
     */
    private Charge toEntity(ChargeDTO dto) {
        Charge charge = new Charge();
        charge.setId(dto.getId());
        charge.setCustomerId(dto.getCustomerId());
        charge.setExternalId(dto.getExternalId());
        charge.setValue(dto.getValue());
        charge.setDueDate(dto.getDueDate());
        charge.setBillingType(dto.getBillingType().toUpperCase());
        charge.setDescription(dto.getDescription());
        charge.setPixCode(dto.getPixCode());
        charge.setBoletoCode(dto.getBoletoCode());
        charge.setInvoiceUrl(dto.getInvoiceUrl());
        return charge;
    }
}
