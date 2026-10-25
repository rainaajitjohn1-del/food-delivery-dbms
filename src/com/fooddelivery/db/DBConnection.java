package com.fooddelivery.db;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Opens JDBC connections to the food_delivery MySQL database.
 * Connection settings are read from db.properties in the project root,
 * so nobody has to hard-code a password in the source code.
 */
public final class DBConnection {

    private static final Properties CONFIG = loadConfig();

    private DBConnection() { }

    private static Properties loadConfig() {
        Properties p = new Properties();
        p.setProperty("db.url", "jdbc:mysql://localhost:3306/food_delivery?useSSL=false&allowPublicKeyRetrieval=true");
        p.setProperty("db.user", "root");
        p.setProperty("db.password", "");
        try (InputStream in = new FileInputStream("db.properties")) {
            p.load(in);
        } catch (IOException e) {
            System.err.println("db.properties not found - using default connection settings.");
        }
        return p;
    }

    /** Returns a NEW connection. Always use it in try-with-resources so it gets closed. */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                CONFIG.getProperty("db.url"),
                CONFIG.getProperty("db.user"),
                CONFIG.getProperty("db.password"));
    }

    /** Quick check used by the console demo and the login screen. */
    public static boolean testConnection() {
        try (Connection c = getConnection()) {
            return c.isValid(2);
        } catch (SQLException e) {
            return false;
        }
    }
}
