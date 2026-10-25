package com.fooddelivery.dao;

import com.fooddelivery.db.AppException;
import com.fooddelivery.db.DBConnection;

import java.sql.*;

/** REVIEW */
public class ReviewDAO {

    /** trg_review_before_insert checks the order is Delivered and belongs to this customer. */
    public int addReview(int customerId, int orderId, int rating) throws AppException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "INSERT INTO REVIEW (Rating, Customer_ID, Order_ID) VALUES (?, ?, ?)",
                     Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, rating);
            ps.setInt(2, customerId);
            ps.setInt(3, orderId);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        } catch (SQLException e) {
            throw AppException.fromSQL(e);
        }
    }
}
