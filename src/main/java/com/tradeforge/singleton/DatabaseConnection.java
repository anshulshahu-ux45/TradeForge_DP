package com.tradeforge.singleton;

import java.sql.Connection;
import java.sql.DriverManager;

public class DatabaseConnection {

    private static Connection connection;

    private DatabaseConnection() {
    }

    public static Connection getConnection() {

        if (connection == null) {

            try {

                Class.forName(
                    "com.mysql.cj.jdbc.Driver"
                );

                connection = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/tradeforge",
                    "root",
                    "anshul"
                );

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return connection;
    }
}