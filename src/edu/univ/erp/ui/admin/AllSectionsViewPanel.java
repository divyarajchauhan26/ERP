package edu.univ.erp.ui.admin;

import edu.univ.erp.service.CatalogService;
import edu.univ.erp.domain.Section;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

/**
 * Panel to view the master list of all Sections (including Course details).
 */
public class AllSectionsViewPanel extends JPanel {

    private JTable sectionTable;
    private DefaultTableModel tableModel;
    private CatalogService catalogService;

    public AllSectionsViewPanel() {
        this.catalogService = new CatalogService();
        setLayout(new BorderLayout(10, 10));

        // --- Table Setup ---
        String[] columnNames = {"Section ID", "Course Code", "Title", "Credits", "Instructor", "Day/Time", "Room", "Capacity", "Semester"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
            @Override
            public Class<?> getColumnClass(int col) {
                if(col == 0 || col == 3 || col == 7) return Integer.class; return String.class;
            }
        };
        sectionTable = new JTable(tableModel);
        sectionTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // --- STYLING ---
        sectionTable.setShowGrid(true);
        sectionTable.setGridColor(new Color(60, 60, 60));
        sectionTable.setIntercellSpacing(new Dimension(1, 1));
        sectionTable.setRowHeight(30);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        sectionTable.getTableHeader().setDefaultRenderer(centerRenderer);

        DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
        leftRenderer.setHorizontalAlignment(JLabel.LEFT);
        leftRenderer.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
        sectionTable.setDefaultRenderer(Object.class, leftRenderer);
        sectionTable.setDefaultRenderer(Integer.class, centerRenderer);
        // ---------------

        JScrollPane scrollPane = new JScrollPane(sectionTable);

        JButton refreshButton = new JButton("Refresh Section List");
        refreshButton.addActionListener(e -> loadSectionData());

        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        topPanel.add(new JLabel("Active Course Sections (All Terms)", SwingConstants.CENTER), BorderLayout.NORTH);
        topPanel.add(refreshButton, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        loadSectionData();
    }

    /**
     * Fetches all sections (which includes course details) and populates the table.
     */
    public void loadSectionData() {
        tableModel.setRowCount(0);
        try {
            List<Section> sections = catalogService.getAllSections();
            for (Section s : sections) {
                tableModel.addRow(new Object[]{
                        s.getSectionId(),
                        s.getCourseCode(),
                        s.getCourseTitle(),
                        s.getCredits(),
                        s.getInstructorName(),
                        s.getDayTime(),
                        s.getRoom(),
                        s.getCapacity(),
                        s.getSemester()
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading sections: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}