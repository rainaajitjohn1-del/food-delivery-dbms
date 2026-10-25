package com.fooddelivery.model;

/** One item inside an order (a row of ORDER_ITEM joined with MENU_ITEM). */
public record OrderLine(int itemId, String itemName, int quantity, double price) {

    public double lineTotal() {
        return quantity * price;
    }
}
