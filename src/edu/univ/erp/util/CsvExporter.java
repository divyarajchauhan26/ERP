package edu.univ.erp.util;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class CsvExporter {

    /**
     * Exports a list of maps (where each map is a row and key is the column name)
     * to a CSV file.
     * @param fileName The name of the file to save.
     * @param data The list of data rows.
     * @param columnOrder The header order (keys to use from the map).
     * @throws IOException
     */
    public static void exportData(String fileName, List<Map<String, Object>> data, String[] columnOrder) throws IOException {
        try (FileWriter writer = new FileWriter(fileName)) {

            // 1. Write Header
            for (int i = 0; i < columnOrder.length; i++) {
                writer.write(columnOrder[i] + (i == columnOrder.length - 1 ? "" : ","));
            }
            writer.write("\n");

            // 2. Write Data Rows
            for (Map<String, Object> row : data) {
                for (int i = 0; i < columnOrder.length; i++) {
                    Object value = row.get(columnOrder[i]);
                    // Simple replacement for commas
                    String formattedValue = (value != null ? value.toString() : "").replace(",", ";");
                    writer.write(formattedValue + (i == columnOrder.length - 1 ? "" : ","));
                }
                writer.write("\n");
            }
        }
    }
}