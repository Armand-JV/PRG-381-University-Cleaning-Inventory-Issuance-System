package za.ac.belgiumcampus;
import java.sql.*;
    public class SupplierDAO {
        private Connection conn;

        public SupplierDAO(Connection conn) {
            this.conn = conn;
        }

        // CREATE
        public void addSupplier(String name, String contact, String phone, String email, String address) throws SQLException {
            String sql = "INSERT INTO suppliers (supplier_name, contact_person, phone, email, address) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, name);
            stmt.setString(2, contact);
            stmt.setString(3, phone);
            stmt.setString(4, email);
            stmt.setString(5, address);
            stmt.executeUpdate();
        }

        // READ
        public void listSuppliers() throws SQLException {
            String sql = "SELECT supplier_id, supplier_name, email FROM suppliers";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                System.out.println(rs.getInt("supplier_id") + " - " + rs.getString("supplier_name") + " (" + rs.getString("email") + ")");
            }
        }

        // UPDATE
        public void updateSupplierEmail(int supplierId, String newEmail) throws SQLException {
            String sql = "UPDATE suppliers SET email = ? WHERE supplier_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, newEmail);
            stmt.setInt(2, supplierId);
            stmt.executeUpdate();
        }

        // DELETE
        public void deleteSupplier(int supplierId) throws SQLException {
            String sql = "DELETE FROM suppliers WHERE supplier_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, supplierId);
            stmt.executeUpdate();
        }
    }


