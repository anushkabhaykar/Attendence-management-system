package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import model.FaceData;
import util.DBConnection;

/**
 * Data Access Object for face_data table.
 * Stores and retrieves biometric feature vectors.
 * Location: src/dao/FaceDataDAO.java
 */
public class FaceDataDAO {

    public boolean saveOrUpdate(FaceData faceData) throws SQLException {
        FaceData existing = findByStudentId(faceData.getStudentId());
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DBConnection.getConnection();
            if (existing == null) {
                String sql = "INSERT INTO face_data (student_id, encoding, sample_count) VALUES (?, ?, ?)";
                stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                stmt.setInt(1, faceData.getStudentId());
                stmt.setString(2, faceData.getEncoding());
                stmt.setInt(3, faceData.getSampleCount());
                int affected = stmt.executeUpdate();
                return affected > 0;
            } else {
                String sql = "UPDATE face_data SET encoding = ?, sample_count = ? WHERE student_id = ?";
                stmt = conn.prepareStatement(sql);
                stmt.setString(1, faceData.getEncoding());
                stmt.setInt(2, faceData.getSampleCount());
                stmt.setInt(3, faceData.getStudentId());
                int affected = stmt.executeUpdate();
                return affected > 0;
            }
        } finally {
            DBConnection.close(null, stmt, conn);
        }
    }

    public FaceData findByStudentId(int studentId) throws SQLException {
        String sql = "SELECT id, student_id, encoding, sample_count, created_at FROM face_data WHERE student_id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, studentId);
            rs = stmt.executeQuery();

            if (rs.next()) {
                return new FaceData(
                        rs.getInt("id"),
                        rs.getInt("student_id"),
                        rs.getString("encoding"),
                        rs.getInt("sample_count"),
                        rs.getTimestamp("created_at")
                );
            }
            return null;
        } finally {
            DBConnection.close(rs, stmt, conn);
        }
    }

    public List<FaceData> getAllFaceData() throws SQLException {
        List<FaceData> list = new ArrayList<>();
        String sql = "SELECT id, student_id, encoding, sample_count, created_at FROM face_data";
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);

            while (rs.next()) {
                list.add(new FaceData(
                        rs.getInt("id"),
                        rs.getInt("student_id"),
                        rs.getString("encoding"),
                        rs.getInt("sample_count"),
                        rs.getTimestamp("created_at")
                ));
            }
            return list;
        } finally {
            DBConnection.close(rs, stmt, conn);
        }
    }

    public boolean deleteByStudentId(int studentId) throws SQLException {
        String sql = "DELETE FROM face_data WHERE student_id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, studentId);
            return stmt.executeUpdate() > 0;
        } finally {
            DBConnection.close(null, stmt, conn);
        }
    }
}
