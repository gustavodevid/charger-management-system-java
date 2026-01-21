package com.dac.chargemanager.api.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Health check servlet for monitoring application status.
 * Uses Spring autowiring for DataSource injection.
 * Mapped via web.xml to /health
 */
public class HealthServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(HealthServlet.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private DataSource dataSource;

    @Override
    public void init() throws ServletException {
        super.init();
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Ensure injection in case init wasn't called properly
        if (dataSource == null) {
            SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        }
        
        logger.debug("Health check requested");

        Map<String, Object> health = new LinkedHashMap<>();
        health.put("service", "charge-manager");
        health.put("framework", "Spring Framework");
        health.put("timestamp", LocalDateTime.now().toString());

        boolean databaseUp = checkDatabase();
        health.put("database", databaseUp ? "UP" : "DOWN");
        health.put("status", databaseUp ? "UP" : "DOWN");

        Map<String, String> endpoints = new LinkedHashMap<>();
        endpoints.put("soap-customer", "/ws/customer");
        endpoints.put("soap-charge", "/ws/charge");
        endpoints.put("wsdl-customer", "/ws/customer?wsdl");
        endpoints.put("wsdl-charge", "/ws/charge?wsdl");
        health.put("endpoints", endpoints);

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        
        if (databaseUp) {
            resp.setStatus(HttpServletResponse.SC_OK);
        } else {
            resp.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        }

        objectMapper.writeValue(resp.getWriter(), health);
    }

    private boolean checkDatabase() {
        try {
            if (dataSource == null) {
                logger.warn("DataSource not injected");
                return false;
            }
            try (Connection conn = dataSource.getConnection()) {
                conn.createStatement().execute("SELECT 1");
                return true;
            }
        } catch (Exception e) {
            logger.warn("Database health check failed: {}", e.getMessage());
            return false;
        }
    }
}
