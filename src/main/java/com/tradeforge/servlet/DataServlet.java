package com.tradeforge.servlet;

import com.tradeforge.dao.StockDAO;
import com.tradeforge.dao.TradeDAO;
import com.tradeforge.model.User;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;

@WebServlet("/api/data")
public class DataServlet extends HttpServlet {

    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        HttpSession session =
                request.getSession(false);

        User user =
                session != null
                        ? (User) session.getAttribute("user")
                        : null;

        if (user == null) {

            response.setStatus(401);

            response.getWriter().print(
                    "{\"status\":\"error\"," +
                    "\"message\":\"Please login first\"}"
            );

            return;
        }

        StockDAO stockDAO = new StockDAO();

        TradeDAO tradeDAO = new TradeDAO();

        String stocks =
                stockDAO.getStocksJSON();

        String transactions =
                tradeDAO.getTransactionsJSON(user.id);

        String json =
                "{"
                + "\"user\":{"
                + "\"id\":" + user.id + ","
                + "\"username\":\"" + user.username + "\","
                + "\"balance\":" + user.balance
                + "},"

                + "\"stocks\":" + stocks + ","

                + "\"transactions\":" + transactions

                + "}";

        response.getWriter().print(json);
    }
}