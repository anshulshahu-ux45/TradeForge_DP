package com.tradeforge.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.tradeforge.singleton.DatabaseConnection;

public class StockDAO {

    public String getStocksJSON() {

        StringBuilder json =
            new StringBuilder("[");

        String sql =
            "SELECT id, symbol, price, quantity FROM stocks";

        try {

            Connection con =
                DatabaseConnection.getConnection();

            PreparedStatement ps =
                con.prepareStatement(sql);

            ResultSet rs =
                ps.executeQuery();

            boolean first = true;

            while (rs.next()) {

                if (!first) {
                    json.append(",");
                }

                json.append("{")
                    .append("\"id\":")
                    .append(rs.getInt("id"))
                    .append(",")

                    .append("\"symbol\":\"")
                    .append(rs.getString("symbol"))
                    .append("\",")

                    .append("\"name\":\"")
                    .append(rs.getString("symbol"))
                    .append("\",")

                    .append("\"price\":")
                    .append(rs.getDouble("price"))
                    .append(",")

                    .append("\"quantity\":")
                    .append(rs.getInt("quantity"))

                    .append("}");

                first = false;
            }

        } catch (Exception e) {

            e.printStackTrace();
        }

        json.append("]");

        return json.toString();
    }
}