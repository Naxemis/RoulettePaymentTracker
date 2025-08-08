package com.roulettepaymenttracker;

import java.io.InputStream;
import java.util.Properties;

public class ConfigLoader {
    private static final Properties properties = new Properties();

    static {
        try (InputStream input = ConfigLoader.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                System.out.println("Unable to find config.properties");
            } else {
                properties.load(input);
            }
        } catch (Exception exception) {
            System.out.println("Error loading config.properties: " + exception.getMessage());
        }
    }

    public static String get_property(String propertyName) {
        return properties.getProperty(propertyName);
    }
}