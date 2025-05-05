package com.yourorg.finance.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class ConnectionManager {
    private static final HikariDataSource ds;

    static {
        // 1) Configure Hikari to use a local SQLite file in your project folder
        HikariConfig cfg = new HikariConfig();
        cfg.setJdbcUrl("jdbc:sqlite:pfm.db");
        // optional tuning:
        cfg.setMaximumPoolSize(5);
        ds = new HikariDataSource(cfg);

        // 2) Initialize database schema on startup
        try (Connection conn = ds.getConnection()) {
            initSchema(conn);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new ExceptionInInitializerError("Failed to initialize database schema");
        }
    }

    private static void initSchema(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            // Users table
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS users (
                      id INTEGER PRIMARY KEY AUTOINCREMENT,
                      username TEXT UNIQUE NOT NULL,
                      password_hash TEXT NOT NULL,
                      role TEXT NOT NULL
                    );
                    """);
            // Categories table
            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS categories (
                          id INTEGER PRIMARY KEY AUTOINCREMENT,
                          user_id INTEGER NOT NULL,
                          name TEXT NOT NULL UNIQUE,
                          FOREIGN KEY(user_id) REFERENCES users(id)
                        );
                    """);

            // Transactions table
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS transactions (
                      id INTEGER PRIMARY KEY AUTOINCREMENT,
                      user_id INTEGER NOT NULL,
                      date TEXT NOT NULL,
                      description TEXT,
                      category TEXT,
                      amount REAL NOT NULL,
                      FOREIGN KEY(user_id) REFERENCES users(id)
                    );
                    """);

            // Budgets table
            // Budgets table
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS budgets (
                      id INTEGER PRIMARY KEY AUTOINCREMENT,
                      user_id INTEGER NOT NULL,
                      category TEXT NOT NULL,
                      limit_amount REAL NOT NULL,
                      FOREIGN KEY(user_id) REFERENCES users(id)
                    );
                    """);


            // Reminders table
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS reminders (
                      id INTEGER PRIMARY KEY AUTOINCREMENT,
                      user_id INTEGER NOT NULL,
                      reminder_time TEXT NOT NULL,
                      message TEXT NOT NULL,
                      FOREIGN KEY(user_id) REFERENCES users(id)
                    );
                    """);
        }
    }

    /**
     * Obtain a pooled connection
     */
    public static Connection getConnection() throws SQLException {
        return ds.getConnection();
    }
}
