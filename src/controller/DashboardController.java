package controller;

import java.sql.SQLException;
import java.util.List;
import javax.swing.JOptionPane;
import model.Attendance;
import service.AttendanceService;
import service.AuthenticationService;
import view.DashboardFrame;
import view.LoginFrame;

/**
 * Controller managing dashboard statistics, live metrics, and view transitions.
 * Location: src/controller/DashboardController.java
 */
public class DashboardController {

    private final DashboardFrame view;
    private final AttendanceService attendanceService;
    private final AuthenticationService authService;

    public DashboardController(DashboardFrame view) {
        this.view = view;
        this.attendanceService = new AttendanceService();
        this.authService = AuthenticationService.getInstance();
    }

    public void loadDashboardData() {
        try {
            int[] stats = attendanceService.getTodayStats();
            view.updateMetrics(stats[0], stats[1], stats[2], stats[3]);

            List<Attendance> recentList = attendanceService.getRecentAttendance(10);
            view.updateRecentTable(recentList);
        } catch (SQLException e) {
            System.err.println("[DashboardController] Error loading stats: " + e.getMessage());
        }
    }

    public void handleLogout() {
        int confirm = JOptionPane.showConfirmDialog(view,
                "Are you sure you want to log out?",
                "Confirm Logout", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            authService.logout();
            view.dispose();
            new LoginFrame().setVisible(true);
        }
    }
}
