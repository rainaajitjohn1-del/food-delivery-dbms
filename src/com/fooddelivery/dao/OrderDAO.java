package com.fooddelivery.dao;

import com.fooddelivery.db.AppException;
import com.fooddelivery.db.DBConnection;
import com.fooddelivery.model.Order;
import com.fooddelivery.model.OrderLine;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

/** ORDERS, ORDER_ITEM, PAYMENT, COUPON */
public class OrderDAO {

    private static final String SUMMARY_SQL = """
        SELECT s.*, rv.Rating
        FROM   v_order_summary s
        LEFT JOIN REVIEW rv ON rv.Order_ID = s.Order_ID""";

    /**
     * Live total for the cart screen: SUM(price x qty) minus the coupon's % discount.
     * itemQty maps Item_ID -> quantity. Throws if the coupon code does not exist.
     */
    public double getTotal(Map<Integer, Integer> itemQty, String couponCode) throws AppException {
        if (itemQty == null || itemQty.isEmpty()) return 0;
        StringJoiner placeholders = new StringJoiner(",");
        itemQty.keySet().forEach(k -> placeholders.add("?"));

        try (Connection con = DBConnection.getConnection()) {
            double subtotal = 0;
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT Item_ID, Price FROM MENU_ITEM WHERE Item_ID IN (" + placeholders + ")")) {
                int i = 1;
                for (Integer id : itemQty.keySet()) ps.setInt(i++, id);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        subtotal += rs.getDouble("Price") * itemQty.get(rs.getInt("Item_ID"));
                    }
                }
            }
            double discount = 0;
            if (!UserDAO.isBlank(couponCode)) {
                try (PreparedStatement ps = con.prepareStatement(
                        "SELECT Discount FROM COUPON WHERE Coupon_Code = ?")) {
                    ps.setString(1, couponCode.trim().toUpperCase());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) throw new AppException("Invalid coupon code.");
                        discount = rs.getDouble(1);
                    }
                }
            }
            return Math.round(subtotal * (1 - discount / 100) * 100) / 100.0;
        } catch (SQLException e) {
            throw AppException.fromSQL(e);
        }
    }

    /**
     * Places an order through the stored procedure sp_place_order, which inserts
     * ORDERS + ORDER_ITEM + PAYMENT in ONE transaction (all-or-nothing).
     * Returns the new Order_ID.
     */
    public int placeOrder(int customerId, int addressNo, int restaurantId,
                          Map<Integer, Integer> itemQty, String couponCode, String payMethod)
            throws AppException {
        if (itemQty == null || itemQty.isEmpty()) {
            throw new AppException("Your cart is empty.");
        }
        if (UserDAO.isBlank(payMethod)) {
            throw new AppException("Please choose a payment method.");
        }
        // build JSON like [{"item":1,"qty":2},{"item":2,"qty":1}]
        StringJoiner json = new StringJoiner(",", "[", "]");
        itemQty.forEach((item, qty) -> json.add("{\"item\":" + item + ",\"qty\":" + qty + "}"));

        try (Connection con = DBConnection.getConnection();
             CallableStatement cs = con.prepareCall("{CALL sp_place_order(?, ?, ?, ?, ?, ?, ?)}")) {
            cs.setInt(1, customerId);
            cs.setInt(2, addressNo);
            cs.setInt(3, restaurantId);
            if (UserDAO.isBlank(couponCode)) cs.setNull(4, Types.VARCHAR);
            else cs.setString(4, couponCode.trim().toUpperCase());
            cs.setString(5, payMethod);
            cs.setString(6, json.toString());
            cs.registerOutParameter(7, Types.INTEGER);
            cs.execute();
            return cs.getInt(7);
        } catch (SQLException e) {
            throw AppException.fromSQL(e);
        }
    }

    /** All orders of one customer, newest first. */
    public List<Order> getOrderHistory(int customerId) throws AppException {
        return queryOrders(SUMMARY_SQL + " WHERE s.Customer_ID = ? ORDER BY s.Order_ID DESC", customerId);
    }

    /** All orders received by one restaurant: active ones first, then newest first. */
    public List<Order> getRestaurantOrders(int restaurantId) throws AppException {
        return queryOrders(SUMMARY_SQL + " WHERE s.Restaurant_ID = ? "
                + "ORDER BY (s.Order_Status = 'Delivered'), s.Order_ID DESC", restaurantId);
    }

    public Order getOrder(int orderId) throws AppException {
        List<Order> list = queryOrders(SUMMARY_SQL + " WHERE s.Order_ID = ?", orderId);
        if (list.isEmpty()) throw new AppException("Order not found.");
        return list.get(0);
    }

    /** Items inside one order (for an order-details screen). */
    public List<OrderLine> getOrderItems(int orderId) throws AppException {
        List<OrderLine> lines = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("""
                 SELECT m.Item_ID, m.Item_Name, oi.Quantity, m.Price
                 FROM   ORDER_ITEM oi JOIN MENU_ITEM m ON m.Item_ID = oi.Item_ID
                 WHERE  oi.Order_ID = ? ORDER BY m.Item_Name""")) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lines.add(new OrderLine(rs.getInt(1), rs.getString(2), rs.getInt(3), rs.getDouble(4)));
                }
            }
            return lines;
        } catch (SQLException e) {
            throw AppException.fromSQL(e);
        }
    }

    /** Status changes are validated by the trg_orders_status_flow trigger. */
    public void updateOrderStatus(int orderId, String newStatus) throws AppException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "UPDATE ORDERS SET Order_Status = ? WHERE Order_ID = ?")) {
            ps.setString(1, newStatus);
            ps.setInt(2, orderId);
            if (ps.executeUpdate() == 0) throw new AppException("Order not found.");
        } catch (SQLException e) {
            throw AppException.fromSQL(e);
        }
    }

    private List<Order> queryOrders(String sql, int param) throws AppException {
        List<Order> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, param);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int rating = rs.getInt("Rating");
                    Integer r = rs.wasNull() ? null : rating;
                    list.add(new Order(
                            rs.getInt("Order_ID"), rs.getInt("Customer_ID"),
                            rs.getString("Customer_Name"), rs.getString("Delivery_City"),
                            rs.getInt("Restaurant_ID"), rs.getString("Rest_Name"),
                            rs.getString("Order_Status"), rs.getDouble("Subtotal"),
                            rs.getString("Coupon_Code"), rs.getDouble("Discount_Percent"),
                            rs.getDouble("Total_Amount"), rs.getString("Payment_Method"),
                            rs.getString("Pay_Status"), r));
                }
            }
            return list;
        } catch (SQLException e) {
            throw AppException.fromSQL(e);
        }
    }
}
