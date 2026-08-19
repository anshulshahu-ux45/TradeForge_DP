package com.tradeforge.chain;

public class Agent1 extends AgentHandler {

    public int handle(int quantity) {

        if (quantity <= 500) {

            return 1;

        }

        return next.handle(quantity);
    }
}