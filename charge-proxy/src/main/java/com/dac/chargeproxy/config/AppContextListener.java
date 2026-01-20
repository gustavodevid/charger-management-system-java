package com.dac.chargeproxy.config;

import com.dac.chargeproxy.client.AsaasClient;
import com.dac.chargeproxy.soap.ChargeProxyServiceImpl;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Application context listener for initializing and destroying application resources.
 */
@WebListener
public class AppContextListener implements ServletContextListener {

    private static final Logger logger = LoggerFactory.getLogger(AppContextListener.class);

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        logger.info("===========================================");
        logger.info("Charge Proxy Application Starting...");
        logger.info("===========================================");

        try {
            // Initialize configuration
            AppConfig config = AppConfig.getInstance();

            // Initialize ASAAS client with mode (REAL or STUB)
            AsaasClient asaasClient = new AsaasClient(
                    config.getAsaasApiUrl(),
                    config.getAsaasAccessToken(),
                    config.getAsaasApiMode()
            );

            // Initialize SOAP service
            ChargeProxyServiceImpl soapService = new ChargeProxyServiceImpl(asaasClient);

            // Store in servlet context
            ServletContext ctx = sce.getServletContext();
            ctx.setAttribute("asaasClient", asaasClient);
            ctx.setAttribute("chargeProxyService", soapService);

            // Store singleton reference for SOAP service
            ServiceLocator.setAsaasClient(asaasClient);

            logger.info("===========================================");
            logger.info("Charge Proxy Application Started!");
            logger.info("Endpoints:");
            logger.info("  - SOAP:    /ws/charge");
            logger.info("  - Webhook: /webhook");
            logger.info("  - Health:  /health");
            logger.info("ASAAS API URL: {}", config.getAsaasApiUrl());
            logger.info("ASAAS API Mode: {}", config.getAsaasApiMode());
            logger.info("===========================================");

        } catch (Exception e) {
            logger.error("Failed to initialize application", e);
            throw new RuntimeException("Application initialization failed", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        logger.info("Charge Proxy Application Shutting Down...");

        // Close HTTP client
        try {
            AsaasClient client = ServiceLocator.getAsaasClient();
            if (client != null) {
                client.close();
            }
        } catch (IllegalStateException e) {
            // Client may not have been initialized
            logger.debug("AsaasClient was not initialized: {}", e.getMessage());
        }

        logger.info("Charge Proxy Application Stopped.");
    }
}
