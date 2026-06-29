package edu.univ.erp.ui.auth;

import edu.univ.erp.auth.AuthDAO;
import edu.univ.erp.auth.AuthDAOImpl;
import edu.univ.erp.auth.PasswordHasher;
import edu.univ.erp.auth.UserSession;
import edu.univ.erp.domain.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.SQLException;

public class ProfileDialog extends JDialog {

    private JPasswordField oldPassField;
    private JPasswordField newPassField;
    private JPasswordField confirmPassField;
    private AuthDAO authDAO;

    public ProfileDialog(JFrame parent) {
        super(parent, "User Profile & Settings", true); // Modal
        this.authDAO = new AuthDAOImpl();
        User currentUser = UserSession.getInstance().getCurrentUser();

        setLayout(new BorderLayout(15, 15));
        setSize(450, 450);
        setLocationRelativeTo(parent);

        // --- 1. Top Panel: User Info (Based on available data) ---
        JPanel infoPanel = new JPanel(new GridBagLayout());
        infoPanel.setBorder(BorderFactory.createTitledBorder("Current User Information"));
        GridBagConstraints gbcInfo = new GridBagConstraints();
        gbcInfo.insets = new Insets(5, 10, 5, 10);
        gbcInfo.anchor = GridBagConstraints.WEST;

        // Helper to add rows
        int row = 0;
        gbcInfo.gridx = 0; gbcInfo.gridy = row; infoPanel.add(new JLabel("Username:"), gbcInfo);
        gbcInfo.gridx = 1; infoPanel.add(new JLabel(currentUser.getUsername()), gbcInfo); row++;

        gbcInfo.gridx = 0; gbcInfo.gridy = row; infoPanel.add(new JLabel("Role:"), gbcInfo);
        gbcInfo.gridx = 1; infoPanel.add(new JLabel(currentUser.getRole()), gbcInfo); row++;

        // Note: For a complete solution, you would inject a service here to fetch
        // Roll No or Department based on the role and display it here.
        // For now, we display the core info.

        add(infoPanel, BorderLayout.NORTH);

        // --- 2. Center Panel: Change Password Form ---
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Change Password"));
        GridBagConstraints gbcPass = new GridBagConstraints();
        gbcPass.insets = new Insets(5, 5, 5, 5);
        gbcPass.fill = GridBagConstraints.HORIZONTAL;

        gbcPass.gridx = 0; gbcPass.gridy = 0; formPanel.add(new JLabel("Current Password:"), gbcPass);
        oldPassField = new JPasswordField(15);
        gbcPass.gridx = 1; formPanel.add(oldPassField, gbcPass);

        gbcPass.gridx = 0; gbcPass.gridy = 1; formPanel.add(new JLabel("New Password:"), gbcPass);
        newPassField = new JPasswordField(15);
        gbcPass.gridx = 1; formPanel.add(newPassField, gbcPass);

        gbcPass.gridx = 0; gbcPass.gridy = 2; formPanel.add(new JLabel("Confirm New Password:"), gbcPass);
        confirmPassField = new JPasswordField(15);
        gbcPass.gridx = 1; formPanel.add(confirmPassField, gbcPass);

        JButton changeButton = new JButton("Change Password");
        changeButton.setBackground(new Color(243, 156, 18)); // Orange for action
        changeButton.setForeground(Color.WHITE);
        changeButton.setFocusPainted(false);

        JButton cancelButton = new JButton("Close");

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(changeButton);
        buttonPanel.add(cancelButton);

        // Add Password form to center
        JPanel centerWrapper = new JPanel(new BorderLayout());
        centerWrapper.add(formPanel, BorderLayout.NORTH);
        add(centerWrapper, BorderLayout.CENTER);

        add(buttonPanel, BorderLayout.SOUTH);

        // --- Actions ---
        changeButton.addActionListener(e -> performChangePassword());
        cancelButton.addActionListener(e -> dispose());
    }

    private void performChangePassword() {
        String oldPass = new String(oldPassField.getPassword());
        String newPass = new String(newPassField.getPassword());
        String confirmPass = new String(confirmPassField.getPassword());

        if (oldPass.isEmpty() || newPass.isEmpty() || !newPass.equals(confirmPass)) {
            JOptionPane.showMessageDialog(this, "Please check all fields and ensure new passwords match.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            int userId = UserSession.getInstance().getCurrentUser().getUserId();

            // 1. Verify Old Password
            String currentHash = authDAO.getPasswordHash(userId);
            if (currentHash == null || !PasswordHasher.checkPassword(oldPass, currentHash)) {
                JOptionPane.showMessageDialog(this, "Current password is incorrect.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // 2. Hash New Password and Save
            String newHash = PasswordHasher.hashPassword(newPass);
            authDAO.updatePassword(userId, newHash);

            JOptionPane.showMessageDialog(this, "Password changed successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            dispose();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}