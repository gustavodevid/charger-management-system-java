package com.dac.chargemanager.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

/**
 * Database configuration using Spring and HikariCP connection pool.
 * Handles DataSource creation, Flyway migrations, and transaction management.
 */
@Configuration
@EnableTransactionManagement
public class DatabaseConfig {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);

    @Value("${db.host:localhost}")
    private String host;

    @Value("${db.port:5432}")
    private String port;

    @Value("${db.name:chargedb}")
    private String database;

    @Value("${db.username:chargeuser}")
    private String username;

    @Value("${db.password:chargepass}")
    private String password;

    @Value("${db.pool.maxSize:10}")
    private int maxPoolSize;

    @Value("${db.pool.minIdle:5}")
    private int minIdle;

    @Value("${db.pool.idleTimeout:300000}")
    private long idleTimeout;

    @Value("${db.pool.connectionTimeout:20000}")
    private long connectionTimeout;

    @Bean(destroyMethod = "close")
    public DataSource dataSource() {
        // Override with environment variables if present
        String envHost = System.getenv("DB_HOST");
        String envPort = System.getenv("DB_PORT");
        String envName = System.getenv("DB_NAME");
        String envUser = System.getenv("DB_USER");
        String envPass = System.getenv("DB_PASSWORD");
        
        String actualHost = (envHost != null && !envHost.isEmpty()) ? envHost : host;
        String actualPort = (envPort != null && !envPort.isEmpty()) ? envPort : port;
        String actualName = (envName != null && !envName.isEmpty()) ? envName : database;
        String actualUser = (envUser != null && !envUser.isEmpty()) ? envUser : username;
        String actualPass = (envPass != null && !envPass.isEmpty()) ? envPass : password;

        String jdbcUrl = String.format("jdbc:postgresql://%s:%s/%s", actualHost, actualPort, actualName);

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(actualUser);
        config.setPassword(actualPass);
        config.setDriverClassName("org.postgresql.Driver");
        config.setMaximumPoolSize(maxPoolSize);
        config.setMinimumIdle(minIdle);
        config.setIdleTimeout(idleTimeout);
        config.setConnectionTimeout(connectionTimeout);
        config.setPoolName("ChargeManagerPool");

        logger.info("Initializing database connection pool: {}", jdbcUrl);
        HikariDataSource dataSource = new HikariDataSource(config);
        logger.info("Database connection pool initialized successfully");

        return dataSource;
    }

    @Bean
    public PlatformTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean
    public Flyway flyway(DataSource dataSource) {
        logger.info("Running Flyway database migrations...");
        
        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .baselineOnMigrate(true)
                .load();

        int migrationsApplied = flyway.migrate().migrationsExecuted;
        logger.info("Flyway migrations completed. {} migrations applied.", migrationsApplied);

        return flyway;
    }
}
