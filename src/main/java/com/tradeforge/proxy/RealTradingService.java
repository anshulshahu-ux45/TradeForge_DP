package com.tradeforge.proxy;

import com.tradeforge.model.Trade;

public class RealTradingService
        implements TradingService {

    public String trade(Trade trade) {

        return "Trade executed successfully";
    }
}