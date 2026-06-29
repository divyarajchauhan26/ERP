package edu.univ.erp.ui.admin;

import edu.univ.erp.data.SettingsDAO;
import edu.univ.erp.data.SettingsDAOImpl;
import edu.univ.erp.service.AdminService;
import edu.univ.erp.service.ServiceException;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.io.File;
import java.sql.SQLException;
import java.time.LocalDate;

public class SettingsPanel extends JPanel {

    private SettingsDAO settingsDAO;
    private AdminService adminService;

    // UI Components
    private JLabel statusLabel;
    private JButton toggleButton;

    // Date Components
    private JTextField regDeadlineField;
    private JTextField dropDeadlineField;
    private JButton updateDatesButton;
    private JLabel currentDatesLabel;

    // Backup/Restore Components
    private JButton backupButton;
    private JButton restoreButton;

    private boolean isMaintenanceOn = false;

    public SettingsPanel() {
        this.settingsDAO = new SettingsDAOImpl();
        this.adminService = new AdminService();
        setLayout(new BorderLayout(20, 20));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // --- Container for Maintenance and Deadlines ---
        JPanel topContainer = new JPanel(new BorderLayout(10, 10));

        // --- Panel 1: System Control (Maintenance) ---
        JPanel maintPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        maintPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY), "Maintenance Mode Toggle",
                TitledBorder.LEFT, TitledBorder.TOP, null, Color.WHITE));

        statusLabel = new JLabel("Loading...");
        statusLabel.setFont(new Font("Arial", Font.BOLD, 16));

        JPanel statusWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusWrapper.add(statusLabel);

        toggleButton = new JButton("Toggle Mode");
        toggleButton.setFocusPainted(false);
        toggleButton.setPreferredSize(new Dimension(150, 35));

        maintPanel.add(statusWrapper);
        maintPanel.add(Box.createHorizontalStrut(50));
        maintPanel.add(toggleButton);

        // --- Panel 2: Academic Calendar (Set Dates) ---
        JPanel datePanel = new JPanel(new GridBagLayout());
        datePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY), "Academic Calendar Deadlines",
                TitledBorder.LEFT, TitledBorder.TOP, null, Color.WHITE));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10); gbc.fill = GridBagConstraints.HORIZONTAL;

        // Registration Deadline Row
        gbc.gridx=0; gbc.gridy=0;
        datePanel.add(new JLabel("Registration Deadline (YYYY-MM-DD):"), gbc);
        regDeadlineField = new JTextField(15);
        gbc.gridx=1;
        datePanel.add(regDeadlineField, gbc);

        // Drop Deadline Row
        gbc.gridx=0; gbc.gridy=1;
        datePanel.add(new JLabel("Drop Deadline (YYYY-MM-DD):"), gbc);
        dropDeadlineField = new JTextField(15);
        gbc.gridx=1;
        datePanel.add(dropDeadlineField, gbc);

        // Button
        updateDatesButton = new JButton("Update Dates");
        updateDatesButton.setBackground(new Color(52, 152, 219));
        updateDatesButton.setForeground(Color.WHITE);
        updateDatesButton.setFocusPainted(false);
        gbc.gridx=1; gbc.gridy=2;
        gbc.insets = new Insets(15, 10, 10, 10);
        datePanel.add(updateDatesButton, gbc);

        // Status Label
        currentDatesLabel = new JLabel("Current: -");
        currentDatesLabel.setForeground(Color.CYAN);
        currentDatesLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        gbc.gridx=0; gbc.gridy=3; gbc.gridwidth=2;
        gbc.insets = new Insets(5, 10, 10, 10);
        datePanel.add(currentDatesLabel, gbc);

        topContainer.add(maintPanel, BorderLayout.NORTH);
        topContainer.add(datePanel, BorderLayout.CENTER);


        // --- Panel 3: Backup / Restore (New Section) ---
        JPanel backupPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        backupPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY), "Database Management (ERP DB)",
                TitledBorder.LEFT, TitledBorder.TOP, null, Color.WHITE));

        backupButton = new JButton("Backup ERP DB");
        backupButton.setBackground(new Color(40, 167, 69)); // Green for Backup (Save)
        backupButton.setForeground(Color.WHITE);
        backupButton.setFocusPainted(false);

        restoreButton = new JButton("Restore ERP DB");
        restoreButton.setBackground(new Color(192, 57, 43)); // Red for Restore (Dangerous operation)
        restoreButton.setForeground(Color.WHITE);
        restoreButton.setFocusPainted(false);

        backupPanel.add(backupButton);
        backupPanel.add(restoreButton);

        // --- Add Panels to Main ---
        add(topContainer, BorderLayout.NORTH);
        add(backupPanel, BorderLayout.CENTER);


        // --- Listeners ---
        toggleButton.addActionListener(e -> performToggle());
        updateDatesButton.addActionListener(e -> performUpdateDates());
        backupButton.addActionListener(e -> performBackup());
        restoreButton.addActionListener(e -> performRestore());

        loadCurrentStatus();
    }

    // --- Backup/Restore Methods ---
    private void performBackup() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Save Database Backup File");
        fc.setSelectedFile(new File("university_erp_db_backup_" + LocalDate.now() + ".sql"));

        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            try {
                // Execute backup via AdminService
                adminService.backupErpDatabase(file.getAbsolutePath());
                JOptionPane.showMessageDialog(this, "ERP Database backed up successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (ServiceException e) {
                JOptionPane.showMessageDialog(this, "Backup Failed: Check MySQL path in AdminService.java.\nError: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void performRestore() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "<html><p style='color:red;'>WARNING: Restoring will overwrite ALL existing ERP data.</p>Are you sure you want to proceed?",
                "Confirm Database Restore", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            JFileChooser fc = new JFileChooser();
            fc.setDialogTitle("Select Database Backup File (.sql)");

            if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                try {
                    // Execute restore via AdminService
                    adminService.restoreErpDatabase(file.getAbsolutePath());
                    JOptionPane.showMessageDialog(this, "ERP Database restored successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                } catch (ServiceException e) {
                    JOptionPane.showMessageDialog(this, "Restore Failed: Check MySQL path in AdminService.java.\nError: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    // --- Existing UI Methods ---

    public void loadCurrentStatus() {
        try {
            // 1. Maintenance
            this.isMaintenanceOn = settingsDAO.isMaintenanceModeOn();
            updateMaintUILabel();

            // Set toggle button text immediately based on status
            toggleButton.setText(isMaintenanceOn ? "Turn OFF" : "Turn ON");

            // 2. Dates
            LocalDate regDate = settingsDAO.getRegistrationDeadline();
            LocalDate dropDate = settingsDAO.getDropDeadline();

            regDeadlineField.setText(regDate.toString());
            dropDeadlineField.setText(dropDate.toString());

            currentDatesLabel.setText("<html>Current Settings:<br/>&nbsp;&nbsp;&nbsp;Register by: <b>" + regDate + "</b> | Drop by: <b>" + dropDate + "</b></html>");

        } catch (SQLException e) {
            statusLabel.setText("Error loading data.");
        }
    }

    private void performUpdateDates() {
        try {
            String regStr = regDeadlineField.getText();
            String dropStr = dropDeadlineField.getText();

            // Validate
            LocalDate.parse(regStr);
            LocalDate.parse(dropStr);

            // Save
            settingsDAO.setRegistrationDeadline(regStr);
            settingsDAO.setDropDeadline(dropStr);

            JOptionPane.showMessageDialog(this, "Academic dates updated successfully!");
            loadCurrentStatus();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Invalid Date Format (Use YYYY-MM-DD)", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void performToggle() {
        try {
            boolean newStatus = !this.isMaintenanceOn;
            settingsDAO.setMaintenanceMode(newStatus);
            this.isMaintenanceOn = newStatus;

            updateMaintUILabel();
            toggleButton.setText(newStatus ? "Turn OFF" : "Turn ON");

            JOptionPane.showMessageDialog(this, "Maintenance Mode is now " + (newStatus ? "ON" : "OFF"));
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void updateMaintUILabel() {
        if (this.isMaintenanceOn) {
            statusLabel.setText("STATUS: ON (Read-Only)");
            statusLabel.setForeground(new Color(231, 76, 60)); // Brighter Red
            toggleButton.setBackground(new Color(26, 188, 156)); // Turquoise/Green for turning OFF
        } else {
            statusLabel.setText("STATUS: OFF (Active)");
            statusLabel.setForeground(new Color(46, 204, 113)); // Bright Green
            toggleButton.setBackground(new Color(192, 57, 43)); // Dark Red for turning ON
        }
    }
}