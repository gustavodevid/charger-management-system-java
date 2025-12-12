package com.dac.chargeproxy.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * REST Health check controller for the Charge Proxy service.
 */
@RestController
@RequestMapping("/api/health")
public class HealthController {

    @GetMapping
    public Map<String, Object> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "charge-proxy");
        health.put("timestamp", LocalDateTime.now());
        health.put("soapEndpoint", "http://localhost:8082/ws/charge");
        health.put("wsdlUrl", "http://localhost:8082/ws/charge?wsdl");
        return health;
    }
}

