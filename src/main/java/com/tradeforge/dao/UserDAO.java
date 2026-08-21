package com.tradeforge.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.tradeforge.model.User;
import com.tradeforge.singleton.DatabaseConnection;

public class UserDAO {

    public User login(String username, String password) {

        String sql =
            "SELECT id, username, balance " +
            "FROM users " +
            "WHERE username = ? AND password = ?";

        try {

            Connection con =
                DatabaseConnection.getConnection();

            if (con == null) {
                return null;
            }

            PreparedStatement ps =
                con.prepareStatement(sql);

            ps.setString(1, username);
            ps.setString(2, password);

            ResultSet rs = ps.executeQuery();

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

    public User findById(int userId) {

        String sql =
            "SELECT id, username, balance " +
            "FROM users WHERE id = ?";

        try {

            Connection con = DatabaseConnection.getConnection();

            if (con == null) {
                return null;
            }

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, userId);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return new User(
                            rs.getInt("id"),
                            rs.getString("username"),
                            rs.getDouble("balance")
                        );
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}