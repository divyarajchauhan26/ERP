package edu.univ.erp.ui.student;

import edu.univ.erp.auth.UserSession;
import edu.univ.erp.data.SettingsDAO;
import edu.univ.erp.data.SettingsDAOImpl;
import edu.univ.erp.domain.Section;
import edu.univ.erp.service.CatalogService;
import edu.univ.erp.service.StudentService;
import edu.univ.erp.service.ServiceException;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class CourseCatalogPanel extends JPanel {

    private JTable catalogTable;
    private DefaultTableModel tableModel;
    private JButton registerButton;
    private JLabel infoLabel;

    private CatalogService catalogService;
    private StudentService studentService;
    private SettingsDAO settingsDAO;
    private List<Section> sectionList;

    private boolean isRegistrationOpen = true;

    public CourseCatalogPanel() {
        this.catalogService = new CatalogService();
        this.studentService = new StudentService();
        this.settingsDAO = new SettingsDAOImpl();

        setLayout(new BorderLayout(10, 10));

        JPanel headerPanel = new JPanel(new GridLayout(2, 1));
        headerPanel.add(new JLabel("Available Courses", SwingConstants.CENTER));
        infoLabel = new JLabel("Loading deadline...", SwingConstants.CENTER);
        infoLabel.setFont(new Font("Arial", Font.BOLD, 14));
        headerPanel.add(infoLabel);
        add(headerPanel, BorderLayout.NORTH);

        // --- Table ---
        String[] columnNames = {"Course Code", "Title", "Credits", "Instructor", "Capacity"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
            @Override public Class<?> getColumnClass(int col) {
                if(col==2 || col==4) return Integer.class; return String.class;
            }
        };

        catalogTable = new JTable(tableModel);
        catalogTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // --- STYLING ---
        catalogTable.setShowGrid(true);
        catalogTable.setGridColor(new Color(100, 100, 100));
        catalogTable.setIntercellSpacing(new Dimension(1, 1));
        catalogTable.setRowHeight(30);

        ((DefaultTableCellRenderer)catalogTable.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(JLabel.CENTER);

        DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
        leftRenderer.setHorizontalAlignment(JLabel.LEFT);
        leftRenderer.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
        catalogTable.setDefaultRenderer(Object.class, leftRenderer);
        catalogTable.setDefaultRenderer(Integer.class, leftRenderer);
        catalogTable.setDefaultRenderer(String.class, leftRenderer);
        // ---------------

        JScrollPane scrollPane = new JScrollPane(catalogTable);
        add(scrollPane, BorderLayout.CENTER);

        registerButton = new JButton("Register for Selected Section");
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.add(registerButton);
        add(bottomPanel, BorderLayout.SOUTH);

        registerButton.addActionListener(e -> performRegistration());

        checkDeadlineAndLoadData();
    }

    /**
     * Public method to check registration status and load data.
     * Changed from private to public for StudentDashboard access.
     */
    public void checkDeadlineAndLoadData() {
        try {
            LocalDate dbDate = settingsDAO.getDatabaseCurrentDate();
            LocalDate deadline = settingsDAO.getRegistrationDeadline();

            if (dbDate.isAfter(deadline)) {
                isRegistrationOpen = false;
                infoLabel.setText("Registration Closed (Deadline was: " + deadline + ")");
                infoLabel.setForeground(Color.RED);
                registerButton.setEnabled(false);
                registerButton.setText("Registration Closed");
                tableModel.setRowCount(0);
                return;
            }

            isRegistrationOpen = true;
            infoLabel.setText("Registration Open until: " + deadline);
            infoLabel.setForeground(new Color(0, 128, 0));
            registerButton.setEnabled(true);
            registerButton.setText("Register for Selected Section");
            loadCatalogData();

        } catch (SQLException e) {
            infoLabel.setText("Error checking deadline");
        }
    }

    private void loadCatalogData() {
        try {
            tableModel.setRowCount(0);
            this.sectionList = catalogService.getAllSections();
            for (Section section : sectionList) {
                Object[] row = {
                        section.getCourseCode(),
                        section.getCourseTitle(),
                        section.getCredits(),
                        section.getInstructorName(),
                        section.getCapacity()
                };
                tableModel.addRow(row);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading catalog: " + e.getMessage());
        }
    }

    private void performRegistration() {
        if (!isRegistrationOpen) return;
        int selectedRow = catalogTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a section.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Section selectedSection = sectionList.get(selectedRow);
            int studentId = UserSession.getInstance().getCurrentUser().getUserId();
            studentService.registerForSection(studentId, selectedSection.getSectionId());
            JOptionPane.showMessageDialog(this, "Successfully registered for " + selectedSection.getCourseCode(), "Success", JOptionPane.INFORMATION_MESSAGE);
            // After successful registration, reload the catalog (capacity might change) and update My Registrations
            checkDeadlineAndLoadData();

            // Note: StudentDashboard listener should handle refreshing other tabs (MyRegistrations)

        } catch (ServiceException | SQLException ex) {
            JOptionPane.showMessageDialog(this, "Registration failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}