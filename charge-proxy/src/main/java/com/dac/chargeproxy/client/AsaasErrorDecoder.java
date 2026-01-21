package com.dac.chargeproxy.client;

import com.dac.chargeproxy.client.dto.AsaasErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import feign.codec.ErrorDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

/**
 * Feign error decoder for ASAAS API responses.
 * 
 * Converts ASAAS error responses into meaningful exceptions.
 */
public class AsaasErrorDecoder implements ErrorDecoder {

    private static final Logger logger = LoggerFactory.getLogger(AsaasErrorDecoder.class);
    
    private final ObjectMapper objectMapper;

    public AsaasErrorDecoder(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Exception decode(String methodKey, Response response) {
        String errorMessage = buildErrorMessage(response);
        
        logger.error("ASAAS API error - Method: {}, Status: {}, Message: {}", 
                methodKey, response.status(), errorMessage);

        return new AsaasApiException(response.status(), errorMessage);
    }

    private String buildErrorMessage(Response response) {
        try {
            if (response.body() != null) {
                try (InputStream bodyStream = response.body().asInputStream()) {
                    AsaasErrorResponse errorResponse = objectMapper.readValue(bodyStream, AsaasErrorResponse.class);
                    return errorResponse.getFormattedMessage();
                }
            }
        } catch (IOException e) {
            logger.warn("Could not parse ASAAS error response: {}", e.getMessage());
        }
        
        return "HTTP " + response.status() + ": " + response.reason();
    }

    /**
     * Custom exception for ASAAS API errors.
     */
    public static class AsaasApiException extends RuntimeException {
        private final int statusCode;

        public AsaasApiException(int statusCode, String message) {
            super(message);
            this.statusCode = statusCode;
        }

        public int getStatusCode() {
            return statusCode;
        }

        public boolean isValidationError() {
            return statusCode == 400;
        }

        public boolean isAuthError() {
            return statusCode == 401;
        }

        public boolean isNotFound() {
            return statusCode == 404;
        }

        public boolean isServerError() {
            return statusCode >= 500;
        }
    }
}
