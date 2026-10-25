package com.fooddelivery.model;

/** avgRating is the derived attribute Avg_Rating (null if the restaurant has no reviews yet). */
public record Restaurant(int restaurantId, String name, int managerId, Double avgRating) {

    @Override
    public String toString() {
        return avgRating == null ? name : name + "  (" + avgRating + " ★)";
    }
}
