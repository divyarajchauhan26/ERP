package edu.univ.erp.util;

import javax.swing.table.DefaultTableModel;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;

public class PdfExporter {

    /**
     * Creates a mock PDF (saving it as a simple TXT file with a .pdf extension
     * for demonstration purposes) of the transcript content.
     */
    public static void exportTranscriptPdf(String fileName, DefaultTableModel model, String studentName) throws IOException {
        try (FileWriter writer = new FileWriter(fileName)) {

            writer.write("==================================================\n");
            writer.write("          OFFICIAL ACADEMIC TRANSCRIPT          \n");
            writer.write("==================================================\n");
            writer.write("Student: " + studentName + "\n");
            writer.write("Date Generated: " + LocalDate.now() + "\n\n");

            // 1. Write Header
            StringBuilder header = new StringBuilder();
            for (int i = 0; i < model.getColumnCount(); i++) {
                header.append(String.format("%-15s", model.getColumnName(i)));
            }
            writer.write(header.toString().trim() + "\n");
            writer.write("--------------------------------------------------\n");

            // 2. Write Data Rows
            for (int i = 0; i < model.getRowCount(); i++) {
                StringBuilder row = new StringBuilder();
                for (int j = 0; j < model.getColumnCount(); j++) {
                    row.append(String.format("%-15s", model.getValueAt(i, j)));
                }
                writer.write(row.toString().trim() + "\n");
            }
            writer.write("\n==================================================\n");
            writer.write("This document is a preview. Final grades are subject to audit.");
        }
    }
}