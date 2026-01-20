package com.dac.chargemanager.business.event;

import com.dac.chargemanager.business.dto.ChargeDTO;
import com.dac.chargemanager.business.service.EmailService;
import com.dac.chargemanager.infra.entity.ChargeStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Listener that sends email notifications when charge events occur.
 */
public class EmailNotificationListener implements ChargeEventListener {

    private static final Logger logger = LoggerFactory.getLogger(EmailNotificationListener.class);

    private final EmailService emailService;

    public EmailNotificationListener(EmailService emailService) {
        this.emailService = emailService;
    }

    @Override
    public void onChargeEvent(ChargeEvent event) {
        logger.debug("Received charge event: {}", event);

        ChargeDTO charge = event.getCharge();
        if (charge == null) {
            logger.warn("Charge is null in event, skipping notification");
            return;
        }

        switch (event.getEventType()) {
            case CHARGE_CREATED:
                handleChargeCreated(charge);
                break;
            case STATUS_CHANGED:
                handleStatusChanged(charge, event.getPreviousStatus(), event.getNewStatus());
                break;
            case CHARGE_CANCELED:
                handleChargeCanceled(charge, event.getPreviousStatus());
                break;
            case CHARGE_PAID:
                handleChargePaid(charge, event.getPreviousStatus());
                break;
            default:
                logger.warn("Unknown event type: {}", event.getEventType());
        }
    }

    private void handleChargeCreated(ChargeDTO charge) {
        logger.info("Handling CHARGE_CREATED event for charge {}", charge.getId());
        emailService.sendChargeCreatedNotification(charge);
    }

    private void handleStatusChanged(ChargeDTO charge, ChargeStatus oldStatus, ChargeStatus newStatus) {
        logger.info("Handling STATUS_CHANGED event for charge {} - {} -> {}", 
                charge.getId(), oldStatus, newStatus);
        
        // Send notification for specific status changes
        if (newStatus == ChargeStatus.PAID) {
            emailService.sendChargeStatusNotification(charge, oldStatus, newStatus);
        } else if (newStatus == ChargeStatus.REGISTERED) {
            emailService.sendChargeStatusNotification(charge, oldStatus, newStatus);
        } else if (newStatus == ChargeStatus.CANCELED) {
            emailService.sendChargeStatusNotification(charge, oldStatus, newStatus);
        }
    }

    private void handleChargeCanceled(ChargeDTO charge, ChargeStatus oldStatus) {
        logger.info("Handling CHARGE_CANCELED event for charge {}", charge.getId());
        emailService.sendChargeStatusNotification(charge, oldStatus, ChargeStatus.CANCELED);
    }

    private void handleChargePaid(ChargeDTO charge, ChargeStatus oldStatus) {
        logger.info("Handling CHARGE_PAID event for charge {}", charge.getId());
        emailService.sendChargeStatusNotification(charge, oldStatus, ChargeStatus.PAID);
    }
}
