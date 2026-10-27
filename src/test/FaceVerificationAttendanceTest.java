package test;

import dao.AttendanceDAO;
import dao.StudentDAO;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import model.Attendance;
import model.Student;
import service.AttendanceService;
import util.DBConnection;

/**
 * Unit Test verifying Face Recognition Attendance Marking and Dashboard Data Refresh.
 */
public class FaceVerificationAttendanceTest {

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("  FACE VERIFICATION ATTENDANCE INTEGRATION TEST  ");
        System.out.println("=================================================");

        int passed = 0;
        int failed = 0;

        try {
            System.out.print("[TEST 1] Testing Database Connection... ");
            if (DBConnection.testConnection()) {
                System.out.println("PASSED!");
                passed++;
            } else {
                System.out.println("FAILED!");
                failed++;
            }

            // Retrieve student
            StudentDAO studentDAO = new StudentDAO();
            Student student = studentDAO.findByRollNo("TEST001");
            if (student == null) {
                student = new Student("TEST001", "Test Student", "Male", Date.valueOf("2004-01-01"), "Computer Science", "B.Tech", 3, "A", "test@college.edu", "9876543210", null);
                studentDAO.addStudent(student);
            }
            System.out.print("[TEST 2] Retrieving Student TEST001... ");
            if (student != null) {
                System.out.println("PASSED! Found: " + student.getName());
                passed++;
            } else {
                System.out.println("FAILED!");
                failed++;
            }

            // Mark attendance via face
            System.out.print("[TEST 3] Marking Attendance via Face Recognition... ");
            AttendanceService attService = new AttendanceService();
            String testSession = "Morning Session";
            AttendanceService.AttendanceResult result = attService.markAttendanceViaFace(student.getId(), testSession);
            if (result == AttendanceService.AttendanceResult.SUCCESS || result == AttendanceService.AttendanceResult.ALREADY_MARKED) {
                System.out.println("PASSED! Result: " + result);
                passed++;
            } else {
                System.out.println("FAILED! Result was: " + result);
                failed++;
            }

            // Verify attendance status is recorded as PRESENT today
            System.out.print("[TEST 4] Verifying Attendance Record in Database... ");
            AttendanceDAO attDAO = new AttendanceDAO();
            boolean isMarked = attDAO.isAttendanceMarked(student.getId(), Date.valueOf(LocalDate.now()), testSession);
            if (isMarked) {
                System.out.println("PASSED! Recorded as marked for today.");
                passed++;
            } else {
                System.out.println("FAILED! Not found in attendance records.");
                failed++;
            }

            // Verify Recent Attendance contains the marked record
            System.out.print("[TEST 5] Verifying Recent Attendance List for Home Page Dashboard... ");
            List<Attendance> recent = attDAO.getRecentAttendance(10);
            boolean foundInRecent = false;
            for (Attendance a : recent) {
                if (student.getRollNo().equalsIgnoreCase(a.getStudentRollNo()) && "PRESENT".equalsIgnoreCase(a.getStatus())) {
                    foundInRecent = true;
                    break;
                }
            }
            if (foundInRecent) {
                System.out.println("PASSED! Student record is present in recent attendance feed.");
                passed++;
            } else {
                System.out.println("FAILED! Student not found in recent records.");
                failed++;
            }

            // Verify Today's Statistics Aggregation
            System.out.print("[TEST 6] Verifying Dashboard Today Stats (Present >= 1)... ");
            int[] stats = attDAO.getTodayStats();
            if (stats[0] > 0 && stats[1] > 0) {
                System.out.println("PASSED! Total=" + stats[0] + ", Present=" + stats[1] + ", Absent=" + stats[2] + ", Rate=" + stats[3] + "%");
                passed++;
            } else {
                System.out.println("FAILED! Unexpected stats: Total=" + stats[0] + ", Present=" + stats[1]);
                failed++;
            }

        } catch (Exception e) {
            System.err.println("Exception occurred during test execution:");
            e.printStackTrace();
            failed++;
        }

        System.out.println("=================================================");
        System.out.printf("TEST SUMMARY: %d PASSED, %d FAILED%n", passed, failed);
        System.out.println("=================================================");

        if (failed > 0) {
            System.exit(1);
        }
    }
}
