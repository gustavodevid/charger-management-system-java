package com.dac.chargemanager.api.config;

import com.dac.chargemanager.api.soap.CustomerSoapService;
import jakarta.xml.ws.Endpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for publishing the SOAP web service endpoint.
 */
@Configuration
public class SoapConfig {

    private static final Logger logger = LoggerFactory.getLogger(SoapConfig.class);

    private final CustomerSoapService customerSoapService;

    @Value("${soap.endpoint.port:8081}")
    private int soapPort;

    public SoapConfig(CustomerSoapService customerSoapService) {
        this.customerSoapService = customerSoapService;
    }

    /**
     * Publishes the Customer SOAP endpoint.
     * The WSDL will be available at http://localhost:8081/ws/customer?wsdl
     */
    @Bean
    public Endpoint customerEndpoint() {
        String address = "http://0.0.0.0:" + soapPort + "/ws/customer";
        logger.info("Publishing SOAP endpoint at: {}", address);
        logger.info("WSDL available at: {}?wsdl", address);
        Endpoint endpoint = Endpoint.publish(address, customerSoapService);
        return endpoint;
    }
}

