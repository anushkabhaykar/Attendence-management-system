package controller;

import dao.StudentDAO;
import java.sql.Date;
import java.sql.SQLException;
import java.util.List;
import javax.swing.JOptionPane;
import model.Student;
import service.AuthenticationService;
import util.ValidationUtil;
import view.FaceEnrollmentFrame;
import view.StudentFrame;

/**
 * Controller handling student administration actions, input validation,
 * and biometric enrollment initiation.
 * Location: src/controller/StudentController.java
 */
public class StudentController {

    private final StudentFrame view;
    private final StudentDAO studentDAO;
    private final AuthenticationService authService;

    public StudentController(StudentFrame view) {
        this.view = view;
        this.studentDAO = new StudentDAO();
        this.authService = AuthenticationService.getInstance();
    }

    public void loadStudents() {
        try {
            List<Student> students = studentDAO.getAllStudents();
            view.populateTable(students);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(view, "Error loading students: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void searchStudents(String keyword, String department, Integer year, String division) {
        try {
            List<Student> list = studentDAO.searchStudents(keyword, department, year, division);
            view.populateTable(list);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(view, "Search failed: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void addStudent(String rollNo, String name, String gender, String dobStr,
                           String department, String course, int year, String division,
                           String email, String phone) {
        // Validation
        if (!validateInputs(rollNo, name, dobStr, email, phone, year, division)) return;

        Date dob = ValidationUtil.parseSqlDate(dobStr);

        try {
            // Check for duplicate roll number
            if (studentDAO.findByRollNo(rollNo) != null) {
                JOptionPane.showMessageDialog(view, "Roll Number '" + rollNo + "' is already registered.", "Duplicate Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Student s = new Student(rollNo.trim().toUpperCase(), name.trim(), gender, dob,
                    department, course, year, division.trim().toUpperCase(), email.trim(), phone.trim(), null);

            boolean success = studentDAO.addStudent(s);
            if (success) {
                view.clearForm();
                loadStudents();
                Student created = studentDAO.findByRollNo(rollNo.trim().toUpperCase());
                if (created != null) {
                    int opt = JOptionPane.showConfirmDialog(view,
                            "Student '" + created.getName() + " (" + created.getRollNo() + ")' registered successfully!\n\n"
                          + "Would you like to open Face Biometric Enrollment now to capture their face?",
                            "Registration Complete", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                    if (opt == JOptionPane.YES_OPTION) {
                        new FaceEnrollmentFrame(created).setVisible(true);
                    }
                }
            } else {
                JOptionPane.showMessageDialog(view, "Failed to register student.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(view, "Database error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void updateStudent(int id, String rollNo, String name, String gender, String dobStr,
                              String department, String course, int year, String division,
                              String email, String phone) {
        if (id <= 0) {
            JOptionPane.showMessageDialog(view, "Please select a student from the table to update.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!validateInputs(rollNo, name, dobStr, email, phone, year, division)) return;

        Date dob = ValidationUtil.parseSqlDate(dobStr);

        try {
            Student existing = studentDAO.findById(id);
            if (existing == null) {
                JOptionPane.showMessageDialog(view, "Student not found in database.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Check if roll number changed and conflicts
            if (!existing.getRollNo().equalsIgnoreCase(rollNo)) {
                Student dup = studentDAO.findByRollNo(rollNo);
                if (dup != null && dup.getId() != id) {
                    JOptionPane.showMessageDialog(view, "Roll Number '" + rollNo + "' is already used by another student.", "Duplicate Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }

            Student s = new Student(id, rollNo.trim().toUpperCase(), name.trim(), gender, dob,
                    department, course, year, division.trim().toUpperCase(), email.trim(), phone.trim(), existing.getFaceReference(), null);

            boolean success = studentDAO.updateStudent(s);
            if (success) {
                JOptionPane.showMessageDialog(view, "Student details updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                view.clearForm();
                loadStudents();
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(view, "Database error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void deleteStudent(int id) {
        if (id <= 0) {
            JOptionPane.showMessageDialog(view, "Please select a student to delete.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Authorization check: Only ADMIN can delete students
        if (!authService.isAdmin()) {
            JOptionPane.showMessageDialog(view, "Access Denied: Only Administrators are permitted to delete students.", "Authorization Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(view,
                "Are you sure you want to permanently delete this student?\nAll attendance and biometric data will also be deleted.",
                "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean success = studentDAO.deleteStudent(id);
                if (success) {
                    JOptionPane.showMessageDialog(view, "Student deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    view.clearForm();
                    loadStudents();
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(view, "Error deleting student: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void openFaceEnrollment(int studentId) {
        if (studentId <= 0) {
            JOptionPane.showMessageDialog(view, "Please select a student from the table first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Student student = studentDAO.findById(studentId);
            if (student == null) {
                JOptionPane.showMessageDialog(view, "Student record not found.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            FaceEnrollmentFrame enrollmentFrame = new FaceEnrollmentFrame(student);
            enrollmentFrame.setVisible(true);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(view, "Database error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean validateInputs(String rollNo, String name, String dobStr, String email, String phone, int year, String division) {
        if (ValidationUtil.isEmpty(rollNo) || !ValidationUtil.isValidRollNo(rollNo)) {
            JOptionPane.showMessageDialog(view, "Please enter a valid Roll Number (e.g., CS202601).", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (ValidationUtil.isEmpty(name) || name.trim().length() < 2) {
            JOptionPane.showMessageDialog(view, "Please enter the student's full name.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (ValidationUtil.parseSqlDate(dobStr) == null) {
            JOptionPane.showMessageDialog(view, "Please enter a valid Date of Birth in YYYY-MM-DD format.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (!ValidationUtil.isValidYear(year)) {
            JOptionPane.showMessageDialog(view, "Please enter an academic year between 1 and 6.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (!ValidationUtil.isValidDivision(division)) {
            JOptionPane.showMessageDialog(view, "Please enter a valid Division (e.g., A, B).", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (!ValidationUtil.isValidEmail(email)) {
            JOptionPane.showMessageDialog(view, "Please enter a valid email address.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (!ValidationUtil.isValidPhone(phone)) {
            JOptionPane.showMessageDialog(view, "Please enter a valid 10-15 digit phone number.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }
}
