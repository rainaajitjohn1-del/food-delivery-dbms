package com.fooddelivery.dao;

import com.fooddelivery.db.AppException;
import com.fooddelivery.db.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Sales and performance reports, read from the views in 03_business_logic.sql.
 * Every report returns List<String[]> where the FIRST row is the column headers,
 * so it can go straight into a JTable.
 */
public class ReportDAO {

    public List<String[]> getSalesReport() throws AppException {
        return run("SELECT Rest_Name AS Restaurant, Delivered_Orders AS `Delivered Orders`, "
                 + "Total_Revenue AS `Revenue (Rs)` FROM v_restaurant_sales ORDER BY Total_Revenue DESC");
    }

    public List<String[]> getTopItems() throws AppException {
        return run("SELECT Item_Name AS Item, Rest_Name AS Restaurant, Units_Sold AS `Units Sold`, "
                 + "Gross_Sales AS `Sales (Rs)` FROM v_top_items ORDER BY Units_Sold DESC, Gross_Sales DESC LIMIT 10");
    }

    public List<String[]> getRestaurantRatings() throws AppException {
        return run("SELECT Rest_Name AS Restaurant, COALESCE(Avg_Rating, '-') AS `Avg Rating`, "
                 + "Total_Reviews AS Reviews FROM v_restaurant_rating ORDER BY Avg_Rating DESC");
    }

    public List<String[]> getAgentPerformance() throws AppException {
        return run("SELECT Agent_Name AS Agent, Vehicle_No AS Vehicle, Completed, Active "
                 + "FROM v_agent_performance ORDER BY Completed DESC");
    }

    private List<String[]> run(String sql) throws AppException {
        List<String[]> rows = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            ResultSetMetaData md = rs.getMetaData();
            int n = md.getColumnCount();
            String[] header = new String[n];
            for (int i = 0; i < n; i++) header[i] = md.getColumnLabel(i + 1);
            rows.add(header);
            while (rs.next()) {
                String[] row = new String[n];
                for (int i = 0; i < n; i++) row[i] = rs.getString(i + 1);
                rows.add(row);
            }
            return rows;
        } catch (SQLException e) {
            throw AppException.fromSQL(e);
        }
    }
}
