package edu.univ.erp.ui.student;

import edu.univ.erp.auth.UserSession;
import edu.univ.erp.domain.Enrollment;
import edu.univ.erp.domain.Grade;
import edu.univ.erp.domain.Section;
import edu.univ.erp.service.StudentService;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class MyGradesPanel extends JPanel {

    private JComboBox<String> courseSelector;
    private JTable gradesTable;
    private DefaultTableModel tableModel;

    private JLabel currentScoreLabel;
    private JLabel finalGradeLabel;

    private List<Enrollment> myEnrollments;
    private List<Section> mySections;
    private StudentService studentService;

    public MyGradesPanel() {
        this.studentService = new StudentService();
        setLayout(new BorderLayout(10, 10));

        // --- 1. Top Panel: Course Selector ---
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Select Course to View:"));
        courseSelector = new JComboBox<>();
        topPanel.add(courseSelector);
        add(topPanel, BorderLayout.NORTH);

        // --- 2. Center Panel: Grades Table ---
        String[] columnNames = {"Component", "Marks Obtained / Max", "Weightage"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        gradesTable = new JTable(tableModel);

        // --- STYLING START ---
        gradesTable.setShowGrid(true);
        gradesTable.setGridColor(new Color(100, 100, 100)); // Gray Grid
        gradesTable.setIntercellSpacing(new Dimension(1, 1));
        gradesTable.setRowHeight(35); // Comfortable height

        // Header: Center Aligned
        ((DefaultTableCellRenderer)gradesTable.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(JLabel.CENTER);

        // Data: Left Aligned + Padding
        DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
        leftRenderer.setHorizontalAlignment(JLabel.LEFT);
        leftRenderer.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0)); // 10px padding left
        gradesTable.setDefaultRenderer(Object.class, leftRenderer);
        // --- STYLING END ---

        JScrollPane scrollPane = new JScrollPane(gradesTable);
        add(scrollPane, BorderLayout.CENTER);

        // --- 3. Bottom Panel: Summary Section ---
        JPanel summaryPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        summaryPanel.setBorder(BorderFactory.createTitledBorder("Weighted Grade Summary"));

        currentScoreLabel = new JLabel("Current Cumulative Grade: -");
        currentScoreLabel.setFont(new Font("Arial", Font.BOLD, 15));

        finalGradeLabel = new JLabel("Official Final Grade: Pending");
        finalGradeLabel.setFont(new Font("Arial", Font.BOLD, 14));
        finalGradeLabel.setForeground(Color.BLUE);

        summaryPanel.add(currentScoreLabel);
        summaryPanel.add(finalGradeLabel);
        add(summaryPanel, BorderLayout.SOUTH);

        loadCourseSelector();
        courseSelector.addActionListener(e -> loadGradesForSelectedCourse());
    }

    // --- REQUIRED METHOD FOR DASHBOARD REFRESH ---
    public void refreshData() {
        // For grades, we just reload the list of courses
        // This handles the case where you just registered for a new class
        loadCourseSelector();
    }

    private void loadCourseSelector() {
        try {
            int studentId = UserSession.getInstance().getCurrentUser().getUserId();
            myEnrollments = studentService.getMyEnrollments(studentId);
            mySections = studentService.getMyTimetable(studentId);

            // Preserve previous selection if possible
            int previousIndex = courseSelector.getSelectedIndex();

            courseSelector.removeAllItems();
            for (Enrollment enrollment : myEnrollments) {
                String displayName = "Section ID: " + enrollment.getSectionId();
                for (Section section : mySections) {
                    if (section.getSectionId() == enrollment.getSectionId()) {
                        displayName = section.getCourseCode() + " - " + section.getCourseTitle();
                        break;
                    }
                }
                courseSelector.addItem(displayName);
            }

            // If the user had something selected, try to keep it, otherwise select first
            if (courseSelector.getItemCount() > 0) {
                if (previousIndex >= 0 && previousIndex < courseSelector.getItemCount()) {
                    courseSelector.setSelectedIndex(previousIndex);
                } else {
                    courseSelector.setSelectedIndex(0);
                }
                // Force load of the selected course
                loadGradesForSelectedCourse();
            } else {
                // Clear table if no courses
                tableModel.setRowCount(0);
                currentScoreLabel.setText("Current Cumulative Grade: -");
                finalGradeLabel.setText("Official Final Grade: -");
            }

        } catch (SQLException e) {
            if(this.isShowing()) {
                JOptionPane.showMessageDialog(this, "Error loading course list: " + e.getMessage());
            }
        }
    }

    private void loadGradesForSelectedCourse() {
        int idx = courseSelector.getSelectedIndex();
        if (idx == -1) return;

        if (idx >= myEnrollments.size()) return; // Safety check

        Enrollment selectedEnrollment = myEnrollments.get(idx);

        try {
            tableModel.setRowCount(0);
            List<Grade> grades = studentService.getGrades(selectedEnrollment.getEnrollmentId());

            double totalEarnedWeight = 0.0;
            double totalPossibleWeight = 0.0;
            String finalGradeStatus = "Pending";

            for (Grade grade : grades) {
                // Format: "30.0 / 60.0"
                String marksDisplay = grade.getScore() + " / " + grade.getMaxMarks();
                // Format: "30.0%"
                String weightDisplay = grade.getWeightage() + "%";

                tableModel.addRow(new Object[]{
                        grade.getComponent(),
                        marksDisplay,
                        weightDisplay
                });

                if (grade.getMaxMarks() > 0) {
                    double percentage = grade.getScore() / grade.getMaxMarks();
                    double earnedWeight = percentage * grade.getWeightage();
                    totalEarnedWeight += earnedWeight;
                    totalPossibleWeight += grade.getWeightage();
                }

                if (grade.getFinalGrade() != null && !grade.getFinalGrade().isEmpty()) {
                    finalGradeStatus = grade.getFinalGrade();
                }
            }

            currentScoreLabel.setText(String.format("Current Cumulative Grade: %.2f / %.2f", totalEarnedWeight, totalPossibleWeight));

            if ("Pending".equals(finalGradeStatus)) {
                finalGradeLabel.setText("Official Final Grade: Pending");
                finalGradeLabel.setForeground(Color.GRAY);
            } else {
                finalGradeLabel.setText("Official Final Grade: " + finalGradeStatus);
                finalGradeLabel.setForeground(new Color(0, 128, 0));
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }
}