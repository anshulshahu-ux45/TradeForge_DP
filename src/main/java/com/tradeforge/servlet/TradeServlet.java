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

        String accept = request.getHeader("Accept");
        String requestedWith = request.getHeader("X-Requested-With");
        boolean isAjax = (accept != null && accept.contains("application/json")) || "XMLHttpRequest".equals(requestedWith);

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


            // 1. FACTORY PATTERN

            Transaction transaction =
                TransactionFactory
                .create(type);

            if (transaction != null) {
                transaction.execute();
            }


            // 2. CHAIN OF RESPONSIBILITY PATTERN

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


            // 3. PROXY PATTERN

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


            // 4. DATABASE (SINGLETON PATTERN via TradeDAO)

            TradeDAO dao =
                new TradeDAO();

            try {
                dao.saveTrade(
                    trade,
                    agentId,
                    0
                );
            } catch (Exception dbEx) {
                // Log DB exception, continue trade processing execution
                dbEx.printStackTrace();
            }


            // 5. OBSERVER PATTERN

            TradeSubject subject =
                new TradeSubject();

            subject.addObserver(
                new UserObserver()
            );

            String notifyMsg = type +
                " transaction completed. Agent " +
                agentId +
                " handled the order.";

            subject.notifyUsers(notifyMsg);


            if (isAjax) {
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().print("{"
                    + "\"status\":\"success\","
                    + "\"message\":\"" + result + "\","
                    + "\"agentId\":" + agentId + ","
                    + "\"userId\":" + userId + ","
                    + "\"stockId\":" + stockId + ","
                    + "\"type\":\"" + type + "\","
                    + "\"quantity\":" + quantity + ","
                    + "\"notification\":\"" + notifyMsg + "\""
                    + "}");
            } else {
                response.setContentType("text/html;charset=UTF-8");
                response.getWriter().println(
                    result +
                    "<br>Agent " +
                    agentId +
                    " handled the order."
                );
            }

        } catch (Exception e) {

            if (isAjax) {
                response.setStatus(400);
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().print("{\"status\":\"error\",\"message\":\"" + (e.getMessage() != null ? e.getMessage().replace("\"", "\\\"") : "Trade failed") + "\"}");
            } else {
                response.getWriter().println(
                    "Error: " + e.getMessage()
                );
            }
        }
    }
}