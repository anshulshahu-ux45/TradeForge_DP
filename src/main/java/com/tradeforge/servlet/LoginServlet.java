package com.tradeforge.servlet;

import java.io.IOException;

import com.tradeforge.dao.UserDAO;
import com.tradeforge.model.User;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        String username =
            request.getParameter("username");

        String password =
            request.getParameter("password");

        UserDAO dao = new UserDAO();

        User user =
            dao.login(username, password);

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        if (user != null) {

            request.getSession()
                   .setAttribute("user", user);

            response.getWriter().print(
                "{"
                + "\"status\":\"success\","
                + "\"user\":{"
                + "\"id\":" + user.id + ","
                + "\"username\":\"" + user.username + "\","
                + "\"balance\":" + user.balance
                + "}"
                + "}"
            );

        } else {

            response.setStatus(401);

            response.getWriter().print(
                "{\"status\":\"error\","
                + "\"message\":\"Invalid username or password\"}"
            );
        }
    }

    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        HttpSession session =
            request.getSession(false);

        User user =
            session != null
            ? (User) session.getAttribute("user")
            : null;

        response.setContentType("application/json");

        if (user != null) {

            response.getWriter().print(
                "{\"loggedIn\":true,"
                + "\"user\":{\"id\":" + user.id
                + ",\"username\":\"" + user.username
                + "\",\"balance\":" + user.balance
                + "}}"
            );

        } else {

            response.getWriter().print(
                "{\"loggedIn\":false}"
            );
        }
    }
}