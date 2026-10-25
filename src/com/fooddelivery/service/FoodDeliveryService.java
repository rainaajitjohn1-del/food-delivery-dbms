package com.fooddelivery.service;

import com.fooddelivery.dao.*;
import com.fooddelivery.db.AppException;
import com.fooddelivery.model.*;

import java.util.List;
import java.util.Map;

/**
 * THE ONE CLASS THE FRONT END USES.
 *
 *   FoodDeliveryService api = new FoodDeliveryService();
 *   try {
 *       List<Restaurant> list = api.getRestaurants();
 *   } catch (AppException ex) {
 *       JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
 *   }
 *
 * Every method talks to MySQL through JDBC (DAO classes in com.fooddelivery.dao).
 * No SQL should ever be written inside the UI code.
 */
public class FoodDeliveryService {

    private final UserDAO users = new UserDAO();
    private final AddressDAO addresses = new AddressDAO();
    private final RestaurantDAO restaurants = new RestaurantDAO();
    private final MenuItemDAO menu = new MenuItemDAO();
    private final OrderDAO orders = new OrderDAO();
    private final DeliveryDAO deliveries = new DeliveryDAO();
    private final ReviewDAO reviews = new ReviewDAO();
    private final ReportDAO reports = new ReportDAO();

    public static final String[] CATEGORIES =
            {"Starter", "Main Course", "Combo", "Snack", "Dessert", "Beverage"};
    public static final String[] PAYMENT_METHODS = {"UPI", "Card", "COD"};

    // ------------------------------------------------------------ Login / registration
    /** user.role() is "CUSTOMER", "MANAGER" or "AGENT". */
    public User login(String email) throws AppException { return users.login(email); }

    /** Returns the new customer's User_ID. */
    public int registerCustomer(String first, String last, String email, String phone) throws AppException {
        return users.registerCustomer(first, last, email, phone);
    }

    public void addPhone(int userId, String phone) throws AppException { users.addPhone(userId, phone); }

    // ------------------------------------------------------------ Customer
    public List<Restaurant> getRestaurants() throws AppException { return restaurants.getRestaurants(); }

    public List<MenuItem> getMenu(int restaurantId) throws AppException { return menu.getMenu(restaurantId); }

    public List<Address> getAddresses(int customerId) throws AppException { return addresses.getAddresses(customerId); }

    public Address addAddress(int customerId, String city) throws AppException {
        return addresses.addAddress(customerId, city);
    }

    /** itemQty: Item_ID -> quantity. couponCode may be null/empty. */
    public double getTotal(Map<Integer, Integer> itemQty, String couponCode) throws AppException {
        return orders.getTotal(itemQty, couponCode);
    }

    /** Returns the new Order_ID. payMethod is "UPI", "Card" or "COD". */
    public int placeOrder(int customerId, int addressNo, int restaurantId,
                          Map<Integer, Integer> itemQty, String couponCode, String payMethod)
            throws AppException {
        return orders.placeOrder(customerId, addressNo, restaurantId, itemQty, couponCode, payMethod);
    }

    public List<Order> getOrderHistory(int customerId) throws AppException { return orders.getOrderHistory(customerId); }

    public Order getOrder(int orderId) throws AppException { return orders.getOrder(orderId); }

    public List<OrderLine> getOrderItems(int orderId) throws AppException { return orders.getOrderItems(orderId); }

    public void addReview(int customerId, int orderId, int rating) throws AppException {
        reviews.addReview(customerId, orderId, rating);
    }

    // ------------------------------------------------------------ Restaurant manager
    public Restaurant getMyRestaurant(int managerId) throws AppException { return restaurants.getMyRestaurant(managerId); }

    /** Returns the new Item_ID. */
    public int addMenuItem(int restaurantId, String name, double price, String category) throws AppException {
        return menu.addMenuItem(restaurantId, name, price, category);
    }

    public void updateMenuItem(int itemId, String name, double price, String category) throws AppException {
        menu.updateMenuItem(itemId, name, price, category);
    }

    public void deleteMenuItem(int itemId) throws AppException { menu.deleteMenuItem(itemId); }

    public List<Order> getRestaurantOrders(int restaurantId) throws AppException {
        return orders.getRestaurantOrders(restaurantId);
    }

    /** Use order.nextStatus() to know which status to send. To go Out for Delivery, use assignDelivery. */
    public void updateOrderStatus(int orderId, String newStatus) throws AppException {
        orders.updateOrderStatus(orderId, newStatus);
    }

    public List<User> getAvailableAgents() throws AppException { return users.getAvailableAgents(); }

    /** Order must be Preparing; it automatically becomes Out for Delivery. Returns Delivery_ID. */
    public int assignDelivery(int orderId, int agentId) throws AppException {
        return deliveries.assignDelivery(orderId, agentId);
    }

    // ------------------------------------------------------------ Delivery agent
    public List<Delivery> getMyDeliveries(int agentId) throws AppException { return deliveries.getMyDeliveries(agentId); }

    public void markDelivered(int deliveryId) throws AppException { deliveries.markDelivered(deliveryId); }

    // ------------------------------------------------------------ Reports (first row = headers)
    public List<String[]> getSalesReport() throws AppException { return reports.getSalesReport(); }

    public List<String[]> getTopItems() throws AppException { return reports.getTopItems(); }

    public List<String[]> getRestaurantRatings() throws AppException { return reports.getRestaurantRatings(); }

    public List<String[]> getAgentPerformance() throws AppException { return reports.getAgentPerformance(); }
}
