package com.tradeforge.dao;

import com.tradeforge.model.User;
import com.tradeforge.singleton.DatabaseConnection;

import java.sql.*;

public class UserDAO {

    public User login(
            String username,
            String password) {

        try {

            Connection con =
                DatabaseConnection.getConnection();

            PreparedStatement ps =
                con.prepareStatement(
                    "SELECT * FROM users " +
                    "WHERE username=? AND password=?"
                );

            ps.setString(1, username);
            ps.setString(2, password);

            ResultSet rs =
                ps.executeQuery();

            if (rs.next()) {

                return new User(
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getDouble("balance")
                );
            }

        } catch (Exception e) {

            e.printStackTrace();
        }

        return null;
    }
}