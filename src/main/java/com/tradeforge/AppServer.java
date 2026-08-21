package com.tradeforge;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.tradeforge.chain.Agent1;
import com.tradeforge.chain.Agent2;
import com.tradeforge.chain.Agent3;
import com.tradeforge.chain.AgentHandler;
import com.tradeforge.dao.StockDAO;
import com.tradeforge.dao.TradeDAO;
import com.tradeforge.dao.UserDAO;
import com.tradeforge.factory.Transaction;
import com.tradeforge.factory.TransactionFactory;
import com.tradeforge.model.Trade;
import com.tradeforge.observer.TradeSubject;
import com.tradeforge.observer.UserObserver;
import com.tradeforge.proxy.TradingProxy;
import com.tradeforge.proxy.TradingService;

/**
 * Embedded TradeForge Server Launcher.
 * Serves the animated HTML/CSS/JS Web Dashboard and runs the Java backend design patterns.
 */
public class AppServer {

    private static final int PORT = 8080;
    private static final String WEBAPP_DIR = "src/main/webapp";

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

        // Serve Static Frontend Assets (dashboard.html, style.css, app.js, etc.)
        server.createContext("/", new StaticFileHandler());

        // Java Trade Endpoint (Executes Factory, Chain of Resp, Proxy, Observer, Singleton)
        server.createContext("/trade", new TradeHandler());

        // Java Login Endpoint
        server.createContext("/login", new LoginHandler());

        // Java Data API Endpoint
        server.createContext("/api/data", new DataHandler());

