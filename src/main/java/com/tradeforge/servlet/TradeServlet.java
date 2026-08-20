package com.tradeforge.servlet;

import java.io.IOException;

import com.tradeforge.chain.Agent1;
import com.tradeforge.chain.Agent2;
import com.tradeforge.chain.Agent3;
import com.tradeforge.chain.AgentHandler;
import com.tradeforge.dao.TradeDAO;
import com.tradeforge.factory.Transaction;
import com.tradeforge.factory.TransactionFactory;
import com.tradeforge.model.Trade;
import com.tradeforge.observer.TradeSubject;
import com.tradeforge.observer.UserObserver;
import com.tradeforge.proxy.TradingProxy;
import com.tradeforge.proxy.TradingService;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/trade")
public class TradeServlet extends HttpServlet {

    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {

            HttpSession session =
                    request.getSession(false);

            if (session == null ||
                    session.getAttribute("user") == null) {

                response.setStatus(401);

                response.getWriter().print(
                        "{\"status\":\"error\"," +
                        "\"message\":\"Please login first\"}"
                );

                return;
            }

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

            if (quantity <= 0) {
                throw new Exception(
                        "Quantity must be greater than 0"
                );
            }

            // FACTORY
            Transaction transaction =
                    TransactionFactory.create(type);

            if (transaction == null) {
                throw new Exception(
                        "Invalid transaction type"
                );
            }

            transaction.execute();

            // CHAIN OF RESPONSIBILITY
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

            // TRADE OBJECT
            Trade trade =
                    new Trade(
                            userId,
                            stockId,
                            type,
                            quantity
                    );

            // PROXY
            TradingService service =
                    new TradingProxy();

            String result =
                    service.trade(trade);

            // STOCK PRICE
            double price =
                    getStockPrice(stockId);

            double amount =
                    price * quantity;

            // DATABASE
            TradeDAO dao =
                    new TradeDAO();

            boolean saved =
                    dao.saveTrade(
                            trade,
                            agentId,
                            amount
                    );

            if (!saved) {
                throw new Exception(
                        "Transaction could not be saved"
                );
            }

            // OBSERVER
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

            response.getWriter().print(
                    "{"
                    + "\"status\":\"success\","
                    + "\"message\":\"" + result + "\","
                    + "\"agentId\":" + agentId + ","
                    + "\"amount\":" + amount
                    + "}"
            );

        } catch (Exception e) {

            e.printStackTrace();

            response.setStatus(400);

            response.getWriter().print(
                    "{\"status\":\"error\"," +
                    "\"message\":\"" +
                    e.getMessage() +
                    "\"}"
            );
        }
    }

    private double getStockPrice(int stockId) {

        switch (stockId) {

            case 1:
                return 3500.00;

            case 2:
                return 2850.50;

            case 3:
                return 1620.00;

            case 4:
                return 1540.75;

            case 5:
                return 1120.30;

            case 6:
                return 980.60;

            default:
                return 0;
        }
    }
}