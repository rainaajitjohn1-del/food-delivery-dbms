package com.fooddelivery.model;

/** A delivery as seen by the delivery agent. status is "Assigned" or "Delivered". */
public record Delivery(int deliveryId, int orderId, String status, int agentId,
                       String customerName, String deliveryCity, String restaurantName,
                       double total, String paymentMethod, String paymentStatus) {

    /** True if the agent must collect cash from the customer. */
    public boolean collectCash() {
        return "COD".equals(paymentMethod) && "Pending".equals(paymentStatus);
    }

    @Override
    public String toString() {
        return "Delivery #" + deliveryId + " - Order #" + orderId + " - " + deliveryCity + " - " + status;
    }
}
