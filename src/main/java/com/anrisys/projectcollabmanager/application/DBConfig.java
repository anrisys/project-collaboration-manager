package com.anrisys.projectcollabmanager.application;

import com.anrisys.projectcollabmanager.util.DBPropertiesLoader;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.util.Properties;

public class DBConfig {
    private static final HikariDataSource dataSource;

    static  {
        Properties props = DBPropertiesLoader.load();
        HikariConfig config = new HikariConfig();

        config.setDriverClassName(props.getProperty("db.driverClassName"));
        config.setJdbcUrl(props.getProperty("db.jdbc.url"));
        config.setUsername(props.getProperty("db.jdbc.username"));
        config.setPassword(props.getProperty("db.jdbc.password"));

        // pool
        config.setMaximumPoolSize(Integer.parseInt(props.getProperty("hikaricp.maximumPoolSize")));
        config.setMinimumIdle(5);;
        config.setIdleTimeout(60_000);
        config.setMaxLifetime(60 * 60 * 1000);

        dataSource = new HikariDataSource(config);
    }

    public static DataSource getDataSource() {
        return dataSource;
    }

    public static void closeDataSource() {
        if(dataSource != null) {
            dataSource.close();
        }
    }

    private DBConfig() {}
}