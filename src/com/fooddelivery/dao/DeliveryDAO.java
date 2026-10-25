package com.fooddelivery.dao;

import com.fooddelivery.db.AppException;
import com.fooddelivery.db.DBConnection;
import com.fooddelivery.model.Delivery;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** DELIVERY */
public class DeliveryDAO {

    /**
     * Assigns an agent via sp_assign_delivery. The triggers check that the order
     * is Preparing and the agent is free, then move the order to Out for Delivery.
     * Returns the new Delivery_ID.
     */
    public int assignDelivery(int orderId, int agentId) throws AppException {
        try (Connection con = DBConnection.getConnection();
             CallableStatement cs = con.prepareCall("{CALL sp_assign_delivery(?, ?, ?)}")) {
            cs.setInt(1, orderId);
            cs.setInt(2, agentId);
            cs.registerOutParameter(3, Types.INTEGER);
            cs.execute();
            return cs.getInt(3);
        } catch (SQLException e) {
            throw AppException.fromSQL(e);
        }
    }

    /** An agent's deliveries: active ones first. */
    public List<Delivery> getMyDeliveries(int agentId) throws AppException {
        String sql = """
            SELECT d.Delivery_ID, d.Order_ID, d.Delivery_Status, d.Agent_ID,
                   s.Customer_Name, s.Delivery_City, s.Rest_Name, s.Total_Amount,
                   s.Payment_Method, s.Pay_Status
            FROM   DELIVERY d
            JOIN   v_order_summary s ON s.Order_ID = d.Order_ID
            WHERE  d.Agent_ID = ?
            ORDER  BY (d.Delivery_Status = 'Delivered'), d.Delivery_ID DESC""";
        List<Delivery> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, agentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Delivery(rs.getInt(1), rs.getInt(2), rs.getString(3), rs.getInt(4),
                            rs.getString(5), rs.getString(6), rs.getString(7), rs.getDouble(8),
                            rs.getString(9), rs.getString(10)));
                }
            }
            return list;
        } catch (SQLException e) {
            throw AppException.fromSQL(e);
        }
    }

    /** Agent completes a delivery: order -> Delivered, COD payment -> Paid (via triggers). */
    public void markDelivered(int deliveryId) throws AppException {
        try (Connection con = DBConnection.getConnection();
             CallableStatement cs = con.prepareCall("{CALL sp_mark_delivered(?)}")) {
            cs.setInt(1, deliveryId);
            cs.execute();
        } catch (SQLException e) {
            throw AppException.fromSQL(e);
        }
    }
}
