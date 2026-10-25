package com.fooddelivery.dao;

import com.fooddelivery.db.AppException;
import com.fooddelivery.db.DBConnection;
import com.fooddelivery.model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** USERS, USER_PHONE, CUSTOMER, DELIVERY_AGENT, RESTAURANT_MANAGER */
public class UserDAO {

    /** Login by email. The role comes from whichever subclass table the user belongs to. */
    public User login(String email) throws AppException {
        String sql = """
            SELECT u.User_ID, u.First_Name, u.Last_Name, u.Email,
                   CASE WHEN c.Customer_ID IS NOT NULL THEN 'CUSTOMER'
                        WHEN m.Manager_ID  IS NOT NULL THEN 'MANAGER'
                        WHEN a.Agent_ID    IS NOT NULL THEN 'AGENT'
                   END AS Role
            FROM   USERS u
            LEFT JOIN CUSTOMER c           ON c.Customer_ID = u.User_ID
            LEFT JOIN RESTAURANT_MANAGER m ON m.Manager_ID  = u.User_ID
            LEFT JOIN DELIVERY_AGENT a     ON a.Agent_ID    = u.User_ID
            WHERE  u.Email = ?""";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email.trim().toLowerCase());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new AppException("No account found with this email.");
                }
                int id = rs.getInt("User_ID");
                return new User(id, rs.getString("First_Name"), rs.getString("Last_Name"),
                        rs.getString("Email"), rs.getString("Role"), getPhones(con, id));
            }
        } catch (SQLException e) {
            throw AppException.fromSQL(e);
        }
    }

    private List<String> getPhones(Connection con, int userId) throws SQLException {
        List<String> phones = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT Phone FROM USER_PHONE WHERE User_ID = ? ORDER BY Phone")) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) phones.add(rs.getString(1));
            }
        }
        return phones;
    }

    /**
     * Registers a new customer as ONE transaction:
     * USERS -> CUSTOMER -> USER_PHONE. If any insert fails, nothing is saved.
     * Returns the new User_ID.
     */
    public int registerCustomer(String firstName, String lastName, String email, String phone)
            throws AppException {
        if (isBlank(firstName) || isBlank(lastName) || isBlank(email) || isBlank(phone)) {
            throw new AppException("Please fill in all fields.");
        }
        Connection con = null;
        try {
            con = DBConnection.getConnection();
            con.setAutoCommit(false);

            int userId;
            try (PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO USERS (First_Name, Last_Name, Email) VALUES (?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, firstName.trim());
                ps.setString(2, lastName.trim());
                ps.setString(3, email.trim().toLowerCase());
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    keys.next();
                    userId = keys.getInt(1);
                }
            }
            try (PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO CUSTOMER (Customer_ID) VALUES (?)")) {
                ps.setInt(1, userId);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO USER_PHONE (User_ID, Phone) VALUES (?, ?)")) {
                ps.setInt(1, userId);
                ps.setString(2, phone.trim());
                ps.executeUpdate();
            }
            con.commit();
            return userId;
        } catch (SQLException e) {
            rollback(con);
            throw AppException.fromSQL(e);
        } finally {
            close(con);
        }
    }

    /** Adds an extra phone number (multivalued attribute). */
    public void addPhone(int userId, String phone) throws AppException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "INSERT INTO USER_PHONE (User_ID, Phone) VALUES (?, ?)")) {
            ps.setInt(1, userId);
            ps.setString(2, phone.trim());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw AppException.fromSQL(e);
        }
    }

    /** Delivery agents who are NOT currently on an active delivery. */
    public List<User> getAvailableAgents() throws AppException {
        String sql = """
            SELECT u.User_ID, u.First_Name, u.Last_Name, u.Email
            FROM   DELIVERY_AGENT a
            JOIN   USERS u ON u.User_ID = a.Agent_ID
            WHERE  NOT EXISTS (SELECT 1 FROM DELIVERY d
                               WHERE d.Agent_ID = a.Agent_ID
                                 AND d.Delivery_Status = 'Assigned')
            ORDER  BY u.First_Name""";
        List<User> agents = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                agents.add(new User(rs.getInt(1), rs.getString(2), rs.getString(3),
                        rs.getString(4), "AGENT", List.of()));
            }
            return agents;
        } catch (SQLException e) {
            throw AppException.fromSQL(e);
        }
    }

    static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    static void rollback(Connection con) {
        if (con != null) {
            try { con.rollback(); } catch (SQLException ignored) { }
        }
    }

    static void close(Connection con) {
        if (con != null) {
            try { con.close(); } catch (SQLException ignored) { }
        }
    }
}
