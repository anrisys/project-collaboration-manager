package com.anrisys.projectcollabmanager.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class DBPropertiesLoader {
    private static final String FILE_NAME = "database.properties";

    public static Properties load() {
        Properties properties = new Properties();
        try (InputStream input = DBPropertiesLoader.class.getClassLoader().getResourceAsStream(FILE_NAME)) {
            if(input == null) {
                throw new RuntimeException("Unable to find database.properties");
            }
            properties.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Error loading database properties", e);
        } ;
        return properties;
    }
}
