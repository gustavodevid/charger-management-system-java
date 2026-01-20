package com.dac.chargeproxy.business;

import com.dac.chargeproxy.soap.model.ChargeRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

/**
 * Business rules for the Charge Proxy.
 * These rules are specific to the internal system, not ASAAS rules.
 */
public class ProxyBusinessRules {

    private static final Logger logger = LoggerFactory.getLogger(ProxyBusinessRules.class);

    // Value constraints
    public static final BigDecimal MIN_VALUE = new BigDecimal("5.00");
    public static final BigDecimal MAX_VALUE = new BigDecimal("100000.00");

    // Due date constraints
    public static final int MAX_DUE_DATE_DAYS = 60;

    // Valid billing types
    private static final String[] VALID_BILLING_TYPES = {"PIX", "BOLETO", "CREDIT_CARD"};

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private ProxyBusinessRules() {
        // Utility class
    }

    /**
     * Validates a charge request according to system business rules.
     * 
     * @param request the charge request to validate
     * @throws ProxyBusinessException if validation fails
     */
    public static void validateChargeRequest(ChargeRequest request) {
        logger.debug("Validating charge request: customerId={}, value={}, dueDate={}, billingType={}",
                request.getCustomerId(), request.getValue(), request.getDueDate(), request.getBillingType());

        // Validate customerId
        validateCustomerId(request.getCustomerId());

        // Validate value
        validateValue(request.getValue());

        // Validate due date
        validateDueDate(request.getDueDate());

        // Validate billing type
        validateBillingType(request.getBillingType());

        logger.debug("Charge request validation passed");
    }

    /**
     * Validates the customer ID.
     */
    private static void validateCustomerId(String customerId) {
        if (customerId == null || customerId.trim().isEmpty()) {
            throw new ProxyBusinessException("INVALID_CUSTOMER", "Customer ID is required");
        }
    }

    /**
     * Validates the charge value.
     */
    private static void validateValue(BigDecimal value) {
        if (value == null) {
            throw new ProxyBusinessException("INVALID_VALUE", "Value is required");
        }

        if (value.compareTo(MIN_VALUE) < 0) {
            throw new ProxyBusinessException("VALUE_TOO_LOW",
                    String.format("Value must be at least R$ %.2f. Provided: R$ %.2f", MIN_VALUE, value));
        }

        if (value.compareTo(MAX_VALUE) > 0) {
            throw new ProxyBusinessException("VALUE_TOO_HIGH",
                    String.format("Value cannot exceed R$ %.2f. Provided: R$ %.2f", MAX_VALUE, value));
        }
    }

    /**
     * Validates the due date.
     */
    private static void validateDueDate(String dueDateStr) {
        if (dueDateStr == null || dueDateStr.trim().isEmpty()) {
            throw new ProxyBusinessException("INVALID_DUE_DATE", "Due date is required");
        }

        LocalDate dueDate;
        try {
            dueDate = LocalDate.parse(dueDateStr, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new ProxyBusinessException("INVALID_DUE_DATE",
                    "Due date must be in format yyyy-MM-dd. Provided: " + dueDateStr);
        }

        LocalDate today = LocalDate.now();

        // Due date cannot be in the past
        if (dueDate.isBefore(today)) {
            throw new ProxyBusinessException("DUE_DATE_IN_PAST",
                    "Due date cannot be in the past. Provided: " + dueDateStr);
        }

        // Due date cannot be more than MAX_DUE_DATE_DAYS days in the future
        long daysUntilDue = ChronoUnit.DAYS.between(today, dueDate);
        if (daysUntilDue > MAX_DUE_DATE_DAYS) {
            throw new ProxyBusinessException("DUE_DATE_TOO_FAR",
                    String.format("Due date cannot be more than %d days in the future. Provided: %s (%d days)",
                            MAX_DUE_DATE_DAYS, dueDateStr, daysUntilDue));
        }
    }

    /**
     * Validates the billing type.
     */
    private static void validateBillingType(String billingType) {
        if (billingType == null || billingType.trim().isEmpty()) {
            throw new ProxyBusinessException("INVALID_BILLING_TYPE", "Billing type is required");
        }

        boolean isValid = false;
        for (String validType : VALID_BILLING_TYPES) {
            if (validType.equalsIgnoreCase(billingType)) {
                isValid = true;
                break;
            }
        }

        if (!isValid) {
            throw new ProxyBusinessException("INVALID_BILLING_TYPE",
                    "Billing type must be PIX, BOLETO, or CREDIT_CARD. Provided: " + billingType);
        }
    }

    /**
     * Maps ASAAS status to internal system status.
     * 
     * @param asaasStatus the status from ASAAS
     * @return the internal system status
     */
    public static String mapAsaasStatusToInternal(String asaasStatus) {
        if (asaasStatus == null) {
            return "PENDING";
        }

        String status = asaasStatus.toUpperCase();
        
        return switch (status) {
            // Pending states
            case "PENDING", "AWAITING_RISK_ANALYSIS" -> "PENDING";
            
            // Paid states
            case "RECEIVED", "CONFIRMED", "RECEIVED_IN_CASH" -> "PAID";
            
            // Overdue = still registered, waiting payment
            case "OVERDUE" -> "REGISTERED";
            
            // Canceled/refunded states
            case "REFUNDED", "REFUND_REQUESTED", "REFUND_IN_PROGRESS",
                 "CHARGEBACK_REQUESTED", "CHARGEBACK_DISPUTE", 
                 "AWAITING_CHARGEBACK_REVERSAL", "DUNNING_RECEIVED", 
                 "DUNNING_REQUESTED", "DELETED" -> "CANCELED";
            
            // Default to REGISTERED for unknown states
            default -> {
                logger.warn("Unknown ASAAS status: {}. Mapping to REGISTERED.", asaasStatus);
                yield "REGISTERED";
            }
        };
    }

    /**
     * Maps internal system status to ASAAS status (for requests).
     * 
     * @param internalStatus the internal system status
     * @return the ASAAS status
     */
    public static String mapInternalStatusToAsaas(String internalStatus) {
        if (internalStatus == null) {
            return "PENDING";
        }

        return switch (internalStatus.toUpperCase()) {
            case "PENDING" -> "PENDING";
            case "REGISTERED" -> "PENDING"; // ASAAS doesn't have REGISTERED, treat as PENDING
            case "PAID" -> "RECEIVED";
            case "CANCELED" -> "DELETED";
            default -> "PENDING";
        };
    }

    /**
     * Checks if a billing type is valid.
     */
    public static boolean isValidBillingType(String billingType) {
        if (billingType == null) {
            return false;
        }
        for (String validType : VALID_BILLING_TYPES) {
            if (validType.equalsIgnoreCase(billingType)) {
                return true;
            }
        }
        return false;
    }
}
