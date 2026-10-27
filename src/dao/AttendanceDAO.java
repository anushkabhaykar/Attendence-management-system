package dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import model.Attendance;
import model.AttendanceReport;
import util.DBConnection;

/**
 * Data Access Object for attendance table.
 * Location: src/dao/AttendanceDAO.java
 */
public class AttendanceDAO {

    public boolean markAttendance(Attendance a) throws SQLException {
        String sql = "INSERT INTO attendance (student_id, attendance_date, attendance_time, status, method, session_id, marked_by) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setInt(1, a.getStudentId());
            stmt.setString(2, a.getAttendanceDate() != null ? a.getAttendanceDate().toString() : LocalDate.now().toString());
            stmt.setString(3, a.getAttendanceTime() != null ? a.getAttendanceTime().toString() : LocalTime.now().toString().substring(0, 8));
            stmt.setString(4, a.getStatus() != null ? a.getStatus() : "PRESENT");
            stmt.setString(5, a.getMethod() != null ? a.getMethod() : "Face Recognition");
            stmt.setString(6, a.getSessionId() != null ? a.getSessionId().trim() : "Morning Session");
            stmt.setString(7, a.getMarkedBy() != null ? a.getMarkedBy() : "Face AI System");

            int affected = stmt.executeUpdate();
            return affected > 0;
        } catch (SQLException e) {
            String msg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
            if (msg.contains("unique") || msg.contains("constraint") || msg.contains("duplicate")) {
                // Record already exists for this student, date, and session
                return false;
            }
            throw e;
        } finally {
            DBConnection.close(null, stmt, conn);
        }
    }

    public boolean isAttendanceMarked(int studentId, Date date, String sessionId) throws SQLException {
        String sql = "SELECT id FROM attendance WHERE student_id = ? AND attendance_date = ? AND LOWER(TRIM(session_id)) = LOWER(TRIM(?))";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, studentId);
            stmt.setString(2, date != null ? date.toString() : LocalDate.now().toString());
            stmt.setString(3, sessionId != null ? sessionId.trim() : "Morning Session");
            rs = stmt.executeQuery();
            return rs.next();
        } finally {
            DBConnection.close(rs, stmt, conn);
        }
    }

    public List<Attendance> getTodayAttendanceRecords() throws SQLException {
        Date today = Date.valueOf(LocalDate.now());
        return getAttendanceByDate(today);
    }

    public List<Attendance> getAttendanceByDate(Date date) throws SQLException {
        return filterAttendance(date, null, null, null);
    }

    public List<Attendance> filterAttendance(Date date, String department, String status, String keyword) throws SQLException {
        List<Attendance> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT a.id, a.student_id, a.attendance_date, a.attendance_time, a.status, a.method, a.session_id, a.marked_by, "
              + "s.roll_no, s.name AS student_name, s.department, s.year, s.division "
              + "FROM attendance a "
              + "JOIN students s ON a.student_id = s.id "
              + "WHERE 1=1 "
        );
        List<Object> params = new ArrayList<>();

        if (date != null) {
            sql.append("AND a.attendance_date = ? ");
            params.add(date.toString());
        }
        if (department != null && !department.trim().isEmpty() && !department.equalsIgnoreCase("All Departments")) {
            sql.append("AND s.department = ? ");
            params.add(department.trim());
        }
        if (status != null && !status.trim().isEmpty() && !status.equalsIgnoreCase("All Status")) {
            sql.append("AND a.status = ? ");
            params.add(status.trim());
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (LOWER(s.roll_no) LIKE ? OR LOWER(s.name) LIKE ?) ");
            String kw = "%" + keyword.trim().toLowerCase() + "%";
            params.add(kw);
            params.add(kw);
        }

        sql.append("ORDER BY a.attendance_date DESC, a.attendance_time DESC");

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql.toString());
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }
            rs = stmt.executeQuery();

            while (rs.next()) {
                Attendance a = new Attendance(
                        rs.getInt("id"),
                        rs.getInt("student_id"),
                        DBConnection.safeGetDate(rs, "attendance_date"),
                        DBConnection.safeGetTime(rs, "attendance_time"),
                        rs.getString("status"),
                        rs.getString("method"),
                        rs.getString("session_id"),
                        rs.getString("marked_by")
                );
                a.setStudentRollNo(rs.getString("roll_no"));
                a.setStudentName(rs.getString("student_name"));
                a.setDepartment(rs.getString("department"));
                a.setYear(rs.getInt("year"));
                a.setDivision(rs.getString("division"));
                list.add(a);
            }
            return list;
        } finally {
            DBConnection.close(rs, stmt, conn);
        }
    }

    public List<Attendance> getRecentAttendance(int limit) throws SQLException {
        List<Attendance> list = new ArrayList<>();
        String sql = "SELECT a.id, a.student_id, a.attendance_date, a.attendance_time, a.status, a.method, a.session_id, a.marked_by, "
                   + "s.roll_no, s.name AS student_name, s.department, s.year, s.division "
                   + "FROM attendance a "
                   + "JOIN students s ON a.student_id = s.id "
                   + "ORDER BY a.attendance_date DESC, a.attendance_time DESC LIMIT ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, limit);
            rs = stmt.executeQuery();

            while (rs.next()) {
                Attendance a = new Attendance(
                        rs.getInt("id"),
                        rs.getInt("student_id"),
                        DBConnection.safeGetDate(rs, "attendance_date"),
                        DBConnection.safeGetTime(rs, "attendance_time"),
                        rs.getString("status"),
                        rs.getString("method"),
                        rs.getString("session_id"),
                        rs.getString("marked_by")
                );
                a.setStudentRollNo(rs.getString("roll_no"));
                a.setStudentName(rs.getString("student_name"));
                a.setDepartment(rs.getString("department"));
                a.setYear(rs.getInt("year"));
                a.setDivision(rs.getString("division"));
                list.add(a);
            }
            return list;
        } finally {
            DBConnection.close(rs, stmt, conn);
        }
    }

    public boolean updateAttendanceStatus(int id, String newStatus, String markedBy) throws SQLException {
        return updateAttendanceStatus(id, newStatus, "Manual", markedBy);
    }

    public boolean updateAttendanceStatus(int id, String newStatus, String method, String markedBy) throws SQLException {
        String sql = "UPDATE attendance SET status = ?, method = ?, marked_by = ?, attendance_time = ? WHERE id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, newStatus);
            stmt.setString(2, method != null ? method : "Face Recognition");
            stmt.setString(3, markedBy);
            stmt.setString(4, Time.valueOf(LocalTime.now()).toString());
            stmt.setInt(5, id);
            return stmt.executeUpdate() > 0;
        } finally {
            DBConnection.close(null, stmt, conn);
        }
    }

    /**
     * Calculates statistics for today:
     * index 0: Total Registered Students
     * index 1: Present Count Today
     * index 2: Absent Count Today
     * index 3: Today's Attendance Percentage (as integer 0-100)
     */
    public int[] getTodayStats() throws SQLException {
        int[] stats = new int[4];
        Date today = Date.valueOf(LocalDate.now());

        // 1. Total students
        String totalSql = "SELECT COUNT(*) FROM students";
        // 2. Present today
        String presentSql = "SELECT COUNT(DISTINCT student_id) FROM attendance WHERE attendance_date = ? AND status = 'PRESENT'";
        // 3. Marked absent today
        String absentSql = "SELECT COUNT(DISTINCT student_id) FROM attendance WHERE attendance_date = ? AND status = 'ABSENT'";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();

            stmt = conn.prepareStatement(totalSql);
            rs = stmt.executeQuery();
            if (rs.next()) {
                stats[0] = rs.getInt(1);
            }
            rs.close();
            stmt.close();

            stmt = conn.prepareStatement(presentSql);
            stmt.setString(1, today.toString());
            rs = stmt.executeQuery();
            if (rs.next()) {
                stats[1] = rs.getInt(1);
            }
            rs.close();
            stmt.close();

            stmt = conn.prepareStatement(absentSql);
            stmt.setString(1, today.toString());
            rs = stmt.executeQuery();
            if (rs.next()) {
                stats[2] = rs.getInt(1);
            }

            // If some students haven't checked in yet, absent can be: total - present
            if (stats[0] > 0) {
                stats[2] = Math.max(stats[2], stats[0] - stats[1]);
                stats[3] = (int) Math.round(((double) stats[1] / stats[0]) * 100.0);
            } else {
                stats[2] = 0;
                stats[3] = 0;
            }

            return stats;
        } finally {
            DBConnection.close(rs, stmt, conn);
        }
    }

    /**
     * Generates aggregated student-wise attendance reports.
     */
    public List<AttendanceReport> generateStudentWiseReport(Date startDate, Date endDate,
                                                           String department, Integer year) throws SQLException {
        List<AttendanceReport> reports = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT s.roll_no, s.name, s.department, s.year, s.division, "
              + "COUNT(a.id) AS total_recorded, "
              + "SUM(CASE WHEN a.status = 'PRESENT' THEN 1 ELSE 0 END) AS present_count, "
              + "SUM(CASE WHEN a.status = 'ABSENT' THEN 1 ELSE 0 END) AS absent_count "
              + "FROM students s "
              + "LEFT JOIN attendance a ON s.id = a.student_id "
        );
        List<Object> params = new ArrayList<>();

        if (startDate != null && endDate != null) {
            sql.append("AND a.attendance_date BETWEEN ? AND ? ");
            params.add(startDate.toString());
            params.add(endDate.toString());
        } else if (startDate != null) {
            sql.append("AND a.attendance_date >= ? ");
            params.add(startDate.toString());
        } else if (endDate != null) {
            sql.append("AND a.attendance_date <= ? ");
            params.add(endDate.toString());
        }

        sql.append("WHERE 1=1 ");

        if (department != null && !department.trim().isEmpty() && !department.equalsIgnoreCase("All Departments")) {
            sql.append("AND s.department = ? ");
            params.add(department.trim());
        }
        if (year != null && year > 0) {
            sql.append("AND s.year = ? ");
            params.add(year);
        }

        sql.append("GROUP BY s.id, s.roll_no, s.name, s.department, s.year, s.division ORDER BY s.roll_no ASC");

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql.toString());
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }
            rs = stmt.executeQuery();

            while (rs.next()) {
                String rollNo = rs.getString("roll_no");
                String name = rs.getString("name");
                String dept = rs.getString("department");
                int yr = rs.getInt("year");
                String div = rs.getString("division");
                int total = rs.getInt("total_recorded");
                int present = rs.getInt("present_count");
                int absent = rs.getInt("absent_count");

                double pct = total > 0 ? (present * 100.0) / total : 0.0;
                pct = Math.round(pct * 10.0) / 10.0;

                reports.add(new AttendanceReport(rollNo, name, dept, yr, div, total, present, absent, pct));
            }
            return reports;
        } finally {
            DBConnection.close(rs, stmt, conn);
        }
    }
}
