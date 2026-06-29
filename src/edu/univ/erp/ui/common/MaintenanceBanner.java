package edu.univ.erp.ui.common;

import edu.univ.erp.access.AccessControlService;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

public class MaintenanceBanner extends JPanel {

    private JLabel messageLabel;
    private AccessControlService accessControl;

    public MaintenanceBanner() {
        this.accessControl = new AccessControlService();
        setLayout(new BorderLayout());
        setBackground(new Color(150, 40, 40));
        setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        messageLabel = new JLabel("SYSTEM IS IN READ-ONLY MAINTENANCE MODE. Changes are temporarily blocked.", SwingConstants.CENTER);
        messageLabel.setForeground(Color.WHITE);
        messageLabel.setFont(new Font("Arial", Font.BOLD, 14));
        add(messageLabel, BorderLayout.CENTER);

        // Initial visibility check
        checkStatus();
    }

    public void checkStatus() {
        try {
            boolean isMaintOn = accessControl.isMaintenanceModeOn();
            setVisible(isMaintOn);
        } catch (SQLException e) {
            System.err.println("Error checking maintenance status: " + e.getMessage());
            setVisible(true); // Fail-safe: Assume maintenance if status check fails
        }
    }
}