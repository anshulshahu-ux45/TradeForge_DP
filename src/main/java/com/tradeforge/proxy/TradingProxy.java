package com.tradeforge.proxy;

import com.tradeforge.model.Trade;

public class TradingProxy
        implements TradingService {

    private RealTradingService service =
        new RealTradingService();

    public String trade(Trade trade) {

        if (trade.userId <= 0) {

            return "Please login first";
        }

        return service.trade(trade);
    }
}