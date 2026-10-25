package com.fooddelivery.dao;

import com.fooddelivery.db.AppException;
import com.fooddelivery.db.DBConnection;
import com.fooddelivery.model.MenuItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** MENU_ITEM - full CRUD for restaurant managers */
public class MenuItemDAO {

    public List<MenuItem> getMenu(int restaurantId) throws AppException {
        List<MenuItem> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT Item_ID, Item_Name, Price, Category, Restaurant_ID FROM MENU_ITEM "
                   + "WHERE Restaurant_ID = ? "
                   + "ORDER BY FIELD(Category,'Starter','Main Course','Combo','Snack','Dessert','Beverage'), Item_Name")) {
            ps.setInt(1, restaurantId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new MenuItem(rs.getInt(1), rs.getString(2), rs.getDouble(3),
                            rs.getString(4), rs.getInt(5)));
                }
            }
            return list;
        } catch (SQLException e) {
            throw AppException.fromSQL(e);
        }
    }

    /** CREATE - returns the new Item_ID */
    public int addMenuItem(int restaurantId, String name, double price, String category)
            throws AppException {
        if (UserDAO.isBlank(name)) throw new AppException("Please enter an item name.");
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "INSERT INTO MENU_ITEM (Item_Name, Price, Category, Restaurant_ID) VALUES (?, ?, ?, ?)",
                     Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name.trim());
            ps.setDouble(2, price);
            ps.setString(3, category);
            ps.setInt(4, restaurantId);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        } catch (SQLException e) {
            throw AppException.fromSQL(e);
        }
    }

    /** UPDATE */
    public void updateMenuItem(int itemId, String name, double price, String category)
            throws AppException {
        if (UserDAO.isBlank(name)) throw new AppException("Please enter an item name.");
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "UPDATE MENU_ITEM SET Item_Name = ?, Price = ?, Category = ? WHERE Item_ID = ?")) {
            ps.setString(1, name.trim());
            ps.setDouble(2, price);
            ps.setString(3, category);
            ps.setInt(4, itemId);
            if (ps.executeUpdate() == 0) throw new AppException("Menu item not found.");
        } catch (SQLException e) {
            throw AppException.fromSQL(e);
        }
    }

    /** DELETE - blocked by the foreign key if the item appears in any order */
    public void deleteMenuItem(int itemId) throws AppException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("DELETE FROM MENU_ITEM WHERE Item_ID = ?")) {
            ps.setInt(1, itemId);
            if (ps.executeUpdate() == 0) throw new AppException("Menu item not found.");
        } catch (SQLException e) {
            throw AppException.fromSQL(e);
        }
    }
}
