package com.tradeforge.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.tradeforge.model.Trade;
import com.tradeforge.singleton.DatabaseConnection;

public class TradeDAO {

    public boolean saveTrade(Trade trade, int agentId, double amount) {

        String sql =
                "INSERT INTO transactions " +
                "(user_id, stock_id, agent_id, type, quantity, amount) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try {
            Connection con = DatabaseConnection.getConnection();

            if (con == null) {
                return false;
            }

            PreparedStatement ps = con.prepareStatement(sql);

            ps.setInt(1, trade.userId);
            ps.setInt(2, trade.stockId);
            ps.setInt(3, agentId);
            ps.setString(4, trade.type);
            ps.setInt(5, trade.quantity);
            ps.setDouble(6, amount);

            ps.executeUpdate();

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public String getTransactionsJSON(int userId) {

        StringBuilder json = new StringBuilder("[");

        String sql =
                "SELECT t.*, s.symbol " +
                "FROM transactions t " +
                "JOIN stocks s ON t.stock_id = s.id " +
                "WHERE t.user_id = ? " +
                "ORDER BY t.id DESC";

        try {
            Connection con = DatabaseConnection.getConnection();

            PreparedStatement ps = con.prepareStatement(sql);

            ps.setInt(1, userId);

            ResultSet rs = ps.executeQuery();

            boolean first = true;

            while (rs.next()) {

                if (!first) {
                    json.append(",");
                }

                json.append("{")
                        .append("\"type\":\"")
                        .append(rs.getString("type"))
                        .append("\",")

                        .append("\"symbol\":\"")
                        .append(rs.getString("symbol"))
                        .append("\",")

                        .append("\"quantity\":")
                        .append(rs.getInt("quantity"))
                        .append(",")

                        .append("\"agentId\":")
                        .append(rs.getInt("agent_id"))
                        .append(",")

                        .append("\"amount\":")
                        .append(rs.getDouble("amount"))

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