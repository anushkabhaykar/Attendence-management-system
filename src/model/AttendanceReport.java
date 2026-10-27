package model;

/**
 * Model class for aggregated attendance statistics and reports.
 * Location: src/model/AttendanceReport.java
 */
public class AttendanceReport {
    private String rollNo;
    private String studentName;
    private String department;
    private int year;
    private String division;
    private int totalClasses;
    private int presentCount;
    private int absentCount;
    private double percentage;

    public AttendanceReport() {
    }

    public AttendanceReport(String rollNo, String studentName, String department,
                            int year, String division, int totalClasses,
                            int presentCount, int absentCount, double percentage) {
        this.rollNo = rollNo;
        this.studentName = studentName;
        this.department = department;
        this.year = year;
        this.division = division;
        this.totalClasses = totalClasses;
        this.presentCount = presentCount;
        this.absentCount = absentCount;
        this.percentage = percentage;
    }

    public String getRollNo() {
        return rollNo;
    }

    public void setRollNo(String rollNo) {
        this.rollNo = rollNo;
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

    public int getTotalClasses() {
        return totalClasses;
    }

    public void setTotalClasses(int totalClasses) {
        this.totalClasses = totalClasses;
    }

    public int getPresentCount() {
        return presentCount;
    }

    public void setPresentCount(int presentCount) {
        this.presentCount = presentCount;
    }

    public int getAbsentCount() {
        return absentCount;
    }

    public void setAbsentCount(int absentCount) {
        this.absentCount = absentCount;
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }
}
