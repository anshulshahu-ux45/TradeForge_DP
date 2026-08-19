package com.tradeforge.chain;

public abstract class AgentHandler {

    protected AgentHandler next;

    public void setNext(AgentHandler next) {
        this.next = next;
    }

    public abstract int handle(int quantity);
}