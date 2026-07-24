
package za.ac.belgiumcampus.view;

import za.ac.belgiumcampus.dao.MaterialDAO;
import za.ac.belgiumcampus.dao.StockIssuanceDAO;
import za.ac.belgiumcampus.model.Material;
import za.ac.belgiumcampus.model.MaterialUsage;
import za.ac.belgiumcampus.model.StockIssuance;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReportsFrame extends JFrame {

    private static final Logger logger = Logger.getLogger(ReportsFrame.class.getName());
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");

    private static final String INVENTORY_REPORT = "Inventory Report";
    private static final String LOW_STOCK_REPORT = "Low-Stock Report";
    private static final String ISSUANCE_HISTORY_REPORT = "Issuance History Report";
    private static final String MATERIAL_USAGE_REPORT = "Material Usage Report";

    private final MaterialDAO materialDAO = new MaterialDAO();
    private final StockIssuanceDAO issuanceDAO = new StockIssuanceDAO();

    private JComboBox<String> cbReportType;
    private DefaultTableModel tableModel;
    private JTable table;
    private JLabel lblStatus;

    public ReportsFrame() {
        setTitle("Reports - University Cleaning Inventory System");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel content = new JPanel(new BorderLayout(10, 10));
        content.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.add(buildHeading());
        top.add(buildToolbar());
        content.add(top, BorderLayout.NORTH);

        tableModel = new DefaultTableModel();
        table = new JTable(tableModel);
        table.setRowHeight(22);
        content.add(new JScrollPane(table), BorderLayout.CENTER);

        lblStatus = new JLabel(" ");
        lblStatus.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
        content.add(lblStatus, BorderLayout.SOUTH);

        setContentPane(content);

        setSize(950, 620);
        setLocationRelativeTo(null);

        generateReport();
    }

    private JPanel buildHeading() {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel heading = new JLabel("Reports");
        heading.setFont(new Font("SansSerif", Font.BOLD, 20));
        panel.add(heading, BorderLayout.WEST);
        return panel;
    }

    private JPanel buildToolbar() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.add(new JLabel("Report:"));

        cbReportType = new JComboBox<>(new String[]{
                INVENTORY_REPORT, LOW_STOCK_REPORT, ISSUANCE_HISTORY_REPORT, MATERIAL_USAGE_REPORT
        });
        panel.add(cbReportType);

        JButton btnGenerate = new JButton("Generate");
        btnGenerate.addActionListener(e -> generateReport());
        panel.add(btnGenerate);

        JButton btnExport = new JButton("Export to CSV");
        btnExport.addActionListener(e -> exportToCsv());
        panel.add(btnExport);

        return panel;
    }

    private void generateReport() {
        String reportType = (String) cbReportType.getSelectedItem();
        try {
            if (INVENTORY_REPORT.equals(reportType)) {
                loadInventoryReport();
            } else if (LOW_STOCK_REPORT.equals(reportType)) {
                loadLowStockReport();
            } else if (ISSUANCE_HISTORY_REPORT.equals(reportType)) {
                loadIssuanceHistoryReport();
            } else if (MATERIAL_USAGE_REPORT.equals(reportType)) {
                loadMaterialUsageReport();
            }
            setStatus(tableModel.getRowCount() + " row(s).", false);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Unable to generate report: " + reportType, e);
            setStatus("Could not generate report: " + e.getMessage(), true);
        }
    }

    private void loadInventoryReport() throws SQLException {
        List<Material> materials = materialDAO.getAll();
        tableModel = new DefaultTableModel(new String[]{
                "ID", "Material", "Unit", "Quantity", "Reorder Level", "Unit Price", "Supplier", "Status"
        }, 0);
        for (Material m : materials) {
            tableModel.addRow(new Object[]{
                    m.getMaterialId(), m.getMaterialName(), m.getUnitOfMeasure(), m.getQuantity(),
                    m.getReorderLevel(), "R " + m.getUnitPrice().setScale(2, RoundingMode.HALF_UP),
                    m.getSupplierName() == null ? "-" : m.getSupplierName(),
                    m.isLowStock() ? "LOW STOCK" : "OK"
            });
        }
        table.setModel(tableModel);
    }

    private void loadLowStockReport() throws SQLException {
        List<Material> materials = materialDAO.getLowStockMaterials();
        tableModel = new DefaultTableModel(new String[]{
                "ID", "Material", "Unit", "Quantity", "Reorder Level", "Supplier"
        }, 0);
        for (Material m : materials) {
            tableModel.addRow(new Object[]{
                    m.getMaterialId(), m.getMaterialName(), m.getUnitOfMeasure(), m.getQuantity(),
                    m.getReorderLevel(), m.getSupplierName() == null ? "-" : m.getSupplierName()
            });
        }
        table.setModel(tableModel);
    }

    private void loadIssuanceHistoryReport() throws SQLException {
        List<StockIssuance> issuances = issuanceDAO.getAll();
        tableModel = new DefaultTableModel(new String[]{
                "ID", "Material", "Cleaner", "Quantity", "Date", "Issued By", "Notes"
        }, 0);
        for (StockIssuance si : issuances) {
            tableModel.addRow(new Object[]{
                    si.getIssuanceId(), si.getMaterialName(), si.getCleanerName(), si.getQuantityIssued(),
                    si.getIssuanceDate() == null ? "-" : si.getIssuanceDate().format(DATE_FORMAT),
                    si.getIssuedByName() == null ? "-" : si.getIssuedByName(),
                    si.getNotes() == null ? "" : si.getNotes()
            });
        }
        table.setModel(tableModel);
    }

    private void loadMaterialUsageReport() throws SQLException {
        List<MaterialUsage> usage = issuanceDAO.getMaterialUsage();
        tableModel = new DefaultTableModel(new String[]{"Material", "Total Quantity Issued"}, 0);
        for (MaterialUsage u : usage) {
            tableModel.addRow(new Object[]{u.getMaterialName(), u.getTotalIssued()});
        }
        table.setModel(tableModel);
    }

    private void exportToCsv() {
        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Nothing to export - generate a report first.");
            return;
        }

        JFileChooser chooser = new JFileChooser();
        String suggestedName = ((String) cbReportType.getSelectedItem()).replace(" ", "_") + ".csv";
        chooser.setSelectedFile(new File(suggestedName));

        int result = chooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        try (FileWriter writer = new FileWriter(chooser.getSelectedFile())) {
            int columnCount = tableModel.getColumnCount();
            for (int col = 0; col < columnCount; col++) {
                writer.append(escapeCsv(tableModel.getColumnName(col)));
                writer.append(col < columnCount - 1 ? "," : "\n");
            }
            for (int row = 0; row < tableModel.getRowCount(); row++) {
                for (int col = 0; col < columnCount; col++) {
                    Object value = tableModel.getValueAt(row, col);
                    writer.append(escapeCsv(value == null ? "" : value.toString()));
                    writer.append(col < columnCount - 1 ? "," : "\n");
                }
            }
            setStatus("Report exported to " + chooser.getSelectedFile().getName(), false);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Unable to export report", e);
            setStatus("Could not export report: " + e.getMessage(), true);
        }
    }


    private String escapeCsv(String value) {
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private void setStatus(String message, boolean isError) {
        lblStatus.setText(message);
        lblStatus.setForeground(isError ? new Color(0xB3, 0x1E, 0x1E) : new Color(0x1E, 0x6B, 0x2E));
    }
}
