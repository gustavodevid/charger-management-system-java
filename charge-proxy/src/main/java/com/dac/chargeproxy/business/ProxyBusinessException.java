package com.dac.chargeproxy.business;

/**
 * Exception thrown when a business rule validation fails in the Charge Proxy.
 */
public class ProxyBusinessException extends RuntimeException {

    private final String errorCode;

    public ProxyBusinessException(String message) {
        super(message);
        this.errorCode = "BUSINESS_ERROR";
    }

    public ProxyBusinessException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ProxyBusinessException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "BUSINESS_ERROR";
    }

    public String getErrorCode() {
        return errorCode;
    }
}
