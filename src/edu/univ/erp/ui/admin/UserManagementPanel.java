package edu.univ.erp.ui.admin;

import edu.univ.erp.domain.Instructor;
import edu.univ.erp.domain.Student;
import edu.univ.erp.service.AdminService;
import edu.univ.erp.service.ServiceException;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class UserManagementPanel extends JPanel {

    private AdminService adminService;

    // Form Components
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JComboBox<String> roleComboBox;
    private JTextField rollOrDeptField;
    private JLabel rollOrDeptLabel;
    private JLabel programLabel;
    private JTextField programField;
    private JButton createUserButton;

    // List View Components
    private JComboBox<String> viewTypeCombo;
    private JTable usersTable;
    private DefaultTableModel tableModel;
    private JButton resetPasswordButton;
    // REMOVED: unlockUserButton

    public UserManagementPanel() {
        this.adminService = new AdminService();
        setLayout(new BorderLayout(10, 10));

        // --- 1. TOP: Create User Form ---
        JPanel createPanel = new JPanel(new GridBagLayout());
        createPanel.setBorder(BorderFactory.createTitledBorder("Create New User"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx=0; gbc.gridy=0; createPanel.add(new JLabel("Username:"), gbc);
        usernameField = new JTextField(15); gbc.gridx=1; createPanel.add(usernameField, gbc);

        gbc.gridx=0; gbc.gridy=1; createPanel.add(new JLabel("Password:"), gbc);
        passwordField = new JPasswordField(15); gbc.gridx=1; createPanel.add(passwordField, gbc);

        gbc.gridx=0; gbc.gridy=2; createPanel.add(new JLabel("Role:"), gbc);
        String[] roles = {"Student", "Instructor"};
        roleComboBox = new JComboBox<>(roles); gbc.gridx=1; createPanel.add(roleComboBox, gbc);

        gbc.gridx=0; gbc.gridy=3;
        rollOrDeptLabel = new JLabel("Roll Number:");
        createPanel.add(rollOrDeptLabel, gbc);
        rollOrDeptField = new JTextField(15); gbc.gridx=1; createPanel.add(rollOrDeptField, gbc);

        gbc.gridx=0; gbc.gridy=4;
        programLabel = new JLabel("Program:");
        createPanel.add(programLabel, gbc);
        programField = new JTextField(15); gbc.gridx=1; createPanel.add(programField, gbc);

        createUserButton = new JButton("Create User");
        gbc.gridx=1; gbc.gridy=5; createPanel.add(createUserButton, gbc);

        add(createPanel, BorderLayout.NORTH);

        // --- 2. CENTER: User List Table ---
        JPanel listPanel = new JPanel(new BorderLayout(5, 5));
        listPanel.setBorder(BorderFactory.createTitledBorder("Existing Users"));

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.add(new JLabel("View:"));
        String[] viewOptions = {"Students", "Instructors"};
        viewTypeCombo = new JComboBox<>(viewOptions);
        filterPanel.add(viewTypeCombo);
        JButton refreshButton = new JButton("Refresh List");
        filterPanel.add(refreshButton);

        // Reset Password Button (Red)
        resetPasswordButton = new JButton("Reset Password");
        resetPasswordButton.setBackground(new Color(130, 50, 50));
        resetPasswordButton.setForeground(Color.WHITE);
        filterPanel.add(resetPasswordButton);

        // REMOVED: Unlock Button code

        listPanel.add(filterPanel, BorderLayout.NORTH);

        tableModel = new DefaultTableModel();
        usersTable = new JTable(tableModel) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };

        // Styling
        usersTable.setShowGrid(true);
        usersTable.setGridColor(new Color(100, 100, 100));
        usersTable.setIntercellSpacing(new Dimension(1, 1));
        usersTable.setRowHeight(30);
        ((DefaultTableCellRenderer)usersTable.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(JLabel.CENTER);
        DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
        leftRenderer.setHorizontalAlignment(JLabel.LEFT);
        leftRenderer.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
        usersTable.setDefaultRenderer(Object.class, leftRenderer);

        JScrollPane scrollPane = new JScrollPane(usersTable);
        listPanel.add(scrollPane, BorderLayout.CENTER);

        add(listPanel, BorderLayout.CENTER);

        // --- Action Listeners ---
        roleComboBox.addActionListener(e -> {
            if ("Student".equals(roleComboBox.getSelectedItem())) {
                rollOrDeptLabel.setText("Roll Number:");
                programLabel.setVisible(true);
                programField.setVisible(true);
            } else {
                rollOrDeptLabel.setText("Department:");
                programLabel.setVisible(false);
                programField.setVisible(false);
            }
        });

        createUserButton.addActionListener(e -> performCreateUser());
        viewTypeCombo.addActionListener(e -> loadUserList());
        refreshButton.addActionListener(e -> loadUserList());
        resetPasswordButton.addActionListener(e -> performResetPassword());
        // REMOVED: unlockUserButton listener

        loadUserList();
    }

    private void performResetPassword() {
        int selectedRow = usersTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Select a user to reset password.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String username = (String) tableModel.getValueAt(selectedRow, 0);
        String newPass = JOptionPane.showInputDialog(this, "Enter NEW password for " + username + ":");
        if (newPass != null && !newPass.trim().isEmpty()) {
            try {
                adminService.resetUserPassword(username, newPass);
                JOptionPane.showMessageDialog(this, "Password reset successfully.");
            } catch (SQLException | ServiceException e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
            }
        }
    }

    private void performCreateUser() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());
        String role = (String) roleComboBox.getSelectedItem();
        String rollOrDept = rollOrDeptField.getText();
        String program = programField.getText();

        if (username.isEmpty() || password.isEmpty() || rollOrDept.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields required.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if ("Student".equals(role) && program.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Program is required for students.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            adminService.createNewUser(username, password, role, rollOrDept, program);
            JOptionPane.showMessageDialog(this, "User Created Successfully!");
            usernameField.setText(""); passwordField.setText("");
            rollOrDeptField.setText(""); programField.setText("");
            loadUserList();
        } catch (SQLException | ServiceException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadUserList() {
        String viewType = (String) viewTypeCombo.getSelectedItem();
        tableModel.setRowCount(0);

        try {
            if ("Students".equals(viewType)) {
                tableModel.setColumnIdentifiers(new String[]{"Username", "Roll Number", "Program"});
                List<Student> students = adminService.getAllStudents();
                for (Student s : students) {
                    tableModel.addRow(new Object[]{s.getUsername(), s.getRollNo(), s.getProgram()});
                }
            } else {
                tableModel.setColumnIdentifiers(new String[]{"Username", "Department"});
                List<Instructor> instructors = adminService.getAllInstructors();
                for (Instructor i : instructors) {
                    tableModel.addRow(new Object[]{i.getUsername(), i.getDepartment()});
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading list: " + e.getMessage());
        }
    }
}