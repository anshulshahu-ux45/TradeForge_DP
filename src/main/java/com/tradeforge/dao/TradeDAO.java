package com.tradeforge.dao;

import com.tradeforge.model.Trade;
import com.tradeforge.singleton.DatabaseConnection;

import java.sql.*;

public class TradeDAO {

    public void saveTrade(
            Trade trade,
            int agentId,
            double amount) {

        try {

            Connection con =
                DatabaseConnection.getConnection();

            String sql =
                "INSERT INTO transactions " +
                "(user_id,stock_id,agent_id,type," +
                "quantity,amount) " +
                "VALUES(?,?,?,?,?,?)";

            PreparedStatement ps =
                con.prepareStatement(sql);

            ps.setInt(1, trade.userId);
            ps.setInt(2, trade.stockId);
            ps.setInt(3, agentId);
            ps.setString(4, trade.type);
            ps.setInt(5, trade.quantity);
            ps.setDouble(6, amount);

            ps.executeUpdate();

        } catch (Exception e) {

            e.printStackTrace();
        }
    }
}