package za.ac.belgiumcampus.dao;

import za.ac.belgiumcampus.DatabaseConnection;
import za.ac.belgiumcampus.model.DashboardSummary;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DashboardDAO {

    public DashboardSummary getSummary() throws SQLException {
        String sql = "SELECT total_materials, low_stock_items, total_cleaners, recent_issuances "
                + "FROM vw_dashboard_summary";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return new DashboardSummary(
                        rs.getInt("total_materials"),
                        rs.getInt("low_stock_items"),
                        rs.getInt("total_cleaners"),
                        rs.getInt("recent_issuances")
                );
            }
            return new DashboardSummary(0, 0, 0, 0);
        }
    }
}
