package com.tradeforge.observer;

import java.util.ArrayList;

public class TradeSubject {

    private ArrayList<Observer> observers =
        new ArrayList<>();

    public void addObserver(Observer observer) {

        observers.add(observer);
    }

    public void notifyUsers(String message) {

        for (Observer observer : observers) {

            observer.update(message);
        }
    }
}