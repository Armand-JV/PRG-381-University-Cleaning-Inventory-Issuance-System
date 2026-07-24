package za.ac.belgiumcampus.view;

import za.ac.belgiumcampus.dao.DashboardDAO;
import za.ac.belgiumcampus.dao.StockIssuanceDAO;
import za.ac.belgiumcampus.model.DashboardSummary;
import za.ac.belgiumcampus.model.StockIssuance;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridLayout;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DashboardPanel extends JPanel {

    private static final Logger logger = Logger.getLogger(DashboardPanel.class.getName());
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");

    private static final Color COLOR_MATERIALS = new Color(0x2D, 0x5B, 0xA6);
    private static final Color COLOR_LOW_STOCK = new Color(0xB3, 0x1E, 0x1E);
    private static final Color COLOR_CLEANERS = new Color(0x1E, 0x6B, 0x2E);
    private static final Color COLOR_ISSUANCES = new Color(0x8A, 0x5A, 0x00);

    private final DashboardDAO dashboardDAO = new DashboardDAO();
    private final StockIssuanceDAO issuanceDAO = new StockIssuanceDAO();

    private JLabel lblTotalMaterials;
    private JLabel lblLowStock;
    private JLabel lblTotalCleaners;
    private JLabel lblRecentIssuances;
    private JLabel lblStatus;
    private DefaultTableModel recentTableModel;

    public DashboardPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        add(buildHeading(), BorderLayout.NORTH);
        add(buildCenter(), BorderLayout.CENTER);

        refresh();
    }

    private JPanel buildHeading() {
        JPanel panel = new JPanel(new BorderLayout());

        JLabel heading = new JLabel("Dashboard");
        heading.setFont(new Font("SansSerif", Font.BOLD, 20));

        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.addActionListener(e -> refresh());

        panel.add(heading, BorderLayout.WEST);
        panel.add(btnRefresh, BorderLayout.EAST);
        return panel;
    }

    private JPanel buildCenter() {
        JPanel center = new JPanel(new BorderLayout(10, 10));

        JPanel cards = new JPanel(new GridLayout(1, 4, 10, 10));
        cards.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        lblTotalMaterials = new JLabel("0");
        lblLowStock = new JLabel("0");
        lblTotalCleaners = new JLabel("0");
        lblRecentIssuances = new JLabel("0");

        cards.add(buildStatCard("Total Materials", lblTotalMaterials, COLOR_MATERIALS));
        cards.add(buildStatCard("Low-Stock Items", lblLowStock, COLOR_LOW_STOCK));
        cards.add(buildStatCard("Total Cleaners", lblTotalCleaners, COLOR_CLEANERS));
        cards.add(buildStatCard("Issuances (7 days)", lblRecentIssuances, COLOR_ISSUANCES));

        center.add(cards, BorderLayout.NORTH);
        center.add(buildRecentIssuancesPanel(), BorderLayout.CENTER);

        lblStatus = new JLabel(" ");
        lblStatus.setBorder(BorderFactory.createEmptyBorder(6, 0, 0, 0));
        center.add(lblStatus, BorderLayout.SOUTH);

        return center;
    }

    private JPanel buildStatCard(String title, JLabel valueLabel, Color color) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color, 2),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)));
        card.setBackground(Color.WHITE);

        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 30));
        valueLabel.setForeground(color);
        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        titleLabel.setForeground(Color.DARK_GRAY);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(valueLabel);
        card.add(Box.createVerticalStrut(4));
        card.add(titleLabel);
        return card;
    }

    private JPanel buildRecentIssuancesPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 5));

        JLabel label = new JLabel("Recent Stock Issuances");
        label.setFont(new Font("SansSerif", Font.BOLD, 14));
        panel.add(label, BorderLayout.NORTH);

        recentTableModel = new DefaultTableModel(
                new String[]{"Date", "Material", "Cleaner", "Qty", "Issued By"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(recentTableModel);
        table.setRowHeight(22);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        return panel;
    }

    public void refresh() {
        try {
            DashboardSummary summary = dashboardDAO.getSummary();
            lblTotalMaterials.setText(String.valueOf(summary.getTotalMaterials()));
            lblLowStock.setText(String.valueOf(summary.getLowStockItems()));
            lblTotalCleaners.setText(String.valueOf(summary.getTotalCleaners()));
            lblRecentIssuances.setText(String.valueOf(summary.getRecentIssuances()));

            List<StockIssuance> recent = issuanceDAO.getRecent(10);
            recentTableModel.setRowCount(0);
            for (StockIssuance si : recent) {
                recentTableModel.addRow(new Object[]{
                        si.getIssuanceDate() == null ? "-" : si.getIssuanceDate().format(DATE_FORMAT),
                        si.getMaterialName(),
                        si.getCleanerName(),
                        si.getQuantityIssued(),
                        si.getIssuedByName() == null ? "-" : si.getIssuedByName()
                });
            }
            lblStatus.setText(" ");
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Unable to load dashboard data", e);
            lblStatus.setForeground(COLOR_LOW_STOCK);
            lblStatus.setText("Could not load dashboard data: " + e.getMessage());
        }
    }
}
