package com.tradeforge.servlet;

import com.tradeforge.dao.UserDAO;
import com.tradeforge.model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;

@WebServlet("/login")
public class LoginServlet
        extends HttpServlet {

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

        if (user != null) {

            request.getSession()
                   .setAttribute("user", user);

            response.sendRedirect(
                "dashboard.html"
            );

        } else {

            response.sendRedirect(
                "login.html?error=1"
            );
        }
    }
}