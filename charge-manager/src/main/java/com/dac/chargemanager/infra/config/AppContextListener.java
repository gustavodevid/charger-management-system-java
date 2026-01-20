package com.dac.chargemanager.infra.config;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

/**
 * Application context listener for initializing and destroying application resources.
 * Handles Flyway migrations and component initialization.
 */
@WebListener
public class AppContextListener implements ServletContextListener {

    private static final Logger logger = LoggerFactory.getLogger(AppContextListener.class);

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        logger.info("===========================================");
        logger.info("Charge Manager Application Starting...");
        logger.info("===========================================");

        try {
            // Initialize database configuration
            DatabaseConfig dbConfig = DatabaseConfig.getInstance();
            DataSource dataSource = dbConfig.getDataSource();

            // Run Flyway migrations
            runFlywayMigrations(dataSource);

            // Initialize all services via ServiceLocator
            ServiceLocator.initialize(dataSource);

            // Store DataSource in servlet context for access by other components
            ServletContext ctx = sce.getServletContext();
            ctx.setAttribute("dataSource", dataSource);

            logger.info("===========================================");
            logger.info("Charge Manager Application Started!");
            logger.info("SOAP Endpoints:");
            logger.info("  - Customer: /ws/customer");
            logger.info("  - Charge:   /ws/charge");
            logger.info("Health Check: /health");
            logger.info("===========================================");

        } catch (Exception e) {
            logger.error("Failed to initialize application", e);
            throw new RuntimeException("Application initialization failed", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        logger.info("Charge Manager Application Shutting Down...");

        // Shutdown ServiceLocator (event publisher)
        ServiceLocator.shutdown();

        // Close database connection pool
        DatabaseConfig.getInstance().close();

        logger.info("Charge Manager Application Stopped.");
    }

    private void runFlywayMigrations(DataSource dataSource) {
        logger.info("Running Flyway database migrations...");
        
        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .baselineOnMigrate(true)
                .load();

        int migrationsApplied = flyway.migrate().migrationsExecuted;
        logger.info("Flyway migrations completed. {} migrations applied.", migrationsApplied);
    }
}
