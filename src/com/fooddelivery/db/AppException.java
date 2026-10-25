package com.fooddelivery.db;

import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The ONLY exception the front end needs to catch.
 * getMessage() always returns a sentence that can be shown directly to the user,
 * e.g. JOptionPane.showMessageDialog(this, ex.getMessage());
 *
 * fromSQL() converts raw MySQL errors (trigger messages, CHECK / UNIQUE /
 * FOREIGN KEY violations, connection failures) into readable messages.
 */
public class AppException extends Exception {

    public AppException(String message) {
        super(message);
    }

    public AppException(String message, Throwable cause) {
        super(message, cause);
    }

    // constraint name -> user-friendly message
    private static final Map<String, String> FRIENDLY = new LinkedHashMap<>();
    static {
        // UNIQUE (error 1062)
        FRIENDLY.put("uq_users_email",      "This email is already registered.");
        FRIENDLY.put("pk_user_phone",       "This phone number is already saved for this user.");
        FRIENDLY.put("uq_item_per_rest",    "This restaurant already has an item with that name.");
        FRIENDLY.put("uq_review_order",     "This order has already been reviewed.");
        FRIENDLY.put("uq_delivery_order",   "A delivery agent is already assigned to this order.");
        FRIENDLY.put("uq_agent_vehicle",    "This vehicle number is already registered.");
        FRIENDLY.put("uq_payment_order",    "A payment already exists for this order.");
        // CHECK (error 3819)
        FRIENDLY.put("chk_phone_10dig",     "Phone number must be a valid 10-digit mobile number.");
        FRIENDLY.put("chk_users_email",     "Please enter a valid email address.");
        FRIENDLY.put("chk_item_price",      "Price must be greater than 0.");
        FRIENDLY.put("chk_item_category",   "Category must be Starter, Main Course, Dessert, Beverage, Snack or Combo.");
        FRIENDLY.put("chk_review_rating",   "Rating must be between 1 and 5.");
        FRIENDLY.put("chk_oi_quantity",     "Quantity of each item must be between 1 and 20.");
        FRIENDLY.put("chk_pay_method",      "Payment method must be UPI, Card or COD.");
        FRIENDLY.put("chk_order_status",    "Invalid order status.");
        FRIENDLY.put("chk_coupon_discount", "Coupon discount must be between 1 and 50 percent.");
        // FOREIGN KEY (errors 1451 / 1452)
        FRIENDLY.put("fk_orders_coupon",    "Invalid coupon code.");
        FRIENDLY.put("fk_orders_address",   "Please choose one of your saved addresses.");
        FRIENDLY.put("fk_orders_customer",  "Customer account not found.");
        FRIENDLY.put("fk_orders_restaurant","Restaurant not found.");
        FRIENDLY.put("fk_oi_item",          "One of the selected items does not exist.");
        FRIENDLY.put("fk_delivery_agent",   "Selected delivery agent does not exist.");
        FRIENDLY.put("fk_delivery_order",   "Order not found.");
        FRIENDLY.put("fk_review_order",     "Order not found.");
        FRIENDLY.put("fk_review_customer",  "Customer account not found.");
        FRIENDLY.put("fk_address_customer", "Customer account not found.");
    }

    public static AppException fromSQL(SQLException e) {
        String raw = e.getMessage() == null ? "" : e.getMessage();
        // some drivers prefix messages with "(conn=12) "
        String msg = raw.replaceFirst("^\\(conn=\\d+\\)\\s*", "");
        String state = e.getSQLState() == null ? "" : e.getSQLState();
        int code = e.getErrorCode();

        // 1. Business-rule violations raised by our triggers / procedures
        if ("45000".equals(state)) {
            return new AppException(msg, e);
        }
        // 2. Cannot reach the database / wrong password
        if (code == 1045 || msg.contains("Access denied")) {
            return new AppException("MySQL rejected the username or password in db.properties.", e);
        }
        if (state.startsWith("08") || msg.contains("Communications link failure")
                || msg.contains("Could not connect") || msg.contains("Public Key Retrieval")) {
            return new AppException("Cannot connect to the database. Check that MySQL is running "
                    + "and the password in db.properties is correct.", e);
        }
        // 3. Deleting a row that other rows still depend on
        if (code == 1451) {
            if (msg.contains("fk_oi_item")) {
                return new AppException("This item appears in past orders, so it cannot be deleted.", e);
            }
            return new AppException("This record is used elsewhere and cannot be deleted.", e);
        }
        // 4. Known constraint names
        for (Map.Entry<String, String> entry : FRIENDLY.entrySet()) {
            if (msg.contains(entry.getKey())) {
                return new AppException(entry.getValue(), e);
            }
        }
        // 5. Generic fallbacks
        if (code == 1062) return new AppException("This record already exists.", e);
        if (code == 3819) return new AppException("Some of the values entered are not allowed.", e);
        if (code == 1452) return new AppException("A referenced record does not exist.", e);
        if (code == 1048) return new AppException("Please fill in all required fields.", e);
        return new AppException("Database error: " + msg, e);
    }
}
