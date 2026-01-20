package com.dac.chargeproxy.config;

import com.dac.chargeproxy.client.AsaasClient;

/**
 * Service Locator pattern for accessing shared services.
 * Used by JAX-WS endpoints that cannot use constructor injection.
 */
public class ServiceLocator {

    private static AsaasClient asaasClient;

    private ServiceLocator() {
        // Utility class
    }

    public static AsaasClient getAsaasClient() {
        if (asaasClient == null) {
            throw new IllegalStateException("AsaasClient not initialized. Application may not have started properly.");
        }
        return asaasClient;
    }

    public static void setAsaasClient(AsaasClient client) {
        asaasClient = client;
    }
}
