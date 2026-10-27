package model;

import java.sql.Date;
import java.sql.Time;

/**
 * Model class representing an individual student's attendance record.
 * Location: src/model/Attendance.java
 */
public class Attendance {
    private int id;
    private int studentId;
    private Date attendanceDate;
    private Time attendanceTime;
    private String status;   // "PRESENT", "ABSENT", "LATE"
    private String method;   // "Face Recognition", "Manual"
    private String sessionId; // e.g. "Morning Session"
    private String markedBy;  // "Face AI System", "Prof. Sharma", etc.

    // Joined fields from students table for UI display convenience
    private String studentRollNo;
    private String studentName;
    private String department;
    private int year;
    private String division;

    public Attendance() {
    }

    public Attendance(int id, int studentId, Date attendanceDate, Time attendanceTime,
                      String status, String method, String sessionId, String markedBy) {
        this.id = id;
        this.studentId = studentId;
        this.attendanceDate = attendanceDate;
        this.attendanceTime = attendanceTime;
        this.status = status;
        this.method = method;
        this.sessionId = sessionId;
        this.markedBy = markedBy;
    }

    public Attendance(int studentId, Date attendanceDate, Time attendanceTime,
                      String status, String method, String sessionId, String markedBy) {
        this.studentId = studentId;
        this.attendanceDate = attendanceDate;
        this.attendanceTime = attendanceTime;
        this.status = status;
        this.method = method;
        this.sessionId = sessionId;
        this.markedBy = markedBy;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public Date getAttendanceDate() {
        return attendanceDate;
    }

    public void setAttendanceDate(Date attendanceDate) {
        this.attendanceDate = attendanceDate;
    }

    public Time getAttendanceTime() {
        return attendanceTime;
    }

    public void setAttendanceTime(Time attendanceTime) {
        this.attendanceTime = attendanceTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getMarkedBy() {
        return markedBy;
    }

    public void setMarkedBy(String markedBy) {
        this.markedBy = markedBy;
    }

    public String getStudentRollNo() {
        return studentRollNo;
    }

    public void setStudentRollNo(String studentRollNo) {
        this.studentRollNo = studentRollNo;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getDivision() {
        return division;
    }

    public void setDivision(String division) {
        this.division = division;
    }
}
