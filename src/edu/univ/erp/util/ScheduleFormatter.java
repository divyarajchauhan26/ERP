package edu.univ.erp.util;

import java.util.ArrayList;
import java.util.List;

public class ScheduleFormatter {

    private static final String[] DAYS = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};

    /**
     * Parses a schedule string (supports new "Mon 10:00-10:50, Wed..." and old "MWF 10:00-10:50" formats)
     * and returns a list of DayTime objects (simple helper class assumed).
     * Since we don't have a DayTime class, we'll return a map representation for UI population.
     */
    public static List<String[]> parseScheduleToDayTimeList(String schedule) {
        List<String[]> scheduleList = new ArrayList<>();
        if (schedule == null || schedule.isEmpty()) return scheduleList;

        if (schedule.contains(",")) {
            // CASE 1: New Format (Comma Separated) -> "Mon 10:00-10:50, Wed..."
            String[] daySegments = schedule.split(",");
            for (String segment : daySegments) {
                segment = segment.trim();
                String[] parts = segment.split(" "); // ["Mon", "10:00-10:50"]
                if(parts.length < 2) continue;

                String day = parts[0];
                String timeRange = parts[1];
                String[] times = timeRange.split("-");
                String start = times.length > 0 ? times[0] : "";
                String end = times.length > 1 ? times[1] : "";
                scheduleList.add(new String[]{day, start, end});
            }
        }
        else {
            // CASE 2: Old Format -> "MWF 10:00-10:50"
            String[] parts = schedule.split(" ", 2);
            if(parts.length >= 2) {
                String days = parts[0]; // "MWF"
                String timeRange = parts[1]; // "10:00-10:50"
                String[] times = timeRange.split("-");
                String start = times.length > 0 ? times[0] : "";
                String end = times.length > 1 ? times[1] : "";

                for(String dayName : DAYS) {
                    // Check for single letter days
                    if (dayName.substring(0, 1).equals(days.substring(0, 1)) && days.contains(dayName.substring(0, 1))) {
                        scheduleList.add(new String[]{dayName, start, end});
                    }
                    // Handle "Th" separately
                    if (dayName.equals("Thu") && days.contains("Th")) {
                        scheduleList.add(new String[]{dayName, start, end});
                    }
                }
            }
        }
        return scheduleList;
    }

    /**
     * Converts the schedule components (day, start, end) into a single display string.
     */
    public static String buildScheduleString(boolean[] checks, String[] starts, String[] ends) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (int i = 0; i < DAYS.length; i++) {
            if (checks[i]) {
                if (!first) sb.append(", ");
                sb.append(DAYS[i]).append(" ").append(starts[i])
                        .append("-").append(ends[i]);
                first = false;
            }
        }
        return sb.toString();
    }
}