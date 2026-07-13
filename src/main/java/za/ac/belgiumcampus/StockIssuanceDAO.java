package za.ac.belgiumcampus;

import java.sql.*;

public class StockIssuanceDAO {
    private final Connection conn;

    public StockIssuanceDAO(Connection conn) {
        this.conn = conn;
    }

    // CREATE
    public void issueMaterial(int materialId, int cleanerId, int issuedBy, int quantity, String notes) throws SQLException {
        String sql = "INSERT INTO stock_issuances (material_id, cleaner_id, issued_by, quantity_issued, issuance_date, notes) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP, ?)";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, materialId);
        stmt.setInt(2, cleanerId);
        stmt.setInt(3, issuedBy);
        stmt.setInt(4, quantity);
        stmt.setString(5, notes);
        stmt.executeUpdate();
    }

    // READ
    public void listIssuances() throws SQLException {
        String sql = "SELECT issuance_id, material_id, cleaner_id, quantity_issued, issuance_date, notes FROM stock_issuances";
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        while (rs.next()) {
            System.out.println("Issuance #" + rs.getInt("issuance_id") +
                    " | Material: " + rs.getInt("material_id") +
                    " | Cleaner: " + rs.getInt("cleaner_id") +
                    " | Qty: " + rs.getInt("quantity_issued") +
                    " | Date: " + rs.getTimestamp("issuance_date") +
                    " | Notes: " + rs.getString("notes"));
        }
    }
}
