package edu.univ.erp.ui.student;

import edu.univ.erp.auth.UserSession;
import edu.univ.erp.ui.auth.LoginWindow;
import edu.univ.erp.ui.auth.ProfileDialog;
import edu.univ.erp.ui.common.MaintenanceBanner;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;

public class StudentDashboard extends JFrame {

    private JTabbedPane tabbedPane;
    private JSplitPane splitPane;
    private JButton menuToggle;

    private MaintenanceBanner banner;

    private Map<String, JComponent> tabMap;

    public StudentDashboard() {
        setTitle("Student ERP Dashboard - " + UserSession.getInstance().getCurrentUser().getUsername());
        setMinimumSize(new Dimension(1000, 700));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initializeTabMap();

        // --- 1. Top Bar (Header) ---
        JPanel headerPanel = createHeaderPanel();

        // --- 2. Sidebar (Vertical Menu) ---
        JPanel sidebarPanel = createSidebar();

        // --- 3. Content Panel (TABS) ---
        tabbedPane = createContentTabs();

        // --- 4. Main Layout ---

        // Content wrapper to hold tabs and the logo at the bottom right
        JPanel contentWrapper = new JPanel(new BorderLayout());
        contentWrapper.add(tabbedPane, BorderLayout.CENTER);
        // FIX: Method signature now correctly accepts String and Color arguments
        contentWrapper.add(createBottomRightLogo("/as.jpg", new Color(52, 152, 219)), BorderLayout.SOUTH);

        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sidebarPanel, contentWrapper);
        splitPane.setDividerSize(1);
        splitPane.setOneTouchExpandable(true);
        splitPane.setDividerLocation(200);

        // Frame Layout: Header (North) + SplitPane (Center)
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(splitPane, BorderLayout.CENTER);

        add(mainPanel);

        // --- Event Listeners ---
        menuToggle.addActionListener(e -> toggleSidebar());

