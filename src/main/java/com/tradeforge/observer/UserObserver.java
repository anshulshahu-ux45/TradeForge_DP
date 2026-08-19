package com.tradeforge.observer;

public class UserObserver implements Observer {

    public void update(String message) {

        System.out.println(
            "Notification: " + message
        );
    }
}