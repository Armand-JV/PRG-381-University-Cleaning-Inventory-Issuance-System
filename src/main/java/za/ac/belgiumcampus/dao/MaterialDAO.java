package za.ac.belgiumcampus.dao;

import za.ac.belgiumcampus.DatabaseConnection;
import za.ac.belgiumcampus.exception.NegativeStockException;
import za.ac.belgiumcampus.exception.ValidationException;
import za.ac.belgiumcampus.model.Material;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Data-access layer for cleaning materials. Every method opens its own
 * connection via {@link DatabaseConnection#getConnection()} and closes it
 * in a try-with-resources block, so the DAO is safe to call repeatedly
 * from Swing action listeners without leaking connections.
 *
 * Validation (required fields, non-negative numbers) is enforced here as
 * well as in the view, so the rule holds no matter what calls this class -
 * the same defence-in-depth approach already used for stock issuance in
 * 006_create_stock_issuances.sql.
 *
 * @author user
 */
public class MaterialDAO implements GenericDAO<Material> {

    private static final String SELECT_BASE =
            "SELECT m.material_id, m.material_name, m.description, m.unit_of_measure, "
            + "m.quantity, m.reorder_level, m.unit_price, m.supplier_id, s.supplier_name, "
            + "m.is_active, m.created_at, m.updated_at "
            + "FROM materials m LEFT JOIN suppliers s ON s.supplier_id = m.supplier_id ";

    @Override
    public Material add(Material material) throws SQLException, ValidationException {
        validate(material);

        String sql = "INSERT INTO materials "
                + "(material_name, description, unit_of_measure, quantity, reorder_level, unit_price, supplier_id) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            bindMaterialFields(stmt, material);
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    material.setMaterialId(keys.getInt(1));
                }
            }
            return material;
        }
    }

    @Override
    public Material getById(int id) throws SQLException {
        String sql = SELECT_BASE + "WHERE m.material_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    @Override
    public List<Material> getAll() throws SQLException {
        String sql = SELECT_BASE + "WHERE m.is_active = TRUE ORDER BY m.material_name";
        List<Material> materials = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                materials.add(mapRow(rs));
            }
        }
        return materials;
    }

    /**
     * Search and filter: matches on material name (case-insensitive,
     * partial match) - backs the search box on the Materials screen.
     */
    public List<Material> search(String keyword) throws SQLException {
        String sql = SELECT_BASE + "WHERE m.is_active = TRUE AND m.material_name ILIKE ? "
                + "ORDER BY m.material_name";
        List<Material> materials = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + keyword + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    materials.add(mapRow(rs));
                }
            }
        }
        return materials;
    }

    /**
     * Low-stock alert logic: any active material whose quantity has fallen
     * to, or below, its reorder level. Backs the red-row highlighting in
     * MaterialsFrame and feeds the Dashboard's "Low-stock items" count and
     * the Low-Stock report (same rule as vw_low_stock_materials).
     */
    public List<Material> getLowStockMaterials() throws SQLException {
        String sql = SELECT_BASE + "WHERE m.is_active = TRUE AND m.quantity <= m.reorder_level "
                + "ORDER BY m.quantity ASC";
        List<Material> materials = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                materials.add(mapRow(rs));
            }
        }
        return materials;
    }

    @Override
    public void update(Material material) throws SQLException, ValidationException {
        validate(material);

        String sql = "UPDATE materials SET material_name = ?, description = ?, unit_of_measure = ?, "
                + "quantity = ?, reorder_level = ?, unit_price = ?, supplier_id = ? "
                + "WHERE material_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            bindMaterialFields(stmt, material);
            stmt.setInt(8, material.getMaterialId());

            int rows = stmt.executeUpdate();
            if (rows == 0) {
                throw new ValidationException("Material #" + material.getMaterialId() + " no longer exists.");
            }
        }
    }

    /**
     * "Delete" is implemented as a soft delete (is_active = FALSE) rather
     * than a hard DELETE. Materials that have already been issued to a
     * cleaner are protected by a NOT NULL/RESTRICT foreign key from
     * stock_issuances (006_create_stock_issuances.sql), so removing the
     * row outright would either fail or destroy issuance history. Soft
     * deleting keeps history intact while removing the item from every
     * active list, dropdown, and report.
     */
    @Override
    public void delete(int id) throws SQLException {
        String sql = "UPDATE materials SET is_active = FALSE WHERE material_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    // ---------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------

    private void bindMaterialFields(PreparedStatement stmt, Material m) throws SQLException {
        stmt.setString(1, m.getMaterialName());
        stmt.setString(2, m.getDescription());
        stmt.setString(3, m.getUnitOfMeasure());
        stmt.setInt(4, m.getQuantity());
        stmt.setInt(5, m.getReorderLevel());
        stmt.setBigDecimal(6, m.getUnitPrice());
        if (m.getSupplierId() == null) {
            stmt.setNull(7, java.sql.Types.INTEGER);
        } else {
            stmt.setInt(7, m.getSupplierId());
        }
    }

    private Material mapRow(ResultSet rs) throws SQLException {
        Timestamp createdAt = rs.getTimestamp("created_at");
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        int supplierId = rs.getInt("supplier_id");

        return new Material(
                rs.getInt("material_id"),
                rs.getString("material_name"),
                rs.getString("description"),
                rs.getString("unit_of_measure"),
                rs.getInt("quantity"),
                rs.getInt("reorder_level"),
                rs.getBigDecimal("unit_price"),
                rs.wasNull() ? null : supplierId,
                rs.getString("supplier_name"),
                rs.getBoolean("is_active"),
                createdAt == null ? null : createdAt.toLocalDateTime(),
                updatedAt == null ? null : updatedAt.toLocalDateTime()
        );
    }

    /**
     * Validation & business rules, enforced before any statement is sent
     * to the database:
     *  - required fields must not be blank
     *  - quantity, reorder level and unit price may never be negative
     *  - unit price must parse as a proper monetary value
     */
    private void validate(Material m) throws ValidationException {
        if (m.getMaterialName() == null || m.getMaterialName().isBlank()) {
            throw new ValidationException("Material name is required.");
        }
        if (m.getUnitOfMeasure() == null || m.getUnitOfMeasure().isBlank()) {
            throw new ValidationException("Unit of measure is required.");
        }
        if (m.getQuantity() < 0) {
            throw new NegativeStockException("Quantity cannot be negative.");
        }
        if (m.getReorderLevel() < 0) {
            throw new NegativeStockException("Reorder level cannot be negative.");
        }
        if (m.getUnitPrice() == null || m.getUnitPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("Unit price cannot be negative.");
        }
    }
}
