package za.ac.belgiumcampus.dao;

import za.ac.belgiumcampus.DatabaseConnection;
import za.ac.belgiumcampus.exception.InsufficientStockException;
import za.ac.belgiumcampus.exception.ValidationException;
import za.ac.belgiumcampus.model.Material;
import za.ac.belgiumcampus.model.MaterialUsage;
import za.ac.belgiumcampus.model.StockIssuance;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class StockIssuanceDAO {

    private static final String SELECT_HISTORY =
            "SELECT si.issuance_id, si.material_id, m.material_name, si.cleaner_id, c.full_name AS cleaner_name, "
            + "si.issued_by, u.full_name AS issued_by_name, si.quantity_issued, si.issuance_date, si.notes "
            + "FROM stock_issuances si "
            + "JOIN materials m ON m.material_id = si.material_id "
            + "JOIN cleaners c ON c.cleaner_id = si.cleaner_id "
            + "LEFT JOIN users u ON u.user_id = si.issued_by ";

    public void issueMaterial(StockIssuance issuance) throws SQLException, ValidationException {
        validate(issuance);
        checkAvailability(issuance.getMaterialId(), issuance.getQuantityIssued());

        String sql = "INSERT INTO stock_issuances (material_id, cleaner_id, issued_by, quantity_issued, notes) "
                + "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, issuance.getMaterialId());
            stmt.setInt(2, issuance.getCleanerId());
            if (issuance.getIssuedBy() == null) {
                stmt.setNull(3, Types.INTEGER);
            } else {
                stmt.setInt(3, issuance.getIssuedBy());
            }
            stmt.setInt(4, issuance.getQuantityIssued());
            stmt.setString(5, issuance.getNotes());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new InsufficientStockException("Could not issue stock: " + e.getMessage());
        }
    }

    public List<StockIssuance> getAll() throws SQLException {
        String sql = SELECT_HISTORY + "ORDER BY si.issuance_date DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            return mapRows(rs);
        }
    }

    public List<StockIssuance> getRecent(int limit) throws SQLException {
        String sql = SELECT_HISTORY + "ORDER BY si.issuance_date DESC LIMIT ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, limit);
            try (ResultSet rs = stmt.executeQuery()) {
                return mapRows(rs);
            }
        }
    }

    public List<MaterialUsage> getMaterialUsage() throws SQLException {
        String sql = "SELECT material_id, material_name, total_issued FROM vw_material_usage "
                + "ORDER BY total_issued DESC";
        List<MaterialUsage> usage = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                usage.add(new MaterialUsage(
                        rs.getInt("material_id"),
                        rs.getString("material_name"),
                        rs.getInt("total_issued")
                ));
            }
        }
        return usage;
    }

    private List<StockIssuance> mapRows(ResultSet rs) throws SQLException {
        List<StockIssuance> issuances = new ArrayList<>();
        while (rs.next()) {
            issuances.add(mapRow(rs));
        }
        return issuances;
    }

    private StockIssuance mapRow(ResultSet rs) throws SQLException {
        Timestamp issuanceDate = rs.getTimestamp("issuance_date");
        int issuedBy = rs.getInt("issued_by");
        boolean issuedByIsNull = rs.wasNull();

        return new StockIssuance(
                rs.getInt("issuance_id"),
                rs.getInt("material_id"),
                rs.getString("material_name"),
                rs.getInt("cleaner_id"),
                rs.getString("cleaner_name"),
                issuedByIsNull ? null : issuedBy,
                rs.getString("issued_by_name"),
                rs.getInt("quantity_issued"),
                issuanceDate == null ? null : issuanceDate.toLocalDateTime(),
                rs.getString("notes")
        );
    }

    private void checkAvailability(int materialId, int quantityRequested) throws SQLException, ValidationException {
        Material material = new MaterialDAO().getById(materialId);
        if (material == null) {
            throw new ValidationException("Selected material no longer exists.");
        }
        if (quantityRequested > material.getQuantity()) {
            throw new InsufficientStockException(
                    "Cannot issue " + quantityRequested + " " + material.getUnitOfMeasure()
                    + " of \"" + material.getMaterialName() + "\": only " + material.getQuantity()
                    + " " + material.getUnitOfMeasure() + " in stock.");
        }
    }

    private void validate(StockIssuance issuance) throws ValidationException {
        if (issuance.getMaterialId() <= 0) {
            throw new ValidationException("Select a material to issue.");
        }
        if (issuance.getCleanerId() <= 0) {
            throw new ValidationException("Select a cleaner to issue to.");
        }
        if (issuance.getQuantityIssued() <= 0) {
            throw new ValidationException("Quantity issued must be greater than zero.");
        }
    }
}
