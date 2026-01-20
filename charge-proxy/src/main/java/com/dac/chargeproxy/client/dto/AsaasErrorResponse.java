package com.dac.chargeproxy.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * DTO for ASAAS Error response.
 * 
 * ASAAS returns errors in a structured format with a list of error objects.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AsaasErrorResponse {

    /**
     * List of errors returned by ASAAS.
     */
    private List<AsaasError> errors;

    public AsaasErrorResponse() {
    }

    public List<AsaasError> getErrors() {
        return errors;
    }

    public void setErrors(List<AsaasError> errors) {
        this.errors = errors;
    }

    /**
     * Gets a formatted error message from all errors.
     */
    public String getFormattedMessage() {
        if (errors == null || errors.isEmpty()) {
            return "Unknown error from ASAAS";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < errors.size(); i++) {
            AsaasError error = errors.get(i);
            if (i > 0) {
                sb.append("; ");
            }
            sb.append(error.getDescription());
            if (error.getCode() != null && !error.getCode().isEmpty()) {
                sb.append(" (").append(error.getCode()).append(")");
            }
        }
        return sb.toString();
    }

    /**
     * Inner class representing a single ASAAS error.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AsaasError {

        /**
         * Error code.
         */
        private String code;

        /**
         * Error description.
         */
        private String description;

        public AsaasError() {
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        @Override
        public String toString() {
            return "AsaasError{" +
                    "code='" + code + '\'' +
                    ", description='" + description + '\'' +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "AsaasErrorResponse{" +
                "errors=" + errors +
                '}';
    }
}
