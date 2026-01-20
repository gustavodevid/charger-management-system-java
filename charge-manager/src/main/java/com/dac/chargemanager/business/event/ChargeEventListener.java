package com.dac.chargemanager.business.event;

/**
 * Observer interface for charge events.
 */
public interface ChargeEventListener {

    /**
     * Called when a charge event occurs.
     * 
     * @param event the charge event
     */
    void onChargeEvent(ChargeEvent event);
}
