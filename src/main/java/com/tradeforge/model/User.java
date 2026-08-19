package com.tradeforge.model;

public class User {

    public int id;
    public String username;
    public double balance;

    public User(int id, String username, double balance) {
        this.id = id;
        this.username = username;
        this.balance = balance;
    }
}