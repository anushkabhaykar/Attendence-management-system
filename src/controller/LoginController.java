package controller;

import java.sql.SQLException;
import javax.swing.JOptionPane;
import model.User;
import service.AuthenticationService;
import view.DashboardFrame;
import view.LoginFrame;

/**
 * Controller managing user authentication workflow and initial dashboard transition.
 * Location: src/controller/LoginController.java
 */
public class LoginController {

    private final LoginFrame view;
    private final AuthenticationService authService;

    public LoginController(LoginFrame view) {
        this.view = view;
        this.authService = AuthenticationService.getInstance();
    }

    public void handleLogin(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            JOptionPane.showMessageDialog(view, "Please enter your username.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (password == null || password.trim().isEmpty()) {
            JOptionPane.showMessageDialog(view, "Please enter your password.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            User user = authService.login(username.trim(), password);
            if (user != null) {
                // Successful login
                view.dispose();
                DashboardFrame dashboard = new DashboardFrame();
                dashboard.setVisible(true);
            } else {
                JOptionPane.showMessageDialog(view, "Invalid username or password.", "Authentication Failed", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            String advice = util.DBConnection.isSqlite()
                    ? "Please check file write permissions for the project directory."
                    : "Please ensure MySQL server is running in XAMPP.";
            JOptionPane.showMessageDialog(view,
                    "Database Connection Failed!\n" + advice + "\nError: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean handleSignUp(String fullName, String username, String password, String confirmPassword, String role) {
        if (fullName == null || fullName.trim().isEmpty()) {
            JOptionPane.showMessageDialog(view, "Please enter your full name.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (username == null || username.trim().isEmpty() || username.trim().length() < 3) {
            JOptionPane.showMessageDialog(view, "Username must be at least 3 characters long.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (password == null || password.trim().length() < 4) {
            JOptionPane.showMessageDialog(view, "Password must be at least 4 characters long.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(view, "Passwords do not match.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        try {
            dao.UserDAO userDAO = new dao.UserDAO();
            if (userDAO.findByUsername(username.trim()) != null) {
                JOptionPane.showMessageDialog(view, "Username '" + username + "' is already taken. Please choose another.", "Duplicate Username", JOptionPane.WARNING_MESSAGE);
                return false;
            }

            String hash = util.PasswordUtil.hashPassword(password);
            User newUser = new User(username.trim(), hash, fullName.trim(), role != null ? role : "TEACHER");
            boolean created = userDAO.createUser(newUser);
            if (created) {
                JOptionPane.showMessageDialog(view, "Account created successfully for " + fullName + "!\nYou can now log in.", "Sign Up Successful", JOptionPane.INFORMATION_MESSAGE);
                return true;
            } else {
                JOptionPane.showMessageDialog(view, "Failed to create account. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(view, "Database error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
}