        // Tab Change Listener for data refresh and maintenance banner
        tabbedPane.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if (banner != null) {
                    banner.checkStatus();
                }
                refreshSelectedTab();
            }
        });

        // Initial check for banner status
        if (this.banner != null) {
            this.banner.checkStatus();
        }
    }

    private void initializeTabMap() {
        tabMap = new LinkedHashMap<>();
        tabMap.put("Course Catalog", new CourseCatalogPanel());
        tabMap.put("My Registrations", new MyRegistrationsPanel());
        tabMap.put("My Timetable", new TimetablePanel());
        tabMap.put("My Grades", new MyGradesPanel());
        tabMap.put("Transcript", new TranscriptPanel());
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(40, 40, 40));
        header.setPreferredSize(new Dimension(getWidth(), 60));
        header.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

        // Left: Menu Toggle (Three Lines)
        menuToggle = new JButton("☰");
        menuToggle.setFont(new Font("Monospaced", Font.BOLD, 24));
        menuToggle.setPreferredSize(new Dimension(50, 50));
        menuToggle.setFocusPainted(false);
        menuToggle.setBorderPainted(false);
        menuToggle.setContentAreaFilled(false);
        menuToggle.setForeground(Color.WHITE);

        // Center: App Title (and Maintenance Banner)
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        JLabel titleLabel = new JLabel("University ERP", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setForeground(Color.LIGHT_GRAY);
        titlePanel.add(titleLabel, BorderLayout.CENTER);

        // Integrate Maintenance Banner (South of Title)
        this.banner = new MaintenanceBanner();
        titlePanel.add(this.banner, BorderLayout.SOUTH);

        // Right: Profile Circle
        JPanel profilePanel = createProfileIcon();

        header.add(menuToggle, BorderLayout.WEST);
        header.add(titlePanel, BorderLayout.CENTER);
        header.add(profilePanel, BorderLayout.EAST);

        return header;
    }

    private JPanel createProfileIcon() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        panel.setOpaque(false);

        String initial = UserSession.getInstance().getCurrentUser().getUsername().substring(0, 1).toUpperCase();
        JLabel profileIcon = new JLabel(initial);
        profileIcon.setPreferredSize(new Dimension(30, 30));
        profileIcon.setHorizontalAlignment(SwingConstants.CENTER);
        profileIcon.setVerticalAlignment(SwingConstants.CENTER);
        profileIcon.setOpaque(true);
        profileIcon.setBackground(new Color(52, 152, 219)); // Blue circle
        profileIcon.setForeground(Color.WHITE);
        profileIcon.setFont(new Font("Arial", Font.BOLD, 16));
        profileIcon.setBorder(BorderFactory.createLineBorder(Color.WHITE, 1));

        profileIcon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showProfileMenu(profileIcon, e.getX(), e.getY());
            }
            @Override
            public void mouseEntered(MouseEvent e) {
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                setCursor(Cursor.getDefaultCursor());
            }
        });

        panel.add(profileIcon);
        return panel;
    }

    private void showProfileMenu(Component invoker, int x, int y) {
        JPopupMenu menu = new JPopupMenu();

        JMenuItem viewProfile = new JMenuItem("View/Edit Profile");
        JMenuItem logout = new JMenuItem("Logout");

        viewProfile.addActionListener(e -> new ProfileDialog(this).setVisible(true));
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
            panel.add(Box.createRigidArea(new Dimension(0, 5))); // Spacer
        }

        // Add a flexible filler space to push buttons up
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

        // Action to switch tabs
        button.addActionListener(e -> {
            int index = getTabIndex(text);
            if (index != -1) {
                tabbedPane.setSelectedIndex(index);
            }
        });

        return button;
    }

    private JTabbedPane createContentTabs() {
        // We use JTabbedPane internally, but hide the tab controls to let the sidebar drive navigation
        JTabbedPane pane = new JTabbedPane();
        for (JComponent component : tabMap.values()) {
            // We use the tabbed pane simply as a card layout switcher
            pane.addTab("", component);
        }

        // Hide the default tab controls (Aesthetics)
        pane.setUI(new javax.swing.plaf.basic.BasicTabbedPaneUI() {
            @Override
            protected int calculateTabAreaHeight(int tabPlacement, int horizTextGap, int iconVTextGap) {
                return 0; // Hide tabs completely
            }
        });

        return pane;
    }

    private int getTabIndex(String tabName) {
        // Using FQDN for List to avoid java.awt.List conflict
        java.util.List<String> keys = new java.util.ArrayList<>(tabMap.keySet());
        return keys.indexOf(tabName);
    }

    private void toggleSidebar() {
        // Check if expanded (e.g., width > 50)
        if (splitPane.getDividerLocation() > 50) {
            // Collapse to 1 (just the divider)
            splitPane.setDividerLocation(1);
        } else {
            // Expand to default width
            splitPane.setDividerLocation(200);
        }
    }

    private void refreshSelectedTab() {
        Component selected = tabbedPane.getSelectedComponent();

        if (selected instanceof CourseCatalogPanel) {
            ((CourseCatalogPanel) selected).checkDeadlineAndLoadData();
        }
        else if (selected instanceof MyRegistrationsPanel) {
            ((MyRegistrationsPanel) selected).refreshData();
        }
        else if (selected instanceof TimetablePanel) {
            ((TimetablePanel) selected).refreshData();
        }
        else if (selected instanceof MyGradesPanel) {
            ((MyGradesPanel) selected).refreshData();
        }
        else if (selected instanceof TranscriptPanel) {
            ((TranscriptPanel) selected).refreshData();
        }
    }

    private void performLogout() {
        UserSession.getInstance().clearSession();
        this.dispose();
        new LoginWindow().setVisible(true);
    }

    /**
     * Creates the logo panel for the bottom right content area.
     * @param logoPath Path to the logo image file.
     * @param profileColor The profile color (not directly used for logo, but kept for context).
     */
    private JPanel createBottomRightLogo(String logoPath, Color profileColor) {
        JPanel logoContainer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        logoContainer.setOpaque(false);

        // Load the logo (assuming the file is named as.jpg in src/)
        URL logoUrl = getClass().getResource(logoPath);

        if (logoUrl != null) {
            try {
                ImageIcon icon = new ImageIcon(logoUrl);

                // FIX: Scale the image down (70x70)
                Image img = icon.getImage();
                Image scaledImg = img.getScaledInstance(70, 70, Image.SCALE_SMOOTH);
                JLabel logoLabel = new JLabel(new ImageIcon(scaledImg));

                logoContainer.add(logoLabel);
            } catch (Exception e) {
                // Ignore load errors; the placeholder will be empty
            }
        }

        // Add padding at the bottom/right edge of the container
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(logoContainer, BorderLayout.EAST);
        wrapper.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 10));

        return wrapper;
    }
}