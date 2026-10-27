package model;

import java.sql.Date;
import java.sql.Timestamp;

/**
 * Model class representing a registered college student.
 * Location: src/model/Student.java
 */
public class Student {
    private int id;
    private String rollNo;
    private String name;
    private String gender;
    private Date dob;
    private String department;
    private String course;
    private int year;
    private String division;
    private String email;
    private String phone;
    private String faceReference; // file path or identifier for face reference
    private Timestamp createdAt;

    public Student() {
    }

    public Student(int id, String rollNo, String name, String gender, Date dob,
                   String department, String course, int year, String division,
                   String email, String phone, String faceReference, Timestamp createdAt) {
        this.id = id;
        this.rollNo = rollNo;
        this.name = name;
        this.gender = gender;
        this.dob = dob;
        this.department = department;
        this.course = course;
        this.year = year;
        this.division = division;
        this.email = email;
        this.phone = phone;
        this.faceReference = faceReference;
        this.createdAt = createdAt;
    }

    public Student(String rollNo, String name, String gender, Date dob,
                   String department, String course, int year, String division,
                   String email, String phone, String faceReference) {
        this.rollNo = rollNo;
        this.name = name;
        this.gender = gender;
        this.dob = dob;
        this.department = department;
        this.course = course;
        this.year = year;
        this.division = division;
        this.email = email;
        this.phone = phone;
        this.faceReference = faceReference;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getRollNo() {
        return rollNo;
    }

    public void setRollNo(String rollNo) {
        this.rollNo = rollNo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Date getDob() {
        return dob;
    }

    public void setDob(Date dob) {
        this.dob = dob;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getFaceReference() {
        return faceReference;
    }

    public void setFaceReference(String faceReference) {
        this.faceReference = faceReference;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public boolean hasFaceEnrolled() {
        return faceReference != null && !faceReference.trim().isEmpty();
    }

    @Override
    public String toString() {
        return rollNo + " - " + name + " (" + department + ")";
    }
}
