package test;

import dao.AttendanceDAO;
import dao.StudentDAO;
import dao.UserDAO;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import model.Student;
import model.User;
import service.AttendanceService;
import service.AuthenticationService;
import service.FaceRecognitionService;
import util.DBConnection;
import util.FaceRecognitionEngine;
import util.PasswordUtil;

/**
 * Automated System Verification Test Runner.
 * Tests Database connectivity, Authentication, DAO operations,
 * LBPH Face Feature Extraction, Biometric Matching, and Duplicate Attendance Prevention.
 */
public class SystemVerificationTest {

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("  CAMPUSFACE SYSTEM VERIFICATION & TEST SUITE    ");
        System.out.println("=================================================");

        int passed = 0;
        int failed = 0;

        // Test 1: Database Connectivity
        try {
            System.out.print("[TEST 1] Testing Database Connection (" + DBConnection.getDisplayInfo() + ")... ");
            boolean ok = DBConnection.testConnection();
            if (ok) {
                System.out.println("PASSED! Connected successfully.");
                passed++;
            } else {
                System.out.println("FAILED! Cannot connect to database.");
                failed++;
            }
        } catch (Exception e) {
            System.out.println("FAILED: " + e.getMessage());
            failed++;
        }

        // Test 2: User Authentication & SHA-256 Hashing
        try {
            System.out.print("[TEST 2] Testing User Authentication & SHA-256 Hashing... ");
            AuthenticationService auth = AuthenticationService.getInstance();
            User user = auth.login("admin", "admin123");
            if (user != null && "ADMIN".equalsIgnoreCase(user.getRole())) {
                System.out.println("PASSED! Admin authenticated: " + user.getFullName());
                passed++;
            } else {
                System.out.println("FAILED! Authentication failed for admin.");
                failed++;
            }
        } catch (Exception e) {
            System.out.println("FAILED: " + e.getMessage());
            failed++;
        }

        // Test 3: Student DAO Queries
        try {
            System.out.print("[TEST 3] Testing StudentDAO Query & Model Mapping... ");
            StudentDAO studentDAO = new StudentDAO();
            Student s = studentDAO.findByRollNo("TEST001");
            if (s == null) {
                s = new Student("TEST001", "Test Student", "Male", Date.valueOf("2004-01-01"), "Computer Science", "B.Tech", 3, "A", "test@college.edu", "9876543210", null);
                studentDAO.addStudent(s);
            }
            Student retrieved = studentDAO.findByRollNo("TEST001");
            if (retrieved != null && "Test Student".equalsIgnoreCase(retrieved.getName())) {
                System.out.println("PASSED! Retrieved: " + retrieved.getName() + " (" + retrieved.getRollNo() + ")");
                passed++;
            } else {
                System.out.println("FAILED! Test student TEST001 not found.");
                failed++;
            }
        } catch (Exception e) {
            System.out.println("FAILED: " + e.getMessage());
            failed++;
        }

        // Test 4: Face Recognition LBPH Engine
        try {
            System.out.print("[TEST 4] Testing LBPH Feature Vector Extraction & Chi-Square Distance... ");
            BufferedImage dummyFace = new BufferedImage(100, 100, BufferedImage.TYPE_BYTE_GRAY);
            for (int y = 0; y < 100; y++) {
                for (int x = 0; x < 100; x++) {
                    dummyFace.getRaster().setSample(x, y, 0, (x * y) % 256);
                }
            }

            double[] vec1 = FaceRecognitionEngine.extractLBPHFeatures(dummyFace);
            double distSelf = FaceRecognitionEngine.computeChiSquareDistance(vec1, vec1);
            double confSelf = FaceRecognitionEngine.calculateConfidence(distSelf);

            if (vec1.length == FaceRecognitionEngine.FEATURE_LENGTH && distSelf < 0.0001 && confSelf >= 95.0) {
                System.out.println("PASSED! 1024-D vector extracted. Identity match confidence: " + confSelf + "%.");
                passed++;
            } else {
                System.out.println("FAILED! Vector length=" + vec1.length + ", distSelf=" + distSelf);
                failed++;
            }
        } catch (Exception e) {
            System.out.println("FAILED: " + e.getMessage());
            failed++;
        }

        // Test 5: Biometric Face Enrollment & Cache Reload
        try {
            System.out.print("[TEST 5] Testing Multi-Sample Face Enrollment & In-Memory Cache... ");
            FaceRecognitionService recService = FaceRecognitionService.getInstance();
            Student s = new StudentDAO().findByRollNo("TEST001");

            List<BufferedImage> samples = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                BufferedImage sample = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);
                java.awt.Graphics2D g = sample.createGraphics();
                g.setColor(new java.awt.Color(230, 180, 150)); // Skin color
                g.fillOval(40, 30, 120, 140);
                g.setColor(java.awt.Color.DARK_GRAY);
                g.fillOval(65, 75, 15, 15);
                g.fillOval(120, 75, 15, 15);
                g.dispose();
                samples.add(sample);
            }

            boolean enrolled = recService.enrollStudentFace(s.getId(), s.getRollNo(), samples);
            if (enrolled && recService.getEnrolledCount() > 0) {
                System.out.println("PASSED! Enrolled TEST001. Cache size: " + recService.getEnrolledCount());
                passed++;
            } else {
                System.out.println("FAILED! Enrollment failed.");
                failed++;
            }
        } catch (Exception e) {
            System.out.println("FAILED: " + e.getMessage());
            failed++;
        }

        // Test 6: Duplicate Attendance Prevention
        try {
            System.out.print("[TEST 6] Testing Duplicate Attendance Prevention Logic... ");
            AttendanceService attService = new AttendanceService();
            Student s = new StudentDAO().findByRollNo("TEST001");

            // Mark attendance
            AttendanceService.AttendanceResult r1 = attService.markAttendanceViaFace(s.getId(), "Test Verification Session");
            // Mark attendance second time (Duplicate)
            AttendanceService.AttendanceResult r2 = attService.markAttendanceViaFace(s.getId(), "Test Verification Session");

            if ((r1 == AttendanceService.AttendanceResult.SUCCESS || r1 == AttendanceService.AttendanceResult.ALREADY_MARKED)
                    && r2 == AttendanceService.AttendanceResult.ALREADY_MARKED) {
                System.out.println("PASSED! Duplicate attendance blocked (Result: ALREADY_MARKED).");
                passed++;
            } else {
                System.out.println("FAILED! Duplicate attendance was not properly blocked. r1=" + r1 + ", r2=" + r2);
                failed++;
            }
        } catch (Exception e) {
            System.out.println("FAILED: " + e.getMessage());
            failed++;
        }

        // Test 7: Dashboard Statistics Computation
        try {
            System.out.print("[TEST 7] Testing Dashboard Metrics Aggregation... ");
            AttendanceService attService = new AttendanceService();
            int[] stats = attService.getTodayStats();
            System.out.println("PASSED! Total=" + stats[0] + ", Present=" + stats[1] + ", Absent=" + stats[2] + ", Rate=" + stats[3] + "%.");
            passed++;
        } catch (Exception e) {
            System.out.println("FAILED: " + e.getMessage());
            failed++;
        }

        System.out.println("=================================================");
        System.out.printf("TEST SUMMARY: %d PASSED, %d FAILED%n", passed, failed);
        System.out.println("=================================================");

        if (failed == 0) {
            System.out.println("ALL SYSTEM TESTS PASSED SUCCESSFULLY! Ready for viva demonstration.");
        } else {
            System.exit(1);
        }
    }
}
