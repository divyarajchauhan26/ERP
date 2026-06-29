package edu.univ.erp.ui.admin;

import edu.univ.erp.domain.Course;
import edu.univ.erp.domain.Section;
import edu.univ.erp.service.AdminService;
import edu.univ.erp.service.CatalogService;
import edu.univ.erp.service.ServiceException;
import edu.univ.erp.util.ScheduleFormatter;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class CourseManagementPanel extends JPanel {

    private AdminService adminService;
    private CatalogService catalogService;

    private List<Course> allCourses;

    // --- Tab 1 Components (Create/Delete Course) ---
    private JTextField courseCodeField, courseTitleField, creditsField;
    private JButton createCourseButton;
    private JButton deleteCourseButton; // <--- NEW BUTTON

    // --- Tab 2 Components (Create Section) ---
    private JComboBox<String> secCourseSelector;
    private JTextField secInstUsername, secRoom, secCap;
    private JButton createSectionButton;
    private JCheckBox[] createCheckboxes;
    private JTextField[] createStartFields, createEndFields;

    // --- Tab 3 Components (Edit/Delete Section) ---
    private JComboBox<String> sectionSelector;
    private JComboBox<String> editCourseSelector;
    private JTextField editInstUsername, editRoom, editCap, editSem, editYear;
    private JButton updateSectionButton;
    private JButton deleteSectionButton; // <--- NEW BUTTON
    private List<Section> allSections;
    private JCheckBox[] editCheckboxes;
    private JTextField[] editStartFields, editEndFields;

    private final String[] DAYS = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};

    public CourseManagementPanel() {
        this.adminService = new AdminService();
        this.catalogService = new CatalogService();

        setLayout(new BorderLayout());
        JTabbedPane tabbedPane = new JTabbedPane();

        // ==================================================
        // TAB 1: CREATE/DELETE COURSE
        // ==================================================
        JPanel tab1 = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10); gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx=0; gbc.gridy=0; tab1.add(new JLabel("Course Code:"), gbc);
        courseCodeField = new JTextField(15);
        gbc.gridx=1; tab1.add(courseCodeField, gbc);

        gbc.gridx=0; gbc.gridy=1; tab1.add(new JLabel("Title:"), gbc);
        courseTitleField = new JTextField(25);
        gbc.gridx=1; tab1.add(courseTitleField, gbc);

        gbc.gridx=0; gbc.gridy=2; tab1.add(new JLabel("Credits:"), gbc);
        creditsField = new JTextField(5);
        gbc.gridx=1; tab1.add(creditsField, gbc);

        // Button Panel for Tab 1
        JPanel tab1ButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        createCourseButton = new JButton("Create New Course");
        tab1ButtonPanel.add(createCourseButton);

        deleteCourseButton = new JButton("Delete Course"); // <--- DELETE BUTTON
        deleteCourseButton.setBackground(new Color(192, 57, 43)); // Red
        deleteCourseButton.setForeground(Color.WHITE);
        tab1ButtonPanel.add(deleteCourseButton);

        gbc.gridx=1; gbc.gridy=3;
        gbc.insets = new Insets(20, 10, 10, 10);
        tab1.add(tab1ButtonPanel, gbc); // Add button panel

        tabbedPane.addTab("Create/Delete Course", tab1);

        // ==================================================
        // TAB 2: CREATE NEW SECTION
        // ==================================================
        JPanel tab2 = new JPanel(new GridBagLayout());
        gbc.insets = new Insets(5, 5, 5, 5); // Reset insets

        gbc.gridx=0; gbc.gridy=0; tab2.add(new JLabel("Course:"), gbc);
        secCourseSelector = new JComboBox<>();
        gbc.gridx=1; tab2.add(secCourseSelector, gbc);

        gbc.gridx=0; gbc.gridy=1; tab2.add(new JLabel("Instructor Username:"), gbc);
        secInstUsername = new JTextField(15);
        gbc.gridx=1; tab2.add(secInstUsername, gbc);

        // Schedule Builder (Create)
        JPanel createSchedPanelWrapper = new JPanel(new BorderLayout());
        createSchedPanelWrapper.setBorder(BorderFactory.createTitledBorder("Schedule (Day/Time)"));
        JPanel createSchedPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        createCheckboxes = new JCheckBox[6];
        createStartFields = new JTextField[6];
        createEndFields = new JTextField[6];
        initScheduleComponents(createSchedPanel, createCheckboxes, createStartFields, createEndFields);
        createSchedPanelWrapper.add(createSchedPanel, BorderLayout.CENTER);

        gbc.gridx=0; gbc.gridy=2; tab2.add(new JLabel(""), gbc);
        gbc.gridx=1; gbc.gridy=2; tab2.add(createSchedPanelWrapper, gbc);

        int row = 3;
        gbc.gridx=0; gbc.gridy=row++; tab2.add(new JLabel("Room:"), gbc);
        secRoom = new JTextField(15); gbc.gridx=1; tab2.add(secRoom, gbc);

        gbc.gridx=0; gbc.gridy=row++; tab2.add(new JLabel("Capacity:"), gbc);
        secCap = new JTextField(5); gbc.gridx=1; tab2.add(secCap, gbc);

        createSectionButton = new JButton("Create Section");
        gbc.gridx=1; gbc.gridy=row;
        gbc.insets = new Insets(15, 5, 5, 5);
        tab2.add(createSectionButton, gbc);

        tabbedPane.addTab("Create New Section", tab2);

        // ==================================================
        // TAB 3: EDIT/DELETE EXISTING SECTION
        // ==================================================
        JPanel tab3 = new JPanel(new GridBagLayout());

        gbc.gridx=0; gbc.gridy=0; tab3.add(new JLabel("Select Section:"), gbc);
        sectionSelector = new JComboBox<>();
        gbc.gridx=1; tab3.add(sectionSelector, gbc);

        gbc.gridx=0; gbc.gridy=1; tab3.add(new JLabel("Course:"), gbc);
        editCourseSelector = new JComboBox<>();
        gbc.gridx=1; tab3.add(editCourseSelector, gbc);

        gbc.gridx=0; gbc.gridy=2; tab3.add(new JLabel("Instructor Username:"), gbc);
        editInstUsername = new JTextField(15);
        gbc.gridx=1; tab3.add(editInstUsername, gbc);

        // Schedule Builder (Edit)
        JPanel editSchedPanelWrapper = new JPanel(new BorderLayout());
        editSchedPanelWrapper.setBorder(BorderFactory.createTitledBorder("Schedule (Day/Time)"));
        JPanel editSchedPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        editCheckboxes = new JCheckBox[6];
        editStartFields = new JTextField[6];
        editEndFields = new JTextField[6];
        initScheduleComponents(editSchedPanel, editCheckboxes, editStartFields, editEndFields);
        editSchedPanelWrapper.add(editSchedPanel, BorderLayout.CENTER);

        gbc.gridx=0; gbc.gridy=3; tab3.add(new JLabel(""), gbc);
        gbc.gridx=1; gbc.gridy=3; tab3.add(editSchedPanelWrapper, gbc);

        row = 4;
        gbc.gridx=0; gbc.gridy=row++; tab3.add(new JLabel("Room:"), gbc);
        editRoom = new JTextField(15); gbc.gridx=1; tab3.add(editRoom, gbc);

        gbc.gridx=0; gbc.gridy=row++; tab3.add(new JLabel("Capacity:"), gbc);
        editCap = new JTextField(5); gbc.gridx=1; tab3.add(editCap, gbc);

        gbc.gridx=0; gbc.gridy=row++; tab3.add(new JLabel("Semester:"), gbc);
        editSem = new JTextField("Monsoon", 10); gbc.gridx=1; tab3.add(editSem, gbc);

        gbc.gridx=0; gbc.gridy=row++; tab3.add(new JLabel("Year:"), gbc);
        editYear = new JTextField("2025", 5); gbc.gridx=1; tab3.add(editYear, gbc);

        // Button Panel for Tab 3
        JPanel tab3ButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        updateSectionButton = new JButton("Update Section");
        tab3ButtonPanel.add(updateSectionButton);

        deleteSectionButton = new JButton("Delete Section"); // <--- DELETE BUTTON
        deleteSectionButton.setBackground(new Color(192, 57, 43)); // Red
        deleteSectionButton.setForeground(Color.WHITE);
        tab3ButtonPanel.add(deleteSectionButton);

        gbc.gridx=1; gbc.gridy=row;
        gbc.insets = new Insets(15, 5, 5, 5);
        tab3.add(tab3ButtonPanel, gbc);

        tabbedPane.addTab("Edit Existing Section", tab3);

        add(tabbedPane, BorderLayout.CENTER);

        // --- Listeners ---
        createCourseButton.addActionListener(e -> performCreateCourse());
        deleteCourseButton.addActionListener(e -> performDeleteCourse()); // <--- NEW LISTENER
        createSectionButton.addActionListener(e -> performCreateSection());
        deleteSectionButton.addActionListener(e -> performDeleteSection()); // <--- NEW LISTENER

        tabbedPane.addChangeListener(e -> {
            if (tabbedPane.getSelectedIndex() == 1 || tabbedPane.getSelectedIndex() == 2) {
                loadCourseSelectors();
                if (tabbedPane.getSelectedIndex() == 2) {
                    loadSectionSelector();
                }
            }
        });

        sectionSelector.addActionListener(e -> loadSectionDetails());
        updateSectionButton.addActionListener(e -> performUpdateSection());

        loadCourseSelectors();
    }

    private void loadCourseSelectors() {
        try {
            allCourses = adminService.getAllCourses();
            secCourseSelector.removeAllItems();
            editCourseSelector.removeAllItems();

            for(Course c : allCourses) {
                String item = c.getCode() + " - " + c.getTitle();
                secCourseSelector.addItem(item);
                editCourseSelector.addItem(item);
            }
        } catch (SQLException e) { showError("Error loading courses for selector: " + e.getMessage()); }
    }


    private void initScheduleComponents(JPanel panel, JCheckBox[] checks, JTextField[] starts, JTextField[] ends) {
        for (int i = 0; i < 6; i++) {
            JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            checks[i] = new JCheckBox(DAYS[i]);
            checks[i].setPreferredSize(new Dimension(55, 25));

            starts[i] = new JTextField("10:00", 5);
            ends[i] = new JTextField("10:50", 5);

            starts[i].setEnabled(false);
            ends[i].setEnabled(false);

            int finalI = i;
            checks[i].addActionListener(e -> {
                boolean isSelected = checks[finalI].isSelected();
                starts[finalI].setEnabled(isSelected);
                ends[finalI].setEnabled(isSelected);
            });

            row.add(checks[i]);
            row.add(starts[i]);
            row.add(new JLabel("-"));
            row.add(ends[i]);
            panel.add(row);
        }
    }

    private String buildScheduleString(JCheckBox[] checks, JTextField[] starts, JTextField[] ends) {

        boolean[] checkStates = new boolean[checks.length];
        String[] startTimes = new String[starts.length];
        String[] endTimes = new String[ends.length];

        for(int i=0; i<checks.length; i++) {
            checkStates[i] = checks[i].isSelected();
            startTimes[i] = starts[i].getText();
            endTimes[i] = ends[i].getText();
        }

        return ScheduleFormatter.buildScheduleString(checkStates, startTimes, endTimes);
    }

    private void loadScheduleToUI(String schedule) {
        for(int i=0; i<6; i++) {
            editCheckboxes[i].setSelected(false);
            editStartFields[i].setEnabled(false);
            editEndFields[i].setEnabled(false);
            editStartFields[i].setText("10:00");
            editEndFields[i].setText("10:50");
        }

        if(schedule == null || schedule.isEmpty()) return;

        List<String[]> dayTimeList = ScheduleFormatter.parseScheduleToDayTimeList(schedule);

        for(String[] entry : dayTimeList) {
            String dayName = entry[0];
            String start = entry[1];
            String end = entry[2];

            for(int i=0; i<DAYS.length; i++) {
                if(DAYS[i].equals(dayName)) {
                    editCheckboxes[i].setSelected(true);
                    editStartFields[i].setEnabled(true);
                    editEndFields[i].setEnabled(true);
                    editStartFields[i].setText(start);
                    editEndFields[i].setText(end);
                    break;
                }
            }
        }
    }

    private void performCreateCourse() {
        try {
            String code = courseCodeField.getText();
            String title = courseTitleField.getText();
            int credits = Integer.parseInt(creditsField.getText());
            adminService.createCourse(code, title, credits);
            JOptionPane.showMessageDialog(this, "Course Created!");
            courseCodeField.setText(""); courseTitleField.setText(""); creditsField.setText("");
            loadCourseSelectors();
        } catch (Exception e) { showError(e.getMessage()); }
    }

    // --- NEW METHOD ---
    private void performDeleteCourse() {
        String code = courseCodeField.getText();
        if (code.isEmpty()) { showError("Enter a course code to delete."); return; }

        int confirm = JOptionPane.showConfirmDialog(this,
                "WARNING: Are you sure you want to delete course " + code + "?\nThis action cannot be undone and will fail if sections exist.",
                "Confirm Course Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                adminService.deleteCourse(code);
                JOptionPane.showMessageDialog(this, "Course '" + code + "' deleted successfully!");
                courseCodeField.setText("");
                loadCourseSelectors(); // Refresh dropdowns
            } catch (ServiceException e) {
                showError("Deletion Failed: " + e.getMessage());
            } catch (SQLException e) {
                showError("Database Error: " + e.getMessage());
            }
        }
    }

    private void performCreateSection() {
        try {
            String selectedCourse = (String) secCourseSelector.getSelectedItem();
            if (selectedCourse == null) { showError("Select a course."); return; }
            String courseCode = selectedCourse.split(" - ")[0];

            String instUser = secInstUsername.getText();
            String rm = secRoom.getText();

            // --- VALIDATION FIX: Capacity check ---
            int cap = Integer.parseInt(secCap.getText());
            if (cap <= 0) {
                showError("Capacity must be a positive number.");
                return;
            }
            // --- END FIX ---

            String dt = buildScheduleString(createCheckboxes, createStartFields, createEndFields);

            if (dt.isEmpty()) { showError("Select at least one day."); return; }

            adminService.createSection(courseCode, instUser, dt, rm, cap);
            JOptionPane.showMessageDialog(this, "Section Created!");

            secInstUsername.setText(""); secRoom.setText(""); secCap.setText("");
            for(int i=0; i<6; i++) {
                createCheckboxes[i].setSelected(false);
                createStartFields[i].setEnabled(false);
            }
        } catch (NumberFormatException e) {
            showError("Invalid input for Credits or Capacity (Must be a number).");
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    // --- NEW METHOD ---
    private void performDeleteSection() {
        int idx = sectionSelector.getSelectedIndex();
        if (idx == -1) { showError("Select a section to delete."); return; }

        Section selectedSection = allSections.get(idx);
        int sectionId = selectedSection.getSectionId();
        String courseCode = selectedSection.getCourseCode();

        int confirm = JOptionPane.showConfirmDialog(this,
                "WARNING: Are you sure you want to delete Section " + sectionId + " (" + courseCode + ")?\nThis action cannot be undone and will be blocked if students are enrolled.",
                "Confirm Section Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                adminService.deleteSection(sectionId);
                JOptionPane.showMessageDialog(this, "Section deleted successfully!");
                loadSectionSelector(); // Refresh section selector
            } catch (ServiceException e) {
                showError("Deletion Failed: " + e.getMessage());
            } catch (SQLException e) {
                showError("Database Error: " + e.getMessage());
            }
        }
    }

    private void loadSectionSelector() {
        try {
            sectionSelector.removeAllItems();
            allSections = catalogService.getAllSections();
            for (Section s : allSections) {
                sectionSelector.addItem("Sec " + s.getSectionId() + " - " + s.getCourseCode());
            }
        } catch (SQLException e) { showError("Error loading sections"); }
    }

    private void loadSectionDetails() {
        int idx = sectionSelector.getSelectedIndex();
        if (idx == -1 || allSections == null || idx >= allSections.size()) return;

        Section s = allSections.get(idx);

        String courseItem = s.getCourseCode() + " - " + s.getCourseTitle();
        editCourseSelector.setSelectedItem(courseItem);

        editInstUsername.setText(s.getInstructorName());
        editRoom.setText(s.getRoom());
        editCap.setText(String.valueOf(s.getCapacity()));
        editSem.setText(s.getSemester());
        editYear.setText(String.valueOf(s.getYear()));

        loadScheduleToUI(s.getDayTime());
    }

    private void performUpdateSection() {
        try {
            int idx = sectionSelector.getSelectedIndex();
            if (idx == -1) return;
            int sectionId = allSections.get(idx).getSectionId();

            String selectedCourse = (String) editCourseSelector.getSelectedItem();
            if (selectedCourse == null) { showError("Select a course."); return; }
            String courseCode = selectedCourse.split(" - ")[0];

            String instUser = editInstUsername.getText();
            String rm = editRoom.getText();

            // --- VALIDATION FIX: Capacity check ---
            int cap = Integer.parseInt(editCap.getText());
            if (cap <= 0) {
                showError("Capacity must be a positive number.");
                return;
            }
            // --- END FIX ---

            String sem = editSem.getText();
            int yr = Integer.parseInt(editYear.getText());

            String dt = buildScheduleString(editCheckboxes, editStartFields, editEndFields);
            if (dt.isEmpty()) { showError("Select at least one day."); return; }

            adminService.updateSection(sectionId, courseCode, instUser, dt, rm, cap, sem, yr);
            JOptionPane.showMessageDialog(this, "Section Updated!");
            loadSectionSelector();

        } catch (NumberFormatException e) {
            showError("Invalid input for Year or Capacity (Must be a number).");
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}