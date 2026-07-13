package za.ac.belgiumcampus;

import java.sql.*;

public class MaterialDAO {
    private final Connection conn;

    public MaterialDAO(Connection conn) {
        this.conn = conn;
    }

    // CREATE
    public void addMaterial(String name, String description, String unit, int quantity, int reorderLevel, double unitPrice, int supplierId) throws SQLException {
        String sql = "INSERT INTO materials (material_name, description, unit_of_measure, quantity, reorder_level, unit_price, supplier_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, name);
        stmt.setString(2, description);
        stmt.setString(3, unit);
        stmt.setInt(4, quantity);
        stmt.setInt(5, reorderLevel);
        stmt.setDouble(6, unitPrice);
        stmt.setInt(7, supplierId);
        stmt.executeUpdate();
    }

    // READ
    public void listMaterials() throws SQLException {
        String sql = "SELECT material_id, material_name, quantity, unit_price FROM materials";
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        while (rs.next()) {
            System.out.println(rs.getInt("material_id") + " - " + rs.getString("material_name") +
                    " | Qty: " + rs.getInt("quantity") +
                    " | Price: " + rs.getDouble("unit_price"));
        }
    }

    // UPDATE
    public void updateQuantity(int materialId, int newQuantity) throws SQLException {
        String sql = "UPDATE materials SET quantity = ? WHERE material_id = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, newQuantity);
        stmt.setInt(2, materialId);
        stmt.executeUpdate();
    }

    // DELETE
    public void deleteMaterial(int materialId) throws SQLException {
        String sql = "DELETE FROM materials WHERE material_id = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, materialId);
        stmt.executeUpdate();
    }
}
