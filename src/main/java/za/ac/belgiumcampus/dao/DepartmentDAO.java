package za.ac.belgiumcampus.dao;

import java.sql.*;

public class DepartmentDAO {
    private final Connection conn;

    public DepartmentDAO(Connection conn) {
        this.conn = conn;
    }

    public void addDepartment(String name) throws SQLException {
        String sql = "INSERT INTO departments (department_name) VALUES (?)";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, name);
        stmt.executeUpdate();
    }

    public void listDepartments() throws SQLException {
        String sql = "SELECT department_id, department_name FROM departments";
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        while (rs.next()) {
            System.out.println(rs.getInt("department_id") + " - " + rs.getString("department_name"));
        }
    }
}
