package com.dac.chargeproxy.config;

import com.dac.chargeproxy.client.AsaasAuthInterceptor;
import com.dac.chargeproxy.client.AsaasClient;
import com.dac.chargeproxy.client.AsaasErrorDecoder;
import com.dac.chargeproxy.client.AsaasFeignClient;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Feign;
import feign.Logger;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import feign.slf4j.Slf4jLogger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ASAAS API configuration for Spring.
 * Configures OpenFeign client for ASAAS REST API communication.
 */
@Configuration
public class AsaasConfig {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(AsaasConfig.class);

    @Value("${asaas.api.url:https://sandbox.asaas.com/api/v3}")
    private String asaasApiUrl;

    @Value("${asaas.api.accessToken:}")
    private String asaasAccessToken;

    @Value("${asaas.api.mode:REAL}")
    private String asaasApiMode;

    /**
     * Creates ObjectMapper configured for ASAAS API responses.
     */
    @Bean
    public ObjectMapper asaasObjectMapper() {
        return new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * Creates the OpenFeign client for ASAAS API.
     * Configured with Jackson encoder/decoder and authentication interceptor.
     */
    @Bean
    public AsaasFeignClient asaasFeignClient(ObjectMapper asaasObjectMapper) {
        String actualUrl = resolveConfigValue(asaasApiUrl, "ASAAS_API_URL");
        String actualToken = resolveConfigValue(asaasAccessToken, "ASAAS_ACCESS_TOKEN");

        logger.info("Creating AsaasFeignClient - URL: {}", actualUrl);

        return Feign.builder()
                .encoder(new JacksonEncoder(asaasObjectMapper))
                .decoder(new JacksonDecoder(asaasObjectMapper))
                .errorDecoder(new AsaasErrorDecoder(asaasObjectMapper))
                .requestInterceptor(new AsaasAuthInterceptor(actualToken))
                .logger(new Slf4jLogger(AsaasFeignClient.class))
                .logLevel(Logger.Level.FULL)
                .target(AsaasFeignClient.class, actualUrl);
    }

    /**
     * Creates the AsaasClient service that uses the Feign client.
     * Supports both REAL mode (using Feign) and STUB mode (for testing).
     */
    @Bean
    public AsaasClient asaasClient(AsaasFeignClient feignClient) {
        String actualUrl = resolveConfigValue(asaasApiUrl, "ASAAS_API_URL");
        String actualMode = resolveConfigValue(asaasApiMode, "ASAAS_API_MODE");

        logger.info("Creating AsaasClient - URL: {}, Mode: {}", actualUrl, actualMode);

        return new AsaasClient(feignClient, actualMode);
    }

    /**
     * Resolves configuration value, preferring environment variable over property.
     */
    private String resolveConfigValue(String propertyValue, String envVarName) {
        String envValue = System.getenv(envVarName);
        return (envValue != null && !envValue.isEmpty()) ? envValue : propertyValue;
    }

    @Bean
    public String asaasApiUrl() {
        return resolveConfigValue(asaasApiUrl, "ASAAS_API_URL");
    }

    @Bean
    public String asaasApiMode() {
        return resolveConfigValue(asaasApiMode, "ASAAS_API_MODE");
    }
}
