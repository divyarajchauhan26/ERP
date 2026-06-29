package edu.univ.erp.ui.student;

import edu.univ.erp.auth.UserSession;
import edu.univ.erp.domain.Enrollment;
import edu.univ.erp.domain.Grade;
import edu.univ.erp.domain.Section;
import edu.univ.erp.service.StudentService;
import edu.univ.erp.util.CsvExporter; // <--- NEW IMPORT
import edu.univ.erp.util.PdfExporter; // <--- NEW IMPORT

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

public class TranscriptPanel extends JPanel {

    private JTable transcriptTable;
    private DefaultTableModel tableModel;
    private JButton downloadButton;
    private JButton downloadPdfButton; // <--- NEW BUTTON
    private StudentService studentService;

    public TranscriptPanel() {
        this.studentService = new StudentService();
        setLayout(new BorderLayout(10, 10));

        // --- 1. Header ---
        JLabel headerLabel = new JLabel("Official Academic Transcript Preview", SwingConstants.CENTER);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 16));
        add(headerLabel, BorderLayout.NORTH);

        // --- 2. The Table ---
        String[] columnNames = {"Course Code", "Course Title", "Credits", "Semester", "Year", "Final Grade"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        transcriptTable = new JTable(tableModel);
        transcriptTable.setFillsViewportHeight(true);

        // --- STYLING ---
        transcriptTable.setShowGrid(true);
        transcriptTable.setGridColor(new Color(100, 100, 100));
        transcriptTable.setIntercellSpacing(new Dimension(1, 1));
        transcriptTable.setRowHeight(35);

        ((DefaultTableCellRenderer)transcriptTable.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(JLabel.CENTER);

        DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
        leftRenderer.setHorizontalAlignment(JLabel.LEFT);
        leftRenderer.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
        transcriptTable.setDefaultRenderer(Object.class, leftRenderer);
        // --- STYLING END ---

        JScrollPane scrollPane = new JScrollPane(transcriptTable);
        add(scrollPane, BorderLayout.CENTER);

        // --- 3. Download Buttons ---
        downloadButton = new JButton("Download Transcript (CSV)");
        downloadPdfButton = new JButton("Download Transcript (PDF)");

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.add(downloadButton);
        bottomPanel.add(downloadPdfButton);
        add(bottomPanel, BorderLayout.SOUTH);

        // --- Actions ---
        downloadButton.addActionListener(e -> generateTranscriptFile(true)); // CSV
        downloadPdfButton.addActionListener(e -> generateTranscriptFile(false)); // PDF

        refreshData();
    }

    public void refreshData() {
        loadTranscriptData();
    }

    private void loadTranscriptData() {
        try {
            tableModel.setRowCount(0);
            int studentId = UserSession.getInstance().getCurrentUser().getUserId();

            List<Enrollment> enrollments = studentService.getMyEnrollments(studentId);
            List<Section> sections = studentService.getMyTimetable(studentId);

            for (Enrollment enrollment : enrollments) {
                Section matchingSection = null;
                for (Section sec : sections) {
                    if (sec.getSectionId() == enrollment.getSectionId()) {
                        matchingSection = sec;
                        break;
                    }
                }

                if (matchingSection != null) {
                    String finalGrade = "Pending";
                    List<Grade> grades = studentService.getGrades(enrollment.getEnrollmentId());

                    for (Grade g : grades) {
                        if (g.getFinalGrade() != null && !g.getFinalGrade().isEmpty()) {
                            finalGrade = g.getFinalGrade();
                            break;
                        }
                    }

                    Object[] row = {
                            matchingSection.getCourseCode(),
                            matchingSection.getCourseTitle(),
                            matchingSection.getCredits(),
                            matchingSection.getSemester(),
                            matchingSection.getYear(),
                            finalGrade
                    };
                    tableModel.addRow(row);
                }
            }
        } catch (SQLException e) {
            if(this.isShowing()) {
                JOptionPane.showMessageDialog(this, "Error loading transcript: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void generateTranscriptFile(boolean isCSV) {
        String format = isCSV ? "CSV" : "PDF";
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Transcript as " + format);
        String studentName = UserSession.getInstance().getCurrentUser().getUsername();

        File defaultFile = new File(studentName + "_transcript" + (isCSV ? ".csv" : ".pdf"));
        fileChooser.setSelectedFile(defaultFile);

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();

            try {
                if (isCSV) {
                    // Prepare data for CsvExporter
                    List<Map<String, Object>> csvData = new ArrayList<>();
                    String[] columnNames = {"Course Code", "Course Title", "Credits", "Semester", "Year", "Final Grade"};

                    for (int i = 0; i < tableModel.getRowCount(); i++) {
                        Map<String, Object> row = new HashMap<>();
                        for (int j = 0; j < tableModel.getColumnCount(); j++) {
                            row.put(columnNames[j], tableModel.getValueAt(i, j));
                        }
                        csvData.add(row);
                    }

                    CsvExporter.exportData(file.getAbsolutePath(), csvData, columnNames);

                } else {
                    // PDF (Mock TXT) Export using PdfExporter
                    PdfExporter.exportTranscriptPdf(file.getAbsolutePath(), tableModel, studentName);
                }

                JOptionPane.showMessageDialog(this, "Transcript saved successfully as " + format + "!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error saving file: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}