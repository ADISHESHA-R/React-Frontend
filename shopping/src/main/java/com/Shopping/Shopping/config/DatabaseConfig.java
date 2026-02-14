package com.Shopping.Shopping.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.net.URI;

@Configuration
@Profile("prod")
public class DatabaseConfig {

    @Value("${DATABASE_URL:}")
    private String databaseUrl;

    @Bean
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        
        if (databaseUrl != null && !databaseUrl.isEmpty()) {
            try {
                // Parse DATABASE_URL and convert to JDBC format
                URI dbUri = new URI(databaseUrl.replace("postgresql://", "http://"));
                
                String username = dbUri.getUserInfo().split(":")[0];
                String password = dbUri.getUserInfo().split(":")[1];
                String host = dbUri.getHost();
                int port = dbUri.getPort() == -1 ? 5432 : dbUri.getPort(); // Default PostgreSQL port
                String dbName = dbUri.getPath().replaceFirst("/", "");
                
                // Build JDBC URL
                String jdbcUrl = String.format("jdbc:postgresql://%s:%d/%s", host, port, dbName);
                
                config.setJdbcUrl(jdbcUrl);
                config.setUsername(username);
                config.setPassword(password);
            } catch (Exception e) {
                // Fallback: try to use DATABASE_URL directly if it's already in JDBC format
                if (databaseUrl.startsWith("jdbc:")) {
                    config.setJdbcUrl(databaseUrl);
                } else {
                    throw new RuntimeException("Invalid DATABASE_URL format: " + databaseUrl, e);
                }
            }
        }
        
        // HikariCP connection pool settings
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(5);
        config.setConnectionTimeout(20000);
        config.setIdleTimeout(300000);
        config.setMaxLifetime(600000);
        config.setLeakDetectionThreshold(60000);
        config.setDriverClassName("org.postgresql.Driver");
        
        return new HikariDataSource(config);
    }
}
