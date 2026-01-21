package com.dac.chargemanager.infra.client;

import jakarta.annotation.PreDestroy;
import jakarta.xml.soap.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * SOAP RPC Client for communicating with Charge Proxy service.
 * Uses SOAP RPC/Literal style for all operations.
 */
@Component
public class ChargeProxyClient {

    private static final Logger logger = LoggerFactory.getLogger(ChargeProxyClient.class);
    private static final String NAMESPACE = "http://chargeproxy.dac.com/soap";
    private static final String NAMESPACE_PREFIX = "ns";

    private final String proxyUrl;
    private final HttpClient httpClient;
    private final MessageFactory messageFactory;

    public ChargeProxyClient(@Value("${charge.proxy.url:}") String configuredUrl) {
        this.proxyUrl = buildProxyUrl(configuredUrl);
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
        try {
            this.messageFactory = MessageFactory.newInstance();
        } catch (SOAPException e) {
            throw new RuntimeException("Failed to create SOAP MessageFactory", e);
        }
        logger.info("ChargeProxyClient initialized with URL: {}", this.proxyUrl);
    }

    private String buildProxyUrl(String configuredUrl) {
        // Check environment variables first
        String envHost = System.getenv("CHARGE_PROXY_HOST");
        String envPort = System.getenv("CHARGE_PROXY_PORT");
        
        if (envHost != null && !envHost.isEmpty()) {
            String port = (envPort != null && !envPort.isEmpty()) ? envPort : "8080";
            return "http://" + envHost + ":" + port + "/charge-proxy/ws/charge";
        }
        
        // Fall back to configured URL
        if (configuredUrl != null && !configuredUrl.isEmpty()) {
            return configuredUrl;
        }
        
        // Default URL
        return "http://charge-proxy:8080/charge-proxy/ws/charge";
    }

    /**
     * Creates a charge in the proxy service.
     * 
     * @param customerId the customer ID
     * @param value the charge value
     * @param dueDate the due date (yyyy-MM-dd)
     * @param billingType PIX, BOLETO, or CREDIT_CARD
     * @param description optional description
     * @return the proxy response with charge details
     */
    public ProxyChargeResponse createCharge(String customerId, BigDecimal value, 
                                            String dueDate, String billingType, String description) {
        logger.info("Calling proxy createCharge - customer: {}, value: {}, type: {}", 
                customerId, value, billingType);

        try {
            // Create SOAP message
            SOAPMessage soapMessage = createChargeRequest(customerId, value, dueDate, billingType, description);
            
            // Send request
            String response = sendSoapRequest(soapMessage);
            
            // Parse response
            return parseChargeResponse(response);

        } catch (Exception e) {
            logger.error("Error calling proxy createCharge", e);
            return ProxyChargeResponse.error("Failed to communicate with proxy: " + e.getMessage());
        }
    }

    /**
     * Gets a charge from the proxy service.
     */
    public ProxyChargeResponse getCharge(String chargeId) {
        logger.info("Calling proxy getCharge - chargeId: {}", chargeId);

        try {
            SOAPMessage soapMessage = createGetChargeRequest(chargeId);
            String response = sendSoapRequest(soapMessage);
            return parseChargeResponse(response);

        } catch (Exception e) {
            logger.error("Error calling proxy getCharge", e);
            return ProxyChargeResponse.error("Failed to communicate with proxy: " + e.getMessage());
        }
    }

    /**
     * Cancels a charge in the proxy service.
     */
    public ProxyChargeResponse cancelCharge(String chargeId) {
        logger.info("Calling proxy cancelCharge - chargeId: {}", chargeId);

        try {
            SOAPMessage soapMessage = createCancelChargeRequest(chargeId);
            String response = sendSoapRequest(soapMessage);
            return parseChargeResponse(response);

        } catch (Exception e) {
            logger.error("Error calling proxy cancelCharge", e);
            return ProxyChargeResponse.error("Failed to communicate with proxy: " + e.getMessage());
        }
    }

