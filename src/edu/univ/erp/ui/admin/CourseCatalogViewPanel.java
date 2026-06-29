package edu.univ.erp.ui.admin;

import edu.univ.erp.data.CourseDAO;
import edu.univ.erp.data.CourseDAOImpl;
import edu.univ.erp.domain.Course;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

/**
 * Panel to view the master list of all Courses (Code, Title, Credits).
 */
public class CourseCatalogViewPanel extends JPanel {

    private JTable courseTable;
    private DefaultTableModel tableModel;
    private CourseDAO courseDAO;

    public CourseCatalogViewPanel() {
        this.courseDAO = new CourseDAOImpl();
        setLayout(new BorderLayout(10, 10));

        // --- Table Setup ---
        String[] columnNames = {"Course Code", "Title", "Credits"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
            @Override
            public Class<?> getColumnClass(int col) {
                if(col == 2) return Integer.class; return String.class;
            }
        };
        courseTable = new JTable(tableModel);
        courseTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // --- STYLING ---
        courseTable.setShowGrid(true);
        courseTable.setGridColor(new Color(60, 60, 60));
        courseTable.setIntercellSpacing(new Dimension(1, 1));
        courseTable.setRowHeight(30);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        courseTable.getTableHeader().setDefaultRenderer(centerRenderer);

        DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
        leftRenderer.setHorizontalAlignment(JLabel.LEFT);
        leftRenderer.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
        courseTable.setDefaultRenderer(Object.class, leftRenderer);
        courseTable.setDefaultRenderer(Integer.class, centerRenderer);
        // ---------------

        JScrollPane scrollPane = new JScrollPane(courseTable);

        JButton refreshButton = new JButton("Refresh Course List");
        refreshButton.addActionListener(e -> loadCourseData());

        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        topPanel.add(new JLabel("Master Course Catalog", SwingConstants.CENTER), BorderLayout.NORTH);
        topPanel.add(refreshButton, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        loadCourseData();
    }

    /**
     * Fetches all courses from the database and populates the table.
     */
    public void loadCourseData() {
        tableModel.setRowCount(0);
        try {
            List<Course> courses = courseDAO.getAllCourses();
            for (Course c : courses) {
                tableModel.addRow(new Object[]{
                        c.getCode(),
                        c.getTitle(),
                        c.getCredits()
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading courses: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}