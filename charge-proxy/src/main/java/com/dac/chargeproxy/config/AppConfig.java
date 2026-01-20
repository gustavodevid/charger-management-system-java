package com.dac.chargeproxy.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Application configuration singleton.
 * Loads configuration from application.properties and environment variables.
 */
public class AppConfig {

    private static final Logger logger = LoggerFactory.getLogger(AppConfig.class);
    private static volatile AppConfig instance;
    private static final Pattern ENV_VAR_PATTERN = Pattern.compile("\\$\\{([^:}]+)(?::([^}]*))?\\}");

    private final Properties properties;
    private final String asaasApiUrl;
    private final String asaasAccessToken;
    private final String asaasApiMode;

    private AppConfig() {
        this.properties = loadProperties();

        this.asaasApiUrl = getEnvOrProperty("ASAAS_API_URL", "asaas.api.url", 
                "https://sandbox.asaas.com/api/v3");
        this.asaasAccessToken = getEnvOrProperty("ASAAS_ACCESS_TOKEN", "asaas.api.accessToken", "");
        this.asaasApiMode = getEnvOrProperty("ASAAS_API_MODE", "asaas.api.mode", "REAL");

        logger.info("Configuration loaded - ASAAS API URL: {}, Mode: {}", asaasApiUrl, asaasApiMode);
    }

    public static AppConfig getInstance() {
        if (instance == null) {
            synchronized (AppConfig.class) {
                if (instance == null) {
                    instance = new AppConfig();
                }
            }
        }
        return instance;
    }

    /**
     * Static method to get a property value.
     * Supports environment variable substitution in the format ${VAR_NAME:default}.
     */
    public static String getProperty(String key) {
        return getProperty(key, null);
    }

    /**
     * Static method to get a property value with default.
     * Supports environment variable substitution in the format ${VAR_NAME:default}.
     */
    public static String getProperty(String key, String defaultValue) {
        AppConfig config = getInstance();
        String value = config.properties.getProperty(key);
        
        if (value == null) {
            return defaultValue;
        }

        // Check if value contains environment variable reference
        return config.resolveEnvVariables(value, defaultValue);
    }

    /**
     * Resolves environment variables in a property value.
     * Format: ${ENV_VAR:default_value}
     */
    private String resolveEnvVariables(String value, String fallbackDefault) {
        Matcher matcher = ENV_VAR_PATTERN.matcher(value);
        StringBuffer result = new StringBuffer();
        
        while (matcher.find()) {
            String envVar = matcher.group(1);
            String defaultVal = matcher.group(2);
            
            String envValue = System.getenv(envVar);
            String replacement = (envValue != null && !envValue.isEmpty()) 
                    ? envValue 
                    : (defaultVal != null ? defaultVal : "");
            
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);
        
        String resolvedValue = result.toString();
        return resolvedValue.isEmpty() && fallbackDefault != null ? fallbackDefault : resolvedValue;
    }

    public String getAsaasApiUrl() {
        return asaasApiUrl;
    }

    public String getAsaasAccessToken() {
        return asaasAccessToken;
    }

    public String getAsaasApiMode() {
        return asaasApiMode;
    }

    private Properties loadProperties() {
        Properties props = new Properties();
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("application.properties")) {
            if (is != null) {
                props.load(is);
            }
        } catch (IOException e) {
            logger.warn("Could not load application.properties: {}", e.getMessage());
        }
        return props;
    }

    private String getEnvOrProperty(String envKey, String propKey, String defaultValue) {
        String envValue = System.getenv(envKey);
        if (envValue != null && !envValue.isEmpty()) {
            return envValue;
        }
        
        String propValue = properties.getProperty(propKey);
        if (propValue != null) {
            return resolveEnvVariables(propValue, defaultValue);
        }
        
        return defaultValue;
    }
}
