package com.fooddelivery.dao;

import com.fooddelivery.db.AppException;
import com.fooddelivery.db.DBConnection;
import com.fooddelivery.model.Restaurant;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** RESTAURANT (+ derived Avg_Rating from the v_restaurant_rating view) */
public class RestaurantDAO {

    private static final String BASE_SQL = """
        SELECT r.Restaurant_ID, r.Rest_Name, r.Manager_ID, v.Avg_Rating
        FROM   RESTAURANT r
        JOIN   v_restaurant_rating v ON v.Restaurant_ID = r.Restaurant_ID""";

    public List<Restaurant> getRestaurants() throws AppException {
        List<Restaurant> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(BASE_SQL + " ORDER BY r.Rest_Name");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
            return list;
        } catch (SQLException e) {
            throw AppException.fromSQL(e);
        }
    }

    /** The restaurant managed by this manager (MANAGES is 1:1). */
    public Restaurant getMyRestaurant(int managerId) throws AppException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(BASE_SQL + " WHERE r.Manager_ID = ?")) {
            ps.setInt(1, managerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new AppException("No restaurant is linked to this manager.");
                return map(rs);
            }
        } catch (SQLException e) {
            throw AppException.fromSQL(e);
        }
    }

    private Restaurant map(ResultSet rs) throws SQLException {
        double rating = rs.getDouble("Avg_Rating");
        Double avg = rs.wasNull() ? null : rating;
        return new Restaurant(rs.getInt("Restaurant_ID"), rs.getString("Rest_Name"),
                rs.getInt("Manager_ID"), avg);
    }
}
