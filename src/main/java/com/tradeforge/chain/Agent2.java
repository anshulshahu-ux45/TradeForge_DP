package com.tradeforge.chain;

public class Agent2 extends AgentHandler {

    public int handle(int quantity) {

        if (quantity <= 700) {

            return 2;

        }

        return next.handle(quantity);
    }
}