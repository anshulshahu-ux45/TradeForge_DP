package com.tradeforge.servlet;

import java.io.IOException;

import com.tradeforge.dao.StockDAO;
import com.tradeforge.dao.TradeDAO;
import com.tradeforge.dao.UserDAO;
import com.tradeforge.model.User;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

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

        User freshUser =
                new UserDAO().findById(user.id);

        if (freshUser == null) {
            response.setStatus(401);
            response.getWriter().print(
                    "{\"status\":\"error\",\"message\":\"User account not found\"}"
            );
            return;
        }

        session.setAttribute("user", freshUser);

        StockDAO stockDAO = new StockDAO();

        TradeDAO tradeDAO = new TradeDAO();

        String stocks =
                stockDAO.getStocksJSON();

        String transactions =
                tradeDAO.getTransactionsJSON(freshUser.id);

        String json =
                "{"
                + "\"user\":{"
                + "\"id\":" + freshUser.id + ","
                + "\"username\":\"" + freshUser.username + "\","
                + "\"balance\":" + freshUser.balance
                + "},"

                + "\"stocks\":" + stocks + ","

                + "\"transactions\":" + transactions + ","

                + "\"portfolio\":" +
                tradeDAO.getPortfolioJSON(freshUser.id)
                + "}";

        response.getWriter().print(json);
    }
}