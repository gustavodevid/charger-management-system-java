package com.dac.chargemanager.business.event;

import com.dac.chargemanager.business.dto.ChargeDTO;
import com.dac.chargemanager.infra.entity.ChargeStatus;

import java.time.LocalDateTime;

/**
 * Event class representing a charge state change.
 */
public class ChargeEvent {

    public enum EventType {
        CHARGE_CREATED,
        CHARGE_UPDATED,
        STATUS_CHANGED,
        CHARGE_CANCELED,
        CHARGE_PAID
    }

    private final EventType eventType;
    private final ChargeDTO charge;
    private final ChargeStatus previousStatus;
    private final ChargeStatus newStatus;
    private final LocalDateTime timestamp;

    public ChargeEvent(EventType eventType, ChargeDTO charge, ChargeStatus previousStatus, ChargeStatus newStatus) {
        this.eventType = eventType;
        this.charge = charge;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.timestamp = LocalDateTime.now();
    }

    public EventType getEventType() {
        return eventType;
    }

    public ChargeDTO getCharge() {
        return charge;
    }

    public ChargeStatus getPreviousStatus() {
        return previousStatus;
    }

    public ChargeStatus getNewStatus() {
        return newStatus;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "ChargeEvent{" +
                "eventType=" + eventType +
                ", chargeId=" + (charge != null ? charge.getId() : null) +
                ", previousStatus=" + previousStatus +
                ", newStatus=" + newStatus +
                ", timestamp=" + timestamp +
                '}';
    }
}
