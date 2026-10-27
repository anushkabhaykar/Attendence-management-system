package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import model.Student;
import util.DBConnection;

/**
 * Data Access Object for students table.
 * Location: src/dao/StudentDAO.java
 */
public class StudentDAO {

    public boolean addStudent(Student s) throws SQLException {
        String sql = "INSERT INTO students (roll_no, name, gender, dob, department, course, year, division, email, phone, face_reference) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, s.getRollNo());
            stmt.setString(2, s.getName());
            stmt.setString(3, s.getGender());
            stmt.setString(4, s.getDob() != null ? s.getDob().toString() : "");
            stmt.setString(5, s.getDepartment());
            stmt.setString(6, s.getCourse());
            stmt.setInt(7, s.getYear());
            stmt.setString(8, s.getDivision());
            stmt.setString(9, s.getEmail());
            stmt.setString(10, s.getPhone());
            stmt.setString(11, s.getFaceReference());

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    s.setId(rs.getInt(1));
                }
                return true;
            }
            return false;
        } finally {
            DBConnection.close(rs, stmt, conn);
        }
    }

    public boolean updateStudent(Student s) throws SQLException {
        String sql = "UPDATE students SET roll_no = ?, name = ?, gender = ?, dob = ?, department = ?, "
                   + "course = ?, year = ?, division = ?, email = ?, phone = ?, face_reference = ? WHERE id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, s.getRollNo());
            stmt.setString(2, s.getName());
            stmt.setString(3, s.getGender());
            stmt.setString(4, s.getDob() != null ? s.getDob().toString() : "");
            stmt.setString(5, s.getDepartment());
            stmt.setString(6, s.getCourse());
            stmt.setInt(7, s.getYear());
            stmt.setString(8, s.getDivision());
            stmt.setString(9, s.getEmail());
            stmt.setString(10, s.getPhone());
            stmt.setString(11, s.getFaceReference());
            stmt.setInt(12, s.getId());

            return stmt.executeUpdate() > 0;
        } finally {
            DBConnection.close(null, stmt, conn);
        }
    }

    public boolean deleteStudent(int id) throws SQLException {
        String sql = "DELETE FROM students WHERE id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } finally {
            DBConnection.close(null, stmt, conn);
        }
    }

    public Student findById(int id) throws SQLException {
        String sql = "SELECT * FROM students WHERE id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);
            rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToStudent(rs);
            }
            return null;
        } finally {
            DBConnection.close(rs, stmt, conn);
        }
    }

    public Student findByRollNo(String rollNo) throws SQLException {
        String sql = "SELECT * FROM students WHERE LOWER(roll_no) = LOWER(?)";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, rollNo);
            rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToStudent(rs);
            }
            return null;
        } finally {
            DBConnection.close(rs, stmt, conn);
        }
    }

    public List<Student> getAllStudents() throws SQLException {
        List<Student> list = new ArrayList<>();
        String sql = "SELECT * FROM students ORDER BY roll_no ASC";
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);

            while (rs.next()) {
                list.add(mapResultSetToStudent(rs));
            }
            return list;
        } finally {
            DBConnection.close(rs, stmt, conn);
        }
    }

    public List<Student> searchStudents(String keyword, String department, Integer year, String division) throws SQLException {
        List<Student> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM students WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (LOWER(roll_no) LIKE ? OR LOWER(name) LIKE ? OR LOWER(email) LIKE ?) ");
            String kw = "%" + keyword.trim().toLowerCase() + "%";
            params.add(kw);
            params.add(kw);
            params.add(kw);
        }
        if (department != null && !department.trim().isEmpty() && !department.equalsIgnoreCase("All Departments")) {
            sql.append("AND department = ? ");
            params.add(department.trim());
        }
        if (year != null && year > 0) {
            sql.append("AND year = ? ");
            params.add(year);
        }
        if (division != null && !division.trim().isEmpty() && !division.equalsIgnoreCase("All Divisions")) {
            sql.append("AND division = ? ");
            params.add(division.trim());
        }

        sql.append("ORDER BY roll_no ASC");

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
                list.add(mapResultSetToStudent(rs));
            }
            return list;
        } finally {
            DBConnection.close(rs, stmt, conn);
        }
    }

    public boolean updateFaceReference(int studentId, String faceReference) throws SQLException {
        String sql = "UPDATE students SET face_reference = ? WHERE id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, faceReference);
            stmt.setInt(2, studentId);
            return stmt.executeUpdate() > 0;
        } finally {
            DBConnection.close(null, stmt, conn);
        }
    }

    public int getTotalStudentCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM students";
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } finally {
            DBConnection.close(rs, stmt, conn);
        }
    }

    private Student mapResultSetToStudent(ResultSet rs) throws SQLException {
        return new Student(
                rs.getInt("id"),
                rs.getString("roll_no"),
                rs.getString("name"),
                rs.getString("gender"),
                DBConnection.safeGetDate(rs, "dob"),
                rs.getString("department"),
                rs.getString("course"),
                rs.getInt("year"),
                rs.getString("division"),
                rs.getString("email"),
                rs.getString("phone"),
                rs.getString("face_reference"),
                rs.getTimestamp("created_at")
        );
    }
}
