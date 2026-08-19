package com.tradeforge.model;

public class Stock {

    public int id;
    public String symbol;
    public double price;
    public int quantity;

    public Stock(int id, String symbol,
                 double price, int quantity) {

        this.id = id;
        this.symbol = symbol;
        this.price = price;
        this.quantity = quantity;
    }
}