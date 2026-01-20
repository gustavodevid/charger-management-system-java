package com.dac.chargemanager.infra.config;

import com.dac.chargemanager.business.event.ChargeEventPublisher;
import com.dac.chargemanager.business.event.EmailNotificationListener;
import com.dac.chargemanager.business.service.ChargeService;
import com.dac.chargemanager.business.service.CustomerService;
import com.dac.chargemanager.business.service.EmailService;
import com.dac.chargemanager.infra.client.ChargeProxyClient;
import com.dac.chargemanager.infra.repository.ChargeRepository;
import com.dac.chargemanager.infra.repository.CustomerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

/**
 * Service Locator pattern for accessing shared services.
 * Used by JAX-WS endpoints that cannot use constructor injection.
 */
public class ServiceLocator {

    private static final Logger logger = LoggerFactory.getLogger(ServiceLocator.class);

    private static DataSource dataSource;
    
    // Repositories
    private static CustomerRepository customerRepository;
    private static ChargeRepository chargeRepository;
    
    // Services
    private static CustomerService customerService;
    private static ChargeService chargeService;
    private static EmailService emailService;
    
    // Events
    private static ChargeEventPublisher chargeEventPublisher;
    
    // Clients
    private static ChargeProxyClient chargeProxyClient;

    private ServiceLocator() {
        // Utility class
    }

    /**
     * Initializes all services. Should be called during application startup.
     */
    public static void initialize(DataSource ds) {
        logger.info("Initializing ServiceLocator...");
        
        dataSource = ds;
        
        // Initialize Repositories
        customerRepository = new CustomerRepository(dataSource);
        chargeRepository = new ChargeRepository(dataSource);
        logger.debug("Repositories initialized");
        
        // Initialize Event Publisher
        chargeEventPublisher = new ChargeEventPublisher(true);
        logger.debug("Event Publisher initialized");
        
        // Initialize Services
        customerService = new CustomerService(customerRepository);
        emailService = new EmailService();
        chargeService = new ChargeService(chargeRepository, customerService, chargeEventPublisher);
        logger.debug("Services initialized");
        
        // Initialize Clients
        String proxyUrl = getProxyUrl();
        chargeProxyClient = new ChargeProxyClient(proxyUrl);
        logger.debug("Clients initialized");
        
        // Register Event Listeners
        EmailNotificationListener emailListener = new EmailNotificationListener(emailService);
        chargeEventPublisher.addListener(emailListener);
        logger.debug("Event Listeners registered");
        
        logger.info("ServiceLocator initialized successfully");
    }

    /**
     * Shuts down services. Should be called during application shutdown.
     */
    public static void shutdown() {
        logger.info("Shutting down ServiceLocator...");
        if (chargeEventPublisher != null) {
            chargeEventPublisher.shutdown();
        }
        logger.info("ServiceLocator shutdown complete");
    }

    private static String getProxyUrl() {
        String proxyHost = System.getenv("CHARGE_PROXY_HOST");
        String proxyPort = System.getenv("CHARGE_PROXY_PORT");
        
        if (proxyHost == null || proxyHost.isEmpty()) {
            proxyHost = "charge-proxy";
        }
        if (proxyPort == null || proxyPort.isEmpty()) {
            proxyPort = "8080";
        }
        
        return "http://" + proxyHost + ":" + proxyPort + "/charge-proxy/ws/charge";
    }

    // Getters

    public static DataSource getDataSource() {
        if (dataSource == null) {
            throw new IllegalStateException("DataSource not initialized. Application may not have started properly.");
        }
        return dataSource;
    }

    public static CustomerRepository getCustomerRepository() {
        if (customerRepository == null) {
            throw new IllegalStateException("CustomerRepository not initialized.");
        }
        return customerRepository;
    }

    public static ChargeRepository getChargeRepository() {
        if (chargeRepository == null) {
            throw new IllegalStateException("ChargeRepository not initialized.");
        }
        return chargeRepository;
    }

    public static CustomerService getCustomerService() {
        if (customerService == null) {
            throw new IllegalStateException("CustomerService not initialized. Application may not have started properly.");
        }
        return customerService;
    }

    public static ChargeService getChargeService() {
        if (chargeService == null) {
            throw new IllegalStateException("ChargeService not initialized.");
        }
        return chargeService;
    }

    public static EmailService getEmailService() {
        if (emailService == null) {
            throw new IllegalStateException("EmailService not initialized.");
        }
        return emailService;
    }

    public static ChargeEventPublisher getChargeEventPublisher() {
        if (chargeEventPublisher == null) {
            throw new IllegalStateException("ChargeEventPublisher not initialized.");
        }
        return chargeEventPublisher;
    }

    public static ChargeProxyClient getChargeProxyClient() {
        if (chargeProxyClient == null) {
            throw new IllegalStateException("ChargeProxyClient not initialized.");
        }
        return chargeProxyClient;
    }

    // Legacy setters for backward compatibility
    
    public static void setCustomerService(CustomerService service) {
        customerService = service;
    }

    public static void setDataSource(DataSource ds) {
        dataSource = ds;
    }
}
