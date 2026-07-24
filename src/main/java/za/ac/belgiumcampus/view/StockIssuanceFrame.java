package za.ac.belgiumcampus.view;

import za.ac.belgiumcampus.dao.CleanerDAO;
import za.ac.belgiumcampus.dao.MaterialDAO;
import za.ac.belgiumcampus.dao.StockIssuanceDAO;
import za.ac.belgiumcampus.exception.ValidationException;
import za.ac.belgiumcampus.model.Cleaner;
import za.ac.belgiumcampus.model.Material;
import za.ac.belgiumcampus.model.StockIssuance;
import za.ac.belgiumcampus.model.User;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class StockIssuanceFrame extends JFrame {

    private static final Logger logger = Logger.getLogger(StockIssuanceFrame.class.getName());
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");

    private final MaterialDAO materialDAO = new MaterialDAO();
    private final CleanerDAO cleanerDAO = new CleanerDAO();
    private final StockIssuanceDAO issuanceDAO = new StockIssuanceDAO();
    private final User loggedInUser;

    private JComboBox<Material> cbMaterial;
    private JComboBox<Cleaner> cbCleaner;
    private JTextField txtQuantity;
    private JTextField txtNotes;
    private JLabel lblStatus;
    private DefaultTableModel historyModel;

    public StockIssuanceFrame(User loggedInUser) {
        this.loggedInUser = loggedInUser;

        setTitle("Stock Issuance Management - University Cleaning Inventory System");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel content = new JPanel(new BorderLayout(10, 10));
        content.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        content.add(buildHeading(), BorderLayout.NORTH);
        content.add(buildForm(), BorderLayout.WEST);
        content.add(buildHistoryPanel(), BorderLayout.CENTER);
        setContentPane(content);

        setSize(1050, 620);
        setLocationRelativeTo(null);

        loadCombos();
        refreshHistory();
    }

    private JPanel buildHeading() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));

        JLabel heading = new JLabel("Stock Issuance");
        heading.setFont(new Font("SansSerif", Font.BOLD, 20));
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = new JLabel("Issue cleaning materials to cleaners and track issuance history");
        sub.setForeground(Color.DARK_GRAY);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        textPanel.add(heading);
        textPanel.add(sub);
        panel.add(textPanel, BorderLayout.WEST);
        return panel;
    }

    private JPanel buildForm() {
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Issue Material"),
                BorderFactory.createEmptyBorder(5, 10, 10, 10)));
        form.setPreferredSize(new Dimension(290, 0));

        cbMaterial = new JComboBox<>();
        cbMaterial.setAlignmentX(Component.LEFT_ALIGNMENT);
        cbMaterial.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));

        cbCleaner = new JComboBox<>();
        cbCleaner.setAlignmentX(Component.LEFT_ALIGNMENT);
        cbCleaner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));

        txtQuantity = new JTextField();
        txtQuantity.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtQuantity.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));
        restrictToNumeric(txtQuantity);

        txtNotes = new JTextField();
        txtNotes.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtNotes.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));

        form.add(formLabel("Material *"));
        form.add(cbMaterial);
        form.add(Box.createVerticalStrut(10));
        form.add(formLabel("Cleaner *"));
        form.add(cbCleaner);
        form.add(Box.createVerticalStrut(10));
        form.add(formLabel("Quantity to Issue *"));
        form.add(txtQuantity);
        form.add(Box.createVerticalStrut(10));
        form.add(formLabel("Notes"));
        form.add(txtNotes);
        form.add(Box.createVerticalStrut(15));

        JButton btnIssue = new JButton("Issue Material");
        btnIssue.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnIssue.addActionListener(e -> onIssue());
        form.add(btnIssue);

        form.add(Box.createVerticalStrut(6));
        JButton btnClear = new JButton("Clear");
        btnClear.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnClear.addActionListener(e -> clearForm());
        form.add(btnClear);

        form.add(Box.createVerticalGlue());
        return form;
    }

    private JLabel formLabel(String text) {
        JLabel label = new JLabel(text);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private JPanel buildHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel label = new JLabel("Issuance History");
        label.setFont(new Font("SansSerif", Font.BOLD, 14));
        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.addActionListener(e -> {
            loadCombos();
            refreshHistory();
        });
        toolbar.add(label);
        toolbar.add(btnRefresh);
        panel.add(toolbar, BorderLayout.NORTH);

        historyModel = new DefaultTableModel(
                new String[]{"Date", "Material", "Cleaner", "Qty", "Issued By", "Notes"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(historyModel);
        table.setRowHeight(22);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        lblStatus = new JLabel(" ");
        lblStatus.setBorder(BorderFactory.createEmptyBorder(6, 0, 0, 0));
        panel.add(lblStatus, BorderLayout.SOUTH);

        return panel;
    }

    private void loadCombos() {
        Material previouslySelectedMaterial = (Material) cbMaterial.getSelectedItem();
        Cleaner previouslySelectedCleaner = (Cleaner) cbCleaner.getSelectedItem();

        try {
            cbMaterial.removeAllItems();
            List<Material> materials = materialDAO.getAll();
            for (Material m : materials) {
                cbMaterial.addItem(m);
                if (previouslySelectedMaterial != null && m.getMaterialId() == previouslySelectedMaterial.getMaterialId()) {
                    cbMaterial.setSelectedItem(m);
                }
            }
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Unable to load materials", e);
            setStatus("Could not load materials: " + e.getMessage(), true);
        }

        try {
            cbCleaner.removeAllItems();
            List<Cleaner> cleaners = cleanerDAO.getAllCleaners();
            for (Cleaner c : cleaners) {
                cbCleaner.addItem(c);
                if (previouslySelectedCleaner != null && c.getCleanerId() == previouslySelectedCleaner.getCleanerId()) {
                    cbCleaner.setSelectedItem(c);
                }
            }
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Unable to load cleaners", e);
            setStatus("Could not load cleaners: " + e.getMessage(), true);
        }
    }

    private void refreshHistory() {
        try {
            List<StockIssuance> issuances = issuanceDAO.getAll();
            historyModel.setRowCount(0);
            for (StockIssuance si : issuances) {
                historyModel.addRow(new Object[]{
                        si.getIssuanceDate() == null ? "-" : si.getIssuanceDate().format(DATE_FORMAT),
                        si.getMaterialName(), si.getCleanerName(), si.getQuantityIssued(),
                        si.getIssuedByName() == null ? "-" : si.getIssuedByName(),
                        si.getNotes() == null ? "" : si.getNotes()
                });
            }
            setStatus(issuances.size() + " issuance(s) on record.", false);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Unable to load issuance history", e);
            setStatus("Could not load issuance history: " + e.getMessage(), true);
        }
    }

    private void onIssue() {
        Material material = (Material) cbMaterial.getSelectedItem();
        Cleaner cleaner = (Cleaner) cbCleaner.getSelectedItem();

        if (material == null) {
            showValidationError("Select a material to issue.");
            return;
        }
        if (cleaner == null) {
            showValidationError("Select a cleaner to issue to.");
            return;
        }

        int quantity;
        try {
            quantity = Integer.parseInt(txtQuantity.getText().trim());
        } catch (NumberFormatException e) {
            showValidationError("Quantity to issue is required and must be a whole number.");
            return;
        }

        StockIssuance issuance = new StockIssuance();
        issuance.setMaterialId(material.getMaterialId());
        issuance.setCleanerId(cleaner.getCleanerId());
        issuance.setIssuedBy(loggedInUser == null ? null : loggedInUser.getUserId());
        issuance.setQuantityIssued(quantity);
        String notes = txtNotes.getText().trim();
        issuance.setNotes(notes.isEmpty() ? null : notes);

        try {
            issuanceDAO.issueMaterial(issuance);
            setStatus("Issued " + quantity + " " + material.getUnitOfMeasure() + " of \""
                    + material.getMaterialName() + "\" to " + cleaner.getFullName() + ".", false);
            clearForm();
            loadCombos();
            refreshHistory();
        } catch (ValidationException e) {
            showValidationError(e.getMessage());
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Unable to issue material", e);
            showDatabaseError(e);
        }
    }

    private void clearForm() {
        txtQuantity.setText("");
        txtNotes.setText("");
        if (cbMaterial.getItemCount() > 0) {
            cbMaterial.setSelectedIndex(0);
        }
        if (cbCleaner.getItemCount() > 0) {
            cbCleaner.setSelectedIndex(0);
        }
    }

    
    private void restrictToNumeric(JTextField field) {
        field.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if (!Character.isDigit(c) && !Character.isISOControl(c)) {
                    e.consume();
                }
            }
        });
    }

    private void setStatus(String message, boolean isError) {
        lblStatus.setText(message);
        lblStatus.setForeground(isError ? new Color(0xB3, 0x1E, 0x1E) : new Color(0x1E, 0x6B, 0x2E));
    }

    private void showValidationError(String message) {
        setStatus(message, true);
        JOptionPane.showMessageDialog(this, message, "Validation Error", JOptionPane.WARNING_MESSAGE);
    }

    private void showDatabaseError(SQLException e) {
        String message = "A database error occurred: " + e.getMessage();
        setStatus(message, true);
        JOptionPane.showMessageDialog(this, message, "Database Error", JOptionPane.ERROR_MESSAGE);
    }
}
