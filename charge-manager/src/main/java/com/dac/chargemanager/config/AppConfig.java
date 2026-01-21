package com.dac.chargemanager.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;

/**
 * Main Spring configuration class for Charge Manager application.
 * Replaces the traditional ServiceLocator pattern with Spring DI.
 */
@Configuration
@ComponentScan(basePackages = {
    "com.dac.chargemanager.api",
    "com.dac.chargemanager.business",
    "com.dac.chargemanager.infra"
})
@Import({DatabaseConfig.class, EmailConfig.class})
@PropertySource(value = "classpath:application.properties", ignoreResourceNotFound = true)
public class AppConfig {
    // Configuration is handled via annotations and imported configs
}
