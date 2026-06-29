package edu.univ.erp.ui.instructor;

import edu.univ.erp.auth.UserSession;
import edu.univ.erp.ui.auth.LoginWindow;
import edu.univ.erp.ui.auth.ProfileDialog;
import edu.univ.erp.ui.common.MaintenanceBanner;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;

public class InstructorDashboard extends JFrame {

    private JTabbedPane tabbedPane;
    private JSplitPane splitPane;
    private JButton menuToggle;
    private Map<String, JComponent> tabMap;

    public InstructorDashboard() {
        setTitle("Instructor Dashboard - " + UserSession.getInstance().getCurrentUser().getUsername());
        setMinimumSize(new Dimension(1000, 700));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initializeTabMap();

        // --- 1. Top Bar (Header) ---
        JPanel headerPanel = createInstructorHeader();

        // --- 2. Sidebar (Vertical Menu) ---
        JPanel sidebarPanel = createSidebar();

        // --- 3. Content Panel (TABS) ---
        tabbedPane = createContentTabs();

        // --- 4. Main Layout ---

        // Content wrapper to place the logo at the bottom right
        JPanel contentWrapper = new JPanel(new BorderLayout());
        contentWrapper.add(tabbedPane, BorderLayout.CENTER);
        // FIX: Method signature now correctly accepts String and Color arguments
        contentWrapper.add(createBottomRightLogo("/as.jpg", new Color(230, 126, 34)), BorderLayout.SOUTH); // Instructor Color (Orange)

        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sidebarPanel, contentWrapper);
        splitPane.setDividerSize(1);
        splitPane.setOneTouchExpandable(true);
        splitPane.setDividerLocation(200);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(splitPane, BorderLayout.CENTER);

        add(mainPanel);

        // --- Event Listeners ---
        menuToggle.addActionListener(e -> toggleSidebar());

        // Tab Change Listener to refresh maintenance banner
        MaintenanceBanner banner = (MaintenanceBanner) headerPanel.getClientProperty("MaintenanceBanner");

