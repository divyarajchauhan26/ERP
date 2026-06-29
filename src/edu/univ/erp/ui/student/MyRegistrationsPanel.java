package edu.univ.erp.ui.student;

import edu.univ.erp.auth.UserSession;
import edu.univ.erp.data.SettingsDAO;
import edu.univ.erp.data.SettingsDAOImpl;
import edu.univ.erp.domain.Enrollment;
import edu.univ.erp.domain.Section;
import edu.univ.erp.service.StudentService;
import edu.univ.erp.service.ServiceException;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class MyRegistrationsPanel extends JPanel {

    private JTable registrationsTable;
    private DefaultTableModel tableModel;
    private JButton dropButton;
    private JLabel deadlineLabel;

    private StudentService studentService;
    private SettingsDAO settingsDAO;
    private List<Enrollment> enrollmentList;

    public MyRegistrationsPanel() {
        this.studentService = new StudentService();
        this.settingsDAO = new SettingsDAOImpl();

        setLayout(new BorderLayout(10, 10));

        // --- Table Setup ---
        String[] columnNames = {"Course Code", "Section ID", "Status"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        registrationsTable = new JTable(tableModel);
        registrationsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // --- STYLING ---
        registrationsTable.setShowGrid(true);
        registrationsTable.setGridColor(new Color(100, 100, 100));
        registrationsTable.setIntercellSpacing(new Dimension(1, 1));
        registrationsTable.setRowHeight(30);

        ((DefaultTableCellRenderer)registrationsTable.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(JLabel.CENTER);

        DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
        leftRenderer.setHorizontalAlignment(JLabel.LEFT);
        leftRenderer.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
        registrationsTable.setDefaultRenderer(Object.class, leftRenderer);
        // ---------------

        JScrollPane scrollPane = new JScrollPane(registrationsTable);

        add(new JLabel("My Registered Sections", SwingConstants.CENTER), BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        // --- Bottom Controls ---
        JPanel bottomContainer = new JPanel(new GridLayout(2, 1, 5, 5));
        bottomContainer.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        deadlineLabel = new JLabel("Loading deadline...", SwingConstants.CENTER);
        deadlineLabel.setFont(new Font("Arial", Font.BOLD, 14));

        dropButton = new JButton("Drop Selected Section");
        dropButton.setPreferredSize(new Dimension(200, 35));

        JPanel btnWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnWrapper.add(dropButton);

        bottomContainer.add(deadlineLabel);
        bottomContainer.add(btnWrapper);
        add(bottomContainer, BorderLayout.SOUTH);

        // Action Listener for Drop
        dropButton.addActionListener(e -> performDrop());

        // Initial Load
        refreshData();
    }

    /**
     * Public method to refresh data from the database.
     * Called by StudentDashboard when this tab is selected.
     */
    public void refreshData() {
        loadRegistrationData();
        updateDeadlineLabel();
    }

    private void updateDeadlineLabel() {
        try {
            LocalDate deadline = settingsDAO.getDropDeadline();
            LocalDate today = settingsDAO.getDatabaseCurrentDate();
            deadlineLabel.setText("Last date to drop: " + deadline.toString());

            if (today.isAfter(deadline)) {
                deadlineLabel.setForeground(Color.RED);
                deadlineLabel.setText("Drop Period Over (Deadline was " + deadline + ")");
                dropButton.setEnabled(false);
            } else {
                deadlineLabel.setForeground(new Color(0, 128, 0));
                dropButton.setEnabled(true);
            }
        } catch (SQLException e) {
            deadlineLabel.setText("Error checking deadline");
        }
    }

    public void loadRegistrationData() {
        try {
            // Clear existing rows
            tableModel.setRowCount(0);

            int studentId = UserSession.getInstance().getCurrentUser().getUserId();

            // Fetch fresh data
            this.enrollmentList = studentService.getMyEnrollments(studentId);
            List<Section> mySections = studentService.getMyTimetable(studentId);

            for (Enrollment enr : enrollmentList) {
                String courseCode = "Unknown";
                // Find matching course code for this section
                for (Section sec : mySections) {
                    if (sec.getSectionId() == enr.getSectionId()) {
                        courseCode = sec.getCourseCode();
                        break;
                    }
                }
                Object[] row = { courseCode, enr.getSectionId(), enr.getStatus() };
                tableModel.addRow(row);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading data: " + e.getMessage());
        }
    }

    private void performDrop() {
        int selectedRow = registrationsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a section to drop.");
            return;
        }

        try {
            // Double check deadline before processing
            LocalDate deadline = settingsDAO.getDropDeadline();
            LocalDate today = settingsDAO.getDatabaseCurrentDate();
            if (today.isAfter(deadline)) {
                JOptionPane.showMessageDialog(this, "Drop period is over!", "Error", JOptionPane.ERROR_MESSAGE);
                updateDeadlineLabel();
                return;
            }

            Enrollment selectedEnrollment = enrollmentList.get(selectedRow);
            studentService.dropSection(selectedEnrollment.getEnrollmentId());

            JOptionPane.showMessageDialog(this, "Successfully dropped section.");

            // Refresh UI immediately after drop
            refreshData();

        } catch (ServiceException | SQLException ex) {
            JOptionPane.showMessageDialog(this, "Drop failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}