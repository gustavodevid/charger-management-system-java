package com.dac.chargeproxy.servlet;

import com.dac.chargeproxy.model.WebhookPayload;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.stream.Collectors;

/**
 * Webhook endpoint for receiving ASAAS payment notifications.
 * 
 * Receives POST requests from ASAAS and forwards status updates
 * to the Charge Manager service via SOAP.
 * 
 * Mapped via web.xml to /webhook
 */
public class WebhookServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(WebhookServlet.class);

    @Value("${chargemanager.soap.url:http://charge-manager:8080/charge-manager/ws/charge}")
    private String chargeManagerUrl;

    @Value("${webhook.auth.token:}")
    private String webhookToken;

    private HttpClient httpClient;

    @Override
    public void init() throws ServletException {
        super.init();
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
        
        // Override with environment variables if present
        String envUrl = System.getenv("CHARGE_MANAGER_SOAP_URL");
        if (envUrl != null && !envUrl.isEmpty()) {
            this.chargeManagerUrl = envUrl;
        } else if (chargeManagerUrl == null || chargeManagerUrl.isEmpty()) {
            this.chargeManagerUrl = "http://charge-manager:8080/charge-manager/ws/charge";
        }
        
        String envToken = System.getenv("WEBHOOK_AUTH_TOKEN");
        if (envToken != null && !envToken.isEmpty()) {
            this.webhookToken = envToken;
        }
        
        logger.info("WebhookServlet initialized - Manager URL: {}", chargeManagerUrl);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        logger.info("Webhook received from: {}", request.getRemoteAddr());

        // Validate Bearer token if configured
        if (!validateToken(request)) {
            logger.warn("Unauthorized webhook request");
            sendResponse(response, HttpServletResponse.SC_UNAUTHORIZED, 
                    "{\"error\": \"Unauthorized\"}");
            return;
        }

        // Read request body
        String requestBody = readRequestBody(request);
        logger.debug("Webhook payload: {}", requestBody);

        try {
            // Parse webhook payload
            WebhookPayload payload = parsePayload(requestBody);
            
            if (payload == null || payload.getPayment() == null) {
                logger.warn("Invalid webhook payload");
                sendResponse(response, HttpServletResponse.SC_BAD_REQUEST, 
                        "{\"error\": \"Invalid payload\"}");
                return;
            }

            logger.info("Processing webhook event: {} for payment: {}", 
                    payload.getEvent(), payload.getPayment().getId());

            // Forward to Charge Manager
            boolean success = forwardToChargeManager(payload);

            if (success) {
                logger.info("Webhook processed successfully for payment: {}", 
                        payload.getPayment().getId());
                sendResponse(response, HttpServletResponse.SC_OK, 
                        "{\"status\": \"processed\"}");
            } else {
                logger.error("Failed to forward webhook to Charge Manager");
                sendResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                        "{\"error\": \"Failed to process webhook\"}");
            }

        } catch (Exception e) {
            logger.error("Error processing webhook", e);
            sendResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                    "{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // Health check for webhook endpoint
        sendResponse(response, HttpServletResponse.SC_OK, 
                "{\"status\": \"Webhook endpoint active\", \"timestamp\": \"" + 
                java.time.LocalDateTime.now() + "\"}");
    }

    private boolean validateToken(HttpServletRequest request) {
        if (webhookToken == null || webhookToken.isEmpty()) {
            // No token configured, accept all requests
            return true;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // Also check for asaas-access-token header
            String asaasToken = request.getHeader("asaas-access-token");
            return webhookToken.equals(asaasToken);
        }

        String token = authHeader.substring(7);
        return webhookToken.equals(token);
    }

    private String readRequestBody(HttpServletRequest request) throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(request.getInputStream(), StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }

    private WebhookPayload parsePayload(String json) {
        // Simple JSON parsing without external library
        try {
            WebhookPayload payload = new WebhookPayload();
            WebhookPayload.PaymentData payment = new WebhookPayload.PaymentData();

            payload.setEvent(extractJsonValue(json, "event"));

            // Parse payment object
            String paymentJson = extractJsonObject(json, "payment");
            if (paymentJson != null) {
                payment.setId(extractJsonValue(paymentJson, "id"));
                payment.setStatus(extractJsonValue(paymentJson, "status"));
                payment.setCustomer(extractJsonValue(paymentJson, "customer"));
                payment.setBillingType(extractJsonValue(paymentJson, "billingType"));
                payment.setInvoiceUrl(extractJsonValue(paymentJson, "invoiceUrl"));
                payment.setBankSlipUrl(extractJsonValue(paymentJson, "bankSlipUrl"));
                payment.setPixQrCodePayload(extractJsonValue(paymentJson, "pixQrCodePayload"));
                payment.setNossoNumero(extractJsonValue(paymentJson, "nossoNumero"));

                String valueStr = extractJsonValue(paymentJson, "value");
                if (valueStr != null && !valueStr.isEmpty()) {
                    try {
                        payment.setValue(new java.math.BigDecimal(valueStr));
                    } catch (NumberFormatException e) {
                        logger.warn("Invalid value in payload: {}", valueStr);
                    }
                }

                payload.setPayment(payment);
            }

            return payload;

        } catch (Exception e) {
            logger.error("Error parsing webhook payload", e);
            return null;
        }
    }

    private String extractJsonValue(String json, String key) {
        String pattern = "\"" + key + "\"\\s*:\\s*";
        int keyIndex = json.indexOf("\"" + key + "\"");
        if (keyIndex == -1) return null;

        int colonIndex = json.indexOf(":", keyIndex);
        if (colonIndex == -1) return null;

        int valueStart = colonIndex + 1;
        while (valueStart < json.length() && Character.isWhitespace(json.charAt(valueStart))) {
            valueStart++;
        }

        if (valueStart >= json.length()) return null;

        char startChar = json.charAt(valueStart);
        if (startChar == '"') {
            // String value
            int valueEnd = json.indexOf("\"", valueStart + 1);
            if (valueEnd == -1) return null;
            return json.substring(valueStart + 1, valueEnd);
        } else if (startChar == '{' || startChar == '[') {
            // Object or array - handled separately
            return null;
        } else {
            // Number or boolean
            int valueEnd = valueStart;
            while (valueEnd < json.length() && 
                   !Character.isWhitespace(json.charAt(valueEnd)) &&
                   json.charAt(valueEnd) != ',' &&
                   json.charAt(valueEnd) != '}') {
                valueEnd++;
            }
            return json.substring(valueStart, valueEnd);
        }
    }

    private String extractJsonObject(String json, String key) {
        String searchKey = "\"" + key + "\"";
        int keyIndex = json.indexOf(searchKey);
        if (keyIndex == -1) return null;

        int colonIndex = json.indexOf(":", keyIndex);
        if (colonIndex == -1) return null;

        int objectStart = json.indexOf("{", colonIndex);
        if (objectStart == -1) return null;

        int depth = 1;
        int objectEnd = objectStart + 1;
        while (depth > 0 && objectEnd < json.length()) {
            char c = json.charAt(objectEnd);
            if (c == '{') depth++;
            else if (c == '}') depth--;
            objectEnd++;
        }

        return json.substring(objectStart, objectEnd);
    }

    private boolean forwardToChargeManager(WebhookPayload payload) {
        try {
            String externalId = payload.getPayment().getId();
            String asaasStatus = payload.getPayment().getStatus();
            String internalStatus = WebhookPayload.mapAsaasStatus(asaasStatus);

            logger.info("Forwarding status update - externalId: {}, asaasStatus: {}, internalStatus: {}", 
                    externalId, asaasStatus, internalStatus);

            // Create SOAP request to update charge status
            String soapRequest = buildUpdateStatusSoapRequest(externalId, internalStatus);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(chargeManagerUrl))
                    .header("Content-Type", "text/xml; charset=utf-8")
                    .header("SOAPAction", "updateChargeStatusByExternalId")
                    .POST(HttpRequest.BodyPublishers.ofString(soapRequest))
                    .timeout(Duration.ofSeconds(30))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            logger.debug("Charge Manager response: {}", response.body());

            return response.statusCode() == 200;

        } catch (Exception e) {
            logger.error("Error forwarding to Charge Manager", e);
            return false;
        }
    }

    private String buildUpdateStatusSoapRequest(String externalId, String status) {
        return """
            <?xml version="1.0" encoding="UTF-8"?>
            <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/"
                           xmlns:cm="http://chargemanager.dac.com/soap">
                <soap:Header/>
                <soap:Body>
                    <cm:updateChargeStatusByExternalId>
                        <cm:externalId>%s</cm:externalId>
                        <cm:status>%s</cm:status>
                    </cm:updateChargeStatusByExternalId>
                </soap:Body>
            </soap:Envelope>
            """.formatted(externalId, status);
    }

    private void sendResponse(HttpServletResponse response, int statusCode, String body) 
            throws IOException {
        response.setStatus(statusCode);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        out.print(body);
        out.flush();
    }
}
