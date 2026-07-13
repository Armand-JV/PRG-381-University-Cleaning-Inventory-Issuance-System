/*
 * MainFrame - the application shell shown after a successful login.
 *
 * Unlike LoginFrame/RegisterFrame, this one is hand-coded rather than
 * built with the NetBeans GUI Builder (Design view), because CardLayout
 * navigation - swapping panels in and out based on a sidebar click -
 * isn't something the visual builder represents well. This is a normal
 * pattern: some Swing containers are best hand-coded even in a project
 * that mostly uses the Form Editor.
 *
 * How it works:
 *   - A sidebar (left) has one button per module.
 *   - A CardLayout content area (center) holds one panel per module.
 *   - Clicking a sidebar button calls cardLayout.show(...) to switch
 *     which panel is visible - no new windows are opened.
 *
 * For teammates: each of you owns one panel below (see the CARD_*
 * constants and buildContentArea()). Replace the placeholder panel for
 * your module with your real one - everyone else's code doesn't change.
 */
package za.ac.belgiumcampus.view;

import za.ac.belgiumcampus.model.User;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

public class MainFrame extends JFrame {

    // Card identifiers - used by both the sidebar buttons and CardLayout.show()
    private static final String CARD_DASHBOARD = "DASHBOARD";
    private static final String CARD_MATERIALS = "MATERIALS";
    private static final String CARD_SUPPLIERS = "SUPPLIERS";
    private static final String CARD_CLEANERS = "CLEANERS";
    private static final String CARD_ISSUANCE = "ISSUANCE";

    private final User loggedInUser;

    private CardLayout cardLayout;
    private JPanel contentPanel;

    public MainFrame(User loggedInUser) {
        this.loggedInUser = loggedInUser;

        setTitle("University Cleaning Inventory & Issuance System");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(1000, 650);
        setLocationRelativeTo(null);

        add(buildSidebar(), BorderLayout.WEST);
        add(buildContentArea(), BorderLayout.CENTER);
        add(buildStatusBar(), BorderLayout.SOUTH);
    }

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));
        sidebar.setPreferredSize(new Dimension(180, 0));
        sidebar.setBackground(new Color(45, 62, 80));

        JLabel heading = new JLabel("Navigation");
        heading.setForeground(Color.WHITE);
        heading.setFont(new Font("SansSerif", Font.BOLD, 14));
        heading.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        sidebar.add(heading);
        sidebar.add(Box.createVerticalStrut(15));

        sidebar.add(createNavButton("Dashboard", CARD_DASHBOARD));
        sidebar.add(createNavButton("Materials", CARD_MATERIALS));
        sidebar.add(createNavButton("Suppliers", CARD_SUPPLIERS));
        sidebar.add(createNavButton("Cleaners", CARD_CLEANERS));
        sidebar.add(createNavButton("Issuance", CARD_ISSUANCE));

        sidebar.add(Box.createVerticalGlue());

        JButton logoutButton = new JButton("Logout");
        logoutButton.setAlignmentX(JButton.LEFT_ALIGNMENT);
        logoutButton.setMaximumSize(new Dimension(160, 30));
        logoutButton.addActionListener(e -> logout());
        sidebar.add(logoutButton);

        return sidebar;
    }

    private JButton createNavButton(String label, String cardName) {
        JButton button = new JButton(label);
        button.setAlignmentX(JButton.LEFT_ALIGNMENT);
        button.setMaximumSize(new Dimension(160, 30));
        button.addActionListener(e -> cardLayout.show(contentPanel, cardName));
        return button;
    }

    private JPanel buildContentArea() {
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);

        // TODO (Member 4): replace with the real DashboardPanel
        contentPanel.add(placeholderPanel("Dashboard"), CARD_DASHBOARD);

        // Use the existing MaterialsFrame UI inside the card (falls back to placeholder if it fails)
        try {
            MaterialsFrame materialsFrame = new MaterialsFrame();
            contentPanel.add(materialsFrame.getContentPane(), CARD_MATERIALS);
        } catch (Exception e) {
            e.printStackTrace();
            contentPanel.add(placeholderPanel("Materials (failed to load)"), CARD_MATERIALS);
        }

        // TODO (Member 3): replace with the real SuppliersPanel
        contentPanel.add(placeholderPanel("Suppliers"), CARD_SUPPLIERS);

        // TODO (Member 3): replace with the real CleanersPanel
        contentPanel.add(placeholderPanel("Cleaners"), CARD_CLEANERS);

        // TODO (Member 4): replace with the real IssuancePanel
        contentPanel.add(placeholderPanel("Issuance"), CARD_ISSUANCE);

        cardLayout.show(contentPanel, CARD_DASHBOARD);
        return contentPanel;
    }

    /**
     * Simple "not built yet" placeholder so MainFrame is testable
     * end-to-end before every teammate's panel exists. Whoever owns a
     * module replaces contentPanel.add(placeholderPanel(...), CARD_X)
     * above with their real panel - nobody else's code needs to change.
     */
    private JPanel placeholderPanel(String moduleName) {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel label = new JLabel(moduleName + " - not built yet", SwingConstants.CENTER);
        label.setFont(new Font("SansSerif", Font.ITALIC, 16));
        label.setForeground(Color.GRAY);
        panel.add(label, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildStatusBar() {
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));

        String displayName = (loggedInUser != null) ? loggedInUser.getFullName() : "Unknown";
        String role = (loggedInUser != null) ? loggedInUser.getRole() : "Unknown";

        JLabel userLabel = new JLabel("Logged in as: " + displayName + " (" + role + ")");
        statusBar.add(userLabel, BorderLayout.WEST);

        return statusBar;
    }

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to logout?",
                "Confirm Logout", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            dispose();
            SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
        }
    }
}