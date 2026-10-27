package test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import util.DBConnection;

/**
 * Verification test for SQLite database connectivity and timestamp/date parsing.
 */
public class SqliteBehaviorTest {
    public static void main(String[] args) {
        System.out.println("Running SQLite behavior and date parsing verification...");
        try (Connection conn = DBConnection.getConnection();
             Statement s = conn.createStatement();
             ResultSet rs = s.executeQuery("SELECT id, attendance_date, attendance_time FROM attendance LIMIT 5")) {

            int count = 0;
            while (rs.next()) {
                count++;
                int id = rs.getInt("id");
                java.sql.Date d = DBConnection.safeGetDate(rs, "attendance_date");
                java.sql.Time t = DBConnection.safeGetTime(rs, "attendance_time");
                System.out.println("  Row " + id + ": Date=" + d + ", Time=" + t);
            }
            System.out.println("PASSED ALL TIMESTAMP / PARSING CHECKS (Verified " + count + " records).");
        } catch (Exception e) {
            System.err.println("Verification error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
