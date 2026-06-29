package edu.univ.erp.ui.instructor;

import edu.univ.erp.auth.UserSession;
import edu.univ.erp.domain.Enrollment;
import edu.univ.erp.domain.Grade;
import edu.univ.erp.domain.Section;
import edu.univ.erp.domain.Student;
import edu.univ.erp.service.InstructorService;
import edu.univ.erp.service.StudentService;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.*;
import java.util.List;

// --- JFreeChart Imports ---
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
// --------------------------

public class ClassStatsPanel extends JPanel {

    private InstructorService instructorService;
    private StudentService studentService;
    private JComboBox<String> sectionSelector;

    private JPanel chartAndTextPanel; // Container for dynamic chart and summary text
    private JTextArea statsSummaryArea; // For text summary (Average, Median, Mode)

    private List<Section> mySections;

    public ClassStatsPanel() {
        this.instructorService = new InstructorService();
        this.studentService = new StudentService();
        setLayout(new BorderLayout(10, 10));

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Select Section for Stats:"));
        sectionSelector = new JComboBox<>();
        topPanel.add(sectionSelector);

        add(topPanel, BorderLayout.NORTH);

        // --- Container for the chart and text output ---
        chartAndTextPanel = new JPanel(new BorderLayout(10, 10));
        add(chartAndTextPanel, BorderLayout.CENTER);

        loadSectionSelector();
        sectionSelector.addActionListener(e -> calculateAndDisplayStats());

        // Initial placeholder text
        statsSummaryArea = new JTextArea("Select a section to view statistics.");
        statsSummaryArea.setEditable(false);
        statsSummaryArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        chartAndTextPanel.add(new JScrollPane(statsSummaryArea), BorderLayout.CENTER);
    }

    private void loadSectionSelector() {
        try {
            int instructorId = UserSession.getInstance().getCurrentUser().getUserId();
            mySections = instructorService.getMySections(instructorId);
            sectionSelector.removeAllItems();
            for (Section s : mySections) {
                sectionSelector.addItem(s.getSectionId() + ": " + s.getCourseCode());
            }
        } catch (SQLException e) {
            statsSummaryArea.setText("Error loading sections.");
        }
    }

