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

    public List<Supplier> getAllActive() throws SQLException {
        String sql = "SELECT supplier_id, supplier_name FROM suppliers "
                + "WHERE is_active = TRUE ORDER BY supplier_name";
        List<Supplier> suppliers = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                suppliers.add(new Supplier(rs.getInt("supplier_id"), rs.getString("supplier_name")));
            }
        }
        return suppliers;
    }
}
