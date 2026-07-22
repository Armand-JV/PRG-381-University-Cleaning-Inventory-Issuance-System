 package za.ac.belgiumcampus.dao;

import za.ac.belgiumcampus.DatabaseConnection;
import za.ac.belgiumcampus.model.Supplier;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Read-only lookup for suppliers, used only to populate the "Supplier"
 * dropdown on the Materials Management screen. Full supplier CRUD
 * (contact person, phone, address) belongs to the Suppliers Management
 * module, which owns the rest of the suppliers table.
 *
 * @author user
 */
public class SupplierDAO {

    public void addSupplier(Supplier supplier) throws SQLException {
        String sql = "INSERT INTO suppliers (supplier_name, contact_person, phone, email, location, is_active) "
                + "VALUES (?, ?, ?, ?, ?, TRUE)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, supplier.getSupplierName());
            stmt.setString(2, supplier.getContactPerson());
            stmt.setString(3, supplier.getPhone());
            stmt.setString(4, supplier.getEmail());
            stmt.setString(5, supplier.getLocation());
            stmt.executeUpdate();
        }
    }

    public void updateSupplier(Supplier supplier) throws SQLException {
        String sql = "UPDATE suppliers SET supplier_name=?, contact_person=?, phone=?, email=?, location=? "
                + "WHERE supplier_id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, supplier.getSupplierName());
            stmt.setString(2, supplier.getContactPerson());
            stmt.setString(3, supplier.getPhone());
            stmt.setString(4, supplier.getEmail());
            stmt.setString(5, supplier.getLocation());
            stmt.setInt(6, supplier.getSupplierId());
            stmt.executeUpdate();
        }
    }

    public void deleteSupplier(int supplierId) throws SQLException {
        String sql = "DELETE FROM suppliers WHERE supplier_id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, supplierId);
            stmt.executeUpdate();
        }
    }

    public List<Supplier> getAllSuppliers() throws SQLException {
        String sql = "SELECT supplier_id, supplier_name, contact_person, phone, email, location FROM suppliers";
        List<Supplier> suppliers = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                suppliers.add(new Supplier(
                        rs.getInt("supplier_id"),
                        rs.getString("supplier_name"),
                        rs.getString("contact_person"),
                        rs.getString("phone"),
                        rs.getString("email"),
                        rs.getString("location")
                ));
            }
        }
        return suppliers;
    }

    /**
     * Used by the Materials Management "Supplier" dropdown, which should
     * only offer suppliers that are still active.
     */
    public List<Supplier> getAllActive() throws SQLException {
        String sql = "SELECT supplier_id, supplier_name, contact_person, phone, email, location "
                + "FROM suppliers WHERE is_active = TRUE";
        List<Supplier> suppliers = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                suppliers.add(new Supplier(
                        rs.getInt("supplier_id"),
                        rs.getString("supplier_name"),
                        rs.getString("contact_person"),
                        rs.getString("phone"),
                        rs.getString("email"),
                        rs.getString("location")
                ));
            }
        }
        return suppliers;
    }

}