        tabbedPane.addChangeListener(e -> {
            if (banner != null) {
                banner.checkStatus();
            }
        });
    }

    private void initializeTabMap() {
        tabMap = new LinkedHashMap<>();
        tabMap.put("My Sections", new MySectionsPanel());
        tabMap.put("Gradebook", new GradebookPanel());
        tabMap.put("Class Stats", new ClassStatsPanel());
    }

    private JPanel createInstructorHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(40, 40, 40));
        header.setPreferredSize(new Dimension(getWidth(), 60));
        header.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

        // Left: Menu Toggle
        menuToggle = new JButton("☰");
        menuToggle.setFont(new Font("Monospaced", Font.BOLD, 24));
        menuToggle.setPreferredSize(new Dimension(50, 50));
        menuToggle.setFocusPainted(false);
        menuToggle.setBorderPainted(false);
        menuToggle.setContentAreaFilled(false);
        menuToggle.setForeground(Color.WHITE);

        // Center: App Title and Maintenance Banner
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        JLabel titleLabel = new JLabel("Instructor Panel", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setForeground(Color.LIGHT_GRAY);
        titlePanel.add(titleLabel, BorderLayout.CENTER);

        MaintenanceBanner banner = new MaintenanceBanner();
        header.putClientProperty("MaintenanceBanner", banner);
        titlePanel.add(banner, BorderLayout.SOUTH);

        // Right: Profile Icon
        JPanel profileContainer = createProfileIconContainer(this, new Color(230, 126, 34)); // Orange

        header.add(menuToggle, BorderLayout.WEST);
        header.add(titlePanel, BorderLayout.CENTER);
        header.add(profileContainer, BorderLayout.EAST);

        return header;
    }

    private JPanel createProfileIconContainer(JFrame parent, Color color) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        panel.setOpaque(false);

        String initial = UserSession.getInstance().getCurrentUser().getUsername().substring(0, 1).toUpperCase();
        JLabel profileIcon = new JLabel(initial);
        profileIcon.setPreferredSize(new Dimension(30, 30));
        profileIcon.setHorizontalAlignment(SwingConstants.CENTER);
        profileIcon.setOpaque(true);
        profileIcon.setBackground(color);
        profileIcon.setForeground(Color.WHITE);
        profileIcon.setFont(new Font("Arial", Font.BOLD, 16));
        profileIcon.setBorder(BorderFactory.createLineBorder(Color.WHITE, 1));

        profileIcon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showProfileMenu(profileIcon, parent, e.getX(), e.getY());
            }
            @Override public void mouseEntered(MouseEvent e) { setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); }
            @Override public void mouseExited(MouseEvent e) { setCursor(Cursor.getDefaultCursor()); }
        });

        panel.add(profileIcon);
        return panel;
    }

    private void showProfileMenu(Component invoker, JFrame parent, int x, int y) {
        JPopupMenu menu = new JPopupMenu();
        JMenuItem viewProfile = new JMenuItem("View/Edit Profile");
        JMenuItem logout = new JMenuItem("Logout");

        viewProfile.addActionListener(e -> new ProfileDialog(parent).setVisible(true));
        logout.addActionListener(e -> performLogout());

        menu.add(new JLabel("  Logged in as: " + UserSession.getInstance().getCurrentUser().getUsername()));
        menu.addSeparator();
        menu.add(viewProfile);
        menu.add(logout);

        menu.show(invoker, x, invoker.getHeight() - 5);
    }

    private JPanel createSidebar() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(50, 50, 50));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        for (String tabName : tabMap.keySet()) {
            JButton button = createSidebarButton(tabName);
            panel.add(button);
            panel.add(Box.createRigidArea(new Dimension(0, 5)));
        }
        panel.add(Box.createVerticalGlue());

        return panel;
    }

    private JButton createSidebarButton(String text) {
        JButton button = new JButton(text);
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setMaximumSize(new Dimension(200, 40));
        button.setMinimumSize(new Dimension(200, 40));
        button.setPreferredSize(new Dimension(200, 40));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setFocusPainted(false);

        button.addActionListener(e -> {
            int index = getTabIndex(text);
            if (index != -1) {
                tabbedPane.setSelectedIndex(index);
            }
        });

        return button;
    }

    private JTabbedPane createContentTabs() {
        JTabbedPane pane = new JTabbedPane();
        for (JComponent component : tabMap.values()) {
            pane.addTab("", component);
        }
        pane.setUI(new javax.swing.plaf.basic.BasicTabbedPaneUI() {
            @Override protected int calculateTabAreaHeight(int tabPlacement, int horizTextGap, int iconVTextGap) { return 0; }
        });
        return pane;
    }

    private int getTabIndex(String tabName) {
        java.util.List<String> keys = new java.util.ArrayList<>(tabMap.keySet());
        return keys.indexOf(tabName);
    }

    private void toggleSidebar() {
        if (splitPane.getDividerLocation() > 50) {
            splitPane.setDividerLocation(1);
        } else {
            splitPane.setDividerLocation(200);
        }
    }

    private void performLogout() {
        UserSession.getInstance().clearSession();
        this.dispose();
        new LoginWindow().setVisible(true);
    }

    /**
     * Creates the logo panel for the bottom right content area.
     */
    private JPanel createBottomRightLogo(String logoPath, Color profileColor) {
        JPanel logoContainer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        logoContainer.setOpaque(false);

        URL logoUrl = getClass().getResource(logoPath);

        if (logoUrl != null) {
            try {
                ImageIcon icon = new ImageIcon(logoUrl);
                Image img = icon.getImage();
                Image scaledImg = img.getScaledInstance(70, 70, Image.SCALE_SMOOTH); // Reduced size
                JLabel logoLabel = new JLabel(new ImageIcon(scaledImg));

                logoContainer.add(logoLabel);
            } catch (Exception e) { }
        }

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(logoContainer, BorderLayout.EAST);
        wrapper.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 10));

        return wrapper;
    }
}