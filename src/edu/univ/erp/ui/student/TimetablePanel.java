package edu.univ.erp.ui.student;

import edu.univ.erp.auth.UserSession;
import edu.univ.erp.domain.Section;
import edu.univ.erp.service.StudentService;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class TimetablePanel extends JPanel {

    private JTable timetableTable;
    private DefaultTableModel tableModel;
    private StudentService studentService;

    public TimetablePanel() {
        this.studentService = new StudentService();
        setLayout(new BorderLayout(10, 10));

        // --- Create the Table ---
        String[] columnNames = {"Course Code", "Title", "Schedule", "Room", "Instructor"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // View-only
            }
        };
        timetableTable = new JTable(tableModel);

        // --- STYLING START ---
        // 1. Enable Grid Lines
        timetableTable.setShowGrid(true);
        timetableTable.setGridColor(new Color(100, 100, 100)); // Subtle Gray
        timetableTable.setIntercellSpacing(new Dimension(1, 1));

        // 2. Row Height (Tall for multiline)
        timetableTable.setRowHeight(60);

        // 3. Header Styling (Center Aligned)
        DefaultTableCellRenderer headerRenderer = (DefaultTableCellRenderer) timetableTable.getTableHeader().getDefaultRenderer();
        headerRenderer.setHorizontalAlignment(JLabel.CENTER);

        // 4. Data Styling (Left Aligned + Padding)
        DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
        leftRenderer.setHorizontalAlignment(JLabel.LEFT);
        leftRenderer.setVerticalAlignment(JLabel.TOP); // Text starts at top
        // Add 5px padding on the left
        leftRenderer.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Apply to all columns
        for (int i = 0; i < timetableTable.getColumnCount(); i++) {
            timetableTable.getColumnModel().getColumn(i).setCellRenderer(leftRenderer);
        }
        // --- STYLING END ---

        JScrollPane scrollPane = new JScrollPane(timetableTable);

        add(new JLabel("My Weekly Timetable", SwingConstants.CENTER), BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        // Initial Load
        refreshData();
    }

    // --- REQUIRED METHOD FOR DASHBOARD REFRESH ---
    public void refreshData() {
        loadTimetableData();
    }

    private void loadTimetableData() {
        try {
            tableModel.setRowCount(0);
            int studentId = UserSession.getInstance().getCurrentUser().getUserId();
            List<Section> sections = studentService.getMyTimetable(studentId);

            for (Section section : sections) {
                Object[] row = {
                        section.getCourseCode(),
                        section.getCourseTitle(),
                        formatDayTimeHTML(section.getDayTime()),
                        section.getRoom(),
                        section.getInstructorName()
                };
                tableModel.addRow(row);
            }
        } catch (SQLException e) {
            // Only show error if visible to avoid spam
            if (this.isShowing()) {
                JOptionPane.showMessageDialog(this, "Error loading timetable: " + e.getMessage(),
                        "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Helper to convert diverse time formats into HTML with line breaks.
     */
    private String formatDayTimeHTML(String raw) {
        if (raw == null || raw.isEmpty()) return "";

        StringBuilder sb = new StringBuilder("<html>");

        if (raw.contains(",")) {
            String[] parts = raw.split(",");
            for (String part : parts) {
                sb.append(part.trim()).append("<br>");
            }
        }
        else {
            String[] parts = raw.split(" ", 2);
            if (parts.length < 2) {
                sb.append(raw);
            } else {
                String days = parts[0];
                String time = parts[1];

                if (days.contains("M")) sb.append("Mon: ").append(time).append("<br>");
                if (days.contains("Th")) { sb.append("Thu: ").append(time).append("<br>"); days = days.replace("Th", ""); }
                if (days.contains("T")) sb.append("Tue: ").append(time).append("<br>");
                if (days.contains("W")) sb.append("Wed: ").append(time).append("<br>");
                if (days.contains("F")) sb.append("Fri: ").append(time).append("<br>");
                if (days.contains("S")) sb.append("Sat: ").append(time).append("<br>");
            }
        }

        sb.append("</html>");
        return sb.toString();
    }
}