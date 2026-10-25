package com.fooddelivery.model;

/**
 * One order as shown on screen (built from the v_order_summary view).
 * total is the derived attribute Total_Amount (subtotal minus coupon discount).
 * rating is null if the order has not been reviewed.
 */
public record Order(int orderId, int customerId, String customerName, String deliveryCity,
                    int restaurantId, String restaurantName, String status,
                    double subtotal, String couponCode, double discountPercent, double total,
                    String paymentMethod, String paymentStatus, Integer rating) {

    public boolean canBeReviewed() {
        return "Delivered".equals(status) && rating == null;
    }

    /** The next status in the workflow, or null if the order is already Delivered. */
    public String nextStatus() {
        return switch (status) {
            case "Placed" -> "Preparing";
            case "Preparing" -> "Out for Delivery";
            case "Out for Delivery" -> "Delivered";
            default -> null;
        };
    }

    @Override
    public String toString() {
        return "Order #" + orderId + " - " + restaurantName + " - " + status
                + " - ₹" + String.format("%.2f", total);
    }
}
