package edu.univ.erp.ui.instructor;

import edu.univ.erp.auth.UserSession;
import edu.univ.erp.domain.Section;
import edu.univ.erp.service.InstructorService;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class MySectionsPanel extends JPanel {

    private JTable sectionsTable;
    private DefaultTableModel tableModel;
    private InstructorService instructorService;

    public MySectionsPanel() {
        this.instructorService = new InstructorService();
        setLayout(new BorderLayout(10, 10));

        String[] columnNames = {"Course Code", "Title", "Schedule (Day/Time)", "Room", "Semester", "Year"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        sectionsTable = new JTable(tableModel);

        // --- STYLING ---
        sectionsTable.setShowGrid(true);
        sectionsTable.setGridColor(new Color(100, 100, 100));
        sectionsTable.setIntercellSpacing(new Dimension(1, 1));
        sectionsTable.setRowHeight(60); // Tall rows for multiline time

        ((DefaultTableCellRenderer)sectionsTable.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(JLabel.CENTER);

        DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
        leftRenderer.setHorizontalAlignment(JLabel.LEFT);
        leftRenderer.setVerticalAlignment(JLabel.TOP);
        leftRenderer.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        sectionsTable.setDefaultRenderer(Object.class, leftRenderer);
        // ---------------

        JScrollPane scrollPane = new JScrollPane(sectionsTable);

        add(new JLabel("My Assigned Sections", SwingConstants.CENTER), BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        loadSectionsData();
    }

    private void loadSectionsData() {
        try {
            tableModel.setRowCount(0);
            int instructorId = UserSession.getInstance().getCurrentUser().getUserId();
            List<Section> sections = instructorService.getMySections(instructorId);

            for (Section section : sections) {
                Object[] row = {
                        section.getCourseCode(),
                        section.getCourseTitle(),
                        formatDayTimeHTML(section.getDayTime()), // Format
                        section.getRoom(),
                        section.getSemester(),
                        section.getYear()
                };
                tableModel.addRow(row);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading assigned sections: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String formatDayTimeHTML(String raw) {
        if (raw == null || raw.isEmpty()) return "";
        StringBuilder sb = new StringBuilder("<html>");
        if (raw.contains(",")) {
            String[] parts = raw.split(",");
            for (String part : parts) sb.append(part.trim()).append("<br>");
        } else {
            String[] parts = raw.split(" ", 2);
            if (parts.length < 2) sb.append(raw);
            else {
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