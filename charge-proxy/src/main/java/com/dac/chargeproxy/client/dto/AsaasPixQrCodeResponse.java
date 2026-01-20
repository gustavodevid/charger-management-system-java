package com.dac.chargeproxy.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;

/**
 * DTO for ASAAS PIX QR Code response.
 * 
 * Used when fetching PIX QR Code details for a payment.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AsaasPixQrCodeResponse {

    /**
     * QR Code ID.
     */
    private String id;

    /**
     * Base64 encoded QR Code image.
     */
    private String encodedImage;

    /**
     * PIX copy-paste payload (copia e cola).
     */
    private String payload;

    /**
     * Expiration date of the QR Code.
     */
    private String expirationDate;

    public AsaasPixQrCodeResponse() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEncodedImage() {
        return encodedImage;
    }

    public void setEncodedImage(String encodedImage) {
        this.encodedImage = encodedImage;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public String getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(String expirationDate) {
        this.expirationDate = expirationDate;
    }

    @Override
    public String toString() {
        return "AsaasPixQrCodeResponse{" +
                "id='" + id + '\'' +
                ", payload='" + (payload != null ? payload.substring(0, Math.min(50, payload.length())) + "..." : null) + '\'' +
                ", expirationDate='" + expirationDate + '\'' +
                '}';
    }
}
