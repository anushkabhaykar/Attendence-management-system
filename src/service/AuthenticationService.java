package service;

import dao.UserDAO;
import java.sql.SQLException;
import model.User;
import util.PasswordUtil;

/**
 * Service managing user authentication, role authorization, and session state.
 * Location: src/service/AuthenticationService.java
 */
public class AuthenticationService {

    private static AuthenticationService instance;
    private final UserDAO userDAO;
    private User currentUser;

    private AuthenticationService() {
        this.userDAO = new UserDAO();
    }

    public static synchronized AuthenticationService getInstance() {
        if (instance == null) {
            instance = new AuthenticationService();
        }
        return instance;
    }

    /**
     * Authenticates a user with username and password.
     * @return User object on success, or null on invalid credentials
     * @throws SQLException on database errors
     */
    public User login(String username, String plainPassword) throws SQLException {
        if (username == null || plainPassword == null || username.trim().isEmpty()) {
            return null;
        }

        User user = userDAO.findByUsername(username.trim());
        if (user != null) {
            boolean valid = PasswordUtil.verifyPassword(plainPassword, user.getPasswordHash());
            if (valid) {
                this.currentUser = user;
                return user;
            }
        }
        return null;
    }

    public void logout() {
        this.currentUser = null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public boolean isAdmin() {
        return currentUser != null && currentUser.isAdmin();
    }
}
