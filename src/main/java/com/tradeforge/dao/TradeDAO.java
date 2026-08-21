package com.tradeforge.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.tradeforge.model.Trade;
import com.tradeforge.model.TradeResult;
import com.tradeforge.singleton.DatabaseConnection;

public class TradeDAO {

    public TradeResult executeTrade(Trade trade, int agentId)
            throws SQLException {

        Connection con = DatabaseConnection.getConnection();

        if (con == null) {
            throw new SQLException("Database connection unavailable");
        }

        boolean previousAutoCommit = con.getAutoCommit();

        try {
            con.setAutoCommit(false);

            double balance;
            double price;
            int marketQuantity;

            String userSql =
                    "SELECT balance FROM users WHERE id = ? FOR UPDATE";
            try (PreparedStatement ps = con.prepareStatement(userSql)) {
                ps.setInt(1, trade.userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new SQLException("User account not found");
                    }
                    balance = rs.getDouble("balance");
                }
            }

            String stockSql =
                    "SELECT price, quantity FROM stocks WHERE id = ? FOR UPDATE";
            try (PreparedStatement ps = con.prepareStatement(stockSql)) {
                ps.setInt(1, trade.stockId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new SQLException("Stock not found");
                    }
                    price = rs.getDouble("price");
                    marketQuantity = rs.getInt("quantity");
                }
            }

            double amount = price * trade.quantity;
            boolean isBuy = "BUY".equalsIgnoreCase(trade.type);

            if (isBuy && balance < amount) {
                throw new SQLException("Insufficient balance");
            }

            if (isBuy && marketQuantity < trade.quantity) {
                throw new SQLException("Insufficient market quantity");
            }

            if (!isBuy) {
                String holdingsSql =
                        "SELECT COALESCE(SUM(CASE " +
                        "WHEN UPPER(type) = 'BUY' THEN quantity " +
                        "ELSE -quantity END), 0) AS held " +
                        "FROM transactions WHERE user_id = ? AND stock_id = ?";

                try (PreparedStatement ps = con.prepareStatement(holdingsSql)) {
                    ps.setInt(1, trade.userId);
                    ps.setInt(2, trade.stockId);
                    try (ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        if (rs.getInt("held") < trade.quantity) {
                            throw new SQLException("Insufficient holdings to sell");
                        }
                    }
                }
            }

            String userUpdateSql =
                    "UPDATE users SET balance = balance + ? WHERE id = ?";
            try (PreparedStatement ps = con.prepareStatement(userUpdateSql)) {
                ps.setDouble(1, isBuy ? -amount : amount);
                ps.setInt(2, trade.userId);
                ps.executeUpdate();
            }

            String stockUpdateSql =
                    "UPDATE stocks SET quantity = quantity + ? WHERE id = ?";
            try (PreparedStatement ps = con.prepareStatement(stockUpdateSql)) {
                ps.setInt(1, isBuy ? -trade.quantity : trade.quantity);
                ps.setInt(2, trade.stockId);
                ps.executeUpdate();
            }

            String transactionSql =
                    "INSERT INTO transactions " +
                    "(user_id, stock_id, agent_id, type, quantity, amount) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = con.prepareStatement(transactionSql)) {
                ps.setInt(1, trade.userId);
                ps.setInt(2, trade.stockId);
                ps.setInt(3, agentId);
                ps.setString(4, trade.type.toUpperCase());
                ps.setInt(5, trade.quantity);
                ps.setDouble(6, amount);
                ps.executeUpdate();
            }

            con.commit();

            return new TradeResult(
                    amount,
                    isBuy ? balance - amount : balance + amount
            );

        } catch (Exception e) {
            try {
                con.rollback();
            } catch (SQLException rollbackException) {
                e.addSuppressed(rollbackException);
            }
            if (e instanceof SQLException) {
                throw (SQLException) e;
            }
            throw new SQLException("Transaction failed", e);
        } finally {
            con.setAutoCommit(previousAutoCommit);
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

    public String getPortfolioJSON(int userId) {

        StringBuilder json = new StringBuilder("[");

        String sql =
                "SELECT s.id, s.symbol, s.name, s.price, " +
                "SUM(CASE WHEN UPPER(t.type) = 'BUY' THEN t.quantity " +
                "ELSE -t.quantity END) AS quantity, " +
                "SUM(CASE WHEN UPPER(t.type) = 'BUY' THEN t.amount ELSE 0 END) " +
                "AS invested " +
                "FROM transactions t JOIN stocks s ON t.stock_id = s.id " +
                "WHERE t.user_id = ? " +
                    "GROUP BY s.id, s.symbol, s.price " +
                "HAVING SUM(CASE WHEN UPPER(t.type) = 'BUY' THEN t.quantity " +
                "ELSE -t.quantity END) > 0 " +
                "ORDER BY s.symbol";

        try {
            Connection con = DatabaseConnection.getConnection();

            if (con == null) {
                return json.append("]").toString();
            }

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, userId);

                try (ResultSet rs = ps.executeQuery()) {
                    boolean first = true;
                    while (rs.next()) {
                        if (!first) {
                            json.append(",");
                        }

                        int quantity = rs.getInt("quantity");
                        double invested = rs.getDouble("invested");
                        double price = rs.getDouble("price");

                        json.append("{")
                                .append("\"stockId\":").append(rs.getInt("id"))
                                .append(",\"symbol\":\"").append(rs.getString("symbol"))
                                    .append("\",\"name\":\"").append(rs.getString("symbol"))
                                .append("\",\"quantity\":").append(quantity)
                                .append(",\"avgPrice\":").append(quantity > 0 ? invested / quantity : 0)
                                .append(",\"currentPrice\":").append(price)
                                .append(",\"currentValue\":").append(price * quantity)
                                .append("}");

                        first = false;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return json.append("]").toString();
    }
}