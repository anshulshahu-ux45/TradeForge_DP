package com.tradeforge;

import java.util.Scanner;

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
import com.tradeforge.model.TradeResult;
import com.tradeforge.model.User;

public class ConsoleApp {

    private final Scanner scanner = new Scanner(System.in);
    private final UserDAO userDAO = new UserDAO();
    private final StockDAO stockDAO = new StockDAO();
    private final TradeDAO tradeDAO = new TradeDAO();

    public static void main(String[] args) {
        new ConsoleApp().run();
    }

    private void run() {
        printBanner();

        User user = login();
        if (user == null) {
            System.out.println("Unable to log in. Check the database and credentials.");
            return;
        }

        System.out.println("Logged in as " + user.username + ".");

        boolean running = true;
        while (running) {
            printMenu(user);
            String choice = scanner.nextLine().trim();

            try {
                switch (choice) {
                    case "1":
                        showStocks();
                        break;
                    case "2":
                        user = executeTrade(user, "BUY");
                        break;
                    case "3":
                        user = executeTrade(user, "SELL");
                        break;
                    case "4":
                        showPortfolio(user);
                        break;
                    case "5":
                        showTransactions(user);
                        break;
                    case "6":
                        user = refreshUser(user);
                        System.out.printf("Current balance: %.2f%n", user.balance);
                        break;
                    case "0":
                        running = false;
                        break;
                    default:
                        System.out.println("Choose one of the listed options.");
                }
            } catch (Exception e) {
                System.out.println("Operation failed: " + e.getMessage());
            }
        }

        System.out.println("Goodbye.");
    }

    private User login() {
        System.out.print("Username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine();
        return userDAO.login(username, password);
    }

    private void printMenu(User user) {
        System.out.println();
        System.out.println("TradeForge | " + user.username +
                " | Balance: " + String.format("%.2f", user.balance));
        System.out.println("1. View stocks");
        System.out.println("2. Buy stock");
        System.out.println("3. Sell stock");
        System.out.println("4. View portfolio");
        System.out.println("5. View transactions");
        System.out.println("6. Refresh balance");
        System.out.println("0. Exit");
        System.out.print("Select: ");
    }

    private void showStocks() {
        System.out.println("\nAvailable stocks:");
        System.out.println(stockDAO.getStocksJSON());
    }

    private User executeTrade(User user, String type) throws Exception {
        System.out.println("\n" + type + " order");
        int stockId = readPositiveInt("Stock ID: ");
        int quantity = readPositiveInt("Quantity: ");

        Transaction transaction = TransactionFactory.create(type);
        if (transaction == null) {
            throw new IllegalArgumentException("Invalid transaction type");
        }
        transaction.execute();

        int agentId = routeToAgent(quantity);
        Trade trade = new Trade(user.id, stockId, type, quantity);
        TradeResult result = tradeDAO.executeTrade(trade, agentId);

        System.out.printf(
                "%s completed. Agent %d handled the order. Amount: %.2f, Balance: %.2f%n",
                type, agentId, result.amount, result.balance);

        user.balance = result.balance;
        return user;
    }

    private void showPortfolio(User user) {
        System.out.println("\nPortfolio:");
        System.out.println(tradeDAO.getPortfolioJSON(user.id));
    }

    private void showTransactions(User user) {
        System.out.println("\nTransactions:");
        System.out.println(tradeDAO.getTransactionsJSON(user.id));
    }

    private User refreshUser(User user) {
        User refreshed = userDAO.findById(user.id);
        return refreshed == null ? user : refreshed;
    }

    private int routeToAgent(int quantity) {
        AgentHandler agent1 = new Agent1();
        AgentHandler agent2 = new Agent2();
        AgentHandler agent3 = new Agent3();
        agent1.setNext(agent2);
        agent2.setNext(agent3);
        return agent1.handle(quantity);
    }

    private int readPositiveInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                int value = Integer.parseInt(scanner.nextLine().trim());
                if (value > 0) {
                    return value;
                }
            } catch (NumberFormatException ignored) {
                // Ask again for invalid console input.
            }
            System.out.println("Enter a positive whole number.");
        }
    }

    private void printBanner() {
        System.out.println("========================================");
        System.out.println("          TRADEFORGE CONSOLE             ");
        System.out.println("========================================");
    }
}