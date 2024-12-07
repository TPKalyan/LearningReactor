package com.example.demo;

import java.sql.Connection;
import java.sql.DriverManager;

public class PostgresTest {
    public static void main(String[] args) {
        String jdbcUrl = "jdbc:postgresql://localhost:5432/newsletter_enrichment";
        String username = "username";
        String password = "password";

        try (Connection conn = DriverManager.getConnection(jdbcUrl, username, password)) {
            if (conn != null) {
                System.out.println("Connected to the database!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
