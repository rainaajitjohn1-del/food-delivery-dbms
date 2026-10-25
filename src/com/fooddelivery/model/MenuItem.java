package com.fooddelivery.model;

public record MenuItem(int itemId, String itemName, double price, String category, int restaurantId) {

    @Override
    public String toString() {
        return itemName + " - ₹" + String.format("%.2f", price);
    }
}
