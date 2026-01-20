package com.dac.chargemanager.infra.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Database configuration using HikariCP connection pool.
 * Singleton pattern for managing the DataSource.
 */
public class DatabaseConfig {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);
    private static volatile DatabaseConfig instance;
    private final HikariDataSource dataSource;

    private DatabaseConfig() {
        Properties props = loadProperties();
        
        String host = getEnvOrProperty(props, "DB_HOST", "db.host", "localhost");
        String port = getEnvOrProperty(props, "DB_PORT", "db.port", "5432");
        String database = getEnvOrProperty(props, "DB_NAME", "db.name", "chargedb");
        String username = getEnvOrProperty(props, "DB_USER", "db.username", "chargeuser");
        String password = getEnvOrProperty(props, "DB_PASSWORD", "db.password", "chargepass");

        String jdbcUrl = String.format("jdbc:postgresql://%s:%s/%s", host, port, database);

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName("org.postgresql.Driver");
        
        // Pool configuration
        config.setMaximumPoolSize(Integer.parseInt(
                getEnvOrProperty(props, "DB_POOL_MAX_SIZE", "db.pool.maxSize", "10")));
        config.setMinimumIdle(Integer.parseInt(
                getEnvOrProperty(props, "DB_POOL_MIN_IDLE", "db.pool.minIdle", "5")));
        config.setIdleTimeout(Long.parseLong(
                getEnvOrProperty(props, "DB_POOL_IDLE_TIMEOUT", "db.pool.idleTimeout", "300000")));
        config.setConnectionTimeout(Long.parseLong(
                getEnvOrProperty(props, "DB_POOL_CONNECTION_TIMEOUT", "db.pool.connectionTimeout", "20000")));
        
        config.setPoolName("ChargeManagerPool");

        logger.info("Initializing database connection pool: {}", jdbcUrl);
        this.dataSource = new HikariDataSource(config);
        logger.info("Database connection pool initialized successfully");
    }

    public static DatabaseConfig getInstance() {
        if (instance == null) {
            synchronized (DatabaseConfig.class) {
                if (instance == null) {
                    instance = new DatabaseConfig();
                }
            }
        }
        return instance;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            logger.info("Closing database connection pool");
            dataSource.close();
        }
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
