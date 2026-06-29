package edu.univ.erp.ui.instructor;

import edu.univ.erp.auth.UserSession;
import edu.univ.erp.domain.Enrollment;
import edu.univ.erp.domain.Grade;
import edu.univ.erp.domain.Section;
import edu.univ.erp.domain.Student;
import edu.univ.erp.service.InstructorService;
import edu.univ.erp.service.StudentService;
import edu.univ.erp.service.ServiceException;
import edu.univ.erp.util.CsvExporter; // <--- NEW IMPORT

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.sql.SQLException;
import java.util.*;
import java.util.List;

public class GradebookPanel extends JPanel {

    private InstructorService instructorService;
    private StudentService studentService;

    private List<Section> mySections;
    private List<Student> studentsInSection;
    private Map<Integer, Integer> studentToEnrollmentMap;

    private JComboBox<String> sectionSelector;
    private JTable studentTable;
    private DefaultTableModel studentTableModel;

    private JTable gradeTable;
    private DefaultTableModel gradeTableModel;

    private JTextField componentField;
    private JTextField scoreField;
    private JTextField maxMarksField;
    private JTextField weightageField;
    private JButton saveGradeButton;
    private JButton publishButton;
    private JButton exportButton; // <--- NEW BUTTON
    private JPanel gradeEntryPanel;