        server.setExecutor(null);
        System.out.println("=================================================");
        System.out.println("🚀 TradeForge Animated Web Dashboard Server Running!");
        System.out.println("👉 Open Web Dashboard: http://localhost:" + PORT + "/dashboard.html");
        System.out.println("👉 Open Login Portal:   http://localhost:" + PORT + "/login.html");
        System.out.println("=================================================");
        server.start();
    }

    // Handler for /trade executing all 5 Design Patterns
    static class TradeHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String accept = exchange.getRequestHeaders().getFirst("Accept");
            String requestedWith = exchange.getRequestHeaders().getFirst("X-Requested-With");
            boolean isAjax = (accept != null && accept.contains("application/json")) || "XMLHttpRequest".equals(requestedWith);

            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
                BufferedReader br = new BufferedReader(isr);
                String query = br.readLine();
                Map<String, String> params = parseQueryParams(query);

                int userId = params.containsKey("userId") ? Integer.parseInt(params.get("userId")) : 1;
                int stockId = params.containsKey("stockId") ? Integer.parseInt(params.get("stockId")) : 1;
                int quantity = params.containsKey("quantity") ? Integer.parseInt(params.get("quantity")) : 100;
                String type = params.getOrDefault("type", "BUY");

                // 1. FACTORY PATTERN
                Transaction transaction = TransactionFactory.create(type);
                if (transaction != null) {
                    transaction.execute();
                }

                // 2. CHAIN OF RESPONSIBILITY PATTERN
                AgentHandler agent1 = new Agent1();
                AgentHandler agent2 = new Agent2();
                AgentHandler agent3 = new Agent3();
                agent1.setNext(agent2);
                agent2.setNext(agent3);
                int agentId = agent1.handle(quantity);

                // 3. PROXY PATTERN
                Trade trade = new Trade(userId, stockId, type, quantity);
                TradingService service = new TradingProxy();
                String result = service.trade(trade);

                // 4. SINGLETON / DATABASE PATTERN
                try {
                    TradeDAO dao = new TradeDAO();
                    dao.executeTrade(trade, agentId);
                } catch (Exception e) {
                    if (isAjax) {
                        String error = "{\"status\":\"error\",\"message\":\""
                                + e.getMessage() + "\"}";
                        exchange.getResponseHeaders().set(
                                "Content-Type", "application/json; charset=UTF-8");
                        byte[] bytes = error.getBytes(StandardCharsets.UTF_8);
                        exchange.sendResponseHeaders(400, bytes.length);
                        try (OutputStream os = exchange.getResponseBody()) {
                            os.write(bytes);
                        }
                        return;
                    }
                }

                // 5. OBSERVER PATTERN
                TradeSubject subject = new TradeSubject();
                subject.addObserver(new UserObserver());
                String notifyMsg = type + " transaction completed. Agent " + agentId + " handled the order.";
                subject.notifyUsers(notifyMsg);

                if (isAjax) {
                    String jsonResponse = "{"
                        + "\"status\":\"success\","
                        + "\"message\":\"" + result + "\","
                        + "\"agentId\":" + agentId + ","
                        + "\"userId\":" + userId + ","
                        + "\"stockId\":" + stockId + ","
                        + "\"type\":\"" + type + "\","
                        + "\"quantity\":" + quantity + ","
                        + "\"notification\":\"" + notifyMsg + "\""
                        + "}";

                    exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
                    byte[] bytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
                    exchange.sendResponseHeaders(200, bytes.length);
                    OutputStream os = exchange.getResponseBody();
                    os.write(bytes);
                    os.close();
                } else {
                    // Standard browser form submission redirect to dashboard.html
                    exchange.getResponseHeaders().set("Location", "/dashboard.html");
                    exchange.sendResponseHeaders(302, -1);
                }
            } else {
                exchange.getResponseHeaders().set("Location", "/dashboard.html");
                exchange.sendResponseHeaders(302, -1);
            }
        }
    }

    // Handler for /login
    static class LoginHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String accept = exchange.getRequestHeaders().getFirst("Accept");
            String requestedWith = exchange.getRequestHeaders().getFirst("X-Requested-With");
            boolean isAjax = (accept != null && accept.contains("application/json")) || "XMLHttpRequest".equals(requestedWith);

            if (isAjax) {
                String jsonResponse = "{\"status\":\"success\",\"user\":{\"id\":1,\"username\":\"Trader\",\"balance\":50000.00}}";
                exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
                byte[] bytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(200, bytes.length);
                OutputStream os = exchange.getResponseBody();
                os.write(bytes);
                os.close();
            } else {
                // Redirect form post straight to visual dashboard.html web page!
                exchange.getResponseHeaders().set("Location", "/dashboard.html");
                exchange.sendResponseHeaders(302, -1);
            }
        }
    }

    // Handler for /api/data
    static class DataHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                int userId = 1;
                com.tradeforge.model.User user = new UserDAO().findById(userId);

                if (user == null) {
                    sendJson(exchange, 401,
                            "{\"status\":\"error\",\"message\":\"User account not found\"}");
                    return;
                }

                StockDAO stockDAO = new StockDAO();
                TradeDAO tradeDAO = new TradeDAO();
                String json = "{"
                    + "\"user\":{"
                    + "\"id\":" + user.id + ","
                    + "\"username\":\"" + user.username + "\","
                    + "\"balance\":" + user.balance
                    + "},"
                    + "\"stocks\":" + stockDAO.getStocksJSON() + ","
                    + "\"transactions\":" + tradeDAO.getTransactionsJSON(user.id) + ","
                    + "\"portfolio\":" + tradeDAO.getPortfolioJSON(user.id)
                    + "}";

                sendJson(exchange, 200, json);
            } catch (Exception e) {
                sendJson(exchange, 500,
                        "{\"status\":\"error\",\"message\":\"Database unavailable\"}");
            }
        }
    }

    private static void sendJson(
            HttpExchange exchange,
            int status,
            String json) throws IOException {
        exchange.getResponseHeaders().set(
                "Content-Type", "application/json; charset=UTF-8");
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    // Handler for serving static HTML, CSS, JS files
    static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String pathStr = exchange.getRequestURI().getPath();
            if (pathStr.equals("/") || pathStr.equals("/login")) {
                pathStr = "/dashboard.html";
            }

            Path filePath = Paths.get(WEBAPP_DIR + pathStr);
            if (Files.exists(filePath) && !Files.isDirectory(filePath)) {
                String contentType = "text/html";
                if (pathStr.endsWith(".css")) contentType = "text/css";
                else if (pathStr.endsWith(".js")) contentType = "text/javascript";
                else if (pathStr.endsWith(".png")) contentType = "image/png";

                exchange.getResponseHeaders().set("Content-Type", contentType + "; charset=UTF-8");
                byte[] bytes = Files.readAllBytes(filePath);
                exchange.sendResponseHeaders(200, bytes.length);
                OutputStream os = exchange.getResponseBody();
                os.write(bytes);
                os.close();
            } else {
                // If requested page doesn't exist, serve dashboard.html
                Path dashPath = Paths.get(WEBAPP_DIR + "/dashboard.html");
                if (Files.exists(dashPath)) {
                    exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
                    byte[] bytes = Files.readAllBytes(dashPath);
                    exchange.sendResponseHeaders(200, bytes.length);
                    OutputStream os = exchange.getResponseBody();
                    os.write(bytes);
                    os.close();
                } else {
                    String notFound = "404 Not Found";
                    exchange.sendResponseHeaders(404, notFound.length());
                    OutputStream os = exchange.getResponseBody();
                    os.write(notFound.getBytes());
                    os.close();
                }
            }
        }
    }

    private static Map<String, String> parseQueryParams(String query) {
        Map<String, String> result = new HashMap<>();
        if (query == null || query.isEmpty()) return result;
        for (String param : query.split("&")) {
            String[] entry = param.split("=");
            if (entry.length > 1) {
                try {
                    result.put(URLDecoder.decode(entry[0], "UTF-8"), URLDecoder.decode(entry[1], "UTF-8"));
                } catch (Exception e) {
                    result.put(entry[0], entry[1]);
                }
            }
        }
        return result;
    }
}
