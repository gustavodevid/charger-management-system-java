package com.dac.chargemanager.api.servlet;

import com.dac.chargemanager.infra.config.ServiceLocator;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Health check servlet for monitoring application status.
 */
@WebServlet(name = "HealthServlet", urlPatterns = {"/health", "/actuator/health"})
public class HealthServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(HealthServlet.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        logger.debug("Health check requested");

        Map<String, Object> health = new LinkedHashMap<>();
        health.put("service", "charge-manager");
        health.put("timestamp", LocalDateTime.now().toString());

        boolean databaseUp = checkDatabase();
        health.put("database", databaseUp ? "UP" : "DOWN");
        health.put("status", databaseUp ? "UP" : "DOWN");

        Map<String, String> endpoints = new LinkedHashMap<>();
        endpoints.put("soap", "/ws/customer");
        endpoints.put("wsdl", "/ws/customer?wsdl");
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
            DataSource dataSource = ServiceLocator.getDataSource();
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
