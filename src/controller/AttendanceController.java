package controller;

import java.io.File;
import java.sql.Date;
import java.sql.SQLException;
import java.util.List;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import model.Attendance;
import model.AttendanceReport;
import service.AttendanceService;
import service.AuthenticationService;
import view.AttendanceFrame;
import view.ReportsFrame;

/**
 * Controller handling attendance view queries, manual marking overrides,
 * report filtering, and CSV export.
 * Location: src/controller/AttendanceController.java
 */
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final AuthenticationService authService;

    public AttendanceController() {
        this.attendanceService = new AttendanceService();
        this.authService = AuthenticationService.getInstance();
    }

    public void loadAttendanceRecords(AttendanceFrame frame, Date date, String department, String status, String keyword) {
        try {
            List<Attendance> list = attendanceService.filterAttendance(date, department, status, keyword);
            frame.populateTable(list);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, "Error loading attendance records: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void markManualAttendance(AttendanceFrame frame, int studentId, String status, String sessionId) {
        markManualAttendance(frame, studentId, Date.valueOf(java.time.LocalDate.now()), status, sessionId);
    }

    public void markManualAttendance(AttendanceFrame frame, int studentId, Date date, String status, String sessionId) {
        // Authorization check: Teacher or Admin can mark manually
        String userName = authService.getCurrentUser() != null ? authService.getCurrentUser().getFullName() : "Teacher";

        try {
            boolean success = attendanceService.markAttendanceManually(studentId, date, status, sessionId, userName);
            if (success) {
                JOptionPane.showMessageDialog(frame, "Attendance recorded successfully as " + status + ".", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(frame, "Failed to record attendance.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, "Database error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void loadReportData(ReportsFrame frame, Date startDate, Date endDate, String department, Integer year) {
        try {
            List<AttendanceReport> reports = attendanceService.generateStudentWiseReport(startDate, endDate, department, year);
            frame.populateReportTable(reports);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, "Error generating report: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void exportReportToCSV(ReportsFrame frame, List<AttendanceReport> reports) {
        if (reports == null || reports.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "No report data available to export.", "Export Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save Attendance Report as CSV");
        chooser.setSelectedFile(new File("Attendance_Report_" + System.currentTimeMillis() + ".csv"));
        chooser.setFileFilter(new FileNameExtensionFilter("CSV Comma Delimited (*.csv)", "csv"));

        int userSelection = chooser.showSaveDialog(frame);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = chooser.getSelectedFile();
            if (!fileToSave.getName().toLowerCase().endsWith(".csv")) {
                fileToSave = new File(fileToSave.getParentFile(), fileToSave.getName() + ".csv");
            }

            boolean ok = attendanceService.exportReportsToCSV(reports, fileToSave);
            if (ok) {
                JOptionPane.showMessageDialog(frame, "Report exported successfully to:\n" + fileToSave.getAbsolutePath(), "Export Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(frame, "Failed to export report.", "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
