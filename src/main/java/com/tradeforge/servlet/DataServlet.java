package com.tradeforge.servlet;

import com.tradeforge.model.User;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;

@WebServlet("/api/data")
public class DataServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        String username = (user != null) ? user.username : "Trader";
        double balance = (user != null) ? user.balance : 50000.00;
        int userId = (user != null) ? user.id : 1;

        String json = "{"
            + "\"user\":{\"id\":" + userId + ",\"username\":\"" + username + "\",\"balance\":" + balance + "},"
            + "\"stocks\":["
            + "{\"id\":1,\"symbol\":\"TCS\",\"name\":\"Tata Consultancy Services\",\"price\":3500.00,\"change\":1.25,\"quantity\":5000,\"category\":\"Tech\"},"
            + "{\"id\":2,\"symbol\":\"RELIANCE\",\"name\":\"Reliance Industries\",\"price\":2850.50,\"change\":-0.45,\"quantity\":4200,\"category\":\"Energy\"},"
            + "{\"id\":3,\"symbol\":\"INFY\",\"name\":\"Infosys Limited\",\"price\":1620.00,\"change\":2.10,\"quantity\":6100,\"category\":\"Tech\"},"
            + "{\"id\":4,\"symbol\":\"HDFCBANK\",\"name\":\"HDFC Bank Ltd.\",\"price\":1540.75,\"change\":0.80,\"quantity\":8000,\"category\":\"Banking\"},"
            + "{\"id\":5,\"symbol\":\"ICICIBANK\",\"name\":\"ICICI Bank Ltd.\",\"price\":1120.30,\"change\":-1.15,\"quantity\":3500,\"category\":\"Banking\"},"
            + "{\"id\":6,\"symbol\":\"TATAMOTORS\",\"name\":\"Tata Motors Ltd.\",\"price\":980.60,\"change\":3.40,\"quantity\":9500,\"category\":\"Auto\"}"
            + "],"
            + "\"portfolio\":["
            + "{\"stockId\":1,\"symbol\":\"TCS\",\"quantity\":15,\"avgPrice\":3450.00,\"currentPrice\":3500.00},"
            + "{\"stockId\":3,\"symbol\":\"INFY\",\"quantity\":25,\"avgPrice\":1580.00,\"currentPrice\":1620.00},"
            + "{\"stockId\":4,\"symbol\":\"HDFCBANK\",\"quantity\":10,\"avgPrice\":1550.00,\"currentPrice\":1540.75}"
            + "],"
            + "\"patterns\":["
            + "{\"name\":\"Singleton\",\"component\":\"DatabaseConnection\",\"status\":\"Active Connection\",\"desc\":\"Single database instance shared across DAOs.\"},"
            + "{\"name\":\"Factory Method\",\"component\":\"TransactionFactory\",\"status\":\"Buy/Sell Instantiator\",\"desc\":\"Encapsulates creation of BuyTransaction and SellTransaction.\"},"
            + "{\"name\":\"Chain of Resp.\",\"component\":\"AgentHandler (Agent1->2->3)\",\"status\":\"Dynamic Agent Routing\",\"desc\":\"Routes order to Agent 1 (<=500), Agent 2 (<=700), Agent 3 (>700).\"},"
            + "{\"name\":\"Proxy Pattern\",\"component\":\"TradingProxy\",\"status\":\"Access & Pre-Validation Shield\",\"desc\":\"Enforces login checks before forwarding to RealTradingService.\"},"
            + "{\"name\":\"Observer Pattern\",\"component\":\"TradeSubject & UserObserver\",\"status\":\"Real-time Broadcast\",\"desc\":\"Notifies registered observers on successful trade completion.\"}"
            + "]"
            + "}";

        response.getWriter().print(json);
    }
}
