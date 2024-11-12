package com.mylogo.visitors.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

//@Configuration
public class DataSourceConfig {
    public static DataSource createConnectionPool() {


        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(String.format("jdbc:mysql://10.206.0.3:3306/%s", "mylogo")); // Database name
        hikariConfig.setUsername("root"); // e.g. 'root'
        hikariConfig.setPassword("mahesh"); // e.g. 'my-secret-pw'

        // Specify additional connection properties.
        hikariConfig.addDataSourceProperty("socketFactory", "com.google.cloud.sql.mysql.SocketFactory");
        hikariConfig.addDataSourceProperty("cloudSqlInstance", "burner-mahtekum:us-central1:mytest123");
        hikariConfig.addDataSourceProperty("useSSL", "false");

        return new HikariDataSource(hikariConfig);
    }
}
