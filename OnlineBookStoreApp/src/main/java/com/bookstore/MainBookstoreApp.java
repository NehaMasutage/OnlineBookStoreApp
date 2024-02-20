package com.bookstore;

import java.sql.*;
import java.util.Scanner;

public class MainBookstoreApp {
	private static String un = "root";
	private static String pass="root";
	private static String driver ="com.mysql.cj.jdbc.Driver";
	private static String url ="jdbc:mysql://localhost:3310/online_bookstore";
	

    public static void main(String[] args) {
        try (Connection conn = DriverManager.getConnection(url,un,pass)) {
            createTables(conn);

            Scanner scanner = new Scanner(System.in);

            while (true) {
                System.out.println("************MENU************");
                System.out.println("1. Add a Book");
                System.out.println("2. Register a Customer");
                System.out.println("3. Show All Books");
                System.out.println("4. Show All Customers");
                System.out.println("5. Place an Order");
                System.out.println("6. Exit");
                System.out.println("Enter your choice:");

                int choice = scanner.nextInt();
                scanner.nextLine(); // Consume newline

                switch (choice) {
                    case 1:
                        addBook(conn, scanner);
                        break;
                    case 2:
                        registerCustomer(conn, scanner);
                        break;
                    case 3:
                        showAllBooks(conn);
                        break;
                    case 4:
                        showAllCustomers(conn);
                        break;
                    case 5:
                        placeOrder(conn, scanner);
                        break;
                    case 6:
                        System.out.println("Exiting...");
                        System.exit(0);
                        break;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void createTables(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS books (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "title VARCHAR(255)," +
                    "author VARCHAR(255)," +
                    "price DECIMAL(10, 2))");

            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS customers (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "name VARCHAR(255)," +
                    "email VARCHAR(255))");

            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS orders (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "customer_id INT," +
                    "book_id INT," +
                    "FOREIGN KEY (customer_id) REFERENCES customers(id)," +
                    "FOREIGN KEY (book_id) REFERENCES books(id))");
        }
    }

    private static void addBook(Connection conn, Scanner scanner) throws SQLException {
        System.out.println("Enter Book Title:");
        String title = scanner.nextLine();
        System.out.println("Enter Book Author:");
        String author = scanner.nextLine();
        System.out.println("Enter Book Price:");
        double price = scanner.nextDouble();
        scanner.nextLine(); // Consume newline

        String sql = "INSERT INTO books (title, author, price) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, title);
            pstmt.setString(2, author);
            pstmt.setDouble(3, price);
            pstmt.executeUpdate();
            System.out.println("Book added successfully!");
        }
    }

    private static void registerCustomer(Connection conn, Scanner scanner) throws SQLException {
        System.out.println("Enter Customer Name:");
        String name = scanner.nextLine();
        System.out.println("Enter Customer Email:");
        String email = scanner.nextLine();

        String sql = "INSERT INTO customers (name, email) VALUES (?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, email);
            pstmt.executeUpdate();
            System.out.println("Customer registered successfully!");
        }
    }

    private static void showAllBooks(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM books")) {
            if (!rs.isBeforeFirst()) {
                System.out.println("No books available.");
            } else {
                System.out.println("Available Books:");
                //System.out.println("Title\tAuthor\tPrice");
                System.out.printf("%-40s %-30s %-10s", "Title","Author","Price");
                System.out.println();
        		System.out.println("-------------------------------------------------------------------------------------------");
                while (rs.next()) {
                    System.out.printf("%-40s %-30s %-10f",rs.getString("title"),rs.getString("author"),
                           rs.getDouble("price"));
                    System.out.println();
                }
            }
        }
    }

    private static void showAllCustomers(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM customers")) {
            if (!rs.isBeforeFirst()) {
                System.out.println("No customers registered.");
            } else {
                System.out.println("Registered Customers:");
                System.out.println("Title\tEmail");
        		System.out.println("----------------------------------------------------");
                while (rs.next()) {
                    System.out.println(rs.getString("name") +
                            "\t" + rs.getString("email"));
                }
            }
        }
    }

    private static void placeOrder(Connection conn, Scanner scanner) throws SQLException {
        System.out.println("Enter Customer Email:");
        String email = scanner.nextLine();

        String customerQuery = "SELECT id FROM customers WHERE email = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(customerQuery)) {
            pstmt.setString(1, email);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (!rs.isBeforeFirst()) {
                    System.out.println("Customer not found.");
                    return;
                }
                rs.next();
                int customerId = rs.getInt("id");

                showAllBooks(conn);
                System.out.println("Enter Book Title to order:");
                String title = scanner.nextLine();

                String bookQuery = "SELECT id FROM books WHERE title = ?";
                try (PreparedStatement pstmt2 = conn.prepareStatement(bookQuery)) {
                    pstmt2.setString(1, title);
                    try (ResultSet rs2 = pstmt2.executeQuery()) {
                        if (!rs2.isBeforeFirst()) {
                            System.out.println("Book not found.");
                            return;
                        }
                        rs2.next();
                        int bookId = rs2.getInt("id");

                        String insertOrderQuery = "INSERT INTO orders (customer_id, book_id) VALUES (?, ?)";
                        try (PreparedStatement pstmt3 = conn.prepareStatement(insertOrderQuery)) {
                            pstmt3.setInt(1, customerId);
                            pstmt3.setInt(2, bookId);
                            pstmt3.executeUpdate();
                            System.out.println("Order placed successfully!");
                        }
                    }
                }
            }
        }
    }
}
