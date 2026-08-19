package com.tradeforge.factory;

public class TransactionFactory {

    public static Transaction create(String type) {

        if (type.equalsIgnoreCase("BUY")) {

            return new BuyTransaction();

        }

        if (type.equalsIgnoreCase("SELL")) {

            return new SellTransaction();

        }

        return null;
    }
}