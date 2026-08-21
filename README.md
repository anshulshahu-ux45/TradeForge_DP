TradeForge is a design pattern-based stock trading management system that simulates stock buying and selling through a network of authorized trading agents. The project demonstrates the practical implementation of five software design patterns: Singleton, Proxy, Factory Method, Chain of Responsibility, and Observer. It uses a database to manage stocks, customers, agents, orders, transactions, and watchlists, providing a structured and scalable architecture for simulated stock trading.
Design Patterns Used:
🔐 Proxy – Agent authentication and authorization
🏭 Factory Method – Creation of Buy/Sell orders
🔗 Chain of Responsibility – Agent-level request handling
📦 Singleton – Centralized stock market management
🔔 Observer – Stock price change notifications

Console Experience
------------------
Build the project with Maven, then launch the terminal client from PowerShell:

	mvn clean package -DskipTests
	java -cp "target/classes;$env:USERPROFILE\.m2\repository\com\mysql\mysql-connector-j\9.4.0\mysql-connector-j-9.4.0.jar" com.tradeforge.ConsoleApp

The console client supports database login, stock viewing, BUY and SELL orders,
portfolio viewing, transaction history, and balance refresh. It uses the same
atomic database trade workflow as the web application.
