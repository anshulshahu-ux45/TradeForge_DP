package com.tradeforge.model;

public class Trade {

    public int userId;
    public int stockId;
    public String type;
    public int quantity;

    public Trade(int userId, int stockId,
                 String type, int quantity) {

        this.userId = userId;
        this.stockId = stockId;
        this.type = type;
        this.quantity = quantity;
    }
}