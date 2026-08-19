package com.tradeforge.servlet;

import com.tradeforge.factory.*;
import com.tradeforge.model.Trade;
import com.tradeforge.proxy.*;
import com.tradeforge.chain.*;
import com.tradeforge.observer.*;
import com.tradeforge.dao.TradeDAO;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;

@WebServlet("/trade")
public class TradeServlet
        extends HttpServlet {

    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        try {

            int userId =
                Integer.parseInt(
                    request.getParameter("userId")
                );

            int stockId =
                Integer.parseInt(
                    request.getParameter("stockId")
                );

            int quantity =
                Integer.parseInt(
                    request.getParameter("quantity")
                );

            String type =
                request.getParameter("type");


            // 1. FACTORY

            Transaction transaction =
                TransactionFactory
                .create(type);

            transaction.execute();


            // 2. CHAIN OF RESPONSIBILITY

            AgentHandler agent1 =
                new Agent1();

            AgentHandler agent2 =
                new Agent2();

            AgentHandler agent3 =
                new Agent3();

            agent1.setNext(agent2);
            agent2.setNext(agent3);

            int agentId =
                agent1.handle(quantity);


            // 3. PROXY

            Trade trade =
                new Trade(
                    userId,
                    stockId,
                    type,
                    quantity
                );

            TradingService service =
                new TradingProxy();

            String result =
                service.trade(trade);


            // 4. DATABASE

            TradeDAO dao =
                new TradeDAO();

            dao.saveTrade(
                trade,
                agentId,
                0
            );


            // 5. OBSERVER

            TradeSubject subject =
                new TradeSubject();

            subject.addObserver(
                new UserObserver()
            );

            subject.notifyUsers(
                type +
                " transaction completed. Agent " +
                agentId +
                " handled the order."
            );


            response.getWriter().println(
                result +
                "<br>Agent " +
                agentId +
                " handled the order."
            );

        } catch (Exception e) {

            response.getWriter().println(
                "Error: " + e.getMessage()
            );
        }
    }
}