    /**
     * Health check on the proxy service.
     */
    public String healthCheck() {
        logger.debug("Calling proxy healthCheck");

        try {
            SOAPMessage soapMessage = createHealthCheckRequest();
            String response = sendSoapRequest(soapMessage);
            return parseHealthCheckResponse(response);

        } catch (Exception e) {
            logger.error("Error calling proxy healthCheck", e);
            return "Proxy unreachable: " + e.getMessage();
        }
    }

    /**
     * Creates a SOAP RPC request for createCharge operation.
     * RPC style: parameters are direct children of the operation element.
     */
    private SOAPMessage createChargeRequest(String customerId, BigDecimal value, 
                                            String dueDate, String billingType, String description) throws SOAPException {
        SOAPMessage message = messageFactory.createMessage();
        SOAPEnvelope envelope = message.getSOAPPart().getEnvelope();
        envelope.addNamespaceDeclaration(NAMESPACE_PREFIX, NAMESPACE);

        SOAPBody body = envelope.getBody();
        // RPC style: operation element with namespace
        SOAPElement operation = body.addChildElement("createCharge", NAMESPACE_PREFIX, NAMESPACE);
        
        // RPC style: ChargeRequest parameter (complex type wrapper)
        SOAPElement request = operation.addChildElement("ChargeRequest");
        request.addChildElement("customerId").addTextNode(customerId);
        request.addChildElement("value").addTextNode(value.toString());
        request.addChildElement("dueDate").addTextNode(dueDate);
        request.addChildElement("billingType").addTextNode(billingType);
        if (description != null) {
            request.addChildElement("description").addTextNode(description);
        }

        message.saveChanges();
        return message;
    }

    /**
     * Creates a SOAP RPC request for getCharge operation.
     */
    private SOAPMessage createGetChargeRequest(String chargeId) throws SOAPException {
        SOAPMessage message = messageFactory.createMessage();
        SOAPEnvelope envelope = message.getSOAPPart().getEnvelope();
        envelope.addNamespaceDeclaration(NAMESPACE_PREFIX, NAMESPACE);

        SOAPBody body = envelope.getBody();
        // RPC style: operation element with namespace
        SOAPElement operation = body.addChildElement("getCharge", NAMESPACE_PREFIX, NAMESPACE);
        // RPC style: simple parameter without namespace
        operation.addChildElement("chargeId").addTextNode(chargeId);

        message.saveChanges();
        return message;
    }

    /**
     * Creates a SOAP RPC request for cancelCharge operation.
     */
    private SOAPMessage createCancelChargeRequest(String chargeId) throws SOAPException {
        SOAPMessage message = messageFactory.createMessage();
        SOAPEnvelope envelope = message.getSOAPPart().getEnvelope();
        envelope.addNamespaceDeclaration(NAMESPACE_PREFIX, NAMESPACE);

        SOAPBody body = envelope.getBody();
        // RPC style: operation element with namespace
        SOAPElement operation = body.addChildElement("cancelCharge", NAMESPACE_PREFIX, NAMESPACE);
        // RPC style: simple parameter without namespace
        operation.addChildElement("chargeId").addTextNode(chargeId);

        message.saveChanges();
        return message;
    }

    /**
     * Creates a SOAP RPC request for healthCheck operation.
     */
    private SOAPMessage createHealthCheckRequest() throws SOAPException {
        SOAPMessage message = messageFactory.createMessage();
        SOAPEnvelope envelope = message.getSOAPPart().getEnvelope();
        envelope.addNamespaceDeclaration(NAMESPACE_PREFIX, NAMESPACE);

        SOAPBody body = envelope.getBody();
        // RPC style: operation element with namespace (no parameters)
        body.addChildElement("healthCheck", NAMESPACE_PREFIX, NAMESPACE);

        message.saveChanges();
        return message;
    }

