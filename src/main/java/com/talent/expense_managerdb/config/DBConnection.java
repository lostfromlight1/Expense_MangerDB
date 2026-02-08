package main.java.com.talent.expense_managerdb.config;

import java.sql.Connection;
import java.sql.DriverManager;

public class DBConnection {

    private static final String URL =
            "jdbc:mysql://localhost:3306/expense_manager_db";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (Exception e) {
            throw new main.java.com.talent.expense_managerdb.exception.DatabaseException("Failed to connect to database", e);
        }
    }
}
