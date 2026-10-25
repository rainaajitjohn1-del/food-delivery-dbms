package com.fooddelivery.dao;

import com.fooddelivery.db.AppException;
import com.fooddelivery.db.DBConnection;
import com.fooddelivery.model.Address;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** ADDRESS (weak entity owned by CUSTOMER) */
public class AddressDAO {

    public List<Address> getAddresses(int customerId) throws AppException {
        List<Address> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT Customer_ID, Address_No, City FROM ADDRESS "
                   + "WHERE Customer_ID = ? ORDER BY Address_No")) {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Address(rs.getInt(1), rs.getInt(2), rs.getString(3)));
                }
            }
            return list;
        } catch (SQLException e) {
            throw AppException.fromSQL(e);
        }
    }

    /** Adds an address; Address_No (the partial key) is the next number for this customer. */
    public Address addAddress(int customerId, String city) throws AppException {
        if (UserDAO.isBlank(city)) {
            throw new AppException("Please enter a city.");
        }
        String sql = """
            INSERT INTO ADDRESS (Customer_ID, Address_No, City)
            SELECT ?, COALESCE(MAX(Address_No), 0) + 1, ?
            FROM   ADDRESS WHERE Customer_ID = ?""";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            ps.setString(2, city.trim());
            ps.setInt(3, customerId);
            ps.executeUpdate();
            List<Address> all = getAddresses(customerId);
            return all.get(all.size() - 1);
        } catch (SQLException e) {
            throw AppException.fromSQL(e);
        }
    }
}
