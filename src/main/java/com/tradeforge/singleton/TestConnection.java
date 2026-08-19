package com.tradeforge.singleton;
import java.sql.Connection;

public class TestConnection {

    public static void main(String[] args) {

        Connection con =
            DatabaseConnection.getConnection();

        if (con != null) {
            System.out.println(
                "Database Connected Successfully!"
            );
        } else {
            System.out.println(
                "Database Connection Failed!"
            );
        }
    }
}