    private String sendSoapRequest(SOAPMessage soapMessage) throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        soapMessage.writeTo(outputStream);
        String requestBody = outputStream.toString();

        logger.debug("SOAP Request: {}", requestBody);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(proxyUrl))
                .header("Content-Type", "text/xml; charset=utf-8")
                .header("SOAPAction", "")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .timeout(Duration.ofSeconds(30))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        logger.debug("SOAP Response: {}", response.body());

        if (response.statusCode() != 200) {
            throw new RuntimeException("HTTP error: " + response.statusCode());
        }

        return response.body();
    }

    private ProxyChargeResponse parseChargeResponse(String xmlResponse) {
        try {
            // Simple XML parsing
            ProxyChargeResponse response = new ProxyChargeResponse();

            response.setSuccess(extractValue(xmlResponse, "success").equalsIgnoreCase("true"));
            response.setChargeId(extractValue(xmlResponse, "chargeId"));
            response.setStatus(extractValue(xmlResponse, "status"));
            response.setPixCode(extractValue(xmlResponse, "pixCode"));
            response.setBoletoCode(extractValue(xmlResponse, "boletoCode"));
            response.setInvoiceUrl(extractValue(xmlResponse, "invoiceUrl"));
            response.setErrorMessage(extractValue(xmlResponse, "errorMessage"));

            String valueStr = extractValue(xmlResponse, "value");
            if (valueStr != null && !valueStr.isEmpty()) {
                response.setValue(new BigDecimal(valueStr));
            }

            return response;

        } catch (Exception e) {
            logger.error("Error parsing charge response", e);
            return ProxyChargeResponse.error("Failed to parse response: " + e.getMessage());
        }
    }

    private String parseHealthCheckResponse(String xmlResponse) {
        return extractValue(xmlResponse, "status");
    }

    private String extractValue(String xml, String tagName) {
        String startTag = "<" + tagName + ">";
        String endTag = "</" + tagName + ">";

        int startIndex = xml.indexOf(startTag);
        int endIndex = xml.indexOf(endTag);

        if (startIndex != -1 && endIndex != -1) {
            return xml.substring(startIndex + startTag.length(), endIndex).trim();
        }

        // Try with namespace prefix
        startTag = "<ns2:" + tagName + ">";
        endTag = "</ns2:" + tagName + ">";
        startIndex = xml.indexOf(startTag);
        endIndex = xml.indexOf(endTag);

        if (startIndex != -1 && endIndex != -1) {
            return xml.substring(startIndex + startTag.length(), endIndex).trim();
        }

        return null;
    }

    @PreDestroy
    public void close() {
        logger.info("ChargeProxyClient closed");
    }

    /**
     * Response class for proxy charge operations.
     */
    public static class ProxyChargeResponse {
        private boolean success;
        private String chargeId;
        private String status;
        private BigDecimal value;
        private String pixCode;
        private String boletoCode;
        private String invoiceUrl;
        private String errorMessage;

        public static ProxyChargeResponse error(String message) {
            ProxyChargeResponse response = new ProxyChargeResponse();
            response.setSuccess(false);
            response.setErrorMessage(message);
            return response;
        }

        // Getters and Setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }

        public String getChargeId() { return chargeId; }
        public void setChargeId(String chargeId) { this.chargeId = chargeId; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public BigDecimal getValue() { return value; }
        public void setValue(BigDecimal value) { this.value = value; }

        public String getPixCode() { return pixCode; }
        public void setPixCode(String pixCode) { this.pixCode = pixCode; }

        public String getBoletoCode() { return boletoCode; }
        public void setBoletoCode(String boletoCode) { this.boletoCode = boletoCode; }

        public String getInvoiceUrl() { return invoiceUrl; }
        public void setInvoiceUrl(String invoiceUrl) { this.invoiceUrl = invoiceUrl; }

        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    }
}
