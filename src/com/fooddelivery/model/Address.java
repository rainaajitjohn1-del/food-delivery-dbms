package com.fooddelivery.model;

/** Weak entity: identified by (customerId, addressNo). */
public record Address(int customerId, int addressNo, String city) {

    @Override
    public String toString() {
        return "Address " + addressNo + " - " + city;
    }
}
