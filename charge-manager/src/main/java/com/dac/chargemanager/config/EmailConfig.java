package com.dac.chargemanager.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.mail.Authenticator;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import java.util.Properties;

/**
 * Email configuration for Spring.
 * Configures Jakarta Mail Session as a Spring Bean.
 */
@Configuration
public class EmailConfig {

    private static final Logger logger = LoggerFactory.getLogger(EmailConfig.class);

    @Value("${mail.host:smtp.gmail.com}")
    private String mailHost;

    @Value("${mail.port:587}")
    private int mailPort;

    @Value("${mail.username:}")
    private String mailUsername;

    @Value("${mail.password:}")
    private String mailPassword;

    @Value("${mail.smtp.auth:true}")
    private boolean smtpAuth;

    @Value("${mail.smtp.starttls.enable:true}")
    private boolean starttlsEnable;

    @Value("${mail.from:noreply@chargemanager.com}")
    private String mailFrom;

    @Bean
    public Session mailSession() {
        // Override with environment variables if present
        String envHost = System.getenv("MAIL_HOST");
        String envPort = System.getenv("MAIL_PORT");
        String envUser = System.getenv("MAIL_USERNAME");
        String envPass = System.getenv("MAIL_PASSWORD");

        String actualHost = (envHost != null && !envHost.isEmpty()) ? envHost : mailHost;
        int actualPort = (envPort != null && !envPort.isEmpty()) ? Integer.parseInt(envPort) : mailPort;
        String actualUser = (envUser != null && !envUser.isEmpty()) ? envUser : mailUsername;
        String actualPass = (envPass != null && !envPass.isEmpty()) ? envPass : mailPassword;

        Properties props = new Properties();
        props.put("mail.smtp.host", actualHost);
        props.put("mail.smtp.port", String.valueOf(actualPort));
        props.put("mail.smtp.auth", String.valueOf(smtpAuth));
        props.put("mail.smtp.starttls.enable", String.valueOf(starttlsEnable));

        logger.info("Configuring mail session: host={}, port={}", actualHost, actualPort);

        Session session;
        if (actualUser != null && !actualUser.isEmpty() && actualPass != null && !actualPass.isEmpty()) {
            session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(actualUser, actualPass);
                }
            });
            logger.info("Mail session configured with authentication");
        } else {
            session = Session.getInstance(props);
            logger.info("Mail session configured without authentication");
        }

        return session;
    }

    @Bean
    public String mailFrom() {
        String envFrom = System.getenv("MAIL_FROM");
        return (envFrom != null && !envFrom.isEmpty()) ? envFrom : mailFrom;
    }
}
