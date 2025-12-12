package com.dac.chargeproxy.config;

import com.dac.chargeproxy.soap.ChargeProxyService;
import jakarta.xml.ws.Endpoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for publishing the SOAP web service endpoint.
 */
@Configuration
public class WebServiceConfig {

    private final ChargeProxyService chargeProxyService;

    public WebServiceConfig(ChargeProxyService chargeProxyService) {
        this.chargeProxyService = chargeProxyService;
    }

    /**
     * Publishes the SOAP endpoint at /ws/charge.
     * The WSDL will be available at /ws/charge?wsdl
     */
    @Bean
    public Endpoint chargeProxyEndpoint() {
        Endpoint endpoint = Endpoint.publish("http://0.0.0.0:8082/ws/charge", chargeProxyService);
        return endpoint;
    }
}

