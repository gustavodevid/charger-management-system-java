package com.dac.chargeproxy.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;

/**
 * Main Spring configuration class for Charge Proxy application.
 * Replaces the traditional ServiceLocator pattern with Spring DI.
 */
@Configuration
@ComponentScan(basePackages = {
    "com.dac.chargeproxy.business",
    "com.dac.chargeproxy.client",
    "com.dac.chargeproxy.soap",
    "com.dac.chargeproxy.servlet"
})
@Import({AsaasConfig.class})
@PropertySource(value = "classpath:application.properties", ignoreResourceNotFound = true)
public class SpringAppConfig {
    // Configuration is handled via annotations and imported configs
}
