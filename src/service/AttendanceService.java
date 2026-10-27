package service;

import dao.AttendanceDAO;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import model.Attendance;
import model.AttendanceReport;

/**
 * Business service handling attendance verification, duplicate detection,
 * reporting, and CSV data export.
 * Location: src/service/AttendanceService.java
 */
public class AttendanceService {

    public enum AttendanceResult {
        SUCCESS,
        ALREADY_MARKED,
        FAILED
    }

    private final AttendanceDAO attendanceDAO;

    public AttendanceService() {
        this.attendanceDAO = new AttendanceDAO();
    }

    /**
     * Marks attendance automatically via Face Recognition with duplicate prevention for today.
     */
    public synchronized AttendanceResult markAttendanceViaFace(int studentId, String sessionId) throws SQLException {
        return markAttendanceViaFace(studentId, Date.valueOf(LocalDate.now()), sessionId);
    }

    /**
     * Marks attendance automatically via Face Recognition for a specific date with duplicate prevention.
     */
    public synchronized AttendanceResult markAttendanceViaFace(int studentId, Date attendanceDate, String sessionId) throws SQLException {
        Date targetDate = (attendanceDate != null) ? attendanceDate : Date.valueOf(LocalDate.now());
        String session = (sessionId != null && !sessionId.trim().isEmpty()) ? sessionId.trim() : "Morning Session";

        // Check if attendance has already been recorded for this date and session
        if (attendanceDAO.isAttendanceMarked(studentId, targetDate, session)) {
            // If already recorded, check if it was marked ABSENT; if so, update to PRESENT!
            List<Attendance> records = attendanceDAO.getAttendanceByDate(targetDate);
            for (Attendance a : records) {
                if (a.getStudentId() == studentId && session.equalsIgnoreCase(a.getSessionId())) {
                    if ("ABSENT".equalsIgnoreCase(a.getStatus())) {
                        attendanceDAO.updateAttendanceStatus(a.getId(), "PRESENT", "Face Recognition", "Face AI System");
                        return AttendanceResult.SUCCESS;
                    }
                    break;
                }
            }
            return AttendanceResult.ALREADY_MARKED;
        }

        Attendance record = new Attendance(
                studentId,
                targetDate,
                Time.valueOf(LocalTime.now()),
                "PRESENT",
                "Face Recognition",
                session,
                "Face AI System"
        );

        try {
            boolean success = attendanceDAO.markAttendance(record);
            return success ? AttendanceResult.SUCCESS : AttendanceResult.ALREADY_MARKED;
        } catch (SQLException e) {
            String msg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
            if (msg.contains("unique") || msg.contains("constraint") || msg.contains("duplicate")) {
                return AttendanceResult.ALREADY_MARKED;
            }
            throw e;
        }
    }

    /**
     * Marks attendance manually by an authorized teacher or administrator for today.
     */
    public boolean markAttendanceManually(int studentId, String status, String sessionId, String markedBy) throws SQLException {
        return markAttendanceManually(studentId, Date.valueOf(LocalDate.now()), status, sessionId, markedBy);
    }

    /**
     * Marks attendance manually by an authorized teacher or administrator for a specific date.
     */
    public boolean markAttendanceManually(int studentId, Date attendanceDate, String status, String sessionId, String markedBy) throws SQLException {
        Date targetDate = (attendanceDate != null) ? attendanceDate : Date.valueOf(LocalDate.now());
        String session = (sessionId != null && !sessionId.trim().isEmpty()) ? sessionId.trim() : "Morning Session";

        // If duplicate exists, update status, otherwise insert
        if (attendanceDAO.isAttendanceMarked(studentId, targetDate, session)) {
            // Find existing record and update status
            List<Attendance> records = attendanceDAO.getAttendanceByDate(targetDate);
            for (Attendance a : records) {
                if (a.getStudentId() == studentId && session.equalsIgnoreCase(a.getSessionId())) {
                    return attendanceDAO.updateAttendanceStatus(a.getId(), status, markedBy);
                }
            }
        }

        Attendance record = new Attendance(
                studentId,
                targetDate,
                Time.valueOf(LocalTime.now()),
                status != null ? status : "PRESENT",
                "Manual",
                session,
                markedBy != null ? markedBy : "Teacher"
        );
        boolean ok = attendanceDAO.markAttendance(record);
        if (!ok) {
            List<Attendance> records = attendanceDAO.getAttendanceByDate(targetDate);
            for (Attendance a : records) {
                if (a.getStudentId() == studentId && session.equalsIgnoreCase(a.getSessionId())) {
                    return attendanceDAO.updateAttendanceStatus(a.getId(), status, markedBy);
                }
            }
        }
        return ok;
    }

    public int[] getTodayStats() throws SQLException {
        return attendanceDAO.getTodayStats();
    }

    public List<Attendance> getTodayAttendanceRecords() throws SQLException {
        return attendanceDAO.getTodayAttendanceRecords();
    }

    public List<Attendance> filterAttendance(Date date, String department, String status, String keyword) throws SQLException {
        return attendanceDAO.filterAttendance(date, department, status, keyword);
    }

    public List<Attendance> getRecentAttendance(int limit) throws SQLException {
        return attendanceDAO.getRecentAttendance(limit);
    }

    public List<AttendanceReport> generateStudentWiseReport(Date startDate, Date endDate,
                                                           String department, Integer year) throws SQLException {
        return attendanceDAO.generateStudentWiseReport(startDate, endDate, department, year);
    }

    /**
     * Exports attendance reports to a CSV file on disk.
     */
    public boolean exportReportsToCSV(List<AttendanceReport> reports, File targetFile) {
        if (reports == null || targetFile == null) return false;

        try (PrintWriter writer = new PrintWriter(new FileWriter(targetFile))) {
            // Header
            writer.println("Roll Number,Student Name,Department,Year,Division,Total Classes,Present,Absent,Percentage");
            // Rows
            for (AttendanceReport r : reports) {
                writer.printf("\"%s\",\"%s\",\"%s\",%d,\"%s\",%d,%d,%d,%.1f%%%n",
                        r.getRollNo(),
                        r.getStudentName(),
                        r.getDepartment(),
                        r.getYear(),
                        r.getDivision(),
                        r.getTotalClasses(),
                        r.getPresentCount(),
                        r.getAbsentCount(),
                        r.getPercentage()
                );
            }
            return true;
        } catch (IOException e) {
            System.err.println("[AttendanceService] Error exporting CSV: " + e.getMessage());
            return false;
        }
    }
}