    private void calculateAndDisplayStats() {
        int idx = sectionSelector.getSelectedIndex();
        if (idx == -1) return;
        Section sec = mySections.get(idx);

        // Clear previous content
        chartAndTextPanel.removeAll();
        statsSummaryArea.setText("Calculating comprehensive stats for " + sec.getCourseCode() + "...\n");

        try {
            List<Student> students = instructorService.getStudentsBySection(sec.getSectionId());

            if (students.isEmpty()) {
                statsSummaryArea.append("No students enrolled.");
                chartAndTextPanel.add(new JScrollPane(statsSummaryArea), BorderLayout.CENTER);
                chartAndTextPanel.revalidate();
                chartAndTextPanel.repaint();
                return;
            }

            // Storage for stats: Component Name -> List of Scores
            Map<String, List<Double>> componentScores = new HashMap<>();
            List<Double> finalTotalScores = new ArrayList<>();

            for (Student s : students) {
                // Find enrollment
                List<Enrollment> enrs = studentService.getMyEnrollments(s.getUserId());
                int enrollmentId = -1;
                for(Enrollment e : enrs) {
                    if(e.getSectionId() == sec.getSectionId()) {
                        enrollmentId = e.getEnrollmentId();
                        break;
                    }
                }

                if (enrollmentId != -1) {
                    List<Grade> grades = instructorService.getGradesForEnrollment(enrollmentId);
                    double studentWeightedTotal = 0.0;

                    for (Grade g : grades) {
                        // Add to component list
                        componentScores.putIfAbsent(g.getComponent(), new ArrayList<>());
                        componentScores.get(g.getComponent()).add(g.getScore());

                        // Calculate contribution to final grade
                        if (g.getMaxMarks() > 0) {
                            double percentage = g.getScore() / g.getMaxMarks();
                            studentWeightedTotal += (percentage * g.getWeightage());
                        }
                    }
                    finalTotalScores.add(studentWeightedTotal);
                }
            }

            // --- 1. Generate Chart Data and Chart ---
            JFreeChart chart = createComponentAverageChart(sec.getCourseCode(), componentScores);
            ChartPanel chartPanel = new ChartPanel(chart);
            chartPanel.setPreferredSize(new Dimension(500, 300));

            // --- 2. Generate Text Summary ---
            StringBuilder sb = new StringBuilder();
            sb.append("=== CLASS STATISTICS ===\n");
            sb.append("Total Students: ").append(students.size()).append("\n\n");

            // Component Stats (Textual)
            sb.append("--- Component Analysis ---\n");
            sb.append(String.format("%-15s | %-8s | %-8s\n", "Component", "Average", "Median"));
            sb.append("--------------------------------------\n");

            for (String comp : componentScores.keySet()) {
                List<Double> scores = componentScores.get(comp);
                sb.append(String.format("%-15s | %-8.2f | %-8.2f\n",
                        comp,
                        calcAverage(scores),
                        calcMedian(scores)));
            }

            // Final Overall Stats
            sb.append("\n--- Overall Final Score Analysis (Weighted %) ---\n");
            sb.append(String.format("Average Weighted Total: %.2f\n", calcAverage(finalTotalScores)));
            sb.append(String.format("Median Weighted Total:  %.2f\n", calcMedian(finalTotalScores)));

            statsSummaryArea.setText(sb.toString());

            // --- 3. Assemble Panel ---
            chartAndTextPanel.setLayout(new BorderLayout(10, 10));
            chartAndTextPanel.add(chartPanel, BorderLayout.NORTH); // Chart at the top
            chartAndTextPanel.add(new JScrollPane(statsSummaryArea), BorderLayout.CENTER); // Text below

            chartAndTextPanel.revalidate();
            chartAndTextPanel.repaint();

        } catch (SQLException e) {
            statsSummaryArea.setText("Error calculating stats: " + e.getMessage());
            chartAndTextPanel.add(new JScrollPane(statsSummaryArea), BorderLayout.CENTER);
            chartAndTextPanel.revalidate();
            chartAndTextPanel.repaint();
        }
    }

    // --- JFreeChart Helper ---
    private JFreeChart createComponentAverageChart(String courseCode, Map<String, List<Double>> componentScores) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (Map.Entry<String, List<Double>> entry : componentScores.entrySet()) {
            double average = calcAverage(entry.getValue());
            // Add the average score for this component to the dataset
            dataset.addValue(average, "Average Score", entry.getKey());
        }

        JFreeChart barChart = ChartFactory.createBarChart(
                "Grade Component Averages for " + courseCode,
                "Grade Component",
                "Average Score",
                dataset,
                PlotOrientation.VERTICAL,
                false, true, false);

        // Customize the appearance for the dark theme
        barChart.setBackgroundPaint(new Color(60, 63, 65)); // Match FlatDarkLaf background
        barChart.getTitle().setPaint(Color.WHITE);
        barChart.getPlot().setBackgroundPaint(new Color(75, 78, 80));
        barChart.getPlot().setOutlinePaint(Color.DARK_GRAY);
        barChart.getCategoryPlot().getDomainAxis().setLabelPaint(Color.WHITE);
        barChart.getCategoryPlot().getDomainAxis().setTickLabelPaint(Color.LIGHT_GRAY);
        barChart.getCategoryPlot().getRangeAxis().setLabelPaint(Color.WHITE);
        barChart.getCategoryPlot().getRangeAxis().setTickLabelPaint(Color.LIGHT_GRAY);

        return barChart;
    }


    // --- Helper Math Functions ---

    private double calcAverage(List<Double> scores) {
        if (scores == null || scores.isEmpty()) return 0.0;
        double sum = 0;
        for (double s : scores) sum += s;
        return sum / scores.size();
    }

    private double calcMedian(List<Double> scores) {
        if (scores == null || scores.isEmpty()) return 0.0;
        Collections.sort(scores);
        int middle = scores.size() / 2;
        if (scores.size() % 2 == 1) {
            return scores.get(middle);
        } else {
            return (scores.get(middle - 1) + scores.get(middle)) / 2.0;
        }
    }
}