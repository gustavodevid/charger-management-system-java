package com.dac.chargeproxy.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring Web MVC configuration.
 * Configures web-specific components like controllers and servlets.
 */
@Configuration
@EnableWebMvc
@ComponentScan(basePackages = "com.dac.chargeproxy.servlet")
public class WebConfig implements WebMvcConfigurer {
    // Web configuration - can be extended for additional MVC settings
}
