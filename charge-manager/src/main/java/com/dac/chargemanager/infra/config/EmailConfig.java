package com.dac.chargemanager.infra.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Email configuration singleton.
 */
public class EmailConfig {

    private static final Logger logger = LoggerFactory.getLogger(EmailConfig.class);
    private static volatile EmailConfig instance;

    private final String host;
    private final int port;
    private final String username;
    private final String password;
    private final boolean smtpAuth;
    private final boolean starttls;
    private final String fromAddress;
    private final String fromName;

    private EmailConfig() {
        Properties props = loadProperties();

        this.host = getEnvOrProperty(props, "MAIL_HOST", "mail.host", "smtp.gmail.com");
        this.port = Integer.parseInt(getEnvOrProperty(props, "MAIL_PORT", "mail.port", "587"));
        this.username = getEnvOrProperty(props, "MAIL_USERNAME", "mail.username", "");
        this.password = getEnvOrProperty(props, "MAIL_PASSWORD", "mail.password", "");
        this.smtpAuth = Boolean.parseBoolean(getEnvOrProperty(props, "MAIL_SMTP_AUTH", "mail.smtp.auth", "true"));
        this.starttls = Boolean.parseBoolean(getEnvOrProperty(props, "MAIL_SMTP_STARTTLS", "mail.smtp.starttls.enable", "true"));
        this.fromAddress = getEnvOrProperty(props, "MAIL_FROM_ADDRESS", "mail.from.address", "noreply@chargemanager.com");
        this.fromName = getEnvOrProperty(props, "MAIL_FROM_NAME", "mail.from.name", "Charge Manager");

        logger.info("Email configuration loaded - Host: {}, Port: {}", host, port);
    }

    public static EmailConfig getInstance() {
        if (instance == null) {
            synchronized (EmailConfig.class) {
                if (instance == null) {
                    instance = new EmailConfig();
                }
            }
        }
        return instance;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public boolean isSmtpAuth() {
        return smtpAuth;
    }

    public boolean isStarttls() {
        return starttls;
    }

    public String getFromAddress() {
        return fromAddress;
    }

    public String getFromName() {
        return fromName;
    }

    public boolean isConfigured() {
        return username != null && !username.isEmpty() && password != null && !password.isEmpty();
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

    private String getEnvOrProperty(Properties props, String envKey, String propKey, String defaultValue) {
        String envValue = System.getenv(envKey);
        if (envValue != null && !envValue.isEmpty()) {
            return envValue;
        }
        return props.getProperty(propKey, defaultValue);
    }
}
