package com.dac.chargemanager.business.event;

import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Publisher for charge events (Observable in Observer pattern).
 * Spring-managed component with automatic listener registration.
 */
@Component
public class ChargeEventPublisher {

    private static final Logger logger = LoggerFactory.getLogger(ChargeEventPublisher.class);

    private final List<ChargeEventListener> listeners;
    private final ExecutorService executorService;
    private final boolean async;

    public ChargeEventPublisher() {
        this(true); // Async by default
    }

    public ChargeEventPublisher(boolean async) {
        this.listeners = new ArrayList<>();
        this.async = async;
        if (async) {
            this.executorService = Executors.newFixedThreadPool(2);
        } else {
            this.executorService = null;
        }
    }

    /**
     * Automatically registers all ChargeEventListener beans.
     * This method is called by Spring after all beans are created.
     */
    @Autowired(required = false)
    public void registerListeners(List<ChargeEventListener> eventListeners) {
        if (eventListeners != null) {
            for (ChargeEventListener listener : eventListeners) {
                addListener(listener);
            }
            logger.info("Registered {} event listeners via Spring autowiring", eventListeners.size());
        }
    }

    /**
     * Registers a listener to receive charge events.
     * 
     * @param listener the listener to register
     */
    public void addListener(ChargeEventListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
            logger.debug("Listener registered: {}", listener.getClass().getSimpleName());
        }
    }

    /**
     * Removes a listener.
     * 
     * @param listener the listener to remove
     */
    public void removeListener(ChargeEventListener listener) {
        listeners.remove(listener);
        logger.debug("Listener removed: {}", listener.getClass().getSimpleName());
    }

    /**
     * Publishes a charge event to all registered listeners.
     * 
     * @param event the event to publish
     */
    public void publish(ChargeEvent event) {
        logger.info("Publishing event: {}", event);

        if (listeners.isEmpty()) {
            logger.debug("No listeners registered for event");
            return;
        }

        for (ChargeEventListener listener : listeners) {
            if (async && executorService != null) {
                executorService.submit(() -> notifyListener(listener, event));
            } else {
                notifyListener(listener, event);
            }
        }
    }

    private void notifyListener(ChargeEventListener listener, ChargeEvent event) {
        try {
            listener.onChargeEvent(event);
        } catch (Exception e) {
            logger.error("Error notifying listener {}: {}", 
                    listener.getClass().getSimpleName(), e.getMessage(), e);
        }
    }

    /**
     * Shuts down the executor service.
     * Called automatically by Spring on application shutdown.
     */
    @PreDestroy
    public void shutdown() {
        if (executorService != null) {
            executorService.shutdown();
            logger.info("ChargeEventPublisher shutdown");
        }
    }

    /**
     * Returns the number of registered listeners.
     */
    public int getListenerCount() {
        return listeners.size();
    }
}
