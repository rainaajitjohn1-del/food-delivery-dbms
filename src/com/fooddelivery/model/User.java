package com.fooddelivery.model;

import java.util.List;

/** A logged-in user. role is "CUSTOMER", "MANAGER" or "AGENT" (disjoint specialization of USER). */
public record User(int userId, String firstName, String lastName, String email,
                   String role, List<String> phones) {

    public String fullName() {
        return firstName + " " + lastName;
    }

    @Override
    public String toString() {
        return fullName() + " (" + email + ")";
    }
}
