package com.fooddelivery;

import com.fooddelivery.db.AppException;
import com.fooddelivery.db.DBConnection;
import com.fooddelivery.model.*;
import com.fooddelivery.service.FoodDeliveryService;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Text-menu demo of JDBC connectivity (retrieval + DML) - works without the Swing UI.
 * Run this class in IntelliJ (green arrow next to main).
 */
public class ConsoleDemo {

    private static final FoodDeliveryService api = new FoodDeliveryService();
    private static final Scanner in = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("=== Food Delivery & Restaurant Order Management - JDBC demo ===");
        System.out.println(DBConnection.testConnection()
                ? "Connected to MySQL database 'food_delivery'.\n"
                : "COULD NOT CONNECT - check MySQL is running and db.properties has the right password.\n");

        while (true) {
            System.out.println("""
                    -------------------------------------------
                     1. Login (by email)
                     2. List restaurants                [SELECT]
                     3. Show a restaurant's menu        [SELECT]
                     4. Place an order                  [INSERT - transaction]
                     5. Customer order history          [SELECT - view]
                     6. Update order status             [UPDATE]
                     7. Assign delivery agent           [INSERT - procedure]
                     8. Mark delivery as delivered      [UPDATE - procedure]
                     9. Add a review                    [INSERT]
                    10. Add / edit / delete menu item   [INSERT / UPDATE / DELETE]
                    11. Reports                         [SELECT - views]
                     0. Exit
                    -------------------------------------------""");
            int choice = readInt("Choose: ");
            try {
                switch (choice) {
                    case 1 -> login();
                    case 2 -> api.getRestaurants().forEach(r ->
                            System.out.println("  [" + r.restaurantId() + "] " + r));
                    case 3 -> showMenu(readInt("Restaurant ID: "));
                    case 4 -> placeOrder();
                    case 5 -> api.getOrderHistory(readInt("Customer ID: ")).forEach(o ->
                            System.out.println("  " + o + " | " + o.paymentMethod() + " " + o.paymentStatus()));
                    case 6 -> {
                        int id = readInt("Order ID: ");
                        String status = readLine("New status (Placed/Preparing/Out for Delivery/Delivered): ");
                        api.updateOrderStatus(id, status);
                        System.out.println("  Updated: " + api.getOrder(id));
                    }
                    case 7 -> assignAgent();
                    case 8 -> {
                        api.markDelivered(readInt("Delivery ID: "));
                        System.out.println("  Delivery completed.");
                    }
                    case 9 -> {
                        api.addReview(readInt("Customer ID: "), readInt("Order ID: "), readInt("Rating (1-5): "));
                        System.out.println("  Review saved.");
                    }
                    case 10 -> menuCrud();
                    case 11 -> reports();
                    case 0 -> { System.out.println("Bye!"); return; }
                    default -> System.out.println("Invalid choice.");
                }
            } catch (AppException e) {
                System.out.println("  ERROR: " + e.getMessage());
            }
            System.out.println();
        }
    }

    private static void login() throws AppException {
        User u = api.login(readLine("Email: "));
        System.out.println("  Welcome " + u.fullName() + "! Role: " + u.role()
                + " | User ID: " + u.userId() + " | Phones: " + u.phones());
        if ("MANAGER".equals(u.role())) {
            System.out.println("  You manage: " + api.getMyRestaurant(u.userId()));
        } else if ("AGENT".equals(u.role())) {
            api.getMyDeliveries(u.userId()).forEach(d -> System.out.println("  " + d
                    + (d.collectCash() ? "  (collect Rs " + d.total() + " cash)" : "")));
        } else {
            api.getAddresses(u.userId()).forEach(a -> System.out.println("  " + a));
        }
    }

    private static void showMenu(int restaurantId) throws AppException {
        for (MenuItem m : api.getMenu(restaurantId)) {
            System.out.printf("  [%2d] %-22s %-12s Rs %8.2f%n", m.itemId(), m.itemName(), m.category(), m.price());
        }
    }

    private static void placeOrder() throws AppException {
        int customerId = readInt("Customer ID: ");
        api.getAddresses(customerId).forEach(a -> System.out.println("  " + a));
        int addressNo = readInt("Address number: ");
        int restaurantId = readInt("Restaurant ID: ");
        showMenu(restaurantId);

        Map<Integer, Integer> cart = new LinkedHashMap<>();
        while (true) {
            int item = readInt("Item ID (0 to finish): ");
            if (item == 0) break;
            cart.put(item, readInt("Quantity: "));
        }
        String coupon = readLine("Coupon code (Enter to skip): ");
        System.out.println("  Total to pay: Rs " + api.getTotal(cart, coupon));
        String method = readLine("Payment method (UPI/Card/COD): ");

        int orderId = api.placeOrder(customerId, addressNo, restaurantId, cart, coupon, method);
        System.out.println("  Order placed! " + api.getOrder(orderId));
        api.getOrderItems(orderId).forEach(l -> System.out.println(
                "    " + l.quantity() + " x " + l.itemName() + " = Rs " + l.lineTotal()));
    }

    private static void assignAgent() throws AppException {
        int orderId = readInt("Order ID (must be Preparing): ");
        List<User> free = api.getAvailableAgents();
        System.out.println("  Available agents:");
        free.forEach(a -> System.out.println("   [" + a.userId() + "] " + a.fullName()));
        int deliveryId = api.assignDelivery(orderId, readInt("Agent ID: "));
        System.out.println("  Delivery #" + deliveryId + " created. " + api.getOrder(orderId));
    }

    private static void menuCrud() throws AppException {
        String action = readLine("add / edit / delete: ").trim().toLowerCase();
        switch (action) {
            case "add" -> {
                int id = api.addMenuItem(readInt("Restaurant ID: "), readLine("Item name: "),
                        readDouble("Price: "), readLine("Category (Starter/Main Course/Combo/Snack/Dessert/Beverage): "));
                System.out.println("  Added item #" + id);
            }
            case "edit" -> {
                api.updateMenuItem(readInt("Item ID: "), readLine("New name: "),
                        readDouble("New price: "), readLine("New category: "));
                System.out.println("  Item updated.");
            }
            case "delete" -> {
                api.deleteMenuItem(readInt("Item ID: "));
                System.out.println("  Item deleted.");
            }
            default -> System.out.println("  Unknown action.");
        }
    }

    private static void reports() throws AppException {
        print("SALES PER RESTAURANT", api.getSalesReport());
        print("TOP ITEMS", api.getTopItems());
        print("RESTAURANT RATINGS", api.getRestaurantRatings());
        print("AGENT PERFORMANCE", api.getAgentPerformance());
    }

    private static void print(String title, List<String[]> rows) {
        System.out.println("\n  " + title);
        for (String[] row : rows) {
            StringBuilder sb = new StringBuilder("  ");
            for (String cell : row) sb.append(String.format("%-22s", cell));
            System.out.println(sb);
        }
    }

    private static String readLine(String prompt) {
        System.out.print(prompt);
        return in.hasNextLine() ? in.nextLine() : "";
    }

    private static int readInt(String prompt) {
        while (true) {
            String s = readLine(prompt).trim();
            try { return Integer.parseInt(s); }
            catch (NumberFormatException e) {
                if (!in.hasNextLine() && s.isEmpty()) return 0;
                System.out.println("  Please enter a number.");
            }
        }
    }

    private static double readDouble(String prompt) {
        while (true) {
            try { return Double.parseDouble(readLine(prompt).trim()); }
            catch (NumberFormatException e) { System.out.println("  Please enter a number."); }
        }
    }
}