    public GradebookPanel() {
        this.instructorService = new InstructorService();
        this.studentService = new StudentService();
        this.studentToEnrollmentMap = new HashMap<>();

        setLayout(new BorderLayout(10, 10));

        // --- Top: Section Selector ---
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Select Section:"));
        sectionSelector = new JComboBox<>();
        topPanel.add(sectionSelector);

        // --- Center: Split Pane ---

        // 1. LEFT SIDE (Students List + Publish/Export Buttons)
        JPanel leftPanel = new JPanel(new BorderLayout(5, 5));

        String[] studentCols = {"Roll No", "Name"};
        studentTableModel = new DefaultTableModel(studentCols, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        studentTable = new JTable(studentTableModel);
        styleTable(studentTable);
        studentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane studentScroll = new JScrollPane(studentTable);
        leftPanel.add(studentScroll, BorderLayout.CENTER);

        // Publish Button
        publishButton = new JButton("Publish Grades for Class");
        publishButton.setBackground(new Color(40, 167, 69));
        publishButton.setForeground(Color.WHITE);
        publishButton.setFocusPainted(false);

        // Export Button
        exportButton = new JButton("Export Roster (CSV)");
        exportButton.setBackground(new Color(50, 50, 130));
        exportButton.setForeground(Color.WHITE);
        exportButton.setFocusPainted(false);

        // Wrapper for buttons padding
        JPanel btnWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnWrapper.add(publishButton);
        btnWrapper.add(exportButton);
        leftPanel.add(btnWrapper, BorderLayout.SOUTH);

        // 2. RIGHT SIDE (Individual Student Grades)
        JPanel rightPanel = new JPanel(new BorderLayout(5, 5));

        String[] gradeCols = {"Component", "Score", "Max Marks", "Weightage %"};
        gradeTableModel = new DefaultTableModel(gradeCols, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        gradeTable = new JTable(gradeTableModel);
        styleTable(gradeTable);
        JScrollPane gradeScroll = new JScrollPane(gradeTable);

        // --- Grade Entry Form ---
        gradeEntryPanel = new JPanel(new GridLayout(5, 2, 5, 5));
        gradeEntryPanel.setBorder(BorderFactory.createTitledBorder("Manage Grades"));

        gradeEntryPanel.add(new JLabel("Component Name:"));
        componentField = new JTextField();
        gradeEntryPanel.add(componentField);

        gradeEntryPanel.add(new JLabel("Score Obtained:"));
        scoreField = new JTextField();
        gradeEntryPanel.add(scoreField);

        gradeEntryPanel.add(new JLabel("Max Marks (e.g. 50):"));
        maxMarksField = new JTextField();
        gradeEntryPanel.add(maxMarksField);

        gradeEntryPanel.add(new JLabel("Weightage % (e.g. 20):"));
        weightageField = new JTextField();
        gradeEntryPanel.add(weightageField);

        gradeEntryPanel.add(new JLabel(""));
        saveGradeButton = new JButton("Save Grade");
        gradeEntryPanel.add(saveGradeButton);

        gradeEntryPanel.setVisible(false);

        rightPanel.add(new JLabel("Grades for Selected Student", SwingConstants.CENTER), BorderLayout.NORTH);
        rightPanel.add(gradeScroll, BorderLayout.CENTER);
        rightPanel.add(gradeEntryPanel, BorderLayout.SOUTH);

        // Split Pane connects Left (List+Button) and Right (Details)
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        splitPane.setDividerLocation(300);

        add(topPanel, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);

        // Listeners
        loadSectionSelector();
        sectionSelector.addActionListener(e -> loadStudentsForSelectedSection());
        studentTable.getSelectionModel().addListSelectionListener(e -> loadGradesForSelectedStudent());
        saveGradeButton.addActionListener(e -> performSaveGrade());
        publishButton.addActionListener(e -> performPublishGrade());
        exportButton.addActionListener(e -> performExportGrades()); // <--- NEW LISTENER
    }

    private void styleTable(JTable table) {
        table.setShowGrid(true);
        table.setGridColor(new Color(100, 100, 100));
        table.setIntercellSpacing(new Dimension(1, 1));
        table.setRowHeight(30);
        ((DefaultTableCellRenderer)table.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(JLabel.CENTER);
        DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
        leftRenderer.setHorizontalAlignment(JLabel.LEFT);
        leftRenderer.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
        table.setDefaultRenderer(Object.class, leftRenderer);
    }

    private void loadSectionSelector() {
        try {
            int instructorId = UserSession.getInstance().getCurrentUser().getUserId();
            mySections = instructorService.getMySections(instructorId);
            sectionSelector.removeAllItems();
            for (Section s : mySections) {
                sectionSelector.addItem(s.getSectionId() + ": " + s.getCourseCode());
            }
        } catch (SQLException e) { showError("Error loading sections: " + e.getMessage()); }
    }

    private void loadStudentsForSelectedSection() {
        int idx = sectionSelector.getSelectedIndex();
        if (idx == -1) return;
        Section sec = mySections.get(idx);

        studentTableModel.setRowCount(0);
        gradeTableModel.setRowCount(0);
        gradeEntryPanel.setVisible(false);
        studentToEnrollmentMap.clear();

        try {
            studentsInSection = instructorService.getStudentsBySection(sec.getSectionId());
            for (Student s : studentsInSection) {
                studentTableModel.addRow(new Object[]{s.getRollNo(), s.getUsername()});
            }
        } catch (SQLException e) { showError("Error loading students: " + e.getMessage()); }
    }

    private void loadGradesForSelectedStudent() {
        int row = studentTable.getSelectedRow();
        if (row == -1) {
            gradeEntryPanel.setVisible(false);
            return;
        }

        gradeTableModel.setRowCount(0);
        gradeEntryPanel.setVisible(true);

        try {
            Student s = studentsInSection.get(row);

            int enrollmentId = -1;
            List<Enrollment> enrs = studentService.getMyEnrollments(s.getUserId());
            int currentSecId = mySections.get(sectionSelector.getSelectedIndex()).getSectionId();

            for(Enrollment e : enrs) {
                if(e.getSectionId() == currentSecId) {
                    enrollmentId = e.getEnrollmentId();
                    break;
                }
            }

            if(enrollmentId == -1) { showError("Enrollment not found"); return; }
            studentToEnrollmentMap.put(s.getUserId(), enrollmentId);

            List<Grade> grades = instructorService.getGradesForEnrollment(enrollmentId);
            for (Grade g : grades) {
                gradeTableModel.addRow(new Object[]{
                        g.getComponent(),
                        g.getScore(),
                        g.getMaxMarks(),
                        g.getWeightage()
                });
            }

        } catch (SQLException e) { showError("Error: " + e.getMessage()); }
    }

    private void performSaveGrade() {
        int row = studentTable.getSelectedRow();
        if (row == -1) return;
        try {
            String component = componentField.getText();
            double score = Double.parseDouble(scoreField.getText());
            double maxMarks = Double.parseDouble(maxMarksField.getText());
            double weightage = Double.parseDouble(weightageField.getText());
            if (component.isEmpty()) { showError("Component name required"); return; }
            Student s = studentsInSection.get(row);
            int enrollmentId = studentToEnrollmentMap.get(s.getUserId());
            instructorService.submitGrade(enrollmentId, component, score, maxMarks, weightage);
            JOptionPane.showMessageDialog(this, "Grade Saved!");
            componentField.setText(""); scoreField.setText("");
            maxMarksField.setText(""); weightageField.setText("");
            loadGradesForSelectedStudent();
        } catch (NumberFormatException e) { showError("Numbers required."); }
        catch (SQLException | ServiceException e) { showError("Error: " + e.getMessage()); }
    }

    private void performPublishGrade() {
        // ... (Existing logic for publishing grades)
        if (sectionSelector.getSelectedIndex() == -1) return;

        // Ask Instructor for Grading Scale
        JPanel panel = new JPanel(new GridLayout(7, 2, 5, 5));
        JTextField fieldA = new JTextField("90");
        JTextField fieldAm = new JTextField("85");
        JTextField fieldB = new JTextField("80");
        JTextField fieldBm = new JTextField("75");
        JTextField fieldC = new JTextField("70");
        JTextField fieldCm = new JTextField("60");
        JTextField fieldD = new JTextField("50");

        panel.add(new JLabel("Min for A:")); panel.add(fieldA);
        panel.add(new JLabel("Min for A-:")); panel.add(fieldAm);
        panel.add(new JLabel("Min for B:")); panel.add(fieldB);
        panel.add(new JLabel("Min for B-:")); panel.add(fieldBm);
        panel.add(new JLabel("Min for C:")); panel.add(fieldC);
        panel.add(new JLabel("Min for C-:")); panel.add(fieldCm);
        panel.add(new JLabel("Min for D:")); panel.add(fieldD);

        int result = JOptionPane.showConfirmDialog(this, panel,
                "Publish Grades for Class", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            try {
                Map<String, Double> cutoffs = new HashMap<>();
                cutoffs.put("A", Double.parseDouble(fieldA.getText()));
                cutoffs.put("A-", Double.parseDouble(fieldAm.getText()));
                cutoffs.put("B", Double.parseDouble(fieldB.getText()));
                cutoffs.put("B-", Double.parseDouble(fieldBm.getText()));
                cutoffs.put("C", Double.parseDouble(fieldC.getText()));
                cutoffs.put("C-", Double.parseDouble(fieldCm.getText()));
                cutoffs.put("D", Double.parseDouble(fieldD.getText()));

                int idx = sectionSelector.getSelectedIndex();
                Section sec = mySections.get(idx);

                instructorService.publishAllGrades(sec.getSectionId(), cutoffs);

                JOptionPane.showMessageDialog(this, "All grades published successfully!");

            } catch (NumberFormatException e) {
                showError("Invalid number entered for cutoff.");
            } catch (SQLException | ServiceException e) {
                showError("Error publishing: " + e.getMessage());
            }
        }
    }

    private void performExportGrades() {
        int idx = sectionSelector.getSelectedIndex();
        if (idx == -1) { showError("Select a section first."); return; }

        Section sec = mySections.get(idx);
        String courseCode = sec.getCourseCode();
        String instructorName = sec.getInstructorName();

        try {
            // 1. Get the comprehensive roster data from the service
            List<Map<String, Object>> exportData = instructorService.getFullGradeRosterForExport(sec.getSectionId());

            if (exportData.isEmpty()) {
                showError("No enrollment data found for this section.");
                return;
            }

            // 2. Dynamically determine the full set of columns for the header
            Set<String> dynamicHeaders = new TreeSet<>();
            for (Map<String, Object> row : exportData) {
                dynamicHeaders.addAll(row.keySet());
            }

            // 3. Define the mandatory header order (Student Info)
            List<String> finalHeaderList = new ArrayList<>();
            finalHeaderList.add("Roll No");
            finalHeaderList.add("Student Name");

            // 4. Add dynamic grade component headers in a consistent order (A-Z)
            Set<String> sortedGradeKeys = new TreeSet<>(dynamicHeaders);
            for (String key : sortedGradeKeys) {
                if (!key.equals("Roll No") && !key.equals("Student Name")
                        && !key.equals("Weighted Total") && !key.equals("Max Weight Possible")
                        && !key.equals("Final Grade")) {
                    finalHeaderList.add(key);
                }
            }

            // 5. Add final summary columns
            finalHeaderList.add("Weighted Total");
            finalHeaderList.add("Max Weight Possible");
            finalHeaderList.add("Final Grade");


            // 6. File Chooser Dialog
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Save Grade Roster");
            fileChooser.setSelectedFile(new File(courseCode + "_GradeRoster.csv"));

            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();

                // 7. Export to CSV using the CsvExporter utility
                CsvExporter.exportData(file.getAbsolutePath(), exportData, finalHeaderList.toArray(new String[0]));

                JOptionPane.showMessageDialog(this, "Grade Roster saved successfully to " + file.getName(), "Success", JOptionPane.INFORMATION_MESSAGE);
            }

        } catch (ServiceException | SQLException e) {
            showError("Error exporting roster: " + e.getMessage());
        } catch (Exception ex) {
            showError("An unexpected error occurred during export: " + ex.getMessage());
        }
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }
}