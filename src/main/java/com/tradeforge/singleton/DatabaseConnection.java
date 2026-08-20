package com.tradeforge.singleton;

import java.sql.Connection;
import java.sql.DriverManager;

public class DatabaseConnection {

    private static Connection connection;

    private DatabaseConnection() {
    }

    public static Connection getConnection() {

        try {

            if (connection == null || connection.isClosed()) {

                Class.forName("com.mysql.cj.jdbc.Driver");

                connection = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/tradeforge",
                    "root",
                    "anshul"
                );
            }

        } catch (Exception e) {

            e.printStackTrace();
        }

        return connection;
    }
}